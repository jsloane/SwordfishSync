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
            <h1>Bulk modify filter entries</h1>
                <fieldset class="form">
                	<div class="fieldcontain"><label for="filter-helper-text">Simple</label>
                		<select id="filter-helper-type">
                			<option selected="selected" value="add">Add filter</option>
							<option value="ignore">Ignore filter</option>
						</select>
                		<g:field type="text" name="filter-helper-text" value=""/>
                		<button id="filter-helper-add">Add to filter</button>
                	</div>
                </fieldset>
                
            <g:form action="bulkModify" method="POST">
                <g:hiddenField name="version" value="${this.filterAttribute?.version}" />
            	<g:hiddenField name="returnToFeedProvider" value="${params.returnToFeedProvider}" />
            	<g:hiddenField name="feedProvider.id" value="${params.feedProvider.id}" />

                <fieldset class="form">
                	<div class="fieldcontain"><label for="advanced">Advanced</label>
                	<div style="display:inline-block;width:30%;">
		                Add regex:<br/>
		                <g:textArea style="width:100%;height:500px;" name="filterAddEntries" value="${feedProvider.filterAttributes.findAll{it.filterType == swordfishsync.FeedProvider.FeedFilterAction.ADD}.join('\n')}" />
	                </div>
	                <div style="display:inline-block;width:30%;">
		                Ignore regex:<br/>
		                <g:textArea style="width:100%;height:500px;" name="filterIgnoreEntries" value="${feedProvider.filterAttributes.findAll{it.filterType == swordfishsync.FeedProvider.FeedFilterAction.IGNORE}.join('\n')}" />
	                </div>
				</div>
				
				<br/>
				
                <fieldset class="buttons">
                    <input class="save" type="submit" value="${message(code: 'default.button.update.label', default: 'Update')}" />
                </fieldset>
            </g:form>
        </div>
        
        <g:javascript>
			$(document).ready(function() {
				var addToTextarea = function($texarea, string) {
					$texarea.append(string);
					$texarea.scrollTop(
						$texarea[0].scrollHeight - $texarea.height()
					);
				};
				
				$('#filter-helper-add').click(function(event) {
					var regexString = $('#filter-helper-text').val();
					if (regexString) {
						regexString = '&#13;&#10;(?i).*' + regexString.replace(/ /g, '.*') + '.*';
						var type = $('#filter-helper-type').val();
						if (type === "add") {
							addToTextarea($('#filterAddEntries'), regexString);
						} else if (type === "ignore") {
							addToTextarea($('#filterIgnoreEntries'), regexString);
						}
						$('#filter-helper-text').val('');
					}
					event.preventDefault();
				});
			});
        </g:javascript>
        
    </body>
</html>
