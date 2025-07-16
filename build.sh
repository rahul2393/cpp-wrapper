#!/bin/bash

set -e

echo "Building C++ Cache Library..."
cd cpp
mkdir -p build
cd build
cmake ..
make -j$(nproc)
cd ../..

echo "Building Java JNI Library..."
cd java
mkdir -p build
cd build
cmake ..
make -j$(nproc)
cd ../..

echo "Building Go Benchmark..."
cd go
go mod tidy
go build -o cache_benchmark .
cd ..

echo "Building Java Benchmark..."
cd java
mvn clean compile
cd ..

echo "Running Go Benchmark..."
cd go
# Set DYLD_LIBRARY_PATH for macOS dynamic library loading
export DYLD_LIBRARY_PATH=../cpp/build/lib:$DYLD_LIBRARY_PATH
./cache_benchmark
cd ..

echo "Running Java Benchmark..."
cd java
# Set java.library.path for JNI library loading
export DYLD_LIBRARY_PATH=../cpp/build/lib:$DYLD_LIBRARY_PATH
java -Djava.library.path=build/lib -cp target/classes Main
cd ..

echo "Benchmark completed!" 