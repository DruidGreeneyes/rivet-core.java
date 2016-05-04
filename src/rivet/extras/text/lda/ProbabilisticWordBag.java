package rivet.extras.text.lda;

import rivet.core.util.ProbabilisticBag;

public class ProbabilisticWordBag extends ProbabilisticBag<String> {
		
	public String name(int threshold) {
		StringBuilder sb = new StringBuilder();
		bag.descendingKeySet()
			.stream()
			.reduce(0,
					(i, s) -> {
						if (i < threshold) {
							sb.append(s);
							return i + count(s);
						} else return i;
					},
					(i1, i2) -> i1 + i2);
		return sb.toString();
	}
	
	public String name() { return name(count() / 10); }
	
}
