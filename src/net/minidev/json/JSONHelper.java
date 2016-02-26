package net.minidev.json;

import java.io.IOException;
import java.util.Map;

public class JSONHelper {
	/**
	 * Global default compression type
	 */
	public static JSONStyle COMPRESSION = JSONStyle.NO_COMPRESS;

	static final char[] hexDigits = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
			'C', 'D', 'E', 'F' };

	public static boolean writeJSONString(Object value, Appendable out, JSONStyle compression)
			throws IOException {
		if (null != value) {
			Class<?> clz = value.getClass();
			if (byte[].class == clz) {
				byte[] blob = (byte[]) value;
				bytesToHex0(out, blob, blob.length);
			} else if (clz.isArray()) {
				writeJSONString((Object[]) value, out, compression);
			} else if (Map.class.isAssignableFrom(clz)) {
				writeJSONString((Map<?, ?>) value, out, compression);
			} else {
				out.append(value.toString());
			}
		} else {
			out.append("null");
		}
		return true;
	}

	public static String toJSONString(Object value) {
		return toJSONString(value, JSONStyle.NO_COMPRESS);
	}

	public static String toJSONString(Object value, JSONStyle compression) {
		StringBuilder sb = new StringBuilder();
		try {
			writeJSONString(value, sb, compression);
		} catch (IOException e) {
			// can not append on a StringBuilder
		}
		return sb.toString();
	}

	static Appendable bytesToHex0(Appendable sb, byte[] blob, int len) throws IOException {
		for (int i = 0; i < len; i++) {
			int onebyte = blob[i];
			sb.append(hexDigits[(onebyte >>> 4) & 0xF]);
			sb.append(hexDigits[onebyte & 0xF]);
		}
		return sb;
	}

	/**
	 * 
	 * @param array 对象数组
	 * @return
	 */
	public static void writeJSONString(Object[] array, Appendable out, JSONStyle compression)
			throws IOException {
		compression.arrayStart(out);
		boolean needSep = false;
		for (Object o : array) {
			if (needSep)
				compression.objectNext(out);
			else
				needSep = true;
			writeJSONString(o, out, compression);
		}
		compression.arrayStop(out);
	}

	public static void writeJSONString(Map<?, ?> map, Appendable out, JSONStyle compression) throws IOException {
		boolean first = true;
		compression.objectStart(out);
		/**
		 * do not use <String, Object> to handle non String key maps
		 */
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object v = entry.getValue();
			if (v == null && compression.ignoreNull())
				continue;
			if (first) {
				compression.objectFirstStart(out);
				first = false;
			} else {
				compression.objectNext(out);
			}
			writeJSONKV(entry.getKey().toString(), v, out, compression);
			// compression.objectElmStop(out);
		}
		compression.objectStop(out);
	}

	/**
	 * Write a Key : value entry to a stream
	 */
	public static void writeJSONKV(String key, Object value, Appendable out, JSONStyle compression)
			throws IOException {
		if (key == null)
			out.append("null");
		else if (!compression.mustProtectKey(key))
			out.append(key);
		else {
			out.append('"');
			escape(key, out, compression);
			out.append('"');
		}
		compression.objectEndOfKey(out);
		if (value instanceof String) {
			compression.writeString(out, (String) value);
		} else
			writeJSONString(value, out, compression);
		compression.objectElmStop(out);
	}

	public static String escape(String s) {
		return escape(s, COMPRESSION);
	}

	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters
	 * (U+0000 through U+001F).
	 */
	public static String escape(String s, JSONStyle compression) {
		if (s == null)
			return null;
		StringBuilder sb = new StringBuilder();
		compression.escape(s, sb);
		return sb.toString();
	}

	public static void escape(String s, Appendable ap) {
		escape(s, ap, COMPRESSION);
	}

	public static void escape(String s, Appendable ap, JSONStyle compression) {
		if (s == null)
			return;
		compression.escape(s, ap);
	}
}
