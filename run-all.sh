#!/bin/bash

###############################################################################
# Advanced Library Management System - Complete Launcher
# This script starts all components and opens the application in browser
###############################################################################

set -e  # Exit on error

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
FRONTEND_PORT=5001
BACKEND_PORT=8080
AI_SERVICE_PORT=5002
MONGODB_PORT=27017
FRONTEND_URL="http://localhost:${FRONTEND_PORT}"

# Log file
LOG_DIR="$SCRIPT_DIR/logs"
mkdir -p "$LOG_DIR"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
LOG_FILE="$LOG_DIR/startup_${TIMESTAMP}.log"

# Create log file
touch "$LOG_FILE"

# PID file to track processes
PID_FILE="$LOG_DIR/app.pids"

###############################################################################
# Helper Functions
###############################################################################

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1" | tee -a "$LOG_FILE"
}

log_warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1" | tee -a "$LOG_FILE"
}

log_info() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] INFO:${NC} $1" | tee -a "$LOG_FILE"
}

# Cleanup function
cleanup() {
    log_warn "Shutting down all services..."
    
    if [ -f "$PID_FILE" ]; then
        while read -r pid; do
            if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
                log "Stopping process $pid"
                kill "$pid" 2>/dev/null || true
            fi
        done < "$PID_FILE"
        rm "$PID_FILE"
    fi
    
    log "Cleanup complete"
    exit 0
}

# Set trap to cleanup on exit
trap cleanup EXIT INT TERM

# Check if port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        return 0
    else
        return 1
    fi
}

# Wait for service to be ready
wait_for_service() {
    local host=$1
    local port=$2
    local service_name=$3
    local max_attempts=30
    local attempt=1
    
    log_info "Waiting for $service_name to start on $host:$port..."
    
    while [ $attempt -le $max_attempts ]; do
        if check_port $port; then
            log "✓ $service_name is ready!"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    log_error "$service_name failed to start within expected time"
    return 1
}

# Check if MongoDB is running
check_mongodb() {
    if check_port $MONGODB_PORT; then
        log "✓ MongoDB is already running on port $MONGODB_PORT"
        return 0
    else
        log_warn "MongoDB is not running on port $MONGODB_PORT"
        return 1
    fi
}

# Start MongoDB
start_mongodb() {
    log "Checking MongoDB status..."
    
    if check_mongodb; then
        return 0
    fi
    
    log "Starting MongoDB..."
    
    # Try to start MongoDB using brew services (macOS)
    if command -v brew > /dev/null 2>&1; then
        if brew services list | grep -q mongodb-community; then
            brew services start mongodb-community >> "$LOG_FILE" 2>&1 || true
            sleep 3
            if check_mongodb; then
                return 0
            fi
        fi
    fi
    
    # Try to start MongoDB directly
    if command -v mongod > /dev/null 2>&1; then
        log_info "Attempting to start mongod directly..."
        mkdir -p "$SCRIPT_DIR/data/db"
        
        # Try to start without fork first to see if there's already a lock
        if mongod --dbpath "$SCRIPT_DIR/data/db" --fork --logpath "$LOG_DIR/mongodb.log" 2>&1 | grep -q "already"; then
            log_warn "MongoDB data directory may be locked. Checking if MongoDB is already running..."
            sleep 2
            if check_mongodb; then
                log "MongoDB was already running"
                return 0
            fi
        fi
        
        sleep 3
        if check_mongodb; then
            return 0
        fi
    fi
    
    # One final check - maybe it started despite the error message
    log_info "Performing final MongoDB check..."
    sleep 2
    if check_mongodb; then
        log "MongoDB is now running"
        return 0
    fi
    
    log_error "Failed to start MongoDB. Please start it manually."
    log_info "You can start MongoDB using: brew services start mongodb-community"
    log_info "Or manually: mongod --dbpath ./data/db"
    
    # Ask user if they want to continue anyway
    echo ""
    read -p "MongoDB is not running. Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log_warn "Continuing without MongoDB..."
        return 0
    fi
    
    return 1
}

# Build Java backend
build_java_backend() {
    log "Building Java backend..."
    
    cd java-backend
    
    if [ ! -f "pom.xml" ]; then
        log_error "pom.xml not found in java-backend directory"
        return 1
    fi
    
    # Check if Maven is installed
    if ! command -v mvn > /dev/null 2>&1; then
        log_error "Maven is not installed. Please install Maven first."
        return 1
    fi
    
    log_info "Running Maven clean package..."
    mvn clean package -DskipTests >> "$LOG_FILE" 2>&1
    
    if [ $? -eq 0 ]; then
        log "✓ Java backend built successfully"
        cd "$SCRIPT_DIR"
        return 0
    else
        log_error "Failed to build Java backend"
        cd "$SCRIPT_DIR"
        return 1
    fi
}

# Start Java backend
start_java_backend() {
    log "Starting Java backend on port $BACKEND_PORT..."
    
    cd java-backend
    
    # Find the JAR file
    JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" | head -1)
    
    if [ -z "$JAR_FILE" ]; then
        log_error "No JAR file found in target directory"
        cd "$SCRIPT_DIR"
        return 1
    fi
    
    log_info "Starting $JAR_FILE"
    java -jar "$JAR_FILE" > "$LOG_DIR/backend.log" 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID >> "$PID_FILE"
    
    cd "$SCRIPT_DIR"
    
    # Wait for backend to start
    if wait_for_service "localhost" $BACKEND_PORT "Java Backend"; then
        log "✓ Java backend started successfully (PID: $BACKEND_PID)"
        return 0
    else
        log_error "Java backend failed to start"
        return 1
    fi
}

# Start Python AI Service (optional)
start_ai_service() {
    log "Checking for Python AI service..."
    
    if [ ! -d "python-ai-service" ]; then
        log_info "Python AI service directory not found, skipping..."
        return 0
    fi
    
    cd python-ai-service
    
    if [ ! -f "app.py" ] || [ ! -s "app.py" ]; then
        log_info "Python AI service not configured, skipping..."
        cd "$SCRIPT_DIR"
        return 0
    fi
    
    log "Starting Python AI service on port $AI_SERVICE_PORT..."
    
    # Check if virtual environment exists
    if [ -d "venv" ]; then
        source venv/bin/activate
    elif [ -d "../venv" ]; then
        source ../venv/bin/activate
    else
        # Create a virtual environment if it doesn't exist
        log_info "Creating virtual environment for AI service..."
        python3 -m venv venv
        source venv/bin/activate
    fi
    
    # Install requirements if needed
    if [ -f "requirements.txt" ]; then
        pip install -q -r requirements.txt >> "$LOG_FILE" 2>&1
    fi
    
    python app.py > "$LOG_DIR/ai-service.log" 2>&1 &
    AI_PID=$!
    echo $AI_PID >> "$PID_FILE"
    
    cd "$SCRIPT_DIR"
    
    log "✓ Python AI service started (PID: $AI_PID)"
    return 0
}

# Start Python frontend
start_python_frontend() {
    log "Starting Python frontend on port $FRONTEND_PORT..."
    
    cd python-frontend
    
    if [ ! -f "app.py" ]; then
        log_error "app.py not found in python-frontend directory"
        cd "$SCRIPT_DIR"
        return 1
    fi
    
    # Check if virtual environment exists
    if [ -d "venv" ]; then
        source venv/bin/activate
    elif [ -d "../venv" ]; then
        source ../venv/bin/activate
    else
        # Create a virtual environment if it doesn't exist
        log_info "Creating virtual environment..."
        python3 -m venv venv
        source venv/bin/activate
    fi
    
    # Install requirements if needed
    if [ -f "requirements.txt" ]; then
        log_info "Installing frontend dependencies..."
        pip install -q -r requirements.txt >> "$LOG_FILE" 2>&1
    fi
    
    python app.py > "$LOG_DIR/frontend.log" 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID >> "$PID_FILE"
    
    cd "$SCRIPT_DIR"
    
    # Wait for frontend to start
    if wait_for_service "localhost" $FRONTEND_PORT "Python Frontend"; then
        log "✓ Python frontend started successfully (PID: $FRONTEND_PID)"
        return 0
    else
        log_error "Python frontend failed to start"
        return 1
    fi
}

# Open browser
open_browser() {
    log "Opening browser at $FRONTEND_URL..."
    
    # Wait a moment for everything to stabilize
    sleep 2
    
    # Open browser based on OS
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        open "$FRONTEND_URL"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Linux
        xdg-open "$FRONTEND_URL" 2>/dev/null || sensible-browser "$FRONTEND_URL" 2>/dev/null || true
    elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]]; then
        # Windows
        start "$FRONTEND_URL"
    fi
    
    log "✓ Browser opened"
}

# Display status
display_status() {
    echo ""
    echo -e "${GREEN}╔════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║     Advanced Library Management System - Running Status       ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${BLUE}📊 Services Status:${NC}"
    echo -e "  • MongoDB:         Running on port $MONGODB_PORT"
    echo -e "  • Java Backend:    Running on port $BACKEND_PORT"
    echo -e "  • Python Frontend: Running on port $FRONTEND_PORT"
    echo ""
    echo -e "${BLUE}🌐 Access Points:${NC}"
    echo -e "  • Frontend:        ${GREEN}$FRONTEND_URL${NC}"
    echo -e "  • Backend API:     http://localhost:$BACKEND_PORT/api"
    echo -e "  • Analytics:       $FRONTEND_URL/analytics"
    echo ""
    echo -e "${BLUE}📝 Logs:${NC}"
    echo -e "  • Combined Log:    $LOG_FILE"
    echo -e "  • Backend Log:     $LOG_DIR/backend.log"
    echo -e "  • Frontend Log:    $LOG_DIR/frontend.log"
    echo ""
    echo -e "${YELLOW}⚠️  Press Ctrl+C to stop all services${NC}"
    echo ""
    echo -e "${GREEN}════════════════════════════════════════════════════════════════${NC}"
    echo ""
}

###############################################################################
# Main Execution
###############################################################################

main() {
    clear
    echo ""
    echo -e "${GREEN}╔════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║   Advanced Library Management System - Complete Launcher      ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    log "Starting Advanced Library Management System..."
    log "Log file: $LOG_FILE"
    echo ""
    
    # Initialize PID file
    echo -n > "$PID_FILE"
    
    # Step 1: Start MongoDB
    if ! start_mongodb; then
        log_error "Cannot proceed without MongoDB"
        exit 1
    fi
    echo ""
    
    # Step 2: Build and start Java backend
    if ! build_java_backend; then
        log_error "Failed to build Java backend"
        exit 1
    fi
    echo ""
    
    if ! start_java_backend; then
        log_error "Failed to start Java backend"
        exit 1
    fi
    echo ""
    
    # Step 3: Start Python AI service (optional)
    start_ai_service
    echo ""
    
    # Step 4: Start Python frontend
    if ! start_python_frontend; then
        log_error "Failed to start Python frontend"
        exit 1
    fi
    echo ""
    
    # Step 5: Open browser
    open_browser
    echo ""
    
    # Display status
    display_status
    
    # Keep script running
    log "All services started successfully!"
    log "Application is ready to use at $FRONTEND_URL"
    log "Press Ctrl+C to stop all services"
    
    # Wait indefinitely
    while true; do
        sleep 1
    done
}

# Run main function
main
