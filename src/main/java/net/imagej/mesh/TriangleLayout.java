package net.imagej.mesh;

import org.mastodon.pool.PoolObjectLayout;

/**
 * The layout of a Triangle data structure in the memory of a RefPool
 *
 * @author Tobias Pietzsch (MPI-CBG)
 * @author Kyle Harrington (University of Idaho, Moscow)
 */
public class TriangleLayout extends PoolObjectLayout
{
	final IndexField v1 = indexField();
	final IndexField v2 = indexField();
	final IndexField v3 = indexField();
	// indexArrayField ... but IndexArrayAttribute not yet implemented
}