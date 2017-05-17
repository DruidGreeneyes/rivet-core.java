package com.github.druidgreeneyes.rivet.core.labels;

import static com.github.druidgreeneyes.rivet.core.labels.RIVs.makeIndices;
import static com.github.druidgreeneyes.rivet.core.labels.RIVs.makeSeed;
import static com.github.druidgreeneyes.rivet.core.labels.RIVs.makeVals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.junit.Before;
import org.junit.Test;

import com.github.druidgreeneyes.rivet.core.labels.MapRIV;
import com.github.druidgreeneyes.rivet.core.labels.RIV;
import com.github.druidgreeneyes.rivet.core.labels.VectorElement;
import com.github.druidgreeneyes.rivet.core.util.Util;
import com.github.druidgreeneyes.rivet.core.vectorpermutations.Permutations;

public class MapRIVTests {

    public static void assertEqual(final RIV rivA, final RIV rivB) {
        assertEquals(rivA.size(), rivB.size());
        assertEquals(rivA.count(), rivB.count());
        final VectorElement[] pointsA = rivA.points();
        final VectorElement[] pointsB = rivB.points();
        for (int i = 0; i < pointsA.length; i++)
            assertEquals("RIV equality failure at points index " + i, pointsA[i], pointsB[i]);
        assertTrue(rivA.equals(rivB));
    }

    public static <T> void assertError(final Function<T, ?> fun, final T arg) {
        try {
            fun.apply(arg);
            fail("Expected error, recieved none.");
        } catch (final Exception e) {
        }
    }

    int[]                                     testKeys;
    double[]                                  testVals;
    ConcurrentHashMap<Integer, MutableDouble> testPointMap;
    int                                       testSize;
    int                                       testK;

    long testSeed;

    String testString =
            "0|1.000000 1|-1.000000 2|1.000000 3|-1.000000 4|1.000000 5|-1.000000 6|1.000000 7|-1.000000 8|1.000000 9|-1.000000 10|1.000000 11|-1.000000 12|1.000000 13|-1.000000 14|1.000000 15|-1.000000 16|1.000000 17|-1.000000 18|1.000000 19|-1.000000 20|1.000000 21|-1.000000 22|1.000000 23|-1.000000 24|1.000000 25|-1.000000 26|1.000000 27|-1.000000 28|1.000000 29|-1.000000 30|1.000000 31|-1.000000 32|1.000000 33|-1.000000 34|1.000000 35|-1.000000 36|1.000000 37|-1.000000 38|1.000000 39|-1.000000 40|1.000000 41|-1.000000 42|1.000000 43|-1.000000 44|1.000000 45|-1.000000 46|1.000000 47|-1.000000 16000";

    double e = Util.roundingError;

    @Before
    public void setUp() throws Exception {
        testK = 48;
        testSize = 16000;
        testSeed = makeSeed("seed");
        testKeys = makeIndices(testSize, testK, testSeed);
        testVals = makeVals(testK, testSeed);
        testPointMap = new ConcurrentHashMap<>();
        for (int i = 0; i < 48; i += 2) {
            final int j = i + 1;
            testPointMap.put(i, new MutableDouble(1.0));
            testPointMap.put(j, new MutableDouble(-1.0));
        }
    }

    @Test
    public final void testAdd() {
        final RIV testRIV4 = new MapRIV(testPointMap, testSize);
        final RIV testRIV = new MapRIV(testSize).add(testRIV4);
        final RIV testRIV8 = testRIV4.add(testRIV4);
        assertEquals(-2, testRIV8.get(9), e);
        assertEquals(testK, testRIV8.count());
        assertEqual(testRIV4, testRIV);
    }

    @Test
    public final void testDivide() {
        final MapRIV testRIV4 = new MapRIV(testPointMap, testSize);
        final RIV testRIV = testRIV4.divide(2);
        assertEquals(-0.5, testRIV.get(9), e);
    }

    @Test
    public final void testEqualsRandomIndexVector() {
        final MapRIV testRIV1 =
                MapRIV.generateLabel(testSize, testK, "testRIV1");
        final MapRIV testRIV2 =
                MapRIV.generateLabel(testSize, testK, "testRIV2");
        assertEqual(testRIV1, testRIV1);
        assertFalse(testRIV1.equals(testRIV2));
    }

    @Test
    public final void testFromString() {
        final MapRIV testRIV4 = new MapRIV(testPointMap, testSize);
        assertEqual(testRIV4, MapRIV.fromString(testString));
    }

    @Test
    public final void testGenerateLabelIntIntCharSequence() {
        final MapRIV testRIV1 =
                MapRIV.generateLabel(testSize, testK, "testRIV1");
        final MapRIV testRIV2 =
                MapRIV.generateLabel(testSize, testK, "testRIV2");
        assertEquals(testSize, testRIV2.size());
        assertEquals(testSize, testRIV1.size());
        assertEquals(testK, testRIV2.count());
        assertEquals(testK, testRIV1.count());
    }

    @Test
    public final void testGenerateLabelIntIntCharSequenceIntInt() {
        final MapRIV testRIV1 =
                MapRIV.generateLabel(testSize, testK, "testRIV1", 0, 5);
        final MapRIV testRIV2 =
                MapRIV.generateLabel(testSize, testK, "testRIV2", 5, 10);
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
                makeIndices(testSize,
                                   testK,
                                   makeSeed("not seed"));
        assertFalse(Arrays.stream(test2)
                          .allMatch((x) -> ArrayUtils.contains(testKeys, x)));
    }

    @Test
    public final void testMakeSeed() {
        final long seed2 = makeSeed("seed");
        final long seed3 = makeSeed("not seed");
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
    public final void testMapRIVHashMapOfIntegerDoubleInt() {
        final MapRIV testRIV = new MapRIV(testPointMap, testSize);
        assertEquals(testSize, testRIV.size());
        assertEquals(testK, testRIV.count());
        final VectorElement[] points = testRIV.points();
        assertEquals(testK, points.length);
        for (final VectorElement point : points)
            assertEquals(point.value(),
                         testPointMap.get(point.index())
                                     .getValue(),
                         e);
    }

    @Test
    public final void testMapRIVInt() {
        final MapRIV testRIV = new MapRIV(15000);
        assertTrue(testRIV.size() == 15000);
        assertTrue(testRIV.count() == 0);
        assertEquals(0, testRIV.get(14500), e);
    }

    @Test
    public final void testMapRIVIntArrayDoubleArrayInt() {
        final MapRIV testRIV = new MapRIV(testKeys, testVals, testSize);
        assertEquals(testSize, testRIV.size());
        assertEquals(testK, testRIV.count());
        for (int i = 0; i < testK; i++)
            assertEquals(testVals[i], testRIV.get(testKeys[i]), e);
    }

    @Test
    public final void testMapRIVMapRIV() {
        final MapRIV testRIV1 =
                MapRIV.generateLabel(testSize, testK, "testRIV1");
        final MapRIV testRIV2 = new MapRIV(testRIV1);
        assertEqual(testRIV1, testRIV2);
    }

    @Test
    public final void testMultiply() {
        final MapRIV testRIV4 = new MapRIV(testPointMap, testSize);
        final RIV testRIV8 = testRIV4.multiply(2);
        assertEquals(-2, testRIV8.get(9), e);
    }

    @Test
    public final void testNormalize() {
        final MapRIV testRIV4 = new MapRIV(testPointMap, testSize);
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
        final MapRIV testRIV4 = new MapRIV(testPointMap, testSize);
        final Permutations p = Permutations.generate(testSize);
        final MapRIV testRIVA = testRIV4.permute(p, 1);
        final MapRIV testRIVB = testRIV4.permute(p, -1);
        assertFalse(testRIVA.equals(testRIV4));
        assertFalse(testRIVA.equals(testRIVB));
        assertFalse(testRIV4.equals(testRIVB));
        assertEquals(testK, testRIVB.count());
        assertEquals(testK, testRIVA.count());
        assertEqual(testRIVB.permute(p, 1), testRIV4);
        assertEqual(testRIVA.permute(p, -1), testRIV4);
    }

    @Test
    public final void testSubtract() {
        final MapRIV testRIV4 = new MapRIV(testPointMap, testSize);
        final MapRIV testRIV0 = testRIV4.subtract(testRIV4);
        assertEquals(0, testRIV0.get(9), e);
        assertEquals(0, testRIV0.count());
    }

    @Test
    public final void testToString() {
        final MapRIV testRIV = new MapRIV(testPointMap, testSize);
        assertEquals(testString, testRIV.toString());
    }

}
