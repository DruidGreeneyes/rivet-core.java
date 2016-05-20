package rivet.core.extras;

import static java.util.Arrays.stream;

import rivet.core.labels.ArrayRIV;

public final class UntrainedWords {
    private UntrainedWords(){}
    
    public static String[] tokenizeText (String text) {
        return text.split("\\s+");
    }
    
    public static ArrayRIV[] rivWords (String[] words, int size, int k) {
        ArrayRIV[] res = new ArrayRIV[words.length];
        for (int i = 0; i < words.length; i++) {
            ArrayRIV riv = ArrayRIV.generateLabel(size, k, words[i]);
            res[i] = riv;
        }
        return res;
    }
    
    public static ArrayRIV sumArrayRIVs (ArrayRIV[] rivs) { 
        return stream(rivs)
                .reduce(new ArrayRIV(rivs[0].size()),
                        (i, r) -> i.add(r));
    }
    
    public static ArrayRIV rivAndSumWords (String[] words, int size, int k) {
        return stream(words)
                .reduce(new ArrayRIV(size), 
                        (identity, word) -> identity.add(ArrayRIV.generateLabel(size, k, word)),
                        ArrayRIV::add);
    }
    
    public static ArrayRIV rivAndSumWords_2 (String[] words, int size, int k) {
        return new ArrayRIV(size)
                    .add(stream(words)
                            .map((word) ->ArrayRIV.generateLabel(size, k, word)));
    }
    
    public static ArrayRIV rivettizeText (String text, int size, int k) {
        return rivAndSumWords(tokenizeText(text), size, k);
    }
}
