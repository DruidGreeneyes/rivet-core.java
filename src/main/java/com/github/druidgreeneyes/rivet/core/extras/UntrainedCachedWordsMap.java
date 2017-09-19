package com.github.druidgreeneyes.rivet.core.extras;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import com.github.druidgreeneyes.rivet.core.labels.MapRIV;
import com.github.druidgreeneyes.rivet.core.labels.RIV;
import com.github.druidgreeneyes.rivet.core.labels.RIVs;

public final class UntrainedCachedWordsMap {
  private UntrainedCachedWordsMap() {}

  public static RIV
         cacheingRivettizeText(final ConcurrentHashMap<String, RIV> cache,
                               final String text,
                               final int size,
                               final int k) {
    return sumMapRIVs(rivAndCacheWords(cache, tokenizeText(text), size, k));
  }

  public static RIV[]
         rivAndCacheWords(final ConcurrentHashMap<String, RIV> cache,
                          final String[] words,
                          final int size,
                          final int nnz) {
    final RIV[] res = IntStream.range(0, words.length)
                               .mapToObj(i -> cache.compute(words[i],
                                                            (k, v) -> v != null
                                                                                ? v
                                                                                : MapRIV.generate(size,
                                                                                                  nnz,
                                                                                                  words[i])))
                               .toArray(RIV[]::new);
    return res;

  }

  public static RIV rivAndSumWords(final String[] words, final int size,
                                   final int nnz) {
    return Arrays.stream(words)
                 .map(RIVs.generator(size, nnz, MapRIV::new))
                 .reduce(new MapRIV(size),
                         (i, r) -> i.destructiveAdd(r));
  }

  public static RIV rivettizeText(final String text, final int size,
                                  final int k) {
    return rivAndSumWords(tokenizeText(text), size, k);
  }

  public static RIV[] rivWords(final String[] words, final int size,
                               final int k) {
    final RIV[] res = new RIV[words.length];
    for (int i = 0; i < words.length; i++) {
      final RIV riv = MapRIV.generate(size, k, words[i]);
      res[i] = riv;
    }
    return res;
  }

  public static RIV sumMapRIVs(final RIV[] rivs) {
    return Arrays.stream(rivs).reduce(new MapRIV(rivs[0].size()),
                                      (i, r) -> i.destructiveAdd(r));
  }

  public static String[] tokenizeText(final String text) {
    return text.split("\\s+");
  }
}
