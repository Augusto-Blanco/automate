package net.jmb.cryptobot.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import net.jmb.cryptobot.beans.MexcOrder;
import net.jmb.cryptobot.data.entity.AssetConfig;
import net.jmb.cryptobot.data.entity.Cotation;
import net.jmb.cryptobot.data.entity.Trade;
import net.jmb.cryptobot.data.enums.OrderSide;
import net.jmb.cryptobot.data.enums.OrderState;


public class MexcTradeService extends TradeService {

	
	@Autowired
	MexcRestClientService restClientService;		
	

	
	@Override
	public synchronized List<Cotation> registerLastCotations() throws Exception {
		if (canExchange()) {
			List<Cotation> newCotations = restClientService.updateCotationsPrice(symbol);
			return newCotations;
		}
		return null;
	}
	
	
	
	
	@Scheduled(cron = "${cryptobot.trade.evaluation.scheduler.cron}")
	@Transactional
	public synchronized void evaluateTradeForLastCotation() throws Exception {
		
		if (canExchange()) {
			
			asset = cotationService.getCryptobotRepository().getAssetRepository().findBySymbolAndPlatformEquals(symbol, platform);
			
			if (asset != null) {
				
				Trade trade = null;
				
				List<Cotation> lastCotations = registerLastCotations();			
				if (lastCotations != null && lastCotations.size() > 0) {
					
					lastCotations = cotationService.getCryptobotRepository().getAllCotationsSinceLastRated(symbol);
					
					if (lastCotations != null && lastCotations.size() > 0) {
						Cotation cotation = lastCotations.get(lastCotations.size() - 1);
						AssetConfig assetConfig = cotationService.getAssetConfigForCotation(cotation);
		
						getLogger().info("-- Evaluate trade --");
						cotation = cotationService.evaluateTradesForCotations(lastCotations, asset, assetConfig.realEval(true));
						
	
							if (cotation != null && "B".equalsIgnoreCase(cotation.getFlagBuy())) {
								Double freeAmount = restClientService.getFreeQuantity("USDT");
								Double maxInvest = asset.getMaxInvest();
								if (maxInvest != null && maxInvest > 0d && maxInvest < freeAmount) {
									freeAmount = maxInvest;
								}
								Double lastPrice = restClientService.getLastPrice(symbol);
								if (freeAmount > 1d && lastPrice != null && lastPrice > 0d && lastPrice <= 1.001 * cotation.getPrice()) {
									Double quantity = freeAmount / lastPrice; 
									trade = sendOrder(OrderSide.BUY, quantity, lastPrice);			
								}
							} else if (cotation != null && "S".equalsIgnoreCase(cotation.getFlagSell())) {
								Double quantity = restClientService.getFreeQuantity(symbol);
								Double lastPrice = restClientService.getLastPrice(symbol);
								if (lastPrice > 0d && quantity > 0d) {
									trade = sendOrder(OrderSide.SELL, quantity, lastPrice);			
								}
							}
						
						if (trade != null) {
							super.registerTradeForCotation(trade, cotation);
						}
					}
				}
			}
		}

	}
	
	
	public synchronized Trade sendOrder(OrderSide orderSide, Double quantity, Double price) {
		
		if (asset != null && canExchange()) {
			
			Double fees = asset.getFeesRate().doubleValue() / 100;
			
			BigDecimal qty = switch (orderSide) {
				case BUY -> new BigDecimal(quantity / (1.001 + fees));
				case SELL -> new BigDecimal(quantity);		
			};

			if (quantity > 1) {
				qty = qty.setScale(2, RoundingMode.HALF_DOWN);
			} else {
				qty = qty.setScale(7, RoundingMode.HALF_DOWN);
			}
			
			Integer nbDecimals = asset.getNbDecimals();
			BigDecimal decPrice = switch (orderSide) {
				case BUY -> new BigDecimal(price * 1.001).setScale(nbDecimals, RoundingMode.HALF_UP);
				case SELL -> new BigDecimal(price * 0.999).setScale(nbDecimals, RoundingMode.HALF_DOWN);		
			};			
			
			MexcOrder sendOrder = restClientService.sendOrder(symbol, orderSide, qty, decPrice);
			if (sendOrder != null) {
				return new Trade()
						.symbol(symbol)
						.time(new Date())
						.asset(asset)
						.amount(qty.doubleValue() * decPrice.doubleValue())
						.price(decPrice.doubleValue())
						.quantity(qty.doubleValue())
						.side(orderSide.name())
						.platform(platform)
						.tradeRef(sendOrder.getOrderId())
						.state(OrderState.PENDING.name());
			}
		}
		return null;
	}
	
	

}
