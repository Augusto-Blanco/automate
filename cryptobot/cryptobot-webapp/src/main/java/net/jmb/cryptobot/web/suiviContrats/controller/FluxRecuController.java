package net.jmb.cryptobot.web.suiviContrats.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import net.jmb.cryptobot.data.bean.AssetQO;
import net.jmb.cryptobot.data.bean.PageData;
import net.jmb.cryptobot.data.entity.Asset;
import net.jmb.cryptobot.suiviContrats.service.SuiviContratsService;


@Controller
@RequestMapping("/fluxRecu")
public class FluxRecuController {
	
	@Autowired
	SuiviContratsService suiviContratsService;
	
	
	@RequestMapping("/view/liste")
	public ModelAndView listeFluxRecus(AssetQO fluxRecuQO, PageRequest pageRequest) {
		Map<String, Object> model = new HashMap<>();
		if (fluxRecuQO == null || fluxRecuQO.isEmpty()) {
			model.put("msgComplement", "(derniers flux reçus)");
			fluxRecuQO = new AssetQO();
		}
		PageData<Asset> fluxRecus = suiviContratsService.getFluxRecus(fluxRecuQO, pageRequest);
		model.put("pageFluxRecus", fluxRecus);
		model.put("fluxRecuQO", fluxRecuQO);
		
		return new ModelAndView("suiviContrats/viewListeFluxRecus", model);
		
	}

}