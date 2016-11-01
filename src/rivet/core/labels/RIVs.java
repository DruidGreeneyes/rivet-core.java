package rivet.core.labels;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import pair.UniformPair;
import rivet.core.exceptions.SizeMismatchException;
import rivet.core.vectorpermutations.Permutations;

public class RIVs {

    public static RIV addRIVs(final RIV rivA, final RIV rivB)
            throws SizeMismatchException {
        return rivA.add(rivB);
    }

    public static double dotProduct(final RIV rivA, final RIV rivB) {
        return getMatchingValStream(rivA,
                                    rivB).mapToDouble(pair -> pair.left
                                                              * pair.right)
                                         .sum();
    }

    private static IntStream getMatchingKeyStream(final RIV rivA,
            final RIV rivB) {
        return rivA.keyStream()
                   .filter(rivA::contains);
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
