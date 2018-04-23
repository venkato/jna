package org.jna.jvmtiutils2;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;

import junit.framework.Assert;

// TODO add default values
public class ClassUtils {
	private static final Logger log = Logger.getLogger(ClassUtils.class.getName());

    public static final Pattern annonymousPattern = Pattern
            .compile(".*\\$\\d+");

    // public static final Pattern cglibClassPattern = Pattern
    // .compile(".*.([^.]+)\\$\\$EnhancerByCGLIB\\$\\$\\.+");

    private final static Pattern nestedClassNamePattern = Pattern
            .compile(".*(\\.[A-Z].*)(\\.[A-Z].*)");

    public static final HashMap<Class, String> primitiveClassesToString = new HashMap();

    public static final HashMap<String, Class> primitiveClassesFromString = new HashMap();

    public static final List<Class> primitiveArraysClasses = Collections
            .unmodifiableList(Arrays.asList(new Class[] { int[].class,
                    long[].class, boolean[].class, char[].class, short[].class,
                    byte[].class, float[].class, double[].class }));

    public static final List<Class> primitiveClasses = Collections
            .unmodifiableList(Arrays.asList(new Class[] { int.class,
                    long.class, boolean.class, char.class, short.class,
                    byte.class, float.class, double.class, void.class }));

    public static final List<Class> primitiveWrapperClasses = Collections
            .unmodifiableList(Arrays.asList(new Class[] { Integer.class,
                    Long.class, Boolean.class, Character.class, Short.class,
                    Byte.class, Float.class, Double.class, Void.class, }));

    public static final Comparator<Class> classComparator = new Comparator<Class>() {

        public int compare(final Class o1, final Class o2) {
            return o1.getName().compareTo(o2.getName());
        }

    };

    public static final List<String> primitiveClassesNames;

    public static final Map<Class, Class> primitiveToWrapperMap;

    public static final Map<Class, Class> wrapperToPrimitiveMap;
    static {
        final List<String> primitiveClassHumanNameList1 = new Vector<String>();

        final Map<Class, Class> primitiveToWrapper1 = new HashMap<Class, Class>();
        final Map<Class, Class> wrapperToPrimitive1 = new HashMap<Class, Class>();
        int i = 0;
        for (final Class clazz : primitiveClasses) {
            primitiveClassHumanNameList1.add(clazz.getName());
            primitiveToWrapper1.put(clazz, primitiveWrapperClasses.get(i));
            wrapperToPrimitive1.put(primitiveWrapperClasses.get(i), clazz);

            if (i == primitiveClasses.size() - 2) {
                break;
            }
            i++;
        }
        primitiveClassesNames = Collections
                .unmodifiableList(primitiveClassHumanNameList1);

        primitiveToWrapperMap = Collections
                .unmodifiableMap(primitiveToWrapper1);
        wrapperToPrimitiveMap = Collections
                .unmodifiableMap(wrapperToPrimitive1);

        for (final Class class1 : primitiveArraysClasses) {
            final Class nativeClass = class1.getComponentType();
            Assert.assertTrue(nativeClass.isPrimitive());
            final String s = class1.getName();
            final String s2 = s.substring(1);
            primitiveClassesToString.put(nativeClass, s2);
            primitiveClassesFromString.put(s2, nativeClass);
        }
        primitiveClassesFromString.put("V", void.class);
        primitiveClassesToString.put(void.class, "V");
    }

    public static String getClassSignature(final Class class1) {
        if (class1.isPrimitive()) {
            return primitiveClassesToString.get(class1);
        }
        return class1.getName();
    }

    public static String getMethodSignature(final Method method) {
        StringBuffer res = new StringBuffer("(");
        for (final Class class1 : method.getParameterTypes()) {
            res = res.append(getClassSignature(class1));
        }
        res.append(")");
        res.append(getClassSignature(method.getReturnType()));
        return res.toString();
    }

    /**
     * @return human readable string representation of clazz. Examples: int,
     *         java.lang.Integer[][].
     */
    public static String classToString(final Class clazz) {
        if (clazz.isArray()) {
            return classToString(clazz.getComponentType()) + "[]";
        }
        return clazz.getName();
    }

    /**
     * Convert human readable string representation className to java class.
     * className can be like int or java.lang.Integer[][]. Add support for
     * ClassLoader.loadClass(..) method loading all primitive classes with human
     * readable name.
     */
    // TODO add classLoader param
    public static Class loadClass(final String className)
            throws ClassNotFoundException {
        return loadClass(className, Thread.currentThread()
                .getContextClassLoader());
    }

    public static Class loadClass(final String className,
            final ClassLoader classLoader) throws ClassNotFoundException {
        if (className.endsWith("[]")) {
            return Array.newInstance(
                    loadClass(className.substring(0, className.length() - 2),
                            classLoader), 0).getClass();
        }
        // hack to work on some jmx servers
        if ("[B;".equals(className)) {
            return byte[].class;
        }
        final int i = primitiveClassesNames.indexOf(className);
        if (i == -1) {
            return Class.forName(className, false, classLoader);
        }
        return primitiveClasses.get(i);

    }

    /**
     * @return true if clazz is number class or primitive number class, else
     *         false.
     */
    public static boolean isNumberClass(final Class clazz) {
        if (clazz.isPrimitive()) {
            return isNumberClass(primitiveToWrapperMap.get(clazz));
        }
        return Number.class.isAssignableFrom(clazz);

    }

    public static final String cglibPartClassName = "$$EnhancerByCGLIB";

    private static final String cglibPartClassName2 = cglibPartClassName + "$$";

    public static String getSimpleName2(final Class clazz) {
        String simpleClassName;
        if (Proxy.class.isAssignableFrom(clazz)) {
            final Class[] interfaces = clazz.getInterfaces();
            if (interfaces.length == 0) {
                log.severe(clazz.getName());
                simpleClassName = "Proxy";
            } else {
                simpleClassName = "Proxy: " + getSimpleName1(interfaces[0]);
            }
        } else {
            simpleClassName = ClassUtils.getSimpleName1(clazz);
            final int i = simpleClassName.indexOf(cglibPartClassName2);
            if (i != -1) {
                return simpleClassName.substring(0, i - 1) + cglibPartClassName;
            }
        }
        if (simpleClassName.length() == 0) {
            final Class superClass = clazz.getSuperclass();
            if (superClass == Object.class) {
                final Class[] interfaces = clazz.getInterfaces();
                if (interfaces.length == 1) {
                    simpleClassName = null;
                    for (final Class class1 : interfaces) {
                        if (simpleClassName == null) {
                            simpleClassName = ClassUtils.getSimpleName1(class1);
                        } else {
                            simpleClassName += ", "
                                    + ClassUtils.getSimpleName1(class1);
                        }
                    }
                } else {
                    if (interfaces.length == 0) {
                        return "Object";
                    }
                    log.severe(clazz.getName());
                }
            } else {
                simpleClassName = getSimpleName1(superClass);
            }
        }
        return simpleClassName;
    }

    public static String getSimpleName1(final Class clazz) {
        throw new Error();
    }

    public static boolean isClassAndParentsPublic(final Class clazz) {
        if (clazz.isInterface()) {
            return true;
        }
        Class superClass = clazz;
        while (superClass != Object.class) {
            if (!Modifier.isPublic(superClass.getModifiers())) {
                return false;
            }
            superClass = superClass.getSuperclass();
        }
        return true;
    }

    public static HashSet<Class> getAllSuperClassesAndInterfaces(
            final Class clazz) {
        final HashSet<Class> result = new HashSet();
        Class superClass = clazz;
        while (superClass != null) {
            result.addAll(getInterfaces(superClass));
            superClass = superClass.getSuperclass();
            result.add(superClass);
        }
        result.remove(null);
        result.remove(Object.class);
        return result;
    }

    private static Set<Class> getInterfaces(final Class interface1) {
        final HashSet<Class> result = new HashSet();
        final Class[] intf = interface1.getInterfaces();
        for (final Class class1 : intf) {
            result.add(class1);
            result.addAll(getInterfaces(class1));
        }
        return result;
    }

    // public static String getSimpleName(Class clazz) {
    //        
    //        
    // if (Proxy.class.isAssignableFrom(clazz)) {
    // final Class[] interfaces = clazz.getInterfaces();
    // if (interfaces.length == 0) {
    // log.warn(clazz.getName());
    // return "Proxy";
    // } else {
    // String simpleClassName;
    // for (Class class1 : interfaces) {
    // if(simpleClassName==null) {
    // simpleClassName=getSimpleName(class1);
    // }else {
    // simpleClassName+=", "+getSimpleName(class1);
    // }
    // }
    // return simpleClassName;
    // // = "Proxy: " + interfaces[0].getSimpleName();
    // }
    // } else {
    // simpleClassName = clazz.getSimpleName();
    // }
    // if (simpleClassName.length() == 0) {
    // final Class superClass = clazz.getSuperclass();
    // if (superClass == Object.class) {
    // final Class[] interfaces = clazz.getInterfaces();
    // if (interfaces.length == 1) {
    // simpleClassName = interfaces[0].getSimpleName();
    // } else {
    // log.warn(clazz.getName());
    // }
    // } else {
    // simpleClassName = superClass.getSimpleName();
    // }
    // }
    //        
    // String tos = classToString(clazz);
    // int dollarSign = tos.indexOf('$');
    // if(dollarSign==tos.length()) {
    // log.warn(tos);
    // Class superCLass=clazz.getSuperclass();
    // if(superCLass!=Object.class) {
    // return getSimpleName(superCLass);
    // }
    // String simpleClassName;
    // for (Class class1 : clazz.getInterfaces()) {
    // if(simpleClassName==null) {
    // simpleClassName=getSimpleName(class1);
    // }else {
    // simpleClassName+=", "+getSimpleName(class1);
    // }
    // }
    // return simpleClassName;
    // }
    // if (dollarSign > 0) {
    // String tail = tos.substring(dollarSign + 1);
    // }
    // }

    public static void checkSubClass(final Class subclass,
            final Class superClass) {
        if (!superClass.isAssignableFrom(subclass)) {
            throw buildClassCastException(subclass, superClass);
        }
    }

    public static ClassCastException buildClassCastException(
            final Class subclass, final Class superClass)
            throws ClassCastException {
        return new ClassCastException(subclass.getName()
                + " cannot be cast to " + superClass.getName());
    }

    public static boolean isAnonymous(final Class class1) {
        return annonymousPattern.matcher(class1.getName()).matches();
    }

    // public static boolean isCglibClass(final Class class1) {
    // return class1.getName().contains("$$EnhancerByCGLIB$$");
    // }

    public static Collection<Class> findCommonSuperClasses(
            final Collection<Class> classes) {
        if (classes.contains(Object.class)) {
            final Set<Class> result = new HashSet();
            // result.add(Object.class);
            return result;
        }
        Collection<Class> result = null;
        for (final Class class1 : classes) {
            final Set<Class> superClasses = getAllSuperClassesAndInterfaces(class1);
            if (result == null) {
                result = superClasses;
            } else {
                result = CollectionUtils.intersection(result, superClasses);
            }
        }
        // result.a
        return result;
    }

    public static Class findCommonSuperClass(final Class class1,
            final Class class2) {
        if (class1 == class2) {
            return class1;
        }
        if (class1.isArray() || class2.isArray()) {
            return null;
        }
        if (class1 == Object.class || class2 == Object.class) {
            return Object.class;
        }
        final Set<Class> superClassesForClass2 = ClassUtils
                .getAllSuperClassesAndInterfaces(class2);
        superClassesForClass2.add(class2);
        return findCommonSuperClass(class1, superClassesForClass2);
    }

    public static Class findCommonSuperClass(final Class class1,
            final Set<Class> superClassesForClass2) {
        // while (superClass!=null&&superClass!=Object.class) {
        if (superClassesForClass2.contains(class1)) {
            return class1;
        }
        final Class superClass = class1.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            final Class sc = findCommonSuperClass(superClass,
                    superClassesForClass2);
            if (sc != null) {
                return sc;
            }
        }
        final Class[] intf = superClass.getInterfaces();
        for (final Class class8 : intf) {
            final Class class5 = findCommonSuperClass(class8,
                    superClassesForClass2);
            if (class5 != null) {
                return class5;
            }

        }
        return null;
    }

}
