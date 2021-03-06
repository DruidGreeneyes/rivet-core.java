package com.github.druidgreeneyes.rivet.core.lexicon;

import com.github.druidgreeneyes.rivet.core.labels.RIV;

public interface Lexicon {
    public int count();

    public Lexicon add(final String word, final RIV riv);

    public RIV getLex(final String word);

    public RIV getInd(final String word);
}
