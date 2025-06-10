package net.jmb.cryptobot.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import net.jmb.cryptobot.data.entity.Asset;
import net.jmb.cryptobot.data.entity.Cotation;
import net.jmb.cryptobot.data.entity.Position;
import net.jmb.cryptobot.data.entity.Trade;
import net.jmb.cryptobot.data.enums.OrderSide;
import net.jmb.cryptobot.data.enums.Period;
import net.jmb.cryptobot.data.repository.AssetRepository;
import net.jmb.cryptobot.data.repository.CryptobotRepository;
import net.jmb.cryptobot.data.repository.PositionRepository;

@Service
public abstract class TradeService extends CommonService {

	
	@Autowired
	CotationService cotationService = null;	
	
	@Autowired
	AssetRepository assetRepository;
	
	@Autowired
	CryptobotRepository cryptobotRepository;
	
	@Autowired
	PositionRepository positionRepository;
	
	@Value("${symbol:}")
	String symbol = null;
	
	@Value("${platform:}")
	String platform = null;
	
	@Value("${initDate:}")
	String initDate = null;
	
	@Value("${noExchange:false}")
	Boolean noExchange = false;
	
	private boolean locked = true;
	
	
	
	@Transactional
	public abstract List<Cotation> registerLastCotations() throws Exception;

	@Transactional
	public abstract void evaluateTradeForLastCotation() throws Exception;
	
	public abstract Double getFreeAssetQuantity(Asset asset);
	
	public abstract Double getLastPrice(Asset asset);
	
	public abstract Trade sendOrder(Asset asset, OrderSide orderSide, Double quantity, Double price);
	
	protected abstract Trade updateTradeState(Asset asset, Trade trade);
	
	protected abstract List<Trade> addUnknownTrades(Asset asset);
	
	
	public synchronized void init(String symbol, String platform, Boolean canExchange) throws Exception {
		this.platform = platform;
		this.symbol = symbol;
		this.initDate = null;
		this.noExchange = (canExchange == null || !canExchange);
		initAndLock();
	}
	
	
	public synchronized void initAndLock() throws Exception {
		if (symbol != null) {
			locked = true;			
		}
	}
	
	
	public void unlock() {
		locked = false;
	}
	
	protected boolean isLocked() {
		return locked;
	}
	
	
	public boolean canExchange() {
		return (this.noExchange == null || this.noExchange == false);
	}
	
	
	protected Asset getAsset() {
		return assetRepository.findBySymbolAndPlatformEquals(symbol, platform);
	}
	
	
	
	@Transactional
	@Scheduled(cron = "${cryptobot.cotation.evaluation.scheduler.cron}")
	public synchronized Cotation evaluateLastCotations() throws Exception {
		Asset asset = getAsset();
		if (asset != null) {
			Date dateRef = StringUtils.isNotBlank(initDate) ? new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(initDate) : null;
			Cotation lastCotation = cotationService.evaluateLastCotations(asset, dateRef);
			return lastCotation;
		}
		return null;
	}
	
	
	@Transactional	
	@Scheduled(cron = "0 7 * * * *")
	public synchronized void checkAndResetLossForCotations() throws Exception {
		Asset asset = getAsset();
		cotationService.checkAndResetLossForCotations(asset);
	}
		
	
	@Transactional
	protected Trade registerTradeForCotation(Trade trade, Cotation cotation) {
		if (trade != null) {
			cotation.setTrade(trade);
			return cotationService.getCryptobotRepository().saveTrade(trade);
		}
		return null;
	}
	
	
	@Transactional
	@Scheduled(cron = "${cryptobot.position.scheduler.cron}")
	public synchronized void updateTradesAndPosition() {		
		
		Asset asset = getAsset();
		
		Position position = asset.getPosition();
		if (position == null) {
			position = new Position().asset(asset).platform(platform).symbol(symbol).totalBuy(0d).totalSell(0d).perf(0d).cost(0d).value(0d).quantity(0d).avgPrice(0d);
			positionRepository.save(position);
		}
		Date firstTrade = position.getFirstTrade();
		Date lastTrade = position.getLastTrade();
		Double totalBuy = position.getTotalBuy() != null ? position.getTotalBuy() : 0d;
		Double totalSell = position.getTotalSell() != null ? position.getTotalSell() : 0d;		
		
		// mise à jour des ordres ordres ouverts (non encore exécutés) et de la position résultante
		List<Trade> pendingTradesForAsset = cryptobotRepository.getPendingTradesForAsset(asset);
		if (pendingTradesForAsset != null) {
			for (Trade trade : pendingTradesForAsset) {
				OrderSide orderSide = trade.getOrderSideEnum();
				Double execAmount = trade.getExecAmount() != null ? trade.getExecAmount() : 0d;
				if (firstTrade == null || firstTrade.after(trade.getTime())) {
					firstTrade = trade.getTime();
				}
				if (lastTrade == null || lastTrade.before(trade.getTime())) {
					lastTrade = trade.getTime();
				}
				try {
					Trade updatedTrade = updateTradeState(asset, trade);
					Double newExecAmount = updatedTrade.getExecAmount() != null ? updatedTrade.getExecAmount() : 0d;
					switch (orderSide) {
						case BUY -> totalBuy += (newExecAmount - execAmount);
						case SELL -> totalSell += (newExecAmount - execAmount);
					}
				} catch (Exception e) {
					getLogger().error(e.getMessage(), e);
				}
			}
		}		
		// intégration des ordres passés en dehors de l'appli et màj de la position résultante
		List<Trade> unknownTrades = addUnknownTrades(asset);
		if (unknownTrades != null && unknownTrades.size() > 0) {
			for (Trade trade : unknownTrades) {
				OrderSide orderSide = trade.getOrderSideEnum();
				Double execAmount = trade.getExecAmount() != null ? trade.getExecAmount() : 0d;
				if (firstTrade == null || firstTrade.after(trade.getTime())) {
					firstTrade = trade.getTime();
				}
				if (lastTrade == null || lastTrade.before(trade.getTime())) {
					lastTrade = trade.getTime();
				}
				switch (orderSide) {
					case BUY -> totalBuy += execAmount;
					case SELL -> totalSell += execAmount;
				}
			}
		}
		
		// mise à jour des soldes Achat / Vente et màj de la position correspondante
		List<Trade> unsoldedTrades = updateUnsoldedTrades(asset);
		position = updateUnsoldedTradesPosition(position, unsoldedTrades);

		Double perf = position.getTotalSell() - position.getTotalBuy() + position.getValue();
		position.totalBuy(totalBuy).totalSell(totalSell).perf(perf).firstTrade(firstTrade).lastTrade(lastTrade);
		getLogger().info(position != null ? position.toString() : "");
	}
	
	
	@Transactional
	@Scheduled(cron = "${cryptobot.reset.evaluation.scheduler.cron}")  
	public synchronized void resetEvaluations() {	
		Asset asset = getAsset();
		soldPosition(asset, null);
		cotationService.resetEvaluationForAsset(asset, Period._48h);		
	}
	

	private Position updateUnsoldedTradesPosition(Position position, List<Trade> unsoldedTrades) {
		if (unsoldedTrades != null && unsoldedTrades.size() > 0) {
			Double totalUnsoldQty = 0d, avgPrice = 0d, totalUnsoldAmount = 0d, value = 0d;
			for (Trade trade : unsoldedTrades) {
				if (trade.getOrderSideEnum() == OrderSide.BUY) {
					Double soldedQty = trade.getSoldedQty() != null ? trade.getSoldedQty() : 0d;
					Double price = trade.getPrice();
					Double unsoldQty = (trade.getExecQty() != null ? trade.getExecQty() : trade.getQuantity()) - soldedQty;
					totalUnsoldQty += unsoldQty;
					totalUnsoldAmount += (price * unsoldQty);
				}
			}
			avgPrice = totalUnsoldQty > 0 ? totalUnsoldAmount / totalUnsoldQty : 0d;
			value = totalUnsoldQty * getLastPrice(position.getAsset());
			position.avgPrice(avgPrice).cost(totalUnsoldAmount).quantity(totalUnsoldQty).value(value);
		} else {
			position.avgPrice(0d).cost(0d).quantity(0d).value(0d);
		}
		return position;
	}
	
	
	private List<Trade> updateUnsoldedTrades(Asset asset) {
		List<Trade> unsoldedTrades = cryptobotRepository.getUnsoldedTradesForAsset(asset);
		List<Trade> finalUnsoldedTrades = new ArrayList<Trade>();				
		if (unsoldedTrades != null) {
			for (int i = 0; i < unsoldedTrades.size(); i++) {
				Trade curTrade = unsoldedTrades.get(i);
				OrderSide side = curTrade.getOrderSideEnum();
				Double sellSoldedQty = curTrade.getSoldedQty();
				Double sellExecQty = curTrade.getExecQty();
				if (side.equals(OrderSide.SELL) && i > 0 && sellExecQty != null && !sellExecQty.equals(sellSoldedQty)) {
					if (sellSoldedQty == null) {
						sellSoldedQty = 0d;
					}
					for (int j = i - 1; j >= 0; j--) {
						Trade prevTrade = unsoldedTrades.get(j);
						OrderSide prevSide = prevTrade.getOrderSideEnum();
						Double buySoldedQty = prevTrade.getSoldedQty();
						Double buyExecQty = prevTrade.getExecQty();
						if (prevSide.equals(OrderSide.BUY) && buyExecQty != null && !buyExecQty.equals(buySoldedQty)) {
							if (buySoldedQty == null) {
								buySoldedQty = 0d;
							}
							double deltaSoldedQty = Math.min(sellExecQty - sellSoldedQty, buyExecQty - buySoldedQty);
							sellSoldedQty += deltaSoldedQty;
							curTrade.soldedQty(sellSoldedQty);
							buySoldedQty += deltaSoldedQty;
							prevTrade.soldedQty(buySoldedQty);
							if (sellSoldedQty >= sellExecQty) {
								break;
							}
						}						
					}
				}						
			}
			finalUnsoldedTrades.addAll(unsoldedTrades.stream().filter(trade -> trade.getSoldedQty() == null || trade.getSoldedQty().compareTo(trade.getExecQty()) < 0).toList());			
		}
		return finalUnsoldedTrades;
	}
	
	
	protected Trade soldPosition(Asset asset, Double quantity) {
		// si on a l'actif en stock, on récupère son prix d'achat moyen et on place un ordre de vente limite au-dessus
		if (quantity == null) {
			quantity = getFreeAssetQuantity(asset);
		}
		if (asset != null && quantity > 0 && asset.getPosition() != null) {
			Double avgCostPrice = asset.getPosition().getAvgPrice();
			if (avgCostPrice != null && avgCostPrice > 0d) {				
				double price = avgCostPrice * (1 + asset.getVarLowLimit() / 100);
				Trade soldPositionTrade = sendOrder(asset, OrderSide.SELL, quantity, price);
				if (soldPositionTrade != null) {
					getLogger().info("Position soldée pour " + asset.getSymbol() 
						+ ": quantité vendue " + BigDecimal.valueOf(quantity).setScale(5, RoundingMode.HALF_EVEN)
						+ ", prix " + BigDecimal.valueOf(price).setScale(5, RoundingMode.HALF_EVEN)
						+ ", montant " + BigDecimal.valueOf(price * quantity).setScale(2, RoundingMode.HALF_EVEN));
					cryptobotRepository.saveTrade(soldPositionTrade);
					return soldPositionTrade;
				}
			}
		}
		return null;
	}
	
	
	protected Double getDesiredAmountToBuy(Asset asset, Double avalaibleAmount) {
		if (asset != null && avalaibleAmount != null && avalaibleAmount > 0d) {
			List<Asset> allAssets = assetRepository.findAll();
			return getDesiredAmountToBuy(asset, allAssets, avalaibleAmount);
		}
		return 0d;
	}


	private Double getDesiredAmountToBuy(Asset asset, List<Asset> allAssets, Double avalaibleAmount) {
		Double desiredRatio = 0d, effectiveRatio = 0d, desiredAmount = 0d;		
		if (asset != null && avalaibleAmount != null && avalaibleAmount > 0d) {	
			BigDecimal realSum = BigDecimal.valueOf(avalaibleAmount);			
			// ratio souhaité
			Double confSum = allAssets.stream()
				.map(Asset::getMaxInvest)
				.reduce((invest0, invest1) -> invest0 + invest1)
				.orElse(0d);
			if (confSum > 0d) {
				desiredRatio = new BigDecimal(asset.getMaxInvest() / confSum * 100).setScale(1, RoundingMode.HALF_EVEN).doubleValue();			
			}
			// montant total
			Double totalInvest = allAssets.stream()
				.map(Asset::getPosition)
				.filter(position -> position != null && position.getCost() != null && position.getCost() > 0d)
				.map(Position::getCost)
				.reduce((cost0, cost1) -> cost0 + cost1)
				.orElse(null);
			
			if (totalInvest != null) {
				realSum = realSum.add(BigDecimal.valueOf(totalInvest));
			}
			// ratio réel
			if (asset.getPosition() != null && asset.getPosition().getCost() != null && asset.getPosition().getCost() > 0d) {
				Double cost = asset.getPosition().getCost();
				effectiveRatio = cost / realSum.doubleValue() * 100;
			}
			// montant achat souhaité
			if (effectiveRatio < desiredRatio) {
				desiredAmount = Math.min(avalaibleAmount, (desiredRatio - effectiveRatio) * realSum.doubleValue() / 100);
			}
		}
		return desiredAmount;
	}


	


}
