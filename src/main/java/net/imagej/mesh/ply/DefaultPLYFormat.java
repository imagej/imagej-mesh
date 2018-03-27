
package net.imagej.mesh.ply;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.imagej.mesh.mastodon.DefaultMesh;
import net.imagej.mesh.mastodon.Mesh;
import net.imagej.mesh.mastodon.Triangle;
import net.imagej.mesh.mastodon.TrianglePool;
import net.imagej.mesh.mastodon.Vertex3;
import net.imagej.mesh.mastodon.Vertex3Pool;

import org.mastodon.collection.RefList;
import org.mastodon.collection.ref.RefArrayList;
import org.scijava.plugin.AbstractHandlerPlugin;
import org.smurn.jply.Element;
import org.smurn.jply.ElementReader;
import org.smurn.jply.PlyReader;
import org.smurn.jply.PlyReaderFile;
import org.smurn.jply.util.NormalMode;
import org.smurn.jply.util.NormalizingPlyReader;
import org.smurn.jply.util.TesselationMode;
import org.smurn.jply.util.TextureMode;

import gnu.trove.map.hash.TIntIntHashMap;

/**
 * A class for reading and writing PLY files PLY specs:
 * http://www.cs.virginia.edu/~gfx/Courses/2001/Advanced.spring.01/plylib/Ply.txt
 *
 * @author Kyle Harrington (University of Idaho, Moscow)
 */
public class DefaultPLYFormat extends AbstractHandlerPlugin<File> implements
	PLYFormat
{

	@Override
	public Class<File> getType() {
		return File.class;
	}

	@Override
	public Mesh read(final File plyFile) throws IOException {
		PlyReader plyReader = new PlyReaderFile(plyFile);
		plyReader = new NormalizingPlyReader(plyReader, TesselationMode.TRIANGLES,
			NormalMode.ADD_NORMALS_CCW, TextureMode.PASS_THROUGH);

		final int vertexCount = plyReader.getElementCount("vertex");
		final int triangleCount = plyReader.getElementCount("face");

		final Mesh mesh = new DefaultMesh(vertexCount, triangleCount);
		final Vertex3Pool vp = mesh.getVertex3Pool();
		final TrianglePool tp = mesh.getTrianglePool();

		ElementReader reader = plyReader.nextElementReader();

		// First read vertices so we have all indices, maybe this could be more
		// efficient
		final RefList<Vertex3> vertices = new RefArrayList<>(vp);
		while (reader != null) {
			if (reader.getElementType().getName().equals("vertex")) {
				Element vertex = reader.readElement();
				while (vertex != null) {
					final Vertex3 v = vp.create().init((float) vertex.getDouble("x"),
						(float) vertex.getDouble("y"), (float) vertex.getDouble("z"),
						(float) vertex.getDouble("nx"), (float) vertex.getDouble("ny"),
						(float) vertex.getDouble("nz"), 0f, 0f, 0f);// We could populate our
																												// texture coordinate
																												// here
					vertices.add(v);
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

		// First read vertices so we have all indices, maybe this could be more
		// efficient
		final RefList<Triangle> triangles = new RefArrayList<>(tp);
		while (reader != null) {
			if (reader.getElementType().getName().equals("face")) {
				Element triangle = reader.readElement();
				while (triangle != null) {
					final int[] indices = triangle.getIntList("vertex_index");
					final Vertex3 v1 = vertices.get(indices[0]);
					final Vertex3 v2 = vertices.get(indices[1]);
					final Vertex3 v3 = vertices.get(indices[2]);
					final Triangle t = tp.create().init(v1, v2, v3, vp.create().init(0f,
						0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
					triangles.add(t);
					triangle = reader.readElement();
				}
			}
			reader.close();
			reader = plyReader.nextElementReader();
		}

		plyReader.close();

		mesh.setTriangles(triangles);
		mesh.setVertices(vertices);

		return mesh;
	}

	@Override
	public byte[] writeBinary(final Mesh mesh) throws IOException {
		final int vertexBytes = 3 * 4 + 3 * 4 + 3 * 4;
		final int triangleBytes = 3 * 4 + 1;
		final String header =
			"ply\nformat binary_little_endian 1.0\ncomment This binary PLY mesh was created with imagej-mesh.\n";
		final String vertexHeader = "element vertex " + mesh.getVertices().size() +
			"\nproperty float x\nproperty float y\nproperty float z\nproperty float nx\nproperty float ny\nproperty float nz\nproperty float r\n property float g\n property float b\n";
		final String triangleHeader = "element face " + mesh.getTriangles().size() +
			"\nproperty list uchar int vertex_index\n";
		final String endHeader = "end_header\n";
		final int bytes = header.getBytes().length + vertexHeader
			.getBytes().length + triangleHeader.getBytes().length + endHeader
				.getBytes().length + mesh.getVertices().size() * vertexBytes + mesh
					.getTriangles().size() * triangleBytes;
		final ByteBuffer buffer = ByteBuffer.allocate(bytes).order(
			ByteOrder.LITTLE_ENDIAN);

		buffer.put(header.getBytes());
		buffer.put(vertexHeader.getBytes());
		buffer.put(triangleHeader.getBytes());
		buffer.put(endHeader.getBytes());

		// Do not populate file if there are no vertices
		if (mesh.getVertices().isEmpty()) {
			return buffer.array();
		}

		// Write vertices
		final TIntIntHashMap refToVertId = new TIntIntHashMap(mesh.getVertices()
			.size());
		final Vertex3Pool vp = mesh.getVertex3Pool();
		int vertId = 0;
		for (final Vertex3 v : mesh.getVertices()) {
			buffer.putFloat(v.getX());
			buffer.putFloat(v.getY());
			buffer.putFloat(v.getZ());
			buffer.putFloat(v.getNX());
			buffer.putFloat(v.getNY());
			buffer.putFloat(v.getNZ());
			buffer.putFloat(v.getU());
			buffer.putFloat(v.getV());
			buffer.putFloat(v.getW());
			refToVertId.put(vp.getId(v), vertId);
			++vertId;
		}

		// Write triangles
		for (final Triangle t : mesh.getTriangles()) {
			buffer.put((byte) 3);
			buffer.putInt(refToVertId.get(t.getVertex(0).getInternalPoolIndex()));
			buffer.putInt(refToVertId.get(t.getVertex(1).getInternalPoolIndex()));
			buffer.putInt(refToVertId.get(t.getVertex(2).getInternalPoolIndex()));
		}

		return buffer.array();
	}

	@Override
	public byte[] writeAscii(final Mesh mesh) throws IOException {
		final int vertexBytes = 3 * 4 + 3 * 4 + 3 * 4;
		final int triangleBytes = 3 * 4 + 1;
		final String header =
			"ply\nformat ascii 1.0\ncomment This binary PLY mesh was created with imagej-mesh.\n";
		final String vertexHeader = "element vertex " + mesh.getVertices().size() +
			"\nproperty float x\nproperty float y\nproperty float z\nproperty float nx\nproperty float ny\nproperty float nz\nproperty float r\n property float g\n property float b\n";
		final String triangleHeader = "element face " + mesh.getTriangles().size() +
			"\nproperty list uchar int vertex_index\n";
		final String endHeader = "end_header\n";

		final ByteArrayOutputStream os = new ByteArrayOutputStream();

		final Writer writer = new OutputStreamWriter(os, "UTF-8");

		writer.write(header + vertexHeader + triangleHeader + endHeader);
		writer.flush();

		// Do not populate file if there are no vertices
		if (mesh.getVertices().isEmpty()) {
			return os.toByteArray();
		}

		// Write vertices
		final TIntIntHashMap refToVertId = new TIntIntHashMap(mesh.getVertices()
			.size());
		final Vertex3Pool vp = mesh.getVertex3Pool();
		int vertId = 0;
		for (final Vertex3 v : mesh.getVertices()) {
			writer.write(Float.toString(v.getX()));
			writer.write(' ');
			writer.write(Float.toString(v.getY()));
			writer.write(' ');
			writer.write(Float.toString(v.getZ()));
			writer.write(' ');
			writer.write(Float.toString(v.getNX()));
			writer.write(' ');
			writer.write(Float.toString(v.getNY()));
			writer.write(' ');
			writer.write(Float.toString(v.getNZ()));
			writer.write(' ');
			writer.write(Float.toString(v.getU()));
			writer.write(' ');
			writer.write(Float.toString(v.getV()));
			writer.write(' ');
			writer.write(Float.toString(v.getW()));
			writer.write('\n');
			refToVertId.put(vp.getId(v), vertId);
			++vertId;
		}

		// Write triangles
		for (final Triangle t : mesh.getTriangles()) {
			writer.write("3 ");
			writer.write(Integer.toString(refToVertId.get(t.getVertex(0)
				.getInternalPoolIndex())));
			writer.write(' ');
			writer.write(Integer.toString(refToVertId.get(t.getVertex(1)
				.getInternalPoolIndex())));
			writer.write(' ');
			writer.write(Integer.toString(refToVertId.get(t.getVertex(2)
				.getInternalPoolIndex())));
			writer.write('\n');
		}
		writer.flush();
		return os.toByteArray();
	}

}
