package swordfishsync

import static org.springframework.http.HttpStatus.*

import grails.transaction.Transactional

@Transactional(readOnly = true)
class FilterAttributeController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond FilterAttribute.list(params), model:[filterAttributeCount: FilterAttribute.count()]
    }

    def show(FilterAttribute filterAttribute) {
		println 'params: ' + params
        respond filterAttribute, model: [params: params]
    }

    def create() {
		//if (!params.feedProvider?.id) {
			// error
		//}
		
        respond new FilterAttribute(params), model:[params: params]
    }
	
	@Transactional
	def bulkModify() {
		FeedProvider feedProvider = FeedProvider.get(params.feedProvider.id)
		
		if (request.method == 'POST') {
			// save all filter entries
			String eol = System.lineSeparator()
			
			feedProvider.filterAttributes.clear()
			params.filterAddEntries.split(eol).each { entry ->
				if (entry.trim()) {
					FilterAttribute filterAttribute = new FilterAttribute(filterType: FeedProvider.FeedFilterAction.ADD, filterRegex: entry)
					feedProvider.addToFilterAttributes(filterAttribute)
				}
			}
			params.filterIgnoreEntries.split(eol).each { entry ->
				if (entry.trim()) {
					FilterAttribute filterAttribute = new FilterAttribute(filterType: FeedProvider.FeedFilterAction.IGNORE, filterRegex: entry)
					feedProvider.addToFilterAttributes(filterAttribute)
				}
			}
			if (!feedProvider.save()) {
				flash.errorMessages = ['Error saving filter entries']
			}
		}
		
		render(view: 'bulkModify', model: [
			'feedProvider': feedProvider
		])
		
	}
	
    @Transactional
    def save(FilterAttribute filterAttribute) {
        if (filterAttribute == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (filterAttribute.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond filterAttribute.errors, view:'create'
            return
        }
		
        //filterAttribute.save flush: true
		if (params.feedProvider.id) {
			println 'params.feedProvider.id: ' + params.feedProvider.id
			FeedProvider feedProvider = FeedProvider.get(params.feedProvider.id)
			println 'feedProvider: ' + feedProvider
			feedProvider.addToFilterAttributes(filterAttribute)
			feedProvider.save flush: true
			flash.successMessages = ['Filter entry added']
		} else {
			// todo: error
		}
		
		if ('true'.equals(params.returnToFeedProvider) && params.feedProvider.id) {
			chain(controller: 'feedProvider', action: 'show', params: [id: params.feedProvider.id])
		} else {
			request.withFormat {
				form multipartForm {
					flash.message = message(code: 'default.created.message', args: [message(code: 'filterAttribute.label', default: 'FilterAttribute'), filterAttribute.id])
					redirect filterAttribute
				}
				'*' { respond filterAttribute, [status: CREATED] }
			}
		}

    }

    def edit(FilterAttribute filterAttribute) {
        respond filterAttribute, model:[params: params]
    }

    @Transactional
    def update(FilterAttribute filterAttribute) {
        if (filterAttribute == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (filterAttribute.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond filterAttribute.errors, view:'edit'
            return
        }

        filterAttribute.save flush:true
		flash.successMessages = ['Filter entry updated']
		
		println 'params: ' + params
		if ('true'.equals(params.returnToFeedProvider) && params.feedProvider.id) {
			chain(controller: 'feedProvider', action: 'show', params: [id: params.feedProvider.id])
		} else {
	        request.withFormat {
	            form multipartForm {
	                flash.message = message(code: 'default.updated.message', args: [message(code: 'filterAttribute.label', default: 'FilterAttribute'), filterAttribute.id])
	                redirect action: 'show', id: filterAttribute.id, params: ['feedProvider.id': params.feedProvider.id, 'returnToFeedProvider': true]
	            }
	            '*'{ respond filterAttribute, [status: OK] }
	        }
		}
    }

    @Transactional
    def delete(FilterAttribute filterAttribute) {

        if (filterAttribute == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }
		
		def feedProviders = FeedProvider.withCriteria {
			filterAttributes {
				idEq(filterAttribute.id)
			}
		}
		
		feedProviders.each { feedProvider ->
			feedProvider.removeFromFilterAttributes(filterAttribute)
			feedProvider.save()
		}

        filterAttribute.delete flush:true
		flash.successMessages = ['Filter entry deleted']
		
		if ('true'.equals(params.returnToFeedProvider) && params.feedProvider.id) {
			chain(controller: 'feedProvider', action: 'show', params: [id: params.feedProvider.id])
		} else {
	        request.withFormat {
	            form multipartForm {
	                flash.message = message(code: 'default.deleted.message', args: [message(code: 'filterAttribute.label', default: 'FilterAttribute'), filterAttribute.id])
	                redirect action:"index", method:"GET"
	            }
	            '*'{ render status: NO_CONTENT }
	        }
		}
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'filterAttribute.label', default: 'FilterAttribute'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
