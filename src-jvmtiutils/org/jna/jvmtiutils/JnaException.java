package org.jna.jvmtiutils;



import sun.reflect.Reflection;

public class JnaException extends Exception {


    public JnaException(int resultCode) {
        super("result code = " + resultCode);
    }

    public JnaException(String msg) {
        super(msg);
    }

    public JnaException() {

    }

}
