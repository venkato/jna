cls
@rm -rf jna-native-utils.dll 
@rm -rf jna-native-utils.dll
rem c/progi/java/zulu11.45.27-ca-jdk11.0.10-win_x64/
x86_64-w64-mingw32-gcc  -I/cygdrive/c/progi/java/zulu11.45.27-ca-jdk11.0.10-win_x64/include/win32 -I/cygdrive/c/progi/java/zulu11.45.27-ca-jdk11.0.10-win_x64/include -g -Wl,--add-stdcall-alias -shared -o jna-native-utils.dll nativetest-thread6.c

rem x86_64-w64-mingw32-gcc  -I/cygdrive/c/progi/java/zulu11.45.27-ca-jdk11.0.10-win_x64/include/win32 -I/cygdrive/c/progi/java/zulu11.45.27-ca-jdk11.0.10-win_x64/include -g -Wl,--add-stdcall-alias -shared -o jna-native-utils.dll nativetest-thread6.c

cp -f jna-native-utils.dll ..\dist\jna-native-utils64.dll