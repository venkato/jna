package org.jna.jvmtiutils;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.logging.Logger;

import com.sun.nik.Native;
import com.sun.nik.NativeLibPath;
import com.sun.nik.Pointer;

import junit.framework.Assert;
import net.sf.jremoterun.utilities.JrrClassUtils;

public class JnaNativeMethods {

	private static final Logger log = Logger.getLogger(JnaNativeMethods.class.getName());

	public static void init()
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, FileNotFoundException {
		org.jna.jvmtiutils.JnaParams.checkEnabled();
		if (JnaParams.inited) {
			log.info("already inited 2");
			return;
		}
		JnaParams.getJnaParamsRef();

//		System.setProperty("nik.debug_load", "true");
//		System.setProperty("nik.debug_load.nik", "true");
//		System.setProperty("nik.library.path", JnaParams.jnaParams.libDir.getAbsolutePath());
//		System.setProperty("nik.noclasspath", "true");
//		System.setProperty("nik.boot.library.name", JnaParams.jnaParams.jnaDispatchLibName);
//		System.setProperty("nik.boot.library.path", JnaParams.jnaParams.libDir.getAbsolutePath());
//		Assert.assertTrue(JnaParams.jnaParams.libDir.getAbsolutePath(), JnaParams.jnaParams.libDir.exists());
		Assert.assertTrue(JnaParams.jnaParams.jnaNativeUtilsFullPath.getAbsolutePath(),
				JnaParams.jnaParams.jnaNativeUtilsFullPath.exists());
		Assert.assertTrue(JnaParams.jnaParams.jnaDispatchLibPathFull.getAbsolutePath(),
				JnaParams.jnaParams.jnaNativeUtilsFullPath.exists());
//		log.info("cp2");
//		log.info(null);
//		System.load(JnaParams.jnaParams.jnaNativeUtilsFullPath.getAbsolutePath());

		System.load(JnaParams.jnaParams.jnaDispatchLibPathFull.getAbsolutePath());
		log.info("jna lib loaded : "+JnaParams.jnaParams.jnaDispatchLibPathFull);
		System.out.println("jna lib loaded : "+JnaParams.jnaParams.jnaDispatchLibPathFull);
		 NativeLibPath.nativeLibPath =
		 JnaParams.jnaParams.jnaDispatchLibPathFull.getAbsolutePath();
//		log.info("cp1");
		initVars();
		log.info("initializing Native");
		Native.getAPIChecksum();
//		JrrClassUtils.setFieldValue(Native.class, "DEBUG_JNA_LOAD", true);
//		JrrClassUtils.setFieldValue(Native.class, "DEBUG_LOAD", true);

//		log.info("Native.POINTER_SIZE : "+ Native.POINTER_SIZE);
		JnaParams.inited = true;
		log.info("init fisnish");
	}

	public static Class initCLASS = JnaNativeUtils.class;

	public static long convertObjectToPointer(Object address) throws IllegalArgumentException, IllegalAccessException {
		Pointer pointer = JnaNativeUtils.INSTANCE.convertObjectToPointer(address);
		return getPointerAddress(pointer);
	}

	public static Object convertPointerToObject(Pointer object) {
		return JnaNativeUtils.INSTANCE.convertPointerToObject(object);
	}

	public native static int convertObjectToPointer2(Object address)
			throws IllegalArgumentException, IllegalAccessException;

	public native static Object convertPointerToObject2(int ref);

	// public static native Object convertPointerToObject2(Pointer object);

	// public static native Object convertPointerToObjectMakeGlobalRef(int
	// object);

	public static native void initVars();

	private static Field peerField;

	static {
		try {
			peerField = JrrClassUtils.findField(Pointer.class, "peer");
		} catch (NoSuchFieldException e) {
			throw new Error(e);
		}
	}

	public static long getPointerAddress(final Pointer pointer) {
		if (pointer == null) {
			log.severe("pointer is null");
			return -1;
		}
		try {
			return (Long) peerField.get(pointer);
		} catch (IllegalAccessException e) {
			throw new Error(e);
		}
	}
}
