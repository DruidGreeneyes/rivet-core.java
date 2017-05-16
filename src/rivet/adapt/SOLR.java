package rivet.adapt;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;

import rivet.core.labels.ColtRIV;
import rivet.core.labels.DenseRIV;
import rivet.core.labels.HPPCRIV;
import rivet.core.labels.MapRIV;
import rivet.core.labels.ArrayRIV;

public class SOLR {
  public static MapRIV mapRIVFromCollections(final Collection<Object> indices,
                                             final Collection<Object> values,
                                             final int size) {
    final MapRIV res = MapRIV.empty(size);
    final Iterator<Object> is = indices.iterator();
    final Iterator<Object> vs = values.iterator();
    while (is.hasNext() && vs.hasNext())
      res.put((int) is.next(), (double) vs.next());
    return res;
  }

  public static ColtRIV coltRIVFromCollections(
                                               final Collection<Object> indices,
                                               final Collection<Object> values,
                                               final int size) {
    final ColtRIV res = ColtRIV.empty(size);
    final Iterator<Object> is = indices.iterator();
    final Iterator<Object> vs = values.iterator();
    while (is.hasNext() && vs.hasNext())
      res.put((int) is.next(), (double) vs.next());
    return res;
  }

  public static HPPCRIV hppcRIVFromCollections(
                                               final Collection<Object> indices,
                                               final Collection<Object> values,
                                               final int size) {
    final HPPCRIV res = HPPCRIV.empty(size);
    final Iterator<Object> is = indices.iterator();
    final Iterator<Object> vs = values.iterator();
    while (is.hasNext() && vs.hasNext())
      res.put((int) is.next(), (double) vs.next());
    return res;
  }

  public static DenseRIV denseRIVFromCollections(
                                                 final Collection<Object> indices,
                                                 final Collection<Object> values,
                                                 final int size) {
    final DenseRIV res = DenseRIV.empty(size);
    final Iterator<Object> is = indices.iterator();
    final Iterator<Object> vs = values.iterator();
    while (is.hasNext() && vs.hasNext())
      res.put((int) is.next(), (double) vs.next());
    return res;
  }

  public static ArrayRIV arrayRIVFromCollections(
                                                 final Collection<Object> indices,
                                                 final Collection<Object> values,
                                                 final int size) {
    final Integer[] is = indices.toArray(new Integer[indices.size()]);
    final Double[] vs = values.toArray(new Double[values.size()]);
    return new ArrayRIV(ArrayUtils.toPrimitive(is),
                        ArrayUtils.toPrimitive(vs), size);
  }
}
