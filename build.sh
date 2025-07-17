#!/bin/bash

set -e

# Detect OS for library path and extensions
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    LIBRARY_PATH_VAR="DYLD_LIBRARY_PATH"
    LIBRARY_EXT="dylib"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux (Ubuntu)
    LIBRARY_PATH_VAR="LD_LIBRARY_PATH"
    LIBRARY_EXT="so"
else
    echo "Unsupported OS: $OSTYPE"
    exit 1
fi

# Clear previous benchmark results
rm -f benchmark_results.csv

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
# Set library path for dynamic library loading
export $LIBRARY_PATH_VAR=../cpp/build/lib:${!LIBRARY_PATH_VAR}
./cache_benchmark
cd ..

echo "Running Java Benchmark..."
cd java
# Set library path for JNI library loading
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # On Linux, include system library paths for libstdc++
    export $LIBRARY_PATH_VAR=../cpp/build/lib:/lib/x86_64-linux-gnu:/usr/lib/x86_64-linux-gnu:${!LIBRARY_PATH_VAR}
else
    export $LIBRARY_PATH_VAR=../cpp/build/lib:${!LIBRARY_PATH_VAR}
fi
# Run Java benchmark
java -Djava.library.path=build/lib -cp target/classes Main
cd ..

echo "Generating Performance Plots..."
if command -v python3 &> /dev/null; then
    python3 -c "import matplotlib.pyplot as plt" &> /dev/null || {
        echo "Installing matplotlib..."
        pip3 install matplotlib
    }
    python3 plot_results.py
else
    echo "Python3 not found. Skipping plot generation."
    echo "To generate plots, install Python3 and matplotlib, then run: python3 plot_results.py"
fi

echo "Benchmark completed!" 
echo "Results saved to benchmark_results.csv"
if [ -d "plots" ]; then
    echo "Performance plots saved to plots/ directory"
fi 