package rivet.core.extras;

import static java.util.Arrays.stream;

import rivet.core.labels.MapRIV;

public final class UntrainedWords2 {
    public static MapRIV rivAndSumWords(final String[] words, final int size,
            final int k) {
        return stream(words).map((w) -> MapRIV.generateLabel(size, k, w))
                .reduce(new MapRIV(size), (i, r) -> i.destructiveAdd(r));
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
        return stream(rivs).reduce(new MapRIV(rivs[0].size()),
                (i, r) -> i.destructiveAdd(r));
    }

    public static String[] tokenizeText(final String text) {
        return text.split("\\s+");
    }

    private UntrainedWords2() {
    }
}
