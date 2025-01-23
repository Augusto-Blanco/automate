package net.jmb.cryptobot.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import net.jmb.cryptobot.data.enums.Period;
import net.jmb.cryptobot.data.repository.CryptobotRepository;


public abstract class CommonService {	

	
	protected ResourceLoader resourceLoader = new DefaultResourceLoader();
	
	@Autowired
	protected CryptobotRepository cryptobotRepository;


	protected Logger getLogger() {
		return LoggerFactory.getLogger(this.getClass());
	}	
	
	public CryptobotRepository getCryptobotRepository() {
		return cryptobotRepository;
	}
	
	public Date previousDateForPeriod(Date refDate, Period period) {
		
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
	
	public Date nextDateForPeriod(Date refDate, Period period) {
		
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
