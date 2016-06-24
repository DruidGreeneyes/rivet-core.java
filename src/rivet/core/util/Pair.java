package rivet.core.util;

import java.util.function.BiFunction;

public class Pair<A, B> {
    public static <A, B> Pair<A, B> make(final A left, final B right) {
        return new Pair<>(left, right);
    }

    public final A left;

    public final B right;

    protected Pair(final A a, final B b) {
        left = a;
        right = b;
    }

    public <R> R apply(final BiFunction<A, B, R> fun) {
        return fun.apply(this.left, this.right);
    }
}
