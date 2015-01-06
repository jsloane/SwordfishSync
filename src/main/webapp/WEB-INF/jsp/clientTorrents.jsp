<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="mmt" tagdir="/WEB-INF/tags" %>

<mmt:pageWrapper pageId="active-torrents" pageName="Client Torrents">
	<ul id="activeTorrentList" class="table">
		<li id="activeTorrentListHeader" class="table-header-group">
			<div class="table-cell">
			    Name
			</div>
			<div class="table-cell">
			    Status
			</div>
			<div class="table-cell">
			    Activity Date
			</div>
			<div class="table-cell">
			    Progress
			</div>
		</li>
	</ul>
	<script id="activeTorrentTemplate" type="text/x-jquery-tmpl">
        <li class="table-row">
            <div class="table-cell">
                \${name}
            </div>
            <div class="table-cell">
                \${statusTitle}
            </div>
            <div class="table-cell activityDate" data-timestamp="\${activityDate}">
                \${getDate(activityDate)}
            </div>
            <div class="table-cell">
                <progress class="torrent-progress" value="\${percentDone}" max="1"></progress>
            </div>
        </li>
	</script>
	<script type="text/javascript">
		var activeTorrents = [];
		<c:forEach items="${activeTorrents}" var="activeTorrent">
		    activeTorrents.push(${activeTorrent.JSONObject});
		</c:forEach>
		$("#activeTorrentTemplate").tmpl(activeTorrents)
		    .insertAfter("#activeTorrentListHeader");
		
		sortList($('#activeTorrentList'), 'li.table-row', '.activityDate', 'data-timestamp');
	</script>
</mmt:pageWrapper>
