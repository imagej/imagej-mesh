
package net.imagej.mesh;

import net.imglib2.RealLocalizable;

/**
 * One vertex of a {@link Vertices} collection.
 *
 * @author Curtis Rueden
 * @see Vertices
 */
public interface Vertex extends RealLocalizable {

	/**
	 * The mesh to which the vertex belongs.
	 * 
	 * @see Mesh#vertices()
	 */
	Mesh mesh();

	/** Index into the mesh's list of vertices. */
	long index();

	/** X position of vertex, as a float. */
	default float xf() {
		return mesh().vertices().xf(index());
	}

	/** Y position of vertex, as a float. */
	default float yf() {
		return mesh().vertices().yf(index());
	}

	/** Z position of vertex, as a float. */
	default float zf() {
		return mesh().vertices().zf(index());
	}

	/** X coordinate of vertex normal, as a float. */
	default float nxf() {
		return mesh().vertices().nxf(index());
	}

	/** Y coordinate of vertex normal, as a float. */
	default float nyf() {
		return mesh().vertices().nyf(index());
	}

	/** Z coordinate of vertex normal, as a float. */
	default float nzf() {
		return mesh().vertices().nzf(index());
	}

	/** U value of vertex texture coordinate, as a float. */
	default float uf() {
		return mesh().vertices().uf(index());
	}

	/** V value of vertex texture coordinate, as a float. */
	default float vf() {
		return mesh().vertices().vf(index());
	}

	/** W value of vertex texture coordinate, as a float. */
	default float wf() {
		return mesh().vertices().wf(index());
	}

	/** X position of vertex, as a double. */
	default double x() {
		return mesh().vertices().x(index());
	}

	/** Y position of vertex, as a double. */
	default double y() {
		return mesh().vertices().y(index());
	}

	/** Z position of vertex, as a double. */
	default double z() {
		return mesh().vertices().z(index());
	}

	/** X coordinate of vertex normal, as a double. */
	default double nx() {
		return mesh().vertices().nx(index());
	}

	/** Y coordinate of vertex normal, as a double. */
	default double ny() {
		return mesh().vertices().ny(index());
	}

	/** Z coordinate of vertex normal, as a double. */
	default double nz() {
		return mesh().vertices().nz(index());
	}

	/** U value of vertex texture coordinate, as a double. */
	default double u() {
		return mesh().vertices().u(index());
	}

	/** V value of vertex texture coordinate, as a double. */
	default double v() {
		return mesh().vertices().v(index());
	}

	/** W value of vertex texture coordinate, as a double. */
	default double w() {
		return mesh().vertices().w(index());
	}

	// -- RealLocalizable methods --

	@Override
	default void localize(final float[] position) {
		position[0] = xf();
		position[1] = yf();
		position[2] = zf();
	}

	@Override
	default void localize(final double[] position) {
		position[0] = x();
		position[1] = y();
		position[2] = z();
	}

	@Override
	default float getFloatPosition(final int d) {
		switch (d) {
			case 0:
				return xf();
			case 1:
				return yf();
			case 2:
				return zf();
		}
		throw new IndexOutOfBoundsException("" + d);
	}

	@Override
	default double getDoublePosition(final int d) {
		switch (d) {
			case 0:
				return x();
			case 1:
				return y();
			case 2:
				return z();
		}
		throw new IndexOutOfBoundsException("" + d);
	}

	// -- EuclideanSpace methods --

	@Override
	default int numDimensions() {
		return 3;
	}
}
