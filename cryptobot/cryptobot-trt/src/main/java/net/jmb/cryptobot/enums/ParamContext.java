package net.jmb.cryptobot.enums;

import java.util.stream.Stream;

public enum ParamContext {
	
	TOUT_CONTEXTE("*"),
	SANS_CONTEXTE("");
	
	public final String val;
	
	ParamContext(String val) {
		this.val = val;
	}
	
	ParamContext() {
		this.val = name();
	}

	public static ParamContext get(String val) {
		return Stream.of(values())
				.filter(elem -> elem.val.equals(val))
				.findFirst()
				.orElse(null);
	}



}
