package net.jmb.cryptobot.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import net.jmb.cryptobot.beans.MexcOrder;
import net.jmb.cryptobot.data.entity.Asset;
import net.jmb.cryptobot.data.entity.AssetConfig;
import net.jmb.cryptobot.data.entity.Cotation;
import net.jmb.cryptobot.data.entity.Trade;
import net.jmb.cryptobot.data.enums.OrderSide;

@Service
public class TradeService extends CommonService implements CommandLineRunner {

	
	@Autowired
	CotationService cotationService;	
	@Autowired
	MexcRestClientService restClientService;		
	@Autowired
	String symbol;
	@Autowired
	String platform;
	
	Asset asset;

	
	@Override
	@Transactional
	public void run(String... args) throws Exception {
		
		if (symbol != null) {
			asset = cotationService.getCryptobotRepository().getAssetRepository().findBySymbolAndPlatformEquals(symbol, platform);
			registerLastCotations();
			evaluateLastCotations();
		}
		

		
		// au démarrage
		// on détermine l'Asset
		// on récupère les dernières cotations et on les enregistre
		// on calcule la config optimale pour évaluer les nouvelles cotations en s'appuyant sur la plus récente évaluation 
		// terminé
		
		// Tâches de fond
		
		// - toutes les 5 minutes
		//   on récupère les nouvelles cotations
		//   on les évalue par rapport à la plus récente config pour l'actif
		//   on lance l'ordre d'achat ou de vente selon le flag sur la dernière cotation évaluée
		
		// - toutes les 15 minutes
		//   on calcule la config optimale pour évaluer les nouvelles cotations

	}	
	

	@Transactional	
	public synchronized List<Cotation> registerLastCotations() throws Exception {		
		List<Cotation> newCotations = restClientService.updateCotationsPrice(symbol);
		return newCotations;
		
	}
	
	@Scheduled(cron = "${cryptobot.trade.evaluation.scheduler.cron}")
	@Transactional
	public synchronized void evaluateTradeForLastCotation() throws Exception {
		
		if (asset != null) {
			
			Trade trade = null;
			
			List<Cotation> lastCotations = registerLastCotations();			
			if (lastCotations != null && lastCotations.size() > 0) {
				Cotation cotation = lastCotations.get(lastCotations.size() - 1);
				AssetConfig assetConfig = cotationService.getAssetConfigForCotation(cotation);
				cotation = cotationService.evaluateTradesForCotations(lastCotations, asset, assetConfig);
				if ("B".equalsIgnoreCase(cotation.getFlagBuy())) {
					Double freeAmount = restClientService.getFreeQuantity("USDT");
					Double lastPrice = restClientService.getLastPrice(symbol);
					if (lastPrice > 0d && freeAmount > 0d) {
						Double quantity = freeAmount / lastPrice; 
						trade = sendOrder(OrderSide.BUY, quantity, lastPrice);			
					}
				} else if ("S".equalsIgnoreCase(cotation.getFlagSell())) {
					Double quantity = restClientService.getFreeQuantity(symbol);
					Double lastPrice = restClientService.getLastPrice(symbol);
					if (lastPrice > 0d && quantity > 0d) {
						trade = sendOrder(OrderSide.SELL, quantity, lastPrice);			
					}
				}
				if (trade != null) {
					cotation.setTrade(trade);
					cotationService.getCryptobotRepository().saveTrade(trade);
				}
			}
		}

	}
	
	@Scheduled(cron = "${cryptobot.cotation.evaluation.scheduler.cron}")
	@Transactional
	public synchronized void evaluateLastCotations() throws Exception {
		
		if (asset != null) {
			cotationService.evaluateLastCotations(asset);
		}
	}

	
	
	public synchronized Trade sendOrder(OrderSide orderSide, Double quantity, Double price) {
		
		if (quantity > 1) {
			quantity = new BigDecimal(quantity).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
		} else {
			quantity = new BigDecimal(quantity).setScale(7, RoundingMode.HALF_DOWN).doubleValue();
		}
		
		MexcOrder sendOrder = restClientService.sendOrder(symbol, orderSide, quantity, price);
		if (sendOrder != null) {
			return new Trade().asset(asset).amount(quantity * price).price(price).quantity(quantity).side(orderSide.name()).platform(platform);
		}
		return null;

	}
	
	
	

	

}
