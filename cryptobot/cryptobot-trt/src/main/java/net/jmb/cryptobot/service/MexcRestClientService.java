package net.jmb.cryptobot.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.transaction.Transactional;
import net.jmb.cryptobot.CryptoBatchApp;
import net.jmb.cryptobot.data.entity.Cotation;

@Service
public class MexcRestClientService extends CommonService implements CommandLineRunner {
	
	public final static String BASE_URL = "https://api.mexc.com/api/v3";
	
	@Autowired
	RestTemplate mexcRestTemplate;
	
	@Autowired
	CotationService cotationService;
	
	
	private String symbol = null;
	
	
	@Override
	public void run(String... args) throws Exception {
		Map<String, String> params = CryptoBatchApp.getParameters(args);
		if (params != null && params.get("symbol") != null && "MEXC".equalsIgnoreCase(params.get("platform"))) {
			symbol = params.get("symbol");
			updateCotationsPrice();
		}
	}	
	

	@Scheduled(cron = "${cryptobot.kline.scheduler.cron}")
	@Transactional
	public void updateCotationsPrice() throws Exception {
		if (symbol != null && mexcRestTemplate != null) {
			
			List<Cotation> newCotations = new ArrayList<Cotation>();
			
			Map<String, String> uriParams = new HashMap<String, String>();
			uriParams.put("symbol", symbol);
			uriParams.put("interval", "5m");
			
			String urlTemplate = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/klines")
			        .queryParam("symbol", symbol)
			        .queryParam("interval", "5m")
			        .encode()
			        .toUriString();
			
			try {
				@SuppressWarnings("unchecked")
				ArrayList<List<Object>> resultList = mexcRestTemplate.getForObject(urlTemplate, ArrayList.class);
				if (resultList != null) {
					String symbol = this.symbol.replaceAll("USD.", "");
					
					for (List<Object> list : resultList) {
						Long resultTime = (Long) list.get(0);				
						Date datetime = new Date(Long.valueOf(resultTime));
						String resultPrice = (String) list.get(1);
						Double price = Double.valueOf(resultPrice);
						Cotation cotation = new Cotation().symbol(symbol).datetime(datetime).price(price);
						newCotations.add(cotation);					
					}
					
					newCotations = cotationService.registerNewCotations(symbol, newCotations);
					System.out.println(newCotations);
				}
			} catch (Exception e) {
				getLogger().error(e.getMessage(), e);
			}
		}
	}
	
	

	

}
