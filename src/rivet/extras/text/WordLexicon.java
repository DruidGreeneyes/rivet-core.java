package rivet.extras.text;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import rivet.core.arraylabels.Labels;
import rivet.core.arraylabels.RIV;

public class WordLexicon extends DualHashBidiMap<String, RIV> {
	private final int size;
	
	public WordLexicon(int size) {super(); this.size = size;}
	
	public RIV meanVector() {
		return values().stream()
				.reduce(new RIV(size), Labels::addLabels)
				.divideBy((double)size());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8893148657223464815L;
	
		
}
