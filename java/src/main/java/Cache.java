public class Cache {
    static {
        System.loadLibrary("cache_lib");
    }

    private long handle;

    public Cache() {
        this.handle = cacheCreate();
    }

    public void destroy() {
        if (handle != 0) {
            cacheDestroy(handle);
            handle = 0;
        }
    }

    public void populateOrderedMap(String key, String value) {
        cachePopulateOrderedMap(handle, key, value);
    }

    public void populateHashMap(String key, String value) {
        cachePopulateHashMap(handle, key, value);
    }

    public void setProto(String key, byte[] protoData) {
        cacheSetProto(handle, key, protoData, protoData.length);
    }

    public byte[] getProto(String key) {
        return cacheGetProto(handle, key);
    }

    public String lookupOrdered(String key) {
        return cacheLookupOrdered(handle, key);
    }

    public String lookupHash(String key) {
        return cacheLookupHash(handle, key);
    }

    public long getOrderedLookupTimeNs() {
        return cacheGetOrderedLookupTimeNs(handle);
    }

    public long getHashLookupTimeNs() {
        return cacheGetHashLookupTimeNs(handle);
    }

    // Native method declarations
    private native long cacheCreate();
    private native void cacheDestroy(long handle);
    private native void cachePopulateOrderedMap(long handle, String key, String value);
    private native void cachePopulateHashMap(long handle, String key, String value);
    private native void cacheSetProto(long handle, String key, byte[] protoData, int protoSize);
    private native byte[] cacheGetProto(long handle, String key);
    private native String cacheLookupOrdered(long handle, String key);
    private native String cacheLookupHash(long handle, String key);
    private native long cacheGetOrderedLookupTimeNs(long handle);
    private native long cacheGetHashLookupTimeNs(long handle);
} 