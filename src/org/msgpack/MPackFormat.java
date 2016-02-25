package org.msgpack;

import java.util.Arrays;

import org.msgpack.ByteCode.FamilyType;

public enum MPackFormat {
	/**
	 * INT7
	 * 
	 * <pre>
	 * ---format name--|-first byte (in binary)-|--first byte (in hex)--
	 * positive fixint | 0xxxxxxx               | 0x00 - 0x7f (0 ~ 127)
	 * </pre>
	 */
	POSFIXINT(FamilyType.INTEGER),
	/**
	 * MAP4, format name: fixmap, first byte: 1000xxxx(in binary), 0x80 - 0x8f
	 * (in hex)
	 */
	FIXMAP(FamilyType.MAP),
	/** ARRAY4 */
	FIXARRAY(FamilyType.ARRAY),
	/** STR5 */
	FIXSTR(FamilyType.STRING),

	NIL(FamilyType.NIL),

	NEVER_USED(FamilyType.UNDEFINED),

	BOOLEAN(FamilyType.BOOLEAN),

	BIN8(FamilyType.BINARY),

	BIN16(FamilyType.BINARY),

	BIN32(FamilyType.BINARY),

	EXT8(FamilyType.EXTENSION),

	EXT16(FamilyType.EXTENSION),

	EXT32(FamilyType.EXTENSION),

	FLOAT32(FamilyType.FLOAT),

	FLOAT64(FamilyType.FLOAT),

	UINT8(FamilyType.INTEGER),

	UINT16(FamilyType.INTEGER),

	UINT32(FamilyType.INTEGER),

	UINT64(FamilyType.INTEGER),

	INT8(FamilyType.INTEGER),

	INT16(FamilyType.INTEGER),

	INT32(FamilyType.INTEGER),

	INT64(FamilyType.INTEGER),
	/** The fixed-length value type: 3(1+1+1) bytes */
	FIXEXT1(FamilyType.EXTENSION),
	/** The fixed-length value type: 4(1+1+2) bytes */
	FIXEXT2(FamilyType.EXTENSION),
	/** The fixed-length value type: 6(1+1+4) bytes */
	FIXEXT4(FamilyType.EXTENSION),
	/** The fixed-length value type: 10(1+1+8) bytes */
	FIXEXT8(FamilyType.EXTENSION),
	/** The fixed-length value type: 18(1+1+16) bytes */
	FIXEXT16(FamilyType.EXTENSION),

	STR8(FamilyType.STRING),

	STR16(FamilyType.STRING),

	STR32(FamilyType.STRING),

	ARRAY16(FamilyType.ARRAY),

	ARRAY32(FamilyType.ARRAY),

	MAP16(FamilyType.MAP),

	MAP32(FamilyType.MAP),
	/**
	 * <pre>
	 * ---format name--|-first byte (in binary)-|--first byte (in hex)--
	 * negative fixint | 111xxxxx               |	0xe0 - 0xff (-32 ~ -1)
	 * </pre>
	 */
	NEGFIXINT(FamilyType.INTEGER);

	private static final MPackFormat[] formatTable;

	private final byte family;

	MPackFormat(byte family) {
		this.family = family;
	}

	/**
	 * Retruns the family corresponding to this MPackFormat
	 * 
	 * @return family type
	 * @throws MPackFormatException if this == NEVER_USED type
	 */
	public byte getFamily() throws MPackFormatException {
		if (this == NEVER_USED) {
			throw new MPackFormatException("Cannot convert NEVER_USED to a valid object");
		}
		return family;
	}

	public boolean isNilType() {
		return this == NIL;
	}

	public boolean isBooleanType() {
		return this == BOOLEAN;
	}

	public boolean isIntegerType() {
		return FamilyType.INTEGER == family;
	}

	public boolean isFloatType() {
		return FamilyType.FLOAT == family;
	}

	public boolean isStringType() {
		return FamilyType.STRING == family;
	}

	public boolean isBinaryType() {
		return FamilyType.BINARY == family;
	}

	public boolean isArrayType() {
		return FamilyType.ARRAY == family;
	}

	public boolean isMapType() {
		return FamilyType.MAP == family;
	}

	public boolean isExtensionType() {
		return FamilyType.EXTENSION == family;
	}

	/**
	 * Returns a MPackFormat type of the specified byte value
	 * 
	 * @param b MPackFormat of the given byte
	 * @return
	 */
	public static MPackFormat valueOf(final byte b) {
		return formatTable[b & 0xFF];
	}

	// /**
	// * Converting a byte value into {@link MPackFormat} For faster
	// performance, use {@link #valueOf}
	// *
	// * @param b
	// * MPackFormat of the given byte
	// * @return
	// */
	// static MPackFormat toFormatType(final byte b) {
	// if (ByteCode.isPosFixInt(b)) {
	// return POSFIXINT;
	// }
	// if (ByteCode.isNegFixInt(b)) {
	// return NEGFIXINT;
	// }
	// if (ByteCode.isFixStr(b)) {
	// return FIXSTR;
	// }
	// if (ByteCode.isFixedArray(b)) {
	// return FIXARRAY;
	// }
	// if (ByteCode.isFixedMap(b)) {
	// return FIXMAP;
	// }
	// switch (b) {
	// case ByteCode.NIL:
	// return NIL;
	// case ByteCode.FALSE:
	// case ByteCode.TRUE:
	// return BOOLEAN;
	// case ByteCode.BIN8:
	// return BIN8;
	// case ByteCode.BIN16:
	// return BIN16;
	// case ByteCode.BIN32:
	// return BIN32;
	// case ByteCode.EXT8:
	// return EXT8;
	// case ByteCode.EXT16:
	// return EXT16;
	// case ByteCode.EXT32:
	// return EXT32;
	// case ByteCode.FLOAT32:
	// return FLOAT32;
	// case ByteCode.FLOAT64:
	// return FLOAT64;
	// case ByteCode.UINT8:
	// return UINT8;
	// case ByteCode.UINT16:
	// return UINT16;
	// case ByteCode.UINT32:
	// return UINT32;
	// case ByteCode.UINT64:
	// return UINT64;
	// case ByteCode.INT8:
	// return INT8;
	// case ByteCode.INT16:
	// return INT16;
	// case ByteCode.INT32:
	// return INT32;
	// case ByteCode.INT64:
	// return INT64;
	// case ByteCode.FIXEXT1:
	// return FIXEXT1;
	// case ByteCode.FIXEXT2:
	// return FIXEXT2;
	// case ByteCode.FIXEXT4:
	// return FIXEXT4;
	// case ByteCode.FIXEXT8:
	// return FIXEXT8;
	// case ByteCode.FIXEXT16:
	// return FIXEXT16;
	// case ByteCode.STR8:
	// return STR8;
	// case ByteCode.STR16:
	// return STR16;
	// case ByteCode.STR32:
	// return STR32;
	// case ByteCode.ARRAY16:
	// return ARRAY16;
	// case ByteCode.ARRAY32:
	// return ARRAY32;
	// case ByteCode.MAP16:
	// return MAP16;
	// case ByteCode.MAP32:
	// return MAP32;
	// default:
	// return NEVER_USED;
	// }
	// }

	/**
	 * generate a look up table for converting byte values into MPackFormat
	 * types
	 */
	static MPackFormat[] generateFormatTable() {
		MPackFormat[] fts = new MPackFormat[256];

		Arrays.fill(fts, 0xe0, 0x100, NEGFIXINT);
		Arrays.fill(fts, 0, 0x80, POSFIXINT);
		Arrays.fill(fts, 0x80, 0x90, FIXMAP);
		Arrays.fill(fts, 0x90, 0xa0, FIXARRAY);
		Arrays.fill(fts, 0xa0, 0xc0, FIXSTR);
		// nil 11000000 0xc0
		fts[0xc0] = NIL;
		// (never used) 11000001 0xc1
		fts[0xc1] = NEVER_USED;
		// false 11000010 0xc2
		fts[0xc2] = BOOLEAN;
		// true 11000011 0xc3
		fts[0xc3] = BOOLEAN;
		// bin 8 11000100 0xc4
		fts[0xc4] = BIN8;
		// bin 16 11000101 0xc5
		fts[0xc5] = BIN16;
		// bin 32 11000110 0xc6
		fts[0xc6] = BIN32;
		// ext 8 11000111 0xc7
		fts[0xc7] = EXT8;
		// ext 16 11001000 0xc8
		fts[0xc8] = EXT16;
		// ext 32 11001001 0xc9
		fts[0xc9] = EXT32;
		// float 32 11001010 0xca
		fts[0xca] = FLOAT32;
		// float 64 11001011 0xcb
		fts[0xcb] = FLOAT64;
		// uint 8 11001100 0xcc
		fts[0xcc] = UINT8;
		// uint 16 11001101 0xcd
		fts[0xcd] = UINT16;
		// uint 32 11001110 0xce
		fts[0xce] = UINT32;
		// uint 64 11001111 0xcf
		fts[0xcf] = UINT64;
		// int 8 11010000 0xd0
		fts[0xd0] = INT8;
		// int 16 11010001 0xd1
		fts[0xd1] = INT16;
		// int 32 11010010 0xd2
		fts[0xd2] = INT32;
		// int 64 11010011 0xd3
		fts[0xd3] = INT64;
		// fixext 1 11010100 0xd4
		fts[0xd4] = FIXEXT1;
		// fixext 2 11010101 0xd5
		fts[0xd5] = FIXEXT2;
		// fixext 4 11010110 0xd6
		fts[0xd6] = FIXEXT4;
		// fixext 8 11010111 0xd7
		fts[0xd7] = FIXEXT8;
		// fixext 16 11011000 0xd8
		fts[0xd8] = FIXEXT16;
		// str 8 11011001 0xd9
		fts[0xd9] = STR8;
		// str 16 11011010 0xda
		fts[0xda] = STR16;
		// str 32 11011011 0xdb
		fts[0xdb] = STR32;
		// array 16 11011100 0xdc
		fts[0xdc] = ARRAY16;
		// array 32 11011101 0xdd
		fts[0xdd] = ARRAY32;
		// map 16 11011110 0xde
		fts[0xde] = MAP16;
		// map 32 11011111 0xdf
		fts[0xdf] = MAP32;
		return fts;
	}

	static {
		// Preparing a look up table for converting byte values into MPackFormat
		// types
		// MPackFormat[] fts = new MPackFormat[256];
		// formatTable = fts;
		// for (int b = 0; b <= 0xFF; ++b) {
		// MPackFormat ft = toFormatType((byte) b);
		// fts[b] = ft;
		// }
		formatTable = generateFormatTable();
	}
}
