package rivet.core.labels;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import rivet.core.exceptions.SizeMismatchException;
import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

/**
 * Implementation of RIV that uses ConcurrentHashMap<Integer, Double> to store
 * data. Has proven to be significantly faster than array-based representations
 * of RIVs when doing vector arithmetic.
 *
 * @author josh
 */
public final class MapRIV extends ConcurrentHashMap<Integer, Double>
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
        final ConcurrentHashMap<Integer, Double> elts =
                new ConcurrentHashMap<>();
        for (final String s : pointStrings) {
            final String[] elt = s.split("\\|");
            if (elt.length != 2)
                throw new IndexOutOfBoundsException(
                        "Wrong number of partitions: " + s);
            else
                elts.put(Integer.parseInt(elt[0]), Double.parseDouble(elt[1]));
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

    public MapRIV(final ConcurrentHashMap<Integer, Double> points,
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
            put(keys[i], vals[i]);
    }

    public MapRIV(final MapRIV riv) {
        super(riv);
        size = riv.size;
    }

    /**
     * An optimized version of add() for use when adding MapRIVs to eachother.
     *
     * @param other
     *            : A MapRIV of the same size as this one.
     * @return this + other
     * @throws SizeMismatchException
     */
    public MapRIV add(final MapRIV other) throws SizeMismatchException {
        // assertSizeMatch(other, "Cannot add rivs of mismatched sizes.");
        return copy().destructiveAdd(other)
                     .destructiveRemoveZeros();
    }

    /*
     * @Override public MapRIV add(final RIV other) throws SizeMismatchException
     * { assertSizeMatch(other, "Cannot add rivs of mismatched sizes."); return
     * copy().destructiveAdd(other) .destructiveRemoveZeros(); }
     */

    private void addPoint(final int index, final double value) {
        merge(index, value, (a, b) -> a + b);
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

    /**
     * An optimized version of destructiveAdd() for use when adding MapRIVs to
     * eachother.
     *
     * @param other
     *            : A MapRIV of the same size as this one.
     * @return this + other
     * @throws SizeMismatchException
     */
    public MapRIV destructiveAdd(final MapRIV other)
            throws SizeMismatchException {
        // assertSizeMatch(other, "Cannot add rivs of mismatched sizes.");
        other.forEach((i, v) -> merge(i, v, (a, b) -> a + b));
        return this;
    }

    @Override
    public MapRIV destructiveAdd(final RIV other) throws SizeMismatchException {
        // assertSizeMatch(other, "Cannot add rivs of mismatched sizes.");
        for (final VectorElement point : other.points())
            addPoint(point.index(), point.value());
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
        replaceAll((k, v) -> v * scalar);
        return this;
    }

    @Override
    public MapRIV destructiveRemoveZeros() {
        for (final int i : new HashSet<>(keySet()))
            compute(i, (k, v) -> Util.doubleEquals(v, 0)
                    ? null
                    : v);
        return this;
    }

    /**
     * An optimized version of destructiveSub() for use when adding MapRIVs to
     * eachother.
     *
     * @param other
     *            : A MapRIV of the same size as this one.
     * @return this - other
     * @throws SizeMismatchException
     */
    public MapRIV destructiveSub(final MapRIV other)
            throws SizeMismatchException {
        // assertSizeMatch(other, "Cannot subtract rivs of mismatched sizes.");
        other.forEach((i, v) -> subtractPoint(i, v));
        return this;
    }

    @Override
    public MapRIV destructiveSub(final RIV other) throws SizeMismatchException {
        // assertSizeMatch(other, "Cannot subtract rivs of mismatched sizes.");
        for (final VectorElement point : other.points())
            subtractPoint(point.index(), point.value());
        return this;
    }

    /*
     * @Override public MapRIV divide(final double scalar) { return
     * copy().destructiveMult(1 / scalar); }
     */

    @Override
    public boolean equals(final Object other) {
        if (this == other)
            return true;
        else if (!ArrayUtils.contains(other.getClass()
                                           .getInterfaces(),
                                      RIV.class))
            return false;
        else if (other.getClass()
                      .equals(MapRIV.class))
            return equalsMapRIV((MapRIV) other);
        else
            return equalsRIV((RIV) other);
    }

    public boolean equalsMapRIV(final MapRIV other) {
        return size == other.size() && super.equals(other);
    }

    public boolean equalsRIV(final RIV other) {
        return size == other.size()
               && Arrays.deepEquals(points(), other.points());
    }

    @Override
    public double get(final int index) throws IndexOutOfBoundsException {
        assertValidIndex(index);
        return super.getOrDefault(index, 0.0);
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

    @Override
    public VectorElement[] points() {
        return stream().map(VectorElement::elt)
                       .sorted(VectorElement::compare)
                       .toArray(VectorElement[]::new);
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
        return entrySet().stream();
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
        merge(index, value, (a, b) -> a - b);
    }

    /**
     * Returns the sum of the keys of this RIV. Pretty much guaranteed to not be
     * unique.
     */
    @Override
    public int hashCode() {
        return keyStream().sum();
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
                       .mapToDouble(x -> x);
    }

    @Override
    public MapRIV destructiveAdd(final RIV...rivs) {
        IntStream.range(0, size)
                 .parallel()
                 .forEach(i -> merge(i,
                                     Arrays.stream(rivs)
                                           .parallel()
                                           .mapToDouble(riv -> riv.get(i))
                                           .sum(),
                                     (a, b) -> a + b));
        return this;
    }

    @Override
    public MapRIV destructiveSub(final RIV...rivs) {
        IntStream.range(0, size)
                 .parallel()
                 .forEach(i -> merge(i,
                                     Arrays.stream(rivs)
                                           .parallel()
                                           .mapToDouble(riv -> riv.get(i))
                                           .sum(),
                                     (a, b) -> a - b));
        return this;
    }

    @Override
    public MapRIV destructiveDiv(final double scalar) {
        replaceAll((k, v) -> v / scalar);
        return this;
    }
}
