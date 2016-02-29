package org.misc.io;

import java.io.FilterOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.msgpack.ByteCode;

/**
 * @author fangss
 * 
 */
public class DigestMPacker extends FilterOutputStream {
	protected final MessageDigest digest;
	protected final ByteBuffer byteBuffer;

	public DigestMPacker() {
		super(VainOutputStream.NUL); // an OutputStream that writes to nowhere
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			// ALog.ignore("neverx", e);
			throw new Error(e);
		}
		byteBuffer = ByteBuffer.allocate(10);
	}

	@Override
	public void write(int b) {
		digest.update((byte) b);
	}

	@Override
	public void write(byte[] b) {
		digest.update(b);
	}

	@Override
	public void write(byte[] b, int off, int len) {
		digest.update(b, off, len);
	}

	public byte[] digest(Map<?, ?> map) {
		packMapHeader(map.size());
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object k = entry.getKey();
			Object v = entry.getValue();
			packNilOrByteArrayOrAsString(k).packNilOrByteArrayOrAsString(v);
		}
		return digest.digest();
	}

	public void writeByte(byte b) {
		write(b);
	}

	private void writeByteAndByte(byte b, byte v) {
		write(byteBuffer.put(0, b).put(1, v).array(), 0, 5);
	}

	private void writeByteAndShort(byte b, short v) {
		write(byteBuffer.put(0, b).putShort(1, v).array(), 0, 3);
	}

	private void writeByteAndInt(byte b, int v) {
		write(byteBuffer.put(0, b).putInt(1, v).array(), 0, 5);
	}

	private void writeByteAndFloat(byte b, float v) {
		write(byteBuffer.put(0, b).putFloat(1, v).array(), 0, 5);
	}

	private void writeByteAndDouble(byte b, double v) {
		write(byteBuffer.put(0, b).putDouble(1, v).array(), 0, 9);
	}

	private void writeByteAndLong(byte b, long v) {
		write(byteBuffer.put(0, b).putLong(1, v).array(), 0, 9);
	}

	// ext format family: code_of_ext + your_ext_data_length + your_ext_type

	private void writeByteIntegerByte(byte ext, int payloadLen, byte extType) {
		byteBuffer.put(0, ext).position(1);
		if (ByteCode.EXT8 == ext) {
			byteBuffer.put((byte) payloadLen);
		} else if (ByteCode.EXT16 == ext) {
			byteBuffer.putShort((short) payloadLen);
		} else if (ByteCode.EXT32 == ext) {
			byteBuffer.putInt(payloadLen);
		}
		write(byteBuffer.put(extType).array(), 0, byteBuffer.limit());
	}

	public DigestMPacker packNil() {
		writeByte(ByteCode.NIL);
		return this;
	}

	public DigestMPacker packString(String s) {
		if (s.length() > 0) {
			// TODO encoding error?
			byte[] bs;
			try {
				bs = s.getBytes("UTF-8");
				packRawStringHeader(bs.length);
				write(bs, 0, bs.length);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return this;
		}
		return packRawStringHeader(0);
	}

	public DigestMPacker packRawStringHeader(int len) {
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

	public DigestMPacker packBinaryHeader(int len) {
		if (len < (1 << 8)) {
			writeByteAndByte(ByteCode.BIN8, (byte) len);
		} else if (len < (1 << 16)) {
			writeByteAndShort(ByteCode.BIN16, (short) len);
		} else {
			writeByteAndInt(ByteCode.BIN32, len);
		}
		return this;
	}

	public DigestMPacker packMapHeader(int mapSize) {
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

	public DigestMPacker packNilOrByteArrayOrAsString(Object obj) {
		if (null == obj) {
			writeByte(ByteCode.NIL);
		} else if (obj instanceof byte[]) {
			packBinaryHeader(((byte[]) obj).length);
			write((byte[]) obj);
		} else {
			packString(obj.toString());
		}
		return this;
	}

	public DigestMPacker pack(Map<?, ?> map) {
		packMapHeader(map.size());
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object k = entry.getKey();
			Object v = entry.getValue();
			packNilOrByteArrayOrAsString(k).packNilOrByteArrayOrAsString(v);
		}
		return this;
	}

	@SuppressWarnings("resource")
	public static byte[] digestOnce(Map<?, ?> map) {
		DigestMPacker dg = new DigestMPacker();
		return dg.digest(map);
		// dg.close();
	}

	// @Override
	// public String toString() {
	// return super.out.toString();
	// }
}
