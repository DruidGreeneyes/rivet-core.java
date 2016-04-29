package rivet.extras.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeMap;
import scala.Tuple2;
import scala.Tuple3;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import static java.util.Arrays.stream;

import java.util.ArrayList;


public class LDA {
	
	public static HashMap<String, String[]> gibbsSample (String[][] texts, int numTopics, int changeThreshold) {
		HashMap<Integer, String[]> topicsA = new HashMap<>();
		ArrayList<Tuple3<Integer, String, Integer>> topicsB = new ArrayList<>(); 
		Random r = new Random();
		for (int c = 0; c < texts.length; c++) {
			String[] text = texts[c];
			for (int i = 0; i < text.length; i++) {
				String word = text[i];
				int topic = r.nextInt(numTopics);
				topicsB.add(new Tuple3<>(c, word, topic));
				topicsA.put(topic, ArrayUtils.add(topicsA.getOrDefault(topic, new String[0]), word));
			}
		}
		
		int changes;
		do {
			changes = 0;
			for (Tuple3<Integer, String, Integer> word : topicsB) {
				int[] topics = topicsA.keySet().stream().mapToInt(x -> x).toArray();
				double[] probs = new double[numTopics];
				topicsA.forEach((topic, words) -> {
					double pTopicGivenDocument = topicsB.stream().filter((e) -> e._1() == word._1() && e._3() == topic).count() / (double)texts[word._1()].length;
					double pWordGivenTopic = stream(words).filter(word._2()::equals).count() / (double)words.length;
					double prob = pTopicGivenDocument * pWordGivenTopic;
					probs[topic] = prob;
				});
				
				double factor = stream(probs).sum();
				double[] topicProbs = stream(probs).map((p) -> p / factor).toArray();
				
				int newTopic = new EnumeratedIntegerDistribution(topics, topicProbs).sample();
				if (newTopic != word._3()) {
					changes++;
					String[] oldTopicWordList = topicsA.get(word._3());
					topicsA.put(word._3(), ArrayUtils.remove(oldTopicWordList, ArrayUtils.indexOf(oldTopicWordList, word._2())));
					topicsA.put(newTopic, ArrayUtils.add(topicsA.get(newTopic), word._2()));
				}
			}
		} while (changes/topicsB.size() * 100 > changeThreshold);
		
		HashMap<String, String[]> topics = new HashMap<>();
		topicsA.forEach((topicID, words) -> {
			TreeMap<Integer, String> wordCounts = new TreeMap<>();
			for (String word : words) {
				if (!wordCounts.containsValue(word))
					wordCounts.put((int)stream(words).filter(word::equals).count(), word);
			}
			HashSet<String> probableTopicNames = new HashSet<>();
			int count = 0;
			do {
				int key = wordCounts.lastKey();
				probableTopicNames.add(wordCounts.get(key));
				wordCounts.remove(key);
				count += key;
			} while (count < words.length / 10);
			
			String topicName = String.join("/", probableTopicNames);
			topics.put(topicName, words);
		});
		return topics;
	}
	
//	public static HashMap<Double, Tuple2<String, String[]>> heirarchicalGibbsSample (String[][] texts, int numTopics, int changeThreshold) {
//		
//	}
}
