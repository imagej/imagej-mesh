
package net.imagej.mesh;

import java.util.Iterator;

/**
 * Collection of vertices for a {@link Mesh}.
 *
 * @author Curtis Rueden
 */
public interface Vertices extends Iterable<Vertex> {

	/** The mesh to which the collection of vertices belongs. */
	Mesh mesh();

	/** Number of vertices in the collection. */
	long size();

	/** X position of a vertex, as a float. */
	float xf(long vIndex);

	/** Y position of a vertex, as a float. */
	float yf(long vIndex);

	/** Z position of a vertex, as a float. */
	float zf(long vIndex);

	/** X coordinate of vertex normal, as a float. */
	float nxf(long vIndex);

	/** Y coordinate of vertex normal, as a float. */
	float nyf(long vIndex);

	/** Z coordinate of vertex normal, as a float. */
	float nzf(long vIndex);

	/** U value of vertex texture coordinate, as a float. */
	float uf(long vIndex);

	/** V value of vertex texture coordinate, as a float. */
	float vf(long vIndex);

	/** W value of vertex texture coordinate, as a float. */
	float wf(long vIndex);

	/**
	 * Adds a vertex.
	 *
	 * @param x X position of the vertex.
	 * @param y Y position of the vertex.
	 * @param z Z position of the vertex.
	 * @param nx X coordinate of the vertex's normal.
	 * @param ny Y coordinate of the vertex's normal.
	 * @param nz Z coordinate of the vertex's normal.
	 * @param u U value of vertex texture coordinate.
	 * @param v V value of vertex texture coordinate.
	 * @param w W value of vertex texture coordinate.
	 * @return Index of newly added vertex.
	 */
	long addf(float x, float y, float z, //
		float nx, float ny, float nz, //
		float u, float v, float w);

	/**
	 * Overwrites a vertex's position, normal and texture coordinates.
	 *
	 * @param vIndex Index of vertex to overwrite.
	 * @param x X position of the vertex.
	 * @param y Y position of the vertex.
	 * @param z Z position of the vertex.
	 * @param nx X coordinate of the vertex's normal.
	 * @param ny Y coordinate of the vertex's normal.
	 * @param nz Z coordinate of the vertex's normal.
	 * @param u U value of vertex texture coordinate.
	 * @param v V value of vertex texture coordinate.
	 * @param w W value of vertex texture coordinate.
	 */
	void setf(long vIndex, float x, float y, float z, //
		float nx, float ny, float nz, //
		float u, float v, float w);

	/**
	 * Adds a vertex.
	 *
	 * @param x X position of the vertex.
	 * @param y Y position of the vertex.
	 * @param z Z position of the vertex.
	 * @return Index of newly added vertex.
	 */
	default long addf(final float x, final float y, final float z) {
		return addf(x, y, z, 0, 0, 0, 0, 0, 0);
	}

	/**
	 * Overwrites a vertex's position.
	 *
	 * @param vIndex Index of vertex to overwrite.
	 * @param x X position of the vertex.
	 * @param y Y position of the vertex.
	 * @param z Z position of the vertex.
	 */
	default void setf(final long vIndex, //
		final float x, final float y, final float z)
	{
		setf(vIndex, x, y, z, 0, 0, 0, 0, 0, 0);
	}

	/** X position of a vertex, as a double. */
	default double x(final long vIndex) {
		return xf(vIndex);
	}

	/** Y position of a vertex, as a double. */
	default double y(final long vIndex) {
		return yf(vIndex);
	}

	/** Z position of a vertex, as a double. */
	default double z(final long vIndex) {
		return zf(vIndex);
	}

	/** X coordinate of vertex normal, as a double. */
	default double nx(final long vIndex) {
		return nxf(vIndex);
	}

	/** Y coordinate of vertex normal, as a double. */
	default double ny(final long vIndex) {
		return nyf(vIndex);
	}

	/** Z coordinate of vertex normal, as a double. */
	default double nz(final long vIndex) {
		return nzf(vIndex);
	}

	/** U value of vertex texture coordinate, as a double. */
	default double u(long vIndex) {
		return uf(vIndex);
	}

	/** V value of vertex texture coordinate, as a double. */
	default double v(long vIndex) {
		return vf(vIndex);
	}

	/** W value of vertex texture coordinate, as a double. */
	default double w(long vIndex) {
		return wf(vIndex);
	}

	/**
	 * Adds a vertex.
	 *
	 * @param x X coordinate of the vertex.
	 * @param y Y coordinate of the vertex.
	 * @param z Z coordinate of the vertex.
	 * @return Index of newly added vertex.
	 */
	default long add(final double x, final double y, final double z) {
		return addf((float) x, (float) y, (float) z);
	}

	/**
	 * Overwrites the position of a vertex.
	 *
	 * @param vIndex Index of vertex to overwrite.
	 * @param x X position of the vertex.
	 * @param y Y position of the vertex.
	 * @param z Z position of the vertex.
	 */
	default void set(final long vIndex, final double x, final double y,
		final double z)
	{
		set(vIndex, x, y, z, 0, 0, 0, 0, 0, 0);
	}

	/**
	 * Overwrites a vertex's position, normal and texture coordinates.
	 *
	 * @param vIndex Index of vertex to overwrite.
	 * @param x X position of the vertex.
	 * @param y Y position of the vertex.
	 * @param z Z position of the vertex.
	 * @param nx X coordinate of the vertex's normal.
	 * @param ny Y coordinate of the vertex's normal.
	 * @param nz Z coordinate of the vertex's normal.
	 * @param u U value of vertex texture coordinate.
	 * @param v V value of vertex texture coordinate.
	 * @param w W value of vertex texture coordinate.
	 */
	default void set(final long vIndex, final double x, final double y,
		final double z, //
		final double nx, final double ny, final double nz, //
		final double u, final double v, final double w)
	{
		set(vIndex, (float) x, (float) y, (float) z, //
			(float) nx, (float) ny, (float) nz, //
			(float) u, (float) v, (float) w);
	}

	// -- Iterable methods --

	@Override
	default Iterator<Vertex> iterator() {
		return new Iterator<Vertex>() {

			private long index = -1;

			private Vertex vertex = new Vertex() {

				@Override
				public Mesh mesh() { return Vertices.this.mesh(); }

				@Override
				public long index() { return index; }
			};

			@Override
			public boolean hasNext() {
				return index + 1 < size();
			}

			@Override
			public Vertex next() {
				index++;
				return vertex;
			}
		};
	}
}
