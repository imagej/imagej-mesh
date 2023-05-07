package net.imagej.mesh.util;

import java.util.Random;

/**
 * Utilities to sort an index array pointing to values in another array.
 */
public class SortBy {

    /*
     * Sorting.
     */

    public static void sortBy(final int[] index, final double[] arr, final int left, final int right) {
	if (right <= left)
	    return;

	final int i = partition(index, arr, left, right);
	sortBy(index, arr, left, i - 1);
	sortBy(index, arr, i + 1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private static int partition(final int[] index, final double[] arr, final int left, final int right) {
	int i = left - 1;
	int j = right;
	while (true) {
	    while (less(arr[index[++i]], arr[index[right]]))
		;
	    while (less(arr[index[right]], arr[index[--j]]))
		if (j == left)
		    break; // don't go out-of-bounds
	    if (i >= j)
		break; // check if pointers cross
	    exch(index, arr, i, j); // swap two elements into place
	}
	exch(index, arr, i, right); // swap with partition element
	return i;
    }

    // is x < y ?
    private static final boolean less(final double x, final double y) {
	return (x < y);
    }

    private static void exch(final int[] index, final double[] arr, final int i, final int j) {
	final int b = index[i];
	index[i] = index[j];
	index[j] = b;
    }

    /*
     * Binary search.
     */

    public static int binarySearch(final int[] indices, final double[] a, final int fromIndex, final int toIndex,
	    final double key) {
	int low = fromIndex;
	int high = toIndex - 1;

	while (low <= high) {
	    final int mid = (low + high) >>> 1;
	    final double midVal = a[indices[mid]];

	    if (midVal < key)
		low = mid + 1; // Neither val is NaN, thisVal is smaller
	    else if (midVal > key)
		high = mid - 1; // Neither val is NaN, thisVal is larger
	    else {
		final long midBits = Double.doubleToLongBits(midVal);
		final long keyBits = Double.doubleToLongBits(key);
		if (midBits == keyBits) // Values are equal
		    return mid; // Key found
		else if (midBits < keyBits) // (-0.0, 0.0) or (!NaN, NaN)
		    low = mid + 1;
		else // (0.0, -0.0) or (NaN, !NaN)
		    high = mid - 1;
	    }
	}
	return -(low + 1); // key not found.
    }

    /*
     * Main.
     */

    public static void main(final String[] args) {

	final Random ran = new Random(1l);
	final int n = 10;
	final double[] arr = new double[n];
	for (int i = 0; i < n; i++)
	    arr[i] = ran.nextDouble();

	System.out.print(String.format("Before sorting: %4.2f", arr[0]));
	for (int i = 1; i < arr.length; i++)
	    System.out.print(String.format(", %4.2f", arr[i]));
	System.out.println();

	final int[] index = SortArray.quicksort(arr);
	System.out.print(String.format("After sorting:  %4.2f", arr[0]));
	for (int i = 1; i < arr.length; i++)
	    System.out.print(String.format(", %4.2f", arr[i]));
	System.out.println();

	System.out.print(String.format("Index:          %4d", index[0]));
	for (int i = 1; i < arr.length; i++)
	    System.out.print(String.format(", %4d", index[i]));
	System.out.println();

	System.out.println();
	final double[] arr2 = new double[n];
	for (int i = 0; i < n; i++)
	    arr2[i] = ran.nextDouble();

	System.out.print(String.format("New array:      %4.2f", arr2[0]));
	for (int i = 1; i < arr2.length; i++)
	    System.out.print(String.format(", %4.2f", arr2[i]));
	System.out.println();

	final int jmax = 7;
	System.out.print(String.format("Before sorting:                    %4d", index[0]));
	for (int i = 1; i < jmax; i++)
	    System.out.print(String.format(", %4d", index[i]));
	System.out.println();

	System.out.print(String.format("Values in array for these indices: %4.2f", arr2[index[0]]));
	for (int i = 1; i < jmax; i++)
	    System.out.print(String.format(", %4.2f", arr2[index[i]]));
	System.out.println();

	System.out.println("Sort first " + jmax + " elements index by new array:");
	sortBy(index, arr2, 0, jmax - 1);

	System.out.print(String.format("After sorting:                     %4d", index[0]));
	for (int i = 1; i < jmax; i++)
	    System.out.print(String.format(", %4d", index[i]));
	System.out.println();

	System.out.print(String.format("Sorted array                       %4.2f", arr2[index[0]]));
	for (int i = 1; i < jmax; i++)
	    System.out.print(String.format(", %4.2f", arr2[index[i]]));
	System.out.println();
	System.out.println();

	double key = 0;
	for (int i = 0; i < n + 1; i++) {
	    System.out.print(String.format("Let's search for: %.2f", key));
	    int k = binarySearch(index, arr2, 0, jmax, key);
	    if (k < 0)
		k = -(k + 1);
	    System.out.println(" -> " + k);

	    key += 0.1;
	}

    }
}
