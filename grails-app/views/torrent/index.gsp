<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'torrent.label', default: 'Torrent')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="list-torrent" class="content scaffold-list" role="main">
            <h1><g:message code="default.list.label" args="[entityName]" />
	            <g:if test="${feedProvider}">
	            	for Feed ${feedProvider.name}
	            </g:if>
            </h1>
            
            <table class="do-data-table">
            	<thead>
            		<tr>
            			<th>Actions</th>
            			<th>Name</th>
	            		<g:if test="${feedProvider}">
            				<th>Status</th>
            			</g:if>
            			<th class="default-sort default-sort-order-desc">Date Published</th>
            			<th>Date Added</th>
            			<th>Date Completed</th>
            			<th>inCurrentFeed</th>
            			<th>Client ID</th>
            			<th>Hash</th>
            		</tr>
            	</thead>
            	<tbody>
            		<g:each in="${torrentList}" var="torrent">
            			<g:if test="${!feedProvider || (feedProvider && torrent.getTorrentState(feedProvider))}">
	            			<tr>
	            				<td style="white-space:nowrap;">
	            					<span>Details</span>
	            					<span>Download</span>
	            				</td>
	            				<td><a href="<g:createLink controller="torrent" action="show" id="${torrent.id}" />">${torrent.name}</a></td>
	            				<g:if test="${feedProvider}">
	            					<td>${torrent.getTorrentState(feedProvider)?.status}</td>
	            				</g:if>
	            				<td>${torrent.datePublished}</td>
	            				<td>${torrent.dateCreated}</td>
	            				<td>${torrent.dateCompleted}</td>
	            				<td>${torrent.inCurrentFeed}</td>
	            				<td>${torrent.clientTorrentId}</td>
	            				<td>${torrent.hashString}</td>
	            			</tr>
            			</g:if>
            		</g:each>
            	</tbody>
            </table>
            
            
        </div>
    </body>
</html>