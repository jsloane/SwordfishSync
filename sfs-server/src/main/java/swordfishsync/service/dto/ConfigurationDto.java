package swordfishsync.service.dto;

import java.util.ArrayList;
import java.util.List;

import swordfishsync.domain.Configuration;

public class ConfigurationDto {

	String						title;
	List<ConfigurationDto>		childConfiguration;
	SettingDto					setting;

	public static ConfigurationDto convertToConfigurationDto(Configuration configuration) {
		ConfigurationDto configurationDto = new ConfigurationDto();
		
		configurationDto.setTitle(configuration.getTitle());
		configurationDto.setSetting(SettingDto.convertToSettingDto(configuration.getSetting()));
		
		List<ConfigurationDto> childConfigurationDto = new ArrayList<ConfigurationDto>();
		for (Configuration childConfiguration : configuration.getChildConfiguration()) {
			childConfigurationDto.add(ConfigurationDto.convertToConfigurationDto(childConfiguration));
		}
		configurationDto.setChildConfiguration(childConfigurationDto);
		
		return configurationDto;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<ConfigurationDto> getChildConfiguration() {
		return childConfiguration;
	}

	public void setChildConfiguration(List<ConfigurationDto> childConfiguration) {
		this.childConfiguration = childConfiguration;
	}

	public SettingDto getSetting() {
		return setting;
	}

	public void setSetting(SettingDto setting) {
		this.setting = setting;
	}

	@Override
	public String toString() {
		return "ConfigurationDto [title=" + title + ", childConfiguration=" + childConfiguration + ", setting="
				+ setting + "]";
	}
	
}
