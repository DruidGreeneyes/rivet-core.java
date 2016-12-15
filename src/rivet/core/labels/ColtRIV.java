package rivet.core.labels;

import static rivet.core.util.colt.ColtConversions.procedurize;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import cern.colt.map.tdouble.OpenIntDoubleHashMap;
import cern.jet.math.tdouble.DoubleMult;
import rivet.core.util.IntDoubleConsumer;
import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

/**
 * Merging cern.colt.map.OpenIntDoubleHashMap with java 8 Map in order to
 * primitive-based mappings that are both fast -and- easy to use.
 *
 * @author josh
 *
 */
public class ColtRIV extends OpenIntDoubleHashMap implements RIV {

    /**
     *
     */
    private static final long serialVersionUID = 7489480432514925162L;

    public static ColtRIV empty(final int size) {
        return new ColtRIV(size);
    }

    public static ColtRIV fromString(final String string) {
        String[] bits = string.split(" ");
        final int size = Integer.parseInt(bits[bits.length - 1]);
        bits = Arrays.copyOf(bits, bits.length - 1);
        final VectorElement[] elements = Arrays.stream(bits)
                                               .map(VectorElement::fromString)
                                               .toArray(VectorElement[]::new);
        return new ColtRIV(elements, size);
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
    public static ColtRIV generateLabel(final int size, final int k,
            final CharSequence word) {
        final long seed = makeSeed(word);
        final int j = k % 2 == 0
                ? k
                : k + 1;
        return new ColtRIV(makeIndices(size, j, seed), makeVals(j, seed), size);
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
    public static ColtRIV generateLabel(final int size, final int k,
            final CharSequence source, final int startIndex,
            final int tokenLength) {
        return generateLabel(size,
                             k,
                             Util.safeSubSequence(source,
                                                  startIndex,
                                                  startIndex + tokenLength));
    }

    public static Function<String, ColtRIV> labelGenerator(final int size,
            final int nnz) {
        return word -> generateLabel(size, nnz, word);
    }

    public static Function<Integer, ColtRIV> labelGenerator(final int size,
            final int nnz, final CharSequence source, final int tokenLength) {
        return i -> generateLabel(size, nnz, source, i, tokenLength);
    }

    public static int[] makeIndices(final int size, final int count,
            final long seed) {
        return Util.randInts(size, count, seed)
                   .toArray();
    }

    /**
     * @param word
     * @return a probably-unique long, used to seed java's Random.
     */
    public static long makeSeed(final CharSequence word) {
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
    public static double[] makeVals(final int count, final long seed) {
        final double[] vals = new double[count];
        for (int i = 0; i < count; i += 2) {
            vals[i] = 1;
            vals[i + 1] = -1;
        }
        return Util.shuffleDoubleArray(vals, seed);
    }

    private static int[] permuteKeys(final int[] keys,
            final int[] permutation) {
        for (int i = 0; i < keys.length; i++)
            keys[i] = permutation[keys[i]];
        return keys;
    }

    public final int size;

    public ColtRIV(final int[] indices, final double[] values, final int size) {
        super();
        for (int i = 0; i < indices.length; i++)
            put(indices[i], values[i]);
        this.size = size;
    }

    private ColtRIV(final int size) {
        super();
        this.size = size;
    }

    /*
     * @Override public ColtRIV add(final RIV other) throws
     * SizeMismatchException { return copy().destructiveAdd(other); }
     */

    public ColtRIV(final RIV riv) {
        this(riv.size());
        riv.forEach(this::put);
    }

    public ColtRIV(final VectorElement[] points, final int size) {
        super();
        this.size = size;
        for (final VectorElement point : points)
            put(point.index(), point.value());
    }

    @Override
    public boolean contains(final int index) throws IndexOutOfBoundsException {
        return containsKey(index);
    }

    @Override
    public ColtRIV copy() {
        return new ColtRIV(this);
    }

    @Override
    public int count() {
        return super.size();
    }

    @Override
    public ColtRIV destructiveAdd(final RIV other) {
        for (final int i : other.keyArr())
            put(i, get(i) + other.get(i));
        return this;
    }

    /*
     * @Override public ColtRIV divide(final double scalar) { return
     * copy().destructiveDiv(scalar); }
     */

    @Override
    public ColtRIV destructiveAdd(final RIV...rivs) {
        for (int i = 0; i < size; i++) {
            double v = get(i);
            for (final RIV riv : rivs)
                v += riv.get(i);
            if (v == 0)
                removeKey(i);
            else
                put(i, v);
        }
        return this;
    }

    @Override
    public ColtRIV destructiveDiv(final double scalar) {
        assign(DoubleMult.div(scalar));
        return this;
    }

    /*
     * @Override public double magnitude() { return Math.sqrt(valStream().map(x
     * -> x * x) .sum()); }
     *
     * @Override public ColtRIV multiply(final double scalar) { return
     * copy().destructiveMult(scalar); }
     */

    @Override
    public ColtRIV destructiveMult(final double scalar) {
        assign(DoubleMult.mult(scalar));
        return this;
    }

    /*
     * @Override public ColtRIV normalize() { final double mag = magnitude();
     * final ColtRIV res = copy(); res.assign(x -> x / mag); return res; }
     */

    @Override
    public ColtRIV destructiveRemoveZeros() {
        int i;
        while (Integer.MIN_VALUE != (i = keyOf(0.0)))
            removeKey(i);
        return this;
    }

    @Override
    public ColtRIV destructiveSub(final RIV other) {
        for (final int i : other.keyArr())
            put(i, get(i) - other.get(i));
        return this;
    }

    @Override
    public ColtRIV destructiveSub(final RIV...rivs) {
        for (int i = 0; i < size; i++) {
            double v = get(i);
            for (final RIV riv : rivs)
                v -= riv.get(i);
            if (v == 0)
                removeKey(i);
            else
                put(i, v);
        }
        return this;
    }

    @Override
    public boolean equals(final Object other) {
        return RIVs.equals(this, other);
    }

    @Override
    public boolean equals(final RIV other) {
        if (other.getClass()
                 .equals(ColtRIV.class))
            return equals((ColtRIV) other);
        else
            return size == other.size()
                   && Arrays.deepEquals(points(), other.points());
    }

    public boolean equals(final ColtRIV other) {
        return size == other.size() && super.equals(other);
    }

    /*
     * @Override public ColtRIV subtract(final RIV other) throws
     * SizeMismatchException { return copy().destructiveSub(other); }
     */

    @Override
    public IntStream keyStream() {
        return Arrays.stream(keyArr());
    }

    /*
     * @Override public ColtRIV removeZeros() { return
     * copy().destructiveRemoveZeros(); }
     */

    @Override
    public ColtRIV permute(final Permutations permutations, final int times) {
        if (times == 0)
            return this;
        final int[] perm = (times > 0)
                ? permutations.left
                : permutations.right;
        final int t = Math.abs(times);
        final int[] newKeys = keyArr();
        for (int i = 0; i < t; i++)
            permuteKeys(newKeys, perm);
        final ColtRIV res = new ColtRIV(newKeys, valArr(), size);
        return res;
    }

    @Override
    public Stream<VectorElement> pointStream() {
        final Stream.Builder<VectorElement> sb = Stream.builder();
        forEachPair(procedurize((k, v) -> sb.accept(VectorElement.elt(k, v))));
        return sb.build();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final VectorElement point : points())
            sb.append(point.toString() + " ");
        sb.append("" + size);
        return sb.toString();
    }

    @Override
    public DoubleStream valStream() {
        final DoubleStream.Builder sb = DoubleStream.builder();
        forEachPair(procedurize((k, v) -> sb.accept(v)));
        return sb.build();
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
        final VectorElement[] points = new VectorElement[count()];
        final AtomicInteger c = new AtomicInteger();
        forEachPair(procedurize((k, v) -> points[c.getAndIncrement()] =
                VectorElement.elt(k, v)));
        Arrays.sort(points);
        return points;
    }

    @Override
    public int[] keyArr() {
        final int[] keys = new int[count()];
        final int c = 0;
        for (int i = 0; i < size; i++)
            if (containsKey(i)) {
                keys[c] = i;
                i++;
            }
        return keys;
    }

    @Override
    public double[] valArr() {
        double[] vals = new double[size];
        for (int i = 0; i < size; i++)
            vals[i] = get(i);
        vals = ArrayUtils.removeAllOccurences(vals, 0.0);
        return vals;
    }

    @Override
    public void forEach(final IntDoubleConsumer fun) {
        super.forEachPair(procedurize(fun));
    }
}
