package com.pva.diffengine.service;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;

@Service
@Qualifier("keyService")
@NoArgsConstructor
public class KeyServiceImpl implements KeyService{
    private String[] keyFields;
    private String prefix;
    private String delimiter;
    private final HashMap<String, String> hmKeys = new HashMap<> ();

    @Override
    public void setKeysData(String prefix, String delimiter, String[] keys) {
        this.prefix = prefix;
        this.delimiter = delimiter;
        this.keyFields = keys;
        parseKeys();
    }

    private void parseKeys() {
        for (String keyField: keyFields) {
            hmKeys.put(keyField.substring(0,keyField.lastIndexOf(delimiter)),
                    keyField.substring(keyField.lastIndexOf(delimiter)+1));
        }
    }

    @Override
    public boolean isKeyField(String fieldName) {
        return Arrays.asList(keyFields).contains(fieldName);
    }

    @Override
    public String findKey(String parentPrefix) {
        return hmKeys.get(parentPrefix);
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getDelimiter() {
        return delimiter;
    }
}
