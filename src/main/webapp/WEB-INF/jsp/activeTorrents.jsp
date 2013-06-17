<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
	<head>
	    <title>MyMedia</title>
	    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/main.css"/>">
        <script src="<c:url value="/resources/javascript/jquery-1.9.1.js"/>"></script>
        <script src="<c:url value="/resources/javascript/jquery.tmpl.js"/>"></script>
        <script>
            var getActivityDate = function(activityDate) {
        	    return new Date(activityDate * 1000);
        	};
        </script>
	</head>
	<body>
	    ${message}
	    <br/><br/>
        <a href="${pageContext.request.contextPath}/">Index</a>
        <br/>

        <ul id="activeTorrentList" class="table">
            <li id="activeTorrentListHeader" class="table-header-group">
                <div class="table-cell">
                    Name
                </div>
                <div class="table-cell">
                    Activity Date
                </div>
            </li>
        </ul>
        <script id="activeTorrentTemplate" type="text/x-jquery-tmpl">
            <li class="table-row">
                <div class="table-cell">
                    \${name}
                </div>
                <div class="table-cell activityDate" data-timestamp="\${activityDate}">
                    \${getActivityDate(activityDate)}
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
            
            var mylist = $('#activeTorrentList');
            var listitems = mylist.children('li.table-row').get();
            listitems.sort(function(a, b) {
                return $(b).find(".activityDate").attr("data-timestamp").localeCompare($(a).find(".activityDate").attr("data-timestamp"));
            })
            $.each(listitems, function(idx, itm) { mylist.append(itm); });
        </script>
        
        
        
	</body>
</html>
