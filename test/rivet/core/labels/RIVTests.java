package rivet.core.labels;

import static org.junit.Assert.*;
import static rivet.core.util.Util.roundingError;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import rivet.core.util.IntDoubleConsumer;

import static rivet.test.TestMethods.testMethods;

@RunWith(Parameterized.class)
public class RIVTests {
	
	private Class<?> rivClass;
	
	public RIVTests(Class<?> c) {
		rivClass = c;
	}
	
	@Parameters
	public static Collection<Class<?>> classesUnderTest () {
		/*
		 * NOTE: DenseRIV is not included here
		 * because it is guaranteed to fail any test the compares against default values,
		 * and I am too lazy to write exceptions into the tests.
		 */
		return Arrays.asList(new Class<?>[] {
			ArrayRIV.class,
			ColtRIV.class,
			HPPCRIV.class,
			ImmutableRIV.class,
			MapRIV.class
		});
	}
	
	public static final int DEFAULT_SIZE = 1600;
	public static final int DEFAULT_NNZ = 48;
	private static final String base = "0|1.000000 1|-1.000000 2|1.000000 3|-1.000000 4|1.000000 5|-1.000000 6|1.000000 7|-1.000000 8|1.000000 9|-1.000000 10|1.000000 11|-1.000000 12|1.000000 13|-1.000000 14|1.000000 15|-1.000000 16|1.000000 17|-1.000000 18|1.000000 19|-1.000000 20|1.000000 21|-1.000000 22|1.000000 23|-1.000000 24|1.000000 25|-1.000000 26|1.000000 27|-1.000000 28|1.000000 29|-1.000000 30|1.000000 31|-1.000000 32|1.000000 33|-1.000000 34|1.000000 35|-1.000000 36|1.000000 37|-1.000000 38|1.000000 39|-1.000000 40|1.000000 41|-1.000000 42|1.000000 43|-1.000000 44|1.000000 45|-1.000000 46|1.000000 47|-1.000000 ";
	public static final String DEFAULT_STRING = base + DEFAULT_SIZE;
	public static final String DEFAULT_DSTRING = base + generatePadding() + DEFAULT_SIZE;
	public static final int DEFAULT_TEST_INDEX = 19;
	public static final double DEFAULT_TEST_VALUE = -1;
	public static final String DEFAULT_TEST_WORD = "test-word";
	public static final VectorElement[] DEFAULT_POINTS = makeDefaultPoints();
	public static final int[] DEFAULT_INDICES = Arrays.stream(DEFAULT_POINTS).mapToInt(VectorElement::index).toArray();
	public static final double[] DEFAULT_VALS = Arrays.stream(DEFAULT_POINTS).mapToDouble(VectorElement::value).toArray();
	
	public static final int DEFAULT_HASH = -24;
	public static final int DEFAULT_HASH_2 = -48;
	
	public static final double DEFAULT_MAGNITUDE = Math.sqrt(Arrays.stream(DEFAULT_VALS).map(x -> x * x).sum());
	
	public static final double DEFAULT_SATURATION = 48 / 1600.0;
	
	//Utilities
	private static String generatePadding() {
		StringBuilder sb = new StringBuilder();
		for (int i = 48; i < DEFAULT_SIZE; i++)
			sb.append("" + i + "|0.000000 ");
		return sb.toString();
	}
	
	private static VectorElement[] makeDefaultPoints() {
		VectorElement[] points = new VectorElement[DEFAULT_NNZ];
		for (int i = 0; i < DEFAULT_NNZ; i+=2) {
			int j = i+1;
			points[i] = VectorElement.elt(i, 1);
			points[j] = VectorElement.elt(j, -1);
		}
		return points;
	}
	
	private static final Class<?>[] DEFAULT_CPARAMTYPES = new Class<?>[] {
		int[].class,
		double[].class,
		int.class
	};
	
	private RIV invokeDefaultConstructor() {
		Object res = null;
		Constructor<?> cst = null;
		try {
			cst = rivClass.getConstructor(DEFAULT_CPARAMTYPES);
		} catch (NoSuchMethodException  e) {
			fail("Class " + rivClass.getName() + " provides no constructor that accepts these args: " + int[].class.toString() + ", " + double[].class.toString() + ", " + int.class.toString());
		} catch (IllegalArgumentException | SecurityException e) {
			fail(e.getStackTrace().toString());
		}
		if (cst == null)
			fail("No constructor found!");
		try {
			res = cst.newInstance(DEFAULT_INDICES, DEFAULT_VALS, DEFAULT_SIZE);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			fail("Constructor found but not invoked!\n" + e.getStackTrace().toString());
		}
		return (RIV) res;
	}
	
	private RIV invokeEmptyConstructor() {
		Object res = null;
		Constructor<?> cst = null;
		try {
			cst = rivClass.getConstructor(int.class);
		} catch (NoSuchMethodException  e) {
			fail("Class " + rivClass.getName() + " provides no constructor that accepts these args: " + int.class.toString());
		} catch (IllegalArgumentException | SecurityException e) {
			fail(e.getStackTrace().toString());
		}
		if (cst == null)
			fail("No constructor found!");
		try {
			res = cst.newInstance(DEFAULT_SIZE);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			fail("Constructor found but not invoked!\n" + e.getStackTrace().toString());
		}
		return (RIV) res;
	}
	
	public static <T> void assertError(final Class<?> exceptionClass, final Function<T, ?> fun, final T arg) {
		boolean error = false;
		Class<?> gotClass = Exception.class;
        try {
            fun.apply(arg);
        } catch (final Exception e) {
        	error = true;
        	gotClass = e.getClass();
        }
        if (!error)
        	fail("Expected exception but got none.");
        else
        	if (!gotClass.equals(exceptionClass))
        		fail("Expected exception of type " + exceptionClass.getName() + ", but got " + gotClass.getName());
    }
	
	public static <T> void assertError(final Class<?> exceptionClass, final Supplier<?> fun) {
		boolean error = false;
		Class<?> gotClass = Exception.class;
        try {
            fun.get();
        } catch (final Exception e) {
        	error = true;
        	gotClass = e.getClass();
        }
        if (!error)
        	fail("Expected exception but got none.");
        else
        	if (!gotClass.equals(exceptionClass))
        		fail("Expected exception of type " + exceptionClass.getName() + ", but got " + gotClass.getName());
    }
	
	/**
	 * NOTE: No destructive methods are used here because:
	 *   A) as defined in the RIV interface, all safe
	 *     methods call this.copy().destructiveOp()
	 *   B) except in ImmutableRIV, where the safe methods
	 *     are overridden, and the destructive methods all
	 *     throw NotImplementedException.
	 */
	
	
	//Test Static Methods
	private RIV invokeEmpty() {
		RIV res = null;
		try {
			res = (RIV) rivClass.getDeclaredMethod("empty", int.class).invoke(null, DEFAULT_SIZE);
		} catch (NoSuchMethodException e) {
			fail(rivClass.getName() + ".empty() method not found!");
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			fail(e.getStackTrace().toString());
		}
		return res;
	}
	
	@Test
	public void testEmpty() {
		RIV riv = (RIV) invokeEmpty();
		testMethods(
				riv::size,
				riv::count).expect(
						DEFAULT_SIZE,
						0);
	}
	
	private RIV invokeFromString() {
		RIV res = null;
		try {
			res = (RIV) rivClass.getDeclaredMethod("fromString", String.class).invoke(null, DEFAULT_STRING);
		} catch (NoSuchMethodException e) {
			fail(rivClass.getName() + ".fromString() method not found!");
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		return res;
	}
	
	@Test
	public void testFromString() {
		RIV riv = invokeFromString();
		testMethods(
				riv::size,
				riv::count,
				() -> riv.get(DEFAULT_TEST_INDEX))
		.expect(DEFAULT_SIZE, DEFAULT_NNZ, DEFAULT_TEST_VALUE);
	}
	
	private RIV invokeGenerateLabelSizeNNZWord() {
		RIV res = null;
		try {
			res = (RIV) rivClass.getDeclaredMethod("generateLabel", int.class, int.class, CharSequence.class).invoke(null, DEFAULT_SIZE, DEFAULT_NNZ, DEFAULT_TEST_WORD);
		} catch (NoSuchMethodException e) {
			fail(rivClass.getName() + ".generateLabel() method not found!");
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			fail(e.getStackTrace().toString());
		}
		return res;
	}
	
	@Test
	public void testGenerateLabelSizeNNZWord() {
		RIV riv = (RIV) invokeGenerateLabelSizeNNZWord();
		testMethods(
				riv::size,
				riv::count).expect(
						DEFAULT_SIZE,
						DEFAULT_NNZ);
	}
	
	
	
	//Test Constructors
	@Test
	public void testConstructEmpty() {
		RIV riv = invokeEmptyConstructor();
		testMethods(riv::size, riv::count).expect(DEFAULT_SIZE, 0);
	}
	
	@Test
	public void testConstructKeyarrValarrSize() {
		RIV riv = invokeDefaultConstructor();
		testMethods(riv::size, riv::count, riv::keyArr, riv::valArr).expect(DEFAULT_SIZE, DEFAULT_NNZ, DEFAULT_INDICES, DEFAULT_VALS);
	}
	
	@Test
	public void testAddRiv() {
		double[] vals2 = Arrays.stream(DEFAULT_VALS).map(x -> x * 2).toArray();
		RIV riv = invokeDefaultConstructor();
		RIV riv2 = riv.add(riv);
		RIV riv0 = invokeEmptyConstructor().add(riv);
		testMethods(riv2::valArr, () -> riv0, riv0::keyArr, riv0::valArr).expect(vals2, riv, DEFAULT_INDICES, DEFAULT_VALS);
	}
	
	@Test
	public void testAddRivs() {
		double[] vals5 = Arrays.stream(DEFAULT_VALS).map(x -> x * 5).toArray();
		RIV riv = invokeDefaultConstructor();
		RIV riv5 = riv.add(riv, riv, riv, riv);
		testMethods(riv5::count, riv5::keyArr, riv5::valArr).expect(DEFAULT_NNZ, DEFAULT_INDICES, vals5);
		assertNotEquals(riv, riv5);
	}
	
	@Test
	public void testContains() {
		RIV riv = invokeDefaultConstructor();
		testMethods(() -> riv.contains(DEFAULT_TEST_INDEX)).expect(true);
	}
	
	@Test
	public void testCount() {
		RIV riv = invokeDefaultConstructor();
		RIV riv0 = invokeEmptyConstructor();
		testMethods(riv::size, riv0::size, riv::count, riv0::count).expect(DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_NNZ, 0);
	}
	
	@Test
	public void testDivide() {
		double[] valsHalf = Arrays.stream(DEFAULT_VALS).map(x -> x / 2.0).toArray();
		RIV riv = invokeDefaultConstructor();
		RIV rivHalf = riv.divide(2);
		assertNotEquals(riv, rivHalf);
		testMethods(rivHalf::valArr).expect(valsHalf);
	}
	
	@Test
	public void testEquals() {
		RIV rivA = invokeDefaultConstructor();
		RIV rivB = rivA.copy();
		RIV riv2 = rivA.add(rivA);
		Integer rivNot = 5;
		testMethods(
				() -> rivA.equals(rivB),
				() -> rivA.equals(riv2),
				() -> rivA.equals(rivNot)).expect(true, false, false);
	}
	
	@Test
	public void testForEach() {
		RIV riv = invokeDefaultConstructor();
		Stream.Builder<VectorElement> s = Stream.builder();
		riv.forEach((IntDoubleConsumer)(i, v) -> {
			s.accept(VectorElement.elt(i, v));
		});
		VectorElement[] points = s.build().sorted().toArray(VectorElement[]::new);
		assertArrayEquals(DEFAULT_POINTS, points);
	}
	
	@Test
	public void testGet() {
		RIV riv = invokeDefaultConstructor();
		assertEquals(DEFAULT_TEST_VALUE, riv.get(DEFAULT_TEST_INDEX), roundingError);
	}
	
	@Test
	public void testHashCode() {
		RIV riv = invokeDefaultConstructor();
		RIV riv2 = riv.add(riv);
		testMethods(riv::hashCode, riv2::hashCode).expect(DEFAULT_HASH, DEFAULT_HASH_2);
	}
	
	@Test
	public void testKeyArr() {
		RIV riv = invokeDefaultConstructor();
		testMethods(riv::keyArr).expect(DEFAULT_INDICES);
	}
	
	@Test
	public void testKeyStream() {
		RIV riv = invokeDefaultConstructor();
		testMethods(() -> riv.keyStream().sorted().toArray()).expect(DEFAULT_INDICES);
	}
	
	@Test
	public void testMagnitude() {
		RIV riv = invokeDefaultConstructor();
		testMethods(riv::magnitude).expect(DEFAULT_MAGNITUDE);
	}
	
	@Test
	public void testMultiply() {
		double[] vals5 = Arrays.stream(DEFAULT_VALS).map(x -> x * 5).toArray();
		RIV riv = invokeDefaultConstructor();
		RIV riv5 = riv.multiply(5);
		testMethods(riv5::keyArr, riv5::valArr).expect(riv.keyArr(), vals5);
	}
	
	@Test
	public void testNormalize() {
		RIV riv5 = invokeDefaultConstructor().multiply(5);
		RIV rivN = riv5.normalize();
		assertNotEquals(riv5, rivN);
		assertNotEquals(riv5.magnitude(), rivN.magnitude());
		assertEquals(1.0, rivN.magnitude(), roundingError);
	}
	
	@Test
	public void testPoints() {
		RIV riv = invokeDefaultConstructor();
		assertArrayEquals(DEFAULT_POINTS, riv.points());
	}
	
	@Test
	public void testRemoveZeros() {
		RIV riv = invokeDefaultConstructor();
		riv = riv.subtract(riv);
		RIV riv0 = invokeEmptyConstructor();
		assertEquals(riv0, riv);
	}
	
	@Test
	public void testSaturation() {
		RIV riv = invokeDefaultConstructor();
		RIV riv0 = invokeEmptyConstructor();
		testMethods(riv::saturation, riv0::saturation).expect(DEFAULT_SATURATION, 0.0);
	}
	
	@Test
	public void testSize() {
		RIV riv = invokeDefaultConstructor();
		RIV riv0 = invokeEmptyConstructor();
		RIV riv1 = riv.add(riv0, riv0, riv0, riv, riv0, riv.multiply(-1));
		testMethods(riv1::size, riv0::size).expect(DEFAULT_SIZE, DEFAULT_SIZE);
	}
	
	@Test
	public void testSubtractRIV() {
		RIV riv = invokeDefaultConstructor();
		RIV riv0 = invokeEmptyConstructor();
		testMethods(() -> riv.subtract(riv)).expect(riv0);
	}
	
	@Test
	public void testSubtractRIVs() {
		double[] valsNeg5 = Arrays.stream(DEFAULT_VALS).map(x -> x - (x * 6)).toArray();
		RIV riv = invokeDefaultConstructor();
		RIV rivNeg5 = riv.subtract(riv, riv, riv, riv, riv, riv);
		assertNotEquals(riv, rivNeg5);
		assertArrayEquals(valsNeg5,rivNeg5.valArr(), roundingError);
	}
	
	@Test
	public void testToDense() {
		RIV riv = invokeDefaultConstructor();
		DenseRIV denseRIV = riv.toDense();
		assertEquals(riv, denseRIV);
		assertNotEquals(riv.count(), denseRIV.count());
	}
	
	@Test
	public void testToImmutable() {
		RIV riv = invokeDefaultConstructor();
		ImmutableRIV iRiv = riv.toImmutable();
		assertEquals(riv, iRiv);
		assertError(NotImplementedException.class, iRiv::destructiveAdd, riv);
		assertError(NotImplementedException.class, iRiv::destructiveSub, riv);
		assertError(NotImplementedException.class, iRiv::destructiveDiv, 5.0);
		assertError(NotImplementedException.class, iRiv::destructiveMult, 5.0);
		assertError(NotImplementedException.class, iRiv::destructiveRemoveZeros);
	}
	
	@Test
	public void testValArr() {
		RIV riv = invokeDefaultConstructor();
		assertArrayEquals(DEFAULT_VALS, riv.valArr(), roundingError);
	}
	
	@Test
	public void testToString() {
		RIV riv = invokeDefaultConstructor();
		testMethods(riv::toString).expect(DEFAULT_STRING);
	}
}
