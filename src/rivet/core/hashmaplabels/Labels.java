package rivet.core.hashmaplabels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import rivet.core.vectorpermutations.Permutations;
import rivet.core.hashmaplabels.RIV;
import rivet.core.util.Counter;
import rivet.core.util.Pair;
import rivet.core.util.Util;

public final class Labels {
    
    private Labels(){}
    
    public static Stream<Integer> getMatchingKeyStream (final RIV labelA, final RIV labelB) {
        return labelA.keySet()
                .stream()
                .filter((x) -> labelB.containsKey(x));
    }
    
    public static Stream<Pair<Double, Double>> getMatchingValStream (final RIV labelA, final RIV labelB) {
        return getMatchingKeyStream(labelA, labelB)
                .map((i) -> Pair.make(labelA.get(i), labelB.get(i)));
    }
    
    public static Double dotProduct (final RIV labelA, final RIV labelB) {
        return getMatchingValStream(labelA, labelB)
                .mapToDouble((x) -> x.left * x.right)
                .sum();
    }
    
    public static Double similarity (final RIV labelA, final RIV labelB) {
        final RIV lA = labelA.normalize();
        final RIV lB = labelB.normalize();
        final Double mag = lA.magnitude() * lB.magnitude();
        return (mag == 0)
                ? 0
                : dotProduct(lA, lB) / mag;
    }
    
    public static RIV addLabels (final RIV labelA, final RIV labelB) {
        final RIV result = new RIV(labelA);
        labelB.entrySet().forEach(result::mergePlus);
        return result;
    }
    
    public static RIV addLabels (final RIV...labels) {
        return Arrays.stream(labels)
                .reduce(new RIV(labels[0].size()), Labels::addLabels);
    }
    
    private static List<Double> makeVals (final int count, final Long seed) {
        final List<Double> l = new ArrayList<>();
        for (int i = 0; i < count; i++) 
            l.add((i < count / 2) 
                    ? 1.0 
                    : -1.0);
        return Util.shuffleList(l, seed);
    }
    
    private static Set<Integer> makeIndices (final int size, final int count, final Long seed) {
        return Util.randInts(size, count, seed)
                .boxed()
                .collect(Collectors.toSet());
    }
    
    private static Long makeSeed (final CharSequence word) {
        Counter c = new Counter();
        return word.chars()
                .boxed()
                .mapToLong((x) -> x.longValue() * (10 ^ c.lateInc()))
                .sum();
    }
    
    public static RIV generateLabel (final int size, final int k, final CharSequence word) {
        final Long seed = makeSeed(word);
        final int j = (k % 2 == 0) ? k : k + 1;
        return new RIV(
                makeIndices(size, j, seed),
                makeVals(j, seed),
                size);
    }
    
    public static RIV generateLabel (final int size, final int k, final CharSequence source, final int tokenIndex, final int tokenLength) {
        return generateLabel(size, k, source.subSequence(tokenIndex, tokenIndex + tokenLength));
    }
    
    public static Function<CharSequence, RIV> labelGenerator (final int size, final int k) {
        return (word) -> generateLabel(size, k, word);
    }
    
    public static Function<Integer, RIV> labelGenerator (final int size, final int k, final CharSequence source, final int tokenLength) {
        return (index) -> generateLabel(size, k, source, index, tokenLength);
    }
    
    public static RIV permuteLabels (final RIV label, final Permutations permutations, final int times) {
        return label.permute(permutations, times);
    }
    
    public static Permutations generatePermutations (final int size) {
        return Permutations.generate(size);
    }
}
