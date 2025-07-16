#pragma once

#include <map>
#include <unordered_map>
#include <string>
#include <chrono>
#include "cache.pb.h"

extern "C" {

// C-style interface for Go and Java
typedef void* CacheHandle;

// Cache operations
CacheHandle cache_create();
void cache_destroy(CacheHandle handle);

// Map operations
void cache_populate_ordered_map(CacheHandle handle, const char* key, const char* value);
void cache_populate_hash_map(CacheHandle handle, const char* key, const char* value);

// Proto operations
void cache_set_proto(CacheHandle handle, const char* key, const char* proto_data, int proto_size);
const char* cache_get_proto(CacheHandle handle, const char* key, int* proto_size);

// Lookup operations
const char* cache_lookup_ordered(CacheHandle handle, const char* key);
const char* cache_lookup_hash(CacheHandle handle, const char* key);

// Benchmark helpers
long long cache_get_ordered_lookup_time_ns(CacheHandle handle);
long long cache_get_hash_lookup_time_ns(CacheHandle handle);

} 