package org.jna.jvmtiutils2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import net.sf.jremoterun.JrrUtils;
import net.sf.jremoterun.SimpleJvmTiAgent;
import net.sf.jremoterun.utilities.DefaultObjectName;
import net.sf.jremoterun.utilities.JrrClassUtils;
import net.sf.jremoterun.utilities.JrrUtilities;
import net.sf.jremoterun.utilities.MBeanFromJavaBean;

import net.sf.jremoterun.utilities.javassist.JrrJavassistUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jna.jvmtiutils.JnaNativeMethods;



public class JnaBean implements DefaultObjectName {

	public static ObjectName objectName = JrrUtils.createObjectName("jna:t=jna");

	public static JnaBean jnaBean = new JnaBean();

	public static Object lock = new Object();

	private static final Log log = LogFactory.getLog(JnaBean.class);

	public void notifyLock() {
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	public long getObjectSize(final Object object) throws Exception {
		return JvmTiMethods.getObjectSize(object);
	}

	public MontorObjectUsage getObjectMontoreInfo(final Object object) throws Exception {
		return JvmTiMethods.getObjectModnitorState(object);
	}

	public ObjectName getDefaultObjectName() throws MalformedObjectNameException {
		return objectName;
	}

	public WeakHashMap findRef(final Object object, int maxCount) throws Exception {
		final Date start = new Date();
		log.info(start);
		System.out.println(start);
		final WeakHashMap res = JvmTiMethods.findRef2(object, maxCount);
		long end = System.currentTimeMillis();
		end = end - start.getTime();
		end = end / 1000;
		System.out.println("take time " + end + " sec");
		return res;

	}

	public Set<File> getLoadedLibs() throws Exception {
		final Class class1 = Class.forName("java.lang.ClassLoader$NativeLibrary");
		final Set set = new HashSet(JvmTiMethods.findClassInstances2(class1, 1000).keySet());
		final Set<File> res = new TreeSet();
		for (final Object object : set) {
			final String string = (String) JrrClassUtils.getFieldValue(object, "name");
			// log.info(string);
			res.add(new File(string));
		}
		return res;
	}

	public <T> T findClassInstance(final Class<T> class1) throws Exception {
		HashSet<T> s = findClassInstances(class1);
		int size = s.size();
		if (size == 0) {
			return null;
		}
		if (size > 1) {
			throw new Exception("too many instance " + size);
		}
		return s.iterator().next();
	}

	public <T> HashSet<T> findClassInstances(final Class<T> class1) throws Exception {
		return findClassInstances(class1, 2000);
	}

	public <T> HashSet<T> findClassInstances(final Class<T> class1, int maxCount) throws Exception {
		final Date start = new Date();
		log.info(start);
		System.out.println(start);
		final WeakHashMap res = JvmTiMethods.findClassInstances2(class1, maxCount);
		long end = System.currentTimeMillis();
		end = end - start.getTime();
		end = end / 1000;
		System.out.println("take time " + end + " sec");
		return new HashSet(res.keySet());

	}

	public Object findClassInstancesHuman(final Class class1, int maxCount) throws Exception {
		HashSet<Object> findClassInstances = findClassInstances(class1, maxCount);
		if (findClassInstances.size() == 0) {
			return null;
		}
		if (findClassInstances.size() == 1) {
			return findClassInstances.iterator().next();
		}
		return findClassInstances;
	}

	public ArrayList getThreadMonitors(final Thread thread) throws Exception {
		return JvmTiMethods.getMonitorOwner(thread);
	}

	public Object getThreadWaiting(final Thread thread) throws Exception {
		return JvmTiMethods.getMonitorOwner2(thread);
	}

	public static void updateBean() throws Exception {
		JnaNativeMethods.init();
		JrrUtils.unregisterMBeanQuiet(null, JnaBean.objectName);
		MBeanFromJavaBean.registerMBean(jnaBean);
		synchronized (new Object()) {
			final Object object = JvmTiMethods.getMonitorOwner(Thread.currentThread());
			log.info(object);
		}
	}

	public void reloadClass(final String className) throws Exception {
		final Class class1 = JrrClassUtils.getCurrentClassLoader().loadClass(className.trim());
		SimpleJvmTiAgent.redefineClasses(class1);
	}

	public void reloadClassAndAnonClasses(final String className) throws Exception {
		final Class class1 = JrrClassUtils.getCurrentClassLoader().loadClass(className.trim());
		JrrJavassistUtils.reloadClassAndAnonClasses(class1);
//		JvmTiMethods.reloadClassAndAnonClasses(class1);
	}

	public void reloadClassAndAnonClasses(final Class clazz) throws Exception {
		JrrJavassistUtils.reloadClassAndAnonClasses(clazz);
//		JvmTiMethods.reloadClassAndAnonClasses(clazz);
	}

	public Set<ClassLoader> findClassLoaders() throws Exception {
		return findClassInstances(ClassLoader.class);
	}

	public Set<FileOutputStream> findFileOutputStreams() throws Exception {
		return findClassInstances(FileOutputStream.class);
	}

	public Set<FileInputStream> findFileInputStreams() throws Exception {
		return findClassInstances(FileInputStream.class);
	}

	public Set<ServerSocket> findServerSocket() throws Exception {
		return findClassInstances(ServerSocket.class);
	}

	public Set<java.lang.ProcessBuilder> findNativeProcess() throws Exception {
		return findClassInstances(ProcessBuilder.class);
	}

	public Set<Connection> findSqlConnection() throws Exception {
		FileInputStream.class.getClass();
		return findClassInstances(Connection.class);
	}

	public Set<Socket> findSocket() throws Exception {
		final Set<Socket> socket = findClassInstances(Socket.class);
		final Set<Socket> res = new HashSet(socket.size());
		for (final Socket socket2 : socket) {
			if (!socket2.isClosed()) {
				res.add(socket2);
			}
		}
		return res;
	}

}
