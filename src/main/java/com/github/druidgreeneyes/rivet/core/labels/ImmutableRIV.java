package com.github.druidgreeneyes.rivet.core.labels;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;

import com.github.druidgreeneyes.rivet.core.util.IntDoubleConsumer;
import com.github.druidgreeneyes.rivet.core.util.Util;
import com.github.druidgreeneyes.rivet.core.util.hilbert.Hilbert;
import com.github.druidgreeneyes.rivet.core.vectorpermutations.Permutations;

public class ImmutableRIV implements RIV {

  private static final DoubleBinaryOperator add = (a, b) -> a + b;

  private static final DoubleBinaryOperator subtract = (a, b) -> a - b;

  private final int size;

  private final int[] keys;

  private final double[] vals;

  public ImmutableRIV(final int size) {
    this.size = size;
    keys = new int[0];
    vals = new double[0];
  }

  public ImmutableRIV(final int[] keys,
                      final double[] vals,
                      final int size) {
    this.size = size;
    this.keys = Arrays.copyOf(keys, keys.length);
    this.vals = Arrays.copyOf(vals, vals.length);
  }

  public ImmutableRIV(final RIV riv) {
    this(riv.keyArr(), riv.valArr(), riv.size());
  }

  @Override
  public ImmutableRIV add(final RIV other) {
    return merge(add, other);
  }

  @Override
  public ImmutableRIV add(final RIV... others) {
    return merge(add, others);
  }

  @Override
  public boolean contains(final int index) {
    return ArrayUtils.contains(keys, index);
  }

  @Override
  public RIV copy() {
    return new ImmutableRIV(keys, vals, size);
  }

  @Override
  public int count() {
    return keys.length;
  }

  @Override
  public ImmutableRIV destructiveAdd(final RIV other) {
    throw new NotImplementedException(
                                      "Destructive methods not available on Immutable RIV.");
  }

  @Override
  public ImmutableRIV destructiveAdd(final RIV... rivs) {
    throw new NotImplementedException(
                                      "Destructive methods not available on Immutable RIV.");
  }

  @Override
  public ImmutableRIV destructiveDiv(final double scalar) {
    throw new NotImplementedException(
                                      "Destructive methods not available on Immutable RIV.");
  }

  @Override
  public ImmutableRIV destructiveMult(final double scalar) {
    throw new NotImplementedException(
                                      "Destructive methods not available on Immutable RIV.");
  }

  @Override
  public ImmutableRIV destructiveRemoveZeros() {
    throw new NotImplementedException(
                                      "Destructive methods not available on Immutable RIV.");
  }

  @Override
  public ImmutableRIV destructiveSub(final RIV other) {
    throw new NotImplementedException(
                                      "Destructive methods not available on Immutable RIV.");
  }

  @Override
  public ImmutableRIV destructiveSub(final RIV... rivs) {
    throw new NotImplementedException(
                                      "Destructive methods not available on Immutable RIV.");
  }

  @Override
  public ImmutableRIV divide(final double scalar) {
    return map(divideBy(scalar));
  }

  public boolean equals(final ImmutableRIV other) {
    return size == other.size && Arrays.equals(keys, other.keys)
           && Arrays.equals(vals, other.vals);
  }

  @Override
  public boolean equals(final Object obj) {
    return RIVs.equals(this, obj);
  }

  @Override
  public void forEachNZ(final IntDoubleConsumer fun) {
    for (int i = 0; i < keys.length; i++)
      fun.accept(keys[i], vals[i]);
  }

  @Override
  public double get(final int index) throws IndexOutOfBoundsException {
    if (index < 0 || index >= size)
                                    throw new IndexOutOfBoundsException();
    final int i = ArrayUtils.indexOf(keys, index);
    if (i == ArrayUtils.INDEX_NOT_FOUND)
      return 0;
    else
      return vals[i];
  }

  public BigInteger getFHilbertKey() {
    return Hilbert.fEncodeHilbertKey(this);
  }

  public BigInteger getHilbertKey() {
    return Hilbert.encodeHilbertKey(this);
  }

  public BigInteger getHilbillyKey() {
    return Hilbert.encodeHilbillyKey(this);
  }

  public BigInteger getSHilbertKey() {
    return Hilbert.sEncodeHilbertKey(this);
  }

  public BigInteger getSHilbillyKey() {
    return Hilbert.sEncodeHilbillyKey(this);
  }

  // implements the String hashCode function;
  @Override
  public int hashCode() {
    int sum = 0;
    for (int i = 0; i < vals.length; i++)
      sum += vals[i] * (31 ^ vals.length - 1 - i);
    return sum;
  }

  public int hashCode2() {
    int sum = 0;
    for (final double v : vals)
      sum += v;
    return sum;
  }

  @Override
  public int[] keyArr() {
    return Arrays.copyOf(keys, keys.length);
  }

  @Override
  public IntStream keyStream() {
    return Arrays.stream(keys);
  }

  @Override
  public double magnitude() {
    double sum = 0;
    for (final double v : vals)
      sum += v * v;
    return Math.sqrt(sum);
  }

  private ImmutableRIV map(final DoubleUnaryOperator operation) {
    double[] newVals = new double[vals.length];
    int[] zeros = new int[0];
    for (int i = 0; i < vals.length; i++) {
      newVals[i] = operation.applyAsDouble(vals[i]);
      if (newVals[i] == 0)
                           zeros = ArrayUtils.add(zeros, i);
    }
    final int[] newKeys = ArrayUtils.removeAll(keys, zeros);
    newVals = ArrayUtils.removeAll(newVals, zeros);
    return new ImmutableRIV(newKeys, newVals, size);
  }

  private ImmutableRIV merge(final DoubleBinaryOperator mergeFunction,
                             final RIV other) {
    int[] newKeys = IntStream.concat(keyStream(), other.keyStream())
                             .distinct()
                             .sorted()
                             .toArray();
    double[] newVals = new double[newKeys.length];
    int[] zeros = new int[0];
    for (int i = 0; i < newKeys.length; i++) {
      final int k = newKeys[i];
      newVals[i] = mergeFunction.applyAsDouble(get(k), other.get(k));
      if (newVals[i] == 0)
                           zeros = ArrayUtils.add(zeros, i);
    }
    newKeys = ArrayUtils.removeAll(newKeys, zeros);
    newVals = ArrayUtils.removeAll(newVals, zeros);
    return new ImmutableRIV(newKeys, newVals, size);
  }

  private ImmutableRIV merge(final DoubleBinaryOperator mergeFunction,
                             final RIV... others) {
    int[] newKeys = Stream.concat(Stream.of(this), Arrays.stream(others))
                          .flatMapToInt(RIV::keyStream)
                          .distinct()
                          .sorted()
                          .toArray();
    double[] newVals = new double[newKeys.length];
    int[] zeros = new int[0];
    for (int i = 0; i < newKeys.length; i++) {
      final int k = newKeys[i];
      double v = get(k);
      for (final RIV riv : others)
        v = mergeFunction.applyAsDouble(v, riv.get(k));
      if (Util.doubleEquals(v, 0, Util.roundingError))
                                                       zeros = ArrayUtils.add(zeros, i);
      newVals[i] = v;
    }
    newKeys = ArrayUtils.removeAll(newKeys, zeros);
    newVals = ArrayUtils.removeAll(newVals, zeros);
    return new ImmutableRIV(newKeys, newVals, size);
  }

  @Override
  public ImmutableRIV multiply(final double scalar) {
    return map(multiplyBy(scalar));
  }

  @Override
  public ImmutableRIV permute(final Permutations permutations,
                              final int times) {
    if (times == 0)
                    return this;
    return new ImmutableRIV(times > 0
                                      ? RIVs.permuteKeys(keyArr(), permutations.permute, times)
                                      : RIVs.permuteKeys(keyArr(), permutations.inverse, -times),
                            valArr(),
                            size);
  }

  @Override
  public VectorElement[] points() {
    final VectorElement[] points = new VectorElement[keys.length];
    for (int i = 0; i < keys.length; i++)
      points[i] = VectorElement.elt(keys[i], vals[i]);
    return points;
  }

  @Override
  public Stream<VectorElement> pointStream() {
    return IntStream.range(0, keys.length)
                    .mapToObj(i -> VectorElement.elt(keys[i], vals[i]));
  }

  @Override
  public ImmutableRIV removeZeros() {
    return this;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public ImmutableRIV subtract(final RIV other) {
    return merge(subtract, other);
  }

  @Override
  public ImmutableRIV subtract(final RIV... others) {
    return merge(subtract, others);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (final VectorElement point : points())
      sb.append(point.toString() + " ");
    sb.append(size);
    return sb.toString();
  }

  @Override
  public double[] valArr() {
    return Arrays.copyOf(vals, vals.length);
  }

  @Override
  public DoubleStream valStream() {
    return Arrays.stream(vals);
  }

  private static DoubleUnaryOperator divideBy(final double scalar) {
    return a -> a / scalar;
  }

  public static RIV generate(final int size, final int nnz, final CharSequence token) {
    return RIVs.generateRIV(size, nnz, token, ImmutableRIV::new);
  }

  public static RIV generate(final int size,
                             final int nnz,
                             final CharSequence text,
                             final int tokenStart,
                             final int tokenEnd) {
    return generate(size, nnz, text.subSequence(tokenStart, tokenEnd));
  }

  private static DoubleUnaryOperator multiplyBy(final double scalar) {
    return a -> a * scalar;
  }
}
