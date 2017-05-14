package rivet.core.util.hilbert;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

import rivet.core.extras.UntrainedWordsMap;
import rivet.core.labels.ImmutableRIV;

public class HilbertTests {

  public static class RIVComparison implements Comparable<RIVComparison> {
    public BigInteger hilDist;
    public double cosim;
    public ImmutableRIV rivA;
    public ImmutableRIV rivB;

    public RIVComparison(final ImmutableRIV a, final ImmutableRIV b,
                         final BigInteger dist) {
      rivA = a;
      rivB = b;
      cosim = a.similarityTo(b);
      hilDist = dist;
    }

    @Override
    public int compareTo(final RIVComparison other) {
      return hilDist.compareTo(other.hilDist);
    }
  }

  private static Path TEST_DATA = Paths.get("resources/test/hilbert/data");
  private static int size = 8000;
  private static int nnz = 4;

  public static void
         baseTest(final Function<ImmutableRIV, BigInteger> getKey) throws IOException {
    final Comparator<ImmutableRIV> comp = (a, b) -> getKey.apply(a)
                                                          .compareTo(getKey.apply(b));
    final TreeMap<ImmutableRIV, Path> rivs = new TreeMap<>(comp);
    final TreeSet<RIVComparison> comparisons = new TreeSet<>();
    Files.walk(TEST_DATA, 1)
         .filter(p -> !Files.isDirectory(p) && fileSize(p, 50))
         .map(p -> {
           Optional<ImmutablePair<ImmutableRIV, Path>> o = Optional.empty();
           try {
             o = Optional.of(ImmutablePair.of(rivText(p), p));
           } catch (final IOException e) {}
           return o;
         })
         .filter(Optional::isPresent)
         .map(Optional::get)
         .forEach(p -> rivs.put(p.getLeft(), p.getRight()));

    rivs.keySet().forEach(riv -> {
      final ImmutableRIV nextRIV = rivs.higherKey(riv);
      if (null != nextRIV)
        comparisons.add(new RIVComparison(riv, nextRIV,
                                          getKey.apply(nextRIV)
                                                .subtract(getKey.apply(riv))
                                                .abs()));
    });

    final RIVComparison[] top10 = comparisons.stream()
                                             .limit(10)
                                             .toArray(RIVComparison[]::new);
    System.out.println("Best 10 comparisons by curve distance:");
    final double topAvg = Arrays.stream(top10)
                                .peek(c -> System.out.println(compToString(c,
                                                                           rivs)))
                                .mapToDouble(c -> c.cosim)
                                .sum()
                          / 10;
    final RIVComparison[] bot10 = comparisons.descendingSet()
                                             .stream()
                                             .limit(10)
                                             .toArray(RIVComparison[]::new);
    System.out.println("Worst 10 comparisons by curve distance:");
    final double botAvg = Arrays.stream(bot10)
                                .peek(c -> System.out.println(compToString(c,
                                                                           rivs)))
                                .mapToDouble(c -> c.cosim)
                                .sum()
                          / 10;
    System.out.println("Best 10 average cosim: " + topAvg);
    System.out.println("Worst 10 average cosim: " + botAvg);
    assertEquals(1, Double.compare(topAvg, botAvg));
  }

  private static String bigStr(final BigInteger num, final int figs) {
    final String s = num.toString();
    return String.format("%se%d", s.substring(0, figs),
                         s.substring(figs).length());
  }

  public static String compToString(final RIVComparison comp,
                                    final TreeMap<ImmutableRIV, Path> rivs) {
    return String.format("    File A: %s%n    File B: %s%n    Curve Distance: %s%n    Cosim: %.4f%n%n",
                         rivs.get(comp.rivA).getFileName().toString(),
                         rivs.get(comp.rivB).getFileName().toString(),
                         bigStr(comp.hilDist, 4),
                         comp.cosim);
  }

  public static boolean fileSize(final Path path, final long testSize) {
    try {
      final long size = (long) Files.getAttribute(path, "basic:size");
      return size > 50;
    } catch (final IOException e) {
      return false;
    }
  }

  public static ImmutableRIV rivText(final Path p) throws IOException {
    final String[] words = Files.lines(p)
                                .flatMap(line -> Arrays.stream(line.split("\\W+")))
                                .toArray(String[]::new);
    return UntrainedWordsMap.rivAndSumWords(words, size, nnz).toImmutable();
  }

  @Test
  public void testEncodeHilbillyKey() throws IOException {
    System.out.println("Testing Hilbilly Key:");
    baseTest(ImmutableRIV::getHilbillyKey);
  }

  @Test
  public void testFEncodeHilbertKey() throws IOException {
    System.out.println("Testing Hilbert Key:");
    baseTest(ImmutableRIV::getHilbertKey);
  }

  /*
   * @Test public void testSEncodeHilbertKey() throws IOException {
   * System.out.println("Testing SHilbert Key:");
   * baseTest(ImmutableRIV::getSHilbertKey); }
   */

  @Test
  public void testSEncodeHilbillyKey() throws IOException {
    System.out.println("Testing SHilbilly Key");
    baseTest(ImmutableRIV::getSHilbillyKey);
  }

}
