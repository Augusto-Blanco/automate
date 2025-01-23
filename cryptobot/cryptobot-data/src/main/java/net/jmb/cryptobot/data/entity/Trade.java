package net.jmb.cryptobot.data.entity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import net.jmb.cryptobot.data.enums.OrderState;

/**
 * The persistent class for the integration_contrat database table.
 * 
 */
@Entity
@Table(name = "trade")
@NamedQuery(name = "Trade.findAll", query = "SELECT i FROM Trade i")
@JsonIgnoreProperties(ignoreUnknown = true)

public class Trade extends AbstractEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String platform;
	private String symbol;
	private String tradeRef;
	@Temporal(TemporalType.TIMESTAMP)
	private Date time;
	private String side;
	@Column(name="qty")
	private Double quantity;
	private Double price;	
	private Double amount;	
	private String state = OrderState.PENDING.name();
	
	// bi-directional one-to-one association to Cotation
	@OneToOne(mappedBy = "trade", fetch=FetchType.LAZY)
	@JsonIgnore
	private Cotation cotation;
	
	// bi-directional many-to-one association to Asset
	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "assetId")
	private Asset asset;



	public Trade() {
	}

	
	public Cotation getCotation() {
		return this.cotation;
	}

	public void setCotation(Cotation cotation) {
		this.cotation = cotation;
	}


	public Asset getAsset() {
		return this.asset;
	}

	public void setAsset(Asset asset) {
		this.asset = asset;
	}


	public Trade asset(Asset asset) {
		this.asset = asset;
		return this;
	}

	public Trade id(Long id) {
		setId(id);
		return this;
	}
	


	public String getState() {
		return state;
	}
	

	public void setState(String state) {
		this.state = state;
	}
	
	public Trade state(String state) {
		this.state = state;
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


	public Date getTime() {
		return time;
	}


	public void setTime(Date time) {
		this.time = time;
	}


	public String getSide() {
		return side;
	}


	public void setSide(String side) {
		this.side = side;
	}


	public Double getQuantity() {
		return quantity;
	}


	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}


	public Double getPrice() {
		return price;
	}


	public void setPrice(Double price) {
		this.price = price;
	}


	public Double getAmount() {
		return amount;
	}


	public void setAmount(Double amount) {
		this.amount = amount;
	}


		public Trade platform(String platform) {
			this.platform = platform;
			return this;
		}

		public Trade symbol(String symbol) {
			this.symbol = symbol;
			return this;
		}

		public Trade time(Date time) {
			this.time = time;
			return this;
		}

		public Trade side(String side) {
			this.side = side;
			return this;
		}

		public Trade quantity(Double quantity) {
			this.quantity = quantity;
			return this;
		}

		public Trade price(Double price) {
			this.price = price;
			return this;
		}

		public Trade amount(Double amount) {
			this.amount = amount;
			return this;
		}


		public Trade cotation(Cotation cotation) {
			this.cotation = cotation;
			return this;
		}


		public String getTradeRef() {
			return tradeRef;
		}

		public void setTradeRef(String tradeRef) {
			this.tradeRef = tradeRef;
		}
		
		public Trade tradeRef(String tradeRef) {
			this.tradeRef = tradeRef;
			return this;
		}



}