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
		return "MexcOrder [orderId=" + orderId + ", symbol=" + symbol + ", side=" + side + ", origQty=" + origQty
				+ ", price=" + price + ", getDateTime()=" + getDateTime() + "]";
	}
	
	
	
	
}
