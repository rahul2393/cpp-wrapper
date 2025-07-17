import ctypes
import ctypes.util
import os
import sys
from typing import Optional

class Cache:
    def __init__(self):
        # Load the C++ library
        lib_path = os.path.join(os.path.dirname(__file__), '..', 'cpp', 'build', 'lib')
        
        # Try to find the library
        if sys.platform == "darwin":
            lib_name = "libcache_lib.dylib"
        else:
            lib_name = "libcache_lib.so"
        
        lib_path_full = os.path.join(lib_path, lib_name)
        
        if not os.path.exists(lib_path_full):
            raise FileNotFoundError(f"Cache library not found at {lib_path_full}")
        
        self.lib = ctypes.CDLL(lib_path_full)
        
        # Set up function signatures
        self.lib.cache_create.restype = ctypes.c_void_p
        self.lib.cache_destroy.argtypes = [ctypes.c_void_p]
        self.lib.cache_populate_ordered_map.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p]
        self.lib.cache_populate_hash_map.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p]
        self.lib.cache_set_proto.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_char_p, ctypes.c_int]
        self.lib.cache_get_proto.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.POINTER(ctypes.c_int)]
        self.lib.cache_get_proto.restype = ctypes.c_char_p
        self.lib.cache_lookup_ordered.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
        self.lib.cache_lookup_ordered.restype = ctypes.c_char_p
        self.lib.cache_lookup_hash.argtypes = [ctypes.c_void_p, ctypes.c_char_p]
        self.lib.cache_lookup_hash.restype = ctypes.c_char_p
        
        # Create cache handle
        self.handle = self.lib.cache_create()
        if not self.handle:
            raise RuntimeError("Failed to create cache")
    
    def destroy(self):
        """Destroy the cache and free resources"""
        if self.handle:
            self.lib.cache_destroy(self.handle)
            self.handle = None
    
    def populate_ordered_map(self, key: str, value: str):
        """Populate the ordered map with key-value pair"""
        self.lib.cache_populate_ordered_map(
            self.handle,
            key.encode('utf-8'),
            value.encode('utf-8')
        )
    
    def populate_hash_map(self, key: str, value: str):
        """Populate the hash map with key-value pair"""
        self.lib.cache_populate_hash_map(
            self.handle,
            key.encode('utf-8'),
            value.encode('utf-8')
        )
    
    def set_proto(self, key: str, proto_data: bytes):
        """Set proto data for the given key"""
        self.lib.cache_set_proto(
            self.handle,
            key.encode('utf-8'),
            proto_data,
            len(proto_data)
        )
    
    def get_proto(self, key: str) -> Optional[bytes]:
        """Get proto data for the given key"""
        size = ctypes.c_int()
        data = self.lib.cache_get_proto(
            self.handle,
            key.encode('utf-8'),
            ctypes.byref(size)
        )
        if data and size.value > 0:
            return ctypes.string_at(data, size.value)
        return None
    
    def lookup_ordered(self, key: str) -> Optional[str]:
        """Lookup value in ordered map"""
        result = self.lib.cache_lookup_ordered(
            self.handle,
            key.encode('utf-8')
        )
        if result:
            return result.decode('utf-8')
        return None
    
    def lookup_hash(self, key: str) -> Optional[str]:
        """Lookup value in hash map"""
        result = self.lib.cache_lookup_hash(
            self.handle,
            key.encode('utf-8')
        )
        if result:
            return result.decode('utf-8')
        return None
    
    def __enter__(self):
        return self
    
    def __exit__(self, exc_type, exc_val, exc_tb):
        self.destroy() 