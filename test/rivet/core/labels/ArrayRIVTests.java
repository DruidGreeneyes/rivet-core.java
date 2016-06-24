package rivet.core.labels;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

public class ArrayRIVTests {

    static int[] testKeys;
    static double[] testVals;
    static VectorElement[] testPoints;
    static int testSize;
    static int testK;
    static long testSeed;
    static String testString = "0|1.000000 1|-1.000000 2|1.000000 3|-1.000000 4|1.000000 5|-1.000000 6|1.000000 7|-1.000000 8|1.000000 9|-1.000000 10|1.000000 11|-1.000000 12|1.000000 13|-1.000000 14|1.000000 15|-1.000000 16|1.000000 17|-1.000000 18|1.000000 19|-1.000000 20|1.000000 21|-1.000000 22|1.000000 23|-1.000000 24|1.000000 25|-1.000000 26|1.000000 27|-1.000000 28|1.000000 29|-1.000000 30|1.000000 31|-1.000000 32|1.000000 33|-1.000000 34|1.000000 35|-1.000000 36|1.000000 37|-1.000000 38|1.000000 39|-1.000000 40|1.000000 41|-1.000000 42|1.000000 43|-1.000000 44|1.000000 45|-1.000000 46|1.000000 47|-1.000000 16000";
    static double e = Util.roundingError;

    public static <T> void assertThrows(final Class<?> exceptionClass,
            final Function<T, ?> fun, final T arg) {
        try {
            fun.apply(arg);
            fail("Expected error, recieved none.");
        } catch (final Exception e) {
            if (!e.getClass().equals(exceptionClass))
                fail(String.format(
                        "Expected Exception of type %s, recieved type %e",
                        exceptionClass.getName(), e.getClass().getName()));
        }
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        testK = 48;
        testSize = 16000;
        testSeed = ArrayRIV.makeSeed("seed");
        testKeys = ArrayRIV.makeIndices(testSize, testK, testSeed);
        testVals = ArrayRIV.makeVals(testK, testSeed);
        testPoints = new VectorElement[testK];
        for (int i = 0; i < 48; i += 2) {
            final int j = i + 1;
            testPoints[i] = new VectorElement(i, 1);
            testPoints[j] = new VectorElement(j, -1);
        }
    }

    @Test
    public final void testAdd() {
        final ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final ArrayRIV testRIV = new ArrayRIV(testSize).add(testRIV4);
        final ArrayRIV testRIV8 = testRIV4.add(testRIV4);
        assertEquals(-2, testRIV8.get(9), e);
        assertEquals(testK, testRIV8.count());
        assertEquals(testRIV4, testRIV);
    }

    @Test
    public final void testArrayRIVArrayRIV() {
        final ArrayRIV testRIV1 = ArrayRIV.generateLabel(testSize, testK,
                "testRIV1");
        final ArrayRIV testRIV = new ArrayRIV(testRIV1);
        assertEquals(testRIV1, testRIV);
    }

    @Test
    public final void testArrayRIVInt() {
        final ArrayRIV testRIV = new ArrayRIV(15000);
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
        final ArrayRIV testRIV3 = new ArrayRIV(testKeys, testVals, testSize);
        assertEquals(testSize, testRIV3.size());
        assertEquals(testK, testRIV3.count());
        assertArrayEquals(testRIV3.keys(), sortedTestKeys);
        final double[] vals = testRIV3.vals();
        for (int i = 0; i < testK; i++)
            assertEquals(vals[i], sortedTestVals[i], e);
    }

    @Test
    public final void testArrayRIVVectorElementArrayInt() {
        final ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        assertEquals(testSize, testRIV4.size());
        assertEquals(testK, testRIV4.count());
        final VectorElement[] points = testRIV4.points();
        for (int i = 0; i < testK; i++)
            assertEquals(points[i], testPoints[i]);
    }

    @Test
    public final void testDestructiveAdd() {
        final ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final ArrayRIV testRIV = new ArrayRIV(testSize)
                .destructiveAdd(testRIV4);
        assertEquals(-1, testRIV4.get(9), e);
        assertEquals(-1, testRIV.get(9), e);
        assertEquals(testRIV4, testRIV);
    }

    @Test
    public final void testDivide() {
        final ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final ArrayRIV testRIV = testRIV4.divide(2);
        assertEquals(-0.5, testRIV.get(9), e);
    }

    @Test
    public final void testEqualsRandomIndexVector() {
        final ArrayRIV testRIV1 = ArrayRIV.generateLabel(testSize, testK,
                "testRIV1");
        final ArrayRIV testRIV2 = ArrayRIV.generateLabel(testSize, testK,
                "testRIV2");
        assertEquals(testRIV1, testRIV1);
        assertNotEquals(testRIV1, testRIV2);
    }

    @Test
    public final void testFromString() {
        final ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        assertEquals(testRIV4, ArrayRIV.fromString(testString));
    }

    @Test
    public final void testGenerateLabelIntIntCharSequence() {
        final ArrayRIV testRIV1 = ArrayRIV.generateLabel(testSize, testK,
                "testRIV1");
        final ArrayRIV testRIV2 = ArrayRIV.generateLabel(testSize, testK,
                "testRIV2");
        assertEquals(testSize, testRIV2.size());
        assertEquals(testSize, testRIV1.size());
        assertEquals(testK, testRIV2.count());
        assertEquals(testK, testRIV1.count());
    }

    @Test
    public final void testGenerateLabelIntIntCharSequenceIntInt() {
        final ArrayRIV testRIV1 = ArrayRIV.generateLabel(testSize, testK,
                "testRIV1", 0, 5);
        final ArrayRIV testRIV2 = ArrayRIV.generateLabel(testSize, testK,
                "testRIV2", 5, 10);
        assertEquals(testSize, testRIV2.size());
        assertEquals(testSize, testRIV1.size());
        assertEquals(testK, testRIV2.count());
        assertEquals(testK, testRIV1.count());
    }

    @Test
    public final void testGet() {
        final ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        assertEquals(-1, testRIV4.get(9), e);
        assertEquals(0, testRIV4.get(4053), e);
        assertThrows(IndexOutOfBoundsException.class, testRIV4::get, 1000000);
    }

    @Test
    public final void testMakeIndices() {
        assertEquals(testK, testKeys.length);
        assertEquals(testK, Arrays.stream(testKeys).distinct().count());
        assertEquals(testK, Arrays.stream(testKeys)
                .filter((x) -> 0 < x && x < testSize).count());
        final int[] test2 = ArrayRIV.makeIndices(testSize, testK,
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
                .filter((x) -> x == 1 || x == -1).count());
        assertEquals(0, Arrays.stream(testVals).sum(), e);
    }

    @Test
    public final void testMap() {
        final ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final ArrayRIV testRIV = testRIV4.map(ve -> ve.add(2));
        assertEquals(1, testRIV.get(9), e);
        assertEquals(testK, testRIV.count());
    }

    @Test
    public final void testMapKeys() {
        final ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final ArrayRIV testRIV = testRIV4.mapKeys(k -> k * 2);
        assertEquals(0, testRIV.get(9), e);
        assertEquals(-1, testRIV.get(18), e);
        assertEquals(testK, testRIV.count());
    }

    @Test
    public final void testMapVals() {
        final ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final ArrayRIV testRIV = testRIV4.mapVals(v -> v * 5);
        assertEquals(testRIV.get(9), -5, e);
    }

    @Test
    public final void testMultiply() {
        final ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final ArrayRIV testRIV8 = testRIV4.multiply(2);
        assertEquals(-2, testRIV8.get(9), e);
    }

    @Test
    public final void testNormalize() {
        final ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final ArrayRIV testRIVA = testRIV4.multiply(2);
        final ArrayRIV testRIVB = testRIV4.mapVals((v) -> v + 2);
        assertFalse(testRIV4.equals(testRIVB));
        assertEquals(testRIV4.normalize().magnitude(),
                testRIVA.normalize().magnitude(), e);
        assertEquals(testRIV4.normalize().magnitude(),
                testRIVB.normalize().magnitude(), e);
        assertEquals(testRIVA.normalize().magnitude(),
                testRIVB.normalize().magnitude(), e);
    }

    @Test
    public final void testPermute() {
        final ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final Permutations p = Permutations.generate(testSize);
        final ArrayRIV testRIVA = testRIV4.permute(p, 1);
        final ArrayRIV testRIVB = testRIV4.permute(p, -1);
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
        final ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        final ArrayRIV testRIV0 = testRIV4.subtract(testRIV4);
        assertEquals(0, testRIV0.get(9), e);
        assertEquals(0, testRIV0.count());
    }

    @Test
    public final void testToString() {
        final ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        assertEquals(testString, testRIV4.toString());
    }

}
