package rivet.extras.text;

import static java.util.Arrays.stream;

import rivet.core.arraylabels.*;

public class UntrainedWords {
	
	public static String[] tokenizeText (String text) {
		return text.split("\\s+");
	}
	
	public static RIV[] rivWords (String[] words, int size, int k) {
		return stream(words)
				.map(Labels.labelGenerator(size, k))
				.toArray(RIV[]::new);
	}
	
	public static RIV sumRIVs (RIV[] rivs) { return Labels.addLabels(rivs); }
	
	public static RIV rivettizeText (String text, int size, int k) {
		return sumRIVs(rivWords(tokenizeText(text), size, k));
	}
	
}
