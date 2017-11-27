package swordfishsync;

import java.util.Properties;

import javax.annotation.Resource;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Use Spring to configure the application.
 * This configures the DataSource, JPA entities, beans, etc.
 */
@Configuration
@EnableTransactionManagement
@ComponentScan("swordfishsync")
@PropertySource("classpath:application.properties")
@EnableJpaRepositories("swordfishsync.repository")
public class TestConfig {

	@Resource
	private Environment env;

	@Bean
	public DataSource dataSource() throws NamingException {
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
				.build();
	}

	@Bean
	public InitializingBean populatorExecutor() {
		return () -> DatabasePopulatorUtils.execute(databasePopulator(), dataSource());
	}

	@Bean
	public ResourceDatabasePopulator databasePopulator() {
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.setSqlScriptEncoding("UTF-8");
		populator.addScript(new ClassPathResource("test-data.sql"));
		return populator;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws NamingException {
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(false);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan("swordfishsync.domain");
		factory.setDataSource(dataSource());
		factory.setJpaProperties(additionalProperties());
		return factory;
	}

	@Bean
	public PlatformTransactionManager transactionManager() throws NamingException {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
		return transactionManager;
	}

	Properties additionalProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
		properties.setProperty("hibernate.show_sql", "false");
		return properties;
	}

}
