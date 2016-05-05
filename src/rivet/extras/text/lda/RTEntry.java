package rivet.extras.text.lda;

import rivet.core.arraylabels.RIV;

public class RTEntry {
	final RIV riv;
	final int topic;
	
	private RTEntry(RIV r, int t) {riv = r; topic = t;}
	
	public static RTEntry make(RIV riv, int topic) {return new RTEntry(riv, topic);}

}
