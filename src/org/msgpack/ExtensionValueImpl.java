package org.msgpack;

/**
 * {@code ExtensionValueImpl} Implements {@code ExtensionValue} using a {@code byte} and a {@code byte[]} fields.
 *
 * @see ExtensionValue
 */
public class ExtensionValueImpl implements ExtensionValue {
	private final byte type;
	private final byte[] data;

	public ExtensionValueImpl(byte type, byte[] data) {
		this.type = type;
		this.data = data;
	}

	@Override
	public byte getType() {
		return type;
	}

	@Override
	public byte[] getData() {
		return data;
	}
}
