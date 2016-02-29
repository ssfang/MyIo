package org.misc.steam;

import java.io.IOException;
import java.util.Arrays;

public class SteamUtil {

	/**
	 * Copy some or all bytes from a large (over 2GB) <code>InputStreamI</code>
	 * to an <code>OutputStreamI</code>.
	 * <p>
	 * This method uses the provided buffer, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 * 
	 * @param input the <code>InputStreamI</code> to read from
	 * @param output the <code>OutputStreamI</code> to write to
	 * @param length : number of bytes to copy. -1 means all
	 * @param buffer the buffer to use for the copy
	 * 
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException if an I/O error occurs
	 */
	public static long copyLarge(InputStreamI input, OutputStreamI output, final long length, byte[] buffer)
			throws IOException {
		if (length == 0) {
			return 0;
		}
		final int bufferLength = buffer.length;
		int bytesToRead = bufferLength;
		if (length > 0 && length < bufferLength) {
			bytesToRead = (int) length;
		}
		int readedByteCount;
		long totalRead = 0;
		while (bytesToRead > 0 && -1 != (readedByteCount = input.read(buffer, 0, bytesToRead))) {
			output.write(buffer, 0, readedByteCount);
			totalRead += readedByteCount;
			if (length > 0) { // only adjust length if not reading to the end
				// Note the cast must work because buffer.length is an integer
				bytesToRead = (int) Math.min(length - totalRead, bufferLength);
			}
		}
		return totalRead;
	}

	public static void test(String[] args) throws IOException {
		byte[] blob = new byte[10];
		Arrays.fill(blob, (byte) 90);
		org.misc.steam.ByteArrayInputStream in = new org.misc.steam.ByteArrayInputStream(blob);
		org.misc.steam.ByteArrayOutputStream outbuf = new org.misc.steam.ByteArrayOutputStream();

		System.out.println("readFrom = " + outbuf.readFrom(in, in.available()) + ": " + outbuf);
		System.out.println("writeTo = " + in.writeTo(outbuf, in.available()) + ": " + outbuf);
	}

}
