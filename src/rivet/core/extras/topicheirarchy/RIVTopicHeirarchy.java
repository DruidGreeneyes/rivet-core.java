package rivet.core.extras.topicheirarchy;

import java.util.ArrayList;

import rivet.core.labels.ArrayRIV;
import rivet.core.labels.RandomIndexVector;

public class RIVTopicHeirarchy {

    private static final ArrayList<RIVTopicHeirarchy> _find(
            final RIVTopicHeirarchy point, final ArrayRIV riv,
            final ArrayList<RIVTopicHeirarchy> nodes) {
        nodes.add(point);
        if (!point.hasChildren())
            return nodes;
        else {
            final RIVTopicHeirarchy next = point.children.stream().reduce(point,
                    (i, node) -> RandomIndexVector.similarity(
                            i.topic.meanVector(), riv) > RandomIndexVector
                                    .similarity(node.topic.meanVector(), riv)
                                            ? i
                                            : node);
            if (next == point)
                return nodes;
            else
                return _find(next, riv, nodes);
        }
    }

    public static final String[] assignTopics(final RIVTopicHeirarchy point,
            final ArrayRIV riv) {
        final ArrayList<RIVTopicHeirarchy> nodes = _find(findRoot(point), riv,
                new ArrayList<>());
        return nodes.stream().map(RIVTopicHeirarchy::name)
                .toArray(String[]::new);
    }

    public static final RIVTopicHeirarchy find(final RIVTopicHeirarchy point,
            final ArrayRIV riv) {
        final ArrayList<RIVTopicHeirarchy> nodes = _find(findRoot(point), riv,
                new ArrayList<>());
        return nodes.get(nodes.size() - 1);
    }

    public static RIVTopicHeirarchy findRoot(final RIVTopicHeirarchy point) {
        return point.isRoot() ? point : findRoot(point.parent);
    }

    public static RIVTopicHeirarchy makeNode(final NamedRIVMap topic,
            final RIVTopicHeirarchy parent) {
        final RIVTopicHeirarchy child = new RIVTopicHeirarchy(topic, parent,
                parent.similarityThreshold);
        parent.adopt(child);
        return child;
    }

    public static RIVTopicHeirarchy makeRoot(final NamedRIVMap topic,
            final double threshold) {
        return new RIVTopicHeirarchy(topic, null, threshold);
    }

    private NamedRIVMap topic;

    private RIVTopicHeirarchy parent;

    private ArrayList<RIVTopicHeirarchy> children;
    final double similarityThreshold;

    private RIVTopicHeirarchy(final NamedRIVMap t, final double s) {
        this(t, null, s);
    }

    private RIVTopicHeirarchy(final NamedRIVMap t, final RIVTopicHeirarchy p,
            final ArrayList<RIVTopicHeirarchy> c, final double s) {
        topic = t;
        parent = p;
        children = c;
        similarityThreshold = s;
    }

    private RIVTopicHeirarchy(final NamedRIVMap t, final RIVTopicHeirarchy p,
            final double s) {
        this(t, p, new ArrayList<>(), s);
    }

    private void add(final NamedRIV riv) {
        topic.put(riv);
    }

    private void adopt(final NamedRIV riv) {
        adopt(new NamedRIVMap(topic.size, riv));
    }

    private void adopt(final NamedRIVMap topic) {
        adopt(new RIVTopicHeirarchy(topic, similarityThreshold));
    }

    private void adopt(final RIVTopicHeirarchy child) {
        children.add(child);
        child.parent = this;
    }

    public ArrayList<RIVTopicHeirarchy> children() {
        return new ArrayList<>(children);
    }

    public RIVTopicHeirarchy find(final ArrayRIV riv) {
        return find(this, riv);
    }

    public RIVTopicHeirarchy findRoot() {
        return findRoot(this);
    }

    public void graftNew(final NamedRIV riv) {
        final RIVTopicHeirarchy point = find(riv.riv());
        if (RandomIndexVector.similarity(point.topic.meanVector(),
                riv.riv()) >= similarityThreshold)
            point.add(riv);
        else
            point.adopt(riv);
    }

    private boolean hasChildren() {
        return children.size() == 0;
    }

    private boolean isRoot() {
        return parent == null;
    }

    public double magnitude() {
        return topic.magnitude();
    }

    public String name() {
        return topic.name();
    }

    private void orphan(final RIVTopicHeirarchy child) {
        child.parent = null;
        children.remove(child);
    }

    public RIVTopicHeirarchy parent() {
        return parent;
    }

    public void prune(final NamedRIV riv) {
        find(riv.riv()).subtract(riv);
    }

    public void reGraft(final NamedRIV riv) {
        prune(riv);
        graftNew(riv);
    }

    private void subtract(final NamedRIV riv) {
        if (topic.contains(riv))
            topic.remove(riv);
        else
            throw new IndexOutOfBoundsException(String.format(
                    "Tried to subtract but riv is not present in topic!\n%s : %s",
                    riv.name(), String.join(", ", topic.keySet())));
        if (topic.isEmpty())
            suicide();
    }

    private void suicide() {
        children.forEach((child) -> child.updateParent(parent));
        parent.orphan(this);
        topic = null;
        children = null;
    }

    public NamedRIVMap topic() {
        return topic;
    }

    private void updateParent(final RIVTopicHeirarchy newParent) {
        parent.orphan(this);
        newParent.adopt(this);
    }

}
