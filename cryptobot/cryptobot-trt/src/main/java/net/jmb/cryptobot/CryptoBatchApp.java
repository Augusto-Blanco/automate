package net.jmb.cryptobot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.jmb.cryptobot.beans.MexcAccountInfo;
import net.jmb.cryptobot.data.entity.Asset;
import net.jmb.cryptobot.data.entity.AssetConfig;
import net.jmb.cryptobot.data.entity.Cotation;
import net.jmb.cryptobot.data.enums.Period;
import net.jmb.cryptobot.service.CotationService;

@SpringBootApplication
@EnableScheduling
public class CryptoBatchApp implements CommandLineRunner {
	
	static Logger logger = LoggerFactory.getLogger(CryptoBatchApp.class);
	
	@Autowired
	private CotationService cotationService;
	
	
	
	public static void main(String[] args) throws Exception {
		
		ConfigurableApplicationContext ctx = SpringApplication.run(CryptoBatchApp.class, args);
		if (getParameters(args).get("daemon") == null) {
			ctx.close();			
		}
	}


	public void run(String... args) throws Exception {
        
		logger.info("Démarrage du job: " + new Date());
        System.out.println("Démarrage du job: " + new Date());
        
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String platform = null, symbol = null;
        String date = "2025-01-24 18:50";

        
        Asset asset = null;
        
        try {
        	
	        Map<String, String> params = getParameters(args);
	        
	        if (params != null && params.get("symbol") != null) {
				symbol = params.get("symbol");
//				platform =  params.get("platform");
//				asset = cotationService.getCryptobotRepository().getAssetRepository().findBySymbolAndPlatformEquals(symbol, platform);
//				
//				cotationService.initEvaluationForCotations(asset, df.parse(date), false);
				
//		        cotationService.evaluateLastCotations(asset);
			}

      

//        	cotationService.computeCotations(symbol, Period._6j);	        	
        	
        	
//            if (asset != null) {
//    	        List<Cotation> dbCotations = cotationService.getCryptobotRepository().getCotationsSinceDate(symbol, df.parse(date));
//    	        List<AssetConfig> assetConfigList = cotationService.getCryptobotRepository().getAssetConfigRepository().findBySymbolEqualsAndEndTimeGreaterThanEqual(symbol, df.parse(date));
//            	cotationService.recordEvaluationsForCotations(dbCotations, asset, assetConfigList);
//            }
        	
            logger.info("Fin du job: " + new Date());
            System.out.println("Fin du job: " + new Date());
            
        } catch (Exception e) {
            logger.error("Job en anomalie", e);     
            System.out.println("Job en anomalie: " + new Date());
        }  
	}
	
	public static Map<String, String> getParameters(String...args) {
		
		Map<String, String> parameters = new ConcurrentSkipListMap<>();	
		if (args != null && args.length > 0) {

			if (args != null && args.length > 0) {
				Arrays.stream(args).forEach(arg -> {
					String[] keyValue = arg.split("=");
					String key = keyValue[0], value = "";
					if (keyValue.length > 1) {
						value = keyValue[1];
					}
					parameters.put(key, value);
				});
			}
		}
		return parameters;
	}

	

	
	


}
