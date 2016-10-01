package rivet.core.lexicon;

import rivet.core.labels.MapRIV;

public interface Lexicon {
    public int count();

    public Lexicon add(final String word, final MapRIV riv);

    public MapRIV getLex(final String word);

    public MapRIV getInd(final String word);
}
