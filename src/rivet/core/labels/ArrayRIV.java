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

import rivet.core.arraylabels.VectorElement;
import rivet.core.exceptions.SizeMismatchException;
import rivet.core.util.Counter;
import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

public class ArrayRIV implements RandomIndexVector, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1176979873718129432L;
    private final VectorElement[] points;
    private final int size;

    public ArrayRIV(final ArrayRIV riv) {
        points = riv.points;
        size = riv.size;
    }

    public ArrayRIV(final int size) {
        points = new VectorElement[0];
        this.size = size;
    }

    public ArrayRIV(final VectorElement[] points, final int size) {
        this.points = points;
        this.size = size;
    }

    public ArrayRIV(final int[] keys, final double[] vals, final int size) {
        this.size = size;
        final int l = keys.length;
        if (l != vals.length)
            throw new IndexOutOfBoundsException("Different quantity keys than values!");
        final VectorElement[] elts = new VectorElement[l];
        for (int i = 0; i < l; i++)
            elts[i] = VectorElement.elt(keys[i], vals[i]);
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
                    .collect(Collectors.joining(" ", "", String.valueOf(size)));
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

    private boolean sameKeys(final RandomIndexVector other) {
        final int[] keys = keys();
        return other.keyStream().allMatch((k) -> ArrayUtils.contains(keys, k));
    }

    private boolean sameVals(final RandomIndexVector other) {
        final double[] vals = vals();
        return other.valStream().allMatch((v) -> ArrayUtils.contains(vals, v));
    }

    @Override
    public boolean equals(final RandomIndexVector other) {
        if (count() == other.count() && size() == other.size() && sameKeys(other) && sameVals(other))
            return true;
        else
            return false;
    }

    private boolean validIndex(final int index) {
        return 0 <= index && index < size;
    }

    private int binarySearch(final VectorElement elt) {
        return Arrays.binarySearch(points, elt, VectorElement::compare);
    }

    private int binarySearch(final int index) {
        return binarySearch(VectorElement.partial(index));
    }

    private VectorElement getPoint(final int index) throws IndexOutOfBoundsException {
        if (validIndex(index)) {
            final int i = binarySearch(index);
            return i < 0 ? VectorElement.partial(index) : points[i];
        } else
            throw new IndexOutOfBoundsException("Index " + index + " is outside the bounds of this vector.");
    }

    @Override
    public double get(final int index) throws IndexOutOfBoundsException {
        return getPoint(index).value();
    }

    private void destructiveSet(final int index, final double value) throws IndexOutOfBoundsException {
        if (validIndex(index)) {
            final int i = binarySearch(index);
            final int a = i < 0 ? ~i : i;
            points[a] = VectorElement.elt(index, value);
        } else
            throw new IndexOutOfBoundsException("Index " + index + " is outside the bounds of this vector.");
    }
    
    private ArrayRIV removeZeros() {
        VectorElement[] zeros = stream().filter(ve -> ve.contains(0)).toArray(VectorElement[]::new);
        if (zeros.length == 0)
            return this;
        else
            return new ArrayRIV(
                    ArrayUtils.removeElements(points, zeros),
                    size);
    }

    @Override
    public ArrayRIV add(final RandomIndexVector other) throws SizeMismatchException {
        if (size == other.size()) {
            final ArrayRIV res = copy();
            other.keyStream()
                    .forEach((k) -> res.destructiveSet(k, res.contains(k) ? res.get(k) + other.get(k) : other.get(k)));
            return res.removeZeros();
        } else
            throw new SizeMismatchException("Target RIV is the wrong size!");
    }

    @Override
    public ArrayRIV subtract(final RandomIndexVector other) throws SizeMismatchException {
        if (size == other.size()) {
            final ArrayRIV res = copy();
            other.keyStream()
                    .forEach((k) -> res.destructiveSet(k, res.contains(k) ? res.get(k) - other.get(k) : -other.get(k)));
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

    private static double[] makeVals(final int count, final long seed) {
        final double[] l = new double[count];
        for (int i = 0; i < count; i += 2) {
            l[i] = 1;
            l[i + 1] = -1;
        }
        return Util.shuffleDoubleArray(l, seed);
    }

    private static int[] makeIndices(final int size, final int count, final long seed) {
        return Util.randInts(size, count, seed).toArray();
    }

    private static long makeSeed(final CharSequence word) {
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
        return generateLabel(size, k, source.subSequence(startIndex, startIndex + tokenLength));
    }

    public static Function<String, ArrayRIV> labelGenerator(final int size, final int k) {
        return (word) -> generateLabel(size, k, word);
    }

    public static Function<Integer, ArrayRIV> labelGenerator(final String source, final int size, final int k,
            final int tokenLength) {
        return (index) -> generateLabel(size, k, source, index, tokenLength);
    }

    public static ArrayRIV fromString(final String rivString) {
        final String[] r = rivString.split(" ");
        final int l = r.length - 1;
        final int size = Integer.parseInt(r[l]);
        final VectorElement[] elts = new VectorElement[0];
        for (final String s : r)
            if (s.contains("|"))
                ArrayUtils.add(elts, VectorElement.fromString(s));
        return new ArrayRIV(elts, size);
    }
}
