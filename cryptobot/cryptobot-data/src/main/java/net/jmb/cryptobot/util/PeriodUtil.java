package net.jmb.cryptobot.util;

import java.util.Date;

import net.jmb.cryptobot.data.enums.Period;

public class PeriodUtil {
	
	
	public static Date previousDateForPeriod(Date refDate, Period period) {
		
		Date previousDate = null;
		
		if (refDate == null) {
			refDate = new Date();
		}
		if (period == null) {
			period = Period._12h;
		}		
		long seconds = refDate.getTime() / 1000;		
		
		Long previousTime = switch (period) {
			case _5m -> seconds - 5 * 60;
			case _15m -> seconds - 15 * 60;
			case _30m -> seconds - 30 * 60;
			case _1h -> seconds - 60 * 60;
			case _6h -> seconds - 6 * 60 * 60;
			case _12h -> seconds - 12 * 60 * 60;
			case _24h -> seconds - 24 * 60 * 60;
			case _6j -> seconds - 6 * 24 * 60 * 60;
			case _12j -> seconds - 12 * 24 * 60 * 60;
			case _30j -> seconds - 30 * 24 * 60 * 60;
			case INFINITE -> seconds - 365 * 24 * 60 * 60;
		};		
		
		if (previousTime != null) {
			previousDate = new Date(previousTime * 1000);		
		}
		return previousDate;		
	}
	
	public static Date nextDateForPeriod(Date refDate, Period period) {
		
		Date nextDate = null;
		
		if (refDate == null) {
			refDate = new Date();
		}
		if (period == null) {
			period = Period._12h;
		}		
		long seconds = refDate.getTime() / 1000;		
		
		Long nextTime = switch (period) {
			case _5m -> seconds + 5 * 60;
			case _15m -> seconds + 15 * 60;
			case _30m -> seconds + 30 * 60;
			case _1h -> seconds + 60 * 60;
			case _6h -> seconds + 6 * 60 * 60;
			case _12h -> seconds + 12 * 60 * 60;
			case _24h -> seconds + 24 * 60 * 60;
			case _6j -> seconds + 6 * 24 * 60 * 60;
			case _12j -> seconds + 12 * 24 * 60 * 60;
			case _30j -> seconds + 30 * 24 * 60 * 60;
			case INFINITE -> seconds + 365 * 24 * 60 * 60;
		};		
		
		if (nextTime != null) {
			nextDate = new Date(nextTime * 1000);		
		}
		return nextDate;		
	}

}
