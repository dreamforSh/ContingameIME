cmake_minimum_required(VERSION 3.20)
project(contingameime_jni CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# JNI headers. $JAVA_HOME is often empty in the Gradle shell, so Gradle passes -DJAVA_HOME
# (the Gradle JVM's java.home); fall back to CMake's FindJNI otherwise.
if(DEFINED JAVA_HOME AND NOT JAVA_HOME STREQUAL "")
    set(JNI_INCLUDE_DIRS "${JAVA_HOME}/include" "${JAVA_HOME}/include/win32")
else()
    find_package(JNI REQUIRED)
endif()

# Output name matches the Java loader: jni.dll (x64) / jni-x86.dll (x86).
if(CMAKE_SIZEOF_VOID_P EQUAL 4)
    set(OUT_NAME "jni-x86")
else()
    set(OUT_NAME "jni")
endif()

# The single JNI translation unit pulls in the IngameIME library unity-style via its
# #include "IngameIME-JNI/IngameIME/IngameIME/IMM.cpp" (add the submodule first, see
# docs/NATIVE_BUILD.md). The IMM backend is the one instantiated by the Java side.
add_library(${OUT_NAME} SHARED city_windmill_ingameime_client_jni_ExternalBaseIME.cpp)
set_target_properties(${OUT_NAME} PROPERTIES PREFIX "" OUTPUT_NAME ${OUT_NAME})
target_include_directories(${OUT_NAME} PRIVATE ${JNI_INCLUDE_DIRS} ${CMAKE_CURRENT_SOURCE_DIR})
target_compile_definitions(${OUT_NAME} PRIVATE _JNI_IMPLEMENTATION_ UNICODE _UNICODE)

# IMM backend needs imm32; TSF backend (if later enabled) needs ole32/oleaut32.
target_link_libraries(${OUT_NAME} PRIVATE imm32 ole32 oleaut32)

if(MINGW)
    target_link_options(${OUT_NAME} PRIVATE
        -Wl,--kill-at                 # strip @N stdcall decoration (required on x86, no-op on x64)
        -static-libgcc -static-libstdc++
        -Wl,-Bstatic -lwinpthread -Wl,-Bdynamic)  # self-contained: no libgcc/libstdc++/winpthread deps
endif()

# Land the DLL straight into the resources natives dir the Java loader reads from.
# RES_NATIVES_DIR is passed by the Gradle cmakeConfigure task.
if(DEFINED RES_NATIVES_DIR)
    install(TARGETS ${OUT_NAME} RUNTIME DESTINATION "${RES_NATIVES_DIR}")
endif()
