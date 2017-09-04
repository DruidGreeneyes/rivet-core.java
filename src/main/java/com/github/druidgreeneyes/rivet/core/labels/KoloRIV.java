package com.github.druidgreeneyes.rivet.core.labels;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.druidgreeneyes.rivet.core.exceptions.SizeMismatchException;
import com.github.druidgreeneyes.rivet.core.util.Util;
import com.github.druidgreeneyes.rivet.core.vectorpermutations.Permutations;
import com.koloboke.collect.map.hash.HashIntDoubleMap;
import com.koloboke.collect.map.hash.HashIntDoubleMaps;
import com.koloboke.function.IntDoubleConsumer;
import com.koloboke.function.IntDoubleToDoubleFunction;

/**
 * Implementation of RIV that uses ConcurrentHashMap<Integer, Double> to store
 * data. Has proven to be significantly faster than array-based representations
 * of RIVs when doing vector arithmetic.
 *
 * @author josh
 */
public final class KoloRIV extends AbstractRIV implements RIV, Serializable {

  /**
   * CEREAL
   */
  private static final long serialVersionUID = 350977843775988038L;

  /**
   * The dimensionality of this riv.
   */
  private final int size;
  private final HashIntDoubleMap data;

  public KoloRIV(final HashIntDoubleMap points,
                 final int size) {
    this.size = size;
    data = HashIntDoubleMaps.newMutableMap(points);
  }

  public KoloRIV(final int size) {
    data = HashIntDoubleMaps.newMutableMap();
    this.size = size;
  }

  public KoloRIV(final int[] keys, final double[] vals, final int size) {
    if (keys.length != vals.length)
      throw new SizeMismatchException("Different quantity keys than values!");
    this.size = size;
    data = HashIntDoubleMaps.newMutableMap(keys, vals);
  }

  public KoloRIV(final KoloRIV riv) {
    this(riv.size);
    destructiveAdd(riv);
  }

  public KoloRIV(final RIV riv) {
    this(riv.size());
    destructiveAdd(riv);
  }

  @Override
  public boolean contains(final int index) {
    return data.containsKey(index);
  }

  @Override
  public KoloRIV copy() {
    return new KoloRIV(this);
  }

  @Override
  public int count() {
    return data.size();
  }

  @Override
  public KoloRIV destructiveAdd(final RIV other) {
    other.forEachNZ(data::addValue);
    return this;
  }

  @Override
  public KoloRIV destructiveAdd(final RIV... rivs) {
    for (final RIV riv : rivs)
      destructiveAdd(riv);
    return this;
  }

  private static final IntDoubleToDoubleFunction div(final double scalar) {
    return (i, v) -> v / scalar;
  }

  @Override
  public KoloRIV destructiveDiv(final double scalar) {
    data.replaceAll(div(scalar));
    return this;
  }

  private static final IntDoubleToDoubleFunction mult(final double scalar) {
    return (i, v) -> v * scalar;
  }

  /**
   * An optimized, destructive, element-wise multiplier; do not use when you'll
   * have to reference the original structure later.
   *
   * @param scalar
   * @return multiplies every element in this by scalar, then returns this.
   */
  @Override
  public KoloRIV destructiveMult(final double scalar) {
    data.replaceAll(mult(scalar));
    return this;
  }

  @Override
  public KoloRIV destructiveRemoveZeros() {
    data.removeIf((i, v) -> Util.doubleEquals(v, 0));
    return this;
  }

  @Override
  public KoloRIV destructiveSub(final RIV other) throws SizeMismatchException {
    // assertSizeMatch(other, "Cannot subtract rivs of mismatched sizes.");
    other.forEachNZ(this::subtractPoint);
    return this;
  }

  @Override
  public KoloRIV destructiveSub(final RIV... rivs) {
    for (final RIV riv : rivs)
      destructiveSub(riv);
    return this;
  }

  @Override
  public boolean equals(final RIV other) {
    if (other instanceof KoloRIV)
      return equals((KoloRIV) other);
    else
      // return RIVs.equals(this, other);
      return equals((AbstractRIV) other);
  }

  public boolean equals(final KoloRIV other) {
    return size == other.size() && data.equals(other.data);
  }

  @Override
  public void
         forEachNZ(final com.github.druidgreeneyes.rivet.core.util.IntDoubleConsumer fun) {
    data.forEach((IntDoubleConsumer) (i, v) -> fun.accept(i, v));
  }

  private void
          assertValidIndex(final int index) throws IndexOutOfBoundsException {
    if (index < 0 || index >= size)
      throw new IndexOutOfBoundsException("Invalid index: " + index);
  }

  @Override
  public double get(final int index) throws IndexOutOfBoundsException {
    assertValidIndex(index);
    return data.getOrDefault(index, 0.0);
  }

  @Override
  public int[] keyArr() {
    final int[] res = data.keySet().toIntArray();
    Arrays.sort(res);
    return res;
  }

  @Override
  public IntStream keyStream() {
    return Arrays.stream(keyArr());
  }

  /*
   * @Override public MapRIV divide(final double scalar) { return
   * copy().destructiveMult(1 / scalar); }
   */

  @Override
  public KoloRIV permute(final Permutations permutations, final int times) {
    if (times == 0)
      return this;
    else
      return new KoloRIV(times > 0 ? RIVs.permuteKeys(keyArr(),
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
    data.forEach((IntDoubleConsumer) (a,
                                      b) -> points[c.getAndIncrement()] = VectorElement.elt(a,
                                                                                            b));
    Arrays.sort(points);
    return points;
  }

  @Override
  public Stream<VectorElement> pointStream() {
    return stream().map(VectorElement::elt);
  }

  @Override
  public double put(final int index, final double value) {
    return data.put(index, value);
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
    return data.entrySet().stream();
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
  public KoloRIV subtract(final KoloRIV other) {
    return copy().destructiveSub(other).destructiveRemoveZeros();
  }
  /*
   * @Override public MapRIV subtract(final RIV other) throws
   * SizeMismatchException { return copy().destructiveSub(other)
   * .destructiveRemoveZeros(); }
   */

  private void subtractPoint(final int index, final double value) {
    data.addValue(index, -value);
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
    final int[] keys = keyArr();
    final double[] vals = new double[keys.length];
    for (int i = 0; i < keys.length; i++)
      vals[i] = data.get(keys[i]);
    return vals;
  }

  @Override
  public DoubleStream valStream() {
    return Arrays.stream(valArr());
  }

  public static KoloRIV empty(final int size) {
    return new KoloRIV(size);
  }

  /**
   * @param rivString
   *          : A string representation of a RIV, generally got by calling
   *          RIV.toString().
   * @return a MapRIV
   */
  public static KoloRIV fromString(final String rivString) {
    String[] pointStrings = rivString.split(" ");
    final int last = pointStrings.length - 1;
    final int size = Integer.parseInt(pointStrings[last]);
    pointStrings = Arrays.copyOf(pointStrings, last);
    final KoloRIV res = new KoloRIV(size);
    for (final String s : pointStrings) {
      final String[] elt = s.split("\\|");
      if (elt.length != 2)
        throw new IndexOutOfBoundsException("Wrong number of partitions: " + s);
      else
        res.put(Integer.parseInt(elt[0]),
                Double.parseDouble(elt[1]));
    }
    return res;
  }

  public static RIV generate(final int size, final int nnz,
                             final CharSequence token) {
    return RIVs.generateRIV(size, nnz, token, KoloRIV::new);
  }

  public static RIV generate(final int size,
                             final int nnz,
                             final CharSequence text,
                             final int tokenStart,
                             final int tokenWidth) {
    return RIVs.generateRIV(size, nnz, text, tokenStart, tokenWidth,
                            KoloRIV::new);
  }

  public static RIVConstructor getConstructor() {
    return KoloRIV::new;
  }
}
