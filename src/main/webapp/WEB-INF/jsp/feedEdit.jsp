<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="mmt" tagdir="/WEB-INF/tags" %>

<c:choose>
	<c:when test="${newFeed}">
		<c:set var="pageName" value="Adding Feed" />
	</c:when>
	<c:otherwise>
		<c:set var="pageName" value="Editing ${feed.feedInfo.name}" />
	</c:otherwise>
</c:choose>

<mmt:pageWrapper pageId="feeds" pageName="${pageName}">
	<c:choose>
		<c:when test="${newFeed}">
			<h3 class="feed-name">Add feed</h3>
			Options:
			<mmt:buttonLink url="${pageContext.request.contextPath}/feeds" text="Return to List" />
			<h4>Import feed from file</h4>
			<form:form method="post" enctype="multipart/form-data" modelAttribute="uploadedFile" action="${pageContext.request.contextPath}/feeds/add/upload"> 
				<label for="file">File:</label>
				<input type="file" name="file" />
				<input type="submit" value="Import" />
			</form:form>
			<br/><hr/>
			<h4>Enter new feed details</h4>
		</c:when>
		<c:otherwise>
			<h3 class="feed-name">Editing feed: ${feed.feedInfo.name}</h3>
			Options:
			<mmt:buttonLink url="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}" text="Return to Feed" />
			<br/><br/>
		</c:otherwise>
	</c:choose>
	
	* required field
	<form method="post" action="?">
		<div class="table">
			<mmt:tableInput fieldType="checkbox" fieldName="feed_active" fieldLabel="Active:" fieldChecked="${checkedActive}"/>
			<mmt:tableInput fieldType="text" fieldName="feed_name" fieldLabel="Name:" fieldRequired="${true}" fieldValue="${feed.feedInfo.name}"/>
			<mmt:tableInput fieldType="text" fieldName="feed_url" fieldLabel="URL:" fieldRequired="${true}" fieldValue="${feed.feedInfo.url}" fieldClass="long"/>
			<mmt:tableInput fieldType="select" fieldName="feed_action" fieldLabel="Action:" fieldRequired="${true}"
				fieldValue="${feed.feedInfo.action}"
				fieldValues="${actionOptions}"/>
			<c:if test="${!feed.feedInfo.initilised}">
				<mmt:tableInput fieldType="checkbox" fieldName="feed_initialPopulate" fieldLabel="Treat existing feed entries as new when initilising feed:"
				    fieldChecked="${checkedInitialPopulate}"/>
			</c:if>
			<mmt:tableInput fieldType="text" fieldName="feed_syncInterval" fieldLabel="Sync Interval (minutes):" fieldValue="${feed.feedInfo.syncInterval}"/>
			<mmt:tableInput fieldType="text" fieldName="feed_deleteInterval" fieldLabel="Delete Interval (days):" fieldValue="${feed.feedInfo.deleteInterval}"/>
			<mmt:tableInput fieldType="text" fieldName="feed_downloadDirectory" fieldLabel="Download Directory:" fieldValue="${feed.feedInfo.downloadDirectory}"/>
			<mmt:tableInput fieldType="checkbox" fieldName="feed_determineSubDirectory" fieldLabel="Determine Sub Directory:" fieldChecked="${checkedDetermineSubDirectory}"/>
			<mmt:tableInput fieldType="checkbox" fieldName="feed_removeTorrentOnComplete" fieldLabel="Remove Torrent On Complete:" fieldChecked="${checkedRemoveTorrentOnComplete}"/>
			<mmt:tableInput fieldType="checkbox" fieldName="feed_extractRars" fieldLabel="Extract Rars:" fieldChecked="${checkedExtractRars}"/>
			<mmt:tableInput fieldType="text" fieldName="feed_uploadLimit" fieldLabel="Torrent Upload Limit (KB/s):" fieldValue="${feed.feedInfo.uploadLimit}"/>
			<mmt:tableInput fieldType="text" fieldName="feed_notifyEmail" fieldLabel="Notify Email:" fieldValue="${feed.feedInfo.notifyEmail}"/>
			<mmt:tableInput fieldType="text" fieldName="feed_detailsUrlValueFromRegex"
			    fieldLabel="detailsUrlValueFromRegex:"
			    fieldHelp="eg: http://localhost/(\d+)/.*"
			    fieldValue="${feed.feedInfo.detailsUrlValueFromRegex}"
			    fieldClass="long" />
			<mmt:tableInput fieldType="text" fieldName="feed_detailsUrlFormat"
			    fieldLabel="detailsUrlFormat:"
			    fieldHelp="eg: http://localhost/details?id={regex-value}"
			    fieldValue="${feed.feedInfo.detailsUrlFormat}"
			    fieldClass="long" />
		</div>
		<br/>
		
		<button type="submit">Submit</button>
		<c:choose>
			<c:when test="${newFeed}">
				<mmt:buttonLink url="${pageContext.request.contextPath}/feeds" text="Cancel" />
			</c:when>
			<c:otherwise>
				<mmt:buttonLink url="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}" text="Cancel" />
			</c:otherwise>
		</c:choose>
	</form>
</mmt:pageWrapper>
