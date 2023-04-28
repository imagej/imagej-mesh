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

public class ZSlicerGrowContour
{

	/**
	 * Build contour by adding segments (intersection between a triangle and the
	 * plane) to the contour end-point. Has a tolerance for matching ends. Does
	 * not deal with triangles parallel to the plane.
	 * 
	 * @param mesh
	 * @param z
	 * @param tolerance
	 * @return
	 */
	public static List< Contour > slice( final Mesh mesh, final double z, final double tolerance )
	{
		final Triangles triangles = mesh.triangles();
		final Vertices vertices = mesh.vertices();
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

			while ( contour.grow( deque, tolerance ) && !contour.isClosed() )
			{}
		}
		return contours;
	}
}
