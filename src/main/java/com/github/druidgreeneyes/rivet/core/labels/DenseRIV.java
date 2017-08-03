package com.github.druidgreeneyes.rivet.core.labels;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.druidgreeneyes.rivet.core.util.IntDoubleConsumer;
import com.github.druidgreeneyes.rivet.core.vectorpermutations.Permutations;

public class DenseRIV implements RIV, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -4215652990755933410L;

  private final double[] vector;

  public DenseRIV(final double[] densePoints) {
    vector = Arrays.copyOf(densePoints, densePoints.length);
  }

  private DenseRIV(final int size) {
    vector = new double[size];
    Arrays.fill(vector, 0);
  }

  public DenseRIV(final int[] densePoints) {
    vector = new double[densePoints.length];
    for (int i = 0; i < densePoints.length; i++)
      vector[i] = densePoints[i];
  }

  public DenseRIV(final int[] indices,
                  final double[] values,
                  final int size) {
    this(size);
    for (int i = 0; i < indices.length; i++)
      vector[indices[i]] = values[i];
  }

  /*
   * @Override public DenseRIV add(final RIV other) { return
   * copy().destructiveAdd(other); }
   */

  public DenseRIV(final RIV source) {
    this(source.size());
    source.forEachNZ((IntDoubleConsumer) this::put);
  }

  public DenseRIV(final VectorElement[] points, final int size) {
    this(size);
    for (final VectorElement point : points)
      vector[point.index()] = point.value();
  }

  @Override
  public boolean contains(final int index) {
    return index >= 0 && index < vector.length;
  }

  @Override
  public DenseRIV copy() {
    return new DenseRIV(this);
  }

  @Override
  public int count() {
    return size();
  }

  @Override
  public DenseRIV destructiveAdd(final RIV other) {
    for (final VectorElement point : other.points())
      vector[point.index()] += point.value();
    return this;
  }

  @Override
  public DenseRIV destructiveAdd(final RIV... rivs) {
    IntStream.range(0, vector.length)
             .parallel()
             .forEach(i -> vector[i] += Arrays.stream(rivs)
                                              .parallel()
                                              .mapToDouble(riv -> riv.get(i))
                                              .sum());
    return this;
  }

  @Override
  public DenseRIV destructiveDiv(final double scalar) {
    for (int i = 0; i < vector.length; i++)
      vector[i] = vector[i] / scalar;
    return this;
  }

  /*
   * @Override public DenseRIV divide(final double scalar) { return
   * copy().destructiveDiv(scalar); }
   */

  @Override
  public DenseRIV destructiveMult(final double scalar) {
    for (int i = 0; i < vector.length; i++)
      vector[i] = vector[i] * scalar;
    return this;
  }

  /**
   * Doesn't do anything.
   */
  @Deprecated
  @Override
  public DenseRIV destructiveRemoveZeros() {
    return this;
  }

  @Override
  public DenseRIV destructiveSub(final RIV other) {
    for (final VectorElement point : other.points())
      vector[point.index()] -= point.value();
    return this;
  }

  @Override
  public DenseRIV destructiveSub(final RIV... rivs) {
    IntStream.range(0, vector.length)
             .parallel()
             .forEach(i -> vector[i] -= Arrays.stream(rivs)
                                              .parallel()
                                              .mapToDouble(riv -> riv.get(i))
                                              .sum());
    return this;
  }

  /*
   * @Override public double magnitude() { double sum = 0; for (final double v :
   * vector) sum += (v * v); return Math.sqrt(sum); }
   *
   * @Override public DenseRIV multiply(final double scalar) { return
   * copy().destructiveMult(scalar); }
   *
   * private DenseRIV destructiveNorm() { double sum = 0; for (final double v :
   * vector) sum += v; for (int i = 0; i < vector.length; i++) vector[i] =
   * vector[i] / sum; return this; }
   *
   * @Override public DenseRIV normalize() { return copy().destructiveNorm(); }
   */

  public boolean equals(final DenseRIV riv) {
    return Arrays.equals(vector, riv.vector);
  }

  @Override
  public boolean equals(final Object obj) {
    return RIVs.equals(this, obj);
  }

  @Override
  public void forEach(final IntDoubleConsumer fun) {
    for (int i = 0; i < vector.length; i++)
      fun.accept(i, vector[i]);
  }

  /*
   * @Override public DenseRIV subtract(final RIV other) throws
   * SizeMismatchException { return copy().destructiveSub(other); }
   */

  @Override
  public void forEachNZ(final IntDoubleConsumer fun) {
    forEach(fun);
  }

  @Override
  public double get(final int index) {
    return vector[index];
  }

  @Override
  public int hashCode() {
    return RIVs.hashcode(this);
  }

  @Override
  public int[] keyArr() {
    final int[] keys = new int[vector.length];
    for (int i = 0; i < keys.length; i++)
      keys[i] = i;
    return keys;
  }

  @Override
  public IntStream keyStream() {
    return IntStream.range(0, vector.length);
  }

  @Override
  public DenseRIV permute(final Permutations permutations, int times) {
    if (times == 0)
      return this;
    else {
      final int[] prm = times > 0
                                  ? permutations.permute
                                  : permutations.inverse;
      times = Math.abs(times);
      double[] res = Arrays.copyOf(vector, vector.length);
      final double[] p = new double[vector.length];
      for (int t = 0; t < times; t++) {
        for (int i = 0; i < res.length; i++)
          p[prm[i]] = res[i];
        res = Arrays.copyOf(p, p.length);
      }
      return new DenseRIV(res);
    }
  }

  @Override
  public VectorElement[] points() {
    final VectorElement[] points = new VectorElement[vector.length];
    for (int i = 0; i < vector.length; i++)
      points[i] = VectorElement.elt(i, vector[i]);
    return points;
  }

  @Override
  public Stream<VectorElement> pointStream() {
    return keyStream().mapToObj(i -> VectorElement.elt(i, vector[i]));
  }

  public double put(final int index, final double value) {
    final double v = vector[index];
    vector[index] = value;
    return v;
  }

  /**
   * Doesn't do anything.
   */
  @Deprecated
  @Override
  public DenseRIV removeZeros() {
    return this;
  }

  @Override
  public double saturation() {
    return 1;
  }

  @Override
  public int size() {
    return vector.length;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < vector.length; i++)
      sb.append(String.format("%d|%f ", i, vector[i]));
    sb.append("" + vector.length);
    return sb.toString();
  }

  @Override
  public double[] valArr() {
    return Arrays.copyOf(vector, vector.length);
  }

  @Override
  public DoubleStream valStream() {
    return Arrays.stream(vector);
  }

  public static DenseRIV empty(final int size) {
    return new DenseRIV(size);
  }

  public static DenseRIV fromString(final String string) {
    String[] bits = string.split(" ");
    final int size = Integer.parseInt(bits[bits.length - 1]);
    bits = Arrays.copyOf(bits, bits.length - 1);
    final VectorElement[] points = Arrays.stream(bits)
                                         .map(VectorElement::fromString)
                                         .toArray(VectorElement[]::new);
    return new DenseRIV(points, size);
  }

  public static RIV generate(final int size, final int nnz, final CharSequence token) {
    return RIVs.generateRIV(size, nnz, token, DenseRIV::new);
  }

  public static RIV generate(final int size,
                             final int nnz,
                             final CharSequence text,
                             final int tokenStart,
                             final int tokenWidth) {
    return RIVs.generateRIV(size, nnz, text, tokenStart, tokenWidth, DenseRIV::new);
  }
}
