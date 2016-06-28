package rivet.core.extras.topicheirarchy;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import rivet.core.labels.ArrayRIV;

public class NamedRIVMap extends HashMap<String, ArrayRIV> {
    /**
     *
     */
    private static final long serialVersionUID = -3888443959684391274L;

    private static double NAMING_THRESHOLD = 0.1;

    public final int size;

    public NamedRIVMap(final int size) {
        super();
        this.size = size;
    }

    public NamedRIVMap(final int size, final NamedRIV riv) {
        this(size);
        this.put(riv);
    }

    public boolean contains(final NamedRIV riv) {
        return super.containsKey(riv.name());
    }

    public int count() {
        return super.size();
    }

    public double magnitude() {
        return meanVector().magnitude();
    }

    public ArrayRIV meanVector() {
        return stream().reduce(new ArrayRIV(size), ArrayRIV::destructiveAdd);
    }

    public String name() {
        final int numNames = Math.max(1,
                (int) Math.round(count() * NAMING_THRESHOLD));
        return entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e1.getValue().magnitude(),
                        e2.getValue().magnitude()))
                .limit(numNames).map(Entry::getKey)
                .collect(Collectors.joining("/"));
    }

    public void put(final NamedRIV riv) {
        super.put(riv.name(), riv.riv());
    }

    public void remove(final NamedRIV riv) {
        super.remove(riv.name());
    }

    @Override
    public int size() {
        return size;
    }

    public Stream<ArrayRIV> stream() {
        return values().stream();
    }
}
