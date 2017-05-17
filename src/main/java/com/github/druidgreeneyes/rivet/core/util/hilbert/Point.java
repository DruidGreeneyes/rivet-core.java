package com.github.druidgreeneyes.rivet.core.util.hilbert;

import java.util.Arrays;

public class Point {
	public final int[] c;
	
	public Point(final int dim) {
		c= new int[dim];
		Arrays.fill(c, 0);
	}
	
	public Point(final int[] coords) {
		c = Arrays.copyOf(coords, coords.length);
	}
}
