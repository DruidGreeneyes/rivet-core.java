package com.github.druidgreeneyes.rivet.core.labels;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;
import com.github.druidgreeneyes.rivet.core.util.Util;
import com.github.druidgreeneyes.rivet.core.vectorpermutations.Permutations;

public class RIVs {
  private RIVs() {}

  @FunctionalInterface
  public interface IntBiFunction<T> {
    T apply(int a, int b);
  }

  public static RIV generateRIV(final int size,
                                final int k,
                                final CharSequence text,
                                final int point,
                                final int width,
                                final RIVConstructor rivConstructor) {
    return generateRIV(size,
                       k,
                       text.subSequence(Integer.max(0, point),
                                        Integer.min(text.length(),
                                                    point + width)),
                       rivConstructor);
  }

  public static RIV generateRIV(final int size,
                                final int nnz,
                                final CharSequence token,
                                final RIVConstructor rivConstructor) {
    final long seed = makeSeed(token);
    final int[] indices = makeIndices(size, nnz, seed);
    final double[] vals = makeVals(nnz, seed);
    return rivConstructor.make(indices, vals, size);
  }

  /**
   * @param size
   * @param count
   * @param seed
   * @return an array of count random integers between 0 and size
   */
  protected static int[] makeIndices(final int size,
                                     final int count,
                                     final long seed) {
    return Util.randInts(size, count, seed)
               .toArray();
  }

  public static Function<CharSequence, RIV> generator(final int size,
                                                      final int nnz,
                                                      final RIVConstructor rivConstructor) {
    return (token) -> generateRIV(size, nnz, token, rivConstructor);
  }

  public static IntBiFunction<RIV>
         generator(final int size, final int nnz, final CharSequence text,
                   final RIVConstructor rivConstructor) {
    return (start, width) -> generateRIV(size, nnz, text, start, width,
                                         rivConstructor);
  }

  /**
   * @param word
   * @return a probably-unique long, used to seed java's Random.
   */
  protected static long makeSeed(final CharSequence word) {
    final AtomicInteger c = new AtomicInteger();
    return word.chars()
               .mapToLong(ch -> ch
                                * (long) Math.pow(10, c.incrementAndGet()))
               .sum();
  }

  /**
   * @param count
   * @param seed
   * @return an array of count/2 1s and count/2 -1s, in random order.
   */
  protected static double[] makeVals(final int count, final long seed) {
    final double[] l = new double[count];
    for (int i = 0; i < count; i += 2) {
      l[i] = 1;
      l[i + 1] = -1;
    }
    return Util.shuffleDoubleArray(l, seed);
  }

  protected static int[] permuteKeys(final int[] keys, final int[] permutation,
                                     final int times) {
    for (int i = 0; i < times; i++)
      for (int c = 0; c < keys.length; c++)
        keys[c] = permutation[keys[c]];
    return keys;
  }

  protected static int[] permuteKeys(IntStream keys,
                                     final int[] permutation,
                                     final int times) {
    for (int i = 0; i < times; i++)
      keys = keys.map((k) -> permutation[k]);
    return keys.toArray();
  }

  public static RIV permuteRIV(final RIV riv,
                               final Permutations permutations,
                               final int times) {
    return riv.permute(permutations, times);
  }
}
