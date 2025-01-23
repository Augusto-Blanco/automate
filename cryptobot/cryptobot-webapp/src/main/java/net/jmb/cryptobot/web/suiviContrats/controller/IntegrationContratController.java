package net.jmb.cryptobot.web.suiviContrats.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import net.jmb.cryptobot.data.bean.OrderQO;
import net.jmb.cryptobot.data.bean.PageData;
import net.jmb.cryptobot.data.entity.Cotation;
import net.jmb.cryptobot.data.entity.Trade;
import net.jmb.cryptobot.suiviContrats.service.SuiviContratsService;



@Controller
@RequestMapping("/contrat")
public class IntegrationContratController {
	
	@Autowired
	SuiviContratsService suiviContratsService;

	
	@GetMapping("/liste/{idFlux}")
	@ResponseBody
	public ResponseEntity<Object> listeContrats(@PathVariable Integer idFlux) {		
		List<Trade> contrats = suiviContratsService.getContratsFluxRecu(idFlux.longValue());
		return new ResponseEntity<Object>(contrats, HttpStatus.OK);
	}	
	
	@GetMapping(path = "/liste")
	@ResponseBody
	public ResponseEntity<Object> listeContrats(OrderQO contratQO) {		
		return listeContrats(contratQO, null);
	}	
	
	@GetMapping(path = "/liste", params = "start")
	@ResponseBody
	public ResponseEntity<Object> listeContrats(OrderQO contratQO, PageRequest pageRequest) {		
		PageData<Trade> contrats = null;
		if (contratQO == null || contratQO.isEmpty()) {
			contrats = suiviContratsService.getContrats(pageRequest);
		} else {
			contrats = suiviContratsService.getContrats(contratQO, pageRequest);			
		}
		return new ResponseEntity<Object>(contrats, HttpStatus.OK);
	}		
	
	@RequestMapping(path="/view/liste", method={RequestMethod.GET, RequestMethod.POST})
	public ModelAndView viewListeContrats() {
		return new ModelAndView("suiviContrats/viewListeContrats", "contratQO", new OrderQO());
	}
	
	
	@GetMapping("/details/{idContrat}")
	@ResponseBody
	public ResponseEntity<Object> listeDetailsContrat(@PathVariable Long idContrat) {		
		List<Cotation> detailsContrat = suiviContratsService.getDetailsContrat(idContrat);
		return new ResponseEntity<Object>(detailsContrat, HttpStatus.OK);
	}
	
	@GetMapping(path="/details/", params="numContrat")
	@ResponseBody
	public ResponseEntity<Object> listeDetailsContrat(OrderQO contratQO) {		
		List<Cotation> detailsContrat = suiviContratsService.getLastDetailsContrat(contratQO);
		return new ResponseEntity<Object>(detailsContrat, HttpStatus.OK);
	}

}