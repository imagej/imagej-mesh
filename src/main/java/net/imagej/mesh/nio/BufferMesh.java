
package net.imagej.mesh.nio;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Function;

import net.imagej.mesh.Mesh;

/**
 * Mesh implemented using {@link java.nio.Buffer} objects.
 *
 * @author Curtis Rueden
 */
public class BufferMesh implements Mesh {

	private final Vertices vertices;
	private final Triangles triangles;

	public BufferMesh(final int capacity) {
		this(capacity, true);
	}

	public BufferMesh(final int capacity, final boolean direct) {
		this(capacity, direct ? ByteBuffer::allocateDirect : ByteBuffer::allocate);
	}

	public BufferMesh(final int capacity,
		final Function<Integer, ByteBuffer> creator)
	{
		final FloatBuffer verts = creator.apply(capacity * 3).asFloatBuffer();
		final FloatBuffer vNormals = creator.apply(capacity * 3).asFloatBuffer();
		final FloatBuffer texCoords = creator.apply(capacity * 2).asFloatBuffer();
		vertices = new Vertices(verts, vNormals, texCoords);
		final IntBuffer indices = creator.apply(capacity * 3).asIntBuffer();
		final FloatBuffer tNormals = creator.apply(capacity * 3).asFloatBuffer();
		triangles = new Triangles(indices, tNormals);
	}

	@Override
	public Vertices vertices() {
		return vertices;
	}

	@Override
	public Triangles triangles() {
		return triangles;
	}

	// -- Inner classes --

	public class Vertices implements net.imagej.mesh.Vertices {

		private FloatBuffer verts;
		private FloatBuffer normals;
		private FloatBuffer texCoords;

		public Vertices(final FloatBuffer verts, final FloatBuffer normals,
			final FloatBuffer texCoords)
		{
			this.verts = verts;
			this.normals = normals;
			this.texCoords = texCoords;
		}

		public FloatBuffer verts() {
			return verts;
		}

		public FloatBuffer normals() {
			return normals;
		}

		public FloatBuffer texCoords() {
			return texCoords;
		}

		@Override
		public Mesh mesh() {
			return BufferMesh.this;
		}

		@Override
		public long size() {
			return verts.position() / 3;
		}

		@Override
		public float xf(long vIndex) {
			return verts.get(safeIndex(vIndex, 3, 0));
		}

		@Override
		public float yf(long vIndex) {
			return verts.get(safeIndex(vIndex, 3, 1));
		}

		@Override
		public float zf(long vIndex) {
			return verts.get(safeIndex(vIndex, 3, 2));
		}

		@Override
		public float nxf(long vIndex) {
			return normals.get(safeIndex(vIndex, 3, 0));
		}

		@Override
		public float nyf(long vIndex) {
			return normals.get(safeIndex(vIndex, 3, 1));
		}

		@Override
		public float nzf(long vIndex) {
			return normals.get(safeIndex(vIndex, 3, 2));
		}

		@Override
		public float uf(long vIndex) {
			return texCoords.get(safeIndex(vIndex, 2, 0));
		}

		@Override
		public float vf(long vIndex) {
			return texCoords.get(safeIndex(vIndex, 2, 1));
		}

		@Override
		public float wf(long vIndex) {
			return Float.NaN;
		}

		@Override
		public long addf(float x, float y, float z, float nx, float ny, float nz,
			float u, float v, float w)
		{
			final long index = size();
			verts.put(x);
			verts.put(y);
			verts.put(z);
			normals.put(nx);
			normals.put(ny);
			normals.put(nz);
			texCoords.put(u);
			texCoords.put(v);
			// NB: w is ignored!
			return index;
		}

		@Override
		public void setf(long vIndex, float x, float y, float z, //
			float nx, float ny, float nz, //
			float u, float v, float w)
		{
			final int ix = safeIndex(vIndex, 3, 0);
			final int iy = safeIndex(vIndex, 3, 1);
			final int iz = safeIndex(vIndex, 3, 2);
			verts.put(ix, x);
			verts.put(iy, y);
			verts.put(iz, z);
			normals.put(ix, nx);
			normals.put(iy, ny);
			normals.put(iz, nz);
			texCoords.put(safeIndex(vIndex, 2, 0), u);
			texCoords.put(safeIndex(vIndex, 2, 1), v);
			// NB: w is ignored!
		}
	}

	public class Triangles implements net.imagej.mesh.Triangles {

		private IntBuffer indices;
		private FloatBuffer normals;

		public Triangles(final IntBuffer indices, final FloatBuffer normals) {
			this.indices = indices;
			this.normals = normals;
		}

		public IntBuffer indices() {
			return indices;
		}

		public FloatBuffer normals() {
			return normals;
		}

		@Override
		public Mesh mesh() {
			return BufferMesh.this;
		}

		@Override
		public long size() {
			return indices.position() / 3;
		}

		@Override
		public long vertex0(long tIndex) {
			return indices.get(safeIndex(tIndex, 3, 0));
		}

		@Override
		public long vertex1(long tIndex) {
			return indices.get(safeIndex(tIndex, 3, 1));
		}

		@Override
		public long vertex2(long tIndex) {
			return indices.get(safeIndex(tIndex, 3, 2));
		}

		@Override
		public float nxf(long tIndex) {
			return normals.get(safeIndex(tIndex, 3, 0));
		}

		@Override
		public float nyf(long tIndex) {
			return normals.get(safeIndex(tIndex, 3, 1));
		}

		@Override
		public float nzf(long tIndex) {
			return normals.get(safeIndex(tIndex, 3, 2));
		}

		@Override
		public long addf(long v0, long v1, long v2, float nx, float ny, float nz) {
			final long index = size();
			indices.put(safeInt(v0));
			indices.put(safeInt(v1));
			indices.put(safeInt(v2));
			normals.put(nx);
			normals.put(ny);
			normals.put(nz);
			return index;
		}
	}

	private int safeIndex(final long index, final int span, final int offset) {
		return safeInt(span * index + offset);
	}

	private int safeInt(final long value) {
		if (value > Integer.MAX_VALUE) {
			throw new IndexOutOfBoundsException("Value too large: " + value);
		}
		return (int) value;
	}
}
