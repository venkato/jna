//#include "nativetest.h"	/*double quotes tells it to search current directory*/
#include <jni.h>
#include <jvmti.h>

#ifndef JNIEXPORT
#define JNIEXPORT
#define JNICALL
#endif

#define LOAD_WEAKREF(ENV,VAR) \
  ((VAR == 0) \
   ? 0 : ((VAR = (*ENV)->NewWeakGlobalRef(ENV, VAR)) == 0 ? 0 : VAR))
#define FIND_CLASS(ENV,SIMPLE,NAME) \
  (class ## SIMPLE = (*ENV)->FindClass(ENV, NAME))
#define FIND_PRIMITIVE_CLASS(ENV,SIMPLE) \
  (classPrimitive ## SIMPLE = (*ENV)->GetStaticObjectField(ENV,class ## SIMPLE,(*ENV)->GetStaticFieldID(ENV,class ## SIMPLE,"TYPE","Ljava/lang/Class;")))
#define LOAD_CREF(ENV,SIMPLE,NAME) \
  (FIND_CLASS(ENV,SIMPLE,NAME) && LOAD_WEAKREF(ENV,class ## SIMPLE))
#define LOAD_PCREF(ENV,SIMPLE,NAME) \
  (LOAD_CREF(ENV,SIMPLE,NAME) \
   && FIND_PRIMITIVE_CLASS(ENV,SIMPLE) \
   && LOAD_WEAKREF(ENV,classPrimitive ## SIMPLE))

#define LOAD_MID(ENV,VAR,CLASS,NAME,SIG) \
   ((VAR = (*ENV)->GetMethodID(ENV, CLASS, NAME, SIG)) ? VAR : 0)
#define LOAD_FID(ENV,VAR,CLASS,NAME,SIG) \
   ((VAR = (*ENV)->GetFieldID(ENV, CLASS, NAME, SIG)) ? VAR : 0)

static jvmtiHeapReferenceCallback jvmtiHeapReferenceCallbackTarger;
static void (*pt2FunctionSimpleStatic)();
static int debugThisLib = 0;
static JavaVM *vm = NULL;
static jvmtiEnv *jvmti = NULL;
static jclass klassFollowRef;
static int wasAgentOnLoadInvoked = 0;

static JNIEnv* getJniEnvNick7() {
	JNIEnv *env99;
	//(void)memset(env99,0,sizeof(env99));
	//*env99=NULL;
	if (debugThisLib == 1) {
		printf("vm %d\n", vm);
	}
	if ((*vm)->GetEnv(*vm, (void**) &env99, JNI_VERSION_1_4) != JNI_OK) {
		printf("getJniEnvNick7 error\n");
		if (debugThisLib == 1) {
			printf("Failed to get the environment using GetEnv()\n");
		}
		return NULL;
	}
	if (debugThisLib == 1) {
		printf("getJniEnvNick7 11\n");
	}
	return env99;
}

typedef struct ObjectBatch {
	jobject *objects;
	jint count;
} ObjectBatch;

typedef struct ClassInstancesData {
	jint instCount;
	jint maxInstances;
	jlong objTag;
	jvmtiError error;
} ClassInstancesData;

static jint JNICALL cbObjectTagInstance2(jvmtiHeapReferenceKind reference_kind,
		const jvmtiHeapReferenceInfo* reference_info, jlong class_tag,
		jlong referrer_class_tag, jlong size, jlong* tag_ptr,
		jlong* referrer_tag_ptr, jint length, void* user_data) {
	if (debugThisLib == 1) {
		printf("insnace\n");
	}
	ClassInstancesData *data;

	/* Check data structure */
	data = (ClassInstancesData*) user_data;
	if (data == NULL) {
		data->error = 88;
		printf("err 1\n");
		return JVMTI_VISIT_ABORT;
	}

	/* If we have tagged enough objects, just abort */
	if (data->maxInstances != 0 && data->instCount >= data->maxInstances) {
		if (debugThisLib == 1) {
			printf("max instance reach\n");
		}
		return JVMTI_VISIT_ABORT;
	}

	/* If tagged already, just continue */
	if ((*tag_ptr) != (jlong) 0) {
		if (debugThisLib == 1) {
			printf("already tagged\n");
		}
		return JVMTI_VISIT_OBJECTS;
	}

	/* Tag the object so we don't count it again, and so we can retrieve it */
	(*tag_ptr) = data->objTag;
	data->instCount++;
	return JVMTI_VISIT_OBJECTS;
}

static jvmtiIterationControl JNICALL allRefjvmtiHeapObjectCallback2(
		jlong class_tag, jlong size, jlong* tag_ptr, void* user_data) {
	if (debugThisLib == 1) {
		//printf("allRefObjectTagInstance2\n");
		printf("insnace\n");
	}
	ClassInstancesData *data;

	/* Check data structure */
	data = (ClassInstancesData*) user_data;
	if (data == NULL) {
		data->error = 88;
		printf("err 1\n");
		return JVMTI_ITERATION_ABORT;
	}

	/* If tagged already, just continue
	 * */
	if ((*tag_ptr) != (jlong) 0) {
		if (debugThisLib == 1) {
			printf("already tagged\n");
		}
		return JVMTI_ITERATION_CONTINUE;
	}

	/* Tag the object so we don't count it again, and so we can retrieve it */
	(*tag_ptr) = data->objTag;
	data->instCount++;
	return JVMTI_ITERATION_CONTINUE;
}

static jint JNICALL allRefObjectTagInstance2(jlong class_tag, jlong size,
		jlong* tag_ptr, jint length, void* user_data) {
	if (debugThisLib == 1) {
		//printf("allRefObjectTagInstance2\n");
		printf("insnace\n");
	}
	ClassInstancesData *data;

	/* Check data structure */
	data = (ClassInstancesData*) user_data;
	if (data == NULL) {
		data->error = 88;
		printf("err 1\n");
		return JVMTI_VISIT_ABORT;
	}

	/* If tagged already, just continue */
	if ((*tag_ptr) != (jlong) 0) {
		if (debugThisLib == 1) {
			printf("already tagged\n");
		}
		return JVMTI_VISIT_OBJECTS;
	}

	/* Tag the object so we don't count it again, and so we can retrieve it */
	(*tag_ptr) = data->objTag;
	data->instCount++;
	return JVMTI_VISIT_OBJECTS;
}

static jint JNICALL allRefJvmtiPrimitiveFieldCallback(
		jvmtiHeapReferenceKind kind, const jvmtiHeapReferenceInfo* info,
		jlong object_class_tag, jlong* object_tag_ptr, jvalue value,
		jvmtiPrimitiveType value_type, void* user_data) {
	if (debugThisLib == 1) {
		printf("allRefJvmtiPrimitiveFieldCallback\n");
	}
	return JVMTI_VISIT_OBJECTS;
}

static jint JNICALL allRefjvmtiArrayPrimitiveValueCallback(jlong class_tag,
		jlong size, jlong* tag_ptr, jint element_count,
		jvmtiPrimitiveType element_type, const void* elements, void* user_data) {

	if (debugThisLib == 1) {
		printf("allRefjvmtiArrayPrimitiveValueCallback\n");
	}
	return JVMTI_VISIT_OBJECTS;
}

static jint JNICALL allRefjvmtiStringPrimitiveValueCallback(jlong class_tag,
		jlong size, jlong* tag_ptr, const jchar* value, jint value_length,
		void* user_data) {
	if (debugThisLib == 1) {
		printf("allRefjvmtiStringPrimitiveValueCallback\n");
	}
	return JVMTI_VISIT_OBJECTS;
}

JNIEXPORT void* JNICALL getAllRefFuncRef(int funcId) {
	//printf("getAllRefFuncRef ENTER\n");
	switch (funcId) {
	case 1:
		//printf("getAllRefFuncRef 1\n");
		return &allRefObjectTagInstance2;
	case 2:
		//printf("getAllRefFuncRef 2\n");
		return &allRefJvmtiPrimitiveFieldCallback;
	case 3:
		//printf("getAllRefFuncRef 3\n");
		return &allRefjvmtiArrayPrimitiveValueCallback;
	case 4:
		//printf("getAllRefFuncRef 4\n");
		return &allRefjvmtiStringPrimitiveValueCallback;
	case 5:
		//printf("getAllRefFuncRef 4\n");
		return &allRefjvmtiHeapObjectCallback2;
	}
	printf("getAllRefFuncRef err\n");
	return NULL;
}

static jobject makeGlobalJniEnvNick7(jobject a) {
	JNIEnv *env99 = getJniEnvNick7();
	jobject b = (*env99)->NewWeakGlobalRef(env99, a);
	return b;
}

JNIEXPORT void JNICALL invokeGetOwnedMonitorInfoDebug3(int doCall) {
	printf("invokeGetOwnedMonitorInfoDebug3 invoked \n");
	int longzie = sizeof(long);
	printf("longzie = %d \n", longzie);
}

JNIEXPORT void JNICALL invokeGetOwnedMonitorInfoDebug2(int doCall) {
	 invokeGetOwnedMonitorInfoDebug(NULL,doCall);
}

JNIEXPORT void JNICALL invokeGetOwnedMonitorInfoDebug(jthread thread,int doCall) {
     if(jvmti == NULL ){
            printf("jvmti is null \n");
     }
	 printf("jvmti value %d (1547186432) \n",*jvmti);
	 jint* owned_monitor_count_ptr;
	 int owned_monitor_count_ptr_size = sizeof(owned_monitor_count_ptr);
	 printf("owned_monitor_count_ptr_size %d \n",owned_monitor_count_ptr_size);
	jobject** owned_monitors_ptr;
	 int*  functPntr =  &((*jvmti)->GetOwnedMonitorInfo);
	 int functPntr_size = sizeof(functPntr);
	 printf("functPntr_size %d \n",functPntr_size);
	 if(functPntr_size == 8){	
		printf("functPntr 1 %d (1542206592) \n", *functPntr);
		printf("functPntr 3 %d (1547186504)\n", functPntr);
	 }
	 printf(" doCall flag = %d \n",doCall);	 
	 jvmtiError errCode= JVMTI_ERROR_NONE;
	 if(doCall >0 ){	 
	    printf(" calling \n");	 
		errCode= (*jvmti)->GetOwnedMonitorInfo(jvmti,thread,owned_monitor_count_ptr,owned_monitors_ptr);
	 }
	 printf(" result code %d\n", errCode);
	 //jvmtiError errCode= (*jvmti)->GetOwnedMonitorInfo(thread,owned_monitor_count_ptr,owned_monitors_ptr);
	 if (errCode != JVMTI_ERROR_NONE) {
		printf(" err4 %d\n", errCode);
	}
	if(doCall >0 ){
	printf("owned_monitor_count_ptr = %d \n",*owned_monitor_count_ptr);
	}
}

JNIEXPORT jvmtiEnv* JNICALL getJvmTiAddress() {
    if(jvmti == NULL ){
       printf("jvmti is null\n");
    }
	return jvmti;
}

JNIEXPORT int JNICALL isJvmTiNull() {
    if(jvmti == NULL){
       return 2;
    }
    return 3;
}

JNIEXPORT int JNICALL wasAgentOnLoadInvoked2() {
    return wasAgentOnLoadInvoked;
}


JNIEXPORT jvmtiEnv** JNICALL getJvmTiAddress2() {
	return &jvmti;
}

/*
 JNIEXPORT int JNICALL getJniFunctionRef_32(int offset) {
 printf("getJniFunctionRef enter\n");
 JNIEnv *env99 = getJniEnvNick7();
 int envvv = (int) (*env99);
 int envvv2 = envvv + offset;
 int* env7 = (void*) envvv2;
 printf("getJniFunctionRef cp3443\n");
 int* env8 = (void*) (*env7);
 printf("getJniFunctionRef exit\n");
 return env8;
 }

 JNIEXPORT long JNICALL getJniFunctionRef_64(int offset) {
 printf("getJniFunctionRef enter\n");
 JNIEnv *env99 = getJniEnvNick7();
 long envvv = (long) (*env99);
 long envvv2 = envvv + offset;
 long* env7 = (void*) envvv2;
 printf("getJniFunctionRef cp3443\n");
 long* env8 = (void*) (*env7);
 printf("getJniFunctionRef exit\n");
 return env8;
 }
 */
JNIEXPORT int JNICALL isDebugThisLib() {
	return debugThisLib;
}

JNIEXPORT void JNICALL setDebugThisLibMode(int value) {
	debugThisLib = value;
	if (value) {
		printf("setting debug mode 4\n");
	}
}

JNIEXPORT JNIEnv* JNICALL getJvmEnvAddress() {
	return getJniEnvNick7();
}
JNIEXPORT void* JNICALL getFirstJniFunction() {
	JNIEnv *env99 = getJniEnvNick7();
	void* vv=((*env99));
	return vv;
}

JNIEXPORT JavaVM* JNICALL getJavaVmAddress() {
	return vm;
}

//typedef jvmtiIterationControl (JNICALL *jvmtiHeapObjectCallback)
//  (jlong class_tag, jlong size, jlong* tag_ptr, void* user_data);

typedef void (JNICALL *jvmtiHeapObjectCallback22)();

JNIEXPORT jvmtiCapabilities* JNICALL getSetjvmtiCapabilities() {
	jvmtiCapabilities capabilities;
	(void) memset(&capabilities, 0, sizeof(capabilities));
	//capabilities.can_generate_field_modification_events = 1;
	int error = (*jvmti)->GetPotentialCapabilities(jvmti, &capabilities);
	if (error != JVMTI_ERROR_NONE) {
		printf("error GetPotentialCapabilities %d\n", error);
	} else {
		if (debugThisLib == 1) {
			printf("GetPotentialCapabilities cp3 ok\n");
		}
	}
	printf("potencialCap = %d\n", capabilities);
	return &capabilities;
}

JNIEXPORT jvmtiCapabilities* JNICALL getSetjvmtiCapabilities1() {
	/*
	 printf("getSetjvmtiCapabilities enter\n");
	 printf("jvmtiHeapReferenceKind size %d\n",sizeof(jvmtiHeapReferenceKind));
	 printf("jlong size %d\n",sizeof(jlong));
	 printf("long size %d\n",sizeof(long));
	 printf("size double %d\n",sizeof(double));
	 printf("size int %d\n",sizeof(int));
	 printf("size jvmtiHeapObjectFilter %d\n",sizeof(jvmtiHeapObjectFilter));
	 printf("size jvmtiIterationControl %d\n",sizeof(jvmtiIterationControl));
	 */
	//printf("null is %d\n ",NULL);
	jvmtiCapabilities capabilities;
	(void) memset(&capabilities, 0, sizeof(capabilities));
	//capabilities.can_generate_field_modification_events = 1;
	capabilities.can_get_current_contended_monitor = 1;
	capabilities.can_tag_objects = 1;
	capabilities.can_get_owned_monitor_info = 1;
	capabilities.can_generate_monitor_events = 1;
	capabilities.can_get_monitor_info = 1;
	capabilities.can_tag_objects = 1;
	capabilities.can_generate_object_free_events = 1;
	capabilities.can_generate_garbage_collection_events = 1;
	capabilities.can_redefine_classes = 1;
	capabilities.can_redefine_any_class = 1;
	capabilities.can_get_source_debug_extension = 1;
	capabilities.can_get_source_file_name = 1;
	capabilities.can_get_line_numbers = 1;
	capabilities.can_access_local_variables = 1;
	capabilities.can_generate_single_step_events = 1;
	capabilities.can_generate_exception_events = 1;
	capabilities.can_generate_frame_pop_events = 1;
	capabilities.can_generate_breakpoint_events = 1;
	capabilities.can_suspend = 1;
	capabilities.can_generate_method_entry_events = 1;
	capabilities.can_generate_method_exit_events = 1;
	capabilities.can_generate_garbage_collection_events = 1;
	capabilities.can_maintain_original_method_order = 1;
	capabilities.can_generate_monitor_events = 1;
	capabilities.can_tag_objects = 1;
	capabilities.can_pop_frame = 1; 
	capabilities.can_access_local_variables = 1;
	 

	// jvmtiCapabilities needed_capabilities = capabilities;
	int error = (*jvmti)->AddCapabilities(jvmti, &capabilities);
	//capabilities.can_tag_objects
	if (error != JVMTI_ERROR_NONE) {
		printf("error AddCapabilities %d\n", error);
	} else {
		if (debugThisLib == 1) {
		}
		printf("AddCapabilities cp2 ok\n");
	}
	if (debugThisLib == 1) {
		printf("sizeof capabilities %d\n", sizeof(capabilities));
		printf("getSetjvmtiCapabilities exit\n");
	}
	return &capabilities;
	/*
	 */
}



JNIEXPORT int JNICALL suspendThread(jthread thread12) {
	int error = (*jvmti)->SuspendThread(jvmti, thread12);
	if (error != JVMTI_ERROR_NONE) {
		printf("error suspendThread %d\n", error);
	} else {
		if (debugThisLib == 1) {
		}
		printf("GetPotentialCapabilities cp2 ok\n");
	}
	return error;
}

 JNIEXPORT int  JNICALL GetLocalVariableTable(
    jmethodID *method,
    jint* entry_count_ptr,
    jvmtiLocalVariableEntry** table_ptr){
    
	 	int error = (*jvmti)->GetLocalVariableTable(jvmti, *method,entry_count_ptr,table_ptr);
	 	return error;
 }
 
 
 
 JNIEXPORT int  JNICALL GetLocalVariableTable3(
    jthread thread12,
    jint depth,
    jint* entry_count_ptr,
    jvmtiLocalVariableEntry** table_ptr){
    	jmethodID *method;
    	jlocation *loc;
    	int error = (*jvmti)->GetFrameLocation(jvmti, thread12,depth, method,loc);
    	if(error!=0){
    		printf("GetFrameLocation error : %d\n",error);
    		return error;
    	}
    	printf("GetFrameLocation ok\n");
    	
	 	error = (*jvmti)->GetLocalVariableTable(jvmti, *method,entry_count_ptr,table_ptr);
	 	return error;
 }
 

JNIEXPORT jvmtiCapabilities* JNICALL getSetjvmtiCapabilities2() {
	jvmtiCapabilities capabilities;
	(void) memset(&capabilities, 0, sizeof(capabilities));
	//capabilities.can_generate_field_modification_events = 1;
	int error = (*jvmti)->GetPotentialCapabilities(jvmti, &capabilities);
	if (error != JVMTI_ERROR_NONE) {
		printf("error GetPotentialCapabilities %d\n", error);
	} else {
		if (debugThisLib == 1) {
		    printf("GetPotentialCapabilities cp4 ok\n");
		}

	}
	if (debugThisLib == 1) {
	// unsigned int potencialCap = *(&capabilities);
	// printf("potencialCap = %d\n", potencialCap);
	printf("potencialCap = %d\n", capabilities);
	printf("sizeof(byte) = %d\n",sizeof(char));
	printf("sizeof(capabilities) = %d\n",sizeof(capabilities));
 	
 	long cap2 = &capabilities;
 	//int cap3 = (int)capabilities;
 	printf("potencialCap = %d\n", cap2);
 	
 	printf("capabilities.can_suspend = %d\n", capabilities.can_suspend);
 	}

	// jvmtiCapabilities needed_capabilities = capabilities;
	error = (*jvmti)->AddCapabilities(jvmti, &capabilities);
	//capabilities.can_tag_objects
	if (error != JVMTI_ERROR_NONE) {
		printf("error AddCapabilities %d\n", error);
	} else {
		if (debugThisLib == 1) {
	    	printf("AddCapabilities cp5 ok\n");
		}
	}
	if (debugThisLib == 1) {
		printf("sizeof capabilities %d\n", sizeof(capabilities));
		printf("getSetjvmtiCapabilities exit\n");
	}
	return &capabilities;
	/*
	 */
}


void initJVMTI() {
	if (jvmti == NULL) {
		if (debugThisLib == 1) {
			printf("INTI JVMTI\n");
		}
		if (vm == NULL) {
			printf("VM IS NULL\n");
		}
		jint rc;
		rc = (*vm)->GetEnv(vm, &jvmti, JVMTI_VERSION_1_0);
		if (rc != JNI_OK) {
			printf("ERROR: Unable to create jvmtiEnv, rc=%d\n", rc);
			return -1;
		}
		getSetjvmtiCapabilities2();
	} else {
		if (debugThisLib == 1) {
			printf("skip INTI JVMTI\n");
		}
	}
}

JNIEXPORT jobject JNICALL convertObjectToPointer(jobject object) {
	object = makeGlobalJniEnvNick7(object);
	if (debugThisLib == 1) {
		printf("convertObjectToPointer enter=%d\n", object);
		printf("pointer=%d\n", &object);
		printf("value=%d\n", object);
		//printf("convertObjectToPointer exit\n");
	}
	return object;
}

JNIEXPORT jobject JNICALL convertPointerToObject(jobject object) {
	if (debugThisLib == 1) {
		printf("Java_nativetest_convertPointerToObject2 enter=%d\n", object);
	}
	return object;

}

JNIEXPORT jobject JNICALL Java_org_jna_jvmtiutils_JnaNativeMethods_convertObjectToPointer2(
		jobject object) {
	object = makeGlobalJniEnvNick7(object);
	if (debugThisLib == 1) {
		printf("convertObjectToPointer enter=%d\n", object);
		printf("pointer=%d\n", &object);
		printf("value=%d\n", object);
		//printf("convertObjectToPointer exit\n");
	}
	return object;
}

JNIEXPORT jobject JNICALL Java_org_jna_jvmtiutils_JnaNativeMethods_convertPointerToObject2(
		jobject object) {
	if (debugThisLib == 1) {
		printf("Java_nativetest_convertPointerToObject2 enter=%d\n", object);
	}
	return object;

}

JNIEXPORT void JNICALL Java_org_jna_jvmtiutils_JnaNativeMethods_initVars(
		JNIEnv *env, jclass clazz) {
	if (debugThisLib == 1) {
		printf("init enter\n");
	}
	//printf("sizeof(jmethodID) = %d\n",sizeof(jmethodID));

	jint result = (*env)->GetJavaVM(env, &vm); // Get all created JavaVMs
	if (result != 0) {
		printf("GetJavaVM err %d", result);
	}
	initJVMTI();
	//printf("func env pointer=%d\n",&env);
	//printenv(env);
	jclass klass = (*env)->FindClass(env,
	//"org/isty/viewer/ObjectWrapper");
			"java/lang/Object");
	klassFollowRef = (*env)->NewGlobalRef(env, klass);
	if (debugThisLib == 1) {
		printf("init exit\n");
	}
}


JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm2, char *options, void *reserved) {
    wasAgentOnLoadInvoked = 2;
	vm = vm2;
	if (debugThisLib == 1) {
		printf("Agent_OnLoad\n");
	}
	initJVMTI();
	//	initJVMTI();

	if (debugThisLib == 1) {
		printf("Agent_OnLoad exit\n");
	}
	return 0;
}

JNIEXPORT void JNICALL
Agent_OnUnload(JavaVM *vm) {
	if (debugThisLib == 1) {
		printf("Agent_OnUnload\n");
	}
}

static jint JNICALL
cbObjectTagInstance(jvmtiHeapReferenceKind reference_kind,
		const jvmtiHeapReferenceInfo* reference_info, jlong class_tag,
		jlong referrer_class_tag, jlong size, jlong* tag_ptr,
		jlong* referrer_tag_ptr, jint length, void* user_data) {
	printf("cbObjectTagInstance enter\n");
	//(*jvmtiHeapReferenceCallbackTarger)(reference_kind,reference_info,class_tag,
	//		referrer_class_tag,size,tag_ptr,referrer_tag_ptr,length,user_data);
	//JNIEnv* env=getJniEnvNick7();
	(*pt2FunctionSimpleStatic)();
	//(jvmtiHeapReferenceCallbackTarger)(JVMTI_HEAP_REFERENCE_CLASS,NULL,0,1,2,NULL,NULL,0,NULL);
	//	(*jvmtiHeapReferenceCallbackTarger)(reference_kind,NULL,NULL,			NULL,NULL,NULL,NULL,NULL,NULL);
	printf("cbObjectTagInstance exit\n");
}

JNIEXPORT jvmtiHeapReferenceCallback* JNICALL getJvmtiHeapCallbacks9() {
	return &cbObjectTagInstance2;
}

JNIEXPORT jvmtiEnv* JNICALL getSpecialJvmti() {
	jvmtiEnv *jvmti2;
	jvmtiError error;
	int rc;

	/* Get one time use JVMTI Env */
	jvmtiCapabilities caps;
	rc = (*vm)->GetEnv(vm, &jvmti2, JVMTI_VERSION_1_0);
	if (rc != JNI_OK) {
		printf(" err1 %d\n", rc);
		return NULL;
	}
	(void) memset(&caps, 0, (int) sizeof(caps));
	caps.can_tag_objects = 1;
	error = (*jvmti2)->AddCapabilities(jvmti2, &caps);
	if (error != JVMTI_ERROR_NONE) {
		printf(" err2 %d\n", error);
		return NULL;
	}
	if (debugThisLib == 1) {
		printf("getSpecialJvmti exit\n");
	}
	return jvmti2;
}

