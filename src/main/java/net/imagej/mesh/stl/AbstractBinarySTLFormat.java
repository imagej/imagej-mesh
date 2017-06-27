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

import com.google.common.base.Strings;

import net.imagej.mesh.Triangle;
import net.imagej.mesh.TrianglePool;
import net.imagej.mesh.Vertex3Pool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.scijava.plugin.AbstractHandlerPlugin;
import org.scijava.util.FileUtils;

/**
 * An abstract superclass of binary {@link STLFormat} implementations
 * <p>
 * Binary STL formats include the standard and non-standard colour variants
 * </p>
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public abstract class AbstractBinarySTLFormat extends
	AbstractHandlerPlugin<File> implements STLFormat
{

	public static final int HEADER_BYTES = 80;
	public static final String HEADER = Strings.padEnd(
		"Binary STL created with ImageJ", HEADER_BYTES, '.');
	public static final int COUNT_BYTES = 4;
	public static final int FACET_START = HEADER_BYTES + COUNT_BYTES;
	public static final int FACET_BYTES = 50;

	@Override
	public List<Triangle> read(final TrianglePool tp, final Vertex3Pool vp, final File stlFile) throws IOException {
		if (stlFile == null) {
			return Collections.emptyList();
		}

		final byte[] data = Files.readAllBytes(Paths.get(stlFile
			.getAbsolutePath()));

		return readFacets(tp,vp,data);
	}

	@Override
	public boolean supports(final File file) {
		final String extension = FileUtils.getExtension(file);
		if (!EXTENSION.equalsIgnoreCase(extension)) {
			return false;
		}

		try (FileInputStream reader = new FileInputStream(file)) {
			final byte[] dataStart = new byte[5];
			reader.read(dataStart, 0, 5);
			// ASCII STL files begin with the line solid <name> whereas binary files
			// have an arbitrary header
			return !"solid".equals(Arrays.toString(dataStart));
		}
		catch (IOException e) {
			return false;
		}
	}

	@Override
	public Class<File> getType() {
		return File.class;
	}

	public abstract List<Triangle> readFacets(final TrianglePool tp, final Vertex3Pool vp, final byte[] data)
		throws IOException;
}
