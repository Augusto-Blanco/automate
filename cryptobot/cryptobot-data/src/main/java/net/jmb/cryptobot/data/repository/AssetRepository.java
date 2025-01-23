package net.jmb.cryptobot.data.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import net.jmb.cryptobot.data.bean.AssetQO;
import net.jmb.cryptobot.data.entity.Asset;

@Repository
@Transactional
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

	
	
	public static final String ASSET_FOR_TRADE_REF = 
			"select distinct a from Asset a join Trade b on b.asset = a "
		+   "where b.tradeRef = :tradeRef order by a.id desc";
	
	
	default Specification<Asset> specification(AssetQO assetQO) {
		
		Specification<Asset> specif = (root, query, builder) -> {
			Predicate predicate = builder.conjunction();
			if (assetQO != null) {
				if (assetQO.isAvecAno()) {
					Join<Object, Object> contrats = root.join("integrationContrats");
					Predicate avecAno = builder.equal(contrats.get("statutIntegration"), "KO");
					predicate = builder.and(predicate, avecAno);
				}
				if (assetQO.getDDateDebut() != null) {
					Predicate dateDu = builder.greaterThanOrEqualTo(root.get("dateFlux"),
							assetQO.getDDateDebut());
					predicate = builder.and(predicate, dateDu);
				}
				if (assetQO.getDDateFin() != null) {
					Predicate dateAu = builder.lessThanOrEqualTo(root.get("dateFlux"), assetQO.getDDateFin());
					predicate = builder.and(predicate, dateAu);
				}
				if (assetQO.isNotTraiteParVacation()) {					
					Predicate notTraite = builder.isNull(root.get("dateVacation"));
					predicate = builder.and(predicate, notTraite);
				}
			}
			return predicate;
		};
		return specif;
	}
	
	
	default Page<Asset> findByAssetQO(AssetQO assetQO, Pageable pageable) {
		Specification<Asset> specification = specification(assetQO);
		if (pageable != null) {
			return findAll(specification, pageable);
		}
		return null;
	}
	
	default List<Asset> findByAssetQO(AssetQO assetQO) {		
		Specification<Asset> specification = specification(assetQO);
		return findAll(specification);
	}	
	
	
	public Asset findBySymbolAndPlatformEquals(String symbol, String platform);
	
	public Asset findBySymbol(String symbol);
	
	@Query(ASSET_FOR_TRADE_REF)
	public List<Asset> findByTradeRef(String tradeRef);
	


}
