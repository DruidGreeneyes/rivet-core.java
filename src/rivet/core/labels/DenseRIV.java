package rivet.core.labels;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import rivet.core.util.IntDoubleConsumer;
import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

public class DenseRIV implements RIV, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4215652990755933410L;

    public static DenseRIV empty(final int size) {
        return new DenseRIV(size);
    }

    public static DenseRIV fromString(final String string) {
        String[] bits = string.split(" ");
        final int size = Integer.parseInt(bits[bits.length - 1]);
        bits = Arrays.copyOf(bits, bits.length - 1);
        final VectorElement[] points = Arrays.stream(bits)
                                             .map(VectorElement::fromString)
                                             .toArray(VectorElement[]::new);
        return new DenseRIV(points, size);
    }

    /**
     * Uses Java's seeded RNG to generate a random index vector such that, given
     * the same input, generateLabel will always produce the same output.
     *
     * @param size
     * @param k
     * @param word
     * @return a MapRIV
     */
    public static DenseRIV generateLabel(final int size, final int nnz,
            final CharSequence word) {
        final long seed = makeSeed(word);
        final int j = nnz % 2 == 0
                ? nnz
                : nnz + 1;
        return new DenseRIV(makeIndices(size, j, seed), makeVals(j, seed),
                size);
    }

    /**
     * Uses Java's seeded RNG to generate a random index vector such that, given
     * the same input, generateLabel will always produce the same output.
     *
     * @param size
     * @param k
     * @param word
     * @return a MapRIV
     */
    public static DenseRIV generateLabel(final int size, final int k,
            final CharSequence source, final int startIndex,
            final int tokenLength) {
        return generateLabel(size,
                             k,
                             Util.safeSubSequence(source,
                                                  startIndex,
                                                  startIndex + tokenLength));
    }

    public static Function<String, DenseRIV> labelGenerator(final int size,
            final int nnz) {
        return word -> generateLabel(size, nnz, word);
    }

    /*
     * @Override public DenseRIV add(final RIV other) { return
     * copy().destructiveAdd(other); }
     */

    public static Function<Integer, DenseRIV> labelGenerator(final int size,
            final int nnz, final CharSequence source, final int tokenLength) {
        return i -> generateLabel(size, nnz, source, i, tokenLength);
    }

    static int[] makeIndices(final int size, final int count, final long seed) {
        return Util.randInts(size, count, seed)
                   .toArray();
    }

    static long makeSeed(final CharSequence word) {
        final AtomicInteger c = new AtomicInteger();
        return word.chars()
                   .mapToLong(ch -> ch
                                    * (long) Math.pow(10, c.incrementAndGet()))
                   .sum();
    }

    static double[] makeVals(final int count, final long seed) {
        final double[] l = new double[count];
        for (int i = 0; i < count; i += 2) {
            l[i] = 1;
            l[i + 1] = -1;
        }
        return Util.shuffleDoubleArray(l, seed);
    }

    private final double[] vector;

    private DenseRIV(final double[] points) {
        vector = Arrays.copyOf(points, points.length);
    }

    public DenseRIV(final int[] indices, final double[] values,
            final int size) {
        this(size);
        for (int i = 0; i < indices.length; i++)
            vector[indices[i]] = values[i];
    }

    /*
     * @Override public DenseRIV divide(final double scalar) { return
     * copy().destructiveDiv(scalar); }
     */

    public double put(final int index, final double value) {
        double v = vector[index];
        vector[index] = value;
        return v;
    }

    public DenseRIV(final RIV source) {
        this(source.size());
        source.forEach(this::put);
    }

    private DenseRIV(final int size) {
        vector = new double[size];
        Arrays.fill(vector, 0);
    }

    public DenseRIV(final VectorElement[] points, final int size) {
        this(size);
        for (final VectorElement point : points)
            vector[point.index()] = point.value();
    }

    /*
     * @Override public double magnitude() { double sum = 0; for (final double v
     * : vector) sum += (v * v); return Math.sqrt(sum); }
     *
     * @Override public DenseRIV multiply(final double scalar) { return
     * copy().destructiveMult(scalar); }
     *
     * private DenseRIV destructiveNorm() { double sum = 0; for (final double v
     * : vector) sum += v; for (int i = 0; i < vector.length; i++) vector[i] =
     * vector[i] / sum; return this; }
     *
     * @Override public DenseRIV normalize() { return copy().destructiveNorm();
     * }
     */

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

    /*
     * @Override public DenseRIV subtract(final RIV other) throws
     * SizeMismatchException { return copy().destructiveSub(other); }
     */

    @Override
    public DenseRIV destructiveAdd(final RIV other) {
        for (final VectorElement point : other.points())
            vector[point.index()] += point.value();
        return this;
    }

    @Override
    public DenseRIV destructiveAdd(final RIV...rivs) {
        IntStream.range(0, vector.length)
                 .parallel()
                 .forEach(i -> vector[i] += Arrays.stream(rivs)
                                                  .parallel()
                                                  .mapToDouble(riv -> riv.get(i))
                                                  .sum());
        return this;
    }

    @Override
    public DenseRIV destructiveDiv(final double scalar) {
        for (int i = 0; i < vector.length; i++)
            vector[i] = vector[i] / scalar;
        return this;
    }

    @Override
    public DenseRIV destructiveMult(final double scalar) {
        for (int i = 0; i < vector.length; i++)
            vector[i] = vector[i] * scalar;
        return this;
    }

    /**
     * Doesn't do anything.
     */
    @Deprecated
    @Override
    public DenseRIV destructiveRemoveZeros() {
        return this;
    }

    @Override
    public DenseRIV destructiveSub(final RIV other) {
        for (final VectorElement point : other.points())
            vector[point.index()] -= point.value();
        return this;
    }

    @Override
    public DenseRIV destructiveSub(final RIV...rivs) {
        IntStream.range(0, vector.length)
                 .parallel()
                 .forEach(i -> vector[i] -= Arrays.stream(rivs)
                                                  .parallel()
                                                  .mapToDouble(riv -> riv.get(i))
                                                  .sum());
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        return RIVs.equals(this, obj);
    }

    @Override
    public boolean equals(final RIV riv) {
        if (vector.length != riv.size())
            return false;
        for (int i = 0; i < vector.length; i++)
            if (get(i) != riv.get(i))
                return false;
        return true;
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
    public DenseRIV permute(final Permutations permutations, int times) {
        if (times == 0)
            return this;
        else {
            final int[] prm = times > 0
                    ? permutations.left
                    : permutations.right;
            times = Math.abs(times);
            double[] res = Arrays.copyOf(vector, vector.length);
            final double[] p = new double[vector.length];
            for (int t = 0; t < times; t++) {
                for (int i = 0; i < res.length; i++)
                    p[prm[i]] = res[i];
                res = Arrays.copyOf(p, p.length);
            }
            return new DenseRIV(res);
        }
    }

    @Override
    public Stream<VectorElement> pointStream() {
        return keyStream().mapToObj(i -> VectorElement.elt(i, vector[i]));
    }

    /**
     * Doesn't do anything.
     */
    @Deprecated
    @Override
    public DenseRIV removeZeros() {
        return this;
    }

    @Override
    public double saturation() {
        return 1;
    }

    @Override
    public int size() {
        return vector.length;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++)
            sb.append(String.format("%d|%f ", i, vector[i]));
        sb.append("" + vector.length);
        return sb.toString();
    }

    @Override
    public DoubleStream valStream() {
        return Arrays.stream(vector);
    }

    @Override
    public int hashCode() {
        int sum = 0;
        final double[] vals = valArr();
        for (int i = 0; i < vals.length; i++)
            sum += vals[i] * (31 ^ (vals.length - 1 - i));
        return sum;
    }

    @Override
    public VectorElement[] points() {
        final VectorElement[] points = new VectorElement[vector.length];
        for (int i = 0; i < vector.length; i++)
            points[i] = VectorElement.elt(i, vector[i]);
        return points;
    }

    @Override
    public int[] keyArr() {
        final int[] keys = new int[vector.length];
        for (int i = 0; i < keys.length; i++)
            keys[i] = i;
        return keys;
    }

    @Override
    public double[] valArr() {
        return Arrays.copyOf(vector, vector.length);
    }

    @Override
    public void forEach(final IntDoubleConsumer fun) {
        for (int i = 0; i < vector.length; i++)
            fun.accept(i, vector[i]);
    }
}
