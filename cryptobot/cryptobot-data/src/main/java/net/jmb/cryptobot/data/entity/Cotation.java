package net.jmb.cryptobot.data.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import net.jmb.cryptobot.data.enums.OrderSide;


/**
 * The persistent class for the cotation database table.
 * 
 */
@Entity
@NamedQuery(name="Cotation.findAll", query="SELECT c FROM Cotation c")
public class Cotation extends AbstractEntity implements Serializable, Comparable<Cotation> {
	private static final long serialVersionUID = 1L;
	
	private String symbol;
	@Temporal(TemporalType.TIMESTAMP)
	private Date datetime;
	private Double price;
	
	private Double max1h;
	private Double max12h;
	private Double max24h;
	private Double max6j;
	private Double min1h;
	private Double min12h;
	private Double min24h;
	private Double min6j;
	private BigDecimal volat1h;
	private BigDecimal volat12h;
	private BigDecimal volat24h;
	private BigDecimal volat6j;
	
	private BigDecimal var5m;
	private BigDecimal var15m;
	private BigDecimal var30m;
	private BigDecimal var1h;
	private BigDecimal var6h;
	private BigDecimal var12h;
	private BigDecimal var24h;
	private BigDecimal var6j;

	private String currentSide;
	private String flagBuy;
	private Double buyPrice;
	private Double bestBuyPrice;
	private String flagSell;
	private Double sellPrice;
	private Double bestSellPrice;
	private Double quantity;
	private BigDecimal amountB100;
	
	//bi-directional one-to-one association to Trade
	@OneToOne()
	@JoinColumn(name = "tradeId")
	private Trade trade;


	public Cotation() {
	}


	public Date getDatetime() {
		return this.datetime;
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}

	public String getFlagBuy() {
		return this.flagBuy;
	}

	public void setFlagBuy(String flagBuy) {
		this.flagBuy = flagBuy;
	}

	public String getFlagSell() {
		return this.flagSell;
	}

	public void setFlagSell(String flagSell) {
		this.flagSell = flagSell;
	}

	public Double getMax12h() {
		return this.max12h;
	}

	public void setMax12h(Double max12h) {
		this.max12h = max12h;
	}

	public Double getMax1h() {
		return this.max1h;
	}

	public void setMax1h(Double max1h) {
		this.max1h = max1h;
	}

	public Double getMax24h() {
		return this.max24h;
	}

	public void setMax24h(Double max24h) {
		this.max24h = max24h;
	}

	public Double getMax6j() {
		return this.max6j;
	}

	public void setMax6j(Double max6j) {
		this.max6j = max6j;
	}

	public Double getMin12h() {
		return this.min12h;
	}

	public void setMin12h(Double min12h) {
		this.min12h = min12h;
	}

	public Double getMin1h() {
		return this.min1h;
	}

	public void setMin1h(Double min1h) {
		this.min1h = min1h;
	}

	public Double getMin24h() {
		return this.min24h;
	}

	public void setMin24h(Double min24h) {
		this.min24h = min24h;
	}

	public Double getMin6j() {
		return this.min6j;
	}

	public void setMin6j(Double min6j) {
		this.min6j = min6j;
	}

	public Double getPrice() {
		return this.price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}


	public BigDecimal getVar12h() {
		return this.var12h;
	}

	public void setVar12h(BigDecimal var12h) {
		this.var12h = var12h;
	}

	public BigDecimal getVar15m() {
		return this.var15m;
	}

	public void setVar15m(BigDecimal var15m) {
		this.var15m = var15m;
	}

	public BigDecimal getVar1h() {
		return this.var1h;
	}

	public void setVar1h(BigDecimal var1h) {
		this.var1h = var1h;
	}

	public BigDecimal getVar24h() {
		return this.var24h;
	}

	public void setVar24h(BigDecimal var24h) {
		this.var24h = var24h;
	}

	public BigDecimal getVar30m() {
		return this.var30m;
	}

	public void setVar30m(BigDecimal var30m) {
		this.var30m = var30m;
	}

	public BigDecimal getVar5m() {
		return this.var5m;
	}

	public void setVar5m(BigDecimal var5m) {
		this.var5m = var5m;
	}

	public BigDecimal getVar6h() {
		return this.var6h;
	}

	public void setVar6h(BigDecimal var6h) {
		this.var6h = var6h;
	}

	public BigDecimal getVar6j() {
		return this.var6j;
	}

	public void setVar6j(BigDecimal var6j) {
		this.var6j = var6j;
	}

	public BigDecimal getVolat12h() {
		return this.volat12h;
	}

	public void setVolat12h(BigDecimal volat12h) {
		this.volat12h = volat12h;
	}

	public BigDecimal getVolat1h() {
		return this.volat1h;
	}

	public void setVolat1h(BigDecimal volat1h) {
		this.volat1h = volat1h;
	}

	public BigDecimal getVolat24h() {
		return this.volat24h;
	}

	public void setVolat24h(BigDecimal volat24h) {
		this.volat24h = volat24h;
	}

	public BigDecimal getVolat6j() {
		return this.volat6j;
	}

	public void setVolat6j(BigDecimal volat6j) {
		this.volat6j = volat6j;
	}


		public Cotation datetime(Date datetime) {
			this.datetime = datetime;
			return this;
		}

		public Cotation flagBuy(String flagBuy) {
			this.flagBuy = flagBuy;
			return this;
		}
		
		public Cotation flagBuy() {
			this.flagBuy = "B";
			return this;
		}

		public Cotation flagSell() {
			this.flagSell = "S";
			return this;
		}

		public Cotation flagSell(String flagSell) {
			this.flagSell = flagSell;
			return this;
		}

		public Cotation max12h(Double max12h) {
			this.max12h = max12h;
			return this;
		}

		public Cotation max1h(Double max1h) {
			this.max1h = max1h;
			return this;
		}

		public Cotation max24h(Double max24h) {
			this.max24h = max24h;
			return this;
		}

		public Cotation max6j(Double max6j) {
			this.max6j = max6j;
			return this;
		}

		public Cotation min12h(Double min12h) {
			this.min12h = min12h;
			return this;
		}

		public Cotation min1h(Double min1h) {
			this.min1h = min1h;
			return this;
		}

		public Cotation min24h(Double min24h) {
			this.min24h = min24h;
			return this;
		}

		public Cotation min6j(Double min6j) {
			this.min6j = min6j;
			return this;
		}

		public Cotation price(Double price) {
			this.price = price;
			return this;
		}

		public Cotation symbol(String symbol) {
			this.symbol = symbol;
			return this;
		}


		public Cotation var12h(BigDecimal var12h) {
			this.var12h = var12h;
			return this;
		}

		public Cotation var15m(BigDecimal var15m) {
			this.var15m = var15m;
			return this;
		}

		public Cotation var1h(BigDecimal var1h) {
			this.var1h = var1h;
			return this;
		}

		public Cotation var24h(BigDecimal var24h) {
			this.var24h = var24h;
			return this;
		}

		public Cotation var30m(BigDecimal var30m) {
			this.var30m = var30m;
			return this;
		}

		public Cotation var5m(BigDecimal var5m) {
			this.var5m = var5m;
			return this;
		}

		public Cotation var6h(BigDecimal var6h) {
			this.var6h = var6h;
			return this;
		}

		public Cotation var6j(BigDecimal var6j) {
			this.var6j = var6j;
			return this;
		}

		public Cotation volat12h(BigDecimal volat12h) {
			this.volat12h = volat12h;
			return this;
		}

		public Cotation volat1h(BigDecimal volat1h) {
			this.volat1h = volat1h;
			return this;
		}

		public Cotation volat24h(BigDecimal volat24h) {
			this.volat24h = volat24h;
			return this;
		}

		public Cotation volat6j(BigDecimal volat6j) {
			this.volat6j = volat6j;
			return this;
		}


		public Trade getTrade() {
			return trade;
		}


		public void setTrade(Trade trade) {
			this.trade = trade;
		}
		
		public Cotation trade(Trade trade) {
			this.trade = trade;
			return this;
		}


		public String getCurrentSide() {
			return currentSide;
		}

		public void setCurrentSide(String currentSide) {
			this.currentSide = currentSide;
		}
		
		public Cotation currentSide(String currentSide) {
			this.currentSide = currentSide;
			return this;
		}
		
		public Cotation currentSide(OrderSide currentSide) {
			this.currentSide = currentSide.name();
			return this;
		}
		
		public OrderSide getCurrentOrderSide() {
			return(OrderSide.get(currentSide));
		}

		
		public Cotation resetEvaluation() {
			return this.amountB100(null)
				.quantity(null)
				.sellPrice(null)
				.buyPrice(null)
				.bestBuyPrice(null)
				.bestSellPrice(null)
				.currentSide((String)null)
				.flagBuy(null)
				.flagSell(null);
		}


		@Override
		public int compareTo(Cotation o) {
			if (datetime == null && o.getDatetime() != null) {
				return -1;
			}
			if (datetime != null && o.getDatetime() == null) {
				return 1;
			}
			if (symbol == null && o.getSymbol() != null) {
				return -1;
			}
			if (symbol != null && o.getSymbol() == null) {
				return 1;
			}
			
			if (datetime.compareTo(o.getDatetime()) != 0) {
				return datetime.compareTo(o.getDatetime());
			}
			return symbol.compareTo(o.getSymbol());
		}


		@Override
		public int hashCode() {
			return Objects.hash(datetime, symbol);
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Cotation other = (Cotation) obj;
			return Objects.equals(getId(), other.getId()) || Objects.equals(datetime, other.datetime) && Objects.equals(symbol, other.symbol);
		}


		public BigDecimal getAmountB100() {
			return amountB100;
		}

		public void setAmountB100(BigDecimal amountB100) {
			this.amountB100 = amountB100;
		}
		
		public Cotation amountB100(BigDecimal amountB100) {
			this.amountB100 = amountB100;
			return this;
		}


		public Double getBestBuyPrice() {
			return bestBuyPrice;
		}


		public void setBestBuyPrice(Double bestBuyPrice) {
			this.bestBuyPrice = bestBuyPrice;
		}
		
		public Cotation bestBuyPrice(Double bestBuyPrice) {
			this.bestBuyPrice = bestBuyPrice;
			return this;
		}


		public Double getBestSellPrice() {
			return bestSellPrice;
		}


		public void setBestSellPrice(Double bestSellPrice) {
			this.bestSellPrice = bestSellPrice;
		}
		
		public Cotation bestSellPrice(Double bestSellPrice) {
			this.bestSellPrice = bestSellPrice;
			return this;
		}


		public Cotation duplicate() {
			Cotation newCotation = new Cotation();
			newCotation.setId(this.getId());
			newCotation.symbol = this.symbol;
			newCotation.datetime = this.datetime;
			newCotation.price = this.price;
			newCotation.var5m = this.var5m;
			newCotation.currentSide = this.currentSide;
			newCotation.flagBuy = this.flagBuy;
			newCotation.bestBuyPrice = this.bestBuyPrice;
			newCotation.flagSell = this.flagSell;
			newCotation.bestSellPrice = this.bestSellPrice;
			newCotation.quantity = this.quantity;
			newCotation.amountB100 = this.amountB100;
			newCotation.buyPrice = this.buyPrice;
			newCotation.sellPrice = this.sellPrice;
			newCotation.trade = this.trade;
			return newCotation;
		}


		@Override
		public String toString() {
			String date = (datetime != null) ? new SimpleDateFormat("dd/MM HH:mm").format(datetime) : null;
			return "Cotation [" + symbol + " " + date + ": price=" + price + ", side=" + currentSide + ", quantity=" + quantity + ", amountB100=" + amountB100
				+ ", buyPrice=" + buyPrice + ", bestBuyPrice=" + bestBuyPrice + ", sellPrice=" + sellPrice + ", bestSellPrice=" + bestSellPrice + "]";
		}


		public Double getBuyPrice() {
			return buyPrice;
		}


		public void setBuyPrice(Double buyPrice) {
			this.buyPrice = buyPrice;
		}


		public Double getSellPrice() {
			return sellPrice;
		}


		public void setSellPrice(Double sellPrice) {
			this.sellPrice = sellPrice;
		}
		
		public Cotation sellPrice(Double sellPrice) {
			this.sellPrice = sellPrice;
			return this;
		}
		
		public Cotation buyPrice(Double buyPrice) {
			this.buyPrice = buyPrice;
			return this;
		}


		public Double getQuantity() {
			return quantity;
		}


		public void setQuantity(Double quantity) {
			this.quantity = quantity;
		}
		
		public Cotation quantity(Double quantity) {
			this.quantity = quantity;
			return this;
		}


}