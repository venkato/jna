package org.jna.jvmtiutils2;


import com.sun.nik.JniEnvIdentifier;
import com.sun.nik.Library;
import com.sun.nik.Native;

public interface JnaJvmMethods extends Library {

	// private JnaNativeMethods jnaNativeMethods=new JnaNativeMethods();
	JnaJvmMethods INSTANCE = (JnaJvmMethods) Native.loadLibrary(
			"c:\\progi\\java\\cjdk\\jre\\bin\\client\\jvm.dll",
			JnaJvmMethods.class);

	Class _JVM_GetCallerClass(JniEnvIdentifier jniEnvIdentifier, int depth);

}
