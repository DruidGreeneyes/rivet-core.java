package rivet.core.hashmaplabels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import rivet.core.util.Util;
import rivet.core.vectorpermutations.Permutations;

public final class RIV extends HashMap<Integer, Double> {
	private static final long serialVersionUID = 7549131075767220565L;
	/**
	 * 
	 */
	private final int size;
	
	public RIV (final RIV riv) {
		super(riv);
		this.size = riv.size;
	}
	public RIV (final int size) {
		super();
		this.size = size;
	}
	
	public RIV (final Set<Integer> keys, final Collection<Double> values, final int size) {
		this(size);
		final List<Integer> ks = new ArrayList<>(keys);
		final List<Double> vs = new ArrayList<>(values);
		Util.range(ks.size())
				.forEach((i) -> this.put(ks.get(i), vs.get(i)));
	}
	
	public static RIV fromString (final String mapString) {
		final String[] mapstr = mapString.trim()
							.split("\\|");
		RIV res = new RIV(Integer.parseInt(mapstr[1]));
		Arrays.stream(
				mapstr[0].substring(1, mapstr[0].length() - 1)
					.split("\\s+"))
			.map((x) -> x.split("="))
			.forEach((entry) -> res.put(
						Integer.parseInt(entry[0]),
						Double.parseDouble(entry[1])));
		return res;
	}
	
	public String toString() {
		return super.toString() + "|" + this.size;
	}
	
	public int size() { return this.size; }
	
	public void mergePlus (final Integer key, final Double value) {
		this.merge(key, value, Double::sum);
	}
	public void mergePlus (final Entry<Integer, Double> e) {
		this.mergePlus(e.getKey(), e.getValue());
	}
	
	public void mergeMinus (final Integer key, final Double value) {
		final Double v = -value;
		this.merge(key, v, Double::sum);
	}
	public void mergeMinus (final Entry<Integer, Double> e) {
		this.mergeMinus(e.getKey(), e.getValue());
	}
	
	public void put (final Entry<Integer, Double> e) {
		this.put(e.getKey(), e.getValue());
	}
	
	public int[] keyArray () { return this.keySet().stream().mapToInt(x -> x.intValue()).toArray(); }
	public int[] valArray () { return this.values().stream().mapToInt(x -> x.intValue()).toArray(); }
	
	private RIV permuteLoop (int[] permutation, int times) {
		int[] keys = this.keyArray();
		for (int i = 0; i < times; i++)
			for (int c = 0; c < keys.length; c++)
				keys[c] = permutation[keys[c]];
		return new RIV(
				stream(keys).boxed().collect(Collectors.toSet()),
				this.values(),
				this.size());
	}
	
	public RIV permute (Permutations permutations, int times) {
		return (times == 0)
				? this
						: (times > 0)
						? permuteLoop(permutations.left, times)
								: permuteLoop(permutations.right, -times);
	}

	public Double magnitude () {
		return Math.sqrt(
				this.values()
				.parallelStream()
				.mapToDouble((x) -> x * x)
				.sum());
	}
	
	public RIV normalize () {
		final Double mag = this.magnitude();
		return this.divideBy(mag);
	}
	
	public RIV removeZeros () {
		Set<Integer> keys = new HashSet<>();
		List<Double> values = new ArrayList<>();
		this.entrySet()
			.stream()
			.filter((e) -> e.getValue() != 0)
			.forEachOrdered((e) -> { keys.add(e.getKey()); values.add(e.getValue()); });
		return new RIV(keys, values, size);
	}
	
	public RIV subtract (RIV riv) {
		final RIV result = new RIV(this);
		riv.entrySet().parallelStream().forEach(result::mergeMinus);
		return result;
	}
	
	public RIV divideBy (double num) {
		List<Double> newVals = this.values()
								.parallelStream()
								.map((val) -> val / num)
								.collect(Collectors.toList());
		return new RIV(
				this.keySet(),
				newVals,
				this.size());
	}
}
