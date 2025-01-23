package net.jmb.cryptobot.web.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/")
public class MainController {
	
	
	@RequestMapping(path = "")
	public String index() {
		return "index";		
	}

}