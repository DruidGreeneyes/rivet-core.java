package rivet.core.extras;

import java.util.Arrays;

import rivet.core.exceptions.ShingleInfection;
import rivet.core.labels.ArrayRIV;
import rivet.core.util.Util;

public final class Shingles {
    public static int[] findShinglePoints(final String text, final int offset,
            final int width) throws ShingleInfection {
        if (text == null || text.isEmpty())
            throw new ShingleInfection("THIS TEXT IS NOT TEXT!");
        if (offset < 1)
            throw new ShingleInfection(
                    "THIS OFFSET IS A VIOLATION OF THE TOS! PREPARE FOR LEGAL ACTION!");
        return offset == 1 ? Util.range(text.length() - width).toArray()
                : Util.range(0, text.length() - width + offset, offset)
                        .toArray();
    }

    public static ArrayRIV rivAndSumShingles(final String text,
            final int[] shinglePoints, final int width, final int size,
            final int k) {
        return Arrays.stream(shinglePoints).boxed().reduce(new ArrayRIV(size),
                (riv, point) -> riv.add(
                        ArrayRIV.generateLabel(size, k, text, point, width)),
                (rivA, rivB) -> rivA.add(rivB));
    }

    public static ArrayRIV rivettizeText(final String text, final int width,
            final int offset, final int size, final int k)
            throws ShingleInfection {
        final int[] points = findShinglePoints(text, offset, width);
        return rivAndSumShingles(text, points, width, size, k);
    }

    public static ArrayRIV[] rivShingles(final String text,
            final int[] shinglePoints, final int width, final int size,
            final int k) {
        return Arrays.stream(shinglePoints).mapToObj(
                (point) -> ArrayRIV.generateLabel(size, k, text, point, width))
                .toArray(ArrayRIV[]::new);
    }

    public static ArrayRIV sumRIVs(final ArrayRIV[] rivs) {
        return Arrays.stream(rivs).reduce(new ArrayRIV(rivs[0].size()),
                (i, r) -> i.add(r));
    }

    private Shingles() {
    }
}