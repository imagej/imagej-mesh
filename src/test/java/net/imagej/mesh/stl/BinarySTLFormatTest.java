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

package net.imagej.mesh.stl;

import static net.imagej.mesh.stl.AbstractBinarySTLFormat.FACET_BYTES;
import static net.imagej.mesh.stl.BinarySTLFormat.COUNT_BYTES;
import static net.imagej.mesh.stl.BinarySTLFormat.HEADER;
import static net.imagej.mesh.stl.BinarySTLFormat.HEADER_BYTES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.base.Strings;

import net.imagej.mesh.DefaultMesh;
import net.imagej.mesh.Mesh;
import net.imagej.mesh.Triangle;
import net.imagej.mesh.TrianglePool;
import net.imagej.mesh.Vertex3;
import net.imagej.mesh.Vertex3Pool;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mastodon.collection.ref.RefArrayList;

/**
 * Tests for {@link BinarySTLFormat}
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class BinarySTLFormatTest {

	private static final BinarySTLFormat format = new BinarySTLFormat();

	@Test
	public void testWriteNull() throws Exception {
		final byte[] bytes = format.write(null);

		assertNotNull(bytes);
		assertEquals(HEADER_BYTES + COUNT_BYTES, bytes.length);
	}

	@Test
	public void testWrite() throws Exception {
		Mesh mesh = new DefaultMesh();
		Vertex3Pool vp = mesh.getVertex3Pool();
		TrianglePool tp = mesh.getTrianglePool();
		
		final Triangle facet = tp.create().init(
				vp.create().init(1, 0, 0),
				vp.create().init(0, 1, 0), 
				vp.create().init(0, 0, 0), 
				vp.create().init(0, 0, 1));
		final Triangle facet2 = tp.create().init(
				vp.create().init(0, 0, 1),
				vp.create().init(0, 1, 0), 
				vp.create().init(0, 0, 0), 
				vp.create().init(-1, 0, 0));
		List<Triangle> facets = new RefArrayList<>(tp);
		facets.add(facet);
		facets.add(facet2);
		final int expectedSize = HEADER_BYTES + COUNT_BYTES + facets.size() *
				FACET_BYTES;

		final byte[] data = format.write(facets);
		final ByteBuffer buffer = ByteBuffer.wrap(data).order(
				ByteOrder.LITTLE_ENDIAN);

		assertEquals("Size of STL data is incorrect", expectedSize, buffer
				.capacity());

		byte[] headerBytes = new byte[HEADER_BYTES];
		buffer.get(headerBytes, 0, HEADER_BYTES);
		final String header = new String(headerBytes);
		assertEquals("Header of STL data is incorrect", header, HEADER);

		final int facetCount = buffer.getInt();
		assertEquals("Wrong number of facets written", facets.size(), facetCount);

		facets.forEach(f -> assertTriangle(f, buffer, 1e-12));
	}

	@Test
	public void testReadNull() throws Exception {
		final Vertex3Pool vp = new Vertex3Pool( 0 );
		final TrianglePool tp = new TrianglePool( vp, 0 );
		final List<Triangle> facets = format.read(tp,vp,null);

		assertNotNull(facets);
		assertEquals(0, facets.size());
	}

	@Test
	public void testReadBadSize() throws Exception {
		final byte[] data = new byte[61];
		final Vertex3Pool vp = new Vertex3Pool( 0 );
		final TrianglePool tp = new TrianglePool( vp, 0 );
		final List<Triangle> facets = format.readFacets(tp,vp,data);

		assertNotNull(facets);
		assertEquals(0, facets.size());
	}

	@Test
	public void testReadBadFacetCount() throws Exception {
		final ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES + COUNT_BYTES)
				.order(ByteOrder.LITTLE_ENDIAN);
		final byte[] header = Strings.padEnd("Header", 80, '.').getBytes();
		final int facetCount = 2;
		buffer.put(header);
		buffer.putInt(facetCount);

		final Vertex3Pool vp = new Vertex3Pool( 0 );
		final TrianglePool tp = new TrianglePool( vp, 0 );
		final List<Triangle> facets = format.readFacets(tp,vp,buffer.array());

		assertNotNull(facets);
		assertEquals(0, facets.size());
	}

	@Test
	public void testReadFacets() throws Exception {
		final int facetCount = 2;
		final short attributeByteCount = 0;
		final List<float[]> facet = Arrays.asList(new float[] { -2.0f, -1.0f,
				0.0f }, new float[] { 1.0f, 2.0f, 3.0f }, new float[] { 4.0f, 5.0f,
				6.0f }, new float[] { 7.0f, 8.0f, 9.0f });
		final List<float[]> facet1 = Arrays.asList(new float[] { 1.0f, 0.0f, 0.0f },
				new float[] { 1.0f, 0.0f, 0.0f }, new float[] { 1.0f, 0.0f, 0.0f },
				new float[] { 1.0f, 0.0f, 0.0f });
		final ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES + COUNT_BYTES +
				facetCount * FACET_BYTES).order(ByteOrder.LITTLE_ENDIAN);
		final byte[] header = Strings.padEnd("Header", 80, '.').getBytes();

		buffer.put(header);
		buffer.putInt(facetCount);
		writeFacet(buffer, facet, attributeByteCount);
		writeFacet(buffer, facet1, attributeByteCount);

		final Vertex3Pool vp = new Vertex3Pool( 1 );// TODO these are set to 1 because of a bug in mastodon-collection
		final TrianglePool tp = new TrianglePool( vp, 1 );
		final List<Triangle> facets = format.readFacets(tp,vp,buffer.array());

		
		assertTriangle(facets.get(0), facet);
	}

	private static void assertTriangle(final Triangle expectedFacet,
									final List<float[]> actualFacet)
	{
		assertVector(expectedFacet.getVertex(0), actualFacet.get(1));
		assertVector(expectedFacet.getVertex(1), actualFacet.get(2));
		assertVector(expectedFacet.getVertex(2), actualFacet.get(3));
	}

	private static void assertVector(final Vertex3 expectedVector,
									 final float[] actualVector)
	{
		assertEquals(expectedVector.getX(), actualVector[0], 1e-12);
		assertEquals(expectedVector.getY(), actualVector[1], 1e-12);
		assertEquals(expectedVector.getZ(), actualVector[2], 1e-12);
	}

	private static void writeFacet(final ByteBuffer buffer, List<float[]> vectors,
								   final short attributeByteCount)
	{
		vectors.forEach(v -> writeVector(buffer, v[0], v[1], v[2]));
		buffer.putShort(attributeByteCount);
	}

	private static void writeVector(final ByteBuffer buffer, final float x,
									final float y, final float z)
	{
		buffer.order(ByteOrder.LITTLE_ENDIAN).putFloat(x);
		buffer.order(ByteOrder.LITTLE_ENDIAN).putFloat(y);
		buffer.order(ByteOrder.LITTLE_ENDIAN).putFloat(z);
	}

	private static void assertTriangle(Triangle expected, ByteBuffer buffer,
									double delta)
	{
		//System.out.println(expected + " : " + buffer.array().toString());
		assertVector(expected.getNormal(), buffer, delta);
		assertVector(expected.getVertex(0), buffer, delta);
		assertVector(expected.getVertex(1), buffer, delta);
		assertVector(expected.getVertex(2), buffer, delta);
		final short attributeByteCount = buffer.getShort();// TODO but still need to shift

		// TODO
		//
		//assertEquals(expected.attributeByteCount, attributeByteCount);
	}

	private static void assertVector(final Vertex3 expected,
									 final ByteBuffer buffer, final double delta)
	{
		float bx = buffer.getFloat();
		float by = buffer.getFloat();
		float bz = buffer.getFloat();
		assertEquals(expected.getX(), bx, delta);
		assertEquals(expected.getY(), by, delta);
		assertEquals(expected.getZ(), bz, delta);
	}
}