package swordfishsync.domain;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity
public class Setting {

	@Id
	String code; // unique: true

	@Version
	Long version;

	@NotNull
	String type; // type(blank: false, inList: ['string', 'integer', 'boolean'])

	@NotNull
	String value;

	//@NotNull
	Boolean mandatory;

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
		return "Setting [code=" + code + ", version=" + version + ", type=" + type + ", value=" + value + ", mandatory="
				+ mandatory + "]";
	}

}
