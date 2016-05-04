package rivet.extras.text.lda;

import java.util.TreeMap;

import rivet.core.arraylabels.RIV;
import rivet.core.util.Heirarchy;

public class RIVTopicHeirarchy extends TreeMap<Double, RIV> implements Heirarchy<RIV> {
	private final int size;
	
	public RIVTopicHeirarchy(int size) {super(); this.size = size;}
	
	public void add(int row, int column, RIV value) {
		put(row, column, value.add(getOrDefault(row, column, new RIV(size))));
	}
	
	public void subtract(int row, int column, RIV value) {
		RIV v = getOrDefault(row, column, new RIV(size));
		if (v.equals(value))
			remove(row, column);
		else
			add(row, column, value.negate());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8604607952580943169L;
	
	

}
