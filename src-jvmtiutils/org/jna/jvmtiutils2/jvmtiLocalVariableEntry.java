package org.jna.jvmtiutils2;

import java.util.Arrays;
import java.util.List;

import org.jna.jvmtiutils.JniFunction;

import com.sun.nik.Pointer;
import com.sun.nik.Structure;

public class jvmtiLocalVariableEntry extends Structure {

    // public static class ByValue extends jvmtiLocalVariableEntry implements
    // Structure.ByValue { }
    // public static class ByReference extends jvmtiLocalVariableEntry
    // implements Structure.ByReference { }

    // private static final Log log = LogFactory.getLog(Reflection
    // .getCallerClass(1));

    public jvmtiLocalVariableEntry(final Pointer pointer) {
        super(pointer);
    }

    public long start_location;

    public int length;

    public String name;

    public String signature;

    public String generic_signature;

    public int slot;

	@Override
	protected List getFieldOrder() {
		return JniFunction.getFields(getClass());
	}

}
