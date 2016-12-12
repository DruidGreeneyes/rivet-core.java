package rivet.core.labels;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import druid.utils.pair.UniformPair;
import rivet.core.exceptions.SizeMismatchException;
import rivet.core.vectorpermutations.Permutations;

public class RIVs {

    protected static boolean equals(final RIV riv, final Object other) {
        if (riv == other)
            return true;
        else if (ArrayUtils.contains(other.getClass()
                                          .getInterfaces(),
                                     RIV.class))
            return riv.equals((RIV) other);
        else
            return false;
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
        for (final double[] vals : getMatchingVals(rivA, rivB))
            sum += vals[0] * vals[1];
        return sum;
    }

    private static int[] getMatchingKeys(final RIV rivA, final RIV rivB) {
        final int[] keys = rivA.keyArr(), keysB = rivB.keyArr();
        for (int i = 0; i < keys.length; i++)
            if (!ArrayUtils.contains(keysB, keys[i]))
                keys[i] = -1;
        ArrayUtils.removeAllOccurences(keysB, -1);
        return keys;
    }

    private static IntStream getMatchingKeyStream(final RIV rivA,
            final RIV rivB) {
        return rivA.keyStream()
                   .filter(rivB::contains);
    }

    private static double[][] getMatchingVals(final RIV rivA, final RIV rivB) {
        final int[] keys = getMatchingKeys(rivA, rivB);
        final double[][] vals = new double[keys.length][2];
        for (int i = 0; i < keys.length; i++) {
            vals[i][0] = rivA.get(i);
            vals[i][1] = rivB.get(i);
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

    private RIVs() {
    }

}
