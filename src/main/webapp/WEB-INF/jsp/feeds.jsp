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
        <br/>
        <a href="${pageContext.request.contextPath}/feeds/add">Add feed</a>
        <br/>
        <!-- <a href="${pageContext.request.contextPath}/feeds/add">Add new feed</a>
        <br/> -->
        <br/>
        
        <form method="post" action="?">
	        <div class="table" id="feeds">
	            <div class="table-row">
	                <div class="table-cell"></div>
	                <div class="table-cell">
	                    Name
	                </div>
	                <div class="table-cell">
	                    Download directory
	                </div>
	                <div class="table-cell">
	                    Upload limit
	                </div>
	                <div class="table-cell">
	                    URL
	                </div>
	            </div>
	            <c:forEach items="${feeds}" var="feed">
	                <div class="table-row">
	                    <div class="table-cell">
	                        <a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}">
	                            View
	                        </a>
	                    </div>
	                    <div class="table-cell">
	                        ${feed.feedInfo.name}
	                    </div>
	                    <div class="table-cell">
	                        ${feed.feedInfo.downloadDirectory}
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
	                    <div class="table-cell">
	                        ${feed.feedInfo.url}
	                    </div>
	                </div>
	            </c:forEach>
            </div>
        </form>
        
    </body>
</html>
