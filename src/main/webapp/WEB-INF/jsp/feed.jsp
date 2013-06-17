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
        <br/><br/>
        
        <div>
	        <!-- <a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}/edit">
	            Edit feed
	        </a> -->
	        <br/>
	        <a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}/edit/filter">
	            Edit filter
	        </a>
        </div>
        <br/>
        
        
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
                    ${feed.feedInfo.uploadLimit} (if 0 not set)
                </div>
            </div>
        </div>
        
        <br/>
        <br/>
        <br/>
        
        Torrents:
            <div class="table" id="torrent-list">
                <div class="table-row">
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
