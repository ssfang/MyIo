package org.misc;

import java.util.UUID;

public class TimeUID implements java.io.Serializable, Comparable<TimeUID> {
	private static final long serialVersionUID = -127634000582442290L;

	private static int sequenceCounter;
	private static long lastTime = Long.MIN_VALUE;// compare

	public long time;
	public int seq;

	public TimeUID(long time, int seq) {
		this.time = time;
		this.seq = seq;
	}

	public static synchronized int next(long currentTime) {
		if (currentTime > lastTime) { // compare
			lastTime = currentTime;
			sequenceCounter = 1;
		} else {
			sequenceCounter++;
		}
		return sequenceCounter;
	}

	public static TimeUID next() {
		long currentTime = System.currentTimeMillis();
		return new TimeUID(currentTime, next(currentTime));
	}

	@Override
	public int compareTo(TimeUID another) {
		return (this.time < another.time ? -1 : (this.time > another.time ? 1 : (this.seq < another.seq ? -1
				: (this.seq > another.seq ? 1 : 0))));
	}

	@Override
	public int hashCode() {
		long hilo = time ^ seq;
		return ((int) (hilo >> 32)) ^ (int) hilo;
	}

	@Override
	public boolean equals(Object obj) {
		if ((null == obj) || (obj.getClass() != UUID.class))
			return false;
		TimeUID utime = (TimeUID) obj;
		return (time == utime.time && seq == utime.seq);
	}

	@Override
	public String toString() {
		return "UTime [time=" + time + ", seq=" + seq + "]";
	}

	/**
	 * Compares two {@code long} values numerically. The value returned is
	 * identical to what would be returned by:
	 * 
	 * <pre>
	 * Long.valueOf(x).compareTo(Long.valueOf(y))
	 * </pre>
	 * 
	 * @param x the first {@code long} to compare
	 * @param y the second {@code long} to compare
	 * @return the value {@code 0} if {@code x == y}; a value less than
	 *         {@code 0} if {@code x < y}; and a value greater than {@code 0} if
	 *         {@code x > y}
	 * @since 1.7
	 */
	public static int compare(long x, long y) {
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}

	/**
	 * Compares two {@code long} values numerically treating the values as
	 * unsigned.
	 * 
	 * @param x the first {@code long} to compare
	 * @param y the second {@code long} to compare
	 * @return the value {@code 0} if {@code x == y}; a value less than
	 *         {@code 0} if {@code x < y} as unsigned values; and a value
	 *         greater than {@code 0} if {@code x > y} as unsigned values
	 * @since 1.8
	 */
	public static int compareUnsigned(long x, long y) {
		return compare(x + Long.MIN_VALUE, y + Long.MIN_VALUE);
	}

	public static void test() throws Exception {
		long nanoseconds = System.nanoTime();
		for (int i = 0; i < 100; i++) {
			// in general, seq is not too high and is double-digit.
			System.out.println(TimeUID.next());
		}
		long nanoseconds2 = System.nanoTime();
		long esplased = nanoseconds2 - nanoseconds;
		System.out.println("esplased " + esplased / (1000 * 1000d) + " ms(" + esplased + " ns)");
	}
}
