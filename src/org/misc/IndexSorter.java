package org.misc;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Class to sort the indexes of an array based upon their values. Note the array passed into the constructor
 * is not itself sorted.
 * 
 * <pre>
 * String[] letters = { &quot;f&quot;, &quot;s&quot;, &quot;a&quot; };
 * IndexSorter&lt;String&gt; comparator = new IndexSorter&lt;String&gt;(letters);
 * comparator.sort();
 * // Now the indexes are in appropriate order.
 * </pre>
 * 
 * @param <E> a object type, but the Comparable object type is a good choice if using the method
 *            {@link #compare(Integer, Integer)}.
 * @see #sort()
 */
public class IndexSorter<E> implements Comparator<Integer> {
	protected final E[] array;
	protected final Integer[] indexes;

	/**
	 * Constructs a new IndexSorter based upon the parameter array.
	 * 
	 * @param <T> the component type of the array
	 * @param array
	 * @throws NullPointerException if the input array is null
	 */
	@SuppressWarnings("unchecked")
	public <T extends Comparable<T>> IndexSorter(T... array) {
		this.array = (E[]) array;
		indexes = createIndices(array.length);
	}

	/**
	 * Retrieves the element at the specified position in the array. Note that the returned element is sorted
	 * if this object has been sorted.
	 * 
	 * @param index index of the element to return
	 * @return the element at the specified position in the array
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
	 */
	public E get(int index) {
		return array[indexes[index]];
	}

	/**
	 * Returns the number of elements in the array.
	 * 
	 * @return the number of elements in the array
	 */
	public int size() {
		return array.length;
	}

	/**
	 * Retrieves the values provided in the constructor. Note that the array itself is not sorted
	 * 
	 * @return the values provided in the constructor
	 */
	public E[] getArray() {
		return array;
	}

	/**
	 * Retrieves the indexes of the array. The returned array is sorted if this object has been sorted.
	 * 
	 * @return the index array in the original array.
	 */
	public Integer[] getIndexes() {
		return indexes;
	}

	/**
	 * Compares the two values at index <code>index1</code> and <code>index2</code>
	 * 
	 * @param index1 The first index
	 * @param index2 The second index
	 * @return The result of calling compareTo on T objects at position <code>index1</code> and
	 *         <code>index2</code>
	 * @throws ClassCastException if the array contains elements that are not <i>mutually comparable</i> (for
	 *             example, strings and integers)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int compare(Integer index1, Integer index2) {
		// Autounbox from Integer to int to use as array indexes
		return ((Comparable<E>) array[index1]).compareTo(array[index2]);
	}

	/**
	 * Sorts the underlying index array based upon the values provided in the constructor. The underlying
	 * value array is not sorted.
	 * 
	 * @throws ClassCastException if the array contains elements that are not <i>mutually comparable</i> (for
	 *             example, strings and integers)
	 * @throws IllegalArgumentException (optional) if the natural ordering of the array elements is found to
	 *             violate the {@link Comparable} contract
	 */
	public IndexSorter<E> sort() {
		Arrays.sort(indexes, this);
		return this;
	}

	@Override
	public String toString() {
		return "array=" + Arrays.toString(array) + ", indexes=" + Arrays.toString(indexes);
	}

	/**
	 * create a continuous zero-based Integer type indexes array.
	 * 
	 * @param size the size of the array
	 * @return a continuous zero-based Integer type indexes array.
	 */
	public static Integer[] createIndices(int size) {
		Integer[] indices = new Integer[size];
		for (int index = 0; index < size; index++) {
			indices[index] = index; // Autoboxing
		}
		return indices;
	}

	/**
	 * get a new re-arrayed array by the specific index order
	 * 
	 * @param array the array to be re-arrayed, but it cannot be re-arrayed.
	 * @param indexes an index array to re-array
	 * @return a new re-arrayed array
	 */
	public static <T> T[] rearrayByIndex(T[] array, Integer[] indexes) {
		T[] newarray = array.clone();
		int length = array.length;
		for (int index = 0; index < length; index++) {
			newarray[index] = array[indexes[index]];
		}
		return newarray;
	}

	public static void test() {
		String[] letters = { "f", "s", "a" };
		IndexSorter<String> comparator = new IndexSorter<String>(letters);
		System.out.println("array = " + Arrays.toString(comparator.getArray()));
		System.out.println("before sorting, indexes=" + Arrays.toString(comparator.getIndexes()));
		comparator.sort();
		System.out.println("after sorting, indexes=" + Arrays.toString(comparator.getIndexes()));
		// Now the indexes are in appropriate order.
		System.out.println("result: " + comparator);
		System.out.println("rearray: " + Arrays.toString(rearrayByIndex(letters, comparator.getIndexes())));
	}
}