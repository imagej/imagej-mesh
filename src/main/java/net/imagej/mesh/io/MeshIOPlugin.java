
package net.imagej.mesh.io;

import net.imagej.mesh.Mesh;

import org.scijava.io.IOPlugin;

/**
 * A plugin which reads and/or writes {@link Mesh} objects.
 *
 * @author Curtis Rueden
 * @author Kyle Harrington (University of Idaho, Moscow)
 */
public interface MeshIOPlugin extends IOPlugin<Mesh> {

	@Override
	default Class<Mesh> getDataType() {
		return Mesh.class;
	}
}
