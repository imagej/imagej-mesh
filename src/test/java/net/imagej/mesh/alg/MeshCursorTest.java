package net.imagej.mesh.alg;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.imagej.mesh.Meshes;
import net.imagej.mesh.obj.Mesh;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class MeshCursorTest {

    @Test
    public void testCube() {
	// Make an image of a cube.
	final int expected = 100;
	final Img<ByteType> img = ArrayImgs.bytes(256, 256, 64);
	final FinalInterval cube = FinalInterval.createMinMax(50, 50, 20, 150, 150, 40);
	Views.interval(img, cube).forEach(p -> p.set((byte) expected));

	// Build a mesh from it.
	final Mesh m = Meshes.marchingCubes(img, expected / 2.);
	final Mesh mesh = Meshes.removeDuplicateVertices(m, 2);

	// Test we are iterating strictly inside the cube.
	final Cursor<ByteType> c1 = new MeshCursor<>(img.randomAccess(), mesh, new double[] { 1., 1., 1. });
	while (c1.hasNext()) {
	    c1.fwd();
	    assertEquals("Unexpected pixel value at " + Util.printCoordinates(c1), expected, c1.get().getInteger());
	    c1.get().set((byte) (expected / 2));
	}

	// Test that we iterated over all the cube.
	final Cursor<ByteType> c2 = Views.interval(img, cube).localizingCursor();
	while (c2.hasNext()) {
	    c2.fwd();
	    assertEquals("Did not iterate over pixel at " + Util.printCoordinates(c2), expected / 2,
		    c2.get().getInteger());
	}

    }

    public static void main(final String[] args) {
	new MeshCursorTest().testCube();
    }

}
