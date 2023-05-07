package net.imagej.mesh.alg.zslicer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import net.imagej.mesh.obj.Mesh;
import net.imagej.mesh.obj.Triangles;
import net.imagej.mesh.obj.Vertices;
import net.imagej.mesh.util.MeshUtil;
import net.imagej.mesh.util.SortArray;
import net.imagej.mesh.util.SortBy;

/**
 * Slice a mesh by a Z plane. Only works for meshes that are two-manifold
 * (otherwise contours are not closed).
 * <p>
 * Round vertices positions and plane position to even and odd multiples of a
 * small fraction of the mesh size, so that they don't intersect. Build contours
 * by iterating from an intersecting edge to the adjacent one, with a determined
 * orientation (CCW for interior contours). The contours returned "know" whether
 * they limit the interior or exterior of the mesh.
 * <p>
 * Inspired from
 * https://github.com/rminetto/slicing/blob/main/code-python/slicer.py#L377
 *
 * @author Jean-Yves Tinevez
 *
 */
public class ZSlicer {

    static final double EPS = 4e-4;

    public static List<Slice> slices(final Mesh mesh, final double[] zs, final double zScale) {

	/*
	 * Slice plane Z positions to odd multiples of eps.
	 */
	final double eps = EPS * zScale;
	final double[] zrs = new double[zs.length];
	for (int i = 0; i < zs.length; i++)
	    zrs[i] = mround(zs[i], eps, 2, 1);

	/*
	 * We want to collect the indices of the triangles which minZ is below zr and
	 * maxZ above zr. Because we have to do it for several Zs, it is worth not
	 * iterating through all the triangles every time. The solution here resembles a
	 * convoluted set intersection.
	 *
	 * We sort the triangles by minZ, and perform a binary search to find the
	 * indices of the triangles which minZ is lower than zr (0 -> k1). Then we sort
	 * the resulting indices by maxZ this time, and perform a binary search to find
	 * within these indices the ones that have a maxZ higher than zr (k2 -> k1).
	 *
	 * After this we have an indices array, and a lower and higher bound inside this
	 * array of triangle that cross the plane zr.
	 */

	// Collect minZ & maxZ of vertices.
	final Triangles triangles = mesh.triangles();
	final Vertices vertices = mesh.vertices();

	final double[] minZs = new double[(int) triangles.size()];
	final double[] maxZs = new double[(int) triangles.size()];
	for (int t = 0; t < triangles.size(); t++) {
	    final long v0 = triangles.vertex0(t);
	    final long v1 = triangles.vertex1(t);
	    final long v2 = triangles.vertex2(t);

	    final double minZ = minZ(vertices, v0, v1, v2, eps);
	    minZs[t] = minZ;
	    final double maxZ = maxZ(vertices, v0, v1, v2, eps);
	    maxZs[t] = maxZ;
	}
	final int[] indexMin = SortArray.quicksort(minZs);
	final int[] indices = new int[indexMin.length];

	/*
	 * Now minZs is sorted by increasing values, and indexMin contains the index of
	 * triangles in the sorted array. For instance the triangle with the smallest
	 * minZ (first in the minZs array) as an id equal to indexMin[0].
	 */

	/*
	 * Build sets of intersecting triangles for each z plane.
	 */
	final List<Slice> slices = new ArrayList<>(zrs.length);
	for (int i = 0; i < zrs.length; i++) {
	    final double zr = zrs[i];

	    // All triangles with minZ < zr
	    int k1 = Arrays.binarySearch(minZs, zr);
	    if (k1 < 0)
		k1 = -(k1 + 1);

	    // Write these indices into an array (so that we do not change minZs).
	    System.arraycopy(indexMin, 0, indices, 0, k1);

	    /*
	     * All the triangles with id indices[0], ... indices[jmin-1] (jmin is not
	     * included, either equal or larger than zr) have a minZ below zr.
	     *
	     * Now: search among those triangles what ones have a maxZ larger than zr. We
	     * will sort indexMin[0 -> jmin] by maxZ.
	     */

	    // Sort indices by the maxZ of the triangle they point to.
	    SortBy.sortBy(indices, maxZs, 0, k1 - 1);

	    // Search the closest z.
	    int k2 = SortBy.binarySearch(indices, maxZs, 0, k1, zr);
	    if (k2 < 0)
		k2 = -(k2 + 1);

	    /*
	     * All the triangles with id indices[k2], ... indices[k1-1] have a minZ below zr
	     * and a maxZ above zr.
	     */

	    final List<Contour> contours = process(mesh, indices, k2, k1, zr, eps);
	    slices.add(new Slice(contours));
	}
	return slices;
    }

    /**
     * Returns a slice as a list of {@link Contour}s for the triangles of the
     * specified mesh which indices are in the specified indices array, between
     * index start (inclusive) and end (not inclusive).
     *
     * @param mesh    the mesh to slice.
     * @param indices the triangle indices.
     * @param start   the start index in the triangles indices.
     * @param end     the end index (not inclusive) in the triangle indices.
     * @return a new list of {@link Contour}s.
     */
    private static List<Contour> process(final Mesh mesh, final int[] indices, final int start, final int end,
	    final double zr, final double eps) {

	// Deal with intersecting triangle.
	final LinkedList<Segment> segments = new LinkedList<>();
	for (int i = start; i < end; i++) {
	    final int id = indices[i];
	    final Segment segment = triangleIntersection(mesh, id, zr, eps);
	    if (segment != null)
		segments.add(segment);
	}

	// Sort segments by first edge.
	Collections.sort(segments);

	// Build contours from segments.
	final List<Contour> contours = new ArrayList<>();
	while (!segments.isEmpty()) {
	    final Segment first = segments.pop();
	    if (segments.isEmpty())
		break;

	    // Contour coordinates.
	    final TDoubleArrayList x = new TDoubleArrayList();
	    final TDoubleArrayList y = new TDoubleArrayList();
	    // Normals of triangles projected on XY.
	    final TDoubleArrayList nx = new TDoubleArrayList();
	    final TDoubleArrayList ny = new TDoubleArrayList();

	    // Init.
	    x.add(first.xa);
	    y.add(first.ya);
	    nx.add(first.nx);
	    ny.add(first.ny);
	    boolean isClosed = false;
	    Segment match = new Segment(Double.NaN, Double.NaN, first.eb, -1, Double.NaN, Double.NaN);
	    final long endEdge = first.ea;

	    // To determine interior vs exterior.
	    double minX = first.xa;
	    int minXIndex = 0;

	    // Grow contour.
	    while (!isClosed) {
		final int i = Collections.binarySearch(segments, match);
		if (i < 0)
		    break; // Did not find a match, stop contour growth.

		final Segment segment = segments.remove(i);
		x.add(segment.xa);
		y.add(segment.ya);
		nx.add(segment.nx);
		ny.add(segment.ny);

		if (segment.xa < minX) {
		    minX = segment.xa;
		    minXIndex = x.size() - 1;
		}

		match = new Segment(Double.NaN, Double.NaN, segment.eb, -1, Double.NaN, Double.NaN);
		if (segment.eb == endEdge)
		    isClosed = true;
	    }

	    // Big enough?
	    if (x.size() < 3)
		continue;

	    // Interior or exterior?
	    final boolean isInterior = nx.getQuick(minXIndex) < 0;

	    // Add it to the slice.
	    contours.add(new Contour(x, y, nx, ny, isInterior));
	}
	return contours;
    }

    /**
     * Slice a mesh by a Z-plane.
     *
     * @param mesh   the mesh to slice. Not modified.
     * @param z      The Z position of the XY plane to slice through.
     * @param zScale some scale in Z (such as the pixel size in Z), used to shift
     *               the vertices and Z-plane position by a small fraction of this
     *               size.
     * @return the section of the mesh at the specified Z position, returned as a
     *         collection of {@link Contour} objects.
     */
    public static Slice slice(final Mesh mesh, final double z, final double zScale) {
	// Slice plane to odd multiples of eps.
	final double eps = EPS * zScale;
	final double zr = mround(z, eps, 2, 1);

	final Triangles triangles = mesh.triangles();
	final Vertices vertices = mesh.vertices();

	final TIntArrayList intersecting = new TIntArrayList();
	for (long f = 0; f < triangles.size(); f++) {
	    final long v0 = triangles.vertex0(f);
	    final long v1 = triangles.vertex1(f);
	    final long v2 = triangles.vertex2(f);

	    final double minZ = minZ(vertices, v0, v1, v2, eps);
	    if (minZ > zr)
		continue;
	    final double maxZ = maxZ(vertices, v0, v1, v2, eps);
	    if (maxZ < zr)
		continue;

	    intersecting.add((int) f);
	}
	final List<Contour> contours = process(mesh, intersecting.toArray(), 0, intersecting.size(), zr, eps);
	return new Slice(contours);
    }

    private static Segment triangleIntersection(final Mesh mesh, final long id, final double z, final double eps) {
	final long v0 = mesh.triangles().vertex0(id);
	final long v1 = mesh.triangles().vertex1(id);
	final long v2 = mesh.triangles().vertex2(id);

	final double x0 = mround(mesh.vertices().x(v0), eps, 2, 0);
	final double x1 = mround(mesh.vertices().x(v1), eps, 2, 0);
	final double x2 = mround(mesh.vertices().x(v2), eps, 2, 0);
	final double y0 = mround(mesh.vertices().y(v0), eps, 2, 0);
	final double y1 = mround(mesh.vertices().y(v1), eps, 2, 0);
	final double y2 = mround(mesh.vertices().y(v2), eps, 2, 0);
	final double z0 = mround(mesh.vertices().z(v0), eps, 2, 0);
	final double z1 = mround(mesh.vertices().z(v1), eps, 2, 0);
	final double z2 = mround(mesh.vertices().z(v2), eps, 2, 0);

	/*
	 * Triangle orientation. Cross product between plane normal and triangle normal.
	 */
	final double nx = mesh.triangles().nx(id);
	final double ny = mesh.triangles().ny(id);
	final double nz = mesh.triangles().nz(id);
	final double[] cross = new double[3]; // holder for cross product.
	MeshUtil.cross(0, 0, 1., nx, ny, nz, cross);

	/*
	 * Because we shifted the vertices position and the intersecting plane position,
	 * we are sure we are not crossing any vertex.
	 *
	 * Crossing two edges, but what ones?
	 */
	// v0 -> v1
	final double[] ei0 = edgeIntersection(x0, y0, z0, x1, y1, z1, z);
	// v0 -> v2
	final double[] ei1 = edgeIntersection(x0, y0, z0, x2, y2, z2, z);
	// v1 -> v2
	final double[] ei2 = edgeIntersection(x1, y1, z1, x2, y2, z2, z);

	final double xa;
	final double xb;
	final double ya;
	final double yb;
	final long ea;
	final long eb;
	if (ei0 == null) // not v0 -> v1
	{
	    xa = ei1[0];
	    ya = ei1[1];
	    xb = ei2[0];
	    yb = ei2[1];
	    ea = MeshUtil.edgeID((int) v2, (int) v0);
	    eb = MeshUtil.edgeID((int) v1, (int) v2);
	} else if (ei1 == null) // not v0 -> v2
	{
	    xa = ei2[0];
	    ya = ei2[1];
	    xb = ei0[0];
	    yb = ei0[1];
	    ea = MeshUtil.edgeID((int) v1, (int) v2);
	    eb = MeshUtil.edgeID((int) v0, (int) v1);
	} else // not v1 -> v2
	{
	    xa = ei0[0];
	    ya = ei0[1];
	    xb = ei1[0];
	    yb = ei1[1];
	    ea = MeshUtil.edgeID((int) v0, (int) v1);
	    eb = MeshUtil.edgeID((int) v2, (int) v0);
	}

	// Careful about segment orientation.
	final double dx = xb - xa;
	final double dy = yb - ya;
	final double d = MeshUtil.dotProduct(cross[0], cross[1], cross[2], dx, dy, 0);
	if (d > 0) {
	    return new Segment(xa, ya, ea, eb, nx, ny);
	} else {
	    // Flip.
	    return new Segment(xb, yb, eb, ea, nx, ny);
	}
    }

    static final double mround(final double v, final double eps, final int mod, final int rem) {
	final long y = Math.round(v / (mod * eps));
	final double z = (y * mod + rem) * eps;
	return z;
    }

    static final class Segment implements Comparable<Segment> {

	final long ea;

	final long eb;

	final double xa;

	final double ya;

	/** Normal at that segment. */
	final double nx;

	/** Normal at that segment. */
	final double ny;

	Segment(final double xa, final double ya, final long ea, final long eb, final double nx, final double ny) {
	    this.xa = xa;
	    this.ya = ya;
	    this.ea = ea;
	    this.eb = eb;
	    this.nx = nx;
	    this.ny = ny;
	}

	@Override
	public String toString() {
	    return String.format("S %d -> %d", ea, eb);
	}

	@Override
	public int compareTo(final Segment o) {
	    return (ea < o.ea) ? -1 : (ea == o.ea) ? 0 : 1;
	}
    }

    private static double[] edgeIntersection(final double xs, final double ys, final double zs, final double xt,
	    final double yt, final double zt, final double z) {
	if ((zs > z && zt > z) || (zs < z && zt < z))
	    return null;

	assert (zs != zt);
	final double t = (z - zs) / (zt - zs);
	final double x = xs + t * (xt - xs);
	final double y = ys + t * (yt - ys);
	return new double[] { x, y };
    }

    private static final double minZ(final Vertices vertices, final long v0, final long v1, final long v2,
	    final double eps) {
	final double z0 = mround(vertices.z(v0), eps, 2, 0);
	final double z1 = mround(vertices.z(v1), eps, 2, 0);
	final double z2 = mround(vertices.z(v2), eps, 2, 0);
	return Math.min(z0, Math.min(z1, z2));
    }

    private static final double maxZ(final Vertices vertices, final long v0, final long v1, final long v2,
	    final double eps) {
	final double z0 = mround(vertices.z(v0), eps, 2, 0);
	final double z1 = mround(vertices.z(v1), eps, 2, 0);
	final double z2 = mround(vertices.z(v2), eps, 2, 0);
	return Math.max(z0, Math.max(z1, z2));
    }
}
