package rivet.core.labels;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import rivet.core.labels.VectorElement;
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
    
    public static <T> void assertError(Function<T, ?> fun, T arg) {
        try {
            fun.apply(arg);
            fail("Expected error, recieved none.");
        } catch(Exception e) {}
    }
    
    public static void assertEqual(VectorElement vecA, VectorElement vecB) {
        assertEquals(vecA.index(), vecB.index());
        assertEquals(vecA.value(), vecB.value(), e);
    }
    
    public static void assertEqual(ArrayRIV rivA, ArrayRIV rivB) {
        assertEquals(rivA.size(), rivB.size());
        assertEquals(rivA.count(), rivB.count());
        VectorElement[] pointsA = rivA.stream().toArray(VectorElement[]::new);
        VectorElement[] pointsB = rivB.stream().toArray(VectorElement[]::new);
        for (int i = 0; i < pointsA.length; i++)
            assertEqual(pointsA[i], pointsB[i]);
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
            int j = i + 1;
            testPoints[i] = new VectorElement(i, 1);
            testPoints[j] = new VectorElement(j, -1);
        }
    }
    
    @Test
    public final void testMakeSeed() {
        long seed2 = ArrayRIV.makeSeed("seed");
        long seed3 = ArrayRIV.makeSeed("not seed");
        assertEquals(testSeed, seed2);
        assertNotEquals(testSeed, seed3);
    }
    
    @Test
    public final void testMakeIndices() {
        assertEquals(testK, testKeys.length);
        assertEquals(testK, Arrays.stream(testKeys).distinct().count());
        assertEquals(testK, Arrays.stream(testKeys).filter((x) -> 0 < x && x < testSize).count());
        int[] test2 = ArrayRIV.makeIndices(testSize, testK, ArrayRIV.makeSeed("not seed"));
        assertFalse(Arrays.stream(test2).allMatch((x) -> ArrayUtils.contains(testKeys, x)));
    }
    
    @Test
    public final void testMakeValues() {
        assertEquals(testK, testVals.length);
        assertEquals(testK, Arrays.stream(testVals).filter((x) -> x == 1 || x == -1).count());
        assertEquals(0, Arrays.stream(testVals).sum(), e);
    }
    
    @Test
    public final void testArrayRIVIntArrayIntArrayInt() {
        int[] sortedTestKeys = testKeys.clone();
        Arrays.sort(sortedTestKeys);
        double[] sortedTestVals = testVals.clone();
        for (int i = 0; i < testK; i++) {
            int index = ArrayUtils.indexOf(testKeys, sortedTestKeys[i]);
            sortedTestVals[i] = testVals[index];
        }
        ArrayRIV testRIV3 = new ArrayRIV(testKeys, testVals, testSize);
        assertEquals(testRIV3.size(), testSize);
        assertEquals(testK, testRIV3.count());
        assertArrayEquals(testRIV3.keys(), sortedTestKeys);
        double[] vals = testRIV3.vals();
        for (int i = 0; i < testK; i++)
            assertEquals(vals[i], sortedTestVals[i], e);
    }
    
    @Test
    public final void testGenerateLabelIntIntCharSequence() {
        ArrayRIV testRIV1 = ArrayRIV.generateLabel(testSize, testK, "testRIV1");
        ArrayRIV testRIV2 = ArrayRIV.generateLabel(testSize, testK, "testRIV2");
        assertEquals(testSize, testRIV2.size());
        assertEquals(testSize, testRIV1.size());
        assertEquals(testK, testRIV2.count());
        assertEquals(testK, testRIV1.count());
    }

    @Test
    public final void testEqualsRandomIndexVector() {
        ArrayRIV testRIV1 = ArrayRIV.generateLabel(testSize, testK, "testRIV1");
        ArrayRIV testRIV2 = ArrayRIV.generateLabel(testSize, testK, "testRIV2");
        assertEqual(testRIV1, testRIV1);
        assertFalse(testRIV1.equals(testRIV2));
    }

    @Test
    public final void testArrayRIVArrayRIV() {
        ArrayRIV testRIV1 = ArrayRIV.generateLabel(testSize, testK, "testRIV1");
        ArrayRIV testRIV = new ArrayRIV(testRIV1);
        assertEqual(testRIV1, testRIV);
    }

    @Test
    public final void testArrayRIVInt() {
        ArrayRIV testRIV = new ArrayRIV(15000);
        assertTrue(testRIV.size() == 15000);
        assertTrue(testRIV.count() == 0);
        assertEquals(0, testRIV.get(14500), e);
    }

    @Test
    public final void testArrayRIVVectorElementArrayInt() {
        ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        assertEquals(testSize, testRIV4.size());
        assertEquals(testK, testRIV4.count());
        VectorElement[] points = testRIV4.stream().toArray(VectorElement[]::new);
        for (int i = 0; i < testK; i++)
            assertEqual(points[i], testPoints[i]);
    }

    @Test
    public final void testToString() {
        ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        assertEquals(testString, testRIV4.toString());
    }

    @Test
    public final void testGet() {
        ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        assertEquals(-1, testRIV4.get(9), e);
        assertEquals(0, testRIV4.get(4053), e);
        assertError((x) -> testRIV4.get(x), 1000000);
    }

    @Test
    public final void testAdd() {
        ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        ArrayRIV testRIV = new ArrayRIV(testSize).add(testRIV4);
        ArrayRIV testRIV8 = testRIV4.add(testRIV4);
        assertEquals(-2, testRIV8.get(9), e);
        assertEquals(testK, testRIV8.count());
        assertEqual(testRIV4, testRIV);
    }

    @Test
    public final void testSubtract() {
        ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        ArrayRIV testRIV0 = testRIV4.subtract(testRIV4);
        assertEquals(0, testRIV0.get(9), e);
        assertEquals(0, testRIV0.count());
    }

    @Test
    public final void testMultiply() {
        ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        ArrayRIV testRIV8 = testRIV4.multiply(2);
        assertEquals(-2, testRIV8.get(9), e);
    }
    
    public final void testDivide() {
        ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        ArrayRIV testRIV = testRIV4.divide(2);
        assertEquals(-0.5, testRIV.get(9), e);
    }
    
    public final void testNormalize() {
        ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        ArrayRIV testRIVA = testRIV4.multiply(2);
        ArrayRIV testRIVB = testRIV4.mapVals((v) -> v + 2);
        assertEqual(testRIV4, testRIVA.normalize());
        assertFalse(testRIV4.equals(testRIVB));
        assertEquals(testRIV4.magnitude(), testRIVA.normalize().magnitude(), e);
        assertEquals(testRIV4.magnitude(), testRIVB.normalize().magnitude(), e);
    }

    @Test
    public final void testMap() {
        ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        ArrayRIV testRIV = testRIV4.map(ve -> ve.add(2));
        assertEquals(1, testRIV.get(9), e);
        assertEquals(testK, testRIV.count());
    }

    @Test
    public final void testMapKeys() {
        ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        ArrayRIV testRIV = testRIV4.mapKeys(k -> k * 2);
        assertEquals(0, testRIV.get(9), e);
        assertEquals(-1, testRIV.get(18), e);
        assertEquals(testK, testRIV.count());
    }

    @Test
    public final void testMapVals() {
        ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        ArrayRIV testRIV = testRIV4.mapVals(v -> v * 5);
        assertEquals(testRIV.get(9), -5, e);
    }

    @Test
    public final void testPermute() {
        ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        Permutations p = Permutations.generate(testSize);
        ArrayRIV testRIVA = testRIV4.permute(p, 1);
        ArrayRIV testRIVB = testRIV4.permute(p, -1);
        assertFalse(testRIVA.equals(testRIV4));
        assertFalse(testRIVA.equals(testRIVB));
        assertFalse(testRIV4.equals(testRIVB));
        assertEquals(testK, testRIVB.count());
        assertEquals(testK, testRIVA.count());
        assertEqual(testRIVB.permute(p, 1), testRIV4);
        assertEqual(testRIVA.permute(p, -1), testRIV4);
    }

    @Test
    public final void testFromString() {
        ArrayRIV testRIV4 = new ArrayRIV(testPoints, testSize);
        assertEqual(testRIV4, ArrayRIV.fromString(testString));
    }

}
