package rivet.core.extras.topicheirarchy;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import rivet.core.labels.ArrayRIV;

public class WordLexicon {
    private final int size;
    private final int nnz;
    
    private final RIVTopicHeirarchy topics;
    private final DualHashBidiMap<String, ArrayRIV> lexicon;
    
    public WordLexicon(int size, int nnz, RIVTopicHeirarchy topics, DualHashBidiMap<String, ArrayRIV> lexicon) {
        super();
        this.size = size;
        this.nnz = nnz;
        this.topics = topics;
        this.lexicon = lexicon;
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
