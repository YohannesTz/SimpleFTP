#!/bin/bash

# Simple FTP Server Run Script
# This script builds and runs the FTP server application

echo "Starting Simple FTP Server..."
echo ""

# Build the project
echo "Building project..."
./gradlew build --quiet

if [ $? -ne 0 ]; then
    echo "Build failed. Please check the error messages above."
    exit 1
fi

echo "Build successful!"
echo ""

# Run the application
echo "Launching FTP Server UI..."
./gradlew run --quiet --console=plain

