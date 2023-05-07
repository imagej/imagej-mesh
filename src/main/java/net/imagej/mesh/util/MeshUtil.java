package net.imagej.mesh.util;

public class MeshUtil {

    /**
     * Dot product between vectors V1 and V2.
     *
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @return
     */
    public static final double dotProduct(final double x1, final double y1, final double z1, final double x2,
	    final double y2, final double z2) {
	return x1 * x2 + y1 * y2 + z1 * z2;
    }

    /**
     * Cross product between vectors V1 and V2. Stores the results in the specified
     * <code>double</code> array.
     *
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @param out a <code>double</code> array of size at least 3 to store the
     *            coordinates of resulting vector.
     */
    public static final void cross(final double x1, final double y1, final double z1, final double x2, final double y2,
	    final double z2, final double[] out) {
	out[0] = y1 * z2 - z1 * y2;
	out[1] = -x1 * z2 + z1 * x2;
	out[2] = x1 * y2 - y1 * x2;
    }

    /**
     * Builds a unique id for an edge between the two specified vertices. Such an
     * edge is undirected; the edge v1-&gt;v2 and v2-&gt;v1 have the the same id.
     *
     * @param v1 the id of the first vertex.
     * @param v2 the id of the second vertex.
     * @return the edge id, as a <code>long</code>.
     */
    public static final long edgeID(final int v1, final int v2) {
	return (v1 < v2) ? concat(v1, v2) : concat(v2, v1);
    }

    private static final long concat(final int i1, final int i2) {
	final long l = (((long) i1) << 32) | (i2 & 0xffffffffL);
	return l;
    }

    /**
     * Returns the id of the first vertex of the specified edge.
     *
     * @param l the edge id.
     * @return the first vertex id, as an <code>int</code>.
     */
    public static final int edgeV1(final long l) {
	final int i1 = (int) (l >> 32);
	return i1;
    }

    /**
     * Returns the id of the second vertex of the specified edge.
     *
     * @param l the edge id.
     * @return the second vertex id, as an <code>int</code>.
     */
    public static final int edgeV2(final long l) {
	final int i2 = (int) l;
	return i2;
    }

    public static final String edgeStr(final long l) {
	return String.format("E: (%d -> %d)", edgeV1(l), edgeV2(l));
    }
}
