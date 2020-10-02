package swordfishsync.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import swordfishsync.domain.Configuration;

public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

	Configuration findByParentConfigurationAndTitle(Configuration parentConfig, String title);

	Configuration findByTitle(String title);

	Configuration findByParentConfigurationIsNull();

}
