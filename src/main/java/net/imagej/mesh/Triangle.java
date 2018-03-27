
package net.imagej.mesh;

/**
 * One triangle of a {@link Triangles} collection.
 *
 * @author Curtis Rueden
 * @see Triangles
 */
public interface Triangle {

	/**
	 * The mesh to which the triangle belongs.
	 * 
	 * @see Mesh#triangles()
	 */
	Mesh mesh();

	/** Index into the mesh's list of triangles. */
	long index();

	/** <strong>Index</strong> of first vertex in the triangle. */
	default long vertex0() {
		return mesh().triangles().vertex0(index());
	}

	/** <strong>Index</strong> of second vertex in the triangle. */
	default long vertex1() {
		return mesh().triangles().vertex1(index());
	}

	/** <strong>Index</strong> of third vertex in the triangle. */
	default long vertex2() {
		return mesh().triangles().vertex2(index());
	}

	/** X coordinate of triangle's normal, as a float. */
	default float nxf() {
		return mesh().triangles().nxf(index());
	}

	/** Y coordinate of triangle's normal, as a float. */
	default float nyf() {
		return mesh().triangles().nyf(index());
	}

	/** Z coordinate of triangle's normal, as a float. */
	default float nzf() {
		return mesh().triangles().nzf(index());
	}

	/** X coordinate of triangle's normal, as a double. */
	default double nx() {
		return mesh().triangles().nx(index());
	}

	/** Y coordinate of triangle's normal, as a double. */
	default double ny() {
		return mesh().triangles().ny(index());
	}

	/** Z coordinate of triangle's normal, as a double. */
	default double nz() {
		return mesh().triangles().nz(index());
	}
}
