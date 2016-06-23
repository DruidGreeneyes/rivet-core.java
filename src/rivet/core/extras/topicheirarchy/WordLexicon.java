package rivet.core.extras.topicheirarchy;

import java.util.Arrays;
import java.util.function.BiFunction;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import rivet.core.labels.ArrayRIV;
import rivet.core.labels.RandomIndexVector;
import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

public class WordLexicon {
    private final int size;
    private final int nnz;
    
    private final RIVTopicHeirarchy topics;
    private final DualHashBidiMap<String, ArrayRIV> lexicon;
    private final Permutations permutations;
    
    public WordLexicon(int size, int nnz, RIVTopicHeirarchy topics, DualHashBidiMap<String, ArrayRIV> lexicon) {
        super();
        this.size = size;
        this.nnz = nnz;
        this.topics = topics;
        this.lexicon = lexicon;
        this.permutations = Permutations.generate(size);
    }
    
    public WordLexicon(int size, int nnz, RIVTopicHeirarchy topics) {
        this(size,
                nnz,
                topics,
                new DualHashBidiMap<>());
    }
    
    public WordLexicon(int size, int nnz, double simThreshold) {
        this(size,
                nnz,
                RIVTopicHeirarchy.makeRoot(new NamedRIVMap(size), simThreshold));
    }
    
    public WordLexicon clear() {
        return new WordLexicon(size, nnz, topics);
    }
    
    public int count() {return lexicon.size();}
    public int size() {return size;}
    
    public boolean contains(String word) {
        return lexicon.containsKey(word);
    }
    
    private double[][] nSquaredSimilarity (ArrayRIV[] rivs, boolean permute) {
        BiFunction<Integer, Integer, Double> sim = permute
                ? (i, c) -> RandomIndexVector.similarity(rivs[i], rivs[c].permute(permutations, c - i))
                        : (i, c) -> RandomIndexVector.similarity(rivs[i], rivs[c]);
        return Util.range(rivs.length)
                .mapToObj(
                        (i) -> Util.range(rivs.length)
                        .mapToDouble(
                                (c) -> sim.apply(i, c))
                        .toArray())
                .toArray(double[][]::new);
    }
    
    public double nGramTest(String[] parts) {
        ArrayRIV[] rivs = Arrays.stream(parts).map(this::get).toArray(ArrayRIV[]::new);
        double[][] sims = nSquaredSimilarity(rivs, true);
        return Arrays.stream(sims)
            .flatMapToDouble(Arrays::stream)
            .average()
            .orElseGet(() -> 0);
    }
    
    public ArrayRIV get(String word) {
        return lexicon.getOrDefault(word, ArrayRIV.generateLabel(size, nnz, word));
    }
    
    public void set (String word, ArrayRIV riv) {
        if (contains(word))
            topics.reGraft(NamedRIV.make(word, riv));
        else
            topics.graftNew(NamedRIV.make(word, riv));
        lexicon.put(word, riv);
    }
    
    public ArrayRIV meanVector() {
        return lexicon.values().stream()
                .reduce(new ArrayRIV(size), ArrayRIV::add)
                .divide((double)lexicon.size());
    }
    
    public String[] assignTopicsToDocument(ArrayRIV docRIV) {
        return RIVTopicHeirarchy.assignTopics(topics, docRIV);
    }
}
