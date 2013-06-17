<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>  
<html>
	<head>
	    <title>MyMedia</title>
	    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/main.css"/>">
	</head>
	<body>
	    ${message}
	    <br/><br/>
        <a href="${pageContext.request.contextPath}/active-torrents">Active torrents</a>
        <a href="${pageContext.request.contextPath}/feeds">Feeds</a>
	    <br/><br/>
	    <!-- 
	    Number of feeds: ${fn:length(feeds)}
	    <br/>
	    
	    
        <c:forEach items="${feeds}" var="feed">
            <div class="table" id="feed">
                <div class="table-row">
                    <div class="table-cell">
                        ${feed.feedInfo.name}
                    </div>
                    <div class="table-cell">
                        <a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}">
                            View
                        </a>
                    </div>
                </div>
            </div>
        </c:forEach>
	    
	    
	    <c:forEach items="${feeds}" var="feed">
            <div class="table" id="feed">
		        <div class="table-row">
	                <div class="table-cell">
	                    ${feed.feedInfo.name}
	                </div>
	                <div class="table-cell">
	                    ${feed.feedInfo.downloadDirectory}
	                </div>
	                <div class="table-cell">
	                    ${feed.feedInfo.uploadLimit}
	                </div>
	                <div class="table-cell">
	                    ${feed.feedInfo.url}
	                </div>
	                <div class="table-cell">
	                    <a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}">
	                        View
	                    </a>
	                </div>
	            </div>
            </div>
            <div class="table" id="torrent-list">
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
	    </c:forEach>
	     -->
	    
	</body>
</html>
