package net.jmb.cryptobot.data.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the integration_contrat database table.
 * 
 */
@Entity
@Table(name = "position")
@NamedQuery(name = "Position.findAll", query = "SELECT i FROM Position i")
@JsonIgnoreProperties(ignoreUnknown = true)

public class Position extends AbstractEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String platform;
	private String symbol;
	private Double quantity;
	private Double avgPrice;
	private Double cost;
	private Double totalBuy;
	private Double totalSell;
	private Double perf;
	private Long closedTrade;

	// bi-directional association to Asset
	@OneToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "assetId", insertable = true, updatable = true)
	private Asset asset;


	
	public Position() {
	}

	
	public Asset getAsset() {
		return this.asset;
	}

	public void setAsset(Asset asset) {
		this.asset = asset;
	}

	public Position asset(Asset asset) {
		this.asset = asset;
		return this;
	}

	public Position id(Long id) {
		setId(id);
		return this;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public Position platform(String platform) {
		this.platform = platform;
		return this;
	}

	public Position symbol(String symbol) {
		this.symbol = symbol;
		return this;
	}

	public Position quantity(Double quantity) {
		this.quantity = quantity;
		return this;
	}

	public Double getAvgPrice() {
		return avgPrice;
	}

	public void setAvgPrice(Double avgPrice) {
		this.avgPrice = avgPrice;
	}

	public Double getCost() {
		return cost;
	}

	public void setCost(Double cost) {
		this.cost = cost;
	}

	public Double getTotalBuy() {
		return totalBuy;
	}

	public void setTotalBuy(Double totalBuy) {
		this.totalBuy = totalBuy;
	}

	public Double getTotalSell() {
		return totalSell;
	}

	public void setTotalSell(Double totalSell) {
		this.totalSell = totalSell;
	}

	public Double getPerf() {
		return perf;
	}

	public void setPerf(Double perf) {
		this.perf = perf;
	}

	public Long getClosedTrade() {
		return closedTrade;
	}

	public void setClosedTrade(Long closedTrade) {
		this.closedTrade = closedTrade;
	}


	public Position avgPrice(Double avgPrice) {
		this.avgPrice = avgPrice;
		return this;
	}

	public Position cost(Double cost) {
		this.cost = cost;
		return this;
	}

	public Position totalBuy(Double totalBuy) {
		this.totalBuy = totalBuy;
		return this;
	}

	public Position totalSell(Double totalSell) {
		this.totalSell = totalSell;
		return this;
	}

	public Position perf(Double perf) {
		this.perf = perf;
		return this;
	}

	public Position closedTrade(Long closedTrade) {
		this.closedTrade = closedTrade;
		return this;
	}


	@Override
	public String toString() {
		return "Position [symbol=" + symbol + ", quantity=" + quantity + ", avgPrice=" + avgPrice + ", cost=" + cost
				+ ", totalBuy=" + totalBuy + ", totalSell=" + totalSell + ", perf=" + perf + "]";
	}

}