#!/usr/bin/env python3
"""
Simple test script to verify the Python cache implementation works correctly.
"""

from cache import Cache

def test_cache():
    """Test basic cache functionality"""
    print("Testing Python Cache Implementation...")
    
    cache = Cache()
    
    try:
        # Test ordered map operations
        print("Testing ordered map...")
        cache.populate_ordered_map("test_key", "test_value")
        result = cache.lookup_ordered("test_key")
        assert result == "test_value", f"Expected 'test_value', got '{result}'"
        print("✓ Ordered map test passed")
        
        # Test hash map operations
        print("Testing hash map...")
        cache.populate_hash_map("hash_key", "hash_value")
        result = cache.lookup_hash("hash_key")
        assert result == "hash_value", f"Expected 'hash_value', got '{result}'"
        print("✓ Hash map test passed")
        
        # Test proto operations
        print("Testing proto operations...")
        test_data = b"Hello, World! This is test proto data."
        cache.set_proto("proto_key", test_data)
        result = cache.get_proto("proto_key")
        assert result == test_data, f"Expected {test_data}, got {result}"
        print("✓ Proto operations test passed")
        
        # Test non-existent keys
        print("Testing non-existent keys...")
        result = cache.lookup_ordered("nonexistent")
        assert result is None, f"Expected None for non-existent key, got '{result}'"
        result = cache.lookup_hash("nonexistent")
        assert result is None, f"Expected None for non-existent key, got '{result}'"
        print("✓ Non-existent key test passed")
        
        print("\nAll tests passed! ✓")
        
    except Exception as e:
        print(f"Test failed: {e}")
        raise
    finally:
        cache.destroy()

if __name__ == "__main__":
    test_cache() 