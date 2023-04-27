package net.imagej.mesh;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import io.scif.img.ImgOpener;
import io.scif.img.SCIFIOImgPlus;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.mesh.nio.BufferMesh;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Cast;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class ZSlicerDemo
{
	public static < T extends RealType< T > > void main( final String[] args ) throws IOException, URISyntaxException
	{
		System.out.println( "Opening image." );
		final String filePath = "samples/CElegans3D-smoothed-mask-orig-t7.tif";
		final List< SCIFIOImgPlus< ? > > imgs = new ImgOpener().openImgs( filePath );
		final ImgPlus< T > img = Cast.unchecked( imgs.get( 0 ) );
		final double[] pixelSizes = new double[] {
				img.averageScale( img.dimensionIndex( Axes.X ) ),
				img.averageScale( img.dimensionIndex( Axes.Y ) ),
				img.averageScale( img.dimensionIndex( Axes.Z ) ) };
		System.out.println( Util.printCoordinates( pixelSizes ) );

		// First channel is the smoothed version.
		System.out.println( "Marching cube on grayscale." );
		final RandomAccessibleInterval< T > smoothed;
		if ( img.dimensionIndex( Axes.CHANNEL ) >= 0 )
			smoothed = Views.hyperSlice( img, img.dimensionIndex( Axes.CHANNEL ), 0 );
		else
			smoothed = img;

		final double isoLevel = 250;
		final Mesh mesh1 = Meshes.marchingCubes( smoothed, isoLevel );
		final double z = 11.;
		runMesh( mesh1, z, pixelSizes, filePath, "-grayscale" );

		System.out.println( "Finished!" );
	}

	private static void runMesh( Mesh mesh, final double z, final double[] pixelSizes, final String filePath, final String suffix ) throws IOException
	{
		System.out.println( "Before removing duplicates: " + mesh );
		mesh = Meshes.removeDuplicateVertices( mesh, 2 );
		System.out.println( "After removing duplicates: " + mesh );
		System.out.println( "Scaling." );
		Meshes.scale( mesh, pixelSizes );

		System.out.println( "N connected components: " + Meshes.nConnectedComponents( mesh ) );
		System.out.println( "Splitting in connected components:" );
		int i = 0;
		for ( final BufferMesh cc : MeshConnectedComponents.iterable( mesh ) )
		{
			i++;
			System.out.println( " # " + i + ": " + cc );
//			new PLYMeshIO().save( cc, filePath + suffix + "-" + i + ".ply" );

			ZSlicer.slice( cc, z );

			break;
		}
//		System.out.println( "Simplifying to 10%:" );
//		i = 0;
//		for ( final BufferMesh cc : MeshConnectedComponents.iterable( mesh ) )
//		{
//			i++;
//			final Mesh simplified = Meshes.simplify( cc, 0.1f, 10 );
//			System.out.println( " # " + i + ": " + simplified );
//			new PLYMeshIO().save( simplified, filePath + suffix + "-simplified-" + i + ".ply" );
//		}

		System.out.println();
	}
}
