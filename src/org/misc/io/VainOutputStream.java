package org.misc.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 不做任何输出的OutputStream, 或叫做NullOutputStream（This OutputStream writes all data to
 * the famous /dev/null.），故也是线程安全的
 */
public class VainOutputStream extends OutputStream {
	/** 方便而避免创建 A singleton OutputStream that writes to nowhere */
	public static final VainOutputStream NUL = new VainOutputStream();

	// 即使封装为私有，也可以
	// Constructor<VainOutputStream> c =
	// VainOutputStream.class.getDeclaredConstructor();
	// c.setAccessible(true);
	// VainOutputStream stream = c.newInstance(null);
	private VainOutputStream() {
	}

	/**
	 * Does nothing - output to <code>/dev/null</code>.
	 * 
	 * @param b The bytes to write
	 * @param off The start offset
	 * @param len The number of bytes to write
	 */
	@Override
	public void write(byte[] b, int off, int len) {
		// to /dev/null
	}

	/**
	 * Does nothing - output to <code>/dev/null</code>.
	 * 
	 * @param b The byte to write
	 */
	@Override
	public void write(int b) {
		// to /dev/null
	}

	/**
	 * Does nothing - output to <code>/dev/null</code>.
	 * 
	 * @param b The bytes to write
	 * @throws IOException never
	 */
	@Override
	public void write(byte[] b) throws IOException {
		// to /dev/null
	}
}