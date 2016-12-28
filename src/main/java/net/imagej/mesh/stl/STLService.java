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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.scijava.plugin.HandlerService;
import org.scijava.service.SciJavaService;

/**
 * Interface for service that works with STL formats.
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public interface STLService extends HandlerService<File, STLFormat>,
	SciJavaService
{

	/** Reads the data from the given file into a string. */
	List<STLFacet> read(File file) throws IOException;

	/** Writes the facets into the given file */
	void write(File file, List<STLFacet> facets) throws IOException;

	// -- HandlerService methods --

	/** Gets the STL format which best handles the given file. */
	@Override
	STLFormat getHandler(File file);

	// -- SingletonService methods --

	/** Gets the list of available STL formats. */
	@Override
	List<STLFormat> getInstances();

	// -- Typed methods --

	/** Gets whether the given file contains STL data in a supported format. */
	@Override
	boolean supports(File file);
}
