package rivet.core.extras.topicheirarchy;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import rivet.core.labels.ArrayRIV;

public class NamedRIVMap extends HashMap<String, ArrayRIV>{
    /**
     * 
     */
    private static final long serialVersionUID = -3888443959684391274L;

    private static double NAMING_THRESHOLD = 0.1;
    
    public final int size;
    
    public NamedRIVMap (int size) {super(); this.size = size;}
    public NamedRIVMap (int size, NamedRIV riv) {
        this(size);
        this.put(riv);
    }
    
    public boolean contains(NamedRIV riv) {
        return super.containsKey(riv.name());
    }
    
    public void put(NamedRIV riv) {
        super.put(riv.name(), riv.riv());
    }
    
    public void remove(NamedRIV riv) {
        super.remove(riv.name());
    }
    
    @Override
    public int size() {return size;}
    public int count() {return super.size();}
    
    public Stream<ArrayRIV> stream() {return values().stream();}
    
    public ArrayRIV meanVector() {
        return new ArrayRIV(size).add(stream()).divide(count());
    }
    public double magnitude() {
        return meanVector().magnitude();
    }
    
    public String name() {
        int numNames = Math.max(1, (int) Math.round(count() * NAMING_THRESHOLD));
        return entrySet()
                .stream()
                .sorted((e1, e2) -> Double.compare(e1.getValue().magnitude(), e2.getValue().magnitude()))
                .limit(numNames)
                .map(Entry::getKey)
                .collect(Collectors.joining("/"));
    }
}
