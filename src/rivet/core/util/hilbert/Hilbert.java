package rivet.core.util.hilbert;

import static java.math.BigInteger.ZERO;
import static java.math.BigInteger.ONE;

import java.math.BigInteger;
import java.util.Arrays;
import rivet.core.labels.DenseRIV;
import rivet.core.labels.RIV;

public final class Hilbert {
  private Hilbert() {}

  // integer bit ops
  private static int bitAtPoint(final int n, final int position) {
    return bitIntersection(n, nthBit(position));
  }

  private static boolean testBit(final int n, final int position) {
    return bitAtPoint(n, position) != 0;
  }

  private static int ones(final int howMany) {
    return ~0 >> 32 - howMany;
  }

  private static int nthBit(final int n) {
    return 1 << n;
  }

  private static int alterBit(final int n, final int pos,
                              final boolean newValue) {
    return newValue
                    ? setBit(n, pos)
                    : clearBit(n, pos);
  }

  private static int setBit(final int n, final int pos) {
    return bitUnion(n, nthBit(pos));
  }

  private static int clearBit(final int n, final int pos) {
    return clearBits(n, nthBit(pos));
  }

  private static int clearBits(final int n, final int mask) {
    return bitIntersection(n, invertBits(mask));
  }

  private static int invertBits(final int n) {
    return ~n;
  }

  private static int bitIntersection(final int n, final int mask) {
    return n & mask;
  }

  private static int flipBit(final int n, final int pos) {
    return bitDifference(n, nthBit(pos));
  }

  private static int bitDifference(final int n, final int mask) {
    return n ^ mask;
  }

  private static int bitUnion(final int n, final int mask) {
    return n | mask;
  }

  @FunctionalInterface
  public interface ObjIntPredicate<T> {
    boolean test(T obj, int i);
  }

  /*
   * From source: n = number of dimensions in the target space (16k) m = order
   * of the curve passing through a space (higher = less information loss. Say,
   * 16, for giggles) N = the number of bits in a derived key, =n * m i = a
   * number in the range (1..m) we iterate m times through the algo, so i also
   * represents conveniently which iteration we are on. j = a number in the
   * range (1..n) r = a result key, in binary, of N bits, divided into m
   * sections of n digits each. p.i.j = a single binary digit in R; the nth
   * digit of the mth section. NOTE: byte is used in the source material in
   * place of section, but since byte is the 8-bit integer type in java, I chose
   * not to use it here. a.j = a single coordinate in dimension j of point a.
   * alpha.i.j = a binary digit in a coordinate a.j, such that [A.1.j..A.m.j] is
   * the binary representation of j. alpha.i = binary number composed of
   * [A.i.1..A.i.n]
   */

  private static final BigInteger TWO = BigInteger.valueOf(2L);
  private static final BigInteger THREE = BigInteger.valueOf(3L);

  private static BigInteger genHighBitMask(final int dims) {
    return ONE.shiftLeft(dims).subtract(ONE);
  }

  private static BigInteger sigma_i(final BigInteger rho) {
    return rho.xor(rho.shiftRight(1));
  }

  private static BigInteger tau_i(final BigInteger rho) {
    if (rho.compareTo(THREE) < 0)
      return ZERO;
    else if (!rho.testBit(0))
      return sigma_i(rho.subtract(ONE));
    else
      return sigma_i(rho.subtract(TWO));
  }

  private static int principlePosition(final BigInteger n, final int dims) {
    final boolean atZero = n.testBit(0);
    for (int i = 1; i < dims; i++)
      if (atZero ^ n.testBit(i))
        return dims - i;
    return dims;
  }

  private static int J_i(final BigInteger rho, final int dims) {
    return principlePosition(rho, dims);
  }

  private static BigInteger _rotateRight(final BigInteger n, final int distance,
                                         final int dims,
                                         final BigInteger highBitMask) {
    return n.or(n.shiftLeft(dims)).shiftRight(distance).and(highBitMask);
  }

  private static BigInteger _rotateLeft(final BigInteger n, final int distance,
                                        final int dims,
                                        final BigInteger highBitMask) {
    return n.shiftLeft(distance)
            .or(n.shiftRight(dims - distance))
            .and(highBitMask);
  }

  private static BigInteger addHat_i(final BigInteger noHat, final int jMod,
                                     final int dimensions,
                                     final BigInteger highBitMask) {
    return _rotateRight(noHat, jMod, dimensions, highBitMask);
  }

  private static BigInteger omega_i(final BigInteger omega,
                                    final BigInteger tauHatPrev) {
    return omega.xor(tauHatPrev);
  }

  private static BigInteger alpha_i(final BigInteger omega,
                                    final BigInteger sigmaHat) {
    return omega.xor(sigmaHat);
  }

  private static BigInteger trimHighBits(final BigInteger n, final int dims) {
    BigInteger b = n;
    final int end = n.bitLength();
    for (int i = dims; i < end; i++)
      b = b.clearBit(i);
    return b;
  }

  private static BigInteger rho_i(final BigInteger key, final int dims,
                                  final int i) {
    return trimHighBits(key.shiftRight(i * dims), dims);
  }

  private static double[] decodePoints(final BigInteger[] alpha,
                                       final int dimensions) {
    final double[] points = new double[dimensions];
    for (int n = 0; n < dimensions; n++) {
      int i = 0;
      for (int m = 0; m < ORDER; m++)
        if (alpha[m].testBit(n))
          i = setBit(i, m);
      points[n] = i;
    }
    return points;
  }

  // Deriving a coordinate point from a hilbert key:
  public static DenseRIV decodeHilbertKey(final BigInteger key,
                                          final int dims) {
    final BigInteger[] alpha = new BigInteger[ORDER];
    final BigInteger highBitMask = genHighBitMask(dims);
    BigInteger rho = rho_i(key, dims, 0);
    int J = J_i(rho, dims);
    int jMod = 0;
    BigInteger sigma = rho;
    BigInteger tau = tau_i(rho);
    BigInteger sigmaHat = sigma;
    BigInteger omega = ZERO;
    BigInteger tauHat = tau;
    alpha[0] = alpha_i(omega, sigmaHat);
    for (int i = 1; i < ORDER; i++) {
      rho = rho_i(key, dims, i);
      jMod += J - 1;
      J = J_i(rho, dims);
      sigma = sigma_i(rho);
      tau = tau_i(rho);
      sigmaHat = addHat_i(sigma, jMod, dims, highBitMask);
      omega = omega_i(omega, tauHat);
      tauHat = addHat_i(tau, jMod, dims, highBitMask);
      alpha[i] = alpha_i(omega, sigmaHat);
    }
    final double[] points = decodePoints(alpha, dims);
    return new DenseRIV(points);
  }

  private static BigInteger rAlpha_i(final int[] point, final int i,
                                     final int dims) {
    BigInteger alpha = ZERO;
    for (int n = 0; n < dims; n++)
      if (testBit(point[n], i))
        alpha = alpha.setBit(n);
    return alpha;
  }

  private static BigInteger rSigmaHat_i(final BigInteger alpha,
                                        final BigInteger omega) {
    return alpha.xor(omega);
  }

  private static BigInteger rAddHat_i(final BigInteger n, final int jMod,
                                      final int dims,
                                      final BigInteger highBitMask) {
    return _rotateLeft(n, jMod, dims, highBitMask);
  }

  private static BigInteger rRho_i(final BigInteger sigma) {
    return sigma.xor(sigma.shiftRight(1));
  }

  public static BigInteger encodeHilbertKey(final RIV riv) {
    final int dims = riv.size();
    final BigInteger highBitMask = genHighBitMask(dims);
    final int[] point = new int[dims];
    for (int i = 0; i < dims; i++)
      point[i] = (int) riv.get(i);
    BigInteger alpha = rAlpha_i(point, 0, dims);
    BigInteger omega = ZERO;
    BigInteger sigmaHat = alpha;
    BigInteger sigma = sigmaHat;
    BigInteger rho = sigma;
    int J = J_i(rho, dims);
    int jMod = 0;
    BigInteger tau = tau_i(rho);
    BigInteger tauHat = tau;
    BigInteger key = rho;
    for (int i = 1; i < ORDER; i++) {
      alpha = rAlpha_i(point, i, dims);
      omega = omega_i(omega, tauHat);
      sigmaHat = rSigmaHat_i(alpha, omega);
      jMod += J - 1;
      sigma = rAddHat_i(sigmaHat, jMod, dims, highBitMask);
      rho = rRho_i(sigma);
      J = J_i(rho, dims);
      tau = tau_i(rho);
      tauHat = rAddHat_i(tau, jMod, dims, highBitMask);
      key = key.or(rho.shiftLeft(i * dims));
    }
    return key;
  }

  // Second attempt
  // Do it sideways, so we don't have to deal with BigInts the whole way
  // through.
  private static final int ORDER = 32;

  private static BigInteger encodeAsBigInt(final int[] key) {
    BigInteger r = BigInteger.ZERO;
    int i = 0;
    for (final int k : key)
      for (int j = 0; j < ORDER; j++, i++)
        if (testBit(k, j))
          r = r.setBit(i);
    return r;
  }

  private static BigInteger sEncodeAsBigInt(final int[] key) {
    BigInteger r = BigInteger.ZERO;
    int i = 0;
    for (int j = 0; j < ORDER; j++)
      for (final int k : key) {
        if (testBit(k, j))
          r = r.setBit(i);
        i++;
      }
    return r;
  }

  private static int[] decodeFromBigInt(final BigInteger bint, final int order,
                                        final int dims) {
    final int[] key = new int[0];
    final int bits = order * dims;
    for (int i = 0, j = 0; j < bits; i++, j = i * ORDER) {
      int b = 0;
      for (int z = 0; z < ORDER; z++)
        b = alterBit(b, z, bint.testBit(z + j));
      key[i] = b;
    }
    return key;
  }

  private static int sPrincipalPosition(final int[] sBits, final int bitLine,
                                        final int dims) {
    final boolean lsb = testBit(sBits[0], bitLine);
    for (int n = 1; n < dims; n++)
      if (lsb ^ testBit(sBits[n], bitLine))
        return dims - n;
    return dims - 1;
  }

  private static int[] sCalculateJ(final int[] P) {
    final int[] J = new int[ORDER];
    for (int i = 0; i < ORDER; i++)
      J[i] = sPrincipalPosition(P, i, P.length);
    return J;
  }

  private static int[] sSigma(final int[] P, final int dims) {
    final int[] s = new int[P.length];
    final int end = dims - 1;
    Arrays.fill(s, 0);
    for (int i = ORDER - 1; i >= 0; i--) {
      s[end] = alterBit(s[end], i, testBit(P[end], i));
      for (int c = end - 1; c >= 0; c--) {
        final boolean p = testBit(P[c], i) ^ testBit(P[c + 1], i);
        s[c] = alterBit(s[c], i, p);
      }
    }
    return s;
  }

  private static int[] sTau(final int[] P, final int[] sigma, final int dims) {
    final int parityBits = P[0];
    final int[] t = Arrays.copyOf(sigma, sigma.length);
    t[0] = ~t[0];
    for (int m = ORDER - 1; m >= 0; m--)
      if (!testBit(parityBits, m)) {
        final int pPos = sPrincipalPosition(sigma, m, dims);
        t[pPos] = flipBit(t[pPos], m);
      }
    return t;
  }

  private static void sRotateRight(final int[] sBits, final int bitLine,
                                   final int distance) {
    final int dis = distance % sBits.length;
    if (dis == 0) return;
    final boolean[] heldBits = new boolean[dis];
    for (int d = 0; d < dis; d++)
      heldBits[d] = testBit(sBits[d], bitLine);
    for (int d = 0; d < sBits.length - dis; d++)
      sBits[d] = alterBit(sBits[d], bitLine, testBit(sBits[d + dis], bitLine));
    for (int d = sBits.length - dis, b = 0; d < sBits.length; d++, b++)
      sBits[d] = alterBit(sBits[d], bitLine, heldBits[b]);
  }

  private static int[] sAddHat(final int[] sBits, final int[] J) {
    final int[] hat = Arrays.copyOf(sBits, sBits.length);
    int j = 0;
    for (int i = 0; i < J.length; i++) {
      j += J[i] - 1;
      sRotateRight(hat, i, j);
    }
    return hat;
  }

  private static int[] sOmega(final int[] tauHat, final int dims) {
    final int[] omega = new int[tauHat.length];
    Arrays.fill(omega, 0);
    for (int n = dims - 2; n >= 0; n--) {
      int o = omega[n];
      final int t = tauHat[n];
      for (int i = ORDER; i >= 0; i--)
        o = alterBit(o, i, testBit(o, i + 1) ^ testBit(t, i + 1));
      omega[n] = o;
    }
    return omega;
  }

  private static int[] sAlpha(final int[] omega, final int[] sigmaHat) {
    final int[] alpha = new int[omega.length];
    for (int n = alpha.length - 1; n >= 0; n--)
      alpha[n] = omega[n] ^ sigmaHat[n];
    return alpha;
  }

  public static DenseRIV sDecodeHilbertKey(final BigInteger key,
                                           final int dims) {
    final int[] P = decodeFromBigInt(key, ORDER, dims);
    final int[] J = sCalculateJ(P);
    final int[] sigma = sSigma(P, dims);
    final int[] tau = sTau(P, sigma, dims);
    final int[] sigmaHat = sAddHat(sigma, J);
    final int[] tauHat = sAddHat(tau, J);
    final int[] omega = sOmega(tauHat, dims);
    final int[] alpha = sAlpha(omega, sigmaHat);
    final double[] points = new double[alpha.length];
    Arrays.setAll(points, i -> (double) alpha[i]);
    return new DenseRIV(points);
  }

  private static void sReverseOmega_i(final int[] omega, final int[] tauHat,
                                      final int bitLine, final int dims) {
    if (bitLine < dims - 1) {
      final int n = bitLine + 1;
      for (int i = 0; i < omega.length; i++)
        omega[i] = alterBit(omega[i], bitLine,
                            testBit(omega[i], n) ^ testBit(tauHat[i], n));
    }
  }

  private static void sReverseSigmaHat_i(final int[] sigmaHat,
                                         final int[] alpha, final int[] omega,
                                         final int bitLine) {
    for (int i = 0; i < omega.length; i++)
      sigmaHat[i] = alterBit(sigmaHat[i],
                             bitLine, testBit(alpha[i], bitLine)
                                      ^ testBit(omega[i], bitLine));
  }

  private static void sRotateLeft(final int[] sBits, final int bitLine,
                                  final int distance) {
    final int dis = distance % sBits.length;
    if (dis == 0) return;
    final boolean[] heldBits = new boolean[dis];
    final int end = sBits.length - 1;
    for (int b = 0, d = end; d > end - dis; d--, b++)
      heldBits[b] = testBit(sBits[d], bitLine);
    for (int d = sBits.length - 1; d >= dis; d--)
      sBits[d] = alterBit(sBits[d], bitLine, testBit(sBits[d - dis], bitLine));
    for (int d = dis - 1, b = 0; d >= 0; d--, b++)
      sBits[d] = alterBit(sBits[d], bitLine, heldBits[b]);
  }

  private static void copyBitLine(final int[] src, final int[] dest,
                                  final int bitLine) {
    for (int i = 0; i < src.length; i++)
      dest[i] = alterBit(dest[i], bitLine, testBit(src[i], bitLine));
  }

  private static void sReverseAddHat_i(final int[] noHat, final int[] hat,
                                       final int bitLine, final int jMod) {
    copyBitLine(noHat, hat, bitLine);
    sRotateLeft(hat, bitLine, jMod);
  }

  private static void sReverseRho_i(final int[] rho, final int[] sigma,
                                    final int bitLine) {
    final int end = rho.length - 1;
    rho[end] = alterBit(rho[end], bitLine, testBit(sigma[end], bitLine));
    for (int m = end - 1; m >= 0; m--)
      rho[m] = alterBit(rho[m], bitLine, testBit(sigma[m + 1], bitLine));
  }

  private static void sReverseJ_i(final int[] J, final int[] rho,
                                  final int bitLine) {
    J[bitLine] = sPrincipalPosition(rho, bitLine, rho.length);
  }

  private static void sReverseTau_i(final int[] tau, final int[] sigma,
                                    final int[] rho, final int bitLine) {
    copyBitLine(sigma, tau, bitLine);
    tau[0] = flipBit(tau[0], bitLine);
    if (!testBit(rho[0], bitLine)) {
      final int p = sPrincipalPosition(sigma, bitLine, sigma.length);
      tau[p] = flipBit(tau[p], bitLine);
    }
  }

  public static BigInteger sEncodeHilbertKey(final RIV riv) {
    final int dims = riv.size();
    final int[] alpha = new int[dims];
    for (int i = 0; i < dims; i++)
      alpha[i] = (int) Math.round(riv.get(i));
    int bitLine = ORDER - 1;
    final int[] omega = new int[dims];
    Arrays.fill(omega, 0);
    final int[] sigmaHat = Arrays.copyOf(alpha, dims);
    sReverseSigmaHat_i(sigmaHat, alpha, omega, bitLine);
    final int[] sigma = Arrays.copyOf(sigmaHat, dims);
    final int[] rho = Arrays.copyOf(sigma, dims);
    sReverseRho_i(rho, sigma, bitLine);
    final int[] J = new int[ORDER];
    sReverseJ_i(J, rho, bitLine);
    final int[] tau = Arrays.copyOf(sigma, dims);
    sReverseTau_i(tau, sigma, rho, bitLine);
    final int[] tauHat = Arrays.copyOf(tau, dims);
    int jMod = J[bitLine] - 1;

    for (bitLine--; bitLine >= 0; bitLine--) {
      sReverseOmega_i(omega, tauHat, bitLine, dims);
      sReverseSigmaHat_i(sigmaHat, alpha, omega, bitLine);
      sReverseAddHat_i(sigmaHat, sigma, bitLine, jMod);
      sReverseRho_i(rho, sigma, bitLine);
      sReverseJ_i(J, rho, bitLine);
      sReverseTau_i(tau, sigma, rho, bitLine);
      sReverseAddHat_i(tau, tauHat, bitLine, jMod);
      jMod += J[bitLine] - 1;
    }
    return sEncodeAsBigInt(rho);
  }

  // now do it fast.

  private static int MASK = nthBit(ORDER - 1);

  private static int[] gmask = generateGMask();

  private static int[] generateGMask() {
    final int[] gmask = new int[ORDER];
    for (int i = 0; i < ORDER; i++)
      gmask[i] = 1 << i;
    return gmask;
  }

  private static int gmask(final int n, final int dims) {
    return gmask[(dims - 1 - n) % ORDER];
  }

  private static int _gmask(final int n, final int dims) {
    final int m = (dims - 1 - n) % ORDER;
    return 1 << m;
  }

  /*
   * Tests if one of the following is true: A) sigma has a 1 in position pos and
   * rho does not have a 1 in position pos+1 B) rho has a 1 in position pos+1
   * and sigma does not have a 1 in position pos
   */
  private static boolean pTest(final int sigma, final int rho,
                               final int mask) {
    final int r = rho >> 1;
    return (sigma & mask ^ r & mask) == 1;
  }

  private static int fReverseP_i(final int sigma,
                                 final int dims) {
    int rho = sigma & gmask(0, dims);
    for (int i = 1; i < dims; i++) {
      final int mask = gmask(i, dims);
      if (pTest(sigma, rho, mask))
        rho = setBit(rho, i);
    }
    return rho;
  }

  private static int fReverseA_i(final int[] point, final int mask,
                                 final int dims) {
    int a = 0;
    for (int j = 0; j < dims; j++)
      if (1 == (point[j] & mask))
        a = setBit(a, j);
    return a;
  }

  private static int fCalculateJ_i(final int rho_i, final int dims) {
    // For an integer rho_i, find the subscript of the Principal Position,
    // defined as
    int n = 1;
    final int J = dims;
    for (; n < J; n++)
      if (testBit(rho_i, n) && testBit(rho_i, 0))
        break;
    return n == J ? J : J - n;
  }

  private static int fCalculateS_i(final int rho_i) {
    return rho_i ^ rho_i << 1;
  }

  private static int fCalculateT_i(final int rho_i) {
    if (rho_i < 3)
      return 0;
    else if (rho_i % 2 == 0)
      return fCalculateS_i(rho_i - 1);
    else
      return fCalculateS_i(rho_i - 2);
  }

  private static int fAddHat(final int val, final int jMod, final int dims) {
    int res = val;
    final int mod = jMod % dims;
    final int d = dims % ORDER;
    if (mod != 0) {
      final int t1 = res >>> mod;
      final int t2 = res << dims - mod;
      res = t1 | t2 & ones(d);
    }
    return res;
  }

  private static void finalEncodeStep(final int[] keySections, final int rho,
                                      final int i, final int dims) {
    final int e = i / ORDER;
    final int iMod = i % ORDER;
    if (iMod > ORDER - dims) {
      keySections[e] |= rho << iMod;
      keySections[e + 1] |= rho >>> ORDER - iMod;
    } else
      keySections[e] |= rho << i - e * ORDER;
  }

  public static BigInteger fEncodeHilbertKey(final RIV riv) {
    final int dims = riv.size();
    final int[] point = new int[dims];
    for (int i = 0; i < dims; i++)
      point[i] = (int) Math.round(riv.get(i));

    final int[] keySections = new int[dims];
    Arrays.fill(keySections, 0);

    int i = (ORDER - 1) * dims;
    int m = MASK;

    // cycle once to initialize everything
    int alpha = fReverseA_i(point, m, dims);
    int sigma = alpha,
        sigmaHat = alpha;
    int rho = fReverseP_i(sigma, dims);
    finalEncodeStep(keySections, rho, i, dims);

    int J = fCalculateJ_i(rho, dims);
    int xJ = J - 1;
    int tau = fCalculateT_i(rho);
    int tauHat = tau;

    // enter the loop
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
    return encodeAsBigInt(keySections);
  }

  public static BigInteger encodeHilbillyKey(final RIV riv) {
    final int[] allVals = new int[riv.size()];
    for (int i = 0; i < allVals.length; i++)
      allVals[i] = (int) Math.round(riv.get(i));
    return encodeAsBigInt(allVals);
  }

  public static BigInteger sEncodeHilbillyKey(final RIV riv) {
    final int[] allVals = new int[riv.size()];
    for (int i = 0; i < allVals.length; i++)
      allVals[i] = (int) Math.round(riv.get(i));
    return sEncodeAsBigInt(allVals);
  }
}