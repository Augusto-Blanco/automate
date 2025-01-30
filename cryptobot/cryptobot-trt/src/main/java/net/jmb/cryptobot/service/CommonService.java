package net.jmb.cryptobot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;


public abstract class CommonService {	

	
	protected ResourceLoader resourceLoader = new DefaultResourceLoader();
	

	protected Logger getLogger() {
		return LoggerFactory.getLogger(this.getClass());
	}	
	
	



}
