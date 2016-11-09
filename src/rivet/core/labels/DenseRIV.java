package rivet.core.labels;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import rivet.core.exceptions.SizeMismatchException;
import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

public class DenseRIV implements RIV, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4215652990755933410L;
    private final double[]    vector;

    public DenseRIV(final int size, final int[] indices,
            final double[] values) {
        vector = new double[size];
        Arrays.fill(vector, 0);
        for (int i = 0; i < indices.length; i++)
            vector[indices[i]] = values[i];
    }

    public DenseRIV(final int size, final VectorElement[] points) {
        vector = new double[size];
        Arrays.fill(vector, 0);
        for (final VectorElement point : points)
            vector[point.index()] = point.value();
    }

    private DenseRIV(final double[] points) {
        vector = Arrays.copyOf(points, points.length);
    }

    public DenseRIV(final RIV source) {
        this(source.size(), source.points());
    }

    @Override
    public DenseRIV add(final RIV other) {
        return copy().destructiveAdd(other);
    }

    @Override
    public boolean contains(final int index) {
        return index >= 0 && index < vector.length;
    }

    @Override
    public DenseRIV copy() {
        return new DenseRIV(this);
    }

    @Override
    public int count() {
        return size();
    }

    @Override
    public DenseRIV destructiveAdd(final RIV other) {
        for (final VectorElement point : other.points())
            vector[point.index()] += point.value();
        return this;
    }

    @Override
    public DenseRIV destructiveSub(final RIV other) {
        for (final VectorElement point : other.points())
            vector[point.index()] -= point.value();
        return this;
    }

    public DenseRIV destructiveDiv(final double scalar) {
        for (int i = 0; i < vector.length; i++)
            vector[i] = vector[i] / scalar;
        return this;
    }

    public DenseRIV destructiveMult(final double scalar) {
        for (int i = 0; i < vector.length; i++)
            vector[i] = vector[i] * scalar;
        return this;
    }

    @Override
    public DenseRIV divide(final double scalar) {
        return copy().destructiveDiv(scalar);
    }

    @Override
    public double get(final int index) {
        return vector[index];
    }

    @Override
    public IntStream keyStream() {
        return IntStream.range(0, vector.length);
    }

    @Override
    public double magnitude() {
        double sum = 0;
        for (final double v : vector)
            sum += (v * v);
        return Math.sqrt(sum);
    }

    @Override
    public DenseRIV multiply(final double scalar) {
        return copy().destructiveMult(scalar);
    }

    private DenseRIV destructiveNorm() {
        double sum = 0;
        for (final double v : vector)
            sum += v;
        for (int i = 0; i < vector.length; i++)
            vector[i] = vector[i] / sum;
        return this;
    }

    @Override
    public DenseRIV normalize() {
        return copy().destructiveNorm();
    }

    @Override
    public DenseRIV permute(final Permutations permutations, int times) {
        if (times == 0)
            return this;
        else {
            final int[] prm = (times > 0)
                    ? permutations.left
                    : permutations.right;
            times = Math.abs(times);
            double[] res = Arrays.copyOf(vector, vector.length);
            final double[] p = new double[vector.length];
            for (int i = 0; i < times; i++) {
                p[prm[i]] = res[i];
                res = Arrays.copyOf(p, p.length);
            }
            return new DenseRIV(res);
        }
    }

    @Override
    public VectorElement[] points() {
        return keyStream().mapToObj(i -> VectorElement.elt(i, vector[i]))
                          .toArray(VectorElement[]::new);
    }

    @Override
    public int size() {
        return vector.length;
    }

    @Override
    public DenseRIV subtract(final RIV other) throws SizeMismatchException {
        return copy().destructiveSub(other);
    }

    @Override
    public DoubleStream valStream() {
        return Arrays.stream(vector);
    }

    @Override
    public double saturation() {
        return 1;
    }

    public static DenseRIV generateLabel(final int size, final int nnz,
            final CharSequence word) {
        final long seed = makeSeed(word);
        final int j = nnz % 2 == 0
                ? nnz
                : nnz + 1;
        return new DenseRIV(size, makeIndices(size, j, seed),
                makeVals(j, seed));
    }

    private static int[] makeIndices(final int size, final int count,
            final long seed) {
        return Util.randInts(size, count, seed)
                   .toArray();
    }

    private static long makeSeed(final CharSequence word) {
        final AtomicInteger c = new AtomicInteger();
        return word.chars()
                   .mapToLong(ch -> ch
                                    * (long) Math.pow(10, c.incrementAndGet()))
                   .sum();
    }

    private static double[] makeVals(final int count, final long seed) {
        final double[] l = new double[count];
        for (int i = 0; i < count; i += 2) {
            l[i] = 1;
            l[i + 1] = -1;
        }
        return Util.shuffleDoubleArray(l, seed);
    }
}
