<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'configuration.label', default: 'Configuration')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="edit-configuration" class="content scaffold-edit" role="main">
			<g:configuration configuration="${configuration}" level="${1}" mode="view" />
        </div>
        <fieldset class="buttons">
            <g:link class="edit" action="edit" ><g:message code="default.button.edit.label" default="Edit" /></g:link>
        </fieldset>
    </body>
</html>