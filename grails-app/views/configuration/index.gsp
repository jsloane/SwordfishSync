<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'configuration.label', default: 'Configuration')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="edit-configuration" class="content scaffold-edit" role="main">
        	<h1>Settings</h1>
        	<ul class="property-list">
        		<li class="fieldcontain">
				    <span id="valueObject-label" class="property-label">
				    	Purging Torrents will update status of those in progress to skipped if they are not being downloaded by the torrent client.
				    </span>
    				<div class="property-value" aria-labelledby="valueObject-label">
			        	<form action="purgeInprogressTorrents" method="POST">
			        		<input class="save" type="submit" value="Purge Torrents">
			        	</form>
				    </div>
				</li>
			</ul>
			<g:configuration configuration="${configuration}" level="${1}" mode="view" />
        </div>
        <fieldset class="buttons">
            <g:link class="edit" action="edit" ><g:message code="default.button.edit.label" default="Edit" /></g:link>
        </fieldset>
    </body>
</html>