package com.pva.diffengine.service;

public interface KeyService {
    void setKeysData(String prefix, String delimiter, String[] keys);

    boolean isKeyField(String fieldName);
    String findKey(String parentPrefix);
    String getPrefix();
    String getDelimiter();
}
