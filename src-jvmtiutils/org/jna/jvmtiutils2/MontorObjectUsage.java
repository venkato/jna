package org.jna.jvmtiutils2;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class MontorObjectUsage {

    private static final Log log = LogFactory.getLog(MontorObjectUsage.class);

    public Thread monitorOwner;

    public ArrayList notifiers = new ArrayList();

    public ArrayList waitiers = new ArrayList();

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        if (monitorOwner != null) {
            sb.append("monitorOwner=").append(monitorOwner).append(" ");
        }

        if (notifiers.size() != 0) {
            sb.append("notifiers=").append(notifiers).append(" ");
        }

        if (waitiers.size() != 0) {
            sb.append("waitiers=").append(waitiers).append(" ");
        }

        return sb.toString();
    }
}
