cls
@rm -rf jna-native-utils.dll 
@rm -rf jna-native-utils.dll
x86_64-w64-mingw32-gcc  -I/cygdrive/c/progi/java/jdk1.7.0_51-64/include/win32 -I/cygdrive/c/progi/java/jdk1.7.0_51-64/include -g -Wl,--add-stdcall-alias -shared -o jna-native-utils.dll nativetest-thread6.c

cp -f jna-native-utils.dll ..\dist\jna-native-utils64.dll