package org.jna.jvmtiutils2;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jna.jvmtiutils.JniFunction;



import com.sun.nik.Pointer;
import com.sun.nik.Structure;

public class jvmtiMonitorUsage extends Structure {

	private static final Log log = LogFactory.getLog(jvmtiMonitorUsage.class);

	public Pointer owner;

	public int entry_count;

	public int waiter_count;

	public Pointer waiters;

	public int notify_waiter_count;

	public Pointer notify_waiters;

	public jvmtiMonitorUsage(final Pointer pointer) {
		super(pointer);
		setAlignType(Structure.ALIGN_NONE);
	}

	@Override
	protected List<String> getFieldOrder() {
		List<String> fields= JniFunction.getFields(getClass());
		log.info(fields);
		return fields;
	}
}
