package rivet.core.labels;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableDouble;

import rivet.core.exceptions.SizeMismatchException;
import rivet.core.util.IntDoubleConsumer;
import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

/**
 * Implementation of RIV that uses ConcurrentHashMap<Integer, Double> to store
 * data. Has proven to be significantly faster than array-based representations
 * of RIVs when doing vector arithmetic.
 *
 * @author josh
 */
public final class MapRIV extends ConcurrentHashMap<Integer, MutableDouble>
        implements RIV {

    /**
     * CEREAL
     */
    private static final long serialVersionUID = 350977843775988038L;

    public static MapRIV empty(final int size) {
        return new MapRIV(size);
    }

    /**
     * @param rivString
     *            : A string representation of a RIV, generally got by calling
     *            RIV.toString().
     * @return a MapRIV
     */
    public static MapRIV fromString(final String rivString) {
        String[] pointStrings = rivString.split(" ");
        final int last = pointStrings.length - 1;
        final int size = Integer.parseInt(pointStrings[last]);
        pointStrings = Arrays.copyOf(pointStrings, last);
        final ConcurrentHashMap<Integer, MutableDouble> elts =
                new ConcurrentHashMap<>();
        for (final String s : pointStrings) {
            final String[] elt = s.split("\\|");
            if (elt.length != 2)
                throw new IndexOutOfBoundsException(
                        "Wrong number of partitions: " + s);
            else
                elts.put(Integer.parseInt(elt[0]),
                         new MutableDouble(Double.parseDouble(elt[1])));
        }
        return new MapRIV(elts, size).destructiveRemoveZeros();
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
    public static MapRIV generateLabel(final int size, final int k,
            final CharSequence word) {
        final long seed = makeSeed(word);
        final int j = k % 2 == 0
                ? k
                : k + 1;
        return new MapRIV(makeIndices(size, j, seed), makeVals(j, seed), size);
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
    public static MapRIV generateLabel(final int size, final int k,
            final CharSequence source, final int startIndex,
            final int tokenLength) {
        return generateLabel(size,
                             k,
                             Util.safeSubSequence(source,
                                                  startIndex,
                                                  startIndex + tokenLength));
    }

    /**
     * GenerateLabel, enclosed in a lambda statement.
     *
     * @param size
     * @param k
     * @return
     */
    public static Function<String, MapRIV> labelGenerator(final int size,
            final int k) {
        return (word) -> generateLabel(size, k, word);
    }

    /**
     * GenerateLabel, enclosed in a lambda statement.
     *
     * @param size
     * @param k
     * @return
     */
    public static Function<Integer, MapRIV> labelGenerator(final String source,
            final int size, final int k, final int tokenLength) {
        return (index) -> generateLabel(size, k, source, index, tokenLength);
    }

    /**
     * @param size
     * @param count
     * @param seed
     * @return an array of count random integers between 0 and size
     */
    static int[] makeIndices(final int size, final int count, final long seed) {
        return Util.randInts(size, count, seed)
                   .toArray();
    }

    /**
     * @param word
     * @return a probably-unique long, used to seed java's Random.
     */
    static long makeSeed(final CharSequence word) {
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
    static double[] makeVals(final int count, final long seed) {
        final double[] l = new double[count];
        for (int i = 0; i < count; i += 2) {
            l[i] = 1;
            l[i + 1] = -1;
        }
        return Util.shuffleDoubleArray(l, seed);
    }

    private static int[] permuteKeys(IntStream keys, final int[] permutation,
            final int times) {
        for (int i = 0; i < times; i++)
            keys = keys.map((k) -> permutation[k]);
        return keys.toArray();
    }

    /**
     * The dimensionality of this riv.
     */
    private final int size;

    public MapRIV(final ConcurrentHashMap<Integer, MutableDouble> points,
            final int size) {
        super(points);
        this.size = size;
    }

    public MapRIV(final int size) {
        super();
        this.size = size;
    }

    public MapRIV(final int[] keys, final double[] vals, final int size) {
        super();
        this.size = size;
        final int l = keys.length;
        if (l != vals.length)
            throw new SizeMismatchException(
                    "Different quantity keys than values!");
        for (int i = 0; i < l; i++)
            put(keys[i], new MutableDouble(vals[i]));
    }

    public MapRIV(final MapRIV riv) {
        super(riv);
        size = riv.size;
    }

    private void addPoint(final Integer index, final MutableDouble value) {
        compute(index, (i, v) -> {
            v.add(value);
            return v;
        });
    }

    private void addPoint(final int index, final double value) {
        compute(index, (i, v) -> {
            v.add(value);
            return v;
        });
    }

    /*
     * private void assertSizeMatch(final RIV other, final String message)
     * throws SizeMismatchException { if (size != other.size()) throw new
     * SizeMismatchException(message); }
     */

    private void assertValidIndex(final int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(
                    "Index " + index + " is outside the bounds of this vector.");
    }

    @Override
    public boolean contains(final int index) {
        return containsKey(index);
    }

    @Override
    public MapRIV copy() {
        return new MapRIV(this);
    }

    @Override
    public int count() {
        return super.size();
    }

    public MapRIV destructiveAdd(final MapRIV other)
            throws SizeMismatchException {
        other.forEach((BiConsumer<Integer, MutableDouble>) this::addPoint);
        return this;
    }

    @Override
    public MapRIV destructiveAdd(final RIV other) throws SizeMismatchException {
        // assertSizeMatch(other, "Cannot add rivs of mismatched sizes.");
        other.forEach(this::addPoint);
        return this;
    }

    /**
     * An optimized, destructive, element-wise multiplier; do not use when
     * you'll have to reference the original structure later.
     *
     * @param scalar
     * @return multiplies every element in this by scalar, then returns this.
     */
    @Override
    public MapRIV destructiveMult(final double scalar) {
        replaceAll((k, v) -> {
            v.setValue(v.getValue() * scalar);
            return v;
        });
        return this;
    }

    @Override
    public MapRIV destructiveRemoveZeros() {
        for (final int i : new HashSet<>(keySet()))
            compute(i, (k, v) -> Util.doubleEquals(v.getValue(), 0)
                    ? null
                    : v);
        return this;
    }

    public MapRIV destructiveSub(final MapRIV other)
            throws SizeMismatchException {
        other.forEach((BiConsumer<Integer, MutableDouble>) this::subtractPoint);
        return this;
    }

    @Override
    public MapRIV destructiveSub(final RIV other) throws SizeMismatchException {
        // assertSizeMatch(other, "Cannot subtract rivs of mismatched sizes.");
        other.forEach(this::subtractPoint);
        return this;
    }

    /*
     * @Override public MapRIV divide(final double scalar) { return
     * copy().destructiveMult(1 / scalar); }
     */

    @Override
    public boolean equals(final Object other) {
        return RIVs.equals(this, other);
    }

    public boolean equals(final MapRIV other) {
        return size == other.size() && super.equals(other);
    }

    @Override
    public boolean equals(final RIV other) {
        if (other.getClass()
                 .equals(MapRIV.class))
            return equals((MapRIV) other);
        else
            return size == other.size()
                   && Arrays.deepEquals(points(), other.points());
    }

    public double getOrDefault(final int index, final double otherVal) {
        final MutableDouble v = super.get(index);
        if (null == v)
            return otherVal;
        return v.getValue();
    }

    @Override
    public double get(final int index) throws IndexOutOfBoundsException {
        assertValidIndex(index);
        return getOrDefault(index, 0.0);
    }

    @Override
    public IntStream keyStream() {
        return keySet().stream()
                       .mapToInt(x -> x);
    }

    /*
     * @Override public double magnitude() { return Math.sqrt(valStream().map(x
     * -> x * x) .sum()); }
     *
     * @Override public MapRIV multiply(final double scalar) { return
     * copy().destructiveMult(scalar); }
     *
     * @Override public MapRIV normalize() { return divide(magnitude()); }
     */

    @Override
    public MapRIV permute(final Permutations permutations, final int times) {
        if (times == 0)
            return this;
        else
            return new MapRIV(times > 0
                    ? permuteKeys(keyStream(), permutations.left, times)
                    : permuteKeys(keyStream(), permutations.right, -times),
                    valStream().toArray(), size);
    }

    /*
     * @Override public MapRIV removeZeros() { final ConcurrentHashMap<Integer,
     * Double> map = entrySet().stream() .filter(e -> !Util.doubleEquals(0,
     * e.getValue())) .collect(ConcurrentHashMap::new, (i, e) ->
     * i.put(e.getKey(), e.getValue()), ConcurrentHashMap::putAll); return new
     * MapRIV(map, size); }
     */

    @Override
    public int size() {
        return size;
    }

    /**
     * @return all index/value pairs in this, as a stream
     */
    public Stream<Entry<Integer, Double>> stream() {
        return entrySet().stream()
                         .map(e -> new AbstractMap.SimpleImmutableEntry<>(
                                 e.getKey(), e.getValue()
                                              .getValue()));
    }

    /**
     * An optimized version of subtract() for use when adding MapRIVs to
     * eachother.
     *
     * @param other
     *            : A MapRIV of the same size as this one.
     * @return this - other
     * @throws SizeMismatchException
     */
    public MapRIV subtract(final MapRIV other) {
        return copy().destructiveSub(other)
                     .destructiveRemoveZeros();
    }
    /*
     * @Override public MapRIV subtract(final RIV other) throws
     * SizeMismatchException { return copy().destructiveSub(other)
     * .destructiveRemoveZeros(); }
     */

    private void subtractPoint(final int index, final double value) {
        addPoint(index, -value);
    }

    private void subtractPoint(final Integer index, final MutableDouble value) {
        compute(index, (i, v) -> {
            v.subtract(value);
            return v;
        });
    }

    /**
     * Returns the sum of the keys of this RIV. Pretty much guaranteed to not be
     * unique.
     */
    @Override
    public int hashCode() {
        int sum = 0;
        final double[] vals = valArr();
        for (int i = 0; i < vals.length; i++)
            sum += vals[i] * (31 ^ (vals.length - 1 - i));
        return sum;
    }

    @Override
    public String toString() {
        // "0|1 1|3 4|2 5"
        // "I|V I|V I|V Size"
        return stream().sorted((e1, e2) -> Integer.compare(e1.getKey(),
                                                           e2.getKey()))
                       .map((e) -> String.format("%d|%f",
                                                 e.getKey(),
                                                 e.getValue()))
                       .collect(Collectors.joining(" ",
                                                   "",
                                                   " " + String.valueOf(size)));
    }

    @Override
    public DoubleStream valStream() {
        return values().stream()
                       .mapToDouble(x -> x.getValue());
    }

    @Override
    public MapRIV destructiveAdd(final RIV...rivs) {
        for (final RIV riv : rivs)
            destructiveAdd(riv);
        return this;
    }

    @Override
    public MapRIV destructiveSub(final RIV...rivs) {
        for (final RIV riv : rivs)
            destructiveSub(riv);
        return this;
    }

    @Override
    public MapRIV destructiveDiv(final double scalar) {
        replaceAll((k, v) -> {
            v.setValue(v.getValue() / scalar);
            return v;
        });
        return this;
    }

    @Override
    public Stream<VectorElement> pointStream() {
        return stream().map(VectorElement::elt);
    }

    @Override
    public int[] keyArr() {
        return ArrayUtils.toPrimitive(keySet().toArray(new Integer[count()]));
    }

    @Override
    public double[] valArr() {
        return ArrayUtils.toPrimitive(values().toArray(new Double[count()]));
    }

    @Override
    public VectorElement[] points() {
        final VectorElement[] points = new VectorElement[count()];
        final AtomicInteger c = new AtomicInteger();
        forEach(10000,
                (a, b) -> points[c.getAndIncrement()] =
                        VectorElement.elt(a, b.getValue()));
        Arrays.sort(points);
        return points;
    }

    @Override
    public void forEach(final IntDoubleConsumer fun) {
        final BiConsumer<Integer, MutableDouble> f =
                (i, v) -> fun.accept(i, v.getValue());
        super.forEach(f);
    }
}
