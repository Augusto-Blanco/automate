package net.jmb.cryptobot.data.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

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
	private Double value;
	@Temporal(TemporalType.TIMESTAMP)
	private Date firstTrade;
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastTrade;
	
	

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


	@Override
	public String toString() {
		return "Position [symbol=" + symbol + ", quantity=" + numericVal(quantity) + ", avgPrice=" + numericVal(avgPrice) + ", cost=" + numericVal(cost) 
			+ ", value=" + numericVal(value) + ", perf=" + numericVal(perf) + ", totalBuy=" + numericVal(totalBuy) + ", totalSell=" + numericVal(totalSell)
			+ ", lastTrade=" + lastTrade + "]";
	}


	public Double getValue() {
		return value;
	}


	public void setValue(Double value) {
		this.value = value;
	}
	
	public Position value(Double value) {
		this.value = value;
		return this;
	}


	public Date getFirstTrade() {
		return firstTrade;
	}


	public void setFirstTrade(Date firstTrade) {
		this.firstTrade = firstTrade;
	}
	
	public Position firstTrade(Date firstTrade) {
		this.firstTrade = firstTrade;
		return this;
	}


	public Date getLastTrade() {
		return lastTrade;
	}


	public void setLastTrade(Date lastTrade) {
		this.lastTrade = lastTrade;
	}
	
	public Position lastTrade(Date lastTrade) {
		this.lastTrade = lastTrade;
		return this;
	}
	
	private String numericVal(Double val) {
		if (val == null) {
			return new BigDecimal(0.0).toPlainString();
		}
		String result = new BigDecimal(val).setScale(4, RoundingMode.HALF_EVEN).toPlainString();
		result = Pattern.compile("(\\.[0-9]{3})0+$").matcher(result).replaceAll(matchResult -> matchResult.group(1));
		result = Pattern.compile("(\\.[0-9]{2})0+$").matcher(result).replaceAll(matchResult -> matchResult.group(1));
		return result;
	}

}