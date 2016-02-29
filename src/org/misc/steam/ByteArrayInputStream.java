package org.misc.steam;

import java.io.IOException;

public class ByteArrayInputStream extends java.io.ByteArrayInputStream implements InputStreamI {
	public ByteArrayInputStream(byte buf[]) {
		super(buf);
	}

	public ByteArrayInputStream(byte[] buf, int offset, int length) {
		super(buf, offset, length);
	}

	@Override
	public int writeTo(OutputStreamI out, int len) throws IOException {
		if (len < 0) {
			throw new IndexOutOfBoundsException();
		}
		int bytenum = count - pos;
		if (bytenum > 0) {
			bytenum = Math.min(len, bytenum);
			out.write(buf, pos, Math.min(len, bytenum));
			pos += bytenum;
			return bytenum;
		} else {
			return -1;
		}
		// out.readFrom(this, len);
	}
}
