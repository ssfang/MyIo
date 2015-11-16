package org.msgpack;

public class MPackException extends RuntimeException {
	private static final long serialVersionUID = -7871964655271083759L;

	public MPackException() {
		super();
	}
	
	public MPackException(String message, Throwable cause) {
		super(message, cause);
	}

	public MPackException(String message) {
		super(message);
	}

	public MPackException(Throwable cause) {
		super(cause);
	}

}
