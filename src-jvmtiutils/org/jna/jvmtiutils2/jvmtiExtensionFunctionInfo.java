package org.jna.jvmtiutils2;

import java.util.List;

import com.sun.nik.Pointer;
import com.sun.nik.Structure;

public class jvmtiExtensionFunctionInfo extends Structure {

    public jvmtiExtensionFunctionInfo(final Pointer pointer) {
        super(pointer);
    }

    public Pointer func;

    public String id;

    public String short_description;

    public int param_count;

    public Pointer params;

    public int error_count;

    public Pointer errors;

	@Override
	protected List getFieldOrder() {
		// TODO Auto-generated method stub
		return null;
	}

}
