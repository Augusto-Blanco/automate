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

@Entity
@Table(name = "asset")
@NamedQuery(name="Asset.findAll", query="SELECT v FROM Asset v")
public class Asset extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	private String platform;	
	private String symbol;
	@Column(name="qty")
	private Double quantity;
	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;	
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdate;	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="close")
	private Date closeDate;
	private Integer nbDecimals;
	private BigDecimal perf;


	@OneToMany(mappedBy="asset", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
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

	
	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}
	
	public Asset quantity(Double quantity) {
		this.quantity = quantity;
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

	

}