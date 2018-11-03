package net.imagej.refmesh;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.imagej.mesh.Vertices;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.mastodon.Options;
import org.mastodon.RefPool;
import org.mastodon.collection.ref.RefPoolBackedRefCollection;
import org.mastodon.collection.util.AbstractRefPoolCollectionWrapper;

import static net.imagej.refmesh.RefMesh.safeInt;

public class VertexPool implements RefPool< VertexRef >, Iterable< VertexRef >
{
	final Vertices vertices;

	private final ConcurrentLinkedQueue< VertexRef > tmpObjRefs;

	VertexPool( final RefMesh mesh )
	{
		vertices = mesh.mesh.vertices();
		tmpObjRefs = new ConcurrentLinkedQueue<>();
	}

	public VertexRef add()
	{
		return add( createRef() );
	}

	public VertexRef add( VertexRef ref )
	{
		ref.updateAccess( this, safeInt( vertices.addf( 0, 0, 0 ) ) );
		return ref;
	}

	public VertexRef add( Vector3fc position, Vector3fc normal, Vector2fc texture )
	{
		return add( position, normal, texture, createRef() );
	}

	public VertexRef add( Vector3fc position, Vector3fc normal, Vector2fc texture, VertexRef ref )
	{
		ref.updateAccess( this, safeInt(
				vertices.addf( position.x(), position.y(), position.z(), normal.x(), normal.y(), normal.z(), texture.x(), texture.y() ) ) );
		return ref;
	}

	@Override
	public VertexRef createRef()
	{
		final VertexRef ref = tmpObjRefs.poll();
		return ref == null ? new VertexRef() : ref;
	}

	@Override
	public void releaseRef( final VertexRef obj )
	{
		tmpObjRefs.add( obj );
	}

	@Override
	public VertexRef getObject( final int id, final VertexRef obj )
	{
		if ( Options.DEBUG && ( id < 0 || id >= vertices.size() ) )
			throw new NoSuchElementException( "index=" + id );

		obj.updateAccess( this, id );
		return obj;
	}

	@Override
	public VertexRef getObjectIfExists( final int id, final VertexRef obj )
	{
		if ( id < 0 || id >= vertices.size() )
			return null;

		obj.updateAccess( this, id );
		return obj;
	}

	@Override
	public int getId( final VertexRef o )
	{
		return o.getInternalPoolIndex();
	}

	@Override
	public Class< VertexRef > getRefClass()
	{
		return VertexRef.class;
	}

	@Override
	public Iterator< VertexRef > iterator()
	{
		return new Iter();
	}

	public int size()
	{
		return safeInt( vertices.size() );
	}

	private final AbstractRefPoolCollectionWrapper< VertexRef, VertexPool > asRefCollection = new AbstractRefPoolCollectionWrapper< VertexRef, VertexPool >( this )
	{
		@Override
		public int size()
		{
			return pool.size();
		}

		@Override
		public Iterator< VertexRef > iterator()
		{
			return pool.iterator();
		}
	};

	public RefPoolBackedRefCollection< VertexRef > asRefCollection()
	{
		return asRefCollection;
	}

	private class Iter implements Iterator< VertexRef >
	{

		private final VertexRef ref = createRef();

		private int i = 0;

		@Override
		public boolean hasNext()
		{
			return i < size();
		}

		@Override
		public VertexRef next()
		{
			return getObject( i++, ref );
		}
	}

	private final Iterable< VertexRef > safeIterable = SafeIter::new;

	public Iterable< VertexRef > safe()
	{
		return safeIterable;
	}

	private class SafeIter implements Iterator< VertexRef >
	{
		private int i = 0;

		@Override
		public boolean hasNext()
		{
			return i < size();
		}

		@Override
		public VertexRef next()
		{
			return getObject( i++, createRef() );
		}
	}

}
