# Java Profiling Guide

## Overview
This guide explains how to read and analyze Java profiling data to understand performance characteristics of your applications.

## Types of Java Profiling

### 1. **GC Logs (What we used)**
The simplest form of profiling that shows garbage collection activity.

**How to read GC logs:**
```
[0.401s][info][gc,heap,exit] Heap
[0.401s][info][gc,heap,exit]  garbage-first heap   total reserved 4194304K, committed 264192K, used 48194K
```

**What this means:**
- `total reserved 4194304K`: Maximum heap size (4GB)
- `committed 264192K`: Memory actually allocated by OS (264MB)
- `used 48194K`: Memory actually used by application (48MB)
- `[0.401s]`: Timestamp when this was logged

**Key insights:**
- If `used` is much smaller than `committed`, you have memory overhead
- If GC frequency is high, you have memory pressure
- No GC logs during benchmark = good (no memory pressure)

### 2. **Java Flight Recorder (JFR)**
Advanced profiling tool built into the JVM.

**How to enable:**
```bash
java -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=profile.jfr Main
```

**How to analyze:**
```bash
# Command line analysis
jfr print profile.jfr
jfr summary profile.jfr

# GUI analysis (if available)
jmc profile.jfr
```

**What JFR shows:**
- CPU usage by method
- Memory allocation patterns
- GC activity
- I/O operations
- Thread activity

### 3. **Async-Profiler**
Low-overhead sampling profiler.

**How to use:**
```bash
# Install async-profiler
# Run with profiling
java -agentpath:async-profiler/libasyncProfiler.so=start,event=cpu,file=profile.html Main
```

## Reading Our Benchmark Results

### **Timing Breakdown Analysis**

From our Java benchmark:
```
Population: 40374792 ns (40.4ms)
Ordered lookups total: 142599417 ns (142.6ms) (1425.99 ns/op)
Hash lookups total: 56211667 ns (56.2ms) (562.12 ns/op)
Proto operations total: 6933500 ns (6.9ms) (693.35 ns/op)
```

**How to interpret:**
1. **Population time**: How long it takes to populate the cache
   - JNI: 40.4ms (slow due to C++ calls)
   - Native: 8.3ms (much faster)

2. **Lookup times**: Time per operation
   - JNI ordered: 1425.99 ns/op
   - Native ordered: 169.42 ns/op
   - **Overhead**: ~8.4x slower with JNI

3. **Proto operations**: Data retrieval
   - JNI: 693.35 ns/op
   - Native: 26.51 ns/op
   - **Overhead**: ~26x slower with JNI

### **Memory Analysis**

```
Heap: total reserved 4194304K, committed 264192K, used 48194K
```

**Memory efficiency:**
- **Reserved**: 4GB (maximum possible)
- **Committed**: 264MB (actually allocated)
- **Used**: 48MB (actually used)
- **Efficiency**: 48MB/264MB = 18% (good)

## Advanced Profiling Techniques

### **1. Method-Level Profiling**

Add to your Java code:
```java
// Profile specific methods
long start = System.nanoTime();
// ... your code ...
long duration = System.nanoTime() - start;
System.out.println("Method took: " + duration + " ns");
```

### **2. Memory Allocation Profiling**

```java
// Track memory allocations
Runtime runtime = Runtime.getRuntime();
long before = runtime.totalMemory() - runtime.freeMemory();
// ... your code ...
long after = runtime.totalMemory() - runtime.freeMemory();
System.out.println("Memory allocated: " + (after - before) + " bytes");
```

### **3. GC Monitoring**

```bash
# Enable detailed GC logging
java -XX:+UseG1GC -Xlog:gc*:file=gc.log Main

# Analyze GC log
grep "GC" gc.log | tail -10
```

## Interpreting Performance Data

### **Red Flags to Watch For:**

1. **High GC frequency**: Too many objects being created
2. **Large memory overhead**: Committed >> Used
3. **Slow population**: Inefficient data structure setup
4. **High lookup variance**: Inconsistent performance

### **Good Performance Indicators:**

1. **Low GC activity**: Efficient memory usage
2. **Consistent timing**: Predictable performance
3. **Small memory footprint**: Efficient data structures
4. **Fast population**: Quick setup

## Tools for Analysis

### **Built-in Tools:**
- `jstack`: Thread analysis
- `jmap`: Memory analysis
- `jstat`: GC statistics
- `jcmd`: General JVM commands

### **External Tools:**
- **JProfiler**: Commercial profiler
- **YourKit**: Commercial profiler
- **VisualVM**: Free GUI profiler
- **async-profiler**: Low-overhead profiler

## Example Analysis Commands

```bash
# Get thread dump
jstack <pid> > threads.txt

# Get heap dump
jmap -dump:format=b,file=heap.hprof <pid>

# Monitor GC
jstat -gc <pid> 1000

# Flight Recorder analysis
jfr print java_profile.jfr | grep "Main.benchmark"
```

## Key Takeaways from Our Benchmark

1. **JNI overhead is substantial**: 4-26x slower than native
2. **Data transfer is expensive**: Proto operations show biggest difference
3. **Memory usage is efficient**: No GC pressure
4. **Native Java collections are fast**: Comparable to Go native performance

The profiling clearly shows that for simple operations like caching, native implementations are dramatically better than JNI/CGO approaches. 