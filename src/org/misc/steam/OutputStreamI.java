package org.misc.steam;

import java.io.IOException;

/**
 * <pre>
 * class OutputStreamWithBuffer extends BufferedOutputStream implements OutputStreamI {
 * 
 * 	public int readFrom(InputStreamI in, int len) throws IOException {
 * 		// actively
 * 		int bytenum = in.read(buf, count, Math.min(buf.length - count, len));
 * 		if (bytenum &gt; 0 &amp;&amp; bytenum &lt; len) { // still available
 * 			// using the internal buffer
 * 			SteamUtil.copy(this, out, len - bytenum, buf);
 * 		}
 * 	}
 * }
 * 
 * class MyOutputStream extends FilterOutputStream implements OutputStreamI {
 * 
 * 	public int readFrom(InputStreamI in, int len) throws IOException {
 * 		// passive
 * 		in.writeTo(this, len);
 * 	}
 * }
 * </pre>
 * */
public interface OutputStreamI {
	public void write(byte b[], int off, int len) throws IOException;

	// public int writeTo(OutputStreamI out, int len) throws IOException;

	public int readFrom(InputStreamI in, int len) throws IOException;
}
