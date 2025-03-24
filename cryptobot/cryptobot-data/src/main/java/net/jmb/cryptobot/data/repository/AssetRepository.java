package net.jmb.cryptobot.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import net.jmb.cryptobot.data.entity.Asset;

@Repository
@Transactional
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

	
	
	public static final String ASSET_FOR_TRADE_REF = 
			"select distinct a from Asset a join Trade b on b.asset = a "
		+   "where b.tradeRef = :tradeRef order by a.id desc";
	
	
	public Asset findBySymbolAndPlatformEquals(String symbol, String platform);
	
	public List<Asset> findBySymbol(String symbol);
	
	public List<Asset> findByPlatform(String platform);
	
	@Query(ASSET_FOR_TRADE_REF)
	public List<Asset> findByTradeRef(String tradeRef);
	


}
