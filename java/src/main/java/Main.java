public class Main {
    public static void main(String[] args) {
        System.out.println("Java Cache Benchmark");
        System.out.println("===================");
        benchmarkCache();
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
            System.out.println("\nBenchmarking ordered map lookups...");
            long totalOrderedTime = 0;
            int iterations = 100000;
            for (int i = 0; i < iterations; i++) {
                String key = "key_" + (i % 10000);
                cache.lookupOrdered(key);
                totalOrderedTime += cache.getOrderedLookupTimeNs();
            }
            double avgOrderedTime = (double) totalOrderedTime / iterations;

            // Benchmark hash map lookups
            System.out.println("Benchmarking hash map lookups...");
            long totalHashTime = 0;
            for (int i = 0; i < iterations; i++) {
                String key = "key_" + (i % 10000);
                cache.lookupHash(key);
                totalHashTime += cache.getHashLookupTimeNs();
            }
            double avgHashTime = (double) totalHashTime / iterations;

            // Benchmark proto operations
            System.out.println("Benchmarking proto operations...");
            long startTime = System.nanoTime();
            for (int i = 0; i < 10000; i++) {
                cache.getProto("test_proto");
            }
            long endTime = System.nanoTime();
            long protoTime = (endTime - startTime) / 10000;

            System.out.printf("\nResults:\n");
            System.out.printf("Average ordered map lookup time: %.2f ns\n", avgOrderedTime);
            System.out.printf("Average hash map lookup time: %.2f ns\n", avgHashTime);
            System.out.printf("Average proto get time: %d ns\n", protoTime);

        } finally {
            cache.destroy();
        }
    }
} 