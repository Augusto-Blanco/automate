package net.jmb.batch.cryptobot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.jmb.cryptobot.service.CryptobotService;


@Component
@Order(10)
public class CryptoBotJobRunner implements CommandLineRunner {
	
	static Logger logger = LoggerFactory.getLogger(CryptoBotJobRunner.class);
	
	@Autowired
	private JobLauncher jobLauncher;
	@Autowired
	private Job evaluateCotationJob;	
	@Autowired
	private CryptobotService cryptobotService;
	

	void afterJob(JobExecution jobExecution) {
		cryptobotService.getCryptobotRepository();
    }			


	@Scheduled(cron = "${lobot.cryptobot.batch.scheduler.cron}")
	public void run(String... args) throws Exception {
        logger.info("Démarrage du travail : " + evaluateCotationJob.getName());        
        try {
        	JobParameters jobParameters = new JobParametersBuilder()
        			.addLong("time", System.currentTimeMillis())
        			.toJobParameters();
            JobExecution execution = jobLauncher.run(evaluateCotationJob, jobParameters);
            logger.info("Status : {}", execution.getStatus());
            logger.info("Fin du job");
            
            afterJob(execution);
            
        } catch (Exception e) {
            logger.error("Job en anomalie", e);            
        }  
	}

	
}
