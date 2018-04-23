package org.jna.jvmtiutils2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jna.jvmtiutils.JnaNativeMethods;


import com.sun.nik.Pointer;

public class JnaUtils2 {

    private static final Log log = LogFactory.getLog(JnaUtils2.class);

    public static long getPointerAddress(final Pointer pointer)
            throws Exception {
        return JnaNativeMethods.getPointerAddress(pointer);
    }

}
