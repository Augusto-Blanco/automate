<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
	
	<!-- Table recueillant la liste des contrats sélectionnés -->
	<table class="table bg contrats">
		<thead>
			<tr>
				<c:if test="${not param.isFromFluxRecu}">
					<th>Date flux</th>
					<th>Date vacation</th>	
				</c:if>
				<th>N° contrat</th>
				<th>Code offre collective</th>
				<th>N° SIRET</th>
				<th>Anomalie niveau 1</th>
				<th>Traitement(s) KO</th>
				<th>Etat</th>
			</tr>
		</thead>
		<tbody>
		</tbody>
	</table>
		

