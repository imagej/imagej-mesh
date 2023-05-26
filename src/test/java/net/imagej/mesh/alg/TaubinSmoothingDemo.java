package net.imagej.mesh.alg;

import java.io.IOException;
import java.util.Random;

import net.imagej.mesh.Mesh;
import net.imagej.mesh.Meshes;
import net.imagej.mesh.alg.TaubinSmoothing.TaubinWeightType;
import net.imagej.mesh.io.ply.PLYMeshIO;
import net.imglib2.FinalInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class TaubinSmoothingDemo {

    public static void main(final String[] args) throws IOException {

	// Make a cube
	final Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(100, 100, 100);
	final FinalInterval cube = Intervals.createMinMax(25, 25, 25, 75, 75, 75);
	Views.interval(img, cube).forEach(p -> p.setOne());

	final Mesh m1 = Meshes.marchingCubes(img, 0.5);
	final Mesh mesh = Meshes.removeDuplicateVertices(m1, 2);

	// Add noise.
	final Random ran = new Random(45l);
	for (int i = 0; i < mesh.vertices().size(); i++) {
	    final double x = mesh.vertices().x(i);
	    final double y = mesh.vertices().y(i);
	    final double z = mesh.vertices().z(i);
	    
	    mesh.vertices().set(i, 
		    x + ran.nextDouble() - 0.5,
		    y + ran.nextDouble() - 0.5,
		    z + ran.nextDouble() - 0.5 );
	}

	new PLYMeshIO().save(mesh, "samples/BeforeSmooth.ply");
	new PLYMeshIO().save(TaubinSmoothing.smooth(mesh), "samples/SmoothedBit.ply");
	new PLYMeshIO().save(TaubinSmoothing.smooth(mesh, 10, 0.5, -0.53, TaubinWeightType.NAIVE),
		"samples/SmoothedMore.ply");
    }

}
