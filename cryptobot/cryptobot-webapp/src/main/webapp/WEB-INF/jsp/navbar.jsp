<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>


<nav class="nav nav-tabs mt-2" id="menu">
	<div class="nav-item">
		<a class="nav-link ${navMap.accueil}" id="accueil-tab" href="${pageContext.request.contextPath}/">
			Accueil
		</a>
	</div>
	<div class="nav-item">
		<a class="nav-link ${navMap.fluxRecu}" id="fluxRecu-tab" href="${pageContext.request.contextPath}/fluxRecu/view/liste">
			Suivi Flux Reçus
		</a>
	</div>
	<div class="nav-item">
		<a class="nav-link ${navMap.contrat}" id="contrat-tab" href="${pageContext.request.contextPath}/contrat/view/liste">
			Suivi Contrats
		</a>
	</div>
	<div class="nav-item">
		<a class="nav-link ${navMap.gestion}" id="contrat-tab" href="${pageContext.request.contextPath}/contrat/view/detail">
			Gestion Contrat
		</a>
	</div>
</nav>


