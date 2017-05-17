package com.github.druidgreeneyes.rivet.core.util.hilbert;

import java.math.BigInteger;

public class HKey {
	public final BigInteger k;
	public final int order;
	public final int dimensions;
	
	public HKey(BigInteger key, int ord, int dims) {
		k = key;
		order = ord;
		dimensions = dims;
	}
}
