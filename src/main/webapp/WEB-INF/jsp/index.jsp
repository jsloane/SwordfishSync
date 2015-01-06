<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mmt" tagdir="/WEB-INF/tags" %>

<mmt:pageWrapper pageId="index" pageName="Status">
	<h3>Torrents downloading</h3>
	<ul id="list-torrents-downloading" class="table">
		<li id="list-torrents-downloading-header" class="table-header-group">
			<div class="table-cell">
				Feed
			</div>
			<div class="table-cell">
				Torrent
			</div>
			<div class="table-cell">
				Date added
			</div>
			<div class="table-cell">
				Progress
			</div>
		</li>
		<c:forEach items="${feeds}" var="feed">
			<c:forEach items="${feed.feedInfo.feedTorrents}" var="torrent">
				<c:if test="${torrent != null && torrent.status == torrentDownloading}">
					<c:set var="foundTorrentDownloading" value="true" />
					<li class="table-row">
						<div class="table-cell">
							${feed.feedInfo.name}
						</div>
						<div class="table-cell">
							${torrent.name}
						</div>
						<div class="table-cell">
							${torrent.dateAdded}
						</div>
						<div class="table-cell">
							<progress class="torrent-progress" value="${torrent.getClientFieldsForTorrent().getPercentDone()}" max="1"></progress>
						</div>
					</li>
				</c:if>
			</c:forEach>
		</c:forEach>
		<c:if test="${empty foundTorrentDownloading}">
			<li class="table-row">
				<div class="table-cell">
					No torrents downloading.
				</div>
			</li>
		</c:if>
	</ul>
	
	<h3>Recently notified torrents</h3>
	<ul id="list-recently-notified-torrents" class="table">
		<li id="list-recently-notified-torrents-header" class="table-header-group">
			<div class="table-cell">
				Feed
			</div>
			<div class="table-cell">
				Torrent
			</div>
			<div class="table-cell">
				Action
			</div>
			<div class="table-cell">
				Date added
			</div>
		</li>
		<c:forEach items="${feeds}" var="feed">
			<c:forEach items="${feed.feedInfo.feedTorrents}" var="torrent">
				<c:if test="${torrent != null && torrent.status == torrentNotifiedNotAdded}">
				<c:set var="foundTorrentNotified" value="true" />
				<li class="table-row">
					<div class="table-cell">
						${feed.feedInfo.name}
					</div>
					<div class="table-cell">
						${torrent.name}
					</div>
					<div class="table-cell">
						<c:if test="${torrent.url ne feed.getTorrentDetailsUrl(torrent)}">
							<a href="${feed.getTorrentDetailsUrl(torrent)}">Details</a>
						</c:if>
						<c:if test="${torrent.status == torrentNotAdded || torrent.status == torrentNotifiedNotAdded || torrent.status == torrentSkipped}">
							<a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}/torrents/${torrent.id}/download">Download</a>
						</c:if>
					</div>
					<div class="table-cell sort-timestamp" data-timestamp="${torrent.dateAdded.getTime()}">
						${torrent.dateAdded}
					</div>
				</li>
			</c:if>
			</c:forEach>
		</c:forEach>
		<c:if test="${empty foundTorrentNotified}">
			<li class="table-row">
				<div class="table-cell">
					No recent torrents.
				</div>
			</li>
		</c:if>
	</ul>
	
	<h3>Recently completed torrents</h3>
	<ul id="list-recently-completed-torrents" class="table">
		<li id="list-recently-completed-torrents-header" class="table-header-group">
			<div class="table-cell">
				Feed
			</div>
			<div class="table-cell">
				Torrent
			</div>
			<div class="table-cell">
				Date completed
			</div>
			<div class="table-cell">
				Location
			</div>
		</li>
		<c:forEach items="${feeds}" var="feed">
			<c:forEach items="${feed.feedInfo.feedTorrents}" var="torrent">
				<c:if test="${torrent != null && (torrent.status == torrentNotifiedCompleted || torrent.status == torrentCompleted)}">
					<c:set var="foundTorrentCompleted" value="true" />
					<li class="table-row">
						<div class="table-cell">
							${feed.feedInfo.name}
						</div>
						<div class="table-cell">
							${torrent.name}
						</div>
						<div class="table-cell sort-timestamp" data-timestamp="${torrent.dateCompleted.getTime()}">
							${torrent.dateCompleted}
						</div>
						<div class="table-cell">
							${torrent.getDownloadDirectoryLocation()}
						</div>
					</li>
				</c:if>
			</c:forEach>
		</c:forEach>
		<c:if test="${empty foundTorrentCompleted}">
			<li class="table-row">
				<div class="table-cell">
					No torrents found.
				</div>
			</li>
		</c:if>
	</ul>
	
	<script type="text/javascript">
		sortList($('#list-recently-notified-torrents'), '.table-row', '.sort-timestamp', 'data-timestamp');
		sortList($('#list-recently-completed-torrents'), '.table-row', '.sort-timestamp', 'data-timestamp');
	</script>
</mmt:pageWrapper>