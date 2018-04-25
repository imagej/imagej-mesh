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

package net.imagej.mesh.io.ply;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.imagej.mesh.Mesh;
import net.imagej.mesh.Triangle;
import net.imagej.mesh.Vertex;
import net.imagej.mesh.io.MeshIOPlugin;
import net.imagej.mesh.naive.NaiveFloatMesh;

import org.scijava.io.AbstractIOPlugin;
import org.scijava.plugin.Plugin;
import org.scijava.util.FileUtils;
import org.smurn.jply.Element;
import org.smurn.jply.ElementReader;
import org.smurn.jply.PlyReader;
import org.smurn.jply.PlyReaderFile;
import org.smurn.jply.util.NormalMode;
import org.smurn.jply.util.NormalizingPlyReader;
import org.smurn.jply.util.TesselationMode;
import org.smurn.jply.util.TextureMode;

import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TLongIntHashMap;

/**
 * A plugin for reading and writing <a href=
 * "http://www.cs.virginia.edu/~gfx/Courses/2001/Advanced.spring.01/plylib/Ply.txt">PLY
 * files</a>.
 * 
 * @author Kyle Harrington (University of Idaho, Moscow)
 * @author Curtis Rueden
 */
@Plugin(type = MeshIOPlugin.class)
public class PLYMeshIO extends AbstractIOPlugin<Mesh> implements MeshIOPlugin {

	// -- PLYMeshIO methods --

	public void read(final File plyFile, final Mesh mesh) throws IOException {
		PlyReader plyReader = new PlyReaderFile(plyFile);
		plyReader = new NormalizingPlyReader(plyReader, TesselationMode.TRIANGLES,
			NormalMode.ADD_NORMALS_CCW, TextureMode.PASS_THROUGH);

		ElementReader reader = plyReader.nextElementReader();

		final TIntLongHashMap rowToVertIndex = new TIntLongHashMap();
		int vertCount = 0;

		// First read vertices so we have all indices, maybe this could be more
		// efficient
		while (reader != null) {
			if (reader.getElementType().getName().equals("vertex")) {
				Element vertex = reader.readElement();
				while (vertex != null) {
					float x = (float) vertex.getDouble("x");
					float z = (float) vertex.getDouble("z");
					float y = (float) vertex.getDouble("y");
					float nx = (float) vertex.getDouble("nx");
					float ny = (float) vertex.getDouble("ny");
					float nz = (float) vertex.getDouble("nz");
					float u = 0, v = 0, w = 0; // TODO: texture coordinate
					final long vIndex = mesh.vertices().addf(x, y, z, nx, ny, nz, u, v, w);
					rowToVertIndex.put(vertCount++, vIndex);
					vertex = reader.readElement();
				}
			}
			reader.close();
			reader = plyReader.nextElementReader();
		}
		plyReader.close();

		// Now read faces
		plyReader = new PlyReaderFile(plyFile);
		plyReader = new NormalizingPlyReader(plyReader, TesselationMode.TRIANGLES,
			NormalMode.ADD_NORMALS_CCW, TextureMode.PASS_THROUGH);

		reader = plyReader.nextElementReader();

		// First read vertices so we have all indices.
		// Maybe this could be more efficient.
		while (reader != null) {
			if (reader.getElementType().getName().equals("face")) {
				Element triangle = reader.readElement();
				while (triangle != null) {
					final int[] indices = triangle.getIntList("vertex_index");
					final long v1 = rowToVertIndex.get(indices[0]);
					final long v2 = rowToVertIndex.get(indices[1]);
					final long v3 = rowToVertIndex.get(indices[2]);
					final float nx = 0, ny = 0, nz = 0;
					mesh.triangles().add(v1, v2, v3, nx, ny, nz);
					triangle = reader.readElement();
				}
			}
			reader.close();
			reader = plyReader.nextElementReader();
		}

		plyReader.close();
	}

	public byte[] writeBinary(final Mesh mesh) {
		final int vertexBytes = 3 * 4 + 3 * 4 + 3 * 4;
		final int triangleBytes = 3 * 4 + 1;
		final String header = "ply\n" + //
			"format binary_little_endian 1.0\n" + //
			"comment This binary PLY mesh was created with imagej-mesh.\n";
		final String vertexHeader = "" + //
			"element vertex " + mesh.vertices().size() + "\n" + //
			"property float x\nproperty float y\nproperty float z\n" + //
			"property float nx\nproperty float ny\nproperty float nz\n" + //
			"property float r\nproperty float g\nproperty float b\n";
		final String triangleHeader = "element face " + mesh.triangles().size() +
			"\nproperty list uchar int vertex_index\n";
		final String endHeader = "end_header\n";
		final long bytes = header.getBytes().length + //
			vertexHeader.getBytes().length + triangleHeader.getBytes().length +
			endHeader.getBytes().length + mesh.vertices().size() * vertexBytes + //
			mesh.triangles().size() * triangleBytes;
		if (bytes > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Mesh data too large: " + bytes);
		}
		final ByteBuffer buffer = ByteBuffer.allocate((int) bytes).order(
			ByteOrder.LITTLE_ENDIAN);

		buffer.put(header.getBytes());
		buffer.put(vertexHeader.getBytes());
		buffer.put(triangleHeader.getBytes());
		buffer.put(endHeader.getBytes());

		// Do not populate file if there are no vertices
		if (mesh.vertices().size() == 0) {
			return buffer.array();
		}

		// Write vertices
		final TLongIntHashMap refToVertId = //
			new TLongIntHashMap((int) mesh.vertices().size());
		int vertId = 0;
		for (final Vertex v : mesh.vertices()) {
			buffer.putFloat(v.xf());
			buffer.putFloat(v.yf());
			buffer.putFloat(v.zf());
			buffer.putFloat(v.nxf());
			buffer.putFloat(v.nyf());
			buffer.putFloat(v.nzf());
			buffer.putFloat(v.uf());
			buffer.putFloat(v.vf());
			buffer.putFloat(v.wf());
			refToVertId.put(v.index(), vertId);
			++vertId;
		}

		// Write triangles
		for (final Triangle t : mesh.triangles()) {
			buffer.put((byte) 3);
			buffer.putInt(refToVertId.get(t.vertex0()));
			buffer.putInt(refToVertId.get(t.vertex1()));
			buffer.putInt(refToVertId.get(t.vertex2()));
		}

		return buffer.array();
	}

	public byte[] writeAscii(final Mesh mesh) throws IOException {
		final String header =
			"ply\nformat ascii 1.0\ncomment This binary PLY mesh was created with imagej-mesh.\n";
		final String vertexHeader = "element vertex " + mesh.vertices().size() +
			"\nproperty float x\nproperty float y\nproperty float z\nproperty float nx\nproperty float ny\nproperty float nz\nproperty float r\n property float g\n property float b\n";
		final String triangleHeader = "element face " + mesh.triangles().size() +
			"\nproperty list uchar int vertex_index\n";
		final String endHeader = "end_header\n";

		// TODO: Fail fast more robustly if mesh is too large.
		// But need to modify the API to not return a byte[].
		if (mesh.vertices().size() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Too many vertices: " + //
				mesh.vertices().size());
		}
		if (mesh.triangles().size() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Too many triangles: " + //
				mesh.triangles().size());
		}

		final ByteArrayOutputStream os = new ByteArrayOutputStream();

		final Writer writer = new OutputStreamWriter(os, "UTF-8");

		writer.write(header);
		writer.write(vertexHeader);
		writer.write(triangleHeader);
		writer.write(endHeader);

		// Do not populate file if there are no vertices
		if (mesh.vertices().size() == 0) {
			writer.flush();
			return os.toByteArray();
		}

		// Write vertices
		final TLongIntHashMap refToVertId = new TLongIntHashMap(//
			(int) mesh.vertices().size());
		int vertId = 0;
		for (final Vertex v : mesh.vertices()) {
			writer.write(Float.toString(v.xf()));
			writer.write(' ');
			writer.write(Float.toString(v.yf()));
			writer.write(' ');
			writer.write(Float.toString(v.zf()));
			writer.write(' ');
			writer.write(Float.toString(v.nxf()));
			writer.write(' ');
			writer.write(Float.toString(v.nyf()));
			writer.write(' ');
			writer.write(Float.toString(v.nzf()));
			writer.write(' ');
			writer.write(Float.toString(v.uf()));
			writer.write(' ');
			writer.write(Float.toString(v.vf()));
			writer.write(' ');
			writer.write(Float.toString(v.wf()));
			writer.write('\n');
			refToVertId.put(v.index(), vertId);
			++vertId;
		}

		// Write triangles
		for (final Triangle t : mesh.triangles()) {
			writer.write("3 ");
			writer.write(Integer.toString(refToVertId.get(t.vertex0())));
			writer.write(' ');
			writer.write(Integer.toString(refToVertId.get(t.vertex1())));
			writer.write(' ');
			writer.write(Integer.toString(refToVertId.get(t.vertex2())));
			writer.write('\n');
		}
		writer.flush();
		return os.toByteArray();
	}

	// -- IOPlugin methods --

	@Override
	public boolean supportsOpen(final String source) {
		return true;
	}

	@Override
	public boolean supportsSave(final String source) {
		return true;
	}

	@Override
	public Mesh open(final String source) throws IOException {
		final Mesh mesh = new NaiveFloatMesh();
		read(new File(source), mesh);
		return mesh;
	}

	@Override
	public void save(final Mesh data, final String destination) throws IOException {
		final byte[] bytes = writeBinary(data);
		FileUtils.writeFile(new File(destination), bytes);
	}
}
