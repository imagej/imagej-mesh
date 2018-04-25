/*-
 * #%L
 * SciJava I/O plugins for 3D mesh structures.
 * %%
 * Copyright (C) 2016 University of Idaho, Royal Veterinary College, and
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

package net.imagej.mesh.io.stl;

import static net.imagej.mesh.io.stl.STLMeshIO.COUNT_BYTES;
import static net.imagej.mesh.io.stl.STLMeshIO.FACET_BYTES;
import static net.imagej.mesh.io.stl.STLMeshIO.HEADER;
import static net.imagej.mesh.io.stl.STLMeshIO.HEADER_BYTES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.base.Strings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.imagej.mesh.Mesh;
import net.imagej.mesh.Triangle;
import net.imagej.mesh.naive.NaiveFloatMesh;

import org.junit.Test;

/**
 * Tests {@link STLMeshIO}.
 *
 * @author Richard Domander (Royal Veterinary College, London)
 * @author Kyle Harrington (University of Idaho, Moscow)
 * @author Curtis Rueden
 */
public class STLMeshIOTest {

	@Test
	public void testWriteNull() throws Exception {
		final STLMeshIO meshIO = new STLMeshIO();
		final byte[] bytes = meshIO.write(null);

		assertNotNull(bytes);
		assertEquals(HEADER_BYTES + COUNT_BYTES, bytes.length);
	}

	@Test
	public void testWrite() throws Exception {
		final Mesh mesh = new NaiveFloatMesh();

		final double t0v0x = 1, t0v0y = 0, t0v0z = 0;
		final double t0v1x = 0, t0v1y = 1, t0v1z = 0;
		final double t0v2x = 0, t0v2y = 0, t0v2z = 0;
		final double t0nx = 0, t0ny = 0, t0nz = 1;
		mesh.triangles().add( //
			t0v0x, t0v0y, t0v0z, //
			t0v1x, t0v1y, t0v1z, //
			t0v2x, t0v2y, t0v2z, //
			t0nx, t0ny, t0nz); //

		final double t1v0x = 0, t1v0y = 0, t1v0z = 1;
		final double t1v1x = 0, t1v1y = 1, t1v1z = 0;
		final double t1v2x = 0, t1v2y = 0, t1v2z = 0;
		final double t1nx = -1, t1ny = 0, t1nz = 0;
		mesh.triangles().add( //
			t1v0x, t1v0y, t1v0z, //
			t1v1x, t1v1y, t1v1z, //
			t1v2x, t1v2y, t1v2z, //
			t1nx, t1ny, t1nz); //

		final long expectedSize = HEADER_BYTES + COUNT_BYTES + //
			mesh.triangles().size() * FACET_BYTES;

		final STLMeshIO meshIO = new STLMeshIO();
		final byte[] data = meshIO.write(mesh);
		final ByteBuffer buffer = ByteBuffer.wrap(data).order(
			ByteOrder.LITTLE_ENDIAN);

		assertEquals("Size of STL data is incorrect", //
			expectedSize, buffer.capacity());

		final byte[] headerBytes = new byte[HEADER_BYTES];
		buffer.get(headerBytes, 0, HEADER_BYTES);
		final String header = new String(headerBytes);
		assertEquals("Header of STL data is incorrect", header, HEADER);

		final int facetCount = buffer.getInt();
		assertEquals("Wrong number of facets written", //
			mesh.triangles().size(), facetCount);

		mesh.triangles().forEach(f -> assertTriangle(f, buffer, 1e-12));
	}

	@Test(expected = NullPointerException.class)
	public void testReadNull() throws Exception {
		final STLMeshIO meshIO = new STLMeshIO();
		final Mesh mesh = new NaiveFloatMesh();
		meshIO.read(mesh, (byte[]) null);
	}

	@Test
	public void testReadBadSize() throws Exception {
		final STLMeshIO meshIO = new STLMeshIO();
		final Mesh mesh = new NaiveFloatMesh();
		meshIO.read(mesh, new byte[61]);

		assertEquals(0, mesh.vertices().size());
		assertEquals(0, mesh.triangles().size());
	}

	@Test
	public void testReadBadFacetCount() throws Exception {
		final ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES + COUNT_BYTES)
			.order(ByteOrder.LITTLE_ENDIAN);
		final byte[] header = Strings.padEnd("Header", 80, '.').getBytes();
		final int facetCount = 2;
		buffer.put(header);
		buffer.putInt(facetCount);

		final STLMeshIO meshIO = new STLMeshIO();
		final Mesh mesh = new NaiveFloatMesh();
		meshIO.read(mesh, buffer.array());

		assertEquals(0, mesh.vertices().size());
		assertEquals(0, mesh.triangles().size());
	}

	@Test
	public void testReadFacets() throws Exception {
		final int facetCount = 2;
		final short attributeByteCount = 0;
		final List<float[]> facet0 = Arrays.asList(//
			new float[] { -2.0f, -1.0f, 0.0f }, //
			new float[] { 1.0f, 2.0f, 3.0f }, //
			new float[] { 4.0f, 5.0f, 6.0f }, //
			new float[] { 7.0f, 8.0f, 9.0f });
		final List<float[]> facet1 = Arrays.asList(//
			new float[] { 1.0f, 0.0f, 0.0f }, //
			new float[] { 1.0f, 0.0f, 0.0f }, //
			new float[] { 1.0f, 0.0f, 0.0f }, //
			new float[] { 1.0f, 0.0f, 0.0f });
		final ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES + COUNT_BYTES +
			facetCount * FACET_BYTES).order(ByteOrder.LITTLE_ENDIAN);
		final byte[] header = Strings.padEnd("Header", 80, '.').getBytes();

		buffer.put(header);
		buffer.putInt(facetCount);
		writeFacet(buffer, facet0, attributeByteCount);
		writeFacet(buffer, facet1, attributeByteCount);

		final STLMeshIO meshIO = new STLMeshIO();
		final Mesh mesh = new NaiveFloatMesh();
		meshIO.read(mesh, buffer.array());

		final Iterator<Triangle> triangles = mesh.triangles().iterator();
		assertTriangle(facet0, triangles.next());
		assertTriangle(facet1, triangles.next());
	}

	// -- Helper methods --

	private static void assertTriangle(final List<float[]> expected,
		final Triangle actual)
	{
		assertVector(expected.get(0), actual.nxf(), actual.nyf(), actual.nzf());
		assertVector(expected.get(1), actual.v0xf(), actual.v0yf(), actual.v0zf());
		assertVector(expected.get(2), actual.v1xf(), actual.v1yf(), actual.v1zf());
		assertVector(expected.get(3), actual.v2xf(), actual.v2yf(), actual.v2zf());
	}

	private static void assertVector(final float[] expected, //
		final float x, final float y, final float z)
	{
		final double delta = 1e-12;
		assertEquals(expected[0], x, delta);
		assertEquals(expected[1], y, delta);
		assertEquals(expected[2], z, delta);
	}

	private static void writeFacet(final ByteBuffer buffer,
		final List<float[]> vectors, final short attributeByteCount)
	{
		vectors.forEach(v -> writeVector(buffer, v[0], v[1], v[2]));
		buffer.putShort(attributeByteCount);
	}

	private static void writeVector(final ByteBuffer buffer, //
		final float x, final float y, final float z)
	{
		buffer.order(ByteOrder.LITTLE_ENDIAN).putFloat(x);
		buffer.order(ByteOrder.LITTLE_ENDIAN).putFloat(y);
		buffer.order(ByteOrder.LITTLE_ENDIAN).putFloat(z);
	}

	private static void assertTriangle(final Triangle expected,
		final ByteBuffer buffer, final double delta)
	{
		assertEquals(expected.nxf(), buffer.getFloat(), delta);
		assertEquals(expected.nyf(), buffer.getFloat(), delta);
		assertEquals(expected.nzf(), buffer.getFloat(), delta);
		assertEquals(expected.v0xf(), buffer.getFloat(), delta);
		assertEquals(expected.v0yf(), buffer.getFloat(), delta);
		assertEquals(expected.v0zf(), buffer.getFloat(), delta);
		assertEquals(expected.v1xf(), buffer.getFloat(), delta);
		assertEquals(expected.v1yf(), buffer.getFloat(), delta);
		assertEquals(expected.v1zf(), buffer.getFloat(), delta);
		assertEquals(expected.v2xf(), buffer.getFloat(), delta);
		assertEquals(expected.v2yf(), buffer.getFloat(), delta);
		assertEquals(expected.v2zf(), buffer.getFloat(), delta);

		buffer.getShort(); // attributeByteCount
		// TODO
		// assertEquals(expected.attributeByteCount, attributeByteCount);
	}
}
