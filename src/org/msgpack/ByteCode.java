package org.msgpack;

/**
 * The prefix code set of MessagePack. See also
 * https://github.com/msgpack/msgpack/blob/master/spec.md for details.
 */
public class ByteCode {

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

	// Other Helper

	/** @see {@link #isPosFixInt(byte)} */
	public static final byte POSFIXINT_MASK = (byte) 0x80;

	/** @see {@link #isFixedMap(byte)} */
	public static final byte FIXMAP_PREFIX = (byte) 0x80;
	/** @see {@link #isFixedArray(byte)} */
	public static final byte FIXARRAY_PREFIX = (byte) 0x90;
	/** @see {@link #isFixStr(byte)} */
	public static final byte FIXSTR_PREFIX = (byte) 0xa0;

	/** @see {@link #isNegFixInt(byte)} */
	public static final byte NEGFIXINT_PREFIX = (byte) 0xe0;

	/**
	 * <b>format name | first byte (in binary) | first byte (in hex)</b><br>
	 * positive fixint | 0xxxxxxx | 0x00 - 0x7f<br>
	 * negative fixint | 111xxxxx | 0xe0 - 0xff
	 * 
	 * @param b
	 * @return
	 */
	public static final boolean isFixInt(byte b) {
		int v = b & 0xFF;
		return v <= 0x7f || v >= 0xe0;
	}

	/**
	 * format name: negative fixint, first byte: 111xxxxx (in binary) / 0xe0 -
	 * 0xff (in hex)
	 * 
	 * @param b
	 * @return
	 */
	public static final boolean isNegFixInt(byte b) {
		return (b & NEGFIXINT_PREFIX) == NEGFIXINT_PREFIX;
	}

	/**
	 * format name: positive fixint , first byte: 0xxxxxxx (in binary), 0x00 -
	 * 0x7f (in hex)
	 * 
	 * @param b
	 * @return
	 */
	public static final boolean isPosFixInt(byte b) {
		return (b & POSFIXINT_MASK) == 0;
	}

	/**
	 * format name: fixmap, first byte: 1000xxxx(in binary), 0x80 - 0x8f (in
	 * hex)
	 * 
	 * @param b
	 * @return
	 */
	public static final boolean isFixedMap(byte b) {
		return (b & (byte) 0xe0) == FIXMAP_PREFIX;
	}

	/**
	 * format name: fixarray, first byte: 1001xxxx(in binary), 0x90 - 0x9f (in
	 * hex)
	 * 
	 * @param b
	 * @return
	 */
	public static final boolean isFixedArray(byte b) {
		return (b & (byte) 0xf0) == FIXARRAY_PREFIX;
	}

	/**
	 * format name: fixstr, first byte: 101xxxxx(in binary), 0xa0 - 0xbf (in
	 * hex)
	 * 
	 * @param b
	 * @return
	 */
	public static final boolean isFixStr(byte b) {
		return (b & (byte) 0xe0) == FIXSTR_PREFIX;
	}

	/**
	 * format name: fixstr alias, first byte: 101xxxxx(in binary), 0xa0 - 0xbf
	 * (in hex)
	 * 
	 * @param b
	 * @return
	 * @see #isFixStr(byte)
	 */
	public static final boolean isFixedRaw(byte b) {
		return (b & (byte) 0xe0) == FIXSTR_PREFIX;
	}

	// /** 低8位是code的细分类型，见{@link
	// CodeType}，9-12位的4个比特位为一个字节表示code的家族类型，是粗分类型，见{@link FamilyType} */
	// private static final short[] CODE2TYPE;

	// private static final String[] CODETYPE2NAME;

	// /** java中枚举类型实现比较好，有安全检查 */
	// public static final byte getCodeType(byte code) {
	// return (byte) CODE2TYPE[0xff & code];
	// }
	//
	// /** java中枚举类型实现比较好，有安全检查和有意义，方便查看 {@link FamilyType} */
	// public static final byte getFamilyType(byte code) {
	// return (byte) ((0x0f00 & CODE2TYPE[0xff & code]) >> 8);
	// }
	//
	// /** java中枚举类型实现比较好，有安全检查 */
	// public static final String getCodeTypeName(byte codeType) {
	// // throw new UnsupportedOperationException("java enum to implement");
	// return CodeType.byteType2Name[codeType];
	// }

	// static {
	// short[] codeTypes = new short[256];
	// CODE2TYPE = codeTypes;
	// Arrays.fill(codeTypes, 0xe0, 0x100, (short) ((0x00ff &
	// CodeType.NEGFIXINT) + (FamilyType.INTEGER << 8)));
	// Arrays.fill(codeTypes, 0, 0x80, (short) ((0x00ff & CodeType.POSFIXINT) +
	// (FamilyType.INTEGER << 8)));
	// Arrays.fill(codeTypes, 0x80, 0x90, (short) ((0x00ff & CodeType.FIXMAP) +
	// (FamilyType.MAP << 8)));
	// Arrays.fill(codeTypes, 0x90, 0xa0, (short) ((0x00ff & CodeType.FIXARRAY)
	// + (FamilyType.ARRAY << 8)));
	// Arrays.fill(codeTypes, 0xa0, 0xc0, (short) ((0x00ff & CodeType.FIXSTR) +
	// (FamilyType.STRING << 8)));
	// // nil 11000000 0xc0
	// codeTypes[0xc0] = (0x00ff & CodeType.NIL) + (FamilyType.NIL << 8);
	// // (never used) 11000001 0xc1
	// codeTypes[0xc1] = (0x00ff & CodeType.NEVER_USED) + (FamilyType.UNDEFINED
	// << 8);
	// // false 11000010 0xc2
	// codeTypes[0xc2] = (0x00ff & CodeType.BOOLEAN) + (FamilyType.BOOLEAN <<
	// 8);
	// // true 11000011 0xc3
	// codeTypes[0xc3] = (0x00ff & CodeType.BOOLEAN) + (FamilyType.BOOLEAN <<
	// 8);
	// // bin 8 11000100 0xc4
	// codeTypes[0xc4] = (0x00ff & CodeType.BIN8) + (FamilyType.BINARY << 8);
	// // bin 16 11000101 0xc5
	// codeTypes[0xc5] = (0x00ff & CodeType.BIN16) + (FamilyType.BINARY << 8);
	// // bin 32 11000110 0xc6
	// codeTypes[0xc6] = (0x00ff & CodeType.BIN32) + (FamilyType.BINARY << 8);
	// // ext 8 11000111 0xc7
	// codeTypes[0xc7] = (0x00ff & CodeType.EXT8) + (FamilyType.EXTENSION << 8);
	// // ext 16 11001000 0xc8
	// codeTypes[0xc8] = (0x00ff & CodeType.EXT16) + (FamilyType.EXTENSION <<
	// 8);
	// // ext 32 11001001 0xc9
	// codeTypes[0xc9] = (0x00ff & CodeType.EXT32 + FamilyType.EXTENSION << 8);
	// // float 32 11001010 0xca
	// codeTypes[0xca] = (0x00ff & CodeType.FLOAT32) + (FamilyType.FLOAT << 8);
	// // float 64 11001011 0xcb
	// codeTypes[0xcb] = (0x00ff & CodeType.FLOAT64) + (FamilyType.FLOAT << 8);
	// // uint 8 11001100 0xcc
	// codeTypes[0xcc] = (0x00ff & CodeType.UINT8) + (FamilyType.INTEGER << 8);
	// // uint 16 11001101 0xcd
	// codeTypes[0xcd] = (0x00ff & CodeType.UINT16) + (FamilyType.INTEGER << 8);
	// // uint 32 11001110 0xce
	// codeTypes[0xce] = (0x00ff & CodeType.UINT32) + (FamilyType.INTEGER << 8);
	// // uint 64 11001111 0xcf
	// codeTypes[0xcf] = (0x00ff & CodeType.UINT64) + (FamilyType.INTEGER << 8);
	// // int 8 11010000 0xd0
	// codeTypes[0xd0] = (0x00ff & CodeType.INT8) + (FamilyType.INTEGER << 8);
	// // int 16 11010001 0xd1
	// codeTypes[0xd1] = (0x00ff & CodeType.INT16) + (FamilyType.INTEGER << 8);
	// // int 32 11010010 0xd2
	// codeTypes[0xd2] = (0x00ff & CodeType.INT32) + (FamilyType.INTEGER << 8);
	// // int 64 11010011 0xd3
	// codeTypes[0xd3] = (0x00ff & CodeType.INT64) + (FamilyType.INTEGER << 8);
	// // fixext 1 11010100 0xd4
	// codeTypes[0xd4] = (0x00ff & CodeType.FIXEXT1) + (FamilyType.EXTENSION <<
	// 8);
	// // fixext 2 11010101 0xd5
	// codeTypes[0xd5] = (0x00ff & CodeType.FIXEXT2) + (FamilyType.EXTENSION <<
	// 8);
	// // fixext 4 11010110 0xd6
	// codeTypes[0xd6] = (0x00ff & CodeType.FIXEXT4) + (FamilyType.EXTENSION <<
	// 8);
	// // fixext 8 11010111 0xd7
	// codeTypes[0xd7] = (0x00ff & CodeType.FIXEXT8) + (FamilyType.EXTENSION <<
	// 8);
	// // fixext 16 11011000 0xd8
	// codeTypes[0xd8] = (0x00ff & CodeType.FIXEXT16) + (FamilyType.EXTENSION <<
	// 8);
	// // str 8 11011001 0xd9
	// codeTypes[0xd9] = (0x00ff & CodeType.STR8) + (FamilyType.STRING << 8);
	// // str 16 11011010 0xda
	// codeTypes[0xda] = (0x00ff & CodeType.STR16) + (FamilyType.STRING << 8);
	// // str 32 11011011 0xdb
	// codeTypes[0xdb] = (0x00ff & CodeType.STR32) + (FamilyType.STRING << 8);
	// // array 16 11011100 0xdc
	// codeTypes[0xdc] = (0x00ff & CodeType.ARRAY16) + (FamilyType.ARRAY << 8);
	// // array 32 11011101 0xdd
	// codeTypes[0xdd] = (0x00ff & CodeType.ARRAY32) + (FamilyType.ARRAY << 8);
	// // map 16 11011110 0xde
	// codeTypes[0xde] = (0x00ff & CodeType.MAP16) + (FamilyType.MAP << 8);
	// // map 32 11011111 0xdf
	// codeTypes[0xdf] = (0x00ff & CodeType.MAP32) + (FamilyType.MAP << 8);
	// }

	// public interface CodeType {
	// // public static final byte NIL = (byte) 0xc0;
	// // public static final byte NEVER_USED = (byte) 0xc1;
	// // public static final byte BIN8 = (byte) 0xc4;
	// // public static final byte BIN16 = (byte) 0xc5;
	// // public static final byte BIN32 = (byte) 0xc6;
	// // public static final byte EXT8 = (byte) 0xc7;
	// // public static final byte EXT16 = (byte) 0xc8;
	// // public static final byte EXT32 = (byte) 0xc9;
	// // public static final byte FLOAT32 = (byte) 0xca;
	// // public static final byte FLOAT64 = (byte) 0xcb;
	// // public static final byte UINT8 = (byte) 0xcc;
	// // public static final byte UINT16 = (byte) 0xcd;
	// // public static final byte UINT32 = (byte) 0xce;
	// // public static final byte UINT64 = (byte) 0xcf;
	// //
	// // public static final byte INT8 = (byte) 0xd0;
	// // public static final byte INT16 = (byte) 0xd1;
	// // public static final byte INT32 = (byte) 0xd2;
	// // public static final byte INT64 = (byte) 0xd3;
	// //
	// // public static final byte FIXEXT1 = (byte) 0xd4;
	// // public static final byte FIXEXT2 = (byte) 0xd5;
	// // public static final byte FIXEXT4 = (byte) 0xd6;
	// // public static final byte FIXEXT8 = (byte) 0xd7;
	// // public static final byte FIXEXT16 = (byte) 0xd8;
	// //
	// // public static final byte STR8 = (byte) 0xd9;
	// // public static final byte STR16 = (byte) 0xda;
	// // public static final byte STR32 = (byte) 0xdb;
	// //
	// // public static final byte ARRAY16 = (byte) 0xdc;
	// // public static final byte ARRAY32 = (byte) 0xdd;
	// //
	// // public static final byte MAP16 = (byte) 0xde;
	// // public static final byte MAP32 = (byte) 0xdf;
	// //
	// // /** format name: negative fixint, first byte: 111xxxxx (in binary) /
	// 0xe0 - 0xff (in hex) */
	// // public static final byte NEGFIXINT = (byte) 0xe0;
	// // /** format name: positive fixint , first byte: 0xxxxxxx (in binary),
	// 0x00 - 0x7f (in hex) */
	// // public static final byte POSFIXINT = (byte) 0xe1;
	// // /** format name: fixmap, first byte: 1000xxxx(in binary), 0x80 - 0x8f
	// (in hex) */
	// // public static final byte FIXMAP = (byte) 0xe2;
	// // /** format name: fixarray, first byte: 1001xxxx(in binary), 0x90 -
	// 0x9f (in hex) */
	// // public static final byte FIXARRAY = (byte) 0xe3;
	// // /** format name: fixstr, first byte: 101xxxxx(in binary), 0xa0 - 0xbf
	// (in hex) */
	// // public static final byte FIXSTR = (byte) 0xe4;
	// //
	// // public static final byte BOOLEAN = (byte) 0xe5;
	//
	// /** INT7 format name: positive fixint , first byte: 0xxxxxxx (in binary),
	// 0x00 - 0x7f (in hex) */
	// public static final byte POSFIXINT = 0;
	// /** MAP4, format name: fixmap, first byte: 1000xxxx(in binary), 0x80 -
	// 0x8f (in hex) */
	// public static final byte FIXMAP = 1;
	// /** ARRAY4 format name: fixarray, first byte: 1001xxxx(in binary), 0x90 -
	// 0x9f (in hex) */
	// public static final byte FIXARRAY = 2;
	// /** STR5 format name: fixstr, first byte: 101xxxxx(in binary), 0xa0 -
	// 0xbf (in hex) */
	// public static final byte FIXSTR = 3;
	// public static final byte NIL = 4;
	// public static final byte NEVER_USED = 5;
	// public static final byte BOOLEAN = 6;
	// public static final byte BIN8 = 7;
	// public static final byte BIN16 = 8;
	// public static final byte BIN32 = 9;
	// public static final byte EXT8 = 10;
	// public static final byte EXT16 = 11;
	// public static final byte EXT32 = 12;
	// public static final byte FLOAT32 = 13;
	// public static final byte FLOAT64 = 14;
	// public static final byte UINT8 = 15;
	// public static final byte UINT16 = 16;
	// public static final byte UINT32 = 17;
	// public static final byte UINT64 = 18;
	// public static final byte INT8 = 19;
	// public static final byte INT16 = 20;
	// public static final byte INT32 = 21;
	// public static final byte INT64 = 22;
	// public static final byte FIXEXT1 = 23;
	// public static final byte FIXEXT2 = 24;
	// public static final byte FIXEXT4 = 25;
	// public static final byte FIXEXT8 = 26;
	// public static final byte FIXEXT16 = 27;
	// public static final byte STR8 = 28;
	// public static final byte STR16 = 29;
	// public static final byte STR32 = 30;
	// public static final byte ARRAY16 = 31;
	// public static final byte ARRAY32 = 32;
	// public static final byte MAP16 = 33;
	// public static final byte MAP32 = 34;
	// /** format name: negative fixint, first byte: 111xxxxx (in binary) / 0xe0
	// - 0xff (in hex) */
	// public static final byte NEGFIXINT = 35;
	//
	// // 36
	// static final String[] byteType2Name = new String[] { "POSFIXINT",
	// "FIXMAP", "FIXARRAY", "FIXSTR", "NIL",
	// "NEVER_USED",
	// "BOOLEAN", "BIN8", "BIN16", "BIN32", "EXT8", "EXT16", "EXT32", "FLOAT32",
	// "FLOAT64", "UINT8", "UINT16",
	// "UINT32",
	// "UINT64", "INT8", "INT16", "INT32", "INT64", "FIXEXT1", "FIXEXT2",
	// "FIXEXT4", "FIXEXT8", "FIXEXT16",
	// "STR8", "STR16",
	// "STR32", "ARRAY16", "ARRAY32", "MAP16", "MAP32", "NEGFIXINT" };
	// }

	/**
	 * <h1>MessagePack specification</h1>
	 * 
	 * <h2>Type system</h2>
	 * 
	 * <h3>Types</h3>
	 * <ul>
	 * <li>
	 * <b>Integer</b> represents an integer</li>
	 * <li>
	 * <b>Nil</b> represents nil</li>
	 * <li>
	 * <b>Boolean</b> represents true or false</li>
	 * <li>
	 * <b>Float</b> represents a floating point number</li>
	 * <li>
	 * <b>Raw String</b> extending Raw type represents a UTF-8 string</li>
	 * <li><b>Raw Binary</b> extending Raw type represents a byte array</li>
	 * <li><b>Array</b> represents a sequence of objects</li>
	 * <li>
	 * <b>Map</b> represents key-value pairs of objects</li>
	 * <li>
	 * <b>Extension</b> represents a tuple of type information and a byte array
	 * where type information is an integer whose meaning is defined by
	 * applications<br>
	 * </li>
	 * </ul>
	 * 
	 * <h2>Limitation</h2>
	 * 
	 * <ul>
	 * <li>a value of an Integer object is limited from <code>-(2^63)</code>
	 * upto <code>(2^64)-1</code></li>
	 * <li>maximum length of a Binary object is <code>(2^32)-1</code></li>
	 * <li>maximum byte size of a String object is <code>(2^32)-1</code></li>
	 * <li>String objects may contain invalid byte sequence and the behavior of
	 * a deserializer depends on the actual implementation when it received
	 * invalid byte sequence
	 * 
	 * <ul>
	 * <li>Deserializers should provide functionality to get the original byte
	 * array so that applications can decide how to handle the object</li>
	 * </ul>
	 * </li>
	 * <li>maximum number of elements of an Array object is
	 * <code>(2^32)-1</code></li>
	 * <li>maximum number of key-value associations of a Map object is
	 * <code>(2^32)-1</code></li>
	 * </ul>
	 * 
	 * <h2>Extension type</h2>
	 * 
	 * MessagePack allows applications to define application-specific types
	 * using the Extension type. Extension type consists of an integer and a
	 * byte array where the integer represents a kind of types and the byte
	 * array represents data.
	 * <p>
	 * Applications can assign <code>0</code> to <code>127</code> to store
	 * application-specific type information.
	 * <p>
	 * MessagePack reserves <code>-1</code> to <code>-128</code> for future
	 * extension to add predefined types which will be described in separated
	 * documents.
	 * <p>
	 * 
	 * <code>[0, 127]: application-specific types </code><br>
	 * <code>[-128, -1]: reserved for predefined types</code>
	 * <p>
	 * 
	 * <h2>Notation in diagrams</h2>
	 * 
	 * <pre>
	 * <code>one byte:
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
	 * </code>
	 * </pre>
	 * <p>
	 * <code>X</code>, <code>Y</code>, <code>Z</code> and <code>A</code> are the
	 * symbols that will be replaced by an actual bit.
	 * </p>
	 * 
	 * @author fangss
	 * 
	 */
	public interface FamilyType {
		/** {@link ByteCode#NEVER_USED} */
		public static final byte UNDEFINED = 0;
		/**
		 * <h2>nil format</h2>
		 * 
		 * Nil format stores nil in 1 byte.
		 * 
		 * <pre>
		 * <code>nil:
		 * +--------+
		 * |  0xc0  |
		 * +--------+
		 * </code>
		 * </pre>
		 * 
		 * {@link ByteCode#NIL}
		 */
		public static final byte NIL = 1;// 0001
		/**
		 * <h2>bool format family</h2>
		 * 
		 * Bool format family stores false or true in 1 byte.
		 * 
		 * <pre>
		 * <code>false:
		 * +--------+
		 * |  0xc2  |
		 * +--------+
		 * 
		 * true:
		 * +--------+
		 * |  0xc3  |
		 * +--------+
		 * </code>
		 * </pre>
		 * 
		 * {@link ByteCode#FALSE} , {@link ByteCode#TRUE}
		 */
		public static final byte BOOLEAN = 2;// 0010
		/**
		 * <h2>int format family</h2>
		 * 
		 * Int format family stores an integer in 1, 2, 3, 5, or 9 bytes.
		 * 
		 * <pre>
		 * <code>positive fixnum stores 7-bit positive integer
		 * +--------+
		 * |0XXXXXXX|
		 * +--------+
		 * 
		 * negative fixnum stores 5-bit negative integer
		 * +--------+
		 * |111YYYYY|
		 * +--------+
		 * 
		 * 0XXXXXXX is 8-bit unsigned integer
		 * 111YYYYY is 8-bit signed integer
		 * 
		 * uint 8 stores a 8-bit unsigned integer
		 * +--------+--------+
		 * |  0xcc  |ZZZZZZZZ|
		 * +--------+--------+
		 * 
		 * uint 16 stores a 16-bit big-endian unsigned integer
		 * +--------+--------+--------+
		 * |  0xcd  |ZZZZZZZZ|ZZZZZZZZ|
		 * +--------+--------+--------+
		 * 
		 * uint 32 stores a 32-bit big-endian unsigned integer
		 * +--------+--------+--------+--------+--------+
		 * |  0xce  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|
		 * +--------+--------+--------+--------+--------+
		 * 
		 * uint 64 stores a 64-bit big-endian unsigned integer
		 * +--------+--------+--------+--------+--------+--------+--------+--------+--------+
		 * |  0xcf  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|
		 * +--------+--------+--------+--------+--------+--------+--------+--------+--------+
		 * 
		 * int 8 stores a 8-bit signed integer
		 * +--------+--------+
		 * |  0xd0  |ZZZZZZZZ|
		 * +--------+--------+
		 * 
		 * int 16 stores a 16-bit big-endian signed integer
		 * +--------+--------+--------+
		 * |  0xd1  |ZZZZZZZZ|ZZZZZZZZ|
		 * +--------+--------+--------+
		 * 
		 * int 32 stores a 32-bit big-endian signed integer
		 * +--------+--------+--------+--------+--------+
		 * |  0xd2  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|
		 * +--------+--------+--------+--------+--------+
		 * 
		 * int 64 stores a 64-bit big-endian signed integer
		 * +--------+--------+--------+--------+--------+--------+--------+--------+--------+
		 * |  0xd3  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|
		 * +--------+--------+--------+--------+--------+--------+--------+--------+--------+
		 * </code>
		 * </pre>
		 * 
		 * {@link ByteCode#INT8}, {@link ByteCode#INT16}, {@link ByteCode#INT32}
		 * , {@link ByteCode#INT64}, {@link ByteCode#UINT8},
		 * {@link ByteCode#UINT16}, {@link ByteCode#UINT32},
		 * {@link ByteCode#UINT64}, {@link ByteCode#NEGFIXINT_PREFIX},
		 * {@link ByteCode#POSFIXINT_MASK}
		 * */
		public static final byte INTEGER = 3;// 0011
		/**
		 * <h2>float format family</h2>
		 * 
		 * Float format family stores a floating point number in 5 bytes or 9
		 * bytes.
		 * 
		 * <pre>
		 * <code>float 32 stores a floating point number in IEEE 754 single precision floating point number format:
		 * +--------+--------+--------+--------+--------+
		 * |  0xca  |XXXXXXXX|XXXXXXXX|XXXXXXXX|XXXXXXXX|
		 * +--------+--------+--------+--------+--------+
		 * 
		 * float 64 stores a floating point number in IEEE 754 double precision floating point number format:
		 * +--------+--------+--------+--------+--------+--------+--------+--------+--------+
		 * |  0xcb  |YYYYYYYY|YYYYYYYY|YYYYYYYY|YYYYYYYY|YYYYYYYY|YYYYYYYY|YYYYYYYY|YYYYYYYY|
		 * +--------+--------+--------+--------+--------+--------+--------+--------+--------+
		 * 
		 * where
		 * XXXXXXXX_XXXXXXXX_XXXXXXXX_XXXXXXXX is a big-endian IEEE 754 single precision floating point number.
		 *   Extension of precision from single-precision to double-precision does not lose precision.
		 * YYYYYYYY_YYYYYYYY_YYYYYYYY_YYYYYYYY_YYYYYYYY_YYYYYYYY_YYYYYYYY_YYYYYYYY is a big-endian
		 *   IEEE 754 double precision floating point number
		 * </code>
		 * </pre>
		 * 
		 * {@link ByteCode#FLOAT32} , {@link ByteCode#FLOAT64}
		 * */
		public static final byte FLOAT = 4;// 0100
		/**
		 * <h2>str format family</h2>
		 * 
		 * Str format family stores a byte array in 1, 2, 3, or 5 bytes of extra
		 * bytes in addition to the size of the byte array.
		 * 
		 * <pre>
		 * <code>fixstr stores a byte array whose length is upto 31 bytes:
		 * +--------+========+
		 * |101XXXXX|  data  |
		 * +--------+========+
		 * 
		 * str 8 stores a byte array whose length is upto (2^8)-1 bytes:
		 * +--------+--------+========+
		 * |  0xd9  |YYYYYYYY|  data  |
		 * +--------+--------+========+
		 * 
		 * str 16 stores a byte array whose length is upto (2^16)-1 bytes:
		 * +--------+--------+--------+========+
		 * |  0xda  |ZZZZZZZZ|ZZZZZZZZ|  data  |
		 * +--------+--------+--------+========+
		 * 
		 * str 32 stores a byte array whose length is upto (2^32)-1 bytes:
		 * +--------+--------+--------+--------+--------+========+
		 * |  0xdb  |AAAAAAAA|AAAAAAAA|AAAAAAAA|AAAAAAAA|  data  |
		 * +--------+--------+--------+--------+--------+========+
		 * 
		 * where
		 * XXXXX is a 5-bit unsigned integer which represents N
		 * YYYYYYYY is a 8-bit unsigned integer which represents N
		 * ZZZZZZZZ_ZZZZZZZZ is a 16-bit big-endian unsigned integer which represents N
		 * AAAAAAAA_AAAAAAAA_AAAAAAAA_AAAAAAAA is a 32-bit big-endian unsigned integer which represents N
		 * N is the length of data
		 * </code>
		 * </pre>
		 * 
		 * {@link ByteCode#FIXSTR_PREFIX}, {@link ByteCode#STR16} ,
		 * {@link ByteCode#STR32}
		 */
		public static final byte STRING = 5;// 0101
		/**
		 * <h2>bin format family</h2>
		 * 
		 * Bin format family stores an byte array in 2, 3, or 5 bytes of extra
		 * bytes in addition to the size of the byte array.
		 * 
		 * <pre>
		 * <code>bin 8 stores a byte array whose length is upto (2^8)-1 bytes:
		 * +--------+--------+========+
		 * |  0xc4  |XXXXXXXX|  data  |
		 * +--------+--------+========+
		 * 
		 * bin 16 stores a byte array whose length is upto (2^16)-1 bytes:
		 * +--------+--------+--------+========+
		 * |  0xc5  |YYYYYYYY|YYYYYYYY|  data  |
		 * +--------+--------+--------+========+
		 * 
		 * bin 32 stores a byte array whose length is upto (2^32)-1 bytes:
		 * +--------+--------+--------+--------+--------+========+
		 * |  0xc6  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|  data  |
		 * +--------+--------+--------+--------+--------+========+
		 * 
		 * where
		 * XXXXXXXX is a 8-bit unsigned integer which represents N
		 * YYYYYYYY_YYYYYYYY is a 16-bit big-endian unsigned integer which represents N
		 * ZZZZZZZZ_ZZZZZZZZ_ZZZZZZZZ_ZZZZZZZZ is a 32-bit big-endian unsigned integer which represents N
		 * N is the length of data
		 * </code>
		 * </pre>
		 * 
		 * {@link ByteCode#BIN8}, {@link ByteCode#BIN16} ,
		 * {@link ByteCode#BIN32}
		 * */
		public static final byte BINARY = 6;// 0110
		/**
		 * <h2>array format family</h2>
		 * 
		 * Array format family stores a sequence of elements in 1, 3, or 5 bytes
		 * of extra bytes in addition to the elements.
		 * 
		 * <pre>
		 * <code>fixarray stores an array whose length is upto 15 elements:
		 * +--------+~~~~~~~~~~~~~~~~~+
		 * |1001XXXX|    N objects    |
		 * +--------+~~~~~~~~~~~~~~~~~+
		 * 
		 * array 16 stores an array whose length is upto (2^16)-1 elements:
		 * +--------+--------+--------+~~~~~~~~~~~~~~~~~+
		 * |  0xdc  |YYYYYYYY|YYYYYYYY|    N objects    |
		 * +--------+--------+--------+~~~~~~~~~~~~~~~~~+
		 * 
		 * array 32 stores an array whose length is upto (2^32)-1 elements:
		 * +--------+--------+--------+--------+--------+~~~~~~~~~~~~~~~~~+
		 * |  0xdd  |ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|ZZZZZZZZ|    N objects    |
		 * +--------+--------+--------+--------+--------+~~~~~~~~~~~~~~~~~+
		 * 
		 * where
		 * XXXX is a 4-bit unsigned integer which represents N
		 * YYYYYYYY_YYYYYYYY is a 16-bit big-endian unsigned integer which represents N
		 * ZZZZZZZZ_ZZZZZZZZ_ZZZZZZZZ_ZZZZZZZZ is a 32-bit big-endian unsigned integer which represents N
		 *     N is the size of a array
		 * </code>
		 * </pre>
		 * 
		 * {@link ByteCode#FIXARRAY_PREFIX}, {@link ByteCode#ARRAY16} ,
		 * {@link ByteCode#ARRAY32}
		 */
		public static final byte ARRAY = 7;// 0111
		/**
		 * <h2>map format family</h2>
		 * 
		 * Map format family stores a sequence of key-value pairs in 1, 3, or 5
		 * bytes of extra bytes in addition to the key-value pairs.
		 * 
		 * <pre>
		 * <code>fixmap stores a map whose length is upto 15 elements
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
		 * </code>
		 * </pre>
		 * 
		 * {@link ByteCode#FIXMAP_PREFIX}, {@link ByteCode#MAP16} ,
		 * {@link ByteCode#MAP32}
		 */
		public static final byte MAP = 8;// 1000
		/**
		 * <h2>ext format family</h2>
		 * 
		 * Ext format family stores a tuple of an integer and a byte array.
		 * 
		 * <pre>
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
		 * {@link ByteCode#FIXEXT1}, {@link ByteCode#FIXEXT2},
		 * {@link ByteCode#FIXEXT4}, {@link ByteCode#FIXEXT8},
		 * {@link ByteCode#FIXEXT16}
		 * */
		public static final byte EXTENSION = 9;// 1001
	}
}
