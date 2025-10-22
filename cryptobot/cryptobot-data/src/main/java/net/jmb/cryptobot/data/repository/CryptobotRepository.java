package net.jmb.cryptobot.data.repository;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import net.jmb.cryptobot.data.entity.Asset;
import net.jmb.cryptobot.data.entity.AssetConfig;
import net.jmb.cryptobot.data.entity.Cotation;
import net.jmb.cryptobot.data.entity.Trade;
import net.jmb.cryptobot.data.enums.Period;
import net.jmb.cryptobot.util.PeriodUtil;

@Repository
@Transactional
public class CryptobotRepository extends GenericRepository {
	
	@Autowired
	AssetRepository assetRepository;
	
	@Autowired
	TradeRepository tradeRepository;
	
	@Autowired
	CotationRepository cotationRepository;
	
	@Autowired
	AssetConfigRepository assetConfigRepository ;
	
	
	
	public List<Cotation> getCotationsSinceDate(String symbol, Date startDate) {	
		return getCotationsSinceDate(symbol, startDate, false);		
	}
	
	
	public List<Cotation> getCotationsSinceDate(String symbol, Date startDate, boolean analysisByHour) {		
		List<Cotation> cotations = null;		
		if (startDate != null) {
			if (!analysisByHour) {
				cotations = cotationRepository.findBySymbolEqualsAndDatetimeGreaterThanEqualOrderByDatetime(symbol, startDate);
			} else {
				cotations = cotationRepository.findBySymbolEqualsAndDatetimeGreaterThanEqualAndTopHourTrueOrderByDatetime(symbol, startDate);
			}
		}		
		return cotations;		
	}
	
	
	public Cotation getLastRatedCotation(String symbol) {
		Cotation cotation = null;
		
		if (symbol != null) {
			List<Cotation> lastRatedCotationsForSymbol = cotationRepository.findLastRatedCotationForSymbol(symbol);
			if (lastRatedCotationsForSymbol != null && lastRatedCotationsForSymbol.size() > 0) {
				cotation = lastRatedCotationsForSymbol.get(lastRatedCotationsForSymbol.size() - 1);
			}
		}
		return cotation;
	}
	
	public List<Cotation> getAllCotationsSinceLastRated(String symbol) {
		Cotation lastRated = getLastRatedCotation(symbol);
		List<Cotation> allCotationsSinceLastRated = null;
		if (lastRated != null) {
			allCotationsSinceLastRated = getCotationsSinceDate(symbol, lastRated.getDatetime());
		} else {
			allCotationsSinceLastRated = getCotationsSinceDate(symbol, PeriodUtil.previousDateForPeriod(new Date(), Period._6j));
		}
		return allCotationsSinceLastRated;
	}
	
	
	public Cotation getLastCotationBeforeDate(String symbol, Date dateRef, boolean longTimeAnalysis) {
		Cotation cotation = null;
		
		if (symbol != null) {
			if (dateRef == null) {
				dateRef = new Date();
			}
			List<Cotation> lastCotationsForSymbolBeforeDate = null;
			if (longTimeAnalysis) {
				lastCotationsForSymbolBeforeDate = cotationRepository.findLastHourCotationForSymbolBeforeDate(symbol, dateRef);
			} else {
				lastCotationsForSymbolBeforeDate = cotationRepository.findLastCotationForSymbolBeforeDate(symbol, dateRef);
			}
			if (lastCotationsForSymbolBeforeDate != null && lastCotationsForSymbolBeforeDate.size() > 0) {
				cotation = lastCotationsForSymbolBeforeDate.get(lastCotationsForSymbolBeforeDate.size() - 1);
			}
		}
		return cotation;
	}
	
	
	public Cotation getLastSellCotationBeforeDate(String symbol, Date dateRef) {
		Cotation cotation = null;
		
		if (symbol != null) {
			if (dateRef == null) {
				dateRef = new Date();
			}
			try {
				List<Cotation> lastCotations = cotationRepository.findLastSellCotationBeforeDate(symbol, dateRef);
				if (lastCotations != null && lastCotations.size() > 0) {
					cotation = lastCotations.get(lastCotations.size() - 1);
				}
			} catch (Exception e) {}
		}
		return cotation;
	}
	
	
	
	public Cotation getMin24hCotationAfterDate(String symbol, Date dateRef) {
		Cotation cotation = null;
		
		if (symbol != null) {
			if (dateRef == null) {
				dateRef = new Date();
			}
			List<Cotation> cotations = cotationRepository.findMinPrice24hCotationForSymbolAfterDate(symbol, dateRef);
			
			if (cotations != null && cotations.size() > 0) {
				for (Cotation tmp : cotations) {
					if (cotation == null || tmp.getMin24h() < cotation.getMin24h()) {
						cotation = tmp;
					}
				}
			}
		}
		return cotation;
	}
	
	
	public Cotation getMinCotationBetweenDates(String symbol, Date startDate, Date endDate) {
		Cotation cotation = null;		
		if (symbol != null && startDate != null && endDate != null) {
			List<Cotation> cotations = cotationRepository.findMinPriceCotationForSymbolBetweenDates(symbol, startDate, endDate);			
			if (cotations != null && cotations.size() > 0) {
				for (Cotation tmp : cotations) {
					cotation = tmp;
				}
			}
		}
		return cotation;
	}
		
	
	public AssetConfig getAssetConfigForCotation(Cotation cotation) {
		AssetConfig assetConfig = null;
		List<AssetConfig> configList = assetConfigRepository.findBySymbolAndDate(cotation.getSymbol(), cotation.getDatetime());
		if (configList != null && configList.size() > 0) {			
			for (AssetConfig wAssetConfig : configList) {
				if (assetConfig == null) {
					assetConfig = new AssetConfig().symbol(wAssetConfig.getSymbol()).maxVarHigh(wAssetConfig.getMaxVarHigh()).maxVarLow(wAssetConfig.getMaxVarLow())
							.stopLoss(wAssetConfig.getStopLoss()).startTime(wAssetConfig.getStartTime()).endTime(wAssetConfig.getEndTime()).analysisPeriod(wAssetConfig.getAnalysisPeriod());
				} else {
					if (assetConfig.getMaxVarHigh().compareTo(wAssetConfig.getMaxVarHigh()) > 0) {
						assetConfig.maxVarHigh(wAssetConfig.getMaxVarHigh());
					}
					if (assetConfig.getMaxVarLow().compareTo(wAssetConfig.getMaxVarLow()) < 0) {
						assetConfig.maxVarLow(wAssetConfig.getMaxVarLow());
					}
					if (assetConfig.getAnalysisPeriodEnum().compareTo(wAssetConfig.getAnalysisPeriodEnum()) < 0) {
						assetConfig.analysisPeriod(wAssetConfig.getAnalysisPeriod());
					}
					if (assetConfig.getStartTime().compareTo(wAssetConfig.getStartTime()) > 0) {
						assetConfig.startTime(wAssetConfig.getStartTime());
					}					
				}
			}
		}
		return assetConfig;
	}	
	
	
	
	public void saveAsset(Asset asset) {
		if (asset != null) {
			assetRepository.save(asset);
		}		
	}

	
	public Trade saveTrade(Trade trade) {
		if (trade != null) {
			return tradeRepository.save(trade);
		}
		return null;
	}
	
		
	
	public List<Trade> getPendingTradesForAsset(Asset asset) {
		return tradeRepository.findByAssetIdAndStateInOrderByTime(asset.getId(), Arrays.asList("PENDING", "PARTIAL"));
	}
	

	public List<Trade> getUnsoldedTradesForAsset(Asset asset) {
		return tradeRepository.findByAssetIdAndNotSoldedQty(asset.getId());
	}
	
	
	public Trade getTradeForRef(String tradeRef) {
		return tradeRepository.findByTradeRef(tradeRef);
	}
	
	
	public Cotation getCotationForTrade(Long tradeId) {
		Cotation cotation = cotationRepository.findByTradeId(tradeId);
		return cotation;
	}
	


	
	
	public AssetRepository getAssetRepository() {
		return assetRepository;
	}

	public TradeRepository getTradeRepository() {
		return tradeRepository;
	}

	public CotationRepository getCotationRepository() {
		return cotationRepository;
	}

	public AssetConfigRepository getAssetConfigRepository() {
		return assetConfigRepository;
	}



}
