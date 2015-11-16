package org.msgpack;

public class MPackSizeException extends MPackException {

	private static final long serialVersionUID = -2361787671149198606L;

	private final long size;

	public MPackSizeException(long size) {
		this.size = size;
	}
	
	@Override
	public String getMessage() {
		return Long.toString(size);
	}
}