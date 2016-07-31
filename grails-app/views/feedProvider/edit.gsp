<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'feedProvider.label', default: 'FeedProvider')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="edit-feedProvider" class="content scaffold-edit" role="main">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${this.feedProvider}">
            <ul class="errors" role="alert">
                <g:eachError bean="${this.feedProvider}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
                </g:eachError>
            </ul>
            </g:hasErrors>
            <g:form resource="${this.feedProvider}" method="PUT">
                <g:hiddenField name="version" value="${this.feedProvider?.version}" />
                <fieldset class="form">
                	<div class="fieldcontain required"><label for="url">URL<span class="required-indicator">*</span></label>
                		<g:field type="url" name="url" value="${feedProvider.feed.url}" required=""/>
                	</div>
                    <f:all bean="feedProvider" except="filterAttributes,torrentStates,feed"/>
                </fieldset>
                <fieldset class="buttons">
                    <input class="save" type="submit" value="${message(code: 'default.button.update.label', default: 'Update')}" />
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
