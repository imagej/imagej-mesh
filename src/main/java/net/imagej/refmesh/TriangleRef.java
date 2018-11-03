package net.imagej.refmesh;

import java.util.Iterator;
import net.imagej.mesh.Triangles;
import net.imagej.mesh.Vertices;
import org.joml.Vector3f;
import org.mastodon.Ref;

import static net.imagej.refmesh.RefMesh.safeInt;

public class TriangleRef implements Ref< TriangleRef >
{
	/**
	 * Current index in pool.
	 */
	private int index;

	/**
	 * The pool into which this proxy currently refers.
	 */
	private TrianglePool pool;

	/**
	 * Make this proxy refer the element at the specified
	 * {@code index} in the specified {@code pool}.
	 *
	 * @param pool
	 * @param index
	 */
	void updateAccess( final TrianglePool pool, final int index )
	{
		if ( this.pool != pool )
			this.pool = pool;
		this.index = index;
	}

	@Override
	public int getInternalPoolIndex()
	{
		return index;
	}

	@Override
	public TriangleRef refTo( final TriangleRef obj )
	{
		updateAccess( obj.pool, obj.index );
		return this;
	}

	@Override
	public boolean equals( final Object o )
	{
		if ( this == o )
			return true;
		if ( !( o instanceof TriangleRef ) )
			return false;
		final TriangleRef ref = ( TriangleRef ) o;
		return index == ref.index && pool.equals( ref.pool );
	}

	@Override
	public int hashCode()
	{
		return pool.hashCode() + 31 * index;
	}

	public Vector3f getNormal()
	{
		return getNormal( new Vector3f() );
	}

	public Vector3f getNormal( final Vector3f dest )
	{
		final Triangles ts = pool.triangles;
		return dest.set( ts.nxf( index ), ts.nyf( index ), ts.nzf( index ) );
	}

	public VertexRef vertex0()
	{
		return vertex0( pool.vertexPool.createRef() );
	}

	public VertexRef vertex0( final VertexRef ref )
	{
		ref.updateAccess( pool.vertexPool, safeInt( pool.triangles.vertex0( index ) ) );
		return ref;
	}

	public VertexRef vertex1()
	{
		return vertex1( pool.vertexPool.createRef() );
	}

	public VertexRef vertex1( final VertexRef ref )
	{
		ref.updateAccess( pool.vertexPool, safeInt( pool.triangles.vertex1( index ) ) );
		return ref;
	}

	public VertexRef vertex2()
	{
		return vertex2( pool.vertexPool.createRef() );
	}

	public VertexRef vertex2( final VertexRef ref )
	{
		ref.updateAccess( pool.vertexPool, safeInt( pool.triangles.vertex2( index ) ) );
		return ref;
	}

	public VertexRefs vertices()
	{
		return vertices;
	}

	private final VertexRefs vertices = new VertexRefs();

	public class VertexRefs implements Iterable< VertexRef >
	{
		private Iter iterator;

		private Iterable< VertexRef > safeVertices;

		@Override
		public Iterator< VertexRef > iterator()
		{
			if ( iterator == null )
				iterator = new Iter();
			else
				iterator.reset();
			return iterator;
		}

		public Iterable< VertexRef > safe()
		{
			if ( safeVertices == null )
				safeVertices = SafeIter::new;
			return safeVertices;
		}
	};

	private class SafeIter implements Iterator< VertexRef >
	{
		private int i = 0;

		@Override
		public boolean hasNext()
		{
			return i < 3;
		}

		@Override
		public VertexRef next()
		{
			return vertex( i++, pool.vertexPool.createRef() );
		}
	}

	class Iter implements Iterator< VertexRef >
	{
		private VertexRef ref = pool.vertexPool.createRef();

		private int i = 0;

		void reset()
		{
			i = 0;
		}

		@Override
		public boolean hasNext()
		{
			return i < 3;
		}

		@Override
		public VertexRef next()
		{
			return vertex( i++, ref );
		}
	}

	/**
	 * @param vi
	 * 		0, 1, or2
	 */
	private VertexRef vertex( final int vi, final VertexRef ref )
	{
		if ( vi == 0 )
		{
			ref.updateAccess( pool.vertexPool,
					safeInt( pool.triangles.vertex0( index ) ) );
		}
		else if ( vi == 1 )
		{
			ref.updateAccess( pool.vertexPool,
					safeInt( pool.triangles.vertex1( index ) ) );
		}
		else
		{
			ref.updateAccess( pool.vertexPool,
					safeInt( pool.triangles.vertex2( index ) ) );
		}
		return ref;
	}
}
