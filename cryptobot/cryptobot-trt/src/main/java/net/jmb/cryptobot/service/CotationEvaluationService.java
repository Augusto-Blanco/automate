package net.jmb.cryptobot.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
import net.jmb.cryptobot.data.repository.AssetConfigRepository;
import net.jmb.cryptobot.data.repository.CryptobotRepository;
import net.jmb.cryptobot.enums.ParamContext;
import net.jmb.cryptobot.util.PeriodUtil;

@Service
public class CotationEvaluationService extends CommonService {
	
	public static final ParamContext CONTEXTE = ParamContext.TOUT_CONTEXTE;
	
	@Autowired
	CryptobotRepository cryptobotRepository;
		
	
	
	@Transactional
	public void recordEvaluationsForCotations(List<Cotation> cotations, Asset asset, List<AssetConfig> assetConfigList) {
		
		Map<AssetConfig, List<Cotation>> map = new Hashtable<AssetConfig, List<Cotation>>();
		
		resetAssetConfigs(assetConfigList);
		
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
	}
	
	
	@Transactional
	private void resetAssetConfigs(List<AssetConfig> assetConfigList) {		
		getLogger().info("** resetAssetConfigs **");
		for (AssetConfig assetConfig : assetConfigList) {
			AssetConfigRepository assetConfigRepository = cryptobotRepository.getAssetConfigRepository();
			assetConfigRepository.deleteDateGreaterOrEquals(assetConfig.getSymbol(), assetConfig.getEndTime(), assetConfig.getAnalysisPeriod());
			assetConfigRepository.save(assetConfig);
			getLogger().info(assetConfig.toString());
		}		
		getLogger().info("");
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
	public void checkAndResetLossForCotations(Asset asset) {
		
		if (asset != null) {
			String symbol = asset.getSymbol();			
			Cotation lastRatedCotation = cryptobotRepository.getLastRatedCotation(symbol);			
			if (lastRatedCotation != null) {
				checkAndResetLossForCotation(asset, lastRatedCotation);
			}
		}
	}
	
	private void checkAndResetLossForCotation(Asset asset, Cotation cotation) {

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
	


}
