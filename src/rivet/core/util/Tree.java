package rivet.core.util;

import java.util.ArrayList;

public class Tree<T> {
	public final T data;
	protected final Tree<T> parent;
	protected final ArrayList<Tree<T>> children;
	
	protected Tree (T d, Tree<T> p, ArrayList<Tree<T>> c) { data = d; parent = p; children = c; }
	protected Tree (T d, Tree<T> p) {this(d, p, new ArrayList<>());}
	protected Tree (T d) {this(d, null);}
	
	public boolean hasChildren() { return children.size() == 0; }
	public boolean isRoot() { return parent == null; }
	
	public void adopt (Tree<T> child) { children.add(child); }
	public void adopt (T data) { adopt(new Tree<>(data, this)); }
	
	public void slay (T data) { children.removeIf((x) -> x.data == data); }
	
	public Tree<T> child(int index) { return children.get(index); }
	public Tree<T> parent() {return parent;}
	public T get() {return data;}
	
	public ArrayList<Tree<T>> children() { return new ArrayList<>(children); }
	
	public static <T> Tree<T> makeRoot(T data) {return new Tree<>(data, null); }
	public static <T> Tree<T> makeNode(T data, Tree<T> parent) {
		Tree<T> child = new Tree<>(data, parent);
		parent.adopt(child);
		return child;
	}
	
}
