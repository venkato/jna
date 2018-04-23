package org.jna.jvmtiutils2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jna.jvmtiutils.JnaNativeMethods;



import com.sun.nik.Pointer;

public class JnaNativeBean {

	private static final Log log = LogFactory.getLog(JnaNativeBean.class);

	// public static void

	public long convertObjectToPointer(final Object object)
			throws IllegalArgumentException, IllegalAccessException {
		return JnaNativeMethods.convertObjectToPointer(object);
	}

	public Object convertPointerToObject(final long i) {
		return JnaNativeMethods.convertPointerToObject(new Pointer(i));
	}

	// public Object convertPointerToObjectMakeGlobalRef(final int i) {
	// return JnaNativeMethods.convertPointerToObjectMakeGlobalRef(i);
	// }

	public long getPointerAddress(final Pointer pointer)
			throws IllegalArgumentException, IllegalAccessException {
		return JnaNativeMethods.convertObjectToPointer(pointer);
	}
}
