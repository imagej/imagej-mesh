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

import com.google.common.io.Files;
import org.scijava.plugin.AbstractHandlerService;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default service for working with STL formats
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
@Plugin(type = Service.class)
public class DefaultSTLService extends AbstractHandlerService<File, STLFormat>
	implements STLService
{

	@Override
	public List<STLFacet> read(final File file) throws IOException {
		final STLFormat format = getHandler(file);
		if (format == null) return null;
		return format.read(file);
	}

	@Override
	public void write(final File file, final List<STLFacet> facets) throws IOException {
		final STLFormat format = getHandler(file);
		if (format == null) return;
		final byte[] bytes = format.write(facets);

		Files.write(bytes, file);
	}

	// -- PTService methods --

	@Override
	public Class<STLFormat> getPluginType() {
		return STLFormat.class;
	}

	// -- Typed methods --

	@Override
	public Class<File> getType() {
		return File.class;
	}
}
