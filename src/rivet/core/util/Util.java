package rivet.core.util;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public final  class Util {
	private Util(){}
	
	public static <T> List<T> shuffleList(final List<T> lis, final Long seed) {
		final int size = lis.size();
		return randInts(size, size, seed)
				.mapToObj(lis::get)
				.collect(toList());
	}
	
	public static double[] shuffleDoubleArray(final double[] arr, final Long seed) {
		final int size = arr.length;
		return randInts(size, size, seed)
				.mapToDouble((i) -> arr[i])
				.toArray();
	}
	
	public static IntStream randInts (final int bound, final int length, final Long seed) {
		return new Random(seed).ints(0, bound).distinct().limit(length);
	}
	
	public static LongStream range (final Long start, final Long bound) 					{ return LongStream.range(start, bound);}
	public static LongStream range (final Long bound) 										{ return range(0L, bound); }
	public static LongStream range (final Long start, final Long bound, final Long step) 	{ return range(start, bound).filter((x) -> (x - start) % step == 0L); }
	
	public static IntStream range (final int start, final int bound) 						{ return IntStream.range(start, bound); }
	public static IntStream range (final int bound) 										{ return range(0, bound); }
	public static IntStream range (final int start, final int bound, final int step) 		{ return range(start, bound).filter((x) -> (x - start) % step == 0); }
	
	public static int[] quickRange (final int start, final int bound, final int step) {
		int steps = (bound - start) / step + 1;
		int[] res = new int[steps];
		for (int i = 0; i <= steps; i++)
			res[i] = i * step + start;
		return res;
	}
	public static int[] quickRange (final int start, final int bound) { return quickRange(start, bound, 1); }
	public static int[] quickRange (final int bound) { return quickRange(0, bound); }
	
	public static void forRange(final int start, final int bound, final int step, final Consumer<Integer> fun) {
		for(int i = start; i < bound; i += step)
			fun.accept(i);
	}
	public static void forRange(final int start, final int bound, final Consumer<Integer> fun) {forRange(start, bound, 1, fun);}
	public static void forRange(final int bound, final Consumer<Integer> fun) 				   {forRange(0, bound, fun);}
}
