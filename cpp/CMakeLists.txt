cmake_minimum_required(VERSION 3.16)
project(cache_lib)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find Protobuf
find_package(Protobuf REQUIRED)

# Generate protobuf files
protobuf_generate_cpp(PROTO_SRCS PROTO_HDRS proto/cache.proto)

# Create shared library
add_library(cache_lib SHARED
    cache.cpp
    ${PROTO_SRCS}
)

# Include directories
target_include_directories(cache_lib PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${Protobuf_INCLUDE_DIRS}
)

# Link libraries
target_link_libraries(cache_lib
    ${Protobuf_LIBRARIES}
)

# Set output directory
set_target_properties(cache_lib PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib
    RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin
) 