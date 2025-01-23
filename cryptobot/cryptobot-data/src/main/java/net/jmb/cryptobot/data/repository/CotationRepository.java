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

		
	
	@Query(LAST_COTATION_FOR_SYMBOL_QUERY)
	Cotation findLastCotationForSymbolBeforeDate(String symbol, Date maxDate);		
	
	
	List<Cotation> findBySymbolEqualsAndDatetimeGreaterThanEqual(String symbol, Date datetime);
	
	List<Cotation> findBySymbolEqualsAndDatetimeBetween(String symbol, Date datetime0, Date datetime1);
	
	Cotation findByTradeId(Long id);
	


}
