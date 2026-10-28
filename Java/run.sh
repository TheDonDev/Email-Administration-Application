#!/bin/bash
# Build and run the Email Administration Application
set -e

cd "$(dirname "$0")"

SRC="src/emailapp"
CLASSES="bin"
LIB="lib/sqlite-jdbc.jar"

echo "=== Email Administration Application ==="
echo "Building..."

mkdir -p "$CLASSES"

javac -cp "$LIB" -d "$CLASSES" $(find "$SRC" -name "*.java")

echo ""
echo "Build complete. Running application..."
echo ""

java -cp "$CLASSES:$LIB" emailapp.Main
