package rivet.extras.text.lda;

public class WDTEntry {
	final String word;
	final int document;
	final int topic;
	
	private WDTEntry (String w, int d, int t) { word = w; document = d; topic = t; }
	
	public static WDTEntry make(String word, int document, int topic) { 
		return new WDTEntry(word, document, topic); 
	}
}
