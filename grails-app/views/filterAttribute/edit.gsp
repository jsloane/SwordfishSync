<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'filterAttribute.label', default: 'FilterAttribute')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <a href="#edit-filterAttribute" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div class="nav" role="navigation">
            <ul>
            	<g:if test="${params.returnToFeedProvider}">
                	<li><g:link class="list" controller="feedProvider" action="show" id="${params.feedProvider.id}">Return to Feed</g:link></li>
                </g:if>
            </ul>
        </div>
        <div id="edit-filterAttribute" class="content scaffold-edit" role="main">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:hasErrors bean="${this.filterAttribute}">
            <ul class="errors" role="alert">
                <g:eachError bean="${this.filterAttribute}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
                </g:eachError>
            </ul>
            </g:hasErrors>
            <g:form resource="${this.filterAttribute}" method="PUT">
                <g:hiddenField name="version" value="${this.filterAttribute?.version}" />
            	<g:hiddenField name="returnToFeedProvider" value="${params.returnToFeedProvider}" />
            	<g:hiddenField name="feedProvider.id" value="${params.feedProvider.id}" />
                <fieldset class="form">
                    <f:all bean="filterAttribute"/>
                </fieldset>
                <fieldset class="buttons">
                    <input class="save" type="submit" value="${message(code: 'default.button.update.label', default: 'Update')}" />
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
