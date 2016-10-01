package rivet.core.lexicon;

import pair.Pair;
import rivet.core.labels.MapRIV;

public interface Lexicon {
    public int count();

    public Lexicon add(final String word, final MapRIV riv);

    public Pair<MapRIV, MapRIV> get(final String word);

    public MapRIV getLex(final String word);

    public MapRIV getInd(final String word);
}
