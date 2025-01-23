package net.jmb.cryptobot.data.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;


/**
 * The persistent class for the asset_config database table.
 * 
 */
@Entity
@Table(name="asset_config")
@NamedQuery(name="AssetConfig.findAll", query="SELECT a FROM AssetConfig a")

public class AssetConfig extends AbstractEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String symbol;
	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;	
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;
	private BigDecimal maxVarHigh;
	private BigDecimal maxVarLow;
	private BigDecimal stopLoss;

	
		
	public AssetConfig() {
	}

	
	public Date getEndTime() {
		return this.endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public BigDecimal getMaxVarHigh() {
		return this.maxVarHigh;
	}

	public void setMaxVarHigh(BigDecimal maxVarHigh) {
		this.maxVarHigh = maxVarHigh;
	}

	public BigDecimal getMaxVarLow() {
		return this.maxVarLow;
	}

	public void setMaxVarLow(BigDecimal maxVarLow) {
		this.maxVarLow = maxVarLow;
	}

	public Date getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public BigDecimal getStopLoss() {
		return this.stopLoss;
	}

	public void setStopLoss(BigDecimal stopLoss) {
		this.stopLoss = stopLoss;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}


	public AssetConfig symbol(String symbol) {
		this.symbol = symbol;
		return this;
	}

	public AssetConfig startTime(Date startTime) {
		this.startTime = startTime;
		return this;
	}

	public AssetConfig endTime(Date endTime) {
		this.endTime = endTime;
		return this;
	}

	public AssetConfig maxVarHigh(BigDecimal maxVarHigh) {
		this.maxVarHigh = maxVarHigh;
		return this;
	}

	public AssetConfig maxVarLow(BigDecimal maxVarLow) {
		this.maxVarLow = maxVarLow;
		return this;
	}

	public AssetConfig stopLoss(BigDecimal stopLoss) {
		this.stopLoss = stopLoss;
		return this;
	}


	@Override
	public String toString() {
		return "AssetConfig [symbol=" + symbol + ", startTime=" + startTime + ", maxVarHigh=" + maxVarHigh
				+ ", maxVarLow=" + maxVarLow + ", stopLoss=" + stopLoss + "]";
	}



}