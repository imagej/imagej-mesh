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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import net.imagej.mesh.Triangle;
import net.imagej.mesh.TrianglePool;
import net.imagej.mesh.Vertex3;
import net.imagej.mesh.Vertex3Pool;

import org.mastodon.collection.ref.RefArrayList;

/**
 * The {@link STLFormat} implementation for standard binary STL files
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class BinarySTLFormat extends AbstractBinarySTLFormat {

	@Override
	public List<Triangle> readFacets(final TrianglePool tp, final Vertex3Pool vp,
		final byte[] data)
	{
		final List<Triangle> facets = new RefArrayList<>(tp);

		if (data.length < FACET_START) {
			return facets;
		}

		final ByteBuffer buffer = ByteBuffer.wrap(data).order(
			ByteOrder.LITTLE_ENDIAN);
		final int facetCount = buffer.getInt(HEADER_BYTES);
		final int expectedSize = HEADER_BYTES + COUNT_BYTES + facetCount *
			FACET_BYTES;
		if (expectedSize != buffer.capacity()) {
			return facets;
		}

		buffer.position(FACET_START);
		for (int offset = FACET_START; offset < buffer.capacity(); offset +=
			FACET_BYTES)
		{
			final Triangle facet = readFacet(tp, vp, buffer);
			facets.add(facet);
		}

		return facets;
	}

	@Override
	public byte[] write(final List<Triangle> facets) {
		final int facetCount = facets == null ? 0 : facets.size();
		final int bytes = HEADER_BYTES + COUNT_BYTES + facetCount * FACET_BYTES;
		final ByteBuffer buffer = ByteBuffer.allocate(bytes).order(
			ByteOrder.LITTLE_ENDIAN);

		buffer.put(HEADER.getBytes());
		buffer.putInt(facetCount);

		if (facets == null) {
			return buffer.array();
		}

		facets.forEach(f -> writeFacet(buffer, f));

		return buffer.array();
	}

	private static void writeFacet(final ByteBuffer buffer,
		final Triangle facet)
	{
		// TODO Blend vertices
		writeVector(buffer, facet.getNormal());
		writeVector(buffer, facet.getVertex(0));
		writeVector(buffer, facet.getVertex(1));
		writeVector(buffer, facet.getVertex(2));
		buffer.putShort((short) 0); // Attribute byte count
	}

	@SuppressWarnings("unused")
	private static void writeVector(final ByteBuffer buffer, final float x,
		final float y, final float z)
	{
		buffer.putFloat(x);
		buffer.putFloat(y);
		buffer.putFloat(z);
	}

	private static void writeVector(final ByteBuffer buffer,
		final Vertex3 vector)
	{
		buffer.putFloat(vector.getX());
		buffer.putFloat(vector.getY());
		buffer.putFloat(vector.getZ());
	}

	private static Triangle readFacet(final TrianglePool tp, final Vertex3Pool vp,
		final ByteBuffer buffer)
	{
		final Triangle tref = tp.createRef();

		final Vertex3 normal = readVector(vp, buffer);
		final Vertex3 vertex0 = readVector(vp, buffer);
		final Vertex3 vertex1 = readVector(vp, buffer);
		final Vertex3 vertex2 = readVector(vp, buffer);
		@SuppressWarnings("unused")
		final short attributeByteCount = buffer.getShort();// sorry TODO

		return tp.create(tref).init(vertex0, vertex1, vertex2, normal);
	}

	private static Vertex3 readVector(final Vertex3Pool vp,
		final ByteBuffer buffer)
	{
		final Vertex3 vref = vp.createRef();

		final float x = buffer.getFloat();
		final float y = buffer.getFloat();
		final float z = buffer.getFloat();

		return vp.create(vref).init(x, y, z, 0, 0, 0, 0, 0, 0);
	}
}
