<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!-- 
Input ~ PageData
	pageNumber
	pageSize
	totalPages
	hasNext
	hasPrevious
	empty
	sortColName
	sortDirection
Output ~ PageRequest
	sortColName
	sortDirection
	start
	length
	numPage
 -->
 
 		<c:if test="${pageData ne null and pageData.totalPages gt 1}">
			
			<input type="hidden" name="length" value="${pageData.pageSize}">
			<input type="hidden" name="sortColName" value="${pageData.sortColName}">
			<input type="hidden" name="sortDirection" value="${pageData.sortDirection}">
			
		 	<c:set var="totalPages" value="${pageData.totalPages}"></c:set>
		 	<c:if test="${totalPages > maxPageNumber}">
		 		<c:set var="totalPages" value="${maxPageNumber}"></c:set>
		 	</c:if>
					
			<!-- gestion bouton précédent -->
			<c:choose>
				<c:when test="${pageData.hasPrevious}">
					<c:set var="prevValue" value="${pageData.pageNumber - 1}"/>
				</c:when>
				<c:otherwise>
					<c:set var="prevStatus" value="disabled"/>
				</c:otherwise>			
			</c:choose>
			<!-- gestion bouton suivant -->
			<c:choose>
				<c:when test="${pageData.hasNext}">
					<c:set var="nextValue" value="${pageData.pageNumber + 1}"/>
				</c:when>
				<c:otherwise>
					<c:set var="nextStatus" value="disabled"/>
				</c:otherwise>			
			</c:choose>
			
			<ul class="pagination justify-content-end">				
				<li class="page-item ${prevStatus}">
					<button type="submit" name="numPage" value="${prevValue}" class="page-link" ${prevStatus}>
						Précédent
					</button>
				</li>				
				<c:forEach begin="0" end="${totalPages - 1}" var="numPage">
					<c:set var="pageClass" value=""/>
					<c:set var="pageStatus" value=""/>
					<c:if test="${pageData.pageNumber eq numPage}">
						<c:set var="pageClass" value="active"/>
						<c:set var="pageStatus" value="disabled"/>
					</c:if>
					<li class="page-item ${pageClass}">					
						<button type="submit" name="numPage" value="${numPage}" class="page-link" ${pageStatus}>
							${numPage + 1}
						</button>
					</li>
				</c:forEach>
				<c:if test="${pageData.totalPages > maxPageNumber}">
			 		<li class="page-item disabled}">...</li>
			 	</c:if>
				<li class="page-item ${nextStatus}">
					<button type="submit" name="numPage" value="${nextValue}" class="page-link" ${nextStatus}>
						Suivant
					</button>
				</li>				
			</ul>

		</c:if>