package net.imagej.refmesh;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.imagej.mesh.Mesh;
import net.imagej.mesh.Triangles;
import net.imagej.mesh.Vertices;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.mastodon.Options;
import org.mastodon.Ref;
import org.mastodon.RefPool;

public class RefMesh {

	private final Mesh mesh;

	private final VertexPool vertexPool;

	private final TrianglePool trianglePool;

	public RefMesh(final Mesh mesh) {
		this.mesh = mesh;
		vertexPool = new VertexPool(this);
		trianglePool = new TrianglePool(this);
	}

	public VertexPool vertices() {
		return vertexPool;
	}

	public TrianglePool edges() {
		return trianglePool;
	}

	/*
	 * =======================================
	 * TRIANGLES
	 * =======================================
	 */

	public static class TrianglePool
		extends AbstractCollection< TriangleRef >
		implements RefPool< TriangleRef >
	{
		private final Triangles triangles;

		private final VertexPool vertexPool;

		private final ConcurrentLinkedQueue< TriangleRef > tmpObjRefs;

		TrianglePool(final RefMesh mesh) {
			triangles = mesh.mesh.triangles();
			vertexPool = mesh.vertexPool;
			tmpObjRefs = new ConcurrentLinkedQueue<>();
		}

		@Override
		public TriangleRef createRef() {
			final TriangleRef ref = tmpObjRefs.poll();
			return ref == null ? new TriangleRef() : ref;
		}

		@Override
		public void releaseRef(final TriangleRef obj) {
			tmpObjRefs.add(obj);
		}

		@Override
		public TriangleRef getObject(final int id, final TriangleRef obj) {
			if (Options.DEBUG && (id < 0 || id >= triangles.size()))
				throw new NoSuchElementException("index=" + id);

			obj.updateAccess(this, id);
			return obj;
		}

		@Override
		public TriangleRef getObjectIfExists(final int id,
			final TriangleRef obj)
		{
			if (id < 0 || id >= triangles.size()) return null;

			obj.updateAccess(this, id);
			return obj;
		}

		@Override
		public int getId(final TriangleRef o) {
			return o.getInternalPoolIndex();
		}

		@Override
		public Class< TriangleRef > getRefClass() {
			return TriangleRef.class;
		}

		@Override
		public Iterator< TriangleRef > iterator() {
			return new Iter();
		}

		@Override
		public int size() {
			return safeInt(triangles.size());
		}

		private class Iter implements Iterator< TriangleRef >
		{
			private final TriangleRef ref = createRef();

			private int i = 0;

			@Override
			public boolean hasNext() {
				return i < size();
			}

			@Override
			public TriangleRef next() {
				return getObject(i++, ref);
			}
		}
	}

	public static class TriangleRef implements Ref< TriangleRef >, Iterable< VertexRef > {

		/**
		 * Current index in current triangles array.
		 */
		private int index;

		/**
		 * The pool into which this proxy currently refers.
		 */
		private TrianglePool pool;

		/**
		 * Make this proxy refer the element at the specified
		 * {@code index} in the specified {@code triangles}.
		 *
		 * @param pool
		 * @param index
		 */
		void updateAccess(final TrianglePool pool, final int index)
		{
			if (this.pool != pool) this.pool = pool;
			this.index = index;
		}

		@Override
		public int getInternalPoolIndex() {
			return index;
		}

		@Override
		public TriangleRef refTo(final TriangleRef obj) {
			updateAccess(obj.pool, obj.index);
			return this;
		}

		public VertexRef vertex0()
		{
			return vertex(0);
		}

		public VertexRef vertex0(final VertexRef ref)
		{
			return vertex(0, ref);
		}

		public VertexRef vertex1()
		{
			return vertex(1);
		}

		public VertexRef vertex1(final VertexRef ref)
		{
			return vertex(1, ref);
		}

		public VertexRef vertex2()
		{
			return vertex(2);
		}

		public VertexRef vertex2(final VertexRef ref)
		{
			return vertex(2, ref);
		}

		/**
		 * @param vi 0, 1, or2
		 */
		private VertexRef vertex(final int vi)
		{
			return vertex(vi, pool.vertexPool.createRef());
		}

		/**
		 * @param vi 0, 1, or2
		 */
		private VertexRef vertex(final int vi, final VertexRef ref)
		{
			if (vi == 0) {
				ref.updateAccess(pool.vertexPool,
					safeInt(pool.triangles.vertex0(index)));
			}
			else if (vi == 1) {
				ref.updateAccess(pool.vertexPool,
					safeInt(pool.triangles.vertex1(index)));
			}
			else {
				ref.updateAccess(pool.vertexPool,
					safeInt(pool.triangles.vertex2(index)));
			}
			return ref;
		}

		private Iter iterator;

		@Override
		public Iterator< VertexRef > iterator() {
			if ( iterator == null )
				iterator = new Iter();
			else
				iterator.reset();
			return iterator;
		}

		public Iterator< VertexRef > safe_iterator()
		{
			return new Iter();
		}

		private class Iter implements Iterator< VertexRef > {

			private VertexRef ref = pool.vertexPool.createRef();
			private int i = 0;

			void reset() {
				int i = 0;
			}

			@Override
			public boolean hasNext() {
				return i < 3;
			}

			@Override
			public VertexRef next() {
				return vertex(i++, ref);
			}
		}
	}

	/*
	 * =======================================
	 * VERTICES
	 * =======================================
	 */

	public static class VertexPool
		extends AbstractCollection< VertexRef >
		implements RefPool< VertexRef >
	{

		private final Vertices vertices;

		private final ConcurrentLinkedQueue< VertexRef > tmpObjRefs;

		VertexPool(final RefMesh mesh) {
			vertices = mesh.mesh.vertices();
			tmpObjRefs = new ConcurrentLinkedQueue<>();
		}

		@Override
		public VertexRef createRef() {
			final VertexRef ref = tmpObjRefs.poll();
			return ref == null ? new VertexRef() : ref;
		}

		@Override
		public void releaseRef(final VertexRef obj) {
			tmpObjRefs.add(obj);
		}

		@Override
		public VertexRef getObject(final int id, final VertexRef obj) {
			if (Options.DEBUG && (id < 0 || id >= vertices.size()))
				throw new NoSuchElementException("index=" + id);

			obj.updateAccess(this, id);
			return obj;
		}

		@Override
		public VertexRef getObjectIfExists(final int id, final VertexRef obj) {
			if (id < 0 || id >= vertices.size()) return null;

			obj.updateAccess(this, id);
			return obj;
		}

		@Override
		public int getId(final VertexRef o) {
			return o.getInternalPoolIndex();
		}

		@Override
		public Class< VertexRef > getRefClass() {
			return VertexRef.class;
		}

		@Override
		public Iterator< VertexRef > iterator() {
			return new Iter();
		}

		@Override
		public int size() {
			return safeInt(vertices.size());
		}

		private class Iter implements Iterator< VertexRef >
		{
			private final VertexRef ref = createRef();

			private int i = 0;

			@Override
			public boolean hasNext() {
				return i < size();
			}

			@Override
			public VertexRef next() {
				return getObject(i++, ref);
			}
		}
	}

	public static class VertexRef implements Ref< VertexRef > {

		/**
		 * Current index in current vertices array.
		 */
		private int index;

		/**
		 * The pool into which this proxy currently refers.
		 */
		private VertexPool pool;

		/**
		 * Make this proxy refer the element at the specified
		 * {@code index} in the specified {@code pool}.
		 *
		 * @param pool
		 * @param index
		 */
		void updateAccess(final VertexPool pool, final int index)
		{
			if (this.pool != pool) this.pool = pool;
			this.index = index;
		}

		@Override
		public int getInternalPoolIndex() {
			return index;
		}

		@Override
		public VertexRef refTo(final VertexRef obj) {
			updateAccess(obj.pool, obj.index);
			return this;
		}

		public Vector3f getPosition() {
			return getPosition(new Vector3f());
		}

		public Vector3f getPosition(final Vector3f dest) {
			final Vertices vs = pool.vertices;
			return dest.set(vs.xf(index), vs.yf(index),
				vs.zf(index));
		}

		public void setPosition(final Vector3fc position) {
			pool.vertices
				.setPositionf(index, position.x(), position.y(), position.z());
		}

		public Vector3f getNormal() {
			return getNormal(new Vector3f());
		}

		public Vector3f getNormal(final Vector3f dest) {
			final Vertices vs = pool.vertices;
			return dest.set(vs.nxf(index), vs.nyf(index),
				vs.nzf(index));
		}

		public void setNormal(final Vector3fc normal) {
			pool.vertices
				.setNormalf(index, normal.x(), normal.y(), normal.z());
		}

		public Vector2f getTexture() {
			return getTexture(new Vector2f());
		}

		public Vector2f getTexture(final Vector2f dest) {
			final Vertices vs = pool.vertices;
			return dest.set(vs.uf(index), vs.vf(index));
		}

		public void setTexture(final Vector2fc tex) {
			pool.vertices.setTexturef(index, tex.x(), tex.y());
		}
	}

	/*
	 * =======================================
	 * HELPERS
	 * =======================================
	 */

	private static int safeInt(final long index) {
		if (index > Integer.MAX_VALUE) {
			throw new IndexOutOfBoundsException("Index too large: " + index);
		}
		return (int) index;
	}
}
