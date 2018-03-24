
package net.imagej.mesh.ply;

import java.io.File;
import java.io.IOException;

import net.imagej.mesh.Mesh;

import org.scijava.plugin.HandlerPlugin;

/**
 * An interface for reading and writing PLY files PLY specs:
 * http://www.cs.virginia.edu/~gfx/Courses/2001/Advanced.spring.01/plylib/Ply.txt
 *
 * @author Kyle Harrington (University of Idaho, Moscow)
 */
public interface PLYFormat extends HandlerPlugin<File> {

	String EXTENSION = "ply";

	Mesh read(final File plyFile) throws IOException;

	/** Writes the facets into a byte[] that can then be saved into a file */
	byte[] writeBinary(final Mesh mesh) throws IOException;

	/** Writes the facets into a byte[] that can then be saved into a file */
	byte[] writeAscii(final Mesh mesh) throws IOException;
}
