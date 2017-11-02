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

import net.imagej.mesh.Triangle;

import org.scijava.Priority;
import org.scijava.io.AbstractIOPlugin;
import org.scijava.io.IOPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * {@link IOPlugin} for handling STL files
 *
 * @author Richard Domander (Royal Veterinary College, London)
 * @see STLService
 */
@Plugin(type = IOPlugin.class, priority = Priority.LOW_PRIORITY - 1)
public class STLIOPlugin extends AbstractIOPlugin<List<Triangle>> {

	@Parameter(required = false)
	private STLService stlService;

	@Override
	public Class<List<Triangle>> getDataType() {
		return (Class) List.class;
	}

	@Override
	public boolean supportsOpen(final String source) {
		return stlService != null && stlService.supports(new File(source));
	}

	@Override
	public boolean supportsSave(final String destination) {
		return stlService != null && stlService.supports(new File(destination));
	}

	@Override
	public void save(final List<Triangle> data, final String destination)
		throws IOException, NullPointerException
	{
		stlService.write(new File(destination), data);
	}
}
