package net.imagej.mesh.alg.zslicer;

import java.util.ArrayList;
import java.util.List;

public class RamerDouglasPeucker {

    /**
     * Applies the Ramer-Douglas-Peucker algorithm to the contours of a slice.
     */
    public static final Slice simplify(final Slice slice, final double epsilon) {

	final List<Contour> simplifiedSlice = new ArrayList<>(slice.size());
	for (final Contour contour : slice) {
	    final Contour out = new Contour(contour.isInterior());
	    douglasPeucker(contour, 0, contour.size(), epsilon, out);
	    simplifiedSlice.add(out);
	}
	return new Slice(simplifiedSlice);
    }

    /**
     * Given a contour, build a similar contour with fewer points.
     * <p>
     * The Ramer–Douglas–Peucker algorithm (RDP) is an algorithm for reducing the
     * number of points in a curve that is approximated by a series of points.
     * <p>
     *
     * @see <a href=
     *      "https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm">Ramer–Douglas–Peucker
     *      Algorithm (Wikipedia)</a>
     * @author Justin Wetherell
     *
     * @param in      the contour to simplify.
     * @param epsilon distance threshold.
     * @return a new simplified contour.
     */
    public static final Contour simplify(final Contour in, final double epsilon) {
	final Contour out = new Contour(in.isInterior());
	douglasPeucker(in, 0, in.size(), epsilon, out);
	return out;
    }

    private static final void douglasPeucker(final Contour in, final int s, final int e, final double epsilon,
	    final Contour out) {
	// Find the point with the maximum distance
	double dmax = 0;
	int index = 0;

	final int start = s;
	final int end = e - 1;
	for (int i = start + 1; i < end; i++) {
	    // Point
	    final double px = in.x(i);
	    final double py = in.y(i);
	    // Start
	    final double vx = in.x(start);
	    final double vy = in.y(start);
	    // End
	    final double wx = in.x(end);
	    final double wy = in.y(end);
	    final double d = perpendicularDistance(px, py, vx, vy, wx, wy);
	    if (d > dmax) {
		index = i;
		dmax = d;
	    }
	}
	// If max distance is greater than epsilon, recursively simplify
	if (dmax > epsilon) {
	    // Recursive call
	    douglasPeucker(in, s, index, epsilon, out);
	    douglasPeucker(in, index, e, epsilon, out);
	} else {
	    if ((end - start) > 0) {
		out.add(in.x(start), in.y(start), in.nx(start), in.ny(start));
		out.add(in.x(end), in.y(end), in.nx(end), in.ny(end));
	    } else {
		// Compute mean normals.
		double sumnx = 0.;
		double sumny = 0.;
		for (int i = start; i < end; i++) {
		    sumnx += in.nx(i);
		    sumny += in.ny(i);
		}
		final int n = (end - start);
		out.add(in.x(start), in.y(start), sumnx / n, sumny / n);
	    }
	}
    }

    private static final double perpendicularDistance(final double px, final double py, final double vx,
	    final double vy, final double wx, final double wy) {
	return Math.sqrt(distanceToSegmentSquared(px, py, vx, vy, wx, wy));
    }

    private static final double distanceToSegmentSquared(final double px, final double py, final double vx,
	    final double vy, final double wx, final double wy) {
	final double l2 = distanceSquaredBetweenPoints(vx, vy, wx, wy);
	if (l2 == 0)
	    return distanceSquaredBetweenPoints(px, py, vx, vy);
	final double t = ((px - vx) * (wx - vx) + (py - vy) * (wy - vy)) / l2;
	if (t < 0)
	    return distanceSquaredBetweenPoints(px, py, vx, vy);
	if (t > 1)
	    return distanceSquaredBetweenPoints(px, py, wx, wy);
	return distanceSquaredBetweenPoints(px, py, (vx + t * (wx - vx)), (vy + t * (wy - vy)));
    }

    private static final double distanceSquaredBetweenPoints(final double vx, final double vy, final double wx,
	    final double wy) {
	final double deltax = (vx - wx);
	final double deltay = (vy - wy);
	return deltax * deltax + deltay * deltay;
    }
}
