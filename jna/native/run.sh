rm ../build/native-linux-x86-64/libjnidispatch.so

gcc -m64 -m64 -o /w/c/newjava/jna/2015.01.25-3/jna/build/native-linux-x86-64/libjnidispatch.so -static-libgcc -nodefaultlibs -shared -Wl,-soname,/w/c/newjava/jna/2015.01.25-3/jna/build/native-linux-x86-64/libjnidispatch.so /w/c/newjava/jna/2015.01.25-3/jna/build/native-linux-x86-64/dispatch.o /w/c/newjava/jna/2015.01.25-3/jna/build/native-linux-x86-64/callback.o /w/c/newjava/jna/2015.01.25-3/jna/build/native-linux-x86-64/libffi/.libs/libffi.a

ldd /w/c/newjava/jna/2015.01.25-3/jna/build/native-linux-x86-64/libjnidispatch.so
