package rivet.core.util.hilbert;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
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
    final HashMap<ImmutableRIV, Path> paths = new HashMap<>();
    final TreeMap<BigInteger, ImmutableRIV> rivs = new TreeMap<>();
    final TreeSet<RIVComparison> comparisons = new TreeSet<>();
    Files.walk(TEST_DATA, 1)
         .filter(p -> !Files.isDirectory(p) && fileSize(p, 50))
         .map(path -> {
           final Optional<ImmutableRIV> o = rivText(path);
           final ImmutableRIV riv = o.orElseThrow(() -> new RuntimeException("IO FAILURE at path "
                                                                             + path.toString()));
           paths.put(riv, path);
           return riv;
         })
         .forEach(riv -> rivs.put(getKey.apply(riv), riv));

    final BigInteger lastKey = rivs.lastKey();
    rivs.keySet().forEach(k -> {
      if (k == lastKey) return;
      final BigInteger nextK = rivs.higherKey(k);
      final ImmutableRIV nextRIV = rivs.get(nextK);
      comparisons.add(new RIVComparison(rivs.get(k), nextRIV,
                                        nextK
                                             .subtract(k)
                                             .abs()));
    });

    final RIVComparison[] top10 = comparisons.stream()
                                             .limit(10)
                                             .toArray(RIVComparison[]::new);
    System.out.println("Best 10 comparisons by curve distance:");
    final double topAvg = Arrays.stream(top10)
                                .peek(c -> System.out.println(compToString(c,
                                                                           paths)))
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
                                                                           paths)))
                                .mapToDouble(c -> c.cosim)
                                .sum()
                          / 10;
    System.out.println("Best 10 average cosim: " + topAvg);
    System.out.println("Worst 10 average cosim: " + botAvg);
    System.out.println("Average Key Distance: " + bigStr(comparisons.stream()
                                                                    .map(c -> c.hilDist)
                                                                    .reduce(BigInteger.ZERO,
                                                                            BigInteger::add)
                                                                    .divide(BigInteger.valueOf(comparisons.size())),
                                                         4));
    System.out.println("Median Key Distance: " + bigStr(comparisons.stream()
                                                                   .map(c -> c.hilDist)
                                                                   .reduce(BigInteger.ZERO,
                                                                           BigInteger::add)
                                                                   .divide(BigInteger.valueOf(comparisons.size())),
                                                        4));
    assertEquals(1, Double.compare(topAvg, botAvg));
  }

  private static String bigStr(final BigInteger num, final int figs) {
    final String s = num.toString();
    return String.format("%se%d", s.substring(0, figs),
                         s.substring(figs).length());
  }

  public static String compToString(final RIVComparison comp,
                                    final HashMap<ImmutableRIV, Path> paths) {
    return String.format("    File A: %s%n    File B: %s%n    Curve Distance: %s%n    Cosim: %.4f%n%n",
                         paths.get(comp.rivA)
                              .getFileName()
                              .toString(),
                         paths.get(comp.rivB)
                              .getFileName()
                              .toString(),
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

  public static Optional<ImmutableRIV> rivText(final Path p) {
    try {
      final String[] words = Files.lines(p)
                                  .flatMap(line -> Arrays.stream(line.split("\\W+")))
                                  .toArray(String[]::new);
      return Optional.of(UntrainedWordsMap.rivAndSumWords(words, size, nnz)
                                          .toImmutable());
    } catch (final IOException e) {
      return Optional.empty();
    }
  }

  @Test
  public void testEncodeHilbillyKey() throws IOException {
    System.out.println("Testing Hilbilly Key:");
    baseTest(ImmutableRIV::getHilbillyKey);
  }

  @Test
  public void testEncodeHilbertKey() throws IOException {
    System.out.println("Testing Hilbert Key:");
    baseTest(ImmutableRIV::getHilbertKey);
  }

  // @Test
  public void testFEncodeHilbertKey() throws IOException {
    System.out.println("Testing FHilbert Key:");
    baseTest(ImmutableRIV::getFHilbertKey);
  }

  // @Test
  public void testSEncodeHilbertKey() throws IOException {
    System.out.println("Testing SHilbert Key:");
    baseTest(ImmutableRIV::getSHilbertKey);
  }

  @Test
  public void testSEncodeHilbillyKey() throws IOException {
    System.out.println("Testing SHilbilly Key");
    baseTest(ImmutableRIV::getSHilbillyKey);
  }

}
