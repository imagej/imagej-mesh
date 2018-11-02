package net.imagej.refmesh;

import net.imagej.mesh.Mesh;
import net.imagej.mesh.Vertices;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.mastodon.Ref;

public class RefMesh
{
	final Mesh mesh;

	final VertexPool vertexPool;

	private final TrianglePool trianglePool;

	public RefMesh( final Mesh mesh )
	{
		this.mesh = mesh;
		vertexPool = new VertexPool( this );
		trianglePool = new TrianglePool( this );
	}

	public VertexPool vertices()
	{
		return vertexPool;
	}

	public TrianglePool triangles()
	{
		return trianglePool;
	}

	/*
	 * HELPERS
	 */

	static int safeInt( final long index )
	{
		if ( index > Integer.MAX_VALUE )
		{
			throw new IndexOutOfBoundsException( "Index too large: " + index );
		}
		return ( int ) index;
	}
}
