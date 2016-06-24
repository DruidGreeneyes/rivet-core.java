package rivet.core.labels;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import rivet.core.exceptions.SizeMismatchException;
import rivet.core.util.Pair;
import rivet.core.vectorpermutations.Permutations;

public interface RandomIndexVector {

    public static RandomIndexVector addRIVs(final RandomIndexVector... rivs)
            throws SizeMismatchException {
        return Arrays.stream(rivs).reduce(RandomIndexVector::addRIVs).get();
    }

    public static RandomIndexVector addRIVs(final RandomIndexVector rivA,
            final RandomIndexVector rivB) throws SizeMismatchException {
        return rivA.add(rivB);
    }

    public static double dotProduct(final RandomIndexVector rivA,
            final RandomIndexVector rivB) {
        return getMatchingValStream(rivA, rivB)
                .mapToDouble((valPair) -> valPair.apply((a, b) -> a * b)).sum();
    }

    public static IntStream getMatchingKeyStream(final RandomIndexVector rivA,
            final RandomIndexVector rivB) {
        return rivA.keyStream().filter(rivA::contains);
    }

    public static Stream<Pair<Double, Double>> getMatchingValStream(
            final RandomIndexVector rivA, final RandomIndexVector rivB) {
        return getMatchingKeyStream(rivA, rivB)
                .mapToObj((i) -> Pair.make(rivA.get(i), rivB.get(i)));
    }

    public static RandomIndexVector permuteRIV(final RandomIndexVector riv,
            final Permutations permutations, final int times) {
        return riv.permute(permutations, times);
    }

    public static double similarity(final RandomIndexVector rivA,
            final RandomIndexVector rivB) {
        final double mag = rivA.magnitude() * rivB.magnitude();
        return mag == 0 ? 0 : dotProduct(rivA, rivB) / mag;
    }

    RandomIndexVector add(RandomIndexVector other) throws SizeMismatchException;

    boolean contains(int index) throws IndexOutOfBoundsException;

    RandomIndexVector copy();

    int count();

    RandomIndexVector divide(double scalar);

    @Override
    boolean equals(Object other);

    default <T> T evert(final Function<RandomIndexVector, T> fun) {
        return fun.apply(this);
    }

    double get(int index) throws IndexOutOfBoundsException;

    default int[] keys() {
        return keyStream().toArray();
    }

    IntStream keyStream();

    default double magnitude() {
        return Math.sqrt(valStream().map((v) -> v * v).sum());
    }

    RandomIndexVector mapKeys(IntUnaryOperator fun);

    RandomIndexVector mapVals(DoubleUnaryOperator fun);

    RandomIndexVector multiply(double scalar);

    RandomIndexVector normalize();

    RandomIndexVector permute(Permutations permutations, int times);

    VectorElement[] points();

    int size();

    RandomIndexVector subtract(RandomIndexVector other)
            throws SizeMismatchException;

    @Override
    String toString();

    default double[] vals() {
        return valStream().toArray();
    }

    DoubleStream valStream();
}
