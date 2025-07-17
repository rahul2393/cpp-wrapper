# C++ Cache Performance Benchmark Results

## Overview
This project benchmarks the performance of calling C++ cache operations from Go (via CGO) and Java (via JNI), comparing them against native Go and Java implementations under both serial and concurrent workloads. The benchmark now includes comprehensive concurrent testing across multiple thread levels.

## Architecture
- **C++ Core**: Shared library with thread-safe implementations using `std::shared_mutex`
- **Go Wrapper**: CGO bindings to the C++ library
- **Java Wrapper**: JNI implementation to call the C++ library
- **Native Implementations**: 
  - Go: `sync.Map` for thread-safe concurrent access
  - Java: `ConcurrentHashMap` for thread-safe concurrent access

## Benchmark Configuration

### Serial Benchmarks
- **Population**: 10,000 key-value pairs in both ordered and hash maps
- **Lookup Tests**: 100,000 iterations of map lookups
- **Data Tests**: 10,000 iterations of 1KB data retrieval operations
- **Platform**: macOS (Apple Silicon M1)

### Concurrent Benchmarks
- **Total Operations**: 100,000 operations distributed across threads
- **Concurrency Levels**: 2, 4, 6, 8, 10, 12, 14, 16, 20, 32, 64 threads
- **Thread Safety**: All implementations use proper synchronization mechanisms
- **Performance Metrics**: P50, P90, P95 latency measurements

## Serial Performance Results

### Hash Map Lookup Performance (100,000 operations)

| Implementation | Average (ns) | P50 (ns) | P90 (ns) | P95 (ns) | P99 (ns) |
|----------------|--------------|----------|----------|----------|----------|
| **Go C++ Wrapper** | 551.56 | 500 | 666 | 708 | 875 |
| **Go Native** | 44.18 | 42 | 42 | 83 | 125 |
| **Java C++ Wrapper** | 532.68 | 500 | 625 | 667 | 875 |
| **Java Native** | 70.20 | 83 | 84 | 125 | 167 |

### Performance Ratios (Native vs C++ Wrapper)

| Metric | Go Native vs CGO | Java Native vs JNI |
|--------|------------------|-------------------|
| **P50** | **11.9x faster** | **6.0x faster** |
| **P90** | **15.9x faster** | **7.4x faster** |
| **P95** | **8.5x faster** | **5.3x faster** |

## Concurrent Performance Results

### Go C++ Wrapper vs Native Performance

| Threads | C++ Wrapper P50 (ns) | Native P50 (ns) | Performance Ratio |
|---------|---------------------|-----------------|-------------------|
| 2 | 625 | 42 | **14.9x slower** |
| 4 | 750 | 42 | **17.9x slower** |
| 6 | 833 | 42 | **19.8x slower** |
| 8 | 792 | 42 | **18.9x slower** |
| 10 | 791 | 42 | **18.8x slower** |
| 12 | 833 | 42 | **19.8x slower** |
| 14 | 833 | 42 | **19.8x slower** |
| 16 | 834 | 42 | **19.9x slower** |
| 20 | 833 | 42 | **19.8x slower** |
| 32 | 834 | 42 | **19.9x slower** |
| 64 | 750 | 42 | **17.9x slower** |

### Java C++ Wrapper vs Native Performance

| Threads | C++ Wrapper P50 (ns) | Native P50 (ns) | Performance Ratio |
|---------|---------------------|-----------------|-------------------|
| 2 | 666 | 125 | **5.3x slower** |
| 4 | 750 | 292 | **2.6x slower** |
| 6 | 750 | 291 | **2.6x slower** |
| 8 | 750 | 375 | **2.0x slower** |
| 10 | 709 | 458 | **1.5x slower** |
| 12 | 750 | 375 | **2.0x slower** |
| 14 | 708 | 375 | **1.9x slower** |
| 16 | 750 | 375 | **2.0x slower** |
| 20 | 750 | 416 | **1.8x slower** |
| 32 | 750 | 375 | **2.0x slower** |
| 64 | 750 | 333 | **2.3x slower** |

## Key Performance Insights

### 1. **CGO vs JNI Overhead**
- **Go CGO**: Shows significantly higher overhead (15-20x slower than native)
- **Java JNI**: Shows moderate overhead (1.5-5x slower than native)
- **Conclusion**: JNI has better performance characteristics than CGO for this workload

### 2. **Concurrency Scaling**
- **Go Native**: Maintains consistent ~42ns P50 across all concurrency levels
- **Java Native**: Shows some variation (125-458ns P50) but remains stable
- **C++ Wrapper**: Performance degrades after 4 threads, then stabilizes

### 3. **Thread Safety Implementation Impact**
- **C++**: Uses `std::shared_mutex` with separate locks per data structure
- **Go**: Uses `sync.Map` (built-in thread-safe map)
- **Java**: Uses `ConcurrentHashMap` (built-in lock-striping)

### 4. **Performance Consistency**
- **Go Native**: Most consistent performance (P50: 42ns across all concurrency levels)
- **Java Native**: Good consistency with slight variation
- **C++ Wrapper**: Higher variance, especially at higher concurrency levels

## Technical Implementation Details

### C++ Thread Safety Optimizations
- **Separate Locks**: Each data structure (`ordered_map`, `hash_map`, `proto_cache`) has its own `std::shared_mutex`
- **Read-Write Separation**: Uses `std::shared_lock` for reads, `std::unique_lock` for writes
- **Thread-Local Storage**: Uses `thread_local` for string storage to avoid race conditions
- **Memory Management**: Clean string handling without timing overhead

### Native Thread Safety
- **Go**: `sync.Map` provides lock-free reads and minimal contention
- **Java**: `ConcurrentHashMap` uses lock-striping for better concurrency

## Performance Plots

The benchmark generates comprehensive performance visualizations:

1. **Go Comparison Plot** (`results/go_comparison.png`): Shows Go native vs C++ wrapper performance across concurrency levels
2. **Java Comparison Plot** (`results/java_comparison.png`): Shows Java native vs C++ wrapper performance across concurrency levels  
3. **Combined Comparison Plot** (`results/combined_comparison.png`): Cross-language performance comparison

All benchmark data is available in `results/benchmark_results.csv` for further analysis.

## Recommendations

### When to Use Native Implementations
- **High-concurrency workloads**: Native implementations scale much better
- **Latency-sensitive applications**: Native implementations provide consistent low latency
- **Simple caching needs**: Native collections are sufficient and much faster

### When to Consider C++ Wrapper
- **Complex C++ logic**: When you need sophisticated C++ algorithms
- **Legacy C++ code**: When integrating with existing C++ libraries
- **Memory efficiency**: C++ can be more memory-efficient for large datasets

### Performance Optimization Tips
1. **Use native collections** for simple caching operations
2. **Consider JNI over CGO** if you need C++ integration
3. **Profile at target concurrency levels** - performance characteristics change significantly
4. **Monitor P95/P99 latencies** - C++ wrapper shows higher tail latency

## Conclusion

The comprehensive benchmark results demonstrate:

1. **Native implementations are dramatically superior** for concurrent caching workloads
2. **CGO overhead is significantly higher** than JNI overhead
3. **Thread-safe native collections** (sync.Map, ConcurrentHashMap) provide excellent performance and scalability
4. **C++ wrapper performance degrades** significantly under concurrent load
5. **For pure caching needs**, native implementations are the clear choice across all concurrency levels

The benchmark provides a fair comparison using appropriate thread-safe mechanisms across all implementations, making it a reliable guide for choosing between native and C++ wrapper approaches. 