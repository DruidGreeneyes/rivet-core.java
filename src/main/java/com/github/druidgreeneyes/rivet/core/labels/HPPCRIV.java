package com.github.druidgreeneyes.rivet.core.labels;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.carrotsearch.hppc.IntDoubleHashMap;
import com.carrotsearch.hppc.predicates.IntDoublePredicate;
import com.carrotsearch.hppc.procedures.IntDoubleProcedure;
import com.carrotsearch.hppc.cursors.IntDoubleCursor;
import com.github.druidgreeneyes.rivet.core.util.IntDoubleConsumer;
import com.github.druidgreeneyes.rivet.core.util.Util;
import com.github.druidgreeneyes.rivet.core.vectorpermutations.Permutations;

public class HPPCRIV extends AbstractRIV implements RIV, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 7489480432514925162L;

  private final int size;
  private final IntDoubleHashMap data;

  public HPPCRIV(final HPPCRIV riv) {
    data = new IntDoubleHashMap(riv.data);
    size = riv.size;
  }

  public HPPCRIV(final int size) {
    data = new IntDoubleHashMap();
    this.size = size;
  }

  public HPPCRIV(final int[] indices, final double[] values, final int size) {
    this.size = size;
    data = IntDoubleHashMap.from(indices, values);
    data.ensureCapacity(size);
  }

  public HPPCRIV(final RIV riv) {
    this(riv.size());
    riv.forEachNZ(data::put);
  }

  public HPPCRIV(final VectorElement[] points, final int size) {
    this(size);
    for (final VectorElement point : points)
      data.put(point.index(), point.value());
  }

  @Override
  public boolean contains(final int index) {
    return data.containsKey(index);
  }

  @Override
  public HPPCRIV copy() {
    return new HPPCRIV(this);
  }

  @Override
  public int count() {
    return data.size();
  }

  @Override
  public HPPCRIV destructiveAdd(final RIV other) {
    other.forEachNZ(data::addTo);
    return this;
  }

  @Override
  public HPPCRIV destructiveAdd(final RIV... rivs) {
    for (final RIV riv : rivs)
      destructiveAdd(riv);
    return this;
  }

  @Override
  public HPPCRIV destructiveDiv(final double scalar) {
    for (int i = 0; i < data.values.length; i++)
      data.values[i] /= scalar;
    return this;
  }

  @Override
  public HPPCRIV destructiveMult(final double scalar) {
    for (int i = 0; i < data.values.length; i++)
      data.values[i] *= scalar;
    return this;
  }

  @Override
  public HPPCRIV destructiveRemoveZeros() {
    data.removeAll((IntDoublePredicate) (k, v) -> Util.doubleEquals(v, 0.0));
    return this;
  }

  @Override
  public HPPCRIV destructiveSub(final RIV other) {
    other.forEachNZ((i, v) -> data.addTo(i, -v));
    return this;
  }

  @Override
  public HPPCRIV destructiveSub(final RIV... rivs) {
    for (final RIV riv : rivs)
      destructiveSub(riv);
    return this;
  }

  @Override
  public boolean equals(final RIV other) {
    if (other instanceof HPPCRIV)
      return equals((HPPCRIV) other);
    else
      // return RIVs.equals(this, other);
      return equals((AbstractRIV) other);
  }

  public boolean equals(final HPPCRIV other) {
    return size == other.size && data.equals(other.data);
  }

  @Override
  public void forEachNZ(final IntDoubleConsumer fun) {
    final Consumer<IntDoubleCursor> c = cur -> fun.accept(cur.key, cur.value);
    data.forEach(c);
  }

  @Override
  public void forEach(final IntDoubleConsumer fun) {
    for (int i = 0; i < size; i++)
      fun.accept(i, get(i));
  }

  @Override
  public double get(final int index) throws IndexOutOfBoundsException {
    if (size <= index || index < 0)
      throw new IndexOutOfBoundsException(
                                          index
                                          + " is outside the bounds of this RIV");
    return data.getOrDefault(index, 0);
  }

  @Override
  public int[] keyArr() {
    final int[] keys = new int[count()];
    for (int i = 0, j = 0; i < size; i++)
      if (!Util.doubleEquals(get(i), 0))
        keys[j++] = i;
    return keys;
  }

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
                                   ? RIVs.permuteKeys(keyArr(),
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
    data.forEach((IntDoubleProcedure) (k,
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
  public double put(final int index, final double value) {
    return data.put(index, value);
  }

  @Override
  public int size() {
    return size;
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

  public static RIV generate(final int size, final int nnz,
                             final CharSequence token) {
    return RIVs.generateRIV(size, nnz, token, HPPCRIV::new);
  }

  public static RIV generate(final int size,
                             final int nnz,
                             final CharSequence text,
                             final int tokenStart,
                             final int tokenWidth) {
    return RIVs.generateRIV(size, nnz, text, tokenStart, tokenWidth,
                            HPPCRIV::new);
  }

  public static RIVConstructor getConstructor() {
    return HPPCRIV::new;
  }
}
