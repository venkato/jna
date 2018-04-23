package org.jna.jvmtiutils2;

import net.sf.jremoterun.utilities.JrrUtilities;

import com.sun.nik.Native;
import com.sun.nik.Pointer;
import com.sun.nik.win32.StdCallLibrary;

public interface JvmDll extends StdCallLibrary {

    JvmDll INSTANCE = (JvmDll) Native.loadLibrary("jvm55", JvmDll.class);

    int _JVM_IsSupportedJNIVersion(int a);

    void _JVM_DumpAllStacks(Pointer pointer, Class class1);
}
