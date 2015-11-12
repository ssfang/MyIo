package org.fang.stream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.DigestOutputStream;

/**
 * 只是对写入一个流的数据进行中间编码控制，相当于{@link DigestOutputStream}，通过本类方法向我写入的东西毫无保留的全部流入内部流
 * <p/>
 * 写入分两种，一种数据字节已知（类型+值），空，布尔值，整数以及浮点数等；另一种，类型（有的包括长度）+长度+负载，也许负载需要多次写入（先头再负载）。
 * <ul>
 * <li>nil format, bool format family, int format family, float format family</li>
 * <li>str format family, bin format family,array format family, map format family,ext format family</li>
 * </ul>
 * 各种格式类型数据，要么一次写入内部流（pack类型方法），要么头部和负载分开写入内部流（packXXHeader的方法+write方法）。
 * 
 * @author fangss
 * 
 * @param <T>
 * @see <a href="https://github.com/msgpack/msgpack/blob/master/spec.md">MessagePack specification</a>
 * @see <a href="https://github.com/msgpack/msgpack-java">msgpack-java</a>
 */
public class MsgPackStream<T extends OutputStream> extends FilterOutputStream {
	/**
	 * The prefix code set of MessagePack. See also https://github.com/msgpack/msgpack/blob/master/spec.md for details.
	 */
	public static final class Code {
		public static final boolean isFixInt(byte b) {
			int v = b & 0xFF;
			return v <= 0x7f || v >= 0xe0;
		}

		/**
		 * positive fixint 0xxxxxxx 0x00 - 0x7f
		 * 
		 * @param b
		 * @return
		 */
		public static final boolean isPosFixInt(byte b) {
			return (b & POSFIXINT_MASK) == 0;
		}

		/**
		 * negative fixint 111xxxxx 0xe0 - 0xff
		 * 
		 * @param b
		 * @return
		 */
		public static final boolean isNegFixInt(byte b) {
			return (b & NEGFIXINT_PREFIX) == NEGFIXINT_PREFIX;
		}

		public static final boolean isFixStr(byte b) {
			return (b & (byte) 0xe0) == Code.FIXSTR_PREFIX;
		}

		public static final boolean isFixedArray(byte b) {
			return (b & (byte) 0xf0) == Code.FIXARRAY_PREFIX;
		}

		public static final boolean isFixedMap(byte b) {
			return (b & (byte) 0xe0) == Code.FIXMAP_PREFIX;
		}

		public static final boolean isFixedRaw(byte b) {
			return (b & (byte) 0xe0) == Code.FIXSTR_PREFIX;
		}

		public static final byte POSFIXINT_MASK = (byte) 0x80;

		public static final byte FIXMAP_PREFIX = (byte) 0x80;
		public static final byte FIXARRAY_PREFIX = (byte) 0x90;
		public static final byte FIXSTR_PREFIX = (byte) 0xa0;

		public static final byte NIL = (byte) 0xc0;
		public static final byte NEVER_USED = (byte) 0xc1;
		public static final byte FALSE = (byte) 0xc2;
		public static final byte TRUE = (byte) 0xc3;
		public static final byte BIN8 = (byte) 0xc4;
		public static final byte BIN16 = (byte) 0xc5;
		public static final byte BIN32 = (byte) 0xc6;
		public static final byte EXT8 = (byte) 0xc7;
		public static final byte EXT16 = (byte) 0xc8;
		public static final byte EXT32 = (byte) 0xc9;
		public static final byte FLOAT32 = (byte) 0xca;
		public static final byte FLOAT64 = (byte) 0xcb;
		public static final byte UINT8 = (byte) 0xcc;
		public static final byte UINT16 = (byte) 0xcd;
		public static final byte UINT32 = (byte) 0xce;
		public static final byte UINT64 = (byte) 0xcf;

		public static final byte INT8 = (byte) 0xd0;
		public static final byte INT16 = (byte) 0xd1;
		public static final byte INT32 = (byte) 0xd2;
		public static final byte INT64 = (byte) 0xd3;

		public static final byte FIXEXT1 = (byte) 0xd4;
		public static final byte FIXEXT2 = (byte) 0xd5;
		public static final byte FIXEXT4 = (byte) 0xd6;
		public static final byte FIXEXT8 = (byte) 0xd7;
		public static final byte FIXEXT16 = (byte) 0xd8;

		public static final byte STR8 = (byte) 0xd9;
		public static final byte STR16 = (byte) 0xda;
		public static final byte STR32 = (byte) 0xdb;

		public static final byte ARRAY16 = (byte) 0xdc;
		public static final byte ARRAY32 = (byte) 0xdd;

		public static final byte MAP16 = (byte) 0xde;
		public static final byte MAP32 = (byte) 0xdf;

		public static final byte NEGFIXINT_PREFIX = (byte) 0xe0;
	}

	/** 对于已知不超过10字节大小数据写入的缓冲区，如整数，布尔值串等，以便一次写入内部流 */
	ByteBuffer byteBuffer;

	public MsgPackStream(T out) {
		super(out);
		byteBuffer = ByteBuffer.allocate(10);
	}

	@SuppressWarnings("unchecked")
	public T getOutput() {
		return (T) out;
	}

	/**
	 * <pre>
	 * ---format name--|-first byte (in binary)-|-first byte (in hex)-
	 * positive fixint | 0xxxxxxx               | 0x00 - 0x7f
	 * </pre>
	 * 
	 * @param posfixnum
	 *          0x00 ~ 0x7f / 0 ~ 127
	 * @throws IOException
	 */
	private void writePositiveFixNumber(byte posfixnum) throws IOException {
		out.write(posfixnum);
	}

	/**
	 * <pre>
	 * ---format name--|-first byte (in binary)-|-first byte (in hex)-
	 * negative fixint | 111xxxxx               |	0xe0 - 0xff
	 * </pre>
	 * 
	 * @param negfixnum
	 *          0xe0 ~ 0xff / -32 ~ -1
	 * @throws IOException
	 */
	private void writeNegativeFixNumber(byte negfixnum) throws IOException {
		out.write(negfixnum);
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
	 * <li><b>Other fix type header</b>: one byte = code_for_fix_xx + length_of_fix_xx</li>
	 * </ul>
	 * 
	 * @param b
	 * @throws IOException
	 */
	public void writeByte(byte b) throws IOException {
		out.write(b);
	}

	// Number family: code+value; String, Binary, Array, Map family: code+length

	private void writeByteAndByte(byte b, byte v) throws IOException {
		out.write(byteBuffer.put(0, b).put(1, v).array(), 0, 2);
	}

	private void writeByteAndShort(byte b, short v) throws IOException {
		out.write(byteBuffer.put(0, b).putShort(0, v).array(), 0, 3);
	}

	private void writeByteAndInt(byte b, int v) throws IOException {
		out.write(byteBuffer.put(0, b).putInt(0, v).array(), 0, 5);
	}

	private void writeByteAndFloat(byte b, float v) throws IOException {
		out.write(byteBuffer.put(0, b).putFloat(0, v).array(), 0, 5);
	}

	private void writeByteAndDouble(byte b, double v) throws IOException {
		out.write(byteBuffer.put(0, b).putDouble(0, v).array(), 0, 9);
	}

	private void writeByteAndLong(byte b, long v) throws IOException {
		out.write(byteBuffer.put(0, b).putLong(0, v).array(), 0, 9);
	}

	// ext format family: code_of_ext + your_ext_data_length + your_ext_type

	private void writeByteIntegerByte(byte ext, int payloadLen, byte extType) throws IOException {
		// out.write(byteBuffer.put(0, ext).putInt(1, payloadLen).put(5, extType).array(), 0, 6);
		byteBuffer.put(0, ext).position(1);
		if (Code.EXT8 == ext) {
			byteBuffer.put((byte) payloadLen);
		} else if (Code.EXT16 == ext) {
			byteBuffer.putShort((short) payloadLen);
		} else if (Code.EXT32 == ext) {
			byteBuffer.putInt(payloadLen);
		}
		byteBuffer.put(extType);
		out.write(byteBuffer.array(), 0, byteBuffer.limit());
	}

	/** 写入Nil */
	public MsgPackStream<T> pack() throws IOException {
		return packNil();
	}

	/** 写入布尔值 */
	public MsgPackStream<T> pack(boolean b) throws IOException {
		return packBoolean(b);
	}

	/** 写入整数 */
	public MsgPackStream<T> pack(int i) throws IOException {
		return packInt(i);
	}

	/** 写入长整数 */
	public MsgPackStream<T> pack(long l) throws IOException {
		return packLong(l);
	}

	/** 写入字符串 */
	public MsgPackStream<T> pack(String s) throws IOException {
		return packString(s);
	}

	public MsgPackStream<T> packNil() throws IOException {
		writeByte(Code.NIL);
		return this;
	}

	public MsgPackStream<T> packBoolean(boolean b) throws IOException {
		writeByte(b ? Code.TRUE : Code.FALSE);
		return this;
	}

	// public MsgPackStream<T> packByte(byte b) throws IOException {
	// if (b < -(1 << 5)) {
	// writeByteAndByteBuffer(INT8, byteBuffer.put(1, b));
	// } else {
	// writeFixedValue(b);
	// }
	// return this;
	// }
	//
	// public MsgPackStream<T> packShort(short v) throws IOException {
	// if (v < -(1 << 5)) {
	// if (v < -(1 << 7)) {
	// writeByteAndByteBuffer(INT16, byteBuffer.putShort(1, v));
	// } else {
	// writeByteAndByteBuffer(INT8, byteBuffer.put(1, (byte) v));
	// }
	// } else if (v < (1 << 7)) {
	// writeFixedValue((byte) v);
	// } else {
	// if (v < (1 << 8)) {
	// writeByteAndByteBuffer(UINT8, byteBuffer.put(1, (byte) v));
	// } else {
	// writeByteAndByteBuffer(UINT16, byteBuffer.putShort(1, v));
	// }
	// }
	// return this;
	// }
	//
	// public MsgPackStream<T> packLong(long longValue) throws IOException {
	// if (longValue < -(1L << 5)) {
	// if (longValue < -(1L << 15)) {
	// if (longValue < -(1L << 31)) {
	// writeByteAndByteBuffer(INT64, byteBuffer.putLong(1, longValue));
	// } else {
	// writeByteAndByteBuffer(INT32, byteBuffer.putInt(1, (int) longValue));
	// }
	// } else {
	// if (longValue < -(1 << 7)) {
	// writeByteAndByteBuffer(INT16, byteBuffer.putShort(1, (short) longValue));
	// } else {
	// writeByteAndByteBuffer(INT8, byteBuffer.put(1, (byte) longValue));
	// }
	// }
	// } else if (longValue < (1 << 7)) {
	// // fixnum
	// writeFixedValue((byte) longValue);
	// } else {
	// if (longValue < (1L << 16)) {
	// if (longValue < (1 << 8)) {
	// writeByteAndByteBuffer(UINT8, byteBuffer.put(1, (byte) longValue));
	// } else {
	// writeByteAndByteBuffer(UINT16, byteBuffer.putShort(1, (short) longValue));
	// }
	// } else {
	// if (longValue < (1L << 32)) {
	// writeByteAndByteBuffer(UINT32, byteBuffer.putInt(1, (int) longValue));
	// } else {
	// writeByteAndByteBuffer(UINT64, byteBuffer.putLong(1, longValue));
	// }
	// }
	// }
	// return this;
	// }
	//
	// public MsgPackStream<T> packBigInteger(BigInteger bi) throws IOException {
	// if (bi.bitLength() <= 63) {
	// packLong(bi.longValue());
	// } else if (bi.bitLength() == 64 && bi.signum() == 1) {
	// writeByteAndByteBuffer(UINT64, byteBuffer.putLong(1, bi.longValue()));
	// } else {
	// throw new IllegalArgumentException("MessagePack cannot serialize BigInteger larger than 2^64-1");
	// }
	// return this;
	// }
	//
	// public MsgPackStream<T> packFloat(float v) throws IOException {
	// writeByteAndByteBuffer(FLOAT32, byteBuffer.putFloat(1, v));
	// return this;
	// }
	//
	// public MsgPackStream<T> packDouble(double v) throws IOException {
	// writeByteAndByteBuffer(FLOAT64, byteBuffer.putDouble(1, v));
	// return this;
	// }

	/**
	 * Pack the input String in UTF-8 encoding
	 * 
	 * @param s
	 * @return
	 * @throws IOException
	 */
	public MsgPackStream<T> packString(String s) throws IOException {
		if (s.length() > 0) {
			// TODO encoding error?
			byte[] bs = s.getBytes("UTF-8");
			packRawStringHeader(bs.length);
			out.write(bs, 0, bs.length);
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
	public MsgPackStream<T> packOrNil(String s) throws IOException {
		if (null != s) {
			return packString(s);
		}
		writeByte(Code.NIL);
		return this;
	}

	public MsgPackStream<T> packOrNil(byte[] byteArray) throws IOException {
		if (null != byteArray) {
			packBinaryHeader(byteArray.length);
			write(byteArray);
		} else {
			writeByte(Code.NIL);
		}
		return this;
	}

	/**
	 * 打包如下类型：null - packNil, CharSequence - packString, Float - packFloat, Double - packDouble, BigInteger - packBigInteger,
	 * Number - packLong
	 * 
	 * @param obj
	 * @throws IOException
	 */
	public void pack(Object obj) throws IOException {
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
	}

	// public MsgPackStream<T> packArrayHeader(int arraySize) throws IOException {
	// if (arraySize < 0) {
	// throw new IllegalArgumentException("array size must be >= 0");
	// }
	//
	// if (arraySize < (1 << 4)) {
	// out.write((byte) (Code.FIXARRAY_PREFIX | arraySize));
	// } else if (arraySize < (1 << 16)) {
	// writeByteAndByteBuffer(Code.ARRAY16, byteBuffer.putShort(1, (short) arraySize));
	// } else {
	// writeByteAndByteBuffer(Code.ARRAY32, byteBuffer.putInt(1, arraySize));
	// }
	// return this;
	// }
	//
	// public MsgPackStream<T> packMapHeader(int mapSize) throws IOException {
	// if (mapSize < 0) {
	// throw new IllegalArgumentException("map size must be >= 0");
	// }
	//
	// if (mapSize < (1 << 4)) {
	// out.write((byte) (Code.FIXMAP_PREFIX | mapSize));
	// } else if (mapSize < (1 << 16)) {
	// writeByteAndByteBuffer(Code.MAP16, byteBuffer.putShort(1, (short) mapSize));
	// } else {
	// writeByteAndByteBuffer(Code.MAP32, byteBuffer.putInt(1, mapSize));
	// }
	// return this;
	// }

	public MsgPackStream<T> packByte(byte b) throws IOException {
		if (b < -(1 << 5)) {
			writeByteAndByte(Code.INT8, b);
		} else {
			writeByte(b);
		}
		return this;
	}

	public MsgPackStream<T> packShort(short v) throws IOException {
		if (v < -(1 << 5)) {
			if (v < -(1 << 7)) {
				writeByteAndShort(Code.INT16, v);
			} else {
				writeByteAndByte(Code.INT8, (byte) v);
			}
		} else if (v < (1 << 7)) {
			writeByte((byte) v);
		} else {
			if (v < (1 << 8)) {
				writeByteAndByte(Code.UINT8, (byte) v);
			} else {
				writeByteAndShort(Code.UINT16, v);
			}
		}
		return this;
	}

	public MsgPackStream<T> packInt(int r) throws IOException {
		if (r < -(1 << 5)) {
			if (r < -(1 << 15)) {
				writeByteAndInt(Code.INT32, r);
			} else if (r < -(1 << 7)) {
				writeByteAndShort(Code.INT16, (short) r);
			} else {
				writeByteAndByte(Code.INT8, (byte) r);
			}
		} else if (r < (1 << 7)) {
			writeByte((byte) r);
		} else {
			if (r < (1 << 8)) {
				writeByteAndByte(Code.UINT8, (byte) r);
			} else if (r < (1 << 16)) {
				writeByteAndShort(Code.UINT16, (short) r);
			} else {
				// unsigned 32
				writeByteAndInt(Code.UINT32, r);
			}
		}
		return this;
	}

	public MsgPackStream<T> packLong(long v) throws IOException {
		if (v < -(1L << 5)) {
			if (v < -(1L << 15)) {
				if (v < -(1L << 31)) {
					writeByteAndLong(Code.INT64, v);
				} else {
					writeByteAndInt(Code.INT32, (int) v);
				}
			} else {
				if (v < -(1 << 7)) {
					writeByteAndShort(Code.INT16, (short) v);
				} else {
					writeByteAndByte(Code.INT8, (byte) v);
				}
			}
		} else if (v < (1 << 7)) {
			// fixnum
			writeByte((byte) v);
		} else {
			if (v < (1L << 16)) {
				if (v < (1 << 8)) {
					writeByteAndByte(Code.UINT8, (byte) v);
				} else {
					writeByteAndShort(Code.UINT16, (short) v);
				}
			} else {
				if (v < (1L << 32)) {
					writeByteAndInt(Code.UINT32, (int) v);
				} else {
					writeByteAndLong(Code.UINT64, v);
				}
			}
		}
		return this;
	}

	public MsgPackStream<T> packBigInteger(BigInteger bi) throws IOException {
		if (bi.bitLength() <= 63) {
			packLong(bi.longValue());
		} else if (bi.bitLength() == 64 && bi.signum() == 1) {
			writeByteAndLong(Code.UINT64, bi.longValue());
		} else {
			throw new IllegalArgumentException("MessagePack cannot serialize BigInteger larger than 2^64-1");
		}
		return this;
	}

	public MsgPackStream<T> packFloat(float v) throws IOException {
		writeByteAndFloat(Code.FLOAT32, v);
		return this;
	}

	public MsgPackStream<T> packDouble(double v) throws IOException {
		writeByteAndDouble(Code.FLOAT64, v);
		return this;
	}

	public MsgPackStream<T> packArrayHeader(int arraySize) throws IOException {
		if (arraySize < 0) {
			throw new IllegalArgumentException("array size must be >= 0");
		}

		if (arraySize < (1 << 4)) {
			writeByte((byte) (Code.FIXARRAY_PREFIX | arraySize));
		} else if (arraySize < (1 << 16)) {
			writeByteAndShort(Code.ARRAY16, (short) arraySize);
		} else {
			writeByteAndInt(Code.ARRAY32, arraySize);
		}
		return this;
	}

	public MsgPackStream<T> packMapHeader(int mapSize) throws IOException {
		if (mapSize < 0) {
			throw new IllegalArgumentException("map size must be >= 0");
		}

		if (mapSize < (1 << 4)) {
			writeByte((byte) (Code.FIXMAP_PREFIX | mapSize));
		} else if (mapSize < (1 << 16)) {
			writeByteAndShort(Code.MAP16, (short) mapSize);
		} else {
			writeByteAndInt(Code.MAP32, mapSize);
		}
		return this;
	}

	public MsgPackStream<T> packExtensionTypeHeader(byte extType, int payloadLen) throws IOException {
		if (payloadLen < (1 << 8)) {
			if (payloadLen > 0 && (payloadLen & (payloadLen - 1)) == 0) { // check whether dataLen == 2^x
				if (payloadLen == 1) {
					writeByteAndByte(Code.FIXEXT1, extType);
				} else if (payloadLen == 2) {
					writeByteAndByte(Code.FIXEXT2, extType);
				} else if (payloadLen == 4) {
					writeByteAndByte(Code.FIXEXT4, extType);
				} else if (payloadLen == 8) {
					writeByteAndByte(Code.FIXEXT8, extType);
				} else if (payloadLen == 16) {
					writeByteAndByte(Code.FIXEXT16, extType);
				} else {
					writeByteIntegerByte(Code.EXT8, payloadLen, extType);
				}
			} else {
				writeByteIntegerByte(Code.EXT8, payloadLen, extType);
			}
		} else if (payloadLen < (1 << 16)) {
			writeByteIntegerByte(Code.EXT16, payloadLen, extType);
		} else {
			writeByteIntegerByte(Code.EXT32, payloadLen, extType);

			// TODO support dataLen > 2^31 - 1
		}
		return this;
	}

	public MsgPackStream<T> packBinaryHeader(int len) throws IOException {
		if (len < (1 << 8)) {
			writeByteAndByte(Code.BIN8, (byte) len);
		} else if (len < (1 << 16)) {
			writeByteAndShort(Code.BIN16, (short) len);
		} else {
			writeByteAndInt(Code.BIN32, len);
		}
		return this;
	}

	public MsgPackStream<T> packRawStringHeader(int len) throws IOException {
		if (len < (1 << 5)) {
			writeByte((byte) (Code.FIXSTR_PREFIX | len));
		} else if (len < (1 << 8)) {
			writeByteAndByte(Code.STR8, (byte) len);
		} else if (len < (1 << 16)) {
			writeByteAndShort(Code.STR16, (short) len);
		} else {
			writeByteAndInt(Code.STR32, len);
		}
		return this;
	}

}