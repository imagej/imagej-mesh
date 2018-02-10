package net.imagej.mesh;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.mastodon.pool.*;
import org.mastodon.pool.attributes.FloatArrayAttribute;

/**
 * A class for storing vertices in a RefPool
 *
 * @author Tobias Pietzsch (MPI-CBG)
 * @author Kyle Harrington (University of Idaho, Moscow)
 */
public class Vertex3Pool extends Pool< Vertex3, BufferMappedElement >
{
	final FloatArrayAttribute< Vertex3 > position;
	final FloatArrayAttribute< Vertex3 > normal;
	final FloatArrayAttribute< Vertex3 > uv;

	final Vertex3Layout vertexLayout;

	public Vertex3Pool( final int initialCapacity )
	{
		super( initialCapacity, new Vertex3Layout(), Vertex3.class, SingleArrayMemPool.factory( BufferMappedElementArray.factory ) );
		this.vertexLayout = new Vertex3Layout();// we need to make these twice
		position = new FloatArrayAttribute<>( vertexLayout.position, this );
		normal = new FloatArrayAttribute<>( vertexLayout.normal, this );
		uv = new FloatArrayAttribute<>( vertexLayout.uv, this );
	}

	public Vertex3Pool(ByteBuffer bb)
	{
		super( bb.limit(), new Vertex3Layout(), Vertex3.class, SingleArrayMemPool.factory( BufferMappedElementArray.wrappingFactory(bb) ) );
		this.vertexLayout = new Vertex3Layout();// we need to make these twice
		position = new FloatArrayAttribute<>( vertexLayout.position, this );
		normal = new FloatArrayAttribute<>( vertexLayout.normal, this );
		uv = new FloatArrayAttribute<>( vertexLayout.uv, this );

	}

	public Vertex3 create()
	{
		return super.create( createRef() );
	}

	@Override
	public Vertex3 create( final Vertex3 obj )
	{
		return super.create( obj );
	}

	@Override
	public void delete( final Vertex3 obj )
	{
		super.delete( obj );
	}

	@Override
	protected Vertex3 createEmptyRef()
	{
		return new Vertex3( this );
	}

	public FloatBuffer getFloatBuffer()
	{
		final SingleArrayMemPool< BufferMappedElementArray, ? > memPool = ( SingleArrayMemPool< BufferMappedElementArray, ? > ) getMemPool();
		final BufferMappedElementArray dataArray =  memPool.getDataArray();
		return dataArray.getBuffer().asFloatBuffer();
	}
}