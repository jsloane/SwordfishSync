package swordfishsync

class ApiController {
	
    def deleteMessage() {
		if (params.id) {
			def message = Message.get(params.id)
			if (message) {
				message.delete(flush: true)
				response.status = 200
			} else {
				response.status = 404
			}
		} else {
			response.status = 404
		}
	}
	
}
