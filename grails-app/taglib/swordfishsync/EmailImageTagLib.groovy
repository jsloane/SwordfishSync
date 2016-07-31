package swordfishsync

class EmailImageTagLib {
    static defaultEncodeAs = [taglib:'raw']
	def emailImage = { attrs, body ->
		if (attrs.url) {
			out << '<div><img src="' + attrs.url + '"/></div>'
		}
	}
}
