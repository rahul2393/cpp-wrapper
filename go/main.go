package main

import "fmt"

func main() {
	fmt.Println("=== Serial Benchmarks ===")
	fmt.Println("Go Cache Benchmark (C++ backend)")
	fmt.Println("==================")
	BenchmarkCache()
	fmt.Println("\n------------------------------\n")
	BenchmarkGoNativeCache()

	fmt.Println("\n\n=== Concurrent Benchmarks ===")
	BenchmarkConcurrentCache()
	fmt.Println("\n------------------------------\n")
	BenchmarkConcurrentGoNativeCache()
}
