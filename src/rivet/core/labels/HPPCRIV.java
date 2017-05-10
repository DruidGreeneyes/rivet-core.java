package rivet.core.labels;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import com.carrotsearch.hppc.IntDoubleHashMap;
import com.carrotsearch.hppc.predicates.IntDoublePredicate;
import com.carrotsearch.hppc.procedures.IntDoubleProcedure;

import rivet.core.util.IntDoubleConsumer;
import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

public class HPPCRIV extends IntDoubleHashMap implements RIV, Serializable {

    /**
    *
    */
    private static final long serialVersionUID = 7489480432514925162L;

    public static HPPCRIV empty(final int size) {
        return new HPPCRIV(size);
    }

    public static HPPCRIV fromString(final String string) {
        String[] bits = string.split(" ");
        final int size = Integer.parseInt(bits[bits.length - 1]);
        bits = Arrays.copyOf(bits, bits.length - 1);
        final VectorElement[] elements = Arrays.stream(bits)
                                               .map(VectorElement::fromString)
                                               .toArray(VectorElement[]::new);
        return new HPPCRIV(elements, size);
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
    public static HPPCRIV generateLabel(final int size, final int k,
            final CharSequence word) {
        final long seed = makeSeed(word);
        final int j = k % 2 == 0
                ? k
                : k + 1;
        return new HPPCRIV(makeIndices(size, j, seed), makeVals(j, seed), size);
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
    public static HPPCRIV generateLabel(final int size, final int k,
            final CharSequence source, final int startIndex,
            final int tokenLength) {
        return generateLabel(size,
                             k,
                             Util.safeSubSequence(source,
                                                  startIndex,
                                                  startIndex + tokenLength));
    }

    public static Function<String, HPPCRIV> labelGenerator(final int size,
            final int nnz) {
        return word -> generateLabel(size, nnz, word);
    }

    public static Function<Integer, HPPCRIV> labelGenerator(final int size,
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

    private final int size;

    public HPPCRIV(final int size) {
        super();
        this.size = size;
    }

    public HPPCRIV(final int[] indices, final double[] values, final int size) {
        this(size);
        for (int i = 0; i < indices.length; i++)
            put(indices[i], values[i]);
    }

    public HPPCRIV(final HPPCRIV riv) {
        super(riv);
        size = riv.size;
    }

    public HPPCRIV(final VectorElement[] points, final int size) {
        this(size);
        for (final VectorElement point : points)
            put(point.index(), point.value());
    }

    public HPPCRIV(final RIV riv) {
        this(riv.size());
        riv.forEach(this::put);
    }

    @Override
    public boolean contains(final int index) {
        return super.containsKey(index);
    }

    @Override
    public HPPCRIV copy() {
        return new HPPCRIV(this);
    }

    @Override
    public int count() {
        return super.size();
    }

    @Override
    public HPPCRIV destructiveAdd(final RIV other) {
        for (final int i : other.keyArr()) {
        	double v = other.get(i);
        	if (!Util.doubleEquals(v, 0))
        		addTo(i, v);
        }
        return this;
    }

    @Override
    public HPPCRIV destructiveAdd(final RIV...rivs) {
        for (int i = 0; i < size; i++) {
            double v = get(i);
            final double vv = v;
            for (final RIV riv : rivs)
                v += riv.get(i);
            if (!Util.doubleEquals(v, 0))
                put(i, v);
            else if (!Util.doubleEquals(vv, 0))
                remove(i);
        }
        return this;
    }

    @Override
    public HPPCRIV destructiveSub(final RIV other) {
        for (final int i : other.keyArr())
            addTo(i, -other.get(i));
        return this;
    }

    @Override
    public HPPCRIV destructiveSub(final RIV...rivs) {
        for (int i = 0; i < size; i++) {
            double v = get(i);
            for (final RIV riv : rivs)
                v -= riv.get(i);
            if (v == 0)
                remove(i);
            else
                put(i, v);
        }
        return this;
    }

    @Override
    public HPPCRIV destructiveDiv(final double scalar) {
        forEach((IntDoubleProcedure) (k, v) -> put(k, v / scalar));
        return this;
    }
    
    @Override
    public boolean equals(final Object other) {
    	return RIVs.equals(this, other);
    }
    
    /* 
    public boolean equals(final HPPCRIV other) {
    	return size == other.size && super.equals(other);
    }
    */

    @Override
    public double get(final int index) throws IndexOutOfBoundsException {
        if (size <= index || index < 0)
            throw new IndexOutOfBoundsException(
                    index + " is outside the bounds of this RIV");
        return getOrDefault(index, 0);
    }

    @Override
    public IntStream keyStream() {
        return Arrays.stream(keyArr());
    }

    @Override
    public HPPCRIV destructiveMult(final double scalar) {
        forEach((IntDoubleProcedure) (k, v) -> put(k, v * scalar));
        return this;
    }

    private static void permute(final VectorElement[] points,
            final int[] permutation) {
        for (final VectorElement point : points)
            point.destructiveSet(permutation[point.index()]);
    }

    @Override
    public HPPCRIV permute(final Permutations permutations, final int times) {
        if (times == 0)
            return this;
        final int[] perm = (times > 0)
                ? permutations.left
                : permutations.right;
        final int t = Math.abs(times);
        final VectorElement[] points = points();
        for (int i = 0; i < t; i++)
            permute(points, perm);
        Arrays.sort(points);
        return new HPPCRIV(points, size);
    }

    @Override
    public VectorElement[] points() {
        final VectorElement[] points = new VectorElement[count()];
        final AtomicInteger c = new AtomicInteger();
        forEach((IntDoubleProcedure) (k, v) -> points[c.getAndIncrement()] =
                VectorElement.elt(k, v));
        Arrays.sort(points);
        return points;
    }

    @Override
    public Stream<VectorElement> pointStream() {
        return Arrays.stream(points());
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public DoubleStream valStream() {
        return Arrays.stream(valArr());
    }

    @Override
    public HPPCRIV destructiveRemoveZeros() {
        removeAll((IntDoublePredicate) (k, v) -> Util.doubleEquals(v, 0.0));
        return this;
    }

    @Override
    public int hashCode() {
        return RIVs.hashcode(this);
    }

    @Override
    public int[] keyArr() {
        int[] keys = new int[count()];
        for (int i = 0, j = 0; i < size; i++)
        	if (!Util.doubleEquals(get(i), 0))
        		keys[j++] = i;
        return keys;
    }

    @Override
    public double[] valArr() {
    	double[] vals = new double[count()];
    	int c = 0;
    	for (int k : keyArr())
    		vals[c++] = get(k);
    	return vals;
    }

    @Override
    public void forEach(final IntDoubleConsumer fun) {
    	IntDoubleProcedure f = (a, b) -> {
    		fun.accept(a, b);
    	};
        super.forEach(f);
    }
    
    public String toString() {
    	return RIVs.toString(this);
    }
}
