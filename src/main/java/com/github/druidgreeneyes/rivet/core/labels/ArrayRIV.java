package com.github.druidgreeneyes.rivet.core.labels;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import com.github.druidgreeneyes.rivet.core.util.IntDoubleConsumer;
import com.github.druidgreeneyes.rivet.core.vectorpermutations.Permutations;

public final class ArrayRIV extends AbstractRIV implements RIV, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -1176979873718129432L;

  private VectorElement[] points;

  private final int size;

  public ArrayRIV(final int size) {
    points = new VectorElement[0];
    this.size = size;
  }

  public ArrayRIV(final int[] keys, final double[] vals, final int size) {
    this.size = size;
    final int l = keys.length;
    if (l != vals.length)
      throw new IndexOutOfBoundsException(
                                          "Different quantity keys than values!");
    final VectorElement[] elts = new VectorElement[l];
    for (int i = 0; i < l; i++)
      elts[i] = VectorElement.elt(keys[i], vals[i]);
    Arrays.sort(elts, VectorElement::compare);
    points = elts;
    removeZeros();
  }

  public ArrayRIV(final RIV riv) {
    points = Arrays.stream(riv.points())
                   .map(VectorElement::copy)
                   .toArray(VectorElement[]::new);
    size = riv.size();
  }

  public ArrayRIV(final VectorElement[] points, final int size) {
    this.points = ArrayUtils.clone(points);
    Arrays.sort(points);
    this.size = size;
  }

  private int binarySearch(final int index) {
    return binarySearch(VectorElement.fromIndex(index));
  }

  private int binarySearch(final VectorElement elt) {
    return Arrays.binarySearch(points, elt, VectorElement::compare);
  }

  @Override
  public boolean contains(final int index) {
    return keyStream().anyMatch((k) -> k == index);
  }

  @Override
  public ArrayRIV copy() {
    return new ArrayRIV(this);
  }

  @Override
  public int count() {
    return points.length;
  }

  @Override
  public ArrayRIV destructiveAdd(final RIV other) {
    other.keyStream()
         .forEach((
                   k) -> destructiveSet(getPoint(k).destructiveAdd(other.get(k))));
    return this;
  }

  @Override
  public ArrayRIV destructiveAdd(final RIV... rivs) {
    for (int i = 0; i < size; i++)
      for (final RIV riv : rivs)
        getPoint(i).destructiveAdd(riv.get(i));
    return this;
  }

  @Override
  public ArrayRIV destructiveDiv(final double scalar) {
    Arrays.stream(points)
          .forEach(elt -> elt.destructiveDiv(scalar));
    return this;
  }

  @Override
  public ArrayRIV destructiveMult(final double scalar) {
    Arrays.stream(points)
          .forEach(elt -> elt.destructiveMult(scalar));
    return this;
  }

  @Override
  public ArrayRIV destructiveRemoveZeros() {
    for (int i = 0; i < points.length; i++)
      if (points[i].contains(0)) {
        points = ArrayUtils.remove(points, i);
        i--;
      }
    return this;
  }

  private void destructiveSet(final VectorElement elt)
                                                       throws IndexOutOfBoundsException {
    if (validIndex(elt.index())) {
      final int i = binarySearch(elt);
      if (i < 0)
        points = ArrayUtils.add(points, ~i, elt);
      else
        points[i] = elt;
    } else
      throw new IndexOutOfBoundsException(
                                          "Index " + elt.index()
                                          + " is outside the bounds of this vector.");
  }

  @Override
  public ArrayRIV destructiveSub(final RIV other) {
    other.keyStream()
         .forEach(k -> destructiveSet(getPoint(k).destructiveSub(other.get(k))));
    return this;
  }

  @Override
  public ArrayRIV destructiveSub(final RIV... rivs) {
    for (int i = 0; i < size; i++)
      for (final RIV riv : rivs)
        getPoint(i).destructiveSub(riv.get(i));
    return this;
  }

  @Override
  public boolean equals(final RIV other) {
    if (other instanceof ArrayRIV)
      return equals((ArrayRIV) other);
    else
      return equals((AbstractRIV) other);
  }

  public boolean equals(final ArrayRIV other) {
    return size == other.size && Arrays.deepEquals(points, other.points);
  }

  @Override
  public void forEachNZ(final IntDoubleConsumer fun) {
    for (final VectorElement elt : points)
      fun.accept(elt.index(), elt.value());
  }

  @Override
  public double get(final int index) throws IndexOutOfBoundsException {
    return getPoint(index).value();
  }

  private VectorElement getPoint(final int index)
                                                  throws IndexOutOfBoundsException {
    if (validIndex(index)) {
      final int i = binarySearch(index);
      return i < 0
                   ? VectorElement.fromIndex(index)
                   : points[i];
    } else
      throw new IndexOutOfBoundsException(
                                          "Index " + index
                                          + " is outside the bounds of this vector.");
  }

  @Override
  public int[] keyArr() {
    final int[] keys = new int[points.length];
    for (int i = 0; i < points.length; i++)
      keys[i] = points[i].index();
    return keys;
  }

  @Override
  public IntStream keyStream() {
    return stream().mapToInt(VectorElement::index);
  }

  @Override
  public double magnitude() {
    return Math.sqrt(valStream().map((v) -> v * v)
                                .sum());
  }

  @Override
  public ArrayRIV permute(final Permutations permutations, final int times) {
    if (times == 0)
      return this;
    else
      return new ArrayRIV(times > 0
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
    return points;
  }

  @Override
  public Stream<VectorElement> pointStream() {
    return Arrays.stream(points);
  }

  @Override
  public double put(final int index, final double value) {
    final double v = get(index);
    destructiveSet(VectorElement.elt(index, value));
    return v;
  }

  @Override
  public ArrayRIV removeZeros() {
    final VectorElement[] elts = stream().filter(ve -> !ve.contains(0))
                                         .toArray(VectorElement[]::new);
    if (elts.length == count())
      return this;
    else
      return new ArrayRIV(elts, size);
  }

  @Override
  public int size() {
    return size;
  }

  public Stream<VectorElement> stream() {
    return Arrays.stream(points);
  }

  @Override
  public String toString() {
    // "0|1 1|3 4|2 5"
    // "I|V I|V I|V Size"
    return stream().map(VectorElement::toString)
                   .collect(Collectors.joining(" ",
                                               "",
                                               " " + String.valueOf(size)));
  }

  @Override
  public double[] valArr() {
    final double[] vals = new double[points.length];
    for (int i = 0; i < points.length; i++)
      vals[i] = points[i].value();
    return vals;
  }

  private boolean validIndex(final int index) {
    return 0 <= index && index < size;
  }

  @Override
  public DoubleStream valStream() {
    return stream().mapToDouble(VectorElement::value);
  }

  public static RIVConstructor getConstructor() {
    return ArrayRIV::new;
  }

  public static ArrayRIV empty(final int size) {
    return new ArrayRIV(size);
  }

  public static ArrayRIV fromString(final String rivString) {
    String[] r = rivString.split(" ");
    final int l = r.length - 1;
    final int size = Integer.parseInt(r[l]);
    r = ArrayUtils.remove(r, l);
    final VectorElement[] elts = new VectorElement[l];
    for (int i = 0; i < l; i++)
      elts[i] = VectorElement.fromString(r[i]);
    return new ArrayRIV(elts, size);
  }

  public static RIV generate(final int size, final int nnz,
                             final CharSequence token) {
    return RIVs.generateRIV(size, nnz, token, ArrayRIV::new);
  }

  public static RIV generate(final int size,
                             final int nnz,
                             final CharSequence text,
                             final int tokenStart,
                             final int tokenWidth) {
    return RIVs.generateRIV(size, nnz, text, tokenStart, tokenWidth,
                            ArrayRIV::new);
  }
}
