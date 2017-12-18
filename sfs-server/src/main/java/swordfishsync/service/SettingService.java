package swordfishsync.service;

import java.math.BigDecimal;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import swordfishsync.domain.Configuration;
import swordfishsync.domain.Setting;
import swordfishsync.repository.ConfigurationRepository;
import swordfishsync.repository.MessageRepository;
import swordfishsync.repository.SettingRepository;
import swordfishsync.security.SecurityDelegatingFilterProxy;
import swordfishsync.service.dto.ConfigurationDto;

@Transactional
@Service("settingService")
public class SettingService {

	public static final String CODE_APP_TITLE = "app.title"; // TODO
	public static final String CODE_APP_ERROR_EMAIL = "app.error.email";
	public static final String CODE_APP_NOTIFICATION_EMAIL_SUBJECT = "app.notifications.email.subject";
	public static final String CODE_APP_SECURITY_ENABLEBASICAUTH = "app.security.enableBasicAuth";
	public static final String CODE_APP_SECURITY_USERNAME = "app.security.username";
	public static final String CODE_APP_SECURITY_PASSWORD = "app.security.password";
	public static final String CODE_APP_SECURITY_ACCEPTUNTRUSTEDCERTS = "app.security.acceptUntrustedCertificates";

	public static final String CODE_MEDIA_TVDB_APIKEY = "media.tvdb.apikey";
	public static final String CODE_MEDIA_TVDB_NOTICE = "media.tvdb.notice";
	public static final String CODE_MEDIA_TMDB_APIKEY = "media.tmdb.apikey";
	public static final String CODE_MEDIA_TMDB_NOTICE = "media.tmdb.notice";

	public static final String CODE_EMAIL_HOST = "email.host";
	public static final String CODE_EMAIL_PORT = "email.port";
	public static final String CODE_EMAIL_FROM = "email.from";

	public static final String CODE_TORRENT_TYPE = "torrent.type";
	public static final String CODE_TORRENT_HOST = "torrent.host";
	public static final String CODE_TORRENT_PORT = "torrent.port";
	public static final String CODE_TORRENT_USERNAME = "torrent.username";
	public static final String CODE_TORRENT_PASSWORD = "torrent.password";
	
	@Resource
	ConfigurationRepository configuratonRepository;

	@Resource
	SettingRepository settingRepository;

	@Resource
	MessageRepository messageRepository;

	@Resource
	TorrentClientService torrentClientService;

	@Resource
	JavaMailSenderImpl mailSender;
	
	public void initiliseConfigurationAndSettings() {

		Configuration mainConfig = findOrCreateConfiguration(null, "Configuration");
		Configuration applicationConfig = findOrCreateConfiguration(mainConfig, "Application");
		findOrCreateSetting(applicationConfig, "Title", CODE_APP_TITLE, String.class, "SwordfishSync", true);
		findOrCreateSetting(applicationConfig, "Notification Email Subject", CODE_APP_NOTIFICATION_EMAIL_SUBJECT, String.class, "SwordfishSync Notification", false);
		findOrCreateSetting(applicationConfig, "Error Report Email", CODE_APP_ERROR_EMAIL, String.class, "", false);
		Configuration securityConfig = findOrCreateConfiguration(applicationConfig, "Security");
		findOrCreateSetting(securityConfig, "Enable Basic Authentication", CODE_APP_SECURITY_ENABLEBASICAUTH, Boolean.class, "false", true);
		findOrCreateSetting(securityConfig, "Username", CODE_APP_SECURITY_USERNAME, String.class, "swordfishsync", true);
		findOrCreateSetting(securityConfig, "Password", CODE_APP_SECURITY_PASSWORD, String.class, "swordfishsync", true);
		findOrCreateSetting(securityConfig, "Accept untrusted connections", CODE_APP_SECURITY_ACCEPTUNTRUSTEDCERTS, Boolean.class, "false", true);
		Configuration mediaConfig = findOrCreateConfiguration(mainConfig, "Media");
		Configuration tvdbConfig = findOrCreateConfiguration(mediaConfig, "TVDB");
		findOrCreateSetting(tvdbConfig, "API Key", CODE_MEDIA_TVDB_APIKEY, String.class, "", false);
		findOrCreateSetting(tvdbConfig, "Notice", CODE_MEDIA_TVDB_NOTICE, String.class, "thetvdb.com", false);
		Configuration tmdbConfig = findOrCreateConfiguration(mediaConfig, "TMDb");
		findOrCreateSetting(tmdbConfig, "API Key", CODE_MEDIA_TMDB_APIKEY, String.class, "", false);
		findOrCreateSetting(tmdbConfig, "Notice", CODE_MEDIA_TMDB_NOTICE, String.class, "This product uses the TMDb API but is not endorsed or certified by TMDb.", false);
		Configuration emailConfig = findOrCreateConfiguration(mainConfig, "Email");
		findOrCreateSetting(emailConfig, "Hostname", CODE_EMAIL_HOST, String.class, "localhost", true);
		findOrCreateSetting(emailConfig, "Port", CODE_EMAIL_PORT, Integer.class, "25", true);
		findOrCreateSetting(emailConfig, "From address", CODE_EMAIL_FROM, String.class, "localhost", true);
		Configuration torrentHostConfig = findOrCreateConfiguration(mainConfig, "Torrent Client");
		findOrCreateSetting(torrentHostConfig, "Type", CODE_TORRENT_TYPE, String.class, "transmission", true);
		findOrCreateSetting(torrentHostConfig, "Hostname", CODE_TORRENT_HOST, String.class, "localhost", true);
		findOrCreateSetting(torrentHostConfig, "Port", CODE_TORRENT_PORT, Integer.class, "9091", true);
		findOrCreateSetting(torrentHostConfig, "Username", CODE_TORRENT_USERNAME, String.class, "transmission", false);
		findOrCreateSetting(torrentHostConfig, "Password", CODE_TORRENT_PASSWORD, String.class, "transmission", false);

		// set auth enabled config
		SecurityDelegatingFilterProxy.authEnabled = getValue(CODE_APP_SECURITY_ENABLEBASICAUTH, Boolean.class);

		// set mail config
        mailSender.setHost(getValue(SettingService.CODE_EMAIL_HOST, String.class));
        mailSender.setPort(getValue(SettingService.CODE_EMAIL_PORT, Integer.class));

	}

	private Configuration findOrCreateConfiguration(Configuration parentConfig, String title) {
		Configuration config = null;

		if (parentConfig != null) {
			config = configuratonRepository.findByParentConfigurationAndTitle(parentConfig, title);
		} else {
			config = configuratonRepository.findByTitle(title);
		}
		
		if (config == null) {
			config = new Configuration();
			config.setTitle(title);
			if (parentConfig != null) {
				config.setParentConfiguration(parentConfig);
			}
			config = configuratonRepository.save(config);
		}
		
		return config;
	}

	private <T> Setting findOrCreateSetting(Configuration parentConfig, String configTitle, String code, Class<T> classType, String defaultValue, boolean mandatory) {
		Configuration settingConfig = findOrCreateConfiguration(parentConfig, configTitle);
		Setting setting = settingRepository.findByCode(code);

		if (settingConfig.getSetting() == null || setting == null) {
			if (setting == null) {
				setting = new Setting();
			}
			setting.setCode(code);
			setting.setType(classType.getName());
			setting.setValue(defaultValue);
			setting.setMandatory(mandatory);
			setting = settingRepository.save(setting);
			settingConfig.setSetting(setting);
			settingConfig = configuratonRepository.save(settingConfig);
		}
		
		// TODO mandatory

		return settingConfig.getSetting();		
	}
	
	public <T> Setting setValue(String code, String value) {
		Setting setting = settingRepository.findByCode(code);
		
		if (setting == null) {
			// unknown setting
			throw new IllegalArgumentException("Unknown setting [" + code + "]");
		}
		
		if (setting.getMandatory() && (value == null || value.isEmpty())) {
			// missing value for mandatory setting
			throw new IllegalArgumentException(String.format("Mandatory setting [%s] not provided", setting.getCode()));
		}
		
		if (setting.getValue().equals(value)) {
			return setting;
		}

		// validate type
		if (Boolean.class.getName().equals(setting.getType())) {
			decodeValue(Boolean.class, value);
		} else if (Integer.class.getName().equals(setting.getType())) {
			decodeValue(Integer.class, value);
		} else if (BigDecimal.class.getName().equals(setting.getType())) {
			decodeValue(BigDecimal.class, value);
		} else if (String.class.getName().equals(setting.getType())) {
			decodeValue(String.class, value);
		}
		
		setting.setValue(value);
		setting = settingRepository.saveAndFlush(setting);

		if (CODE_EMAIL_HOST.equals(code)) {
			// update mailSender host
			mailSender.setHost(decodeValue(String.class, value));
		}
		if (CODE_EMAIL_PORT.equals(code)) {
			// update mailSender port
			mailSender.setPort(decodeValue(Integer.class, value));
		}
		if (CODE_TORRENT_HOST.equals(code) || CODE_TORRENT_PORT.equals(code) || CODE_TORRENT_USERNAME.equals(code) || CODE_TORRENT_PASSWORD.equals(code)) {
			// update torrent client
			torrentClientService.setTorrentClient();
		}
		if (CODE_APP_SECURITY_ENABLEBASICAUTH.equals(code)) {
			// update auth
			SecurityDelegatingFilterProxy.authEnabled = decodeValue(Boolean.class, value);
		}
		
		return setting;
	}
	
	public <T> T getValue(String code, Class<T> classType) {
		Setting setting = settingRepository.findByCodeAndType(code, classType.getName());

		if (setting == null) {
			return null;
		}
		
		return decodeValue(classType, setting.getValue());
	}

	private <T> T decodeValue(Class<T> classType, String value) {
		T valueObject = null;
		if (value != null) {
			if (Boolean.class.getName().equals(classType.getName())) {
				valueObject = classType.cast(Boolean.valueOf(value));
			} else if (Integer.class.getName().equals(classType.getName())) {
				try {
					valueObject = classType.cast(Integer.valueOf(value));
				} catch (NumberFormatException ne) {
				}
			} else if (BigDecimal.class.getName().equals(classType.getName())) {
				try {
					valueObject = classType.cast(new BigDecimal(value));
				} catch (NumberFormatException ne) {
				}
			} else if (String.class.getName().equals(classType.getName())) {
				valueObject = classType.cast(value);
			} else {
				// unknown type
				throw new IllegalArgumentException("Unsupported setting type [" + classType.getName() + "]");
			}
		}
		return valueObject;
	}

	public ConfigurationDto getConfiguration() {
		Configuration configuration = configuratonRepository.findByParentConfigurationIsNull();
		return ConfigurationDto.convertToConfigurationDto(configuration);
	}

	public void setSettings(Map<String, String> settingCodeValues) {
		for (Map.Entry<String, String> settingCodeValue : settingCodeValues.entrySet()) {
		    String code = settingCodeValue.getKey();
		    String value = settingCodeValue.getValue();
		    setValue(code, value);
		}
	}
	
}
