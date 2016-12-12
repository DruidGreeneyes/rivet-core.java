package rivet.core.labels;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

public final class ArrayRIV implements RIV, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1176979873718129432L;

    public static ArrayRIV empty(final int size, final int k) {
        return new ArrayRIV(size);
    }

    public static ArrayRIV fromString(final String rivString) {
        String[] r = rivString.split(" ");
        final int l = r.length - 1;
        final int size = Integer.parseInt(r[l]);
        r = ArrayUtils.remove(r, l);
        final VectorElement[] elts = new VectorElement[l];
        for (int i = 0; i < l; i++)
            elts[i] = VectorElement.fromString(r[i]);
        return new ArrayRIV(elts, size);
    }

    public static ArrayRIV generateLabel(final int size, final int k,
            final CharSequence word) {
        final long seed = makeSeed(word);
        final int j = k % 2 == 0
                ? k
                : k + 1;
        return new ArrayRIV(makeIndices(size, j, seed), makeVals(j, seed),
                size);
    }

    public static ArrayRIV generateLabel(final int size, final int k,
            final CharSequence source, final int startIndex,
            final int tokenLength) {
        return generateLabel(size,
                             k,
                             Util.safeSubSequence(source,
                                                  startIndex,
                                                  startIndex + tokenLength));
    }

    public static Function<String, ArrayRIV> labelGenerator(final int size,
            final int k) {
        return (word) -> generateLabel(size, k, word);
    }

    public static Function<Integer, ArrayRIV> labelGenerator(
            final String source, final int size, final int k,
            final int tokenLength) {
        return (index) -> generateLabel(size, k, source, index, tokenLength);
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

    private static int[] permuteKeys(IntStream keys, final int[] permutation,
            final int times) {
        for (int i = 0; i < times; i++)
            keys = keys.map((k) -> permutation[k]);
        return keys.toArray();
    }

    private VectorElement[] points;

    private final int size;

    public ArrayRIV(final RIV riv) {
        points = Arrays.stream(riv.points())
                       .map(VectorElement::copy)
                       .toArray(VectorElement[]::new);
        size = riv.size();
    }

    public ArrayRIV(final int size) {
        points = new VectorElement[0];
        this.size = size;
    }

    public ArrayRIV(final int[] keys, final double[] vals, final int size) {
        this.size = size;
        final int l = keys.length;
        if (l != vals.length)
            throw new IndexOutOfBoundsException(
                    "Different quantity keys than values!");
        final VectorElement[] elts = new VectorElement[l];
        for (int i = 0; i < l; i++)
            elts[i] = VectorElement.elt(keys[i], vals[i]);
        Arrays.sort(elts, VectorElement::compare);
        points = elts;
        removeZeros();
    }

    public ArrayRIV(final VectorElement[] points, final int size) {
        this.points = ArrayUtils.clone(points);
        Arrays.sort(points);
        this.size = size;
    }

    /**
     * @Override public ArrayRIV add(final RIV other) { return
     *           copy().destructiveAdd(other) .removeZeros(); }
     *
     *           public ArrayRIV add(final RIV...rivs) { return
     *           copy().destructiveAdd(rivs); }
     **/

    private int binarySearch(final int index) {
        return binarySearch(VectorElement.fromIndex(index));
    }

    private int binarySearch(final VectorElement elt) {
        return Arrays.binarySearch(points, elt, VectorElement::compare);
    }

    @Override
    public boolean contains(final int index) {
        return keyStream().anyMatch((k) -> k == index);
    }

    @Override
    public ArrayRIV copy() {
        return new ArrayRIV(this);
    }

    @Override
    public int count() {
        return points.length;
    }

    @Override
    public ArrayRIV destructiveAdd(final RIV other) {
        other.keyStream()
             .forEach((
                     k) -> destructiveSet(getPoint(k).destructiveAdd(other.get(k))));
        return this;
    }

    @Override
    public ArrayRIV destructiveSub(final RIV other) {
        other.keyStream()
             .forEach(k -> destructiveSet(getPoint(k).destructiveSub(other.get(k))));
        return this;
    }

    private void destructiveSet(final VectorElement elt)
            throws IndexOutOfBoundsException {
        if (validIndex(elt.index())) {
            final int i = binarySearch(elt);
            if (i < 0)
                points = ArrayUtils.add(points, ~i, elt);
            else
                points[i] = elt;
        } else
            throw new IndexOutOfBoundsException(
                    "Index " + elt.index() + " is outside the bounds of this vector.");
    }

    /*
     * @Override public ArrayRIV divide(final double scalar) { return
     * mapVals((v) -> v / scalar); }
     */

    @Override
    public boolean equals(final Object other) {
        return RIVs.equals(this, other);
    }

    @Override
    public boolean equals(final RIV other) {
        return size() == other.size() && Arrays.equals(points, other.points());
    }

    @Override
    public double get(final int index) throws IndexOutOfBoundsException {
        return getPoint(index).value();
    }

    private VectorElement getPoint(final int index)
            throws IndexOutOfBoundsException {
        if (validIndex(index)) {
            final int i = binarySearch(index);
            return i < 0
                    ? VectorElement.fromIndex(index)
                    : points[i];
        } else
            throw new IndexOutOfBoundsException(
                    "Index " + index + " is outside the bounds of this vector.");
    }

    @Override
    public IntStream keyStream() {
        return stream().mapToInt(VectorElement::index);
    }

    @Override
    public double magnitude() {
        return Math.sqrt(valStream().map((v) -> v * v)
                                    .sum());
    }

    /*
     * protected ArrayRIV mapVals(final DoubleUnaryOperator fun) { return new
     * ArrayRIV(keyStream().toArray(), valStream().map(fun) .toArray(),
     * size).destructiveRemoveZeros(); }
     */

    /*
     * @Override public ArrayRIV multiply(final double scalar) { return
     * mapVals((v) -> v * scalar); }
     */

    /*
     * @Override public ArrayRIV normalize() { return divide(magnitude()); }
     */

    @Override
    public ArrayRIV permute(final Permutations permutations, final int times) {
        if (times == 0)
            return this;
        final IntStream keys = keyStream();
        return new ArrayRIV(times > 0
                ? permuteKeys(keys, permutations.left, times)
                : permuteKeys(keys, permutations.right, -times),
                valStream().toArray(), size);
    }

    @Override
    public VectorElement[] points() {
        return points;
    }

    @Override
    public Stream<VectorElement> pointStream() {
        return Arrays.stream(points);
    }

    @Override
    public ArrayRIV removeZeros() {
        final VectorElement[] elts = stream().filter(ve -> !ve.contains(0))
                                             .toArray(VectorElement[]::new);
        if (elts.length == count())
            return this;
        else
            return new ArrayRIV(elts, size);
    }

    @Override
    public int size() {
        return size;
    }

    public Stream<VectorElement> stream() {
        return Arrays.stream(points);
    }

    /*
     * @Override public ArrayRIV subtract(final RIV other) throws
     * SizeMismatchException { return copy().destructiveSub(other)
     * .removeZeros(); }
     */

    @Override
    public int hashCode() {
        return keyStream().sum();
    }

    @Override
    public String toString() {
        // "0|1 1|3 4|2 5"
        // "I|V I|V I|V Size"
        return stream().map(VectorElement::toString)
                       .collect(Collectors.joining(" ",
                                                   "",
                                                   " " + String.valueOf(size)));
    }

    private boolean validIndex(final int index) {
        return 0 <= index && index < size;
    }

    @Override
    public DoubleStream valStream() {
        return stream().mapToDouble(VectorElement::value);
    }

    @Override
    public ArrayRIV destructiveRemoveZeros() {
        for (int i = 0; i < points.length; i++)
            if (points[i].contains(0)) {
                points = ArrayUtils.remove(points, i);
                i--;
            }
        return this;
    }

    @Override
    public ArrayRIV destructiveAdd(final RIV...rivs) {
        for (int i = 0; i < size; i++)
            for (final RIV riv : rivs)
                getPoint(i).destructiveAdd(riv.get(i));
        return this;
    }

    @Override
    public RIV destructiveSub(final RIV...rivs) {
        for (int i = 0; i < size; i++)
            for (final RIV riv : rivs)
                getPoint(i).destructiveSub(riv.get(i));
        return this;
    }

    @Override
    public RIV destructiveDiv(final double scalar) {
        Arrays.stream(points)
              .forEach(elt -> elt.destructiveDiv(scalar));
        return this;
    }

    @Override
    public RIV destructiveMult(final double scalar) {
        Arrays.stream(points)
              .forEach(elt -> elt.destructiveMult(scalar));
        return this;
    }

    @Override
    public int[] keyArr() {
        final int[] keys = new int[points.length];
        for (int i = 0; i < points.length; i++)
            keys[i] = points[i].index();
        return keys;
    }

    @Override
    public double[] valArr() {
        final double[] vals = new double[points.length];
        for (int i = 0; i < points.length; i++)
            vals[i] = points[i].value();
        return vals;
    }
}
