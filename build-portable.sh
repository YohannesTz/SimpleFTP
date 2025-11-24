#!/bin/bash

# Build Portable JAR Script
# Creates a single executable JAR with all dependencies included

echo "========================================="
echo "Building Portable FTP Server JAR"
echo "========================================="
echo ""

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean --quiet

if [ $? -ne 0 ]; then
    echo "❌ Clean failed. Please check the error messages above."
    exit 1
fi

echo "✅ Clean successful"
echo ""

# Build the shadow JAR (fat JAR with all dependencies)
echo "Building portable JAR with all dependencies..."
./gradlew shadowJar

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi

echo ""
echo "========================================="
echo "✅ Build Successful!"
echo "========================================="
echo ""

# Find the generated JAR
JAR_FILE=$(find build/libs -name "*-portable.jar" | head -n 1)

if [ -n "$JAR_FILE" ]; then
    JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
    echo "📦 Portable JAR created:"
    echo "   Location: $JAR_FILE"
    echo "   Size: $JAR_SIZE"
    echo ""
    echo "To run the portable JAR:"
    echo "   java -jar $JAR_FILE"
    echo ""
    echo "Or copy the JAR to any system with Java installed:"
    echo "   cp $JAR_FILE /path/to/destination/"
    echo "   java -jar SimpleFTPServer-portable-1.0.jar"
else
    echo "⚠️  Warning: Could not find the generated JAR file"
    echo "   Please check the build/libs directory"
fi

echo ""
echo "========================================="

