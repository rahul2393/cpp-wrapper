package main

import "fmt"

func main() {
	fmt.Println("Go Cache Benchmark (C++ backend)")
	fmt.Println("==================")
	BenchmarkCache()
	fmt.Println("\n------------------------------\n")
	BenchmarkGoNativeCache()
}
