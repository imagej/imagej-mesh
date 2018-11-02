package net.imagej.refmesh;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.imagej.mesh.Triangles;
import org.joml.Vector3f;
import org.mastodon.Options;
import org.mastodon.RefPool;

import static net.imagej.refmesh.RefMesh.safeInt;

public class TrianglePool implements RefPool< TriangleRef >, Iterable< TriangleRef >
{
	final Triangles triangles;

	final VertexPool vertexPool;

	private final ConcurrentLinkedQueue< TriangleRef > tmpObjRefs;

	TrianglePool( final RefMesh mesh )
	{
		triangles = mesh.mesh.triangles();
		vertexPool = mesh.vertexPool;
		tmpObjRefs = new ConcurrentLinkedQueue<>();
	}

	public TriangleRef add( VertexRef v0, VertexRef v1, VertexRef v2, Vector3f normal )
	{
		return add( v0, v1, v2, normal, createRef() );
	}

	public TriangleRef add( VertexRef v0, VertexRef v1, VertexRef v2, Vector3f normal, TriangleRef ref )
	{
		return add( v0, v1, v2, normal.x(), normal.y(), normal.z(), ref );
	}

	public TriangleRef add( VertexRef v0, VertexRef v1, VertexRef v2, float nx, float ny, float nz )
	{
		return add( v0, v1, v2, nx, ny, nz, createRef() );
	}

	public TriangleRef add( VertexRef v0, VertexRef v1, VertexRef v2, float nx, float ny, float nz, TriangleRef ref )
	{
		if ( Options.DEBUG && ( ( v0.pool != vertexPool ) || ( v1.pool != vertexPool ) || ( v2.pool != vertexPool ) ) )
			throw new IllegalArgumentException( "trying to add vertex from a different mesh" );

		ref.updateAccess( this, safeInt( triangles.addf( v0.getInternalPoolIndex(), v1.getInternalPoolIndex(), v2.getInternalPoolIndex(), nx, ny, nz ) ) );
		return ref;
	}

	@Override
	public TriangleRef createRef()
	{
		final TriangleRef ref = tmpObjRefs.poll();
		return ref == null ? new TriangleRef() : ref;
	}

	@Override
	public void releaseRef( final TriangleRef obj )
	{
		tmpObjRefs.add( obj );
	}

	@Override
	public TriangleRef getObject( final int id, final TriangleRef obj )
	{
		if ( Options.DEBUG && ( id < 0 || id >= triangles.size() ) )
			throw new NoSuchElementException( "index=" + id );

		obj.updateAccess( this, id );
		return obj;
	}

	@Override
	public TriangleRef getObjectIfExists( final int id,
			final TriangleRef obj )
	{
		if ( id < 0 || id >= triangles.size() )
			return null;

		obj.updateAccess( this, id );
		return obj;
	}

	@Override
	public int getId( final TriangleRef o )
	{
		return o.getInternalPoolIndex();
	}

	@Override
	public Class< TriangleRef > getRefClass()
	{
		return TriangleRef.class;
	}

	@Override
	public Iterator< TriangleRef > iterator()
	{
		return new Iter();
	}

	public int size()
	{
		return safeInt( triangles.size() );
	}

	private class Iter implements Iterator< TriangleRef >
	{
		private final TriangleRef ref = createRef();

		private int i = 0;

		@Override
		public boolean hasNext()
		{
			return i < size();
		}

		@Override
		public TriangleRef next()
		{
			return getObject( i++, ref );
		}
	}

	private final Iterable< TriangleRef > safeIterable = SafeIter::new;

	public Iterable< TriangleRef > safe()
	{
		return safeIterable;
	}

	private class SafeIter implements Iterator< TriangleRef >
	{
		private int i = 0;

		@Override
		public boolean hasNext()
		{
			return i < size();
		}

		@Override
		public TriangleRef next()
		{
			return getObject( i++, createRef() );
		}
	}
}
