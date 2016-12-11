package rivet.core.labels;

import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import rivet.core.vectorpermutations.Permutations;

public class ImmutableRIV implements RIV {
    private final int      size;
    private final int[]    keys;
    private final double[] vals;

    private ImmutableRIV(final int size, final int[] keys,
            final double[] vals) {
        this.size = size;
        this.keys = Arrays.copyOf(keys, keys.length);
        this.vals = Arrays.copyOf(vals, vals.length);
    }

    private ImmutableRIV(final int size, final VectorElement[] points) {
        this.size = size;
        keys = new int[points.length];
        vals = new double[points.length];
        for (int i = 0; i < points.length; i++) {
            keys[i] = points[i].index();
            vals[i] = points[i].value();
        }
    }

    public ImmutableRIV(final RIV riv) {
        this(riv.size(), riv.points());
    }

    @Override
    public boolean contains(final int index) {
        return ArrayUtils.contains(keys, index);
    }

    @Override
    public RIV copy() {
        return new ImmutableRIV(size, keys, vals);
    }

    @Override
    public int count() {
        return keys.length;
    }

    private ImmutableRIV map(final DoubleUnaryOperator operation) {
        double[] newVals = new double[vals.length];
        final int[] zeros = new int[0];
        for (int i = 0; i < vals.length; i++) {
            newVals[i] = operation.applyAsDouble(vals[i]);
            if (newVals[i] == 0)
                ArrayUtils.add(zeros, i);
        }
        final int[] newKeys = ArrayUtils.removeAll(keys, zeros);
        newVals = ArrayUtils.removeAll(newVals, zeros);
        return new ImmutableRIV(size, newKeys, newVals);
    }

    private ImmutableRIV merge(final DoubleBinaryOperator mergeFunction,
            final RIV other) {
        int[] newKeys = IntStream.concat(keyStream(), other.keyStream())
                                 .distinct()
                                 .sorted()
                                 .toArray();
        double[] newVals = new double[newKeys.length];
        final int[] zeros = new int[0];
        for (int i = 0; i < newKeys.length; i++) {
            final int k = newKeys[i];
            newVals[i] = mergeFunction.applyAsDouble(get(k), other.get(k));
            if (newVals[i] == 0)
                ArrayUtils.add(zeros, i);
        }
        newKeys = ArrayUtils.removeAll(newKeys, zeros);
        newVals = ArrayUtils.removeAll(newVals, zeros);
        return new ImmutableRIV(size, newKeys, newVals);
    }

    private ImmutableRIV merge(final DoubleBinaryOperator mergeFunction,
            final RIV...others) {
        int[] newKeys = Stream.concat(Stream.of(this), Arrays.stream(others))
                              .flatMapToInt(RIV::keyStream)
                              .distinct()
                              .sorted()
                              .toArray();
        double[] newVals = new double[newKeys.length];
        int[] zeros = new int[0];
        for (int i = 0; i < newKeys.length; i++) {
            final int k = newKeys[i];
            double v = get(k);
            for (final RIV riv : others)
                v = mergeFunction.applyAsDouble(v, riv.get(k));
            if (v == 0)
                zeros = ArrayUtils.add(zeros, i);
            newVals[i] = v;
        }
        newKeys = ArrayUtils.removeAll(newKeys, zeros);
        newVals = ArrayUtils.removeAll(newVals, zeros);
        return new ImmutableRIV(size, newKeys, newVals);
    }

    private static final DoubleBinaryOperator add = (a, b) -> a + b;

    @Override
    public ImmutableRIV add(final RIV other) {
        return merge(add, other);
    }

    @Override
    public ImmutableRIV add(final RIV...others) {
        return merge(add, others);
    }

    @Override
    public ImmutableRIV destructiveAdd(final RIV other) {
        return add(other);
    }

    @Override
    public ImmutableRIV destructiveAdd(final RIV...rivs) {
        return add(rivs);
    }

    private static final DoubleBinaryOperator subtract = (a, b) -> a - b;

    @Override
    public ImmutableRIV subtract(final RIV other) {
        return merge(subtract, other);
    }

    @Override
    public ImmutableRIV subtract(final RIV...others) {
        return merge(subtract, others);
    }

    @Override
    public ImmutableRIV destructiveSub(final RIV other) {
        return subtract(other);
    }

    @Override
    public ImmutableRIV destructiveSub(final RIV...rivs) {
        return subtract(rivs);
    }

    private static DoubleUnaryOperator divideBy(final double scalar) {
        return a -> a / scalar;
    }

    @Override
    public ImmutableRIV divide(final double scalar) {
        return map(divideBy(scalar));
    }

    @Override
    public ImmutableRIV destructiveDiv(final double scalar) {
        return divide(scalar);
    }

    @Override
    public boolean equals(final Object obj) {
        return RIVs.equals(this, obj);
    }

    @Override
    public boolean equals(final RIV other) {
        if (other.getClass()
                 .equals(ImmutableRIV.class))
            return equals(other);
        else
            return size == other.size()
                   && Arrays.deepEquals(points(), other.points());
    }

    public boolean equals(final ImmutableRIV other) {
        return size == other.size && Arrays.equals(keys, other.keys)
               && Arrays.equals(vals, other.vals);
    }

    @Override
    public double get(final int index) throws IndexOutOfBoundsException {
        try {
            return vals[ArrayUtils.indexOf(keys, index)];
        } catch (final ArrayIndexOutOfBoundsException e) {
            if (0 <= index && index < size)
                return 0;
            else
                throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    @Override
    public IntStream keyStream() {
        return Arrays.stream(keys);
    }

    private static DoubleUnaryOperator multiplyBy(final double scalar) {
        return a -> a * scalar;
    }

    @Override
    public ImmutableRIV multiply(final double scalar) {
        return map(multiplyBy(scalar));
    }

    @Override
    public ImmutableRIV destructiveMult(final double scalar) {
        return multiply(scalar);
    }

    private Stream<VectorElement> permute(final Stream<VectorElement> points,
            final int[] permutation) {
        return points.map(elt -> elt.destructiveSet(permutation[elt.index()]));
    }

    @Override
    public ImmutableRIV permute(final Permutations permutations,
            final int times) {
        if (times == 0)
            return this;
        final int[] permutation = (times > 0)
                ? permutations.left
                : permutations.right;
        final int t = Math.abs(times);
        Stream<VectorElement> points = pointStream();
        for (int i = 0; i < t; i++)
            points = permute(points, permutation);
        return new ImmutableRIV(size, points.sorted()
                                            .toArray(VectorElement[]::new));
    }

    @Override
    public Stream<VectorElement> pointStream() {
        return IntStream.range(0, keys.length)
                        .mapToObj(i -> VectorElement.elt(keys[i], vals[i]));
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public DoubleStream valStream() {
        return Arrays.stream(vals);
    }

    @Override
    public ImmutableRIV destructiveRemoveZeros() {
        return this;
    }

    @Override
    public int hashCode() {
        return keyStream().sum();
    }
}
