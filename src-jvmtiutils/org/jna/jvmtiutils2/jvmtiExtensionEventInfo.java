package org.jna.jvmtiutils2;

import java.util.List;

import com.sun.nik.Pointer;
import com.sun.nik.Structure;

public class jvmtiExtensionEventInfo extends Structure {

    public jvmtiExtensionEventInfo(final Pointer pointer) {
        super(pointer);
    }

    public int extension_event_index;

    public String id;

    public String short_description;

    public int param_count;

    public Pointer params;

	@Override
	protected List getFieldOrder() {
		// TODO Auto-generated method stub
		return null;
	}

}
