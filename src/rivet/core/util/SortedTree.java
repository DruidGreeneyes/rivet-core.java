package rivet.core.util;

import java.util.ArrayList;
import java.util.Comparator;

public class SortedTree<T> implements Comparable<SortedTree<T>> {
    private final T data;
    private final SortedTree<T> parent;
    private final ArrayList<SortedTree<T>> children;
    private final Comparator<T> compare;
    
    private SortedTree (T d, SortedTree<T> p, ArrayList<SortedTree<T>> c, Comparator<T> comp) { data = d; parent = p; children = c; compare = comp;}
    private SortedTree (T d, SortedTree<T> p, Comparator<T> comp) {this(d, p, new ArrayList<>(), comp); }
    
    public boolean hasChildren() { return children.size() == 0; }
    public boolean isRoot() { return parent == null; }
    
    public void adopt (SortedTree<T> child) {
        children.add(child);
        children.sort((x, y) -> x.compareTo(y));
    }
    public void adopt (T data) { adopt(new SortedTree<>(data, this, compare)); }
    
    public void slay (T data) { children.removeIf((x) -> x.data == data); }
    
    public SortedTree<T> child(int index) { return children.get(index); }
    public SortedTree<T> parent() {return parent;}
    public T get() {return data;}
    
    public ArrayList<SortedTree<T>> children() { return new ArrayList<>(children); }
    
    public static <T> SortedTree<T> makeRoot(T data, Comparator<T> comparator) {return new SortedTree<>(data, null, comparator); }
    public static <T> SortedTree<T> makeNode(T data, SortedTree<T> parent) {
        SortedTree<T> child = new SortedTree<>(data, parent, parent.compare);
        parent.adopt(child);
        return child;
    }
    
    @Override
    public int compareTo(SortedTree<T> other) { return compare.compare(this.data, other.data); }
    
}
