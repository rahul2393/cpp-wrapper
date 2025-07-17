import time
import csv
import threading
import os
from concurrent.futures import ThreadPoolExecutor
from typing import List, Dict, Any
import statistics
from cache import Cache
import collections

def calculate_percentile(times: List[float], percentile: int) -> float:
    """Calculate percentile from sorted array"""
    if not times:
        return 0.0
    sorted_times = sorted(times)
    index = (percentile * len(sorted_times)) // 100
    if index >= len(sorted_times):
        index = len(sorted_times) - 1
    return sorted_times[index]

def print_timing_stats(operation: str, times: List[float]):
    """Print timing statistics including percentiles"""
    if not times:
        return
    
    avg = statistics.mean(times)
    p50 = calculate_percentile(times, 50)
    p90 = calculate_percentile(times, 90)
    p95 = calculate_percentile(times, 95)
    p99 = calculate_percentile(times, 99)
    
    print(f"{operation}:")
    print(f"  Average: {avg:.2f} ns")
    print(f"  P50: {p50:.0f} ns")
    print(f"  P90: {p90:.0f} ns")
    print(f"  P95: {p95:.0f} ns")
    print(f"  P99: {p99:.0f} ns")

def benchmark_cache():
    """Benchmark the C++ cache via Python ctypes"""
    print("=== Serial Benchmarks ===")
    print("Python Cache Benchmark (ctypes backend)")
    print("===================")
    
    cache = Cache()
    
    try:
        # Populate maps
        print("Populating maps...")
        for i in range(10000):
            key = f"key_{i}"
            value = f"value_{i}"
            cache.populate_ordered_map(key, value)
            cache.populate_hash_map(key, value)

        # Create 1KB proto data
        proto_data = bytes([i % 256 for i in range(1024)])
        cache.set_proto("test_proto", proto_data)

        # Benchmark ordered map lookups
        print("Benchmarking ordered map lookups...")
        ordered_times = []
        iterations = 100000
        for i in range(iterations):
            key = f"key_{i % 10000}"
            start = time.perf_counter_ns()
            result = cache.lookup_ordered(key)
            ordered_times.append(time.perf_counter_ns() - start)
            if result is None:
                print("Unexpected null result")

        # Benchmark hash map lookups
        print("Benchmarking hash map lookups...")
        hash_times = []
        for i in range(iterations):
            key = f"key_{i % 10000}"
            start = time.perf_counter_ns()
            result = cache.lookup_hash(key)
            hash_times.append(time.perf_counter_ns() - start)
            if result is None:
                print("Unexpected null result")

        # Benchmark proto operations
        print("Benchmarking proto operations...")
        proto_times = []
        for i in range(10000):
            start = time.perf_counter_ns()
            cache.get_proto("test_proto")
            proto_times.append(time.perf_counter_ns() - start)

        print(f"\nResults:")
        print_timing_stats("Ordered map lookup", ordered_times)
        print_timing_stats("Hash map lookup", hash_times)
        print_timing_stats("Proto get", proto_times)

    finally:
        cache.destroy()

def benchmark_python_native_cache():
    """Benchmark native Python cache implementations"""
    print("\n------------------------------\n")
    print("Python Native Cache Benchmark")
    print("=============================")
    
    # Use OrderedDict for ordered map simulation
    ordered_map = collections.OrderedDict()
    hash_map = {}
    data_map = {}

    print("Populating maps...")
    for i in range(10000):
        key = f"key_{i}"
        value = f"value_{i}"
        ordered_map[key] = value
        hash_map[key] = value

    # 1KB data
    proto_data = bytes([i % 256 for i in range(1024)])
    data_map["test_proto"] = proto_data

    # Benchmark ordered map lookups
    print("Benchmarking ordered map lookups...")
    ordered_times = []
    iterations = 100000
    for i in range(iterations):
        key = f"key_{i % 10000}"
        start = time.perf_counter_ns()
        _ = ordered_map.get(key)
        ordered_times.append(time.perf_counter_ns() - start)

    # Benchmark hash map lookups
    print("Benchmarking hash map lookups...")
    hash_times = []
    for i in range(iterations):
        key = f"key_{i % 10000}"
        start = time.perf_counter_ns()
        _ = hash_map.get(key)
        hash_times.append(time.perf_counter_ns() - start)

    # Benchmark proto operations
    print("Benchmarking proto operations...")
    proto_times = []
    for i in range(10000):
        start = time.perf_counter_ns()
        _ = data_map.get("test_proto")
        proto_times.append(time.perf_counter_ns() - start)

    print(f"\nResults (Python Native):")
    print_timing_stats("Ordered map lookup", ordered_times)
    print_timing_stats("Hash map lookup", hash_times)
    print_timing_stats("Proto get", proto_times)

class ConcurrentBenchmarkResult:
    """Holds the results of concurrent benchmarking"""
    def __init__(self, operation: str, concurrency: int, p50: float, p90: float, p95: float, times: List[float]):
        self.operation = operation
        self.concurrency = concurrency
        self.p50 = p50
        self.p90 = p90
        self.p95 = p95
        self.times = times

def run_concurrent_benchmark(operation: str, concurrency: int, total_ops: int, bench_func) -> ConcurrentBenchmarkResult:
    """Run concurrent benchmark with the given parameters"""
    ops_per_thread = total_ops // concurrency
    all_times = []
    
    def worker():
        return bench_func(ops_per_thread)
    
    with ThreadPoolExecutor(max_workers=concurrency) as executor:
        futures = [executor.submit(worker) for _ in range(concurrency)]
        for future in futures:
            thread_times = future.result()
            all_times.extend(thread_times)
    
    p50 = calculate_percentile(all_times, 50)
    p90 = calculate_percentile(all_times, 90)
    p95 = calculate_percentile(all_times, 95)
    
    return ConcurrentBenchmarkResult(operation, concurrency, p50, p90, p95, all_times)

def write_csv_results(filename: str, results: List[ConcurrentBenchmarkResult], language: str, implementation: str):
    """Write benchmark results to CSV file"""
    # Check if file exists to determine if we need to write headers
    file_exists = os.path.exists(filename)
    
    with open(filename, 'a', newline='') as csvfile:
        writer = csv.writer(csvfile)
        
        # Write headers only if file doesn't exist
        if not file_exists:
            writer.writerow(['language', 'implementation', 'concurrency', 'p50', 'p90', 'p95'])
        
        for result in results:
            writer.writerow([
                language,
                implementation,
                result.concurrency,
                f"{result.p50:.0f}",
                f"{result.p90:.0f}",
                f"{result.p95:.0f}"
            ])

def benchmark_concurrent_cache():
    """Benchmark concurrent cache operations"""
    print("\n\n=== Concurrent Benchmarks ===")
    
    cache = Cache()
    results = []
    lock = threading.Lock()  # Add a lock for all reads
    
    try:
        # Populate maps
        print("Populating maps for concurrent benchmarks...")
        for i in range(10000):
            key = f"key_{i}"
            value = f"value_{i}"
            cache.populate_ordered_map(key, value)
            cache.populate_hash_map(key, value)

        proto_data = bytes([i % 256 for i in range(1024)])
        cache.set_proto("test_proto", proto_data)

        # Test different concurrency levels
        concurrency_levels = [1, 2, 4, 8, 16]
        total_ops = 100000

        for concurrency in concurrency_levels:
            print(f"\nTesting concurrency level: {concurrency}")
            
            # Ordered map lookups
            def ordered_lookup_worker(ops):
                times = []
                for i in range(ops):
                    key = f"key_{i % 10000}"
                    start = time.perf_counter_ns()
                    with lock:
                        cache.lookup_ordered(key)
                    times.append(time.perf_counter_ns() - start)
                return times
            
            result = run_concurrent_benchmark("ordered_lookup", concurrency, total_ops, ordered_lookup_worker)
            results.append(result)
            print(f"  Ordered lookup P95: {result.p95:.0f} ns")

            # Hash map lookups
            def hash_lookup_worker(ops):
                times = []
                for i in range(ops):
                    key = f"key_{i % 10000}"
                    start = time.perf_counter_ns()
                    with lock:
                        cache.lookup_hash(key)
                    times.append(time.perf_counter_ns() - start)
                return times
            
            result = run_concurrent_benchmark("hash_lookup", concurrency, total_ops, hash_lookup_worker)
            results.append(result)
            print(f"  Hash lookup P95: {result.p95:.0f} ns")

        # Write results to CSV
        write_csv_results("../benchmark_results.csv", results, "Python", "ctypes")

    finally:
        cache.destroy()

def benchmark_concurrent_python_native_cache():
    """Benchmark concurrent native Python cache operations"""
    print("\n------------------------------\n")
    print("Python Native Concurrent Cache Benchmark")
    print("========================================")
    
    # Create thread-safe dictionaries
    ordered_map = collections.OrderedDict()
    hash_map = {}
    data_map = {}
    lock = threading.Lock()  # Add a lock for all reads
    
    # Populate maps
    print("Populating maps for concurrent benchmarks...")
    for i in range(10000):
        key = f"key_{i}"
        value = f"value_{i}"
        ordered_map[key] = value
        hash_map[key] = value

    proto_data = bytes([i % 256 for i in range(1024)])
    data_map["test_proto"] = proto_data

    results = []
    concurrency_levels = [1, 2, 4, 8, 16]
    total_ops = 100000

    for concurrency in concurrency_levels:
        print(f"\nTesting concurrency level: {concurrency}")
        
        # Ordered map lookups
        def ordered_lookup_worker(ops):
            times = []
            for i in range(ops):
                key = f"key_{i % 10000}"
                start = time.perf_counter_ns()
                with lock:
                    _ = ordered_map.get(key)
                times.append(time.perf_counter_ns() - start)
            return times
        
        result = run_concurrent_benchmark("ordered_lookup", concurrency, total_ops, ordered_lookup_worker)
        results.append(result)
        print(f"  Ordered lookup P95: {result.p95:.0f} ns")

        # Hash map lookups
        def hash_lookup_worker(ops):
            times = []
            for i in range(ops):
                key = f"key_{i % 10000}"
                start = time.perf_counter_ns()
                with lock:
                    _ = hash_map.get(key)
                times.append(time.perf_counter_ns() - start)
            return times
        
        result = run_concurrent_benchmark("hash_lookup", concurrency, total_ops, hash_lookup_worker)
        results.append(result)
        print(f"  Hash lookup P95: {result.p95:.0f} ns")

    # Write results to CSV
    write_csv_results("../benchmark_results.csv", results, "Python", "native")

if __name__ == "__main__":
    benchmark_cache()
    benchmark_python_native_cache()
    benchmark_concurrent_cache()
    benchmark_concurrent_python_native_cache() 