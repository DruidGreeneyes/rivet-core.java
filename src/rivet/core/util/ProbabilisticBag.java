package rivet.core.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

public class ProbabilisticBag<T> {
    
    protected final TreeMap<T, Integer> bag;
    private EnumeratedIntegerDistribution dist;
    private List<T> items;
    private boolean upToDate;
    
    private Comparator<T> bagSorter = (t1, t2) -> Integer.compare(count(t1), count(t2));
    
    public ProbabilisticBag () {
        bag = new TreeMap<T, Integer>(bagSorter);
        upToDate = false;
    }
    
    private void redistribute() {
        items = bag.keySet().stream().collect(Collectors.toList());
        int count = this.count();
        int[] itemIndices = Util.range(bag.size()).toArray();
        double[] itemProbs = bag.values().stream().mapToDouble(x -> x / count).toArray();
        dist = new EnumeratedIntegerDistribution(itemIndices, itemProbs);
        upToDate = true;
    }
    
    private void update() { if (!upToDate) redistribute(); }
    
    public void add(T item, int num) {bag.put(item, bag.getOrDefault(item, 0) + num); upToDate = false; }    
    public void add(T item) { add(item, 1); }
    
    public void subtract(T item, int num) {
        int count = bag.getOrDefault(item, 0);
        if (count > num)
            bag.put(item, count - num);
        else if (count > 0)
            bag.remove(item);
        upToDate = false;
    }
    public void subtract(T item) { subtract(item, 1); }
    
    public int count(T item) { return bag.get(item); }
    public int count() { return bag.values().stream().reduce(0, (x, y) -> x + y); }
    
    public double probability (T item) { return count(item) / (double)count(); }
    public double[] probabilities () { final int count = count(); return bag.values().stream().mapToDouble(x -> x / count).toArray(); }
    
    public List<T> sample (int sampleSize) {
        update();
        int[] sample = dist.sample(sampleSize);
        return Arrays.stream(sample).mapToObj(x -> items.get(x)).collect(Collectors.toList());
    }
    public T sample () { update(); return items.get(dist.sample()); }
}
