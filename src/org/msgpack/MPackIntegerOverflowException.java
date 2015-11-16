package org.msgpack;

/**
 * This error is thrown when the user tries to read an integer value using a smaller types. For example, calling
 * {@link MUnpacker#unpackInt()} for an integer value that is larger than {@link Integer#MAX_VALUE} will cause this exception.
 */
public class MPackIntegerOverflowException extends MPackException {

	private static final long serialVersionUID = -4283216830101716249L;
	
	private final Number number;

	public MPackIntegerOverflowException(Number number) {
		this.number = number;
	}

	@Override
	public String getMessage() {
		return number.toString();
	}
}