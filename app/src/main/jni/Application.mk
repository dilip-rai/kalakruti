# Use clang compiler
#
NDK_TOOLCHAIN_VERSION   := clang

# Target Android 4.3.0 or higher
#
APP_PLATFORM    := android-18

# Compile for ARM and Intel x86 processors
#
APP_ABI := armeabi-v7a

# Use the LLVM libc++ as a shared library.
#
APP_STL := c++_static

# Enable exceptions and RTTI
#
APP_CPPFLAGS += -fexceptions -frtti -Werror
