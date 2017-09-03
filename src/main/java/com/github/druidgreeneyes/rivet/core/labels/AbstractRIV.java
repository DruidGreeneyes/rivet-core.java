package com.github.druidgreeneyes.rivet.core.labels;

import java.io.Serializable;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.druidgreeneyes.rivet.core.util.IntDoubleConsumer;
import com.github.druidgreeneyes.rivet.core.util.Util;
import com.github.druidgreeneyes.rivet.core.vectorpermutations.Permutations;

public abstract class AbstractRIV implements RIV, Serializable {
  /**
   *
   */
  private static final long serialVersionUID = -3648672040431659942L;

  @Override
  public AbstractRIV add(final RIV other) {
    return copy().destructiveAdd(other).destructiveRemoveZeros();
  }

  @Override
  public AbstractRIV add(final RIV... rivs) {
    return copy().destructiveAdd(rivs).destructiveRemoveZeros();
  }

  @Override
  public abstract boolean contains(final int index);

  @Override
  public abstract AbstractRIV copy();

  @Override
  public abstract int count();

  @Override
  public abstract AbstractRIV destructiveAdd(final RIV other);

  @Override
  public abstract AbstractRIV destructiveAdd(final RIV... rivs);

  @Override
  public abstract AbstractRIV destructiveDiv(final double scalar);

  @Override
  public abstract AbstractRIV destructiveMult(final double scalar);

  @Override
  public abstract AbstractRIV destructiveRemoveZeros();

  @Override
  public abstract AbstractRIV destructiveSub(final RIV other);

  @Override
  public abstract AbstractRIV destructiveSub(final RIV... rivs);

  @Override
  public AbstractRIV divide(final double scalar) {
    return copy().destructiveDiv(scalar).destructiveRemoveZeros();
  }

  @Override
  public abstract boolean equals(Object other);

  @Override
  public boolean equals(final RIV other) {
    if (this == other)
      return true;
    if (size() != other.size())
      return false;
    else
      for (int i = 0; i < size(); i++)
      if (!Util.doubleEquals(get(i), other.get(i)))
        return false;
    return true;
  }

  @Override
  public void forEach(final IntDoubleConsumer fun) {
    for (int i = 0; i < size(); i++)
      fun.accept(i, get(i));
  }

  @Override
  public abstract void forEachNZ(IntDoubleConsumer fun);

  @Override
  public abstract double get(final int index) throws IndexOutOfBoundsException;

  @Override
  public abstract int hashCode();

  @Override
  public abstract int[] keyArr();

  @Override
  public abstract IntStream keyStream();

  @Override
  public double magnitude() {
    return Math.sqrt(valStream().map(x -> x * x).sum());
  }

  @Override
  public AbstractRIV multiply(final double scalar) {
    return copy().destructiveMult(scalar);
  }

  @Override
  public AbstractRIV normalize() {
    return divide(magnitude());
  }

  @Override
  public abstract AbstractRIV permute(final Permutations permutations,
                                      final int times);

  @Override
  public abstract VectorElement[] points();

  @Override
  public abstract Stream<VectorElement> pointStream();

  @Override
  public abstract double put(final int index, final double value);

  @Override
  public AbstractRIV removeZeros() {
    return copy().destructiveRemoveZeros();
  }

  @Override
  public double saturation() {
    return count() / (double) size();
  }

  @Override
  public double similarityTo(final RIV riv) {
    return RIVs.similarity(this, riv);
  }

  @Override
  public abstract int size();

  @Override
  public AbstractRIV subtract(final RIV... rivs) {
    return copy().destructiveSub(rivs).destructiveRemoveZeros();
  }

  @Override
  public AbstractRIV subtract(final RIV other) {
    return copy().destructiveSub(other).destructiveRemoveZeros();
  }

  @Override
  public DenseRIV toDense() {
    return new DenseRIV(this);
  }

  @Override
  public ImmutableRIV toImmutable() {
    return new ImmutableRIV(this);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    forEachNZ((i, v) -> {
      sb.append(String.format("%d|%f ", i, v));
    });
    sb.append(size());
    return sb.toString();
  }

  @Override
  public abstract double[] valArr();

  @Override
  public abstract DoubleStream valStream();
}
