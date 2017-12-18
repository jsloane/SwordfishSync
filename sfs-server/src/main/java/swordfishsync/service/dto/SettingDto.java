package swordfishsync.service.dto;

import swordfishsync.domain.Setting;

public class SettingDto {

	String code;
	String type;
	String value;
	Boolean mandatory;
	
	public static SettingDto convertToSettingDto(Setting setting) {
		if (setting == null) {
			return null;
		}
		
		SettingDto settingDto = new SettingDto();
		
		settingDto.setCode(setting.getCode());
		settingDto.setType(setting.getType());
		settingDto.setValue(setting.getValue());
		settingDto.setMandatory(setting.getMandatory());
		
		return settingDto;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Boolean getMandatory() {
		return mandatory;
	}

	public void setMandatory(Boolean mandatory) {
		this.mandatory = mandatory;
	}

	@Override
	public String toString() {
		return "SettingDto [code=" + code + ", type=" + type + ", value=" + value + ", mandatory=" + mandatory + "]";
	}
	
}
