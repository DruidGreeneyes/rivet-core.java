package rivet.extras.text.lda;

import java.util.ArrayList;

public class TopicHeirarchy {

    private ProbabilisticWordBag topic;
    private TopicHeirarchy parent;
    private ArrayList<TopicHeirarchy> children;

    private TopicHeirarchy (ProbabilisticWordBag t, TopicHeirarchy p, ArrayList<TopicHeirarchy> c) { topic = t; parent = p; children = c; }
    private TopicHeirarchy (ProbabilisticWordBag t, TopicHeirarchy p) { this(t, p, new ArrayList<>()); }
    private TopicHeirarchy (ProbabilisticWordBag t) { this(t, null); }

    public void updateTopic (ProbabilisticWordBag newTopic) { topic = newTopic; }
    public void updateParent (TopicHeirarchy newParent) {
        parent.orphan(this);
        newParent.adopt(this);
    }

    public boolean hasChildren() { return children.size() == 0; }
    public boolean isRoot() { return parent == null; }

    public void adopt (TopicHeirarchy child) { children.add(child); child.parent = this; }
    public void adopt (ProbabilisticWordBag topic) { adopt(new TopicHeirarchy(topic)); }

    public void orphan (TopicHeirarchy child) { 
        child.parent = null; 
        children.remove(child);
    }


    public ArrayList<TopicHeirarchy> children() { return new ArrayList<>(children); }
    public TopicHeirarchy child(int index) { return children.get(index); }
    public TopicHeirarchy parent() {return parent;}
    public ProbabilisticWordBag topic() {return topic;}


    public static TopicHeirarchy makeRoot(ProbabilisticWordBag topic) {return new TopicHeirarchy(topic, null); }
    public static TopicHeirarchy makeNode(ProbabilisticWordBag topic, TopicHeirarchy parent) {
        TopicHeirarchy child = new TopicHeirarchy(topic, parent);
        parent.adopt(child);
        return child;
    }

    public TopicHeirarchy findRoot() { return findRoot(this); }

    public static TopicHeirarchy findRoot(TopicHeirarchy point) { return (point.isRoot()) ? point : findRoot(point.parent); }
}
