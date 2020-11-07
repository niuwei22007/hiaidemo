# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("hiaidemo")

set(CMAKE_VERBOSE_MAKEFILE on)
set(libs "${CMAKE_CURRENT_SOURCE_DIR}/../../../libs")

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../src/main/cpp/include)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/../../../libs/${ANDROID_ABI})

add_library(hiai SHARED IMPORTED)
set_target_properties(hiai PROPERTIES
        IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libhiai.so")

add_library(hiai_ir SHARED IMPORTED)
set_target_properties(hiai_ir PROPERTIES
        IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libhiai_ir.so")

add_library(hiai_ir_build SHARED IMPORTED)
set_target_properties(hiai_ir_build PROPERTIES
        IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libhiai_ir_build.so")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        hiaijni

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        hiaijni.cpp
        createModel.cpp
        buildmodel.cpp
        classify_jni.cpp
        classify_async_jni.cpp
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        hiaijni

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        android hiai hiai_ir hiai_ir_build
        )