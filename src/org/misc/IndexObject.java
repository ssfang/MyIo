package org.misc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author fangss
 * 
 * @param <T>
 */
public class IndexObject<T extends Comparable<T>> implements Comparable<IndexObject<T>> {
	public final int index;
	private final T obj;

	public IndexObject(int index, T obj) {
		this.index = index;
		this.obj = obj;
	}

	@Override
	public int compareTo(IndexObject<T> o) {
		return obj.compareTo(o.obj);
	}

	public static <E extends Comparable<E>> IndexObject<E>[] createIndexArray(E[] array) {
		int length = array.length;
		@SuppressWarnings("unchecked")
		IndexObject<E>[] indexes = new IndexObject[length];
		for (int i = 0; i < length; i++) {
			indexes[i] = new IndexObject<E>(i, array[i]);
		}
		return indexes;
	}

	public static int[] getIndexArray(IndexObject<?>[] array) {
		int length = array.length;
		int[] indexes = new int[length];
		for (int i = 0; i < length; i++) {
			indexes[i] = array[i].index;
		}
		return indexes;
	}

	public static int[] sortedIndexesOf(String[] array) {
		IndexObject<String>[] indexArray = IndexObject.createIndexArray(array);
		Arrays.sort(indexArray);
		return IndexObject.getIndexArray(indexArray);
	}

	public static int[] sortedIndexesOf(Object[] array, Comparator<Object> comparator) {
		Arrays.sort(array, comparator);
		return null;
	}

	/**
	 * Get the indices of the input array <code>selection</code> in the array <code>inArray</code>
	 * 
	 * @param inArray an referred array
	 * @param selection a target array to create the indices
	 * @return the indices of the parameter <code>selection</code> in <code>inArray</code>
	 * @throws NullPointerException if the either input parameter is <code>null</code>, or one of the array
	 *             <code>selection</code> is not contained in <code>inArray</code>
	 */
	public static int[] getIndexesIn(Object[] inArray, Object[] selection) {
		int length = selection.length;
		int[] indexes = new int[length];
		Map<Object, Integer> map = toObject2IndexMap(inArray);
		for (; --length >= 0;) {
			indexes[length] = map.get(selection[length]);
		}
		return indexes;
	}

	/**
	 * Get a map with its content as the key and its indexes in the original array as the value
	 * 
	 * @param array the array to be converted
	 * @return An object that maps keys to values
	 * @throws NullPointerException if the input array is <code>null</code>
	 */
	public static <T> Map<T, Integer> toObject2IndexMap(T[] array) {
		int length = array.length;
		HashMap<T, Integer> map = new HashMap<T, Integer>(length);
		for (; --length >= 0;) {
			map.put(array[length], length);
		}
		return map;
	}

	/**
	 * Get a sorted map with its content as the key and its indexes in the original array as the value
	 * 
	 * @param <T> T extends Comparable, but the input is a Object array
	 * @param array the array to be converted
	 * @return An object that maps keys to values, which is sorted in the keys.
	 * @throws ClassCastException if the array contains elements that are not <i>mutually comparable</i> (for
	 *             example, strings and integers)
	 */
	public static <T> Map<T, Integer> toObject2IndexTreeMap(T[] array) {
		int length = array.length;
		// Comparable.class.isAssignableFrom(array.getClass().getComponentType());
		// Comparable.class.isAssignableFrom(array[0].getClass());
		TreeMap<T, Integer> map = new TreeMap<T, Integer>();
		try {
			for (; --length >= 0;) {
				map.put(array[length], length);
			}
		} catch (ClassCastException e) {
			// java.lang.ClassCastException: [I cannot be cast to java.lang.Comparable
			// when creating a TreeMap without Comparator
			throw e;
		}
		return map;
	}

	public static <T> Map<T, Integer> toObject2IndexMap(T[] array, Class<? extends Map<T, Integer>> clz) {
		int length = array.length;
		Map<T, Integer> map;
		try {
			map = clz.newInstance();
			for (; --length >= 0;) {
				map.put(array[length], length);
			}
			return map;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void test() {
		String[] sNames = { "t", "a", "b", "c", "d" };
		String[] cNames = { "t", "c", "b" };
		Object[] cValues = { "t1", "c1", "b1" };
		String[] selectedNames = { "b", "c" };

		int[] indexes = getIndexesIn(cNames, selectedNames);
		for (int i : indexes) {
			System.out.println(cValues[i]);
		}
		System.out.println(Arrays.toString(indexes));
		System.out.println(Arrays.toString(sortedIndexesOf(sNames)));
		Collection<Integer> sIndexes = toObject2IndexTreeMap(sNames).values();
		System.out.println(Arrays.toString(CollectionUtil.toIntArray(sIndexes)));
		// java.lang.ClassCastException: [I cannot be cast to java.lang.Comparable
		System.out.println(toObject2IndexTreeMap(new Object[] { new int[] { 2 } }));
	}
}