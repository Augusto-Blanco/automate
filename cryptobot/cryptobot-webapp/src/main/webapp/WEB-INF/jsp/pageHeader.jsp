<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>

<html lang="fr">
<head>
	<title>Lobot</title>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta http-equiv="Content-Security-Policy" content="img-src 'self' data: https:">	
	<script src="${pageContext.request.contextPath}/jquery/jquery-3.7.1.min.js"></script>
	<script src="${pageContext.request.contextPath}/jquery/jquery-ui-1.13.2/jquery-ui.min.js"></script>
	<link href="${pageContext.request.contextPath}/bootstrap/bootstrap-5.3.3-dist/css/bootstrap.min.css" rel="stylesheet">
	<script	src="${pageContext.request.contextPath}/bootstrap/bootstrap-5.3.3-dist/js/bootstrap.bundle.min.js"></script>	
	<link href="${pageContext.request.contextPath}/css/custom-datatables.css" rel="stylesheet"> 
	<script src="${pageContext.request.contextPath}/datatables/datatables.min.js"></script>	
	<link href="${pageContext.request.contextPath}/bootstrap-icons-1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
	<link href="${pageContext.request.contextPath}/css/lobot.css?v=3" rel="stylesheet">
	<script	src="${pageContext.request.contextPath}/scripts/common.js?v=1" ></script>
	<script	src="${pageContext.request.contextPath}/scripts/common-datatables.js?v=1" ></script>	
	<script type="text/javascript">
		var contextPath = "${pageContext.request.contextPath}";
	</script>
</head>

<body class="container-xl">

	<div class="sticky-top bg-body pb-2">
		<div class="header-page content">
			<div class="col"></div>
			<div class="col-auto text-center"><h1>CTF - Lobot : Suivi des intégrations de flux AI</h1></div>
			<div class="col">	
			</div>	
		</div>		
		<jsp:include page="navbar.jsp" />	
	</div>

