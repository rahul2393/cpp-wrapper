# Python Cache Implementation

This directory contains the Python implementation of the cache benchmark, using ctypes to interface with the C++ cache library.

## Files

- `cache.py` - Python wrapper around the C++ cache library using ctypes
- `main.py` - Benchmark runner with serial and concurrent tests
- `test_cache.py` - Simple test script to verify functionality
- `requirements.txt` - Python dependencies (none required - uses standard library only)

## Implementation Details

The Python implementation uses `ctypes` to create a Python wrapper around the C++ cache library, similar to how Java uses JNI and Go uses CGO. Key features:

- **ctypes Integration**: Direct FFI calls to the C++ library functions
- **Memory Management**: Proper cleanup of cache handles and resources
- **Thread Safety**: Support for concurrent operations using ThreadPoolExecutor
- **Performance Metrics**: Comprehensive timing statistics including percentiles
- **Native Comparison**: Benchmarks against native Python data structures

## Usage

### Running Tests
```bash
python3 test_cache.py
```

### Running Benchmarks
```bash
python3 main.py
```

### Integration with Build Script
The Python benchmarks are automatically run as part of the main `build.sh` script.

## Performance Characteristics

The Python implementation provides:
- Serial benchmarks for ordered map, hash map, and proto operations
- Concurrent benchmarks with varying thread counts (1, 2, 4, 8, 16)
- Comparison between ctypes-backed cache and native Python implementations
- CSV output for result analysis and plotting

## Dependencies

- Python 3.6+
- Standard library modules only (no external dependencies)
- C++ cache library (built separately)

## Platform Support

- macOS (tested with libcache_lib.dylib)
- Linux (tested with libcache_lib.so)
- Cross-platform ctypes implementation 