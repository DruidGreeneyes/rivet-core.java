package rivet.core.labels;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableDouble;

import druid.utils.pair.UniformPair;
import rivet.core.exceptions.SizeMismatchException;
import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

public class RIVs {

    /**
     * @param size
     * @param count
     * @param seed
     * @return an array of count random integers between 0 and size
     */
    protected static int[] makeIndices(final int size, final int count, final long seed) {
        return Util.randInts(size, count, seed)
                   .toArray();
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

    protected static int[] permuteKeys(IntStream keys, final int[] permutation,
            final int times) {
        for (int i = 0; i < times; i++)
            keys = keys.map((k) -> permutation[k]);
        return keys.toArray();
    }

    protected static boolean equals(final RIV riv, final Object other) {
        if (riv == other)
            return true;
        else if (riv.getClass().equals(other.getClass()))
        	return riv.equals(riv.getClass().cast(other));
        else if (ArrayUtils.contains(other.getClass()
                                          .getInterfaces(),
                                     RIV.class))
            return riv.equals((RIV) other);
        else
            return false;
    }
    
    protected static int hashcode(final RIV riv) {
        int sum = 0;
        final double[] vals = riv.valArr();
        for (int i = 0; i < vals.length; i++)
            sum += vals[i] * (31 ^ (vals.length - 1 - i));
        return sum;
    }

    public static RIV addRIVs(final RIV rivA, final RIV rivB)
            throws SizeMismatchException {
        return rivA.add(rivB);
    }

    public static double dotProduct2(final RIV rivA, final RIV rivB) {
        return getMatchingValStream(rivA,
                                    rivB).mapToDouble(pair -> pair.left
                                                              * pair.right)
                                         .sum();
    }

    public static double dotProduct(final RIV rivA, final RIV rivB) {
        double sum = 0;
        for (final VectorElement p : rivA.points())
            sum += p.value() * rivB.get(p.index());
        return sum;
    }

    static int[] getMatchingKeys(final RIV rivA, final RIV rivB) {
        final int[] keys = rivA.keyArr();
        final int[] keysB = rivB.keyArr();
        for (int i = 0; i < keys.length; i++)
            if (!ArrayUtils.contains(keysB, keys[i]))
                keys[i] = -1;
        return ArrayUtils.removeAllOccurences(keys, -1);
    }

    private static IntStream getMatchingKeyStream(final RIV rivA,
            final RIV rivB) {
        return rivA.keyStream()
                   .filter(rivB::contains);
    }

    static double[][] getMatchingVals(final RIV rivA, final RIV rivB) {
        final int[] keys = getMatchingKeys(rivA, rivB);
        final double[][] vals = new double[keys.length][2];
        for (int i = 0; i < keys.length; i++) {
            vals[i][0] = rivA.get(keys[i]);
            vals[i][1] = rivB.get(keys[i]);
        }
        return vals;
    }

    private static Stream<UniformPair<Double>> getMatchingValStream(
            final RIV rivA, final RIV rivB) {
        return getMatchingKeyStream(rivA,
                                    rivB).mapToObj(i -> UniformPair.make(rivA.get(i),
                                                                         rivB.get(i)));
    }

    public static RIV permuteRIV(final RIV riv, final Permutations permutations,
            final int times) {
        return riv.permute(permutations, times);
    }

    public static double similarity(final RIV rivA, final RIV rivB) {
        final double mag = rivA.magnitude() * rivB.magnitude();
        return mag == 0
                ? 0
                : dotProduct(rivA, rivB) / mag;
    }

    public static RIV sumRIVs(final RIV zeroValue, final RIV...rivs)
            throws SizeMismatchException {
        return sumRIVs(zeroValue, Arrays.stream(rivs));
    }

    public static RIV sumRIVs(final RIV zeroValue, final Stream<RIV> rivs)
            throws SizeMismatchException {
        return rivs.reduce(zeroValue, RIV::destructiveAdd);
    }
    
    protected static String toString(RIV riv) {
    	StringBuilder sb = new StringBuilder();
        for (VectorElement point : riv.points())
        	sb.append(point.toString() + " ");
        sb.append(riv.size());
        return sb.toString();
    }

    private RIVs() {
    }

}
