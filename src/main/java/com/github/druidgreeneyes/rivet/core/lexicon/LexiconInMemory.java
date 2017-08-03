package com.github.druidgreeneyes.rivet.core.lexicon;

import java.util.concurrent.ConcurrentHashMap;

import com.github.druidgreeneyes.rivet.core.labels.MapRIV;
import com.github.druidgreeneyes.rivet.core.labels.RIV;

public class LexiconInMemory extends ConcurrentHashMap<String, LexiconEntry>
                             implements
                             Lexicon {
  /**
   *
   */
  private static final long serialVersionUID = -1191298199455037908L;

  private final int size;
  private final int nnz;
  private final RIV emptyRIV;

  public LexiconInMemory(final int vectorSize, final int vectorNNZ) {
    super();
    size = vectorSize;
    nnz = vectorNNZ;
    emptyRIV = MapRIV.empty(size);
  }

  @Override
  public LexiconInMemory add(final String word, final RIV riv) {
    compute(word,
            (k, v) -> v == null
                                ? newEntry(k)
                                : v.mapRight(r -> r.destructiveAdd(riv)));
    return this;
  }

  @Override
  public int count() {
    return size();
  }

  @Override
  public LexiconEntry get(final Object word) {
    return word.getClass()
               .equals(String.class)
                                     ? get((String) word)
                                     : null;
  }

  private LexiconEntry get(final String word) {
    return compute(word,
                   (k, v) -> v == null
                                       ? newEntry(k)
                                       : v);
  }

  @Override
  public RIV getInd(final String word) {
    return get(word).left;
  }

  @Override
  public RIV getLex(final String word) {
    return get(word).right;
  }

  private LexiconEntry newEntry(final String word) {
    return new LexiconEntry(MapRIV.generate(size, nnz, word),
                            emptyRIV);
  }

  @Override
  public int size() {
    return size;
  }

}
