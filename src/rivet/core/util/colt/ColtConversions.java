package rivet.core.util.colt;

import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

import cern.colt.function.tdouble.DoubleProcedure;
import cern.colt.function.tdouble.IntDoubleProcedure;
import cern.colt.function.tint.IntProcedure;
import rivet.core.util.IntDoubleConsumer;

public class ColtConversions {

    public ColtConversions() {
        // TODO Auto-generated constructor stub
    }

    public static IntProcedure procedurize(final IntConsumer consumer) {
        return i -> {
            consumer.accept(i);
            return true;
        };
    }

    public static DoubleProcedure procedurize(final DoubleConsumer consumer) {
        return i -> {
            consumer.accept(i);
            return true;
        };
    }

    public static IntDoubleProcedure procedurize(
            final IntDoubleConsumer consumer) {
        return (a, b) -> {
            consumer.accept(a, b);
            return true;
        };
    }
}
