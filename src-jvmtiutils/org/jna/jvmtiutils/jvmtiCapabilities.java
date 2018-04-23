package org.jna.jvmtiutils;

import java.util.List;

import com.sun.nik.Pointer;
import com.sun.nik.Structure;

public class jvmtiCapabilities extends Structure {

    public static class ByValue extends jvmtiCapabilities implements Structure.ByValue { }
    public static class ByReference extends jvmtiCapabilities implements Structure.ByReference { }

    
    public jvmtiCapabilities() {
        // TODO Auto-generated constructor stub
    }
    
    public jvmtiCapabilities(Pointer pointer) {
        super(pointer);
    }
    public short can_tag_objects;

    public short can_generate_field_modification_events;

    public short can_generate_field_access_events;

    public short can_get_bytecodes;

    public short can_get_synthetic_attribute;

    public short can_get_owned_monitor_info;

    public short can_get_current_contended_monitor;

    public short can_get_monitor_info;

    public short can_pop_frame;

    public short can_redefine_classes;

    public short can_signal_thread;

    public short can_get_source_file_name;

    public short can_get_line_numbers;

    public short can_get_source_debug_extension;

    public short can_access_local_variables;

    public short can_maintain_original_method_order;

    public short can_generate_single_step_events;

    public short can_generate_exception_events;

    public short can_generate_frame_pop_events;

    public short can_generate_breakpoint_events;

    public short can_suspend;

    public short can_redefine_any_class;

    public short can_get_current_thread_cpu_time;

    public short can_get_thread_cpu_time;

    public short can_generate_method_entry_events;

    public short can_generate_method_exit_events;

    public short can_generate_all_class_hook_events;

    public short can_generate_compiled_method_load_events;

    public short can_generate_monitor_events;

    public short can_generate_vm_object_alloc_events;

    public short can_generate_native_method_bind_events;

    public short can_generate_garbage_collection_events;

    public short can_generate_object_free_events;

    public short can_force_early_return;

    public short can_get_owned_monitor_stack_depth_info;

    public short can_get_constant_pool;

    public short can_set_native_method_prefix;

    public short can_retransform_classes;

    public short can_retransform_any_class;

    public short can_generate_resource_exhaustion_heap_events;

    public short can_generate_resource_exhaustion_threads_events;

	@Override
	protected List getFieldOrder() {
		return JniFunction.getFields(getClass());
	}

}
