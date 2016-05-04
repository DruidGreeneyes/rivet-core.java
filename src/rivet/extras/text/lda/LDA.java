package rivet.extras.text.lda;

import java.util.HashMap;
import java.util.Random;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import static java.util.Arrays.stream;

import java.util.ArrayList;
import java.util.Arrays;

import rivet.core.util.Util;


public final class LDA {
	private LDA(){}
	
	public static HashMap<String, ProbabilisticWordBag> gibbsSample (String[][] texts, int numTopics, int changeThreshold) {
		int[] topicNums = Util.range(numTopics).toArray();
		ProbabilisticWordBag[] topicBags = Arrays.stream(topicNums).mapToObj((x) -> new ProbabilisticWordBag()).toArray(ProbabilisticWordBag[]::new);
		ArrayList<WDTEntry> topicEntries = new ArrayList<>();
		
		Random r = new Random();
		for (int c = 0; c < texts.length; c++) {
			String[] text = texts[c];
			for (int i = 0; i < text.length; i++) {
				String word = text[i];
				int topic = r.nextInt(numTopics);
				topicEntries.add(WDTEntry.make(word, c, topic));
				topicBags[topic].add(word);
			}
		}
		
		int numWords = topicEntries.size();
		
		int changes;
		do {
			changes = 0;
			for (WDTEntry entry : topicEntries) {
				double[] probs = Arrays.stream(topicNums)
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
		} while (changes/numWords * 100 > changeThreshold);
		
		HashMap<String, ProbabilisticWordBag> topics = new HashMap<>();
		Arrays.stream(topicBags).forEach((bag) -> topics.put(bag.name(), bag));
		
		return topics;
	}
}
