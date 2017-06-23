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

If you want to peg to a particular release, you can get it from Maven:

```

<dependency>
  <groupId>com.github.druidgreeneyes</groupId>
  <artifactId>rivet-core</artifactId>
  <version>1.0.0</version>
</dependency>

```

You can also get it from jitpack.io, if you prefer:


```

<repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
</repository>

...

<dependency>
  <groupId>com.github.druidgreeneyes</groupId>
  <artifactId>rivet-core.java</artifactId>
  <version>v1.0.0</version>
</dependency>

```

And if you want to pull whatever's currently in development, you can do that too (through jitpack), though it's guaranteed to break sometimes.

```

<dependency>
  <groupId>com.github.druidgreeneyes</groupId>
  <artifactId>rivet-core.java</artifactId>
  <version>-SNAPSHOT</version>
</dependency>

```

The core functionality is in `rivet.core.labels`, where you'll probably use `MapRIV`. There are a number of other implementations of the RIV interface, but MapRIV has so far proven to be the fastest by a pretty significant margin, so you may as well start there. If you see flaws in that or in one of the other implementations, and/or you know how they can be done better, feel free to create an issue with your suggestion, or a pr with a fix. 

If you want some examples for what to do with it, or if you want some basic use without having to roll your own, you can check out `rivet.core.extras`. Currently there's an implementation of text shingling and one of untrained word-by-word analysis, but the real power in RIVs is going to be in cluster- and database-backed big-data applications, and for that you'll probably want to just use `labels` as your base library and build into Spark or MapReduce or Tez or something like that. You can see how it works in Spark by looking at my [rivet-cluster.java](https://github.com/DruidGreeneyes/rivet-cluster.java) repo.

If you have any questions, let me know. If you get bugs, leave an issue; include the full stacktrace and the relevant code, or I probably won't even look. Or if you know me, just hit me up on skype or google-chat and I'll see what I can do.

## BABY STEPS, MOTHERF%$@#ER!!!!!

Random Index Vectors give you a conveniently scalable way to mathematically represent blocks of text. The easiest way to do this is going to look something like the following:

```java

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
        if (!Util.doubleEquals(sims[r][c], 1.0)) 
          for (int x = 0; x < 10; x++)
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

```

## BUT WHAT ABOUT THE GODDAMNED WIZARDRY!?!?!?

There are a number of other things you can do, and other ways you can use RIVs to represent text. For example, you can build a lexicon (a map of words to the corpus-wide L2 RIV associated with each word, in turn built over time by adding together all words that occur within a 2- to 4-word window of the word in question), and use it to (say) find synonyms, or label documents with topics associated with words that have good similarity with the document in question. If you want to get wacky with it (and you don't mind the high compute cost of doing so), you can encode ordering to words within sentences or context windows by using RIV.permute(n), where n is the given word's location in the context relative to the word it's being added to. Among the things I am working on or have worked on include clustering documents based on cosine similarity, finding and eliminating near-duplicates across corpora, and inferring flat and heirarchical topic models from data. I stood up a small website using a Python version of this library, and was able to do similarity comparisons between 100k word texts in 2 and some change minutes on a 2nd generation raspberry pi. It would probably be faster if I had done it in Java, but I wanted to muck about with Python, so I did. I most recently implemented Hilbert Transformations on RIVs, so for big corpora you should be able to sort by hilbert key (using Hilbert.getHilbertKey(riv), or ImmutableRIV.getHilbertKey()) and use that for some nifty tricks.

## CHOICES, CHOICES, SO MANY F#%ING CHOICES!!!

The first couple things you'll have to figure out, mostly through trial and error, will be what size and what nnz grant you the best balance between how accurate your RIVs are and how many your hardware can process within a suitable span of time. In general, 8000/4 seems to be an acceptable starting point; I know people who are using 16k/24, and I have heard of people using sizes as big as 100k. This will mostly depend on the size of your corpus and the power of your hardware. Beyond that, there are a number of decisions you can take to customize your RIV experience, so to speak. Permutations will allow you to (in theory) pack more real information into a RIV, but it has a sizable cost in terms of ram and cpu usage. If you build a lexicon, then you'll have to decide what will qualify as the surrounding context for your words; I have used windows of 2 and 3, and I have used the entire sentence, on the theory that every piece I leave out is lost information that could theoretically inflect the meaning of the word. I have found that within reasonably-sized rivs (8k to 16k wide), using the whole sentence seems to propagate too much noise into the data, and makes even utterly unrelated things have cosine similarities of 0.6 or greater.

The importance of cleaning your input text cannot be overstated; I ran this on a collection of raw emails and the results were utter garbage, and no matter what tricks I tried when processing the RIVs, I didn't really get better results until I got cleaner texts to work with. I'm have mixed feelings about stopwording and lemmatizing. In theory, they reduce scale and noise from the input data, but ultimately some of the original information is lost in the process. In my ideal world, I would handle raw text and extract meaning from that, because theoretically that is the most accurate way to do this. But, stopwords especially add an incredible amount of noise, and so they kind of collide with everything, and as a result they tend to muddy up all the results. You can mitigate this somewhat by taking the mean vector of a lexicon or of a corpus (that is, the sum of all constituent RIVs divided by the number of constituent RIVs) and subtracting that from all constituents, but in practice this has not proven to be as helpful as I had originally hoped.

Finally, be aware of the distinction between operations and their destructive counterparts. The destructive version will always be faster, but it is faster because it operates in place on the calling RIV, and the previous state of that RIV is lost. So, for example, if you want to find out if rivA is equal to rivB times 2 (I dunno why you would, but for the sake of example...), use `rivA.equals(rivB.multiply(2))` and **NOT** `rivA.equals(rivB.destructiveMult(2))`. Doing it the second way will permanently alter rivB, and throw all your further processing out of whack. If you're adding a bunch of RIVs together (and you will, because that's the whole point), the best compromise between speed and safety will be to do something like I did above: 

```java
RIV[] rivs;
RIV res = MapRIV.empty();
for(riv : rivs)
  res.destructiveAdd(riv);
```

or, if your operation is big enough that it's worth using Java streams, you can just do a reduce operation:

```java
Stream<RIV> rivs;
rivs.reduce(MapRIV.empty(); RIV::destructiveAdd);
```

either way, you're starting with an empty and making destructive modifications to that instead of to something else you might want to use later.