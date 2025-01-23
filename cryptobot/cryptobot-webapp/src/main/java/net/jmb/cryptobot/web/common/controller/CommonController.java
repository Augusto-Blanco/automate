package net.jmb.cryptobot.web.common.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class CommonController {
	

	
	@InitBinder
	public void init(WebDataBinder dataBinder, WebRequest request) {
		
	}
	
	@ModelAttribute
	public void paginationModelAttribute(Model model, WebRequest request) {
		
		// Le tri 
		Sort sort = null;
		String sortColName = request.getParameter("sortColName");
		if (StringUtils.isBlank(sortColName)) {
			sortColName = request.getParameter("order[0][name]");
			String sortNumCol = request.getParameter("order[0][column]");
			if (StringUtils.isBlank(sortColName) && StringUtils.isNotBlank(sortNumCol)) {
				sortColName = request.getParameter("columns[" + sortNumCol + "][data]");			
			}			
		}
		if (StringUtils.isNotBlank(sortColName)) {
			String sortDirection = request.getParameter("sortDirection");
			if (StringUtils.isBlank(sortDirection)) {
				sortDirection = request.getParameter("order[0][dir]");
			}
			Direction dir = "desc".equalsIgnoreCase(sortDirection) ? Direction.DESC : Direction.ASC;
			sort = Sort.by(dir, sortColName);
		}
		
		// La pagination
		String strStart = request.getParameter("start");
		String strLength = request.getParameter("length");
		String strNumPage = request.getParameter("numPage");		
		
		if (StringUtils.isNotBlank(strStart) || StringUtils.isNotBlank(strLength) || StringUtils.isNotBlank(strNumPage)) {
			int start = 0;
			int numPage = 0;
			int pageSize = 50;
			
			if (StringUtils.isNotBlank(strStart)) {
				start = Integer.valueOf(strStart);
			}
			if (StringUtils.isNotBlank(strLength)) {
				pageSize = Integer.valueOf(strLength);
			}
			if (StringUtils.isNotBlank(strNumPage)) {
				numPage = Integer.valueOf(strNumPage);
			} else if (pageSize > 0) {
				numPage = start / pageSize;
			}
			if (sort != null) {
				model.addAttribute(PageRequest.of(numPage, pageSize, sort));
			} else {
				model.addAttribute(PageRequest.of(numPage, pageSize));
			}
		} else {
			model.addAttribute("pageRequest", (PageRequest) null);
		}
		
	}
	
	
	@ModelAttribute
	public void navbarModelAttribute(Model model, HttpServletRequest request) {
		String path = request.getServletPath();
		Map<String, String> navMap = new HashMap<>();
		navMap.put("accueil", "");
		navMap.put("fluxRecu", "");
		navMap.put("contrat", "");
		navMap.put("gestion", "");
		
		if (path != null) {
			if (path.startsWith("/fluxRecu/view")) {
				navMap.put("fluxRecu", "active");
			} else if (path.startsWith("/contrat/view/liste")) {
				navMap.put("contrat", "active");
			} else if (path.startsWith("/contrat/view/detail")) {
				navMap.put("gestion", "active");
			} else if (path.equals("/")) {
				navMap.put("accueil", "active");
			}
		}
		model.addAttribute("navMap", navMap);
	
	}
	
	
	@ExceptionHandler
	public void commmonExceptionHandler(Exception e) {
		
	}

}
