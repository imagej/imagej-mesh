package net.imagej.mesh;

/**
 * Collection of edges for a {@link Mesh}.
 *
 * @author Jean-Yves Tinevez
 */
public interface Edges
{

    /**
     * The mesh to which the collection of edges belongs.
     */
    Mesh mesh();

    /**
     * Number of edges in the collection.
     */
    long size();

    /**
     * <strong>Index</strong> of first vertex in an edge. Edges are stored such that
     * <code>v0 < v1</code> always.
     */
    long v0(long i);

    /**
     * <strong>Index</strong> of second vertex in an edge. Edges are stored such
     * that <code>v0 < v1</code> always.
     */
    long v1(long i);

    /**
     * <strong>Index</strong> of the first triangle adjacent to edge with this
     * index. A value of -1 indicates that the edge does not have adjacent faces.
     */
    long f0(long i);

    /**
     * <strong>Index</strong> of the second triangle adjacent to edge with this
     * index. A value of -1 indicates that the edge does not have a second adjacent
     * face.
     */
    long f1(long i);

    /**
     * Adds an edge to this collection.
     *
     * @param v0 the index of the first vertex of this edge. Must be such that
     *           <code>v0 < v1</code> always.
     * @param v1 the index of the second vertex of this edge. Must be such that
     *           <code>v0 < v1</code> always.
     * @param f0 the index of the first face adjacent to this edge. Use -1 if this
     *           edge does not have adjacent faces.
     * @param f1 the index of the second face adjacent to this edge. Use -1 if this
     *           edge has only one adjacent face.
     * @return index of newly added edge.
     */
    long add(long v0, long v1, long f0, long f1);

    /**
     * Returns the index of the edge made by the vertices with the specified
     * indices, -1 if it is not present in this collection.
     *
     * @param v0 the index of the first vertex of the edge. Must be such that
     *           <code>v0 < v1</code>.
     * @param v1 the index of the second vertex of the edge. Must be such that
     *           <code>v0 < v1</code>.
     * @return the index of the edge made by the vertices with the specified
     *         indices, -1 if it is not present in this collection.
     */
    long indexOf(long v0, long v1);

}
