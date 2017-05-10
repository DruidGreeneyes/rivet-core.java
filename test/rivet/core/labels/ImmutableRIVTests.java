package rivet.core.labels;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

public class ImmutableRIVTests {
	
	private static int[] testKeysA = new int[] {
			0,1,2,3
	};
	private static int[] testKeysB = new int[] {
			1,2,3,4
	};
	private static int[] testKeysC = new int[] {
			2,3,4,5
	};
	private static int[] testKeysD = new int[] {
			3,4,5,6
	};
	private static double[] testVals = generateTestVals();
	private static int size = 7;
	
	private static double[] generateTestVals() {
		double[] res = new double[4];
		Random r = new Random();
		byte[] a = new byte[4];
		byte[] b =new byte[4];
		r.nextBytes(a);
		r.nextBytes(b);
		for (int i = 0; i < 4; i++)
			res[i] = (a[i] * b[i]) / 1.0;
		return res;
	}

	@Test
	public void testGetHilbertKey() {
		System.out.println("testVals: " + Arrays.toString(testVals));
		ImmutableRIV[] test = new ImmutableRIV[] {
				new ImmutableRIV(testKeysB, testVals, size),
				new ImmutableRIV(testKeysD, testVals, size),
				new ImmutableRIV(testKeysA, testVals, size),
				new ImmutableRIV(testKeysC, testVals, size)
		};
		Arrays.sort(test, (a, b) -> a.getHilbertKey().compareTo(b.getHilbertKey()));
		System.out.println("Hilbert keys:");
		for(ImmutableRIV r : test)
			System.out.println(r.getHilbertKey().toString(2) + ": " + Arrays.toString(r.keyArr()));
		assertArrayEquals(test[0].keyArr(),testKeysA);
		assertArrayEquals(test[1].keyArr(), testKeysB);
		assertArrayEquals(test[2].keyArr(),testKeysC);
		assertArrayEquals(test[3].keyArr(), testKeysD);
	}
	
	@Test
	public void testGetHilbillyKey() {
		System.out.println("testVals: " + Arrays.toString(testVals));
		ImmutableRIV[] test = new ImmutableRIV[] {
				new ImmutableRIV(testKeysB, testVals, size),
				new ImmutableRIV(testKeysD, testVals, size),
				new ImmutableRIV(testKeysA, testVals, size),
				new ImmutableRIV(testKeysC, testVals, size)
		};
		Arrays.sort(test, (a, b) -> a.getHilbillyKey().compareTo(b.getHilbillyKey()));
		System.out.println("Hilbilly keys:");
		for(ImmutableRIV r : test)
			System.out.println(r.getHilbillyKey().toString(2) + ": " + Arrays.toString(r.keyArr()));
		assertArrayEquals(test[0].keyArr(),testKeysA);
		assertArrayEquals(test[1].keyArr(), testKeysB);
		assertArrayEquals(test[2].keyArr(),testKeysC);
		assertArrayEquals(test[3].keyArr(), testKeysD);
	}
}
