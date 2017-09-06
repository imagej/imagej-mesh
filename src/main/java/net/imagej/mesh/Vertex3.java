package net.imagej.mesh;

import net.imglib2.RealLocalizable;
import org.mastodon.pool.BufferMappedElement;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.attributes.FloatArrayAttributeValue;

/**
 * A data structure for storing a 3D vertex
 *
 * @author Tobias Pietzsch (MPI-CBG)
 * @author Kyle Harrington (University of Idaho, Moscow)
 */
public class Vertex3 extends PoolObject< Vertex3, Vertex3Pool, BufferMappedElement > implements RealLocalizable
{
	private final FloatArrayAttributeValue position;
	private final FloatArrayAttributeValue normal;
	private final FloatArrayAttributeValue uv;

	public Vertex3( final Vertex3Pool pool )
	{
		super( pool );

		/*
		 * doesn't send property change events
		 */
		position = pool.position.createQuietAttributeValue( this );
		normal = pool.normal.createQuietAttributeValue( this );
		uv = pool.uv.createQuietAttributeValue( this );
	}
	
	public Vertex3 init(
			final float x,
			final float y,
			final float z )
	{
		return init( x, y, z, 0, 0, 0, 0, 0, 0);
	}

	public Vertex3 init(
			final float x,
			final float y,
			final float z,
			final float nx,
			final float ny,
			final float nz,
			final float u,
			final float v,
			final float w )
	{
		// like this:
		pool.position.setQuiet( this, 0, x );
		pool.position.setQuiet( this, 1, y );
		pool.position.setQuiet( this, 2, z );

		// or like this:
		pool.normal.setQuiet( this, 0, nx );
		pool.normal.setQuiet( this, 1, ny );
		pool.normal.setQuiet( this, 2, nz );

		pool.uv.setQuiet( this, 0, u );
		pool.uv.setQuiet( this, 1, v );
		pool.uv.setQuiet( this, 1, w );
		return this;
	}

	@Override
	protected void setToUninitializedState()
	{}

	public float getX()
	{
		return position.get( 0 );
	}

	public void setX( final float x)
	{
		pool.position.setQuiet( this, 0, x );
	}
	
	public float getY()
	{
		return position.get( 1 );
	}
	
	public void setY( final float y)
	{
		pool.position.setQuiet( this, 1, y );
	}
	
	public float getZ()
	{
		return position.get( 2 );
	}

	public void setZ( final float z)
	{
		pool.position.setQuiet( this, 2, z );
	}
	
	public float getNX()
	{
		return normal.get( 0 );
	}
	
	public void setNX( final float nx)
	{
		pool.normal.setQuiet( this, 0, nx );
	}
	
	public float getNY()
	{
		return normal.get( 1 );
	}
	
	public void setNY( final float ny)
	{
		pool.normal.setQuiet( this, 1, ny );
	}
	
	public float getNZ()
	{
		return normal.get( 2 );
	}
	
	public void setNZ( final float nz)
	{
		pool.normal.setQuiet( this, 2, nz );
	}
	
	public float getU()
	{
		return uv.get( 0 );
	}
	
	public void setU( final float u)
	{
		pool.uv.setQuiet( this, 0, u );
	}
	
	public float getV()
	{
		return uv.get( 1 );
	}
	
	public void setV( final float v)
	{
		pool.uv.setQuiet( this, 1, v );
	}
	
	public float getW()
	{
		return uv.get( 2 );
	}
	
	public void setW( final float w)
	{
		pool.uv.setQuiet( this, 2, w );
	}
	
	@Override
	public String toString()
	{
		return String.format( "v(%.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f)", getX(), getY(), getZ(), getNX(), getNY(), getNZ(), getU(), getV(), getW() );
	}

	@Override
	public void localize(float[] position) {
		position[0] = getX();
		position[1] = getY();
		position[2] = getZ();
	}

	@Override
	public void localize(double[] position) {
		position[0] = getX();
		position[1] = getY();
		position[2] = getZ();
	}

	@Override
	public float getFloatPosition(int d) {
		return position.get( d );
	}

	@Override
	public double getDoublePosition(int d) {
		return position.get( d );
	}

	@Override
	public int numDimensions() {
		return 3;
	}
}