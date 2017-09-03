package com.github.druidgreeneyes.rivet.core.extras;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.function.Function;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.druidgreeneyes.rivet.core.extras.Shingles;
import com.github.druidgreeneyes.rivet.core.labels.MapRIV;

public class ShinglesTests {

  static String text1 = "The quick brown fox jumps over the lazy dog.";
  static String text2 = "The quick brown fox jumps over the lazy god.";
  static String text3 = "There is nothing like a good joke. And that was nothing like a good joke.";
  static String text4 = "There is nothing like a good yoke. And that was nothing like a good joke.";

  public static <T> void assertError(final Function<T, ?> fun, final T arg) {
    try {
      fun.apply(arg);
      fail("Expected error, recieved none.");
    } catch (final Exception e) {}
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}

  @Test
  public final void testFindShinglePoints() {
    final int[] testPoints1 = { 0, 3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33,
        36, 39 };
    final int[] testPoints2 = { 0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22,
        24, 26, 28, 30 };
    final int[] points1 = Shingles.findShinglePoints(text1, 3, 6);
    final int[] points2 = Shingles.findShinglePoints(text2, 2, 15);
    assertArrayEquals(testPoints1, points1);
    assertArrayEquals(testPoints2, points2);
    assertError((final String x) -> Shingles.findShinglePoints(x, 2, 15),
                null);
    assertError((x) -> Shingles.findShinglePoints(x, 2, 15), "");
    assertError((x) -> Shingles.findShinglePoints(text1, x, 1), -1);
  }

  @Test
  public final void testRivettizeText() {
    final int width = 9;
    final int offset = 1;
    final int size = 16000;
    final int nnz = 48;
    final MapRIV riv1 = Shingles.rivettizeText(text1, width, offset, size,
                                               nnz);
    final MapRIV riv2 = Shingles.rivettizeText(text2, width, offset, size,
                                               nnz);
    final MapRIV riv3 = Shingles.rivettizeText(text3, width, offset, size,
                                               nnz);
    final MapRIV riv4 = Shingles.rivettizeText(text4, width, offset, size,
                                               nnz);
    final double sim12 = riv1.similarityTo(riv2);
    final double sim34 = riv3.similarityTo(riv4);
    final double sim14 = riv1.similarityTo(riv4);
    final double sim32 = riv3.similarityTo(riv2);
    assertTrue(sim12 > 0.1);
    assertTrue(sim34 > 0.1);
    assertFalse(sim14 > 0.1);
    assertFalse(sim32 > 0.1);
  }

}
