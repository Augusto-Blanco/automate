package net.jmb.cryptobot.data.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PageData<T> {

	private List<T> data;
	private int numberOfElements;
	private int pageNumber;
	private int pageSize;
	private long totalElements;
	private int totalPages;
	private boolean hasNext;
	private boolean hasPrevious;
	private boolean empty;
	
	private String sortColName;
	private String sortDirection;

	
	public PageData() {
		super();
	}

	public PageData(List<T> data) {
		super();
		this.data = data;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public int getNumberOfElements() {
		return numberOfElements;
	}

	public void setNumberOfElements(int numberOfElements) {
		this.numberOfElements = numberOfElements;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public long getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(long totalElements) {
		this.totalElements = totalElements;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	@JsonProperty("hasNext")
	public boolean isHasNext() {
		return hasNext;
	}

	@JsonProperty("hasPrevious")
	public boolean isHasPrevious() {
		return hasPrevious;
	}


	public boolean isEmpty() {
		return empty;
	}


	public PageData<T> data(List<T> data) {
		this.data = data;
		return this;
	}

	public PageData<T> numberOfElements(int numberOfElements) {
		this.numberOfElements = numberOfElements;
		return this;
	}

	public PageData<T> pageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
		return this;
	}

	public PageData<T> pageSize(int pageSize) {
		this.pageSize = pageSize;
		return this;
	}

	public PageData<T> totalElements(long totalElements) {
		this.totalElements = totalElements;
		return this;
	}

	public PageData<T> totalPages(int totalPages) {
		this.totalPages = totalPages;
		return this;
	}

	public PageData<T> setHasNext(boolean hasNext) {
		this.hasNext = hasNext;
		return this;
	}

	public PageData<T> setHasPrevious(boolean hasPrevious) {
		this.hasPrevious = hasPrevious;
		return this;
	}

	public PageData<T> setEmpty(boolean empty) {
		this.empty = empty;
		return this;
	}

	@Override
	public String toString() {
		return "PageData [numberOfElements=" + numberOfElements + ", pageNumber=" + pageNumber + ", pageSize="
				+ pageSize + ", totalElements=" + totalElements + ", totalPages=" + totalPages + ", hasNext=" + hasNext
				+ ", hasPrevious=" + hasPrevious + ", empty=" + empty + "]";
	}

	public String getSortColName() {
		return sortColName;
	}

	public void setSortColName(String sortColName) {
		this.sortColName = sortColName;
	}

	public String getSortDirection() {
		return sortDirection;
	}

	public void setSortDirection(String sortDirection) {
		this.sortDirection = sortDirection;
	}
	
	public PageData<T> sortColName(String sortColName) {
		this.sortColName = sortColName;
		return this;
	}

	public PageData<T> sortDirection(String sortDirection) {
		this.sortDirection = sortDirection;
		return this;
	}

}
