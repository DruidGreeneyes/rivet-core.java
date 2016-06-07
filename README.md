#Rivet.java

Random-Index Vectoring in Java. Written in the voice of Mister Torgue.

##WHAT THE F$%K IS THIS!?!?

Random-Index Vectoring is a memory efficient way to perform textual analysis across corpora of extreme scale. It enables this by using contextual word co-occurrence as a stand-in for word meaning.

##WHAT THE HELL DOES THAT MEAN!?!?

Go read about it:

http://www.ercim.eu/publication/Ercim_News/enw50/sahlgren.html

If that doesn't give you enough of a primer, do the google. Sahlgren has done a lot of work on this and is a reliable source. So is Paradis.

##SOUNDS LIKE A JOYSPLOSION!! HOW CAN I GET IN ON THIS S&@T!?!?

I created a release, so you can use jitpack.io if you like, but I make no guarantees for how well that will keep up with changes to the master. If you want to be able to recieve updates as soon as I make them, the easiest way is probably just to clone me.

The core functionality is in `rivet.core.labels`, where you'll use either `ArrayRIV` or `MapRIV`. these are two different implementations of the same thing, so pick one and roll with it. I *believe* that `ArrayRIV` will be faster, because it's backed by a more primitive datatype and has added only the functionality needed to work conveniently with RIVs, but I haven't tested that hypothesis, so YMMV. Pick whichever you like.

If you want some examples for what to do with it, or if you want some basic use without having to roll your own, you can check out `rivet.core.extras`. Currently there's an implementation of text shingling and one of untrained word-by-word analysis, but the real power in RIVs is going to be in cluster- and database-backed big-data applications, and for that you'll probably want to just use `labels` as your base library and build into Spark or MapReduce or Tez or something like that. You can see how it works in Spark by looking at my [rivet-cluster.java](https://github.com/DruidGreeneyes/rivet-cluster.java) repo.

If you have any questions, let me know. If you get bugs, leave an issue; include the full stacktrace and the relevant code, or I probably won't even look. Or if you know me, just hit me up on skype or google-chat and I'll see what I can do.
