package net.jmb.cryptobot.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import net.jmb.cryptobot.data.entity.Trade;

@Repository
@Transactional
public interface TradeRepository extends JpaRepository<Trade, Long>, JpaSpecificationExecutor<Trade> {


	List<Trade> findByAssetIdAndStateInOrderByTime(Long id, List<String> states);
	
	Trade findFirstBySymbolAndSideOrderByTimeDesc(String symbol, String side);
	
	Trade findFirstBySymbolOrderByTimeDesc(String symbol);
	
	
	@Query(
		"select t from Trade t "
			+ "where asset.id = :assetId and execQty is not null and "
			+ "(soldedQty is null or soldedQty < execQty) "
			+ "order by time "
	)
	List<Trade> findByAssetIdAndNotSoldedQty(Long assetId);


	Trade findByTradeRef(String tradeRef);
	

	List<Trade> findByStateIn(List<String> states);


}
