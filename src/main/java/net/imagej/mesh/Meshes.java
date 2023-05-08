/*-
 * #%L
 * 3D mesh structures for ImageJ.
 * %%
 * Copyright (C) 2016 - 2020 University of Idaho, Royal Veterinary College, and
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

package net.imagej.mesh;

import java.util.HashMap;
import java.util.Map;

import net.imagej.mesh.alg.MarchingCubesBooleanType;
import net.imagej.mesh.alg.MarchingCubesRealType;
import net.imagej.mesh.alg.MeshConnectedComponents;
import net.imagej.mesh.alg.RemoveDuplicateVertices;
import net.imagej.mesh.alg.SimplifyMesh;
import net.imagej.mesh.obj.Mesh;
import net.imagej.mesh.obj.Triangle;
import net.imagej.mesh.obj.Vertex;
import net.imagej.mesh.obj.Vertices;
import net.imagej.mesh.obj.nio.BufferMesh;
import net.imglib2.FinalRealInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;

/**
 * Utility methods for working with {@link Mesh} objects.
 *
 * @author Curtis Rueden
 * @author Kyle Harrington
 */
public class Meshes {

    /**
     * Finds the center of a mesh using vertices.
     *
     * @return a RealPoint representing the mesh's center
     */
    public static RealPoint center(final Mesh m) {
	final RealPoint p = new RealPoint(0, 0, 0);
	for (final Vertex v : m.vertices()) {
            p.move(v);
        }
        for (int d = 0; d < 3; d++) {
            p.setPosition(p.getDoublePosition(d) / m.vertices().size(), d);
        }
        return p;
    }

    public static RealInterval boundingBox(final net.imagej.mesh.obj.Mesh mesh) {
	final double[] min = new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
		Double.POSITIVE_INFINITY };
	final double[] max = new double[] { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
		Double.NEGATIVE_INFINITY };
	for (final Vertex v : mesh.vertices()) {
	    final double x = v.x(), y = v.y(), z = v.z();
	    if (x < min[0])
		min[0] = x;
	    if (y < min[1])
		min[1] = y;
	    if (z < min[2])
		min[2] = z;
	    if (x > max[0])
		max[0] = x;
	    if (y > max[1])
		max[1] = y;
	    if (z > max[2])
		max[2] = z;
	}
	return FinalRealInterval.wrap(min, max);
    }

    /**
     * Copies a mesh into another mesh.
     *
     * @param src  Source mesh, from which data will be copied.
     * @param dest Destination mesh, into which source will be copied.
     */
    public static void copy(final net.imagej.mesh.obj.Mesh src, final net.imagej.mesh.obj.Mesh dest) {
	final Map<Long, Long> vIndexMap = new HashMap<>();
        // Copy the vertices, keeping track when indices change.
	for (final Vertex v : src.vertices()) {
	    final long srcIndex = v.index();
	    final long destIndex = dest.vertices().add(//
                    v.x(), v.y(), v.z(), //
                    v.nx(), v.ny(), v.nz(), //
                    v.u(), v.v());
            if (srcIndex != destIndex) {
                // NB: If the destination vertex index matches the source, we skip
                // recording the entry, to save space in the map. Later, we leave
                // indexes unchanged which are absent from the map.
                //
                // This scenario is actually quite common, because vertices are often
                // numbered in natural order, with the first vertex having index 0,
                // the second having index 1, etc., although it is not guaranteed.
                vIndexMap.put(srcIndex, destIndex);
            }
        }
        // Copy the triangles, taking care to use destination indices.
	for (final Triangle tri : src.triangles()) {
	    final long v0src = tri.vertex0();
	    final long v1src = tri.vertex1();
	    final long v2src = tri.vertex2();
	    final long v0 = vIndexMap.getOrDefault(v0src, v0src);
	    final long v1 = vIndexMap.getOrDefault(v1src, v1src);
	    final long v2 = vIndexMap.getOrDefault(v2src, v2src);

            dest.triangles().add(v0, v1, v2, tri.nx(), tri.ny(), tri.nz());
        }
    }

    /**
     * Calculates the normals for a mesh. Creates a new mesh with the calculated normals. Assumes CCW winding order.
     *
     * @param src  Source mesh, used for vertex and triangle info
     * @param dest Destination mesh, will be populated with src's info plus the calculated normals
     */
    public static void calculateNormals(final net.imagej.mesh.obj.Mesh src, final net.imagej.mesh.obj.Mesh dest) {

        // Compute the triangle normals.
	final HashMap<Long, float[]> triNormals = new HashMap<>();
	for (final Triangle tri : src.triangles()) {
	    final int v0 = (int) tri.vertex0();
	    final int v1 = (int) tri.vertex1();
	    final int v2 = (int) tri.vertex2();

	    final float v0x = src.vertices().xf(v0);
	    final float v0y = src.vertices().yf(v0);
	    final float v0z = src.vertices().zf(v0);
	    final float v1x = src.vertices().xf(v1);
	    final float v1y = src.vertices().yf(v1);
	    final float v1z = src.vertices().zf(v1);
	    final float v2x = src.vertices().xf(v2);
	    final float v2y = src.vertices().yf(v2);
	    final float v2z = src.vertices().zf(v2);

	    final float v10x = v1x - v0x;
	    final float v10y = v1y - v0y;
	    final float v10z = v1z - v0z;

	    final float v20x = v2x - v0x;
	    final float v20y = v2y - v0y;
	    final float v20z = v2z - v0z;

	    final float nx = v10y * v20z - v10z * v20y;
	    final float ny = v10z * v20x - v10x * v20z;
	    final float nz = v10x * v20y - v10y * v20x;
	    final float nmag = (float) Math.sqrt(Math.pow(nx, 2) + Math.pow(ny, 2) + Math.pow(nz, 2));

            triNormals.put(tri.index(), new float[]{nx / nmag, ny / nmag, nz / nmag});
        }

        // Next, compute the normals per vertex based on face normals
	final HashMap<Long, float[]> vNormals = new HashMap<>();// Note: these are cumulative until normalized by
								// vNbrCount
        float[] cumNormal, triNormal;
	for (final Triangle tri : src.triangles()) {
            triNormal = triNormals.get(tri.index());
	    for (final long idx : new long[] { tri.vertex0(), tri.vertex1(), tri.vertex2() }) {
                cumNormal = vNormals.getOrDefault(idx, new float[]{0, 0, 0});
                cumNormal[0] += triNormal[0];
                cumNormal[1] += triNormal[1];
                cumNormal[2] += triNormal[2];
                vNormals.put(idx, cumNormal);
            }
        }

        // Now populate dest
	final Map<Long, Long> vIndexMap = new HashMap<>();
        float[] vNormal;
        double vNormalMag;
        // Copy the vertices, keeping track when indices change.
	for (final Vertex v : src.vertices()) {
	    final long srcIndex = v.index();
            vNormal = vNormals.get(v.index());
            vNormalMag = Math.sqrt(Math.pow(vNormal[0], 2) + Math.pow(vNormal[1], 2) + Math.pow(vNormal[2], 2));
	    final long destIndex = dest.vertices().add(//
                    v.x(), v.y(), v.z(), //
                    vNormal[0] / vNormalMag, vNormal[1] / vNormalMag, vNormal[2] / vNormalMag, //
                    v.u(), v.v());
            if (srcIndex != destIndex) {
                // NB: If the destination vertex index matches the source, we skip
                // recording the entry, to save space in the map. Later, we leave
                // indexes unchanged which are absent from the map.
                //
                // This scenario is actually quite common, because vertices are often
                // numbered in natural order, with the first vertex having index 0,
                // the second having index 1, etc., although it is not guaranteed.
                vIndexMap.put(srcIndex, destIndex);
            }
        }
        // Copy the triangles, taking care to use destination indices.
	for (final Triangle tri : src.triangles()) {
	    final long v0src = tri.vertex0();
	    final long v1src = tri.vertex1();
	    final long v2src = tri.vertex2();
	    final long v0 = vIndexMap.getOrDefault(v0src, v0src);
	    final long v1 = vIndexMap.getOrDefault(v1src, v1src);
	    final long v2 = vIndexMap.getOrDefault(v2src, v2src);
            triNormal = triNormals.get(tri.index());
            dest.triangles().add(v0, v1, v2, triNormal[0], triNormal[1], triNormal[2]);
        }
    }

    /**
     * Simplifies a given mesh. Normals and uv coordinates will be ignored and not added to the output mesh.
     *
     * @param mesh Source mesh
     * @param target_percent the amount in percent to attempt to achieve. For example: 0.25f would result in creating
     *                       a mesh with 25% of triangles contained in the original.
     * @param agressiveness sharpness to increase the threshold. 5..8 are good numbers. more iterations yield higher
     *                      quality. Minimum 4 and maximum 20 are recommended.
     * @return the simplified mesh The result will not include normals or uv coordinates.
     */
    public static Mesh simplify(final Mesh mesh, final float target_percent, final float agressiveness) {
        return new SimplifyMesh(mesh).simplify(target_percent, agressiveness);
    }

    /**
     * Creates a new mesh from a given mesh without any duplicate vertices.
     * Normals and uv coordinates will be ignored and not added to the output mesh.
     *
     * @param mesh Source mesh
     * @param precision decimal digits to take into account when comparing mesh vertices
     * @return new mesh without duplicate vertices. The result will not include normals or uv coordinates.
     */
    public static Mesh removeDuplicateVertices(final Mesh mesh, final int precision) {
        return RemoveDuplicateVertices.calculate(mesh, precision);
    }

    /**
     * Creates mesh e.g. from IterableRegion by using the marching cubes algorithm.
     *
     * @param source The binary input image for the marching cubes algorithm.
     * @return The result mesh of the marching cubes algorithm.
     */
    public static <T extends BooleanType<T>> Mesh marchingCubes(final RandomAccessibleInterval<T> source) {
        return MarchingCubesBooleanType.calculate(source);
    }

    /**
     * Creates mesh e.g. from IterableRegion by using the marching cubes algorithm.
     *
     * @param source  The input image for the marching cubes algorithm.
     * @param isoLevel The threshold to distinguish between foreground and background values.
     * @return The result mesh of the marching cubes algorithm.
     */
    public static <T extends RealType<T>> Mesh marchingCubes(final RandomAccessibleInterval<T> source,
	    final double isoLevel) {
        return MarchingCubesRealType.calculate(source, isoLevel);
    }

    /**
     * Returns the number of connected components in the specified mesh.
     *
     * @param mesh the mesh.
     * @return the number of connected components.
     */
    public static int nConnectedComponents(final Mesh mesh) {
	return MeshConnectedComponents.n(mesh);
    }

    /**
     * Returns an iterable over the connected components of the specified mesh.
     *
     * @param mesh the mesh.
     * @return an iterable.
     */
    public static Iterable<BufferMesh> connectedComponents(final Mesh mesh) {
	return MeshConnectedComponents.iterable(mesh);
    }

    /**
     * Scales the vertices position by the amount specified by the array.
     * <p>
     * Note that the position are simply multiplied by the specified scale, so they
     * are scaled with respect to (0,0,0) which makes this method useful mainly to
     * have a mesh generated by marching-cube algorithm have coordinates in physical
     * uunits.
     *
     * @param mesh       the mesh to scale.
     * @param pixelSizes a <code>double</code> array of at least 3 elements.
     */
    public static void scale(final Mesh mesh, final double[] scale) {
	final Vertices vertices = mesh.vertices();
	final long nVertices = vertices.size();
	for (long i = 0; i < nVertices; i++) {
	    final double x = vertices.x(i);
	    final double y = vertices.y(i);
	    final double z = vertices.z(i);
	    vertices.set(i, x * scale[0], y * scale[1], z * scale[2]);
	}
    }
}
