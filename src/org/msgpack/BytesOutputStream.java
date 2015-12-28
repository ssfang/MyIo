package org.msgpack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 添加了buf获取和输出的方法
 * 
 * @author fangss
 * 
 */
public class BytesOutputStream extends ByteArrayOutputStream {

	/**
	 * @see ByteArrayOutputStream#ByteArrayOutputStream()
	 */
	public BytesOutputStream() {
		super();
	}

	/**
	 * @see ByteArrayOutputStream#ByteArrayOutputStream(int)
	 */
	public BytesOutputStream(int size) {
		super(size);
	}

	/**
	 * 确保缓冲区容量足够而做重新copy
	 * 
	 * @param newcount
	 */
	private void ensureCapacity(int newcount) {
		if (newcount > buf.length) {
			byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
			System.arraycopy(buf, 0, newbuf, 0, count);
			buf = newbuf;
		}
	}

	/**
	 * 从外部流读入
	 * 
	 * @param in
	 * @throws IOException
	 */
	public final void write(InputStream in) throws IOException {
		do {
			int cap;
			int sz;
			do {
				cap = buf.length - count;
				sz = in.read(buf, count, cap);
				if (sz < 0)
					return;
				count += sz;
			} while (cap != sz);
			ensureCapacity(buf.length * 2);
		} while (true);
	}

	/**
	 * 返回内部字节数组缓冲区
	 * 
	 * @return The inneral byte array
	 */
	public byte[] buffer() {
		return buf;
	}

	/**
	 * Creates a new {@link #ByteBuffer} by wrapping the current inneral byte array buffer with the size of current written bytes.
	 * 
	 * @return the created byte buffer.
	 * @see ByteBuffer#wrap(byte[], int, int)
	 * @see #size()
	 */
	public ByteBuffer asByteBuffer() {
		return ByteBuffer.wrap(buf, 0, count);
	}

	/**
	 * Creates a new {@link #ByteArrayInputStream} on the current inneral byte array buffer with the size of current written bytes.
	 * 
	 * @return the created InputStream.
	 * @see ByteArrayInputStream
	 * @see #size()
	 */
	public InputStream newInputStream() {
		return new ByteArrayInputStream(buf, 0, count);
	}

	/**
	 * Creates a new {@link #ByteArrayInputStream} on the current inneral byte array buffer with the given offset and length.
	 * 
	 * @return the created InputStream.
	 * @see ByteArrayInputStream
	 * @see #size()
	 */
	public InputStream newInputStream(int start, int length) {
		return new ByteArrayInputStream(buf, start, length);
	}
}
