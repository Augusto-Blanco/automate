package net.jmb.cryptobot.data.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import net.jmb.cryptobot.data.entity.AssetConfig;

@Repository
@Transactional
public interface AssetConfigRepository extends JpaRepository<AssetConfig, Long>, JpaSpecificationExecutor<AssetConfig> {

	
	public static final String ASSET_CONFIG_FOR_SYMBOL_AND_DATE = 
			"select a from AssetConfig a "
		+   "where a.symbol = :symbol and a.endTime <= :dateRef "
		+ 	"and a.endTime = ( "
		+   "	select max(b.endTime) from AssetConfig b "
		+ 	"	where b.symbol = :symbol and (b.analysisPeriod = a.analysisPeriod or (b.analysisPeriod is NULL and a.analysisPeriod IS NULL))"
		+	"	and b.endTime <= :dateRef "
		+	")";

	
	
	default Specification<AssetConfig> specDateGreaterOrEquals(String symbol, Date refDate, String analysisPeriod) {		
		Specification<AssetConfig> specif = (root, query, criteria) -> {
			Predicate predicate = criteria.conjunction();	
			if (refDate != null) {
				Predicate endTimePredicate = criteria.greaterThanOrEqualTo(root.get("endTime"), refDate);
				predicate = criteria.and(predicate, endTimePredicate);
			}
			if (symbol != null) {
				Predicate symbolPredicate = criteria.equal(root.get("symbol"), symbol);
				predicate = criteria.and(predicate, symbolPredicate);
			}
			if (analysisPeriod != null) {
				Predicate periodPredicate = criteria.equal(root.get("analysisPeriod"), analysisPeriod);
				predicate = criteria.and(predicate, periodPredicate);
			}
			return predicate;
		};
		return specif;
	}
	
	
	default public long deleteDateGreaterOrEquals(String symbol, Date refDate, String analysisPeriod) {
		return delete(specDateGreaterOrEquals(symbol, refDate, analysisPeriod));
	}
	
	
	public List<AssetConfig> findBySymbol(String symbol);
	
	@Query(ASSET_CONFIG_FOR_SYMBOL_AND_DATE)
	public List<AssetConfig> findBySymbolAndDate(String symbol, Date dateRef);
	
	

	default public List<AssetConfig> findBySymbolAndDateOrDateGreater(String symbol, Date dateRef) {
		List<AssetConfig> result = new ArrayList<AssetConfig>();
		List<AssetConfig> refConfigs = findBySymbolAndDate(symbol, dateRef);
		if (refConfigs != null) {
			for (AssetConfig assetConfig : refConfigs) {
				result.addAll(findAll(specDateGreaterOrEquals(symbol, assetConfig.getEndTime(), assetConfig.getAnalysisPeriod())));
			}
		}
		return result;		
	}
	
	


}
