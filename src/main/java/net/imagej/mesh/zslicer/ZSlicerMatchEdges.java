package net.imagej.mesh.zslicer;

import static net.imagej.mesh.zslicer.ZSlicer.edgeIntersection;
import static net.imagej.mesh.zslicer.ZSlicer.maxZ;
import static net.imagej.mesh.zslicer.ZSlicer.minZ;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.procedure.TLongProcedure;
import net.imagej.mesh.Mesh;
import net.imagej.mesh.Triangles;
import net.imagej.mesh.Vertices;
import net.imagej.mesh.util.MeshUtil;
import net.imagej.mesh.zslicer.ZSlicer.Contour;

public class ZSlicerMatchEdges
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
		final Collection< SegmentExt > segments = new ArrayList<>();
				// new HashSet<>();
		final double[] cross = new double[ 3 ]; // holder for cross product.
		for ( int i = 0; i < intersecting.size(); i++ )
		{
			final long id = intersecting.getQuick( i );
			final SegmentExt segment = triangleIntersection( mesh, id, z, cross );
			if ( segment != null )
				segments.add( segment );
		}

		// Sort them by x then y etc.
		final ArrayList< SegmentExt > sorted = new ArrayList<>( segments );
		sorted.sort( Comparator.comparing( s -> s.ea ) );

		// Build contours from segments.
		final ArrayDeque< SegmentExt > deque = new ArrayDeque<>( sorted );
		final List< ContourExt > contours = new ArrayList<>();
		while ( !deque.isEmpty() )
		{
			final SegmentExt start = deque.pop();
			if ( deque.isEmpty() )
				break;

			final ContourExt contour = ContourExt.init( start );
			contours.add( contour );

			while ( contour.grow( deque, zPixelSize ) && !contour.isClosed() )
			{}
		}
		return contours.stream().map( ContourExt::toContour ).collect( Collectors.toList() );
	}

	private static SegmentExt triangleIntersection( final Mesh mesh, final long id, final double z, final double[] cross )
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

		/*
		 * Triangle orientation. Cross product between plane normal and triangle
		 * normal.
		 */
		MeshUtil.cross( 0, 0, 1., mesh.triangles().nx( id ), mesh.triangles().ny( id ), mesh.triangles().nz( id ), cross );

		/*
		 * Because we shifted the vertices position and the intersecting plane
		 * position, we are sure we are not crossing any vertex.
		 * 
		 * Crossing two edges, but what ones?
		 */
		final double[] ei0 = edgeIntersection( x0, y0, z0, x1, y1, z1, z ); // v0 -> v1
		final double[] ei1 = edgeIntersection( x0, y0, z0, x2, y2, z2, z ); // v0 -> v2
		final double[] ei2 = edgeIntersection( x1, y1, z1, x2, y2, z2, z ); // v1 -> v2

		// Careful about winding number
		final double xa;
		final double xb;
		final double ya;
		final double yb;
		final long ea;
		final long eb;
		if ( ei0 == null ) // not v0 -> v1
		{
			xa = ei1[0];
			ya = ei1[1];
			xb = ei2[0];
			yb = ei2[1];
			ea = MeshUtil.edgeID( (int) v2, (int) v0 );
			eb = MeshUtil.edgeID( (int) v1, (int) v2 );
		}
		else if ( ei1 == null ) // not v0 -> v2
		{
			xa = ei2[0];
			ya = ei2[1];
			xb = ei0[0];
			yb = ei0[1];
			ea = MeshUtil.edgeID( (int) v1, (int) v2 );
			eb = MeshUtil.edgeID( (int) v0, (int) v1 );
		}
		else // not v1 -> v2
		{
			xa = ei0[0];
			ya = ei0[1];
			xb = ei1[0];
			yb = ei1[1];
			ea = MeshUtil.edgeID( (int) v0, (int) v1 );
			eb = MeshUtil.edgeID( (int) v2, (int) v0 );
		}
		
		final double dx = xb-xa;
		final double dy = yb-ya;
		final double d = MeshUtil.dotProduct( cross[ 0 ], cross[ 1 ], cross[ 2 ], dx, dy, 0 );
		if ( d > 0 )
		{
			return new SegmentExt( xa, ya, ea, eb );
		}
		else
		{
			// Flip.
			return new SegmentExt( xb, yb, eb, ea );
		}
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

	private static final class SegmentExt
	{

		private final long ea;

		private final long eb;

		private final double xa;

		private final double ya;

		public SegmentExt( final double xa, final double ya, final long ea, final long eb )
		{
			this.xa = xa;
			this.ya = ya;
			this.ea = ea;
			this.eb = eb;
		}

		@Override
		public String toString()
		{
			return String.format( "S %d -> %d", ea, eb );
//			return String.format( "S %d -> %d (%s) -> (%s)", ea, edgeStr( ea ), edgeStr( eb ) );
		}
	}

	private static final class ContourExt
	{

		private final TDoubleArrayList x = new TDoubleArrayList();

		private final TDoubleArrayList y = new TDoubleArrayList();

		private final TLongArrayList es = new TLongArrayList();

		protected boolean isClosed;

		private long matche;

		private final long ende;

		protected ContourExt( final SegmentExt start )
		{
			x.add( start.xa );
			y.add( start.ya );
			isClosed = false;
			matche = start.eb;
			ende = start.ea;
			es.add( start.ea );
		}

		public boolean grow( final ArrayDeque< SegmentExt > segments, final double zPixelSize )
		{

			for ( final SegmentExt segment : segments )
			{
				if ( segment.ea == matche )
				{
					segments.remove( segment );
					es.add( segment.ea );
					x.add( segment.xa );
					y.add( segment.ya );
					matche = segment.eb;
					if ( segment.eb == ende )
						isClosed = true;
					return true;
				}
			}
			return false;
		}

		public boolean isClosed()
		{
			return isClosed;
		}

		public Contour toContour()
		{
			return new Contour( x, y, isClosed );
		}

		public static ContourExt init( final SegmentExt start )
		{
			return new ContourExt( start );
		}

		@Override
		public String toString()
		{
			final StringBuilder str = new StringBuilder();
			str.append( "ending at E: " + ende + '\n' );
			es.forEach( new TLongProcedure()
			{

				@Override
				public boolean execute( final long value )
				{
					str.append( "E: " + value + '\n' );
					return true;
				}
			} );
			return str.toString();
		}
	}
}
