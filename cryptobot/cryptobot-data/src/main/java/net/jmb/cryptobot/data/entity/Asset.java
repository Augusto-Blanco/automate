package net.jmb.cryptobot.data.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import net.jmb.cryptobot.data.enums.Period;

@Entity
@Table(name = "asset")
@NamedQuery(name = "Asset.findAll", query = "SELECT v FROM Asset v")

public class Asset extends AbstractEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String platform;
	private String symbol;
	private Double maxInvest;
	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "close")
	private Date closeDate;
	private Integer nbDecimals;
	private Integer tradeDelay;
	private Double varLowLimit;
	private Double varHighLimit;
	private Double stopLossStart;
	private Integer stopLossLimit;
	private Double maxPercentLoss;
	private BigDecimal feesRate;
	private String analysisPeriod;
	private String frequency;	
	private BigDecimal perf;
	private Double gapFromTrend;

	
	
	@OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonIgnore
	private List<Trade> trades;
	
	

	public Asset() {
	}
	

	@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
	public Date getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public String getPlatform() {
		return this.platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public List<Trade> getTrades() {
		return this.trades;
	}

	public void setTrades(List<Trade> trades) {
		this.trades = trades;
	}

	public Trade addTrade(Trade trade) {
		if (trades == null) {
			trades = new ArrayList<Trade>();
		}
		getTrades().add(trade);
		trade.setAsset(this);
		return trade;
	}

	public Trade removeTrade(Trade trade) {
		getTrades().remove(trade);
		trade.setAsset(null);
		return trade;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Asset startTime(Date startTime) {
		this.startTime = startTime;
		return this;
	}

	public Asset lastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
		return this;
	}

	public Asset closeDate(Date close) {
		this.closeDate = close;
		return this;
	}

	public Asset platform(String platform) {
		this.platform = platform;
		return this;
	}

	public Asset symbol(String symbol) {
		this.symbol = symbol;
		return this;
	}

	public Asset trades(List<Trade> trades) {
		this.trades = trades;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && this.getClass().equals(obj.getClass()) && this.isSameAs((Asset) obj);
	}

	@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
	public Date getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Date closeDate) {
		this.closeDate = closeDate;
	}

	public Double getMaxInvest() {
		return maxInvest;
	}

	public void setMaxInvest(Double maxInvest) {
		this.maxInvest = maxInvest;
	}

	public Asset maxInvest(Double maxInvest) {
		this.maxInvest = maxInvest;
		return this;
	}

	public BigDecimal getPerf() {
		return perf;
	}

	public void setPerf(BigDecimal perf) {
		this.perf = perf;
	}

	public Asset perf(BigDecimal perf) {
		this.perf = perf;
		return this;
	}

	public Integer getNbDecimals() {
		return nbDecimals;
	}

	public void setNbDecimals(Integer nbDecimals) {
		this.nbDecimals = nbDecimals;
	}

	public Asset nbDecimals(Integer nbDecimals) {
		this.nbDecimals = nbDecimals;
		return this;
	}

	
	public Integer getStopLossLimit() {
		return stopLossLimit;
	}

	public void setStopLossLimit(Integer stopLossLimit) {
		this.stopLossLimit = stopLossLimit;
	}

	public BigDecimal getFeesRate() {
		return feesRate;
	}

	public void setFeesRate(BigDecimal feesRate) {
		this.feesRate = feesRate;
	}


	public Double getVarLowLimit() {
		return varLowLimit;
	}


	public void setVarLowLimit(Double varLowLimit) {
		this.varLowLimit = varLowLimit;
	}
	

	public Asset varLowLimit(Double varLowLimit) {
		this.varLowLimit = varLowLimit;
		return this;
	}
	


	public Double getVarHighLimit() {
		return varHighLimit;
	}


	public void setVarHighLimit(Double varHighLimit) {
		this.varHighLimit = varHighLimit;
	}
	
	
	public Asset varHighLimit(Double varHighLimit) {
		this.varHighLimit = varHighLimit;
		return this;
	}


	public Asset stopLossLimit(Integer stopLossLimit) {
		this.stopLossLimit = stopLossLimit;
		return this;
	}

	public Asset feesRate(BigDecimal feesRate) {
		this.feesRate = feesRate;
		return this;
	}


	public Integer getTradeDelay() {
		return tradeDelay;
	}


	public void setTradeDelay(Integer tradeDelay) {
		this.tradeDelay = tradeDelay;
	}
	
	public Asset tradeDelay(Integer tradeDelay) {
		this.tradeDelay = tradeDelay;
		return this;
	}


	public String getAnalysisPeriod() {
		return analysisPeriod;
	}
	
	public Period getAnalysisPeriodEnum() {		
		return Period.get(analysisPeriod);
	}
	

	public void setAnalysisPeriod(String analysisPeriod) {
		this.analysisPeriod = analysisPeriod;
	}
	
	public Asset analysisPeriod(String analysisPeriod) {
		this.analysisPeriod = analysisPeriod;
		return this;
	}


	public String getFrequency() {
		return frequency;
	}
	
	public Period getFrequencyPeriod() {		
		return Period.get(frequency);
	}


	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	
	public Asset frequency(String frequency) {
		this.frequency = frequency;
		return this;
	}

	
	public Asset gapFromTrend(Double gapFromTrend) {
		this.gapFromTrend = gapFromTrend;
		return this;
	}


	public Double getGapFromTrend() {
		return gapFromTrend;
	}


	public void setGapFromTrend(Double gapFromTrend) {
		this.gapFromTrend = gapFromTrend;
	}


	@Override
	public String toString() {
		return "Asset [symbol=" + symbol + ", varLowLimit=" + varLowLimit + ", varHighLimit=" + varHighLimit
				+ ", stopLossLimit=" + stopLossLimit + ", analysisPeriod=" + analysisPeriod + ", frequency=" + frequency
				+ ", gapFromTrend=" + gapFromTrend + "]";
	}


	public Double getStopLossStart() {
		return stopLossStart;
	}


	public void setStopLossStart(Double stopLossStart) {
		this.stopLossStart = stopLossStart;
	}
	
	
	public Asset stopLossStart(Double stopLossStart) {
		this.stopLossStart = stopLossStart;
		return this;
	}


	public Double getMaxPercentLoss() {
		return maxPercentLoss;
	}


	public void setMaxPercentLoss(Double maxPercentLoss) {
		this.maxPercentLoss = maxPercentLoss;
	}
	
	public Asset maxPercentLoss(Double maxPercentLoss) {
		this.maxPercentLoss = maxPercentLoss;
		return this;
	}
	

}