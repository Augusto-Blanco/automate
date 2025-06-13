package net.jmb.cryptobot.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.jmb.cryptobot.beans.MexcAccountInfo;
import net.jmb.cryptobot.beans.MexcOrder;
import net.jmb.cryptobot.data.entity.Asset;
import net.jmb.cryptobot.data.entity.AssetConfig;
import net.jmb.cryptobot.data.entity.Cotation;
import net.jmb.cryptobot.data.entity.Position;
import net.jmb.cryptobot.data.entity.Trade;
import net.jmb.cryptobot.data.enums.OrderSide;
import net.jmb.cryptobot.data.enums.OrderState;
import net.jmb.cryptobot.data.enums.Period;
import net.jmb.cryptobot.util.PeriodUtil;


@Service
public class MexcTradeService extends TradeService {

	
	@Autowired
	MexcRestClientService restClientService;		
	

	
	@Override
	@Transactional
	public synchronized List<Cotation> registerLastCotations() throws Exception {
		if (canExchange()) {
			Asset asset = getAsset();			
			List<Cotation> newCotations = restClientService.updateCotationsPrice(asset);
			return newCotations;
		}
		return null;
	}
	
	
	
	
	@Scheduled(cron = "${cryptobot.trade.evaluation.scheduler.cron}")
	@Transactional
	public synchronized void evaluateTradeForLastCotation() throws Exception {
		
		if (canExchange() && !isLocked()) {			
			Asset asset = getAsset();
			if (asset != null) {				
				Trade trade = null;				
				List<Cotation> lastCotations = registerLastCotations();			
				if (lastCotations != null && lastCotations.size() > 0) {					
					lastCotations = cotationService.getCryptobotRepository().getAllCotationsSinceLastRated(symbol);					
					if (lastCotations != null && lastCotations.size() > 0) {
						Cotation cotation = lastCotations.get(lastCotations.size() - 1);
						Cotation previousCotation = (lastCotations.size() > 1) ? lastCotations.get(lastCotations.size() - 2) : null;
						AssetConfig assetConfig = cotationService.getAssetConfigForCotation(cotation);		
						getLogger().info("-- Evaluate trade --");
						cotation = cotationService.evaluateTradesForCotations(lastCotations, asset, assetConfig.realEval(true));
						
						if (cotation != null && (cotation.isBuyFlag() || cotation.isSellFlag())) {
							MexcAccountInfo accountInfos = restClientService.getAccountInfos();
							Double freeQuantity = getFreeQuantity(symbol, accountInfos);							
							if (cotation.isBuyFlag()) {												
								Double freeAmount = getFreeQuantity(asset.getPair(), accountInfos);
								Double maxInvest = getDesiredAmountToBuy(asset, freeAmount);
								Double lastPrice = getLastPrice(asset);
								if (maxInvest > 50d && lastPrice != null && lastPrice > 0d && lastPrice <= 1.001 * cotation.getPrice()) {
									Double quantity = maxInvest / lastPrice; 
									trade = sendOrder(asset, OrderSide.BUY, quantity, lastPrice);			
								} else {
									cotation.flagBuy(null).currentSide(OrderSide.SELL).quantity(0d);
									if (previousCotation != null) {
										cotation.buyPrice(previousCotation.getBuyPrice());
									}
								}
							} else if (cotation.isSellFlag()) {								
								Double lastPrice = getLastPrice(asset);
								if (lastPrice > 0d && freeQuantity > 0d) {
									Position position = asset.getPosition();
									if (position != null && position.getAvgPrice() != null) {
										if (position.getAvgPrice() > lastPrice) {
											lastPrice = position.getAvgPrice();
										}
									}
									trade = sendOrder(asset, OrderSide.SELL, freeQuantity, lastPrice);
								}
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
	
	@Override
	public Double getFreeAssetQuantity(Asset asset) {		
		if (asset != null && asset.getSymbol() != null) {
			MexcAccountInfo accountInfos = restClientService.getAccountInfos();
			if (accountInfos != null) {
				String symbol = asset.getSymbol();
				if (!symbol.startsWith("USD")) {
					symbol = symbol.replaceAll("USD.", "");
				}
				return accountInfos.getFreeAssetQuantity(symbol);
			}
		}			
		return null;
	}
	
	
	
	private Double getFreeQuantity(String symbol, MexcAccountInfo accountInfos) {
		if (symbol != null && accountInfos != null) {			
			if (!symbol.startsWith("USD")) {
				symbol = symbol.replaceAll("USD.", "");
			}
			return accountInfos.getFreeAssetQuantity(symbol);
		}
		return null;
	}
	
	
	@Override
	public synchronized Trade sendOrder(Asset asset, OrderSide orderSide, Double quantity, Double price) {		
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
			MexcOrder sendOrder = restClientService.sendOrder(asset, orderSide, qty, decPrice);
			if (sendOrder != null) {
				Trade trade = mapOrderIntoTrade(asset, sendOrder, new Trade());
				if (trade.getAmount() == null) {
					trade.amount(qty.multiply(decPrice).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
				}
				return trade;
			}
		}
		return null;
	}
	
	
	private Trade mapOrderIntoTrade(Asset asset, MexcOrder order, Trade trade) {
		
//		MexcOrder [transactTime=null, orderId=C02__528582078313373698049, symbol=SOLUSDT, side=SELL, type=LIMIT, origQty=1.33, price=155.0, executedQty=0.0, cummulativeQuoteQty=0.0, status=NEW, time=1741806023916, updateTime=null, isWorking=true, origQuoteOrderQty=206.15]
		if (order != null && trade != null) {
			String symbol = order.getSymbol().replaceAll("USD.*", "");
			OrderState state = OrderState.PENDING;
			if (order.getStatusEnum() != null) {
				state = switch (order.getStatusEnum()) {
					case CANCELED -> OrderState.CANCELLED;
					case FILLED -> OrderState.COMPLETE;
					case NEW -> OrderState.PENDING;
					case PARTIALLY_CANCELED -> OrderState.CANCELLED;
					case PARTIALLY_FILLED -> OrderState.PARTIAL;
					default -> OrderState.PENDING;
				};
			}
			OrderSide side = ("SELL".equalsIgnoreCase(order.getSide())) ? OrderSide.SELL : OrderSide.BUY;
			trade.platform(platform)
				.symbol(symbol)
				.time(order.getDateTime())
				.amount(order.getOrigQuoteOrderQty())
				.price(order.getPrice())
				.quantity(order.getOrigQty())
				.side(side.name())
				.state(state.name())
				.tradeRef(order.getOrderId())
				.execQty(order.getExecutedQty())
				.execAmount(order.getCummulativeQuoteQty())
			;
			if (asset != null) {
				trade.asset(asset);
			}
		}
		return trade;
	}


	@Override
	protected Trade updateTradeState(Asset asset, Trade trade) {
		try {
			MexcOrder order = restClientService.requestOrder(asset, trade.getTradeRef());
			if (order != null) {
				trade = mapOrderIntoTrade(asset, order, trade);
			}
		} catch (Exception e) {
			getLogger().error(e.getMessage(), e);
		}
		return trade;
	}


	@Override
	protected List<Trade> addUnknownTrades(Asset asset) {
		try {			
			List<MexcOrder> orders = new ArrayList<MexcOrder>();			
			List<MexcOrder> newOrders = restClientService.openOrders(asset);			
			List<MexcOrder> allOrders = restClientService.allOrders(asset, PeriodUtil.previousDateForPeriod(new Date(), Period._48h));			
			if (newOrders != null) {
				orders.addAll(newOrders);
			}
			if (allOrders != null) {
				orders.addAll(allOrders);
			}			
			List<Trade> newTrades = new ArrayList<Trade>();
			if (orders != null && orders.size() > 0) {
				for (MexcOrder order : orders) {
					Trade tradeForRef = cryptobotRepository.getTradeForRef(order.getOrderId());
					if (tradeForRef == null) {
						Trade newTrade = mapOrderIntoTrade(asset, order, new Trade());
						if (!newTrades.contains(newTrade)) {
							cryptobotRepository.saveTrade(newTrade);
							newTrades.add(newTrade);
						}
					}
				}				
			}
			return newTrades;
		} catch (Exception e) {
			getLogger().error(e.getMessage(), e);
		}
		return null;
	}


	@Override
	public Double getLastPrice(Asset asset) {
		Double lastPrice = restClientService.getLastPrice(asset);
		return lastPrice;
	}
	
	

}
