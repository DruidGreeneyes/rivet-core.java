package rivet.core.labels;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import com.carrotsearch.hppc.IntDoubleHashMap;
import com.carrotsearch.hppc.predicates.IntDoublePredicate;
import com.carrotsearch.hppc.procedures.DoubleProcedure;
import com.carrotsearch.hppc.procedures.IntDoubleProcedure;
import com.carrotsearch.hppc.procedures.IntProcedure;

import rivet.core.vectorpermutations.Permutations;

public class HPPCRIV extends IntDoubleHashMap implements RIV {

    private final int size;

    private HPPCRIV(final int size) {
        super();
        this.size = size;
    }

    public HPPCRIV(final int size, final int[] indices, final double[] values) {
        this(size);
        for (int i = 0; i < indices.length; i++)
            put(indices[i], values[i]);
    }

    public HPPCRIV(final HPPCRIV riv) {
        super(riv);
        size = riv.size;
    }

    public HPPCRIV(final int size, final VectorElement[] points) {
        this(size);
        for (final VectorElement point : points)
            put(point.index(), point.value());
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
        for (final int i : other.keyArr())
            addTo(i, other.get(i));
        return this;
    }

    @Override
    public HPPCRIV destructiveAdd(final RIV...rivs) {
        for (final RIV riv : rivs)
            destructiveAdd(riv);
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
        for (final RIV riv : rivs)
            destructiveSub(riv);
        return this;
    }

    @Override
    public HPPCRIV destructiveDiv(final double scalar) {
        forEach((IntDoubleProcedure) (k, v) -> indexReplace(k, v / scalar));
        return this;
    }

    @Override
    public boolean equals(final RIV other) {
        if (other.getClass()
                 .equals(HPPCRIV.class))
            return equals(other);
        else
            return size == other.size()
                   && Arrays.deepEquals(points(), other.points());
    }

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
        forEach((IntDoubleProcedure) (k, v) -> indexReplace(k, v * scalar));
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
        return new HPPCRIV(size, points);
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
        removeAll((IntDoublePredicate) (k, v) -> v == 0);
        return this;
    }

    @Override
    public int hashCode() {
        final AtomicInteger c = new AtomicInteger();
        keys().forEach((IntProcedure) c::addAndGet);
        return c.get();
    }

    @Override
    public int[] keyArr() {
        return keys().toArray();
    }

    @Override
    public double[] valArr() {
        return values().toArray();
    }

}
