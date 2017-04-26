package rivet.core.labels;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.util.function.Function;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RIVTests {

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

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testAddRIVsRandomIndexVectorArray() {
        final ArrayRIV testRIVA = ArrayRIV.generateLabel(100, 4, "token1");
        final ArrayRIV testRIVB = ArrayRIV.generateLabel(100, 4, "token2");
        assertNotEquals(testRIVA, testRIVB);
        assertEquals(testRIVA.add(testRIVB), RIVs.addRIVs(testRIVA, testRIVB));
    }

    @Test
    public final void testDotProduct() {
        final ArrayRIV testRIVA = ArrayRIV.fromString("4|1.0 6|-1.0 10");
        final ArrayRIV testRIVB = ArrayRIV.fromString("3|1.0 7|-1.0 10");
        assertEquals(0, RIVs.dotProduct(testRIVA, testRIVB), 0.000001);
        assertEquals(2, RIVs.dotProduct(testRIVA, testRIVA), 0.000001);
    }

    @Test
    public final void testGetMatchingKeys() {
        final ArrayRIV testRIVA = ArrayRIV.fromString("4|1.0 6|-1.0 10");
        final ArrayRIV testRIVB = ArrayRIV.fromString("3|1.0 7|-1.0 10");
        final int[] referenceA = { 4, 6 };
        final int[] referenceB = {};
        final int[] testKeysA = RIVs.getMatchingKeys(testRIVA, testRIVA);
        final int[] testKeysB = RIVs.getMatchingKeys(testRIVA, testRIVB);
        assertArrayEquals(referenceA, testKeysA);
        assertArrayEquals(referenceB, testKeysB);
    }

    @Test
    public final void testGetMatchingVals() {
        final ArrayRIV testRIVA = ArrayRIV.fromString("4|1.0 6|-1.0 10");
        final ArrayRIV testRIVB = ArrayRIV.fromString("3|1.0 7|-1.0 10");
        final double[][] referenceA = { { 1.0, 1.0 }, { -1.0, -1.0 } };
        final double[][] referenceB = {};
        final double[][] testKeysA = RIVs.getMatchingVals(testRIVA, testRIVA);
        final double[][] testKeysB = RIVs.getMatchingVals(testRIVA, testRIVB);
        assertArrayEquals(referenceA, testKeysA);
        assertArrayEquals(referenceB, testKeysB);
    }

    @Test
    public final void testSimilarity() {
        final ArrayRIV testRIVA = ArrayRIV.fromString("4|1.0 6|-1.0 10");
        assertEquals(1, RIVs.similarity(testRIVA, testRIVA), 0.000001);
    }

}
