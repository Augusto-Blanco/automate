package net.jmb.cryptobot.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import jakarta.transaction.Transactional;
import net.jmb.cryptobot.data.bean.OrderQO;
import net.jmb.cryptobot.data.bean.PageData;
import net.jmb.cryptobot.data.entity.Asset;
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
	public Cotation initEvaluationForCotations(Asset asset, Date dateRef, boolean reset) {
		
		Cotation cotation = null;
		
		if (asset != null && asset.getSymbol() != null) {
			
			String symbol = asset.getSymbol();
			Period analysisPeriod = asset.getAnalysisPeriodEnum();
			if (analysisPeriod == null) {
				analysisPeriod = Period._24h;
			}
			Period frequencyPeriod = asset.getFrequencyPeriod();
			if (frequencyPeriod == null) {
				frequencyPeriod = Period._1h;
			}
		
			Assert.isTrue(frequencyPeriod.compareTo(analysisPeriod) < 0, "La fréquence d'évaluation doit être strictement inférieure à la période d'analyse");
			
			Cotation refCotation = cryptobotRepository.getLastCotationBeforeDate(symbol, dateRef);
			
			if (refCotation != null) {

				dateRef = refCotation.getDatetime();
				// pour initialiser l'achat on prend 2 fois la période d'analyse afin de déterminer le moment optimum d'achat AVANT le début de l'analyse
				Date startDate = previousDateForPeriod(previousDateForPeriod(dateRef, analysisPeriod), analysisPeriod);
				List<Cotation> dbCotations = cryptobotRepository.getCotationsSinceDate(symbol, startDate);
				// traitement sur liste copiée car pas de màj en base
				List<Cotation> cotations = new ArrayList<>(dbCotations.stream().map(Cotation::duplicate).toList()); 

				if (cotations != null && cotations.size() > 0) {
					
					List<AssetConfig> assetConfigList = new ArrayList<AssetConfig>();

					cotation = cotations.get(0);
					int startIndex = 0;
					
					// période juste avant la période d'analyse
					List<Cotation> cotationGrid = getCotationGridOnPeriodForward(cotation, cotations, analysisPeriod);
					
					// détermination cotation optimale à l'achat avant période d'analyse (prix minimum)
					Cotation minCotation = cotationGrid.stream().reduce( 
						(cot1, cot2) -> cot1.getPrice() < cot2.getPrice() ? cot1 : cot2
					).orElse(null);
					
					if (minCotation != null) {
						startIndex = cotations.indexOf(minCotation);
						minCotation.resetEvaluation();
					}
					
					// détermination grille d'analyse : celle qui précède juste la cotation de référence
					int refIndex = cotations.indexOf(refCotation);
					cotationGrid = cotations.subList(startIndex, refIndex + 1);					

					while (cotationGrid != null && cotationGrid.size() > 1) {
						AssetConfig bestAssetConfig = evaluateAssetConfigForCotations(cotationGrid, asset);
						cryptobotRepository.getAssetConfigRepository().deleteDateGreaterOrEquals(symbol, bestAssetConfig.getEndTime());
						cryptobotRepository.getAssetConfigRepository().save(bestAssetConfig);
						assetConfigList.add(bestAssetConfig);
						cotation = cotationGrid.get(cotationGrid.size() - 1);
						cotationGrid = getCotationGridOnPeriodForward(cotation, cotations, frequencyPeriod);
						if (cotationGrid != null && cotationGrid.size() > 1) {
							cotation = cotationGrid.get(cotationGrid.size() - 1);
							cotationGrid = getCotationGridOnPeriodBackward(cotation, cotations, analysisPeriod);						
						}
					}
					
					// mise à jour en base en recalculant à partir des évaluations précédentes et de la cotation de ref
					if (reset) {
						cotation = cotations.get(refIndex);
						refCotation = dbCotations.get(refIndex).resetEvaluation();
					}
					recordEvaluationsForCotations(dbCotations.subList(refIndex, dbCotations.size()), asset, assetConfigList);
				}
			}
		}
		return cotation;
	}
	
	
	@Transactional
	public void recordEvaluationsForCotations(List<Cotation> cotations, Asset asset, List<AssetConfig> assetConfigList) {
		
		Map<AssetConfig, List<Cotation>> map = new Hashtable<AssetConfig, List<Cotation>>();
		
		for (Cotation cotation : cotations) {
			AssetConfig assetConfigForCotation = null;
			for (AssetConfig assetConfig : assetConfigList) {				
				if (assetConfig.getEndTime().compareTo(cotation.getDatetime()) <= 0) {
					if (assetConfigForCotation == null || assetConfig.getEndTime().compareTo(assetConfigForCotation.getEndTime()) > 0) {
						assetConfigForCotation = assetConfig;
					}
				} else {
					break;
				}
			}
			if (assetConfigForCotation != null) {
				List<Cotation> cotationList = map.get(assetConfigForCotation);
				if (cotationList == null) {
					cotationList = new ArrayList<Cotation>();
					map.put(assetConfigForCotation, cotationList);
				}
				cotationList.add(cotation);
			}
		}
		
		Cotation lastCotation = null;
		for (AssetConfig assetConfig : assetConfigList) {
			List<Cotation> cotationList = map.get(assetConfig);
			if (lastCotation != null) {
				cotationList.add(0, lastCotation);
			}
			evaluateTradesForCotations(cotationList, asset, assetConfig.realEval(true));
			lastCotation = cotationList.get(cotationList.size() - 1);
		}
	}



//	@Transactional
//	public Cotation evaluateTradesForCotations(Asset asset, Date dateRef, Boolean init, Period analysisPeriod, Period frequency) {
//		
//		Cotation cotation = null;
//		
//		if (asset != null && asset.getSymbol() != null) {
//			
//			String symbol = asset.getSymbol();			
//			boolean reset = init != null ? init : false;
//			
//			Period subPeriod = (analysisPeriod != null) ? analysisPeriod : Period._12h;
//			Period interval = (frequency != null) ? frequency : Period._1h;
//			
//			Cotation refCotation = cryptobotRepository.getLastCotationBeforeDate(symbol, dateRef);
//			
//			if (refCotation != null) {
//
//				dateRef = refCotation.getDatetime();
//				if (reset) {
//					refCotation.resetEvaluation();
//				}
//
//				Date startDate = previousDateForPeriod(dateRef, subPeriod);
//				List<Cotation> dbCotations = cryptobotRepository.getCotationsSinceDate(symbol, startDate);
//
//				if (dbCotations != null && dbCotations.size() > 0) {
//
//					cotation = dbCotations.get(0);
//					List<Cotation> dbCotationGrid = getCotationGridOnPeriodForward(cotation, dbCotations, subPeriod);
//					int refIndex = dbCotationGrid.indexOf(refCotation);
//					if (refIndex > -1) {
//						dbCotationGrid = dbCotationGrid.subList(0, refIndex + 1);
//					}
//					List<Cotation> cotationGrid = new ArrayList<>(dbCotationGrid.stream().map(Cotation::duplicate).toList());
//
//					while (dbCotationGrid != null && dbCotationGrid.size() > 1) {
//
//						AssetConfig bestAssetConfig = evaluateAssetConfigForCotations(cotationGrid, asset);
//						cryptobotRepository.getAssetConfigRepository().deleteDateGreaterOrEquals(symbol, bestAssetConfig.getStartTime());
//						cryptobotRepository.getAssetConfigRepository().save(bestAssetConfig);
//
//						cotation = dbCotationGrid.get(dbCotationGrid.size() - 1);
//						dbCotationGrid = getCotationGridOnPeriodForward(cotation, dbCotations, interval);
//						if (dbCotationGrid != null && dbCotationGrid.size() > 1) {
//							cotation = evaluateTradesForCotations(dbCotationGrid, asset, bestAssetConfig.realEval(true));
//							dbCotationGrid = getCotationGridOnPeriodBackward(cotation, dbCotations, subPeriod);
//							cotationGrid = new ArrayList<>(dbCotationGrid.stream().map(Cotation::duplicate).toList());
//						}
//					}
//
//				}
//			}
//		}
//		return cotation;
//	}

	
	public AssetConfig evaluateAssetConfigForCotations(List<Cotation> cotationGrid, Asset asset) {
		
		Cotation cotation = null;
		AssetConfig assetConfig = null;
		
		if (cotationGrid != null && cotationGrid.size() > 0 && asset != null) {
			
			Double lowLimit = asset.getVarLowLimit().doubleValue();
			Double highLimit = asset.getVarHighLimit().doubleValue();
			Double maxStopLoss = asset.getStopLossLimit().doubleValue();
			
			BigDecimal amountB100 = null;
			Double maxVarHigh = lowLimit, maxVarLow = lowLimit, stopLoss = 2d;
			Double bestVarHigh = null, bestVarLow = null, bestStopLoss = null;

			while (stopLoss <= maxStopLoss) {
				while (maxVarHigh <= highLimit) {
					while (maxVarLow <= highLimit) {
						cotation = evaluateTradesForCotations(cotationGrid, asset, maxVarHigh, maxVarLow, stopLoss);
						if (amountB100 == null || cotation.getAmountB100().compareTo(amountB100) > 0) {
							bestVarHigh = maxVarHigh;
							bestVarLow = maxVarLow;
							bestStopLoss = stopLoss;
							amountB100 = cotation.getAmountB100();
						}
						maxVarLow += 0.1d;
					}
					maxVarLow = lowLimit;
					maxVarHigh += 0.1d;
				}
				maxVarHigh = maxVarLow = lowLimit;
				stopLoss += 0.1d;
			}
			// on compare à si on ne fait rien (variation 100% requise)
			maxVarHigh = maxVarLow = 100d;
			stopLoss = 2d;
			cotation = evaluateTradesForCotations(cotationGrid, asset, maxVarHigh, maxVarLow, stopLoss);
			if (amountB100 == null || cotation.getAmountB100().compareTo(amountB100) > 0) {
				bestVarHigh = bestVarLow = 100d;
				bestStopLoss = stopLoss;
				amountB100 = cotation.getAmountB100();
			}
			
			if (bestVarHigh != null && bestVarLow != null && bestStopLoss != null) {
				cotation = evaluateTradesForCotations(cotationGrid, asset, bestVarHigh, bestVarLow, bestStopLoss);
				assetConfig = new AssetConfig()
					.symbol(cotation.getSymbol())
					.startTime(cotationGrid.get(0).getDatetime())
					.endTime(cotation.getDatetime())
					.maxVarHigh(BigDecimal.valueOf(bestVarHigh).setScale(2, RoundingMode.HALF_EVEN))
					.maxVarLow(BigDecimal.valueOf(bestVarLow).setScale(2, RoundingMode.HALF_EVEN))
					.stopLoss(BigDecimal.valueOf(bestStopLoss).setScale(2, RoundingMode.HALF_EVEN));
			}
		}
		return assetConfig;
	} 
	
	
	public Cotation evaluateTradesForCotations(List<Cotation> cotationGrid, Asset asset, AssetConfig assetConfig) {
		
		Cotation cotation = null;
		if (assetConfig != null) {
			Double maxVarHigh = assetConfig.getMaxVarHigh() != null ? assetConfig.getMaxVarHigh().doubleValue() : 1000d;
			Double maxVarLow = assetConfig.getMaxVarLow() != null ? assetConfig.getMaxVarLow().doubleValue() : 1000d;
			Double stopLoss = assetConfig.getStopLoss() != null ? assetConfig.getStopLoss().doubleValue() : 1000d;
			cotation = evaluateTradesForCotations(cotationGrid, asset, maxVarHigh, maxVarLow, stopLoss, assetConfig.isRealEval());

			getLogger().info(assetConfig.toString());
			cotationGrid.forEach( cot -> getLogger().info(cot.toString()) );			
			getLogger().info("");
		}		
		return cotation;
	}
	
	public Cotation evaluateTradesForCotations(List<Cotation> cotationGrid, Asset asset, Double maxVarHigh, Double maxVarLow, Double stopLoss) {
		return evaluateTradesForCotations(cotationGrid, asset, maxVarHigh, maxVarLow, stopLoss, false);
	}
	
	
	public Cotation evaluateTradesForCotations(List<Cotation> cotationGrid, Asset asset, Double maxVarHigh, Double maxVarLow, Double stopLoss, boolean realEval) {
		
		double fees = (asset != null && asset.getFeesRate() != null) ? asset.getFeesRate().doubleValue() / 100 : 0.005d;
		long delayBetweenTrades = (asset.getTradeDelay() != null ? asset.getTradeDelay() : 10) * 60 * 1000; 
		
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
					amountB100 = 100d;
					quantity = amountB100 / price;
					cotation.flagBuy().currentSide(OrderSide.BUY).buyPrice(price).bestBuyPrice(price).sellPrice(null).bestSellPrice(null)
							.quantity(quantity).amountB100(BigDecimal.valueOf(amountB100));
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
				if (sellPrice == null) {
					sellPrice = cotation.getSellPrice();
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
						quantity = cotation.getQuantity();
					}
				}
				
				if (currentSide.equals(OrderSide.BUY)) {
					Double deltaPrice = (cotation.getPrice() - buyPrice) / buyPrice *100;
					Double deltaFromBestBuy = (cotation.getPrice() - bestBuyPrice) / bestBuyPrice * 100;
					amountB100 = quantity * cotation.getPrice();
					boolean isDelayOK = (lastBuy == null || cotation.getDatetime().getTime() - lastBuy.getTime() > delayBetweenTrades);
					if (deltaFromBestBuy > maxVarHigh && isDelayOK || deltaPrice <= -stopLoss) {
						currentSide = OrderSide.SELL;	
						quantity = 0d;
						amountB100 = amountB100 * (1 - fees);
						sellPrice = cotation.getPrice();
						bestSellPrice = cotation.getPrice();
						cotation.flagSell();
						lastSell = cotation.getDatetime();
						
						if (realEval) {
							cotation.currentSide(currentSide).sellPrice(sellPrice).buyPrice(buyPrice).bestBuyPrice(bestBuyPrice).bestSellPrice(bestSellPrice)
									.quantity(quantity).amountB100(BigDecimal.valueOf(amountB100).setScale(2, RoundingMode.HALF_EVEN));
							String message = "-- Vente ";
							if (deltaPrice <= -stopLoss) {
								message += "stopLoss " +  BigDecimal.valueOf(deltaPrice).setScale(1, RoundingMode.HALF_EVEN);
							} else {
								message += "takeProfit " + BigDecimal.valueOf(deltaFromBestBuy).setScale(1, RoundingMode.HALF_EVEN);
							}
							getLogger().info(message);
							getLogger().info(cotation.toString());
							getLogger().info("");
						}
					}

				} else if (currentSide.equals(OrderSide.SELL)) {
					Double deltaFromBestSell = (cotation.getPrice() - bestSellPrice) / bestSellPrice * 100;
					boolean isDelayOK = (lastSell == null || cotation.getDatetime().getTime() - lastSell.getTime() > delayBetweenTrades);
						
					if (deltaFromBestSell < -maxVarLow && isDelayOK) {
						currentSide = OrderSide.BUY;
						buyPrice = cotation.getPrice();
						bestBuyPrice = cotation.getPrice();
						amountB100 = amountB100 * (1 - fees);
						quantity = amountB100 / cotation.getPrice();
						cotation.flagBuy();
						lastBuy = cotation.getDatetime();
						
						if (realEval) {
							cotation.currentSide(currentSide).sellPrice(sellPrice).buyPrice(buyPrice).bestBuyPrice(bestBuyPrice).bestSellPrice(bestSellPrice)
									.quantity(quantity).amountB100(BigDecimal.valueOf(amountB100).setScale(2, RoundingMode.HALF_EVEN));
							String message = "-- Achat delta vente " + BigDecimal.valueOf(deltaFromBestSell).setScale(1, RoundingMode.HALF_EVEN);							
							getLogger().info(message);
							getLogger().info(cotation.toString());
							getLogger().info("");
						}						
					}
				}				
				if (bestBuyPrice == null || cotation.getPrice() < bestBuyPrice) {
					bestBuyPrice = cotation.getPrice();
				}
				if (bestSellPrice == null || cotation.getPrice() > bestSellPrice) {
					bestSellPrice = cotation.getPrice();
				}	
				
				// la cotation initiale est la référence de calcul pour les autres : elle ne doit pas être mise à jour
				if (i > 0) {
					cotation.currentSide(currentSide).sellPrice(sellPrice).buyPrice(buyPrice).bestBuyPrice(bestBuyPrice).bestSellPrice(bestSellPrice)
							.quantity(quantity).amountB100(BigDecimal.valueOf(amountB100).setScale(2, RoundingMode.HALF_EVEN));
				}
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
	
	public Cotation findCotationForDate(List<Cotation> cotations, Date refDate, String symbol) {
		int index = findIndexForDate(cotations, refDate, symbol);
		if (index > -1) {
			return cotations.get(index);
		}
		return null;
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
