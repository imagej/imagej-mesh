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

package net.imagej.mesh;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.mastodon.collection.ref.RefArrayList;

/**
 * Tests for {@link Mesh} and {@link DefaultMesh}
 *
 * @author Kyle Harrington (University of Idaho, Moscow)
 */
public class MeshTest {

	@Test
	public void testWrite() throws Exception {
		final Mesh mesh = new DefaultMesh();
		final Vertex3Pool vp = mesh.getVertex3Pool();
		final TrianglePool tp = mesh.getTrianglePool();

		final Triangle facet = tp.create().init(vp.create().init(1, 0, 0), vp
			.create().init(0, 1, 0), vp.create().init(0, 0, 0), vp.create().init(0, 0,
				1));
		final Triangle facet2 = tp.create().init(vp.create().init(0, 0, 1), vp
			.create().init(0, 1, 0), vp.create().init(0, 0, 0), vp.create().init(-1,
				0, 0));
		final List<Triangle> facets = new RefArrayList<>(tp);
		facets.add(facet);
		facets.add(facet2);

		mesh.addFacet(facet);
		mesh.addFacet(facet2);

		assertEquals(mesh.getTriangles().size(), 2);

	}

}
