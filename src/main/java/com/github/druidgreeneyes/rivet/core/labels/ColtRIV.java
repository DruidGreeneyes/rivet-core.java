package com.github.druidgreeneyes.rivet.core.labels;

import static com.github.druidgreeneyes.rivet.core.util.colt.ColtConversions.procedurize;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.druidgreeneyes.rivet.core.util.IntDoubleConsumer;
import com.github.druidgreeneyes.rivet.core.vectorpermutations.Permutations;

import cern.colt.map.tdouble.OpenIntDoubleHashMap;
import cern.jet.math.tdouble.DoubleMult;

/**
 * Merging cern.colt.map.OpenIntDoubleHashMap with java 8 Map in order to
 * primitive-based mappings that are both fast -and- easy to use.
 *
 * @author josh
 *
 */
public class ColtRIV extends AbstractRIV implements RIV, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 7489480432514925162L;

  public final int size;
  private final OpenIntDoubleHashMap data;

  public ColtRIV(final int size) {
    this.size = size;
    data = new OpenIntDoubleHashMap();
  }

  public ColtRIV(final int[] indices, final double[] values, final int size) {
    data = new OpenIntDoubleHashMap();
    for (int i = 0; i < indices.length; i++)
      data.put(indices[i], values[i]);
    this.size = size;
  }

  public ColtRIV(final RIV riv) {
    this(riv.size());
    riv.forEachNZ(data::put);
  }

  public ColtRIV(final VectorElement[] points, final int size) {
    this(size);
    for (final VectorElement point : points)
      data.put(point.index(), point.value());
  }

  @Override
  public boolean contains(final int index) throws IndexOutOfBoundsException {
    return data.containsKey(index);
  }

  @Override
  public ColtRIV copy() {
    return new ColtRIV(this);
  }

  @Override
  public int count() {
    return data.size();
  }

  @Override
  public ColtRIV destructiveAdd(final RIV other) {
    for (final int i : other.keyArr())
      data.put(i, get(i) + other.get(i));
    return this;
  }

  @Override
  public ColtRIV destructiveAdd(final RIV... rivs) {
    for (int i = 0; i < size; i++) {
      double v = get(i);
      for (final RIV riv : rivs)
        v += riv.get(i);
      if (v == 0)
        data.removeKey(i);
      else
        data.put(i, v);
    }
    return this;
  }

  @Override
  public ColtRIV destructiveDiv(final double scalar) {
    data.assign(DoubleMult.div(scalar));
    return this;
  }

  @Override
  public ColtRIV destructiveMult(final double scalar) {
    data.assign(DoubleMult.mult(scalar));
    return this;
  }

  @Override
  public ColtRIV destructiveRemoveZeros() {
    int i;
    while (Integer.MIN_VALUE != (i = data.keyOf(0.0)))
      data.removeKey(i);
    return this;
  }

  /*
   * @Override public ColtRIV add(final RIV other) throws SizeMismatchException
   * { return copy().destructiveAdd(other); }
   */

  @Override
  public ColtRIV destructiveSub(final RIV other) {
    for (final int i : other.keyArr())
      data.put(i, get(i) - other.get(i));
    return this;
  }

  @Override
  public ColtRIV destructiveSub(final RIV... rivs) {
    for (int i = 0; i < size; i++) {
      double v = get(i);
      for (final RIV riv : rivs)
        v -= riv.get(i);
      if (v == 0)
        data.removeKey(i);
      else
        data.put(i, v);
    }
    return this;
  }

  @Override
  public boolean equals(final RIV other) {
    if (other instanceof ColtRIV)
      return equals((ColtRIV) other);
    else
      return equals((AbstractRIV) other);
  }

  public boolean equals(final ColtRIV other) {
    return size == other.size() && data.equals(other.data);
  }

  @Override
  public void forEachNZ(final IntDoubleConsumer fun) {
    data.forEachPair(procedurize(fun));
  }

  /*
   * @Override public ColtRIV divide(final double scalar) { return
   * copy().destructiveDiv(scalar); }
   */

  @Override
  public int[] keyArr() {
    final int[] keys = new int[count()];
    int c = 0;
    for (int i = 0; i < size; i++)
      if (data.containsKey(i)) {
        keys[c] = i;
        c++;
      }
    return keys;
  }

  @Override
  public IntStream keyStream() {
    return Arrays.stream(keyArr());
  }

  /*
   * @Override public double magnitude() { return Math.sqrt(valStream().map(x ->
   * x * x) .sum()); }
   *
   * @Override public ColtRIV multiply(final double scalar) { return
   * copy().destructiveMult(scalar); }
   */

  @Override
  public ColtRIV permute(final Permutations permutations, final int times) {
    if (times == 0)
      return this;
    else
      return new ColtRIV(times > 0
                                   ? RIVs.permuteKeys(keyArr(),
                                                      permutations.permute,
                                                      times)
                                   : RIVs.permuteKeys(keyArr(),
                                                      permutations.inverse,
                                                      -times),
                         valArr(),
                         size);
  }

  /*
   * @Override public ColtRIV normalize() { final double mag = magnitude();
   * final ColtRIV res = copy(); res.assign(x -> x / mag); return res; }
   */

  @Override
  public VectorElement[] points() {
    final VectorElement[] points = new VectorElement[count()];
    final AtomicInteger c = new AtomicInteger();
    data.forEachPair(procedurize((k,
                                  v) -> points[c.getAndIncrement()] = VectorElement.elt(k,
                                                                                        v)));
    Arrays.sort(points);
    return points;
  }

  @Override
  public Stream<VectorElement> pointStream() {
    final Stream.Builder<VectorElement> sb = Stream.builder();
    data.forEachPair(procedurize((k, v) -> sb.accept(VectorElement.elt(k, v))));
    return sb.build();
  }

  @Override
  public double put(final int index, final double value) {
    final double v = get(index);
    data.put(index, value);
    return v;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public double[] valArr() {
    final double[] vals = new double[count()];
    int c = 0;
    for (int i = 0; i < size; i++)
      if (data.containsKey(i)) {
        vals[c] = get(i);
        c++;
      }
    return vals;
  }

  /*
   * @Override public ColtRIV subtract(final RIV other) throws
   * SizeMismatchException { return copy().destructiveSub(other); }
   */

  @Override
  public DoubleStream valStream() {
    final DoubleStream.Builder sb = DoubleStream.builder();
    data.forEachPair(procedurize((k, v) -> sb.accept(v)));
    return sb.build();
  }

  /*
   * @Override public ColtRIV removeZeros() { return
   * copy().destructiveRemoveZeros(); }
   */

  public static ColtRIV empty(final int size) {
    return new ColtRIV(size);
  }

  public static ColtRIV fromString(final String string) {
    String[] bits = string.split(" ");
    final int size = Integer.parseInt(bits[bits.length - 1]);
    bits = Arrays.copyOf(bits, bits.length - 1);
    final VectorElement[] elements = Arrays.stream(bits)
                                           .map(VectorElement::fromString)
                                           .toArray(VectorElement[]::new);
    return new ColtRIV(elements, size);
  }

  public static RIV generate(final int size, final int nnz,
                             final CharSequence token) {
    return RIVs.generateRIV(size, nnz, token, ColtRIV::new);
  }

  public static RIV generate(final int size,
                             final int nnz,
                             final CharSequence text,
                             final int tokenStart,
                             final int tokenWidth) {
    return RIVs.generateRIV(size, nnz, text, tokenStart, tokenWidth,
                            ColtRIV::new);
  }

  @Override
  public double get(final int index) {
    if (data.containsKey(index))
      return data.get(index);
    else
      return 0.0;
  }

  public static RIVConstructor getConstructor() {
    return ColtRIV::new;
  }
}
