package swordfishsync.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import swordfishsync.domain.Setting;

public interface SettingRepository extends JpaRepository<Setting, Long> {

	Setting findByCode(String code);

	Setting findByCodeAndType(String code, String type);

}
