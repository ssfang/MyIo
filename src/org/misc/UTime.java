package org.misc;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class UTime {
	private final static long ALLOWED_MILLIS = 1000;
	private static long lastUTime;

	public static long next() {
		return next(ALLOWED_MILLIS);
	}

	/**
	 * Get the current time in milliseconds that must be great than last time, so it may causes the currently
	 * executing thread to sleep until the difference, measured in milliseconds, between the current time and
	 * last time, is less than the allowed milliseconds.
	 * 
	 * <pre>
	 * case 1: -------|-------|-----------------+--               return current;
	 *               last  current      current + ALLOWED_MILLIS
	 *       
	 * case 2: ---------------|-----------------+--               return current + 1;
	 *                     current      current + ALLOWED_MILLIS
	 *                     last
	 *             
	 * case 3: ---------------|------|----------+--               return last + 1;
	 *                     current last current + ALLOWED_MILLIS
	 *             
	 * case 4: ---------------|-----------------+|--              wait until current = last;
	 *                      current             last
	 *                                  current + ALLOWED_MILLIS
	 * </pre>
	 * 
	 * @param allowedMillis a value larger than its granularity is recommended, referring to
	 *            {@link System#currentTimeMillis()},
	 * @return the current time in milliseconds, which is the difference, measured in milliseconds, between
	 *         the current time and midnight, January 1, 1970 UTC.
	 * @see System#currentTimeMillis()
	 */
	public static synchronized long next(long allowedMillis) {
		long currentTime = System.currentTimeMillis();
		// long oldUTime = lastUTime;
		if (currentTime > lastUTime) { // case 1
			lastUTime = currentTime;
		} else if (currentTime == lastUTime) { // case 2
			currentTime++;
			lastUTime = currentTime;
		} else {
			long delta = lastUTime - currentTime;
			if (delta < allowedMillis) { // case 3
				currentTime = ++lastUTime;
			} else { // case 4
				long last = lastUTime + 1; // the upper value, included
				do {
					try {
						Thread.sleep(delta);
					} catch (InterruptedException ignored) {
						// ignored
					}
					currentTime = System.currentTimeMillis();
					delta = last - currentTime;
				} while (delta > 0); // until delta < allowedMillis, 0 is OK
				lastUTime = currentTime;
			}
		}
		// if (currentTime <= oldUTime) {
		// throw new
		// Error("it's an error for 'gotten currentTime <= oldUTime'");
		// }
		return currentTime;
	}

	static class UTimeTest implements Runnable {
		private final static int loop = 5000;
		private static AtomicLong lastUniqueTime = new AtomicLong();
		/**
		 * it is a reference for the parameters <code>allowedMillis</code> of {@link UTime#next(long)}
		 */
		private final ArrayList<Long> timeDifferenceList = new ArrayList<Long>(loop);

		private long startMillis, endMillis, elapsedNanos;

		@Override
		public void run() {
			String threadName = Thread.currentThread().getName();

			long nowUTime;
			startMillis = System.currentTimeMillis();
			long startNanos = System.nanoTime();
			long lastTime = startMillis, currentTime;

			for (int i = 0; i < loop; i++) {
				synchronized (lastUniqueTime) {
					nowUTime = UTime.next();
					if (nowUTime > lastUniqueTime.get()) {
						lastUniqueTime.set(nowUTime);
						// System.out.println(threadName + ", " + nowUTime);
					} else {
						System.out.println(threadName + ", nowUTime = " + nowUTime + ", lastUniqueTime = "
								+ lastUniqueTime);
						throw new Error("nowUTime <= oldUTime is an error");
					}
				}
				currentTime = System.currentTimeMillis();
				if (lastTime < currentTime) {
					timeDifferenceList.add(currentTime - lastTime);
				}
				lastTime = currentTime;
			}

			endMillis = System.currentTimeMillis();
			long endNanos = System.nanoTime();
			elapsedNanos = endNanos - startNanos;
		}

		void printResult(String label) {
			long esplasedMillis = endMillis - startMillis;

			System.out.println(label + ", esplased: " + endMillis + " - " + startMillis + " = "
					+ esplasedMillis + " ms");
			System.out.println(label + ", esplased: " + elapsedNanos / (1000 * 1000 * 1000d) + " s("
					+ elapsedNanos + " ns)");

			System.out.println(label + ", timeDifferenceList = " + timeDifferenceList);
		}

		public static void test() throws Exception {
			UTimeTest test1 = new UTimeTest();
			UTimeTest test2 = new UTimeTest();

			// first thread

			Thread athread = new Thread(test1);
			athread.start();

			// second thread, that is the main thread
			Thread.sleep(22);
			test2.run();

			// wait for all.
			athread.join();

			test1.printResult("test1");
			test2.printResult("test2");
		}
	}
}
