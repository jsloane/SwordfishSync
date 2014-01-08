<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="mmt" tagdir="/WEB-INF/tags" %>
<html>
	<head>
	    <title>MyMedia</title>
	    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/main.css"/>"></link>
        <script src="<c:url value="/resources/javascript/jquery-1.9.1.js"/>"></script>
        <script src="<c:url value="/resources/javascript/jquery.tmpl.js"/>"></script>
        <script>
            var getActivityDate = function(activityDate) {
        	    return new Date(activityDate * 1000);
        	};
        </script>
	</head>
	<body>
        <div id="header">
            <mmt:header title="${title}" />
        </div>
        <div id="page-wrapper">
            <div id="menu">
                <mmt:navMenu pageid="active-torrents" />
            </div>
            <div id="page">
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
                            \${getActivityDate(activityDate)}
                        </div>
                        <div class="table-cell">
                            <progress class="torrent-progress" value="\${percentDone}" max="1"></progress>
                        </div>
                    </li>
                </script>
		        <script>
		            var activeTorrents = [];
		            <c:forEach items="${activeTorrents}" var="activeTorrent">
		                activeTorrents.push(${activeTorrent.JSONObject});
		            </c:forEach>
		            $("#activeTorrentTemplate").tmpl(activeTorrents)
		                .insertAfter("#activeTorrentListHeader");
		            
		            var myList = $('#activeTorrentList');
		            var listItems = myList.children('li.table-row').get();
		            listItems.sort(function(a, b) {
		                return $(b).find(".activityDate").attr("data-timestamp").localeCompare($(a).find(".activityDate").attr("data-timestamp"));
		            });
		            $.each(listItems, function(idx, itm) {
		            	myList.append(itm);
		            });
		        </script>
            </div>
        </div>
	</body>
</html>
