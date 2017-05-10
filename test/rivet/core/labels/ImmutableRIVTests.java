package rivet.core.labels;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
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
	private static double[] testVals = new double[] {
			1.0,1.0,1.0,1.0
	};
	private static int size = 7;

	@Test
	public void testGetHilbertKey() {
		ImmutableRIV[] test = new ImmutableRIV[] {
				new ImmutableRIV(testKeysB, testVals, size),
				new ImmutableRIV(testKeysD, testVals, size),
				new ImmutableRIV(testKeysA, testVals, size),
				new ImmutableRIV(testKeysC, testVals, size)
		};
		Arrays.sort(test, (a, b) -> a.getHilbertKey().compareTo(b.getHilbertKey()));
		for(ImmutableRIV r : test)
			System.out.println(r.toString());
		assertArrayEquals(test[0].keyArr(),testKeysA);
		assertArrayEquals(test[1].keyArr(), testKeysB);
		assertArrayEquals(test[2].keyArr(),testKeysC);
		assertArrayEquals(test[3].keyArr(), testKeysD);
	}

}
