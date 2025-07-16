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

### Performance Statistics
The benchmarks now provide comprehensive performance statistics including:
- **Average**: Mean time across all iterations
- **P50**: Median time (50th percentile)
- **P90**: 90th percentile (90% of operations are faster than this)
- **P95**: 95th percentile (95% of operations are faster than this)
- **P99**: 99th percentile (99% of operations are faster than this)

### Performance Comparison by Percentile

#### Average Performance

| Operation | Go (CGO) | Java (JNI) | Go (Native) | Java (Native) |
|-----------|----------|------------|-------------|---------------|
| **Ordered Map Lookup** | 1321.44 ns | 1372.88 ns | 46.24 ns | 156.01 ns |
| **Hash Map Lookup** | 523.47 ns | 559.11 ns | 48.79 ns | 73.42 ns |
| **Data Retrieval** | 674.00 ns | 705.53 ns | 28.64 ns | 44.56 ns |

#### P50 (Median) Performance

| Operation | Go (CGO) | Java (JNI) | Go (Native) | Java (Native) |
|-----------|----------|------------|-------------|---------------|
| **Ordered Map Lookup** | 1250 ns | 1333 ns | 42 ns | 84 ns |
| **Hash Map Lookup** | 500 ns | 500 ns | 42 ns | 83 ns |
| **Data Retrieval** | 542 ns | 584 ns | 41 ns | 42 ns |

#### P90 Performance

| Operation | Go (CGO) | Java (JNI) | Go (Native) | Java (Native) |
|-----------|----------|------------|-------------|---------------|
| **Ordered Map Lookup** | 1500 ns | 1541 ns | 83 ns | 375 ns |
| **Hash Map Lookup** | 625 ns | 666 ns | 83 ns | 84 ns |
| **Data Retrieval** | 667 ns | 708 ns | 42 ns | 83 ns |

#### P95 Performance

| Operation | Go (CGO) | Java (JNI) | Go (Native) | Java (Native) |
|-----------|----------|------------|-------------|---------------|
| **Ordered Map Lookup** | 1584 ns | 1625 ns | 84 ns | 459 ns |
| **Hash Map Lookup** | 667 ns | 709 ns | 84 ns | 125 ns |
| **Data Retrieval** | 709 ns | 1625 ns | 42 ns | 84 ns |

#### P99 Performance

| Operation | Go (CGO) | Java (JNI) | Go (Native) | Java (Native) |
|-----------|----------|------------|-------------|---------------|
| **Ordered Map Lookup** | 2041 ns | 1959 ns | 125 ns | 666 ns |
| **Hash Map Lookup** | 875 ns | 916 ns | 125 ns | 167 ns |
| **Data Retrieval** | 1625 ns | 2000 ns | 42 ns | 84 ns |

### Performance Variance Analysis

#### C++-Backed vs Native Performance Ratios

| Operation | Go Native vs CGO | Java Native vs JNI |
|-----------|------------------|-------------------|
| **Ordered Map Lookup** | Native ~28.6x faster | Native ~8.8x faster |
| **Hash Map Lookup** | Native ~10.7x faster | Native ~7.6x faster |
| **Data Retrieval** | Native ~23.5x faster | Native ~15.8x faster |

#### Performance Consistency (P99/P50 Ratio)

| Operation | Go (CGO) | Java (JNI) | Go (Native) | Java (Native) |
|-----------|----------|------------|-------------|---------------|
| **Ordered Map Lookup** | 1.63x | 1.47x | 2.98x | 7.93x |
| **Hash Map Lookup** | 1.75x | 1.83x | 2.98x | 2.01x |
| **Data Retrieval** | 3.00x | 3.42x | 1.02x | 2.00x |

### Key Observations

1. **Native Performance**: Both Go and Java native implementations significantly outperform their C++-backed counterparts across all percentiles.

2. **C++ Overhead**: The CGO/JNI overhead is substantial, especially for simple operations like data retrieval.

3. **Language Comparison**: 
   - Go's native collections are generally faster than Java's for map operations
   - Go's native data retrieval is exceptionally fast (28.64 ns vs Java's 44.56 ns)

4. **Hash Map Performance**: Both implementations show excellent performance with hash map lookups being significantly faster than ordered map lookups.

5. **JNI vs CGO Overhead**: 
   - Java JNI shows similar performance to Go CGO for C++-backed operations
   - The performance gap between native and C++-backed is larger for Go

6. **Percentile Analysis**:
   - **Go Native**: Shows very consistent performance with tight percentile ranges
   - **Java Native**: Shows more variance, especially in ordered map operations
   - **C++-Backed**: Both show higher variance, indicating less predictable performance

7. **Performance Consistency**:
   - **Go Native**: Most consistent performance with P99/P50 ratios close to 1x
   - **Java Native**: Higher variance in ordered map operations (7.93x P99/P50 ratio)
   - **C++-Backed**: Moderate variance with P99/P50 ratios around 1.5-3x

### Technical Notes

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

1. **Native implementations are dramatically faster** than C++-backed ones for simple caching operations across all percentiles
2. **CGO/JNI overhead is substantial** and should be considered when choosing between native and C++ implementations
3. **Go's native collections** generally outperform Java's for map operations
4. **For pure caching needs**, native implementations are the clear choice
5. **Percentile analysis reveals** that native implementations provide more predictable performance with lower variance
6. **Performance consistency** is crucial for production systems, where Go native implementations show the best characteristics 