package net.jmb.batch.cryptobot;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.PlatformTransactionManager;

import net.jmb.cryptobot.service.CryptobotService;

@Configuration
public class SpringBatchConfig {
	
    @Bean
    @DependsOn(value = {"jobRepository", "transactionManager"})    
    Job evaluateCotationJob(JobRepository jobRepository, PlatformTransactionManager transactionManager, CryptobotService service) {
    	JobBuilder jobBuilder = new JobBuilder("evaluateCotationJob", jobRepository);
        SimpleJobBuilder simpleJobBuilder = null;
			Step step = step(jobRepository, transactionManager, service);
			simpleJobBuilder = jobBuilder.start(step);

        if (simpleJobBuilder != null) {
        	return simpleJobBuilder.build();
        }
        return null;
    }
    
    
    protected Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager, CryptobotService service) {        
    	return new StepBuilder("BATCH", jobRepository)
    		.tasklet(tasklet(service), transactionManager)
    		.build();
    }
    
	
	protected MethodInvokingTaskletAdapter tasklet(CryptobotService service) {
		MethodInvokingTaskletAdapter adapter = new MethodInvokingTaskletAdapter();
		adapter.setTargetObject(service);
		adapter.setTargetMethod("process");
		return adapter;
	}

    
    
//    @Bean
//    public JobRepository jobRepository(PlatformTransactionManager transactionManager, DataSource dataSource) throws Exception {
//        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
//        factory.setDataSource(dataSource);
//        factory.setTransactionManager(transactionManager);
//        factory.afterPropertiesSet();
//        return factory.getObject();
//    }
//    
//    @Bean
//    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
//       TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
//       jobLauncher.setJobRepository(jobRepository);
//       jobLauncher.afterPropertiesSet();
//       return jobLauncher;
//    }
	

}
