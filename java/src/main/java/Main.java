import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("Java Cache Benchmark (JNI backend)");
        System.out.println("===================");
        benchmarkCache();
        System.out.println("\n------------------------------\n");
        benchmarkJavaNativeCache();
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
} 