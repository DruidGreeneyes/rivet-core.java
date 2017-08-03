package com.github.druidgreeneyes.rivet.core.extras;

import static java.util.Arrays.stream;

import com.github.druidgreeneyes.rivet.core.labels.ArrayRIV;
import com.github.druidgreeneyes.rivet.core.labels.RIV;

public final class UntrainedWordsArray {

  private UntrainedWordsArray() {
  }

  public static RIV rivAndSumWords(final String[] words,
                                   final int size,
                                   final int k) {
    return stream(words).reduce(new ArrayRIV(size),
                                (identity, word) -> identity.destructiveAdd(ArrayRIV.generate(size,
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
      final RIV riv = ArrayRIV.generate(size, k, words[i]);
      res[i] = riv;
    }
    return res;
  }

  public static RIV sumArrayRIVs(final ArrayRIV[] rivs) {
    return stream(rivs).reduce(new ArrayRIV(rivs[0].size()),
                               (i, r) -> i.destructiveAdd(r));
  }

  public static String[] tokenizeText(final String text) {
    return text.split("\\s+");
  }
}
