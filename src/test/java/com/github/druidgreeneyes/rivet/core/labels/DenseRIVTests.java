package com.github.druidgreeneyes.rivet.core.labels;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

import com.github.druidgreeneyes.rivet.core.labels.DenseRIV;
import com.github.druidgreeneyes.rivet.core.labels.RIV;
import com.github.druidgreeneyes.rivet.core.labels.VectorElement;
import com.github.druidgreeneyes.rivet.core.util.Util;
import com.github.druidgreeneyes.rivet.core.vectorpermutations.Permutations;

public class DenseRIVTests {

  int[] testKeys;

  double[] testVals;

  VectorElement[] testPoints;

  int testSize;
  int testK;
  long testSeed;
  String testString;
  double e = Util.roundingError;

  @Before
  public void setUp() throws Exception {
    testK = 48;
    testSize = 16000;
    testSeed = RIVs.makeSeed("seed");
    testKeys = RIVs.makeIndices(testSize, testK, testSeed);
    testVals = RIVs.makeVals(testK, testSeed);
    testPoints = IntStream.range(0, testSize)
                          .mapToObj(VectorElement::fromIndex)
                          .toArray(VectorElement[]::new);
    for (int i = 0; i < testK; i += 2) {
      final int j = i + 1;
      testPoints[i].destructiveAdd(1.0);
      testPoints[j].destructiveSub(1.0);
    }

    testString = Arrays.stream(testPoints)
                       .map(VectorElement::toString)
                       .collect(Collectors.joining(" ",
                                                   "",
                                                   " " + testSize));
  }

  @Test
  public final void testAdd() {
    final RIV testRIV4 = new DenseRIV(testPoints, testSize);
    final RIV testRIV = DenseRIV.empty(testSize)
                                .add(testRIV4);
    final RIV testRIV8 = testRIV4.add(testRIV4);
    assertEquals(-2, testRIV8.get(9), e);
    assertEquals(testSize, testRIV8.count());
    assertEquals(testRIV4.size(), testRIV.size());
    assertRIVEquals(testRIV4, testRIV);
  }

  @Test
  public final void testDenseRIVDenseRIV() {
    final RIV testRIV1 =
                       DenseRIV.generate(testSize, testK, "testRIV1");
    final DenseRIV testRIV2 = new DenseRIV(testRIV1);
    assertRIVEquals(testRIV1, testRIV2);
  }

  @Test
  public final void testDenseRIVHashMapOfIntegerDoubleInt() {
    final DenseRIV testRIV = new DenseRIV(testPoints, testSize);
    assertEquals(testSize, testRIV.size());
    final VectorElement[] points = testRIV.points();
    assertArrayEquals(points, testPoints);
  }

  @Test
  public final void testDenseRIVInt() {
    final DenseRIV testRIV = DenseRIV.empty(15000);
    assertEquals(15000, testRIV.size());
    assertEquals(0, testRIV.get(14500), e);
  }

  @Test
  public final void testDenseRIVIntArrayDoubleArrayInt() {
    final DenseRIV testRIV = new DenseRIV(testKeys, testVals, testSize);
    assertEquals(testSize, testRIV.size());
    for (int i = 0; i < testK; i++)
      assertEquals(testVals[i], testRIV.get(testKeys[i]), e);
  }

  @Test
  public final void testDivide() {
    final DenseRIV testRIV4 = new DenseRIV(testPoints, testSize);
    final RIV testRIV = testRIV4.divide(2);
    assertEquals(-0.5, testRIV.get(9), e);
  }

  @Test
  public final void testEqualsRandomIndexVector() {
    final RIV testRIV1 =
                       DenseRIV.generate(testSize, testK, "testRIV1");
    final RIV testRIV2 =
                       DenseRIV.generate(testSize, testK, "testRIV2");
    assertRIVEquals(testRIV1, testRIV1);
    assertRIVNotEquals(testRIV1, testRIV2);
  }

  @Test
  public final void testFromString() {
    final DenseRIV testRIV4 = new DenseRIV(testPoints, testSize);
    assertRIVEquals(testRIV4, DenseRIV.fromString(testString));
  }

  @Test
  public final void testGenerateLabelIntIntCharSequence() {
    final RIV testRIV1 =
                       DenseRIV.generate(testSize, testK, "testRIV1");
    final RIV testRIV2 =
                       DenseRIV.generate(testSize, testK, "testRIV2");
    assertEquals(testSize, testRIV2.size());
    assertEquals(testSize, testRIV1.size());
  }

  @Test
  public final void testGenerateLabelIntIntCharSequenceIntInt() {
    final RIV testRIV1 =
                       DenseRIV.generate(testSize, testK, "testRIV1", 0, 5);
    final RIV testRIV2 =
                       DenseRIV.generate(testSize, testK, "testRIV2", 5, 10);
    assertEquals(testSize, testRIV2.size());
    assertEquals(testSize, testRIV1.size());
  }

  @Test
  public final void testMultiply() {
    final DenseRIV testRIV4 = new DenseRIV(testPoints, testSize);
    final RIV testRIV8 = testRIV4.multiply(2);
    assertEquals(-2, testRIV8.get(9), e);
  }

  @Test
  public final void testNormalize() {
    final DenseRIV testRIV4 = new DenseRIV(testPoints, testSize);
    final RIV testRIVA = testRIV4.multiply(2);
    final RIV testRIVB = testRIV4.multiply(5);
    assertFalse(testRIV4.equals(testRIVB));
    assertEquals(testRIV4.normalize()
                         .magnitude(),
                 testRIVA.normalize()
                         .magnitude(),
                 e);
    assertEquals(testRIV4.normalize()
                         .magnitude(),
                 testRIVB.normalize()
                         .magnitude(),
                 e);
    assertEquals(testRIVA.normalize()
                         .magnitude(),
                 testRIVB.normalize()
                         .magnitude(),
                 e);
  }

  @Test
  public final void testPermute() {
    final DenseRIV testRIV4 = new DenseRIV(testPoints, testSize);
    final Permutations p = Permutations.generate(testSize);
    final DenseRIV testRIVA = testRIV4.permute(p, 1);
    final DenseRIV testRIVB = testRIV4.permute(p, -1);
    assertRIVNotEquals(testRIV4, testRIVA);
    assertRIVNotEquals(testRIVA, testRIVB);
    assertRIVNotEquals(testRIV4, testRIVB);
    assertEquals(testSize, testRIVB.count());
    assertEquals(testSize, testRIVA.count());
    assertRIVEquals(testRIVB.permute(p, 1), testRIV4);
    assertRIVEquals(testRIVA.permute(p, -1), testRIV4);
  }

  @Test
  public final void testSubtract() {
    final DenseRIV testRIV4 = new DenseRIV(testPoints, testSize);
    final RIV testRIV0 = testRIV4.subtract(testRIV4);
    assertEquals(0, testRIV0.get(9), e);
    assertEquals(testSize, testRIV0.count());
  }

  @Test
  public final void testToString() {
    final DenseRIV testRIV = new DenseRIV(testPoints, testSize);
    assertEquals(testString, testRIV.toString());
  }

  public static <T> void assertError(final Function<T, ?> fun, final T arg) {
    try {
      fun.apply(arg);
      fail("Expected error, recieved none.");
    } catch (final Exception e) {
    }
  }

  public static void assertRIVEquals(final RIV rivA, final RIV rivB) {
    rivA.keyStream()
        .forEach(i -> {
          if (rivA.get(i) != rivB.get(i))
                                          fail(String.format("Inequality found at index %d; expected %f but got %f",
                                                             i,
                                                             rivA.get(i),
                                                             rivB.get(i)));
        });
  }

  public static void assertRIVNotEquals(final RIV rivA, final RIV rivB) {
    for (final int i : rivA.keyStream()
                           .toArray())
      if (rivA.get(i) != rivB.get(i))
                                      return;
    fail("No inequality found.");
  }

}
