/*-
 * #%L
 * 3D mesh structures for ImageJ.
 * %%
 * Copyright (C) 2016 - 2018 University of Idaho, Royal Veterinary College, and
 * Board of Regents of the University of Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
		assertEquals(5287, bytes.length);
	}

	@Test
	public void testAsciiWrite() throws Exception {
		final PLYMeshIO meshIO = new PLYMeshIO();
		final Mesh mesh = sampleMesh(meshIO);
		final byte[] bytes = meshIO.writeAscii(mesh);
		assertEquals(7420, bytes.length);
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
