package com.pva.diffengine.service;

import java.lang.reflect.InvocationTargetException;

public interface DiffEngine {
    class KeyFieldModified extends Exception { }
    void setKeyFields(String prefix, String[] keys);
    Object compare(Object original, Object edited)
            throws KeyFieldModified,
                InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException;
}
