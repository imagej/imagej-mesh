package net.imagej.mesh.alg;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.imagej.mesh.Meshes;
import net.imagej.mesh.obj.Mesh;
import net.imagej.mesh.obj.nio.BufferMesh;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.Mask;
import net.imglib2.roi.Masks;
import net.imglib2.roi.Regions;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.WritableEllipsoid;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class MeshCursorTest {

    @Test
    public void testCube() {
	// Make an image of a cube.
	final int expected = 100;
	final Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(256, 256, 64);
	final FinalInterval cube = FinalInterval.createMinMax(50, 50, 20, 150, 150, 40);
	Views.interval(img, cube).forEach(p -> p.set((byte) expected));
	test(img, expected);

    }

    @Test
    public void testSmallCube() {
	final int expected = 100;
	final Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(256, 256, 64);
	final FinalInterval cube = FinalInterval.createMinMax(50, 50, 20, 52, 52, 22);
	Views.interval(img, cube).forEach(p -> p.set((byte) expected));
	test(img, expected);
    }

    @Test
    public void testHollowCube() {
	// Cube with a hole inside.
	final int expected = 100;
	final Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(256, 256, 64);
	final FinalInterval cube = FinalInterval.createMinMax(50, 50, 20, 150, 150, 40);
	Views.interval(img, cube).forEach(p -> p.set((byte) expected));
	final FinalInterval hole = FinalInterval.createMinMax(80, 80, 25, 120, 120, 35);
	Views.interval(img, hole).forEach(p -> p.set((byte) 0));
	test(img, expected);
    }

    @Test
    public void testSphere() {
	final int expected = 100;
	final Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(256, 256, 64);

	final WritableEllipsoid sphere = GeomMasks.closedEllipsoid(new double[] { 128, 128, 32 },
		new double[] { 20, 20, 20 });
	Regions.sample(sphere, img).forEach(p -> p.set((byte) expected));
	test(img, expected);
    }

    @Test
    public void testHollowSphere() {
	final int expected = 100;
	final Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(256, 256, 64);

	final WritableEllipsoid sphere = GeomMasks.closedEllipsoid(new double[] { 128, 128, 32 },
		new double[] { 20, 20, 20 });
	Regions.sample(sphere, img).forEach(p -> p.set((byte) expected));
	final WritableEllipsoid hole = GeomMasks.closedEllipsoid(new double[] { 128, 128, 32 },
		new double[] { 10, 10, 10 });
	Regions.sample(hole, img).forEach(p -> p.set((byte) 0));

	test(img, expected);
    }

    @Test
    public void testEllipsoid() {
	final int expected = 100;
	final Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(256, 256, 64);

	final WritableEllipsoid sphere = GeomMasks.closedEllipsoid(new double[] { 128, 128, 32 },
		new double[] { 50, 50, 10 });
	Regions.sample(sphere, img).forEach(p -> p.set((byte) expected));
	test(img, expected);
    }

    private static final void test(final Img<UnsignedByteType> img, final int expected) {
	// Make a mask for testing where we should iterate.
	final Mask mask = Masks.toMask(RealTypeConverters.convert(img, new BitType()));

	// Build a mesh from the source image..
	final Mesh m = Meshes.marchingCubes(img, expected / 2.);
	final Mesh m2 = Meshes.removeDuplicateVertices(m, 2);
	final BufferMesh mesh = new BufferMesh(m2.vertices().isize(), m2.triangles().isize());
	Meshes.calculateNormals(m2, mesh);

	// Test we are iterating strictly inside the cube.
	final Cursor<UnsignedByteType> c1 = new MeshCursor<>(img.randomAccess(), mesh, new double[] { 1., 1., 1. });
	while (c1.hasNext()) {
	    c1.fwd();
	    assertEquals("Unexpected pixel value at " + Util.printCoordinates(c1), expected, c1.get().getInteger());
	    c1.get().set((byte) (expected / 2));
	}

	// Test that we iterated over all the cube.
	final IterableInterval<UnsignedByteType> region = Regions.sampleWithMask(mask, img);
	final Cursor<UnsignedByteType> c2 = region.cursor();
	while (c2.hasNext()) {
	    c2.fwd();
	    assertEquals("Did not iterate over pixel at " + Util.printCoordinates(c2), expected / 2,
		    c2.get().getInteger());
	}
    }
}
