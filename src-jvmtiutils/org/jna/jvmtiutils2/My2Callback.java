package org.jna.jvmtiutils2;

import com.sun.nik.Callback;

public interface My2Callback extends Callback {

    void jvmtiHeapReferenceCallback();

}
