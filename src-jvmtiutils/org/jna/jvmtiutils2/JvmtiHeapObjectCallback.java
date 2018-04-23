package org.jna.jvmtiutils2;

import com.sun.nik.Callback;
import com.sun.nik.Pointer;

public interface JvmtiHeapObjectCallback extends Callback {

    int jvmtiIterationControl(long class_tag, long size, Pointer tag_ptr,
            Pointer user_data);
}
