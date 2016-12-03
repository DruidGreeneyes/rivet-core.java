package rivet.core.labels;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

public class ColtRIVTests {

    public static <T> void assertError(final Function<T, ?> fun, final T arg) {
        try {
            fun.apply(arg);
            fail("Expected error, recieved none.");
        } catch (final Exception e) {
        }
    }

    int[]           testKeys;
    double[]        testVals;
    VectorElement[] testPoints;
    int             testSize;
    int             testK;

    long testSeed;

    String testString =
            "0|1.000000 1|-1.000000 2|1.000000 3|-1.000000 4|1.000000 5|-1.000000 6|1.000000 7|-1.000000 8|1.000000 9|-1.000000 10|1.000000 11|-1.000000 12|1.000000 13|-1.000000 14|1.000000 15|-1.000000 16|1.000000 17|-1.000000 18|1.000000 19|-1.000000 20|1.000000 21|-1.000000 22|1.000000 23|-1.000000 24|1.000000 25|-1.000000 26|1.000000 27|-1.000000 28|1.000000 29|-1.000000 30|1.000000 31|-1.000000 32|1.000000 33|-1.000000 34|1.000000 35|-1.000000 36|1.000000 37|-1.000000 38|1.000000 39|-1.000000 40|1.000000 41|-1.000000 42|1.000000 43|-1.000000 44|1.000000 45|-1.000000 46|1.000000 47|-1.000000 16000";

    double e = Util.roundingError;

    @Before
    public void setUp() throws Exception {
        testK = 48;
        testSize = 16000;
        testSeed = ColtRIV.makeSeed("seed");
        testKeys = ColtRIV.makeIndices(testSize, testK, testSeed);
        testVals = ColtRIV.makeVals(testK, testSeed);
        testPoints = new VectorElement[48];
        for (int i = 0; i < 48; i += 2) {
            final int j = i + 1;
            testPoints[i] = VectorElement.elt(i, 1.0);
            testPoints[j] = VectorElement.elt(j, -1.0);
        }
    }

    @Test
    public final void testAdd() {
        final RIV testRIV4 = new ColtRIV(testPoints, testSize);
        final RIV testRIV = ColtRIV.empty(testSize)
                                   .add(testRIV4);
        final RIV testRIV8 = testRIV4.add(testRIV4);
        assertEquals(-2, testRIV8.get(9), e);
        assertEquals(testK, testRIV8.count());
        assertEquals(testRIV4, testRIV);
    }

    @Test
    public final void testDivide() {
        final ColtRIV testRIV4 = new ColtRIV(testPoints, testSize);
        final RIV testRIV = testRIV4.divide(2);
        assertEquals(-0.5, testRIV.get(9), e);
    }

    @Test
    public final void testEqualsRandomIndexVector() {
        final ColtRIV testRIV1 =
                ColtRIV.generateLabel(testSize, testK, "testRIV1");
        final ColtRIV testRIV2 =
                ColtRIV.generateLabel(testSize, testK, "testRIV2");
        assertEquals(testRIV1, testRIV1);
        assertFalse(testRIV1.equals(testRIV2));
    }

    @Test
    public final void testFromString() {
        final ColtRIV testRIV4 = new ColtRIV(testPoints, testSize);
        assertEquals(testRIV4, ColtRIV.fromString(testString));
    }

    @Test
    public final void testGenerateLabelIntIntCharSequence() {
        final ColtRIV testRIV1 =
                ColtRIV.generateLabel(testSize, testK, "testRIV1");
        final ColtRIV testRIV2 =
                ColtRIV.generateLabel(testSize, testK, "testRIV2");
        assertEquals(testSize, testRIV2.size());
        assertEquals(testSize, testRIV1.size());
        assertEquals(testK, testRIV2.count());
        assertEquals(testK, testRIV1.count());
    }

    @Test
    public final void testGenerateLabelIntIntCharSequenceIntInt() {
        final ColtRIV testRIV1 =
                ColtRIV.generateLabel(testSize, testK, "testRIV1", 0, 5);
        final ColtRIV testRIV2 =
                ColtRIV.generateLabel(testSize, testK, "testRIV2", 5, 10);
        assertEquals(testSize, testRIV2.size());
        assertEquals(testSize, testRIV1.size());
        assertEquals(testK, testRIV2.count());
        assertEquals(testK, testRIV1.count());
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
                ColtRIV.makeIndices(testSize,
                                    testK,
                                    ColtRIV.makeSeed("not seed"));
        assertFalse(Arrays.stream(test2)
                          .allMatch(i -> ArrayUtils.contains(testKeys, i)));
    }

    @Test
    public final void testMakeSeed() {
        final long seed2 = ColtRIV.makeSeed("seed");
        final long seed3 = ColtRIV.makeSeed("not seed");
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
    public final void testColtRIVHashMapOfIntegerDoubleInt() {
        final ColtRIV testRIV = new ColtRIV(testPoints, testSize);
        assertEquals(testSize, testRIV.size());
        assertEquals(testK, testRIV.count());
        final VectorElement[] points = testRIV.points();
        assertEquals(testK, points.length);
        assertArrayEquals(points, testPoints);
    }

    @Test
    public final void testColtRIVInt() {
        final ColtRIV testRIV = ColtRIV.empty(15000);
        assertTrue(testRIV.size() == 15000);
        assertTrue(testRIV.count() == 0);
        assertEquals(0, testRIV.get(14500), e);
    }

    @Test
    public final void testColtRIVIntArrayDoubleArrayInt() {
        final ColtRIV testRIV = new ColtRIV(testKeys, testVals, testSize);
        assertEquals(testSize, testRIV.size());
        assertEquals(testK, testRIV.count());
        for (int i = 0; i < testK; i++)
            assertEquals(testVals[i], testRIV.get(testKeys[i]), e);
    }

    @Test
    public final void testColtRIVColtRIV() {
        final ColtRIV testRIV1 =
                ColtRIV.generateLabel(testSize, testK, "testRIV1");
        final ColtRIV testRIV2 = new ColtRIV(testRIV1);
        assertEquals(testRIV1, testRIV2);
    }

    @Test
    public final void testMultiply() {
        final ColtRIV testRIV4 = new ColtRIV(testPoints, testSize);
        final RIV testRIV8 = testRIV4.multiply(2);
        assertEquals(-2, testRIV8.get(9), e);
    }

    @Test
    public final void testNormalize() {
        final ColtRIV testRIV4 = new ColtRIV(testPoints, testSize);
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
        final ColtRIV testRIV4 = new ColtRIV(testPoints, testSize);
        final Permutations p = Permutations.generate(testSize);
        final ColtRIV testRIVA = testRIV4.permute(p, 1);
        final ColtRIV testRIVB = testRIV4.permute(p, -1);
        assertNotEquals(testRIV4, testRIVA);
        assertNotEquals(testRIVA, testRIVB);
        assertNotEquals(testRIV4, testRIVB);
        assertEquals(testK, testRIVB.count());
        assertEquals(testK, testRIVA.count());
        assertEquals(testRIV4, testRIVA.permute(p, -1));
        assertEquals(testRIV4, testRIVB.permute(p, 1));
    }

    @Test
    public final void testSubtract() {
        final ColtRIV testRIV4 = new ColtRIV(testPoints, testSize);
        final RIV testRIV0 = testRIV4.subtract(testRIV4);
        assertEquals(0, testRIV0.get(9), e);
        assertEquals(0, testRIV0.count());
    }

    @Test
    public final void testToString() {
        final ColtRIV testRIV = new ColtRIV(testPoints, testSize);
        assertEquals(testString, testRIV.toString());
    }

}
