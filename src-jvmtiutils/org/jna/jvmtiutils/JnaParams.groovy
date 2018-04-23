package org.jna.jvmtiutils

import groovy.transform.CompileStatic
import net.sf.jremoterun.utilities.JrrClassUtils
import net.sf.jremoterun.utilities.classpath.MavenCommonUtils
import net.sf.jremoterun.utilities.classpath.MavenDefaultSettings
import net.sf.jremoterun.utilities.nonjdk.PidDetector
import net.sf.jremoterun.utilities.nonjdk.classpath.CustomObjectHandlerImpl
import net.sf.jremoterun.utilities.nonjdk.classpath.refs.GitReferences
import org.apache.commons.io.FileUtils

import java.util.logging.Logger

@CompileStatic
public class JnaParams {

    // public static File filsDir;

    private static final Logger log = JrrClassUtils.getJdkLogForCurrentClass();

    public static volatile boolean inited = false;
    public static File jnaDispatchFullPathDefault;
    public static File resourceDirDefault;
    public static volatile JnaParams jnaParams;
    public static volatile Object lock = new Object();


    static JnaParams getJnaParamsRef() {
        if (jnaParams == null) {
            synchronized (lock) {
                if (jnaParams == null) {
                    jnaParams = new JnaParams();
                }
            }
        }
        return jnaParams;
    }

    public boolean isArch64 = "amd64".equals(System.getProperty("os.arch"));

    public String arch = isArch64 ? "64" : "32";

//    public File libDir;
//    public String jnaDispatchLibName;

    public File jnaNativeUtilsFullPath;

    public File jnaDispatchLibPathFull = jnaDispatchFullPathDefault;

    public File jniMethodProp;

    public File jvmtiProp;
    public File jvmtiErrorCodes;

    public boolean linux = System.getProperty("os.name").startsWith("Linux");

    public String nativeLibPostfix = linux ? ".so" : ".dll";
    public String jnaJvmtiLibName = "jna-native-utils";

//    protected void initLibDir() {
//
//        if (linux) {
//            libDir = new File("/w/c/workspace/jnaMy/file2");
//        } else {
//            libDir = new File("C:/workspace/jnaMy/file2");
//        }
//    }

//    protected void initJnaDsipoatcherLibName() {
//        jnaDispatchLibName = "nikjnidispatch" + arch;
//    }

//    protected void initJnaNativeUtilsLibName() {
//        jnaLibNativeUtilsName = jnaJvmtiLibName + arch;
//    }

    File initJnaDispatchLibPathFull(){
        String libJnaName = 'nikjnidispatch' + arch + nativeLibPostfix;
        File f = new File(GitReferences.jnaRepo.resolveToFile(), 'dist/' + libJnaName);
        assert f.exists()
        CustomObjectHandlerImpl customObjectHandler = (CustomObjectHandlerImpl) MavenDefaultSettings.mavenDefaultSettings.customObjectHandler;
        MavenCommonUtils mcu = new MavenCommonUtils()
        String pathToParent = mcu.getPathToParent(customObjectHandler.cloneGitRepo3.gitBaseDir, f)
        File replicaFile = new File(customObjectHandler.replicaDir, pathToParent)
        return copyNativeLib(f, replicaFile)
    }

    void initJnaNativeUtilsFullPath() {
        List<File> nativeAgents = PidDetector.getNativeAgents3()
        jnaNativeUtilsFullPath = nativeAgents.find { it.name.startsWith(jnaJvmtiLibName) }
        if (jnaNativeUtilsFullPath == null) {
            throw new JnaJvmtiNotInstalledException("jna native not found from nativeAgents = ${nativeAgents}")
        }
    }

    void initJnaNativeUtils() {
        if (jnaDispatchLibPathFull == null) {
            jnaDispatchLibPathFull = initJnaDispatchLibPathFull()
        }
        assert jnaDispatchLibPathFull.exists()
        log.info "jnaDispatchLibPathFull : ${jnaDispatchLibPathFull}"
        if (jnaNativeUtilsFullPath == null) {
            initJnaNativeUtilsFullPath();
        }
        if (!jnaNativeUtilsFullPath.exists()) {
            throw new FileNotFoundException(jnaNativeUtilsFullPath.absolutePath)
        }
    }

    File copyNativeLib(File src, File dest) {
        assert src.exists()
        dest.parentFile.mkdirs()
        assert dest.parentFile.exists()
        int retry = -1;
        while (true) {
            retry++
            if (retry > 10) {
                throw new Exception("too many retry ${retry} ${src} ${dest}")
            }
            File dest2 = dest
            if (retry > 0) {
                dest2 = new File(dest.parentFile, "${retry}/${src.name}")
                dest2.parentFile.mkdir()
            }
            boolean needCopy = CustomObjectHandlerImpl.isCopyFileNeeded(src, dest2)
            if (!needCopy) {
                return dest2
            }
            try {
                FileUtils.copyFile(src, dest2)
                assert src.length() == dest2.length()
                dest2.setLastModified(src.lastModified())
                return dest2
            } catch (IOException e) {
                log.info("${src} ${dest2}", e);
            }
        }

    }

    protected void initOther() {
        File resourcesDir = resourceDirDefault
        if(resourcesDir==null) {
            resourcesDir = GitReferences.jnaJvmtiResourcesDir.resolveToFile();
        }
        assert resourcesDir.exists()
        jniMethodProp = new File(resourcesDir, "jni-method.prop");
        jvmtiProp = new File(resourcesDir, "jvmti-method.prop");
        jvmtiErrorCodes = new File(resourcesDir, "jvmti-err-code.txt");
    }

    public JnaParams() {
        if (inited) {
            throw new IllegalStateException("already inited");
        }
        doInit3();
//        inited = true
//        initLibDir();
//        initJnaDsipoatcherLibName();
//        initJnaNativeUtilsLibName();
    }

    void doInit3(){
        initJnaNativeUtils();
        initOther();
    }
}
