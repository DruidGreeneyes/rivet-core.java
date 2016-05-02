package rivet.core.vectorpermutations;

import org.apache.commons.lang3.ArrayUtils;

import rivet.core.util.Pair;

import static rivet.core.util.Util.randInts;

public class Permutations extends Pair<int[], int[]> {
	
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
