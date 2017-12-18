package swordfishsync.config;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import swordfishsync.service.SettingService;

@Configuration
public class MailConfig {
	
    /*@Value("${email.host}")
    private String host;
	
    @Value("${email.port}")
    private Integer port;*/

	@Resource
	SettingService settingService;
	
    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        //javaMailSender.setHost(host);
        //javaMailSender.setPort(port);
        return javaMailSender;
    }
    
}
