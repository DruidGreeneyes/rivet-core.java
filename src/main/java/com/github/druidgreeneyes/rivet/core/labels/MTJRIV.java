package com.github.druidgreeneyes.rivet.core.labels;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.SparseVector;

import com.github.druidgreeneyes.rivet.core.exceptions.SizeMismatchException;
import com.github.druidgreeneyes.rivet.core.util.IntDoubleConsumer;
import com.github.druidgreeneyes.rivet.core.vectorpermutations.Permutations;

/**
 * Implementation of RIV that uses ConcurrentHashMap<Integer, Double> to store
 * data. Has proven to be significantly faster than array-based representations
 * of RIVs when doing vector arithmetic.
 *
 * @author josh
 */
public final class MTJRIV extends AbstractRIV {

  /**
   * CEREAL
   */
  private static final long serialVersionUID = 3494261572186804173L;

  private final SparseVector data;

  public MTJRIV(final int size) {
    data = new SparseVector(size);
  }

  public MTJRIV(final int[] keys, final double[] vals, final int size) {
    data = new SparseVector(size, keys, vals, true);
  }

  public MTJRIV(final MTJRIV riv) {
    data = new SparseVector(riv.size(), riv.count());
    destructiveAdd(riv);
  }

  public MTJRIV(final RIV riv) {
    data = new SparseVector(riv.size(), riv.count());
    destructiveAdd(riv);
  }

  MTJRIV(final SparseVector sv) {
    data = new SparseVector(sv);
  }

  @Override
  public boolean contains(final int index) {
    return get(index) != 0;
  }

  @Override
  public MTJRIV copy() {
    return new MTJRIV(data.copy());
  }

  @Override
  public int count() {
    return data.getUsed();
  }

  @Override
  public MTJRIV destructiveAdd(final RIV other) throws SizeMismatchException {
    // assertSizeMatch(other, "Cannot add rivs of mismatched sizes.");
    other.forEachNZ(data::add);
    return this;
  }

  @Override
  public MTJRIV destructiveAdd(final RIV... rivs) {
    for (final RIV riv : rivs)
      destructiveAdd(riv);
    return this;
  }

  @Override
  public MTJRIV destructiveDiv(final double scalar) {
    return destructiveMult(1 / scalar);
  }

  /**
   * An optimized, destructive, element-wise multiplier; do not use when you'll
   * have to reference the original structure later.
   *
   * @param scalar
   * @return multiplies every element in this by scalar, then returns this.
   */
  @Override
  public MTJRIV destructiveMult(final double scalar) {
    data.forEach(e -> e.set(e.get() * scalar));
    return this;
  }

  /*
   * private void assertSizeMatch(final RIV other, final String message) throws
   * SizeMismatchException { if (size != other.size()) throw new
   * SizeMismatchException(message); }
   */

  @Override
  public MTJRIV destructiveRemoveZeros() {
    data.compact();
    return this;
  }

  @Override
  public MTJRIV destructiveSub(final RIV other) throws SizeMismatchException {
    other.forEachNZ(this::sub);
    return this;
  }

  @Override
  public MTJRIV destructiveSub(final RIV... rivs) {
    for (final RIV riv : rivs)
      destructiveSub(riv);
    return this;
  }

  public boolean equals(final MTJRIV other) {
    return super.equals(other);
  }

  @Override
  public boolean equals(final Object other) {
    return RIVs.equals(this, other);
  }

  @Override
  public void forEachNZ(final IntDoubleConsumer fun) {
    data.forEach(e -> fun.accept(e.index(), e.get()));
  }

  /**
   * Implements the hash function found in java.lang.String, using values in
   * place of characters. Modifying the RIV is virtually guaranteed to change
   * the hashcode.
   */
  @Override
  public int hashCode() {
    return RIVs.hashcode(this);
  }

  @Override
  public int[] keyArr() {
    return data.getIndex();
  }

  @Override
  public IntStream keyStream() {
    return Arrays.stream(keyArr());
  }

  @Override
  public MTJRIV permute(final Permutations permutations, final int times) {
    if (times == 0)
      return this;
    else
      return new MTJRIV(times > 0
                                  ? RIVs.permuteKeys(keyArr(),
                                                     permutations.permute,
                                                     times)
                                  : RIVs.permuteKeys(keyArr(),
                                                     permutations.inverse,
                                                     -times),
                        valArr(),
                        data.size());
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

  @Override
  public VectorElement[] points() {
    final VectorElement[] points = new VectorElement[count()];
    final AtomicInteger c = new AtomicInteger();
    forEachNZ((a,
               b) -> points[c.getAndIncrement()] = VectorElement.elt(a, b));
    Arrays.sort(points);
    return points;
  }

  /*
   * @Override public MapRIV removeZeros() { final ConcurrentHashMap<Integer,
   * Double> map = entrySet().stream() .filter(e -> !Util.doubleEquals(0,
   * e.getValue())) .collect(ConcurrentHashMap::new, (i, e) -> i.put(e.getKey(),
   * e.getValue()), ConcurrentHashMap::putAll); return new MapRIV(map, size); }
   */

  @Override
  public Stream<VectorElement> pointStream() {
    return stream().map(e -> VectorElement.elt(e.index(), e.get()));
  }

  @Override
  public double put(final int index, final double value) {
    final double v = get(index);
    data.set(index, value);
    return v;
  }

  /*
   * @Override public MapRIV subtract(final RIV other) throws
   * SizeMismatchException { return copy().destructiveSub(other)
   * .destructiveRemoveZeros(); }
   */

  @Override
  public int size() {
    return data.size();
  }

  public Stream<VectorEntry> stream() {
    return StreamSupport.stream(data.spliterator(), true);
  }

  private void sub(final int index, final double value) {
    data.add(index, -value);
  }

  @Override
  public String toString() {
    // "0|1 1|3 4|2 5"
    // "I|V I|V I|V Size"
    final StringBuilder sb = new StringBuilder();
    for (final VectorElement point : points())
      sb.append(point.toString() + " ");
    sb.append(data.size());
    return sb.toString();
  }

  @Override
  public double[] valArr() {
    return data.getData();
  }

  @Override
  public DoubleStream valStream() {
    return Arrays.stream(valArr());
  }

  public static MTJRIV empty(final int size) {
    return new MTJRIV(size);
  }

  /**
   * @param rivString
   *          : A string representation of a RIV, generally got by calling
   *          RIV.toString().
   * @return a MapRIV
   */
  public static MTJRIV fromString(final String rivString) {
    String[] pointStrings = rivString.split(" ");
    final int last = pointStrings.length - 1;
    final int size = Integer.parseInt(pointStrings[last]);
    pointStrings = Arrays.copyOf(pointStrings, last);
    final MTJRIV res = new MTJRIV(size);
    for (final String s : pointStrings) {
      final String[] elt = s.split("\\|");
      if (elt.length != 2)
        throw new IndexOutOfBoundsException(
                                            "Wrong number of partitions: " + s);
      else
        res.data.add(Integer.parseInt(elt[0]), Double.parseDouble(elt[1]));
    }
    return res.destructiveRemoveZeros();
  }

  public static RIV generate(final int size, final int nnz,
                             final CharSequence token) {
    return RIVs.generateRIV(size, nnz, token, MTJRIV::new);
  }

  public static RIV generate(final int size,
                             final int nnz,
                             final CharSequence text,
                             final int tokenStart,
                             final int tokenWidth) {
    return RIVs.generateRIV(size, nnz, text, tokenStart, tokenWidth,
                            MTJRIV::new);
  }

  public static RIVConstructor getConstructor() {
    return MTJRIV::new;
  }

  @Override
  public double get(final int index) {
    return data.get(index);
  }
}
