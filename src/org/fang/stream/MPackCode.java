package org.fang.stream;

import java.util.Arrays;

/**
 * The prefix code set of MessagePack. See also https://github.com/msgpack/msgpack/blob/master/spec.md for details.
 */
public final class MPackCode implements CodeType {

	/**低8位是code的细分类型，见{@link CodeType}，9-12位的4个比特位为一个字节表示code的家族类型，是粗分类型，见{@link FamilyType}*/
	private static final short[] CODE2TYPE;
	// private static final String[] CODETYPE2NAME;
	
	static {
		short[] codeTypes = new short[256];
		CODE2TYPE = codeTypes;
		Arrays.fill(codeTypes, 0xe0, 0x100, (short) ((0x00ff & NEGFIXINT) + (FamilyType.INTEGER << 8)));
		Arrays.fill(codeTypes, 0, 0x80, (short) ((0x00ff & POSFIXINT) + (FamilyType.INTEGER << 8)));
		Arrays.fill(codeTypes, 0x80, 0x90, (short) ((0x00ff & FIXMAP) + (FamilyType.MAP << 8)));
		Arrays.fill(codeTypes, 0x90, 0xa0, (short) ((0x00ff & FIXARRAY) + (FamilyType.ARRAY << 8)));
		Arrays.fill(codeTypes, 0xa0, 0xc0, (short) ((0x00ff & FIXSTR) + (FamilyType.STRING << 8)));
		// nil 11000000 0xc0
		codeTypes[0xc2] = (0x00ff & NIL) + (FamilyType.NIL << 8);
		// (never used) 11000001 0xc1
		codeTypes[0xc2] = (0x00ff & NEVER_USED) + (FamilyType.UNDEFINED << 8);
		// false 11000010 0xc2
		codeTypes[0xc2] = (0x00ff & FALSE) + (FamilyType.BOOLEAN << 8);
		// true 11000011 0xc3
		codeTypes[0xc3] = (0x00ff & TRUE) + (FamilyType.BOOLEAN << 8);
		// bin 8 11000100 0xc4
		codeTypes[0xc4] = (0x00ff & BIN8) + (FamilyType.BINARY << 8);
		// bin 16 11000101 0xc5
		codeTypes[0xc5] = (0x00ff & BIN16) + (FamilyType.BINARY << 8);
		// bin 32 11000110 0xc6
		codeTypes[0xc6] = (0x00ff & BIN32) + (FamilyType.BINARY << 8);
		// ext 8 11000111 0xc7
		codeTypes[0xc7] = (0x00ff & EXT8) + (FamilyType.EXTENSION << 8);
		// ext 16 11001000 0xc8
		codeTypes[0xc8] = (0x00ff & EXT16) + (FamilyType.EXTENSION << 8);
		// ext 32 11001001 0xc9
		codeTypes[0xc9] = (0x00ff & EXT32 + FamilyType.EXTENSION << 8);
		// float 32 11001010 0xca
		codeTypes[0xca] = (0x00ff & FLOAT32) + (FamilyType.FLOAT << 8);
		// float 64 11001011 0xcb
		codeTypes[0xcb] = (0x00ff & FLOAT64) + (FamilyType.FLOAT << 8);
		// uint 8 11001100 0xcc
		codeTypes[0xcc] = (0x00ff & UINT8) + (FamilyType.INTEGER << 8);
		// uint 16 11001101 0xcd
		codeTypes[0xcd] = (0x00ff & UINT16) + (FamilyType.INTEGER << 8);
		// uint 32 11001110 0xce
		codeTypes[0xce] = (0x00ff & UINT32) + (FamilyType.INTEGER << 8);
		// uint 64 11001111 0xcf
		codeTypes[0xcf] = (0x00ff & UINT64) + (FamilyType.INTEGER << 8);
		// int 8 11010000 0xd0
		codeTypes[0xd0] = (0x00ff & INT8) + (FamilyType.INTEGER << 8);
		// int 16 11010001 0xd1
		codeTypes[0xd1] = (0x00ff & INT16) + (FamilyType.INTEGER << 8);
		// int 32 11010010 0xd2
		codeTypes[0xd2] = (0x00ff & INT32) + (FamilyType.INTEGER << 8);
		// int 64 11010011 0xd3
		codeTypes[0xd3] = (0x00ff & INT64) + (FamilyType.INTEGER << 8);
		// fixext 1 11010100 0xd4
		codeTypes[0xd4] = (0x00ff & FIXEXT1) + (FamilyType.EXTENSION << 8);
		// fixext 2 11010101 0xd5
		codeTypes[0xd5] = (0x00ff & FIXEXT2) + (FamilyType.EXTENSION << 8);
		// fixext 4 11010110 0xd6
		codeTypes[0xd6] = (0x00ff & FIXEXT4) + (FamilyType.EXTENSION << 8);
		// fixext 8 11010111 0xd7
		codeTypes[0xd7] = (0x00ff & FIXEXT8) + (FamilyType.EXTENSION << 8);
		// fixext 16 11011000 0xd8
		codeTypes[0xd8] = (0x00ff & FIXEXT16) + (FamilyType.EXTENSION << 8);
		// str 8 11011001 0xd9
		codeTypes[0xd9] = (0x00ff & STR8) + (FamilyType.STRING << 8);
		// str 16 11011010 0xda
		codeTypes[0xda] = (0x00ff & STR16) + (FamilyType.STRING << 8);
		// str 32 11011011 0xdb
		codeTypes[0xdb] = (0x00ff & STR32) + (FamilyType.STRING << 8);
		// array 16 11011100 0xdc
		codeTypes[0xdc] = (0x00ff & ARRAY16) + (FamilyType.ARRAY << 8);
		// array 32 11011101 0xdd
		codeTypes[0xdd] = (0x00ff & ARRAY32) + (FamilyType.ARRAY << 8);
		// map 16 11011110 0xde
		codeTypes[0xde] = (0x00ff & MAP16) + (FamilyType.MAP << 8);
		// map 32 11011111 0xdf
		codeTypes[0xdf] = (0x00ff & MAP32) + (FamilyType.MAP << 8);

		// String[] codeType2Name = new String[LAST - FIRST + 1];
		// CODETYPE2NAME = codeType2Name;
		// codeType2Name[NIL - FIRST] = "NIL";
	}

	/**java中枚举类型实现比较好，有安全检查*/
	public static final byte getCodeType(byte code) {
		return (byte) CODE2TYPE[0xff & code];
	}

	/**java中枚举类型实现比较好，有安全检查和有意义，方便查看 {@link FamilyType}*/
	public static final byte getFamilyType(byte code) {
		return (byte) ((0x0f00 & CODE2TYPE[0xff & code]) >> 8);
	}

	/**java中枚举类型实现比较好，有安全检查*/
	public static final String getCodeTypeName(byte codeType) {
		throw new UnsupportedOperationException("java enum to implement");
		// return CODETYPE2NAME[codeType - FIRST];
	}

	/**
	 * <b>format name  | first byte (in binary) | first byte (in hex)</b><br>
	 * positive fixint  | 0xxxxxxx | 0x00 - 0x7f<br>
	 * negative fixint | 111xxxxx | 0xe0 - 0xff
	 * @param b
	 * @return
	 */
	public static final boolean isFixInt(byte b) {
		int v = b & 0xFF;
		return v <= 0x7f || v >= 0xe0;
	}

	/**
	 * format name: negative fixint,  first byte: 111xxxxx (in binary) / 0xe0 - 0xff (in hex)
	 * 
	 * @param b
	 * @return
	 */
	public static final boolean isNegFixInt(byte b) {
		return (b & NEGFIXINT_PREFIX) == NEGFIXINT_PREFIX;
	}

	/**
	 *  format name: positive fixint	,  first byte: 0xxxxxxx (in binary),  0x00 - 0x7f (in hex)
	 * @param b
	 * @return
	 */
	public static final boolean isPosFixInt(byte b) {
		return (b & POSFIXINT_MASK) == 0;
	}

	/**
	 * format name: fixmap, first byte: 1000xxxx(in binary), 0x80 - 0x8f (in hex)
	 * @param b
	 * @return
	 */
	public static final boolean isFixedMap(byte b) {
		return (b & (byte) 0xe0) == MPackCode.FIXMAP_PREFIX;
	}

	/**
	 * format name: fixarray, first byte: 1001xxxx(in binary), 0x90 - 0x9f (in hex)
	 * @param b
	 * @return
	 */
	public static final boolean isFixedArray(byte b) {
		return (b & (byte) 0xf0) == MPackCode.FIXARRAY_PREFIX;
	}

	/**
	 * format name: fixstr, first byte: 101xxxxx(in binary), 0xa0 - 0xbf (in hex)
	 * @param b
	 * @return
	 */
	public static final boolean isFixStr(byte b) {
		return (b & (byte) 0xe0) == MPackCode.FIXSTR_PREFIX;
	}

	/**
	 * format name: fixstr alias, first byte: 101xxxxx(in binary), 0xa0 - 0xbf (in hex)
	 * @param b
	 * @return
	 * @see #isFixStr(byte)
	 */
	public static final boolean isFixedRaw(byte b) {
		return (b & (byte) 0xe0) == MPackCode.FIXSTR_PREFIX;
	}

	public static final byte POSFIXINT_MASK = (byte) 0x80;

	public static final byte FIXMAP_PREFIX = (byte) 0x80;
	public static final byte FIXARRAY_PREFIX = (byte) 0x90;
	public static final byte FIXSTR_PREFIX = (byte) 0xa0;

	public static final byte NEGFIXINT_PREFIX = (byte) 0xe0;
}
