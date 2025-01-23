<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<!DOCTYPE html>
<jsp:include page="../pageHeader.jsp" />

<!-- viewDetailContrat.jsp -->

<script
	src="${pageContext.request.contextPath}/scripts/common-suivi-contrats.js?v=1"></script>
<script type="text/javascript">document.title='Lobot - Contrats'</script>

<div class="header-1">Gestion d'un contrat collectif</div>

<form:form
	action="${pageContext.request.contextPath}/contrat/view/detail"
	modelAttribute="contratQO">
	<fieldset>
		<div class="content mb-4 bottom">
			<div class="col-4 mx-1">
				<label class="form-label mx-0" for="numContrat">Veuillez entrer le n° du contrat collectif à afficher</label>
				<form:input type="text" class="form-control" list="listeNumContrats"
					placeholder="Saisir le n° de contrat" path="numContrat"
					required="true" />
				<datalist id="listeNumContrats">
					<c:forEach items="${allNumContrats}" var="numContrat">
						<option value="${numContrat}">
					</c:forEach>
				</datalist>
			</div>
			<div class="col mx-1">
				<input type="submit" class="btn-primary mx-1" value="Rechercher"
					onclick="rechercherDetailsContrat(event)" />
			</div>
		</div>
	</fieldset>
</form:form>

<div id="containerDetails" class="card py-3 my-3 invisible">
	<div class="row">
		<div class="col">
			<div class="row my-1">
				<div class="col-3">
					<label>N° Contrat : </label>
				</div>
				<div class="col">
					<span id="numCon"></span>
				</div>				
			</div>
			<div class="row my-1">
				<div class="col-3">
					<label>Flux du : </label>
				</div>
				<div class="col">
					<span class="bg-normal" id="dateFlux"></span>
				</div>				
			</div>
			<div class="row my-1">
				<div class="col-3">
					<label>Vacation : </label>
				</div>
				<div class="col">
					<span class="bg-normal" id="dateVacation"></span>
				</div>
			</div>
		</div>
		<div class="col">
			<div class="row my-1">
				<div class="col-3">
					<label>Code offre : </label>
				</div>
				<div class="col">
					<span id="offre"></span>
				</div>				
			</div>
			<div class="row my-1">
				<div class="col-3">
					<label>SIRET : </label>
				</div>
				<div class="col">
					<span id="siret"></span>
				</div>	
			</div>
			<div class="row my-1">	
				<div class="col">
					<label>Anomalie niv 1 : </label>
				</div>
				<div class="col">
					<span id="anoNiv1"></span>
				</div>
			</div>			
		</div>
	</div>
	<div>
		<jsp:include page="detailContrat.jsp" />
	</div>
</div>


<script type="text/javascript">	
		function rechercherDetailsContrat(event) {
			event.preventDefault()
			let numContrat = $("input[name='numContrat']").val()
			$("#containerDetails").removeClass("invisible")
			showDetailsForNumContrat($("input[name='numContrat']").val())
		}
	</script>

<!-- fin viewDetailContrat.jsp -->

<jsp:include page="../pageFooter.jsp" />
