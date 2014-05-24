<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mmt" tagdir="/WEB-INF/tags" %>
<html>
	<head>
	    <title>${title}</title>
	    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/main.css"/>"></link>
        <script src="<c:url value="/resources/javascript/jquery-1.9.1.js"/>"></script>
	</head>
	<body>
	    <div id="header">
	        <mmt:header title="${title}" />
	    </div>
        <div id="page-wrapper">
		    <div id="menu">
		        <mmt:navMenu pageid="index" />
		    </div>
		    <div id="page">
		        
		        <h3>Torrents downloading</h3>
                <div class="table">
                    <div class="table-row" id="torrent-header">
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
                    </div>
                    <c:forEach items="${feeds}" var="feed">
                        <c:forEach items="${feed.feedInfo.feedTorrents}" var="torrent">
                            <c:if test="${torrent != null && torrent.status == torrentDownloading}">
                                <c:set var="foundTorrentDownloading" value="true" />
                                <div class="table-row">
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
                                </div>
                            </c:if>
                        </c:forEach>
                    </c:forEach>
                    <c:if test="${empty foundTorrentDownloading}">
	                    <div class="table-row">
	                        <div class="table-cell">
	                            No torrents downloading.
	                        </div>
	                    </div>
                    </c:if>
                </div>
                
                <h3>Recently completed torrents</h3>
                <div class="table" id="recent-torrents">
                    <div class="table-row" id="torrent-header">
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
                    </div>
                    <c:forEach items="${feeds}" var="feed">
                        <c:forEach items="${feed.feedInfo.feedTorrents}" var="torrent">
                            <c:if test="${torrent != null && (torrent.status == torrentNotifiedCompleted || torrent.status == torrentCompleted)}">
                                <c:set var="foundTorrentCompleted" value="true" />
                                <div class="table-row">
                                    <div class="table-cell">
                                        ${feed.feedInfo.name}
                                    </div>
                                    <div class="table-cell">
                                        ${torrent.name}
                                    </div>
                                    <div class="table-cell dateCompleted" data-timestamp="${torrent.dateCompleted.getTime()}">
                                        ${torrent.dateCompleted}
                                    </div>
                                    <div class="table-cell">
                                        ${torrent.getDownloadDirectoryLocation()}
                                    </div>
                                </div>
                            </c:if>
                        </c:forEach>
                    </c:forEach>
                    <c:if test="${empty foundTorrentCompleted}">
                        <div class="table-row">
                            <div class="table-cell">
                                No torrents found.
                            </div>
                        </div>
                    </c:if>
                </div>
                
                <script>
                    var myList = $('#recent-torrents');
                    var listItems = myList.children('.table-row').get();
                    listItems.sort(function(a, b) {
                        return $(b).find(".dateCompleted").attr("data-timestamp").localeCompare($(a).find(".dateCompleted").attr("data-timestamp"));
                    });
                    $.each(listItems, function(idx, itm) {
                    	myList.append(itm);
                    });
                </script>
                
	        </div>
        </div>
	</body>
</html>
