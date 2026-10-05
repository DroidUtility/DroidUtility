cmake_minimum_required(VERSION 3.22.1)
project("droidutility_terminal")

add_library(droidutility_terminal SHARED ghostty_jni.cpp)

target_include_directories(droidutility_terminal PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/vendor/include)

target_link_libraries(droidutility_terminal
    ${CMAKE_CURRENT_SOURCE_DIR}/vendor/${ANDROID_ABI}/libghostty-vt.a
    android
    log)
