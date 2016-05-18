package rivet.core.labels;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import rivet.core.labels.VectorElement;
import rivet.core.exceptions.SizeMismatchException;
import rivet.core.util.Counter;
import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

public class ArrayRIV implements RandomIndexVector, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1176979873718129432L;
    private VectorElement[] points;
    private final int size;

    public ArrayRIV(final ArrayRIV riv) {
        points = ArrayUtils.clone(riv.points);
        size = riv.size;
    }

    public ArrayRIV(final int size) {
        points = new VectorElement[0];
        this.size = size;
    }

    public ArrayRIV(final VectorElement[] points, final int size) {
        this.points = ArrayUtils.clone(points);
        Arrays.sort(points);
        this.size = size;
    }

    public ArrayRIV(final int[] keys, final double[] ds, final int size) {
        this.size = size;
        final int l = keys.length;
        if (l != ds.length)
            throw new IndexOutOfBoundsException("Different quantity keys than values!");
        final VectorElement[] elts = new VectorElement[l];
        for (int i = 0; i < l; i++)
            elts[i] = VectorElement.elt(keys[i], ds[i]);
        Arrays.sort(elts, VectorElement::compare);
        points = elts;
        this.removeZeros();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int count() {
        return points.length;
    }

    public Stream<VectorElement> stream() {
        return Arrays.stream(points);
    }
    
    @Override
    public String toString() {
        //"0|1 1|3 4|2 5"
        //"I|V I|V I|V Size"
        return stream().map(VectorElement::toString)
                    .collect(Collectors.joining(" ", "", " " + String.valueOf(size)));
    }

    @Override
    public IntStream keyStream() {
        return stream().mapToInt(VectorElement::index);
    }

    @Override
    public DoubleStream valStream() {
        return stream().mapToDouble(VectorElement::value);
    }

    @Override
    public boolean contains(final int index) {
        return keyStream().anyMatch((k) -> k == index);
    }
    
    private boolean sameKeys(RandomIndexVector other) {
        return keyStream().allMatch(other::contains);
    }
    
    private boolean sameVals(RandomIndexVector other) {
        return keyStream().mapToObj((k) -> get(k) == other.get(k))
                .noneMatch(b -> b == false);
    }

    @Override
    public boolean equals(final RandomIndexVector other) {
        return count() == other.count() && size() == other.size() && sameKeys(other) && sameVals(other);
    }

    private boolean validIndex(final int index) {
        return 0 <= index && index < size;
    }

    private int binarySearch(final VectorElement elt) {
        return Arrays.binarySearch(points, elt, VectorElement::compare);
    }

    private int binarySearch(final int index) {
        return binarySearch(VectorElement.fromIndex(index));
    }

    private VectorElement getPoint(final int index) throws IndexOutOfBoundsException {
        if (validIndex(index)) {
            final int i = binarySearch(index);
            return i < 0 ? VectorElement.fromIndex(index) : points[i];
        } else
            throw new IndexOutOfBoundsException("Index " + index + " is outside the bounds of this vector.");
    }

    @Override
    public double get(final int index) throws IndexOutOfBoundsException {
        return getPoint(index).value();
    }

    private void destructiveSet(final int index, final double value) throws IndexOutOfBoundsException {
        if (validIndex(index)) {
            VectorElement point = VectorElement.elt(index, value);
            final int i = binarySearch(index);
            if (i < 0)
                points = ArrayUtils.add(points, ~i, point);
            else
                points[i] = point;
        } else
            throw new IndexOutOfBoundsException("Index " + index + " is outside the bounds of this vector.");
    }
    
    private ArrayRIV removeZeros() {
        VectorElement[] elts = stream().filter(ve -> !ve.contains(0)).toArray(VectorElement[]::new);
        if (elts.length == count())
            return this;
        else
            return new ArrayRIV(
                    elts,
                    size);
    }

    @Override
    public ArrayRIV add(final RandomIndexVector other) throws SizeMismatchException {
        if (size == other.size()) {
            final ArrayRIV res = this.copy();
            other.keyStream()
                .forEach((k) -> res.destructiveSet(k, res.get(k) + other.get(k)));
            return res.removeZeros();
        } else
            throw new SizeMismatchException("Target RIV is the wrong size!");
    }

    @Override
    public ArrayRIV subtract(final RandomIndexVector other) throws SizeMismatchException {
        if (size == other.size()) {
            final ArrayRIV res = copy();
            other.keyStream()
                .forEach((k) -> res.destructiveSet(k, res.get(k) - other.get(k)));
            return res.removeZeros();
        } else
            throw new SizeMismatchException("Target RIV is the wrong size!");
    }

    @Override
    public ArrayRIV multiply(final double scalar) {
        return mapVals((v) -> v * scalar);
    }
    
    @Override
    public ArrayRIV divide(final double scalar) {
        return mapVals((v) -> v / scalar);
    }
    
    @Override
    public ArrayRIV normalize() {
        return divide(magnitude());
    }

    @Override
    public ArrayRIV copy() {
        return new ArrayRIV(this);
    }

    public ArrayRIV map(final UnaryOperator<VectorElement> fun) {
        return new ArrayRIV(
                stream().map(fun)
                    .filter(ve -> !ve.contains(0))
                    .toArray(VectorElement[]::new), 
                size);
    }

    @Override
    public ArrayRIV mapKeys(final IntUnaryOperator fun) {
        return new ArrayRIV(keyStream().map(fun).toArray(), vals(), size).removeZeros();
    }

    @Override
    public ArrayRIV mapVals(final DoubleUnaryOperator fun) {
        return new ArrayRIV(keys(), valStream().map(fun).toArray(), size).removeZeros();
    }

    private static int[] permuteKeys(IntStream keys, final int[] permutation, final int times) {
        for (int i = 0; i < times; i++)
            keys = keys.map((k) -> permutation[k]);
        return keys.toArray();
    }

    @Override
    public ArrayRIV permute(final Permutations permutations, final int times) {
        if (times == 0)
            return this;
        final IntStream keys = keyStream();
        return new ArrayRIV(
                times > 0 ? permuteKeys(keys, permutations.left, times) : permuteKeys(keys, permutations.right, -times),
                vals(), size);
    }

    static double[] makeVals(final int count, final long seed) {
        final double[] l = new double[count];
        for (int i = 0; i < count; i += 2) {
            l[i] = 1;
            l[i + 1] = -1;
        }
        return Util.shuffleDoubleArray(l, seed);
    }

    static int[] makeIndices(final int size, final int count, final long seed) {
        return Util.randInts(size, count, seed).toArray();
    }

    static long makeSeed(final CharSequence word) {
        final Counter c = new Counter();
        return word.chars().mapToLong((ch) -> ch * (long) Math.pow(10, c.inc())).sum();
    }

    public static ArrayRIV generateLabel(final int size, final int k, final CharSequence word) {
        final long seed = makeSeed(word);
        final int j = k % 2 == 0 ? k : k + 1;
        return new ArrayRIV(makeIndices(size, j, seed), makeVals(j, seed), size);
    }

    public static ArrayRIV generateLabel(final int size, final int k, final CharSequence source, final int startIndex,
            final int tokenLength) {
        return generateLabel(size, k, Util.safeSubSequence(source, startIndex, startIndex + tokenLength));
    }

    public static Function<String, ArrayRIV> labelGenerator(final int size, final int k) {
        return (word) -> generateLabel(size, k, word);
    }

    public static Function<Integer, ArrayRIV> labelGenerator(final String source, final int size, final int k,
            final int tokenLength) {
        return (index) -> generateLabel(size, k, source, index, tokenLength);
    }

    public static ArrayRIV fromString(final String rivString) {
        String[] r = rivString.split(" ");
        final int l = r.length - 1;
        final int size = Integer.parseInt(r[l]);
        r = ArrayUtils.remove(r, l);
        VectorElement[] elts = new VectorElement[l];
        for (int i = 0; i < l; i++)
            elts[i] = VectorElement.fromString(r[i]);
        return new ArrayRIV(elts, size);
    }
}
