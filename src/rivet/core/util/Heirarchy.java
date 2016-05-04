package rivet.core.util;

import java.util.SortedMap;

public interface Heirarchy<T> extends SortedMap<Double, T> {

	default T get(int row, int column) { return get(row + decimalify(column)); }
	
	default T getOrDefault(int row, int column, T defaultValue) {
		return getOrDefault(row + decimalify(column), defaultValue);
	}
	
	default void put(int row, int column, T value) { put(row + decimalify(column), value); }
	
	default void remove(int row, int column) { remove(row + decimalify(column)); }
	
	/**
	 * 
	 */
	static final long serialVersionUID = -1873811488075444687L;
	static double decimalify(int i) {
		double c = i;
		while (c > 1) c = c / 10;
		return c;
	}
}
