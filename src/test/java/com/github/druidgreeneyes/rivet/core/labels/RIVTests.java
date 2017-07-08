package com.github.druidgreeneyes.rivet.core.labels;

import static com.github.druidgreeneyes.rivet.core.util.Util.roundingError;
import static org.junit.Assert.*;

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

import com.github.druidgreeneyes.rivet.core.labels.ArrayRIV;
import com.github.druidgreeneyes.rivet.core.labels.ColtRIV;
import com.github.druidgreeneyes.rivet.core.labels.DenseRIV;
import com.github.druidgreeneyes.rivet.core.labels.HPPCRIV;
import com.github.druidgreeneyes.rivet.core.labels.ImmutableRIV;
import com.github.druidgreeneyes.rivet.core.labels.MapRIV;
import com.github.druidgreeneyes.rivet.core.labels.RIV;
import com.github.druidgreeneyes.rivet.core.labels.VectorElement;
import com.github.druidgreeneyes.rivet.core.util.IntDoubleConsumer;

@RunWith(Parameterized.class)
public class RIVTests {

  public static final int DEFAULT_SIZE = 1600;

  public static final int DEFAULT_NNZ = 48;

  private static final String base =
                                   "0|1.000000 1|-1.000000 2|1.000000 3|-1.000000 4|1.000000 5|-1.000000 6|1.000000 7|-1.000000 8|1.000000 9|-1.000000 10|1.000000 11|-1.000000 12|1.000000 13|-1.000000 14|1.000000 15|-1.000000 16|1.000000 17|-1.000000 18|1.000000 19|-1.000000 20|1.000000 21|-1.000000 22|1.000000 23|-1.000000 24|1.000000 25|-1.000000 26|1.000000 27|-1.000000 28|1.000000 29|-1.000000 30|1.000000 31|-1.000000 32|1.000000 33|-1.000000 34|1.000000 35|-1.000000 36|1.000000 37|-1.000000 38|1.000000 39|-1.000000 40|1.000000 41|-1.000000 42|1.000000 43|-1.000000 44|1.000000 45|-1.000000 46|1.000000 47|-1.000000 ";

  public static final String DEFAULT_STRING = base + DEFAULT_SIZE;
  public static final String DEFAULT_DSTRING = base + generatePadding()
                                               + DEFAULT_SIZE;
  public static final int DEFAULT_TEST_INDEX = 19;
  public static final double DEFAULT_TEST_VALUE = -1;
  public static final String DEFAULT_TEST_WORD = "test-word";
  public static final VectorElement[] DEFAULT_POINTS = makeDefaultPoints();
  public static final int[] DEFAULT_INDICES = Arrays.stream(DEFAULT_POINTS)
                                                    .mapToInt(VectorElement::index)
                                                    .toArray();
  public static final double[] DEFAULT_VALS = Arrays.stream(DEFAULT_POINTS)
                                                    .mapToDouble(VectorElement::value)
                                                    .toArray();
  public static final int DEFAULT_HASH = -24;
  public static final int DEFAULT_HASH_2 = -48;
  public static final double DEFAULT_MAGNITUDE = Math.sqrt(Arrays.stream(DEFAULT_VALS)
                                                                 .map(x -> x
                                                                           * x)
                                                                 .sum());

  public static final double DEFAULT_SATURATION = 48 / 1600.0;
  private static final Class<?>[] DEFAULT_CPARAMTYPES = new Class<?>[] {
                                                                         int[].class,
                                                                         double[].class,
                                                                         int.class
  };

  private final Class<?> rivClass;

  public RIVTests(final Class<?> c) {
    rivClass = c;
  }

  private RIV invokeDefaultConstructor() {
    Object res = null;
    Constructor<?> cst = null;
    try {
      cst = rivClass.getConstructor(DEFAULT_CPARAMTYPES);
    } catch (final NoSuchMethodException e) {
      fail("Class " + rivClass.getName()
           + " provides no constructor that accepts these args: "
           + int[].class.toString()
           + ", "
           + double[].class.toString()
           + ", "
           + int.class.toString());
    } catch (IllegalArgumentException | SecurityException e) {
      fail(e.getStackTrace().toString());
    }
    if (cst == null)
                     fail("No constructor found!");
    try {
      res = cst.newInstance(DEFAULT_INDICES, DEFAULT_VALS, DEFAULT_SIZE);
    } catch (InstantiationException
             | IllegalAccessException
             | InvocationTargetException e) {
      fail("Constructor found but not invoked!\n"
           + e.getStackTrace().toString());
    }
    return (RIV) res;
  }

  /**
   * NOTE: No destructive methods are used here because: A) as defined in the RIV
   * interface, all safe methods call this.copy().destructiveOp() B) except in
   * ImmutableRIV, where the safe methods are overridden, and the destructive
   * methods all throw NotImplementedException.
   */

  // Test Static Methods
  private RIV invokeEmpty() {
    RIV res = null;
    try {
      res = (RIV) rivClass.getDeclaredMethod("empty", int.class)
                          .invoke(null, DEFAULT_SIZE);
    } catch (final NoSuchMethodException e) {
      fail(rivClass.getName() + ".empty() method not found!");
    } catch (IllegalAccessException
             | IllegalArgumentException
             | InvocationTargetException
             | SecurityException e) {
      fail(e.getStackTrace().toString());
    }
    return res;
  }

  private RIV invokeEmptyConstructor() {
    Object res = null;
    Constructor<?> cst = null;
    try {
      cst = rivClass.getConstructor(int.class);
    } catch (final NoSuchMethodException e) {
      fail("Class " + rivClass.getName()
           + " provides no constructor that accepts these args: "
           + int.class.toString());
    } catch (IllegalArgumentException | SecurityException e) {
      fail(e.getStackTrace().toString());
    }
    if (cst == null)
                     fail("No constructor found!");
    try {
      res = cst.newInstance(DEFAULT_SIZE);
    } catch (InstantiationException
             | IllegalAccessException
             | InvocationTargetException e) {
      fail("Constructor found but not invoked!\n"
           + e.getStackTrace().toString());
    }
    return (RIV) res;
  }

  private RIV invokeFromString() {
    RIV res = null;
    try {
      res = (RIV) rivClass.getDeclaredMethod("fromString", String.class)
                          .invoke(null, DEFAULT_STRING);
    } catch (final NoSuchMethodException e) {
      fail(rivClass.getName() + ".fromString() method not found!");
    } catch (IllegalAccessException
             | IllegalArgumentException
             | InvocationTargetException
             | SecurityException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return res;
  }

  private RIV invokeGenerateSizeNNZWord() {
    RIV res = null;
    try {
      res = (RIV) rivClass.getDeclaredMethod("generate",
                                             int.class,
                                             int.class,
                                             CharSequence.class)
                          .invoke(null,
                                  DEFAULT_SIZE,
                                  DEFAULT_NNZ,
                                  DEFAULT_TEST_WORD);
    } catch (final NoSuchMethodException e) {
      fail(rivClass.getName() + ".generateLabel() method not found!");
    } catch (IllegalAccessException
             | IllegalArgumentException
             | InvocationTargetException
             | SecurityException e) {
      fail(e.getStackTrace().toString());
    }
    return res;
  }

  @Test
  public void testAddRiv() {
    final double[] vals2 = Arrays.stream(DEFAULT_VALS)
                                 .map(x -> x * 2)
                                 .toArray();
    final RIV riv = invokeDefaultConstructor();
    final RIV riv2 = riv.add(riv);
    final RIV riv0 = invokeEmptyConstructor().add(riv);
    assertArrayEquals(vals2, riv2.valArr(), roundingError);
    assertTrue(riv.equals(riv0));
    assertArrayEquals(DEFAULT_INDICES, riv0.keyArr());
    assertArrayEquals(DEFAULT_VALS, riv0.valArr(), roundingError);
  }

  @Test
  public void testAddRivs() {
    final double[] vals5 = Arrays.stream(DEFAULT_VALS)
                                 .map(x -> x * 5)
                                 .toArray();
    final RIV riv = invokeDefaultConstructor();
    final RIV riv5 = riv.add(riv, riv, riv, riv);
    assertEquals(DEFAULT_NNZ, riv5.count());
    assertArrayEquals(DEFAULT_INDICES, riv5.keyArr());
    assertArrayEquals(vals5, riv5.valArr(), roundingError);
    assertFalse(riv.equals(riv5));
  }

  // Test Constructors
  @Test
  public void testConstructEmpty() {
    final RIV riv = invokeEmptyConstructor();
    assertEquals(DEFAULT_SIZE, riv.size());
    assertEquals(0, riv.count());
  }

  @Test
  public void testConstructKeyarrValarrSize() {
    final RIV riv = invokeDefaultConstructor();
    assertEquals(DEFAULT_SIZE, riv.size());
    assertEquals(DEFAULT_NNZ, riv.count());
    assertArrayEquals(DEFAULT_INDICES, riv.keyArr());
    assertArrayEquals(DEFAULT_VALS, riv.valArr(), roundingError);
  }

  @Test
  public void testContains() {
    final RIV riv = invokeDefaultConstructor();
    assertTrue(riv.contains(DEFAULT_TEST_INDEX));
  }

  @Test
  public void testCount() {
    final RIV riv = invokeDefaultConstructor();
    final RIV riv0 = invokeEmptyConstructor();
    assertEquals(DEFAULT_SIZE, riv.size());
    assertEquals(DEFAULT_SIZE, riv0.size());
    assertEquals(DEFAULT_NNZ, riv.count());
    assertEquals(0, riv0.count());
  }

  @Test
  public void testDivide() {
    final double[] valsHalf = Arrays.stream(DEFAULT_VALS)
                                    .map(x -> x / 2.0)
                                    .toArray();
    final RIV riv = invokeDefaultConstructor();
    final RIV rivHalf = riv.divide(2);
    assertNotEquals(riv, rivHalf);
    assertArrayEquals(valsHalf, rivHalf.valArr(), roundingError);
  }

  @Test
  public void testEmpty() {
    final RIV riv = invokeEmpty();
    assertEquals(DEFAULT_SIZE, riv.size());
    assertEquals(0, riv.count());
  }

  @Test
  public void testEquals() {
    final RIV rivA = invokeDefaultConstructor();
    final RIV rivB = rivA.copy();
    final RIV riv2 = rivA.add(rivA);
    final Integer notARIV = 5;
    assertTrue(rivA.equals(rivB));
    assertFalse(rivA.equals(riv2));
    assertFalse(rivA.equals(notARIV));
  }

  @Test
  public void testForEach() {
    final RIV riv = invokeDefaultConstructor();
    final Stream.Builder<VectorElement> s = Stream.builder();
    riv.forEachNZ((IntDoubleConsumer) (i, v) -> {
      s.accept(VectorElement.elt(i, v));
    });
    final VectorElement[] points = s.build()
                                    .sorted()
                                    .toArray(VectorElement[]::new);
    assertArrayEquals(DEFAULT_POINTS, points);
  }

  @Test
  public void testFromString() {
    final RIV riv = invokeFromString();
    assertEquals(DEFAULT_SIZE, riv.size());
    assertEquals(DEFAULT_NNZ, riv.count());
    assertEquals(DEFAULT_TEST_VALUE,
                 riv.get(DEFAULT_TEST_INDEX),
                 roundingError);
  }

  @Test
  public void testGenerateLabelSizeNNZWord() {
    final RIV riv = invokeGenerateSizeNNZWord();
    assertEquals(DEFAULT_SIZE, riv.size());
    assertEquals(DEFAULT_NNZ, riv.count());
  }

  @Test
  public void testGet() {
    final RIV riv = invokeDefaultConstructor();
    assertEquals(DEFAULT_TEST_VALUE,
                 riv.get(DEFAULT_TEST_INDEX),
                 roundingError);
  }

  @Test
  public void testHashCode() {
    final RIV riv = invokeDefaultConstructor();
    final RIV riv2 = riv.add(riv);
    assertEquals(DEFAULT_HASH, riv.hashCode());
    assertEquals(DEFAULT_HASH_2, riv2.hashCode());
  }

  @Test
  public void testKeyArr() {
    final RIV riv = invokeDefaultConstructor();
    assertArrayEquals(DEFAULT_INDICES, riv.keyArr());
  }

  @Test
  public void testKeyStream() {
    final RIV riv = invokeDefaultConstructor();
    assertArrayEquals(DEFAULT_INDICES, riv.keyStream().sorted().toArray());
  }

  @Test
  public void testMagnitude() {
    final RIV riv = invokeDefaultConstructor();
    assertEquals(DEFAULT_MAGNITUDE, riv.magnitude(), roundingError);
  }

  @Test
  public void testMultiply() {
    final double[] vals5 = Arrays.stream(DEFAULT_VALS)
                                 .map(x -> x * 5)
                                 .toArray();
    final RIV riv = invokeDefaultConstructor();
    final RIV riv5 = riv.multiply(5);
    assertArrayEquals(riv.keyArr(), riv5.keyArr());
    assertArrayEquals(vals5, riv5.valArr(), roundingError);
  }

  @Test
  public void testNormalize() {
    final RIV riv5 = invokeDefaultConstructor().multiply(5);
    final RIV rivN = riv5.normalize();
    assertNotEquals(riv5, rivN);
    assertNotEquals(riv5.magnitude(), rivN.magnitude());
    assertEquals(1.0, rivN.magnitude(), roundingError);
  }

  @Test
  public void testPoints() {
    final RIV riv = invokeDefaultConstructor();
    assertArrayEquals(DEFAULT_POINTS, riv.points());
  }

  @Test
  public void testRemoveZeros() {
    RIV riv = invokeDefaultConstructor();
    riv = riv.subtract(riv);
    final RIV riv0 = invokeEmptyConstructor();
    assertEquals(riv0, riv);
  }

  @Test
  public void testSaturation() {
    final RIV riv = invokeDefaultConstructor();
    final RIV riv0 = invokeEmptyConstructor();
    assertEquals(DEFAULT_SATURATION, riv.saturation(), roundingError);
    assertEquals(0.0, riv0.saturation(), roundingError);
  }

  @Test
  public void testSize() {
    final RIV riv = invokeDefaultConstructor();
    final RIV riv0 = invokeEmptyConstructor();
    final RIV riv1 = riv.add(riv0, riv0, riv0, riv, riv0, riv.multiply(-1));
    assertEquals(DEFAULT_SIZE, riv1.size());
    assertEquals(DEFAULT_SIZE, riv0.size());
  }

  @Test
  public void testSubtractRIV() {
    final RIV riv = invokeDefaultConstructor();
    final RIV riv0 = invokeEmptyConstructor();
    assertTrue(riv.subtract(riv).equals(riv0));
  }

  @Test
  public void testSubtractRIVs() {
    final double[] valsNeg5 = Arrays.stream(DEFAULT_VALS)
                                    .map(x -> x - x * 6)
                                    .toArray();
    final RIV riv = invokeDefaultConstructor();
    final RIV rivNeg5 = riv.subtract(riv, riv, riv, riv, riv, riv);
    assertNotEquals(riv, rivNeg5);
    assertArrayEquals(valsNeg5, rivNeg5.valArr(), roundingError);
  }

  @Test
  public void testToDense() {
    final RIV riv = invokeDefaultConstructor();
    final DenseRIV denseRIV = riv.toDense();
    assertEquals(riv, denseRIV);
    assertNotEquals(riv.count(), denseRIV.count());
  }

  @Test
  public void testToImmutable() {
    final RIV riv = invokeDefaultConstructor();
    final ImmutableRIV iRiv = riv.toImmutable();
    assertEquals(riv, iRiv);
    assertError(NotImplementedException.class, iRiv::destructiveAdd, riv);
    assertError(NotImplementedException.class, iRiv::destructiveSub, riv);
    assertError(NotImplementedException.class, iRiv::destructiveDiv, 5.0);
    assertError(NotImplementedException.class, iRiv::destructiveMult, 5.0);
    assertError(NotImplementedException.class, iRiv::destructiveRemoveZeros);
  }

  @Test
  public void testToString() {
    final RIV riv = invokeDefaultConstructor();
    assertEquals(DEFAULT_STRING, riv.toString());
  }

  @Test
  public void testValArr() {
    final RIV riv = invokeDefaultConstructor();
    assertArrayEquals(DEFAULT_VALS, riv.valArr(), roundingError);
  }

  public static <T> void assertError(final Class<?> exceptionClass,
                                     final Function<T, ?> fun,
                                     final T arg) {
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
    else if (!gotClass.equals(exceptionClass))
                                               fail("Expected exception of type " + exceptionClass.getName()
                                                    + ", but got "
                                                    + gotClass.getName());
  }

  public static <T> void assertError(final Class<?> exceptionClass,
                                     final Supplier<?> fun) {
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
    else if (!gotClass.equals(exceptionClass))
                                               fail("Expected exception of type " + exceptionClass.getName()
                                                    + ", but got "
                                                    + gotClass.getName());
  }

  @Parameters
  public static Collection<Class<?>> classesUnderTest() {
    /*
     * NOTE: DenseRIV is not included here because it is guaranteed to fail any test
     * the compares against default values, and I am too lazy to write exceptions
     * into the tests.
     */
    return Arrays.asList(new Class<?>[] {
                                          ArrayRIV.class,
                                          ColtRIV.class,
                                          HPPCRIV.class,
                                          ImmutableRIV.class,
                                          MapRIV.class,
                                          MTJRIV.class
    });
  }

  // Utilities
  private static String generatePadding() {
    final StringBuilder sb = new StringBuilder();
    for (int i = 48; i < DEFAULT_SIZE; i++)
      sb.append("" + i + "|0.000000 ");
    return sb.toString();
  }

  private static VectorElement[] makeDefaultPoints() {
    final VectorElement[] points = new VectorElement[DEFAULT_NNZ];
    for (int i = 0; i < DEFAULT_NNZ; i += 2) {
      final int j = i + 1;
      points[i] = VectorElement.elt(i, 1);
      points[j] = VectorElement.elt(j, -1);
    }
    return points;
  }
}
