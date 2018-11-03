package net.imagej.refmesh;

import net.imagej.mesh.Vertices;
import net.imglib2.RealLocalizable;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.mastodon.Ref;

public class VertexRef implements Ref< VertexRef >, RealLocalizable
{
	/**
	 * Current index in pool.
	 */
	private int index;

	/**
	 * The pool into which this proxy currently refers.
	 */
	VertexPool pool;

	/**
	 * Make this proxy refer the element at the specified
	 * {@code index} in the specified {@code pool}.
	 *
	 * @param pool
	 * @param index
	 */
	void updateAccess( final VertexPool pool, final int index )
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
	public VertexRef refTo( final VertexRef obj )
	{
		updateAccess( obj.pool, obj.index );
		return this;
	}

	@Override
	public boolean equals( final Object o )
	{
		if ( this == o )
			return true;
		if ( !( o instanceof VertexRef ) )
			return false;
		final VertexRef ref = ( VertexRef ) o;
		return index == ref.index && pool.equals( ref.pool );
	}

	@Override
	public int hashCode()
	{
		return pool.hashCode() + 31 * index;
	}

	public Vector3f getPosition()
	{
		return getPosition( new Vector3f() );
	}

	public Vector3f getPosition( final Vector3f dest )
	{
		final Vertices vs = pool.vertices;
		return dest.set( vs.xf( index ), vs.yf( index ), vs.zf( index ) );
	}

	public VertexRef setPosition( final Vector3fc position )
	{
		pool.vertices.setPositionf( index, position.x(), position.y(), position.z() );
		return this;
	}

	public Vector3f getNormal()
	{
		return getNormal( new Vector3f() );
	}

	public Vector3f getNormal( final Vector3f dest )
	{
		final Vertices vs = pool.vertices;
		return dest.set( vs.nxf( index ), vs.nyf( index ), vs.nzf( index ) );
	}

	public VertexRef setNormal( final Vector3fc normal )
	{
		pool.vertices.setNormalf( index, normal.x(), normal.y(), normal.z() );
		return this;
	}

	public Vector2f getTexture()
	{
		return getTexture( new Vector2f() );
	}

	public Vector2f getTexture( final Vector2f dest )
	{
		final Vertices vs = pool.vertices;
		return dest.set( vs.uf( index ), vs.vf( index ) );
	}

	public VertexRef setTexture( final Vector2fc tex )
	{
		pool.vertices.setTexturef( index, tex.x(), tex.y() );
		return this;
	}

	public VertexRef set( final VertexRef v )
	{
		final Vertices vv = v.pool.vertices;
		final int vi = v.getInternalPoolIndex();
		pool.vertices.setf( index, vv.xf( vi ), vv.yf( vi ), vv.zf( vi ), vv.nxf( vi ), vv.nyf( vi ), vv.nzf( vi ), vv.uf( vi ), vv.vf( vi ) );
		return this;
	}

	/*
	 * RealLocalizable
	 */

	@Override
	public void localize( final float[] position )
	{
		final Vertices vs = pool.vertices;
		position[ 0 ] = vs.xf( index );
		position[ 1 ] = vs.yf( index );
		position[ 2 ] = vs.zf( index );
	}

	@Override
	public void localize( final double[] position )
	{
		final Vertices vs = pool.vertices;
		position[ 0 ] = vs.x( index );
		position[ 1 ] = vs.y( index );
		position[ 2 ] = vs.z( index );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		switch ( d )
		{
		case 0:
			return pool.vertices.xf( index );
		case 1:
			return pool.vertices.yf( index );
		case 2:
			return pool.vertices.zf( index );
		}
		throw new IndexOutOfBoundsException( "" + d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		switch ( d )
		{
		case 0:
			return pool.vertices.x( index );
		case 1:
			return pool.vertices.y( index );
		case 2:
			return pool.vertices.z( index );
		}
		throw new IndexOutOfBoundsException( "" + d );
	}

	@Override
	public int numDimensions()
	{
		return 3;
	}
}
