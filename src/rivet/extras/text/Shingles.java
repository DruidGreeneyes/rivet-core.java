package rivet.extras.text;

import static java.util.Arrays.stream;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import rivet.core.util.Util;
import rivet.core.arraylabels.*;
import rivet.core.exceptions.ShingleInfection;

public final class Shingles {
	private Shingles(){}
	
	public static int[] findShinglePoints (String text, int offset) throws ShingleInfection {
		if (text == null || text.isEmpty())
			throw new ShingleInfection("THIS TEXT IS NOT TEXT!");
		if (offset == 0)
			throw new ShingleInfection("THIS OFFSET IS A VIOLATION OF THE TOS! PREPARE FOR LEGAL ACTION!");
		return Util.range(0, text.length(), offset).toArray();
	}
	
	public static String[] shingleText(String text, int width, int offset) {
		String[] res = new String[0];
		for (int i = 0; i < text.length(); i += offset)
			res = ArrayUtils.add(res, text.substring(i, i + width));
		return res;
	}
	
	public static RIV[] rivShingles (String[] shingles, int size, int k) {
		return stream(shingles)
			.map(Labels.labelGenerator(size, k))
			.toArray(RIV[]::new);
	}
	
	public static RIV[] rivShingles (String text, int[] shinglePoints, int width, int size, int k) {
		int length = text.length();
		return Arrays.stream(shinglePoints)
					.mapToObj((point) -> 
						Labels.generateLabel(
								size,
								k,
								text,
								point,
								(point + width < length) 
									? width
											: length - point))
					.toArray(RIV[]::new);
	}
	
	public static RIV rivAndSumShingles (String text, int[] shinglePoints, int width, int size, int k) {
		int length = text.length();
		return Arrays.stream(shinglePoints)
			.boxed()
			.reduce(new RIV(size),
					(riv, point) -> riv.add(
							Labels.generateLabel(
									size,
									k,
									text,
									point,
									(point + width < length) 
										? width
												: length - point)),
					(rivA, rivB) -> Labels.addLabels(rivA, rivB));
	}
	
	public static RIV sumRIVs (RIV[] rivs) { return Labels.addLabels(rivs); }
	
	public static RIV rivettizeText(String text, int width, int offset, int size, int k) throws ShingleInfection {
		int[] points = findShinglePoints(text, offset);
		return rivAndSumShingles(text, points, width, size, k);
	}
}