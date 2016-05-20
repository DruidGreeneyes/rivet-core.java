package rivet.core.extras.topicheirarchy;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import rivet.core.labels.ArrayRIV;

public abstract class WordLexicon {
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
    
    public abstract WordLexicon clear();
    
    public int count() {return lexicon.size();}
    public int size() {return size;}
    
    public ArrayRIV get(String key) {
        return lexicon.getOrDefault(key, new ArrayRIV(size));
    }
    
    
    
    public ArrayRIV meanVector() {
        return lexicon.values().stream()
                .reduce(new ArrayRIV(size), ArrayRIV::add)
                .divide((double)lexicon.size());
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = -8893148657223464815L;
    
        
}
