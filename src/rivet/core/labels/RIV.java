package rivet.core.labels;

import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import rivet.core.exceptions.SizeMismatchException;
import rivet.core.vectorpermutations.Permutations;

public interface RIV {

    RIV add(final RIV other) throws SizeMismatchException;

    boolean contains(final int index) throws IndexOutOfBoundsException;

    RIV copy();

    int count();

    RIV destructiveAdd(final RIV other);

    RIV destructiveSub(final RIV other) throws SizeMismatchException;

    RIV divide(final double scalar);

    @Override
    boolean equals(final Object other);

    default <T> T evert(final Function<RIV, T> fun) {
        return fun.apply(this);
    }

    double get(final int index) throws IndexOutOfBoundsException;

    IntStream keyStream();

    double magnitude();

    RIV multiply(final double scalar);

    RIV normalize();

    RIV permute(final Permutations permutations, final int times);

    VectorElement[] points();

    int size();

    RIV subtract(final RIV other) throws SizeMismatchException;

    @Override
    String toString();

    DoubleStream valStream();
}
