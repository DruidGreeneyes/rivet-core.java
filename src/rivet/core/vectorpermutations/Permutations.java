package rivet.core.vectorpermutations;

import static rivet.core.util.Util.randInts;

import org.apache.commons.lang3.ArrayUtils;

import pair.Pair;

/**
 * An immutable pair of permutation vectors, such that perms.right is the
 * inverse of perms.left, and vice versa.
 *
 * @author josh
 */
public final class Permutations extends Pair<int[], int[]> {

    /**
     * @param size
     * @return a permutation pair for random index vectors of a given size
     */
    public static Permutations generate(final int size) {
        final int[] permutation = randInts(size, size, 0L).toArray();
        final int[] inverse = new int[size];
        for (int i = 0; i < size; i++)
            inverse[i] = ArrayUtils.indexOf(permutation, i);
        return new Permutations(permutation, inverse);
    }

    private Permutations(final int[] permute, final int[] inverse) {
        super(permute, inverse);
    }

}
