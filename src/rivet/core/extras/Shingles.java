package rivet.core.extras;

import java.util.Arrays;

import rivet.core.util.Util;
import rivet.core.exceptions.ShingleInfection;
import rivet.core.labels.ArrayRIV;

public final class Shingles {
    private Shingles(){}
    
    public static int[] findShinglePoints (String text, int offset, int width) throws ShingleInfection {
        if (text == null || text.isEmpty())
            throw new ShingleInfection("THIS TEXT IS NOT TEXT!");
        if (offset < 1 )
            throw new ShingleInfection("THIS OFFSET IS A VIOLATION OF THE TOS! PREPARE FOR LEGAL ACTION!");
        return (offset == 1)
                ? Util.range(text.length() - width).toArray()
                : Util.range(0, text.length() - width + offset, offset).toArray();
    }
    
    public static ArrayRIV[] rivShingles (String text, int[] shinglePoints, int width, int size, int k) {
        return Arrays.stream(shinglePoints)
                    .mapToObj((point) -> 
                        ArrayRIV.generateLabel(
                                size,
                                k,
                                text,
                                point,
                                width))
                    .toArray(ArrayRIV[]::new);
    }
    
    public static ArrayRIV rivAndSumShingles (String text, int[] shinglePoints, int width, int size, int k) {
        return Arrays.stream(shinglePoints)
            .boxed()
            .reduce(new ArrayRIV(size),
                    (riv, point) -> riv.add(
                            ArrayRIV.generateLabel(
                                    size,
                                    k,
                                    text,
                                    point,
                                    width)),
                    (rivA, rivB) -> rivA.add(rivB));
    }
    
    public static ArrayRIV sumRIVs (ArrayRIV[] rivs) { 
        return Arrays.stream(rivs).reduce(
                new ArrayRIV(rivs[0].size()),
                (i, r) -> i.add(r));
    }
    
    public static ArrayRIV rivettizeText(String text, int width, int offset, int size, int k) throws ShingleInfection {
        int[] points = findShinglePoints(text, offset, width);
        return rivAndSumShingles(text, points, width, size, k);
    }
}