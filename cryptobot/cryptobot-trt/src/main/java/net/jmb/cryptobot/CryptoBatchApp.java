package net.jmb.cryptobot;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CryptoBatchApp {
	
	static Logger logger = LoggerFactory.getLogger(CryptoBatchApp.class);
	

	
	public static void main(String[] args) throws Exception {
		
		ConfigurableApplicationContext ctx = SpringApplication.run(CryptoBatchApp.class, args);
		if (getParameters(args).get("daemon") == null) {
			ctx.close();			
		}
	}

	
	public static Map<String, String> getParameters(String...args) {
		
		Map<String, String> parameters = new ConcurrentSkipListMap<>();	
		if (args != null && args.length > 0) {

			if (args != null && args.length > 0) {
				Arrays.stream(args).forEach(arg -> {
					String[] keyValue = arg.split("=");
					String key = keyValue[0], value = "";
					if (keyValue.length > 1) {
						value = keyValue[1];
					}
					parameters.put(key, value);
				});
			}
		}
		return parameters;
	}

	

	
	


}
