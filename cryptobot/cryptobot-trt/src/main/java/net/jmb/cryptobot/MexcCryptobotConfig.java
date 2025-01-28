package net.jmb.cryptobot;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import net.jmb.cryptobot.data.repository.CryptobotRepository;

@Configuration
class MexcCryptobotConfig {	
	

	@Autowired
	CryptobotRepository cryptobotRepository;
	
	@Value("${cryptobot.mexc.restclient.proxy.adr}")
	String proxyAdr;
	
	@Value("${cryptobot.mexc.restclient.proxy.port}")
	String proxyPort;
	
	@Autowired
	ConfigurableEnvironment env;
	
	
	@Bean
	String symbol() {
		String property = env.getProperty("symbol");
		return property;
	}
	
	@Bean
	String platform() {
		String property = env.getProperty("platform");
		return property;
	}	
	
	@Bean
	String apiKey() {
		String property = env.getProperty("apiKey");
		return property;
	}
	
	@Bean
	String secretKey() {
		String property = env.getProperty("secretKey");
		return property;
	}
	
	
	
	@Bean
	RestTemplate mexcRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
    	
		RestTemplate restTemplate = null;   	
		
    	if (StringUtils.isNotBlank(proxyAdr) && NumberUtils.isDigits(proxyPort)) {

			TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;			
			SSLContext sslContext = SSLContexts.custom()
					.loadTrustMaterial(null, acceptingTrustStrategy)
					.build();
			
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
					.<ConnectionSocketFactory>create()
					.register("https", new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
					.register("http", new PlainConnectionSocketFactory())
					.build();

			BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry);
			
			CloseableHttpClient httpClient = HttpClients.custom()
					.setProxy(new HttpHost(proxyAdr, Integer.valueOf(proxyPort)))
					.setConnectionManager(connectionManager)
					.build();

			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		
			restTemplate = new RestTemplate(requestFactory);
			
		} else {			
    		restTemplate = new RestTemplate();
    	}
    	
    	return restTemplate;
    }
	
	
    
}
