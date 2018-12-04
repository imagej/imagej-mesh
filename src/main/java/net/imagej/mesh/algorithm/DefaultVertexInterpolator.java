/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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
package net.imagej.mesh.algorithm;

/**
 * Linearly interpolate the position where an isosurface cuts an edge
 * between two vertices, each with their own scalar value.
 * 
 * @author Tim-Oliver Buchholz (University of Konstanz)
 * @author Curtis Rueden
 */
public class DefaultVertexInterpolator implements VertexInterpolator {

	@Override
	public void interpolate(final double[] output, final int[] point1,
		final int[] point2, final double value1, final double value2,
		final double isoLevel)
	{
		if (Math.abs(isoLevel - value1) < 0.00001) {
			for (int i = 0; i < 3; i++) {
				output[i] = point1[i];
			}
		}
		else if (Math.abs(isoLevel - value2) < 0.00001) {
			for (int i = 0; i < 3; i++) {
				output[i] = point2[i];
			}
		}
		else if (Math.abs(value1 - value2) < 0.00001) {
			for (int i = 0; i < 3; i++) {
				output[i] = point1[i];
			}
		}
		else {
			final double mu = (isoLevel - value1) / (value2 - value1);

			output[0] = point1[0] + mu * (point2[0] - point1[0]);
			output[1] = point1[1] + mu * (point2[1] - point1[1]);
			output[2] = point1[2] + mu * (point2[2] - point1[2]);
		}
	}

}
