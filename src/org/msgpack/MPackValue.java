package org.msgpack;

public interface MPackValue {
	public void writeTo(MPacker packer) throws java.io.IOException;
}
