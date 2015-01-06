<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mmt" tagdir="/WEB-INF/tags" %>

<mmt:pageWrapper pageId="feeds" pageName="Feed - ${feed.feedInfo.name}">
	<h3 class="feed-name">Viewing feed: ${feed.feedInfo.name}</h3>
	Options:
	<mmt:buttonLink url="${pageContext.request.contextPath}/feeds" text="Return to List" />
	<mmt:buttonLink url="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}/export" text="Export" />
	<mmt:buttonLink url="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}/edit" text="Edit" />
	<mmt:buttonLink url="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}/delete" text="Delete" />
	<mmt:buttonLink url="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}/torrents/add" text="Manually add torrent" />
	<br/><br/>
    
	<div class="table" id="feed">
		<mmt:tableData fieldName="Active:" fieldValue="${feed.feedInfo.active}"/>
		<mmt:tableData fieldName="Name:" fieldValue="${feed.feedInfo.name}"/>
		<mmt:tableData fieldName="URL:" fieldValue="${feed.feedInfo.url}"/>
		<mmt:tableData fieldName="Initilised:" fieldValue="${feed.feedInfo.initilised}"/>
		<mmt:tableData fieldName="Date created:" fieldValue="${feed.feedInfo.created}"/>
		<mmt:tableData fieldName="Date updated:" fieldValue="${feed.feedInfo.updated}"/>
		<mmt:tableData fieldName="Action:" fieldValue="${feed.feedInfo.action}"/>
		<mmt:tableData fieldName="Sync Interval (minutes):" fieldValue="${feed.feedInfo.syncInterval}"/>
		<mmt:tableData fieldName="Delete Interval (days):" fieldValue="${feed.feedInfo.deleteInterval}"/>
		<mmt:tableData fieldName="Download directory:" fieldValue="${feed.feedInfo.downloadDirectory}"/>
		<mmt:tableData fieldName="Determine Sub Directory:" fieldValue="${feed.feedInfo.determineSubDirectory}"/>
		<mmt:tableData fieldName="System command on torrent completion:" fieldValue="${feed.feedInfo.systemCommand}"/>
		<mmt:tableData fieldName="Remove Torrent On Completion:" fieldValue="${feed.feedInfo.removeTorrentOnComplete}"/>
		<mmt:tableData fieldName="Extract Rars:" fieldValue="${feed.feedInfo.extractRars}"/>
		<div class="table-row">
			<div class="table-cell">
			   Torrent upload limit:
			</div>
			<div class="table-cell">
				<c:choose>
					<c:when test="${feed.feedInfo.uploadLimit > 0}">
					   ${feed.feedInfo.uploadLimit} KB/s
					</c:when>
					<c:otherwise>
					   Not set
					</c:otherwise>
				</c:choose>
			</div>
	   </div>
	   <mmt:tableData fieldName="Notify Email:" fieldValue="${feed.feedInfo.notifyEmail}"/>
		<div class="table-row">
			<div class="table-cell">
			 Filter:
			</div>
			<div class="table-cell">
				<c:choose>
					<c:when test="${feed.feedInfo.filterEnabled}">
					   Enabled
					</c:when>
					<c:otherwise>
					   Disabled
					</c:otherwise>
				</c:choose>
				<mmt:buttonLink url="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}/edit/filter" text="Edit filter" />
			</div>
		</div>
	</div>
	
	<br/>
	
	Torrents:
	<div class="table" id="torrent-list">
		<div class="table-row" id="torrent-header">
			<div class="table-cell">
			    Action
			</div>
			<div class="table-cell">
			    Name
			</div>
			<div class="table-cell">
			    Client ID
			</div>
			<div class="table-cell">
			    Status
			</div>
			<div class="table-cell">
			    Date Published
			</div>
			<div class="table-cell">
			    Date Completed
			</div>
			<div class="table-cell">
			    Hash
			</div>
		</div>
		<c:forEach items="${feed.feedInfo.feedTorrents}" var="torrent">
			<c:if test="${torrent != null}">
				<div class="table-row torrent-record" data-url="${torrent.url}" data-notification-url="${feed.getTorrentDetailsUrl(torrent)}">
					<div class="table-cell">
						<c:if test="${torrent.url ne feed.getTorrentDetailsUrl(torrent)}">
						    <a href="${feed.getTorrentDetailsUrl(torrent)}">Details</a>
						</c:if>
						<c:if test="${torrent.status == torrentNotAdded || torrent.status == torrentNotifiedNotAdded || torrent.status == torrentSkipped}">
						    <a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}/torrents/${torrent.id}/download">Download</a>
						</c:if>
					</div>
					<div class="table-cell">
					    ${torrent.name}
					</div>
					<div class="table-cell">
					    ${torrent.clientTorrentId}
					</div>
					<div class="table-cell">
					    ${torrent.status}
					</div>
					<div class="table-cell">
					    ${torrent.dateAdded}
					</div>
					<div class="table-cell">
					    ${torrent.dateCompleted}
					</div>
					<div class="table-cell">
					    ${torrent.hashString}
					</div>
				</div>
			</c:if>
		</c:forEach>
	</div>
</mmt:pageWrapper>
