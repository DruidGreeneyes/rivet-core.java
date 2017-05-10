package rivet.test;

import static org.junit.Assert.*;
import static rivet.core.util.Util.roundingError;

import java.util.function.Supplier;

public final class TestMethods {
	private final Supplier<?>[] methods;
	private TestMethods(Supplier<?>...methods) {this.methods = methods;}
	public static final TestMethods testMethods(Supplier<?>...methods) {return new TestMethods(methods);}
	public final void expect(Object...expectedResults){
		for (int i = 0; i < methods.length; i++) {
			Object eRes = expectedResults[i];
			Supplier<?> method = methods[i];
			Class<?> clazz = expectedResults[i].getClass();
			if (clazz.equals(double.class))
				assertEquals((double)eRes, (double)method.get(), roundingError);
			else if (clazz.equals(int[].class))
				assertArrayEquals((int[])eRes, (int[])method.get());
			else if (clazz.equals(double[].class))
				assertArrayEquals((double[])eRes, (double[])method.get(), roundingError);
			else if (clazz.equals(boolean.class)) 
				if ((boolean)eRes)
					assertTrue((boolean)method.get());
				else
					assertFalse((boolean)method.get());
			else if (clazz.equals(String.class))
				assertEquals((String)expectedResults[i], (String)methods[i].get());
			else
				assertEquals(clazz.cast(expectedResults[i]), clazz.cast(methods[i].get()));
		}
	}
}
