package net.imagej.mesh;

import java.util.List;

import org.mastodon.collection.RefList;
import org.mastodon.collection.ref.RefArrayList;

public class DefaultMesh implements Mesh {
	protected final TrianglePool tp;
	protected final Vertex3Pool vp;
	
	protected final RefList< Triangle > triangles;
	protected final RefList< Vertex3 > vertices;
	
	public DefaultMesh() {
		vp = new Vertex3Pool( 1 );// TODO these are set to 1 because of a bug in mastodon-collection
		tp = new TrianglePool( vp, 1 );
		
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

}
