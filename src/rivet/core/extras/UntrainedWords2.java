package rivet.core.extras;

import java.util.Arrays;

import rivet.core.labels.MapRIV;

public final class UntrainedWords2 {

    public static MapRIV rivAndSumWords(final String[] words, final int size,
            final int k) {
        return Arrays.stream(words).reduce(new MapRIV(size),
                (identity, word) -> identity
                        .destructiveAdd(MapRIV.generateLabel(size, k, word)),
                (a, b) -> a.destructiveAdd(b));
    }

    public static MapRIV rivettizeText(final String text, final int size,
            final int k) {
        return rivAndSumWords(tokenizeText(text), size, k);
    }

    public static MapRIV[] rivWords(final String[] words, final int size,
            final int k) {
        final MapRIV[] res = new MapRIV[words.length];
        for (int i = 0; i < words.length; i++) {
            final MapRIV riv = MapRIV.generateLabel(size, k, words[i]);
            res[i] = riv;
        }
        return res;
    }

    public static MapRIV sumMapRIVs(final MapRIV[] rivs) {
        return Arrays.stream(rivs).reduce(new MapRIV(rivs[0].size()),
                (i, r) -> i.destructiveAdd(r));
    }

    public static String[] tokenizeText(final String text) {
        return text.split("\\s+");
    }

    private UntrainedWords2() {
    }
}
