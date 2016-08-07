<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'filterAttribute.label', default: 'FilterAttribute')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav" role="navigation">
            <ul>
            	<g:if test="${params.returnToFeedProvider}">
                	<li><g:link class="list" controller="feedProvider" action="show" id="${params.feedProvider.id}">Return to Feed</g:link></li>
                </g:if>
            </ul>
        </div>
        <div id="show-filterAttribute" class="content scaffold-show" role="main">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <f:display bean="filterAttribute" />
            <g:form resource="${this.filterAttribute}" method="DELETE">
            	<g:hiddenField name="returnToFeedProvider" value="${params.returnToFeedProvider}" />
            	<g:hiddenField name="feedProvider.id" value="${params.feedProvider.id}" />
                <fieldset class="buttons">
                    <g:link class="edit" action="edit" resource="${this.filterAttribute}" params="${['feedProvider.id': params.feedProvider.id, 'returnToFeedProvider': false]}"><g:message code="default.button.edit.label" default="Edit"/></g:link>
                    <input class="delete" type="submit" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
