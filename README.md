# C++ Cache Performance Benchmark

This project benchmarks the performance of calling C++ cache operations from Go (via CGO) and Java (via JNI).

## Project Structure

```
.
├── proto/           # Protobuf definitions
├── cpp/            # C++ shared library with cache implementation
├── go/             # Go wrapper using CGO
├── java/           # Java wrapper using JNI
└── build.sh        # Build and run script
```

## Features

The C++ cache library provides:
- **Ordered Map**: std::map for sorted key-value storage
- **Hash Map**: std::unordered_map for O(1) lookups
- **Proto Support**: 1KB protobuf message handling
- **Performance Timing**: Nanosecond-precision lookup timing

## Prerequisites

- **C++**: CMake 3.16+, C++17 compiler, Protobuf
- **Go**: Go 1.21+
- **Java**: Java 11+, Maven, JNI headers
- **Protobuf**: protobuf-compiler

### macOS Installation
```bash
brew install cmake protobuf go maven
```

### Ubuntu/Debian Installation
```bash
sudo apt-get install cmake libprotobuf-dev protobuf-compiler golang-go maven openjdk-11-jdk
```

## Quick Start

1. **Build and Run All Benchmarks**:
```bash
chmod +x build.sh
./build.sh
```

2. **Manual Build Steps**:

### Build C++ Library
```bash
cd cpp
mkdir build && cd build
cmake ..
make
cd ../..
```

### Build Go Benchmark
```bash
cd go
go mod tidy
go build -o cache_benchmark .
cd ..
```

### Build Java Benchmark
```bash
cd java
mkdir build && cd build
cmake ..
make
mvn clean compile
cd ../..
```

### Run Benchmarks
```bash
# Go
cd go
export LD_LIBRARY_PATH=../cpp/build/lib:$LD_LIBRARY_PATH
./cache_benchmark

# Java
cd java
export LD_LIBRARY_PATH=build/lib:$LD_LIBRARY_PATH
java -cp target/classes Main
```

## Benchmark Details

Each benchmark performs:
1. **Population**: 10,000 key-value pairs in both ordered and hash maps
2. **Proto Setup**: 1KB protobuf message storage
3. **Lookup Tests**: 100,000 iterations of map lookups
4. **Proto Tests**: 10,000 proto retrieval operations

## Expected Results

The benchmarks will output:
- Average ordered map lookup time (nanoseconds)
- Average hash map lookup time (nanoseconds)  
- Average proto retrieval time (nanoseconds)

## Architecture

### C++ Core (`cpp/`)
- `cache.h`: C-style interface for language bindings
- `cache.cpp`: Implementation with std::map, std::unordered_map
- `CMakeLists.txt`: Build configuration

### Go Wrapper (`go/`)
- `cache.go`: CGO bindings to C++ library
- `main.go`: Benchmark runner
- `go.mod`: Module dependencies

### Java Wrapper (`java/`)
- `Cache.java`: JNI interface class
- `CacheJNI.cpp`: JNI implementation
- `Main.java`: Benchmark runner
- `pom.xml`: Maven configuration

## Performance Considerations

- **CGO Overhead**: Go's CGO has minimal overhead for simple calls
- **JNI Overhead**: Java JNI has higher overhead due to JVM boundary crossing
- **Memory Management**: Simplified string handling for prototype
- **Timing Precision**: Nanosecond-level measurements for accurate comparison

## Troubleshooting

### Library Not Found
```bash
export LD_LIBRARY_PATH=/path/to/library:$LD_LIBRARY_PATH
```

### Protobuf Issues
```bash
# Regenerate protobuf files
protoc --cpp_out=cpp proto/cache.proto
```

### JNI Issues
```bash
# Ensure JAVA_HOME is set
export JAVA_HOME=/path/to/java
``` 