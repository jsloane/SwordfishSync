package swordfishsync.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Configuration {

	@Id
	@GeneratedValue
	Long					id;

	@Version
	Long					version;

	String					title;
	
	@ManyToOne
	@JsonIgnore
	private Configuration	parentConfiguration;

	@OneToMany(mappedBy = "parentConfiguration", fetch = FetchType.EAGER)
	List<Configuration>		childConfiguration;
	
	@OneToOne
	Setting					setting;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Configuration getParentConfiguration() {
		return parentConfiguration;
	}

	public void setParentConfiguration(Configuration parentConfiguration) {
		this.parentConfiguration = parentConfiguration;
	}

	public List<Configuration> getChildConfiguration() {
		return childConfiguration;
	}

	public void setChildConfiguration(List<Configuration> childConfiguration) {
		this.childConfiguration = childConfiguration;
	}

	public Setting getSetting() {
		return setting;
	}

	public void setSetting(Setting setting) {
		this.setting = setting;
	}

	@Override
	public String toString() {
		return "Configuration [id=" + id + ", version=" + version + ", title=" + title + ", setting=" + setting + "]";
	}

}
