package net.jmb.cryptobot.exception;

public class CryptobotException extends RuntimeException {
	private static final long serialVersionUID = 5157770066965280014L;

	public CryptobotException() {
		super();
	}

	public CryptobotException(String message, Throwable cause) {
		super(message, cause);
	}

	public CryptobotException(String message) {
		super(message);
	}

	public CryptobotException(Throwable cause) {
		super(cause);
	}
	
	

}
