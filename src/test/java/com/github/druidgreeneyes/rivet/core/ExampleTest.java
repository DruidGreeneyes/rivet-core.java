package com.github.druidgreeneyes.rivet.core;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import com.github.druidgreeneyes.rivet.core.labels.MapRIV;
import com.github.druidgreeneyes.rivet.core.labels.RIV;
import com.github.druidgreeneyes.rivet.core.util.Util;

public class ExampleTest {

  final Path path = Paths.get("resources/test/hilbert/data");
  String[] DOCUMENTS = getDocuments(path);

  public String[] getDocuments(final Path path) {
    try {
      return Files.walk(path, 1)
                  .filter(p -> !Files.isDirectory(p) && fileSize(p, 50))
                  .map(p -> {
                    try {
                      return Optional.of(Files.lines(p)
                                              .collect(Collectors.joining(" ")));
                    } catch (final IOException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                      return Optional.empty();
                    }
                  })
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .toArray(String[]::new);
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  public static boolean fileSize(final Path path, final long testSize) {
    try {
      final long size = (long) Files.getAttribute(path, "basic:size");
      return size > 50;
    } catch (final IOException e) {
      return false;
    }
  }

  @Test
  public void example() {

    final String[] documents = DOCUMENTS;// import your documents here.

    final int size = 8000; // this is the width of the sparse vectors we'll be
                           // making
    final int nnz = 4; // this is the number of non-zero elements you want each
                       // one to start with

    final RIV[] rivs = new RIV[documents.length];
    int fill = 0;

    for (final String text : documents) {
      final RIV riv = MapRIV.empty(size);
      for (final String word : text.split("\\W+"))
        riv.destructiveAdd(MapRIV.generateLabel(size, nnz, word));
      rivs[fill++] = riv;
    }

    /**
     * Now we have a collection of rivs, each of which represents a document at
     * the same index in the original collection of texts.
     *
     * Using this, we can go through the list and see which are most similar to
     * eachother.
     **/

    final double[][] sims = new double[rivs.length][rivs.length];

    fill = 0;
    int fill2 = 0;
    for (final RIV rivA : rivs) {
      fill2 = 0;
      for (final RIV rivB : rivs)
        sims[fill][fill2++] = rivA.similarityTo(rivB);
      fill++;
    }

    /**
     * We technically don't need a square matrix for this, because
     * a.similarityTo(b) == b.similarityTo(a) always. But f#$k it, you can deal.
     * Anyway, now we can go through the matrix and find the (say) 10 pairs of
     * documents that are most similar to one another without being identical
     * (a.similarityTo(b) != 1)
     **/

    final double[][] pairs = new double[10][3]; // this is a collection of
                                                // [index, index, similarity]
                                                // triples
    for (int r = 0; r < sims.length; r++)
      for (int c = 0; c < sims[r].length; c++)
        if (!Util.doubleEquals(sims[r][c], 1.0)) for (int x = 0; x < 10; x++)
          if (pairs[x][2] == 0.0 || pairs[x][2] < sims[r][c]) {
            pairs[x][0] = r;
            pairs[x][1] = c;
            pairs[x][2] = sims[r][c];
            break;
          }

    System.out.println("Top 10 most similar documents:");
    for (final double[] pair : pairs) {
      final String a = documents[(int) pair[0]].substring(0, 20) + "...";
      final String b = documents[(int) pair[1]].substring(0, 20) + "...";
      System.out.println(a + " <=> " + b + ": " + pair[2]);
    }
  }
}
