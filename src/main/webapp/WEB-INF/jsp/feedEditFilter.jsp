<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <title>MyMedia</title>
        <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/main.css"/>">
        <script src="<c:url value="/resources/javascript/jquery-1.9.1.js"/>"></script>
        <script>
            $(document).ready(function() {
            	
            	var addToTextarea = function($texarea, string) {
            		$texarea.append(string);
            		$texarea.scrollTop(
            			$texarea[0].scrollHeight - $texarea.height()
                    );
            	};
            	
	            $('#filter_helper_add').click(function(event) {
	                var regexString = $('#filter_helper_regex').val();
	                if (regexString) {
		                regexString = '(?i).*' + regexString.replace(/ /g, '.*') + '.*&#13;&#10;';
	                    var type = $('#filter_helper_type').val();
	                    if (type === "add") {
	                    	addToTextarea($('#filter_add_regex'), regexString);
	                    } else if (type === "ignore") {
                            addToTextarea($('#filter_ignore_regex'), regexString);
	                    }
	                    $('#filter_helper_regex').val('');
                    }
	                event.preventDefault();
	            });
	            
            });
        </script>
    </head>
    <body>
        ${message}
        <br/><br/>
        <a href="${pageContext.request.contextPath}/">Index</a>
        <a href="${pageContext.request.contextPath}/feeds">Feeds</a>
        <a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}">Return to feed</a>
        <br/><br/>
        
            
        <form method="post" action="?">
            
        
        
            <!-- 
            <label for="filter_enabled">Enable filter</label>
            <input type="checkbox" name="filter_enabled" id="filter_enabled" /> value="value" NOT WORKING
            
            <br/><br/>
            
            Action when no match (filter type)
            <select id="filter_type" name="filter_type">
                <option value="add">Add: Add all records, unless matched by ignore regex</option>
                <option selected="selected" value="ignore">Ignore: Only add records matched by add regex</option>
            </select> NOT WORKING
            
            Precedence filter (filter type)
            <select id="filter_precedence" name="filter_type">
                <option value="add">Add: check add filter first</option>
                <option selected="selected" value="ignore">Ignore: Check ignore filter first</option>
            </select> NOT WORKING
             -->
            
            
            <br/><br/>
            <b>Simple:</b>
            <br/>
            <input type="text" id="filter_helper_regex"/>
            <select id="filter_helper_type">
                <option selected="selected" value="add">Add filter</option>
                <option value="ignore">Ignore filter</option>
            </select>
            <button id="filter_helper_add">Add to filter</button>
            
            <br/><br/>
            <b>Advanced:</b>
            <br/>
            
            Example regex: <pre>(?i).*Movie.*Title.*1080p.*<br/>(?i).*TV.*Show.*Title.*S?([0-9]+)E([0-9]+).*720p.*</pre>
            <br/>
            Add regex:
            <textarea id="filter_add_regex" name="filter_add_regex"><c:forEach items="${feed.feedInfo.filterAttributes}" var="filterAttribute"><c:if test="${filterAttribute.filterType == 'add'}">${filterAttribute.filterRegex}${newLine}</c:if></c:forEach></textarea>
            Ignore regex:
            <textarea id="filter_ignore_regex" name="filter_ignore_regex"><c:forEach items="${feed.feedInfo.filterAttributes}" var="filterAttribute"><c:if test="${filterAttribute.filterType == 'ignore'}">${filterAttribute.filterRegex}${newLine}</c:if></c:forEach></textarea>
            
            
            <br/><br/>
            
            <button type="submit">Submit</button>
            
        </form>
        
    </body>
</html>
