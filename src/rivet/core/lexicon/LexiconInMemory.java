package rivet.core.lexicon;

import java.util.concurrent.ConcurrentHashMap;

import rivet.core.labels.MapRIV;

public class LexiconInMemory extends ConcurrentHashMap<String, LexiconEntry>
        implements Lexicon {
    /**
     * 
     */
    private static final long serialVersionUID = -1191298199455037908L;

    private final int    size;
    private final int    nnz;
    private final MapRIV emptyRIV;

    public LexiconInMemory(int vectorSize, int vectorNNZ) {
        super();
        size = vectorSize;
        nnz = vectorNNZ;
        emptyRIV = MapRIV.empty(size);
    }

    @Override
    public int count() {
        return size();
    }

    @Override
    public int size() {
        return size;
    }

    private LexiconEntry newEntry(String word) {
        return new LexiconEntry(MapRIV.generateLabel(size, nnz, word),
                emptyRIV);
    }

    @Override
    public LexiconInMemory add(String word, MapRIV riv) {
        compute(word, (k, v) -> (v == null)
                ? newEntry(k)
                : v.mapRight(r -> r.destructiveAdd(riv)));
        return this;
    }

    @Override
    public LexiconEntry get(Object word) {
        return (word.getClass()
                    .equals(String.class))
                            ? get((String) word)
                            : null;
    }

    private LexiconEntry get(String word) {
        return compute(word, (k, v) -> (v == null)
                ? newEntry(k)
                : v);
    }

    @Override
    public MapRIV getLex(String word) {
        return get(word).right;
    }

    @Override
    public MapRIV getInd(String word) {
        return get(word).left;
    }

}
