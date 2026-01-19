#!/bin/bash

###############################################################################
# Quick Start Script - No Maven Required
###############################################################################

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
FRONTEND_PORT=5001
BACKEND_PORT=8080
LOG_DIR="$SCRIPT_DIR/logs"
mkdir -p "$LOG_DIR"
PID_FILE="$LOG_DIR/app.pids"

echo -e "${GREEN}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║   Advanced Library Management System - Quick Start    ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# Initialize PID file
echo -n > "$PID_FILE"

# Cleanup function
cleanup() {
    echo -e "\n${YELLOW}Shutting down all services...${NC}"
    if [ -f "$PID_FILE" ]; then
        while read -r pid; do
            if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
                echo "Stopping process $pid"
                kill "$pid" 2>/dev/null || true
            fi
        done < "$PID_FILE"
        rm "$PID_FILE"
    fi
    echo -e "${GREEN}Cleanup complete${NC}"
    exit 0
}

trap cleanup EXIT INT TERM

# Check port
check_port() {
    lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null 2>&1
}

# Wait for service
wait_for_service() {
    local port=$1
    local name=$2
    local max_attempts=30
    
    echo -n "Waiting for $name to start"
    for i in $(seq 1 $max_attempts); do
        if check_port $port; then
            echo -e " ${GREEN}✓${NC}"
            return 0
        fi
        echo -n "."
        sleep 2
    done
    echo -e " ${YELLOW}⚠${NC}"
    return 1
}

# Start Java Backend
echo -e "${BLUE}[1/2]${NC} Starting Java Backend..."
cd java-backend

JAR_FILE="target/library-management-system-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${YELLOW}Warning: JAR file not found at $JAR_FILE${NC}"
    # Try to find any JAR file
    JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" -not -name "*original*" 2>/dev/null | head -1)
    if [ -z "$JAR_FILE" ]; then
        echo -e "${YELLOW}Error: No JAR file found. Please build the project first with: mvn clean package${NC}"
        exit 1
    fi
fi

# Use Homebrew Java if available
if [ -d "/opt/homebrew/opt/openjdk@17" ]; then
    export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
    export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
fi

java -jar "$JAR_FILE" > "$LOG_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID >> "$PID_FILE"

cd "$SCRIPT_DIR"

if wait_for_service $BACKEND_PORT "Backend"; then
    echo -e "  Backend running on port ${GREEN}$BACKEND_PORT${NC} (PID: $BACKEND_PID)"
else
    echo -e "  ${YELLOW}Backend may still be starting...${NC}"
fi

echo ""

# Start Python Frontend
echo -e "${BLUE}[2/2]${NC} Starting Python Frontend..."
cd python-frontend

# Check for virtual environment
if [ -d "venv" ]; then
    source venv/bin/activate
elif [ -d "../venv" ]; then
    source ../venv/bin/activate
else
    echo "  Creating virtual environment..."
    python3 -m venv venv
    source venv/bin/activate
fi

# Install requirements
if [ -f "requirements.txt" ]; then
    echo "  Installing dependencies..."
    pip install -q -r requirements.txt
fi

python app.py > "$LOG_DIR/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo $FRONTEND_PID >> "$PID_FILE"

cd "$SCRIPT_DIR"

if wait_for_service $FRONTEND_PORT "Frontend"; then
    echo -e "  Frontend running on port ${GREEN}$FRONTEND_PORT${NC} (PID: $FRONTEND_PID)"
fi

echo ""

# Display status
echo -e "${GREEN}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║              🎉 System Ready!                          ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}🌐 Access Points:${NC}"
echo -e "  • Frontend:     ${GREEN}http://localhost:$FRONTEND_PORT${NC}"
echo -e "  • Backend API:  http://localhost:$BACKEND_PORT/api"
echo -e "  • Analytics:    http://localhost:$FRONTEND_PORT/analytics"
echo ""
echo -e "${BLUE}👥 Test Users:${NC}"
echo -e "  • Admin:   username: ${GREEN}admin${NC}   password: ${GREEN}admin123${NC}"
echo -e "  • User 1:  username: ${GREEN}alice${NC}   password: ${GREEN}alice123${NC}"
echo -e "  • User 2:  username: ${GREEN}bob${NC}     password: ${GREEN}bob123${NC}"
echo ""
echo -e "${BLUE}📚 Database Info:${NC}"
echo -e "  • Books: 10 | Users: 5 | Categories: 10 | Loans: 2"
echo ""
echo -e "${YELLOW}⚠️  Press Ctrl+C to stop all services${NC}"
echo ""

# Open browser
sleep 2
if [[ "$OSTYPE" == "darwin"* ]]; then
    open "http://localhost:$FRONTEND_PORT"
fi

# Keep running
while true; do
    sleep 1
done
