package net.jmb.cryptobot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import net.jmb.cryptobot.beans.MexcOrder;
import net.jmb.cryptobot.service.MexcRestClientService;


public class CryptoTestApp {
	
	static Logger logger = LoggerFactory.getLogger(CryptoTestApp.class);


	
	public static void main(String[] args) throws Exception {
		
		ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(CryptoTestApp.class.getPackageName());
		TestService testService = ctx.getBean(TestService.class);
		testService.test();
		ctx.close();			

	}
	
	@Service
	static class TestService {
		
		@Autowired
		MexcRestClientService restClientService;
		
		public void test() {
			
			String symbol = "LINK";
			String orderId = "C02__518339535839485952049";
			MexcOrder mexcOrder = restClientService.requestOrder(symbol, orderId);
			System.out.println(mexcOrder);
			
		}
		
		
	}

	


	

	
	


}
