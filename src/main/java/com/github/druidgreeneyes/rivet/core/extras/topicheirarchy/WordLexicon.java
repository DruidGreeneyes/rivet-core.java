package com.github.druidgreeneyes.rivet.core.extras.topicheirarchy;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import com.github.druidgreeneyes.rivet.core.labels.ArrayRIV;
import com.github.druidgreeneyes.rivet.core.labels.RIV;
import com.github.druidgreeneyes.rivet.core.labels.RIVConstructor;
import com.github.druidgreeneyes.rivet.core.labels.RIVs;
import com.github.druidgreeneyes.rivet.core.util.Util;
import com.github.druidgreeneyes.rivet.core.vectorpermutations.Permutations;

public class WordLexicon {
  private final int size;
  private final int nnz;

  private final Function<CharSequence, RIV> rivMaker;

  private final RIVTopicHeirarchy topics;
  private final DualHashBidiMap<String, RIV> lexicon;
  private final Permutations permutations;

  public WordLexicon(final int size, final int nnz,
                     final Function<CharSequence, RIV> rivMaker,
                     final RIVTopicHeirarchy topics,
                     final DualHashBidiMap<String, RIV> lexicon) {
    super();
    this.size = size;
    this.nnz = nnz;
    this.topics = topics;
    this.lexicon = lexicon;
    permutations = Permutations.generate(size);
    this.rivMaker = rivMaker;
  }

  public WordLexicon(final int size, final int nnz,
                     final RIVConstructor rivConstructor,
                     final double simThreshold) {
    this(size, nnz, rivConstructor,
         RIVTopicHeirarchy.makeRoot(new NamedRIVMap(size),
                                    simThreshold));
  }

  public WordLexicon(final int size, final int nnz,
                     final RIVConstructor rivConstructor,
                     final RIVTopicHeirarchy topics) {
    this(size, nnz, rivConstructor, topics, new DualHashBidiMap<>());
  }

  public WordLexicon(final int size, final int nnz,
                     final RIVConstructor rivConstructor,
                     final RIVTopicHeirarchy topics,
                     final DualHashBidiMap<String, RIV> lexicon) {
    this(size, nnz, RIVs.generator(size, nnz, rivConstructor), topics,
         lexicon);
  }

  public String[] assignTopicsToDocument(final RIV docRIV) {
    return RIVTopicHeirarchy.assignTopics(topics, docRIV);
  }

  public WordLexicon clear() {
    return new WordLexicon(size, nnz, rivMaker, topics,
                           new DualHashBidiMap<>());
  }

  public boolean contains(final String word) {
    return lexicon.containsKey(word);
  }

  public int count() {
    return lexicon.size();
  }

  public RIV get(final String word) {
    return lexicon.getOrDefault(word, rivMaker.apply(word));
  }

  public RIV meanVector() {
    return lexicon.values()
                  .stream()
                  .reduce(new ArrayRIV(size), RIV::destructiveAdd)
                  .divide(lexicon.size());
  }

  public double nGramTest(final String[] parts) {
    final ArrayRIV[] rivs = Arrays.stream(parts)
                                  .map(this::get)
                                  .toArray(ArrayRIV[]::new);
    final double[][] sims = nSquaredSimilarity(rivs, true);
    return Arrays.stream(sims)
                 .flatMapToDouble(Arrays::stream)
                 .average()
                 .orElseGet(() -> 0);
  }

  private double[][] nSquaredSimilarity(final ArrayRIV[] rivs,
                                        final boolean permute) {
    final BiFunction<Integer, Integer, Double> sim = permute
                                                             ? (i,
                                                                c) -> rivs[i].similarityTo(rivs[c].permute(permutations,
                                                                                                           c - i))
                                                             : (i,
                                                                c) -> rivs[i].similarityTo(rivs[c]);
    return Util.range(rivs.length)
               .mapToObj((i) -> Util.range(rivs.length)
                                    .mapToDouble((c) -> sim.apply(i, c))
                                    .toArray())
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
