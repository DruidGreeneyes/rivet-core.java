# Rivet.java

[![Build Status](https://travis-ci.org/DruidGreeneyes/rivet-core.java.svg?branch=master)](https://travis-ci.org/DruidGreeneyes/rivet-core.java)

Random-Index Vectoring in Java. Written in the voice of Mister Torgue.

## WHAT THE F$%K IS THIS!?!?

Random-Index Vectoring is a memory efficient way to perform textual analysis across corpora of extreme scale. It enables this by using contextual word co-occurrence as a stand-in for word meaning.

## WHAT THE HELL DOES THAT MEAN!?!?

Go read about it:

http://www.ercim.eu/publication/Ercim_News/enw50/sahlgren.html

If that doesn't give you enough of a primer, do the google. Sahlgren has done a lot of work on this and is a reliable source. So is Paradis.

## SOUNDS LIKE A JOYSPLOSION!! HOW CAN I GET IN ON THIS S&@T!?!?

I'm working to get on maven, but in the mean time you can use jitpack.io if you like.

The core functionality is in `rivet.core.labels`, where you'll probably use `MapRIV`. There are a number of other implementations of the RIV interface, but MapRIV has so far proven to be the fastest by a pretty significant margin, so you may as well start there. If you see flaws in that or in one of the other implementations, and/or you know how they can be done better, feel free to create an issue with your suggestion, or a pr with a fix. 

If you want some examples for what to do with it, or if you want some basic use without having to roll your own, you can check out `rivet.core.extras`. Currently there's an implementation of text shingling and one of untrained word-by-word analysis, but the real power in RIVs is going to be in cluster- and database-backed big-data applications, and for that you'll probably want to just use `labels` as your base library and build into Spark or MapReduce or Tez or something like that. You can see how it works in Spark by looking at my [rivet-cluster.java](https://github.com/DruidGreeneyes/rivet-cluster.java) repo.

If you have any questions, let me know. If you get bugs, leave an issue; include the full stacktrace and the relevant code, or I probably won't even look. Or if you know me, just hit me up on skype or google-chat and I'll see what I can do.

## BABY STEPS, MOTHERF%$@#ER!!!!!

Random Index Vectors give you a conveniently scalable way to mathematically represent blocks of text. The easiest way to do this is going to look something like the following:

```java

final String[] documents = //import your documents here.

final int size = 8000;     //this is the width of the sparse vectors we'll be making
final int nnz = 4;         //this is the number of non-zero elements you want each one to start with

RIV[] rivs = new RIV[documents.length];
int fill = 0;
for(String text : document) {
  RIV riv = MapRIV.empty();
  for(String word : text.split("\\W+"))
  	riv.destructiveAdd(MapRIV.generateLabel(size, nnz, word));
  rivs[fill++] = riv;
}

/**
 * Now we have a collection of rivs, each of which represents a document 
 * at the same index in the original collection of texts.
 * 
 * Using this, we can go through the list and see which are most similar to eachother.
 **/

double[][] sims = new double[rivs.length][rivs.length];

fill = 0;
int fill2 = 0;
for(RIV rivA : rivs) {
  fill2 = 0;
  for(RIV rivB : rivs)
    sims[fill++][fill2++] = rivA.similarityTo(rivB);
}

/**
 * We technically don't need a square matrix for this, because a.similarityTo(b) == b.similarityTo(a) always.
 * But f#$k it, you can deal. Anyway, now we can go through the matrix and find the (say) 10 pairs 
 * of documents that are most similar to one another without being identical (a.similarityTo(b) != 1)
 **/
 
double[][] pairs = new double[10][3]; //this is a collection of [index, index, similarity] triples
for (int r = 0; i < sims.length; i++)
  for (int c = 0; n < row.length; n++)
    if(!Util.doubleEquals(sims[r][c], 1.0)) {
      for (int x = 0; x < 10; x++)
        if((pairs[x][2] == null) || (pairs[x][2] < sims[r][c])) {
          pairs[x][0] = r;
          pairs[x][1] = c;
          pairs[x][3] = sims[r][c];
        }
    }
    
System.out.println("Top 10 most similar documents:");
for(double[] pair : pairs) {
  String a = documents[(int)pair[0]].substring(0, 20) + "...";
  String b = documents[(int)pair[1]].substring(0, 20) + "...";
  System.out.println(a + " <=> " + b + ": " + pair[2]);
}