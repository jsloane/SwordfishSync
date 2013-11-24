<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>MyMedia</title>
        <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/main.css"/>">
    </head>
    <body>
        ${message}
        <br/><br/>
        <a href="${pageContext.request.contextPath}/">Index</a>
        <a href="${pageContext.request.contextPath}/feeds">Feeds</a>
        <br/>
        <a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}/edit">Edit</a>
        <a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}/export">Export</a>
        <a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}/delete">Delete</a>
        <br/><br/>
        
        
        
        <div class="table" id="feed">
            <div class="table-row">
                <div class="table-cell">
                    Name:
                </div>
                <div class="table-cell">
                    ${feed.feedInfo.name}
                </div>
            </div>
            <div class="table-row">
                <div class="table-cell">
                    URL:
                </div>
                <div class="table-cell">
                    ${feed.feedInfo.url}
                </div>
            </div>
            <div class="table-row">
                <div class="table-cell">
                    initilised:
                </div>
                <div class="table-cell">
                    ${feed.feedInfo.initilised}
                </div>
            </div>
            <div class="table-row">
                <div class="table-cell">
                    Date created:
                </div>
                <div class="table-cell">
                    ${feed.feedInfo.created}
                </div>
            </div>
            <div class="table-row">
                <div class="table-cell">
                    Date updated:
                </div>
                <div class="table-cell">
                    ${feed.feedInfo.updated}
                </div>
            </div>
            <div class="table-row">
                <div class="table-cell">
                    Download directory:
                </div>
                <div class="table-cell">
                    ${feed.feedInfo.downloadDirectory}
                </div>
            </div>
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
                    <a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}/edit/filter">
                        Edit filter
                    </a>
                </div>
            </div>
        </div>
        
        <br/>
        <br/>
        <br/>
        
        Torrents:
            <div class="table" id="torrent-list">
                <div class="table-row">
                    <div class="table-cell"></div>
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
                <c:forEach items="${feed.feedInfo.feedTorrents}" var="torrent">
                    <div class="table-row">
                        <div class="table-cell">
	                        <c:if test="${torrent.status == torrentNotAdded || torrent.status == torrentNotifiedNotAdded || torrent.status == torrentSkipped}">
                                <a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}/torrent/${torrent.id}/download">Download</a>
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
                </c:forEach>
            </div>
        
    </body>
</html>
