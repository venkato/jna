package org.jna.jvmtiutils;





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
