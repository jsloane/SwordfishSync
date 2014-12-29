<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="mmt" tagdir="/WEB-INF/tags" %>
<html>
    <head>
        <c:choose>
            <c:when test="${newTorrent}">
                <title>${title} - Adding Torrent</title>
            </c:when>
            <c:otherwise>
                <title>${title} - Editing {torrentInfo.name}</title>
            </c:otherwise>
        </c:choose>
        <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/main.css"/>"></link>
    </head>
    <body>
        <div id="header">
            <mmt:header title="${title}" />
        </div>
        <div id="page-wrapper">
            <div id="menu">
                <mmt:navMenu pageid="feeds" />
            </div>
            <div id="page">
                <c:choose>
                    <c:when test="${newTorrent}">
                        <h3 class="feed-name">Add torrent</h3>
                        Options:
                        <mmt:buttonLink url="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}" text="Return to Feed" />
                        <h4>Enter new torrent details</h4>
                    </c:when>
                    <c:otherwise>
                        <h3 class="feed-name">Editing torrent: TorrentName</h3>
                        Options:
                        <mmt:buttonLink url="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}" text="Return to Feed" />
                        <br/><br/>
                    </c:otherwise>
                </c:choose>
                
                <form method="post" action="?">
                    <div class="table">
                        <mmt:tableInput fieldType="text" fieldName="torrent_url" fieldLabel="Link:" fieldRequired="${true}" fieldClass="long"/>
                    </div>
                    <br/>
                    
                    <button type="submit">Submit</button>
                    
                    <c:choose>
                        <c:when test="${newTorrent}">
                            <mmt:buttonLink url="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}" text="Cancel" />
                        </c:when>
                        <c:otherwise>
                            <mmt:buttonLink url="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}/torrent/${newTorrent.id}" text="Cancel" />
                        </c:otherwise>
                    </c:choose>
                </form>
            </div>
        </div>
    </body>
</html>
