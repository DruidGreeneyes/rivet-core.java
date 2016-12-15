package rivet.adapt;

import java.util.Collection;
import java.util.Iterator;

import rivet.core.labels.MapRIV;

public class SOLR {
    public static MapRIV mapRIVFromCollections(final Collection<Object> indices,
            final Collection<Object> values, final int size) {
        final MapRIV res = MapRIV.empty(size);
        final Iterator<Object> is = indices.iterator();
        final Iterator<Object> vs = values.iterator();
        while (is.hasNext() && vs.hasNext())
            res.put((int) is.next(), (double) vs.next());
        return res;
    }
}
