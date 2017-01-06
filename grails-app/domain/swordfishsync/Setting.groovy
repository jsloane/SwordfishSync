package swordfishsync

import java.text.ParseException
import java.text.SimpleDateFormat

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class Setting {
	
	String	code
	String	type
	String	value
	Date 	dateCreated
	Date 	lastUpdated
	
	Object	getValueObject() {
		this.decodeValue(this.type, this.value)
	}
	
	static transients = ['valueObject']
	
    static constraints = {
		code(blank: false, size: 1..100, unique: true)
		type(blank: false, inList: ['string', 'integer', 'decimal', 'date', 'boolean'])
		value(nullable: true, size: 1..100, validator: { val, obj ->
			if (val) {
				Setting.decodeValue(obj.type, val) != null
			}
		})
    }
	
	static mapping = {
		//version	false
		columns {
			code index: 'setting_value_code_idx'
		}
		cache(false)//(true)
	}
	
	static valueFor(String code) {
		Setting setting = Setting.findByCode(code)
		return setting ? Setting.decodeValue(setting.type, setting.value) : null
	}
	
	static valueFor(String code, Object dflt) {
		def val = valueFor(code)
		return val != null ? val : dflt
	}
	
	private static decodeValue(String type, String val) {
		if (val) {
			switch (type) {
				case "boolean":
					if (val == 'on') {
						val = 'true'
					}
					return Boolean.valueOf(val)
					break
				case "integer":
					try {
						return new Integer(val)
					} catch (NumberFormatException ne) {
					}
					break
				case "decimal":
					try {
						return new BigDecimal(val)
					} catch (NumberFormatException ne) {
					}
					break
				case "date":
				try {
					def fmt = val.length() == 10 ? 'yyyy-MM-dd' : 'yyyy-MM-dd HH:mm'
						return new SimpleDateFormat(fmt, Locale.US).parse(val)
					} catch (ParseException pe) {
					}
					break
				default:  // string
					return val
					break
			}
		}
		return null
	}
	
}
