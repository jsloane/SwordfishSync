<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
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
        <c:choose>
            <c:when test="${!newFeed}">
                <a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}">Return to feed</a>
            </c:when>
        </c:choose>
        <br/>
        
        
        <c:choose>
            <c:when test="${newFeed}">
                <h3>Adding feed</h3>
		        <form:form method="post" enctype="multipart/form-data" modelAttribute="uploadedFile" action="${pageContext.request.contextPath}/feeds/add/upload"> 
		            <label for="file">File:</label>
		            <input type="file" name="file" />
		            <input type="submit" value="Upload" />
		        </form:form>
            </c:when>
            <c:otherwise>
                <h3>Editing feed: ${feed.feedInfo.name}</h3>
            </c:otherwise>
        </c:choose>
        
        <c:choose>
		    <c:when test="${feed.fromPropertiesFile}">
			     Feed set in properties file, unable to edit. Should still list fields, just disabled.
			     set class {formDisabled} in controller, put in form, all inputs disabled, and remove this choose
		    </c:when>
		    <c:otherwise>
			
        <%--<form:form method="POST" modelAttribute="feedInfo" action="?">
        
            <form:input path="name" value="${feedInfo.name}" />
            
            <input type="submit" value="Submit">
        </form:form> --%>
            
                * required field
		        <form method="post" action="?">
		            <label for="feed_name">Name:</label>
		            <input type="text" name="feed_name" id="feed_name" value="${feed.feedInfo.name}" />*
		            <br/><br/>
		            <label for="feed_url">URL:</label>
		            <input type="text" name="feed_url" id="feed_url" value="${feed.feedInfo.url}" />*
		            <br/><br/>
		            <label for="feed_initialPopulate">initialPopulate (treat existing RSS entries as new when adding feed):</label>
		            <input type="checkbox" name="feed_initialPopulate" id="feed_initialPopulate" ${checkedInitialPopulate} />
		            <br/><br/>
		            <label for="feed_syncInterval">syncInterval:</label>
		            <input type="text" name="feed_syncInterval" id="feed_syncInterval" value="${feed.feedInfo.syncInterval}" />
		            <br/><br/>
		            <label for="feed_deleteInterval">deleteInterval:</label>
		            <input type="text" name="feed_deleteInterval" id="feed_deleteInterval" value="${feed.feedInfo.deleteInterval}" />
		            <br/><br/>
		            <label for="feed_downloadDirectory">downloadDirectory:</label>
		            <input type="text" name="feed_downloadDirectory" id="feed_downloadDirectory" value="${feed.feedInfo.downloadDirectory}" />
		            <br/><br/>
		            <label for="feed_uploadLimit">uploadLimit:</label>
		            <input type="text" name="feed_uploadLimit" id="feed_uploadLimit" value="${feed.feedInfo.uploadLimit}" />
		            <br/><br/>
		            <label for="feed_notifyEmail">notifyEmail:</label>
		            <input type="text" name="feed_notifyEmail" id="feed_notifyEmail" value="${feed.feedInfo.notifyEmail}" />
		            <br/><br/>
                    <label for="feed_removeTorrentOnComplete">removeTorrentOnComplete:</label>
                    <input type="checkbox" name="feed_removeTorrentOnComplete" id="feed_removeTorrentOnComplete" ${checkedRemoveTorrentOnComplete}" />
                    <br/><br/>
                    <label for="feed_extractRars">extractRars:</label>
                    <input type="checkbox" name="feed_extractRars" id="feed_extractRars" ${checkedExtractRars}" />
                    <br/><br/>
                    <label for="feed_determineSubDirectory">determineSubDirectory:</label>
                    <input type="checkbox" name="feed_determineSubDirectory" id="feed_determineSubDirectory" ${checkedDetermineSubDirectory}" />
                    <br/><br/>
		            
		            <button type="submit">Submit</button>
		            
		        </form>
            </c:otherwise>
        </c:choose>
    </body>
</html>
