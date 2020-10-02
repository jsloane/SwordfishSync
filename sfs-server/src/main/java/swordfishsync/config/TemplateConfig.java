package swordfishsync.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
//import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
//import org.thymeleaf.templateresolver.TemplateResolver;

@Configuration
public class TemplateConfig {

	/*@Bean
	public TemplateResolver templateResolver() {
	    TemplateResolver resolver = new ServletContextTemplateResolver();
	    resolver.setPrefix("/WEB-INF/templates/views/");
	    resolver.setSuffix(".html");
	    resolver.setCharacterEncoding("UTF-8");
	    resolver.setTemplateMode("HTML5");
	    resolver.setOrder(2);
	    return resolver;
	}*/

	@Bean
	public ClassLoaderTemplateResolver emailTemplateResolver() {
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
	    resolver.setPrefix("templates/mail/");
	    resolver.setSuffix(".html");
	    resolver.setTemplateMode("HTML");
	    resolver.setCharacterEncoding("UTF-8");
	    resolver.setOrder(1);
	    return resolver;
	}

	@Bean
	public SpringTemplateEngine templateEngine() {
	    final SpringTemplateEngine engine = new SpringTemplateEngine();
	    final Set<ITemplateResolver> templateResolvers = new HashSet<ITemplateResolver>();
	    templateResolvers.add(emailTemplateResolver());
	    //templateResolvers.add(templateResolver());
	    engine.setTemplateResolvers(templateResolvers);
	    return engine;
	}

}
