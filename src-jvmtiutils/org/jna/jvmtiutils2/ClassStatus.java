package org.jna.jvmtiutils2;

public class ClassStatus {

    // Class bytecodes have been verified 1
    public boolean JVMTI_CLASS_STATUS_VERIFIED;

    // Class preparation is complete 2
    public boolean JVMTI_CLASS_STATUS_PREPARED;

    // (4) Class initialization is complete. Static initializer has been run.
    public boolean JVMTI_CLASS_STATUS_INITIALIZED;

    // 8 Error during initialization makes class unusable
    public boolean JVMTI_CLASS_STATUS_ERROR;

    // 16 Class is an array. If set, all other bits are zero.
    public boolean JVMTI_CLASS_STATUS_ARRAY;

    // 32 Class is a primitive class (for example, java.lang.Integer.TYPE). If
    // set, all other bits are zero.
    public boolean JVMTI_CLASS_STATUS_PRIMITIVE;

    @Override
    public String toString() {

        return "initial=" + JVMTI_CLASS_STATUS_INITIALIZED + " verif="
                + JVMTI_CLASS_STATUS_VERIFIED + " err"
                + JVMTI_CLASS_STATUS_ERROR;
    }
}
