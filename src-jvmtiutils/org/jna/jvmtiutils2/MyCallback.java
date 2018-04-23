package org.jna.jvmtiutils2;

import com.sun.nik.Callback;
import com.sun.nik.Pointer;

public interface MyCallback extends Callback {

    int jvmtiHeapReferenceCallback(int reference_kind, Pointer reference_info,
            long class_tag, long referrer_class_tag, long size,
            Pointer tag_ptr, Pointer referrer_tag_ptr, int length,
            Pointer user_data);

}
