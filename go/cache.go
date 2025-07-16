package main

/*
#cgo CFLAGS: -I../cpp
#cgo LDFLAGS: -L../cpp/build/lib -lcache_lib
#include "cache.h"
*/
import "C"
import (
	"fmt"
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

func (c *Cache) GetOrderedLookupTimeNs() int64 {
	return int64(C.cache_get_ordered_lookup_time_ns(c.handle))
}

func (c *Cache) GetHashLookupTimeNs() int64 {
	return int64(C.cache_get_hash_lookup_time_ns(c.handle))
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
	fmt.Println("\nBenchmarking ordered map lookups...")
	var totalOrderedTime int64
	iterations := 100000
	for i := 0; i < iterations; i++ {
		key := fmt.Sprintf("key_%d", i%10000)
		cache.LookupOrdered(key)
		totalOrderedTime += cache.GetOrderedLookupTimeNs()
	}
	avgOrderedTime := float64(totalOrderedTime) / float64(iterations)

	// Benchmark hash map lookups
	fmt.Println("Benchmarking hash map lookups...")
	var totalHashTime int64
	for i := 0; i < iterations; i++ {
		key := fmt.Sprintf("key_%d", i%10000)
		cache.LookupHash(key)
		totalHashTime += cache.GetHashLookupTimeNs()
	}
	avgHashTime := float64(totalHashTime) / float64(iterations)

	// Benchmark proto operations
	fmt.Println("Benchmarking proto operations...")
	start := time.Now()
	for i := 0; i < 10000; i++ {
		cache.GetProto("test_proto")
	}
	protoTime := time.Since(start).Nanoseconds() / 10000

	fmt.Printf("\nResults:\n")
	fmt.Printf("Average ordered map lookup time: %.2f ns\n", avgOrderedTime)
	fmt.Printf("Average hash map lookup time: %.2f ns\n", avgHashTime)
	fmt.Printf("Average proto get time: %d ns\n", protoTime)
}
