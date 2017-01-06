<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'feedProvider.label', default: 'FeedProvider')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="content scaffold-show" role="main">
	        <h1>Add torrents to feed: ${this.feedProvider.name}</h1>
            <g:form resource="${this.feedProvider}" action="addTorrent">
                <fieldset class="form">
                	<div class="fieldcontain required"><label for="torrentUrls">Enter torrent/magnet links<span class="required-indicator">*</span></label>
	    				<g:textArea name="torrentUrls" style="width:70%;"/>
                	</div>
		    		<p class="form-input-help-text">
		    			Enter each link on a new line.
		    		</p>
                </fieldset>
                <fieldset class="buttons">
                    <g:submitButton name="create" class="save" value="${message(code: 'default.button.add.label', default: 'Add')}" />
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
