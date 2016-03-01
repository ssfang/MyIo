package org.misc;

import java.util.Collection;
import java.util.Iterator;

public class CollectionUtil {

	/**
	 * <p>
	 * Converts an array of object Integer to primitives handling <code>null</code>.
	 * </p>
	 * <p>
	 * This method returns <code>null</code> for a <code>null</code> input array.
	 * </p>
	 * 
	 * @param list a Number collection, may be <code>null</code>
	 * @return an <code>int</code> array, <code>null</code> if null array input
	 * @throws NullPointerException if the input parameter <code>list</code> contains null.
	 */
	public static <E extends Number> int[] toIntArray(Collection<E> list) {
		int[] primitiveIntArray = null;
		if (null != list) {
			int idx = 0;
			primitiveIntArray = new int[list.size()];
			for (Iterator<E> it = list.iterator(); it.hasNext();)
				primitiveIntArray[idx++] = it.next().intValue();
		}
		return primitiveIntArray;
	}

}
