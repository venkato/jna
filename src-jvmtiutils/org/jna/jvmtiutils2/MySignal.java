package org.jna.jvmtiutils2;

import com.sun.nik.Callback;

public interface MySignal extends Callback {

    void invoke(int signal);
}