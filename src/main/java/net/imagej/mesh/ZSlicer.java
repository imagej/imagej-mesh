package net.imagej.mesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TLongArrayList;
import net.imagej.mesh.util.MeshUtil;

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

    private static final double EPS = 4e-4;

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
    public static List<Contour> slice(final Mesh mesh, final double z, final double zScale) {
	// Slice plane to odd multiples of eps.
	final double eps = EPS * zScale;
	final double zr = mround(z, eps, 2, 1);

	final Triangles triangles = mesh.triangles();
	final Vertices vertices = mesh.vertices();

	final TLongArrayList intersecting = new TLongArrayList();
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

	    intersecting.add(f);
	}

	// Deal with intersecting triangle.
	final LinkedList<Segment> segments = new LinkedList<>();
	for (int i = 0; i < intersecting.size(); i++) {
	    final long id = intersecting.getQuick(i);
	    final Segment segment = triangleIntersection(mesh, id, zr, eps);
	    if (segment != null)
		segments.add(segment);
	}

	// Sort segments by first edge.
	Collections.sort(segments);

	// Build contours from segments.
	final List<Contour> contours = new ArrayList<>();
	while (!segments.isEmpty()) {
	    final Segment start = segments.pop();
	    if (segments.isEmpty())
		break;

	    final Contour contour = Contour.init(start);
	    while (contour.grow(segments) && !contour.isClosed()) {
	    }

	    if (contour.size() < 3)
		continue;

	    contour.computeOrientation();
	    contours.add(contour);
	}
	return contours;
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

    private static final double mround(final double v, final double eps, final int mod, final int rem) {
	final long y = Math.round(v / (mod * eps));
	final double z = (y * mod + rem) * eps;
	return z;
    }

    private static final class Segment implements Comparable<Segment> {

	private final long ea;

	private final long eb;

	private final double xa;

	private final double ya;

	/** Normal at that segment. */
	private final double nx;

	/** Normal at that segment. */
	private final double ny;

	private Segment(final double xa, final double ya, final long ea, final long eb, final double nx,
		final double ny) {
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

    public static final class Contour {

	private final TDoubleArrayList x = new TDoubleArrayList();

	private final TDoubleArrayList y = new TDoubleArrayList();

	/** Normals. */
	private final TDoubleArrayList nx = new TDoubleArrayList();

	/** Normals. */
	private final TDoubleArrayList ny = new TDoubleArrayList();

	private final TLongArrayList es = new TLongArrayList();

	private boolean isClosed;

	private boolean isInterior;

	/** Used to search the corresponding segment. */
	private Segment match;

	private final long endEdge;

	private Contour(final Segment start) {
	    x.add(start.xa);
	    y.add(start.ya);
	    nx.add(start.nx);
	    ny.add(start.ny);
	    isClosed = false;
	    match = new Segment(Double.NaN, Double.NaN, start.eb, -1, Double.NaN, Double.NaN);
	    endEdge = start.ea;
	    es.add(start.ea);
	}

	/**
	 * Grows this contour by finding one segment from the specified list that
	 * connect to its head. Sets {@link #isClosed()} to <code>true</code> when the
	 * segment found connects to its tail.
	 *
	 * @param segments the list of segments to grow from. At most one is removed
	 *                 from this list.
	 * @return <code>true</code> if one segment was added to this contour.
	 */
	private boolean grow(final LinkedList<Segment> segments) {
	    final int i = Collections.binarySearch(segments, match);
	    if (i < 0)
		return false; // Did not find a match;

	    final Segment segment = segments.remove(i);
	    es.add(segment.ea);
	    x.add(segment.xa);
	    y.add(segment.ya);
	    nx.add(segment.nx);
	    ny.add(segment.ny);
	    match = new Segment(Double.NaN, Double.NaN, segment.eb, -1, Double.NaN, Double.NaN);
	    if (segment.eb == endEdge)
		isClosed = true;
	    return true;
	}

	public boolean isClosed() {
	    return isClosed;
	}

	public boolean isInterior() {
	    return isInterior;
	}

	/**
	 * Determine whether this contour surrounds the interior of the mesh or the
	 * exterior. Sets the {@link #isInterior} field.
	 */
	private void computeOrientation() {
	    // Leftmost vertex
	    double minx = x.getQuick(0);
	    int k = 0;
	    for (int i = 1; i < x.size(); i++) {
		final double xl = x.getQuick(i);
		if (xl < minx) {
		    minx = xl;
		    k = i;
		}
	    }

	    // Normal is facing where?
	    isInterior = nx.getQuick(k) < 0;
	}

	public double centerX() {
	    double sum = 0.;
	    for (int i = 0; i < x.size(); i++)
		sum += x.getQuick(i);
	    return sum / x.size();
	}

	public double centerY() {
	    double sum = 0.;
	    for (int i = 0; i < y.size(); i++)
		sum += y.getQuick(i);
	    return sum / y.size();
	}

	private static Contour init(final Segment start) {
	    return new Contour(start);
	}

	public int size() {
	    return x.size();
	}

	public double x(final int i) {
	    return x.getQuick(i);
	}

	public double y(final int i) {
	    return y.getQuick(i);
	}

	@Override
	public String toString() {
	    final StringBuilder str = new StringBuilder(super.toString());
	    str.append(String.format("\n%d vertices, is closed: %s, is interior: %s", x.size(), isClosed, isInterior));
	    return str.toString();
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
