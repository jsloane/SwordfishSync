<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'configuration.label', default: 'Configuration')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="edit-configuration" class="content scaffold-edit" role="main">
			<g:configuration configuration="${configuration}" level="${1}" mode="edit" />
        </div>
    </body>
</html>
