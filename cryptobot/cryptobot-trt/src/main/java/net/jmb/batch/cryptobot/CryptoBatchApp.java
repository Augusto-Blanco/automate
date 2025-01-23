package net.jmb.batch.cryptobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = {"net.jmb.batch.cryptobot", "net.jmb.cryptobot"})
public class CryptoBatchApp {
	
	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext ctx = SpringApplication.run(CryptoBatchApp.class, args);
       	ctx.close();			
	}


}
