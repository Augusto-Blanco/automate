<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<jsp:include page="pageHeader.jsp" />

<!-- index.jsp -->
			
	<div class="my-5">
		<h4>Bienvenue</h4>
		<div class="pt-4 pb-3">
			Pour accéder aux fonctionnalités de l'application, veuillez utiliser les liens ci-dessous ou la barre de navigation ci-dessus
		</div>
		<ul>
			<li class="py-2">
				<a href="${pageContext.request.contextPath}/fluxRecu/view/liste">
					Suivi des flux reçus
				</a>
			</li>
			<li class="py-2">
				<a href="${pageContext.request.contextPath}/contrat/view/liste">
					Suivi des intégrations de contrats collectifs
				</a>
			</li>
			<li class="py-2">
				<a href="${pageContext.request.contextPath}/contrat/view/detail">
					Gestion d'un contrat collectif
				</a>
			</li>
		</ul>
	</div>	

	
	<script type="text/javascript">document.title='Lobot - Accueil'</script>
		
<!-- fin index.jsp -->

<jsp:include page="pageFooter.jsp" />
