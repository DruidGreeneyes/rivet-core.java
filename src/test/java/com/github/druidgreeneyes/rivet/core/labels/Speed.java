package com.github.druidgreeneyes.rivet.core.labels;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import com.github.druidgreeneyes.rivet.core.util.Util;

public class Speed {

  private static final Path path = Paths.get("resources/test/hilbert/data");
  private static final String[] DOCUMENTS = getDocuments(path);
  private static final double iterations = 100;

  private static Class<?>[] classes = new Class<?>[] {
      ArrayRIV.class,
      ColtRIV.class,
      // HPPCRIV.class,
      KoloRIV.class,
      MapRIV.class,
      MTJRIV.class
  };

  @Test
  public void test() {
    IntStream.iterate(0, i -> (i + 1) % classes.length)
             .limit((long) iterations)
             .parallel()
             .mapToObj(i -> classes[i])
             .map(c -> {
               final double start = System.nanoTime();
               example(c);
               return ImmutablePair.of(c, (System.nanoTime() - start)
                                          / 1000000000);
             })
             .collect(Collectors.groupingByConcurrent(p -> p.left,
                                                      Collectors.averagingDouble(p -> p.right)))
             .entrySet()
             .stream()
             .sorted((a, b) -> Double.compare(a.getValue(), b.getValue()))
             .forEachOrdered(e -> System.out.format("%s:\t%fs\n", e.getKey(),
                                                    e.getValue()));
  }

  private void example(final Class<?> rivClass) {

    final String[] documents = DOCUMENTS;
    final int size = 8000;
    final int nnz = 4;

    final RIV[] rivs = new RIV[documents.length];
    int fill = 0;

    for (final String text : documents) {
      final RIV riv = invokeEmptyConstructor(rivClass, size);
      final Function<CharSequence, RIV> rivGenerator = RIVs.generator(size,
                                                                      nnz,
                                                                      getDefaultConstructor(rivClass));
      for (final String word : text.split("\\W+"))
        riv.destructiveAdd(rivGenerator.apply(word));
      rivs[fill++] = riv;
    }

    final double[][] sims = new double[rivs.length][rivs.length];
    for (int c = 0; c < rivs.length; c++)
      for (int i = c + 1; i < rivs.length; i++)
        sims[c][i] = rivs[c].similarityTo(rivs[i]);

    final double[][] pairs = new double[10][3];

    for (int r = 0; r < sims.length; r++)
      for (int c = 0; c < sims[r].length; c++)
        if (!Util.doubleEquals(sims[r][c], 1.0)) for (int x = 0; x < 10; x++)
          if (pairs[x][2] == 0.0 || pairs[x][2] < sims[r][c]) {
            pairs[x][0] = r;
            pairs[x][1] = c;
            pairs[x][2] = sims[r][c];
            break;
          }

    try (PrintStream out = new PrintStream("/dev/null")) {
      out.println("Top 10 most similar documents:");
      for (final double[] pair : pairs) {
        final String a = documents[(int) pair[0]].substring(0, 20) + "...";
        final String b = documents[(int) pair[1]].substring(0, 20) + "...";
        out.println(a + " <=> " + b + ": " + pair[2]);
      }
    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static String[] getDocuments(final Path path) {
    try {
      return Files.walk(path, 1)
                  .filter(p -> !Files.isDirectory(p) && fileSize(p, 50))
                  .map(p -> {
                    try {
                      return Optional.of(Files.lines(p)
                                              .collect(Collectors.joining(" ")));
                    } catch (final IOException e) {
                      e.printStackTrace();
                      return Optional.empty();
                    }
                  })
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .toArray(String[]::new);
    } catch (final IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static boolean fileSize(final Path path, final long testSize) {
    try {
      final long size = (long) Files.getAttribute(path, "basic:size");
      return size > 50;
    } catch (final IOException e) {
      return false;
    }
  }

  private static RIVConstructor getDefaultConstructor(final Class<?> rivClass) {
    Object res = null;
    try {
      res = rivClass.getMethod("getConstructor").invoke(null);
    } catch (final NoSuchMethodException e) {
      fail("Class " + rivClass.getName()
           + " provides no constructor that accepts these args: "
           + int[].class.toString()
           + ", "
           + double[].class.toString()
           + ", "
           + int.class.toString());
    } catch (IllegalArgumentException | SecurityException e) {
      fail(e.getStackTrace().toString());
    } catch (final IllegalAccessException e) {
      e.printStackTrace();
    } catch (final InvocationTargetException e) {
      e.printStackTrace();
    }
    return (RIVConstructor) res;
  }

  private static RIV invokeEmptyConstructor(final Class<?> rivClass,
                                            final int size) {
    Object res = null;
    Constructor<?> cst = null;
    try {
      cst = rivClass.getConstructor(int.class);
    } catch (final NoSuchMethodException e) {
      fail("Class " + rivClass.getName()
           + " provides no constructor that accepts these args: "
           + int.class.toString());
    } catch (IllegalArgumentException | SecurityException e) {
      fail(e.getStackTrace().toString());
    }
    if (cst == null)
      fail("No constructor found!");
    try {
      res = cst.newInstance(size);
    } catch (InstantiationException
             | IllegalAccessException
             | InvocationTargetException e) {
      fail("Constructor found but not invoked!\n"
           + e.getStackTrace().toString());
    }
    return (RIV) res;
  }
}
