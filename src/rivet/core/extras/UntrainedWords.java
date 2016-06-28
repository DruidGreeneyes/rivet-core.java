package rivet.core.extras;

import static java.util.Arrays.stream;

import rivet.core.labels.ArrayRIV;

public final class UntrainedWords {
    public static ArrayRIV rivAndSumWords(final String[] words, final int size,
            final int k) {
        return stream(words).reduce(new ArrayRIV(size),
                (identity, word) -> identity
                        .destructiveAdd(ArrayRIV.generateLabel(size, k, word)),
                (a, b) -> a.destructiveAdd(b));
    }

    public static ArrayRIV rivettizeText(final String text, final int size,
            final int k) {
        return rivAndSumWords(tokenizeText(text), size, k);
    }

    public static ArrayRIV[] rivWords(final String[] words, final int size,
            final int k) {
        final ArrayRIV[] res = new ArrayRIV[words.length];
        for (int i = 0; i < words.length; i++) {
            final ArrayRIV riv = ArrayRIV.generateLabel(size, k, words[i]);
            res[i] = riv;
        }
        return res;
    }

    public static ArrayRIV sumArrayRIVs(final ArrayRIV[] rivs) {
        return stream(rivs).reduce(new ArrayRIV(rivs[0].size()),
                (i, r) -> i.destructiveAdd(r));
    }

    public static String[] tokenizeText(final String text) {
        return text.split("\\s+");
    }

    private UntrainedWords() {
    }
}
