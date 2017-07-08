package com.github.druidgreeneyes.rivet.core.labels;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.carrotsearch.hppc.IntDoubleHashMap;
import com.carrotsearch.hppc.predicates.IntDoublePredicate;
import com.carrotsearch.hppc.procedures.IntDoubleProcedure;
import com.github.druidgreeneyes.rivet.core.util.IntDoubleConsumer;
import com.github.druidgreeneyes.rivet.core.util.Util;
import com.github.druidgreeneyes.rivet.core.vectorpermutations.Permutations;

public class HPPCRIV extends IntDoubleHashMap implements RIV, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 7489480432514925162L;

  private final int size;

  public HPPCRIV(final HPPCRIV riv) {
    super(riv);
    size = riv.size;
  }

  public HPPCRIV(final int size) {
    super();
    this.size = size;
  }

  public HPPCRIV(final int[] indices, final double[] values, final int size) {
    this(size);
    for (int i = 0; i < indices.length; i++)
      put(indices[i], values[i]);
  }

  public HPPCRIV(final RIV riv) {
    this(riv.size());
    riv.forEachNZ(this::put);
  }

  public HPPCRIV(final VectorElement[] points, final int size) {
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
    for (final int i : other.keyArr()) {
      final double v = other.get(i);
      if (!Util.doubleEquals(v, 0))
                                    addTo(i, v);
    }
    return this;
  }

  @Override
  public HPPCRIV destructiveAdd(final RIV... rivs) {
    for (int i = 0; i < size; i++) {
      double v = get(i);
      final double vv = v;
      for (final RIV riv : rivs)
        v += riv.get(i);
      if (!Util.doubleEquals(v, 0))
        put(i, v);
      else if (!Util.doubleEquals(vv, 0))
                                          remove(i);
    }
    return this;
  }

  @Override
  public HPPCRIV destructiveDiv(final double scalar) {
    forEach((IntDoubleProcedure) (k, v) -> put(k, v / scalar));
    return this;
  }

  @Override
  public HPPCRIV destructiveMult(final double scalar) {
    forEach((IntDoubleProcedure) (k, v) -> put(k, v * scalar));
    return this;
  }

  @Override
  public HPPCRIV destructiveRemoveZeros() {
    removeAll((IntDoublePredicate) (k, v) -> Util.doubleEquals(v, 0.0));
    return this;
  }

  @Override
  public HPPCRIV destructiveSub(final RIV other) {
    for (final int i : other.keyArr())
      addTo(i, -other.get(i));
    return this;
  }

  @Override
  public HPPCRIV destructiveSub(final RIV... rivs) {
    for (int i = 0; i < size; i++) {
      double v = get(i);
      for (final RIV riv : rivs)
        v -= riv.get(i);
      if (v == 0)
        remove(i);
      else
        put(i, v);
    }
    return this;
  }

  @Override
  public boolean equals(final Object other) {
    return RIVs.equals(this, other);
  }

  @Override
  public void forEachNZ(final IntDoubleConsumer fun) {
    final IntDoubleProcedure f = fun::accept;
    super.forEach(f);
  }

  @Override
  public double get(final int index) throws IndexOutOfBoundsException {
    if (size <= index || index < 0)
                                    throw new IndexOutOfBoundsException(
                                                                        index
                                                                        + " is outside the bounds of this RIV");
    return getOrDefault(index, 0);
  }

  @Override
  public int hashCode() {
    return RIVs.hashcode(this);
  }

  @Override
  public int[] keyArr() {
    final int[] keys = new int[count()];
    for (int i = 0, j = 0; i < size; i++)
      if (!Util.doubleEquals(get(i), 0))
                                         keys[j++] = i;
    return keys;
  }

  /*
   * public boolean equals(final HPPCRIV other) { return size == other.size &&
   * super.equals(other); }
   */

  @Override
  public IntStream keyStream() {
    return Arrays.stream(keyArr());
  }

  @Override
  public HPPCRIV permute(final Permutations permutations, final int times) {
    if (times == 0)
      return this;
    else
      return new HPPCRIV(times > 0
                                   ? RIVs.permuteKeys(keyArr(), permutations.permute, times)
                                   : RIVs.permuteKeys(keyArr(), permutations.inverse, -times),
                         valArr(),
                         size);
  }

  @Override
  public VectorElement[] points() {
    final VectorElement[] points = new VectorElement[count()];
    final AtomicInteger c = new AtomicInteger();
    forEach((IntDoubleProcedure) (k,
                                  v) -> points[c.getAndIncrement()] = VectorElement.elt(k,
                                                                                        v));
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
  public String toString() {
    return RIVs.toString(this);
  }

  @Override
  public double[] valArr() {
    final double[] vals = new double[count()];
    int c = 0;
    for (final int k : keyArr())
      vals[c++] = get(k);
    return vals;
  }

  @Override
  public DoubleStream valStream() {
    return Arrays.stream(valArr());
  }

  public static HPPCRIV empty(final int size) {
    return new HPPCRIV(size);
  }

  public static HPPCRIV fromString(final String string) {
    String[] bits = string.split(" ");
    final int size = Integer.parseInt(bits[bits.length - 1]);
    bits = Arrays.copyOf(bits, bits.length - 1);
    final VectorElement[] elements = Arrays.stream(bits)
                                           .map(VectorElement::fromString)
                                           .toArray(VectorElement[]::new);
    return new HPPCRIV(elements, size);
  }

  public static RIV generate(final int size, final int nnz, final CharSequence token) {
    return RIVs.generateRIV(size, nnz, token, HPPCRIV::new);
  }

  public static RIV generate(final int size,
                             final int nnz,
                             final CharSequence text,
                             final int tokenStart,
                             final int tokenEnd) {
    return generate(size, nnz, text.subSequence(tokenStart, tokenEnd));
  }
}
