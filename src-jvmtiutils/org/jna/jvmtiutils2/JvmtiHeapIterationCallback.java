package org.jna.jvmtiutils2;

import com.sun.nik.Callback;
import com.sun.nik.Pointer;

public interface JvmtiHeapIterationCallback extends Callback {

    int jvmtiHeapIterationCallback(long class_tag, long size, Pointer tag_ptr,
            int length, Pointer data);

    // typedef jint (JNICALL *jvmtiHeapIterationCallback)
    // (jlong class_tag,
    // jlong size,
    // jlong* tag_ptr,
    // jint length,
    // void* user_data);
}
