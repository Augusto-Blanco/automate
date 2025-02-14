package net.jmb.cryptobot.beans;

import java.util.Date;

import net.jmb.cryptobot.data.enums.OrderSide;

public class MexcOrder {
	
	Long transactTime;
	String orderId;
	String symbol;
	String side;
	String type;
	Double origQty;
	Double price;
	
	Double executedQty;
	Double cummulativeQuoteQty;
	String status;
	Long time;
	Long updateTime;
	Boolean isWorking;
	Double origQuoteOrderQty;

	
	public Long getTransactTime() {
		return transactTime;
	}
	public void setTransactTime(Long transactTime) {
		this.transactTime = transactTime;
	}
	public Date getDateTime() {
		return new Date(transactTime);
	}
	
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getSide() {
		return side;
	}
	public OrderSide getOrderSide() {
		return OrderSide.get(side);
	}
	public void setSide(String side) {
		this.side = side;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Double getOrigQty() {
		return origQty;
	}
	public void setOrigQty(Double origQty) {
		this.origQty = origQty;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	@Override
	public String toString() {
		return "MexcOrder [transactTime=" + transactTime + ", orderId=" + orderId + ", symbol=" + symbol + ", side="
				+ side + ", type=" + type + ", origQty=" + origQty + ", price=" + price + ", executedQty=" + executedQty
				+ ", cummulativeQuoteQty=" + cummulativeQuoteQty + ", status=" + status + ", time=" + time
				+ ", updateTime=" + updateTime + ", isWorking=" + isWorking + ", origQuoteOrderQty=" + origQuoteOrderQty
				+ "]";
	}
	
	public Double getExecutedQty() {
		return executedQty;
	}
	public void setExecutedQty(Double executedQty) {
		this.executedQty = executedQty;
	}
	public Double getCummulativeQuoteQty() {
		return cummulativeQuoteQty;
	}
	public void setCummulativeQuoteQty(Double cummulativeQuoteQty) {
		this.cummulativeQuoteQty = cummulativeQuoteQty;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public Long getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}
	public Boolean getIsWorking() {
		return isWorking;
	}
	public void setIsWorking(Boolean isWorking) {
		this.isWorking = isWorking;
	}
	public Double getOrigQuoteOrderQty() {
		return origQuoteOrderQty;
	}
	public void setOrigQuoteOrderQty(Double origQuoteOrderQty) {
		this.origQuoteOrderQty = origQuoteOrderQty;
	}
	
	
	
	
}
