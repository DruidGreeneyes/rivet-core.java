package rivet.core.util.hilbert;

import java.util.stream.IntStream;
import java.math.BigInteger;
import java.util.Arrays;

import rivet.core.labels.DenseRIV;
import rivet.core.labels.RIV;
import rivet.core.util.hilbert.HCode;

public final class Hilbert {
	private Hilbert(){}
	
	//integer bit ops
	private static boolean testBit(final int n, final int position) {
		return (n & (1 << position)) != 0;
	}

	
	private static int alterBit(final int n, final int pos, final boolean newValue) {
		return (newValue)
				? setBit(n, pos)
						: clearBit(n, pos);
	}
	
	private static int alterBit(final int n, final int pos, final int newValue) {
		return alterBit(n, pos, newValue==1);
	}
	
	private static int setBit(final int n, final int pos) {
		return n | (1 << pos);
	}
	
	private static int clearBit(final int n, final int pos) {
		return n & ~(1 << pos);
	}
	
	private static int flipBit(final int n, final int pos) {
		return n ^ (1 << pos);
	}
	
	/*
	 * From source:
	 *  n = number of dimensions in the target space (16k)
	 *  m = order of the curve passing through a space (higher = less information loss. Say, 16, for giggles)
	 *  N = the number of bits in a derived key, =n * m
	 *  i = a number in the range (1..m)
	 *    we iterate m times through the algo, so i also represents conveniently which iteration we are on.
	 *  j = a number in the range (1..n)
	 *  r = a result key, in binary, of N bits, divided into m sections of n digits each.
	 *  p.i.j = a single binary digit in R; the nth digit of the mth section.
	 *    NOTE: byte is used in the source material in place of section, but since byte is the 8-bit integer type in java, I chose not to use it here.
	 *  a.j = a single coordinate in dimension j of point a.
	 *  alpha.i.j = a binary digit in a coordinate a.j, such that [A.1.j..A.m.j] is the binary representation of j.
	 *  alpha.i = binary number composed of [A.i.1..A.i.n]
	 */
	
	/* First Attempt, NO LONGER FUNCTIONAL
	 * 
	 * 
	private static final BigInteger TWO = BigInteger.valueOf(2L);
	private static final BigInteger THREE = BigInteger.valueOf(3L);
	
	private static BigInteger sigma_i(final BigInteger p_i) {
		return p_i.xor(p_i.divide(TWO));
	}
	
	private static BigInteger[] sigma(final BigInteger[] P) {
		BigInteger[] s = new BigInteger[P.length];
		for (int i = 0; i < s.length; i++)
			s[i] = sigma_i(P[i]);
		return s;
	}
	
	private static BigInteger tau_i(final BigInteger p_i) {
		if (p_i.compareTo(THREE) < 0)
			return ZERO;
		else if (p_i.mod(TWO).equals(ZERO))
			return sigma_i(p_i.subtract(ONE));
		else
			return sigma_i(p_i.subtract(TWO));
	}
	
	private static BigInteger[] tau(final BigInteger[] P) {
		BigInteger[] t = new BigInteger[P.length];
		for (int i = 0; i < t.length; i++)
			t[i] = tau_i(P[i]);
		return t;
	}
	
	private static int principlePosition(final BigInteger lsb) {
		for (int i = 1; i < 32; i++) {
			final int m = 1 << i;
			if (lsb.equals(m))
				return i;
		}
		return 0;
	}
	
	private static BigInteger lsb(final BigInteger n, final BigInteger mask) {
		return n.and(mask);
	}
	
	private static int J_i(final BigInteger p_i, final BigInteger mask) {
		return principlePosition(lsb(p_i, mask));
	}
	
	private static int[] calculateJ(final BigInteger[] P, final int dimensions) {
		final BigInteger lsb_mask = ONE.shiftLeft(dimensions).subtract(ONE);
		final int[] J = new int[P.length];
		for (int i = 0; i < P.length; i++) {
			J[i] = J_i(P[i], lsb_mask);
		}
		return J;
	}
	
	private static BigInteger rotateRight(final BigInteger n, final int distance, final int dimensions) {
		BigInteger t = n;
		for (int i = 0; i < distance; i++)
			if (n.testBit(i))
				t = t.setBit(dimensions + i);
		return t.shiftRight(distance);
	}
	
	private static BigInteger[] addHat(final BigInteger[] head, int[] J, final int dimensions) {
		BigInteger[] hat = new BigInteger[head.length];
		for (int i = 0; i < hat.length; i++) {
			int j = 0;
			for (int c = 0; c < i; c++)
				j += J[c] - 1;
			hat[i] = rotateRight(head[i], j, dimensions);
		}
		return hat;
	}
	
	private static BigInteger[] omega(final BigInteger[] tauHat) {
		BigInteger[] o = new BigInteger[tauHat.length];
		o[0] = ZERO;
		for (int i = 1; i < tauHat.length; i++)
			o[i] = o[i - 1].xor(tauHat[i - 1]);
		return o;		
	}
	
	private static BigInteger[] alpha(final BigInteger[] omega, final BigInteger[] sigmaHat) {
		BigInteger[] a = new BigInteger[omega.length];
		for (int i = 0; i < a.length; i++)
			a[i] = omega[i].xor(sigmaHat[i]);
		return a;
	}
	
	private static BigInteger[] calculateP(final HKey key) {
		BigInteger k = key.value;
		BigInteger[] sKey = new BigInteger[key.curveOrder];
		Arrays.setAll(sKey, i -> ZERO);
		for (int m = 0; m < key.curveOrder; m++) {
			int line = key.dimensions * m;
			for (int n = 0, i = line + n; n < key.dimensions; n++, i = line + n)
				if (k.testBit(i))
					sKey[m] = sKey[m].setBit(n);
		}
		return sKey;
	}
	
	private static double[] decodePoints(final BigInteger[] alpha, final int curveOrder, final int dimensions) {
		final double[] points = new double[dimensions];
		for(int n = 0; n < dimensions; n++) {
			int i = 0;
			for (int m = 0; m < curveOrder; m++) {
				i = alterBit(i, m, alpha[m].testBit(n));
			}
			points[n] = (double) i;
		}
		return points;
	}

	//Deriving a coordinate point from a hilbert key:
	public static DenseRIV decodeHilbertKey(HKey key) {
		BigInteger[] P = calculateP(key);
		int[] J = calculateJ(P, key.dimensions);
		BigInteger[] sigma = sigma(P);
		BigInteger[] tau = tau(sigma);
		BigInteger[] sigmaHat = addHat(sigma, J, key.dimensions);
		BigInteger[] tauHat = addHat(tau, J, key.dimensions);
		BigInteger[] omega = omega(tauHat);
		BigInteger[] alpha = alpha(omega, sigmaHat);
		double[] points = decodePoints(alpha, key.curveOrder, key.dimensions);
		return new DenseRIV(points);
	}
	*/
	
	
	//Second attempt
	//Do it sideways, so we don't have to deal with BigInts the whole way through.
	private static final int ORDER = 32;
	
	private static BigInteger encodeAsBigInt(int[] key) {
		BigInteger r = BigInteger.ZERO;
		int i = 0;
		for (int k : key) 
			for (int j = 0; j < ORDER; j++, i++)
				if(testBit(k, j))
					r = r.setBit(i);
		System.out.println("IsNegative: " + r.testBit(r.bitLength()) + ", " + r.toString());
		return r;
	}
	
	private static int[] decodeFromBigInt(BigInteger bint, int order, int dims) {
		int[] key = new int[0];
		int bits = order * dims;
		for (int i = 0, j = 0; j < bits; i++, j = i * ORDER) {
			int b = 0;
			for (int z = 0; z < ORDER; z++)
				b = alterBit(b, z, bint.testBit(z + j));
			key[i] = b;
		}
		return key;
	}
	
	
	private static int[] sCalculateJ(final int[] P) {
		int[] J = new int[ORDER];
		Arrays.fill(J, 0);
		for (int n = 1; n < P.length; n++) {
			for (int i = 1; i < ORDER; i++) {
				if (J[i] == 0 && (testBit(P[n], i) ^ testBit(P[0], i)))
					J[i] = n;
			}
		}
		return J;
	}
	
	private static int sPrincipalPosition(int[] sBits, int bitLine) {
		boolean atZero = testBit(sBits[0], bitLine);
		for (int n = 1; n < sBits.length; n++) {
			if (atZero ^ testBit(sBits[n], bitLine))
				return n;
		}
		return 0;
	}
	
	private static int[] sSigma(final int[] P) {
		int[] s = new int[P.length];
		Arrays.fill(s, 0);
		for (int i = 0; i < ORDER; i++) {
			s[0] = alterBit(s[0], i, testBit(P[0], i));
			for (int c = 1; c < P.length; c++) {
				boolean p = testBit(P[c], i) ^ testBit(P[c - 1], i);
				s[c] = alterBit(s[c], i, p);
			}
		}
		return s;
	}
	
	private static int[] sTau(final int[] P, final int[] sigma) {
		int parityBits = P[P.length];
		int[] t = Arrays.copyOf(sigma, sigma.length);
		t[t.length] = ~t[t.length];
		for (int i = 0; i < ORDER; i++) {
			if(!testBit(parityBits, i)) {
				int pPos = sPrincipalPosition(sigma, i);
				t[pPos] = flipBit(t[pPos], i);
			}
		}
		return t;
	}
	
	private static void sRotateRight(final int[] sBits, int bitLine, int distance) {
		boolean[] heldBits = new boolean[distance];
		for(int d = 0; d < distance; d++) {
			heldBits[d] = testBit(sBits[d], bitLine);
		}
		for(int d = 0; d < sBits.length - distance; d++) {
			sBits[d] = alterBit(sBits[d], bitLine, testBit(sBits[d + distance], bitLine));
		}
		for(int d = sBits.length - distance, b = 0; d < sBits.length; d++, b++) {
			sBits[d] = alterBit(sBits[d], bitLine, heldBits[b]);
		}
	}
	
	private static int[] sAddHat(final int[] sBits, int[] J) {
		int[] hat = Arrays.copyOf(sBits, sBits.length);
		int j = 0;
		for (int i = 0; i < J.length; i++) {
			j += J[i] - 1;
			sRotateRight(hat, i, j);
		}
		return hat;
	}
	
	private static int[] sOmega(final int[] tauHat) {
		int[] omega = new int[tauHat.length];
		Arrays.fill(omega, 0);
		for (int n = 0; n < tauHat.length; n++) {
			int o = omega[n];
			int t = tauHat[n];
			for (int i = 1; i < ORDER; i++)
				o = alterBit(o, i, testBit(o, i - 1) ^ testBit(t, i - 1));
			omega[n] = o;
		}
		return omega;
	}
	
	private static int[] sAlpha(final int[] omega, final int[] sigmaHat) {
		int[] alpha = new int[omega.length];
		for(int n = 0; n < alpha.length; n++)
			alpha[n] = omega[n] ^ sigmaHat[n];
		return alpha;
	}
	
	private static DenseRIV sDecodeHilbertKey(HKey key) {
		int[] P = decodeFromBigInt(key.k, key.order, key.dimensions);
		int[] J = sCalculateJ(P);
		int[] sigma = sSigma(P);
		int[] tau = sTau(P, sigma);
		int[] sigmaHat = sAddHat(sigma, J);
		int[] tauHat = sAddHat(tau, J);
		int[] omega = sOmega(tauHat);
		int[] alpha = sAlpha(omega, sigmaHat);
		double[] points = new double[alpha.length];
		Arrays.setAll(points, i -> (double) alpha[i]);
		return new DenseRIV(points);
	}
	
	//now do it fast.
	
	private static int MASK = 1 << ORDER - 1;
	
	private static int fCalculateP_i(int i, int[] keySections) {
		int element = i / ORDER;
		int iMod = i % ORDER;
		int P = keySections[element] >>> iMod;
		if (iMod > (ORDER - keySections.length)) {
			int temp1 = keySections[element + 1] << (ORDER - iMod);
			P |= temp1;
		}
		return P;
	}
	
	private static int gmask(int i) {
		return 1 << i;
	}
	
	private static int fReverseP_i(int sigma, int dims) {
		int rho = sigma & 1;
		for (int i = 1; i < dims; i++) {
			int m = gmask(i);
			if(1 == (sigma & m ^ (rho >>> 1) & m))
				rho |= m;
		}
		return rho;
	}
	
	private static int fReverseA_i(final int[] point, final int mask, final int dims) {
		int a = 0;
		for (int j = 0; j < dims; j++)
			if (1 == (point[j] & mask))
				a |= gmask(j);
		return a;
	}
	
	private static int fCalculateJ_i(int rho_i, int dims) {
		//For an integer p_i, find the subscript of the Principal Position,
		//defined as 
		int n = 1;
		int J = dims;
		for (; n < J; n++) {
			if ((rho_i >>> n & 1) != (rho_i & 1))
				break;
		}
		return (n == J) ? J : J - n;
	}
	
	private static int fCalculateS_i(int rho_i) {
		return rho_i ^ (rho_i >>> 1);
	}
	
	private static int fCalculateT_i(int rho_i) {
		if (rho_i < 3)
			return 0;
		else if (rho_i % 2 == 0)
			return fCalculateS_i(rho_i - 1);
		else
			return fCalculateS_i(rho_i - 2);
	}
	
	private static int fAddHat(int val, int jMod, int dims) {
		int res = val;
		int mod = jMod % dims;
		if (mod != 0) {
			int t1 = res >>> mod;
			int t2 = res << dims - mod;
			res = t1 | t2 & (1 << dims) - 1;
		}
		return res;
	}
	
	public static void distributeBits(int[] coords, int rho_or_alpha, int mask, int dims) {
		for (int j = dims - 1; rho_or_alpha > 0; rho_or_alpha >>>= 1, j--)
			if ((rho_or_alpha & 1) == 1)
				coords[j] |= mask;
	}
	
	public static DenseRIV fDecodeHilbertKey (BigInteger key, final int dims) {
		final int order = ORDER;
		final int[] point = new int[dims];
		Arrays.fill(point, 0);
		final int[] keySections = decodeFromBigInt(key, order, dims);
		//cycle once to initialize everything
		int i = (order - 1) * dims;
		int m = MASK;
		int rho = fCalculateP_i(i, keySections);
		int J = fCalculateJ_i(rho, dims);
		int xJ = J - 1;
		int alpha, sigma, sigmaHat = sigma = alpha = fCalculateS_i(rho);
		int tau, tauHat = tau = fCalculateT_i(rho);
		distributeBits(point, rho, m, dims);
		
		//enter the loop		
		i -= dims;
		m >>>= 1;
		int omega = 0;
		for (; i >= 0; i -= dims, m >>>= 1) {
			rho = fCalculateP_i(i, keySections);
			sigma = fCalculateS_i(rho);
			sigmaHat = fAddHat(sigma, xJ, dims);
			omega ^= tauHat;
			alpha = omega ^ sigmaHat;
			distributeBits(point, alpha, m, dims);
			if (i > 0) {
				tau = fCalculateT_i(rho);
				tauHat = fAddHat(tau, xJ, dims);
				J = fCalculateJ_i(rho, dims);
				xJ += J - 1;
			}
		}
		return new DenseRIV(point);
	}
	
	private static void finalEncodeStep(final int[] keySections, final int rho, final int i, final int dims)  {
		final int e = i / ORDER;
		final int iMod = i % ORDER;
		if (iMod > ORDER - dims) {
			keySections[e] |= rho << iMod;
			keySections[e + 1] |= rho >>> ORDER - iMod;
		} else
			keySections[e] |= rho << i - e * ORDER;
	}
	
	public static BigInteger fEncodeHilbertKey (RIV riv) {
		final int dims = riv.size();
		final int[] point = new int[dims];
		for (int i = 0; i < dims; i++)
			point[i] = (int)riv.get(i);

		final int[] keySections = new int[dims];
		Arrays.fill(keySections, 0);
		
		int i = (ORDER - 1) * dims;
		int m = MASK;
		
		//cycle once to initialize everything
		int alpha = fReverseA_i(point, m, dims);		
		int sigma = alpha,
				sigmaHat = alpha;
		int rho = fReverseP_i(sigma, dims);
		finalEncodeStep(keySections, rho, i, dims);
		
		int J = fCalculateJ_i(rho, dims);
		int xJ = J - 1;
		int tau = fCalculateT_i(rho);
		int tauHat = tau;
		
		//enter the loop
		i -= dims;
		m >>>= 1;
		int omega = 0;
		for (; i >= 0; i -= dims, m >>>= 1) {
			alpha = fReverseA_i(point, m, dims);
			omega ^= tauHat;
			sigmaHat = alpha ^ omega;
			sigma = fAddHat(sigmaHat, xJ, dims);
			rho = fReverseP_i(sigma, dims);
			finalEncodeStep(keySections, rho, i, dims);
			
			if (i > 0) {
				tau = fCalculateT_i(rho);
				tauHat = fAddHat(tau, xJ, dims);
				J = fCalculateJ_i(rho, dims);
				xJ += J - 1;
			}
		}
		System.out.println("Hilbert Key sections: " + Arrays.toString(keySections));
		return encodeAsBigInt(keySections);
	}
	
	public static BigInteger encodeHilbillyKey(RIV riv) {
		int[] allVals = new int[riv.size()];
		for (int i = 0; i < allVals.length; i++) {
			allVals[i] = (int) Math.round(riv.get(i));
		}
		return encodeAsBigInt(allVals);
	}
	
	
	
	
	//direct (as possible) transposition of C code given by Lawder here: http://www.dcs.bbk.ac.uk/~jkl/pubs/JL1_00a.pdf
	
	private static final int DIM = 3;
	private static final int[] g_mask = IntStream.iterate(DIM - 1, i -> i - 1).limit(DIM).map(i -> 1 << i).toArray();
	
	private static int calc_p(final int i, HCode h) {
		int element = i / ORDER;
		int p = h.c[element];
		int iMod = i % ORDER;
		if (iMod> ORDER - DIM) {
			int temp1 = h.c[element + 1];
			p >>>= iMod;
			temp1 <<= (ORDER - iMod);
			p |= temp1;
		} else {
			p >>>= iMod;
		}
		
		if (DIM < ORDER)
			p &= ((1 << DIM) - 1);
		
		return p;
	}
	
	private static int calc_p2(final int sigma) {
		int p = sigma & g_mask[0];
		for (int i = 1; i < DIM; i++)
			if ((sigma & g_mask[i] ^ (p >>> 1) & g_mask[i]) == 1)
				p |= g_mask[i];
		return p;
	}
	
	private static int calc_j(final int p) {
		int i;
		for (i = 1; i < DIM; i++)
			if ((p >>> i & 1) != (p % 1))
				break;
		return (i == DIM)
				? DIM 
						: DIM - i;
	}
	
	private static int calc_t(final int p) {
		if (p < 3)
			return 0;
		else if (p % 2 == 0)
			return (p - 1) ^ (p - 1) / 2;
		else
			return (p - 2) ^ (p - 2) / 2;
	}
	
	private static int calc_ts_tt(int xJ, int val) {
		int retval = val;
		if (xJ % DIM != 0) {
			int temp1 = val >>> xJ % DIM;
			int temp2 = val << DIM - xJ % DIM;
			retval = (temp1 | temp2) & ((1 << DIM) - 1);
		}
		return retval;
	}
	
	private static final int I = ORDER * DIM - DIM;
	
	private static Point H_decode (HCode h) {
		int mask = MASK;
		int w = 0;
		
		Point point = new HCode(DIM);
		int i = I;
		
		int p = calc_p(i, h);
		int j = calc_j(p);
		int xj = j - 1;
		int a, s, ts = a = s = p ^ p / 2;
		int t, tt = t = calc_t(p);
		
		for (j = DIM - 1; p > 0; p = p>>1, j--) 
			if ((p & 1) == 1)
				point.c[j] |= mask;
		
		for (i -= DIM - 1, mask >>>= 1; i >= 0; i -= DIM, mask >>>=1) {
			p = calc_p(i, h);
			s = p ^ p / 2;
			ts = calc_ts_tt(xj, s);
			w ^= tt;
			a = w ^ ts;
			
			/*--- distrib bits to coords ---*/
			for (j = DIM - 1; a > 0; a >>>= 1, j--)
				if ((a & 1) == 1)
					point.c[j] |= mask;
			
			if (i > 0) {
				t = calc_t(p);
				tt = calc_ts_tt(xj, t);
				j = calc_j(p);
				xj += j - 1;
			}
		}
		
		return point;
	}
	
	private static void encodeHelper1(int a, int j, int mask, Point point) {
		for (j = a = 0; j < DIM; j++)
			if ((point.c[j] & mask) == 1)
				a |= g_mask[j];
	}
	
	private static void encodeHelper2(int element, int i, int p, HCode h) {
		element = i / ORDER;
		if (i % ORDER > ORDER - DIM) {
			h.c[element] |= p << i % ORDER;
			h.c[element + 1] |= p >>> ORDER - i % ORDER;
		} else
			h.c[element] |= i - element * ORDER;
	}
	
	private static HCode H_encode(Point point) {
		int mask = MASK;
		int w = 0;
		
		HCode h = new HCode(DIM);
		int i = I;
		int a, j = a = 0;
		encodeHelper1(a, j, mask, point);
		
		int s, ts = s = a;
		int p = calc_p2(s);
		int element =  0;
		encodeHelper2(element, i, p, h);
		
		j = calc_j(p);
		int xj = j - 1;
		int t, tt = t = calc_t(p);
		
		for (i -= DIM, mask >>>= 1; i >= 0; i -= DIM, mask >>>= 1) {
			encodeHelper1(a, j, mask, point);
			
			w ^= tt;
			ts = a ^ w;
			s = calc_ts_tt(xj, ts);
			p = calc_p2(s);
			
			/* add in DIM bits to hcode */
			encodeHelper2(element, i, p, h);
			
			if (i > 0) {
				t = calc_t(p);
				tt = calc_ts_tt(xj, t);
				j = calc_j(p);
				xj += j - 1;
			}
		}
		
		return h;
	}
}
