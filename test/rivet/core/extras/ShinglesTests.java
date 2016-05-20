package rivet.core.extras;

import static org.junit.Assert.*;

import java.util.function.Function;

import org.junit.BeforeClass;
import org.junit.Test;

import rivet.core.labels.ArrayRIV;
import rivet.core.labels.RandomIndexVector;

public class ShinglesTests {
    
    static String text1 = "The quick brown fox jumps over the lazy dog.";
    static String text2 = "The quick brown fox jumps over the lazy god.";
    static String text3 = "There is nothing like a good joke. And that was nothing like a good joke.";
    static String text4 = "There is nothing like a good yoke. And that was nothing like a good joke.";
    
    public static <T> void assertError(final Function<T, ?> fun, final T arg) {
        try {
            fun.apply(arg);
            fail("Expected error, recieved none.");
        } catch(final Exception e) {}
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public final void testFindShinglePoints() {
        int[] testPoints1 = {0, 3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36, 39};
        int[] testPoints2 = {0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30};
        int[] points1 = Shingles.findShinglePoints(text1, 3, 6);
        int[] points2 = Shingles.findShinglePoints(text2, 2, 15);
        assertArrayEquals(testPoints1, points1);
        assertArrayEquals(testPoints2, points2);
        assertError((String x) -> Shingles.findShinglePoints(x, 2, 15), null);
        assertError((x) -> Shingles.findShinglePoints(x, 2, 15), "");
        assertError((x) -> Shingles.findShinglePoints(text1, x, 1), -1);
    }

    @Test
    public final void testRivettizeText() {
        int width = 9;
        int offset = 1;
        int size = 16000;
        int nnz = 48;
        ArrayRIV riv1 = Shingles.rivettizeText(text1, width, offset, size, nnz);
        ArrayRIV riv2 = Shingles.rivettizeText(text2, width, offset, size, nnz);
        ArrayRIV riv3 = Shingles.rivettizeText(text3, width, offset, size, nnz);
        ArrayRIV riv4 = Shingles.rivettizeText(text4, width, offset, size, nnz);
        double sim12 = RandomIndexVector.similarity(riv1, riv2);
        double sim34 = RandomIndexVector.similarity(riv3, riv4);
        double sim14 = RandomIndexVector.similarity(riv1, riv4);
        double sim32 = RandomIndexVector.similarity(riv3, riv2);
        assertTrue(sim12 > 0.1);
        assertTrue(sim34 > 0.1);
        assertFalse(sim14 > 0.1);
        assertFalse(sim32 > 0.1);
    }

}
