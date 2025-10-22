package net.jmb.cryptobot.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import net.jmb.cryptobot.data.enums.Period;

public class PeriodUtil {
	
	
	public static Date previousDateForPeriod(Date refDate, Period period) {
		
		Date previousDate = null;
		
		if (refDate == null) {
			refDate = new Date();
		}
		if (period == null) {
			period = Period._24h;
		}		
		LocalDateTime localDate = LocalDateTime.ofInstant(refDate.toInstant(), ZoneId.systemDefault());
		
		LocalDateTime previousTime  = switch (period) {
			case _5m -> localDate.minusMinutes(5);
			case _15m -> localDate.minusMinutes(15);
			case _30m -> localDate.minusMinutes(30);
			case _1h -> localDate.minusHours(1);
			case _6h -> localDate.minusHours(6);
			case _12h -> localDate.minusHours(12);
			case _24h -> localDate.minusHours(24);
			case _48h -> localDate.minusHours(48);
			case _6j -> localDate.minusDays(6);
			case _12j -> localDate.minusDays(12);
			case _30j -> localDate.minusDays(30);
			case _2M -> localDate.minusMonths(2);
			case _3M -> localDate.minusMonths(3);
			case _6M ->localDate.minusMonths(6);
			case _12M -> localDate.minusMonths(12);
			case INFINITE -> localDate.minusYears(2);
		};	
		
		if (previousTime != null) {
			previousDate = Date.from(previousTime.atZone(ZoneId.systemDefault()).toInstant());
		}
		return previousDate;		
	}
	
	public static Date nextDateForPeriod(Date refDate, Period period) {
		
		Date nextDate = null;
		
		if (refDate == null) {
			refDate = new Date();
		}
		if (period == null) {
			period = Period._24h;
		}
				
		LocalDateTime localDate = LocalDateTime.ofInstant(refDate.toInstant(), ZoneId.systemDefault());
		
		LocalDateTime nextTime  = switch (period) {
			case _5m -> localDate.plusMinutes(5);
			case _15m -> localDate.plusMinutes(15);
			case _30m -> localDate.plusMinutes(30);
			case _1h -> localDate.plusHours(1);
			case _6h -> localDate.plusHours(6);
			case _12h -> localDate.plusHours(12);
			case _24h -> localDate.plusHours(24);
			case _48h -> localDate.plusHours(48);
			case _6j -> localDate.plusDays(6);
			case _12j -> localDate.plusDays(12);
			case _30j -> localDate.plusDays(30);
			case _2M -> localDate.plusMonths(2);
			case _3M -> localDate.plusMonths(3);
			case _6M ->localDate.plusMonths(6);
			case _12M -> localDate.plusMonths(12);
			case INFINITE -> localDate.minusYears(2);
		};	
		
		if (nextTime != null) {
			nextDate = Date.from(nextTime.atZone(ZoneId.systemDefault()).toInstant());
		}
		
		return nextDate;		
	}

}
