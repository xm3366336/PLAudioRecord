# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

project(mp3lame)

# 限定cmake支持最低版本
# cmake_minimum_required(VERSION 3.4.1)
cmake_minimum_required(VERSION 3.10)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

# 指定so生成到libs目录
set(LIBMP3_DIR ${CMAKE_SOURCE_DIR}/libs)

include_directories(libs/include)
include_directories(./include)
include_directories(${CMAKE_CURRENT_LIST_DIR}/include)

# 设置生成的so动态库最后输出的路径，中间件也会在这儿
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/libs-output/${ANDROID_ABI})

# 指定用到的系统库或者NDK库或者第三方库的搜索路径，可选。
# LINK_DIRECTORIES(/usr/local/lib)

# 添加已有的so库
add_library(
        # 从已有的libs中找到libmp3lame.so库
        mp3lame

        # STATIC：静态库，是目标文件的归档文件，在链接其它目标的时候使用
        # SHARED：动态库，会被动态链接，在运行时被加载
        # MODULE：模块库，是不会被链接到其它目标中的插件，但是可能会在运行时使用dlopen-系列的函数动态链接
        SHARED

        # 资源文件的路径，可以是多个资源文件，IMPORTED是导入
        IMPORTED
)

# 将c文件编译成so库
add_library(
        # 生成libmp3recorder-lib.so的库
        mp3recorder-lib
        # 动态库
        SHARED
        # 资源文件的路径
        cpp/lame_util.c
)

# 从系统库中查找依赖库
find_library(
        # 设置依赖库的名字，下面链接库的时候会用到
        log-lib

        # 查找log依赖库
        # {sdk-path}/ndk-bundle/sysroot/usr/include/android/log.h
        log
)

set_target_properties(
        mp3lame
        PROPERTIES
        IMPORTED_LOCATION ${LIBMP3_DIR}/${ANDROID_ABI}/libmp3lame.so
)

# 配置库的依赖关系(链接关系)
target_link_libraries(
        # 目标库
        mp3recorder-lib
        # 依赖库，可以是多个
        mp3lame
        # 依赖库，可以是多个
        # ${log-lib}
)