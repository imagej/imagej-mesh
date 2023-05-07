package net.imagej.mesh.util;

import java.util.BitSet;
import java.util.Random;

/**
 * Utilities to sort an array and return the sorting index to sort other lists
 * with.
 */
public class SortArray {

    public static int[] quicksort(final double[] main) {
	final int[] index = new int[main.length];
	for (int i = 0; i < index.length; i++)
	    index[i] = i;
	quicksort(main, index);
	return index;
    }

    public static void quicksort(final double[] main, final int[] index) {
	quicksort(main, index, 0, index.length - 1);
    }

    // quicksort a[left] to a[right]
    public static void quicksort(final double[] a, final int[] index, final int left, final int right) {
	if (right <= left)
	    return;
	final int i = partition(a, index, left, right);
	quicksort(a, index, left, i - 1);
	quicksort(a, index, i + 1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private static int partition(final double[] a, final int[] index, final int left, final int right) {
	int i = left - 1;
	int j = right;
	while (true) {
	    while (less(a[++i], a[right]))
		;
	    while (less(a[right], a[--j]))
		if (j == left)
		    break; // don't go out-of-bounds
	    if (i >= j)
		break; // check if pointers cross
	    exch(a, index, i, j); // swap two elements into place
	}
	exch(a, index, i, right); // swap with partition element
	return i;
    }

    // is x < y ?
    private static final boolean less(final double x, final double y) {
	return (x < y);
    }

    // exchange a[i] and a[j]
    private static void exch(final double[] a, final int[] index, final int i, final int j) {
	final double swap = a[i];
	a[i] = a[j];
	a[j] = swap;
	final int b = index[i];
	index[i] = index[j];
	index[j] = b;
    }

    public static void reorder(final double[] data, final int[] ind) {
	final BitSet done = new BitSet(data.length);
	for (int i = 0; i < data.length && done.cardinality() < data.length; i++) {
	    int ia = i;
	    int ib = ind[ia];
	    if (done.get(ia)) { // index is already done
		continue;
	    }
	    if (ia == ib) { // element is at the right place
		done.set(ia);
		continue;
	    }
	    final int x = ia; // start a loop at x = ia
	    // some next index will be x again eventually
	    final double a = data[ia];
	    // keep element a as the last value after the loop
	    while (ib != x && !done.get(ia)) {
		final double b = data[ib];
		// element from index b must go to index a
		data[ia] = b;
		done.set(ia);
		ia = ib;
		ib = ind[ia]; // get next index
	    }
	    data[ia] = a; // set value a to last index
	    done.set(ia);
	}
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

	final double[] copy = arr.clone();

	System.out.print(String.format("Before sorting: %4.2f", arr[0]));
	for (int i = 1; i < arr.length; i++)
	    System.out.print(String.format(", %4.2f", arr[i]));
	System.out.println();

	final int[] index = quicksort(arr);
	System.out.print(String.format("After sorting:  %4.2f", arr[0]));
	for (int i = 1; i < arr.length; i++)
	    System.out.print(String.format(", %4.2f", arr[i]));
	System.out.println();

	System.out.print(String.format("Index:          %4d", index[0]));
	for (int i = 1; i < arr.length; i++)
	    System.out.print(String.format(", %4d", index[i]));
	System.out.println();

	System.out.print(String.format("Copy:           %4.2f", copy[0]));
	for (int i = 1; i < copy.length; i++)
	    System.out.print(String.format(", %4.2f", copy[i]));
	System.out.println();

	reorder(copy, index);
	System.out.print(String.format("Reorder copy:   %4.2f", copy[0]));
	for (int i = 1; i < copy.length; i++)
	    System.out.print(String.format(", %4.2f", copy[i]));
	System.out.println();
    }
}
