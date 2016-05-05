package rivet.extras.text.lda;

import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import static java.util.Arrays.stream;

import java.util.ArrayList;
import java.util.Arrays;

import rivet.core.arraylabels.Labels;
import rivet.core.arraylabels.RIV;
import rivet.core.util.Counter;
import rivet.core.util.Util;
import rivet.extras.text.WordLexicon;


public final class LDA {
	private LDA(){}
	
	public static HashMap<String, ProbabilisticWordBag> gibbsSample (String[][] texts, int numTopics, double changeThreshold) {
		int[] topicNums = Util.range(numTopics).toArray();
		ProbabilisticWordBag[] topicBags = 
				Arrays.stream(topicNums)
				.mapToObj((x) -> new ProbabilisticWordBag())
				.toArray(ProbabilisticWordBag[]::new);
		ArrayList<WDTEntry> topicEntries = new ArrayList<>();
		
		Random r = new Random();
		for (int c = 0; c < texts.length; c++) {
			String[] text = texts[c];
			for (String token : text) {
				int topic = r.nextInt(numTopics);
				topicEntries.add(WDTEntry.make(token, c, topic));
				topicBags[topic].add(token);
			}
		}
		
		double numWords = topicEntries.size();
		
		int changes;
		do {
			changes = 0;
			for (WDTEntry entry : topicEntries) {
				double[] probs = stream(topicNums)
								.mapToDouble((i) -> {
									double pTopicGivenDocument = topicEntries.stream().filter((e) -> 
										e.document == entry.document && 
										e.topic == topicNums[i]).count() / (double)texts[entry.document].length;
									double pWordGivenTopic = topicBags[i].probability(entry.word);
									return pTopicGivenDocument * pWordGivenTopic;
								})
								.toArray();
				
				double factor = stream(probs).sum();
				double[] topicProbs = stream(probs).map((p) -> p / factor).toArray();
				
				int newTopic = new EnumeratedIntegerDistribution(topicNums, topicProbs).sample();
				if (newTopic != entry.topic) {
					changes++;
					topicBags[entry.topic].subtract(entry.word);
					topicBags[newTopic].add(entry.word);
				}
			}
		} while (changes/numWords > changeThreshold);
		
		HashMap<String, ProbabilisticWordBag> topics = new HashMap<>();
		Arrays.stream(topicBags).forEach((bag) -> topics.put(bag.name(), bag));
		
		return topics;
	}
	
	public static RIV[] rivTopics (String[][] texts, int numTopics, double changeThreshold, WordLexicon lexicon) {
		int[] topicNums = Util.range(numTopics).toArray();
		RIV[] topicRIVs = new RIV[numTopics];
		ArrayList<RTEntry> topicEntries = new ArrayList<RTEntry>();
		
		Random r = new Random();
		for (String[] text : texts)
			for (String token : text) {
				RIV riv = lexicon.get(token);
				int topic = r.nextInt(numTopics);
				topicRIVs[topic].add(riv);
				topicEntries.add(RTEntry.make(riv, topic));
			}
		
		
		double numWords = topicEntries.size();
		
		Counter changes = new Counter();
		do {
			changes.zero();
			ArrayList<RTEntry> newEntries =
					topicEntries.stream()
						.map((entry) -> {
							double[] sims = 
									stream(topicNums)
									.mapToDouble((i) -> Labels.similarity(entry.riv, topicRIVs[i]))
									.toArray();
							int newTopic = 0;
							for (int i : topicNums)
								if (sims[i] > sims[newTopic])
									newTopic = i;
							
							if (newTopic != entry.topic) {
								changes.inc();
								topicRIVs[entry.topic].subtract(entry.riv);
								topicRIVs[newTopic].add(entry.riv);
								return RTEntry.make(entry.riv, newTopic);
							} else
								return entry;
						})
						.collect(Collectors.toCollection(ArrayList::new));
			topicEntries = newEntries;
		} while (changes.get() / numWords > changeThreshold);
		
		return topicRIVs;
	}
}
