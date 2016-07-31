package swordfishsync

import grails.core.GrailsApplication

class ConfigurationTagLib {
    static defaultEncodeAs = [taglib:'raw']
	
	GrailsApplication grailsApplication
	
	def configuration = { attrs, body ->
		def formTagLib = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.FormTagLib')
		if ('edit'.equals(attrs.mode)) {
			out << '<form action="save" method="POST">'
			out << renderEditConfigurationHtml(attrs.configuration, attrs.level).toString()
			out << '<fieldset class="buttons"><input class="save" type="submit" value="Save"></fieldset>'
			out << '</form>'
		} else {
			out << renderViewConfigurationHtml(attrs.configuration, attrs.level).toString()
		}
	}
	
	private StringBuilder renderViewConfigurationHtml(Configuration configuration, Integer level) {
		StringBuilder configurationHtml = new StringBuilder()
		
		if (configuration.setting) {
			configurationHtml << f.display([
				'bean': configuration.setting,
				'label': configuration.title,
				'property': 'valueObject'
			])
		} else {
			configurationHtml << "<h${level}>${configuration.title}</h${level}>"
		}
		
		if (level == 1) {
			configurationHtml << '<ol class="property-list">'
		}
		configuration.childConfiguration.each { childConfiguration ->
			configurationHtml << renderViewConfigurationHtml(childConfiguration, level + 1)
		}
		if (level == 1) {
			configurationHtml << '</ol>'
		}
		
		return configurationHtml
	}
	
	private StringBuilder renderEditConfigurationHtml(Configuration configuration, Integer level) {
		StringBuilder configurationHtml = new StringBuilder()
		
		if (configuration.setting) {
			def type = 'text'
			def typeStep = null
			switch(configuration.setting.type) {
				case 'integer':
					type = 'number'
					break
				case 'decimal':
					type = 'number'
					typeStep = 'any'
					break
				case 'date':
					type = 'datetime'
					break
			}
			
			configurationHtml << '<div class="fieldcontain"><label for="' + configuration.setting.code + '">' + configuration.title + '</label>'
			if ('torrent.type'.equals(configuration.setting.code)) {
				configurationHtml << g.select([
					'name': configuration.setting.code,
					'value': configuration.setting.valueObject,
					'from': ['Transmission']
				])
			} else if (configuration.setting.type == 'boolean') {
				configurationHtml << g.checkBox([
					'name': configuration.setting.code,
					'checked': configuration.setting.valueObject
				])
			} else {
				configurationHtml << g.field([
					'type': type,
					'step': typeStep,
					'name': configuration.setting.code,
					'value': configuration.setting.valueObject
				])
			}
			configurationHtml << '</div>'
		} else {
			configurationHtml << "<h${level}>${configuration.title}</h${level}>"
		}
		
		configuration.childConfiguration.each { childConfiguration ->
			configurationHtml << '<fieldset class="form">'
			configurationHtml << renderEditConfigurationHtml(childConfiguration, level + 1)
			configurationHtml << '</fieldset>'
		}
		
		return configurationHtml
	}
	
}
