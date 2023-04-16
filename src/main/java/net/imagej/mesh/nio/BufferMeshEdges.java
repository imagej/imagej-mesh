package net.imagej.mesh.nio;

import static net.imagej.mesh.nio.BufferMesh.create;
import static net.imagej.mesh.nio.BufferMesh.grow;
import static net.imagej.mesh.nio.BufferMesh.ints;
import static net.imagej.mesh.nio.BufferMesh.safeIndex;
import static net.imagej.mesh.nio.BufferMesh.safeInt;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.function.Function;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.procedure.TLongLongProcedure;
import net.imagej.mesh.Mesh;
import net.imagej.mesh.Triangles;
import net.imagej.mesh.Vertices;

public class BufferMeshEdges implements Mesh {

    private class Edges implements net.imagej.mesh.Edges {

	private final IntBuffer edgeIndices;
	private final IntBuffer faceIndices;
	/** Map of vertex pairs (edge source and edge target) to edge index. */
	private final TLongIntHashMap vertexMap;

	public Edges(final IntBuffer edgeIndices, final IntBuffer faceIndices) {
	    this.edgeIndices = edgeIndices;
	    this.faceIndices = faceIndices;
	    this.vertexMap = new TLongIntHashMap(edgeIndices.capacity() / EDGE_SIZE,
		    Constants.DEFAULT_LOAD_FACTOR, -1, -1);
	}

	@Override
	public BufferMeshEdges mesh() {
	    return BufferMeshEdges.this;
	}

	@Override
	public long size() {
	    return edgeIndices.limit() / EDGE_SIZE;
	}

	@Override
	public long v0(final long i) {
	    return edgeIndices.get(safeIndex(i, EDGE_SIZE, 0));
	}

	@Override
	public long v1(final long i) {
	    return edgeIndices.get(safeIndex(i, EDGE_SIZE, 1));
	}

	@Override
	public long f0(final long i) {
	    return faceIndices.get(safeIndex(i, N_FACES, 0));
	}

	@Override
	public long f1(final long i) {
	    return faceIndices.get(safeIndex(i, N_FACES, 1));
	}

	@Override
	public long add(final long v0, final long v1, final long f0, final long f1) {
	    final long index = size();
	    grow(edgeIndices, EDGE_SIZE);
	    edgeIndices.put(safeInt(v0));
	    edgeIndices.put(safeInt(v1));
	    grow(faceIndices, N_FACES);
	    faceIndices.put(safeInt(f0));
	    faceIndices.put(safeInt(f1));

	    // Add the vertex maps
	    vertexMap.put(pair((int) v0, (int) v1), (int) index);

	    return index;
	}

	@Override
	public long indexOf(final long v0, final long v1) {
	    return vertexMap.get(pair((int) v0, (int) v1));
	}

    }

    // 2 ints per edge
    private static final int EDGE_SIZE = 2;

    // Max 2 faces, as ints, per edge.
    private static final int N_FACES = 2;

    private final Mesh mesh;

    private final Edges edges;

    public BufferMeshEdges(final Mesh mesh, final IntBuffer edgeIndices, final IntBuffer faceIndices) {
	this.mesh = mesh;
	this.edges = new Edges(edgeIndices, faceIndices);
    }

    @Override
    public Edges edges() {
	return edges;
    }

    public static BufferMeshEdges wrap(final Mesh simplified, final boolean direct) {
	final Triangles triangles = simplified.triangles();

	// Map of edges to face pairs.
	final TLongLongHashMap faceMap = new TLongLongHashMap(Constants.DEFAULT_CAPACITY, 
		Constants.DEFAULT_LOAD_FACTOR, -1, -1);
	// Stores vertices of a triangle.
	final int[] vs = new int[3];
	// Store edges of a triangle.
	final long[] es = new long[3];
	// Store vertices unpacked from long.
	final int[] eStore = new int[2];

	// Iterate through the triangles to build the edge map.
	for (int i = 0; i < triangles.size(); i++) {
	    vs[0] = (int) triangles.vertex0(i);
	    vs[1] = (int) triangles.vertex1(i);
	    vs[2] = (int) triangles.vertex2(i);
	    Arrays.sort(vs);
	    // Edges are specified by va -> vb with va < vb always.
	    es[0] = pair(vs[0], vs[1]);
	    es[1] = pair(vs[0], vs[2]);
	    es[2] = pair(vs[1], vs[2]);
	    // Add face to edge map.
	    for (final long e : es) {
		final long fp = faceMap.get(e);
		if (fp == faceMap.getNoEntryValue()) {
		    final long f = pair(i, -1);
		    faceMap.put(e, f);
		    continue;
		}
		unpair(fp, eStore);
		// If we find a value in the map, then the 2nd face should still be blank( we
		// iterate every edge at most twice).
		assert (eStore[1] == -1);
		// Store 2nd face in the value.
		faceMap.put(e, pair(eStore[0], i));
	    }
	}

	// Create buffers from the map.
	final int nEdges = faceMap.size();
	final Function<Integer, ByteBuffer> creator = direct ? ByteBuffer::allocateDirect : ByteBuffer::allocate;
	final IntBuffer edgeIndices = ints(create(creator, nEdges * 4 * EDGE_SIZE));
	final IntBuffer faceIndices = ints(create(creator, nEdges * 4 * N_FACES));

	// Create mesh class with yet empty edges.
	final BufferMeshEdges meshEdges = new BufferMeshEdges(simplified, edgeIndices, faceIndices);
	final Edges edges = meshEdges.edges();

	// Adds edges and faces to the Edges buffers.
	final TLongLongProcedure edgeAdder = new TLongLongProcedure() {

	    /** Holder for vertex indices. */
	    private final int[] vs = new int[2];

	    /** Holder for face indices. */
	    private final int[] fs = new int[2];

	    @Override
	    public boolean execute(final long e, final long faces) {
		unpair(e, vs);
		unpair(faces, fs);
		edges.add(vs[0], vs[1], fs[0], fs[1]);
		return true;
	    }
	};
	faceMap.forEachEntry(edgeAdder);
	return meshEdges;
    }

    /**
     * Packs two <code>int</code>s in a <code>long</code>.
     *
     * @param x the first <code>int</code> to store.
     * @param y the second<code>int</code> to store.
     * @return a <code>long</code> that is the concatenation of the birts of the two
     *         <code>int</code>s.
     */
    private static final long pair(final int x, final int y) {
	return (((long) x) << 32) | (y & 0xffffffffL);
    }

    /**
     * Unpacks two <code>int</code>s in a <code>long</code>, and write results in
     * the specified <code>int</code> array.
     *
     * @param l   the <code>long</code> to unpack.
     * @param out the <code>int</code> array (at least size) to write the results
     *            in, at indices 0 and 1 respectively.
     */
    private static final void unpair(final long l, final int[] out) {
	final int x = (int) (l >> 32);
	final int y = (int) l;
	out[0] = x;
	out[1] = y;
    }

    @Override
    public Vertices vertices() {
	return mesh.vertices();
    }

    @Override
    public Triangles triangles() {
	return mesh.triangles();
    }

}
