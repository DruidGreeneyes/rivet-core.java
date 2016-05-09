package rivet.extras.text.lda;

import rivet.core.arraylabels.RIV;

import java.util.ArrayList;
import rivet.core.arraylabels.Labels;

public class RIVTopicHeirarchy {
    
    private RIVTopic topic;
    private RIVTopicHeirarchy parent;
    private ArrayList<RIVTopicHeirarchy> children;
    
    private RIVTopicHeirarchy (RIVTopic t, RIVTopicHeirarchy p, ArrayList<RIVTopicHeirarchy> c) { topic = t; parent = p; children = c; }
    private RIVTopicHeirarchy (RIVTopic t, RIVTopicHeirarchy p) { this(t, p, new ArrayList<>()); }
    private RIVTopicHeirarchy (RIVTopic t) { this(t, null); }
    
    public void update (RIV riv) { topic = RIVTopic.make(riv, topic.name); }
    public void update (RIVTopic newTopic) { topic = newTopic; }
    public void update (RIVTopicHeirarchy newParent) { 
        parent.orphan(this);
        newParent.adopt(this); 
    }
    
    public boolean hasChildren() { return children.size() == 0; }
    public boolean isRoot() { return parent == null; }
    
    public void adopt (RIVTopicHeirarchy child) { children.add(child); child.parent = this; }
    public void adopt (RIVTopic topic) { adopt(new RIVTopicHeirarchy(topic)); }
    
    public void orphan (RIVTopicHeirarchy child) { 
        child.parent = null; 
        children.remove(child);
    }
    

    public ArrayList<RIVTopicHeirarchy> children() { return new ArrayList<>(children); }
    public RIVTopicHeirarchy child(int index) { return children.get(index); }
    public RIVTopicHeirarchy parent() {return parent;}
    public RIVTopic topic() {return topic;}
    
    
    public static RIVTopicHeirarchy makeRoot(RIVTopic topic) {return new RIVTopicHeirarchy(topic, null); }
    public static RIVTopicHeirarchy makeNode(RIVTopic topic, RIVTopicHeirarchy parent) {
        RIVTopicHeirarchy child = new RIVTopicHeirarchy(topic, parent);
        parent.adopt(child);
        return child;
    }
    
    public RIVTopic find(RIV riv) { return find(this, riv); }
    public RIVTopicHeirarchy findRoot() { return findRoot(this); }
    
    public static RIVTopicHeirarchy findRoot(RIVTopicHeirarchy point) { return (point.isRoot()) ? point : findRoot(point.parent); }
    

    public static final RIVTopic find(RIVTopicHeirarchy point, RIV riv) { return _find(findRoot(point), riv); }
    private static final RIVTopic _find(RIVTopicHeirarchy point, RIV riv) {
        RIVTopicHeirarchy next =
                point.children.stream()
                    .reduce(point, (i, node) -> 
                    (Labels.similarity(i.topic.riv, riv) > Labels.similarity(node.topic.riv, riv))
                            ? i
                            : node);
        if (next == point) 
            return point.topic;
        else 
            return _find(next, riv);
    }

}
