package org.misc;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Class to sort the indexes of an array based upon their values. Note the array passed into the constructor
 * is not itself sorted.
 * 
 * <pre>
 * String[] letters = { &quot;f&quot;, &quot;s&quot;, &quot;a&quot; };
 * IndexComparator&lt;String&gt; comparator = new IndexComparator&lt;String&gt;(letters);
 * comparator.sort();
 * // Now the indexes are in appropriate order.
 * </pre>
 */
public class IndexComparator<T extends Comparable<T>> implements Comparator<Integer> {
	private final T[] array;
	private final Integer[] indexes;

	/**
	 * Constructs a new IndexComparator based upon the parameter array.
	 * 
	 * @param array
	 * @throws NullPointerException if the input array is null
	 */
	@SafeVarargs
	public IndexComparator(T... array) {
		this.array = array;
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
	public T get(int index) {
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
	 * @return
	 */
	public T[] getArray() {
		return array;
	}

	/**
	 * Retrieves the indexes of the array. The returned array is sorted if this object has been sorted.
	 * 
	 * @return The array of indexes.
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
	 */
	@Override
	public int compare(Integer index1, Integer index2) {
		// Autounbox from Integer to int to use as array indexes
		return array[index1].compareTo(array[index2]);
	}

	/**
	 * Sorts the underlying index array based upon the values provided in the constructor. The underlying
	 * value array is not sorted.
	 */
	public IndexComparator<T> sort() {
		Arrays.sort(indexes, this);
		return this;
	}

	@Override
	public String toString() {
		return "array=" + Arrays.toString(array) + ", indexes=" + Arrays.toString(indexes);
	}

	public static Integer[] createIndices(int size) {
		Integer[] indices = new Integer[size];
		for (int index = 0; index < size; index++) {
			indices[index] = index; // Autoboxing
		}
		return indices;
	}

	public static void test() {
		String[] letters = { "f", "s", "a" };
		IndexComparator<String> comparator = new IndexComparator<String>(letters);
		System.out.println("array = " + Arrays.toString(comparator.getArray()));
		System.out.println("before sorting, indexes=" + Arrays.toString(comparator.getIndexes()));
		comparator.sort();
		System.out.println("after sorting, indexes=" + Arrays.toString(comparator.getIndexes()));
		// Now the indexes are in appropriate order.
		System.out.println("result: " + comparator);
	}
}