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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import net.jmb.cryptobot.data.entity.Asset;
import net.jmb.cryptobot.data.entity.AssetConfig;
import net.jmb.cryptobot.data.entity.Cotation;
import net.jmb.cryptobot.data.enums.ModeEval;
import net.jmb.cryptobot.data.enums.OrderSide;
import net.jmb.cryptobot.data.enums.Period;
import net.jmb.cryptobot.data.repository.CryptobotRepository;
import net.jmb.cryptobot.enums.ParamContext;
import net.jmb.cryptobot.util.PeriodUtil;

@Service
public class CotationService extends CommonService {
	
	public static final ParamContext CONTEXTE = ParamContext.TOUT_CONTEXTE;
	
	@Autowired
	CryptobotRepository cryptobotRepository;
		
	
	@Transactional
	public Cotation evaluateLastCotations(Asset asset, Date initDate) {		
		if (initDate != null) {			
			return evaluateCotations(asset, initDate, true);			
		} else {	
			Cotation refCotation = cryptobotRepository.getLastRatedCotation(asset.getSymbol());
			if (refCotation != null) {
				return evaluateCotations(asset, refCotation.getDatetime(), false);	
			} else {
				refCotation = cryptobotRepository.getMin24hCotationAfterDate(asset.getSymbol(), PeriodUtil.previousDateForPeriod(new Date(), asset.getAnalysisPeriodEnum()));
				if (refCotation != null) {
					return evaluateCotations(asset, refCotation.getDatetime(), true);
				}
			}
		}
		return null;
	}
	
	
	@Transactional
	public Cotation resetEvaluationForAsset(Asset asset, Period period) {
		Cotation refCotation = null;
		if (period == null) {
			period = Period._48h;
		}
		Date today = new Date();
		Date startDate = PeriodUtil.previousDateForPeriod(today, period);
		if (period.compareTo(Period._48h) > 0) {
			refCotation = cryptobotRepository.getMinCotationBetweenDates(asset.getSymbol(), startDate, PeriodUtil.previousDateForPeriod(today, Period._48h));
		} else {
			refCotation = cryptobotRepository.getMin24hCotationAfterDate(asset.getSymbol(), startDate);
		}
		if (refCotation != null) {
			return evaluateCotations(asset, refCotation.getDatetime(), true);
		}
		return null;
	}
	
	
	@Transactional
	public Cotation evaluateCotations(Asset asset, Date dateRef, boolean reset) {
		
		Cotation refCotation = null;
		
		if (asset != null && asset.getSymbol() != null) {

			String symbol = asset.getSymbol();
			Period analysisPeriod = asset.getAnalysisPeriodEnum();
			if (analysisPeriod == null) {
				analysisPeriod = Period._6j;
			}		
			boolean longTimeAnalysis =  true;			
			refCotation = cryptobotRepository.getLastCotationBeforeDate(symbol, dateRef, longTimeAnalysis);
			
			cryptobotRepository.getAssetConfigRepository().deleteDateGreaterOrEquals(asset.getSymbol(), dateRef, null);
			
			while (analysisPeriod != null && analysisPeriod.compareTo(Period._24h) >= 0) {
				if (analysisPeriod.compareTo(Period._6j) < 0) {
					longTimeAnalysis = false;
				}
				refCotation = initEvaluationForAnalysisPeriod(asset, refCotation, analysisPeriod, longTimeAnalysis, reset);
				if (analysisPeriod.compareTo(Period._6j) > 0) {
					analysisPeriod = Period._6j;
				} else if (analysisPeriod.compareTo(Period._24h) > 0) {
					analysisPeriod = Period._24h;
				} else {
					analysisPeriod = null;
				}
				reset = false;
			}			
		}
		return refCotation;
	}
	
	@Transactional
	protected Cotation initEvaluationForAnalysisPeriod(Asset asset, Cotation refCotation, Period analysisPeriod, boolean longTimeAnalysis, boolean reset) {
		
		Cotation cotation = null;
		
		if (asset != null && asset.getSymbol() != null) {
			
			Period frequencyPeriod = Period._1h;
			if (analysisPeriod.compareTo(Period._6j) >= 0) {
				frequencyPeriod = Period._24h;
			} else if (analysisPeriod.compareTo(Period._48h) >= 0) {
				frequencyPeriod = Period._6h;
			}

			if (refCotation != null) {

				Date dateRef = refCotation.getDatetime();
				Date startDate = PeriodUtil.previousDateForPeriod(dateRef, analysisPeriod);
				
				// pour initialiser l'achat on prend 2 fois la période d'analyse afin de déterminer le moment optimum d'achat AVANT le début de l'analyse
				Date prevDate = PeriodUtil.previousDateForPeriod(startDate, analysisPeriod);
				if (prevDate != null) {
					Cotation minCotation = cryptobotRepository.getMinCotationBetweenDates(asset.getSymbol(), prevDate, startDate);
					if (minCotation != null) {
						startDate = minCotation.getDatetime();
					}
				}				
				
				List<Cotation> dbCotations = cryptobotRepository.getCotationsSinceDate(asset.getSymbol(), startDate, longTimeAnalysis);				
				
				if (dbCotations != null && dbCotations.size() > 0) {
					
					List<AssetConfig> assetConfigList = new ArrayList<AssetConfig>();
					
					// traitement sur liste copiée car pas de màj en base
					List<Cotation> cotations = new ArrayList<>(dbCotations.stream().map(Cotation::duplicate).toList()); 
					
					// grille d'analyse : celle qui précède juste la cotation de référence
					List<Cotation> cotationGrid = null;
					int refIndex = cotations.indexOf(refCotation);	
					if (refIndex > 0) {
						cotations.get(0).resetEvaluation();	
						cotationGrid = cotations.subList(0, refIndex + 1);					
					}

					while (cotationGrid != null && cotationGrid.size() > 1) {
						AssetConfig bestAssetConfig = evaluateAssetConfigForCotations(cotationGrid, asset, longTimeAnalysis).analysisPeriod(analysisPeriod.val);
//						cryptobotRepository.getAssetConfigRepository().deleteDateGreaterOrEquals(asset.getSymbol(), bestAssetConfig.getEndTime(), analysisPeriod.val);
						cryptobotRepository.getAssetConfigRepository().save(bestAssetConfig);
						assetConfigList.add(bestAssetConfig);
						cotation = cotationGrid.get(cotationGrid.size() - 1);
						cotationGrid = getCotationGridOnPeriodForward(cotation, cotations, frequencyPeriod);
						if (cotationGrid != null && cotationGrid.size() > 1) {
							cotation = cotationGrid.get(cotationGrid.size() - 1);
							cotationGrid = getCotationGridOnPeriodBackward(cotation, cotations, analysisPeriod);						
						}
					}
					
					// recalcul en base à partir des évaluations précédentes et de la cotation de ref					
					if (longTimeAnalysis) {
						dbCotations = cryptobotRepository.getCotationsSinceDate(asset.getSymbol(), startDate);
					}
					refIndex = dbCotations.indexOf(refCotation);
					if (refIndex >= 0) {
						if (reset) {
							dbCotations.get(refIndex).resetEvaluation();
						}
						recordEvaluationsForCotations(dbCotations.subList(refIndex, dbCotations.size()), asset, assetConfigList);
					}
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
				checkAndResetLossForCotation(asset, lastCotation);
				cotationList.add(0, lastCotation);
			}
			evaluateTradesForCotations(cotationList, asset, assetConfig.modeEval(ModeEval.RECORD));
			lastCotation = cotationList.get(cotationList.size() - 1);
		}
		getLogger().info("");
	}



	public AssetConfig evaluateAssetConfigForCotations(List<Cotation> cotationGrid, Asset asset, boolean longTime) {
		
		Cotation cotation = null;
		AssetConfig assetConfig = null;
		
		if (cotationGrid != null && cotationGrid.size() > 0 && asset != null) {
			
			Double lowLimit = asset.getVarLowLimit();
			Double highLimit = asset.getVarHighLimit();
			
			BigDecimal amountB100 = null;
			Double maxVarHigh = lowLimit, maxVarLow = lowLimit;
			Double bestVarHigh = null, bestVarLow = null;

			while (maxVarHigh <= highLimit) {
				while (maxVarLow <= highLimit) {
					cotation = evaluateTradesForCotations(cotationGrid, asset, maxVarHigh, maxVarLow);
					if (amountB100 == null || cotation.getAmountB100().compareTo(amountB100) > 0) {
						bestVarHigh = maxVarHigh;
						bestVarLow = maxVarLow;
						amountB100 = cotation.getAmountB100();
					}
					maxVarLow += longTime ? 0.5d : 0.1d;
				}
				maxVarLow = lowLimit;
				maxVarHigh += longTime ? 0.5d : 0.1d;
			}

			// on compare à si on ne fait rien (variation 100% requise)
			maxVarHigh = maxVarLow = 100d;

			cotation = evaluateTradesForCotations(cotationGrid, asset, maxVarHigh, maxVarLow);
			if (amountB100 == null || cotation.getAmountB100().doubleValue() >= 0.999 * amountB100.doubleValue()) {
				bestVarHigh = bestVarLow = 100d;
				amountB100 = cotation.getAmountB100();
			}
			
			if (bestVarHigh != null && bestVarLow != null) {
				cotation = evaluateTradesForCotations(cotationGrid, asset, bestVarHigh, bestVarLow);
				assetConfig = new AssetConfig()
					.symbol(cotation.getSymbol())
					.startTime(cotationGrid.get(0).getDatetime())
					.endTime(cotation.getDatetime())
					.maxVarHigh(BigDecimal.valueOf(bestVarHigh).setScale(2, RoundingMode.HALF_EVEN))
					.maxVarLow(BigDecimal.valueOf(bestVarLow).setScale(2, RoundingMode.HALF_EVEN));
			}
		}
		return assetConfig;
	} 
	

	
	public Cotation evaluateTradesForCotations(List<Cotation> cotationGrid, Asset asset, Double maxVarHigh, Double maxVarLow) {
		AssetConfig assetConfig = new AssetConfig().maxVarHigh(BigDecimal.valueOf(maxVarHigh)).maxVarLow(BigDecimal.valueOf(maxVarLow)).stopLoss(BigDecimal.valueOf(100)).modeEval(ModeEval.EVAL);
		return evaluateTradesForCotations(cotationGrid, asset, assetConfig);
	}
	
	
	public Cotation evaluateTradesForCotations(List<Cotation> cotationGrid, Asset asset, AssetConfig assetConfig) {

		Cotation cotation = null;
		
		if (cotationGrid != null && asset != null && assetConfig != null) {
			
			Double maxVarHigh = assetConfig.getMaxVarHigh() != null ? assetConfig.getMaxVarHigh().doubleValue() : 100d;
			Double maxVarLow = assetConfig.getMaxVarLow() != null ? assetConfig.getMaxVarLow().doubleValue() : 1d;
			Double stopLoss = assetConfig.getStopLoss() != null ? assetConfig.getStopLoss().doubleValue() : maxVarHigh >= maxVarLow ? asset.getStopLossLimit() : asset.getStopLossStart();
			boolean realEval = assetConfig.isRealEval();
			
			if (realEval) {
				getLogger().info(assetConfig.toString());
			}
			
			double fees = (asset.getFeesRate() != null) ? asset.getFeesRate().doubleValue() / 100 : 0.005d;
			Double maxPercentLoss = (asset.getMaxPercentLoss() != null ? asset.getMaxPercentLoss() : 5);
			
			Integer nbLoss = null;
			Boolean stopTrading = null, canResetBestSellPrice = null, canResetBestBuyPrice = null;			
			Double bestSellPrice = null, sellPrice = null, bestBuyPrice = null, prevBestBuyPrice = null, antePrevBestBuy = null, 
					buyPrice = null, quantity = null, amountB100 = null, percentLoss = null;
			OrderSide currentSide = null;	
			
			for (int i = 0; i < cotationGrid.size(); i++) {
				
				cotation = cotationGrid.get(i);
				
				if (i > 0) {
					cotation.flagBuy(null).flagSell(null);
				}
				
				if (currentSide == null && StringUtils.isBlank(cotation.getCurrentSide())) {
					Double price = cotation.getPrice();
					amountB100 = 100d;
					quantity = amountB100 / price;
					cotation.flagBuy().nbLoss(0).percentLoss(0d).canResetBestSellPrice(true).canResetBestBuyPrice(false).currentSide(OrderSide.BUY).buyPrice(price)
							.bestBuyPrice(price).sellPrice(null).bestSellPrice(null).quantity(quantity).amountB100(BigDecimal.valueOf(amountB100));
				}
				
				if (nbLoss == null) {
					nbLoss = cotation.getNbLoss() != null ? cotation.getNbLoss() : 0;
				}
				if (percentLoss == null) {
					percentLoss = cotation.getPercentLoss() != null ? cotation.getPercentLoss() : 0d;
				}
				if (canResetBestSellPrice == null) {
					canResetBestSellPrice = cotation.canResetBestSellPrice();
				}
				if (canResetBestBuyPrice == null) {
					canResetBestBuyPrice = cotation.canResetBestBuyPrice();
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
				if (prevBestBuyPrice == null) {
					prevBestBuyPrice = cotation.getPrevBestBuyPrice();
				}
				if (antePrevBestBuy == null) {
					antePrevBestBuy = cotation.getAntePrevBestBuy();
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
				
				boolean positiveVar5m = (cotation.getVar5m() != null && cotation.getVar5m().doubleValue() >= 0d);
				boolean positiveVar15m = (cotation.getVar15m() != null && cotation.getVar15m().doubleValue() > 0d);
				boolean positiveVar30m = (cotation.getVar30m() != null && cotation.getVar30m().doubleValue() > 0d);

				stopTrading = (maxVarHigh == 100d && maxVarLow == 100d);
				
				// évaluation achat-vente uniquement si la cotation n'est pas celle de référence
				// la cotation initiale est la référence de calcul pour les autres : elle ne doit pas être mise à jour
				if (i > 0) {
					
					cotation.nbLoss(nbLoss).percentLoss(percentLoss).canResetBestSellPrice(canResetBestSellPrice).canResetBestBuyPrice(canResetBestBuyPrice).currentSide(currentSide)
							.sellPrice(sellPrice).buyPrice(buyPrice).bestBuyPrice(bestBuyPrice).bestSellPrice(bestSellPrice).prevBestBuyPrice(prevBestBuyPrice).antePrevBestBuy(antePrevBestBuy)
							.quantity(quantity).amountB100(BigDecimal.valueOf(amountB100).setScale(2, RoundingMode.HALF_EVEN));
					
					Double deltaFromBestBuy = (cotation.getPrice() - bestBuyPrice) / bestBuyPrice * 100;
					Double deltaFromBestSell = (bestSellPrice != null && bestSellPrice > 0d) ? ((cotation.getPrice() - bestSellPrice) / bestSellPrice * 100) : 0d;
					
					boolean isResetBuy = false;
					boolean evaluateSell = currentSide.equals(OrderSide.BUY);
					boolean evaluateBuy = currentSide.equals(OrderSide.SELL);
					
					if (evaluateSell) {
						
						Double deltaPrice = (cotation.getPrice() - buyPrice) / buyPrice * 100;
						amountB100 = quantity * cotation.getPrice();						
						boolean positiveDeltaPrice = deltaPrice >= asset.getVarLowLimit();
						boolean positiveSellCondition = positiveDeltaPrice && deltaFromBestBuy >= maxVarHigh ;
						if (realEval) {
							positiveSellCondition |= positiveDeltaPrice && (deltaFromBestBuy >= 0.95 * maxVarHigh && !positiveVar5m || stopTrading);
						}
						boolean negativeSellCondition = realEval && (deltaPrice <= -stopLoss || percentLoss <= -maxPercentLoss);
						
						if (realEval) {
							getLogger().info("Delta / meilleur prix achat : " + new BigDecimal(deltaFromBestBuy).setScale(1, RoundingMode.HALF_EVEN) + "%");
							getLogger().info("Delta / dernier prix achat : " + new BigDecimal(deltaPrice).setScale(1, RoundingMode.HALF_EVEN) + "%");
						}
						
						if (positiveSellCondition || negativeSellCondition) {							
							currentSide = OrderSide.SELL;
							quantity = 0d;
							amountB100 = amountB100 * (1 - fees);
							sellPrice = cotation.getPrice();
							cotation.flagSell();
							evaluateBuy = false;							
							if (deltaPrice > 0d) {								
								bestSellPrice = cotation.getPrice();
								canResetBestSellPrice = false;
								canResetBestBuyPrice = true;
							}							
							if (realEval) {								
								if (deltaPrice >= 0d) {
									nbLoss = 0;
									percentLoss = 0d;
								} else {
									nbLoss++;
									percentLoss += deltaPrice;									
								}								
								String message = "Vente " + cotation.getSymbol() + ": ";								
								if (deltaPrice > 0d) {
									message += "Take Profit => " + BigDecimal.valueOf(deltaFromBestBuy).setScale(1, RoundingMode.HALF_EVEN) + "%";
								} else if (percentLoss <= -maxPercentLoss) {
									message += "Percent Loss (max " + maxPercentLoss + ") => "	+ BigDecimal.valueOf(percentLoss).setScale(1, RoundingMode.HALF_EVEN) + "%";
								} else {
									message += nbLoss + " Stop Loss (" + stopLoss + ") => "	+ BigDecimal.valueOf(deltaPrice).setScale(1, RoundingMode.HALF_EVEN) + "%";
								}								
								getLogger().info(message);
								getLogger().info("-- " + cotation.toString());
							}
						}	
					}
					
					if (evaluateBuy) {
						
						if (realEval) {
							String msgEvalFromBestSell = "Delta / meilleur prix vente : " + new BigDecimal(deltaFromBestSell).setScale(1, RoundingMode.HALF_EVEN) + "%";
							getLogger().info(msgEvalFromBestSell);
						}
							
						if (deltaFromBestSell <= -maxVarLow && !stopTrading) {
							// on tente de sécuriser l'achat au maximum en fonction de la tendance et des pertes déjà subies
							boolean isTrendOK = isTrendOK(cotation, asset, realEval);
							boolean positiveVar = positiveVar5m;
							if (nbLoss > 0) {
								positiveVar &= positiveVar15m && positiveVar30m;
							}
							if (nbLoss > 1 || realEval && percentLoss <= -maxPercentLoss) {
								positiveVar &= (maxVarHigh > maxVarLow);
							}							
							if (isTrendOK && positiveVar) {
//							if (nbLoss == 0 && isTrendOK || (nbLoss == 0 || isTrendOK) && positiveVar) {								
								currentSide = OrderSide.BUY;
								buyPrice = cotation.getPrice();
								if (cotation.getPrice() < bestBuyPrice || Boolean.TRUE.equals(canResetBestBuyPrice)) { 
									antePrevBestBuy = prevBestBuyPrice;
									prevBestBuyPrice = bestBuyPrice;
									bestBuyPrice = cotation.getPrice();
									isResetBuy = true;
								}
								deltaFromBestBuy = 0d;
								amountB100 = amountB100 * (1 - fees);
								quantity = amountB100 / cotation.getPrice();
								cotation.flagBuy();
								canResetBestSellPrice = true;
								canResetBestBuyPrice = false;								
								if (realEval) {
									String message = "Achat " + cotation.getSymbol() + ": delta vente " + BigDecimal.valueOf(deltaFromBestSell).setScale(1, RoundingMode.HALF_EVEN) + "%";
									getLogger().info(message);
									message	= "-- Stop loss: " + (nbLoss > 0) + " -- Trend OK: " + isTrendOK + " -- Var 15min: " + cotation.getVar15m() 
											+ " -- Var 30min: " + cotation.getVar30m();
									getLogger().info(message);
									getLogger().info("-- " + cotation.toString());
								}								
							}
						}
					}
					
					if (bestBuyPrice == null || cotation.getPrice() < bestBuyPrice) {
						bestBuyPrice = cotation.getPrice();
						canResetBestBuyPrice = false;
						canResetBestSellPrice = true;
					} else if (!realEval && Boolean.TRUE.equals(canResetBestBuyPrice) && deltaFromBestSell <= -maxVarLow) {
						isResetBuy = true;
						antePrevBestBuy = cotation.getPrevBestBuyPrice();
						prevBestBuyPrice = cotation.getBestBuyPrice();
						bestBuyPrice = cotation.getPrice();
						canResetBestBuyPrice = false;
						canResetBestSellPrice = true;
					}					
					if (bestSellPrice == null || cotation.getPrice() > bestSellPrice
							|| !realEval && Boolean.TRUE.equals(canResetBestSellPrice) && deltaFromBestBuy >= maxVarHigh && !isResetBuy) {						
						bestSellPrice = cotation.getPrice();
						canResetBestSellPrice = false;
						canResetBestBuyPrice = true;
					}
					cotation.nbLoss(nbLoss).percentLoss(percentLoss).canResetBestSellPrice(canResetBestSellPrice).canResetBestBuyPrice(canResetBestBuyPrice).currentSide(currentSide)
							.sellPrice(sellPrice).buyPrice(buyPrice).bestBuyPrice(bestBuyPrice).bestSellPrice(bestSellPrice).prevBestBuyPrice(prevBestBuyPrice).antePrevBestBuy(antePrevBestBuy)
							.quantity(quantity).amountB100(BigDecimal.valueOf(amountB100).setScale(2, RoundingMode.HALF_EVEN));					
				}
			}
			
			if (realEval) {
				cotationGrid.forEach( cot -> getLogger().info(cot.toString()) );
				getLogger().info("");
			}
		}		
		return cotation;
	}
	
	
	private boolean isTrendOK(Cotation cotation, Asset asset, boolean realEval) {
		boolean trendOK = true;
		if (realEval && OrderSide.SELL.equals(cotation.getCurrentOrderSide())) {
			Double gapFromTrend = asset.getGapFromTrend();
			if (gapFromTrend != null && gapFromTrend >= 0) {
				Double prevBestBuyPrice = cotation.getPrevBestBuyPrice();
				if (prevBestBuyPrice != null && cotation.getBestBuyPrice() != null) {
					Double actualBestBuyPrice = cotation.getBestBuyPrice();
					Double antePrevBestBuy = cotation.getAntePrevBestBuy();
					Double trend = (actualBestBuyPrice - prevBestBuyPrice) / prevBestBuyPrice;
					if (antePrevBestBuy != null && antePrevBestBuy > 0d) {
						Double test = (prevBestBuyPrice - antePrevBestBuy) / antePrevBestBuy;
						if (trend < test && test < 0d) {
							trend = test;
						}
					}
					Double estimatedBuyPrice;
					if (trend > 0 || cotation.getVar12h().floatValue() < 0) {
						estimatedBuyPrice = actualBestBuyPrice * (1 + trend);
					} else {
						estimatedBuyPrice = actualBestBuyPrice;
					}
					Double price = cotation.getPrice();
					Double gapBetweenPrices = (price - estimatedBuyPrice) / estimatedBuyPrice * 100;
					if (gapBetweenPrices > gapFromTrend) {
						trendOK = false;
						String message = "-- Trend KO pour " + cotation.getSymbol() + " à " + cotation.getDatetime();
						getLogger().info(message);
						message	= " -- trend: " + trend * 100 + "%" + " -- estimated buy price: " + estimatedBuyPrice
								+ " -- actual price: " + price + " -- gap: " + gapBetweenPrices;
						getLogger().info(message);						
					} else {
						String message = "-- Trend OK pour " + cotation.getSymbol() + " à " + cotation.getDatetime();
						getLogger().info(message);
						message	= " -- estimated buy price: " + estimatedBuyPrice + " -- actual price: " + price;
						getLogger().info(message);
						getLogger().info("");
					}
				}				
			}
		}
		return trendOK;
	}
	
	
	@Transactional
	public List<Cotation> registerNewCotations(String symbol, List<Cotation> cotationsList) {
		
		List<Cotation> newCotations = new ArrayList<Cotation>();
		
		if (cotationsList != null && cotationsList.size() > 0) {
			
			Date lastTime = null;
			
			Cotation lastCotation = getCryptobotRepository().getLastCotationBeforeDate(symbol, null, false);
			
			if (lastCotation != null) {
				lastTime = lastCotation.getDatetime();			
			}
			
			if (lastTime == null) {
				newCotations.addAll(cotationsList);
				
			} else {			
				for (Cotation cotation : cotationsList) {
					if (cotation.getDatetime().after(lastTime)) {					
						newCotations.add(cotation);
					}
				}
			}
			
			List<Cotation> allCotations = cryptobotRepository.getCotationsSinceDate(symbol, PeriodUtil.previousDateForPeriod((lastTime != null ? lastTime : new Date()), Period._6j));
						
			if (allCotations != null) {
				
				int startIndex = allCotations.size();				
				allCotations.addAll(newCotations);				
				List<Period> periodsMaj = Arrays.asList(Period._1h, Period._12h, Period._24h, Period._6j);
				
				for (int i = startIndex; i < allCotations.size(); i++) {
					
					Cotation cotation = allCotations.get(i);
					
					for (Period periodMaj : periodsMaj) {
						List<Cotation> cotationGrid = getCotationGridOnPeriodBackward(cotation, allCotations, periodMaj);
						cotation = computeLastCotation(cotationGrid, periodMaj);
					}
					getCryptobotRepository().getCotationRepository().save(cotation);
				}
			}
		}
			
		return newCotations;
	}
	
	
	public CryptobotRepository getCryptobotRepository() {
		return cryptobotRepository;
	}


	public AssetConfig getAssetConfigForCotation(Cotation cotation, ModeEval modeEval) {
		AssetConfig assetConfig = cryptobotRepository.getAssetConfigForCotation(cotation, modeEval);		
		return assetConfig;
	}
	
	
	@Transactional
	public void checkAndResetLossForCotations(Asset asset) {
		
		if (asset != null) {
			String symbol = asset.getSymbol();			
			Cotation lastRatedCotation = cryptobotRepository.getLastRatedCotation(symbol);			
			if (lastRatedCotation != null) {
				checkAndResetLossForCotation(asset, lastRatedCotation);
			}
		}
	}
	
	public void checkAndResetLossForCotation(Asset asset, Cotation cotation) {

		if (asset != null && cotation != null) {
			
			String symbol = asset.getSymbol();
			Integer nbLoss = cotation.getNbLoss();

			if (nbLoss != null && nbLoss > 0) {

				Period period = asset.getAnalysisPeriodEnum();
				Date previousDateForPeriod = PeriodUtil.previousDateForPeriod(cotation.getDatetime(), period);
				List<Cotation> allCotations = cryptobotRepository.getCotationsSinceDate(symbol, previousDateForPeriod);

				if (allCotations != null && allCotations.size() > 1) {
					int endIndex = allCotations.indexOf(cotation);
					if (endIndex > 0) {
						boolean reset = true;
						for (int i = endIndex; i > -1; i--) {
							Cotation initialCotation = allCotations.get(i);
							if (!nbLoss.equals(initialCotation.getNbLoss())) {
								reset = false;
								break;
							}
						}
						if (reset) {
							cotation.nbLoss(0).percentLoss(0d);
							getLogger().info("-- Check and reset loss for :");
							getLogger().info(cotation.toString());
							getLogger().info("");
						}
					}
				}
			}
		}
	}
	
	
	@Transactional
	public List<Cotation> computeCotations(String symbol, Period period) {
		
		List<Cotation> allCotations = cryptobotRepository.getCotationsSinceDate(symbol, PeriodUtil.previousDateForPeriod(null, period));
		
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
	
	
	private Cotation computeLastCotation(List<Cotation> cotationGrid, Period period) {
		
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

	
	
	private List<Cotation>  getCotationGridOnPeriodBackward(Cotation refCotation, List<Cotation> allCotations, Period period) {
		int startIndex = 0;
		Collections.sort(allCotations);
		int endIndex = allCotations.indexOf(refCotation);
		if (endIndex < 0) {
			allCotations.add(refCotation);
			endIndex = allCotations.size()-1;
		}
		Date previousDate = PeriodUtil.previousDateForPeriod(refCotation.getDatetime(), period);
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
	
	private List<Cotation> getCotationGridOnPeriodForward(Cotation refCotation, List<Cotation> allCotations, Period period) {
		int endIndex = -1;
		Collections.sort(allCotations);
		int startIndex = allCotations.indexOf(refCotation);
		if (startIndex >= 0) {
			Date nextDate = PeriodUtil.nextDateForPeriod(refCotation.getDatetime(), period);
			if (nextDate != null) {
				endIndex = findIndexForDate(allCotations, nextDate, refCotation.getSymbol());
			}
			List<Cotation> subList = allCotations.subList(startIndex, endIndex + 1);
			subList.removeIf(cotation -> !cotation.getSymbol().equals(refCotation.getSymbol()));
			return subList;
		}
		return null;
	}
	
	private int findIndexForPeriodBackward(Cotation refCotation, List<Cotation> allCotations, Period period) {
		int index = 0;
		Collections.sort(allCotations);
		int endIndex = allCotations.indexOf(refCotation);
		if (endIndex < 0) {
			allCotations.add(refCotation);
			endIndex = allCotations.size()-1;
		}
		Date previousDate = PeriodUtil.previousDateForPeriod(refCotation.getDatetime(), period);
		if (previousDate != null) {
			index = findIndexForDate(allCotations, previousDate, refCotation.getSymbol());
		}
		if (index < 0) {
			index = 0;
		}
		return index;	
	}

	
	private int findIndexForDate(List<Cotation> cotations, Date refDate, String symbol) {
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
	


}
