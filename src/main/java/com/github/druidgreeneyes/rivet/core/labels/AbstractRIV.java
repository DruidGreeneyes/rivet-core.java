package com.github.druidgreeneyes.rivet.core.labels;

import java.io.Serializable;
import org.apache.commons.lang3.ArrayUtils;

import com.github.druidgreeneyes.rivet.core.util.IntDoubleConsumer;
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
  public abstract AbstractRIV copy();

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
  public double dot(final RIV riv) {
    double sum = 0;
    for (final VectorElement p : points())
      sum += p.value() * riv.get(p.index());
    return sum;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other)
      return true;
    else if (ArrayUtils.contains(other.getClass().getInterfaces(), RIV.class))
      return equals(other);
    else
      return false;
  }

  public abstract boolean equals(final RIV other);

  public boolean equals(final AbstractRIV other) {
    for (int i = 0; i < other.size(); i++)
      if (get(i) != other.get(i))
        return false;
    return true;
  }

  @Override
  public void forEach(final IntDoubleConsumer fun) {
    for (int i = 0; i < size(); i++)
      fun.accept(i, get(i));
  };

  /**
   * Implements the hash function found in java.lang.String, using values in
   * place of characters. Modifying the RIV is virtually guaranteed to change
   * the hashcode.
   */
  @Override
  public int hashCode() {
    int sum = 0;
    final double[] vals = valArr();
    for (int i = 0; i < vals.length; i++)
      sum += vals[i] * (31 ^ vals.length - 1 - i);
    return sum;
  }

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
  public AbstractRIV removeZeros() {
    return copy().destructiveRemoveZeros();
  }

  @Override
  public double saturation() {
    return count() / (double) size();
  }

  @Override
  public double similarityTo(final RIV riv) {
    final double mag = magnitude() * riv.magnitude();
    return mag == 0
                    ? 0
                    : dot(riv) / mag;
  }

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
    destructiveRemoveZeros();
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < size(); i++)
      if (contains(i))
        sb.append(String.format("%d|%f ", i, get(i)));
    sb.append(size());
    return sb.toString();
  }
}
