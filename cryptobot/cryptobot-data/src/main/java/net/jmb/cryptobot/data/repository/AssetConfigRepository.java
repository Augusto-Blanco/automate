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
		+   "where a.symbol = :symbol and (a.endTime IS NULL or a.endTime > :dateRef) "
		+ 	"and a.startTime = :dateRef or a.startTime = ( "
		+   "	select max(b.startTime) from AssetConfig b "
		+ 	"	where b.symbol = :symbol and b.startTime <= :dateRef "
		+ 	"	and (b.endTime IS NULL or b.endTime > :dateRef) "
		+	")";

	
	
	default Specification<AssetConfig> specDateGreaterOrEquals(String symbol, Date refDate) {		
		Specification<AssetConfig> specif = (root, query, builder) -> {
			Predicate predicate = builder.conjunction();	
			if (refDate != null) {
				Predicate startTimePredicate = builder.greaterThanOrEqualTo(root.get("endTime"), refDate);
				predicate = builder.and(predicate, startTimePredicate);
			}
			if (symbol != null) {
				Predicate symbolPredicate = builder.equal(root.get("symbol"), symbol);
				predicate = builder.and(predicate, symbolPredicate);
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
	public AssetConfig findBySymbolAndDate(String symbol, Date dateRef);
	
	
	


}
