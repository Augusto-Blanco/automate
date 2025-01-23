package net.jmb.cryptobot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(
	value = {
		"classpath:application-${spring.profiles.active:}.properties", 
		"classpath:datasource-${spring.profiles.active:}.properties"
	},
	ignoreResourceNotFound = true
)
class ProfileConfig {
	
	@Value("${spring.profile.active:}")
	String profile;
	

    
}
