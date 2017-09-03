package com.github.druidgreeneyes.rivet.core.labels;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableDouble;

import com.github.druidgreeneyes.rivet.core.exceptions.SizeMismatchException;
import com.github.druidgreeneyes.rivet.core.util.IntDoubleConsumer;
import com.github.druidgreeneyes.rivet.core.util.Util;
import com.github.druidgreeneyes.rivet.core.vectorpermutations.Permutations;

/**
 * Implementation of RIV that uses ConcurrentHashMap<Integer, Double> to store
 * data. Has proven to be significantly faster than array-based representations
 * of RIVs when doing vector arithmetic.
 *
 * @author josh
 */
public final class MapRIV extends AbstractRIV implements RIV, Serializable {

  /**
   * CEREAL
   */
  private static final long serialVersionUID = 350977843775988038L;

  /**
   * The dimensionality of this riv.
   */
  private final int size;
  private final ConcurrentHashMap<Integer, MutableDouble> data;

  public MapRIV(final ConcurrentHashMap<Integer, MutableDouble> points,
                final int size) {
    this(size);
    points.forEach((i, v) -> _addPoint(i, v));
  }

  public MapRIV(final int size) {
    data = new ConcurrentHashMap<Integer, MutableDouble>();
    this.size = size;
  }

  public MapRIV(final int[] keys, final double[] vals, final int size) {
    this(size);
    final int l = keys.length;
    if (l != vals.length)
      throw new SizeMismatchException("Different quantity keys than values!");
    for (int i = 0; i < l; i++)
      data.put(keys[i], new MutableDouble(vals[i]));
  }

  public MapRIV(final MapRIV riv) {
    this(riv.size);
    destructiveAdd(riv);
  }

  public MapRIV(final RIV riv) {
    this(riv.size());
    destructiveAdd(riv);
  }

  private void _addPoint(final Integer index, final MutableDouble value) {
    data.compute(index, (i, v) -> {
      if (v == null) v = new MutableDouble();
      v.add(value);
      return v;
    });
  }

  private void addPoint(final int index, final double value) {
    data.compute(index, (i, v) -> {
      if (v == null) v = new MutableDouble();
      v.add(value);
      return v;
    });
  }

  private void assertValidIndex(final int index) {
    if (index > size || index < 0)
      throw new IndexOutOfBoundsException("Index " + index
                                          + " is outside the bounds of this vector.");
  }

  @Override
  public boolean contains(final int index) {
    return data.containsKey(index);
  }

  @Override
  public MapRIV copy() {
    return new MapRIV(this);
  }

  @Override
  public int count() {
    return data.size();
  }

  public MapRIV
         destructiveAdd(final MapRIV other) {
    other.data.forEach(this::_addPoint);
    return this;
  }

  @Override
  public MapRIV destructiveAdd(final RIV other) {
    other.forEachNZ(this::addPoint);
    return this;
  }

  @Override
  public MapRIV destructiveAdd(final RIV... rivs) {
    for (final RIV riv : rivs)
      destructiveAdd(riv);
    return this;
  }

  @Override
  public MapRIV destructiveDiv(final double scalar) {
    data.replaceAll((k, v) -> {
      v.setValue(v.getValue() / scalar);
      return v;
    });
    return this;
  }

  /**
   * An optimized, destructive, element-wise multiplier; do not use when you'll
   * have to reference the original structure later.
   *
   * @param scalar
   * @return multiplies every element in this by scalar, then returns this.
   */
  @Override
  public MapRIV destructiveMult(final double scalar) {
    data.replaceAll((k, v) -> {
      v.setValue(v.getValue() * scalar);
      return v;
    });
    return this;
  }

  @Override
  public MapRIV destructiveRemoveZeros() {
    for (final int i : new HashSet<>(data.keySet()))
      data.compute(i, (k, v) -> Util.doubleEquals(v.getValue(), 0) ? null : v);
    return this;
  }

  public MapRIV
         destructiveSub(final MapRIV other) throws SizeMismatchException {
    other.data.forEach((BiConsumer<Integer, MutableDouble>) this::subtractPoint);
    return this;
  }

  /*
   * private void assertSizeMatch(final RIV other, final String message) throws
   * SizeMismatchException { if (size != other.size()) throw new
   * SizeMismatchException(message); }
   */

  @Override
  public MapRIV destructiveSub(final RIV other) throws SizeMismatchException {
    // assertSizeMatch(other, "Cannot subtract rivs of mismatched sizes.");
    other.forEachNZ(this::subtractPoint);
    return this;
  }

  @Override
  public MapRIV destructiveSub(final RIV... rivs) {
    for (final RIV riv : rivs)
      destructiveSub(riv);
    return this;
  }

  @Override
  public boolean equals(final RIV other) {
    if (other instanceof MapRIV)
      return equals((MapRIV) other);
    else
      return equals((AbstractRIV) other);
  }

  public boolean equals(final MapRIV other) {
    return size == other.size() && data.equals(other.data);
  }

  @Override
  public void forEachNZ(final IntDoubleConsumer fun) {
    data.forEach((i, v) -> fun.accept(i, v.getValue()));
  }

  @Override
  public double get(final int index) throws IndexOutOfBoundsException {
    assertValidIndex(index);
    return getOrDefault(index, 0.0);
  }

  public double getOrDefault(final int index, final double otherVal) {
    final MutableDouble v = data.get(index);
    if (null == v) return otherVal;
    return v.getValue();
  }

  @Override
  public int[] keyArr() {
    return ArrayUtils.toPrimitive(data.keySet().toArray(new Integer[count()]));
  }

  @Override
  public IntStream keyStream() {
    return data.keySet().stream().mapToInt(x -> x);
  }

  /*
   * @Override public MapRIV divide(final double scalar) { return
   * copy().destructiveMult(1 / scalar); }
   */

  @Override
  public MapRIV permute(final Permutations permutations, final int times) {
    if (times == 0)
      return this;
    else
      return new MapRIV(times > 0 ? RIVs.permuteKeys(keyArr(),
                                                     permutations.permute,
                                                     times)
                                  : RIVs.permuteKeys(keyArr(),
                                                     permutations.inverse,
                                                     -times),
                        valArr(),
                        size);
  }

  @Override
  public VectorElement[] points() {
    final VectorElement[] points = new VectorElement[count()];
    final AtomicInteger c = new AtomicInteger();
    data.forEach(10000,
                 (a,
                  b) -> points[c.getAndIncrement()] = VectorElement.elt(a,
                                                                        b.getValue()));
    Arrays.sort(points);
    return points;
  }

  @Override
  public Stream<VectorElement> pointStream() {
    return stream().map(VectorElement::elt);
  }

  @Override
  public double put(final int index, final double value) {
    return data.put(index, new MutableDouble(value)).getValue();
  }

  @Override
  public int size() {
    return size;
  }

  /*
   * @Override public double magnitude() { return Math.sqrt(valStream().map(x ->
   * x * x) .sum()); }
   *
   * @Override public MapRIV multiply(final double scalar) { return
   * copy().destructiveMult(scalar); }
   *
   * @Override public MapRIV normalize() { return divide(magnitude()); }
   */

  /**
   * @return all index/value pairs in this, as a stream
   */
  public Stream<Entry<Integer, Double>> stream() {
    return data.entrySet().stream()
               .map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(),
                                                                e.getValue()
                                                                 .getValue()));
  }

  /*
   * @Override public MapRIV removeZeros() { final ConcurrentHashMap<Integer,
   * Double> map = entrySet().stream() .filter(e -> !Util.doubleEquals(0,
   * e.getValue())) .collect(ConcurrentHashMap::new, (i, e) -> i.put(e.getKey(),
   * e.getValue()), ConcurrentHashMap::putAll); return new MapRIV(map, size); }
   */

  /**
   * An optimized version of subtract() for use when adding MapRIVs to
   * eachother.
   *
   * @param other
   *          : A MapRIV of the same size as this one.
   * @return this - other
   * @throws SizeMismatchException
   */
  public MapRIV subtract(final MapRIV other) {
    return copy().destructiveSub(other).destructiveRemoveZeros();
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
    data.compute(index, (i, v) -> {
      v.subtract(value);
      return v;
    });
  }

  @Override
  public String toString() {
    // "0|1 1|3 4|2 5"
    // "I|V I|V I|V Size"
    final StringBuilder sb = new StringBuilder();
    for (final VectorElement point : points())
      sb.append(point.toString() + " ");
    sb.append(size);
    return sb.toString();
  }

  @Override
  public double[] valArr() {
    final double[] vals = new double[count()];
    final AtomicInteger c = new AtomicInteger();
    data.values().forEach(v -> vals[c.getAndIncrement()] = v.getValue());
    return vals;
  }

  @Override
  public DoubleStream valStream() {
    return data.values().stream().mapToDouble(x -> x.getValue());
  }

  public static MapRIV empty(final int size) {
    return new MapRIV(size);
  }

  /**
   * @param rivString
   *          : A string representation of a RIV, generally got by calling
   *          RIV.toString().
   * @return a MapRIV
   */
  public static MapRIV fromString(final String rivString) {
    String[] pointStrings = rivString.split(" ");
    final int last = pointStrings.length - 1;
    final int size = Integer.parseInt(pointStrings[last]);
    pointStrings = Arrays.copyOf(pointStrings, last);
    final ConcurrentHashMap<Integer, MutableDouble> elts = new ConcurrentHashMap<>();
    for (final String s : pointStrings) {
      final String[] elt = s.split("\\|");
      if (elt.length != 2)
        throw new IndexOutOfBoundsException("Wrong number of partitions: " + s);
      else
        elts.put(Integer.parseInt(elt[0]),
                 new MutableDouble(Double.parseDouble(elt[1])));
    }
    return new MapRIV(elts, size).destructiveRemoveZeros();
  }

  public static RIV generate(final int size, final int nnz,
                             final CharSequence token) {
    return RIVs.generateRIV(size, nnz, token, MapRIV::new);
  }

  public static RIV generate(final int size,
                             final int nnz,
                             final CharSequence text,
                             final int tokenStart,
                             final int tokenWidth) {
    return RIVs.generateRIV(size, nnz, text, tokenStart, tokenWidth,
                            MapRIV::new);
  }

  public static RIVConstructor getConstructor() {
    return MapRIV::new;
  }
}
