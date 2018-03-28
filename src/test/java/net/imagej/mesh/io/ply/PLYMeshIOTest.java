
package net.imagej.mesh.io.ply;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.imagej.mesh.Mesh;

import org.junit.Test;

/**
 * Tests {@link PLYMeshIO}.
 *
 * @author Kyle Harrington (University of Idaho, Moscow)
 * @author Curtis Rueden
 */
public class PLYMeshIOTest {

	@Test
	public void testOpen() throws Exception {
		final PLYMeshIO meshIO = new PLYMeshIO();
		final Mesh m = sampleMesh(meshIO);
		assertEquals(158, m.triangles().size());
		assertEquals(81, m.vertices().size());
	}

	@Test
	public void testBinaryWrite() throws Exception {
		final PLYMeshIO meshIO = new PLYMeshIO();
		final Mesh mesh = sampleMesh(meshIO);
		final byte[] bytes = meshIO.writeBinary(mesh);
		assertEquals(5304, bytes.length);
	}

	@Test
	public void testAsciiWrite() throws Exception {
		final PLYMeshIO meshIO = new PLYMeshIO();
		final Mesh mesh = sampleMesh(meshIO);
		final byte[] bytes = meshIO.writeAscii(mesh);
		assertEquals(7762, bytes.length);
	}

	// -- Helper methods --

	private Mesh sampleMesh(final PLYMeshIO meshIO) throws URISyntaxException,
		IOException
	{
		final URI meshURI = getClass().getResource("cone.ply").toURI();
		assumeTrue(new File(meshURI).exists());
		return meshIO.open(meshURI.getPath());
	}
}
