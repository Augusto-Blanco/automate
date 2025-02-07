package net.jmb.cryptobot.data.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import net.jmb.cryptobot.data.entity.Cotation;

@Repository
@Transactional
public interface CotationRepository extends JpaRepository<Cotation, Long> {

//	private String symbol;
//	private Date datetime;
//	private Double price;
//	private String currentSide;
//	private String flagBuy;
//	private Double bestBuyPrice;
//	private String flagSell;
//	private Double bestSellPrice;
//	private BigDecimal amountB100;
//	@JoinColumn(name = "trade_id")
//	private Trade trade;	

	public static final String LAST_COTATION_FOR_SYMBOL_QUERY = 		
				"select a from Cotation a "
			+   "where a.symbol = :symbol and a.datetime = ( "
			+   "	select max(b.datetime) from Cotation b "
			+ 	"	where b.symbol = :symbol and b.datetime <= :maxDate "
			+	")";
	
	public static final String LAST_SELL_COTATION_BEFORE_DATE_QUERY = 		
			"select a from Cotation a "
		+   "where a.symbol = :symbol and a.currentSide = 'SELL' and a.datetime = ( "
		+   "	select max(b.datetime) from Cotation b "
		+	"	where b.symbol = :symbol and b.currentSide = 'SELL' and b.datetime <= :date "
		+	")";
	

	public static final String LAST_RATED_COTATION_QUERY = 		
			"select a from Cotation a "
		+   "where a.symbol = :symbol and a.currentSide is not null and a.datetime = ( "
		+   "	select max(b.datetime) from Cotation b "
		+ 	"	where b.symbol = :symbol and b.currentSide is not null "
		+	")";
	
	public static final String MIN_PRICE_24H_COTATION_QUERY = 		
			"select a from Cotation a "
		+   "where a.symbol = :symbol and a.price = a.min24h "
		+   "and a.datetime >= :startDate "
		+	"order by datetime";
		
	
	@Query(LAST_RATED_COTATION_QUERY)
	List<Cotation> findLastRatedCotationForSymbol(String symbol);		
	
	@Query(LAST_COTATION_FOR_SYMBOL_QUERY)
	List<Cotation> findLastCotationForSymbolBeforeDate(String symbol, Date maxDate);
	
	@Query(LAST_SELL_COTATION_BEFORE_DATE_QUERY)
	List<Cotation> findLastSellCotationBeforeDate(String symbol, Date date);
	
	@Query(MIN_PRICE_24H_COTATION_QUERY)
	List<Cotation> findMinPrice24hCotationForSymbolAfterDate(String symbol, Date startDate);
	
	
	List<Cotation> findBySymbolEqualsAndDatetimeGreaterThanEqualOrderByDatetime(String symbol, Date datetime);
	
	List<Cotation> findBySymbolEqualsAndDatetimeBetweenOrderByDatetime(String symbol, Date datetime0, Date datetime1);

	
	Cotation findByTradeId(Long id);
	


}
