package net.jmb.cryptobot.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "net.jmb.cryptobot")
public class LobotWebappApplication {

	public static void main(String[] args) {
		SpringApplication.run(LobotWebappApplication.class, args);
	}

}
