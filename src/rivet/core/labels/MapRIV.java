package rivet.core.labels;

import java.io.Serializable;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import rivet.core.exceptions.SizeMismatchException;
import rivet.core.util.Counter;
import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

public class MapRIV implements Serializable, RandomIndexVector {
    
    /**
     * 
     */
    private static final long serialVersionUID = 350977843775988038L;
    private int size;
    private HashMap<Integer, Double> points;
    
    public MapRIV(MapRIV riv) {
        size = riv.size;
        points = new HashMap<>(riv.points);
    }
    
    public MapRIV(int size) {
        this.size = size;
        points = new HashMap<>();
    }
    
    public MapRIV(HashMap<Integer, Double> points, int size) {
        this.points = new HashMap<>(points);
        this.size = size;
    }
    
    public MapRIV(Set<Entry<Integer, Double>> points, int size) {
        this.points = new HashMap<>();
        points.forEach(p -> this.points.put(p.getKey(), p.getValue()));
        this.size = size;
    }
    
    public MapRIV(int[] keys, double[] vals, int size) {
        this.size = size;
        int l = keys.length;
        if (l != vals.length)
            throw new SizeMismatchException("Different quantity keys than values!");
        points = new HashMap<>();
        for (int i = 0; i < l; i++)
            points.put(keys[i], vals[i]);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int count() {
        return points.size();
    }
    
    public Stream<Entry<Integer, Double>> stream() {
        return points.entrySet().stream();
    }
    
    @Override
    public String toString() {
        //"0|1 1|3 4|2 5"
        //"I|V I|V I|V Size"
        StringBuilder s = new StringBuilder();
        stream().map(e -> String.format("%d|%d ", e.getKey(), e.getValue()))
            .forEach((p) -> s.append(p));
        return s.append(size).toString();
    }

    @Override
    public IntStream keyStream() {
        return points.keySet().stream().mapToInt(x->x);
    }

    @Override
    public DoubleStream valStream() {
        return points.values().stream().mapToDouble(x->x);
    }

    @Override
    public boolean contains(int index) {
        return points.containsKey(index);
    }

    @Override
    public boolean equals(RandomIndexVector other) {
        return size == other.size() &&
               count() == other.count() &&
               keys().equals(other.keys()) &&
               vals().equals(other.vals());
    }
    
    private boolean validIndex(int index) {
        return index < size && index >= 0;
    }

    @Override
    public double get(int index) throws IndexOutOfBoundsException {
        if (validIndex(index))
            return points.getOrDefault(index, 0.0);
        else
            throw new IndexOutOfBoundsException("Index " + index + " is outside the bounds of this vector.");
    }

    @Override
    public MapRIV normalize() {
        return divide(magnitude());
    }

    @Override
    public MapRIV copy() {
        return new MapRIV(this);
    }
    
    private MapRIV removeZeros() {
        keyStream().forEach((k) -> {
            if (get(k) == 0)
                points.remove(k);
        });
        return this;
    }
    
    public MapRIV map(UnaryOperator<Entry<Integer, Double>> fun) {
        return new MapRIV(
                stream()
                    .map(fun)
                    .filter(e -> e.getValue() != 0)
                    .collect(Collectors.toSet()),
                size);
    }

    @Override
    public MapRIV mapKeys(IntUnaryOperator fun) {
        return new MapRIV(
                keyStream().map(fun).toArray(),
                vals(),
                size)
                .removeZeros();
    }

    @Override
    public MapRIV mapVals(DoubleUnaryOperator fun) {
        return new MapRIV(
                keys(),
                valStream().map(fun).toArray(),
                size)
                .removeZeros();
    }

    @Override
    public MapRIV add(RandomIndexVector other) throws SizeMismatchException {
        MapRIV res = this.copy();
        other.keyStream().forEach((k) -> res.points.put(k, res.contains(k) 
                                                            ? res.get(k) + other.get(k)
                                                            : other.get(k)));
        return res.removeZeros();
    }

    @Override
    public MapRIV subtract(RandomIndexVector other) throws SizeMismatchException {
        MapRIV res = this.copy();
        other.keyStream().forEach((k) -> res.points.put(k, res.contains(k)
                                                            ? res.get(k) - other.get(k)
                                                            : -other.get(k)));
        return res.removeZeros();
    }

    @Override
    public MapRIV multiply(double scalar) {
        return mapVals((v) -> v * scalar);
    }

    @Override
    public MapRIV divide(double scalar) {
        return mapVals((v) -> v / scalar);
    }
    
    private static int[] permuteKeys(IntStream keys, final int[] permutation, final int times) {
        for (int i = 0; i < times; i++)
            keys = keys.map((k) -> permutation[k]);
        return keys.toArray();
    }

    @Override
    public MapRIV permute(Permutations permutations, int times) {
        if (times == 0)
            return this;
        else {
            IntStream keys = keyStream();
            return new MapRIV(
                    times > 0
                        ? permuteKeys(keys, permutations.left, times)
                        : permuteKeys(keys, permutations.right, -times),
                    vals(),
                    size);
        }
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

    public static MapRIV generateLabel(final int size, final int k, final CharSequence word) {
        final long seed = makeSeed(word);
        final int j = k % 2 == 0 ? k : k + 1;
        return new MapRIV(makeIndices(size, j, seed), makeVals(j, seed), size);
    }

    public static MapRIV generateLabel(final int size, final int k, final CharSequence source, final int startIndex,
            final int tokenLength) {
        return generateLabel(size, k, source.subSequence(startIndex, startIndex + tokenLength));
    }

    public static Function<String, MapRIV> labelGenerator(final int size, final int k) {
        return (word) -> generateLabel(size, k, word);
    }

    public static Function<Integer, MapRIV> labelGenerator(final String source, final int size, final int k,
            final int tokenLength) {
        return (index) -> generateLabel(size, k, source, index, tokenLength);
    }

    public static MapRIV fromString(final String rivString) {
        String[] pointStrings = rivString.split(" ");
        final int last = pointStrings.length - 1;
        final int size = Integer.parseInt(pointStrings[last]);
        pointStrings = ArrayUtils.remove(pointStrings, last);
        final HashMap<Integer, Double> elts = new HashMap<>();
        for (final String s : pointStrings) {
            String[] elt = s.split("\\|");
            if (elt.length != 2)
                throw new IndexOutOfBoundsException("Wrong number of partitions: " + s);
            else 
                elts.put(Integer.parseInt(elt[0]), Double.parseDouble(elt[1]));
        }
        return new MapRIV(elts, size).removeZeros();
    }

}
