package rivet.core.extras;

import java.util.Arrays;

import rivet.core.exceptions.ShingleInfection;
import rivet.core.labels.MapRIV;
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

    public static MapRIV rivAndSumShingles(final String text,
            final int[] shinglePoints, final int width, final int size,
            final int k) {
        return Arrays.stream(shinglePoints).boxed().reduce(new MapRIV(size),
                (riv, point) -> riv
                        .add(MapRIV.generateLabel(size, k, text, point, width)),
                (rivA, rivB) -> rivA.add(rivB));
    }

    public static MapRIV rivettizeText(final String text, final int width,
            final int offset, final int size, final int k)
            throws ShingleInfection {
        final int[] points = findShinglePoints(text, offset, width);
        return rivAndSumShingles(text, points, width, size, k);
    }

    public static MapRIV[] rivShingles(final String text,
            final int[] shinglePoints, final int width, final int size,
            final int k) {
        return Arrays.stream(shinglePoints).mapToObj(
                (point) -> MapRIV.generateLabel(size, k, text, point, width))
                .toArray(MapRIV[]::new);
    }

    public static MapRIV sumRIVs(final MapRIV[] rivs) {
        return Arrays.stream(rivs).reduce(new MapRIV(rivs[0].size()),
                (i, r) -> i.add(r));
    }

    private Shingles() {
    }
}