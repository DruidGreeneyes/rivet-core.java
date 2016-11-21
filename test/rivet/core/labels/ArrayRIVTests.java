package rivet.core.labels;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Test;

import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

public class ArrayRIVTests {

    public static <T> void assertThrows(final Class<?> exceptionClass,
            final Function<T, ?> fun, final T arg) {
        try {
            fun.apply(arg);
            fail("Expected error, recieved none.");
        } catch (final Exception e) {
            if (!e.getClass()
                  .equals(exceptionClass))
                fail(String.format("Expected Exception of type %s, recieved type %e",
                                   exceptionClass.getName(),
                                   e.getClass()
                                    .getName()));
        }
    }

    int[]           testKeys;
    double[]        testVals;
    VectorElement[] testPoints;
    int             testSize;
    int             testK;
    long            testSeed;
    String          testString;

    double e = Util.roundingError;

    @Before
    public void setUp() throws Exception {
        testK = 2;
        testSize = 10;
        testSeed = ArrayRIV.makeSeed("seed");
        testKeys = ArrayRIV.makeIndices(testSize, testK, testSeed);
        testVals = ArrayRIV.makeVals(testK, testSeed);
        testString = "0|1.000000 1|-1.000000 10";
        testPoints = new VectorElement[testK];
        for (int i = 0; i < testPoints.length; i += 2) {
            final int j = i + 1;
            testPoints[i] = new VectorElement(i, 1);
            testPoints[j] = new VectorElement(j, -1);
        }
    }

    @Test
    public final void testAdd() {
        final RIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final RIV testRIV = new ArrayRIV(testSize).add(testRIV4);
        final RIV testRIV8 = testRIV4.add(testRIV4);
        assertEquals("0|1.000000 1|-1.000000 10", testRIV4.toString());
        assertEquals("0|2.000000 1|-2.000000 10", testRIV8.toString());
        assertEquals(-2, testRIV8.get(1), e);
        assertEquals(testK, testRIV8.count());
        assertEquals(testRIV4, testRIV);
    }

    @Test
    public final void testArrayRIVArrayRIV() {
        final RIV testRIV1 =
                ArrayRIV.generateLabel(testSize, testK, "testRIV1");
        final RIV testRIV = new ArrayRIV(testRIV1);
        assertEquals(testRIV1, testRIV);
    }

    @Test
    public final void testArrayRIVInt() {
        final RIV testRIV = new ArrayRIV(15000);
        assertEquals(15000, testRIV.size());
        assertEquals(0, testRIV.count());
        assertEquals(0, testRIV.get(14500), e);
    }

    @Test
    public final void testArrayRIVIntArrayDoubleArrayInt() {
        final int[] sortedTestKeys = testKeys.clone();
        Arrays.sort(sortedTestKeys);
        final double[] sortedTestVals = testVals.clone();
        for (int i = 0; i < testK; i++) {
            final int index = ArrayUtils.indexOf(testKeys, sortedTestKeys[i]);
            sortedTestVals[i] = testVals[index];
        }
        final RIV testRIV3 = new ArrayRIV(testKeys, testVals, testSize);
        assertEquals(testSize, testRIV3.size());
        assertEquals(testK, testRIV3.count());
        assertArrayEquals(testRIV3.keyStream()
                                  .toArray(),
                          sortedTestKeys);
        final double[] vals = testRIV3.valStream()
                                      .toArray();
        for (int i = 0; i < testK; i++)
            assertEquals(vals[i], sortedTestVals[i], e);
    }

    @Test
    public final void testArrayRIVVectorElementArrayInt() {
        final RIV testRIV4 = new ArrayRIV(testPoints, testSize);
        assertEquals(testSize, testRIV4.size());
        assertEquals(testK, testRIV4.count());
        final VectorElement[] points = testRIV4.points();
        for (int i = 0; i < testK; i++)
            assertEquals(points[i], testPoints[i]);
    }

    @Test
    public final void testDestructiveAdd() {
        final RIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final RIV testRIV = new ArrayRIV(testSize).destructiveAdd(testRIV4);
        assertEquals(-1, testRIV4.get(1), e);
        assertEquals(-1, testRIV.get(1), e);
        assertEquals(testRIV4, testRIV);
    }

    @Test
    public final void testDivide() {
        final RIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final RIV testRIV = testRIV4.divide(2);
        assertEquals(-0.5, testRIV.get(1), e);
    }

    @Test
    public final void testEqualsRandomIndexVector() {
        final RIV testRIV1 =
                ArrayRIV.generateLabel(testSize, testK, "testRIV1");
        final RIV testRIV2 =
                ArrayRIV.generateLabel(testSize, testK, "testRIV2");
        assertEquals(testRIV1, testRIV1);
        assertNotEquals(testRIV1, testRIV2);
    }

    @Test
    public final void testFromString() {
        final RIV testRIV4 = new ArrayRIV(testPoints, testSize);
        assertEquals(testRIV4, ArrayRIV.fromString(testString));
    }

    @Test
    public final void testGenerateLabelIntIntCharSequence() {
        final RIV testRIV1 =
                ArrayRIV.generateLabel(testSize, testK, "testRIV1");
        final RIV testRIV2 =
                ArrayRIV.generateLabel(testSize, testK, "testRIV2");
        assertEquals(testSize, testRIV2.size());
        assertEquals(testSize, testRIV1.size());
        assertEquals(testK, testRIV2.count());
        assertEquals(testK, testRIV1.count());
    }

    @Test
    public final void testGenerateLabelIntIntCharSequenceIntInt() {
        final RIV testRIV1 =
                ArrayRIV.generateLabel(testSize, testK, "testRIV1", 0, 5);
        final RIV testRIV2 =
                ArrayRIV.generateLabel(testSize, testK, "testRIV2", 5, 10);
        assertEquals(testSize, testRIV2.size());
        assertEquals(testSize, testRIV1.size());
        assertEquals(testK, testRIV2.count());
        assertEquals(testK, testRIV1.count());
    }

    @Test
    public final void testGet() {
        final RIV testRIV4 = new ArrayRIV(testPoints, testSize);
        assertEquals(-1, testRIV4.get(1), e);
        assertEquals(0, testRIV4.get(9), e);
        assertThrows(IndexOutOfBoundsException.class, testRIV4::get, 1000000);
    }

    @Test
    public final void testMakeIndices() {
        assertEquals(testK, testKeys.length);
        assertEquals(testK, Arrays.stream(testKeys)
                                  .distinct()
                                  .count());
        assertEquals(testK, Arrays.stream(testKeys)
                                  .filter((x) -> 0 < x && x < testSize)
                                  .count());
        final int[] test2 =
                ArrayRIV.makeIndices(testSize,
                                     testK,
                                     ArrayRIV.makeSeed("not seed"));
        assertFalse(Arrays.stream(test2)
                          .allMatch((x) -> ArrayUtils.contains(testKeys, x)));
    }

    @Test
    public final void testMakeSeed() {
        final long seed2 = ArrayRIV.makeSeed("seed");
        final long seed3 = ArrayRIV.makeSeed("not seed");
        assertEquals(testSeed, seed2);
        assertNotEquals(testSeed, seed3);
    }

    @Test
    public final void testMakeValues() {
        assertEquals(testK, testVals.length);
        assertEquals(testK, Arrays.stream(testVals)
                                  .filter((x) -> x == 1 || x == -1)
                                  .count());
        assertEquals(0,
                     Arrays.stream(testVals)
                           .sum(),
                     e);
    }

    @Test
    public final void testMultiply() {
        final RIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final RIV testRIV8 = testRIV4.multiply(2);
        assertEquals(-2, testRIV8.get(1), e);
    }

    @Test
    public final void testNormalize() {
        final ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final RIV testRIVA = testRIV4.multiply(2);
        final ArrayRIV testRIVB = testRIV4.mapVals((v) -> v + 2);
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
        final RIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final Permutations p = Permutations.generate(testSize);
        final RIV testRIVA = testRIV4.permute(p, 1);
        final RIV testRIVB = testRIV4.permute(p, -1);
        assertFalse(testRIVA.equals(testRIV4));
        assertFalse(testRIVA.equals(testRIVB));
        assertFalse(testRIV4.equals(testRIVB));
        assertEquals(testK, testRIVB.count());
        assertEquals(testK, testRIVA.count());
        assertEquals(testRIVB.permute(p, 1), testRIV4);
        assertEquals(testRIVA.permute(p, -1), testRIV4);
    }

    @Test
    public final void testSubtract() {
        final RIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final RIV testRIV0 = testRIV4.subtract(testRIV4);
        assertEquals(0, testRIV0.get(9), e);
        assertEquals(0, testRIV0.count());
    }

    @Test
    public final void testToString() {
        final RIV testRIV4 = new ArrayRIV(testPoints, testSize);
        assertEquals(testString, testRIV4.toString());
    }

}
