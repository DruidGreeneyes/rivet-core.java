package rivet.core.vectorpermutations;

import org.apache.commons.lang3.ArrayUtils;

import scala.Tuple2;

import static rivet.core.util.Util.randInts;

public class Permutations extends Tuple2<int[], int[]> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8713804060993130581L;

	private Permutations(final int[] permute, final int[] inverse) {
		super(permute, inverse);
	}

	public static Permutations generate(final int size) {
		int[] permutation = randInts(size, size, 0L)
				.toArray();
		int[] inverse = new int[size];
		for (int i = 0; i < size; i++)
			inverse[i] = ArrayUtils.indexOf(permutation, i);
		return new Permutations(permutation, inverse);
	}

}
