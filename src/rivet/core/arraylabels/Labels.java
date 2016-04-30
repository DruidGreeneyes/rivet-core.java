package rivet.core.arraylabels;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import rivet.core.vectorpermutations.Permutations;
import rivet.core.util.Counter;
import rivet.core.util.Util;
import scala.Tuple2;

public class Labels {
	
	private Labels(){}
	
	public static IntStream getMatchingKeyStream (final RIV labelA, final RIV labelB) {
		return labelA.keyStream().filter(labelB::contains);
	}
	
	public static Stream<Tuple2<Double, Double>> getMatchingValStream (final RIV labelA, final RIV labelB) {
		return getMatchingKeyStream(labelA, labelB)
				.mapToObj((i) -> new Tuple2<>(labelA.get(i), labelB.get(i)));
	}
	
	public static double dotProduct (final RIV labelA, final RIV labelB) {
		return getMatchingValStream(labelA, labelB)
				.mapToDouble((valPair) -> valPair._1 * valPair._2)
				.sum();
	}
	
	public static Double similarity (final RIV labelA, final RIV labelB) {
		final RIV a = labelA.normalize();
		final RIV b = labelB.normalize();
		final Double mag = a.magnitude() * b.magnitude();
		return (mag == 0)
				? 0
				: dotProduct(a, b) / mag;
	}
	
	public static RIV addLabels(final RIV labelA, final RIV labelB) {
		return new RIV(labelA).add(labelB);
	}
	
	public static RIV addLabels(final RIV...labels) {
		return Arrays.stream(labels)
					.reduce(new RIV(labels[0].size()), Labels::addLabels);
	}
	
	private static double[] makeVals (final int count, long seed) {
		double[] l = new double[count];
		for (int i = 0; i < count; i += 2) l[i] = 1;
		for (int i = 1; i < count; i += 2) l[i] = -1;
		return Util.shuffleDoubleArray(l, seed);
	}
	
	private static int[] makeIndices(final int size, final int count, final long seed) {
		return Util.randInts(size, count, seed).toArray();
	}
	
	private static Long makeSeed (final CharSequence word) {
		Counter c = new Counter();
		return word.chars()
				.boxed()
				.mapToLong((x) -> x.longValue() * (10 ^ c.lateInc()))
				.sum();
	}
	
	public static RIV generateLabel (final int size, final int k, final CharSequence word) {
		final long seed = makeSeed(word);
		final int j = (k % 2 == 0) ? k : k + 1;
		return new RIV(
				makeIndices(size, j, seed),
				makeVals(j, seed),
				size);
	}
	public static RIV generateLabel (final int size, final int k, final CharSequence source, final int startIndex, final int tokenLength) {
		return generateLabel(size, k, source.subSequence(startIndex, startIndex + tokenLength));
	}
	
	public static Function<String, RIV> labelGenerator (final int size, final int k) {
		return (word) -> generateLabel(size, k, word);
	}
	
	public static Function<Integer, RIV> labelGenerator (final String source, final int size, final int k, final int tokenLength) {
		return (index) -> generateLabel(size, k, source, index, tokenLength);
	}
	
	public static RIV permuteLabel (final RIV label, final Permutations permutations, final int times) {
		return label.permute(permutations, times);
	}
	
	public static Permutations generatePermutations (final int size) {
		return Permutations.generate(size);
	}
}
