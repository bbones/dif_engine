package com.pva.diffengine;

import com.pva.diffengine.data.Address;
import com.pva.diffengine.data.Client;
import com.pva.diffengine.data.Contact;
import com.pva.diffengine.data.PersonalData;
import com.pva.diffengine.service.DiffEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.util.ClassUtils.isPrimitiveOrWrapper;

@SpringBootTest
class DemoApplicationTests {
    @Test
    void contextLoads() {

    }
}
