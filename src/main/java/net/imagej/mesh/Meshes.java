/*-
 * #%L
 * 3D mesh structures for ImageJ.
 * %%
 * Copyright (C) 2016 - 2018 University of Idaho, Royal Veterinary College, and
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

import net.imglib2.RealPoint;

import java.util.HashMap;
import java.util.Map;

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
        RealPoint p = new RealPoint(0, 0, 0);
        for (final Vertex v : m.vertices()) {
            p.move(v);
        }
        for (int d = 0; d < 3; d++) {
            p.setPosition(p.getDoublePosition(d) / m.vertices().size(), d);
        }
        return p;
    }

    public static float[] boundingBox(final net.imagej.mesh.Mesh mesh) {
        final float[] boundingBox = new float[]{Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
                Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
        for (final Vertex v : mesh.vertices()) {
            final float x = v.xf(), y = v.yf(), z = v.zf();
            if (x < boundingBox[0]) boundingBox[0] = x;
            if (y < boundingBox[1]) boundingBox[1] = y;
            if (z < boundingBox[2]) boundingBox[2] = z;
            if (x > boundingBox[3]) boundingBox[3] = x;
            if (y > boundingBox[4]) boundingBox[4] = y;
            if (z > boundingBox[5]) boundingBox[5] = z;
        }
        return boundingBox;
    }

    /**
     * Copies a mesh into another mesh.
     *
     * @param src  Source mesh, from which data will be copied.
     * @param dest Destination mesh, into which source will be copied.
     */
    public static void copy(final net.imagej.mesh.Mesh src,
                            final net.imagej.mesh.Mesh dest) {
        final Map<Long, Long> vIndexMap = new HashMap<>();
        // Copy the vertices, keeping track when indices change.
        for (final Vertex v : src.vertices()) {
            long srcIndex = v.index();
            long destIndex = dest.vertices().add(//
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
    public static void calculateNormals(net.imagej.mesh.Mesh src, net.imagej.mesh.Mesh dest) {

        // Compute the triangle normals.
        HashMap<Long, float[]> triNormals = new HashMap<>();
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
        HashMap<Long, float[]> vNormals = new HashMap<>();// Note: these are cumulative until normalized by vNbrCount
        float[] cumNormal, triNormal;
        for (final Triangle tri : src.triangles()) {
            triNormal = triNormals.get(tri.index());
            for (long idx : new long[]{tri.vertex0(), tri.vertex1(), tri.vertex2()}) {
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
            long srcIndex = v.index();
            vNormal = vNormals.get(v.index());
            vNormalMag = Math.sqrt(Math.pow(vNormal[0], 2) + Math.pow(vNormal[1], 2) + Math.pow(vNormal[2], 2));
            long destIndex = dest.vertices().add(//
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
}
