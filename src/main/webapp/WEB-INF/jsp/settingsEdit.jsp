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
        <title>MyMedia</title>
        <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/main.css"/>"></link>
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
                        <h3 class="page-heading">Edit Settings</h3>
                        Options:
                        <mmt:buttonLink url="${pageContext.request.contextPath}/settings" text="Return to Settings" />
                        
                        <h4>Import Settings from file</h4>
                        <form:form method="post" enctype="multipart/form-data" modelAttribute="uploadedFile" action="${pageContext.request.contextPath}/settings/upload"> 
                            <label for="file">File:</label>
                            <input type="file" name="file" />
                            <input type="submit" value="Import" />
                        </form:form>
                        <br/><hr/>
                        
                        
                        <h4>Enter Settings</h4>
                        <form method="post" action="?">
                            <div class="table">
                                <c:forEach items="${config.keys}" var="key">
                                    <fmt:message key="settings.${key}" var="fieldLabel"/>
                                    <c:if test='${!fn:startsWith(fieldLabel, "??")}'>
                                        <mmt:tableInput fieldType="textarea" fieldName="${key}" fieldLabel="${fieldLabel}" fieldValue="${config.getProperty(key)}"/>
                                    </c:if>
                                </c:forEach>
                            </div>
                            
                                    
                            <button type="submit">Submit</button>
                            <mmt:buttonLink url="${pageContext.request.contextPath}/settings" text="Cancel" />
                            
                        </form>
            </div>
        </div>
    </body>
</html>
