package org.fang.msgpack;

/**
 * The prefix code set of MessagePack. See also https://github.com/msgpack/msgpack/blob/master/spec.md for details.
 */
public interface CodeType {

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

	/**format name: negative fixint,  first byte: 111xxxxx (in binary) / 0xe0 - 0xff (in hex)*/
	public static final byte NEGFIXINT = (byte) 0xe0;
	/**format name: positive fixint	,  first byte: 0xxxxxxx (in binary),  0x00 - 0x7f (in hex)*/
	public static final byte POSFIXINT = (byte) 0xe1;
	/**format name: fixmap, first byte: 1000xxxx(in binary), 0x80 - 0x8f (in hex)*/
	public static final byte FIXMAP = (byte) 0xe2;
	/**format name: fixarray, first byte: 1001xxxx(in binary), 0x90 - 0x9f (in hex)*/
	public static final byte FIXARRAY = (byte) 0xe3;
	/**format name: fixstr, first byte: 101xxxxx(in binary), 0xa0 - 0xbf (in hex)*/
	public static final byte FIXSTR = (byte) 0xe4;

	// public static final byte BOOLEAN = (byte) 0xe5;

	public static final byte FIRST = NIL;
	public static final byte LAST = FIXSTR;

	/**
	 *  Type system
	 * 
	 * <h1>Types</h1>
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
	 * <b>Extension</b> represents a tuple of type information and a byte array where type information is an integer whose meaning is
	 * defined by applications<br>
	 * </li>
	 * </ul>
	 * @author fangss
	 *
	 */
	public interface FamilyType {
		public static final byte UNDEFINED = 0;
		public static final byte NIL = 1;// 0001
		public static final byte BOOLEAN = 2;// 0010
		public static final byte INTEGER = 3;// 0011
		public static final byte FLOAT = 4;// 0100
		public static final byte STRING = 5;// 0101
		public static final byte BINARY = 6;// 0110
		public static final byte ARRAY = 7;// 0111
		public static final byte MAP = 8;// 1000
		public static final byte EXTENSION = 9;// 1001
	}
}
