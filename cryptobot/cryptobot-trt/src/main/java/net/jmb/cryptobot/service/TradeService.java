package net.jmb.cryptobot.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import net.jmb.cryptobot.data.entity.Asset;
import net.jmb.cryptobot.data.entity.Cotation;
import net.jmb.cryptobot.data.entity.Trade;
import net.jmb.cryptobot.data.enums.OrderSide;

@Service
public abstract class TradeService extends CommonService implements CommandLineRunner {

	
	@Autowired
	CotationService cotationService = null;	
	
	@Value("${symbol}")
	String symbol = null;
	
	@Value("${platform}")
	String platform = null;
	
	@Value("${initDate:}")
	String initDate = null;
	
	@Value("${noExchange:false}")
	Boolean noExchange = false;
	
	Asset asset = null;

	
	@Override
	@Transactional	
	public synchronized void run(String... args) throws Exception {
		
		if (symbol != null) {
			asset = cotationService.getCryptobotRepository().getAssetRepository().findBySymbolAndPlatformEquals(symbol, platform);

			if (canExchange() || StringUtils.isBlank(initDate)) {
				registerLastCotations();
			}
			evaluateLastCotations();
		}
	}
	
	public boolean canExchange() {
		return (this.noExchange == null || this.noExchange == false);
	}
	

	public abstract List<Cotation> registerLastCotations() throws Exception;

	public abstract void evaluateTradeForLastCotation() throws Exception;
	
	public abstract Trade sendOrder(OrderSide orderSide, Double quantity, Double price);
	
	
	@Transactional	
	@Scheduled(cron = "${cryptobot.cotation.evaluation.scheduler.cron}")	
	public synchronized Cotation evaluateLastCotations() throws Exception {
		
		if (canExchange()) {
			asset = cotationService.getCryptobotRepository().getAssetRepository().findBySymbolAndPlatformEquals(symbol, platform);		
			if (asset != null) {
				
				Date dateRef = StringUtils.isNotBlank(initDate) ? new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(initDate) : null;
				Cotation lastCotation = cotationService.evaluateLastCotations(asset, dateRef, true);
				return lastCotation;
			}
		}
		return null;
	}
	
	
	@Transactional
	protected Trade registerTradeForCotation(Trade trade, Cotation cotation) {
		if (trade != null) {
			cotation.setTrade(trade);
			return cotationService.getCryptobotRepository().saveTrade(trade);
		}
		return null;
	}

	
	

	

}
