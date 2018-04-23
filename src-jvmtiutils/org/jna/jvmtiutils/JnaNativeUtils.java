package org.jna.jvmtiutils;

import com.sun.nik.Library;
import com.sun.nik.Native;
import com.sun.nik.Pointer;

public interface JnaNativeUtils extends Library {

    // private JnaNativeMethods jnaNativeMethods=new JnaNativeMethods();
    JnaNativeUtils INSTANCE = (JnaNativeUtils) Native.loadLibrary(
            JnaParams.jnaParams.jnaNativeUtilsFullPath.getAbsolutePath(),  JnaNativeMethods.initCLASS);

    Pointer getJvmTiAddress();

    Pointer getJvmTiAddress2();
    
    Pointer getJvmEnvAddress();
    
    int isJvmTiNull();
    int wasAgentOnLoadInvoked2();

//    void invokeCallback();
//    void invokeCallback4();
//    Pointer getJvmtiHeapCallbacks889();
//    Pointer getJniFunctionRef_32(int numberfunc);
//    Pointer getJniFunctionRef_64(int numberfunc);
    Pointer getAllRefFuncRef(int funcId);

    Pointer getFirstJniFunction();
    Pointer getJvmtiHeapCallbacks9();
    Pointer getSpecialJvmti();

    Pointer convertObjectToPointer(Object address);
    Object convertPointerToObject(Pointer object);

//    void invokeCallback6();
//    void invokeCallback7(int class1,Pointer pointer,Pointer pointer2);
//    void invokeCallback8(int class1,Pointer pointer2,Pointer userData);
//    void invokeCallback5(Pointer pointer);
//    void invokeCallback2(Pointer fnPointer);
    Pointer getJavaVmAddress();
    
    int GetLocalVariableTable(
    		Pointer method,
    	    Pointer entry_count_ptr,
    	    Pointer table_ptr);
    
    
    int GetLocalVariableTable3(
    		Thread thread,int depth,
    	    Pointer entry_count_ptr,
    	    Pointer table_ptr);

    int isDebugThisLib();

    void setDebugThisLibMode(int value);
    
    int suspendThread(Thread thread);

    Pointer getSetjvmtiCapabilities();
    void testF();
}
