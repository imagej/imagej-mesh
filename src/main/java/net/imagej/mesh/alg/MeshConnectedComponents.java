package net.imagej.mesh.alg;

import java.util.BitSet;
import java.util.Iterator;

import gnu.trove.impl.Constants;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;
import net.imagej.mesh.obj.Mesh;
import net.imagej.mesh.obj.Triangles;
import net.imagej.mesh.obj.Vertices;
import net.imagej.mesh.obj.nio.BufferMesh;

/**
 * Connected components algorithm for meshes.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class MeshConnectedComponents {

    /**
     * Returns the number of connected components in this mesh.
     * 
     * @param mesh the mesh.
     * @return the number of connected components.
     */
    public static final int n(final Mesh mesh) {
	final Triangles triangles = mesh.triangles();
	final Vertices vertices = mesh.vertices();
	final int nVertices = (int) vertices.size();

	final TIntObjectHashMap<TIntArrayList> map = triangleMap(mesh);
	final BitSet visited = new BitSet(nVertices);
	final TIntLinkedList queue = new TIntLinkedList(-1);

	int nCC = 0;

	// Since we iterate in order, we can start searching for the next
	// non-visited vertex a bit further.
	int nextClearBit = 0;
	while ((nextClearBit = visited.nextClearBit(nextClearBit)) < nVertices) {
	    // Get a starting point that was never visited.
	    for (int vid = nextClearBit; vid < nVertices; vid++) {
		if (visited.get(vid))
		    continue;

		final int start = vid;
		nCC++;

		// Visit starting from this one.
		queue.clear();
		queue.add(start);
		while (!queue.isEmpty()) {
		    final int v = queue.removeAt(queue.size() - 1);
		    visited.set(v);
		    final TIntArrayList ts = map.get(v);
		    final int nts = ts.size();
		    for (int i = 0; i < nts; i++) {
			final int t = ts.get(i);
			visit(triangles.vertex0(t), visited, queue);
			visit(triangles.vertex1(t), visited, queue);
			visit(triangles.vertex2(t), visited, queue);
		    }
		}

		// Next time we search for a non-visited vertex, start from the
		// last one we found.
		nextClearBit = start;
	    }
	}
	return nCC;
    }

    public static Iterator<BufferMesh> iterator(final Mesh mesh) {
	return new MyCCIterator(mesh);
    }

    public static Iterable<BufferMesh> iterable(final Mesh mesh) {
	return new Iterable<BufferMesh>() {

	    @Override
	    public Iterator<BufferMesh> iterator() {
		return MeshConnectedComponents.iterator(mesh);
	    }
	};
    }

    private static final boolean visit(final int v, final BitSet visited, final TIntLinkedList queue) {
	if (!visited.get(v)) {
	    queue.add(v);
	    return true;
	}
	return false;
    }

    /**
     * Returns the map of vertex id to the list of triangles they belong to.
     * 
     * @param mesh
     */
    private static final TIntObjectHashMap<TIntArrayList> triangleMap(final Mesh mesh) {
	final Triangles triangles = mesh.triangles();
	final int nTriangles = (int) triangles.size();
	final Vertices vertices = mesh.vertices();
	final int nVertices = (int) vertices.size();

	final TIntObjectHashMap<TIntArrayList> map = new TIntObjectHashMap<>(nVertices);
	for (int tid = 0; tid < nTriangles; tid++) {
	    addToMap(map, triangles.vertex0(tid), tid);
	    addToMap(map, triangles.vertex1(tid), tid);
	    addToMap(map, triangles.vertex2(tid), tid);
	}
	return map;
    }

    private static void addToMap(final TIntObjectHashMap<TIntArrayList> map, final int v, final int tid) {
	TIntArrayList ts = map.get(v);
	if (ts == null) {
	    ts = new TIntArrayList();
	    map.put(v, ts);
	}
	ts.add(tid);
    }

    private static final class MyCCIterator implements Iterator<BufferMesh> {

	private final Mesh mesh;

	private final BitSet visited;

	private BufferMesh next;

	private final TIntObjectHashMap<TIntArrayList> map;

	private int currentStartVertex;

	private final int nVertices;

	public MyCCIterator(final Mesh mesh) {
	    this.mesh = mesh;
	    this.map = triangleMap(mesh);
	    this.nVertices = (int) mesh.vertices().size();
	    this.visited = new BitSet(nVertices);
	    this.currentStartVertex = 0;
	    this.next = prefetch();
	}

	private BufferMesh prefetch() {
	    final Triangles triangles = mesh.triangles();
	    final TIntHashSet cc = new TIntHashSet();

	    while ((currentStartVertex = visited.nextClearBit(currentStartVertex)) < nVertices) {
		// Get a starting point that was never visited.
		for (int vid = currentStartVertex; vid < nVertices; vid++) {
		    if (visited.get(vid))
			continue;

		    final int start = vid;

		    // Visit starting from this one.
		    final TIntLinkedList queue = new TIntLinkedList(-1);
		    queue.add(start);
		    while (!queue.isEmpty()) {
			final int v = queue.removeAt(queue.size() - 1);
			visited.set(v);
			final TIntArrayList ts = map.get(v);
			final int nts = ts.size();
			for (int i = 0; i < nts; i++) {
			    final int t = ts.get(i);
			    boolean add = false;
			    add = visit(triangles.vertex0(t), visited, queue) || add;
			    add = visit(triangles.vertex1(t), visited, queue) || add;
			    add = visit(triangles.vertex2(t), visited, queue) || add;

			    // At least one vertex was new, add its triangle to
			    // the CC.
			    if (add)
				cc.add(t);
			}
		    }

		    // Next time we search for a non-visited vertex, start from
		    // the last one we found.
		    currentStartVertex = start;

		    // Make the connected-component mesh
		    return makeCC(cc);
		}
	    }
	    return null;
	}

	private BufferMesh makeCC(final TIntHashSet cc) {
	    final Triangles inTriangles = mesh.triangles();
	    final Vertices inVertices = mesh.vertices();

	    // Count the number of vertices we need.
	    final TLongHashSet vs = new TLongHashSet();
	    cc.forEach(new TIntProcedure() {

		@Override
		public boolean execute(final int t) {
		    vs.add(inTriangles.vertex0(t));
		    vs.add(inTriangles.vertex1(t));
		    vs.add(inTriangles.vertex2(t));
		    return true;
		}
	    });

	    final BufferMesh out = new BufferMesh(vs.size(), cc.size());
	    final net.imagej.mesh.obj.nio.BufferMesh.Vertices outVertices = out.vertices();
	    final net.imagej.mesh.obj.nio.BufferMesh.Triangles outTriangles = out.triangles();

	    final TLongLongHashMap inOutMap = new TLongLongHashMap(nVertices, Constants.DEFAULT_LOAD_FACTOR, -1, -1);
	    cc.forEach(new TIntProcedure() {

		@Override
		public boolean execute(final int t) {
		    final long inv0 = inTriangles.vertex0(t);
		    long outv0 = inOutMap.get(inv0);
		    if (inOutMap.get(inv0) < 0) {
			// Vertex not already added, create it.
			final float x0 = inVertices.xf(inv0);
			final float y0 = inVertices.yf(inv0);
			final float z0 = inVertices.zf(inv0);
			outv0 = outVertices.addf(x0, y0, z0);
			inOutMap.put(inv0, outv0);
		    }

		    final long inv1 = inTriangles.vertex1(t);
		    long outv1 = inOutMap.get(inv1);
		    if (outv1 < 0) {
			final float x1 = inVertices.xf(inv1);
			final float y1 = inVertices.yf(inv1);
			final float z1 = inVertices.zf(inv1);
			outv1 = outVertices.addf(x1, y1, z1);
			inOutMap.put(inv1, outv1);
		    }

		    final long inv2 = inTriangles.vertex2(t);
		    long outv2 = inOutMap.get(inv2);
		    if (outv2 < 0) {
			final float x2 = inVertices.xf(inv2);
			final float y2 = inVertices.yf(inv2);
			final float z2 = inVertices.zf(inv2);
			outv2 = outVertices.addf(x2, y2, z2);
			inOutMap.put(inv2, outv2);
		    }

		    final float nxf = inTriangles.nxf(t);
		    final float nyf = inTriangles.nyf(t);
		    final float nzf = inTriangles.nzf(t);

		    outTriangles.addf(outv0, outv1, outv2, nxf, nyf, nzf);

		    return true;
		}
	    });
	    return out;
	}

	@Override
	public boolean hasNext() {
	    return next != null;
	}

	@Override
	public BufferMesh next() {
	    final BufferMesh out = next;
	    next = prefetch();
	    return out;
	}

    }
}
