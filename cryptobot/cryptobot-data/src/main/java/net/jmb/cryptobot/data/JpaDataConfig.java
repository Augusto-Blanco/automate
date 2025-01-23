package net.jmb.cryptobot.data;

import java.lang.reflect.InvocationTargetException;
import java.sql.Driver;
import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@PropertySource("classpath:/application.properties")
@PropertySource(value = "classpath:/jpa.properties", name = "jpaProps")
@EnableTransactionManagement
@EnableJpaRepositories
@EnableAutoConfiguration
public class JpaDataConfig {
	
	private static String PACKAGE_TO_SCAN = JpaDataConfig.class.getPackage().getName();
	
	@Autowired
	private ConfigurableEnvironment env;

	
	@Bean
	DataSource dataSource() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		DataSource dataSource;
		try {
			dataSource = jndiDataSource();
		} catch (NamingException e) {
			dataSource = null;
		}
		if (dataSource == null) {
			String dbUrl = env.getProperty("spring.datasource.url");
			String dbUser = env.getProperty("spring.datasource.username");
			String dbPassword = env.getProperty("spring.datasource.password");
			String driverClassName = env.getProperty("spring.datasource.driver");
			if (driverClassName != null) {
				Driver driver = (Driver) Class.forName(driverClassName).getDeclaredConstructor().newInstance();
				dataSource = new SimpleDriverDataSource(driver, dbUrl, dbUser, dbPassword);
			} else {
				dataSource = new DriverManagerDataSource(dbUrl, dbUser, dbPassword);
			}
		}
		return dataSource;
	}

	DataSource jndiDataSource() throws NamingException {
		DataSource dataSource = new JndiTemplate().lookup("java:comp/env/jdbc/datasource", DataSource.class);		
		return dataSource;
	}
	
	@Bean
	LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) throws Exception {
		LocalContainerEntityManagerFactoryBean emfb = new LocalContainerEntityManagerFactoryBean();
		emfb.setDataSource(dataSource);
		emfb.setPackagesToScan(PACKAGE_TO_SCAN);
		Properties jpaProperties = (Properties) env.getPropertySources().get("jpaProps").getSource();
		emfb.setJpaProperties(jpaProperties);
		emfb.setJpaVendorAdapter(jpaVendorAdapter(jpaProperties.getProperty("spring.jpa.jpaVendorAdapter")));
		emfb.setPersistenceUnitName("cryptobot_db");
		return emfb;		
	}	

	private JpaVendorAdapter jpaVendorAdapter(String jpaVendorAdapter) throws Exception {
		JpaVendorAdapter vendorAdapter = Class.forName(jpaVendorAdapter)
				.asSubclass(JpaVendorAdapter.class)
				.getDeclaredConstructor()
				.newInstance();
		return vendorAdapter;
	}


}
