package rivet.core.arraylabels;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import rivet.core.exceptions.SizeMismatchException;
import rivet.core.vectorpermutations.Permutations;

public final class RIV implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 7570655472298563946L;
    //Values
    private final VectorElement[] points;
    private final int size;

    //Constructors
    public RIV(final RIV riv) {
        points = riv.points;
        size = riv.size;
    }
    public RIV(final int size) {points = new VectorElement[0]; this.size = size;}
    public RIV(final VectorElement[] points, final int size) { this.points = points; this.size = size;}
    public RIV(final int[] keys, final double[] vals, final int size) {
        this.size = size;
        final int l = keys.length;
        if (l != vals.length) throw new IndexOutOfBoundsException("Different quantity keys than values!");
        final VectorElement[] elts = new VectorElement[l];
        for (int i = 0; i < l; i++)
            elts[i] = VectorElement.elt(keys[i], vals[i]);
        Arrays.sort(elts, VectorElement::compare);
        points = elts;
    }

    //Methods
    public int size()  {return size;}
    public int count() {return points.length;}

    public Stream<VectorElement> stream() {return Arrays.stream(points);}
    public IntStream keyStream() {return stream().mapToInt(VectorElement::index);}
    public DoubleStream valStream() {return stream().mapToDouble(VectorElement::value);}

    public RIV map(final UnaryOperator<VectorElement> fun) {
        return new RIV(
                stream().map(fun)
                    .filter(ve -> !ve.contains(0))
                    .toArray(VectorElement[]::new),
                size);
    }
    public RIV mapKeys(final IntUnaryOperator fun) {
        return map((ve) -> VectorElement.elt(
                            fun.applyAsInt(ve.index()),
                            ve.value()));
    }
    public RIV mapVals(final DoubleUnaryOperator fun) {
        return map((ve) -> VectorElement.elt(
                            ve.index(),
                            fun.applyAsDouble(ve.value())));
    }

    public int[] keys() {return keyStream().toArray();}
    public double[] vals() {return valStream().toArray();}

    public boolean contains (final int index) {return keyStream().anyMatch((k) -> k == index);}

    private boolean sameKeys(final RIV other) {
        final int[] keys = keys();
        return other.keyStream().allMatch((v) -> ArrayUtils.contains(keys, v));
    }

    private boolean sameVals(final RIV other) {
        final double[] vals = vals();
        return other.valStream().allMatch((v) -> ArrayUtils.contains(vals, v));
    }

    public boolean equals(final RIV other) {
        return count() == other.count() &&
                size() == other.size() &&
                sameKeys(other) &&
                sameVals(other);
    }

    private boolean validIndex(final int index) {return 0 <= index && index < size;}

    private int binarySearch(final VectorElement elt) {
        return Arrays.binarySearch(points,  elt, VectorElement::compare);
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

    public double get(final int index) throws IndexOutOfBoundsException {
        return getPoint(index).value();
    }

    private void destructiveSet(final int index, final double value) throws IndexOutOfBoundsException {
        if (validIndex(index)) {
            final VectorElement point = VectorElement.elt(index, value);
            final int i = binarySearch(index);
            if (i < 0)
                ArrayUtils.add(points, ~i, point);
            else
                points[i] = point;
        } else
            throw new IndexOutOfBoundsException("Index " + index + " is outside the bounds of this vector.");
    }

    private RIV removeZeros() {
        final VectorElement[] zeros = stream()
                .filter(ve -> ve.contains(0))
                .toArray(VectorElement[]::new);
        return zeros.length == 0
                ? this
                : new RIV(
                        ArrayUtils.removeElements(points, zeros),
                        size);
    }

    public RIV add(final RIV other) throws SizeMismatchException {
        if (size == other.size()) {
            final RIV res = new RIV(this);
            other.keyStream()
                    .forEach((k) -> res.destructiveSet(k, res.contains(k)
                                                            ? res.get(k) + other.get(k)
                                                            : other.get(k)));
            return res.removeZeros();
        } else
            throw new SizeMismatchException("Target RIV is the wrong size!");
    }

    public RIV subtract(final RIV other) throws SizeMismatchException {
        if (size == other.size()) {
            final RIV res = new RIV(this);
            other.keyStream()
                    .forEach((k) -> res.destructiveSet(k, res.contains(k)
                                                            ? res.get(k) - other.get(k)
                                                            : -other.get(k)));
            return res.removeZeros();
        } else
            throw new SizeMismatchException("Target RIV is the wrong size!");
    }

    public RIV multiply(final double scalar) {return mapVals((v) -> v * scalar);}
    public RIV divideBy(final double scalar) {return mapVals((v) -> v / scalar);}

    public double magnitude() {
        return Math.sqrt(
                valStream()
                    .map((v) -> v * v)
                    .sum());
    }

    public RIV normalize() {
        return divideBy(magnitude());
    }

    private static int[] permuteKeys(IntStream keys, final int[] permutation, final int times) {
        for (int i = 0; i < times; i++)
            keys = keys.map((k) -> permutation[k]);
        return keys.toArray();
    }


    public RIV permute(final Permutations permutations, final int times) {
        if (times == 0)
            return this;
        else
            return new RIV(
                    times > 0
                        ? permuteKeys(keyStream(), permutations.left, times)
                        : permuteKeys(keyStream(), permutations.right, -times),
                    vals(),
                    size);
    }

    @Override
    public String toString() {
        //"(0|1) (1|3) (4|2) 5"
        //"(I|V) (I|V) (I|V) Size"
        return stream().map(VectorElement::toString)
                    .collect(Collectors.joining(" ", "", String.valueOf(size)));
    }


    //Static methods
    public static RIV fromString(final String rivString) {
        final String[] r = rivString.split(" ");
        final int l = r.length - 1;
        final int size = Integer.parseInt(r[l]);
        final VectorElement[] elts = new VectorElement[0];
        for (final String s : r)
            if (s.contains("(") && s.contains("|") && s.contains(")"))
                ArrayUtils.add(elts, VectorElement.fromString(s));
        return new RIV(elts, size);
    }
}
