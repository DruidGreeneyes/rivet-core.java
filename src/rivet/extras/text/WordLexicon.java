package rivet.extras.text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import rivet.core.arraylabels.Labels;
import rivet.core.arraylabels.RIV;
import rivet.core.util.DCounter;
import rivet.core.util.Pair;
import rivet.extras.text.lda.RIVTopicHeirarchy;
import rivet.extras.text.lda.Topic;

public class WordLexicon extends DualHashBidiMap<String, RIV> {
	private final int size;
	
	private RIVTopicHeirarchy topics;
	
	public WordLexicon(int size) {super(); this.size = size; topics = RIVTopicHeirarchy.makeRoot(Topic.make(new RIV(size), "root")); }
	
	public RIV meanVector() {
		return values().stream()
				.reduce(new RIV(size), Labels::addLabels)
				.divideBy((double)size());
	}
	
	public void assignTopics (double fuzziness) {
		double fuzz = 1 - fuzziness;
		RIV meanVector = meanVector();
		List<Pair<RIV, Double>> rivs = 
				values().stream()
				.map((riv) -> Pair.make(riv, Labels.similarity(riv, meanVector)))
				.sorted((x, y) -> Double.compare(x.right, y.right))
				.collect(Collectors.toList());
		
		List<Pair<RIV, Double>> currentRIVs = new ArrayList<>();
		
		//Begin the topic assignment loop
		RIVTopicHeirarchy node = topics;
		while (rivs.size() > 0) {
			DCounter f = new DCounter();
			
			//Generate the RIV for this Node
			while (currentRIVs.size() < 1) {
				f.inc(fuzz);
				currentRIVs = rivs.stream()
						.filter(x -> x.right >= f.get())
						.collect(Collectors.toList());
			}
			rivs.removeAll(currentRIVs);
			RIV nodeRIV = currentRIVs.stream().map((p) -> p.left).reduce(new RIV(size), Labels::addLabels);
			node.update(nodeRIV);
			
			currentRIVs.clear();
			f.zero();
			
			//Generate the list of child RIVs for this node
			while (currentRIVs.size() < 1) {
				f.inc(fuzz);
				currentRIVs = rivs.stream()
						.filter(x -> x.right >= f.get())
						.collect(Collectors.toList());
			}
			rivs.removeAll(currentRIVs);
			
			//Cluster the 
			
		}
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8893148657223464815L;
	
		
}
