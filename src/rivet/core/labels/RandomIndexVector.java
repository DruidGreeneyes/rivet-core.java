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
    
    VectorElement[] points();
    int size();
    int count();
    
    String toString();
    
    IntStream keyStream();
    DoubleStream valStream();
    
    boolean contains(int index) throws IndexOutOfBoundsException;
    boolean equals(RandomIndexVector other);
    
    double get(int index) throws IndexOutOfBoundsException;
    
    RandomIndexVector copy();
    
    RandomIndexVector mapKeys(IntUnaryOperator fun);
    RandomIndexVector mapVals(DoubleUnaryOperator fun);
    
    RandomIndexVector add(RandomIndexVector other) throws SizeMismatchException;
    RandomIndexVector subtract(RandomIndexVector other) throws SizeMismatchException;
    RandomIndexVector multiply(double scalar);
    RandomIndexVector divide(double scalar);
    
    RandomIndexVector normalize();
    
    RandomIndexVector permute(Permutations permutations, int times);
    
    default int[] keys() {return this.keyStream().toArray();}
    default double[] vals() {return this.valStream().toArray();}
    
    default double magnitude() {
        return Math.sqrt(
                this.valStream()
                .map((v) -> v * v)
                .sum());
    }
    
    default <T> T evert(Function<RandomIndexVector, T> fun) {return fun.apply(this);}
    
    public static IntStream getMatchingKeyStream (final RandomIndexVector rivA, final RandomIndexVector rivB) {
        return rivA.keyStream().filter(rivA::contains);
    }
    
    public static Stream<Pair<Double, Double>> getMatchingValStream (final RandomIndexVector rivA, final RandomIndexVector rivB) {
        return getMatchingKeyStream(rivA, rivB)
                .mapToObj((i) -> Pair.make(rivA.get(i), rivB.get(i)));
    }
    
    public static double dotProduct (final RandomIndexVector rivA, final RandomIndexVector rivB) {
        return getMatchingValStream(rivA, rivB)
                .mapToDouble((valPair) -> valPair.left * valPair.right)
                .sum();
    }
    
    public static double similarity (final RandomIndexVector rivA, final RandomIndexVector rivB) {
        final double mag = rivA.magnitude() * rivB.magnitude();
        return (mag == 0)
                ? 0
                : dotProduct(rivA, rivB) / mag;
    }
    
    public static RandomIndexVector addRIVs(final RandomIndexVector rivA, final RandomIndexVector rivB) throws SizeMismatchException {
        return rivA.add(rivB);
    }
    public static RandomIndexVector addRIVs(final RandomIndexVector...rivs) throws SizeMismatchException {
        return Arrays.stream(rivs)
                .reduce(RandomIndexVector::addRIVs)
                .get();
    }
    
    public static RandomIndexVector permuteRIV(final RandomIndexVector riv, final Permutations permutations, final int times) {
        return riv.permute(permutations, times);
    }
}
