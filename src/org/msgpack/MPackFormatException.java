package org.msgpack;

public class MPackFormatException extends MPackException {

	private static final long serialVersionUID = 20021068703300110L;

	public MPackFormatException(String message) {
		super(message);
	}

	public MPackFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
