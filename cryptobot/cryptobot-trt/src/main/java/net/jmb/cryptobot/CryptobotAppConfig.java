package net.jmb.cryptobot;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import net.jmb.cryptobot.data.repository.CryptobotRepository;

@Configuration
class CryptobotAppConfig {	
	

	@Autowired
	CryptobotRepository cryptobotRepository;
	
	@Value("${cryptobot.mexc.restclient.proxy.adr}")
	String proxyAdr;
	@Value("${cryptobot.mexc.restclient.proxy.port}")
	String proxyPort;
	
	@Autowired
	ConfigurableEnvironment env;
	
	
	@Bean
	protected RestTemplate mexcRestTemplate() {
    	RestTemplate restTemplate = null;   	
    	if (StringUtils.isNotBlank(proxyAdr) && NumberUtils.isDigits(proxyPort)) {
    		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			requestFactory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAdr, Integer.valueOf(proxyPort))));
			restTemplate = new RestTemplate(requestFactory);
    	} else {
    		restTemplate = new RestTemplate();
    	}
    	return restTemplate;
    }
	
	
    
}
