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
export LD_LIBRARY_PATH=../cpp/build/lib:$LD_LIBRARY_PATH
./cache_benchmark
cd ..

echo "Running Java Benchmark..."
cd java
export LD_LIBRARY_PATH=build/lib:$LD_LIBRARY_PATH
java -cp target/classes Main
cd ..

echo "Benchmark completed!" 