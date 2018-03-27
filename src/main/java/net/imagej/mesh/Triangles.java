
package net.imagej.mesh;

import java.util.Iterator;

/**
 * Collection of triangles for a {@link Mesh}.
 *
 * @author Curtis Rueden
 */
public interface Triangles extends Iterable<Triangle> {

	/** The mesh to which the collection of triangles belongs. */
	Mesh mesh();

	/** Number of triangles in the collection. */
	long size();

	/** <strong>Index</strong> of first vertex in a triangle. */
	long vertex0(long tIndex);

	/** <strong>Index</strong> of second vertex in a triangle. */
	long vertex1(long tIndex);

	/** <strong>Index</strong> of third vertex in a triangle. */
	long vertex2(long tIndex);

	/** X coordinate of triangle's normal, as a float. */
	float nxf(long tIndex);

	/** Y coordinate of triangle's normal, as a float. */
	float nyf(long tIndex);

	/** Z coordinate of triangle's normal, as a float. */
	float nzf(long tIndex);

	/**
	 * Adds a triangle to the mesh's triangles list.
	 *
	 * @param v0 Index of triangle's first vertex.
	 * @param v1 Index of triangle's second vertex.
	 * @param v2 Index of triangle's third vertex.
	 * @param nx X coordinate of triangle's normal.
	 * @param ny Y coordinate of triangle's normal.
	 * @param nz Z coordinate of triangle's normal.
	 * @return Index of newly added triangle.
	 */
	long addf(long v0, long v1, long v2, float nx, float ny, float nz);

	/**
	 * Adds a triangle to the mesh's triangles list.
	 * <p>
	 * This is a convenience method that first creates the vertices using
	 * {@link Vertices#addf(float, float, float)}, then calls
	 * {@link #addf(long, long, long, float, float, float)}.
	 * </p>
	 *
	 * @param v0x X coordinate of triangle's first vertex.
	 * @param v0y Y coordinate of triangle's first vertex.
	 * @param v0z Z coordinate of triangle's first vertex.
	 * @param v1x X coordinate of triangle's second vertex.
	 * @param v1y Y coordinate of triangle's second vertex.
	 * @param v1z Z coordinate of triangle's second vertex.
	 * @param v2x X coordinate of triangle's third vertex.
	 * @param v2y Y coordinate of triangle's third vertex.
	 * @param v2z Z coordinate of triangle's third vertex.
	 * @param nx X coordinate of triangle's normal.
	 * @param ny Y coordinate of triangle's normal.
	 * @param nz Z coordinate of triangle's normal.
	 * @return Index of newly added triangle.
	 */
	default long addf(final float v0x, final float v0y, final float v0z, //
		final float v1x, final float v1y, final float v1z, //
		final float v2x, final float v2y, final float v2z, //
		final float nx, final float ny, final float nz)
	{
		final long v0 = mesh().vertices().add(v0x, v0y, v0z);
		final long v1 = mesh().vertices().add(v1x, v1y, v1z);
		final long v2 = mesh().vertices().add(v2x, v2y, v2z);
		return addf(v0, v1, v2, nx, ny, nz);
	}

	/** X coordinate of triangle's normal, as a double. */
	default double nx(final long tIndex) {
		return nxf(tIndex);
	}

	/** Y coordinate of triangle's normal, as a double. */
	default double ny(final long tIndex) {
		return nyf(tIndex);
	}

	/** Z coordinate of triangle's normal, as a double. */
	default double nz(final long tIndex) {
		return nzf(tIndex);
	}

	/**
	 * Adds a triangle to the mesh's triangles list.
	 *
	 * @param v0 Index of triangle's first vertex.
	 * @param v1 Index of triangle's second vertex.
	 * @param v2 Index of triangle's third vertex.
	 * @param nx X coordinate of triangle's normal.
	 * @param ny Y coordinate of triangle's normal.
	 * @param nz Z coordinate of triangle's normal.
	 * @return Index of newly added triangle.
	 */
	default long add(final long v0, final long v1, final long v2, //
		final double nx, final double ny, final double nz)
	{
		return addf(v0, v1, v2, (float) nx, (float) ny, (float) nz);
	}

	/**
	 * Adds a triangle to the mesh's triangles list.
	 * <p>
	 * This is a convenience method that first creates the vertices using
	 * {@link Vertices#add(double, double, double)}, then calls
	 * {@link #add(long, long, long, double, double, double)}.
	 * </p>
	 *
	 * @param v0x X coordinate of triangle's first vertex.
	 * @param v0y Y coordinate of triangle's first vertex.
	 * @param v0z Z coordinate of triangle's first vertex.
	 * @param v1x X coordinate of triangle's second vertex.
	 * @param v1y Y coordinate of triangle's second vertex.
	 * @param v1z Z coordinate of triangle's second vertex.
	 * @param v2x X coordinate of triangle's third vertex.
	 * @param v2y Y coordinate of triangle's third vertex.
	 * @param v2z Z coordinate of triangle's third vertex.
	 * @param nx X coordinate of triangle's normal.
	 * @param ny Y coordinate of triangle's normal.
	 * @param nz Z coordinate of triangle's normal.
	 * @return Index of newly added triangle.
	 */
	default long add(final double v0x, final double v0y, final double v0z, //
		final double v1x, final double v1y, final double v1z, //
		final double v2x, final double v2y, final double v2z, //
		final double nx, final double ny, final double nz)
	{
		final long v0 = mesh().vertices().add(v0x, v0y, v0z);
		final long v1 = mesh().vertices().add(v1x, v1y, v1z);
		final long v2 = mesh().vertices().add(v2x, v2y, v2z);
		return add(v0, v1, v2, nx, ny, nz);
	}

	// -- Iterable methods --

	@Override
	default Iterator<Triangle> iterator() {
		return new Iterator<Triangle>() {

			private long index = -1;

			private Triangle triangle = new Triangle() {

				@Override
				public Mesh mesh() { return Triangles.this.mesh(); }

				@Override
				public long index() { return index; }
			};

			@Override
			public boolean hasNext() {
				return index + 1 < size();
			}

			@Override
			public Triangle next() {
				index++;
				return triangle;
			}
		};
	}
}
