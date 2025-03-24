package net.jmb.cryptobot.data.repository;

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
		+ 	"	where b.symbol = :symbol and b.endTime <= :dateRef "
		+	")";

	
	
	default Specification<AssetConfig> specDateGreaterOrEquals(String symbol, Date refDate) {		
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
			return predicate;
		};
		return specif;
	}
	
	
	default public long deleteDateGreaterOrEquals(String symbol, Date refDate) {
		return delete(specDateGreaterOrEquals(symbol, refDate));
	}
	
	List<AssetConfig> findBySymbolEqualsAndEndTimeGreaterThanEqual(String symbol, Date dateRef);
	
	
	public List<AssetConfig> findBySymbol(String symbol);
	
	@Query(ASSET_CONFIG_FOR_SYMBOL_AND_DATE)
	public List<AssetConfig> findBySymbolAndDate(String symbol, Date dateRef);
	
	
	


}
