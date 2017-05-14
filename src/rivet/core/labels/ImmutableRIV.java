package rivet.core.labels;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;

import rivet.core.util.IntDoubleConsumer;
import rivet.core.util.Util;
import rivet.core.util.hilbert.Hilbert;
import rivet.core.vectorpermutations.Permutations;

public class ImmutableRIV implements RIV {

  private static BigInteger DEFAULT_KEY = BigInteger.valueOf(-1L);

  private static final DoubleBinaryOperator add = (a, b) -> a + b;

  private static final DoubleBinaryOperator subtract = (a, b) -> a - b;

  private static DoubleUnaryOperator divideBy(final double scalar) {
    return a -> a / scalar;
  }

  public static ImmutableRIV empty(final int size) {
    return new ImmutableRIV(ArrayUtils.EMPTY_INT_ARRAY,
                            ArrayUtils.EMPTY_DOUBLE_ARRAY, size);
  }

  public static ImmutableRIV fromString(final String str) {
    final String[] parts = str.split(" ");
    final int size = Integer.parseInt(parts[parts.length - 1]);
    final int[] keys = new int[parts.length - 1];
    final double[] vals = new double[parts.length - 1];
    for (int i = 0; i < parts.length - 1; i++) {
      final String[] bits = parts[i].split("\\|");
      keys[i] = Integer.parseInt(bits[0]);
      vals[i] = Double.parseDouble(bits[1]);
    }
    return new ImmutableRIV(keys, vals, size);
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
  public static ImmutableRIV generateLabel(final int size, final int k,
                                           final CharSequence word) {
    final long seed = RIVs.makeSeed(word);
    final int j = k % 2 == 0
                             ? k
                             : k + 1;
    return new ImmutableRIV(RIVs.makeIndices(size, j, seed),
                            RIVs.makeVals(j, seed), size);
  }

  private static DoubleUnaryOperator multiplyBy(final double scalar) {
    return a -> a * scalar;
  }

  private static void permute(final VectorElement[] points,
                              final int[] permutation) {
    for (final VectorElement point : points)
      point.destructiveSet(permutation[point.index()]);
  }

  private final int size;

  private final int[] keys;

  private final double[] vals;

  private BigInteger key = DEFAULT_KEY;

  private String keyType = "";

  public ImmutableRIV(final int size) {
    this.size = size;
    keys = new int[0];
    vals = new double[0];
  }

  private ImmutableRIV(final int size, final VectorElement[] points) {
    this.size = size;
    final int[] ks = new int[points.length];
    final double[] vs = new double[points.length];
    int[] zeros = new int[0];
    for (int i = 0; i < points.length; i++) {
      ks[i] = points[i].index();
      vs[i] = points[i].value();
      if (vs[i] == 0)
        zeros = ArrayUtils.add(zeros, i);
    }
    keys = ArrayUtils.removeAll(ks, zeros);
    vals = ArrayUtils.removeAll(vs, zeros);
  }

  public ImmutableRIV(final int[] keys, final double[] vals,
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
  public void forEach(final IntDoubleConsumer fun) {
    for (int i = 0; i < keys.length; i++)
      fun.accept(keys[i], vals[i]);
  }

  @Override
  public double get(final int index) throws IndexOutOfBoundsException {
    try {
      return vals[ArrayUtils.indexOf(keys, index)];
    } catch (final ArrayIndexOutOfBoundsException e) {
      if (0 <= index && index < size)
        return 0;
      else
        throw new ArrayIndexOutOfBoundsException(index);
    }
  }

  public BigInteger getHilbertKey() {
    if (keyType != "hilbert") {
      key = Hilbert.fEncodeHilbertKey(this);
      keyType = "hilbert";
    }
    return key;
  }

  public BigInteger getHilbillyKey() {
    if (keyType != "hilbilly") {
      key = Hilbert.encodeHilbillyKey(this);
      keyType = "hilbilly";
    }
    return key;
  }

  public BigInteger getSHilbertKey() {
    if (keyType != "sHilbert") {
      key = Hilbert.sEncodeHilbertKey(this);
      keyType = "sHilbert";
    }
    return key;
  }

  public BigInteger getSHilbillyKey() {
    if (keyType != "sHilbilly") {
      key = Hilbert.sEncodeHilbillyKey(this);
      keyType = "sHilbilly";
    }
    return key;
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
    final int[] permutation = times > 0
                                        ? permutations.left
                                        : permutations.right;
    final int t = Math.abs(times);
    final VectorElement[] points = points();
    for (int i = 0; i < t; i++)
      permute(points, permutation);
    Arrays.sort(points);
    return new ImmutableRIV(size, points);
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
}
