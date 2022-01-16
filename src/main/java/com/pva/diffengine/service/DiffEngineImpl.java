package com.pva.diffengine.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

@Service
@Qualifier("diffEngine")
public class DiffEngineImpl implements DiffEngine {
    private String[] keyFields;
    private String prefix;

    @AllArgsConstructor
    @ToString
    private static class ComparisonResult {
        @Getter private Object difference;
        @Getter private boolean changed;
    }

    @Override
    public void setKeyFields(String prefix, String[] keyFields) {
        this.prefix = prefix;
        this.keyFields = keyFields;
    }

    @Override
    public Object compare(Object original, Object edited) throws
            NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException, KeyFieldModified {

        return nodeCompare(original, edited, prefix).getDifference();
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
            // System.out.println(fieldName);
            String methodName = "get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
            Object v1 = clazz.getMethod(methodName).invoke(original);
            Object v2 = clazz.getMethod(methodName).invoke(edited);

            Method setter = clazz.getMethod(
                    "set" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1),
                    v2.getClass());

            if (isKeyField(parentPrefix + "." + fieldName)) {
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
                Object[] diff = compareArrays(v1, v2, paramClass, parentPrefix + "." + fieldName);
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

        Object[] a = (Object[])v1;
        Object[] b = (Object[])v2;

        ArrayList<ComparisonResult> al = new ArrayList<>();
        for (int i = 0; i < a.length; i++) {
            ComparisonResult cr = nodeCompare(a[i], b[i], parentPrefix);
            al.add(cr);
        }

        if (al.size() > 0) {
            Object[] os = (Object[])Array.newInstance(paramClass.getComponentType(), al.size());
            for (int i = 0; i < al.size(); i++) {
                os[i] = paramClass.getComponentType().cast(al.get(i).getDifference());
            }
            return os;
        }
        return (Object[])Array.newInstance(paramClass.getComponentType(), 0);
    }

    private boolean isComparable(Object obj) {
        return obj instanceof String || obj instanceof Integer || obj instanceof Short || obj instanceof Long
                || obj instanceof Byte || obj instanceof Character || obj instanceof Boolean
                || obj instanceof Float || obj instanceof Double || obj instanceof LocalDate;
    }

    private boolean isKeyField(String fieldName) {
        return Arrays.asList(keyFields).contains(fieldName);
    }
}
