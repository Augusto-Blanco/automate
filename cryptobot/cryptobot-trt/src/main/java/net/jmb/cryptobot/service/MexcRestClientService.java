package net.jmb.cryptobot.service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.transaction.Transactional;
import net.jmb.cryptobot.beans.MexcAccountInfo;
import net.jmb.cryptobot.beans.MexcOrder;
import net.jmb.cryptobot.data.entity.Cotation;
import net.jmb.cryptobot.data.enums.OrderSide;

@Service
public class MexcRestClientService extends CommonService {
	
	public final static String BASE_URL = "https://api.mexc.com/api/v3";
	public final static String API_KEY_HEADER = "X-MEXC-APIKEY";
	
	@Autowired
	RestTemplate mexcRestTemplate;
	@Autowired
	CotationService cotationService;
	@Autowired
	String apiKey;
	@Autowired
	String secretKey;
	

	
	@SuppressWarnings({ "unchecked", "rawtypes" })	
	@Transactional	
	public List<Cotation> updateCotationsPrice(String symbol) throws Exception {		
		
		if (symbol != null && mexcRestTemplate != null) {
			
			String url = BASE_URL + "/klines";
			HttpMethod httpMethod = HttpMethod.GET;
			LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
			
			queryParams.add("symbol", symbol.concat("USDT"));
			queryParams.add("interval", "5m");
			
			List<Cotation> newCotations = new ArrayList<Cotation>();
			
			try {
				RequestEntity<?> request = buildRequest(url, httpMethod, queryParams);
				ResponseEntity<ArrayList> result = mexcRestTemplate.exchange(request, ArrayList.class);
				ArrayList<List<Object>> resultList = (ArrayList<List<Object>>) result.getBody();
				
				if (resultList != null) {
					symbol = symbol.replaceAll("USD.", "");
					
					for (List<Object> list : resultList) {
						Long resultTime = (Long) list.get(0);				
						Date datetime = new Date(Long.valueOf(resultTime));
						String resultPrice = (String) list.get(1);
						Double price = Double.valueOf(resultPrice);
						Cotation cotation = new Cotation().symbol(symbol).datetime(datetime).price(price);
						newCotations.add(cotation);					
					}					
					newCotations = cotationService.registerNewCotations(symbol, newCotations);
				}
				
				return newCotations;
				
			} catch (Exception e) {
				getLogger().error(e.getMessage(), e);
			}
		}
		
		return null;
	}
	
	
	@SuppressWarnings({"rawtypes", "unchecked", "serial"})
	public Double getLastPrice(String symbol) {		

		if (mexcRestTemplate != null && symbol != null) {
			
			String url = BASE_URL + "/ticker/price";
			HttpMethod httpMethod = HttpMethod.GET;
			LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>() {
				{
					add("symbol", symbol.contains("USD") ? symbol : symbol.concat("USDT"));				
				}
			};
			
			try {
				RequestEntity<?> request = buildRequest(url, httpMethod, queryParams);
				ResponseEntity<? extends Map> response = mexcRestTemplate.exchange(request, Map.class);
				Map<String, Object> accountInfo = response.getBody();
				Object result = accountInfo.get("price");
				if (result != null) {
					return Double.valueOf(result.toString());
				}
				
			} catch (Exception e) {
				getLogger().error(e.getMessage(), e);
			}
		}
		return null;
		
	}
	
	public Double getFreeQuantity(String symbol) {
		
		if (symbol != null) {			
			if (!symbol.startsWith("USD")) {
				symbol = symbol.replaceAll("USD.", "");
			}

			MexcAccountInfo accountInfos = getAccountInfos();
			return accountInfos.getFreeAssetQuantity(symbol);
		}
		
		return null;		
	}
	
	
	private MexcAccountInfo getAccountInfos() {
		String url = BASE_URL + "/account";
		HttpMethod httpMethod = HttpMethod.GET;
		
		if (mexcRestTemplate != null) {
			
			try {
				RequestEntity<?> request = buildRequest(url, httpMethod, null);
				ResponseEntity<MexcAccountInfo> result = mexcRestTemplate.exchange(request, MexcAccountInfo.class);
				MexcAccountInfo accountInfo = result.getBody();
				return accountInfo;
				
			} catch (Exception e) {
				getLogger().error(e.getMessage(), e);
			}
		}
		return null;		
	}
	
	@SuppressWarnings("serial")
	public MexcOrder sendOrder(String symbol, OrderSide orderSide, BigDecimal quantity, BigDecimal price) {

		if (mexcRestTemplate != null && symbol != null) {
			
			String url = BASE_URL + "/order";
			HttpMethod httpMethod = HttpMethod.POST;
			LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>() {
				{
					add("symbol", symbol.contains("USD") ? symbol : symbol.concat("USDT"));
					add("side", orderSide.name());
					add("type", "LIMIT");
					add("quantity", "" + quantity.toPlainString());
					add("price", "" + price.toPlainString());
				}
			};

			try {
				RequestEntity<?> request = buildRequest(url, httpMethod, queryParams);
				ResponseEntity<MexcOrder> result = mexcRestTemplate.exchange(request, MexcOrder.class);
				MexcOrder order = result.getBody();
				return order;

			} catch (Exception e) {
				getLogger().error(e.getMessage(), e);
			}
		}
		return null;
	}
	
	
	protected RequestEntity<?> buildRequest(String url, HttpMethod httpMethod, LinkedMultiValueMap<String, String> queryParams) throws URISyntaxException {
		
		RequestEntity<?> requestEntity = null;
		
		HttpHeaders headers = new HttpHeaders();
		//	application/x-www-form-urlencoded
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		if (apiKey != null) {
			headers.add(API_KEY_HEADER, apiKey);				
		}
		
		if (queryParams == null) {
			queryParams = new LinkedMultiValueMap<String, String>();
		}
		
		queryParams.add("timestamp", "" + (new Date().getTime() - 1000));
		queryParams.add("recvWindow", "15000");
		
		String queryString = UriComponentsBuilder.newInstance()
			.queryParams(queryParams)
	        .encode()
	        .toUriString()
	        .replace("?", "");
	
		if (secretKey != null) {
			String signature = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secretKey).hmacHex(queryString);
			queryParams.add("signature", signature);
			queryString += "&signature=" + signature;
		}
		
		String urlTemplate = UriComponentsBuilder.fromHttpUrl(url).query(queryString).encode().toUriString();
		requestEntity = RequestEntity.method(httpMethod, URI.create(urlTemplate)).headers(headers).body(queryParams);

		return requestEntity;
	}
	
	

	

}
