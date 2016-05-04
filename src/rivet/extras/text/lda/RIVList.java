package rivet.extras.text.lda;

import java.util.ArrayList;

import rivet.core.arraylabels.Labels;
import rivet.core.arraylabels.RIV;

public class RIVList extends ArrayList<RIV> {
	private final int size;
	
	public RIVList(int size) {super(); this.size = size;}
	
	public RIV meanVector() {
		return stream()
				.reduce(new RIV(size), Labels::addLabels)
				.divideBy(size());
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1925033388040442727L;
}
