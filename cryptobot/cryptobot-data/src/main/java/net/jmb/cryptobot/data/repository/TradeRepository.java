package net.jmb.cryptobot.data.repository;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import net.jmb.cryptobot.data.bean.OrderQO;
import net.jmb.cryptobot.data.entity.Trade;

@Repository
@Transactional
public interface TradeRepository 
	extends JpaRepository<Trade, Long>, JpaSpecificationExecutor<Trade> {
	
	default Specification<Trade> specification(OrderQO orderQO) {
		if (orderQO != null && !orderQO.isEmpty()) {
			Specification<Trade> specification = 
				(root, query, builder) -> {
					Predicate predicate = builder.conjunction();
					if (StringUtils.isNotBlank(orderQO.getTradeRef())) {
						predicate = builder.equal(root.get("numContrat"), orderQO.getTradeRef());
					} else {
						Join<Object, Object> fluxRecuJoin = root.join("fluxRecu");
						if (orderQO.isAvecAno()) {					
							Predicate avecAno = builder.equal(root.get("statutIntegration"), "KO");
							predicate = builder.and(predicate, avecAno);
						}
						if (orderQO.getDDateDebut() != null) {
							Predicate dateDu = builder.greaterThanOrEqualTo(fluxRecuJoin.get("dateFlux"), orderQO.getDDateDebut());
							predicate = builder.and(predicate, dateDu);
						}
						if (orderQO.getDDateFin() != null) {
							Predicate dateAu = builder.lessThanOrEqualTo(fluxRecuJoin.get("dateFlux"), orderQO.getDDateFin());
							predicate = builder.and(predicate, dateAu);
						}
						if (StringUtils.isNotBlank(orderQO.getBatchEnAno())) {
							Predicate batchEnAno = builder.like(root.get("trtAnoNiv2"), "%" + orderQO.getBatchEnAno() + "%");
							predicate = builder.and(predicate, batchEnAno);
						}
						if (orderQO.isAnoNiv1()) {					
							Predicate avecAno1 = builder.and(builder.isNotNull(root.get("msgAnoNiv1")), builder.notEqual(root.get("msgAnoNiv1"), ""));
							predicate = builder.and(predicate, avecAno1);
						}
						if (StringUtils.isNotBlank(orderQO.getSiret())) {
							Predicate siret = builder.like(root.get("siret"), orderQO.getSiret() + "%");
							predicate = builder.and(predicate, siret);
						}
						if (orderQO.isNotTraiteParVacation()) {					
							Predicate notTraite = builder.isNull(root.get("dateVacation"));
							predicate = builder.and(predicate, notTraite);
						}
					}
					return predicate;
				};
			return specification;
		}
		return null;
	}
	
	
	List<Trade> findByAssetId(Long id);
	
	Page<Trade> findByAssetId(Long id, Pageable pageable);
	
	default Page<Trade> findByOrderQO(OrderQO contratQO) {
		return findByOrderQO(contratQO, null);
	}
	
	default Page<Trade> findByOrderQO(OrderQO contratQO, Pageable pageable) {
		Specification<Trade> specification = specification(contratQO);
		Sort defaultSort = Sort.by(Direction.DESC, "fluxRecu.id").and(Sort.by("id"));
		if (pageable == null) {
			pageable = Pageable.unpaged(defaultSort);
		} else if (pageable.getSort().isEmpty()) {
			pageable = ((PageRequest) pageable).withSort(defaultSort);
		}
		if (specification != null) {
			return findAll(specification, pageable);
		}
		return findAll(pageable);
	}
	
	

	@Query("select distinct tradeRef from Trade")
	Page<String> findDistinctTradeRefs(Pageable pageable);
	

	List<Trade> findByStateIn(List<String> states);


}
