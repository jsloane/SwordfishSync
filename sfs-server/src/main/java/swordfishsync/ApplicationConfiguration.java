package swordfishsync;

import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.Resource;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Use Spring to configure the application. This configures the DataSource, JPA entities, beans,
 * etc.
 */
@Configuration
@EnableScheduling
@EnableAsync
@EnableSpringDataWebSupport
@EnableTransactionManagement
@ComponentScan("swordfishsync")
@PropertySource("classpath:application.properties") // TODO default-application.properties
@EnableJpaRepositories("swordfishsync.repository")
public class ApplicationConfiguration implements SchedulingConfigurer {

	private static final String PROPERTY_NAME_DATABASE_DRIVER = "database.driver";
	private static final String PROPERTY_NAME_DATABASE_URL = "database.url";
	private static final String PROPERTY_NAME_DATABASE_USERNAME = "database.username";
	private static final String PROPERTY_NAME_DATABASE_PASSWORD = "database.password";
	private static final String PROPERTY_NAME_HIBERNATE_DIALECT = "database.hibernate.dialect";
	private static final String PROPERTY_NAME_HIBERNATE_HBM2DDL_AUTO = "database.hibernate.hbm2ddl.auto";
	private static final String PROPERTY_NAME_HIBERNATE_SHOWSQL = "database.hibernate.show_sql";
	private static final String PROPERTY_NAME_HIBERNATE_FORMATSQL = "database.hibernate.format_sql";
	private static final String PROPERTY_NAME_HIBERNATE_DEFAULTSCHEMA = "database.hibernate.default_schema";

	@Resource
	private Environment env;

	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(env.getRequiredProperty(PROPERTY_NAME_DATABASE_DRIVER));
		dataSource.setUrl(env.getRequiredProperty(PROPERTY_NAME_DATABASE_URL));
		dataSource.setUsername(env.getRequiredProperty(PROPERTY_NAME_DATABASE_USERNAME));
		dataSource.setPassword(env.getRequiredProperty(PROPERTY_NAME_DATABASE_PASSWORD));
		return dataSource;
	}

	/**
	 * Load the Entity Manager.
	 * @return the entity manager
	 * @throws NamingException jndi error
	 */
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(false);

		LocalContainerEntityManagerFactoryBean factory =
				new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan("swordfishsync.domain");
		factory.setDataSource(dataSource());
		factory.setJpaProperties(additionalProperties());
		return factory;
	}

	/**
	 * Load the Transaction Manager.
	 * @return the transaction manager
	 * @throws NamingException jndi error
	 */
	@Bean
	public PlatformTransactionManager transactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
		return transactionManager;
	}

	/**
	 * Configure Hibernate settings defined in application.properties to use with the Entity Manager.
	 * @return additional hiberate settings
	 */
	Properties additionalProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.dialect", env.getRequiredProperty(PROPERTY_NAME_HIBERNATE_DIALECT));
		properties.setProperty("hibernate.hbm2ddl.auto", env.getRequiredProperty(PROPERTY_NAME_HIBERNATE_HBM2DDL_AUTO));
		properties.setProperty("hibernate.show_sql", env.getRequiredProperty(PROPERTY_NAME_HIBERNATE_SHOWSQL));
		properties.setProperty("hibernate.format_sql", env.getRequiredProperty(PROPERTY_NAME_HIBERNATE_FORMATSQL));
		properties.setProperty("hibernate.default_schema", env.getRequiredProperty(PROPERTY_NAME_HIBERNATE_DEFAULTSCHEMA));
		return properties;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskScheduler());
	}
	
    @Bean(destroyMethod = "shutdown")
    public Executor taskScheduler() {
    	return Executors.newScheduledThreadPool(1);
    }
    
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        return executor;
    }
    
}
