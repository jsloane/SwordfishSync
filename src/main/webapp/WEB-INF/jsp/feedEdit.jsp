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
        <a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}">Return to feed</a>
        <br/><br/>
        
        
        
            
        <form method="post" action="?">
        
            
            <!-- <label for="feed_url">URL:</label>
            <input type="text" name="feed_url" id="feed_url" value="${feed.feedInfo.url}" />
            <br/><br/> -->
            <label for="feed_name">Name:</label>
            <input type="text" name="feed_name" id="feed_name" value="${feed.feedInfo.name}" />
            <br/><br/>
            <label for="feed_name">Name:</label>
            <input type="text" name="feed_name" id="feed_name" value="${feed.feedInfo.name}" />
            <br/><br/>

            <br/><br/>
            
            <button type="submit">Submit</button>
            
        </form>
        
    </body>
</html>
