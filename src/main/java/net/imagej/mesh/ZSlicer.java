package net.imagej.mesh;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TLongArrayList;

public class ZSlicer
{

	public static List< Contour > slice( final Mesh mesh, final double z )
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
		final Collection< Segment > segments = new ArrayList<>();
//				new HashSet<>();
		// new ArrayDeque<>( intersecting.size() );
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

//		final HashSet< Segment > pruned = new HashSet<>( segments );
//		final ArrayList< Segment > sorted = new ArrayList<>( pruned );
//		sorted.sort( new Comparator< Segment >()
//		{
//
//			@Override
//			public int compare( final Segment o1, final Segment o2 )
//			{
//				final int dx1 = Double.compare( o1.x1, o2.x1 );
//				if ( dx1 != 0 )
//					return dx1;
//				final int dy1 = Double.compare( o1.y1, o2.y1 );
//				if ( dy1 != 0 )
//					return dy1;
//				final int dx2 = Double.compare( o1.x2, o2.x2 );
//				if ( dx2 != 0 )
//					return dx2;
//				return Double.compare( o1.y2, o2.y2 );
//			}
//		} );
//		
//		System.out.println( "Segments: " + segments.size() ); // DEBUG
//		System.out.println( "Segments after pruning: " + sorted.size() ); // DEBUG
//		sorted.forEach( System.out::println );
//		System.out.println( "Triangles:" ); // DEBUG
//		tris.forEach( System.out::println );

		// Build contours from segments.
		final ArrayDeque< Segment > deque = new ArrayDeque<>( segments );
		final List< Contour > contours = new ArrayList<>();
		while ( !deque.isEmpty() )
		{
			final Segment start = deque.pop();
			final Contour contour = Contour.init( start );
			contours.add( contour );

			while ( contour.grow( deque ) && !contour.isClosed() )
			{}
		}

		int i = 0;
		for ( final Contour contour : contours )
		{
			System.out.println( "Contour " + ++i + ":" );
			System.out.println( contour ); // DEBUG
		}
		return contours;
	}

	private static final double minZ( final Vertices vertices, final long v0, final long v1, final long v2 )
	{
		return Math.min( vertices.z( v0 ), Math.min( vertices.z( v1 ), vertices.z( v2 ) ) );
	}

	private static final double maxZ( final Vertices vertices, final long v0, final long v1, final long v2 )
	{
		return Math.max( vertices.z( v0 ), Math.max( vertices.z( v1 ), vertices.z( v2 ) ) );
	}

	private static Triangle triangleParallel( final Mesh mesh, final long id, final double z )
	{
		final long v0 = mesh.triangles().vertex0( id );
		final long v1 = mesh.triangles().vertex1( id );
		final long v2 = mesh.triangles().vertex2( id );

		final double x0 = mesh.vertices().x( v0 );
		final double x1 = mesh.vertices().x( v1 );
		final double x2 = mesh.vertices().x( v2 );
		final double y0 = mesh.vertices().y( v0 );
		final double y1 = mesh.vertices().y( v1 );
		final double y2 = mesh.vertices().y( v2 );

		return new Triangle( x0, y0, x1, y1, x2, y2 );
	}

	private static Segment triangleIntersection( final Mesh mesh, final long id, final double z )
	{
		final long v0 = mesh.triangles().vertex0( id );
		final long v1 = mesh.triangles().vertex1( id );
		final long v2 = mesh.triangles().vertex2( id );

		final double x0 = mesh.vertices().x( v0 );
		final double x1 = mesh.vertices().x( v1 );
		final double x2 = mesh.vertices().x( v2 );
		final double y0 = mesh.vertices().y( v0 );
		final double y1 = mesh.vertices().y( v1 );
		final double y2 = mesh.vertices().y( v2 );
		final double z0 = mesh.vertices().z( v0 );
		final double z1 = mesh.vertices().z( v1 );
		final double z2 = mesh.vertices().z( v2 );

		if ( z0 == z )
		{
			// Start = v0.
			if ( z1 == z )
			{
				// End = v1.
				return new Segment( x0, y0, x1, y1 );
			}
			else if ( z2 == z )
			{
				// End = v2.
				return new Segment( x0, y0, x2, y2 );
			}
			else
			{
				// End between v1 and v2.
				final double[] ei = edgeIntersection( x1, y1, z1, x2, y2, z2, z );
				if ( ei == null )
					return null; // Intersect at a point.

				return new Segment( x0, y0, ei[ 0 ], ei[ 1 ] );
			}
		}
		else
		{
			if ( z1 == z )
			{
				// Start = v1.
				if ( z2 == z )
				{
					// End = v2.
					return new Segment( x1, y1, x2, y2 );
				}
				else
				{
					// End between v0 and v2.
					final double[] ei = edgeIntersection( x0, y0, z0, x2, y2, z2, z );
					if ( ei == null )
						return null; // Intersect at a point.

					return new Segment( x1, y1, ei[ 0 ], ei[ 1 ] );
				}
			}
			else if ( z2 == z )
			{
				// Start = v2, end between v0 and v1.
				final double[] ei = edgeIntersection( x0, y0, z0, x1, y1, z1, z );
				if ( ei == null )
					return null; // Intersect at a point.

				return new Segment( x2, y2, ei[ 0 ], ei[ 1 ] );
			}
			else
			{
				// Not crossing any vertex.
				// Crossing two edges, but what ones?
				final double[] ei0 = edgeIntersection( x0, y0, z0, x1, y1, z1, z );
				final double[] ei1 = edgeIntersection( x0, y0, z0, x2, y2, z2, z );
				final double[] ei2 = edgeIntersection( x1, y1, z1, x2, y2, z2, z );
				if ( ei0 == null )
				{
					return new Segment( ei1[ 0 ], ei1[ 1 ], ei2[ 0 ], ei2[ 1 ] );
				}
				else if ( ei1 == null )
				{
					return new Segment( ei0[ 0 ], ei0[ 1 ], ei2[ 0 ], ei2[ 1 ] );
				}
				else
				{
					return new Segment( ei0[ 0 ], ei0[ 1 ], ei1[ 0 ], ei1[ 1 ] );
				}
			}
		}
	}

	private static double[] edgeIntersection( final double xs, final double ys, final double zs,
			final double xt, final double yt, final double zt, final double z )
	{
		if ( ( zs > z && zt > z ) || ( zs < z && zt < z ) )
			return null;

		assert ( zs != zt );
		final double t = ( z - zs ) / ( zt - zs );
		final double x = xs + t * ( xt - xs );
		final double y = ys + t * ( yt - ys );
		return new double[] { x, y };
	}

	public static String triangleToString( final Mesh mesh, final long id )
	{
		final StringBuilder str = new StringBuilder( id + ": " );

		final Triangles triangles = mesh.triangles();
		final Vertices vertices = mesh.vertices();
		final long v0 = triangles.vertex0( id );
		final double x0 = vertices.x( v0 );
		final double y0 = vertices.y( v0 );
		final double z0 = vertices.z( v0 );
		str.append( String.format( "(%5.1f, %5.1f, %5.1f) - ", x0, y0, z0 ) );

		final long v1 = triangles.vertex1( id );
		final double x1 = vertices.x( v1 );
		final double y1 = vertices.y( v1 );
		final double z1 = vertices.z( v1 );
		str.append( String.format( "(%5.1f, %5.1f, %5.1f) - ", x1, y1, z1 ) );

		final long v2 = triangles.vertex2( id );
		final double x2 = vertices.x( v2 );
		final double y2 = vertices.y( v2 );
		final double z2 = vertices.z( v2 );
		str.append( String.format( "(%5.1f, %5.1f, %5.1f) - ", x2, y2, z2 ) );

		str.append( String.format( "N = (%4.2f, %4.2f, %4.2f) ",
				triangles.nx( id ), triangles.nz( id ), triangles.nz( id ) ) );

		return str.toString();
	}

	private static class Segment
	{

		private final double x1;

		private final double y1;

		private final double x2;

		private final double y2;

		private Segment( final double x1, final double y1, final double x2, final double y2 )
		{
			final double xa, xb, ya, yb;
			if ( x1 < x2 )
			{
				xa = x1;
				ya = y1;
				xb = x2;
				yb = y2;
			}
			else if ( x1 > x2 )
			{
				xa = x2;
				ya = y2;
				xb = x1;
				yb = y1;
			}
			else if ( y1 < y2 )
			{
				xa = x1;
				ya = y1;
				xb = x2;
				yb = y2;
			}
			else if ( y1 > y2 )
			{
				xa = x2;
				ya = y2;
				xb = x1;
				yb = y1;
			}
			else
			{
				throw new IllegalArgumentException( "Trying to create a segment of 0-length." );
			}

			this.x1 = xa;
			this.y1 = ya;
			this.x2 = xb;
			this.y2 = yb;
		}

		@Override
		public String toString()
		{
			return String.format( "S (%9.6f, %9.6f) -> (%9.6f, %9.6f)", x1, y1, x2, y2 );
		}
	}

	private static class Triangle
	{
		public final double x1;

		public final double y1;

		public final double x2;

		public final double y2;

		public final double x3;

		public final double y3;

		private Triangle( final double x1, final double y1, final double x2, final double y2, final double x3, final double y3 )
		{
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			this.x3 = x3;
			this.y3 = y3;
		}

		@Override
		public String toString()
		{
			return String.format( "T (%5.2f, %5.2f) -> (%5.2f, %5.2f) -> (%5.2f, %5.2f)", x1, y1, x2, y2, x3, y3 );
		}
	}

	public static final class Contour
	{

		public final TDoubleArrayList x = new TDoubleArrayList();

		public final TDoubleArrayList y = new TDoubleArrayList();

		private final double endx;

		private final double endy;

		private double matchy;

		private double matchx;

		private final boolean isClosed;

		private Contour( final Segment start )
		{
			endx = start.x2;
			endy = start.y2;
			matchx = start.x1;
			matchy = start.y1;
			isClosed = false;
		}

		public static Contour init( final Segment start )
		{
			return new Contour( start );
		}

		/**
		 * Returns <code>true</code> if we found a segment that matched this
		 * contour.
		 * 
		 * @param segments
		 * @return
		 */
		public boolean grow( final Collection< Segment > segments )
		{
			for ( final Segment segment : segments )
			{
				if ( segment.x1 == matchx && segment.y1 == matchy )
				{
					segments.remove( segment );
					x.add( segment.x1 );
					y.add( segment.y1 );
					matchx = segment.x2;
					matchy = segment.y2;
//					if ( segment.x2 == endx && segment.y2 == endy )
//						isClosed = true;
					return true;
				}
				else if ( segment.x2 == matchx && segment.y2 == matchy )
				{
					segments.remove( segment );
					x.add( segment.x2 );
					y.add( segment.y2 );
					matchx = segment.x1;
					matchy = segment.y1;
//					if ( segment.x1 == endx && segment.y1 == endy )
//						isClosed = true;
					return true;
				}
			}
			return false;
		}

		public boolean isClosed()
		{
			return isClosed;
		}

		@Override
		public String toString()
		{
			final StringBuilder str = new StringBuilder();
			str.append( super.toString() );
			for ( int i = 0; i < x.size(); i++ )
				str.append( String.format( "\n%5.2f, %5.2f", x.getQuick( i ), y.getQuick( i ) ) );

			return str.toString();
		}

		public double[] xScaled( final double scale )
		{
			final double[] arr = x.toArray();
			for ( int i = 0; i < arr.length; i++ )
				arr[ i ] *= scale;
			return arr;
		}

		public double[] yScaled( final double scale )
		{
			final double[] arr = y.toArray();
			for ( int i = 0; i < arr.length; i++ )
				arr[ i ] *= scale;
			return arr;
		}
	}

}
