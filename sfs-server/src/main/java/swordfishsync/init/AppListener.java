package swordfishsync.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import swordfishsync.service.SettingService;

@Component
public class AppListener implements ApplicationListener<ContextRefreshedEvent> {
	
	@Autowired
	SettingService adminService;
	
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// application startup/reload
		adminService.initiliseConfigurationAndSettings();
	}
	
}
