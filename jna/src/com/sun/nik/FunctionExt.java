package com.sun.nik;

import java.util.HashMap;
import java.util.Map;

public class FunctionExt extends Function {

	public static Map<String, Object> myOpts = new HashMap();

	static {
		myOpts.put(Library.OPTION_ALLOW_OBJECTS, Boolean.TRUE);
	}

	
    public FunctionExt(NativeLibrary library, String functionName, int callFlags, String encoding) {
    	super(library, functionName, callFlags, encoding);
    }
    
	public FunctionExt(Pointer functionAddress, int callFlags, String encoding) {
		super(functionAddress, callFlags, encoding);
	}

	public static Pointer getFunctionRef(Callback callback) {
		return CallbackReference.getFunctionPointer(callback);
	}

	



//	// user version with long parameters
//	@Deprecated
//	public void processResult(int flags, Object[] params,
//			int resultPointer) {
//		processResultBeforeExc2(flags, params, (long) resultPointer);
//	}
//
//	public void processResultBeforeExc2(int flags, Object[] params,
//			long resultPointer) {
//		log.info(Arrays.asList(params));
//	}

	// public void processResultBeforeAfterExc(int resultPointer) {
	// log.info(Arrays.asList(params));
	// }
	
	// user version with long parameters


	@Override
	public Object invoke(Class returnType, Object[] inArgs) {
		// log.info( this.options);

		return invoke(returnType, inArgs, myOpts);
	}
}
