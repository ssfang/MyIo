package org.msgpack;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.msgpack.ByteCode.FamilyType;

/**
 * read开头系列方法适用于读负载。{@link #getNextCode()}
 * 。也许ReadableByteChannel或FileChannel需要装饰为InputStream<br>
 * 一般用法：
 * 
 * <pre>
 * <code>
 *     MUnpacker unpacker = new MUnpacker(...);
 *     while(unpacker.hasNext()) {
 *         MPackFormat f = unpacker.getNextFormat();
 *         switch(f) {
 *             case POSFIXINT:
 *             case INT8:
 *             case UINT8: {
 *                int v = unpacker.unpackInt();
 *                break;
 *             }
 *             case STRING: {
 *                String v = unpacker.unpackString();
 *                break;
 *             }
 *             // ...
 *       }
 *     }
 * 
 * </code>
 * </pre>
 * 
 * @author fang
 * 
 * @param <T>
 * @see <a href="https://github.com/msgpack/msgpack-java">msgpack-java</a>
 */
public class MUnpacker extends FilterInputStream {
	private final static int NEXT_DIRTY = -1;

	private static final String EMPTY_STRING = "";
	private static final Object[] EMPTY_ARRAY = new Object[0];

	/**
	 * 二进制格式读为字符串，对象类型为{@link String}，默认编码为UTF-8
	 * 
	 * @see {@link #binaryToString(byte[])}
	 */
	public static final int OPT_READ_BIN_AS_STR = 0x00000001;
	/** 字符串格式不解码作为二进制输出，对象类型为<code>byte[]</code>，默认编码为UTF-8 */
	public static final int OPT_READ_STR_AS_BIN = 0x00000002;
	/**
	 * 数组格式作为<code>List&ltObject&gt</code>输出，即
	 * <code>Arrays.asList(Object[])</code>
	 */
	public static final int OPT_READ_ARRAY_AS_LIST = 0x00000004;
	/** 整数类型按字节大小和有无符号与Java整数类型做最适合匹配；否则，除了UINT64使用BigInteger，其他一律Long */
	public static final int OPT_MATCHED_INTEGER = 0x00000008;
	/** 整数类型按字节大小与Java浮点数类型做最适合匹配：FLOAT32 -> Float, FLOAT64 -> Double；否则一律Double */
	public static final int OPT_MATCHED_FLOAT = 0x00000010;

	/** 用于{@link #unpack()}的全局选项 */
	private int options;

	/** Length threshold */
	private final int readStringMaxSize = Integer.MAX_VALUE, readBinaryMaxSize = Integer.MAX_VALUE;

	@SuppressWarnings("rawtypes")
	private Class<? extends Map> asMapClass;

	/**
	 * generally range: 0 to 255, but {@value #NEXT_DIRTY} is also valid,
	 * represents dirty.
	 * 
	 * @see java.io.FilterInputStream#read()
	 * @see #getNextCode()
	 * */
	private int nextByte = NEXT_DIRTY;

	/** 缓冲区，用于字节转整数 */
	protected ByteBuffer byteBuffer;
	private static final int BUFFER_LENGTH = 8;

	// byte[] buf = new byte[8];

	public MUnpacker(InputStream in) {
		super(in);
		byteBuffer = ByteBuffer.wrap(new byte[BUFFER_LENGTH]);
	}

	@Override
	public long skip(long n) throws IOException {
		nextByte = NEXT_DIRTY;
		return super.skip(n);
	}

	/**
	 * Returns true true if this unpacker has more elements. When this returns
	 * true, subsequent call to {@link #getNextCodeType()} returns a code type.
	 * If false, {@link #getNextCodeType()} will throw an EOFException.
	 * 
	 * @return true if this unpacker has more elements to read
	 */
	public boolean hasNext() throws IOException {
		if (NEXT_DIRTY == nextByte) {
			nextByte = read();
		}
		return 0 <= nextByte;
	}

	// /** 测度是否带有一个或多个选项 */
	// public static boolean hasOption(int options, int optionsToTest) {
	// return (options & optionsToTest) > 0;
	// }

	public int getOptions() {
		return options;
	}

	public MUnpacker setOptions(int options) {
		this.options = options;
		return this;
	}

	@SuppressWarnings("rawtypes")
	public Class<? extends Map> getAsMapClass() {
		return asMapClass;
	}

	public MUnpacker setAsMapClass(@SuppressWarnings("rawtypes") Class<? extends Map> asMapClass) {
		this.asMapClass = asMapClass;
		return this;
	}

	/**
	 * 这个方法功能等于调用{@link #getNextCode()}并赋值<code>nextByte = NEXT_DIRTY;</code><br>
	 * 即缓存先读取缓存再使缓存无效化，无缓存直接从内部流读取，最后缓存总是无效的
	 * 
	 * @return the next byte of this input streamGusher as a signed 8-bit
	 *         <code>byte</code>.
	 * @exception EOFException if this input streamGusher has reached the end.
	 * @exception IOException the streamGusher has been closed and the contained
	 *                input streamGusher does not support reading after close,
	 *                or another I/O error occurs.
	 * @see java.io.FilterInputStream#in
	 */
	private final byte readNextByte() throws IOException {
		int ch;
		if (NEXT_DIRTY != nextByte) {
			ch = nextByte;
			nextByte = NEXT_DIRTY;
		} else {
			ch = read();
			if (ch < 0)
				throw new EOFException();
		}
		return (byte) (ch);
	}

	/** Read 1 bytes as a byte(4 bits) */
	private final byte readByte() throws IOException {
		return consume(1).get(0);
	}

	/** Read 2 bytes as a short(16 bits) integer */
	private short readShort() throws IOException {
		return consume(2).getShort(0);
	}

	/** Read 4 bytes as a 32 bits integer */
	private int readInt() throws IOException {
		return consume(4).getInt(0);
	}

	/** Read 8 bytes as a long(64 bits) integer */
	private long readLong() throws IOException {
		return consume(8).getLong(0);
	}

	/** Read 8 bytes as a double(64 bits) */
	private double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	/** Read 4 bytes as a float(32 bits) */
	private float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	/** @return 1 byte(4 bits): 0 ~ 255 */
	private int readUnsignedByte() throws IOException {
		byte u8 = readByte();
		return u8 & 0xff;
	}

	/** @return 2 bytes(8 bits): 0 ~ 65535 */
	private int readUnsignedShort() throws IOException {
		short u16 = readShort();
		return u16 & 0xffff;
	}

	/**
	 * 读取大的长度，Limitation一节规定字节或数组长度或Map键值对个数都不能超过(2^32)-1
	 * 
	 * @return 4 bytes(32 bits): 0 ~ (2^32)-1
	 */
	private int readNextLength32() throws IOException {
		int u32 = readInt();
		if (u32 < 0) {
			throw overflowU32Size(u32);
		}
		return u32;
	}

	private int readStringHeader(byte b) throws IOException {
		switch (b) {
		case ByteCode.STR8: // str 8
			return readUnsignedByte();
		case ByteCode.STR16: // str 16
			return readUnsignedShort();
		case ByteCode.STR32: // str 32
			return readNextLength32();
		default:
			return -1;
		}
	}

	private int readBinaryHeader(byte b) throws IOException {
		switch (b) {
		case ByteCode.BIN8: // bin 8
			return readUnsignedByte();
		case ByteCode.BIN16: // bin 16
			return readUnsignedShort();
		case ByteCode.BIN32: // bin 32
			return readNextLength32();
		default:
			return -1;
		}
	}

	/**
	 * 临时读取到{@link #byteBuffer}中，默认至多{@value #BUFFER_LENGTH}个
	 * 
	 * @param nBytes 暂时未检查是否在0到{@link byteBuffer#capacity()}之间
	 * @return
	 * @throws EOFException
	 * @throws IOException
	 */
	private ByteBuffer consume(int nBytes) throws IOException {
		nextByte = NEXT_DIRTY;
		int nReadedBytes = read(byteBuffer.array(), 0, nBytes);
		if (nReadedBytes < nBytes) {
			// nReadedBytes : -1-eof, 0-no bytes, < nBytes, not enough.
			throw new EOFException("insufficient data length for reading the value of " + nBytes);
		}
		byteBuffer.limit(nBytes);
		return byteBuffer;
	}

	/**
	 * 读取下一个字节，这个字节会被缓存起来，以后都返回缓存值，除非调用read开头的方法或skip方法，会让缓存的字节无效化。<br>
	 * 即有缓存就读取缓存值，无缓存直接从内部流读取并设置到缓存变量中，最后缓存总是有效的
	 * 
	 * @return the next byte of this input streamGusher or the cache byte as a
	 *         signed 8-bit <code>byte</code>.
	 * @exception EOFException if this input streamGusher has reached the end.
	 * @exception IOException the streamGusher has been closed and the contained
	 *                input streamGusher does not support reading after close,
	 *                or another I/O error occurs.
	 * @see java.io.FilterInputStream#in
	 */
	public byte getNextCode() throws IOException {
		if (NEXT_DIRTY == nextByte) {
			nextByte = read();
		}
		if (nextByte >= 0)
			return (byte) (nextByte);
		throw new EOFException();
	}

	/**
	 * 获取下一个值的格式
	 * 
	 * @return
	 * @throws EOFException
	 * @throws IOException
	 */
	public MPackFormat getNextFormat() throws IOException {
		return MPackFormat.valueOf(getNextCode());
	}

	/**
	 * @return null
	 * @throws MPackFormatException 类型不是boolean的异常
	 * @throws IOException
	 */
	public <V> V unpackNil() throws IOException {
		byte b = readNextByte();
		if (b == ByteCode.NIL) {
			return null;
		}
		throw unexpected("Nil", b);
	}

	/**
	 * 
	 * @return a the primitive type boolean value
	 * @throws MPackFormatException 类型不是boolean的异常
	 * @throws IOException
	 */
	public boolean unpackBoolean() throws IOException {
		byte b = readNextByte();
		if (b == ByteCode.FALSE) {
			return false;
		} else if (b == ByteCode.TRUE) {
			return true;
		}
		throw unexpected("boolean", b);
	}

	public short unpackByte() throws IOException {
		int i = unpackInt();
		if (Byte.MIN_VALUE <= i && i <= Byte.MAX_VALUE) {
			return (byte) i;
		}
		throw new MPackIntegerOverflowException(i);
	}

	public short unpackShort() throws IOException {
		int i = unpackInt();
		if (Short.MIN_VALUE <= i && i <= Short.MAX_VALUE) {
			return (short) i;
		}
		throw new MPackIntegerOverflowException(i);
	}

	/**
	 * 可以获取Byte，Short，Int， Long值，也许会抛出异常
	 * 
	 * @return a the primitive type int value
	 * @throws MPackIntegerOverflowException 由于返回值是java
	 *             int，对于UINT32，UINT64和INT64会有溢出异常
	 * @throws MPackFormatException 类型不是整数的异常
	 * @throws IOException
	 */
	public int unpackInt() throws IOException {
		byte b = readNextByte();
		if (ByteCode.isFixInt(b)) {
			nextByte = NEXT_DIRTY;
			return b;
		}
		switch (b) {
		case ByteCode.UINT8: // unsigned int 8
			byte u8 = readNextByte();
			return u8 & 0xff;
		case ByteCode.UINT16: // unsigned int 16
			short u16 = readShort();
			return u16 & 0xffff;

		case ByteCode.UINT32:// unsigned int 32
			int i = readInt();
			if (i < 0)
				throw overflowU32(i);
			else
				return i;
		case ByteCode.UINT64: // unsigned int 64
			long u64 = readLong();
			if (u64 < 0L || u64 > Integer.MAX_VALUE) {
				throw overflowU64(u64);
			}
			return (int) u64;
		case ByteCode.INT8: // signed int 8
			byte i8 = readNextByte();
			return i8;
		case ByteCode.INT16: // signed int 16
			short i16 = readShort();
			return i16;
		case ByteCode.INT32: // signed int 32
			int i32 = readInt();
			return i32;
		case ByteCode.INT64: // signed int 64
			long i64 = readLong();
			if (i64 < Integer.MIN_VALUE || i64 > Integer.MAX_VALUE) {
				throw overflowI(i64);
			}
		}
		throw unexpected("Integer", b);
	}

	/**
	 * 可以获取Byte，Short，Int， Long值，也许会抛出异常
	 * 
	 * @return a the primitive type long value
	 * @throws MPackIntegerOverflowException 由于返回值是java long，对于UINT64会有溢出异常
	 * @throws MPackFormatException 类型不是整数的异常
	 * @throws IOException
	 */
	public long unpackLong() throws IOException {
		byte b = readNextByte();
		if (ByteCode.isFixInt(b)) {
			nextByte = NEXT_DIRTY;
			return b;
		}
		switch (b) {
		case ByteCode.UINT8: // unsigned int 8
			byte u8 = readNextByte();
			return u8 & 0xff;
		case ByteCode.UINT16: // unsigned int 16
			short u16 = readShort();
			return u16 & 0xffff;
		case ByteCode.UINT32: // unsigned int 32
			int u32 = readInt();
			if (u32 < 0) {
				return (u32 & 0x7fffffff) + 0x80000000L;
			} else {
				return u32;
			}
		case ByteCode.UINT64: // unsigned int 64
			long u64 = readLong();
			if (u64 < 0L) {
				throw overflowU64(u64);
			}
			return u64;
		case ByteCode.INT8: // signed int 8
			byte i8 = readNextByte();
			return i8;
		case ByteCode.INT16: // signed int 16
			short i16 = readShort();
			return i16;
		case ByteCode.INT32: // signed int 32
			int i32 = readInt();
			return i32;
		case ByteCode.INT64: // signed int 64
			long i64 = readLong();
			return i64;
		}
		throw unexpected("Integer", b);
	}

	public float unpackFloat() throws IOException {
		byte b = readNextByte();
		switch (b) {
		case ByteCode.FLOAT32: // float
			float fv = readFloat();
			return fv;
		case ByteCode.FLOAT64: // double
			double dv = readDouble();
			return (float) dv;
		}
		throw unexpected("Float", b);
	}

	public double unpackDouble() throws IOException {
		byte b = readNextByte();
		switch (b) {
		case ByteCode.FLOAT32: // float
			float fv = readFloat();
			return fv;
		case ByteCode.FLOAT64: // double
			double dv = readDouble();
			return dv;
		}
		throw unexpected("Float", b);
	}

	/**
	 * 可以获取整数类型的java BigInteger对象
	 * 
	 * @return
	 * @throws MPackFormatException 类型不是整数的异常
	 * @throws IOException
	 */
	public BigInteger unpackBigInteger() throws IOException {
		byte b = readNextByte();
		if (ByteCode.isFixInt(b)) {
			return BigInteger.valueOf(b);
		}
		switch (b) {
		case ByteCode.UINT8: // unsigned int 8
			byte u8 = readNextByte();
			return BigInteger.valueOf(u8 & 0xff);
		case ByteCode.UINT16: // unsigned int 16
			short u16 = readShort();
			return BigInteger.valueOf(u16 & 0xffff);
		case ByteCode.UINT32: // unsigned int 32
			int u32 = readInt();
			if (u32 < 0) {
				return BigInteger.valueOf((u32 & 0x7fffffff) + 0x80000000L);
			} else {
				return BigInteger.valueOf(u32);
			}
		case ByteCode.UINT64: // unsigned int 64
			long u64 = readLong();
			if (u64 < 0L) {
				BigInteger bi = BigInteger.valueOf(u64 + Long.MAX_VALUE + 1L).setBit(63);
				return bi;
			} else {
				return BigInteger.valueOf(u64);
			}
		case ByteCode.INT8: // signed int 8
			byte i8 = readNextByte();
			return BigInteger.valueOf(i8);
		case ByteCode.INT16: // signed int 16
			short i16 = readShort();
			return BigInteger.valueOf(i16);
		case ByteCode.INT32: // signed int 32
			int i32 = readInt();
			return BigInteger.valueOf(i32);
		case ByteCode.INT64: // signed int 64
			long i64 = readLong();
			return BigInteger.valueOf(i64);
		}
		throw unexpected("Integer", b);
	}

	/**
	 * Skip reading the specified number of bytes. Use this method only if you
	 * know skipping data is safe. For simply skipping the next value, use
	 * {@link #skipValue()}.
	 * 
	 * @param numBytes
	 * @throws IOException
	 */
	public void skipBytes(int numBytes) throws IOException {
		if (numBytes < 0) {
			throw new IllegalArgumentException("skip length must be >= 0: " + numBytes);
		}
		skip(numBytes);
	}

	/**
	 * Skip the next value, then move the cursor at the end of the value
	 * 
	 * @throws IOException
	 */
	public MUnpacker skipValue() throws IOException {
		return skipValue(1);
	}

	/**
	 * Skip the specified number of values, then move the cursor at the end of
	 * the value
	 * 
	 * @param valueCount
	 * @throws IOException
	 */
	public MUnpacker skipValue(int valueCount) throws IOException {
		int remainingValues = valueCount;
		while (remainingValues > 0) {
			byte byteCode = readNextByte();
			MPackFormat format = MPackFormat.valueOf(byteCode);
			switch (format) {
			case POSFIXINT:
			case NEGFIXINT:
			case BOOLEAN:
			case NIL:
				break;
			case FIXMAP: {
				int mapLen = byteCode & 0x0f;// 1000xxxx
				remainingValues += mapLen * 2;
				break;
			}
			case FIXARRAY: {
				int arrayLen = byteCode & 0x0f;// 1001xxxx
				remainingValues += arrayLen;
				break;
			}
			case FIXSTR: {
				int strLen = byteCode & 0x1f; // 101xxxxx
				skipBytes(strLen);
				break;
			}
			case INT8:
			case UINT8:
				skipBytes(1);
				break;
			case INT16:
			case UINT16:
				skipBytes(2);
				break;
			case INT32:
			case UINT32:
			case FLOAT32:
				skipBytes(4);
				break;
			case INT64:
			case UINT64:
			case FLOAT64:
				skipBytes(8);
				break;
			case BIN8:
			case STR8:
				skipBytes(readUnsignedByte());
				break;
			case BIN16:
			case STR16:
				skipBytes(readUnsignedShort());
				break;
			case BIN32:
			case STR32:
				skipBytes(readNextLength32());
				break;
			case FIXEXT1:
				skipBytes(2);
				break;
			case FIXEXT2:
				skipBytes(3);
				break;
			case FIXEXT4:
				skipBytes(5);
				break;
			case FIXEXT8:
				skipBytes(9);
				break;
			case FIXEXT16:
				skipBytes(17);
				break;
			case EXT8:
				skipBytes(readUnsignedByte() + 1);
				break;
			case EXT16:
				skipBytes(readUnsignedShort() + 1);
				break;
			case EXT32:
				skipBytes(readNextLength32() + 1);
				break;
			case ARRAY16:
				remainingValues += readUnsignedShort();
				break;
			case ARRAY32:
				remainingValues += readNextLength32();
				break;
			case MAP16:
				remainingValues += readUnsignedShort() * 2;
				break;
			case MAP32:
				// TODO check int overflow
				remainingValues += readNextLength32() * 2;
				break;
			case NEVER_USED:
				throw new RuntimeException(String.format("unknown code: %02x is found", byteCode));
			}
			remainingValues--;
		}
		return this;
	}

	public String unpackString() throws IOException {
		return unpackString(Integer.MAX_VALUE);
	}

	public String unpackString(int maxUnpackStringSize) throws IOException {
		int strLen = unpackRawStringHeader();
		if (strLen > 0) {
			if (strLen <= maxUnpackStringSize) {
				return new String(readPayload(strLen));
			}
			throw new MPackException(String.format("cannot unpack a String of size larger than %,d: %,d",
					maxUnpackStringSize, strLen));
		}
		return EMPTY_STRING;
	}

	public Map<Object, Object> unpackMap(int count) throws IOException {
		try {
			// Constructor<Map> constructor =
			// asMapClass.getConstructor(int.class);
			// Map<Object, Object> map = constructor.newInstance(count);
			@SuppressWarnings("unchecked")
			Map<Object, Object> map = asMapClass.newInstance();
			for (int i = 0; i < count; i++) {
				Object key = unpack();
				Object value = unpack();
				map.put(key, value);
			}
			return map;
		} catch (InstantiationException e) {
			throw new MPackFormatException("Unpack map with asMapClass = " + asMapClass.getName(), e);
		} catch (IllegalAccessException e) {
			throw new MPackFormatException("Unpack map with asMapClass = " + asMapClass.getName(), e);
		}
	}

	public Object[] unpackArray() throws IOException {
		int arraySize = unpackArrayHeader();
		if (arraySize > 0) {
			Object[] array = new Object[arraySize];
			for (int i = 0; i < arraySize; i++) {
				array[i] = unpack();
			}
			return array;
		}
		return EMPTY_ARRAY;
	}

	public List<Object> unpackList() throws IOException {
		return Arrays.asList(unpackArray());
	}

	/**
	 * 当标志{@link #OPT_READ_BIN_AS_STR}被设置则这个会被调用
	 * 
	 * @param binary
	 * @return
	 */
	protected String binaryToString(byte[] binary) {
		return new String(binary);
	}

	/**
	 * 默认映射规则：NIL->null, BOOLEAN->Boolean, INTEGER->Number(UINT64->BigInteger,
	 * Other->Long), FLOAT->Double, STRING->String, BINARY->byte[],
	 * ARRAY->Object[], MAP->Object[], EXTENSION->
	 * 
	 * @param options
	 * @return
	 * @throws IOException
	 */
	public Object unpack() throws IOException {
		byte byteCode = getNextCode();// 先获取类型再调用其他unpack消耗
		byte bFamilyType = MPackFormat.valueOf(byteCode).getFamily();
		switch (bFamilyType) {
		case FamilyType.NIL:
			return unpackNil();
		case FamilyType.BOOLEAN:
			return unpackBoolean();
		case FamilyType.INTEGER:
			switch (byteCode) {
			case ByteCode.UINT64:
				return unpackBigInteger();
			default:
				return unpackLong();
			}
		case FamilyType.FLOAT:
			return unpackDouble();
		case FamilyType.STRING: {
			int length = unpackRawStringHeader();
			byte[] bs = readPayload(length);
			return (options & OPT_READ_STR_AS_BIN) > 0 ? bs : new String(bs);
		}
		case FamilyType.BINARY: {
			int length = unpackBinaryHeader();
			byte[] bs = readPayload(length);
			return (options & OPT_READ_BIN_AS_STR) > 0 ? binaryToString(bs) : bs;
		}
		case FamilyType.ARRAY: {
			return (options & OPT_READ_ARRAY_AS_LIST) > 0 ? unpackList() : unpackArray();
		}
		case FamilyType.MAP: {
			int size = unpackMapHeader();
			if (null != asMapClass) {
				return unpackMap(size);
			} else {
				Object[] kvs = new Object[size * 2];
				for (int i = 0; i < size * 2;) {
					kvs[i] = unpack();
					i++;
					kvs[i] = unpack();
					i++;
				}
				return kvs;
			}
		}
		case FamilyType.EXTENSION: {
			ExtensionTypeHeader extHeader = unpackExtensionTypeHeader();
			return new ExtensionValueImpl(extHeader.getType(), readPayload(extHeader.getLength()));
		}
		default:
			throw new MPackFormatException("Unknown format family type");
		}
	}

	/** maximum number of key-value associations of a Map object is (2^32)-1 */
	public int unpackMapHeader() throws IOException {
		byte b = readNextByte();
		if (ByteCode.isFixedMap(b)) { // fixmap
			return b & 0x0f;
		}
		switch (b) {
		case ByteCode.MAP16: // map 16
			return readUnsignedShort();
		case ByteCode.MAP32: // map 32
			return readNextLength32();
		}
		throw unexpected("Map", b);
	}

	/** maximum number of elements of an Array object is (2^32)-1 */
	public int unpackArrayHeader() throws IOException {
		byte b = readNextByte();
		if (ByteCode.isFixedArray(b)) { // fixarray
			return b & 0x0f;
		}
		switch (b) {
		case ByteCode.ARRAY16: // array 16
			return readUnsignedShort();
		case ByteCode.ARRAY32: // array 32
			return readNextLength32();
		}
		throw unexpected("Array", b);
	}

	/** maximum length of a Binary object is (2^32)-1 */
	public int unpackBinaryHeader() throws IOException {
		byte b = readNextByte();
		if (ByteCode.isFixedRaw(b)) { // FixRaw
			return b & 0x1f;
		}
		int len = readBinaryHeader(b);
		if (len >= 0) {
			return len;
		}
		throw unexpected("Binary", b);
	}

	/** maximum byte size of a String object is (2^32)-1 */
	public int unpackRawStringHeader() throws IOException {
		byte b = readNextByte();
		if (ByteCode.isFixedRaw(b)) { // FixRaw
			return b & 0x1f;
		}
		int len = readStringHeader(b);
		if (len >= 0) {
			return len;
		}
		throw unexpected("String", b);
	}

	public ExtensionTypeHeader unpackExtensionTypeHeader() throws IOException {
		byte b = readNextByte();
		switch (b) {
		case ByteCode.FIXEXT1:
			return new ExtensionTypeHeader(readNextByte(), 1);
		case ByteCode.FIXEXT2:
			return new ExtensionTypeHeader(readNextByte(), 2);
		case ByteCode.FIXEXT4:
			return new ExtensionTypeHeader(readNextByte(), 4);
		case ByteCode.FIXEXT8:
			return new ExtensionTypeHeader(readNextByte(), 8);
		case ByteCode.FIXEXT16:
			return new ExtensionTypeHeader(readNextByte(), 16);
		case ByteCode.EXT8: {
			int length = readUnsignedShort();
			byte type = readNextByte();
			return new ExtensionTypeHeader(type, length);
		}
		case ByteCode.EXT16: {
			int length = readUnsignedShort();
			byte type = readNextByte();
			return new ExtensionTypeHeader(type, length);
		}
		case ByteCode.EXT32: {
			int length = readNextLength32();
			byte type = readNextByte();
			return new ExtensionTypeHeader(type, length);
		}
		}

		throw unexpected("Ext", b);
	}

	// TODO returns a buffer reference to the payload (zero-copy)

	public void read(ByteBuffer dst) throws IOException {
		throw new UnsupportedOperationException("hasn't been implemented");
	}

	/** 由于未实现内部buffer，不存在引用 */
	public byte[] readAsReference(int length) throws IOException {
		byte[] ref = null;
		throw new UnsupportedOperationException("hasn't been implemented");
	}

	/**
	 * Read up to len bytes of data
	 * 
	 * @param len the number of bytes to read
	 * @return
	 * @throws EOFException
	 * @throws IOException
	 */
	public byte[] readPayload(int length) throws IOException {
		byte[] newArray = new byte[length];
		int nReadedBytes = read(newArray, 0, length);
		if (nReadedBytes < length) {
			throw new EOFException();
		}
		return newArray;
	}

	public void readPayload(byte[] buf) throws IOException {
		readPayload(buf, 0, buf.length);
	}

	public void readPayload(byte[] buf, int off, int len) throws IOException {
		if (super.read(buf, off, len) < 0)
			throw new EOFException();
	}

	/**
	 * Read the specified number of values to an {@link OutputStream}, then move
	 * the cursor at the end of the value
	 * 
	 * @param count the specified number of values to read
	 * @param out the <code>OutputStream</code> to write to
	 * @throws IOException
	 * @see {@link #readValue(int, OutputStream, byte[])}
	 */
	public MUnpacker readValue(int count, OutputStream out) throws IOException {
		byte[] tmpBuffer = new byte[8 * 1024];
		return readValue(count, out, tmpBuffer);
	}

	/**
	 * Read the specified number of values to an {@link OutputStream}, then move
	 * the cursor at the end of the value
	 * 
	 * @param count the specified number of values to read
	 * @param out the <code>OutputStream</code> to write to
	 * @param buffer the buffer to use for the copy
	 * @throws IOException
	 * @see {@link #skipValue(int)}
	 */
	public MUnpacker readValue(int count, OutputStream out, byte[] buffer) throws IOException {
		int remainingValues = count;
		while (remainingValues > 0) {
			int lengthSize = 0, payloadSize = 0;
			byte byteCode = readNextByte();
			MPackFormat format = MPackFormat.valueOf(byteCode);

			out.write(byteCode);// ByteCode

			switch (format) {
			case POSFIXINT:
			case NEGFIXINT:
			case BOOLEAN:
			case NIL:
				// the value is just included in the ByteCode.
				break;
			case FIXMAP: {
				int mapLen = byteCode & 0x0f;// 1000xxxx
				remainingValues += mapLen * 2;
				// the FIXMAP value length is just included in the ByteCode.
				break;
			}
			case FIXARRAY: {
				int arrayLen = byteCode & 0x0f;// 1001xxxx
				remainingValues += arrayLen;
				// the FIXARRAY value length is just included in the ByteCode.
				break;
			}
			case FIXSTR: {
				int strLen = byteCode & 0x1f; // 101xxxxx
				MPack.copyLarge(this, out, strLen, buffer);
				break;
			}
			case INT8:
			case UINT8:
				lengthSize = 1;
				consume(1);// read the value
				break;
			case INT16:
			case UINT16:
				lengthSize = 2;
				consume(2);// read the value
				break;
			case INT32:
			case UINT32:
			case FLOAT32:
				lengthSize = 4;
				consume(4);// read the value
				break;
			case INT64:
			case UINT64:
			case FLOAT64:
				lengthSize = 8;
				consume(8); // read the value
				break;
			case BIN8:
			case STR8:
				lengthSize = 1;
				payloadSize = readUnsignedByte();// read the length header
				break;
			case BIN16:
			case STR16:
				lengthSize = 2;
				payloadSize = readUnsignedShort();// read the length header
				break;
			case BIN32:
			case STR32:
				lengthSize = 4;
				payloadSize = readNextLength32();// read the length header
				break;
			case FIXEXT1:
				payloadSize = 2;
				break;
			case FIXEXT2:
				payloadSize = 3;
				break;
			case FIXEXT4:
				payloadSize = 5;
				break;
			case FIXEXT8:
				payloadSize = 9;
				break;
			case FIXEXT16:
				payloadSize = 17;
				break;
			case EXT8:
				lengthSize = 1;
				payloadSize = readUnsignedByte() + 1;
				break;
			case EXT16:
				lengthSize = 2;
				payloadSize = readUnsignedShort() + 1;
				break;
			case EXT32:
				lengthSize = 4;
				payloadSize = readNextLength32() + 1;
				break;
			case ARRAY16:
				lengthSize = 2;
				remainingValues += readUnsignedShort();
				break;
			case ARRAY32:
				lengthSize = 4;
				remainingValues += readNextLength32();
				break;
			case MAP16:
				lengthSize = 2;
				remainingValues += readUnsignedShort() * 2;
				break;
			case MAP32:
				lengthSize = 4;
				// TODO check int overflow
				remainingValues += readNextLength32() * 2;
				break;
			case NEVER_USED:
				throw new RuntimeException(String.format("unknown code: %02x is found", byteCode));
			}
			if (0 < lengthSize) {
				// copy the number object or the length header of other objects
				out.write(byteBuffer.array(), 0, lengthSize);
			}
			if (payloadSize > 0) {
				MPack.copyLarge(this, out, payloadSize, buffer);
			}
			remainingValues--;
		}
		return this;
	}

	/**
	 * Create an exception for the case when an unexpected byte value is read
	 * 
	 * @param expected
	 * @param byteCode {@link #getNextCode()}
	 * @return
	 * @throws MPackFormatException
	 */
	private static MPackFormatException unexpected(String expected, byte byteCode) throws MPackFormatException {
		Object[] args = new Object[] { expected, MPackFormat.valueOf(byteCode).toString(), byteCode };
		return new MPackFormatException(String.format("Expected %s, but got %s (the code type is %02x)", args));
	}

	/** java int 无法表示大于{@link Integer#MAX_VALUE}的无符号整数 */
	private static MPackIntegerOverflowException overflowU32(int u32) {
		return new MPackIntegerOverflowException(MPack.toUnsignedLong(u32));
	}

	/** java long 无法表示大于{@link Long#MAX_VALUE}的无符号整数 */
	private static MPackIntegerOverflowException overflowU64(long u64) {
		BigInteger bi = BigInteger.valueOf(u64 + Long.MAX_VALUE + 1L).setBit(63);
		return new MPackIntegerOverflowException(bi);
	}

	/**
	 * java int,long 无法表示小于{@link Integer#MIN_VALUE}，{@link Long#MIN_VALUE}
	 * 的有符号整数
	 */
	private static MPackIntegerOverflowException overflowI(long signedNumber) {
		return new MPackIntegerOverflowException(signedNumber);
	}

	private static MPackSizeException overflowU32Size(int u32) {
		return new MPackSizeException(MPack.toUnsignedLong(u32));
	}

	/** {@code new MUnpacker(new BufferedInputStream(in));} */
	public static MUnpacker bufferedMUnpacker(InputStream in) {
		return new MUnpacker(new BufferedInputStream(in));
	}

	/** {@code new MUnpacker(new BufferedInputStream(in));} */
	public static MUnpacker buffered(InputStream in) {
		return new MUnpacker(new BufferedInputStream(in));
	}
}
