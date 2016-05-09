package rivet.core.util;

public class Triple<A, B, C> {
    public final A left;
    public final B center;
    public final C right;
    
    private Triple (A a, B b, C c) { left = a; center = b; right = c; }
    
    public static <A, B, C> Triple<A, B, C> make (A left, B center, C right) { return new Triple<>(left, center, right); }
}
