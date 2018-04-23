package org.jna.jvmtiutils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.collections.MapUtils;

import com.sun.nik.FunctionExt;
import com.sun.nik.Native;
import com.sun.nik.Pointer;

import junit.framework.Assert;

public class JvmTiFunction extends FunctionExt {

	private static final Logger log = Logger.getLogger(JvmTiFunction.class.getName());

	public static Properties functionMap = new Properties();

	public static Map<String, String> errCodes;

	public static Properties errCodesRevert = new Properties();

	static {
		try {
			{
				FileInputStream fis = new FileInputStream(JnaParams.jnaParams.jvmtiProp);
				functionMap.load(fis);
				fis.close();
			}
			{
				FileInputStream fis = new FileInputStream(JnaParams.jnaParams.jvmtiErrorCodes);
				errCodesRevert.load(fis);
				fis.close();
				errCodes = MapUtils.invertMap(errCodesRevert);
			}
			// log.info(errCodes);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	public int resultCode = -1;

	private Pointer jvmTiPointerPointer;

	private static Pointer jvmTiPointerPointerStatic;

	// private static int jvmTiPointer = -1;
	public final int functionNumer;

	private static Object lock = new Object();

	public static Pointer getDefaultJvmtiPointer() {
		if (jvmTiPointerPointerStatic == null) {
			synchronized (lock) {
				if (jvmTiPointerPointerStatic == null) {
					jvmTiPointerPointerStatic = JnaNativeUtils.INSTANCE.getJvmTiAddress();
					if (jvmTiPointerPointerStatic == null) {
						int inn = JnaNativeUtils.INSTANCE.wasAgentOnLoadInvoked2();
						if(inn!=2){
							throw new RuntimeException("agent was not set on startup : "+inn);
						}
						int isNullP =JnaNativeUtils.INSTANCE.isJvmTiNull();
						if(isNullP!=3){
							throw new RuntimeException("jvmti is null : "+isNullP);
						}
						throw new RuntimeException("jvmTiPointerPointerStatic is null");
					}
				}
			}
		}
		if (jvmTiPointerPointerStatic == null) {
			throw new RuntimeException("jvmTiPointerPointerStatic is null");
		}
		Assert.assertNotNull(jvmTiPointerPointerStatic);
		return jvmTiPointerPointerStatic;

	}

	private static Pointer getFunctionPointer(Pointer jvmTiPointer, int functionNumber) {
		Assert.assertNotNull(jvmTiPointer);
		long initFP = JnaNativeMethods.getPointerAddress(jvmTiPointer);
		// initFP = jvmTiPointer.getInt(0);
		if (JnaParams.jnaParams.isArch64) {
			initFP = jvmTiPointer.getNativeLong(0).longValue();
		} else {
			initFP = jvmTiPointer.getInt(0);
		}
		long address = initFP + functionNumber * Native.POINTER_SIZE;
		Assert.assertNotSame(address, 0);
		Pointer pointer5 = new Pointer(address);
		long adress3;
		if (JnaParams.jnaParams.isArch64) {
			adress3 = pointer5.getNativeLong(0).longValue();
		} else {
			adress3 = pointer5.getInt(0);
		}
		Assert.assertNotSame(adress3, 0);
		return new Pointer(adress3);

	}

	public JvmTiFunction(int functionNumber, Pointer jvmTiPointer) {
		super(getFunctionPointer(jvmTiPointer, functionNumber - 1), 0, null);
		jvmTiPointerPointer = jvmTiPointer;
		Assert.assertNotNull(jvmTiPointer);
		functionNumer = functionNumber;
		org.jna.jvmtiutils.JnaParams.checkEnabled();
	}

	public JvmTiFunction(String functionName, Pointer jvmTiPointer) {
		super(getFunctionPointer(jvmTiPointer, Integer.parseInt(functionMap.getProperty(functionName)) - 1), 0, null);
		jvmTiPointerPointer = jvmTiPointer;
		Assert.assertNotNull(jvmTiPointer);
		functionNumer = Integer.parseInt(functionMap.getProperty(functionName));
	}

	public JvmTiFunction(String functionName) {
		this(Integer.parseInt(functionMap.getProperty(functionName)));
	}

	public JvmTiFunction(int functionNumber) {
		this(functionNumber, getDefaultJvmtiPointer());
	}

	public final void invokeJvmTiFunction(Object[] params) throws JnaException {
		Object[] params2 = new Object[params.length + 1];
		System.arraycopy(params, 0, params2, 1, params.length);
		params2[0] = jvmTiPointerPointer;
		invokeInt(params2);
		if (resultCode != 0) {
			throw new JnaException(resultCode + " " + errCodes.get(resultCode + ""));
		}
	}

	@Override
	public void processParams(int flags, Object[] params, long resultPointer, long jniEnvPointer) {
		super.processParams(flags, params, resultPointer, jniEnvPointer);
	}

	@Override
	@Deprecated
	public void processResult(int flags, Object[] params, long resultPointer) {
		resultCode = new Pointer(resultPointer).getInt(0);
		if (resultCode != 0) {
			log.info("!!! fault = " + resultCode + " " + errCodes.get(resultCode + ""));
		} else {
			processResultBeforeExcJvmTi(flags, params, resultPointer);
		}

	}

	// best method for process result
	public void processResultBeforeExcJvmTi(int flags, Object[] params, long resultPointer) {

	}

}
