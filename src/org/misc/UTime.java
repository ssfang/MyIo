package org.misc;

public class UTime {
	private static volatile long lastUTime;

	public static long next() {
		long currentTime = System.currentTimeMillis();
		if (currentTime > lastUTime) {
			lastUTime = currentTime;
			return currentTime;
		} else if (currentTime == lastUTime) {
			currentTime++;
			lastUTime = currentTime;
			return currentTime;
		} else {
			long delta = lastUTime - currentTime;
			if (delta < 1000) {
				return lastUTime++;
			} else {
				long last = lastUTime;
				while (delta > 0) {
					try {
						Thread.sleep(delta);
					} catch (InterruptedException e) {
						//
					}
					currentTime = System.currentTimeMillis();
					delta = last - currentTime;
				}
				if (0 == delta) {
					currentTime++;
				}
				lastUTime = currentTime;
				return currentTime;
			}
		}
	}

	public static void test() throws Exception {
		long nanoseconds = System.nanoTime();
		for (int i = 0; i < 100; i++) {
			// in general, seq is not too high and is double-digit.
			System.out.println(UTime.next());
		}
		long nanoseconds2 = System.nanoTime();
		long esplased = nanoseconds2 - nanoseconds;
		System.out.println("esplased " + esplased / (1000 * 1000d) + " ms(" + esplased + " ns)");
	}
}
