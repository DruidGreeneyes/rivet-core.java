package rivet.core.labels;

import static org.junit.Assert.*;

import org.junit.Test;

import rivet.core.util.Util;

public class VectorElementTests {
    
    static String testString = "1|2.000000";
    
    public final void assertEqual(VectorElement v1, VectorElement v2) {
        assertEquals(v1.index(), v2.index());
        assertEquals(v1.value(), v2.value(), Util.roundingError);
    }

    @Test
    public final void testToString() {
        VectorElement v = VectorElement.elt(1, 2);
        assertEquals(testString, v.toString());
    }

    @Test
    public final void testFromString() {
        VectorElement v = VectorElement.elt(1, 2);
        assertEqual(v, VectorElement.fromString(testString));
    }

}
