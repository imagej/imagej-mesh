package net.imagej.mesh.alg.zslicer;

import static net.imagej.mesh.alg.zslicer.ZSlicer.EPS;
import static net.imagej.mesh.alg.zslicer.ZSlicer.mround;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import gnu.trove.list.array.TDoubleArrayList;
import net.imagej.mesh.util.SortTrove;

/**
 * Class that represents the intersection of a closed 3D mesh (manifold and
 * two-manifold) with a plane. The slice is made of a collection of several
 * closed {@link Contour}s. They are sorted by decreasing area.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class Slice implements Collection<Contour> {

    private final List<Contour> contours;

    Slice(final Collection<Contour> contours) {
	this.contours = new ArrayList<>(contours);
	this.contours.sort(Comparator.comparing(Contour::area).reversed());
    }

    @Override
    public int size() {
	return contours.size();
    }

    @Override
    public boolean isEmpty() {
	return contours.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
	return contours.contains(o);
    }

    @Override
    public Iterator<Contour> iterator() {
	return contours.iterator();
    }

    @Override
    public Object[] toArray() {
	return contours.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
	return contours.toArray(a);
    }

    @Override
    public boolean add(final Contour e) {
	throw new UnsupportedOperationException("Slice class is unmodifiable.");
    }

    @Override
    public boolean remove(final Object o) {
	throw new UnsupportedOperationException("Slice class is unmodifiable.");
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
	return contours.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends Contour> c) {
	throw new UnsupportedOperationException("Slice class is unmodifiable.");
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
	throw new UnsupportedOperationException("Slice class is unmodifiable.");
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
	throw new UnsupportedOperationException("Slice class is unmodifiable.");
    }

    @Override
    public void clear() {
	throw new UnsupportedOperationException("Slice class is unmodifiable.");
    }

    /**
     * Computes the position of the intersection of this slice (as a contour
     * collection) with the ray parallel to the X axis and of ordinate y, and
     * measure the projected normal value at these intersections.
     *
     * @param y      the ordinate of the ray to cast.
     * @param x      an array in which to store the intersection position. Reset by
     *               this call. Is sorted by increasing X value.
     * @param nx     an array in which to store the projected normal value at the
     *               intersections. Reset by this call.
     * @param yScale some scale in Y (such as the pixel size in Y), used to shift
     *               the vertices and ray position by a small fraction of this size.
     */
    public void xRayCast(final double y, final TDoubleArrayList x, final TDoubleArrayList nx, final double yScale) {
	x.resetQuick();
	nx.resetQuick();

	final double yr = mround(y, yScale * EPS, 2, 1);
	for (final Contour c : contours) {

	    int i;
	    int j;
	    for (i = 0, j = c.size() - 1; i < c.size(); j = i++) {

		final double yi = mround(c.y(i), yScale * EPS, 2, 0);
		final double yj = mround(c.y(j), yScale * EPS, 2, 0);
		if ((yj > yr && yi > yr) || (yj < yr && yi < yr))
		    continue;

		final double xi = c.x(i);
		final double xj = c.x(j);

		final double s = (yr - yj) / (yi - yj);
		final double xr = xj + s * (xi - xj);
		x.add(xr);
		nx.add(c.nx(j));
	    }
	}

	final int[] index = SortTrove.quicksort(x);
	SortTrove.reorder(nx, index);
    }

    /**
     * Computes the position of the intersection of this slice (as a contour
     * collection) with the ray parallel to the X axis and of ordinate y.
     *
     * @param y      the ordinate of the ray to cast.
     * @param x      an array in which to store the intersection position. Reset by
     *               this call.
     * @param nx     an array in which to store the projected normal value at the
     *               intersections. Reset by this call.
     * @param yScale some scale in Y (such as the pixel size in Y), used to shift
     *               the vertices and ray position by a small fraction of this size.
     */
    public void xRayCast(final double y, final TDoubleArrayList x, final double yScale) {
	x.resetQuick();

	final double yr = mround(y, yScale * EPS, 2, 1);
	for (final Contour c : contours) {

	    int i;
	    int j;
	    for (i = 0, j = c.size() - 1; i < c.size(); j = i++) {

		final double yi = mround(c.y(i), yScale * EPS, 2, 0);
		final double yj = mround(c.y(j), yScale * EPS, 2, 0);
		if ((yj > yr && yi > yr) || (yj < yr && yi < yr))
		    continue;

		final double xi = c.x(i);
		final double xj = c.x(j);

		final double s = (yr - yj) / (yi - yj);
		final double xr = xj + s * (xi - xj);
		x.add(xr);
	    }
	}
	x.sort();
    }
}
