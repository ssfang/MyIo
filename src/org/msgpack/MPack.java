package org.msgpack;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MPack {
	private static final int EOF = -1;

	public static MPacker newMPacker() {
		return new MPacker(new BytesOutputStream());
	}

	public static MPacker newMPacker(int size) {
		return new MPacker(new BytesOutputStream(size));
	}

	public static MPacker newBufferedMPacker(OutputStream out) {
		return new MPacker(new BufferedOutputStream(out));
	}

	/**
	 * Copy some or all bytes from a large (over 2GB) <code>InputStream</code> to an
	 * <code>OutputStream</code>.
	 * <p>
	 * This method uses the provided buffer, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 * 
	 * @param input  the <code>InputStream</code> to read from
	 * @param output  the <code>OutputStream</code> to write to
	 * @param length : number of bytes to copy. -1 means all
	 * @param buffer the buffer to use for the copy
	 *
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException if an I/O error occurs
	 */
	public static long copyLarge(InputStream input, OutputStream output, final long length, byte[] buffer)
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
		while (bytesToRead > 0 && EOF != (readedByteCount = input.read(buffer, 0, bytesToRead))) {
			output.write(buffer, 0, readedByteCount);
			totalRead += readedByteCount;
			if (length > 0) { // only adjust length if not reading to the end
				// Note the cast must work because buffer.length is an integer
				bytesToRead = (int) Math.min(length - totalRead, bufferLength);
			}
		}
		return totalRead;
	}

	/**
	 * java中有符号的int无法表示大于{@link Integer#MAX_VALUE}的正整数，我们需要转为java长度为8字节long来表示
	 * 
	 * Converts the argument to a {@code long} by an unsigned conversion. In an unsigned conversion to a {@code long}, the
	 * high-order 32 bits of the {@code long} are zero and the low-order 32 bits are equal to the bits of the integer argument.
	 * 
	 * Consequently, zero and positive {@code int} values are mapped to a numerically equal {@code long} value and negative
	 * {@code int} values are mapped to a {@code long} value equal to the input plus 2<sup>32</sup>.
	 * 
	 * @param javaInt
	 *          the value to convert to an unsigned {@code long}
	 * @return the argument converted to {@code long} by an unsigned conversion
	 * @since 1.8
	 * @see {@link Integer#toUnsignedLong(int)}
	 */
	public static long toUnsignedLong(int javaInt) {
		return 0xffffffffL & javaInt;// ((long) javaInt) & 0xffffffffL;
	}
}
