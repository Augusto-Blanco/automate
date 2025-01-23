<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<jsp:include page="../pageHeader.jsp" />

<!-- viewListeFluxRecus.jsp -->
	
	<script type="text/javascript">document.title='Lobot - flux reçus'</script>
	<script	src="${pageContext.request.contextPath}/scripts/fluxRecu.js?v=2"></script>
	<script	src="${pageContext.request.contextPath}/scripts/common-suivi-contrats.js?v=2"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#containerFluxRecus').css('max-height', screen.height/2)
		})
	</script>

	<div class="header-1">
		Sélection des flux reçus
	</div>

	<form:form action="${pageContext.request.contextPath}/fluxRecu/view/liste" modelAttribute="fluxRecuQO">
	
		<fieldset>
			<legend>Critères</legend>
			
			<div class="content mb-4">
				<div class="col-3">
					<label for="typeFluxRecu" class="form-label mx-0">Type de flux</label>
			        <select class="form-select" path="typeFluxRecu" id="typeFluxRecu" required="true">
			            <option value="1" selected="selected">Intégration des contrats collectifs</option>
			        </select>
				</div>
				<div class="col-4">
			        <label class="form-label mx-0" for="dateDebut">Date</label>
			        <div class="input-group">
			    		<span class="input-group-text">du</span>
			            <form:input type="date" class="form-control" placeholder="(jj/mm/aaaa) *" path="dateDebut"/>
			            <span class="input-group-text">au</span>
			            <form:input type="date" class="form-control" placeholder="(jj/mm/aaaa)" path="dateFin"/>
			        </div>
			    </div>
				<div class="col-2">
			    	<label for="avecAno" class="form-label mx-0">Avec anomalie(s)</label> 
			        <div class="form-check form-switch">
			            <form:checkbox class="form-check-input" id="avecAno" path="avecAno" />
			        </div>
			    </div>
			    <div class="col-2">
			    	<label for="notTraiteParVacation" class="form-label mx-0">Attente de traitement</label> 
			        <div class="form-check form-switch">
			            <form:checkbox class="form-check-input" id="notTraiteParVacation" path="notTraiteParVacation" />
			        </div>
			    </div>
			</div>
			
			<button type="submit" class="btn-primary mx-1">Rechercher</button>
			<button type="reset" class="btn-reset mx-1">Réinitialiser</button>
		</fieldset>	
	
		<div class="card my-3">	
			<div class="header-2 my-0">
				Liste des flux &nbsp; ${msgComplement}
			</div>		
			<c:if test="${pageFluxRecus ne null}">
				<!-- 	liste des flux reçus	-->	
				<div class="mx-3 overflow-auto" id="containerFluxRecus">					
					<c:forEach items="${pageFluxRecus.data}" var="fluxRecu">
						<c:set var="fluxRecu" value="${fluxRecu}" scope="request" />
						<jsp:include page="fluxRecu.jsp" />
					</c:forEach>
				</div>
				<!-- 	pagination	-->	
				<div class="m-3">					
					<c:set var="pageData" value="${pageFluxRecus}" scope="request"/>
					<jsp:include page="../pagination.jsp"/>
				</div>
			</c:if>
		</div>
		
	</form:form>
	
	<jsp:include page="modalContrat.jsp" />
	
	<!-- 
		 Boutons d'actions insérés par script dans la dernière colonne de chaque contrat listé : 
		 Ils ne sont visibles que lorsque le curseur est positionné sur la ligne du contrat
	-->	
	<div id="actions">
		<div class="invisible flex-nowrap">
			<button name="visualiser" type="button">
				Détail
			</button>
		</div>
	</div>
		
<!-- fin viewListeFluxRecus.jsp -->

<jsp:include page="../pageFooter.jsp" />
