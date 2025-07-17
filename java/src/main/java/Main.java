import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Serial Benchmarks ===");
        System.out.println("Java Cache Benchmark (JNI backend)");
        System.out.println("===================");
        benchmarkCache();
        System.out.println("\n------------------------------\n");
        benchmarkJavaNativeCache();
        
        System.out.println("\n\n=== Concurrent Benchmarks ===");
        benchmarkConcurrentCache();
        System.out.println("\n------------------------------\n");
        benchmarkConcurrentJavaNativeCache();
    }

    // Calculate percentile from sorted array
    private static long calculatePercentile(long[] times, int percentile) {
        if (times.length == 0) {
            return 0;
        }
        int index = (percentile * times.length) / 100;
        if (index >= times.length) {
            index = times.length - 1;
        }
        return times[index];
    }

    // Print timing statistics including percentiles
    private static void printTimingStats(String operation, long[] times) {
        if (times.length == 0) {
            return;
        }
        
        // Sort times for percentile calculation
        long[] sortedTimes = Arrays.copyOf(times, times.length);
        Arrays.sort(sortedTimes);
        
        // Calculate average
        long total = 0;
        for (long time : times) {
            total += time;
        }
        double avg = (double) total / times.length;
        
        // Calculate percentiles
        long p50 = calculatePercentile(sortedTimes, 50);
        long p90 = calculatePercentile(sortedTimes, 90);
        long p95 = calculatePercentile(sortedTimes, 95);
        long p99 = calculatePercentile(sortedTimes, 99);
        
        System.out.printf("%s:\n", operation);
        System.out.printf("  Average: %.2f ns\n", avg);
        System.out.printf("  P50: %d ns\n", p50);
        System.out.printf("  P90: %d ns\n", p90);
        System.out.printf("  P95: %d ns\n", p95);
        System.out.printf("  P99: %d ns\n", p99);
    }

    public static void benchmarkCache() {
        Cache cache = new Cache();
        
        try {
            // Populate maps
            System.out.println("Populating maps...");
            for (int i = 0; i < 10000; i++) {
                String key = "key_" + i;
                String value = "value_" + i;
                cache.populateOrderedMap(key, value);
                cache.populateHashMap(key, value);
            }

            // Create 1KB proto data
            byte[] protoData = new byte[1024];
            for (int i = 0; i < protoData.length; i++) {
                protoData[i] = (byte) (i % 256);
            }
            cache.setProto("test_proto", protoData);

            // Benchmark ordered map lookups
            System.out.println("Benchmarking ordered map lookups...");
            long[] orderedTimes = new long[100000];
            int iterations = 100000;
            for (int i = 0; i < iterations; i++) {
                String key = "key_" + (i % 10000);
                long start = System.nanoTime();
                String result = cache.lookupOrdered(key);
                orderedTimes[i] = System.nanoTime() - start;
                if (result == null) {
                    System.out.println("Unexpected null result");
                }
            }

            // Benchmark hash map lookups
            System.out.println("Benchmarking hash map lookups...");
            long[] hashTimes = new long[100000];
            for (int i = 0; i < iterations; i++) {
                String key = "key_" + (i % 10000);
                long start = System.nanoTime();
                String result = cache.lookupHash(key);
                hashTimes[i] = System.nanoTime() - start;
                if (result == null) {
                    System.out.println("Unexpected null result");
                }
            }

            // Benchmark proto operations
            System.out.println("Benchmarking proto operations...");
            long[] protoTimes = new long[10000];
            for (int i = 0; i < 10000; i++) {
                long start = System.nanoTime();
                cache.getProto("test_proto");
                protoTimes[i] = System.nanoTime() - start;
            }

            System.out.printf("\nResults:\n");
            printTimingStats("Ordered map lookup", orderedTimes);
            printTimingStats("Hash map lookup", hashTimes);
            printTimingStats("Proto get", protoTimes);

        } finally {
            cache.destroy();
        }
    }

    // Java-native cache benchmark
    public static void benchmarkJavaNativeCache() {
        System.out.println("Java Native Cache Benchmark");
        System.out.println("===========================");
        java.util.TreeMap<String, String> orderedMap = new java.util.TreeMap<>();
        java.util.HashMap<String, String> hashMap = new java.util.HashMap<>();
        java.util.HashMap<String, byte[]> dataMap = new java.util.HashMap<>();

        System.out.println("Populating maps...");
        for (int i = 0; i < 10000; i++) {
            String key = "key_" + i;
            String value = "value_" + i;
            orderedMap.put(key, value);
            hashMap.put(key, value);
        }

        // 1KB data
        byte[] protoData = new byte[1024];
        for (int i = 0; i < protoData.length; i++) {
            protoData[i] = (byte) (i % 256);
        }
        dataMap.put("test_proto", protoData);

        // Benchmark ordered map lookups
        System.out.println("Benchmarking ordered map lookups...");
        long[] orderedTimes = new long[100000];
        int iterations = 100000;
        for (int i = 0; i < iterations; i++) {
            String key = "key_" + (i % 10000);
            long start = System.nanoTime();
            orderedMap.get(key);
            orderedTimes[i] = System.nanoTime() - start;
        }

        // Benchmark hash map lookups
        System.out.println("Benchmarking hash map lookups...");
        long[] hashTimes = new long[100000];
        for (int i = 0; i < iterations; i++) {
            String key = "key_" + (i % 10000);
            long start = System.nanoTime();
            hashMap.get(key);
            hashTimes[i] = System.nanoTime() - start;
        }

        // Benchmark proto operations
        System.out.println("Benchmarking proto operations...");
        long[] protoTimes = new long[10000];
        for (int i = 0; i < 10000; i++) {
            long start = System.nanoTime();
            dataMap.get("test_proto");
            protoTimes[i] = System.nanoTime() - start;
        }

        System.out.printf("\nResults (Java Native):\n");
        printTimingStats("Ordered map lookup", orderedTimes);
        printTimingStats("Hash map lookup", hashTimes);
        printTimingStats("Proto get", protoTimes);
    }

    // ConcurrentBenchmarkResult holds the results of concurrent benchmarking
    static class ConcurrentBenchmarkResult {
        String operation;
        int concurrency;
        long p50;
        long p90;
        long p95;
        long[] times;
        
        ConcurrentBenchmarkResult(String operation, int concurrency, long p50, long p90, long p95, long[] times) {
            this.operation = operation;
            this.concurrency = concurrency;
            this.p50 = p50;
            this.p90 = p90;
            this.p95 = p95;
            this.times = times;
        }
    }

    // Interface for benchmark functions
    interface BenchmarkFunction {
        long[] run(int ops);
    }

    // Run concurrent benchmark with the given parameters
    private static ConcurrentBenchmarkResult runConcurrentBenchmark(String operation, int concurrency, int totalOps, BenchmarkFunction benchFunc) {
        int opsPerThread = totalOps / concurrency;
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        List<Future<long[]>> futures = new ArrayList<>();
        
        // Submit tasks
        for (int i = 0; i < concurrency; i++) {
            futures.add(executor.submit(() -> benchFunc.run(opsPerThread)));
        }
        
        // Collect results
        List<Long> allTimes = new ArrayList<>();
        try {
            for (Future<long[]> future : futures) {
                long[] threadTimes = future.get();
                for (long time : threadTimes) {
                    allTimes.add(time);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Convert to array and sort
        long[] times = allTimes.stream().mapToLong(Long::longValue).toArray();
        Arrays.sort(times);
        
        return new ConcurrentBenchmarkResult(
            operation,
            concurrency,
            calculatePercentile(times, 50),
            calculatePercentile(times, 90),
            calculatePercentile(times, 95),
            times
        );
    }

    // Write CSV results for plotting
    private static void writeCSVResults(String filename, List<ConcurrentBenchmarkResult> results, String language, String implementation) {
        try {
            File file = new File(filename);
            boolean writeHeader = !file.exists() || file.length() == 0;
            
            FileWriter writer = new FileWriter(filename, true);
            
            if (writeHeader) {
                writer.write("language,implementation,concurrency,p50,p90,p95\n");
            }
            
            for (ConcurrentBenchmarkResult result : results) {
                writer.write(String.format("%s,%s,%d,%d,%d,%d\n", 
                    language, implementation, result.concurrency, result.p50, result.p90, result.p95));
            }
            
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing CSV: " + e.getMessage());
        }
    }

    // Benchmark concurrent C++ cache
    public static void benchmarkConcurrentCache() {
        System.out.println("Concurrent C++ Cache Benchmark");
        System.out.println("==============================");
        
        int[] concurrencyLevels = {2, 4, 6, 8, 10, 12, 14, 16, 20, 32, 64};
        int totalOps = 100000;
        List<ConcurrentBenchmarkResult> results = new ArrayList<>();
        
        for (int concurrency : concurrencyLevels) {
            System.out.printf("Testing with %d threads...\n", concurrency);
            
            // Create shared cache
            Cache cache = new Cache();
            
            try {
                // Populate maps
                for (int i = 0; i < 10000; i++) {
                    String key = "key_" + i;
                    String value = "value_" + i;
                    cache.populateOrderedMap(key, value);
                    cache.populateHashMap(key, value);
                }
                
                // Benchmark hash map lookups
                ConcurrentBenchmarkResult result = runConcurrentBenchmark("Hash Map Lookup", concurrency, totalOps, (ops) -> {
                    long[] times = new long[ops];
                    for (int i = 0; i < ops; i++) {
                        String key = "key_" + (i % 10000);
                        long start = System.nanoTime();
                        cache.lookupHash(key);
                        times[i] = System.nanoTime() - start;
                    }
                    return times;
                });
                
                System.out.printf("  Concurrency %d - P50: %d ns, P90: %d ns, P95: %d ns\n", 
                    concurrency, result.p50, result.p90, result.p95);
                
                results.add(result);
                    
            } finally {
                cache.destroy();
            }
        }
        
        // Write results to CSV
        writeCSVResults("benchmark_results.csv", results, "Java", "cpp_wrapper");
    }

    // Benchmark concurrent Java native cache
    public static void benchmarkConcurrentJavaNativeCache() {
        System.out.println("Concurrent Java Native Cache Benchmark");
        System.out.println("======================================");
        
        int[] concurrencyLevels = {2, 4, 6, 8, 10, 12, 14, 16, 20, 32, 64};
        int totalOps = 100000;
        List<ConcurrentBenchmarkResult> results = new ArrayList<>();
        
        for (int concurrency : concurrencyLevels) {
            System.out.printf("Testing with %d threads...\n", concurrency);
            
            // Create shared cache with ConcurrentHashMap (thread-safe)
            ConcurrentHashMap<String, String> hashMap = new ConcurrentHashMap<>();
            
            // Populate maps
            for (int i = 0; i < 10000; i++) {
                String key = "key_" + i;
                String value = "value_" + i;
                hashMap.put(key, value);
            }
            
            // Benchmark hash map lookups
            ConcurrentBenchmarkResult result = runConcurrentBenchmark("Hash Map Lookup", concurrency, totalOps, (ops) -> {
                long[] times = new long[ops];
                for (int i = 0; i < ops; i++) {
                    String key = "key_" + (i % 10000);
                    long start = System.nanoTime();
                    hashMap.get(key);
                    times[i] = System.nanoTime() - start;
                }
                return times;
            });
            
            System.out.printf("  Concurrency %d - P50: %d ns, P90: %d ns, P95: %d ns\n", 
                concurrency, result.p50, result.p90, result.p95);
            
            results.add(result);
        }
        
        // Write results to CSV
        writeCSVResults("benchmark_results.csv", results, "Java", "native");
    }
} 