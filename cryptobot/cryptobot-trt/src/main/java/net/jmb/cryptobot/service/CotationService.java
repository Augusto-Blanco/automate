package net.jmb.cryptobot.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import net.jmb.cryptobot.data.bean.OrderQO;
import net.jmb.cryptobot.data.bean.PageData;
import net.jmb.cryptobot.data.entity.AssetConfig;
import net.jmb.cryptobot.data.entity.Cotation;
import net.jmb.cryptobot.data.entity.Trade;
import net.jmb.cryptobot.data.enums.OrderSide;
import net.jmb.cryptobot.data.enums.Period;
import net.jmb.cryptobot.enums.ParamContext;

@Service
public class CotationService extends CommonService {
	
	public static final ParamContext CONTEXTE = ParamContext.TOUT_CONTEXTE;
	
	@Transactional
	public Cotation evaluateTradesForCotations(String symbol, Date dateRef, Boolean init, Double initParam, Period analysisPeriod, Period frequency) {
		
		Cotation cotation = null;
		
		if (symbol != null) {
			
			boolean reset = init != null ? init : false;
			
			cotation = cryptobotRepository.getLastCotationBeforeDate(symbol, dateRef);
			if (cotation != null) {
				dateRef = cotation.getDatetime();
			}
			if (dateRef == null) {
				dateRef = new Date();
			}
			if (initParam == null) {
				initParam = 2d;
			}
			
			Period subPeriod = (analysisPeriod != null) ? analysisPeriod : Period._12h;
			Period interval = (frequency != null) ? frequency : Period._1h;
			
			Date startDate = reset ? dateRef : previousDateForPeriod(dateRef, subPeriod);			
			List<Cotation> dbCotations = cryptobotRepository.getCotationsSinceDate(symbol, startDate);
			List<Cotation> allCotations = new ArrayList<>(dbCotations.stream().map(Cotation::duplicate).toList());;
			
			if (allCotations != null && allCotations.size() > 0) {
				
				cotation = allCotations.get(0);
				if (reset) {
					cotation.resetEvaluation();
				}
				List<Cotation> cotationGrid = getCotationGridOnPeriodForward(cotation, allCotations, subPeriod);
					
				while (cotationGrid != null && cotationGrid.size() > 1) {
					AssetConfig bestAssetConfig = evaluateAssetConfigForCotations(cotationGrid, initParam);
					cryptobotRepository.getAssetConfigRepository().deleteDateGreaterOrEquals(symbol, bestAssetConfig.getStartTime());
					cryptobotRepository.getAssetConfigRepository().save(bestAssetConfig);					
					cotation = cotationGrid.get(cotationGrid.size() - 1);
					updateCotationsEvalFromGrid(dbCotations, cotationGrid, reset);
					cotationGrid = getCotationGridOnPeriodForward(cotation, allCotations, interval);
					if (cotationGrid != null && cotationGrid.size() > 1) {
						cotation = evaluateTradesForCotations(cotationGrid, bestAssetConfig);
						updateCotationsEvalFromGrid(dbCotations, cotationGrid, true);
						cotationGrid = getCotationGridOnPeriodBackward(cotation, allCotations, subPeriod);
					}					
				}
			}
		}
		return cotation;
	}
	
	
	@Transactional
	public Cotation initEvaluationForLastCotations(String symbol, Period period, double initParam, Period... cutPeriods) {

		Date startDate = previousDateForPeriod(null, period);
		Period analysisPeriod = cutPeriods != null && cutPeriods.length > 0 ? cutPeriods[0] : null;
		Period frequency = cutPeriods != null && cutPeriods.length > 1 ? cutPeriods[1] : null;
		
		return evaluateTradesForCotations(symbol, startDate, true, initParam, analysisPeriod, frequency);		
	}
	
	
	void updateCotationsEvalFromGrid(List<Cotation> cotationsToUpdate, List<Cotation> cotationGrid, boolean replace) {
		if (cotationGrid != null && !cotationGrid.isEmpty() && cotationsToUpdate != null) {
			for (Cotation srcCotation : cotationGrid) {
				int i = cotationsToUpdate.indexOf(srcCotation);
				if (i >= 0) {
					Cotation targetCotation = cotationsToUpdate.get(i);
					if (targetCotation.getAmountB100() == null || replace) {
						targetCotation						
							.currentSide(srcCotation.getCurrentSide())
							.flagBuy(srcCotation.getFlagBuy())
							.flagSell(srcCotation.getFlagSell())
							.bestBuyPrice(srcCotation.getBestBuyPrice())
							.bestSellPrice(srcCotation.getBestSellPrice())
							.amountB100(srcCotation.getAmountB100());
					}
				}
			}
		}
	}
	
	public AssetConfig evaluateAssetConfigForCotations(List<Cotation> cotationGrid, double initParam) {
		
		Cotation cotation = null;
		AssetConfig assetConfig = null;
		
		if (cotationGrid != null && cotationGrid.size() > 0) {
			
			// sauvegarde de la 1ère cotation qui sert de base aux calculs pour les autres
			Cotation firstCotation = cotationGrid.get(0);
			
			BigDecimal amountB100 = null;
			Double maxVarHigh, maxVarLow, stopLoss;
			maxVarHigh = maxVarLow = stopLoss = initParam;
			Double bestVarHigh = null, bestVarLow = null, bestStopLoss = null;

			while (stopLoss <= 7d) {
				while (maxVarHigh <= 10d) {
					while (maxVarLow <= 10d) {
						cotationGrid.remove(0);
						cotationGrid.add(0, firstCotation.duplicate());
						cotation = evaluateTradesForCotations(cotationGrid, maxVarHigh, maxVarLow, stopLoss);
						if (amountB100 == null || cotation.getAmountB100().compareTo(amountB100) > 0) {
							bestVarHigh = maxVarHigh;
							bestVarLow = maxVarLow;
							bestStopLoss = stopLoss;
							amountB100 = cotation.getAmountB100();
						}
						maxVarLow += 0.1d;
					}
					maxVarLow = initParam;
					maxVarHigh += 0.1d;
				}
				maxVarHigh = maxVarLow = initParam;
				stopLoss += 0.1d;
			}
			// on compare à si on ne fait rien (variation 100% requise)
			maxVarHigh = maxVarLow = 100d;
			stopLoss = initParam;
			cotationGrid.remove(0);
			cotationGrid.add(0, firstCotation.duplicate());
			cotation = evaluateTradesForCotations(cotationGrid, maxVarHigh, maxVarLow, stopLoss);
			if (amountB100 == null || cotation.getAmountB100().compareTo(amountB100) > 0) {
				bestVarHigh = 100d;
				bestVarLow = 100d;
				bestStopLoss = initParam;
				amountB100 = cotation.getAmountB100();
			}
			
			if (bestVarHigh != null && bestVarLow != null && bestStopLoss != null) {
				cotationGrid.remove(0);
				cotationGrid.add(0, firstCotation.duplicate());
				cotation = evaluateTradesForCotations(cotationGrid, bestVarHigh, bestVarLow, bestStopLoss);
				assetConfig = new AssetConfig()
						.symbol(cotation.getSymbol())
						.startTime(cotationGrid.get(0).getDatetime())
						.endTime(cotation.getDatetime())
						.maxVarHigh(BigDecimal.valueOf(bestVarHigh).setScale(2, RoundingMode.HALF_EVEN))
						.maxVarLow(BigDecimal.valueOf(bestVarLow).setScale(2, RoundingMode.HALF_EVEN))
						.stopLoss(BigDecimal.valueOf(bestStopLoss).setScale(2, RoundingMode.HALF_EVEN));
				System.out.println(assetConfig);
				System.out.println(cotation);
			}
		}
		return assetConfig;
	} 
	
	
	public Cotation evaluateTradesForCotations(List<Cotation> cotationGrid, AssetConfig assetConfig) {
		
		Cotation cotation = null;
		if (assetConfig != null) {
			Double maxVarHigh = assetConfig.getMaxVarHigh() != null ? assetConfig.getMaxVarHigh().doubleValue() : 1000d;
			Double maxVarLow = assetConfig.getMaxVarLow() != null ? assetConfig.getMaxVarLow().doubleValue() : 1000d;
			Double stopLoss = assetConfig.getStopLoss() != null ? assetConfig.getStopLoss().doubleValue() : 1000d;
			cotation = evaluateTradesForCotations(cotationGrid, maxVarHigh, maxVarLow, stopLoss);
		}		
		return cotation;
	}
	
	
	public Cotation evaluateTradesForCotations(List<Cotation> cotationGrid, Double maxVarHigh, Double maxVarLow, Double stopLoss) {
		
		double fees = 0.01d;
		long delayBetweenTrades = 10 * 60 * 1000; 
		
		Cotation cotation = null;
		
		if (cotationGrid != null) {
			
			Double bestSellPrice = null, sellPrice = null, bestBuyPrice = null, buyPrice = null, quantity = null, amountB100 = null;
			OrderSide currentSide = null;
			Date lastBuy = null, lastSell = null;
			
			
			for (int i = 0; i < cotationGrid.size(); i++) {
				
				cotation = cotationGrid.get(i);
				cotation.flagBuy(null).flagSell(null);
				if (currentSide == null && StringUtils.isBlank(cotation.getCurrentSide())) {
					Double price = cotation.getPrice();
					cotation.flagBuy().currentSide(OrderSide.BUY).buyPrice(price).bestBuyPrice(price).sellPrice(null).bestSellPrice(null).amountB100(BigDecimal.valueOf(100));
					lastBuy = cotation.getDatetime();
				}
				if (currentSide == null) {
					currentSide = cotation.getCurrentOrderSide();
				}
				if (buyPrice == null) {
					buyPrice = cotation.getBuyPrice();
					if (buyPrice == null && currentSide.equals(OrderSide.BUY)) {
						buyPrice = cotation.getPrice();
					}
				}
				if (bestBuyPrice == null) {
					bestBuyPrice = cotation.getBestBuyPrice();
				}
				if (bestSellPrice == null) {
					bestSellPrice = cotation.getBestSellPrice();
				}
				if (amountB100 == null) {
					amountB100 = cotation.getAmountB100().doubleValue();					
				}
				if (quantity == null) {
					quantity = 0d;
					if (currentSide.equals(OrderSide.BUY)) {
						quantity = amountB100 / buyPrice;
					}
				}
				
				if (currentSide.equals(OrderSide.BUY)) {
					Double deltaPrice = (cotation.getPrice() - buyPrice) / buyPrice *100;
					Double deltaFromBestBuy = (cotation.getPrice() - bestBuyPrice) / bestBuyPrice * 100;
					amountB100 = quantity * cotation.getPrice();
					boolean isDelayOK = (lastBuy == null || cotation.getDatetime().getTime() - lastBuy.getTime() > delayBetweenTrades);
					if (deltaFromBestBuy > maxVarHigh && isDelayOK || deltaPrice <= -stopLoss) {
						currentSide = OrderSide.SELL;	
						amountB100 = amountB100 * (1 - fees);
						sellPrice = cotation.getPrice();
						bestSellPrice = cotation.getPrice();
						quantity = 0d;
						cotation.flagSell();
						lastSell = cotation.getDatetime();
					}
					if (cotation.getPrice() < bestBuyPrice) {
						bestBuyPrice = cotation.getPrice();
					}
					
				} else if (currentSide.equals(OrderSide.SELL)) {
					Double deltaFromBestSell = (cotation.getPrice() - bestSellPrice) / bestSellPrice * 100;
					boolean isDelayOK = (lastSell == null || cotation.getDatetime().getTime() - lastSell.getTime() > delayBetweenTrades);
					if (deltaFromBestSell < -maxVarLow & isDelayOK) {
						currentSide = OrderSide.BUY;
						buyPrice = cotation.getPrice();
						bestBuyPrice = cotation.getPrice();
						amountB100 = amountB100 * (1 - fees);
						quantity = amountB100 / cotation.getPrice();
						cotation.flagBuy();
						lastBuy = cotation.getDatetime();
					}
					if (cotation.getPrice() > bestSellPrice) {
						bestSellPrice = cotation.getPrice();
					}					
				}
				cotation.currentSide(currentSide).sellPrice(sellPrice).buyPrice(buyPrice).bestBuyPrice(bestBuyPrice).bestSellPrice(bestSellPrice).amountB100(BigDecimal.valueOf(amountB100).setScale(2, RoundingMode.HALF_EVEN));
			}
			
		}		
		return cotation;
	}
	
	
	@Transactional
	public List<Cotation> computeCotations(String symbol, Period period) {
		
		List<Cotation> allCotations = cryptobotRepository.getCotationsSinceDate(symbol, previousDateForPeriod(null, period));
		
		if (allCotations != null) {
			
			List<Period> periodsMaj = Arrays.asList(Period._1h, Period._12h, Period._24h, Period._6j);
			
			for (int i = 0; i < allCotations.size(); i++) {
				
				Cotation cotation = allCotations.get(i);
				
				for (Period periodMaj : periodsMaj) {
					List<Cotation> cotationGrid = getCotationGridOnPeriodBackward(cotation, allCotations, periodMaj);
					cotation = computeLastCotation(cotationGrid, periodMaj);
				}
			}
		}		
		return allCotations;
	}
	
	
	Cotation computeLastCotation(List<Cotation> cotationGrid, Period period) {
		
		int refIndex = cotationGrid.size() - 1;
		Cotation refCotation = cotationGrid.get(refIndex);
		Double min = null;
		Double max = null;
		for (Cotation cotation : cotationGrid) {
			if (min == null || cotation.getPrice().compareTo(min) < 0) {
				min = cotation.getPrice();
			}
			if (max == null || cotation.getPrice().compareTo(max) > 0) {
				max = cotation.getPrice();
			}
		}
		double _volat = (max - min) / min * 100;
		BigDecimal volatilite = new BigDecimal(String.valueOf(_volat)).setScale(2, RoundingMode.HALF_EVEN);		
		
		switch (period) {
			case _1h -> {
				BigDecimal var5m = null, var15m = null, var30m = null, var1h = null;
				int startIndex = findIndexForPeriodBackward(refCotation, cotationGrid, Period._5m);
				if (refIndex > startIndex) {
					double var = (refCotation.getPrice() - cotationGrid.get(startIndex).getPrice()) / cotationGrid.get(startIndex).getPrice() * 100;
					var5m = new BigDecimal(String.valueOf(var)).setScale(2, RoundingMode.HALF_EVEN);
				}					
				startIndex = findIndexForPeriodBackward(refCotation, cotationGrid, Period._15m);
				if (refIndex > startIndex) {
					double var = (refCotation.getPrice() - cotationGrid.get(startIndex).getPrice()) / cotationGrid.get(startIndex).getPrice() * 100;
					var15m = new BigDecimal(String.valueOf(var)).setScale(2, RoundingMode.HALF_EVEN);
				}					
				startIndex = findIndexForPeriodBackward(refCotation, cotationGrid, Period._30m);
				if (refIndex > startIndex) {
					double var = (refCotation.getPrice() - cotationGrid.get(startIndex).getPrice()) / cotationGrid.get(startIndex).getPrice() * 100;
					var30m = new BigDecimal(String.valueOf(var)).setScale(2, RoundingMode.HALF_EVEN);
				}
				startIndex = findIndexForPeriodBackward(refCotation, cotationGrid, Period._1h);
				if (refIndex > startIndex) {
					double var = (refCotation.getPrice() - cotationGrid.get(startIndex).getPrice()) / cotationGrid.get(startIndex).getPrice() * 100;
					var1h = new BigDecimal(String.valueOf(var)).setScale(2, RoundingMode.HALF_EVEN);
				}
				refCotation.min1h(min).max1h(max).volat1h(volatilite).var5m(var5m).var15m(var15m).var30m(var30m).var1h(var1h);
				
			}
			case _12h -> {
				BigDecimal var12h = null;
				int startIndex = findIndexForPeriodBackward(refCotation, cotationGrid, Period._12h);
				if (refIndex > startIndex) {
					double var = (refCotation.getPrice() - cotationGrid.get(startIndex).getPrice()) / cotationGrid.get(startIndex).getPrice() * 100;
					var12h = new BigDecimal(String.valueOf(var)).setScale(2, RoundingMode.HALF_EVEN);
				}							
				refCotation.min12h(min).max12h(max).volat12h(volatilite).var12h(var12h);
				
			}
			case _24h -> {
				BigDecimal var24h = null;
				int startIndex = findIndexForPeriodBackward(refCotation, cotationGrid, Period._24h);
				if (refIndex > startIndex) {
					double var = (refCotation.getPrice() - cotationGrid.get(startIndex).getPrice()) / cotationGrid.get(startIndex).getPrice() * 100;
					var24h = new BigDecimal(String.valueOf(var)).setScale(2, RoundingMode.HALF_EVEN);
				}
				refCotation.min24h(min).max24h(max).volat24h(volatilite).var24h(var24h);	
				
			}
			case _6j -> {
				BigDecimal var6j = null;
				int startIndex = findIndexForPeriodBackward(refCotation, cotationGrid, Period._6j);
				if (refIndex > startIndex) {
					double var = (refCotation.getPrice() - cotationGrid.get(startIndex).getPrice()) / cotationGrid.get(startIndex).getPrice() * 100;
					var6j = new BigDecimal(String.valueOf(var)).setScale(2, RoundingMode.HALF_EVEN);
				}							
				refCotation.min6j(min).max6j(max).volat6j(volatilite).var6j(var6j);	
				
			}

			default -> throw new IllegalArgumentException("Unexpected value: " + period);			

		}
		
		return refCotation;
	}

	
	
	List<Cotation>  getCotationGridOnPeriodBackward(Cotation refCotation, List<Cotation> allCotations, Period period) {
		int startIndex = 0;
		Collections.sort(allCotations);
		int endIndex = allCotations.indexOf(refCotation);
		if (endIndex < 0) {
			allCotations.add(refCotation);
			endIndex = allCotations.size()-1;
		}
		Date previousDate = previousDateForPeriod(refCotation.getDatetime(), period);
		if (previousDate != null) {
			startIndex = findIndexForDate(allCotations, previousDate, refCotation.getSymbol());
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		List<Cotation> subList = allCotations.subList(startIndex, endIndex+1);
		subList.removeIf(cotation -> !cotation.getSymbol().equals(refCotation.getSymbol()));
		return subList;	
	}
	
	List<Cotation> getCotationGridOnPeriodForward(Cotation refCotation, List<Cotation> allCotations, Period period) {
		int endIndex = -1;
		Collections.sort(allCotations);
		int startIndex = allCotations.indexOf(refCotation);
		if (startIndex >= 0) {
			Date nextDate = nextDateForPeriod(refCotation.getDatetime(), period);
			if (nextDate != null) {
				endIndex = findIndexForDate(allCotations, nextDate, refCotation.getSymbol());
			}
			List<Cotation> subList = allCotations.subList(startIndex, endIndex + 1);
			subList.removeIf(cotation -> !cotation.getSymbol().equals(refCotation.getSymbol()));
			return subList;
		}
		return null;
	}
	
	int findIndexForPeriodBackward(Cotation refCotation, List<Cotation> allCotations, Period period) {
		int index = 0;
		Collections.sort(allCotations);
		int endIndex = allCotations.indexOf(refCotation);
		if (endIndex < 0) {
			allCotations.add(refCotation);
			endIndex = allCotations.size()-1;
		}
		Date previousDate = previousDateForPeriod(refCotation.getDatetime(), period);
		if (previousDate != null) {
			index = findIndexForDate(allCotations, previousDate, refCotation.getSymbol());
		}
		if (index < 0) {
			index = 0;
		}
		return index;	
	}
	
	int findIndexForPeriodForward(Cotation refCotation, List<Cotation> allCotations, Period period) {
		int index = -1;
		Collections.sort(allCotations);
		int startIndex = allCotations.indexOf(refCotation);
		if (startIndex >= 0) {

			Date nextDate = nextDateForPeriod(refCotation.getDatetime(), period);
			if (nextDate != null) {
				index = findIndexForDate(allCotations, nextDate, refCotation.getSymbol());
			}
		}
		return index;	
	}
	
	int findIndexForDate(List<Cotation> cotations, Date refDate, String symbol) {
		int index = -1;
		if (refDate != null && cotations != null && symbol != null) {
			for (int i = 0; i < cotations.size(); i++) {
				Cotation cotation = cotations.get(i);
				if (symbol.equals(cotation.getSymbol())) {
					if (refDate.compareTo(cotation.getDatetime()) >= 0) {
						index = i; 
					} else {
						break;
					}
				}
			}
		}		
		return index;
	}
	


	public Cotation getCotationForTrade(Long idContrat) {
		return cryptobotRepository.getCotationForTrade(idContrat);
	}
	
	
	public Cotation getCotationForTrade(OrderQO orderQO) {
		PageData<Trade> pageTrades = cryptobotRepository.getTrades(orderQO, null);
		if (pageTrades != null) {
			List<Trade> trades = pageTrades.getData();
			if (trades.size() > 0) {
				Trade trade = trades.get(0);
				Cotation cotation = getCotationForTrade(trade.getId());
				if (cotation == null) {
					cotation = new Cotation().trade(trade);
				}
				return cotation;
			}
		}
		return null;
	}


}
