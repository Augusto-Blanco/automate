<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>

	<c:set var="isAno" value="${fluxRecu.nbContratsKO > 0}"></c:set>
	<c:set var="classContratsKO" value=""></c:set>
	<c:if test="${isAno}">
		<c:set var="classContratsKO" value="msg-warning"></c:set>
	</c:if>

		<div class="card bg py-3 my-3" id="fluxRecu-${fluxRecu.id}">
			<div class="row v-center">
				<div class="col-4">
					<div class="py-1">
						<label>Flux du: </label><span class="bg-normal"><fmt:formatDate value="${fluxRecu.dateFlux}" pattern="dd/MM/yyyy à HH:mm"/></span>
					</div>
					<div class="py-1">
						<label><i class="bi-arrow-return-right"></i></label><span>${fluxRecu.fluxIn}</span>
					</div>
				</div>
				<div class="col">
					<div class="py-1">
						<label> Vacation: </label>						
						<span class="bg-normal">
						<c:choose>
							<c:when test="${fluxRecu.dateVacation ne null}">
								<fmt:formatDate value="${fluxRecu.dateVacation}" pattern="dd/MM/yyyy à HH:mm"/>
							</c:when>
							<c:otherwise>
								non exécutée
							</c:otherwise>						
						</c:choose>						
						</span>
					</div>
				</div>
				<div class="col">
					<div class="py-1">
						<label>${fluxRecu.nbContrats} contrats reçus: </label>
						<span class="${classContratsKO}">${fluxRecu.nbContratsKO} en anomalie</span>
					</div>
<!-- 					<div class="py-1"> -->
<%-- 						<label>Archive </label> <span>${fluxRecu.repertoire}</span> --%>
<!-- 					</div>					 -->
				</div>
				<div class="col-3 accordion row">
					<span class="col">
						<c:if test="${isAno}">
						<a class="col btn-primary accordion-button collapsed" data-bs-toggle="collapse" href="#contratsKO-${fluxRecu.id}" 
								onclick="afficherContratsKO(${fluxRecu.id})">
							Contrats KO
						</a>
						</c:if>
					</span>
					<span class="col">
						<a class="col btn accordion-button collapsed" data-bs-toggle="collapse" href="#contrats-${fluxRecu.id}" 
								onclick="afficherContrats(${fluxRecu.id})">
							Contrats
						</a>
					</span>
				</div>
			</div>
			
			<div id="contrats-${fluxRecu.id}" class="collapse" data-bs-parent="#fluxRecu-${fluxRecu.id}">
				<jsp:include page="listeContrats.jsp">
					<jsp:param value="true" name="isFromFluxRecu"/>
				</jsp:include>
			</div>
			<div id="contratsKO-${fluxRecu.id}" class="collapse" data-bs-parent="#fluxRecu-${fluxRecu.id}">
				<jsp:include page="listeContrats.jsp">
					<jsp:param value="true" name="isFromFluxRecu"/>
				</jsp:include>
			</div>			
		
		</div>
		
		

