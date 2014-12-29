<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %> 
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="mmt" tagdir="/WEB-INF/tags" %>
<html>
    <head>
        <title>${title} - Settings</title>
        <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/main.css"/>"></link>
        <script type="text/javascript" src="<c:url value="/resources/javascript/jquery-1.9.1.js"/>"></script>
        <script type="text/javascript" src="<c:url value="/resources/javascript/main.js"/>"></script>
    </head>
    <body>
        <div id="header">
            <mmt:header title="${title}" />
        </div>
        <div id="page-wrapper">
            <div id="menu">
                <mmt:navMenu pageid="settings" />
            </div>
            <div id="page">
                <c:if test='${saved != null}'>
                    <p class="alert">Settings saved, you must <a href="/manager/text/reload?path=/SwordfishSync" target="_blank">restart</a> the application for changes to apply.</p>
                </c:if>
                
	            <h3 class="page-heading">Settings</h3>
	            
                Index page options:
                <mmt:buttonLink url="${pageContext.request.contextPath}/index/download" text="Download Index Page" />
                <mmt:buttonLink url="${pageContext.request.contextPath}/index/revert" text="Revert Default Index Page" />
                <form:form method="post" enctype="multipart/form-data" modelAttribute="uploadedFile" action="${pageContext.request.contextPath}/index/upload"> 
                    <label for="file">Replace index page:</label>
                    <input type="file" name="file" />
                    <input type="submit" value="Upload Index Page" />
                </form:form>
                
                <br/>
	            Options:
	            <mmt:buttonLink url="${pageContext.request.contextPath}/settings/export" text="Export Settings" />
                <mmt:buttonLink url="${pageContext.request.contextPath}/settings/edit" text="Edit/Import Settings" />
	            <br/><br/>
	            Settings stored in ${settingsFile}
	            <br/><br/>
	            <ul class="table" id="table-settings">
	                <c:forEach items="${config.keys}" var="key">
	                    <fmt:message key="settings.${key}" var="fieldLabel"/>
	                    <c:if test='${!fn:startsWith(fieldLabel, "??")}'>
	                        <mmt:tableData
	                           fieldName="${fieldLabel}:"
	                           fieldValue="${config.getProperty(key)}"
	                           fieldNameAttributes="data-name=${key}"/>
	                    </c:if>
	                </c:forEach>
	            </ul>
                <script type="text/javascript">
                    sortList('#table-settings', '.table-row', '.field-name', 'data-name', 'asc');
                </script>
            </div>
        </div>
    </body>
</html>
