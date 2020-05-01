package net.imagej.mesh;

import net.imagej.mesh.naive.NaiveDoubleMesh;
import net.imglib2.Point;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MeshesTest {

	private static final Point p1 = new Point(0, 0, 0);
	private static final Point p2 = new Point(1, 0, 0);
	private static final Point p3 = new Point(1, 1, 0);
	private static final Point p4 = new Point(1, 1, 1);

	@Test
	public void testRemoveDuplicateVertices() {

		Mesh mesh = createMeshWithNoise();

		Mesh res = Meshes.removeDuplicateVertices(mesh, 2);
		assertEquals(4, res.vertices().size());

		assertEquals(p1.getDoublePosition(0), res.vertices().x(0), 0.0001);
		assertEquals(p1.getDoublePosition(1), res.vertices().y(0), 0.0001);
		assertEquals(p1.getDoublePosition(2), res.vertices().z(0), 0.0001);

		assertEquals(p2.getDoublePosition(0), res.vertices().x(1), 0.0001);
		assertEquals(p2.getDoublePosition(1), res.vertices().y(1), 0.0001);
		assertEquals(p2.getDoublePosition(2), res.vertices().z(1), 0.0001);

		assertEquals(p3.getDoublePosition(0), res.vertices().x(2), 0.0001);
		assertEquals(p3.getDoublePosition(1), res.vertices().y(2), 0.0001);
		assertEquals(p3.getDoublePosition(2), res.vertices().z(2), 0.0001);

		assertEquals(p4.getDoublePosition(0), res.vertices().x(3), 0.0001);
		assertEquals(p4.getDoublePosition(1), res.vertices().y(3), 0.0001);
		assertEquals(p4.getDoublePosition(2), res.vertices().z(3), 0.0001);

		res = Meshes.removeDuplicateVertices(mesh, 3);
		assertEquals(6, res.vertices().size());
	}

	private static Mesh createMeshWithNoise() {
		Mesh mesh = new NaiveDoubleMesh();

		// Make mesh with two triangles sharing two points with each other.
		// The points are a bit off in the third decimal digit.
		mesh.vertices().add(p1.getDoublePosition(0) + 0.001, p1.getDoublePosition(1) - 0.001, p1.getDoublePosition(2) - 0.004);
		mesh.vertices().add(p2.getDoublePosition(0) + 0.004, p2.getDoublePosition(1) - 0.000, p2.getDoublePosition(2) + 0.002);
		mesh.vertices().add(p3.getDoublePosition(0) - 0.002, p3.getDoublePosition(1) + 0.003, p3.getDoublePosition(2) + 0.001);
		mesh.triangles().add(0, 1, 2);
		mesh.vertices().add(p2.getDoublePosition(0) + 0.001, p2.getDoublePosition(1) - 0.001, p2.getDoublePosition(2) - 0.004);
		mesh.vertices().add(p4.getDoublePosition(0) + 0.004, p4.getDoublePosition(1) - 0.000, p4.getDoublePosition(2) + 0.002);
		mesh.vertices().add(p3.getDoublePosition(0) + 0.002, p3.getDoublePosition(1) + 0.003, p3.getDoublePosition(2) + 0.001);
		mesh.triangles().add(3, 4, 5);
		return mesh;
	}

}
