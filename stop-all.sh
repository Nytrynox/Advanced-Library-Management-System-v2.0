#!/bin/bash

###############################################################################
# Advanced Library Management System - Stop Script
# This script stops all running services
###############################################################################

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
LOG_DIR="logs"
PID_FILE="$LOG_DIR/app.pids"

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1"
}

echo ""
echo -e "${YELLOW}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${YELLOW}║   Advanced Library Management System - Shutdown Script        ║${NC}"
echo -e "${YELLOW}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

log "Stopping all services..."

# Stop processes from PID file
if [ -f "$PID_FILE" ]; then
    log "Found PID file, stopping tracked processes..."
    
    while read -r pid; do
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            log "Stopping process $pid"
            kill "$pid" 2>/dev/null || true
            sleep 1
            
            # Force kill if still running
            if kill -0 "$pid" 2>/dev/null; then
                log_warn "Force stopping process $pid"
                kill -9 "$pid" 2>/dev/null || true
            fi
        fi
    done < "$PID_FILE"
    
    rm "$PID_FILE"
    log "✓ All tracked processes stopped"
else
    log_warn "No PID file found at $PID_FILE"
fi

# Stop any remaining Java processes (backend)
log "Checking for Java backend processes..."
JAVA_PIDS=$(pgrep -f "java.*library.*jar" || true)
if [ -n "$JAVA_PIDS" ]; then
    log "Stopping Java backend processes: $JAVA_PIDS"
    echo "$JAVA_PIDS" | xargs kill 2>/dev/null || true
    sleep 1
fi

# Stop Python frontend (port 5001)
log "Checking for Python frontend on port 5001..."
FRONTEND_PID=$(lsof -ti:5001 || true)
if [ -n "$FRONTEND_PID" ]; then
    log "Stopping Python frontend (PID: $FRONTEND_PID)"
    kill "$FRONTEND_PID" 2>/dev/null || true
fi

# Stop Python AI service (port 5002)
log "Checking for Python AI service on port 5002..."
AI_PID=$(lsof -ti:5002 || true)
if [ -n "$AI_PID" ]; then
    log "Stopping Python AI service (PID: $AI_PID)"
    kill "$AI_PID" 2>/dev/null || true
fi

# Stop Java backend (port 8080)
log "Checking for Java backend on port 8080..."
BACKEND_PID=$(lsof -ti:8080 || true)
if [ -n "$BACKEND_PID" ]; then
    log "Stopping Java backend (PID: $BACKEND_PID)"
    kill "$BACKEND_PID" 2>/dev/null || true
fi

# Optional: Stop MongoDB (commented out by default as it might be used by other apps)
# Uncomment if you want to stop MongoDB as well
# log_warn "MongoDB is still running. To stop it, run: brew services stop mongodb-community"

echo ""
log "✓ All services stopped successfully!"
echo ""
echo -e "${BLUE}Note: MongoDB is still running. To stop it manually, run:${NC}"
echo -e "  brew services stop mongodb-community"
echo ""
