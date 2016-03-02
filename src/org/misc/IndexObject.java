package org.misc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * a wrapper class to an object with an integer.
 * 
 * @author fangss
 * 
 * @param <T> a Comparable object type is a good choice if using the method {@link #compareTo(IndexObject)}.
 */
public class IndexObject<T> implements Comparable<IndexObject<T>> {
	public final int index;
	private final T obj;

	/**
	 * @param index the index associated with the input parameter <code>obj</code>
	 * @param obj an object, but a Comparable type object is a good choice if using the method
	 *            {@link #compareTo(IndexObject)}.
	 */
	@SuppressWarnings("unchecked")
	public IndexObject(int index, Object obj) {
		this.index = index;
		this.obj = (T) obj;
	}

	/**
	 * @param o the object to be compared.
	 * @throws ClassCastException if the array contains elements that are not <i>mutually comparable</i> (for
	 *             example, strings and integers)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(IndexObject<T> o) {
		return ((Comparable<T>) obj).compareTo(o.obj);
	}

	/**
	 * get a wrapper array that wrap each element of the input array as a IndexObject object
	 * 
	 * @param array an array to be wrapped
	 * @return a new array of which each element is an IndexObject object wrapper for the input array element
	 */
	public static <E> IndexObject<E>[] wrapAsIndexObjectArray(E... array) {
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

	/**
	 * @param <E> the component type of the array
	 * @param array an array to be sorted by. note it's not really sorted.
	 * @return the index array in the original array.
	 * @see Arrays#sort(Object[]);
	 */
	public static <E extends Comparable<E>> int[] sortedIndexesOf(E... array) {
		IndexObject<E>[] indexArray = IndexObject.wrapAsIndexObjectArray(array);
		Arrays.sort(indexArray);
		return IndexObject.getIndexArray(indexArray);
	}

	public static int[] sortedIndexesOf(Object[] array, Comparator<Object> comparator) {
		IndexObject<Object>[] indexArray = IndexObject.wrapAsIndexObjectArray(array);
		Arrays.sort(array, comparator);
		return IndexObject.getIndexArray(indexArray);
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
	public static <T> TreeMap<T, Integer> toObject2IndexTreeMap(T... array) {
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

	/**
	 * 
	 * Retrieves the primitive integer array from the values of a map associated with the specific keys
	 * <p/>
	 * e.g.
	 * 
	 * <pre>
	 * String[] array = { &quot;b0&quot;, &quot;c1&quot;, &quot;d2&quot;, &quot;a3&quot; };
	 * TreeMap&lt;String, Integer&gt; obj2IdxMap = toObject2IndexTreeMap(array);
	 * System.out.println(obj2IdxMap);
	 * 
	 * System.out.println(Arrays.toString(getIntValues(obj2IdxMap, &quot;b0&quot;, &quot;c1&quot;)));
	 * </pre>
	 * 
	 * @param map An map object
	 * @param keys the keys to get
	 * @return a primitive integer array from the values of the map
	 * @throws NullPointerException if the map doesn't contain one of the keys
	 */
	public static <K, V extends Number> int[] getIntValues(Map<K, V> map, K... keys) {
		int[] values = null;
		if (null != keys && !map.isEmpty()) {
			int length = keys.length;
			values = new int[length];
			for (int idx = 0; idx < length; idx++) {
				values[idx] = map.get(keys[idx]).intValue();
			}
		}
		return values;
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
		Map<String, Integer> treeMap = toObject2IndexTreeMap(sNames);
		Collection<Integer> sIndexes = treeMap.values();
		System.out.println(Arrays.toString(CollectionUtil.toIntArray(sIndexes)));

		// java.lang.ClassCastException: [I cannot be cast to java.lang.Comparable
		// System.out.println(toObject2IndexTreeMap(new Object[] { new int[] { 2 } }));
	}

	public static void test2() {
		String[] ff = new String[10000];
		for (int i = 0; i < ff.length; i++) {
			ff[i] = UUID.randomUUID().toString();
		}
		Object[] names = { "t", "t2", ff };
		int length = names.length, fieldsIndex = 0;
		String[] fieldNames = null;
		for (int idx = 0; idx < length; idx++) {
			if (names[idx] instanceof String[]) {
				fieldsIndex = idx;
				fieldNames = (String[]) names[idx];
				break;
			}
		}
		if (null != fieldNames) {
			// // calling once makes the class loaded
			// IndexObject.wrapAsIndexObjectArray(1);
			// TreeMap<String, Integer> mm = new TreeMap<>();

			int fieldCount = fieldNames.length;
			final int nTestLoop = 1000;
			//
			long startNanos = System.nanoTime();
			for (int n = nTestLoop; --n >= 0;) {
				@SuppressWarnings("unchecked")
				IndexObject<String>[] indexNames = new IndexObject[fieldCount];
				for (int idx = 0; idx < fieldCount; idx++) {
					indexNames[idx] = new IndexObject<String>(fieldsIndex + idx, fieldNames[idx]);
				}
				Arrays.sort(indexNames);
				int[] sortedFieldNameIndex = IndexObject.getIndexArray(indexNames);
				// System.out.println(Arrays.toString(sortedFieldNameIndex));
			}
			long endNanos = System.nanoTime();

			//
			long startNanos2 = System.nanoTime();
			for (int n = nTestLoop; --n >= 0;) {
				TreeMap<String, Integer> treeMap = new TreeMap<String, Integer>();
				for (int idx = 0; idx < fieldCount; idx++) {
					treeMap.put(fieldNames[idx], fieldsIndex + idx);
				}
				int[] sortedFieldNameIndex = new int[treeMap.size()];
				Iterator<Integer> it = treeMap.values().iterator();
				for (int i = 0; it.hasNext(); i++) {
					sortedFieldNameIndex[i] = it.next();
				}
				// System.out.println(Arrays.toString(sortedFieldNameIndex));
			}
			long endNanos2 = System.nanoTime();

			// esplased: 4.388037205 s(4388037205 ns)
			// esplased: 4.206914955 s(4206914955 ns)

			long esplasedNanos = endNanos - startNanos;
			System.out.println("esplased: " + esplasedNanos / (1000 * 1000 * 1000d) + " s(" + esplasedNanos
					+ " ns)");
			long esplasedNanos2 = endNanos2 - startNanos2;
			System.out.println("esplased: " + esplasedNanos2 / (1000 * 1000 * 1000d) + " s(" + esplasedNanos2
					+ " ns)");
		}
	}
}