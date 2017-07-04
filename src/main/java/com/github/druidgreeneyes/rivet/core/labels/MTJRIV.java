package com.github.druidgreeneyes.rivet.core.labels;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.SparseVector;

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
public final class MTJRIV extends SparseVector
                          implements RIV, Serializable {

/**
   * CEREAL
   */
	private static final long serialVersionUID = 3494261572186804173L;

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
    MTJRIV res = new MTJRIV(size);
    for (final String s : pointStrings) {
      final String[] elt = s.split("\\|");
      if (elt.length != 2)
        throw new IndexOutOfBoundsException(
                                            "Wrong number of partitions: " + s);
      else
    	  res.add(Integer.parseInt(elt[0]), Double.parseDouble(elt[1]));
    }
    return res.destructiveRemoveZeros();
  }

  /**
   * Uses Java's seeded RNG to generate a random index vector such that, given
   * the same input, generateLabel will always produce the same output.
   *
   * @param size
   * @param k
   * @param word
   * @return a MapRIV
   */
  public static MTJRIV generateLabel(final int size, final int k,
                                     final CharSequence word) {
    final long seed = RIVs.makeSeed(word);
    final int j = k % 2 == 0
                             ? k
                             : k + 1;
    return new MTJRIV(RIVs.makeIndices(size, j, seed), RIVs.makeVals(j, seed), size);
  }

  public static MTJRIV generateLabel(final int size, final int k,
                                     final CharSequence source,
                                     final int startIndex,
                                     final int tokenLength) {
    return generateLabel(size,
                         k,
                         Util.safeSubSequence(source,
                                              startIndex,
                                              startIndex + tokenLength));
  }

  public static Function<String, MTJRIV> labelGenerator(final int size,
                                                        final int k) {
    return (word) -> generateLabel(size, k, word);
  }

  public static Function<Integer, MTJRIV> labelGenerator(final String source,
                                                         final int size,
                                                         final int k,
                                                         final int tokenLength) {
    return (index) -> generateLabel(size, k, source, index, tokenLength);
  }

  private static int[] permuteKeys(IntStream keys, final int[] permutation,
                                   final int times) {
    for (int i = 0; i < times; i++)
      keys = keys.map((k) -> permutation[k]);
    return keys.toArray();
  }

  public MTJRIV(final int size) {
    super(size);
  }

  public MTJRIV(final int[] keys, final double[] vals, final int size) {
    super(size, keys, vals, true);
  }

  public MTJRIV(final RIV riv) {
    super(riv.size(), riv.count());
    destructiveAdd(riv);
  }

  public MTJRIV(final MTJRIV riv) {
    super(riv.size(), riv.count());
    destructiveAdd(riv);
  }

  MTJRIV(SparseVector sv) {
	super(sv);
  }

/*
   * private void assertSizeMatch(final RIV other, final String message) throws
   * SizeMismatchException { if (size != other.size()) throw new
   * SizeMismatchException(message); }
   */

  @Override
  public boolean contains(final int index) {
    return get(index) != 0;
  }

  @Override
  public MTJRIV copy() {
    return new MTJRIV(super.copy());
  }

  @Override
  public int count() {
    return super.getUsed();
  }

  @Override
  public MTJRIV destructiveAdd(final RIV other) throws SizeMismatchException {
    // assertSizeMatch(other, "Cannot add rivs of mismatched sizes.");
    other.forEach(this::add);
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
  public MTJRIV destructiveMult(final double scalar) {
	forEach(e -> e.set(e.get() * scalar));
    return this;
  }

  @Override
  public MTJRIV destructiveRemoveZeros() {
    super.compact();
    return this;
  }

  @Override
  public MTJRIV destructiveSub(final RIV other) throws SizeMismatchException {
    other.forEach(this::sub);
    return this;
  }

  @Override
  public boolean equals(final Object other) {
    return RIVs.equals(this, other);
  }

  public boolean equals(final MTJRIV other) {
    return super.equals(other);
  }

  @Override
  public IntStream keyStream() {
    return Arrays.stream(keyArr());
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
  public MTJRIV permute(final Permutations permutations, final int times) {
    if (times == 0)
      return this;
    else
      return new MTJRIV(times > 0
                                  ? permuteKeys(keyStream(),
                                                permutations.permute, times)
                                  : permuteKeys(keyStream(),
                                                permutations.inverse, -times),
                        valStream().toArray(), size);
  }

  /*
   * @Override public MapRIV removeZeros() { final ConcurrentHashMap<Integer,
   * Double> map = entrySet().stream() .filter(e -> !Util.doubleEquals(0,
   * e.getValue())) .collect(ConcurrentHashMap::new, (i, e) -> i.put(e.getKey(),
   * e.getValue()), ConcurrentHashMap::putAll); return new MapRIV(map, size); }
   */

  @Override
  public int size() {
    return size;
  }
  
  
  /*
   * @Override public MapRIV subtract(final RIV other) throws
   * SizeMismatchException { return copy().destructiveSub(other)
   * .destructiveRemoveZeros(); }
   */
  
  private void sub(final int index, final double value) {
    add(index, -value);
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
  public DoubleStream valStream() {
    return Arrays.stream(valArr());
  }

  @Override
  public MTJRIV destructiveAdd(final RIV... rivs) {
    for (final RIV riv : rivs)
      destructiveAdd(riv);
    return this;
  }

  @Override
  public MTJRIV destructiveSub(final RIV... rivs) {
    for (final RIV riv : rivs)
      destructiveSub(riv);
    return this;
  }

  @Override
  public MTJRIV destructiveDiv(final double scalar) {
    return destructiveMult(1 / scalar);
  }
  
  public Stream<VectorEntry> stream() {
    return StreamSupport.stream(spliterator(), true);
  }

  @Override
  public Stream<VectorElement> pointStream() {
    return stream().map(e -> VectorElement.elt(e.index(), e.get()));
  }

  @Override
  public int[] keyArr() {
    return super.getIndex();
  }

  @Override
  public double[] valArr() {
    return super.getData();
  }

  @Override
  public VectorElement[] points() {
    final VectorElement[] points = new VectorElement[count()];
    final AtomicInteger c = new AtomicInteger();
    forEach((a,
             b) -> points[c.getAndIncrement()] = VectorElement.elt(a, b));
    Arrays.sort(points);
    return points;
  }

  @Override
  public void forEach(final IntDoubleConsumer fun) {
    super.forEach((e) -> fun.accept(e.index(), e.get()));
  }
}
