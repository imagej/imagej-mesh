package net.imagej.mesh.stl;

import net.imagej.mesh.Vertex3Pool;
import net.imagej.mesh.Vertex3;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mastodon.collection.RefList;
import org.mastodon.collection.ref.RefArrayList;

public class Vertex3PoolTest {
	@Test
	public void testVertexAdd() throws Exception {
		final Vertex3Pool vp = new Vertex3Pool( 10 );
		
		final RefList< Vertex3 > myVertices = new RefArrayList<>( vp );
		
		for ( int i = 0; i < 10; ++i )
		{
			final Vertex3 v = vp.create().init( i, 0, 0, 1, 1, 1, 0, 0 );
			myVertices.add( v );
		}
		
		//myVertices.
		float zsum = 0;
		for ( int i = 0; i < vp.size(); i++ ) 
		{
			zsum += myVertices.get(i).getX();
		}
		
		assertEquals( 45, zsum, 0.01 );
	}

}
