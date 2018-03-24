
package net.imagej.mesh;

import java.nio.FloatBuffer;

import org.mastodon.pool.BufferMappedElement;
import org.mastodon.pool.BufferMappedElementArray;
import org.mastodon.pool.Pool;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.IndexAttribute;

/**
 * The RefPool for storing Triangle's
 *
 * @author Tobias Pietzsch (MPI-CBG)
 * @author Kyle Harrington (University of Idaho, Moscow)
 */
public class TrianglePool extends Pool<Triangle, BufferMappedElement> {

	final IndexAttribute<Triangle> iv1;
	final IndexAttribute<Triangle> iv2;
	final IndexAttribute<Triangle> iv3;
	final IndexAttribute<Triangle> normal;

	Vertex3Pool vertex3Pool;

	final TriangleLayout triangleLayout;

	public TrianglePool(final Vertex3Pool vertex3Pool,
		final int initialCapacity)
	{
		super(initialCapacity, new TriangleLayout(), Triangle.class,
			SingleArrayMemPool.factory(BufferMappedElementArray.factory));
		this.triangleLayout = new TriangleLayout();
		this.vertex3Pool = vertex3Pool;
		iv1 = new IndexAttribute<>(triangleLayout.v1, this);
		iv2 = new IndexAttribute<>(triangleLayout.v2, this);
		iv3 = new IndexAttribute<>(triangleLayout.v3, this);
		normal = new IndexAttribute<>(triangleLayout.normal, this);
	}

	public Triangle create() {
		return super.create(createRef());
	}

	@Override
	public Triangle create(final Triangle obj) {
		return super.create(obj);
	}

	@Override
	public void delete(final Triangle obj) {
		super.delete(obj);
	}

	@Override
	protected Triangle createEmptyRef() {
		return new Triangle(this);
	}

	public FloatBuffer getFloatBuffer() {
		final SingleArrayMemPool<BufferMappedElementArray, ?> memPool =
			(SingleArrayMemPool<BufferMappedElementArray, ?>) getMemPool();
		final BufferMappedElementArray dataArray = memPool.getDataArray();
		return dataArray.getBuffer().asFloatBuffer();
	}
}
