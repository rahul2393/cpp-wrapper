public class Main {
    public static void main(String[] args) {
        System.out.println("Java Cache Benchmark (JNI backend)");
        System.out.println("===================");
        benchmarkCache();
        System.out.println("\n------------------------------\n");
        benchmarkJavaNativeCache();
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
            long totalOrderedTime = 0;
            int iterations = 100000;
            for (int i = 0; i < iterations; i++) {
                String key = "key_" + (i % 10000);
                long start = System.nanoTime();
                String result = cache.lookupOrdered(key);
                totalOrderedTime += (System.nanoTime() - start);
                if (result == null) {
                    System.out.println("Unexpected null result");
                }
            }
            double avgOrderedTime = (double) totalOrderedTime / iterations;

            // Benchmark hash map lookups
            System.out.println("Benchmarking hash map lookups...");
            long totalHashTime = 0;
            for (int i = 0; i < iterations; i++) {
                String key = "key_" + (i % 10000);
                long start = System.nanoTime();
                String result = cache.lookupHash(key);
                totalHashTime += (System.nanoTime() - start);
                if (result == null) {
                    System.out.println("Unexpected null result");
                }
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
        long totalOrderedTime = 0;
        int iterations = 100000;
        for (int i = 0; i < iterations; i++) {
            String key = "key_" + (i % 10000);
            long start = System.nanoTime();
            orderedMap.get(key);
            totalOrderedTime += (System.nanoTime() - start);
        }
        double avgOrderedTime = (double) totalOrderedTime / iterations;

        // Benchmark hash map lookups
        System.out.println("Benchmarking hash map lookups...");
        long totalHashTime = 0;
        for (int i = 0; i < iterations; i++) {
            String key = "key_" + (i % 10000);
            long start = System.nanoTime();
            hashMap.get(key);
            totalHashTime += (System.nanoTime() - start);
        }
        double avgHashTime = (double) totalHashTime / iterations;

        // Benchmark proto operations
        System.out.println("Benchmarking proto operations...");
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            dataMap.get("test_proto");
        }
        long end = System.nanoTime();
        long protoTime = (end - start) / 10000;

        System.out.printf("\nResults (Java Native):\n");
        System.out.printf("Average ordered map lookup time: %.2f ns\n", avgOrderedTime);
        System.out.printf("Average hash map lookup time: %.2f ns\n", avgHashTime);
        System.out.printf("Average proto get time: %d ns\n", protoTime);
    }
} 