package org.jna.jvmtiutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import junit.framework.Assert;

import net.sf.jremoterun.utilities.JrrClassUtils;




import com.sun.nik.FunctionExt;
import com.sun.nik.Native;
import com.sun.nik.Pointer;

public class JniFunction extends FunctionExt {
	private static final Logger log = Logger.getLogger(JniFunction.class.getName());
	public static Properties functionMap = new Properties();

	static {
		try {
			{
				FileInputStream fis = new FileInputStream(JnaParams.jnaParams.jniMethodProp);
				functionMap.load(fis);
				fis.close();
			}
			// log.info(errCodes);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	public int resultCode = -1;

	// private static int jvmTiPointer = -1;
	int functionNumer;

	public static Pointer getJniFunctionPointer(String functionName) {
		int id = Integer.parseInt(functionMap.getProperty(functionName));
		log.info(id+"");
		Assert.assertTrue(id > 0);
		return getJniFunctionPointer(id);
	}

	// may not work
	public static Pointer getJniFunctionPointer(int functionNumber) {
		// if(false) {
		// log.info(functionNumber);
		//
		// Pointer
		// pointer=JnaNativeUtils.INSTANCE.getJniFunctionRef_32(functionNumber*4);
		// log.info(pointer);
		// log.info( JnaNativeMethods.getPointerAddress( pointer));
		// // return pointer;
		// }
		Pointer pointer = JnaNativeUtils.INSTANCE.getFirstJniFunction();
		long p = JnaNativeMethods.getPointerAddress(pointer);
		log.info(p+"");
		long p2 = p + (functionNumber) * Native.POINTER_SIZE;
		log.info(p2+"");
		Pointer pointer2 = new Pointer(p2).getPointer(0);
		log.info("pointer address " + JnaNativeMethods.getPointerAddress(pointer2));
		return pointer2;
		// return new Pointer(address);

	}

	public JniFunction(String functionName) {
		this(Integer.parseInt(functionMap.getProperty(functionName)));
		org.jna.jvmtiutils.JnaParams.checkEnabled();
	}

	public JniFunction(int functionNumber) {
		super(getJniFunctionPointer(functionNumber), 0, null);
		// jvmTiPointerPointer=jvmTiPointer;
		functionNumer = functionNumber;
	}

	// public void invokeJvmTiFunction(Object[] params) throws JnaException {
	// if (params == null) {
	// params = new Object[0];
	// }
	// Object[] params2 = new Object[params.length + 1];
	// System.arraycopy(params, 0, params2, 1, params.length);
	// params2[0] = JniEnvIdentifier.jniEnvIdentifier;
	// return invokeInt(params2);
	// // if (resultCode != 0) {
	// // throw new JnaException(resultCode);
	// // }
	// }

	@Override
	public void processResult(int flags, Object[] params, long resultPointer) {
		resultCode = new Pointer(resultPointer).getInt(0);
		if (resultCode == 0) {
			log.info("!!! fault  ");
		} else {
			processResultBeforeExcJvmTi(flags, params, resultPointer);
		}

	}

	// best method for process result
	public void processResultBeforeExcJvmTi(int flags, Object[] params, long resultPointer) {

	}

	public static List<String> getFields(Class class1) {
		List<String> al = new ArrayList<String>();
		Field[] fields = class1.getFields();
		for (Field field : fields) {
			if (field.getDeclaringClass() == class1) {
				al.add(field.getName());
			}
		}
		return al;
	}

}
