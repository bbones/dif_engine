package com.pva.diffengine.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;

import static org.springframework.util.ClassUtils.isPrimitiveOrWrapper;

@Service
@Qualifier("diffEngine")
@Slf4j
public class DiffEngineImpl implements DiffEngine {

    @Autowired
    KeyService keyService;

    @AllArgsConstructor
    @ToString
    private static class ComparisonResult {
        @Getter private Object difference;
        @Getter private boolean changed;
    }

    @Override
    public void setKeysData(String prefix, String delimiter, String[] keys) {
        keyService.setKeysData(prefix, delimiter, keys);
    }

    @Override
    public Object compare(Object original, Object edited) throws
            NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException, KeyFieldModified {

        return nodeCompare(original, edited, keyService.getPrefix()).getDifference();
    }

    private ComparisonResult nodeCompare(Object original, Object edited, String parentPrefix) throws
            KeyFieldModified,
            InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {

        Class<?> clazz = original.getClass();
        Object result = clazz.getDeclaredConstructor().newInstance();
        Field[] originalFields = clazz.getDeclaredFields();
        boolean isChanged = false;

        for(Field f : originalFields) {
            String fieldName = f.getName();
            log.debug(fieldName);
            String methodName = "get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
            Object v1 = clazz.getMethod(methodName).invoke(original);
            Object v2 = clazz.getMethod(methodName).invoke(edited);

            Method setter = clazz.getMethod(
                    "set" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1),
                    v2.getClass());

            if (keyService.isKeyField(parentPrefix + keyService.getDelimiter() + fieldName)) {
                if (! v1.equals(v2)) {
                    throw new KeyFieldModified();
                }
                else {
                    setter.invoke(result, v1);
                }
            }
            if(isComparable(v1)) {
                if (! v1.equals(v2)) {
                    isChanged = true;
                    setter.invoke(result, v2);
                }
            } else if (v1 instanceof Object[]) {
                Class<?> paramClass = setter.getParameterTypes()[0];
                Object[] diff = compareArrays(v1, v2, paramClass,
                        parentPrefix + keyService.getDelimiter() + fieldName);
                if (diff != null) {
                    setter.invoke(result, paramClass.cast(diff));
                }

            } else {
                setter.invoke(result, nodeCompare(v1,v2, parentPrefix+"."+fieldName).getDifference());
            }
        }
        return new ComparisonResult(result, isChanged);
    }

    private Object[] compareArrays(Object v1, Object v2,
                                   Class<?> paramClass, String parentPrefix) throws
            KeyFieldModified,
            InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        if (isPrimitiveOrWrapper(paramClass.getComponentType()) || paramClass.getComponentType().equals(String.class)) {
            if (v1.equals(v2)) {
                return (Object[])Array.newInstance(paramClass.getComponentType(), 0);
            } else {
                return (Object[])v2;
            }
        }

        if (keyService.findKey(parentPrefix) != null) {
            return compareKeyedArrays(v1, v2, paramClass, parentPrefix);
        }

        Object[] a = (Object[])v1;
        Object[] b = (Object[])v2;

        ArrayList<ComparisonResult> al = new ArrayList<>();
        for (int i = 0; i < a.length; i++) {
            ComparisonResult cr = nodeCompare(a[i], b[i], parentPrefix);
            if (cr.isChanged()){
                al.add(cr);
            }
        }

        return arrayListToArray(paramClass, al);
    }

    private Object[] compareKeyedArrays(Object v1, Object v2, Class<?> paramClass, String parentPrefix)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
                    KeyFieldModified, InstantiationException {
        String keyField = keyService.findKey(parentPrefix);

        Map<Object, Object> hmOriginal = arrayToHashMap(paramClass, keyField, (Object[])v1);
        Map<Object, Object> hmEdited = arrayToHashMap(paramClass, keyField, (Object[])v2);

        ArrayList<ComparisonResult> al = new ArrayList<>();

        // Existing records
        for(Map.Entry<Object, Object> original: hmOriginal.entrySet()) {
            Object edited = hmEdited.get(original.getKey());
            if (edited != null) {
                ComparisonResult cr = nodeCompare(original.getValue(), edited, parentPrefix);
                if (cr.isChanged()) {
                    al.add(cr);
                }
            }
        }
         // New records
        hmEdited.keySet().removeAll(hmOriginal.keySet());
        for(Object newKey : hmEdited.keySet()) {
            al.add(new ComparisonResult(hmEdited.get(newKey), true));
        }
        return arrayListToArray(paramClass, al);
    }

    private Object[] arrayListToArray(Class<?> paramClass, ArrayList<ComparisonResult> al) {
        if (al.size() > 0) {
            Object[] os = (Object[]) Array.newInstance(paramClass.getComponentType(), al.size());
            for (int i = 0; i < al.size(); i++) {
                os[i] = paramClass.getComponentType().cast(al.get(i).getDifference());
            }
            return os;
        }
        return (Object[])Array.newInstance(paramClass.getComponentType(), 0);
    }

    private Map<Object, Object> arrayToHashMap(
            Class<?> paramClass, String keyField, Object[] array)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        HashMap<Object, Object> hm = new HashMap<>();
        String methodName = "get" + keyField.substring(0,1).toUpperCase() + keyField.substring(1);
        Method getter = paramClass.getComponentType().getMethod(methodName);
        for (Object el: array) {
            hm.put(getter.invoke(el), el);
        }
        return hm;
    }

    private boolean isComparable(Object obj) {
        return obj instanceof String || obj instanceof Integer || obj instanceof Short || obj instanceof Long
                || obj instanceof Byte || obj instanceof Character || obj instanceof Boolean
                || obj instanceof Float || obj instanceof Double || obj instanceof LocalDate;
    }

}
