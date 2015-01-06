<%@ tag description="Page template." pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mmt" tagdir="/WEB-INF/tags" %>
<%@ attribute name="pageId" required="true" type="java.lang.String" description="Page ID." %>
<%@ attribute name="pageName" required="true" type="java.lang.String" description="Page Name." %>

<html>
    <head>
        <title>${title} - ${pageName}</title>
        <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/main.css"/>"></link>
        <script type="text/javascript" src="<c:url value="/resources/javascript/jquery-1.9.1.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/javascript/jquery.tmpl.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/javascript/main.js"/>"></script>
    </head>
    <body>
        <div id="header">
            <h1>${title}</h1>
        </div>
        <div id="page-wrapper">
            <div id="menu">
                <mmt:navMenu pageid="${pageId}" />
            </div>
            <div id="page">
                <mmt:notifications systemErrors="${systemErrors}" errorMessages="${errorMessages}" successMessages="${successMessages}" />
                <jsp:doBody/>
            </div>
        </div>
    </body>
</html>