package org.msgpack;

import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.security.DigestOutputStream;

/**
 * 只是对写入（pack开头的方法）的数据进行中间编码控制，类似于{@link DigestOutputStream}；<br>
 * 通过本类的方法向我写入的东西毫无保留的全部流入内部流，即我的写指针和底层流一致。
 * <p/>
 * 写入分两种，一种数据字节已知（类型+值），空，布尔值，整数以及浮点数等；<br>
 * 另一种，类型（有的包括长度）+长度+负载，也许负载需要多次写入（先头再负载）。
 * <ul>
 * <li>nil format, bool format family, int format family, float format family</li>
 * <li>str format family, bin format family,array format family, map format
 * family,ext format family</li>
 * </ul>
 * 各种格式类型数据，要么一次写入内部流（pack类型方法），要么头部和负载分开写入内部流（packXXHeader的方法+write方法）。
 * <p/>
 * <b>Notation in diagrams</b>
 * 
 * <pre>
 * one byte:
 * +--------+
 * |        |
 * +--------+
 * 
 * a variable number of bytes:
 * +========+
 * |        |
 * +========+
 * 
 * variable number of objects stored in MessagePack format:
 * +~~~~~~~~~~~~~~~~~+
 * |                 |
 * +~~~~~~~~~~~~~~~~~+
 * </pre>
 * 
 * @author fangss
 * 
 * @param
 * @see <a
 *      href="https://github.com/msgpack/msgpack/blob/master/spec.md">MessagePack
 *      specification</a>
 * @see <a href="https://github.com/msgpack/msgpack-java">msgpack-java</a>
 */
public class MPacker extends FilterOutputStream {

	/**
	 * 对于已知不超过10字节大小数据写入的缓冲区，如整数，布尔值串等，以便一次写入内部流
	 * 
	 * @see #write(ByteBuffer)
	 * */
	protected ByteBuffer byteBuffer;
	/** 统计已经编码的顶级值数量 */
	protected int valueCount;
	/** 初始和最后打包完，应该为0，否则嵌套错误 */
	protected int childCount;
	protected int binarySize;
	/**
	 * String encoder
	 */
	protected CharsetEncoder charsetEncoder;

	public MPacker(OutputStream out) {
		super(out);
		byteBuffer = ByteBuffer.allocate(10);
	}

	public OutputStream getOutput() {
		return out;
	}

	public CharsetEncoder getCharsetEncoder() {
		if (charsetEncoder == null) {
			this.charsetEncoder = Charset.forName("UTF-8").newEncoder();
			byteBuffer = ByteBuffer.allocate(8 * 1024);
		}
		return charsetEncoder;
	}

	public void write(ByteBuffer byteBuffer) throws IOException {
		write(byteBuffer.array(), 0, byteBuffer.limit());
	}

	/**
	 * <h1>what can a single byte represent?</h1>
	 * <ul>
	 * <li>
	 * <b>nil format</b>:
	 * 
	 * <pre>
	 *       +--------+
	 * null= |  0xc0  |
	 *       +--------+
	 * </pre>
	 * 
	 * </li>
	 * <li><b>bool format family</b>:
	 * 
	 * <pre>
	 * 
	 *        +--------+        +--------+
	 * false= |  0xc2  |  true= |  0xc3  |
	 *        +--------+        +--------+
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * <b>int format family(partial for fixnum)</b>: -32 ~ -1 and 0 ~ 127
	 * 
	 * <pre>
	 * positive fixnum stores 7-bit positive integer
	 * +--------+
	 * |0XXXXXXX|    
	 * +--------+
	 * 
	 * negative fixnum stores 5-bit negative integer
	 * +--------+
	 * |111YYYYY|
	 * +--------+
	 * 
	 * 0XXXXXXX is 8-bit unsigned integer, 0x00 ~ 0x7f(0 ~ 127)
	 * 111YYYYY is 8-bit signed integer, 0xe0 ~ 0xff(-32 ~ -1)
	 * </pre>
	 * 
	 * </li>
	 * <li><b>Other fix type header</b>: one byte = code_for_fix_xx +
	 * length_of_fix_xx</li>
	 * </ul>
	 * 
	 * @param b
	 * @throws IOException
	 */
	public void writeByte(byte b) throws IOException {
		write(b);
	}

	// Number family: code+value; String, Binary, Array, Map family: code+length

	private void writeByteAndByte(byte b, byte v) throws IOException {
		byteBuffer.limit(2);
		write(byteBuffer.put(0, b).put(1, v));
	}

	private void writeByteAndShort(byte b, short v) throws IOException {
		byteBuffer.limit(3);
		write(byteBuffer.put(0, b).putShort(1, v));
	}

	private void writeByteAndInt(byte b, int v) throws IOException {
		byteBuffer.limit(5);
		write(byteBuffer.put(0, b).putInt(1, v));
	}

	private void writeByteAndFloat(byte b, float v) throws IOException {
		byteBuffer.limit(5);
		write(byteBuffer.put(0, b).putFloat(1, v));
	}

	private void writeByteAndDouble(byte b, double v) throws IOException {
		byteBuffer.limit(9);
		write(byteBuffer.put(0, b).putDouble(1, v));
	}

	private void writeByteAndLong(byte b, long v) throws IOException {
		byteBuffer.limit(9);
		write(byteBuffer.put(0, b).putLong(1, v));
	}

	// ext format family: code_of_ext + your_ext_data_length + your_ext_type

	private void writeByteIntegerByte(byte ext, int payloadLen, byte extType) throws IOException {
		byteBuffer.put(0, ext).position(1);
		if (ByteCode.EXT8 == ext) {
			byteBuffer.put((byte) payloadLen);
		} else if (ByteCode.EXT16 == ext) {
			byteBuffer.putShort((short) payloadLen);
		} else if (ByteCode.EXT32 == ext) {
			byteBuffer.putInt(payloadLen);
		}
		write(byteBuffer.put(extType));
	}

	/** 写入Nil */
	public MPacker pack() throws IOException {
		return packNil();
	}

	/** 写入布尔值 */
	public MPacker pack(boolean b) throws IOException {
		return packBoolean(b);
	}

	/** 写入整数 */
	public MPacker pack(int i) throws IOException {
		return packInt(i);
	}

	/** 写入长整数 */
	public MPacker pack(long l) throws IOException {
		return packLong(l);
	}

	/** 写入字符串 */
	public MPacker pack(String s) throws IOException {
		return packString(s);
	}

	public MPacker pack(MPackValue value) throws IOException {
		value.writeTo(this);
		return this;
	}

	public MPacker packNil() throws IOException {
		writeByte(ByteCode.NIL);
		return this;
	}

	public MPacker packBoolean(boolean b) throws IOException {
		writeByte(b ? ByteCode.TRUE : ByteCode.FALSE);
		return this;
	}

	private int encodeStringToBufferAt(String s) {
		ByteBuffer bb = byteBuffer;
		int startPosition = bb.position();
		CharsetEncoder encoder = getCharsetEncoder();
		CharBuffer in = CharBuffer.wrap(s);
		CoderResult cr = encoder.encode(in, bb, true);
		if (cr.isError()) {
			try {
				cr.throwException();
			} catch (CharacterCodingException e) {
				throw new MPackFormatException("string coding", e);
			}
		}
		if (cr.isUnderflow() || cr.isOverflow()) {
			return -1;
		}
		return bb.position() - startPosition;
	}

	/**
	 * Pack the input String in UTF-8 encoding
	 * 
	 * @param s
	 * @return
	 * @throws IOException
	 */
	public MPacker packString(String s) throws IOException {
		if (s.length() > 0) {
			// TODO encoding error?
			// TODO test: JVM performs various optimizations (memory allocation,
			// reusing encoder etc.) when String.getBytes is used. So, is it
			// generally faster for small strings?
			byte[] bs = s.getBytes("UTF-8");
			packRawStringHeader(bs.length);
			write(bs, 0, bs.length);
			return this;
		}
		return packRawStringHeader(0);
	}

	/**
	 * Pack the input String in UTF-8 encoding, Allowing Of <code>null</code>
	 * 
	 * @param s
	 * @return
	 * @throws IOException
	 */
	public MPacker packOrNil(String s) throws IOException {
		if (null != s) {
			return packString(s);
		}
		writeByte(ByteCode.NIL);
		return this;
	}

	public MPacker packOrNil(byte[] byteArray) throws IOException {
		if (null != byteArray) {
			packBinaryHeader(byteArray.length);
			write(byteArray);
		} else {
			writeByte(ByteCode.NIL);
		}
		return this;
	}

	/**
	 * 打包如下类型：null - packNil, CharSequence - packString, Float - packFloat,
	 * Double - packDouble, BigInteger - packBigInteger, Number - packLong
	 * 
	 * @param obj
	 * @throws IOException
	 */
	public MPacker pack(Object obj) throws IOException {
		if (null == obj) {
			packNil();
		} else if (obj instanceof Float) {
			packFloat(((Float) obj).floatValue());
		} else if (obj instanceof Double) {
			packDouble(((Double) obj).doubleValue());
		} else if (obj instanceof BigInteger) {
			packBigInteger((BigInteger) obj);
		} else if (obj instanceof Number) {
			packLong(((Number) obj).longValue());
		} else if (obj instanceof byte[]) {
			packBinaryHeader(((byte[]) obj).length);
			write((byte[]) obj);
		} else if (obj.getClass().isArray()) {
			int arrayLength = Array.getLength(obj);
			packArrayHeader(arrayLength);
			for (int i = 0; i < arrayLength; i++) {
				pack(Array.get(obj, i));
			}
		} else { // if (obj instanceof CharSequence)
			packString(obj.toString());
		}
		return this;
	}

	public MPacker packByte(byte b) throws IOException {
		if (b < -(1 << 5)) {
			writeByteAndByte(ByteCode.INT8, b);
		} else {
			writeByte(b);
		}
		return this;
	}

	public MPacker packShort(short v) throws IOException {
		if (v < -(1 << 5)) {
			if (v < -(1 << 7)) {
				writeByteAndShort(ByteCode.INT16, v);
			} else {
				writeByteAndByte(ByteCode.INT8, (byte) v);
			}
		} else if (v < (1 << 7)) {
			writeByte((byte) v);
		} else {
			if (v < (1 << 8)) {
				writeByteAndByte(ByteCode.UINT8, (byte) v);
			} else {
				writeByteAndShort(ByteCode.UINT16, v);
			}
		}
		return this;
	}

	public MPacker packInt(int r) throws IOException {
		if (r < -(1 << 5)) {
			if (r < -(1 << 15)) {
				writeByteAndInt(ByteCode.INT32, r);
			} else if (r < -(1 << 7)) {
				writeByteAndShort(ByteCode.INT16, (short) r);
			} else {
				writeByteAndByte(ByteCode.INT8, (byte) r);
			}
		} else if (r < (1 << 7)) {
			writeByte((byte) r);
		} else {
			if (r < (1 << 8)) {
				writeByteAndByte(ByteCode.UINT8, (byte) r);
			} else if (r < (1 << 16)) {
				writeByteAndShort(ByteCode.UINT16, (short) r);
			} else {
				// unsigned 32
				writeByteAndInt(ByteCode.UINT32, r);
			}
		}
		return this;
	}

	public MPacker packLong(long v) throws IOException {
		if (v < -(1L << 5)) {
			if (v < -(1L << 15)) {
				if (v < -(1L << 31)) {
					writeByteAndLong(ByteCode.INT64, v);
				} else {
					writeByteAndInt(ByteCode.INT32, (int) v);
				}
			} else {
				if (v < -(1 << 7)) {
					writeByteAndShort(ByteCode.INT16, (short) v);
				} else {
					writeByteAndByte(ByteCode.INT8, (byte) v);
				}
			}
		} else if (v < (1 << 7)) {
			// fixnum
			writeByte((byte) v);
		} else {
			if (v < (1L << 16)) {
				if (v < (1 << 8)) {
					writeByteAndByte(ByteCode.UINT8, (byte) v);
				} else {
					writeByteAndShort(ByteCode.UINT16, (short) v);
				}
			} else {
				if (v < (1L << 32)) {
					writeByteAndInt(ByteCode.UINT32, (int) v);
				} else {
					writeByteAndLong(ByteCode.UINT64, v);
				}
			}
		}
		return this;
	}

	public MPacker packBigInteger(BigInteger bi) throws IOException {
		if (bi.bitLength() <= 63) {
			packLong(bi.longValue());
		} else if (bi.bitLength() == 64 && bi.signum() == 1) {
			writeByteAndLong(ByteCode.UINT64, bi.longValue());
		} else {
			throw new IllegalArgumentException("MessagePack cannot serialize BigInteger larger than 2^64-1");
		}
		return this;
	}

	public MPacker packFloat(float v) throws IOException {
		writeByteAndFloat(ByteCode.FLOAT32, v);
		return this;
	}

	public MPacker packDouble(double v) throws IOException {
		writeByteAndDouble(ByteCode.FLOAT64, v);
		return this;
	}

	/**
	 * <pre>
	 * Array format family stores a sequence of elements in 1, 3, or 5 bytes of extra bytes in addition to the elements.
	 * 
	 * 	fixarray stores an array whose length is upto 15 elements:
	 * 	+--------+~~~~~~~~~~~~~~~~~+
	 * 	|1001XXXX|    N objects    |
	 * 	+--------+~~~~~~~~~~~~~~~~~+
	 * 
	 * 	array 16 stores an array whose length is upto (2^16)-1 elements:
	 * 	+--------+--------+--------+~~~~~~~~~~~~~~~~~+
	 * 	|  0xdc  |YYYYYYYY|YYYYYYYY|    N objects    |
	 * 	+--------+--------+--------+~~~~~~~~~~~~~~~~~+
	 * 
	 * 	array 32 stores an array whose length is upto (2^32)-1 elements:
	 * 	+--------+--------+--------+--------+--------+~~~~~~~~~~~~~~~~~+
	 * 	|  0xdd  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|    N objects    |
	 * 	+--------+--------+--------+--------+--------+~~~~~~~~~~~~~~~~~+
	 * 
	 * 	where
	 * XXXX is a 4-bit unsigned integer which represents N
	 * YYYYYYYY_YYYYYYYY is a 16-bit big-endian unsigned integer which represents N
	 * ZZZZZZZZ_ZZZZZZZZ_ZZZZZZZZ_ZZZZZZZZ is a 32-bit big-endian unsigned integer which represents N
	 * 	    N is the size of a array
	 * </pre>
	 * 
	 * @param arraySize
	 * @return
	 * @throws IOException
	 */
	public MPacker packArrayHeader(int arraySize) throws IOException {
		if (arraySize < 0) {
			throw new IllegalArgumentException("array size must be >= 0");
		}

		if (arraySize < (1 << 4)) {
			writeByte((byte) (ByteCode.FIXARRAY_PREFIX | arraySize));
		} else if (arraySize < (1 << 16)) {
			writeByteAndShort(ByteCode.ARRAY16, (short) arraySize);
		} else {
			writeByteAndInt(ByteCode.ARRAY32, arraySize);
		}
		return this;
	}

	/**
	 * Map format family stores a sequence of key-value pairs in 1, 3, or 5
	 * bytes of extra bytes in addition to the key-value pairs.
	 * 
	 * <pre>
	 * fixmap stores a map whose length is upto 15 elements
	 * +--------+~~~~~~~~~~~~~~~~~+
	 * |1000XXXX|   N*2 objects   |
	 * +--------+~~~~~~~~~~~~~~~~~+
	 * 
	 * map 16 stores a map whose length is upto (2^16)-1 elements
	 * +--------+--------+--------+~~~~~~~~~~~~~~~~~+
	 * |  0xde  |YYYYYYYY|YYYYYYYY|   N*2 objects   |
	 * +--------+--------+--------+~~~~~~~~~~~~~~~~~+
	 * 
	 * map 32 stores a map whose length is upto (2^32)-1 elements
	 * +--------+--------+--------+--------+--------+~~~~~~~~~~~~~~~~~+
	 * |  0xdf  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|   N*2 objects   |
	 * +--------+--------+--------+--------+--------+~~~~~~~~~~~~~~~~~+
	 * 
	 * where
	 * XXXX is a 4-bit unsigned integer which represents N
	 * YYYYYYYY_YYYYYYYY is a 16-bit big-endian unsigned integer which represents N
	 * ZZZZZZZZ_ZZZZZZZZ_ZZZZZZZZ_ZZZZZZZZ is a 32-bit big-endian unsigned integer which represents N
	 * N is the size of a map
	 * odd elements in objects are keys of a map
	 * the next element of a key is its associated value
	 * </pre>
	 * 
	 * @param mapSize
	 * @return
	 * @throws IOException
	 */
	public MPacker packMapHeader(int mapSize) throws IOException {
		if (mapSize < 0) {
			throw new IllegalArgumentException("map size must be >= 0");
		}

		if (mapSize < (1 << 4)) {
			writeByte((byte) (ByteCode.FIXMAP_PREFIX | mapSize));
		} else if (mapSize < (1 << 16)) {
			writeByteAndShort(ByteCode.MAP16, (short) mapSize);
		} else {
			writeByteAndInt(ByteCode.MAP32, mapSize);
		}
		return this;
	}

	/**
	 * <h1>ext format family</h1>
	 * 
	 * <pre>
	 * Ext format family stores a tuple of an integer and a byte array.
	 * 
	 * fixext 1 stores an integer and a byte array whose length is 1 byte
	 * +--------+--------+--------+
	 * |  0xd4  |  type  |  data  |
	 * +--------+--------+--------+
	 * 
	 * fixext 2 stores an integer and a byte array whose length is 2 bytes
	 * +--------+--------+--------+--------+
	 * |  0xd5  |  type  |       data      |
	 * +--------+--------+--------+--------+
	 * 
	 * fixext 4 stores an integer and a byte array whose length is 4 bytes
	 * +--------+--------+--------+--------+--------+--------+
	 * |  0xd6  |  type  |                data               |
	 * +--------+--------+--------+--------+--------+--------+
	 * 
	 * fixext 8 stores an integer and a byte array whose length is 8 bytes
	 * +--------+--------+--------+--------+--------+--------+--------+--------+--------+--------+
	 * |  0xd7  |  type  |                                  data                                 |
	 * +--------+--------+--------+--------+--------+--------+--------+--------+--------+--------+
	 * 
	 * fixext 16 stores an integer and a byte array whose length is 16 bytes
	 * +--------+--------+--------+--------+--------+--------+--------+--------+--------+--------+
	 * |  0xd8  |  type  |                                  data                                  
	 * +--------+--------+--------+--------+--------+--------+--------+--------+--------+--------+
	 * +--------+--------+--------+--------+--------+--------+--------+--------+
	 *                               data (cont.)                              |
	 * +--------+--------+--------+--------+--------+--------+--------+--------+
	 * 
	 * ext 8 stores an integer and a byte array whose length is upto (2^8)-1 bytes:
	 * +--------+--------+--------+========+
	 * |  0xc7  |XXXXXXXX|  type  |  data  |
	 * +--------+--------+--------+========+
	 * 
	 * ext 16 stores an integer and a byte array whose length is upto (2^16)-1 bytes:
	 * +--------+--------+--------+--------+========+
	 * |  0xc8  |YYYYYYYY|YYYYYYYY|  type  |  data  |
	 * +--------+--------+--------+--------+========+
	 * 
	 * ext 32 stores an integer and a byte array whose length is upto (2^32)-1 bytes:
	 * +--------+--------+--------+--------+--------+--------+========+
	 * |  0xc9  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|  type  |  data  |
	 * +--------+--------+--------+--------+--------+--------+========+
	 * 
	 * where
	 * XXXXXXXX is a 8-bit unsigned integer which represents N
	 * YYYYYYYY_YYYYYYYY is a 16-bit big-endian unsigned integer which represents N
	 * ZZZZZZZZ_ZZZZZZZZ_ZZZZZZZZ_ZZZZZZZZ is a big-endian 32-bit unsigned integer which represents N
	 * N is a length of data
	 * type is a signed 8-bit signed integer
	 * type < 0 is reserved for future extension including 2-byte type information
	 * </pre>
	 * 
	 * @param extType
	 * @param payloadLen
	 * @return
	 * @throws IOException
	 * @see https://github.com/msgpack/msgpack/blob/master/spec.md#formats-ext
	 */
	public MPacker packExtensionTypeHeader(byte extType, int payloadLen) throws IOException {
		byte byteCode;
		int sizeOfNonFixExtPayloadLen;
		if (payloadLen < (1 << 8)) {
			if (payloadLen > 0 && (payloadLen & (payloadLen - 1)) == 0) { // check
																			// whether
																			// dataLen
																			// ==
																			// 2^x
				if (payloadLen == 1) {
					byteCode = ByteCode.FIXEXT1;
				} else if (payloadLen == 2) {
					byteCode = ByteCode.FIXEXT2;
				} else if (payloadLen == 4) {
					byteCode = ByteCode.FIXEXT4;
				} else if (payloadLen == 8) {
					byteCode = ByteCode.FIXEXT8;
				} else if (payloadLen == 16) {
					byteCode = ByteCode.FIXEXT16;
				} else {
					byteCode = ByteCode.EXT8;
				}
				if (ByteCode.EXT8 != byteCode) {
					writeByteAndByte(byteCode, extType);
					return this;
				}
			} else {
				byteCode = ByteCode.EXT8; // -57 = 0xc7
			}
			// ByteCode.EXT8
			byteBuffer.put(1, (byte) payloadLen);
			sizeOfNonFixExtPayloadLen = 1;
		} else if (payloadLen < (1 << 16)) {
			byteCode = ByteCode.EXT16;// -56 = 0xc8
			byteBuffer.putShort(1, (short) payloadLen);
			sizeOfNonFixExtPayloadLen = 2;
		} else {
			byteCode = ByteCode.EXT32;// -55 = 0xc9
			byteBuffer.putInt(1, payloadLen);
			sizeOfNonFixExtPayloadLen = 4;
			// TODO support dataLen > 2^31 - 1
		}
		byteBuffer.put(0, byteCode).put(1 + sizeOfNonFixExtPayloadLen, extType)
				.limit(1 + sizeOfNonFixExtPayloadLen + 1);
		write(byteBuffer);
		return this;
	}

	public MPacker packBinaryHeader(int len) throws IOException {
		if (len < (1 << 8)) {
			writeByteAndByte(ByteCode.BIN8, (byte) len);
		} else if (len < (1 << 16)) {
			writeByteAndShort(ByteCode.BIN16, (short) len);
		} else {
			writeByteAndInt(ByteCode.BIN32, len);
		}
		return this;
	}

	public MPacker packRawStringHeader(int len) throws IOException {
		if (len < (1 << 5)) {
			writeByte((byte) (ByteCode.FIXSTR_PREFIX | len));
		} else if (len < (1 << 8)) {
			writeByteAndByte(ByteCode.STR8, (byte) len);
		} else if (len < (1 << 16)) {
			writeByteAndShort(ByteCode.STR16, (short) len);
		} else {
			writeByteAndInt(ByteCode.STR32, len);
		}
		return this;
	}

	/**
	 * <ul>
	 * <li>无嵌套 packX: statCount(0);</li>
	 * <li>
	 * packArrayHeader(size): statCount(size);<br>
	 * packMapHeader(size): statCount( 2 * size);</li>
	 * <li>
	 * packBinaryHeader(length): statCount(0); binarySize += length;<br>
	 * packRawStringHeader(length): statCount(0); binarySize += length;<br>
	 * write(length): if(binarySize > 0) binarySize -= length;</li>
	 * </ul>
	 * 
	 * @param newChildCount
	 */
	protected void statCount(int newChildCount) {
		if (0 == binarySize) {
			if (childCount == 0)
				valueCount++;
			else
				childCount += newChildCount - 1;
		} else {
			throw new MPackException("The last package for Raw String or Binary has not been completed");
		}
	}

	/** 获取已经打包的顶级值数量，如22, ["fang", {"ss"=22}, 66], 88, [6, 8]则值数量为4个 */
	public int getValueCount() {
		return valueCount;
	}

	/** {@code new MPacker(new BufferedOutputStream(out))} */
	public static MPacker bufferedMPacker(OutputStream out) {
		return new MPacker(new BufferedOutputStream(out));
	}
}