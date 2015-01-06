<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mmt" tagdir="/WEB-INF/tags" %>

<mmt:pageWrapper pageId="feeds" pageName="${feed.feedInfo.name} - Delete Feed">
	<h3 class="feed-name">Delete feed: ${feed.feedInfo.name}?</h3>
	
	<c:if test="${!empty feed.torrentsInprogress}">
		<p>Warning: this feed has torrents in progress, which will no longer be managed.</p>
		<div class="table" id="torrent-list">
			<div class="table-row" id="torrent-header">
			    <div class="table-cell">
			        name
			    </div>
			    <div class="table-cell">
			        clientTorrentId
			    </div>
			    <div class="table-cell">
			        status
			    </div>
			    <div class="table-cell">
			        dateAdded
			    </div>
			    <div class="table-cell">
			        dateCompleted
			    </div>
			    <div class="table-cell">
			        hashString
			    </div>
			</div>
			<c:forEach items="${feed.torrentsInprogress}" var="torrent">
				<div class="table-row torrent-record">
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
			</c:forEach>
		</div>
	</c:if>
	
	<p>Are you sure you want to delete this feed?</p>
	
	<form method="post" action="?">
	    <input type="hidden" name="confirm_delete" value="yes" />
	    <button type="submit">Yes, Delete</button>
	    <mmt:buttonLink url="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}" text="Cancel" />
	</form>
</mmt:pageWrapper>