package com.github.druidgreeneyes.rivet.core.extras.topicheirarchy;

import com.github.druidgreeneyes.rivet.core.labels.ArrayRIV;

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
    
    public static NamedRIV make(String name, ArrayRIV riv) {
        return new NamedRIV(name, riv);
    }
}
