package com.github.druidgreeneyes.rivet.core.extras;

import java.util.Arrays;

import com.github.druidgreeneyes.rivet.core.labels.MapRIV;
import com.github.druidgreeneyes.rivet.core.labels.RIV;

public final class UntrainedWordsMap {

  private UntrainedWordsMap() {
  }

  public static RIV rivAndSumWords(final String[] words,
                                   final int size,
                                   final int k) {
    return Arrays.stream(words)
                 .reduce(new MapRIV(size),
                         (identity,
                          word) -> identity.destructiveAdd(MapRIV.generate(size,
                                                                           k,
                                                                           word)),
                         (a, b) -> a.destructiveAdd(b));
  }

  public static RIV rivettizeText(final String text,
                                  final int size,
                                  final int k) {
    return rivAndSumWords(tokenizeText(text), size, k);
  }

  public static RIV[] rivWords(final String[] words,
                               final int size,
                               final int k) {
    final RIV[] res = new RIV[words.length];
    for (int i = 0; i < words.length; i++) {
      final RIV riv = MapRIV.generate(size, k, words[i]);
      res[i] = riv;
    }
    return res;
  }

  public static RIV sumMapRIVs(final RIV[] rivs) {
    return Arrays.stream(rivs)
                 .reduce(new MapRIV(rivs[0].size()),
                         (i, r) -> i.destructiveAdd(r));
  }

  public static String[] tokenizeText(final String text) {
    return text.split("\\s+");
  }
}
