package rivet.core.labels;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import pair.Pair;
import rivet.core.exceptions.SizeMismatchException;
import rivet.core.vectorpermutations.Permutations;

public class DenseRIV implements RIV {
	
	private double[] vector;
	
	public DenseRIV(final int size, final VectorElement[] points) {
		vector = new double[size];
		Arrays.fill(vector, 0);
		for (VectorElement point : points) {
			vector[point.index()] = point.value();
		}
	}
	
	public DenseRIV(RIV source) {
		this(source.size(), source.points());
	}

	@Override
	public DenseRIV add(RIV other) {
		return copy().destructiveAdd(other);
	}
	


	@Override
	public boolean contains(int index) {
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
	public DenseRIV destructiveAdd(RIV other) {
		for (VectorElement point : other.points())
			vector[point.index()] = point.value();
		return this;
	}

	@Override
	public DenseRIV destructiveSub(RIV other) {
		for (VectorElement point : other.points())
			vector[point.index()] -= point.value();
		return this;
	}
	
	public DenseRIV destructiveDiv(double scalar) {
		for (int i = 0; i < vector.length; i++)
			vector[i] = vector[i] / scalar;
		return this;
	}
	
	public DenseRIV destructiveMult(double scalar) {
		for (int i = 0; i < vector.length; i++)
			vector[i] = vector[i] * scalar;
		return this;
	}

	@Override
	public DenseRIV divide(double scalar) {
		return copy().destructiveDiv(scalar);
	}

	@Override
	public double get(int index) {
		return vector[index];
	}

	@Override
	public IntStream keyStream() {
		return IntStream.range(0, vector.length);
	}

	@Override
	public double magnitude() {
		double sum = 0;
		for (double v : vector)
			sum += (v * v);
		return Math.sqrt(sum);
	}

	@Override
	public DenseRIV multiply(double scalar) {
		return copy().destructiveMult(scalar);
	}
	
	private DenseRIV destructiveNorm() {
		double sum = 0;
		for (double v : vector)
			sum += v;
		for (int i = 0; i < vector.length; i++)
			vector[i] = vector[i] / sum;
		return this;
	}

	@Override
	public DenseRIV normalize() {
		return copy().destructiveNorm();
	}

	@Override
	public DenseRIV permute(Permutations permutations, int times) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VectorElement[] points() {
		return keyStream().mapToObj(i -> Pair.make(i, vector[i])).toArray(VectorElement[]::new);
	}

	@Override
	public int size() {
		return vector.length;
	}

	@Override
	public DenseRIV subtract(RIV other) throws SizeMismatchException {
		return copy().destructiveSub(other);
	}

	@Override
	public DoubleStream valStream() {
		return Arrays.stream(vector);
	}
	
	@Override
	public double saturation() {
		return 1;
	}

}
