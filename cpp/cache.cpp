#include "cache.h"
#include <iostream>
#include <memory>
#include <chrono>

class Cache {
private:
    std::map<std::string, std::string> ordered_map;
    std::unordered_map<std::string, std::string> hash_map;
    std::unordered_map<std::string, std::string> proto_cache;
    
    long long ordered_lookup_time_ns = 0;
    long long hash_lookup_time_ns = 0;

public:
    void populate_ordered_map(const std::string& key, const std::string& value) {
        ordered_map[key] = value;
    }
    
    void populate_hash_map(const std::string& key, const std::string& value) {
        hash_map[key] = value;
    }
    
    void set_proto(const std::string& key, const std::string& proto_data) {
        proto_cache[key] = proto_data;
    }
    
    std::string get_proto(const std::string& key) {
        auto it = proto_cache.find(key);
        return (it != proto_cache.end()) ? it->second : "";
    }
    
    std::string lookup_ordered(const std::string& key) {
        auto start = std::chrono::high_resolution_clock::now();
        auto it = ordered_map.find(key);
        auto end = std::chrono::high_resolution_clock::now();
        
        ordered_lookup_time_ns = std::chrono::duration_cast<std::chrono::nanoseconds>(end - start).count();
        return (it != ordered_map.end()) ? it->second : "";
    }
    
    std::string lookup_hash(const std::string& key) {
        auto start = std::chrono::high_resolution_clock::now();
        auto it = hash_map.find(key);
        auto end = std::chrono::high_resolution_clock::now();
        
        hash_lookup_time_ns = std::chrono::duration_cast<std::chrono::nanoseconds>(end - start).count();
        return (it != hash_map.end()) ? it->second : "";
    }
    
    long long get_ordered_lookup_time_ns() const { return ordered_lookup_time_ns; }
    long long get_hash_lookup_time_ns() const { return hash_lookup_time_ns; }
};

// Global storage for returned strings (simplified memory management)
static std::string last_returned_string;

extern "C" {

CacheHandle cache_create() {
    return new Cache();
}

void cache_destroy(CacheHandle handle) {
    delete static_cast<Cache*>(handle);
}

void cache_populate_ordered_map(CacheHandle handle, const char* key, const char* value) {
    auto cache = static_cast<Cache*>(handle);
    cache->populate_ordered_map(key, value);
}

void cache_populate_hash_map(CacheHandle handle, const char* key, const char* value) {
    auto cache = static_cast<Cache*>(handle);
    cache->populate_hash_map(key, value);
}

void cache_set_proto(CacheHandle handle, const char* key, const char* proto_data, int proto_size) {
    auto cache = static_cast<Cache*>(handle);
    std::string data(proto_data, proto_size);
    cache->set_proto(key, data);
}

const char* cache_get_proto(CacheHandle handle, const char* key, int* proto_size) {
    auto cache = static_cast<Cache*>(handle);
    last_returned_string = cache->get_proto(key);
    *proto_size = last_returned_string.size();
    return last_returned_string.c_str();
}

const char* cache_lookup_ordered(CacheHandle handle, const char* key) {
    auto cache = static_cast<Cache*>(handle);
    last_returned_string = cache->lookup_ordered(key);
    return last_returned_string.c_str();
}

const char* cache_lookup_hash(CacheHandle handle, const char* key) {
    auto cache = static_cast<Cache*>(handle);
    last_returned_string = cache->lookup_hash(key);
    return last_returned_string.c_str();
}

long long cache_get_ordered_lookup_time_ns(CacheHandle handle) {
    auto cache = static_cast<Cache*>(handle);
    return cache->get_ordered_lookup_time_ns();
}

long long cache_get_hash_lookup_time_ns(CacheHandle handle) {
    auto cache = static_cast<Cache*>(handle);
    return cache->get_hash_lookup_time_ns();
}

} 