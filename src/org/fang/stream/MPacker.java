package org.fang.msgpack;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.DigestOutputStream;

/**
 * 只是对写入（pack开头的方法）的数据进行中间编码控制，类似于{@link DigestOutputStream}；<br>
 * 通过本类的方法向我写入的东西毫无保留的全部流入内部流，即我的写指针和底层流一致。
 * <p/>
 * 写入分两种，一种数据字节已知（类型+值），空，布尔值，整数以及浮点数等；<br>
 * 另一种，类型（有的包括长度）+长度+负载，也许负载需要多次写入（先头再负载）。
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
public class MPacker<T extends OutputStream> extends FilterOutputStream {

	/** 对于已知不超过10字节大小数据写入的缓冲区，如整数，布尔值串等，以便一次写入内部流 */
	ByteBuffer byteBuffer;

	public MPacker(T out) {
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
		out.write(byteBuffer.put(0, b).putShort(1, v).array(), 0, 3);
	}

	private void writeByteAndInt(byte b, int v) throws IOException {
		out.write(byteBuffer.put(0, b).putInt(1, v).array(), 0, 5);
	}

	private void writeByteAndFloat(byte b, float v) throws IOException {
		out.write(byteBuffer.put(0, b).putFloat(1, v).array(), 0, 5);
	}

	private void writeByteAndDouble(byte b, double v) throws IOException {
		out.write(byteBuffer.put(0, b).putDouble(1, v).array(), 0, 9);
	}

	private void writeByteAndLong(byte b, long v) throws IOException {
		out.write(byteBuffer.put(0, b).putLong(1, v).array(), 0, 9);
	}

	// ext format family: code_of_ext + your_ext_data_length + your_ext_type

	private void writeByteIntegerByte(byte ext, int payloadLen, byte extType) throws IOException {
		// out.write(byteBuffer.put(0, ext).putInt(1, payloadLen).put(5, extType).array(), 0, 6);
		byteBuffer.put(0, ext).position(1);
		if (MPackCode.EXT8 == ext) {
			byteBuffer.put((byte) payloadLen);
		} else if (MPackCode.EXT16 == ext) {
			byteBuffer.putShort((short) payloadLen);
		} else if (MPackCode.EXT32 == ext) {
			byteBuffer.putInt(payloadLen);
		}
		byteBuffer.put(extType);
		out.write(byteBuffer.array(), 0, byteBuffer.limit());
	}

	/** 写入Nil */
	public MPacker<T> pack() throws IOException {
		return packNil();
	}

	/** 写入布尔值 */
	public MPacker<T> pack(boolean b) throws IOException {
		return packBoolean(b);
	}

	/** 写入整数 */
	public MPacker<T> pack(int i) throws IOException {
		return packInt(i);
	}

	/** 写入长整数 */
	public MPacker<T> pack(long l) throws IOException {
		return packLong(l);
	}

	/** 写入字符串 */
	public MPacker<T> pack(String s) throws IOException {
		return packString(s);
	}

	public MPacker<T> packNil() throws IOException {
		writeByte(MPackCode.NIL);
		return this;
	}

	public MPacker<T> packBoolean(boolean b) throws IOException {
		writeByte(b ? MPackCode.TRUE : MPackCode.FALSE);
		return this;
	}

	// public MPacker<T> packByte(byte b) throws IOException {
	// if (b < -(1 << 5)) {
	// writeByteAndByteBuffer(INT8, byteBuffer.put(1, b));
	// } else {
	// writeFixedValue(b);
	// }
	// return this;
	// }
	//
	// public MPacker<T> packShort(short v) throws IOException {
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
	// public MPacker<T> packLong(long longValue) throws IOException {
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
	// public MPacker<T> packBigInteger(BigInteger bi) throws IOException {
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
	// public MPacker<T> packFloat(float v) throws IOException {
	// writeByteAndByteBuffer(FLOAT32, byteBuffer.putFloat(1, v));
	// return this;
	// }
	//
	// public MPacker<T> packDouble(double v) throws IOException {
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
	public MPacker<T> packString(String s) throws IOException {
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
	public MPacker<T> packOrNil(String s) throws IOException {
		if (null != s) {
			return packString(s);
		}
		writeByte(MPackCode.NIL);
		return this;
	}

	public MPacker<T> packOrNil(byte[] byteArray) throws IOException {
		if (null != byteArray) {
			packBinaryHeader(byteArray.length);
			write(byteArray);
		} else {
			writeByte(MPackCode.NIL);
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

	// public MPacker<T> packArrayHeader(int arraySize) throws IOException {
	// if (arraySize < 0) {
	// throw new IllegalArgumentException("array size must be >= 0");
	// }
	//
	// if (arraySize < (1 << 4)) {
	// out.write((byte) (MPackCode.FIXARRAY_PREFIX | arraySize));
	// } else if (arraySize < (1 << 16)) {
	// writeByteAndByteBuffer(MPackCode.ARRAY16, byteBuffer.putShort(1, (short) arraySize));
	// } else {
	// writeByteAndByteBuffer(MPackCode.ARRAY32, byteBuffer.putInt(1, arraySize));
	// }
	// return this;
	// }
	//
	// public MPacker<T> packMapHeader(int mapSize) throws IOException {
	// if (mapSize < 0) {
	// throw new IllegalArgumentException("map size must be >= 0");
	// }
	//
	// if (mapSize < (1 << 4)) {
	// out.write((byte) (MPackCode.FIXMAP_PREFIX | mapSize));
	// } else if (mapSize < (1 << 16)) {
	// writeByteAndByteBuffer(MPackCode.MAP16, byteBuffer.putShort(1, (short) mapSize));
	// } else {
	// writeByteAndByteBuffer(MPackCode.MAP32, byteBuffer.putInt(1, mapSize));
	// }
	// return this;
	// }

	public MPacker<T> packByte(byte b) throws IOException {
		if (b < -(1 << 5)) {
			writeByteAndByte(MPackCode.INT8, b);
		} else {
			writeByte(b);
		}
		return this;
	}

	public MPacker<T> packShort(short v) throws IOException {
		if (v < -(1 << 5)) {
			if (v < -(1 << 7)) {
				writeByteAndShort(MPackCode.INT16, v);
			} else {
				writeByteAndByte(MPackCode.INT8, (byte) v);
			}
		} else if (v < (1 << 7)) {
			writeByte((byte) v);
		} else {
			if (v < (1 << 8)) {
				writeByteAndByte(MPackCode.UINT8, (byte) v);
			} else {
				writeByteAndShort(MPackCode.UINT16, v);
			}
		}
		return this;
	}

	public MPacker<T> packInt(int r) throws IOException {
		if (r < -(1 << 5)) {
			if (r < -(1 << 15)) {
				writeByteAndInt(MPackCode.INT32, r);
			} else if (r < -(1 << 7)) {
				writeByteAndShort(MPackCode.INT16, (short) r);
			} else {
				writeByteAndByte(MPackCode.INT8, (byte) r);
			}
		} else if (r < (1 << 7)) {
			writeByte((byte) r);
		} else {
			if (r < (1 << 8)) {
				writeByteAndByte(MPackCode.UINT8, (byte) r);
			} else if (r < (1 << 16)) {
				writeByteAndShort(MPackCode.UINT16, (short) r);
			} else {
				// unsigned 32
				writeByteAndInt(MPackCode.UINT32, r);
			}
		}
		return this;
	}

	public MPacker<T> packLong(long v) throws IOException {
		if (v < -(1L << 5)) {
			if (v < -(1L << 15)) {
				if (v < -(1L << 31)) {
					writeByteAndLong(MPackCode.INT64, v);
				} else {
					writeByteAndInt(MPackCode.INT32, (int) v);
				}
			} else {
				if (v < -(1 << 7)) {
					writeByteAndShort(MPackCode.INT16, (short) v);
				} else {
					writeByteAndByte(MPackCode.INT8, (byte) v);
				}
			}
		} else if (v < (1 << 7)) {
			// fixnum
			writeByte((byte) v);
		} else {
			if (v < (1L << 16)) {
				if (v < (1 << 8)) {
					writeByteAndByte(MPackCode.UINT8, (byte) v);
				} else {
					writeByteAndShort(MPackCode.UINT16, (short) v);
				}
			} else {
				if (v < (1L << 32)) {
					writeByteAndInt(MPackCode.UINT32, (int) v);
				} else {
					writeByteAndLong(MPackCode.UINT64, v);
				}
			}
		}
		return this;
	}

	public MPacker<T> packBigInteger(BigInteger bi) throws IOException {
		if (bi.bitLength() <= 63) {
			packLong(bi.longValue());
		} else if (bi.bitLength() == 64 && bi.signum() == 1) {
			writeByteAndLong(MPackCode.UINT64, bi.longValue());
		} else {
			throw new IllegalArgumentException("MessagePack cannot serialize BigInteger larger than 2^64-1");
		}
		return this;
	}

	public MPacker<T> packFloat(float v) throws IOException {
		writeByteAndFloat(MPackCode.FLOAT32, v);
		return this;
	}

	public MPacker<T> packDouble(double v) throws IOException {
		writeByteAndDouble(MPackCode.FLOAT64, v);
		return this;
	}

	public MPacker<T> packArrayHeader(int arraySize) throws IOException {
		if (arraySize < 0) {
			throw new IllegalArgumentException("array size must be >= 0");
		}

		if (arraySize < (1 << 4)) {
			writeByte((byte) (MPackCode.FIXARRAY_PREFIX | arraySize));
		} else if (arraySize < (1 << 16)) {
			writeByteAndShort(MPackCode.ARRAY16, (short) arraySize);
		} else {
			writeByteAndInt(MPackCode.ARRAY32, arraySize);
		}
		return this;
	}

	public MPacker<T> packMapHeader(int mapSize) throws IOException {
		if (mapSize < 0) {
			throw new IllegalArgumentException("map size must be >= 0");
		}

		if (mapSize < (1 << 4)) {
			writeByte((byte) (MPackCode.FIXMAP_PREFIX | mapSize));
		} else if (mapSize < (1 << 16)) {
			writeByteAndShort(MPackCode.MAP16, (short) mapSize);
		} else {
			writeByteAndInt(MPackCode.MAP32, mapSize);
		}
		return this;
	}

	public MPacker<T> packExtensionTypeHeader(byte extType, int payloadLen) throws IOException {
		if (payloadLen < (1 << 8)) {
			if (payloadLen > 0 && (payloadLen & (payloadLen - 1)) == 0) { // check whether dataLen == 2^x
				if (payloadLen == 1) {
					writeByteAndByte(MPackCode.FIXEXT1, extType);
				} else if (payloadLen == 2) {
					writeByteAndByte(MPackCode.FIXEXT2, extType);
				} else if (payloadLen == 4) {
					writeByteAndByte(MPackCode.FIXEXT4, extType);
				} else if (payloadLen == 8) {
					writeByteAndByte(MPackCode.FIXEXT8, extType);
				} else if (payloadLen == 16) {
					writeByteAndByte(MPackCode.FIXEXT16, extType);
				} else {
					writeByteIntegerByte(MPackCode.EXT8, payloadLen, extType);
				}
			} else {
				writeByteIntegerByte(MPackCode.EXT8, payloadLen, extType);
			}
		} else if (payloadLen < (1 << 16)) {
			writeByteIntegerByte(MPackCode.EXT16, payloadLen, extType);
		} else {
			writeByteIntegerByte(MPackCode.EXT32, payloadLen, extType);

			// TODO support dataLen > 2^31 - 1
		}
		return this;
	}

	public MPacker<T> packBinaryHeader(int len) throws IOException {
		if (len < (1 << 8)) {
			writeByteAndByte(MPackCode.BIN8, (byte) len);
		} else if (len < (1 << 16)) {
			writeByteAndShort(MPackCode.BIN16, (short) len);
		} else {
			writeByteAndInt(MPackCode.BIN32, len);
		}
		return this;
	}

	public MPacker<T> packRawStringHeader(int len) throws IOException {
		if (len < (1 << 5)) {
			writeByte((byte) (MPackCode.FIXSTR_PREFIX | len));
		} else if (len < (1 << 8)) {
			writeByteAndByte(MPackCode.STR8, (byte) len);
		} else if (len < (1 << 16)) {
			writeByteAndShort(MPackCode.STR16, (short) len);
		} else {
			writeByteAndInt(MPackCode.STR32, len);
		}
		return this;
	}

}