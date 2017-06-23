package net.imagej.mesh;

import org.mastodon.pool.BufferMappedElement;
import org.mastodon.pool.PoolObject;

/**
 * A class for storing triangles in a RefPool
 *
 * @author Tobias Pietzsch (MPI-CBG)
 * @author Kyle Harrington (University of Idaho, Moscow)
 */
public class Triangle extends PoolObject< Triangle, TrianglePool, BufferMappedElement >
{
	Triangle( final TrianglePool pool )
	{
		super( pool );
	}

	public Triangle init(
			final Vertex3 v1,
			final Vertex3 v2,
			final Vertex3 v3 )
	{
		pool.iv1.setQuiet( this, pool.vertex3Pool.getId( v1 ) );
		pool.iv2.setQuiet( this, pool.vertex3Pool.getId( v2 ) );
		pool.iv3.setQuiet( this, pool.vertex3Pool.getId( v3 ) );
		return this;
	}

	@Override
	protected void setToUninitializedState()
	{}

	// index = 0,1,2
	public Vertex3 getVertex( final int index )
	{
		return getVertex( index, pool.vertex3Pool.createRef() );
	}

	// index = 0,1,2
	public Vertex3 getVertex( final int index, final Vertex3 ref )
	{
		switch ( index )
		{
		case 0:
			return pool.vertex3Pool.getObject( pool.iv1.get( this ), ref );
		case 1:
			return pool.vertex3Pool.getObject( pool.iv2.get( this ), ref );
		case 2:
			return pool.vertex3Pool.getObject( pool.iv3.get( this ), ref );
		default:
			throw new IllegalArgumentException();
		}
	}

	/*
	 * etc ...
	 */

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "triangle(" );
		final Vertex3 ref = pool.vertex3Pool.createRef();
		sb.append( getVertex( 0, ref ) );
		sb.append( ", " );
		sb.append( getVertex( 1, ref ) );
		sb.append( ", " );
		sb.append( getVertex( 2, ref ) );
		sb.append( ")" );
		return sb.toString();
	}
}