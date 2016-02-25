package org.msgpack;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class MPack {
	private static final int EOF = -1;

	public static MPacker newMPacker() {
		return new MPacker(new BytesOutputStream());
	}

	public static MPacker newMPacker(int size) {
		return new MPacker(new BytesOutputStream(size));
	}

	/**
	 * Copy some or all bytes from a large (over 2GB) <code>InputStream</code>
	 * to an <code>OutputStream</code>.
	 * <p>
	 * This method uses the provided buffer, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 * 
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
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
	 * Converts the argument to a {@code long} by an unsigned conversion. In an
	 * unsigned conversion to a {@code long}, the high-order 32 bits of the
	 * {@code long} are zero and the low-order 32 bits are equal to the bits of
	 * the integer argument.
	 * 
	 * Consequently, zero and positive {@code int} values are mapped to a
	 * numerically equal {@code long} value and negative {@code int} values are
	 * mapped to a {@code long} value equal to the input plus 2<sup>32</sup>.
	 * 
	 * <pre>
	 * <code>
	 * int x = Integer.MAX_VALUE + 1; // 2147483647 [0x7fffffff]
	 * //-2147483648, 2147483648, -2147483648
	 * System.out.println(x + ", " + Integer.toUnsignedLong(x) + ", " + ((long) x));
	 * </code>
	 * </pre>
	 * 
	 * @param javaInt the value to convert to an unsigned {@code long}
	 * @return the argument converted to {@code long} by an unsigned conversion
	 * @since 1.8
	 * @see {@link Integer#toUnsignedLong(int)}
	 */
	public static long toUnsignedLong(int javaInt) {
		return 0xffffffffL & javaInt;// ((long) javaInt) & 0xffffffffL;
	}

	static final char[] hexDigits = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
			'C', 'D', 'E', 'F' };

	/**
	 * 如果入参为null或长度为0则返回null，否则返回十六进制字符串
	 * 
	 * @param bytes
	 * @return Return null, if the range described by {@code offset} and
	 *         {@code count} doesn't exceed {@code bytes.length}.
	 */
	public static String bytes2HexStr(byte[] bytes, int offset, int count) {
		if ((offset | count) < 0 || offset > bytes.length || bytes.length - offset < count) {
			return null;
		}
		char[] tmp = new char[2 * count];
		for (int idx = offset; idx < count; idx++) {
			int onebyte = bytes[idx];
			tmp[idx * 2 + 1] = hexDigits[onebyte & 0xF];
			tmp[idx * 2] = hexDigits[(onebyte >>> 4) & 0xF];
		}
		return new String(tmp);
	}

	public static void printMessagePackFileContent(String filepath) {

		MUnpacker unpacker = null;
		try {
			// int availableBytes = fileInput.available();
			unpacker = new MUnpacker(new BufferedInputStream(new FileInputStream(filepath))) {
				@Override
				protected String binaryToString(byte[] binary) {
					int len = binary.length;
					if (len > 32) {
						len = 32;
					}
					// just handle partial binary not more than 32
					String str = bytes2HexStr(binary, 0, len);
					return len > 32 ? Integer.toHexString(len) + ':' + str : str;
				}
			};

			unpacker.setOptions(MUnpacker.OPT_READ_ARRAY_AS_LIST | MUnpacker.OPT_READ_BIN_AS_STR);
			while (unpacker.hasNext()) {
				Object record = unpacker.unpack();
				System.out.println(record);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != unpacker) {
				try {
					unpacker.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void test() throws Exception {

		// pack test

		BytesOutputStream outputBuffer = new BytesOutputStream();
		MPacker packer = new MPacker(outputBuffer);
		// MessagePack values as follow:
		// [fang, {22="22value", 2.200000047683716=true}]
		// "second value"
		packer.packArrayHeader(2);
		packer.pack("fang");
		packer.packMapHeader(2);
		packer.pack(22).pack("22value");
		packer.packFloat(2.2f).packBoolean(true);
		packer.packString("second value");
		packer.pack("byte array".getBytes());
		packer.close();

		// unpack test after packing.

		ByteArrayInputStream input = outputBuffer.newInputStream();
		MUnpacker unpacker = newUnpacker(input);
		System.out.println(unpacker.unpack());
		System.out.println(unpacker.unpack());
		System.out.println(unpacker.unpack());

		// readValue test

		input.reset();// reset the read cursor.
		BytesOutputStream copiedRawValue = new BytesOutputStream();
		unpacker.readValue(3, copiedRawValue);
		System.out.println("copiedRawValue = " + copiedRawValue);

		unpacker = newUnpacker(copiedRawValue.newInputStream());
		System.out.println("unpack copiedRawValue = " + unpacker.unpack());
		System.out.println("unpack copiedRawValue = " + unpacker.unpack());
		System.out.println("unpack copiedRawValue = " + unpacker.unpack());
		if (!copiedRawValue.asByteBuffer().equals(outputBuffer.asByteBuffer())) {
			System.out.println(Arrays.toString(outputBuffer.buffer()));
			System.out.println(Arrays.toString(copiedRawValue.buffer()));
		}
	}

	private static MUnpacker newUnpacker(InputStream input) {
		MUnpacker unpacker = new MUnpacker(input);
		unpacker.setOptions(MUnpacker.OPT_READ_ARRAY_AS_LIST | MUnpacker.OPT_READ_BIN_AS_STR).setAsMapClass(
				LinkedHashMap.class);
		return unpacker;
	}
}
