
package net.imagej.mesh;

import java.util.List;

public interface Mesh {

	public List<Triangle> getTriangles();

	public List<Vertex3> getVertices();

	public TrianglePool getTrianglePool();

	public Vertex3Pool getVertex3Pool();

	public void addFacet(Triangle facet);

	public void setTriangles(List<Triangle> read);

	public void setVertices(List<Vertex3> vertices);
}
