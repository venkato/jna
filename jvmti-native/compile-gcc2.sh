rm jna-native-utils.so

gcc -fPIC -I/usr/lib/jvm/java-8-oracle/include -I/usr/lib/jvm/java-8-oracle/include/linux -g -static-libgcc -nodefaultlibs  -shared -o jna-native-utils.so nativetest-thread6.c

ls -la jna-native-utils.so
#gcc -fPIC -I/usr/lib/jvm/java-8-oracle/include -I/usr/lib/jvm/java-8-oracle/include/linux -g -static-libgcc -shared -o jna-native-utils.so nativetest-thread6.c

# gcc -I/include -g -static-libgcc -shared -o jna-native-utils.so  nativetest-thread6.c