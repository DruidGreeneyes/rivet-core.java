package com.github.druidgreeneyes.rivet.core.labels;

import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.druidgreeneyes.rivet.core.exceptions.SizeMismatchException;
import com.github.druidgreeneyes.rivet.core.util.IntDoubleConsumer;
import com.github.druidgreeneyes.rivet.core.util.Util;
import com.github.druidgreeneyes.rivet.core.vectorpermutations.Permutations;

/**
 * Interface used by RIV classes; provides all basic arithmetical operations (+
 * - / *), plus vector magnitude and normalization, basic utility methods
 * (contains, copy, count, etc), access to key and value streams, and a toString
 * method. Destructive variants of add/subtract are also available for the sake
 * of optimization, but because they modify the calling structure, neither
 * should be used in situations where the calling structure will need to be
 * referenced later on.
 *
 * @author jbox
 */
public interface RIV {

	/**
	 * @param other
	 *            : A Random Index Vector of the same size as this one.
	 * @return this + other
	 * @throws SizeMismatchException
	 */
	default RIV add(final RIV other) {
		return copy().destructiveAdd(other).destructiveRemoveZeros();
	}

	default RIV add(final RIV... rivs) {
		return copy().destructiveAdd(rivs).destructiveRemoveZeros();
	}

	/**
	 * @param index
	 * @return true if the value at this index in the random index vector != 0,
	 *         otherwise false.
	 * @throws IndexOutOfBoundsException
	 */
	boolean contains(final int index);

	/**
	 * @return a copy of this random index vector
	 */
	RIV copy();

	/**
	 * @return the number of non-zero elements in this random index vector.
	 */
	int count();

	/**
	 * Destructive add/subtract methods are provided for optimization purposes,
	 * but because they modify the calling structure, neither should be used in
	 * situations where the calling structure will need to be referenced later
	 * on.<br/>
	 *
	 * <pre>
	 * Good: using an empty RIV to accumulate a sum
	 *
	 *      given Stream&#060;MapRIV&#062; rivs, int size,
	 *              MapRIV sum = rivs.reduce(new MapRIV(size),
	 *                      (identity, riv) -&#062; identity.destructiveAdd(riv));
	 *
	 * Bad: using destructiveAdd to produce a throwaway result for comparison
	 *
	 *      given List&#060;MapRIV&#062; rivs, MapRIV modifier, MapRIV test
	 *              List&#060;MapRIV&#062; satisfied = rivs.stream().filter(
	 *                      r -&#062; r.destructiveAdd(modifier).equals(test))
	 *                      .collect(Collectors.toList());
	 *              return rivs.containsAll(satisfied);
	 *              //returns False!
	 * </pre>
	 *
	 * <br/>
	 * <br/>
	 *
	 * NOTE: The destructive add/subtract methods do not check for SizeMismatch,
	 * so may cause strange behavior if not used with care.
	 *
	 * @param other
	 *            : A Random Index Vector of the same size as this one.
	 * @return this + other
	 */
	RIV destructiveAdd(final RIV other);

	RIV destructiveAdd(final RIV... rivs);

	RIV destructiveDiv(final double scalar);

	RIV destructiveMult(final double scalar);

	RIV destructiveRemoveZeros();

	/**
	 * Destructive add/subtract methods are provided for optimization purposes,
	 * but because they modify the calling structure, neither should be used in
	 * situations where the calling structure will need to be referenced later
	 * on.<br/>
	 *
	 * <pre>
	 * Good: using an empty RIV to accumulate a sum
	 *
	 *      given Stream&#060;MapRIV&#062; rivs, int size,
	 *              MapRIV sum = rivs.reduce(new MapRIV(size),
	 *                      (identity, riv) -&#062; identity.destructiveAdd(riv));
	 *
	 * Bad: using destructiveAdd to produce a throwaway result for comparison
	 *
	 *      given List&#060;MapRIV&#062; rivs, MapRIV modifier, MapRIV test
	 *              List&#060;MapRIV&#062; satisfied = rivs.stream().filter(
	 *                      r -&#062; r.destructiveAdd(modifier).equals(test))
	 *                      .collect(Collectors.toList());
	 *              return rivs.containsAll(satisfied);
	 *              //returns False!
	 * </pre>
	 *
	 * <br/>
	 * <br/>
	 *
	 * NOTE: The destructive add/subtract methods do not check for SizeMismatch,
	 * so may cause strange behavior if not used with care.
	 *
	 * @param other
	 *            : A Random Index Vector of the same size as this one.
	 * @return this - other
	 */
	RIV destructiveSub(final RIV other);

	RIV destructiveSub(final RIV... rivs);

	/**
	 * Perform element-wise division
	 *
	 * @param scalar
	 *            : A double
	 * @return a copy of this, where each element has been divided by scalar
	 */
	default RIV divide(final double scalar) {
		return copy().destructiveDiv(scalar).destructiveRemoveZeros();
	}

	@Override
	boolean equals(Object other);

	default boolean equals(RIV other) {
        if(size() != other.size())
        	return false;
        else
        	for (int i = 0; i < size(); i++)
        		if (!Util.doubleEquals(get(i), other.get(i)))
        			return false;
        return true;
	}

	/**
	 * Written for the sake of making it easier to string dot-chains together.
	 *
	 * @param fun
	 *            : A function that takes a RIV and returns a value.
	 * @return fun.apply(this)
	 */
	default <T> T evert(final Function<RIV, T> fun) {
		return fun.apply(this);
	}

	void forEach(IntDoubleConsumer fun);

	/**
	 * @param index
	 * @return the value at this index in the random index vector
	 * @throws IndexOutOfBoundsException
	 */
	double get(final int index) throws IndexOutOfBoundsException;

	@Override
	int hashCode();

	int[] keyArr();

	/**
	 * @return the non-zero indices of this random index vector, as an
	 *         IntStream.
	 */
	IntStream keyStream();

	/**
	 * Magnitude represents the length of this random index vector, or the
	 * distance between its coordinate representation and the coordinate origin
	 * (0, 0, 0, ...).
	 *
	 * @return Math.sqrt(this.valStream().map(x -> x * x).sum())
	 */
	default double magnitude() {
		return Math.sqrt(valStream().map(x -> x * x).sum());
	}

	/**
	 * Perform element-wise multiplication
	 *
	 * @param scalar
	 *            : A double
	 * @return a copy of this, where each element has been multiplied by scalar
	 */
	default RIV multiply(final double scalar) {
		return copy().destructiveMult(scalar);
	}

	/**
	 * Create a random index vector with the same proportions as this one, but
	 * whose magnitude is 1.
	 *
	 * @return this.divide(this.magnitude())
	 */
	default RIV normalize() {
		return divide(magnitude());
	}

	/**
	 * Permute this random index vector using a specified permutation pair, one
	 * of which is the inverse of the other, such that rivA.permute(perms,
	 * 2).permute(-2).equals(rivA).
	 *
	 * @param permutations
	 *            : a permutation pair, generally got by calling
	 *            rivet.core.vectorpermutations.Permutations.generate(size).
	 * @param times
	 *            : An int
	 * @return a permuted copy of this random index vector
	 */
	RIV permute(final Permutations permutations, final int times);

	/**
	 * @return An array of VectorElements representing the non-zero points in
	 *         this random index vector.
	 */
	VectorElement[] points();

	Stream<VectorElement> pointStream();

	default RIV removeZeros() {
		return copy().destructiveRemoveZeros();
	}

	default double saturation() {
		return count() / (double)size();
	}

	/**
	 *
	 * @param riv
	 * @return RIVs.similarity(this, riv)
	 */
	default double similarityTo(final RIV riv) {
		return RIVs.similarity(this, riv);
	}

	/**
	 * @return the number of dimensions in this random index vector
	 */
	int size();

	default RIV subtract(final RIV... rivs) {
		return copy().destructiveSub(rivs).destructiveRemoveZeros();
	}

	/**
	 * @param other
	 *            : A Random Index Vector of the same size as this one.
	 * @return this - other
	 * @throws SizeMismatchException
	 */
	default RIV subtract(final RIV other) {
		return copy().destructiveSub(other).destructiveRemoveZeros();
	}

	default DenseRIV toDense() {
		return new DenseRIV(this);
	}

	default ImmutableRIV toImmutable() {
		return new ImmutableRIV(this);
	}

	/**
	 * <pre>
	 * "0|1 1|3 4|2 5"
	 *
	 * "I|V I|V I|V Size"
	 * </pre>
	 *
	 * @return a String representation of this random index vector.
	 */
	@Override
	String toString();

	double[] valArr();

	/**
	 * @return the non-zero values of this random index vector, as a
	 *         DoubleStream.
	 */
	DoubleStream valStream();
}
