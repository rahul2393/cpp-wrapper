# Benchmark Results

This directory contains the performance benchmark results for the C++ cache wrapper comparison.

## Files

- **`benchmark_results.csv`**: Raw benchmark data with latency percentiles (P50, P90, P95) across different concurrency levels
- **`go_comparison.png`**: Performance comparison between Go native and C++ wrapper implementations
- **`java_comparison.png`**: Performance comparison between Java native and C++ wrapper implementations  
- **`combined_comparison.png`**: Cross-language performance comparison across all implementations

## Key Findings

1. **Native implementations are dramatically superior** for concurrent caching workloads
2. **CGO overhead is significantly higher** than JNI overhead (15-20x vs 1.5-5x slower than native)
3. **Thread-safe native collections** provide excellent performance and scalability
4. **C++ wrapper performance degrades** significantly under concurrent load

## Performance Summary

| Implementation | P50 Latency (ns) | Concurrency Scaling |
|----------------|------------------|-------------------|
| Go Native | ~42 | Excellent (consistent across all levels) |
| Java Native | 125-458 | Good (stable with slight variation) |
| Go C++ Wrapper | 625-834 | Poor (degrades after 4 threads) |
| Java C++ Wrapper | 666-750 | Moderate (stable but slower) |

For detailed analysis, see the main `RESULTS.md` file in the project root. 