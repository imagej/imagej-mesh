package net.imagej.refmesh;

import net.imagej.mesh.Vertices;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.mastodon.Ref;

public class VertexRef implements Ref< VertexRef >
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

	/*
	 * not sure about these...
	 */

	private Vector3f tmp3 = new Vector3f();

	private Vector2f tmp2 = new Vector2f();

	public VertexRef setPosition( final VertexRef v )
	{
		return setPosition( v.getPosition( tmp3 ) );
	}

	public VertexRef setNormal( final VertexRef v )
	{
		return setNormal( v.getNormal( tmp3 ) );
	}

	public VertexRef setTexture( final VertexRef v )
	{
		return setTexture( v.getTexture( tmp2 ) );
	}

	public VertexRef set( final VertexRef v )
	{
		return setPosition( v ).setNormal( v ).setTexture( v );
	}
}
