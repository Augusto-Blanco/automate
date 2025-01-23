package net.jmb.cryptobot.data.repository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.jmb.cryptobot.data.bean.PageData;

@Repository
public class GenericRepository {


	public static final int DEFAULT_PAGE_SIZE = 20;
	public static final int DEFAULT_MAX_TO_LOAD = 5000;
	
	@PersistenceContext
	protected EntityManager em;	
	
	
	protected Logger getLogger() {
		return LoggerFactory.getLogger(this.getClass());
	}
	

	public Pageable getPageable(Integer numPage, Integer pageSize) {
		return getPageable(numPage, pageSize, null);
	}
	
	public Pageable getPageableDesc(Integer numPage, Integer pageSize) {
		return getPageable(numPage, pageSize, Sort.by("id").descending());
	}	

	public Pageable getPageable(Integer numPage, Integer pageSize, Sort tri) {
		if (numPage == null) {
			numPage = 0;
		}
		if (pageSize == null) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		if (tri == null) {
			tri = Sort.by("id").ascending();
		}
		return PageRequest.of(numPage, pageSize, tri);
	}
	
	public <T> List<T> getLastItems(JpaRepository<T, Long> repository, Integer nbItems) {
		Page<T> page = repository.findAll(getPageable(null, nbItems, Sort.by(Direction.DESC, "id")));
		if (page != null) {
			return page.getContent();
		}
		return null;
	}
	
	public <T> List<T> getLastItems(JpaRepository<T, Long> repository) {	
		return getLastItems(repository, null);
	}
	
	
	public <T> PageData<T> getPageData(Page<T> page) {		
		PageData<T> pageData = new PageData<T>();		
		if (page != null) {
			pageData.data(page.getContent())
				.numberOfElements(page.getNumberOfElements())
				.pageNumber(page.getNumber())
				.pageSize(page.getSize())
				.totalElements(page.getTotalElements())
				.totalPages(page.getTotalPages())
				.setHasNext(page.hasNext())
				.setHasPrevious(page.hasPrevious())
				.setEmpty(page.isEmpty());
			if (page.getSort() != null && !page.getSort().isEmpty()) {
				Order order = page.getSort().iterator().next();
				pageData.sortColName(order.getProperty()).sortDirection(order.getDirection().name());
			}
		}	
		return pageData;		
	}
	
	
	public <T> PageData<T> getPageData(List<T> resultList) {		
		PageData<T> pageData = new PageData<T>();		
		if (resultList != null) {
			pageData.data(resultList)
				.numberOfElements(resultList.size())
				.pageNumber(0)
				.pageSize(resultList.size())
				.totalElements(resultList.size())
				.totalPages(1)
				.setHasNext(false)
				.setHasPrevious(false)
				.setEmpty(resultList.isEmpty());
		}	
		return pageData;		
	}



}
