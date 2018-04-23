package org.jna.jvmtiutils;

import java.util.List;

import com.sun.nik.Pointer;
import com.sun.nik.Structure;

public class jvmtiClassDefinition extends Structure {

	public static class ByValue extends jvmtiClassDefinition implements
			Structure.ByValue {
	}

	public static class ByReference extends jvmtiClassDefinition implements
			Structure.ByReference {
	}

	public jvmtiClassDefinition() {
		// TODO Auto-generated constructor stub
	}

	public jvmtiClassDefinition(Pointer pointer) {
		super(pointer);
	}

	public Pointer klass;
	public int class_byte_count;
	public Pointer class_bytes;
	@Override
	protected List getFieldOrder() {
		return JniFunction.getFields(getClass());
	}

}
