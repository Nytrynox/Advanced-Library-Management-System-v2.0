# 🚀 Quick Start Scripts

## Run the Complete Application

### `run-all.sh` - One Command to Rule Them All! 

This script automatically:
- ✅ Checks and starts MongoDB
- ✅ Builds the Java backend with Maven
- ✅ Starts the Java backend API (port 8080)
- ✅ Starts the Python AI service (if configured)
- ✅ Starts the Python frontend (port 5001)
- ✅ Opens your browser automatically to http://localhost:5001
- ✅ Creates detailed logs in the `logs/` directory
- ✅ Manages all processes with graceful shutdown

### Usage

```bash
# Navigate to the project directory
cd LibraryManagementSystem

# Run the complete application
./run-all.sh
```

The script will:
1. Show a nice startup interface with progress
2. Start all services in the correct order
3. Wait for each service to be ready
4. Open your default browser
5. Display a status dashboard
6. Keep running until you press Ctrl+C

### Stopping the Application

Press `Ctrl+C` in the terminal where `run-all.sh` is running, or use the stop script:

```bash
./stop-all.sh
```

## Script Features

### 🎨 Color-Coded Output
- **Green**: Success messages
- **Yellow**: Warnings
- **Red**: Errors
- **Blue**: Information

### 📝 Comprehensive Logging
All logs are saved to the `logs/` directory:
- `startup_TIMESTAMP.log` - Complete startup log
- `backend.log` - Java backend logs
- `frontend.log` - Python frontend logs
- `ai-service.log` - AI service logs (if running)
- `mongodb.log` - MongoDB logs (if started by script)

### 🔧 Smart Features
- **Port checking**: Verifies if services are already running
- **Automatic waiting**: Waits for each service to be ready before starting the next
- **Process tracking**: Tracks all PIDs for clean shutdown
- **Error handling**: Stops if critical services fail to start
- **Graceful cleanup**: Properly stops all services on exit

### 🖥️ Service URLs

Once started, access the application at:
- **Frontend**: http://localhost:5001
- **Backend API**: http://localhost:8080/api
- **Analytics Dashboard**: http://localhost:5001/analytics
- **MongoDB**: localhost:27017

## Prerequisites

Make sure you have installed:
- ✅ Java 11 or higher
- ✅ Maven 3.6+
- ✅ Python 3.8+
- ✅ MongoDB
- ✅ pip (Python package manager)

### Installing MongoDB on macOS

```bash
# Using Homebrew
brew tap mongodb/brew
brew install mongodb-community
brew services start mongodb-community
```

## Troubleshooting

### MongoDB won't start
```bash
# Check if MongoDB is installed
brew services list | grep mongodb

# Start MongoDB manually
brew services start mongodb-community

# Or run mongod directly
mkdir -p ./data/db
mongod --dbpath ./data/db
```

### Port already in use
If you see "port already in use" errors:
```bash
# Check what's using the port
lsof -i :5001  # Frontend
lsof -i :8080  # Backend

# Kill the process if needed
kill -9 <PID>
```

### Maven build fails
```bash
# Clean and rebuild
cd java-backend
mvn clean install
```

### Python dependencies issues
```bash
# Create virtual environment
python -m venv venv
source venv/bin/activate  # On macOS/Linux
# venv\Scripts\activate   # On Windows

# Install dependencies
cd python-frontend
pip install -r requirements.txt
```

## Development Mode

For development with auto-reload:
```bash
# Backend (in java-backend directory)
mvn spring-boot:run

# Frontend (in python-frontend directory)
export FLASK_ENV=development
python app.py
```

## Logs Location

All logs are stored in the `logs/` directory:
```
LibraryManagementSystem/
├── logs/
│   ├── startup_YYYYMMDD_HHMMSS.log
│   ├── backend.log
│   ├── frontend.log
│   ├── ai-service.log
│   └── app.pids
```

## Clean Restart

For a completely fresh start:
```bash
# Stop everything
./stop-all.sh

# Clean build artifacts
cd java-backend && mvn clean && cd ..

# Remove logs (optional)
rm -rf logs/

# Start again
./run-all.sh
```

## Notes

- The script uses port 5001 for the frontend (not 5000 to avoid conflicts with macOS AirPlay)
- MongoDB is not stopped by default when you stop the app (as it might be used by other applications)
- All Python dependencies are automatically installed when starting the frontend
- The Java backend is rebuilt every time to ensure latest changes are included

## Support

If you encounter issues:
1. Check the logs in the `logs/` directory
2. Ensure all prerequisites are installed
3. Verify MongoDB is running: `brew services list | grep mongodb`
4. Check if ports 5001, 8080, and 27017 are available

Enjoy your Advanced Library Management System! 📚✨
