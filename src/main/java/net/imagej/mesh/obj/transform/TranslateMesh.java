package net.imagej.mesh.obj.transform;

import java.util.Iterator;

import net.imagej.mesh.obj.Mesh;
import net.imagej.mesh.obj.Triangles;
import net.imagej.mesh.obj.Vertex;
import net.imagej.mesh.obj.Vertices;
import net.imglib2.RealLocalizable;

/**
 * Read-only view of a mesh where vertices are translated by a fixed amount.
 * Does not copy the source mesh.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class TranslateMesh implements Mesh {

    private final Mesh in;
    private final TranslatedVertices vertices;

    public static final Mesh translate(final Mesh in, final RealLocalizable t) {
	return new TranslateMesh(in, t);
    }

    private TranslateMesh(final Mesh in, final RealLocalizable t) {
	this.in = in;
	this.vertices = new TranslatedVertices(in.vertices(), t);
    }

    @Override
    public Vertices vertices() {
	return vertices;
    }

    @Override
    public Triangles triangles() {
	return in.triangles();
    }

    private static final class TranslatedVertices implements Vertices {

	private final Vertices invs;
	private final RealLocalizable t;

	public TranslatedVertices(final Vertices invs, final RealLocalizable t) {
	    this.invs = invs;
	    this.t = t;
	}

	@Override
	public Iterator<Vertex> iterator() {
	    return new Iterator<Vertex>() {

		private long index = -1;

		private final Vertex vertex = new Vertex() {

		    @Override
		    public Mesh mesh() {
			return TranslatedVertices.this.mesh();
		    }

		    @Override
		    public long index() {
			return index;
		    }

		    @Override
		    public double x() {
			return TranslatedVertices.this.x(index());
		    }

		    @Override
		    public double y() {
			return TranslatedVertices.this.y(index());
		    }

		    @Override
		    public double z() {
			return TranslatedVertices.this.z(index());
		    }

		    @Override
		    public float xf() {
			return TranslatedVertices.this.xf(index());
		    }

		    @Override
		    public float yf() {
			return TranslatedVertices.this.yf(index());
		    }

		    @Override
		    public float zf() {
			return TranslatedVertices.this.zf(index());
		    }
		};

		@Override
		public boolean hasNext() {
		    return index + 1 < size();
		}

		@Override
		public Vertex next() {
		    index++;
		    return vertex;
		}
	    };
	}

	@Override
	public Mesh mesh() {
	    return invs.mesh();
	}

	@Override
	public long size() {
	    return invs.size();
	}

	@Override
	public float xf(final long vIndex) {
	    return invs.xf(vIndex) + t.getFloatPosition(0);
	}

	@Override
	public double x(final long vIndex) {
	    return invs.x(vIndex) + t.getDoublePosition(0);
	}

	@Override
	public float yf(final long vIndex) {
	    return invs.yf(vIndex) + t.getFloatPosition(1);
	}

	@Override
	public double y(final long vIndex) {
	    return invs.y(vIndex) + t.getDoublePosition(1);
	}

	@Override
	public float zf(final long vIndex) {
	    return invs.zf(vIndex) + t.getFloatPosition(2);
	}

	@Override
	public double z(final long vIndex) {
	    return invs.z(vIndex) + t.getDoublePosition(2);
	}

	@Override
	public float nxf(final long vIndex) {
	    return invs.nxf(vIndex);
	}

	@Override
	public float nyf(final long vIndex) {
	    return invs.nyf(vIndex);
	}

	@Override
	public float nzf(final long vIndex) {
	    return invs.nzf(vIndex);
	}

	@Override
	public float uf(final long vIndex) {
	    return invs.uf(vIndex);
	}

	@Override
	public float vf(final long vIndex) {
	    return invs.vf(vIndex);
	}

	@Override
	public long addf(final float x, final float y, final float z, final float nx, final float ny, final float nz,
		final float u, final float v) {
	    throw new IllegalArgumentException("The position of mesh views are not modifiable.");
	}

	@Override
	public void setf(final long vIndex, final float x, final float y, final float z, final float nx, final float ny,
		final float nz, final float u, final float v) {
	    throw new IllegalArgumentException("The position of mesh views are not modifiable.");
	}

	@Override
	public void setf(final long vIndex, final float x, final float y, final float z) {
	    throw new IllegalArgumentException("The position of mesh views are not modifiable.");
	}

	@Override
	public void set(final long vIndex, final double x, final double y, final double z) {
	    throw new IllegalArgumentException("The position of mesh views are not modifiable.");
	}

	@Override
	public void set(final long vIndex, final double x, final double y, final double z, final double nx,
		final double ny, final double nz, final double u, final double v) {
	    throw new IllegalArgumentException("The position of mesh views are not modifiable.");
	}

	@Override
	public void setPositionf(final long vIndex, final float x, final float y, final float z) {
	    throw new IllegalArgumentException("The position of mesh views are not modifiable.");
	}

	@Override
	public void setPosition(final long vIndex, final double x, final double y, final double z) {
	    throw new IllegalArgumentException("The position of mesh views are not modifiable.");
	}

	@Override
	public void setNormalf(final long vIndex, final float nx, final float ny, final float nz) {
	    invs.setNormalf(vIndex, nx, ny, nz);
	}

	@Override
	public void setTexturef(final long vIndex, final float u, final float v) {
	    invs.setTexturef(vIndex, u, v);
	}
    }
}
