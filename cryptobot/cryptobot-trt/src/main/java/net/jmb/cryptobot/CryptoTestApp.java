package net.jmb.cryptobot;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import net.jmb.cryptobot.beans.MexcOrder;
import net.jmb.cryptobot.data.entity.Asset;
import net.jmb.cryptobot.data.entity.Cotation;
import net.jmb.cryptobot.service.CotationService;
import net.jmb.cryptobot.service.MexcRestClientService;
import net.jmb.cryptobot.service.TradeService;


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
		
		@Autowired
		CotationService cotationService;
		
//		@Autowired
//		TradeService mexcTradeService;
		
		public void test() throws Exception {
			
//			String platform = "MEXC";
//			String symbol = "XRP";
//			
//			
//			mexcTradeService.init(symbol, platform, false);
//			mexcTradeService.updateTradesAndPosition();
			
//			String orderId = "C02__528156736163532800049";
//			MexcOrder mexcOrder = restClientService.requestOrder(symbol, orderId);
//			System.out.println(mexcOrder);
			
			
//			GregorianCalendar calendar = new GregorianCalendar();
//			calendar.add(Calendar.DAY_OF_YEAR, -6);
//			restClientService.allOrders(symbol, calendar.getTime());			
//			restClientService.openOrders(symbol);

			
//			Asset asset = cotationService.getCryptobotRepository().getAssetRepository().findBySymbolAndPlatformEquals(symbol, platform);			
//			Cotation cotation = cotationService.resetEvaluationForAsset(asset, null);
//			System.out.println(cotation);
			
		}
		
		
	}

	


	

	
	


}
