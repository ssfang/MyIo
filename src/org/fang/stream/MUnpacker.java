package org.fang.stream;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class MUnpacker<T extends InputStream> extends FilterInputStream {
	private final static int NEXT_DIRTY = -1;
	private final static int overflowU8 = 8;// 255
	private final static int overflowU16 = 16;// 65535
	private final static int overflowU32 = 32;
	private final static int overflowU64 = 64;

	private int nextByte = NEXT_DIRTY;

	/** 对于已知不超过10字节大小数据写入的缓冲区，如整数，布尔值串等，以便一次写入内部流 */
	ByteBuffer byteBuffer;
	byte[] buf = new byte[8];

	public MUnpacker(T in) {
		super(in);
		byteBuffer = ByteBuffer.wrap(new byte[8]);
	}

	/**
	 * See the general contract of the <code>readByte</code> method of <code>DataInput</code>.
	 * <p>
	 * Bytes for this operation are read from the contained input stream.
	 * 
	 * @return the next byte of this input stream as a signed 8-bit <code>byte</code>.
	 * @exception EOFException
	 *              if this input stream has reached the end.
	 * @exception IOException
	 *              the stream has been closed and the contained input stream does not support reading after close, or another I/O
	 *              error occurs.
	 * @see java.io.FilterInputStream#in
	 */
	private final byte readByte() throws IOException {
		int ch = in.read();
		if (ch < 0)
			throw new EOFException();
		return (byte) (ch);
	}

	/**
	 * See the general contract of the <code>readUnsignedByte</code> method of <code>DataInput</code>.
	 * <p>
	 * Bytes for this operation are read from the contained input stream. , which is therefore in the range 0 through 255.
	 * 
	 * @return the next byte of this input stream, interpreted as an unsigned 8-bit number.
	 * @exception EOFException
	 *              if this input stream has reached the end.
	 * @exception IOException
	 *              the stream has been closed and the contained input stream does not support reading after close, or another I/O
	 *              error occurs.
	 * @see java.io.FilterInputStream#in
	 */
	public byte getNextCode() throws IOException {
		if (NEXT_DIRTY == nextByte) {
			nextByte = in.read();
			if (nextByte < 0)
				throw new EOFException();
		}
		// return Codes[nextByte] & 0xff;
		return (byte) (nextByte);
	}

	// public int getNextValueType() {
	// return (byte) (0xff00 & nextByte);
	// }

	// public int getNextFormat() throws IOException {
	// if (NEXT_DIRTY == nextByte) {
	// nextByte = readByte();
	// }
	// return nextByte;
	// }

	public long unpackLong() throws IOException {
		byte byte0 = getNextCode();
		if (MPackCode.isFixInt(byte0))
			return byte0;
		switch (byte0) {
		case MPackCode.UINT8:
			return readByte() & 255;
		case MPackCode.UINT16:
			return readShort() & 65535;
		case MPackCode.UINT32:
			int i = readInt();
			if (i < 0)
				return (i & 2147483647) + 2147483648L;
			else
				return i;
		case MPackCode.UINT64:
			long l = readLong();
			if (l < 0L)
				throw overflow(l, overflowU64);
			else
				return l;
		case MPackCode.INT8:
			return readByte();
		case MPackCode.INT16:
			return readShort();
		case MPackCode.INT32:
			return readInt();
		case MPackCode.INT64:
			return readLong();
		}
		throw unexpected("Integer", byte0);
	}

	public int unpackInt(int nBytes) throws IOException {
		byte byte0 = getNextCode();
		if (MPackCode.isFixInt(byte0))
			return byte0;
		switch (byte0) {
		case MPackCode.UINT8:
			byte byte1 = readByte();
			return byte1 & 255;

		case MPackCode.UINT16:
			short word0 = readShort();
			return word0 & 65535;

		case MPackCode.UINT32:
			int i = readInt();
			if (i < 0)
				throw overflow(i, overflowU32);
			else
				return i;

		case MPackCode.UINT64:
			long l = readLong();
			if (l < 0L || l > 2147483647L)
				throw overflow(l, overflowU64);
			else
				return (int) l;
		case MPackCode.INT8:
			return readByte();
		case MPackCode.INT16:
			return readShort();
		case MPackCode.INT32:
			return readInt();
		case MPackCode.INT64:
			long l1 = readLong();
			if (l1 < -2147483648L || l1 > 2147483647L)
				throw overflow(l1);
			else
				return (int) l1;
		}
		throw unexpected("Integer", byte0);
	}

	// 解包为Byte，Short时判断溢出和截断，没有太大意义，应该由调用者在想获取Byte数值时自行考虑截断还是溢出

	public byte unpackByte() throws IOException {
		byte byte0 = getNextCode();
		if (MPackCode.isFixInt(byte0))
			return byte0;
		switch (byte0) {
		case MPackCode.UINT8:
			byte byte1 = readByte();
			if (byte1 < 0)
				throw overflow(byte1, overflowU8);
			else
				return byte1;

		case MPackCode.UINT16:
			short word0 = readShort();
			if (word0 < 0 || word0 > 127)
				throw overflow(word0, overflowU16);
			else
				return (byte) word0;

		case MPackCode.UINT32:
			int i = readInt();
			if (i < 0 || i > 127)
				throw overflow(i, overflowU32);
			else
				return (byte) i;

		case MPackCode.UINT64:
			long l = readLong();
			if (l < 0L || l > 127L)
				throw overflow(l, overflowU64);
			else
				return (byte) (int) l;

		case MPackCode.INT8:
			byte byte2 = readByte();
			return byte2;

		case MPackCode.INT16:
			short word1 = readShort();
			if (word1 < -128 || word1 > 127)
				throw overflow(word1);
			else
				return (byte) word1;

		case MPackCode.INT32:
			i = readInt();
			if (i < -128 || i > 127)
				throw overflow(i);
			else
				return (byte) i;

		case MPackCode.INT64:
			l = readLong();
			if (l < -128L || l > 127L)
				throw overflow(l);
			else
				return (byte) (int) l;
		}
		throw unexpected("Integer", byte0);
	}

	private ByteBuffer consume(int nBytes) throws IOException {
		int nReadedBytes = in.read(byteBuffer.array(), 0, nBytes);
		if (nReadedBytes < nBytes) {
			throw new EOFException("insufficient data length for reading the value of " + nBytes);
		}
		return byteBuffer;
	}

	private long readLong() throws IOException {
		return consume(8).getLong(0);
	}

	private int readInt() throws IOException {
		return consume(4).getInt(0);
	}

	private short readShort() throws IOException {
		return consume(2).getShort(0);
	}

	private static RuntimeException unexpected(String s, byte byte0) throws RuntimeException {
		String s1;
		if (byte0 == NEXT_DIRTY) {
			s1 = "NeverUsed";
		} else {
			String s2 = "";
			s1 = (new StringBuilder()).append(s2.substring(0, 1)).append(s2.substring(1).toLowerCase()).toString();
		}
		return new RuntimeException(String.format("Expected %s, but got %s (%02x)", new Object[] { s, s1, Byte.valueOf(byte0) }));
	}

	private static RuntimeException overflow(long number, int bit) {
		BigInteger biginteger = BigInteger.valueOf(number).setBit(bit - 1);
		return new RuntimeException(biginteger.toString());
	}

	private static RuntimeException overflow(long signedNumber) {
		BigInteger biginteger = BigInteger.valueOf(signedNumber);
		return new RuntimeException(biginteger.toString());
	}
}
