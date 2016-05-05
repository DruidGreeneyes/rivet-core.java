package rivet.extras.text.lda;

import rivet.core.arraylabels.RIV;

public class Topic {
	public final RIV riv;
	public final String name;
	
	private Topic (RIV riv, String name) {this.riv = riv; this.name = name;}
	public static Topic make (RIV riv, String name) { return new Topic(riv, name); }
}
