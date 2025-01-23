package net.jmb.cryptobot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

import net.jmb.cryptobot.data.repository.CryptobotRepository;

@Configuration
@PropertySource("classpath:application.properties")
class CryptobotServiceConfig {	
	

	@Autowired
	CryptobotRepository cryptobotRepository;

	
	@Autowired
	ConfigurableEnvironment env;
	
	

    
}
