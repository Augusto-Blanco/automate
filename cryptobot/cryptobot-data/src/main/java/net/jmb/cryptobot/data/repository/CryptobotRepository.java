package net.jmb.cryptobot.data.repository;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import net.jmb.cryptobot.data.bean.AssetQO;
import net.jmb.cryptobot.data.bean.OrderQO;
import net.jmb.cryptobot.data.bean.PageData;
import net.jmb.cryptobot.data.entity.Asset;
import net.jmb.cryptobot.data.entity.AssetConfig;
import net.jmb.cryptobot.data.entity.Cotation;
import net.jmb.cryptobot.data.entity.Trade;
import net.jmb.cryptobot.data.enums.OrderState;

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
		List<Cotation> cotations = null;		
		if (startDate != null) {				
			cotations = cotationRepository.findBySymbolEqualsAndDatetimeGreaterThanEqual(symbol, startDate);
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
	
	
	public Cotation getLastCotationBeforeDate(String symbol, Date dateRef) {
		Cotation cotation = null;
		
		if (symbol != null) {
			if (dateRef == null) {
				dateRef = new Date();
			}
			List<Cotation> lastCotationsForSymbolBeforeDate = cotationRepository.findLastCotationForSymbolBeforeDate(symbol, dateRef);
			if (lastCotationsForSymbolBeforeDate != null && lastCotationsForSymbolBeforeDate.size() > 0) {
				cotation = lastCotationsForSymbolBeforeDate.get(lastCotationsForSymbolBeforeDate.size() - 1);
			}
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
				cotation = cotations.get(cotations.size() - 1);
			}
		}
		return cotation;
	}
	

	
	
	public AssetConfig getAssetConfigForCotation(Cotation cotation) {
		AssetConfig assetConfig = null;
		List<AssetConfig> configList = assetConfigRepository.findBySymbolAndDate(cotation.getSymbol(), cotation.getDatetime());
		if (configList != null && configList.size() > 0) {
			assetConfig = configList.get(configList.size() - 1);
		}
		return assetConfig;
	}	
	
	
	
	public void saveAsset(Asset asset) {
		if (asset != null) {
			assetRepository.save(asset);
		}		
	}
	
	public void saveTrade(Trade trade) {
		if (trade != null) {
			tradeRepository.save(trade);
		}		
	}
	

	public PageData<Asset> getPageAssets(AssetQO assetQO, Pageable pageRequest) {
		PageData<Asset> pageData = null;
		if (pageRequest != null) {
			pageData = getPageData(assetRepository.findByAssetQO(assetQO, pageRequest));
		} else {
			pageData = getPageData(assetRepository.findByAssetQO(assetQO));
		}
		return pageData;
	}
	
	
	public List<String> getListeTradeRefs(int maxNumber) {
		List<String> listeTradeRefs = null;
		Pageable pageable = getPageable(0, maxNumber, Sort.by(Order.desc("id")));
		Page<String> pageTradeRefs = tradeRepository.findDistinctTradeRefs(pageable);
		if (pageTradeRefs != null) {
			listeTradeRefs = pageTradeRefs.getContent();
		}
		return listeTradeRefs;
	}
		
	
	public List<Trade> getTradesForAsset(Long assetId) {
		return tradeRepository.findByAssetId(assetId);
	}
	
	public PageData<Trade> getTradesForAsset(Long assetId, Pageable pageable) {
		Page<Trade> contrats = tradeRepository.findByAssetId(assetId, pageable);
		return getPageData(contrats);
	}
	
	public PageData<Trade> getTrades(OrderQO contratQO, Pageable pageable) {
		Page<Trade> contrats = tradeRepository.findByOrderQO(contratQO, pageable);
		return getPageData(contrats);
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
	
	

	public List<Trade> getPendingTrades() {
		return tradeRepository.findByStateIn(List.of(OrderState.PENDING.name(), ""));
	}



	public AssetConfigRepository getAssetConfigRepository() {
		return assetConfigRepository;
	}



}
