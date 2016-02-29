package org.misc.steam;

import java.io.IOException;

/**
 * <pre>
 * class InputStreamWithBuffer extends BufferedInputStream implements InputStreamI {
 * 
 * 	public int writeTo(OutputStreamI out, int len) throws IOException {
 * 		// actively
 * 		int bytenum = consumeRemaining(out, len);
 * 		if (bytenum &gt; 0 &amp;&amp; bytenum &lt; len) { // still available
 * 			// using the internal buffer
 * 			SteamUtil.copy(this, out, len - bytenum, buf);
 * 		}
 * 	}
 * 
 * 	public int consumeRemaining(OutputStreamI out, int len) throws IOException {
 * 	}
 * }
 * 
 * class MyInputStream extends FilterInputStream implements InputStreamI {
 * 
 * 	public int writeTo(OutputStreamI out, int len) throws IOException {
 * 		// passive
 * 		out.readFrom(this, len);
 * 	}
 * }
 * </pre>
 * */
public interface InputStreamI {

	public int read(byte b[], int off, int len) throws IOException;

	// public int readFrom(InputStreamI in, int len) throws IOException;

	public int writeTo(OutputStreamI out, int len) throws IOException;
}
