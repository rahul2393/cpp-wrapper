cmake_minimum_required(VERSION 3.16)
project(java_cache_lib)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find Java
find_package(JNI REQUIRED)

# Find Protobuf
find_package(Protobuf REQUIRED)

# Generate protobuf files
protobuf_generate_cpp(PROTO_SRCS PROTO_HDRS ../proto/cache.proto)

# Create JNI shared library
add_library(cache_jni SHARED
    src/main/c/CacheJNI.cpp
    ${PROTO_SRCS}
)

# Include directories
target_include_directories(cache_jni PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/../cpp
    ${JNI_INCLUDE_DIRS}
    ${Protobuf_INCLUDE_DIRS}
)

# Link libraries
target_link_libraries(cache_jni
    ${JNI_LIBRARIES}
    ${Protobuf_LIBRARIES}
)

# Set output directory
set_target_properties(cache_jni PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib
    RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin
) 