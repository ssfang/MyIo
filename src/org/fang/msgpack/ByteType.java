package org.fang.msgpack;

public interface ByteType {
	/** INT7 */
	public static final byte POSFIXINT = 0;
	/** MAP4, format name: fixmap, first byte: 1000xxxx(in binary), 0x80 - 0x8f (in hex) */
	public static final byte FIXMAP = 1;
	/** ARRAY4 */
	public static final byte FIXARRAY = 2;
	/** STR5 */
	public static final byte FIXSTR = 3;
	public static final byte NIL = 4;
	public static final byte NEVER_USED = 5;
	public static final byte BOOLEAN = 6;
	public static final byte BIN8 = 7;
	public static final byte BIN16 = 8;
	public static final byte BIN32 = 9;
	public static final byte EXT8 = 10;
	public static final byte EXT16 = 11;
	public static final byte EXT32 = 12;
	public static final byte FLOAT32 = 13;
	public static final byte FLOAT64 = 14;
	public static final byte UINT8 = 15;
	public static final byte UINT16 = 16;
	public static final byte UINT32 = 17;
	public static final byte UINT64 = 18;
	public static final byte INT8 = 19;
	public static final byte INT16 = 20;
	public static final byte INT32 = 21;
	public static final byte INT64 = 22;
	public static final byte FIXEXT1 = 23;
	public static final byte FIXEXT2 = 24;
	public static final byte FIXEXT4 = 25;
	public static final byte FIXEXT8 = 26;
	public static final byte FIXEXT16 = 27;
	public static final byte STR8 = 28;
	public static final byte STR16 = 29;
	public static final byte STR32 = 30;
	public static final byte ARRAY16 = 31;
	public static final byte ARRAY32 = 32;
	public static final byte MAP16 = 33;
	public static final byte MAP32 = 34;
	/** format name: negative fixint, first byte: 111xxxxx (in binary) / 0xe0 - 0xff (in hex) */
	public static final byte NEGFIXINT = 35;

	// 36
	static final String[] byteType2Name = new String[] { "POSFIXINT", "FIXMAP", "FIXARRAY", "FIXSTR", "NIL", "NEVER_USED",
			"BOOLEAN", "BIN8", "BIN16", "BIN32", "EXT8", "EXT16", "EXT32", "FLOAT32", "FLOAT64", "UINT8", "UINT16", "UINT32",
			"UINT64", "INT8", "INT16", "INT32", "INT64", "FIXEXT1", "FIXEXT2", "FIXEXT4", "FIXEXT8", "FIXEXT16", "STR8", "STR16",
			"STR32", "ARRAY16", "ARRAY32", "MAP16", "MAP32", "NEGFIXINT" };
}
