package main

/*
#cgo CFLAGS: -I../cpp
#cgo LDFLAGS: -L../cpp/build/lib -lcache_lib -lstdc++
#include "cache_c.h"
*/
import "C"
import (
	"encoding/csv"
	"fmt"
	"os"
	"sort"
	"strconv"
	"sync"
	"time"
	"unsafe"
)

type Cache struct {
	handle C.CacheHandle
}

func NewCache() *Cache {
	return &Cache{
		handle: C.cache_create(),
	}
}

func (c *Cache) Destroy() {
	C.cache_destroy(c.handle)
}

func (c *Cache) PopulateOrderedMap(key, value string) {
	C.cache_populate_ordered_map(c.handle, C.CString(key), C.CString(value))
}

func (c *Cache) PopulateHashMap(key, value string) {
	C.cache_populate_hash_map(c.handle, C.CString(key), C.CString(value))
}

func (c *Cache) SetProto(key string, protoData []byte) {
	C.cache_set_proto(c.handle, C.CString(key), (*C.char)(unsafe.Pointer(&protoData[0])), C.int(len(protoData)))
}

func (c *Cache) GetProto(key string) []byte {
	var size C.int
	data := C.cache_get_proto(c.handle, C.CString(key), &size)
	return C.GoBytes(unsafe.Pointer(data), size)
}

func (c *Cache) LookupOrdered(key string) string {
	result := C.cache_lookup_ordered(c.handle, C.CString(key))
	return C.GoString(result)
}

func (c *Cache) LookupHash(key string) string {
	result := C.cache_lookup_hash(c.handle, C.CString(key))
	return C.GoString(result)
}

func BenchmarkCache() {
	cache := NewCache()
	defer cache.Destroy()

	// Populate maps
	fmt.Println("Populating maps...")
	for i := 0; i < 10000; i++ {
		key := fmt.Sprintf("key_%d", i)
		value := fmt.Sprintf("value_%d", i)
		cache.PopulateOrderedMap(key, value)
		cache.PopulateHashMap(key, value)
	}

	// Create 1KB proto data
	protoData := make([]byte, 1024)
	for i := range protoData {
		protoData[i] = byte(i % 256)
	}
	cache.SetProto("test_proto", protoData)

	// Benchmark ordered map lookups
	fmt.Println("Benchmarking ordered map lookups...")
	orderedTimes := make([]int64, 100000)
	iterations := 100000
	for i := 0; i < iterations; i++ {
		key := fmt.Sprintf("key_%d", i%10000)
		start := time.Now()
		result := cache.LookupOrdered(key)
		orderedTimes[i] = time.Since(start).Nanoseconds()
		_ = result
	}

	// Benchmark hash map lookups
	fmt.Println("Benchmarking hash map lookups...")
	hashTimes := make([]int64, 100000)
	for i := 0; i < iterations; i++ {
		key := fmt.Sprintf("key_%d", i%10000)
		start := time.Now()
		result := cache.LookupHash(key)
		hashTimes[i] = time.Since(start).Nanoseconds()
		_ = result
	}

	// Benchmark proto operations
	fmt.Println("Benchmarking proto operations...")
	protoTimes := make([]int64, 10000)
	for i := 0; i < 10000; i++ {
		start := time.Now()
		cache.GetProto("test_proto")
		protoTimes[i] = time.Since(start).Nanoseconds()
	}

	fmt.Printf("\nResults:\n")
	printTimingStats("Ordered map lookup", orderedTimes)
	printTimingStats("Hash map lookup", hashTimes)
	printTimingStats("Proto get", protoTimes)
}

// Go-native cache benchmarks
func BenchmarkGoNativeCache() {
	// Ordered map: use map + sorted keys
	orderedMap := make(map[string]string)
	hashMap := make(map[string]string)
	dataMap := make(map[string][]byte)

	fmt.Println("Go Native Cache Benchmark")
	fmt.Println("=========================")
	fmt.Println("Populating maps...")
	for i := 0; i < 10000; i++ {
		key := fmt.Sprintf("key_%d", i)
		value := fmt.Sprintf("value_%d", i)
		orderedMap[key] = value
		hashMap[key] = value
	}

	// 1KB data
	protoData := make([]byte, 1024)
	for i := range protoData {
		protoData[i] = byte(i % 256)
	}
	dataMap["test_proto"] = protoData

	// Benchmark ordered map lookups
	fmt.Println("Benchmarking ordered map lookups...")
	orderedTimes := make([]int64, 100000)
	iterations := 100000
	for i := 0; i < iterations; i++ {
		key := fmt.Sprintf("key_%d", i%10000)
		start := time.Now()
		_ = orderedMap[key]
		orderedTimes[i] = time.Since(start).Nanoseconds()
	}

	// Benchmark hash map lookups
	fmt.Println("Benchmarking hash map lookups...")
	hashTimes := make([]int64, 100000)
	for i := 0; i < iterations; i++ {
		key := fmt.Sprintf("key_%d", i%10000)
		start := time.Now()
		_ = hashMap[key]
		hashTimes[i] = time.Since(start).Nanoseconds()
	}

	// Benchmark proto operations
	fmt.Println("Benchmarking proto operations...")
	protoTimes := make([]int64, 10000)
	for i := 0; i < 10000; i++ {
		start := time.Now()
		_ = dataMap["test_proto"]
		protoTimes[i] = time.Since(start).Nanoseconds()
	}

	fmt.Printf("\nResults (Go Native):\n")
	printTimingStats("Ordered map lookup", orderedTimes)
	printTimingStats("Hash map lookup", hashTimes)
	printTimingStats("Proto get", protoTimes)
}

// ConcurrentBenchmarkResult holds the results of concurrent benchmarking
type ConcurrentBenchmarkResult struct {
	Operation   string
	Concurrency int
	P50         int64
	P90         int64
	P95         int64
	Times       []int64
}

// runConcurrentBenchmark runs a concurrent benchmark with the given parameters
func runConcurrentBenchmark(operation string, concurrency int, totalOps int, benchFunc func(int) []int64) ConcurrentBenchmarkResult {
	opsPerGoroutine := totalOps / concurrency
	results := make([][]int64, concurrency)
	var wg sync.WaitGroup

	for i := 0; i < concurrency; i++ {
		wg.Add(1)
		go func(goroutineID int) {
			defer wg.Done()
			results[goroutineID] = benchFunc(opsPerGoroutine)
		}(i)
	}

	wg.Wait()

	// Combine all results
	var allTimes []int64
	for _, result := range results {
		allTimes = append(allTimes, result...)
	}

	// Sort for percentile calculation
	sort.Slice(allTimes, func(i, j int) bool {
		return allTimes[i] < allTimes[j]
	})

	return ConcurrentBenchmarkResult{
		Operation:   operation,
		Concurrency: concurrency,
		P50:         calculatePercentile(allTimes, 50),
		P90:         calculatePercentile(allTimes, 90),
		P95:         calculatePercentile(allTimes, 95),
		Times:       allTimes,
	}
}

// writeCSVResults writes benchmark results to CSV file
func writeCSVResults(filename string, results []ConcurrentBenchmarkResult, language string, implementation string) error {
	file, err := os.OpenFile(filename, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		return err
	}
	defer file.Close()

	writer := csv.NewWriter(file)
	defer writer.Flush()

	// Write header if file is empty
	if info, err := file.Stat(); err == nil && info.Size() == 0 {
		header := []string{"language", "implementation", "concurrency", "p50", "p90", "p95"}
		writer.Write(header)
	}

	// Write results
	for _, result := range results {
		record := []string{
			language,
			implementation,
			strconv.Itoa(result.Concurrency),
			strconv.FormatInt(result.P50, 10),
			strconv.FormatInt(result.P90, 10),
			strconv.FormatInt(result.P95, 10),
		}
		writer.Write(record)
	}

	return nil
}

// BenchmarkConcurrentCache runs concurrent benchmarks on C++ cache
func BenchmarkConcurrentCache() {
	fmt.Println("Concurrent C++ Cache Benchmark")
	fmt.Println("==============================")

	concurrencyLevels := []int{2, 4, 6, 8, 10, 12, 14, 16, 20, 32, 64}
	totalOps := 100000
	var results []ConcurrentBenchmarkResult

	for _, concurrency := range concurrencyLevels {
		fmt.Printf("Testing with %d threads...\n", concurrency)

		// Create shared cache
		cache := NewCache()

		// Populate maps
		for i := 0; i < 10000; i++ {
			key := fmt.Sprintf("key_%d", i)
			value := fmt.Sprintf("value_%d", i)
			cache.PopulateOrderedMap(key, value)
			cache.PopulateHashMap(key, value)
		}

		// Benchmark hash map lookups (most common operation)
		result := runConcurrentBenchmark("Hash Map Lookup", concurrency, totalOps, func(ops int) []int64 {
			times := make([]int64, ops)
			for i := 0; i < ops; i++ {
				key := fmt.Sprintf("key_%d", i%10000)
				start := time.Now()
				cache.LookupHash(key)
				times[i] = time.Since(start).Nanoseconds()
			}
			return times
		})

		fmt.Printf("  Concurrency %d - P50: %d ns, P90: %d ns, P95: %d ns\n",
			concurrency, result.P50, result.P90, result.P95)

		results = append(results, result)
		cache.Destroy()
	}

	// Write results to CSV
	if err := writeCSVResults("../benchmark_results.csv", results, "Go", "cpp_wrapper"); err != nil {
		fmt.Printf("Error writing CSV: %v\n", err)
	}
}

// BenchmarkConcurrentGoNativeCache runs concurrent benchmarks on native Go cache
func BenchmarkConcurrentGoNativeCache() {
	fmt.Println("Concurrent Go Native Cache Benchmark")
	fmt.Println("====================================")

	concurrencyLevels := []int{2, 4, 6, 8, 10, 12, 14, 16, 20, 32, 64}
	totalOps := 100000
	var results []ConcurrentBenchmarkResult

	for _, concurrency := range concurrencyLevels {
		fmt.Printf("Testing with %d threads...\n", concurrency)

		// Create shared cache with sync.Map (thread-safe)
		var hashMap sync.Map

		// Populate maps
		for i := 0; i < 10000; i++ {
			key := fmt.Sprintf("key_%d", i)
			value := fmt.Sprintf("value_%d", i)
			hashMap.Store(key, value)
		}

		// Benchmark hash map lookups
		result := runConcurrentBenchmark("Hash Map Lookup", concurrency, totalOps, func(ops int) []int64 {
			times := make([]int64, ops)
			for i := 0; i < ops; i++ {
				key := fmt.Sprintf("key_%d", i%10000)
				start := time.Now()
				_, _ = hashMap.Load(key)
				times[i] = time.Since(start).Nanoseconds()
			}
			return times
		})

		fmt.Printf("  Concurrency %d - P50: %d ns, P90: %d ns, P95: %d ns\n",
			concurrency, result.P50, result.P90, result.P95)

		results = append(results, result)
	}

	// Write results to CSV
	if err := writeCSVResults("../benchmark_results.csv", results, "Go", "native"); err != nil {
		fmt.Printf("Error writing CSV: %v\n", err)
	}
}

// calculatePercentile calculates the nth percentile from a sorted slice of durations
func calculatePercentile(times []int64, percentile int) int64 {
	if len(times) == 0 {
		return 0
	}
	index := (percentile * len(times)) / 100
	if index >= len(times) {
		index = len(times) - 1
	}
	return times[index]
}

// printTimingStats prints average and percentile statistics
func printTimingStats(operation string, times []int64) {
	if len(times) == 0 {
		return
	}

	// Sort times for percentile calculation
	sortedTimes := make([]int64, len(times))
	copy(sortedTimes, times)
	sort.Slice(sortedTimes, func(i, j int) bool {
		return sortedTimes[i] < sortedTimes[j]
	})

	// Calculate average
	var total int64
	for _, t := range times {
		total += t
	}
	avg := float64(total) / float64(len(times))

	// Calculate percentiles
	p50 := calculatePercentile(sortedTimes, 50)
	p90 := calculatePercentile(sortedTimes, 90)
	p95 := calculatePercentile(sortedTimes, 95)
	p99 := calculatePercentile(sortedTimes, 99)

	fmt.Printf("%s:\n", operation)
	fmt.Printf("  Average: %.2f ns\n", avg)
	fmt.Printf("  P50: %d ns\n", p50)
	fmt.Printf("  P90: %d ns\n", p90)
	fmt.Printf("  P95: %d ns\n", p95)
	fmt.Printf("  P99: %d ns\n", p99)
}
