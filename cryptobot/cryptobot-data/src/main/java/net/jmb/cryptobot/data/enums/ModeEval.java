package net.jmb.cryptobot.data.enums;

import java.util.stream.Stream;

public enum ModeEval {
	
	EVAL,
	RECORD,
	TRADE(true);
	
	public final boolean real;
	
	ModeEval(boolean real) {
		this.real = real;
	}
	
	ModeEval() {
		this.real = false;
	}

	public static ModeEval get(String val) {
		return Stream.of(values())
				.filter(elem -> elem.name().equals(val))
				.findFirst()
				.orElse(null);
	}



}
