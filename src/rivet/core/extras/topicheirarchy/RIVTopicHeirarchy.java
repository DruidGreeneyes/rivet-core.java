package rivet.core.extras.topicheirarchy;

import rivet.core.labels.ArrayRIV;

import java.util.ArrayList;

import rivet.core.labels.RandomIndexVector;

public class RIVTopicHeirarchy {
    
    private NamedRIVMap topic;
    private RIVTopicHeirarchy parent;
    private ArrayList<RIVTopicHeirarchy> children;
    final double similarityThreshold;
    
    private RIVTopicHeirarchy (NamedRIVMap t, RIVTopicHeirarchy p, ArrayList<RIVTopicHeirarchy> c, double s) { 
        topic = t;
        parent = p;
        children = c;
        similarityThreshold = s;
    }
    private RIVTopicHeirarchy (NamedRIVMap t, RIVTopicHeirarchy p, double s) { this(t, p, new ArrayList<>(), s); }
    private RIVTopicHeirarchy (NamedRIVMap t, double s) { this(t, null, s); }
    
    @SuppressWarnings("unused")
    private void updateTopic (NamedRIVMap topicBag) { topic = topicBag; }
    private void updateParent (RIVTopicHeirarchy newParent) { 
        parent.orphan(this);
        newParent.adopt(this); 
    }
    
    private boolean hasChildren() { return children.size() == 0; }
    private boolean isRoot() { return parent == null; }
    
    private void adopt (RIVTopicHeirarchy child) { children.add(child); child.parent = this; }
    private void adopt (NamedRIVMap topic) { adopt(new RIVTopicHeirarchy(topic, this.similarityThreshold)); }
    private void adopt (NamedRIV riv) { adopt(new NamedRIVMap(this.topic.size, riv)); }
    
    private void orphan (RIVTopicHeirarchy child) { 
        child.parent = null;
        children.remove(child);
    }
    
    private void suicide () {
        children.forEach((child) -> child.updateParent(parent));
        parent.orphan(this);
        topic = null;
        children = null;
    }
    
    private void subtract (NamedRIV riv) {
        if (topic.contains(riv))
            topic.remove(riv);
        else
            throw new IndexOutOfBoundsException(String.format("Tried to subtract but riv is not present in topic!\n%s : %s", riv.name(), String.join(", ", topic.keySet())));
        if (topic.isEmpty())
            suicide();
    }
    private void add (NamedRIV riv) {
        topic.put(riv);
    }
    
    public RIVTopicHeirarchy find(ArrayRIV riv) { return find(this, riv); }
    public RIVTopicHeirarchy findRoot() { return findRoot(this); }
    
    public String name() {return topic.name();}
    public double magnitude() {return topic.magnitude();}
    
    public void prune (NamedRIV riv) {
        find(riv.riv()).subtract(riv);
    }
    
    public void graftNew (NamedRIV riv) {
        RIVTopicHeirarchy point = find(riv.riv());
        if (RandomIndexVector.similarity(point.topic.meanVector(), riv.riv()) >= similarityThreshold)
            point.add(riv);
        else
            point.adopt(riv);
    }
    
    public void reGraft (NamedRIV riv) {
        prune(riv);
        graftNew(riv);
    }
    
    public ArrayList<RIVTopicHeirarchy> children() { return new ArrayList<>(children); }
    public RIVTopicHeirarchy parent() {return parent;}
    public NamedRIVMap topic() {return topic;}
    
    
    public static RIVTopicHeirarchy makeRoot(NamedRIVMap topic, double threshold) {return new RIVTopicHeirarchy(topic, null, threshold); }
    public static RIVTopicHeirarchy makeNode(NamedRIVMap topic, RIVTopicHeirarchy parent) {
        RIVTopicHeirarchy child = new RIVTopicHeirarchy(topic, parent, parent.similarityThreshold);
        parent.adopt(child);
        return child;
    }
    
    public static RIVTopicHeirarchy findRoot(RIVTopicHeirarchy point) { return (point.isRoot()) ? point : findRoot(point.parent); }
    
    public static final String[] assignTopics(RIVTopicHeirarchy point, ArrayRIV riv) {
        ArrayList<RIVTopicHeirarchy> nodes = _find(findRoot(point), riv, new ArrayList<>());
        return nodes.stream().map(RIVTopicHeirarchy::name).toArray(String[]::new);
    }
    
    public static final RIVTopicHeirarchy find(RIVTopicHeirarchy point, ArrayRIV riv) { 
        ArrayList<RIVTopicHeirarchy> nodes = _find(findRoot(point), riv, new ArrayList<>());
        return nodes.get(nodes.size() - 1);
    }
    private static final ArrayList<RIVTopicHeirarchy> _find(RIVTopicHeirarchy point, ArrayRIV riv, ArrayList<RIVTopicHeirarchy> nodes) {
        nodes.add(point);
        if (!point.hasChildren())
            return nodes;
        else {
            RIVTopicHeirarchy next =
                    point.children.stream()
                        .reduce(point, (i, node) -> 
                        (RandomIndexVector.similarity(i.topic.meanVector(), riv) > RandomIndexVector.similarity(node.topic.meanVector(), riv))
                                ? i
                                : node);
            if (next == point) 
                return nodes;
            else 
                return _find(next, riv, nodes);
        }
    }

}
