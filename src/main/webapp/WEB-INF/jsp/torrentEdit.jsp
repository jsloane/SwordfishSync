<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="mmt" tagdir="/WEB-INF/tags" %>

<c:choose>
	<c:when test="${newTorrent}">
		<c:set var="pageName" value="Add Torrent" />
	</c:when>
	<c:otherwise>
		<c:set var="pageName" value="Editing torrent: ${torrentInfo.name}" />
	</c:otherwise>
</c:choose>

<mmt:pageWrapper pageId="feeds" pageName="${pageName}">
	<h3 class="feed-name">${pageName}</h3>
	Options:
	<mmt:buttonLink url="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}" text="Return to Feed" />
	<br/><br/>
	
	<form method="post" action="?">
		<div class="table">
			<mmt:tableInput fieldType="textarea" fieldName="torrent_urls" fieldLabel="Enter torrent/magnet links:" fieldRequired="${true}" fieldClass="long tall" fieldHelp="Enter each link on a new line." />
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
</mmt:pageWrapper>