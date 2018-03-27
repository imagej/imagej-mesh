
package net.imagej.mesh.naive;

import net.imagej.mesh.Mesh;

import org.scijava.util.FloatArray;
import org.scijava.util.IntArray;

public class NaiveMesh implements Mesh {

	private final Vertices vertices;
	private final Triangles triangles;

	public NaiveMesh() {
		vertices = new Vertices();
		triangles = new Triangles();
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

		private final FloatArray xs, ys, zs;
		private final FloatArray nxs, nys, nzs;
		private final FloatArray us, vs, ws;

		public Vertices() {
			xs = new FloatArray();
			ys = new FloatArray();
			zs = new FloatArray();
			nxs = new FloatArray();
			nys = new FloatArray();
			nzs = new FloatArray();
			us = new FloatArray();
			vs = new FloatArray();
			ws = new FloatArray();
		}

		@Override
		public Mesh mesh() {
			return NaiveMesh.this;
		}

		@Override
		public long size() {
			return xs.size();
		}

		@Override
		public float xf(long vIndex) {
			return xs.get(safeIndex(vIndex));
		}

		@Override
		public float yf(long vIndex) {
			return ys.get(safeIndex(vIndex));
		}

		@Override
		public float zf(long vIndex) {
			return zs.get(safeIndex(vIndex));
		}

		@Override
		public float nxf(long vIndex) {
			return nxs.get(safeIndex(vIndex));
		}

		@Override
		public float nyf(long vIndex) {
			return nys.get(safeIndex(vIndex));
		}

		@Override
		public float nzf(long vIndex) {
			return nzs.get(safeIndex(vIndex));
		}

		@Override
		public float uf(long vIndex) {
			return us.get(safeIndex(vIndex));
		}

		@Override
		public float vf(long vIndex) {
			return vs.get(safeIndex(vIndex));
		}

		@Override
		public float wf(long vIndex) {
			return ws.get(safeIndex(vIndex));
		}

		@Override
		public long addf(float x, float y, float z, float nx, float ny, float nz,
			float u, float v, float w)
		{
			final int index = xs.size();
			xs.add(x);
			ys.add(y);
			zs.add(z);
			nxs.add(nx);
			nys.add(ny);
			nzs.add(nz);
			us.add(u);
			vs.add(v);
			ws.add(w);
			return index;
		}

		@Override
		public void setf(long vIndex, float x, float y, float z, float nx, float ny,
			float nz, float u, float v, float w)
		{
			final int index = safeIndex(vIndex);
			xs.set(index, x);
			ys.set(index, y);
			zs.set(index, z);
			nxs.set(index, nx);
			nys.set(index, ny);
			nzs.set(index, nz);
			us.set(index, u);
			vs.set(index, v);
			ws.set(index, w);
		}

		private int safeIndex(final long index) {
			if (index > Integer.MAX_VALUE) {
				throw new IndexOutOfBoundsException("Index too large: " + index);
			}
			return (int) index;
		}
	}

	public class Triangles implements net.imagej.mesh.Triangles {

		private final IntArray v0s, v1s, v2s;
		private final FloatArray nxs, nys, nzs;

		public Triangles() {
			v0s = new IntArray();
			v1s = new IntArray();
			v2s = new IntArray();
			nxs = new FloatArray();
			nys = new FloatArray();
			nzs = new FloatArray();
		}

		@Override
		public Mesh mesh() {
			return NaiveMesh.this;
		}

		@Override
		public long size() {
			return v1s.size();
		}

		@Override
		public long vertex0(long tIndex) {
			return v0s.get(safeIndex(tIndex));
		}

		@Override
		public long vertex1(long tIndex) {
			return v1s.get(safeIndex(tIndex));
		}

		@Override
		public long vertex2(long tIndex) {
			return v2s.get(safeIndex(tIndex));
		}

		@Override
		public float nxf(long tIndex) {
			return nxs.get(safeIndex(tIndex));
		}

		@Override
		public float nyf(long tIndex) {
			return nys.get(safeIndex(tIndex));
		}

		@Override
		public float nzf(long tIndex) {
			return nzs.get(safeIndex(tIndex));
		}

		@Override
		public long addf(long v0, long v1, long v2, float nx, float ny, float nz) {
			final int index = v0s.size();
			v0s.add(safeIndex(v0));
			v1s.add(safeIndex(v1));
			v2s.add(safeIndex(v2));
			nxs.add(nx);
			nys.add(ny);
			nzs.add(nz);
			return index;
		}

		private int safeIndex(final long index) {
			if (index > Integer.MAX_VALUE) {
				throw new IndexOutOfBoundsException("Index too large: " + index);
			}
			return (int) index;
		}
	}
}
