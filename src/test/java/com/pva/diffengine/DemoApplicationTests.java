package com.pva.diffengine;

import com.pva.diffengine.data.Address;
import com.pva.diffengine.data.Client;
import com.pva.diffengine.data.Contact;
import com.pva.diffengine.data.PersonalData;
import com.pva.diffengine.service.DiffEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.springframework.util.ClassUtils.isPrimitiveOrWrapper;

@SpringBootTest
class DemoApplicationTests {

	private static DiffEngine diffEngine;

	private static Client original;
	private static Client edited;

	@Autowired
	public DemoApplicationTests(DiffEngine de) {
		diffEngine = de;
	}

	@BeforeAll
	static void init() {

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
		diffEngine.setKeysData("$", ".", new String[] {
				"$.clientId",
				"$.personalData.taxCode",
				"$.personalData.addresses.recordId",
				"$.personalData.contacts.id"
		});

		Object difference = diffEngine.compare(original, edited);
		System.out.println("Difference ==============================");
		System.out.println(difference);
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
		System.out.println(paramClasses1[0].getName());
	}

	@Test
	void arrayParameter() throws NoSuchMethodException {
		PersonalData pd = new PersonalData();

		Class<?> clpd = pd.getClass();

		Method arraySetter = clpd.getMethod("setAddresses", Address[].class);
		Class<?>[] paramClasses1 = arraySetter.getParameterTypes();
		System.out.println(paramClasses1[0].getComponentType().getName());

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

			System.out.println(fieldName + "->" + v1.getClass().isPrimitive() + "->" + isPrimitiveOrWrapper(v1.getClass()));
			System.out.println(clazz.getDeclaredField(fieldName).getType().isPrimitive());
		}
	}

	@Test
	void arrayOfPrimitives() throws NoSuchFieldException {
		Class<?> clazz = PersonalData.class;
		Class<?> fieldClass = clazz.getDeclaredField("random").getType().getComponentType();
		System.out.println(isPrimitiveOrWrapper(fieldClass) || fieldClass.equals(String.class));

		Class<?> fieldClass1 = clazz.getDeclaredField("addresses").getType().getComponentType();
		System.out.println(fieldClass1.getName());
		System.out.println(isPrimitiveOrWrapper(fieldClass1) || fieldClass1.equals(String.class));

	}

	@Test
	void contextLoads() {

	}
}
