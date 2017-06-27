package net.imagej.mesh;

import org.mastodon.pool.PoolObjectLayout;

/**
 * The layout for storing a vertex in memory
 *
 * @author Tobias Pietzsch (MPI-CBG)
 * @author Kyle Harrington (University of Idaho, Moscow)
 */
public class Vertex3Layout extends PoolObjectLayout
{
	final FloatArrayField position = floatArrayField( 3 );
	final FloatArrayField normal = floatArrayField( 3 );
	final FloatArrayField uv = floatArrayField( 3 );
}