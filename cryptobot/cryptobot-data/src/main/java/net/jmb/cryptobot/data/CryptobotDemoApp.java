package net.jmb.cryptobot.data;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.jmb.cryptobot.data.entity.Cotation;
import net.jmb.cryptobot.data.entity.Trade;
import net.jmb.cryptobot.data.repository.CryptobotRepository;

public class CryptobotDemoApp  {
	
	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext applicationContext = new AnnotationConfigApplicationContext(CryptobotDemoApp.class.getPackage().getName());
		Runner runner = applicationContext.getBean(Runner.class);
		runner.run(applicationContext);		
		applicationContext.close();
	}
	
	
	@Service
	public static class Runner {
		
		private Logger logger = LoggerFactory.getLogger(this.getClass());
		
		@Autowired
		private CryptobotRepository cryptobotRepository;
		
		@Transactional
		public void run(ApplicationContext applicationContext) throws Exception {

			List<Trade> trades = cryptobotRepository.getPendingTrades();
			logger.info("Trades -> {}", trades);

			for (Trade trade : trades) {
				Cotation cotation = trade.getCotation();
				if (cotation == null) {
					cotation = new Cotation();
				}
				cotation.currentSide(trade.getSide())
						.datetime(trade.getTime())
						.symbol(trade.getSymbol())
						.price(trade.getPrice())
						.trade(trade);
				cryptobotRepository.getCotationRepository().save(cotation);
				trade.setCotation(cotation);
			}
		}		

	}
	

	
}
