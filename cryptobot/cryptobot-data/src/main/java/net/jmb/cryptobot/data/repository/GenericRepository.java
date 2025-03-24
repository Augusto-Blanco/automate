package net.jmb.cryptobot.data.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class GenericRepository {


	public static final int DEFAULT_PAGE_SIZE = 20;
	public static final int DEFAULT_MAX_TO_LOAD = 5000;
	
	@PersistenceContext
	protected EntityManager em;
	
	
	protected Logger getLogger() {
		return LoggerFactory.getLogger(this.getClass());
	}
	

	public <T> T save(T entity) {
		try {
			em.persist(entity);
		} catch (Exception e) {
			entity = em.merge(entity);
		}
		return entity;
	}



}
