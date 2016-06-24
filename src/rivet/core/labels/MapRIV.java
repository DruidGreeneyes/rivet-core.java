package rivet.core.labels;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import rivet.core.exceptions.SizeMismatchException;
import rivet.core.util.Counter;
import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

public class MapRIV implements Serializable, RandomIndexVector {

    /**
     *
     */
    private static final long serialVersionUID = 350977843775988038L;

    public static MapRIV fromString(final String rivString) {
        String[] pointStrings = rivString.split(" ");
        final int last = pointStrings.length - 1;
        final int size = Integer.parseInt(pointStrings[last]);
        pointStrings = ArrayUtils.remove(pointStrings, last);
        final HashMap<Integer, Double> elts = new HashMap<>();
        for (final String s : pointStrings) {
            final String[] elt = s.split("\\|");
            if (elt.length != 2)
                throw new IndexOutOfBoundsException(
                        "Wrong number of partitions: " + s);
            else
                elts.put(Integer.parseInt(elt[0]), Double.parseDouble(elt[1]));
        }
        return new MapRIV(elts, size).removeZeros();
    }

    public static MapRIV generateLabel(final int size, final int k,
            final CharSequence word) {
        final long seed = makeSeed(word);
        final int j = k % 2 == 0 ? k : k + 1;
        return new MapRIV(makeIndices(size, j, seed), makeVals(j, seed), size);
    }

    public static MapRIV generateLabel(final int size, final int k,
            final CharSequence source, final int startIndex,
            final int tokenLength) {
        return generateLabel(size, k, Util.safeSubSequence(source, startIndex,
                startIndex + tokenLength));
    }

    public static Function<String, MapRIV> labelGenerator(final int size,
            final int k) {
        return (word) -> generateLabel(size, k, word);
    }

    public static Function<Integer, MapRIV> labelGenerator(final String source,
            final int size, final int k, final int tokenLength) {
        return (index) -> generateLabel(size, k, source, index, tokenLength);
    }

    static int[] makeIndices(final int size, final int count, final long seed) {
        return Util.randInts(size, count, seed).toArray();
    }

    static long makeSeed(final CharSequence word) {
        final Counter c = new Counter();
        return word.chars().mapToLong((ch) -> ch * (long) Math.pow(10, c.inc()))
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

    private final int size;

    private final HashMap<Integer, Double> points;

    public MapRIV(final HashMap<Integer, Double> points, final int size) {
        this.points = new HashMap<>(points);
        this.size = size;
    }

    public MapRIV(final int size) {
        this.size = size;
        points = new HashMap<>();
    }

    public MapRIV(final int[] keys, final double[] vals, final int size) {
        this.size = size;
        final int l = keys.length;
        if (l != vals.length)
            throw new SizeMismatchException(
                    "Different quantity keys than values!");
        points = new HashMap<>();
        for (int i = 0; i < l; i++)
            points.put(keys[i], vals[i]);
    }

    public MapRIV(final MapRIV riv) {
        size = riv.size;
        points = new HashMap<>(riv.points);
    }

    public MapRIV(final Set<Entry<Integer, Double>> points, final int size) {
        this.points = new HashMap<>();
        points.forEach(p -> this.points.put(p.getKey(), p.getValue()));
        this.size = size;
    }

    private MapRIV(final VectorElement[] points, final int size) {
        this.points = new HashMap<>();
        Arrays.stream(points)
                .forEach(p -> this.points.put(p.index(), p.value()));
        this.size = size;
    }

    @Override
    public MapRIV add(final RandomIndexVector other)
            throws SizeMismatchException {
        final MapRIV res = copy();
        other.keyStream().forEach((k) -> res.points.put(k,
                res.contains(k) ? res.get(k) + other.get(k) : other.get(k)));
        return res.removeZeros();
    }

    @Override
    public boolean contains(final int index) {
        return points.containsKey(index);
    }

    @Override
    public MapRIV copy() {
        return new MapRIV(this);
    }

    @Override
    public int count() {
        return points.size();
    }

    @Override
    public MapRIV divide(final double scalar) {
        return mapVals((v) -> v / scalar);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other)
            return true;
        else if (!ArrayUtils.contains(other.getClass().getInterfaces(),
                RandomIndexVector.class))
            return false;
        else
            return equalsRIV((RandomIndexVector) other);
    }

    public boolean equalsRIV(final RandomIndexVector other) {
        return size() == other.size()
                && Arrays.equals(points(), other.points());
    }

    @Override
    public double get(final int index) throws IndexOutOfBoundsException {
        if (validIndex(index))
            return points.getOrDefault(index, 0.0);
        else
            throw new IndexOutOfBoundsException("Index " + index
                    + " is outside the bounds of this vector.");
    }

    @Override
    public IntStream keyStream() {
        return points.keySet().stream().mapToInt(x -> x);
    }

    public MapRIV map(final UnaryOperator<VectorElement> fun) {
        return new MapRIV(stream().map(VectorElement::elt).map(fun)
                .filter(e -> e.value() != 0).toArray(VectorElement[]::new),
                size);
    }

    @Override
    public MapRIV mapKeys(final IntUnaryOperator fun) {
        return new MapRIV(keyStream().map(fun).toArray(), vals(), size)
                .removeZeros();
    }

    @Override
    public MapRIV mapVals(final DoubleUnaryOperator fun) {
        return new MapRIV(keys(), valStream().map(fun).toArray(), size)
                .removeZeros();
    }

    @Override
    public MapRIV multiply(final double scalar) {
        return mapVals((v) -> v * scalar);
    }

    @Override
    public MapRIV normalize() {
        return divide(magnitude());
    }

    @Override
    public MapRIV permute(final Permutations permutations, final int times) {
        if (times == 0)
            return this;
        else {
            final IntStream keys = keyStream();
            return new MapRIV(
                    times > 0 ? permuteKeys(keys, permutations.left, times)
                            : permuteKeys(keys, permutations.right, -times),
                    vals(), size);
        }
    }

    @Override
    public VectorElement[] points() {
        return stream().map(VectorElement::elt).sorted(VectorElement::compare)
                .toArray(VectorElement[]::new);
    }

    private MapRIV removeZeros() {
        return new MapRIV(
                stream().filter((e) -> !Util.doubleEquals(0, e.getValue()))
                        .collect(Collectors.toSet()),
                size);
    }

    @Override
    public int size() {
        return size;
    }

    public Stream<Entry<Integer, Double>> stream() {
        return points.entrySet().stream();
    }

    @Override
    public MapRIV subtract(final RandomIndexVector other)
            throws SizeMismatchException {
        final MapRIV res = copy();
        other.keyStream().forEach((k) -> res.points.put(k,
                res.contains(k) ? res.get(k) - other.get(k) : -other.get(k)));
        return res.removeZeros();
    }

    @Override
    public String toString() {
        // "0|1 1|3 4|2 5"
        // "I|V I|V I|V Size"
        return stream()
                .sorted((e1, e2) -> Integer.compare(e1.getKey(), e2.getKey()))
                .map((e) -> String.format("%d|%f", e.getKey(), e.getValue()))
                .collect(Collectors.joining(" ", "",
                        " " + String.valueOf(size)));
    }

    private boolean validIndex(final int index) {
        return index < size && index >= 0;
    }

    @Override
    public DoubleStream valStream() {
        return points.values().stream().mapToDouble(x -> x);
    }

}
