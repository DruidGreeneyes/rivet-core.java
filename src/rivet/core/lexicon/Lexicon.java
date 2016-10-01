package rivet.core.lexicon;

import pair.Pair;
import rivet.core.labels.RIV;

public interface Lexicon {
    public int count();

    public Lexicon add(final String word, final RIV riv);

    public Pair<RIV, RIV> get(final String word);

    public RIV getLex(final String word);

    public RIV getInd(final String word);
}
