package rivet.core.extras.topicheirarchy;

import java.util.Arrays;
import java.util.function.BiFunction;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import rivet.core.labels.ArrayRIV;
import rivet.core.labels.RIVs;
import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

public class WordLexicon {
    private final int size;
    private final int nnz;

    private final RIVTopicHeirarchy topics;
    private final DualHashBidiMap<String, ArrayRIV> lexicon;
    private final Permutations permutations;

    public WordLexicon(final int size, final int nnz,
            final double simThreshold) {
        this(size, nnz, RIVTopicHeirarchy.makeRoot(new NamedRIVMap(size),
                simThreshold));
    }

    public WordLexicon(final int size, final int nnz,
            final RIVTopicHeirarchy topics) {
        this(size, nnz, topics, new DualHashBidiMap<>());
    }

    public WordLexicon(final int size, final int nnz,
            final RIVTopicHeirarchy topics,
            final DualHashBidiMap<String, ArrayRIV> lexicon) {
        super();
        this.size = size;
        this.nnz = nnz;
        this.topics = topics;
        this.lexicon = lexicon;
        permutations = Permutations.generate(size);
    }

    public String[] assignTopicsToDocument(final ArrayRIV docRIV) {
        return RIVTopicHeirarchy.assignTopics(topics, docRIV);
    }

    public WordLexicon clear() {
        return new WordLexicon(size, nnz, topics);
    }

    public boolean contains(final String word) {
        return lexicon.containsKey(word);
    }

    public int count() {
        return lexicon.size();
    }

    public ArrayRIV get(final String word) {
        return lexicon.getOrDefault(word,
                ArrayRIV.generateLabel(size, nnz, word));
    }

    public ArrayRIV meanVector() {
        return lexicon.values().stream()
                .reduce(new ArrayRIV(size), ArrayRIV::add)
                .divide(lexicon.size());
    }

    public double nGramTest(final String[] parts) {
        final ArrayRIV[] rivs = Arrays.stream(parts).map(this::get)
                .toArray(ArrayRIV[]::new);
        final double[][] sims = nSquaredSimilarity(rivs, true);
        return Arrays.stream(sims).flatMapToDouble(Arrays::stream).average()
                .orElseGet(() -> 0);
    }

    private double[][] nSquaredSimilarity(final ArrayRIV[] rivs,
            final boolean permute) {
        final BiFunction<Integer, Integer, Double> sim = permute
                ? (i, c) -> RIVs.similarity(rivs[i],
                        rivs[c].permute(permutations, c - i))
                : (i, c) -> RIVs.similarity(rivs[i], rivs[c]);
        return Util.range(rivs.length)
                .mapToObj((i) -> Util.range(rivs.length)
                        .mapToDouble((c) -> sim.apply(i, c)).toArray())
                .toArray(double[][]::new);
    }

    public void set(final String word, final ArrayRIV riv) {
        if (contains(word))
            topics.reGraft(NamedRIV.make(word, riv));
        else
            topics.graftNew(NamedRIV.make(word, riv));
        lexicon.put(word, riv);
    }

    public int size() {
        return size;
    }
}
