package net.jmb.cryptobot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import net.jmb.cryptobot.data.enums.Period;
import net.jmb.cryptobot.service.CotationService;

@SpringBootApplication
public class CryptoBatchApp implements CommandLineRunner {
	
	static Logger logger = LoggerFactory.getLogger(CryptoBatchApp.class);
	
	@Autowired
	private CotationService cotationService;
	
	
	
	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext ctx = SpringApplication.run(CryptoBatchApp.class, args);
       	ctx.close();			
	}


	public void run(String... args) throws Exception {
        logger.info("Démarrage du job: " + new Date());
        System.out.println("Démarrage du job: " + new Date());
        
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
        
        String date = "2025-01-12 01";
        
        try {
//        	cotationService.computeCotations("CYBRO", Period._24h);	        	
//        	initEvaluationForLastCotations("CYBRO", Period._6j, 2, Period._12h, Period._1h);
        	
        	cotationService.evaluateTradesForCotations("CYBRO", df.parse(date), true, 2d, Period._12h, Period._1h);
        	
            logger.info("Fin du job: " + new Date());
            System.out.println("Fin du job: " + new Date());
            
        } catch (Exception e) {
            logger.error("Job en anomalie", e);            
        }  
	}

	

	
	


}
