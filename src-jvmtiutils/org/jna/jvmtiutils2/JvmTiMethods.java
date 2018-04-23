package org.jna.jvmtiutils2;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import junit.framework.Assert;
import net.sf.jremoterun.SimpleJvmTiAgent;
import net.sf.jremoterun.utilities.JavaVM;
import net.sf.jremoterun.utilities.JrrClassUtils;
import net.sf.jremoterun.utilities.ObjectWrapper;

import net.sf.jremoterun.utilities.javassist.JrrJavassistUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.jna.jvmtiutils.JnaException;
import org.jna.jvmtiutils.JnaNativeMethods;
import org.jna.jvmtiutils.JnaNativeUtils;
import org.jna.jvmtiutils.JnaParams;
import org.jna.jvmtiutils.JvmTiFunction;
import org.jna.jvmtiutils.jvmtiClassDefinition;

import com.sun.nik.Library;
import com.sun.nik.Memory;
import com.sun.nik.Native;
import com.sun.nik.NativeLong;
import com.sun.nik.Pointer;
import com.sun.nik.Library.Handler;

public class JvmTiMethods {

	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JvmTiMethods.class);

	protected static HashSet<Class> primitiveIntClassMapping = new HashSet();

	static {
		try {
			JnaNativeMethods.init();
		} catch (Exception e) {
			log.warn(null, e);
			throw new Error(e);
		}
		primitiveIntClassMapping.add(int.class);
		primitiveIntClassMapping.add(byte.class);
		primitiveIntClassMapping.add(boolean.class);
		primitiveIntClassMapping.add(short.class);
		primitiveIntClassMapping.add(char.class);
	}

	public static ArrayList<Class> findCLassesAndAnonClasses(final Class class1) {
		final ArrayList<Class> classes = new ArrayList();
		classes.add(class1);
		final ClassLoader classLoader = class1.getClassLoader();
		int i = 1;
		while (true) {
			try {
				final Class class2 = classLoader.loadClass(class1.getName() + "$" + i);
				classes.add(class2);
				i++;
			} catch (final ClassNotFoundException e) {
				log.info(i);
				break;
			}
		}
		return classes;
	}

	@Deprecated
	public static void reloadClassAndAnonClasses(final Class class1) throws Exception {
		JrrJavassistUtils.reloadClassAndAnonClasses(class1);
//		final List<Class> classes = JrrJavassistUtils.getRelatedClasses2(class1);
//		final ArrayList<Class> classes = new ArrayList();
//		classes.add(class1);
//		final ClassLoader classLoader = class1.getClassLoader();
//		int i = 1;
//		while (true) {
//			try {
//				final Class class2 = classLoader.loadClass(class1.getName() + "$" + i);
//				classes.add(class2);
//				i++;
//			} catch (final ClassNotFoundException e) {
//				log.info(i);
//				break;
//			}
//		}
//		SimpleJvmTiAgent.redefineClasses(classes.toArray(new Class[0]));
	}

	// now work
	public static void redefineClass(final Class clazz, byte[] content) throws Exception {
		Memory memory2 = new Memory(content.length);
		memory2.write(0, content, 0, content.length);
		Memory memory = new Memory(100);
		jvmtiClassDefinition classDefinition = new jvmtiClassDefinition();
		Pointer pointer22 = new Pointer(JnaNativeMethods.convertObjectToPointer2(clazz));
		classDefinition.klass = pointer22;
		classDefinition.class_bytes = memory2;
		classDefinition.class_byte_count = content.length;
		Pointer pointer = classDefinition.getPointer();
		// final Memory sizePointer = new Memory(JnaParams.shift);
		// sizePointer.setPointer(0, sizePointer);
		final JvmTiFunction jvmTiFunction = new JvmTiFunction("RedefineClasses");
		jvmTiFunction.invokeJvmTiFunction(new Object[] { 1, pointer });
	}

	public static void addPathToBootClassPath(final File path) throws Exception {
		final JvmTiFunction jvmTiFunction = new JvmTiFunction("AddToBootstrapClassLoaderSearch");
		jvmTiFunction.invokeJvmTiFunction(new Object[] { path.getAbsolutePath() });
	}

	public static long getObjectSize(final Object object) throws Exception {
		final Memory sizePointer = new Memory(10);
		final JvmTiFunction jvmTiFunction = new JvmTiFunction("GetObjectSize");
		jvmTiFunction.invokeJvmTiFunction(new Object[] { object, sizePointer });
		final long size;
		if (JnaParams.jnaParams.isArch64) {
			size = sizePointer.getLong(0);
		} else {
			size = sizePointer.getInt(0);
		}
		return size;
	}

	public static ArrayList<Class> getLoadedClasses() throws Exception {
		final ObjectWrapper<ArrayList<Class>> result2 = new ObjectWrapper(null);
		final Memory classesNumber = new Memory(Native.POINTER_SIZE);
		final Memory classesPointer = new Memory(Native.POINTER_SIZE);
		final JvmTiFunction jvmTiFunction = new JvmTiFunction(78) {

			@Override
			public void processResultBeforeExcJvmTi(final int flags, final Object[] params, final long resultPointer) {
				final int classNumbers = classesNumber.getInt(0);
				log.info(classNumbers);
				final ArrayList<Class> result = new ArrayList(classNumbers + 10);
				for (int i = 0; i < classNumbers; i++) {
					final long address = JnaNativeMethods.getPointerAddress(classesPointer.getPointer(0).getPointer(0));
					long addr2 = address + i * Native.POINTER_SIZE;
					final Object object = JnaNativeMethods.convertPointerToObject(new Pointer(addr2));
					log.info("i=" + i + " " + object);
					result.add((Class) object);
				}
				result2.setObject(result);
			}

		};
		jvmTiFunction.invokeJvmTiFunction(new Object[] { classesNumber, classesPointer });
		return result2.object;
	}

	@Deprecated
	public static Object[] findInstance(final Class klass, final int maxCount) throws Exception {
		log.debug(0);
		log.debug(6);
		final Pointer heapCallbakPointerFuncObly = JnaNativeUtils.INSTANCE.getJvmtiHeapCallbacks9();
		// Assert.assertNotNull(heapCallbakPointer);
		final Pointer heapCallbakPointer = new Memory(100);
		heapCallbakPointer.setPointer(Native.POINTER_SIZE, heapCallbakPointerFuncObly);
		log.debug(7);
		final Pointer jvmTiPointer = JnaNativeUtils.INSTANCE.getSpecialJvmti();
		final JvmTiFunction jvmTiFunction = new JvmTiFunction("FollowReferences", jvmTiPointer);
		final int heap_filter = 0;
		// Class klass = ObjectWrapper.class;
		log.debug(0);
		final Object initial_object = null;
		final Pointer classInstancesData = new Memory(100); // ClassInstancesData
		final int flag = 10;
		classInstancesData.setInt(0, 0);
		classInstancesData.setInt(4, maxCount);
		classInstancesData.setNativeLong(8, new NativeLong(flag));
		classInstancesData.setInt(12, 0);
		log.debug(1);
		jvmTiFunction.invokeJvmTiFunction(
				new Object[] { heap_filter, klass, initial_object, heapCallbakPointer, classInstancesData });
		log.debug(2);
		if (classInstancesData.getInt(12) != 0) {
			log.debug("err = " + classInstancesData.getInt(12));
			throw new JnaException(classInstancesData.getInt(12));
		}
		final int count = classInstancesData.getInt(0);
		log.debug("ref count = " + count);
		log.debug(2);
		final Pointer objects = new Memory(Native.POINTER_SIZE * count + Native.POINTER_SIZE);
		log.debug("8");
		final Pointer count2 = new Memory(10);
		count2.setInt(0, -1);
		final Object[] result = new Object[count];
		final JvmTiFunction getObjectsWithTagsJvmTiFunction = new JvmTiFunction("GetObjectsWithTags", jvmTiPointer) {

			@Override
			public void processResultBeforeExcJvmTi(final int flags, final Object[] params, final long resultPointer) {
				// Pointer[] objects2=objects.getPointerArray(0);
				{
					log.debug("count2 =" + count2.getInt(0));
					Assert.assertEquals(count, count2.getInt(0));
					final Pointer objectss = objects.getPointer(0);
					for (int i = 0; i < count; i++) {
						final Pointer pointer = objectss.getPointer(i * Native.POINTER_SIZE);

						int address;
						try {
							final Object object = JnaNativeMethods.convertPointerToObject(pointer);
							log.debug(object);
							result[i] = object;
						} catch (final Exception e) {
							// TODO Auto-generated catch block
							log.warn(null, e);
						}
					}
				}
			}
		};
		final int tag_count = 1;
		final Pointer tags = new Pointer(JnaUtils2.getPointerAddress(classInstancesData) + 8);
		log.debug(tags.getNativeLong(0));
		log.debug("9");
		getObjectsWithTagsJvmTiFunction.invokeJvmTiFunction(new Object[] { tag_count, tags, count2, objects, null });
		// GetObjectsWithTags(jvmtiEnv* env,
		// jint tag_count,
		// const jlong* tags,
		// jint* count_ptr,
		// jobject** object_result_ptr,
		// jlong** tag_result_ptr)
		// error = JVMTI_FUNC_PTR(jvmti,GetObjectsWithTags)
		// (jvmti, 1, &(data.objTag), &(instances->count),
		// &(instances->objects), NULL);
		disposeJvmTiEnv(jvmTiPointer);
		return result;
	}

	public static MontorObjectUsage getObjectModnitorState(final Object object) throws Exception {
		final Pointer resultPointer2 = new Memory(100);
		// final Pointer objectsPointer = new Memory(10);
		// jvmtiMonitorUsage monitorUsage=new jvmtiMonitorUsage
		final ObjectWrapper<MontorObjectUsage> result = new ObjectWrapper(null);
		final JvmTiFunction jvmTiFunction = new JvmTiFunction("GetObjectMonitorUsage") {

			@Override
			public void processResultBeforeExcJvmTi(final int flags, final Object[] params, final long resultPointer) {
				final MontorObjectUsage montorObjectUsage = new MontorObjectUsage();
				final jvmtiMonitorUsage monitorUsage = new jvmtiMonitorUsage(resultPointer2);
				monitorUsage.autoRead();
				try {
					{
						montorObjectUsage.monitorOwner = (Thread) JnaNativeMethods
								.convertPointerToObject(monitorUsage.owner);
						montorObjectUsage.waitiers.ensureCapacity(monitorUsage.waiter_count);
						for (int i = 0; i < monitorUsage.waiter_count; i++) {
							final Pointer pointer = monitorUsage.waiters.getPointer(i * Native.POINTER_SIZE);
							final Object object = JnaNativeMethods.convertPointerToObject(pointer);
							montorObjectUsage.waitiers.add(object);

						}
					}
					{
						montorObjectUsage.notifiers.ensureCapacity(monitorUsage.notify_waiter_count);
						for (int i = 0; i < monitorUsage.notify_waiter_count; i++) {
							final Pointer pointer = monitorUsage.notify_waiters.getPointer(i * Native.POINTER_SIZE);
							final Object object = JnaNativeMethods.convertPointerToObject(pointer);
							montorObjectUsage.notifiers.add(object);

						}
					}
					montorObjectUsage.waitiers.removeAll(montorObjectUsage.notifiers);
					result.setObject(montorObjectUsage);
				} catch (final Exception e) {
					log.info(null, e);
					throw new Error(e);
				}
			}
		};
		// Assert.assertEquals(jvmTiFunction.functionNumer, 10);
		jvmTiFunction.invokeJvmTiFunction(new Object[] { object, resultPointer2 });
		log.debug(result);
		return result.getObject();
	}

	public static ArrayList getMonitorOwner(final Thread thread) throws Exception {
		final Pointer countPointer = new Memory(10);
		final Pointer objectsPointer = new Memory(10);
		final ArrayList result = new ArrayList();
		final JvmTiFunction jvmTiFunction = new JvmTiFunction("GetOwnedMonitorInfo") {

			@Override
			public void processResultBeforeExcJvmTi(final int flags, final Object[] params, final long resultPointer) {
				log.info(1);
				super.processResultBeforeExcJvmTi(flags, params, resultPointer);
				final int count = countPointer.getInt(0);
				log.debug(count);
				if (count == 0) {

				} else {
					result.ensureCapacity(count);
					Pointer obj = objectsPointer.getPointer(0);
					obj = obj.getPointer(0);
					try {
						long adddr = JnaUtils2.getPointerAddress(obj);
						for (int i = 0; i < count; i++) {
							final Object res = JnaNativeMethods.convertPointerToObject(new Pointer(adddr));
							log.debug(res == null);
							log.debug(res.getClass());
							log.debug(res);
							result.add(res);
							adddr += Native.POINTER_SIZE;
						}
					} catch (final Exception e) {
						// TODO Auto-generated catch block
						log.warn(null, e);
					}
				}
			}
		};
		Assert.assertEquals(jvmTiFunction.functionNumer, 10);
		jvmTiFunction.invokeJvmTiFunction(new Object[] { thread, countPointer, objectsPointer });
		log.debug(result);
		return result;
	}

	public static Object getMonitorOwner2(final Thread thread) throws Exception {
		// final Pointer countPointer = new Memory(10);
		final Pointer objectsPointer = new Memory(10);
		final ObjectWrapper result = new ObjectWrapper(null);
		final JvmTiFunction jvmTiFunction = new JvmTiFunction("GetCurrentContendedMonitor") {

			@Override
			public void processResultBeforeExcJvmTi(final int flags, final Object[] params, final long resultPointer) {
				super.processResultBeforeExcJvmTi(flags, params, resultPointer);

				final Pointer obj = objectsPointer.getPointer(0);
				// obj = obj.getPointer(0);
				// obj = obj.getPointer(0);
				if (obj == null) {
					log.debug("add is null");
				} else {
					try {
						final Object res = JnaNativeMethods.convertPointerToObject(obj);
						log.debug(res == null);
						log.debug(res.getClass());
						log.debug(res);
						result.setObject(res);
						// result.add(res);

					} catch (final Exception e) {
						// TODO Auto-generated catch block
						log.warn(null, e);
					}
				}
			}
		};
		Assert.assertEquals(jvmTiFunction.functionNumer, 11);
		jvmTiFunction.invokeJvmTiFunction(new Object[] { thread, objectsPointer });
		// log.debug(result);
		return result;
	}

	private static Pointer getPointer() {
		final Pointer heapCallbakPointerFuncObly = JnaNativeUtils.INSTANCE.getAllRefFuncRef(1);
		final Pointer heapCallbakPointerFuncObly2 = JnaNativeUtils.INSTANCE.getAllRefFuncRef(2);
		final Pointer heapCallbakPointerFuncObly3 = JnaNativeUtils.INSTANCE.getAllRefFuncRef(3);
		final Pointer heapCallbakPointerFuncObly4 = JnaNativeUtils.INSTANCE.getAllRefFuncRef(4);
		// Assert.assertNotNull(heapCallbakPointer);
		final Pointer heapCallbakPointer = new Memory(100);
		heapCallbakPointer.setPointer(0, heapCallbakPointerFuncObly);
		heapCallbakPointer.setPointer(8, heapCallbakPointerFuncObly2);
		heapCallbakPointer.setPointer(12, heapCallbakPointerFuncObly3);
		heapCallbakPointer.setPointer(16, heapCallbakPointerFuncObly4);
		return heapCallbakPointer;
	}

	public static WeakHashMap findRef(final Object objectToFInd, int maxCount) throws Exception {
		log.debug(0);
		JavaVM.javaVM.runGC();
		// final ObjectWrapper result3 = new ObjectWrapper(null);
		log.debug(6);
		final Pointer heapCallbakPointer = getPointer();
		log.debug(7);
		final Pointer jvmTiPointer = JnaNativeUtils.INSTANCE.getSpecialJvmti();
		final JvmTiFunction jvmTiFunction = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("IterateThroughHeap")), jvmTiPointer);
		Assert.assertEquals(jvmTiFunction.functionNumer, 116);
		final int heap_filter = 4;
		final Class klass = null;
		// ObjectWrapper.class;
		log.debug(0);
		final Object initial_object = null;
		final Pointer userData = new Memory(100);
		final int flag = 10;
		userData.setInt(0, 0);
		userData.setInt(4, 10);
		userData.setNativeLong(8, new NativeLong(flag));
		userData.setInt(12, 0);
		log.debug(1);
		JnaNativeUtils.INSTANCE.setDebugThisLibMode(0);
		log.debug(2);
		jvmTiFunction.invokeJvmTiFunction(new Object[] { heap_filter, klass, heapCallbakPointer, userData });
		log.debug(2);
		if (userData.getInt(12) != 0) {
			log.info("err = " + userData.getInt(12));
		}
		final int count = userData.getInt(0);
		log.info("ref count = " + count);
		final Pointer tags = new Pointer(JnaUtils2.getPointerAddress(userData) + 8);
		// final Object[] result = new Object[count];
		// IdentityHashMap hashMap = new IdentityHashMap(count);
		final WeakHashMap res3 = getObjectWithTag(objectToFInd, jvmTiPointer, count, tags, maxCount);
		disposeJvmTiEnv(jvmTiPointer);
		return res3;
	}

	public static WeakHashMap findRef2(final Object objectToFInd, int maxCount) throws Exception {
		log.debug(0);
		JavaVM.javaVM.runGC();
		// final ObjectWrapper result3 = new ObjectWrapper(null);
		log.debug(6);
		final Pointer heapCallbakPointer = JnaNativeUtils.INSTANCE.getAllRefFuncRef(5);
		log.debug(7);
		final Pointer jvmTiPointer = JnaNativeUtils.INSTANCE.getSpecialJvmti();
		final JvmTiFunction jvmTiFunction = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("IterateOverHeap")), jvmTiPointer);
		Assert.assertEquals(jvmTiFunction.functionNumer, 111);
		final int heap_filter = 3;
		// Class klass = null;
		// ObjectWrapper.class;
		log.debug(0);
		// Object initial_object = null;
		final Pointer userData = new Memory(100);
		final int flag = 10;
		userData.setInt(0, 0);
		userData.setInt(4, 10);
		userData.setNativeLong(8, new NativeLong(flag));
		userData.setInt(12, 0);
		log.debug(1);
		JnaNativeUtils.INSTANCE.setDebugThisLibMode(0);
		log.debug(2);
		jvmTiFunction.invokeJvmTiFunction(new Object[] { heap_filter, heapCallbakPointer, userData });
		log.debug(2);
		if (userData.getInt(12) != 0) {
			log.info("err = " + userData.getInt(12));
		}
		int count = userData.getInt(0);
		log.info("ref count = " + count);
		// count=Math.min(count, maxCount);
		final Pointer tags = new Pointer(JnaUtils2.getPointerAddress(userData) + 8);
		// IterateOverHeap(jvmtiEnv* env,
		// jvmtiHeapObjectFilter object_filter,
		// jvmtiHeapObjectCallback heap_object_callback,
		// const void* user_data)
		final WeakHashMap res3 = getObjectWithTag(objectToFInd, jvmTiPointer, count, tags, maxCount);
		disposeJvmTiEnv(jvmTiPointer);
		return res3;
	}

	static final Object objectSimple = new Object();

	public static WeakHashMap getObjectWithTag(final Object objectToFInd, final Pointer jvmTiPointer,
			final int appritiateCOunt, final Pointer tags, final int maxCount) throws JnaException {
		log.info("HI----------------============");
		final WeakHashMap res3 = new WeakHashMap(30);
		final Pointer objects = new Memory(4 * appritiateCOunt + 4);
		log.debug("8");
		final Pointer count2 = new Memory(10);
		count2.setInt(0, -1);
		// final Object[] result = new Object[count];
		// IdentityHashMap hashMap = new IdentityHashMap(count);

		final JvmTiFunction getObjectsWithTagsJvmTiFunction = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("GetObjectsWithTags")), jvmTiPointer) {

			@Override
			public void processResultBeforeExcJvmTi(final int flags, final Object[] params, final long resultPointer) {
				// Pointer[] objects2=objects.getPointerArray(0);
				l: try {
					int count22 = count2.getInt(0);
					log.info("count2 =" + count22);
					// count22=Math.min(maxCount, count22);
					final Pointer objectss = objects.getPointer(0);
					int i = 0;
					int siuted = 0;
					do {
						final Pointer pointer = objectss.getPointer(i * Native.POINTER_SIZE);
						final Object object = JnaNativeMethods.convertPointerToObject(pointer);
						if (isSute(object, objectToFInd)) {
							log.info("new");
							res3.put(object, objectSimple);
							siuted++;
							if (siuted >= maxCount) {
								break l;
							}
						}
						i++;
					} while (i < count22);

				} catch (Exception e) {
					log.error(null, e);

				}
				// executer
			}
		};
		final int tag_count = 1;
		// Pointer tags = new Pointer(JnaUtils2.getPointerAddress(userData) +
		// 8);
		log.debug(tags.getNativeLong(0));
		log.debug("9");
		getObjectsWithTagsJvmTiFunction.invokeJvmTiFunction(new Object[] { tag_count, tags, count2, objects, null });
		// log.info(res3);
		// objectToFInd.hashCode();
		return res3;
	}

	public static void disposeJvmTiEnv(final Pointer jvmTiPointer) throws JnaException {
		final JvmTiFunction jvmTiFunctionDispose = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("DisposeEnvironment")), jvmTiPointer);
		jvmTiFunctionDispose.invokeJvmTiFunction(new Object[0]);

	}

	private static boolean isSute(final Object ref, final Object find) throws Exception {
		Class class1 = ref.getClass();
		if (class1 == Class.class) {
			final Class class2 = (Class) ref;
			if (isClassInitilizedHuman(class2).JVMTI_CLASS_STATUS_INITIALIZED) {
				final Field[] field = class2.getDeclaredFields();
				for (final Field field2 : field) {
					final int modif = field2.getModifiers();
					if (Modifier.isStatic(modif)) {
						field2.setAccessible(true);
						final Object o = field2.get(null);
						if (find == o) {
							return true;
						}
					}
				}
			}
		}
		if (class1.isArray()) {
			final int lenth = Array.getLength(ref);
			for (int i = 0; i < lenth; i++) {
				final Object o = Array.get(ref, i);
				if (find == o) {
					return true;
				}
			}
			return false;
		}
		while (class1 != Object.class || class1 == null) {
			final Field[] field = class1.getDeclaredFields();
			for (final Field field2 : field) {
				final int modif = field2.getModifiers();
				if (!Modifier.isStatic(modif)) {
					field2.setAccessible(true);
					final Object o = field2.get(ref);
					if (find == o) {
						return true;
					}
				}
			}
			class1 = class1.getSuperclass();
		}
		return false;
	}

	/**
	 * Not compilted
	 */
	public static Pointer getFrameLocation(Thread thread, final int depth) throws Exception {
		log.setLevel(Level.ALL);
		log.debug(0);
		// final ObjectWrapper result3 = new ObjectWrapper(null);
		final Pointer jvmTiPointer = JnaNativeUtils.INSTANCE.getSpecialJvmti();
		log.debug(8);
		final JvmTiFunction jvmTiFunction = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("GetFrameLocation")), jvmTiPointer);
		final Pointer methodPointer = new Memory(1000);
		final Pointer locationPointer = new Memory(100);
		JnaNativeUtils.INSTANCE.setDebugThisLibMode(0);
		log.debug(2);
		jvmTiFunction.invokeJvmTiFunction(new Object[] { thread, depth, methodPointer, locationPointer });
		log.debug(2);
		log.info(locationPointer.getLong(0));
		// Method method = (Method)
		// JnaNativeUtils.INSTANCE.convertPointerToObject(methodPointer.getPointer(0));
		// log.debug(6);
		return methodPointer;
	}

	public static void suspendThread(Thread thread) throws Exception {
		log.setLevel(Level.ALL);
		log.debug(0);
		// final ObjectWrapper result3 = new ObjectWrapper(null);
		final Pointer jvmTiPointer = JnaNativeUtils.INSTANCE.getSpecialJvmti();
		log.debug(8);
		final JvmTiFunction jvmTiFunction = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("SuspendThread")), jvmTiPointer);
		JnaNativeUtils.INSTANCE.setDebugThisLibMode(0);
		log.debug(2);
		jvmTiFunction.invokeJvmTiFunction(new Object[] { thread });
	}

	public static void setAllowObjectsAsArgs(Library library) throws Exception {
		Library.Handler fieldValue = (Handler) JrrClassUtils.getFieldValue(library, "h");
		Map fieldValue2 = (Map) JrrClassUtils.getFieldValue(fieldValue, "options");
		fieldValue2.put(Library.OPTION_ALLOW_OBJECTS, true);
	}

	public static void suspendThread2(Thread thread) throws Exception {
		log.setLevel(Level.ALL);
		log.debug(0);
		// final ObjectWrapper result3 = new ObjectWrapper(null);
		final Pointer jvmTiPointer = JnaNativeUtils.INSTANCE.getJvmTiAddress();
		final Pointer jvmTiPointer2 = JnaNativeUtils.INSTANCE.getJvmTiAddress();
		log.debug(8);
		final JvmTiFunction jvmTiFunction = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("SuspendThread")), jvmTiPointer);
		JnaNativeUtils.INSTANCE.setDebugThisLibMode(0);
		log.debug(2);
		jvmTiFunction.invokeInt(new Object[] {jvmTiPointer2,  thread });
	}

	public static void resumeThread2(Thread thread) throws Exception {
		log.setLevel(Level.ALL);
		log.debug(0);
		// final ObjectWrapper result3 = new ObjectWrapper(null);
		final Pointer jvmTiPointer = JnaNativeUtils.INSTANCE.getSpecialJvmti();
		log.debug(8);
		final JvmTiFunction jvmTiFunction = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("ResumeThread")), jvmTiPointer);
		JnaNativeUtils.INSTANCE.setDebugThisLibMode(0);
		log.debug(2);
		jvmTiFunction.invokeInt(new Object[] {jvmTiFunction,  thread });
	}


	
	public static Method getLocalVariableTable(Pointer method) throws Exception {
		log.setLevel(Level.ALL);
		log.debug(0);
		// final ObjectWrapper result3 = new ObjectWrapper(null);
		final Pointer jvmTiPointer = JnaNativeUtils.INSTANCE.getSpecialJvmti();
		log.debug(8);
		final JvmTiFunction jvmTiFunction = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("GetLocalVariableTable")), jvmTiPointer);
		final Pointer countPointer = new Memory(10);
		final Pointer enteriesPointer = new Memory(10);
		JnaNativeUtils.INSTANCE.setDebugThisLibMode(0);
		log.debug(2);
//		jvmTiFunction.invokeJvmTiFunction(new Object[] { method, countPointer, enteriesPointer });
		jvmTiFunction.invokeJvmTiFunction(new Object[] { method, countPointer, enteriesPointer });
		log.debug(2);
		int count = countPointer.getInt(0);
		log.info(count);
		// Method method = (Method)
		// JnaNativeUtils.INSTANCE.convertPointerToObject(methodPointer.getPointer(0));
		// log.debug(6);
		return null;
	}
	
	
	public static Method getLocalVariableTable2(Pointer method) throws Exception {
		log.setLevel(Level.ALL);
		log.debug(0);
		// final ObjectWrapper result3 = new ObjectWrapper(null);
		final Pointer jvmTiPointer = JnaNativeUtils.INSTANCE.getSpecialJvmti();
		log.debug(8);
		final JvmTiFunction jvmTiFunction = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("GetLocalVariableTable")), jvmTiPointer);
		final Pointer countPointer = new Memory(10);
		final Pointer enteriesPointer = new Memory(10);
		JnaNativeUtils.INSTANCE.setDebugThisLibMode(0);
		log.debug(2);
//		jvmTiFunction.invokeJvmTiFunction(new Object[] { method, countPointer, enteriesPointer });
		int resultCode = JnaNativeUtils.INSTANCE.GetLocalVariableTable(method, countPointer, enteriesPointer);
		log.info(resultCode);
		if(resultCode!=0) {
			log.info("!!! fault = " + resultCode + " " + JvmTiFunction.errCodes.get(resultCode + ""));
		}
		log.debug(2);
		int count = countPointer.getInt(0);
		log.info(count);
		// Method method = (Method)
		// JnaNativeUtils.INSTANCE.convertPointerToObject(methodPointer.getPointer(0));
		// log.debug(6);
		return null;
	}
	
	
	
	public static Method getLocalVariableTable3(Thread thread,int depth) throws Exception {
		log.setLevel(Level.ALL);
		log.debug(0);
		// final ObjectWrapper result3 = new ObjectWrapper(null);
		log.debug(8);
		final Pointer countPointer = new Memory(10);
		final Pointer enteriesPointer = new Memory(10);
		JnaNativeUtils.INSTANCE.setDebugThisLibMode(0);
		log.debug(2);
//		jvmTiFunction.invokeJvmTiFunction(new Object[] { method, countPointer, enteriesPointer });
		int resultCode = JnaNativeUtils.INSTANCE.GetLocalVariableTable3(thread, depth,countPointer, enteriesPointer);
		log.info(resultCode);
		if(resultCode!=0) {
			log.info("!!! fault = " + resultCode + " " + JvmTiFunction.errCodes.get(resultCode + ""));
		}
		log.debug(2);
		int count = countPointer.getInt(0);
		log.info(count);
		// Method method = (Method)
		// JnaNativeUtils.INSTANCE.convertPointerToObject(methodPointer.getPointer(0));
		// log.debug(6);
		return null;
	}
	
	public static void getCapabilities2() throws Exception {
		log.setLevel(Level.ALL);
		log.debug(0);
		// final ObjectWrapper result3 = new ObjectWrapper(null);
		final Pointer jvmTiPointer = JnaNativeUtils.INSTANCE.getJvmTiAddress();
		final Pointer jvmTiPointer2 = JnaNativeUtils.INSTANCE.getJvmTiAddress();
		log.debug(8);
		final JvmTiFunction jvmTiFunction = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("GetPotentialCapabilities")), jvmTiPointer);
		final Memory capabilitiesPointer = new Memory(16);
		capabilitiesPointer.clear();
		JnaNativeUtils.INSTANCE.setDebugThisLibMode(0);
		log.debug(2);
		jvmTiFunction.invokeInt(new Object[] {jvmTiPointer2, capabilitiesPointer });
		log.debug(2);
	}

	public static void getCapabilities() throws Exception {
		log.setLevel(Level.ALL);
		log.debug(0);
		// final ObjectWrapper result3 = new ObjectWrapper(null);
		final Pointer jvmTiPointer = JnaNativeUtils.INSTANCE.getJvmTiAddress();
		log.debug(8);
		final JvmTiFunction jvmTiFunction = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("GetPotentialCapabilities")), jvmTiPointer);
		final Memory capabilitiesPointer = new Memory(16);
		capabilitiesPointer.clear();
		JnaNativeUtils.INSTANCE.setDebugThisLibMode(0);
		log.debug(2);
		jvmTiFunction.invokeJvmTiFunction(new Object[] { capabilitiesPointer });
		log.debug(2);
		String allValues = "";
		for (int i = 0; i < 15; i++) {
			int int1 = capabilitiesPointer.getByte(i);
			if (int1 < 0) {
				int1 = int1 + 128;
			}
			// int1 = 256 - int1;
			BigInteger bigInteger = new BigInteger(int1 + "");
			// log.info("" + i + " = "+int1+" " + bigInteger.toString(2));
			allValues += bigInteger.toString(2);
		}
		log.info(allValues);
		log.info("can suspend = " + allValues.charAt(20));
		final JvmTiFunction jvmTiFunction2 = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("AddCapabilities")), jvmTiPointer);
		jvmTiFunction2.invokeJvmTiFunction(new Object[] { capabilitiesPointer });

		final Memory capabilitiesPointer2 = new Memory(1000);
		capabilitiesPointer2.clear();

		final JvmTiFunction jvmTiFunction3 = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("GetCapabilities")), jvmTiPointer);
		jvmTiFunction.invokeJvmTiFunction(new Object[] { capabilitiesPointer2 });
		log.debug(2);
		for (int i = 0; i < 60; i++) {
			int int1 = capabilitiesPointer2.getInt(i);
			// log.info(""+i+" = "+int1);
		}
	}

	public static <T> WeakHashMap<T, Object> findClassInstances2(final Class<T> class1, final int maxCount)
			throws Exception {
		Assert.assertNotNull(class1);
		log.setLevel(Level.ALL);
		log.debug(0);
		// final ObjectWrapper result3 = new ObjectWrapper(null);
		log.debug(6);
		final Pointer heapCallbakPointer = JnaNativeUtils.INSTANCE.getAllRefFuncRef(5);
		log.debug(7);
		final Pointer jvmTiPointer = JnaNativeUtils.INSTANCE.getSpecialJvmti();
		log.debug(8);
		final JvmTiFunction jvmTiFunction = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("IterateOverInstancesOfClass")), jvmTiPointer);
		Assert.assertEquals(jvmTiFunction.functionNumer, 112);
		final int heap_filter = 3;
		// Class klass = null;
		// ObjectWrapper.class;
		log.debug(0);
		// Object initial_object = null;
		final Pointer userData = new Memory(100);
		final int flag = 10;
		userData.setInt(0, 0);
		userData.setInt(4, 10);
		userData.setNativeLong(8, new NativeLong(flag));
		userData.setInt(12, 0);
		log.debug(1);
		JnaNativeUtils.INSTANCE.setDebugThisLibMode(0);
		log.debug(2);
		jvmTiFunction.invokeJvmTiFunction(new Object[] { class1, heap_filter, heapCallbakPointer, userData });
		log.debug(2);
		if (userData.getInt(12) != 0) {
			log.info("err = " + userData.getInt(12));
		}
		final int count = userData.getInt(0);
		log.info("ref count = " + count);
		final Pointer tags = new Pointer(JnaUtils2.getPointerAddress(userData) + 8);
		// IterateOverHeap(jvmtiEnv* env,
		// jvmtiHeapObjectFilter object_filter,
		// jvmtiHeapObjectCallback heap_object_callback,
		// const void* user_data)
		final WeakHashMap result = getAllObjectWithTag(jvmTiPointer, count, tags, maxCount);
		JvmTiMethods.disposeJvmTiEnv(jvmTiPointer);
		return result;
	}

	public static WeakHashMap getAllObjectWithTag(final Pointer jvmTiPointer, final int appritiateCOunt,
			final Pointer tags, final int maxCount) throws JnaException {
		final WeakHashMap res3 = new WeakHashMap(30);
		final Pointer objects = new Memory(Native.POINTER_SIZE * appritiateCOunt + Native.POINTER_SIZE);
		log.debug("8");
		final Pointer count2 = new Memory(10);
		count2.setInt(0, -1);
		// final Object[] result = new Object[count];
		// IdentityHashMap hashMap = new IdentityHashMap(count);

		final JvmTiFunction getObjectsWithTagsJvmTiFunction = new JvmTiFunction(
				Integer.parseInt(JvmTiFunction.functionMap.getProperty("GetObjectsWithTags")), jvmTiPointer) {

			@Override
			public void processResultBeforeExcJvmTi(final int flags, final Object[] params, final long resultPointer) {
				// Pointer[] objects2=objects.getPointerArray(0);
				{
					int count22 = count2.getInt(0);
					log.info("count2 =" + count22);
					count22 = Math.min(count22, maxCount);
					// Assert.assertEquals(count, count2.getInt(0));
					final Pointer objectss = objects.getPointer(0);
					for (int i = 0; i < count22; i++) {
						// if(i>100) {
						// return;
						// }
						final Pointer pointer = objectss.getPointer(i * Native.POINTER_SIZE);

						try {
							final Object object = JnaNativeMethods.convertPointerToObject(pointer);
							res3.put(object, objectSimple);
							// hashMap.put(object, );
							// log.info(object);
							// result[i] = object;
						} catch (final Exception e) {
							// TODO Auto-generated catch block
							log.warn(null, e);
						}
					}
				}
			}
		};
		final int tag_count = 1;
		// Pointer tags = new Pointer(JnaUtils2.getPointerAddress(userData) +
		// 8);
		log.debug(tags.getNativeLong(0));
		log.debug("9");
		getObjectsWithTagsJvmTiFunction.invokeJvmTiFunction(new Object[] { tag_count, tags, count2, objects, null });
		// log.info(res3);
		// objectToFInd.hashCode();
		return res3;
	}

	public static ClassStatus isClassInitilizedHuman(final Class class1) throws Exception {
		final int i = getClassInitilizedStatus(class1);
		final ClassStatus classStatus = new ClassStatus();
		// JVMTI_CLASS_STATUS_VERIFIED 1 Class bytecodes have been verified
		// JVMTI_CLASS_STATUS_PREPARED 2 Class preparation is complete
		// JVMTI_CLASS_STATUS_INITIALIZED 4 Class initialization is complete.
		// Static initializer has been run.
		// JVMTI_CLASS_STATUS_ERROR 8 Error during initialization makes class
		// unusable
		// JVMTI_CLASS_STATUS_ARRAY 16 Class is an array. If set, all other bits
		// are zero.
		// JVMTI_CLASS_STATUS_PRIMITIVE 32 Class is a primitive class (for
		// example, java.lang.Integer.TYPE). If set, all other bits are zero.
		classStatus.JVMTI_CLASS_STATUS_VERIFIED = isBitSet(1, i);
		classStatus.JVMTI_CLASS_STATUS_PREPARED = isBitSet(2, i);
		classStatus.JVMTI_CLASS_STATUS_INITIALIZED = isBitSet(4, i);
		classStatus.JVMTI_CLASS_STATUS_ERROR = isBitSet(8, i);
		classStatus.JVMTI_CLASS_STATUS_ARRAY = isBitSet(16, i);
		classStatus.JVMTI_CLASS_STATUS_PRIMITIVE = isBitSet(32, i);
		return classStatus;
	}

	public static boolean isBitSet(final int bitMask, final int value) {
		return (bitMask & value) == bitMask;
	}

	public static int getClassInitilizedStatus(final Class class1) throws Exception {
		final Memory sizePointer = new Memory(4);
		final JvmTiFunction jvmTiFunction = new JvmTiFunction("GetClassStatus");
		jvmTiFunction.invokeJvmTiFunction(new Object[] { class1, sizePointer });
		final int size = sizePointer.getInt(0);
		return size;
	}
}
