 package rivet.core.arraylabels;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class VectorElement implements Comparable<VectorElement> {
	
	//Values
	private final int index;
	private final double value;
	
	//Constructors
	private VectorElement() { this.index = 0; this.value = 0; }
	private VectorElement(int index, double value) { this.index = index; this.value = value; }
	private VectorElement(int index) {this.index = index; this.value = 0;}
	private VectorElement(double value) {this.index = 0; this.value = value;}
	
	//Core Methods
	public int index() { return this.index; }
	public double value() { return this.value; }
	public String toString() { return String.format("%d|%f", index, value);}
	@Override
	public int compareTo(VectorElement p) {	return Integer.compare(this.index, p.index); }
	public boolean equals(VectorElement p) { return index == p.index; }
	public boolean strictEquals(VectorElement p) { return equals(p) && value == p.value; }
	private void assertMatch(VectorElement p) {
		if (!this.equals(p))
			throw new IndexOutOfBoundsException(
					String.format("Point indices do not match! %s != %s",
							this.toString(), p.toString()));
	}
	public VectorElement add(double v) { return elt(this.index, this.value + v); }
	public VectorElement add(VectorElement p) {
		this.assertMatch(p);
		return this.add(p.value);
	}
	public VectorElement subtract(double v) { return this.add(-v); }
	public VectorElement subtract(VectorElement p) { return this.add(-p.value); }
	
	public boolean contains(double v) {return Double.compare(v, this.value) == 0;}
	
	//Convenience Methods
	public <T> T engage (Function<VectorElement, T> fun) { return fun.apply(this); }
	public <T, R> R engage (BiFunction<VectorElement, T, R> fun, T thing) { return fun.apply(this, thing); }
	
	
	//Static Methods
	public static VectorElement zero() { return new VectorElement(); }
	public static VectorElement partial(int index) { return new VectorElement(index); }
	public static VectorElement partial(double value) { return new VectorElement(value); }
	public static VectorElement elt(int index, double value) { return new VectorElement(index, value); }
	public static VectorElement fromString(String eltString) {
		String[] elt = eltString.split("\\|");
		if (elt.length != 2) throw new IndexOutOfBoundsException("Wrong number of partitions: " + eltString);
		return elt(
				Integer.parseInt(elt[0]),
				Double.parseDouble(elt[1]));
	}
	public static int compare (VectorElement a, VectorElement b) { return a.compareTo(b); }
	
}
