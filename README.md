# C++ Cache Library Wrapper

A high-performance cache library with C++ core implementation and Go/Java bindings for cross-language performance comparison.

## Features

- **C++ Core**: High-performance cache implementation with thread safety
- **Go Integration**: CGO bindings for Go applications
- **Java Integration**: JNI bindings for Java applications
- **Concurrent Benchmarking**: Multi-threaded performance testing
- **Performance Visualization**: Automated plotting of benchmark results

## Benchmark Types

### Serial Benchmarks
- 100,000 operations per test
- Single-threaded execution
- P50, P90, P95, P99 latency measurements

### Concurrent Benchmarks
- 100,000 total operations distributed across threads
- Concurrency levels: 2, 4, 6, 8, 10, 12, 14, 16, 20, 32, 64 threads
- Mutex-protected operations for thread safety
- Native vs C++ wrapper performance comparison

## Quick Start

```bash
# Run all benchmarks and generate plots
./build.sh

# Manual plotting (requires Python3 and matplotlib)
python3 plot_results.py
```

## Requirements

- **C++17** compatible compiler
- **Go 1.21+**
- **Java 11+**
- **Python 3.7+** (for plotting)
- **matplotlib** (for plotting)

## Build Process

1. **C++ Library**: Builds shared library with mutex synchronization
2. **Go Benchmark**: Compiles Go benchmark with CGO bindings
3. **Java Benchmark**: Compiles Java benchmark with JNI bindings
4. **Benchmark Execution**: Runs both serial and concurrent tests
5. **Plot Generation**: Creates performance comparison visualizations

## Output

- **Console**: Real-time benchmark results
- **CSV**: `benchmark_results.csv` with detailed metrics
- **Plots**: `plots/` directory with performance visualizations
  - `go_comparison.png` - Go native vs C++ wrapper
  - `java_comparison.png` - Java native vs C++ wrapper
  - `combined_comparison.png` - Cross-language comparison

## Performance Metrics

- **P50**: 50th percentile latency
- **P90**: 90th percentile latency  
- **P95**: 95th percentile latency

## Thread Safety

All implementations include proper synchronization:
- **C++ Cache**: `std::mutex` for thread-safe operations
- **Go Native**: `sync.RWMutex` for read/write separation
- **Java Native**: `ReentrantReadWriteLock` for concurrent access 