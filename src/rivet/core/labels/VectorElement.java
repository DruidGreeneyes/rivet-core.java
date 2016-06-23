package rivet.core.labels;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

import rivet.core.util.Util;

public final class VectorElement
        implements Serializable, Comparable<VectorElement> {

    /**
     *
     */
    private static final long serialVersionUID = -7264116914157443437L;

    public static int compare(final VectorElement a, final VectorElement b) {
        return a.compareTo(b);
    }

    public static VectorElement elt(final Entry<Integer, Double> entry) {
        return new VectorElement(entry.getKey(), entry.getValue());
    }

    public static VectorElement elt(final int index, final double value) {
        return new VectorElement(index, value);
    }

    public static VectorElement fromIndex(final int index) {
        return new VectorElement(index, 0);
    }

    public static VectorElement fromString(final String eltString) {
        final String[] elt = eltString.split("\\|");
        if (elt.length != 2)
            throw new IndexOutOfBoundsException(
                    "Wrong number of partitions: " + eltString);
        return elt(Integer.parseInt(elt[0]), Double.parseDouble(elt[1]));
    }

    public static VectorElement fromValue(final double value) {
        return new VectorElement(0, value);
    }

    // Static Methods
    public static VectorElement zero() {
        return new VectorElement(0, 0);
    }

    // Values
    private final int index;
    private final double value;

    // Constructors
    public VectorElement(final int index, final double value) {
        this.index = index;
        this.value = value;
    }

    public VectorElement add(final double v) {
        return elt(index, value + v);
    }

    public VectorElement add(final VectorElement p) {
        assertMatch(p);
        return this.add(p.value);
    }

    private void assertMatch(final VectorElement p) {
        if (!this.equals(p))
            throw new IndexOutOfBoundsException(
                    String.format("Point indices do not match! %s != %s",
                            toString(), p.toString()));
    }

    @Override
    public int compareTo(final VectorElement p) {
        return Integer.compare(index, p.index);
    }

    public boolean contains(final int value) {
        return this.value == value;
    }

    public <T, R> R engage(final BiFunction<VectorElement, T, R> fun,
            final T thing) {
        return fun.apply(this, thing);
    }

    // Convenience Methods
    public <T> T engage(final Function<VectorElement, T> fun) {
        return fun.apply(this);
    }

    public boolean equals(final VectorElement p) {
        return index == p.index;
    }

    // Core Methods
    public int index() {
        return index;
    }

    public boolean strictEquals(final VectorElement p) {
        return equals(p) && Util.doubleEquals(value, p.value());
    }

    public VectorElement subtract(final int v) {
        return this.add(-v);
    }

    public VectorElement subtract(final VectorElement p) {
        return this.add(-p.value);
    }

    @Override
    public String toString() {
        return String.format("%d|%f", index, value);
    }

    public double value() {
        return value;
    }

}
