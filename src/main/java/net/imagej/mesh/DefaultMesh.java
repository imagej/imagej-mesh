package net.imagej.mesh;

import java.util.List;

import org.mastodon.collection.ref.RefArrayList;

public class DefaultMesh implements Mesh {
	protected final TrianglePool tp;
	protected final Vertex3Pool vp;
	
	protected final List< Triangle > triangles;
	protected final List< Vertex3 > vertices;
	
	public DefaultMesh() {
		this(1,1);
	}
	
	public DefaultMesh( int numVerts, int numTriangles ) {
		vp = new Vertex3Pool( numVerts );// TODO these are set to 1 because of a bug in mastodon-collection
		tp = new TrianglePool( vp, numTriangles );
		
		this.vertices = new RefArrayList<>( vp );
		this.triangles = new RefArrayList<>( tp );
	}
	
	@Override
	public List<Triangle> getTriangles() {
		return triangles;
	}

	@Override
	public List<Vertex3> getVertices() {
		return vertices;
	}

	@Override
	public TrianglePool getTrianglePool() {
		return tp;
	}

	@Override
	public Vertex3Pool getVertex3Pool() {
		return vp;
	}

	@Override
	public void addFacet(Triangle facet) {
		this.triangles.add( facet );
	}

	@Override
	public void setTriangles(List<Triangle> triangles) {
		this.triangles.clear();
		this.triangles.addAll(triangles);
	}

	@Override
	public void setVertices(List<Vertex3> vertices) {
		this.vertices.clear();
		this.vertices.addAll(vertices);
	}

}
