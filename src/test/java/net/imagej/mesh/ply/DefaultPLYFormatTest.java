
package net.imagej.mesh.ply;

import static org.junit.Assert.assertEquals;

import java.io.File;

import net.imagej.mesh.Mesh;

import org.junit.Test;

public class DefaultPLYFormatTest {

	@Test
	public void testRead() throws Exception {
		final DefaultPLYFormat format = new DefaultPLYFormat();

		final Mesh m = format.read(new File(getClass().getResource("cone.ply")
			.getFile()));

		assertEquals(158, m.getTriangles().size());
		assertEquals(81, m.getVertices().size());
	}

	@Test
	public void testBinaryWrite() throws Exception {
		final DefaultPLYFormat format = new DefaultPLYFormat();

		System.out.println(getClass().getResource("cone.ply").getFile());

		final Mesh m = format.read(new File(getClass().getResource("cone.ply")
			.getFile()));

		final byte[] bytes = format.writeBinary(m);

		// FileOutputStream fos = new
		// FileOutputStream("/Users/kharrington/git/imagej-mesh/test_binary.ply");
		// fos.write(bytes);
		// fos.close();

		assertEquals(5306, bytes.length);
	}

	@Test
	public void testAsciiWrite() throws Exception {
		final DefaultPLYFormat format = new DefaultPLYFormat();

		System.out.println(getClass().getResource("cone.ply").getFile());

		final Mesh m = format.read(new File(getClass().getResource("cone.ply")
			.getFile()));

		final byte[] bytes = format.writeAscii(m);

		// FileOutputStream fos = new
		// FileOutputStream("/Users/kharrington/git/imagej-mesh/test_ascii.ply");
		// fos.write(bytes);
		// fos.close();

		assertEquals(7762, bytes.length);
	}
}
