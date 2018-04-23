package org.jna.jvmtiutils;

import net.sf.jremoterun.utilities.JrrClassUtils;
import java.util.logging.Logger;
import groovy.transform.CompileStatic;


@CompileStatic
class JnaJvmtiNotInstalledException extends Exception{

    JnaJvmtiNotInstalledException(String var1) {
        super(var1);
    }
}
