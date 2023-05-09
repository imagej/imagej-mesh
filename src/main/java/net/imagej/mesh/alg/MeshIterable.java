package net.imagej.mesh.alg;

import java.util.Iterator;

import net.imagej.mesh.Meshes;
import net.imagej.mesh.obj.Mesh;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RealInterval;

public class MeshIterable<T> implements IterableInterval<T> {

    private final double[] calibration;

    private final RandomAccessible<T> ra;

    private final Mesh mesh;

    private final RealInterval boundingBox;

    /**
     * Creates an iterable for the pixels of the specified image inside the
     * specified mesh. A calibration array (pixel sizes) is used to transform the
     * pixel coordinates into mesh coordinates. Recompute the mesh bounding-box.
     * 
     * @param img         the random accessible to iterate over. It is the caller
     *                    responsibility to ensure the random accessible is extended
     *                    to accommodate the mesh coordinates.
     * @param mesh        the mesh to iterate in.
     * @param calibration the calibration array, used to map pixel coordinates to
     *                    mesh coordinates
     *                    (<code>mesh_x = pixel_x * calibration[0]</code> etc).
     */
    public MeshIterable(final RandomAccessible<T> ra, final Mesh mesh, final double[] calibration) {
	this(ra, mesh, calibration, Meshes.boundingBox(mesh));
    }

    /**
     * Creates an iterable for the pixels of the specified image inside the
     * specified mesh. A calibration array (pixel sizes) is used to transform the
     * pixel coordinates into mesh coordinates.
     * 
     * @param img         the random accessible to iterate over. It is the caller
     *                    responsibility to ensure the random accessible is extended
     *                    to accommodate the mesh coordinates.
     * @param mesh        the mesh to iterate in.
     * @param calibration the calibration array, used to map pixel coordinates to
     *                    mesh coordinates
     *                    (<code>mesh_x = pixel_x * calibration[0]</code> etc).
     * @param boundingBox the bounding-box if it's available, to avoid recomputing
     *                    it.
     */
    public MeshIterable(final RandomAccessible<T> ra, final Mesh mesh, final double[] calibration,
	    final RealInterval boundingBox) {
	this.ra = ra;
	this.mesh = mesh;
	this.calibration = calibration;
	this.boundingBox = boundingBox;
    }

    @Override
    public int numDimensions() {
	return 3;
    }

    @Override
    public long size() {
	// Costly!
	long size = 0;
	for (@SuppressWarnings("unused")
	final T t : this)
	    size++;

	return size;
    }

    @Override
    public T firstElement() {
	return cursor().next();
    }

    @Override
    public Object iterationOrder() {
	return this;
    }

    @Override
    public Iterator<T> iterator() {
	return cursor();
    }

    @Override
    public long min(final int d) {
	return Math.round(boundingBox.realMin(d) / calibration[d]);
    }

    @Override
    public long max(final int d) {
	return Math.round(boundingBox.realMax(d) / calibration[d]);
    }

    @Override
    public Cursor<T> cursor() {
	return new MeshCursor<>(ra.randomAccess(), mesh, calibration, boundingBox);
    }

    @Override
    public Cursor<T> localizingCursor() {
	return cursor();
    }
}
