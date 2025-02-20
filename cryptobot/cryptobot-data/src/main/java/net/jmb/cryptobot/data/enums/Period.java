package net.jmb.cryptobot.data.enums;

import java.util.stream.Stream;

public enum Period implements Comparable<Period> {
	
	_5m("5m"),
	_15m("15m"),
	_30m("30m"),
	_1h("1h"),
	_6h("6h"),
	_12h("12h"),
	_24h("24h"),
	_48h("48h"),
	_6j("6j"),
	_12j("12j"),
	_30j("30j"),
	INFINITE("");
	
	public final String val;
	
	Period(String val) {
		this.val = val;
	}
	
	Period() {
		this.val = name();
	}

	public static Period get(String val) {
		return Stream.of(values())
				.filter(elem -> elem.val.equals(val))
				.findFirst()
				.orElse(null);
	}


}
