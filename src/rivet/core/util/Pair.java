package rivet.core.util;

public class Pair<A, B> {
    public final A left;
    public final B right;
    
    protected Pair (A a, B b) { left = a; right = b; }
    
    public static <A, B> Pair<A, B> make (A left, B right) { return new Pair<>(left, right); }
}
