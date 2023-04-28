package net.imagej.mesh.zslicer;

import static net.imagej.mesh.zslicer.ZSlicer.maxZ;
import static net.imagej.mesh.zslicer.ZSlicer.minZ;
import static net.imagej.mesh.zslicer.ZSlicer.triangleIntersection;
import static net.imagej.mesh.zslicer.ZSlicer.triangleParallel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import gnu.trove.list.array.TLongArrayList;
import net.imagej.mesh.Mesh;
import net.imagej.mesh.Triangles;
import net.imagej.mesh.Vertices;
import net.imagej.mesh.zslicer.ZSlicer.Contour;
import net.imagej.mesh.zslicer.ZSlicer.Segment;
import net.imagej.mesh.zslicer.ZSlicer.Triangle;

public class ZSlicerRoundPositions
{

	private static final double EPS = 4e-4;

	/**
	 * Like {@link ZSlicerGrowContour} but round vertices positions and plane
	 * position so that they don't intersect. Inspired from
	 * https://github.com/rminetto/slicing/blob/main/code-python/slicer.py#L377
	 * 
	 * @param mesh
	 * @param z
	 * @param zPixelSize
	 *            the pixel size in Z, so that shifts can be computed in
	 *            physical units.
	 * @return
	 */
	public static List< Contour > slice( final Mesh mesh, final double zp, final double zPixelSize )
	{
		// Convert eps to physical size.
		final double eps = EPS * zPixelSize;

		final Triangles triangles = mesh.triangles();
		final Vertices vertices = mesh.vertices();
		// Round coordinates of triangles final to even multiples final of eps.
		roundTrianglesEven( vertices, eps );
		// Slice plane to odd multiples of eps.
		final double z = mround( zp, eps, 2, 1 );

		final TLongArrayList intersecting = new TLongArrayList();
		final TLongArrayList parallel = new TLongArrayList();
		for ( long f = 0; f < triangles.size(); f++ )
		{
			final long v0 = triangles.vertex0( f );
			final long v1 = triangles.vertex1( f );
			final long v2 = triangles.vertex2( f );

			final double minZ = minZ( vertices, v0, v1, v2 );
			if ( minZ > z )
				continue;
			final double maxZ = maxZ( vertices, v0, v1, v2 );
			if ( maxZ < z )
				continue;

			if ( minZ == maxZ )
			{
				parallel.add( f );
				continue;
			}
			intersecting.add( f );
		}

		// Deal with intersecting triangle.
		final Collection< Segment > segments = new HashSet<>();
		for ( int i = 0; i < intersecting.size(); i++ )
		{
			final long id = intersecting.getQuick( i );
			final Segment segment = triangleIntersection( mesh, id, z );
			if ( segment != null )
				segments.add( segment );
		}

		// Deal with parallel triangles.
		final List< Triangle > tris = new ArrayList<>( parallel.size() );
		for ( int i = 0; i < parallel.size(); i++ )
		{
			final long id = parallel.getQuick( i );
			final Triangle triangle = triangleParallel( mesh, id, z );
			tris.add( triangle );
		}
		assert ( tris.isEmpty() );// should not be intersecting.

		// Sort them by x then y etc.
		final ArrayList< Segment > sorted = new ArrayList<>( segments );
		sorted.sort( null );

		// Build contours from segments.
		final ArrayDeque< Segment > deque = new ArrayDeque<>( sorted );
		final List< Contour > contours = new ArrayList<>();
		while ( !deque.isEmpty() )
		{
			final Segment start = deque.pop();
			final Contour contour = Contour.init( start );
			contours.add( contour );

			while ( contour.grow( deque, zPixelSize ) && !contour.isClosed() )
			{}
		}
		return contours;
	}

	private static void roundTrianglesEven( final Vertices vertices, final double eps )
	{
		final int nVertices = ( int ) vertices.size();
		for ( int i = 0; i < nVertices; i++ )
		{
			final double x = vertices.x( i );
			final double y = vertices.y( i );
			final double z = vertices.z( i );
			final double xr = mround( x, eps, 2, 0 );
			final double yr = mround( y, eps, 2, 0 );
			final double zr = mround( z, eps, 2, 0 );
			vertices.set( i, xr, yr, zr );
		}
	}

	private static final double mround( final double v, final double eps, final int mod, final int rem )
	{
		final long y = Math.round( v / ( mod * eps ) );
		final double z = ( y * mod + rem ) * eps;
		return z;
	}
}
