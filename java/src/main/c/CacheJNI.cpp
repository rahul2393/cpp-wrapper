#include <jni.h>
#include <string>
#include "cache.h"

extern "C" {

JNIEXPORT jlong JNICALL Java_Cache_cacheCreate(JNIEnv* env, jobject obj) {
    return (jlong)cache_create();
}

JNIEXPORT void JNICALL Java_Cache_cacheDestroy(JNIEnv* env, jobject obj, jlong handle) {
    cache_destroy((CacheHandle)handle);
}

JNIEXPORT void JNICALL Java_Cache_cachePopulateOrderedMap(JNIEnv* env, jobject obj, jlong handle, jstring key, jstring value) {
    const char* keyStr = env->GetStringUTFChars(key, 0);
    const char* valueStr = env->GetStringUTFChars(value, 0);
    
    cache_populate_ordered_map((CacheHandle)handle, keyStr, valueStr);
    
    env->ReleaseStringUTFChars(key, keyStr);
    env->ReleaseStringUTFChars(value, valueStr);
}

JNIEXPORT void JNICALL Java_Cache_cachePopulateHashMap(JNIEnv* env, jobject obj, jlong handle, jstring key, jstring value) {
    const char* keyStr = env->GetStringUTFChars(key, 0);
    const char* valueStr = env->GetStringUTFChars(value, 0);
    
    cache_populate_hash_map((CacheHandle)handle, keyStr, valueStr);
    
    env->ReleaseStringUTFChars(key, keyStr);
    env->ReleaseStringUTFChars(value, valueStr);
}

JNIEXPORT void JNICALL Java_Cache_cacheSetProto(JNIEnv* env, jobject obj, jlong handle, jstring key, jbyteArray protoData, jint protoSize) {
    const char* keyStr = env->GetStringUTFChars(key, 0);
    jbyte* data = env->GetByteArrayElements(protoData, 0);
    
    cache_set_proto((CacheHandle)handle, keyStr, (const char*)data, protoSize);
    
    env->ReleaseByteArrayElements(protoData, data, JNI_ABORT);
    env->ReleaseStringUTFChars(key, keyStr);
}

JNIEXPORT jbyteArray JNICALL Java_Cache_cacheGetProto(JNIEnv* env, jobject obj, jlong handle, jstring key) {
    const char* keyStr = env->GetStringUTFChars(key, 0);
    int size;
    const char* data = cache_get_proto((CacheHandle)handle, keyStr, &size);
    
    jbyteArray result = env->NewByteArray(size);
    env->SetByteArrayRegion(result, 0, size, (jbyte*)data);
    
    env->ReleaseStringUTFChars(key, keyStr);
    return result;
}

JNIEXPORT jstring JNICALL Java_Cache_cacheLookupOrdered(JNIEnv* env, jobject obj, jlong handle, jstring key) {
    const char* keyStr = env->GetStringUTFChars(key, 0);
    const char* result = cache_lookup_ordered((CacheHandle)handle, keyStr);
    
    jstring jresult = env->NewStringUTF(result);
    env->ReleaseStringUTFChars(key, keyStr);
    
    return jresult;
}

JNIEXPORT jstring JNICALL Java_Cache_cacheLookupHash(JNIEnv* env, jobject obj, jlong handle, jstring key) {
    const char* keyStr = env->GetStringUTFChars(key, 0);
    const char* result = cache_lookup_hash((CacheHandle)handle, keyStr);
    
    jstring jresult = env->NewStringUTF(result);
    env->ReleaseStringUTFChars(key, keyStr);
    
    return jresult;
}

JNIEXPORT jlong JNICALL Java_Cache_cacheGetOrderedLookupTimeNs(JNIEnv* env, jobject obj, jlong handle) {
    return cache_get_ordered_lookup_time_ns((CacheHandle)handle);
}

JNIEXPORT jlong JNICALL Java_Cache_cacheGetHashLookupTimeNs(JNIEnv* env, jobject obj, jlong handle) {
    return cache_get_hash_lookup_time_ns((CacheHandle)handle);
}

} 