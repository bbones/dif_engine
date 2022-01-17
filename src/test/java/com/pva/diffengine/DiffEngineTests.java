package com.pva.diffengine;

import com.pva.diffengine.data.Address;
import com.pva.diffengine.data.Client;
import com.pva.diffengine.data.Contact;
import com.pva.diffengine.data.PersonalData;
import com.pva.diffengine.service.DiffEngine;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.util.ClassUtils.isPrimitiveOrWrapper;

@SpringBootTest
@Slf4j
public class DiffEngineTests {

    private static DiffEngine diffEngine;

    private Client original;
    private Client edited;


    @Autowired
    public DiffEngineTests(DiffEngine de) {
        diffEngine = de;
    }

    @BeforeEach
    void setup() {
        diffEngine.setKeysData("$", ".", new String[] {
                "$.clientId",
                "$.personalData.taxCode",
                "$.personalData.addresses.recordId",
                "$.personalData.contacts.id"
        });


        original = new Client(10L, "VIP", 28,
                new PersonalData(1122334455L, "Test", "Testov",
                        LocalDate.parse("2000-01-01"),
                        new Address[] {
                                new Address(24L, "Kyiv", "Povitroflotskiy", 6),
                                new Address(25L, "Kyiv", "Dehtyarivska", 28)
                        },
                        new Contact[] {
                                new Contact(1010L, 1, "044-111-22-33"),
                                new Contact(1010L, 2, "063-111-22-33")

                        },
                        new String[] {"3", "5", "7"}
                )
        );

        edited = new Client(10L, "REGULAR", 28,
                new PersonalData(1122334455L, "Test", "Ivanov",
                        LocalDate.parse("1990-01-01"),
                        new Address[] {
                                new Address(24L, "Kyiv", "Povitroflotskiy", 6),
                                new Address(25L, "Kyiv", "Stecenka", 66),
                                new Address(26L, "Kyiv", "Peremohy", 20)
                        },
                        new Contact[] {
                                new Contact(1010L, 1, "044-111-22-33"),
                                new Contact(1010L, 2, "063-111-22-33")
                        },
                        new String[] {"3", "5", "9"}
                )
        );
    }

    @Test
    void compareSpecificationObjects()
            throws DiffEngine.KeyFieldModified, InvocationTargetException, InstantiationException,
            IllegalAccessException, NoSuchMethodException {
        Client difference = (Client) diffEngine.compare(original, edited);
        log.debug("difference" + difference.toString());
        assertEquals(difference.getClientId(), 10L);
        assertEquals(difference.getPersonalData().getAddresses().length, 2);
        assertEquals(difference.getPersonalData().getContacts().length, 0);
        assertEquals(difference.getPersonalData().getRandom().length, 3);
    }

    @Test
    void compareSameObjects() throws DiffEngine.KeyFieldModified, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Client difference = (Client) diffEngine.compare(original, original);
        log.debug("difference" + difference.toString());
        assertEquals(difference.getClientId(), 10L);
        assertEquals(difference.getPersonalData().getAddresses().length, 0);
        assertEquals(difference.getPersonalData().getContacts().length, 0);
        assertEquals(difference.getPersonalData().getRandom().length, 0);
    }

    @Test
    void compareWithExceptionKeyFieldModified() {

        edited.setClientId(11L);
        Exception exception = assertThrows(DiffEngine.KeyFieldModified.class, () -> {
            Object difference = diffEngine.compare(original, edited);
            log.debug("difference" + difference.toString());
        });

    }

    @Test
    void checkSetters() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Client client = new Client();
        Class<?> clazz = client.getClass();
        Method setter = clazz.getMethod("setClientId", Long.class);
        Class<?>[] paramClasses = setter.getParameterTypes();
        setter.invoke(client, paramClasses[0].cast(null));

        PersonalData pd = new PersonalData();
        Class<?> clpd = pd.getClass();
        Method arraySetter = clpd.getMethod("setAddresses", Address[].class);
        Class<?>[] paramClasses1 = arraySetter.getParameterTypes();
        log.debug(paramClasses1[0].getName());
    }

    @Test
    void arrayParameter() throws NoSuchMethodException {
        PersonalData pd = new PersonalData();

        Class<?> clpd = pd.getClass();

        Method arraySetter = clpd.getMethod("setAddresses", Address[].class);
        Class<?>[] paramClasses1 = arraySetter.getParameterTypes();
        log.debug(paramClasses1[0].getComponentType().getName());

    }

    @Test
    void fieldIsPrimitive() throws
            NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {

        Class<?> clazz = original.getClass();
        Field[] originalFields = clazz.getDeclaredFields();

        for (Field f : originalFields) {
            String fieldName = f.getName();
            String methodName = "get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
            Object v1 = clazz.getMethod(methodName).invoke(original);

            log.debug(fieldName + "->" + v1.getClass().isPrimitive() + "->" + isPrimitiveOrWrapper(v1.getClass()));
            log.debug("isPrimitive->" + clazz.getDeclaredField(fieldName).getType().isPrimitive());
        }
    }

    @Test
    void arrayOfPrimitives() throws NoSuchFieldException {
        Class<?> clazz = PersonalData.class;
        Class<?> fieldClass = clazz.getDeclaredField("random").getType().getComponentType();
        log.debug("Primitive or Wrapper" + (isPrimitiveOrWrapper(fieldClass) || fieldClass.equals(String.class)));

        Class<?> fieldClass1 = clazz.getDeclaredField("addresses").getType().getComponentType();
        log.debug(fieldClass1.getName());
        log.debug("Primitive, Wrapper or String->" +
                (isPrimitiveOrWrapper(fieldClass1) || fieldClass1.equals(String.class)));

    }

    @Test
    void contextLoads() {

    }
}
