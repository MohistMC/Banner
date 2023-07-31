package com.mohistmc.banner.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Code from @<code>https://github.com/ArclightPowered/api/blob/master/src/main/java/io/izzel/arclight/api/EnumHelper.java</code>
 * modifyed to use for Banner
 */
public class DynamicEnumHelper {

    @SuppressWarnings("unchecked")
    public static <T> T addEnum(Class<T> cl, String name, List<Class<?>> ctorTypes, List<Object> ctorParams) {
        try {
            Unsafe.ensureClassInitialized(cl);
            Field field = getValuesField(cl);
            Object base = Unsafe.staticFieldBase(field);
            long offset = Unsafe.staticFieldOffset(field);
            T[] arr = (T[]) Unsafe.getObject(base, offset);
            T[] newArr = (T[]) Array.newInstance(cl, arr.length + 1);
            System.arraycopy(arr, 0, newArr, 0, arr.length);

            T newInstance = makeEnum(cl, name, arr.length, ctorTypes, ctorParams);

            newArr[arr.length] = newInstance;
            Unsafe.putObject(base, offset, newArr);
            reset(cl);
            return newInstance;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * JavaC stores the field differently than EJC
     */
    private static Field getValuesField(Class<?> cl) throws NoSuchFieldException, SecurityException {
        try {
            return cl.getDeclaredField("ENUM$VALUES");// EJC
        } catch (NoSuchFieldException | SecurityException e) {
            return cl.getDeclaredField("$VALUES");    // JavaC
        }
    }

    public static <T extends Enum<?>> T addEnum(Class<T> enumType, String enumName, Class<?>[] paramTypes, Object[] paramValues) {

        Field valuesField = null;
        Field[] fields = enumType.getDeclaredFields();
        Field[] var6 = fields;
        int var7 = fields.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            Field field = var6[var8];
            String name = field.getName();
            if (name.equals("$VALUES") || name.equals("ENUM$VALUES")) {
                valuesField = field;
                break;
            }
        }

        int flags = 4121;
        if (valuesField == null) {
            String valueType = String.format("[L%s;", enumType.getName().replace('.', '/'));
            Field[] var16 = fields;
            int var18 = fields.length;

            for(int var20 = 0; var20 < var18; ++var20) {
                Field field = var16[var20];
                if ((field.getModifiers() & flags) == flags && field.getType().getName().replace('.', '/').equals(valueType)) {
                    valuesField = field;
                    break;
                }
            }
        }

        if (valuesField == null) {
            return null;
        } else {
            valuesField.setAccessible(true);

            try {
                T[] previousValues = (T[]) valuesField.get(enumType);
                List<T> values = new ArrayList(Arrays.asList(previousValues));
                T newValue = makeEnum(enumType, enumName, values.size(), paramTypes, paramValues);
                values.add(newValue);
                setFailsafeFieldValue(valuesField, (Object)null, values.toArray((Enum[])Array.newInstance(enumType, 0)));
                cleanEnumCache(enumType);
                return newValue;
            } catch (Throwable var12) {
                var12.printStackTrace();
                throw new RuntimeException(var12.getMessage(), var12);
            }
        }
    }

    private static <T extends Enum<?>> T makeEnum(Class<T> enumClass, String value, int ordinal, Class<?>[] additionalTypes, Object[] additionalValues) throws Throwable {
        int additionalParamsCount = additionalValues == null ? 0 : additionalValues.length;
        Object[] params = new Object[additionalParamsCount + 2];
        params[0] = value;
        params[1] = ordinal;
        if (additionalValues != null) {
            System.arraycopy(additionalValues, 0, params, 2, additionalValues.length);
        }

        return (T) enumClass.cast(getConstructorAccessor(enumClass, additionalTypes).invokeWithArguments(params));
    }

    private static MethodHandle getConstructorAccessor(Class<?> enumClass, Class<?>[] additionalParameterTypes) throws Exception {
        Class<?>[] parameterTypes = new Class[additionalParameterTypes.length + 2];
        parameterTypes[0] = String.class;
        parameterTypes[1] = Integer.TYPE;
        System.arraycopy(additionalParameterTypes, 0, parameterTypes, 2, additionalParameterTypes.length);
        return Unsafe.lookup().findConstructor(enumClass, MethodType.methodType(Void.TYPE, parameterTypes));
    }

    @SuppressWarnings("unchecked")
    public static <T> void addEnums(Class<T> cl, List<T> list) {
        try {
            Field field = getValuesField(cl);
            Object base = Unsafe.staticFieldBase(field);
            long offset = Unsafe.staticFieldOffset(field);
            T[] arr = (T[]) Unsafe.getObject(base, offset);
            T[] newArr = (T[]) Array.newInstance(cl, arr.length + list.size());
            System.arraycopy(arr, 0, newArr, 0, arr.length);
            for (int i = 0; i < list.size(); i++) {
                newArr[arr.length + i] = list.get(i);
            }
            Unsafe.putObject(base, offset, newArr);
            reset(cl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T makeEnum(Class<T> cl, String name, int i, List<Class<?>> ctorTypes, List<Object> ctorParams) {
        try {
            Unsafe.ensureClassInitialized(cl);
            List<Class<?>> ctor = new ArrayList<>(ctorTypes.size() + 2);
            ctor.add(String.class);
            ctor.add(int.class);
            ctor.addAll(ctorTypes);
            MethodHandle constructor = Unsafe.lookup().findConstructor(cl, MethodType.methodType(void.class, ctor));
            List<Object> param = new ArrayList<>(ctorParams.size() + 2);
            param.add(name);
            param.add(i);
            param.addAll(ctorParams);
            return (T) constructor.invokeWithArguments(param);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final long[] ENUM_CACHE_OFFSETS;

    static {
        List<Long> offsets = new ArrayList<>();
        for (String s : new String[]{"enumConstantDirectory", "enumConstants", "enumVars"}) {
            try {
                Field field = Class.class.getDeclaredField(s);
                offsets.add(Unsafe.objectFieldOffset(field));
            } catch (NoSuchFieldException ignored) {
            }
        }
        if (offsets.isEmpty()) {
            throw new IllegalStateException("Unable to find offsets for Enum");
        }
        long[] arr = new long[offsets.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = offsets.get(i);
        }
        ENUM_CACHE_OFFSETS = arr;
    }

    private static void reset(Class<?> cl) {
        for (long offset : ENUM_CACHE_OFFSETS) {
            Unsafe.putObjectVolatile(cl, offset, null);
        }
    }

    public static void setStaticField(Field field, Object value) throws ReflectiveOperationException {
        try {
            Unsafe.lookup().ensureInitialized(field.getDeclaringClass());
            Unsafe.putObject(Unsafe.staticFieldBase(field), Unsafe.staticFieldOffset(field), value);
        } catch (Exception var3) {
            throw new ReflectiveOperationException(var3);
        }
    }

    public static <T> T getStaticField(Field field) throws ReflectiveOperationException {
        try {
            Unsafe.lookup().ensureInitialized(field.getDeclaringClass());
            return (T) Unsafe.getObject(Unsafe.staticFieldBase(field), Unsafe.staticFieldOffset(field));
        } catch (Exception var2) {
            throw new ReflectiveOperationException(var2);
        }
    }

    public static void setField(Object obj, Object value, Field field) throws ReflectiveOperationException {
        if (obj == null) {
            setStaticField(field, value);
        } else {
            try {
                Unsafe.putObject(obj, Unsafe.objectFieldOffset(field), value);
            } catch (Exception var4) {
                throw new ReflectiveOperationException(var4);
            }
        }

    }

    public static <T> T getField(Object obj, Field field) throws ReflectiveOperationException {
        if (obj == null) {
            return getStaticField(field);
        } else {
            try {
                return (T) Unsafe.getObject(obj, Unsafe.objectFieldOffset(field));
            } catch (Exception var3) {
                throw new ReflectiveOperationException(var3);
            }
        }
    }

    public static void setFailsafeFieldValue(Field field, Object target, Object value) throws Throwable {
        if (target != null) {
            Unsafe.lookup().findSetter(field.getDeclaringClass(), field.getName(), field.getType()).invoke(target, value);
        } else {
            Unsafe.lookup().findStaticSetter(field.getDeclaringClass(), field.getName(), field.getType()).invoke(value);
        }
    }

    private static void blankField(Class<?> enumClass, String fieldName) throws Throwable {
        Field[] var2 = Class.class.getDeclaredFields();
        int var3 = var2.length;

        for (Field field : var2) {
            if (field.getName().contains(fieldName)) {
                setFailsafeFieldValue(field, enumClass, (Object) null);
                break;
            }
        }

    }

    private static void cleanEnumCache(Class<?> enumClass) throws Throwable {
        blankField(enumClass, "enumConstantDirectory");
        blankField(enumClass, "enumConstants");
        blankField(enumClass, "enumVars");
    }
}
