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

import net.imagej.mesh.Triangle;
import net.imagej.mesh.TrianglePool;
import net.imagej.mesh.Vertex3;

/**
 * A helper class to store a facet read from a STL file
 *
 * @author Richard Domander (Royal Veterinary College, London)
 * @author Kyle Harrington (University of Idaho, Moscow)
 */
public final class STLFacet extends Triangle {

	public final short attributeByteCount = 0;// sorry TODO
	
	public STLFacet(TrianglePool pool) {
		super(pool);
	}

	public Triangle init(final Vertex3 normal, final Vertex3 v0, final Vertex3 v1,
					final Vertex3 v2, final short attributeByteCount)
	{
		return super.init( v0, v1, v2, normal );
		//this.attributeByteCount = attributeByteCount;
	}
}