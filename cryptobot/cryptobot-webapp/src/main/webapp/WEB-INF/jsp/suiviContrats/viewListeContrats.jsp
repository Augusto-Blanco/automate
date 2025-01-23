<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<jsp:include page="../pageHeader.jsp" />

<!-- viewListeContrats.jsp -->
	
	<script	src="${pageContext.request.contextPath}/scripts/contrat.js?v=2"></script>
	<script	src="${pageContext.request.contextPath}/scripts/common-suivi-contrats.js?v=2"></script>
	<script type="text/javascript">document.title='Lobot - Contrats'</script>

	<div class="header-1">
		Sélection des contrats collectifs
	</div>

	<form:form action="${pageContext.request.contextPath}/contrat/view/liste" modelAttribute="contratQO">
		<fieldset>
			<legend>Critères</legend>
			
			<div class="content mb-4">
				<div class="adjust mx-2">
			        <label class="form-label mx-0" for="dateDebut">Date du flux</label>
			        <div class="input-group">
			    		<span class="input-group-text">du</span>
			            <form:input type="date" class="form-control" placeholder="(jj/mm/aaaa) *" path="dateDebut" required="true" />
			            <span class="input-group-text">au</span>
			            <form:input type="date" class="form-control" placeholder="(jj/mm/aaaa)" path="dateFin" />
			        </div>
			    </div>
			    <div class="col mx-2">
				    <label for="numContrat" class="form-label mx-0">N° contrat</label> 
				    <input id="numContrat" name="numContrat" class="form-control mx-0" type="text">
				</div>
			    <div class="col mx-2">
			    	<label for="notTraiteParVacation" class="form-label mx-0">Attente de traitement</label> 
			        <div class="form-check form-switch">
			            <form:checkbox class="form-check-input" id="notTraiteParVacation" path="notTraiteParVacation" />
			        </div>
			    </div>
				<div class="col mx-2">
			    	<label for="avecAno" class="form-label mx-0">Avec anomalie(s)</label> 
			        <div class="form-check form-switch">
			            <form:checkbox class="form-check-input" id="avecAno" path="avecAno" />
			        </div>
			    </div>
			    <div class="col mx-2">
			    	<label for="anoNiv1" class="form-label mx-0">Ano de niveau 1</label> 
			        <div class="form-check form-switch">
			            <form:checkbox class="form-check-input" id="anoNiv1" path="anoNiv1" />
			        </div>
			    </div>
			    <div class="col mx-2">
					<label for="batchEnAno" class="form-label mx-0">Batch en anomalie</label>
			        <form:select class="form-select mx-0" path="batchEnAno" id="batchEnAno">
			            <form:option value="">---------------------------------</form:option>
			            <form:options items="${listeBatchs}" itemLabel="libelleIHM" itemValue="code" />			          
			        </form:select>
				</div>
			</div>
			
			<input type="submit" class="btn-primary mx-1" value="Rechercher" onclick="populateTableContrats(event)"/>
			<input type="reset" class="btn-reset mx-1" value="Réinitialiser" />
		</fieldset>			
	</form:form>

	<div class="card my-3">	
		<jsp:include page="listeContrats.jsp" />
	</div>
	
	<jsp:include page="modalContrat.jsp" />
	
	<!-- 
		 Boutons d'actions insérés par script dans la dernière colonne de chaque contrat listé : 
		 Ils ne sont visibles que lorsque le curseur est positionné sur la ligne du contrat
	-->	
	<div id="actions">
		<div class="invisible flex-nowrap">
			<button name="visualiser" type="button" onclick='visualiser(this)'>
				Détail
			</button>
		</div>
	</div>
	
<!-- fin viewListeContrats.jsp -->	

<jsp:include page="../pageFooter.jsp" />
