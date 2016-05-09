package rivet.extras.text.lda;

import rivet.core.arraylabels.RIV;

public class RIVTopic {
    public final RIV riv;
    public final String name;
    
    private RIVTopic (RIV riv, String name) {this.riv = riv; this.name = name;}
    public static RIVTopic make (RIV riv, String name) { return new RIVTopic(riv, name); }
}
