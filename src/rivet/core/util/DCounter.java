package rivet.core.util;

public final class DCounter {
	private double count;
	public DCounter() { count = 0; }
	public double get() { return count; }
	public double set(double value) { count = value; return count; }
	public double inc() { count++; return count; }
	public double inc(double value) { count += value; return count; }
	public double dec() { count--; return count; }
	public double dec(double value) { count -= value; return count; }
	public double lateInc() { double c = count; count++; return c; }
	public double lateInc(double value) {double c = count; count += value; return c; }
	public double lateDec() { double c = count; count--; return c; }
	public double lateDec(double value) {double c = count; count -= value; return c; }
	public double zero() {return set(0);}
}
