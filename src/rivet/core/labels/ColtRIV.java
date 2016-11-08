package rivet.core.labels;

import static rivet.core.util.colt.ColtConversions.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import cern.colt.function.IntProcedure;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.map.AbstractIntDoubleMap;
import cern.colt.map.OpenIntDoubleHashMap;
import rivet.core.exceptions.SizeMismatchException;
import rivet.core.util.Util;
import rivet.core.util.VectorComputeFunction;
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
    public final int          size;

    private ColtRIV(final int size, final IntArrayList indices,
            final DoubleArrayList values) {
        super();
        for (int i = 0; i < indices.size(); i++)
            put(indices.get(i), values.get(i));
        this.size = size;
    }

    public ColtRIV(final int size, final VectorElement[] points) {
        super();
        this.size = size;
        for (final VectorElement point : points)
            put(point.index(), point.value());
    }

    public static ColtRIV empty(final int size) {
        return new ColtRIV(size, new VectorElement[0]);
    }

    private static IntArrayList makeIndices(final int size, final int count,
            final long seed) {
        final IntArrayList res = new IntArrayList();
        Util.randInts(size, count, seed)
            .forEach(res::add);
        return res;
    }

    /**
     * @param word
     * @return a probably-unique long, used to seed java's Random.
     */
    private static long makeSeed(final CharSequence word) {
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
    private static DoubleArrayList makeVals(final int count, final long seed) {
        final double[] l = new double[count];
        for (int i = 0; i < count; i += 2) {
            l[i] = 1;
            l[i + 1] = -1;
        }
        final double[] v = Util.shuffleDoubleArray(l, seed);
        final DoubleArrayList res = new DoubleArrayList();
        for (final double d : v)
            res.add(d);
        return res;
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
        return new ColtRIV(size, makeIndices(size, j, seed), makeVals(j, seed));
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

    @Override
    public ColtRIV add(final RIV other) throws SizeMismatchException {
        return copy().destructiveAdd(other);
    }

    @Override
    public boolean contains(final int index) throws IndexOutOfBoundsException {
        return containsKey(index);
    }

    @Override
    public ColtRIV copy() {
        return new ColtRIV(size, points());
    }

    @Override
    public int count() {
        return size();
    }

    @Override
    public ColtRIV destructiveAdd(final RIV other) {
        for (final VectorElement point : other.points()) {
            final int i = point.index();
            put(i, get(i) + point.value());
        }
        return this;
    }

    @Override
    public ColtRIV destructiveSub(final RIV other) {
        for (final VectorElement point : other.points()) {
            final int i = point.index();
            put(i, get(i) - point.value());
        }
        return this;
    }

    @Override
    public ColtRIV divide(final double scalar) {
        return copy().destructiveDiv(scalar);
    }

    public ColtRIV destructiveDiv(final double scalar) {
        this.assign(x -> x / scalar);
        return this;
    }

    @Override
    public IntStream keyStream() {
        final IntStream.Builder sb = IntStream.builder();
        keys().forEach(procedurize(sb::accept));
        return sb.build();
    }

    @Override
    public double magnitude() {
        return Math.sqrt(valStream().map(x -> x * x)
                                    .sum());
    }

    @Override
    public ColtRIV multiply(final double scalar) {
        return copy().destructiveMult(scalar);
    }

    public ColtRIV destructiveMult(final double scalar) {
        this.assign(x -> x * scalar);
        return this;
    }

    @Override
    public ColtRIV normalize() {
        final double mag = magnitude();
        final ColtRIV res = copy();
        res.assign(x -> x / mag);
        return res;
    }

    private static IntArrayList permuteKeys(IntStream keys, final int times,
            final int[] permutation) {
        final IntArrayList res = new IntArrayList();
        for (int i = 0; i < times; i++)
            keys = keys.map((k) -> permutation[k]);
        keys.forEach(res::add);
        return res;
    }

    @Override
    public ColtRIV permute(final Permutations permutations, final int times) {
        if (times == 0)
            return this;
        final IntArrayList newKeys = (times > 0)
                ? permuteKeys(keyStream(), times, permutations.left)
                : permuteKeys(keyStream(), -times, permutations.right);
        return new ColtRIV(size, newKeys, this.values());
    }

    @Override
    public VectorElement[] points() {
        final AtomicInteger c = new AtomicInteger();
        final VectorElement[] res = new VectorElement[count()];
        forEachPair((a, b) -> {
            res[c.getAndIncrement()] = VectorElement.elt(a, b);
            return true;
        });
        return res;
    }

    @Override
    public ColtRIV subtract(final RIV other) throws SizeMismatchException {
        return copy().destructiveSub(other);
    }

    @Override
    public DoubleStream valStream() {
        final DoubleStream.Builder sb = DoubleStream.builder();
        values().forEach(procedurize((final double i) -> sb.accept(i)));
        return sb.build();
    }
}