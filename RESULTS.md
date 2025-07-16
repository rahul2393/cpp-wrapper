# C++ Cache Performance Benchmark Results

## Overview
This project benchmarks the performance of calling C++ cache operations from Go (via CGO) and Java (via JNI), comparing them against native Go and Java implementations. The C++ library provides ordered map (std::map), hash map (std::unordered_map), and simple data storage functionality.

## Architecture
- **C++ Core**: Shared library with ordered map, hash map, and data storage
- **Go Wrapper**: CGO bindings to the C++ library
- **Java Wrapper**: JNI implementation to call the C++ library
- **Native Implementations**: Pure Go and Java cache implementations for comparison
- **Note**: Protobuf was temporarily removed due to Abseil linking issues on macOS

## Benchmark Results

### Test Configuration
- **Population**: 10,000 key-value pairs in both ordered and hash maps
- **Lookup Tests**: 100,000 iterations of map lookups
- **Data Tests**: 10,000 iterations of 1KB data retrieval operations
- **Platform**: macOS (Apple Silicon M1)

### Performance Comparison

#### C++-Backed Implementations (CGO/JNI)

| Operation | Go (CGO) | Java (JNI) | Difference |
|-----------|----------|------------|------------|
| **Ordered Map Lookup** | 1221.94 ns | 974.40 ns | Java ~20.3% faster |
| **Hash Map Lookup** | 636.75 ns | 203.27 ns | Java ~68.1% faster |
| **Data Retrieval** | 1550 ns | 690 ns | Java ~55.5% faster |

#### Native Implementations

| Operation | Go (Native) | Java (Native) | Difference |
|-----------|-------------|---------------|------------|
| **Ordered Map Lookup** | 531.71 ns | 112.92 ns | Java ~79.8% faster |
| **Hash Map Lookup** | 127.24 ns | 53.53 ns | Java ~57.9% faster |
| **Data Retrieval** | 6 ns | 56 ns | Go ~89.3% faster |

#### Native vs C++-Backed Performance

| Operation | Go Native vs CGO | Java Native vs JNI |
|-----------|------------------|-------------------|
| **Ordered Map Lookup** | Native ~2.3x faster | Native ~8.6x faster |
| **Hash Map Lookup** | Native ~5.0x faster | Native ~3.8x faster |
| **Data Retrieval** | Native ~258x faster | Native ~12.3x faster |

### Key Observations

1. **Native Performance**: Both Go and Java native implementations significantly outperform their C++-backed counterparts.

2. **C++ Overhead**: The CGO/JNI overhead is substantial, especially for simple operations like data retrieval.

3. **Language Comparison**: 
   - Java's native collections are generally faster than Go's for map operations
   - Go's native data retrieval is exceptionally fast (6 ns vs Java's 56 ns)

4. **Hash Map Performance**: Both implementations show excellent performance with hash map lookups being significantly faster than ordered map lookups.

5. **JNI vs CGO Overhead**: 
   - Java JNI shows better performance than Go CGO for C++-backed operations
   - The performance gap between native and C++-backed is larger for Java

## Technical Notes

### C++ Library Features
- **Ordered Map**: std::map for sorted key-value storage
- **Hash Map**: std::unordered_map for O(1) lookups  
- **Data Storage**: Simple string-based storage for 1KB data blocks
- **Timing**: Nanosecond-precision lookup timing

### Language Integration
- **Go**: Uses CGO with C-style interface (`cache_c.h`)
- **Java**: Uses JNI with native method declarations
- **Memory Management**: Simplified string handling for prototype

### Build System
- **C++**: CMake-based build system
- **Go**: Standard Go modules with CGO
- **Java**: Maven-based build with JNI compilation

## Conclusion

The benchmark results clearly demonstrate that:

1. **Native implementations are dramatically faster** than C++-backed ones for simple caching operations
2. **CGO/JNI overhead is substantial** and should be considered when choosing between native and C++ implementations
3. **Java's native collections** generally outperform Go's for map operations
4. **For pure caching needs**, native implementations are the clear choice
5. **C++ integration should be reserved** for cases where specific C++ features or libraries are required

The choice between native and C++-backed implementations should be based on:
- **Performance requirements**: Native implementations are significantly faster
- **C++ feature needs**: Use C++ backend only if specific C++ libraries or features are required
- **Development complexity**: Native implementations are simpler to develop and maintain

## Future Enhancements
1. **Protobuf Integration**: Resolve Abseil linking issues for full protobuf support
2. **Memory Management**: Implement proper memory management for production use
3. **Error Handling**: Add comprehensive error handling and validation
4. **Thread Safety**: Add thread safety considerations for concurrent access
5. **More Complex Operations**: Benchmark more complex C++ operations to justify the overhead 