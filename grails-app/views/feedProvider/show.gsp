<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'feedProvider.label', default: 'FeedProvider')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="show-feedProvider" class="content scaffold-show" role="main">
            <h1>Feed: ${this.feedProvider.name}</h1>
    		
            <ol class="property-list">
				<f:display bean="feedProvider" property="feed.url"/>
				<f:display bean="feedProvider" property="name"/>
				<f:display bean="feedProvider" property="active"/>
				<f:display bean="feedProvider" property="feedAction"/>
				<li class="fieldcontain">
					<span class="property-label">Torrents</span>
					<div class="property-value">
						<g:link controller="torrent" action="index" params="${[feedProviderId: this.feedProvider.id]}">List torrents</g:link>
					</div>
				</li>
				<f:display bean="feedProvider" property="notifyEmail"/>
				<g:if test="${feedProvider.syncInterval <= 0}">
					<f:display bean="feedProvider" property="syncInterval" value="Not set"/>
				</g:if>
				<g:else>
					<f:display bean="feedProvider" property="syncInterval"/>
				</g:else>
				<f:display bean="feedProvider" property="deleteInterval"/>
				<g:if test="${feedProvider.syncInterval <= 0}">
					<f:display bean="feedProvider" property="uploadLimit" value="Not set"/>
				</g:if>
				<g:else>
					<f:display bean="feedProvider" property="uploadLimit"/>
				</g:else>
				<f:display bean="feedProvider" property="downloadDirectory"/>
				<f:display bean="feedProvider" property="determineSubDirectory"/>
				<f:display bean="feedProvider" property="skipDuplicates"/>
				<f:display bean="feedProvider" property="skipPropersRepacksReals"/>
				<f:display bean="feedProvider" property="extractRars"/>
				<f:display bean="feedProvider" property="removeTorrentOnComplete"/>
				<f:display bean="feedProvider" property="removeTorrentDataOnComplete"/>
				<f:display bean="feedProvider" property="systemCommand"/>
				<f:display bean="feedProvider" property="detailsUrlValueFromRegex"/>
				<f:display bean="feedProvider" property="detailsUrlFormat"/>
				<f:display bean="feedProvider" property="systemCommand"/>
				<f:display bean="feedProvider" property="filterEnabled"/>
				<li class="fieldcontain">
					<span class="property-label"></span>
					<div class="property-value">
						<a href="#" onclick="$('#feed-filter-details').toggle();">Show/Hide Filter Options</a>
					</div>
				</li>
				<div id="feed-filter-details" style="display:none;">
					<g:set var="filterActionValue" value="${feedProvider.filterAction}"/>
					<g:if test="${swordfishsync.FeedProvider.FeedFilterAction.ADD.equals(feedProvider.filterAction)}">
						<g:set var="filterActionValue" value="Add: Add all records, unless matched by ignore regex"/>
					</g:if>
					<g:elseif test="${swordfishsync.FeedProvider.FeedFilterAction.IGNORE.equals(feedProvider.filterAction)}">
						<g:set var="filterActionValue" value="Ignore: Only add records matched by add regex"/>
					</g:elseif>
					<f:display bean="feedProvider" property="filterAction" value="${filterActionValue}"/>
					<f:display bean="feedProvider" property="filterAttributes" collection="${addFilterAttributes}" label="Filter (Add)"
						linkParams="${['feedProvider.id': feedProvider.id, 'returnToFeedProvider': true]}" />
					<f:display bean="feedProvider" property="filterAttributes" collection="${ignoreFilterAttributes}" label="Filter (Ignore)"
						linkParams="${['feedProvider.id': feedProvider.id, 'returnToFeedProvider': true]}" />
					<li class="fieldcontain">
						<span class="property-label"></span>
						<div class="property-value">
							<g:link controller="filterAttribute" action="create" params="${['feedProvider.id': feedProvider.id, 'returnToFeedProvider': true]}">
								Add filter entry
							</g:link>
						</div>
						<span class="property-label"></span>
						<div class="property-value">
							<g:link controller="filterAttribute" action="bulkModify" params="${['feedProvider.id': feedProvider.id, 'returnToFeedProvider': true]}">
								Bulk modify filter entries
							</g:link>
						</div>
					</li>
				</div>
			</ol>
			
            
			<%-- <f:with bean="feedProvider">
				<f:display property="feed.isCurrent"/>
			</f:with>
            <f:display bean="feedProvider" /> --%>
            
            
	        <div class="nav" role="navigation">
	            <g:form resource="${this.feedProvider}" method="DELETE">
	                <fieldset class="buttons">
	                    <g:link class="edit" action="edit" resource="${this.feedProvider}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
	                    <input class="delete" type="submit" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
	                    <g:link class="create" action="addTorrent" resource="${this.feedProvider}">Add Torrent</g:link>
	                </fieldset>
	            </g:form>
	        </div>
            
            <%-- <h1>Torrents</h1>
            
            <table class="do-data-table">
            	<thead>
            		<tr>
            			<th>Actions</th>
            			<th>Name</th>
            			<th>Status</th>
            			<th class="default-sort default-sort-order-desc">Date Published</th>
            			<th>Date Added</th>
            			<th>Date Completed</th>
            			<th>inCurrentFeed</th>
            			<th>Client ID</th>
            			<th>Hash</th>
            		</tr>
            	</thead>
            	<tbody>
            		<g:each in="${feedProvider.feed.torrents}" var="torrent">
            			<g:if test="${torrent.getTorrentState(feedProvider)}">
	            			<tr>
	            				<td style="white-space:nowrap;">
	            					<span>Details</span>
	            					<span>Download</span>
	            				</td>
	            				<td><a href="<g:createLink controller="torrent" action="show" id="${torrent.id}" />">${torrent.name}</a></td>
	            				<td>${torrent.getTorrentState(feedProvider)?.status}</td>
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
            </table> --%>
        </div>
    </body>
</html>
