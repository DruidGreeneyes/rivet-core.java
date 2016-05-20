package rivet.core.extras.topicheirarchy;

import rivet.core.labels.ArrayRIV;

public final class NamedRIV {
    private final String name;
    private final ArrayRIV riv;
    private final double magnitude;
    
    private NamedRIV(final String name, final ArrayRIV riv) {
        this.riv = riv;
        this.name = name;
        this.magnitude = riv.magnitude();
    }
    
    public ArrayRIV riv() {return riv;}
    public String name() {
        return name;
    }
    
    public double magnitude() {return magnitude;}
}
