
package net.imagej.mesh;

/*
 * -- Vertices --
 *
 * Mastodon
 * - net.imagej.mesh.Vertex3 concrete class
 * - vertices are one float array.
 * - can do multiarrays for big data.
 * - might be useful for very large meshes
 *
 * JOML
 * - org.joml.Vector3f concrete class, Vector3fc read-only iface
 * - works with OpenGL and Vulkan
 * - has good geometry operations
 * - Externalizable
 * - fastest in general
 *
 * ClearGL
 * - cleargl.GLVector concrete class
 * - Serializable
 * - "not the thing that should be done in the future"
 *  -- JOML instead
 *
 * ImageJ Ops
 * - net.imagej.ops.geom.geom3d.mesh.Vector3D
 *  -- extends org.apache.commons.math3.geometry.euclidean.thread.Vector3D
 *  -- which implements org.apache.commons.math3.geometry.Vector
 * - needs reconciliation with imagej-mesh
 *
 * -- Mesh --
 *
 * scenery's mesh
 * - uses java.nio.FloatBuffer offheap (see HasGeometry):
 *  -- vertices
 *  -- normals
 *  -- texcoords
 *  -- indices (IntBuffer)
 *
 * ImageJ Mesh
 * - net.imagej.mesh.Mesh interface
 * - One impl backed by JOML object(s)
 * -
 */

/**
 * <a href="https://en.wikipedia.org/wiki/Polygon_mesh">3D mesh</a> data
 * structure consisting of triangles and their vertices.
 * <p>
 * Vertices may be shared by multiple triangles. Coordinates can be retrieved as
 * {@code float} or {@code double} values.
 * </p>
 *
 * @author Curtis Rueden
 */
public interface Mesh {

	/** The mesh's collection of vertices. */
	Vertices vertices();

	/** The mesh's collection of triangles. */
	Triangles triangles();
}
