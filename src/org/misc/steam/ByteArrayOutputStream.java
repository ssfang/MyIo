package org.misc.steam;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Very similar to the java.io.ByteArrayOutputStream but this version is not
 * thread safe and the resulting data is returned contain the internal buffer to
 * avoid an extra byte[] allocation.
 * 
 * @see java.io.ByteArrayOutputStream
 */
public class ByteArrayOutputStream extends java.io.ByteArrayOutputStream implements OutputStreamI {

	/**
	 * The buffer where data is stored.
	 */
	protected byte buf[];

	/**
	 * The number of valid bytes in the buffer.
	 */
	protected int count;

	/**
	 * Creates a new byte array output stream. The buffer capacity is initially
	 * 32 bytes, though its size increases if necessary.
	 */
	public ByteArrayOutputStream() {
		this(64);
	}

	/**
	 * Creates a new byte array output stream, with a buffer capacity of the
	 * specified size, in bytes.
	 * 
	 * @param size the initial size.
	 * @exception IllegalArgumentException if size is negative.
	 */
	public ByteArrayOutputStream(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("Negative initial size: " + size);
		}
		buf = new byte[size];
	}

	/**
	 * Ensures the the buffer has at least the minimumCapacity specified.
	 * 
	 * @param i
	 */
	private void checkCapacity(int minimumCapacity) {
		if (minimumCapacity > buf.length) {
			byte b[] = new byte[Math.max(buf.length << 1, minimumCapacity)];
			System.arraycopy(buf, 0, b, 0, count);
			buf = b;
		}
	}

	@Override
	public void reset() {
		count = 0;
	}

	@Override
	public int size() {
		return count;
	}

	@Override
	public void write(int b) {
		int newsize = count + 1;
		checkCapacity(newsize);
		buf[count] = (byte) b;
		count = newsize;
	}

	/**
	 * Writes <code>len</code> bytes from the specified byte array starting at
	 * offset <code>off</code> to this byte array output stream.
	 * 
	 * @param b the data.
	 * @param off the start offset in the data.
	 * @param len the number of bytes to write.
	 */
	@Override
	public void write(byte b[], int off, int len) {
		int newsize = count + len;
		checkCapacity(newsize);
		System.arraycopy(b, off, buf, count, len);
		count = newsize;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(buf, 0, count);
	}

	public int writeTo(OutputStreamI out, int len) throws IOException {
		out.write(buf, 0, Math.min(len, count));
		return 0;
	}

	@Override
	public int readFrom(InputStreamI input, int len) throws IOException {
		int newsize = count + len;
		checkCapacity(newsize);
		int bytenum = input.read(buf, count, len);
		count = newsize;
		// input.writeTo(this, len);
		return bytenum;
	}

	/**
	 * 返回内部字节数组缓冲区
	 * 
	 * @return The internal byte array
	 */
	public byte[] buffer() {
		return buf;
	}

	@Override
	public byte[] toByteArray() {
		return Arrays.copyOf(buf, count);
	}

	/**
	 * Creates a new {@link #ByteBuffer} by wrapping the current inneral byte
	 * array buffer with the size of current written bytes.
	 * 
	 * @return the created byte buffer.
	 * @see ByteBuffer#wrap(byte[], int, int)
	 * @see #size()
	 */
	public ByteBuffer asByteBuffer() {
		return ByteBuffer.wrap(buf, 0, count);
	}

	/**
	 * Creates a new {@link #ByteArrayInputStream} on the current inneral byte
	 * array buffer with the size of current written bytes.
	 * 
	 * @return the created InputStream.
	 * @see ByteArrayInputStream
	 * @see #size()
	 */
	public InputStream newInputStream() {
		return new ByteArrayInputStream(buf, 0, count);
	}

	/**
	 * Creates a new {@link #ByteArrayInputStream} on the current inneral byte
	 * array buffer with the given offset and length.
	 * 
	 * @return the created InputStream.
	 * @see ByteArrayInputStream
	 * @see #size()
	 */
	public InputStream newInputStream(int start, int length) {
		return new ByteArrayInputStream(buf, start, length);
	}

	@Override
	public String toString() {
		return new String(buf, 0, count);
	}

	@Override
	public String toString(String charsetName) throws UnsupportedEncodingException {
		return new String(buf, 0, count, charsetName);
	}

}