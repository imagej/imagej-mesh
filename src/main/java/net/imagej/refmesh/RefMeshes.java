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

package net.imagej.refmesh;

import java.util.HashMap;
import java.util.Map;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefObjectMap;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.collection.ref.RefObjectHashMap;
import org.mastodon.collection.ref.RefRefHashMap;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.kdtree.IncrementalNearestNeighborSearchOnKDTree;
import org.mastodon.kdtree.KDTree;

/**
 * Illustrates how Meshes.calculateNormals would look with RefMesh wrapper
 * with various degrees of sacrificing convenience for performance.
 */
public class RefMeshes
{
	public static void mergeVertices( RefMesh src, RefMesh dest )
	{
		mergeVertices_ReuseProxies( src, dest );
	}

	public static void mergeVertices( RefMesh src, RefMesh dest, Method method )
	{
		switch( method )
		{
		case REUSEPROXIES:
			mergeVertices_ReuseProxies( src, dest );
			break;
		case REFCOLLECTIONS:
		default:
			mergeVertices_Naive( src, dest );
			break;
		}
	}

	public static void mergeVertices_ReuseProxies( RefMesh src, RefMesh dest )
	{
		final double EPSILON = 0.0000001;

		final VertexRef tmpVx0 = src.vertices().createRef();
		final VertexRef tmpVx1 = src.vertices().createRef();
		final VertexRef tmpVx2 = src.vertices().createRef();
		final VertexRef tmpVx3 = src.vertices().createRef();
		final TriangleRef tmpTr0 = src.triangles().createRef();

		// Copy unique vertices
		long t0 = System.currentTimeMillis();
		final RefRefMap< VertexRef, VertexRef > vMap = new RefRefHashMap<>( src.vertices(), dest.vertices() );
		final IncrementalNearestNeighborSearch< VertexRef > neighbors = new IncrementalNearestNeighborSearchOnKDTree<>(
				KDTree.kdtree( src.vertices().asRefCollection(), src.vertices() ) );
		final RefList< VertexRef > mapsToDestVertex = new RefArrayList<>( src.vertices() );
		for ( final VertexRef vertex : src.vertices() )
		{
			if ( vMap.containsKey( vertex ) )
				continue;

			neighbors.search( vertex );
			mapsToDestVertex.clear();
			mapsToDestVertex.add( vertex );
			VertexRef destVertex = null;
			while ( neighbors.hasNext() && destVertex == null )
			{
				final VertexRef neighbor = neighbors.next();
				final double squareDistance = neighbors.getSquareDistance();
				if ( squareDistance > EPSILON )
					destVertex = dest.vertices().add( tmpVx0 ).set( vertex );
				else
				{
					destVertex = vMap.get( neighbor, tmpVx0 );
					if ( destVertex == null )
						mapsToDestVertex.add( neighbor );
				}
			}
			for ( VertexRef v : mapsToDestVertex )
				vMap.put( v, destVertex, tmpVx1 );
		}

		// Copy the triangles, taking care to use destination indices.
		for ( final TriangleRef tri : src.triangles() )
		{
			final VertexRef v0 = vMap.get( tri.vertex0( tmpVx3 ), tmpVx0 );
			final VertexRef v1 = vMap.get( tri.vertex1( tmpVx3 ), tmpVx1 );
			final VertexRef v2 = vMap.get( tri.vertex2( tmpVx3 ), tmpVx2 );
			dest.triangles().add( v0, v1, v2, tri.getNormal(), tmpTr0 );
		}
	}

	public static void mergeVertices_Naive( RefMesh src, RefMesh dest )
	{
		final double EPSILON = 0.0000001;

		// Copy unique vertices
		final RefRefMap<VertexRef, VertexRef> vMap = new RefRefHashMap<>( src.vertices(), dest.vertices() );
		final IncrementalNearestNeighborSearch< VertexRef > neighbors = new IncrementalNearestNeighborSearchOnKDTree<>(
				KDTree.kdtree( src.vertices().asRefCollection(), src.vertices() ) );
		for ( final VertexRef vertex : src.vertices() )
		{
			neighbors.search( vertex );
			VertexRef destVertex = null;
			while ( neighbors.hasNext() && destVertex == null )
			{
				final VertexRef neighbor = neighbors.next();
				if ( neighbors.getSquareDistance() > EPSILON )
					destVertex = dest.vertices().add().set( vertex );
				else
					destVertex = vMap.get( neighbor );
			}
			vMap.put( vertex, destVertex );
		}

		// Copy the triangles, taking care to use destination indices.
		for ( final TriangleRef tri : src.triangles() )
		{
			final VertexRef v0 = vMap.get( tri.vertex0() );
			final VertexRef v1 = vMap.get( tri.vertex1() );
			final VertexRef v2 = vMap.get( tri.vertex2() );
			dest.triangles().add( v0, v1, v2, tri.getNormal() );
		}
	}

	public enum Method
	{
		NAIVE,
		REFCOLLECTIONS,
		REUSEPROXIES,
		CRAZY
	};

	public static void calculateNormals( RefMesh src, RefMesh dest, Method method )
	{
		// Force recalculation of normals because not all meshes are safe
        switch( method )
		{
		case REFCOLLECTIONS:
			calculateNormals_RefCollections( src, dest );
			break;
		case REUSEPROXIES:
			calculateNormals_RefCollections_ReuseProxies( src, dest );
			break;
		case CRAZY:
			calculateNormals_Mod( src, dest );
			break;
		case NAIVE:
		default:
			calculateNormals_Naive( src, dest );
			break;
		}
	}

	/**
	 * Calculates the normals for a mesh. Creates a new mesh with the calculated normals. Assumes CCW winding order.
	 *
	 * @param src  Source mesh, used for vertex and triangle info
	 * @param dest Destination mesh, will be populated with src's info plus the calculated normals
	 */
	public static void calculateNormals_Naive( RefMesh src, RefMesh dest )
	{
		// Compute the triangle normals.
		final Map< TriangleRef, Vector3f > triNormals = new HashMap<>();
		for ( final TriangleRef tri : src.triangles().safe() )
		{
			VertexRef v0 = tri.vertex0();
			VertexRef v1 = tri.vertex1();
			VertexRef v2 = tri.vertex2();

			final Vector3f v10 = v1.getPosition().sub( v0.getPosition() );
			final Vector3f v20 = v2.getPosition().sub( v0.getPosition() );
			triNormals.put( tri, v10.cross( v20 ).normalize() );
		}

		// Next, compute the normals per vertex based on face normals
		final Map< VertexRef, Vector3f > vNormals = new HashMap<>();// Note: these are cumulative until normalized by vNbrCount
		for ( final TriangleRef tri : src.triangles().safe() )
		{
			final Vector3f triNormal = triNormals.get( tri );
			for ( VertexRef vertex : tri.vertices().safe() )
				vNormals.compute( vertex, ( k, v ) ->
						v == null
								? new Vector3f( triNormal )
								: v.add( triNormal ) );
		}

		// Now populate dest
		final Map< VertexRef, VertexRef > vMap = new HashMap<>();
		// Copy the vertices, keeping track when indices change.
		for ( final VertexRef v : src.vertices().safe() )
		{
			final VertexRef vdest = dest.vertices().add(
					v.getPosition(),
					vNormals.get( v ).normalize(),
					v.getTexture() );
			vMap.put( v, vdest );
		}

		// Copy the triangles, taking care to use destination indices.
		for ( final TriangleRef tri : src.triangles().safe() )
		{
			final VertexRef v0 = vMap.get( tri.vertex0() );
			final VertexRef v1 = vMap.get( tri.vertex1() );
			final VertexRef v2 = vMap.get( tri.vertex2() );
			final Vector3f triNormal = triNormals.get( tri );
			dest.triangles().add( v0, v1, v2, triNormal );
		}
	}

	public static void calculateNormals_RefCollections( RefMesh src, RefMesh dest )
	{
		// Compute the triangle normals.
		final RefObjectMap< TriangleRef, Vector3f > triNormals = new RefObjectHashMap<>( src.triangles() );
		for ( final TriangleRef tri : src.triangles() )
		{
			VertexRef v0 = tri.vertex0();
			VertexRef v1 = tri.vertex1();
			VertexRef v2 = tri.vertex2();

			final Vector3f v10 = v1.getPosition().sub( v0.getPosition() );
			final Vector3f v20 = v2.getPosition().sub( v0.getPosition() );
			triNormals.put( tri, v10.cross( v20 ).normalize() );
		}

		// Next, compute the normals per vertex based on face normals
		final RefObjectMap< VertexRef, Vector3f > vNormals = new RefObjectHashMap<>( src.vertices() );// Note: these are cumulative until normalized by vNbrCount
		for ( final TriangleRef tri : src.triangles() )
		{
			final Vector3f triNormal = triNormals.get( tri );
			for ( VertexRef vertex : tri.vertices() )
				vNormals.compute( vertex, ( k, v ) ->
						v == null
								? new Vector3f( triNormal )
								: v.add( triNormal ) );

		}

		// Now populate dest
		final RefRefMap< VertexRef, VertexRef > vMap = new RefRefHashMap<>( src.vertices(), dest.vertices() );
		// Copy the vertices, keeping track when indices change.
		for ( final VertexRef v : src.vertices() )
		{
			final VertexRef vdest = dest.vertices().add(
					v.getPosition(),
					vNormals.get( v ).normalize(),
					v.getTexture() );
			vMap.put( v, vdest );
		}

		// Copy the triangles, taking care to use destination indices.
		for ( final TriangleRef tri : src.triangles() )
		{
			final VertexRef v0 = vMap.get( tri.vertex0() );
			final VertexRef v1 = vMap.get( tri.vertex1() );
			final VertexRef v2 = vMap.get( tri.vertex2() );
			final Vector3f triNormal = triNormals.get( tri );
			dest.triangles().add( v0, v1, v2, triNormal );
		}
	}

	public static void calculateNormals_RefCollections_ReuseProxies( RefMesh src, RefMesh dest )
	{
		final VertexRef tmpVx0 = src.vertices().createRef();
		final VertexRef tmpVx1 = src.vertices().createRef();
		final VertexRef tmpVx2 = src.vertices().createRef();
		final VertexRef tmpVx3 = src.vertices().createRef();
		final TriangleRef tmpTr0 = src.triangles().createRef();

		// Compute the triangle normals.
		final RefObjectMap< TriangleRef, Vector3f > triNormals = new RefObjectHashMap<>( src.triangles() );
		for ( final TriangleRef tri : src.triangles() )
		{
			VertexRef v0 = tri.vertex0( tmpVx0 );
			VertexRef v1 = tri.vertex1( tmpVx1 );
			VertexRef v2 = tri.vertex2( tmpVx2 );

			final Vector3f v10 = v1.getPosition().sub( v0.getPosition() );
			final Vector3f v20 = v2.getPosition().sub( v0.getPosition() );
			triNormals.put( tri, v10.cross( v20 ).normalize() );
		}

		// Next, compute the normals per vertex based on face normals
		final RefObjectMap< VertexRef, Vector3f > vNormals = new RefObjectHashMap<>( src.vertices() );// Note: these are cumulative until normalized by vNbrCount
		for ( final TriangleRef tri : src.triangles() )
		{
			final Vector3f triNormal = triNormals.get( tri );
			for ( VertexRef vertex : tri.vertices() )
				vNormals.compute( vertex, ( k, v ) ->
						v == null
								? new Vector3f( triNormal )
								: v.add( triNormal ) );
		}

		// Now populate dest
		final RefRefMap< VertexRef, VertexRef > vMap = new RefRefHashMap<>( src.vertices(), dest.vertices() );
		// Copy the vertices, keeping track when indices change.
		for ( final VertexRef v : src.vertices() )
		{
			final VertexRef vdest = dest.vertices().add(
					v.getPosition(),
					vNormals.get( v ).normalize(),
					v.getTexture(),
					tmpVx0 );
			vMap.put( v, vdest, tmpVx1 );
		}

		// Copy the triangles, taking care to use destination indices.
		for ( final TriangleRef tri : src.triangles() )
		{
			final VertexRef v0 = vMap.get( tri.vertex0( tmpVx3 ), tmpVx0 );
			final VertexRef v1 = vMap.get( tri.vertex1( tmpVx3 ), tmpVx1 );
			final VertexRef v2 = vMap.get( tri.vertex2( tmpVx3 ), tmpVx2 );
			final Vector3f triNormal = triNormals.get( tri );
			dest.triangles().add( v0, v1, v2, triNormal, tmpTr0 );
		}
	}

	public static void calculateNormals_Mod( RefMesh src, RefMesh dest )
	{
		final VertexRef tmpVx0 = src.vertices().createRef();
		final VertexRef tmpVx1 = src.vertices().createRef();
		final VertexRef tmpVx2 = src.vertices().createRef();
		final VertexRef tmpVx3 = src.vertices().createRef();
		final TriangleRef tmpTr0 = src.triangles().createRef();
		final Vector3f tmpVc0 = new Vector3f();
		final Vector3f tmpVc1 = new Vector3f();
		final Vector3f tmpVc2 = new Vector3f();
		final Vector2f tmpVc3 = new Vector2f();

		// Compute the triangle normals.
		final RefObjectMap< TriangleRef, Vector3f > triNormals = new RefObjectHashMap<>( src.triangles() );
		for ( final TriangleRef tri : src.triangles() )
		{
			VertexRef v0 = tri.vertex0( tmpVx0 );
			VertexRef v1 = tri.vertex1( tmpVx1 );
			VertexRef v2 = tri.vertex2( tmpVx2 );

			final Vector3f v10 = v1.getPosition( tmpVc1 ).sub( v0.getPosition( tmpVc0 ) );
			final Vector3f v20 = v2.getPosition( tmpVc2 ).sub( v0.getPosition( tmpVc0 ) );
			triNormals.put( tri, v10.cross( v20 ).normalize( new Vector3f() ) );
		}

		// Next, compute the normals per vertex based on face normals
		final RefObjectMap< VertexRef, Vector3f > vNormals = new RefObjectHashMap<>( src.vertices() );
		for ( final TriangleRef tri : src.triangles() )
		{
			final Vector3f triNormal = triNormals.get( tri );
			for ( VertexRef vertex : tri.vertices() )
			{
				vNormals.compute( vertex, ( k, v ) ->
						v == null
								? new Vector3f( triNormal )
								: v.add( triNormal ) );
			}
		}

		// Now populate dest
		final RefRefMap< VertexRef, VertexRef > vMap = new RefRefHashMap<>( src.vertices(), dest.vertices() );
		// Copy the vertices, keeping track when indices change.
		for ( final VertexRef v : src.vertices() )
		{
			final VertexRef vdest = dest.vertices().add(
					v.getPosition( tmpVc0 ),
					vNormals.get( v ).normalize(),
					v.getTexture( tmpVc3 ),
					tmpVx0 );
			if ( v.getInternalPoolIndex() != vdest.getInternalPoolIndex() )
				vMap.put( v, vdest, tmpVx1 );
		}

		// Copy the triangles, taking care to use destination indices.
		for ( final TriangleRef tri : src.triangles() )
		{
			final VertexRef tv0 = tri.vertex0( tmpVx3 );
			VertexRef v0 = vMap.get( tv0, tmpVx0 );
			if ( v0 == null )
				v0 = dest.vertices().getObject( tv0.getInternalPoolIndex(), tmpVx0 );

			final VertexRef tv1 = tri.vertex1( tmpVx3 );
			VertexRef v1 = vMap.get( tv1, tmpVx1 );
			if ( v1 == null )
				v1 = dest.vertices().getObject( tv1.getInternalPoolIndex(), tmpVx1 );

			final VertexRef key = tri.vertex2( tmpVx3 );
			VertexRef v2 = vMap.get( key, tmpVx2 );
			if ( v2 == null )
				v2 = dest.vertices().getObject( key.getInternalPoolIndex(), tmpVx2 );

			final Vector3f triNormal = triNormals.get( tri );
			dest.triangles().add( v0, v1, v2, triNormal, tmpTr0 );
		}
	}
}
