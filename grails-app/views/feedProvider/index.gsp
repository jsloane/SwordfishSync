<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'feedProvider.label', default: 'FeedProvider')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="list-feedProvider" class="content scaffold-list" role="main">
            <h1>Feeds</h1>
            <g:if test="${flash.message}">
                <div class="message" role="status">${flash.message}</div>
            </g:if>
            
            <table class="do-data-table">
            	<thead>
            		<tr>
            			<th>Enabled</th>
            			<th>Actions</th>
            			<th class="default-sort">Name</th>
            			<th>Action</th>
            			<th>Status</th>
            			<th>Last Fetched</th>
            			<th>Last Processed</th>
            			<th>Download Directory</th>
            		</tr>
            	</thead>
            	<tbody>
            		<g:each in="${feedProviderList}" var="feedProvider">
            			<tr>
            				<td>
            					<g:if test="${feedProvider.active}">
            						<span class="feed-active" title="Active">✔</span>
            					</g:if>
            					<g:else>
            						<span class="feed-inactive" title="Inactive">✘</span>
            					</g:else>
            				</td>
            				<td><a href="<g:createLink action="show" id="${feedProvider.id}" />">View</a></td>
            				<td>${feedProvider.name}</td>
            				<td>${feedProvider.feedAction}</td>
            				<td>
            					<g:if test="${feedProvider.active}">
	            					<g:if test="${feedProvider.feed.isCurrent}">
	            						<span class="feed-status-current">Up To Date</span>
	            					</g:if>
	            					<g:else>
	            						<span class="feed-status-error">${feedProvider.feed.errorMessage}</span>
	            					</g:else>
            					</g:if>
            					<g:else>
            						Disabled
            					</g:else>
            				</td>
            				<td>${feedProvider.feed.lastUpdated}</td>
            				<td>${feedProvider.lastProcessed}</td>
            				<td>${feedProvider.downloadDirectory}</td>
            			</tr>
            		</g:each>
            	</tbody>
            </table>
            
	        <div class="nav" role="navigation">
	            <ul>
	                <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
	            </ul>
	        </div>
            
            <%-- <f:table collection="${feedProviderList}" properties="['name', 'downloadDirectory', 'uploadLimit']" />
            
            <div class="pagination">
                <g:paginate total="${feedProviderCount ?: 0}" />
            </div> --%>
            
            
        </div>
    </body>
</html>