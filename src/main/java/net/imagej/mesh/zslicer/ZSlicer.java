package net.imagej.mesh.zslicer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import gnu.trove.list.array.TDoubleArrayList;
import net.imagej.mesh.Mesh;
import net.imagej.mesh.Triangles;
import net.imagej.mesh.Vertices;
import net.imagej.mesh.zslicer.ZSlicerMatchEdges.ContourExt;

public class ZSlicer
{

	public static List< ContourExt > slice( final Mesh mesh, final double z, final double tolerance )
	{
//		return ZSlicerRoundPositions.slice( mesh, z, tolerance );
		return ZSlicerMatchEdges.slice( mesh, z, tolerance );
	}

	static final double minZ( final Vertices vertices, final long v0, final long v1, final long v2 )
	{
		return Math.min( vertices.z( v0 ), Math.min( vertices.z( v1 ), vertices.z( v2 ) ) );
	}

	static final double maxZ( final Vertices vertices, final long v0, final long v1, final long v2 )
	{
		return Math.max( vertices.z( v0 ), Math.max( vertices.z( v1 ), vertices.z( v2 ) ) );
	}

	static Triangle triangleParallel( final Mesh mesh, final long id, final double z )
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

	static Segment triangleIntersection( final Mesh mesh, final long id, final double z )
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
				if ( x0 == x1 && y0 == y1 )
					return null;
				// End = v1.
				return new Segment( x0, y0, x1, y1 );
			}
			else if ( z2 == z )
			{
				if ( x0 == x2 && y0 == y2 )
					return null;
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
					if ( x2 == x1 && y2 == y1 )
						return null;
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
					if ( ei1[ 0 ] == ei2[ 0 ] && ei1[ 1 ] == ei2[ 1 ] )
						return null;
					return new Segment( ei1[ 0 ], ei1[ 1 ], ei2[ 0 ], ei2[ 1 ] );
				}
				else if ( ei1 == null )
				{
					if ( ei0[ 0 ] == ei2[ 0 ] && ei0[ 1 ] == ei2[ 1 ] )
						return null;
					return new Segment( ei0[ 0 ], ei0[ 1 ], ei2[ 0 ], ei2[ 1 ] );
				}
				else
				{
					if ( ei1[ 0 ] == ei0[ 0 ] && ei1[ 1 ] == ei0[ 1 ] )
						return null;
					return new Segment( ei0[ 0 ], ei0[ 1 ], ei1[ 0 ], ei1[ 1 ] );
				}
			}
		}
	}

	static double[] edgeIntersection( final double xs, final double ys, final double zs,
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

	static String triangleToString( final Mesh mesh, final long id )
	{
		final StringBuilder str = new StringBuilder( id + ": " );

		final Triangles triangles = mesh.triangles();
		final Vertices vertices = mesh.vertices();
		final long v0 = triangles.vertex0( id );
		final double x0 = vertices.x( v0 );
		final double y0 = vertices.y( v0 );
		final double z0 = vertices.z( v0 );
		str.append( String.format( "(%f, %f, %f) - ", x0, y0, z0 ) );

		final long v1 = triangles.vertex1( id );
		final double x1 = vertices.x( v1 );
		final double y1 = vertices.y( v1 );
		final double z1 = vertices.z( v1 );
		str.append( String.format( "(%f, %f, %f) - ", x1, y1, z1 ) );

		final long v2 = triangles.vertex2( id );
		final double x2 = vertices.x( v2 );
		final double y2 = vertices.y( v2 );
		final double z2 = vertices.z( v2 );
		str.append( String.format( "(%f, %f, %f) - ", x2, y2, z2 ) );

		str.append( String.format( "N = (%4.2f, %4.2f, %4.2f) ",
				triangles.nx( id ), triangles.nz( id ), triangles.nz( id ) ) );

		return str.toString();
	}

	static class Segment implements Comparable< Segment >
	{

		protected final double xa;

		protected final double ya;

		protected final double xb;

		protected final double yb;

		protected Segment( final double x1, final double y1, final double x2, final double y2 )
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
				xa = x1;
				ya = y1;
				xb = x2;
				yb = y2;
//				throw new IllegalArgumentException( "Trying to create a segment of 0-length." );
			}

			this.xa = xa;
			this.ya = ya;
			this.xb = xb;
			this.yb = yb;
		}

		@Override
		public String toString()
		{
			return String.format( "S (%5.2f, %5.2f) -> (%5.2f, %5.2f)", xa, ya, xb, yb );
		}

		@Override
		public boolean equals( final Object obj )
		{
			if ( !( obj instanceof Segment ) )
				return false;
			final Segment o = ( Segment ) obj;
			if ( xa != o.xa )
				return false;
			if ( ya != o.ya )
				return false;
			if ( xb != o.xb )
				return false;
			if ( yb != o.yb )
				return false;
			return true;
		}

		@Override
		public int hashCode()
		{
			return Arrays.hashCode( new double[] { xa, xb, ya, yb } );
		}

		@Override
		public int compareTo( final Segment o )
		{
			final int dx1 = Double.compare( xa, o.xa );
			if ( dx1 != 0 )
				return dx1;
			final int dy1 = Double.compare( ya, o.ya );
			if ( dy1 != 0 )
				return dy1;
			final int dx2 = Double.compare( xb, o.xb );
			if ( dx2 != 0 )
				return dx2;
			return Double.compare( yb, o.yb );
		}

		/**
		 * Distance from point A of the segment to the specified point.
		 * 
		 * @param x
		 * @param y
		 * @return
		 */
		public double sqDistToA( final double x, final double y )
		{
			final double dx = x - xa;
			final double dy = y - ya;
			return dx * dx + dy * dy;
		}

		/**
		 * Distance from point B of the segment to the specified point.
		 * 
		 * @param x
		 * @param y
		 * @return
		 */
		public double sqDistToB( final double x, final double y )
		{
			final double dx = x - xb;
			final double dy = y - yb;
			return dx * dx + dy * dy;
		}
	}

	static class Triangle
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

	public static class Contour
	{

		public final TDoubleArrayList x;

		public final TDoubleArrayList y;

		protected double matchy;

		protected double matchx;

		protected boolean isClosed;

		Contour( final TDoubleArrayList x, final TDoubleArrayList y, final boolean isClosed )
		{
			this.x = x;
			this.y = y;
			this.isClosed = isClosed;
		}

		Contour()
		{
			this( new TDoubleArrayList(), new TDoubleArrayList(), false );
		}

		public static Contour init( final Segment start )
		{
			final Contour c = new Contour();
			c.x.add( start.xa );
			c.y.add( start.ya );
			c.matchx = start.xb;
			c.matchy = start.yb;
			c.isClosed = false;
			return c;
		}

		/**
		 * Returns <code>true</code> if we found a segment that matched this
		 * contour.
		 * 
		 * @param segments
		 * @return
		 */
		public boolean grow( final Collection< Segment > segments, final double tolerance )
		{
			double shortestDist = Double.POSITIVE_INFINITY;
			Segment best = null;
			for ( final Segment segment : segments )
			{
				final double d2a = segment.sqDistToA( matchx, matchy );
				if ( d2a < shortestDist )
				{
					shortestDist = d2a;
					best = segment;
				}
				final double d2b = segment.sqDistToB( matchx, matchy );
				if ( d2b < shortestDist )
				{
					shortestDist = d2b;
					best = segment;
				}
			}
			if ( shortestDist < tolerance * tolerance )
			{
				if ( best.sqDistToA( matchx, matchy ) < best.sqDistToB( matchx, matchy ) )
				{
					segments.remove( best );
					x.add( best.xa );
					y.add( best.ya );
					matchx = best.xb;
					matchy = best.yb;
				}
				else
				{
					segments.remove( best );
					x.add( best.xb );
					y.add( best.yb );
					matchx = best.xa;
					matchy = best.ya;
				}
				return true;
			}
			return false;

//			for ( final Segment segment : segments )
//			{
//				if ( segment.x1 == matchx && segment.y1 == matchy )
//				{
//					segments.remove( segment );
//					x.add( segment.x1 );
//					y.add( segment.y1 );
//					matchx = segment.x2;
//					matchy = segment.y2;
//					if ( segment.x2 == endx && segment.y2 == endy )
//						isClosed = true;
//					return true;
//				}
//				else if ( segment.x2 == matchx && segment.y2 == matchy )
//				{
//					segments.remove( segment );
//					x.add( segment.x2 );
//					y.add( segment.y2 );
//					matchx = segment.x1;
//					matchy = segment.y1;
//					if ( segment.x1 == endx && segment.y1 == endy )
//						isClosed = true;
//					return true;
//				}
//			}
//			return false;
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
