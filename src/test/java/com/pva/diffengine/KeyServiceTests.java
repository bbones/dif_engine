package com.pva.diffengine;

import com.pva.diffengine.service.KeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class KeyServiceTests {
    @Autowired
    private KeyService keyService;

    @BeforeEach
    void init() {
        keyService.setKeysData("$", ".", new String[] {
                "$.clientId",
                "$.personalData.taxCode",
                "$.personalData.addresses.recordId",
                "$.personalData.contacts.id"
        });
    }

    @Test
    void isKeyField() {
        assertTrue(keyService.isKeyField("$.clientId"));
    }

    @Test
    void findKey() {
        assertEquals(keyService.findKey("$.personalData.addresses"), "recordId");
    }
}
