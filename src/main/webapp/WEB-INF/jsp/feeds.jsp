<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
	            <mmt:navMenu pageid="feeds" />
	        </div>
	        <div id="page">
                <h3 class="feed-name">Feeds list</h3>
	            Options:
	            <mmt:buttonLink url="${pageContext.request.contextPath}/feeds/add" text="Add feed" />
		        
		        <form method="post" action="?">
			        <div class="table" id="feeds">
			            <div class="table-row">
			                <div class="table-cell"></div>
                            <div class="table-cell"></div>
                            <div class="table-cell">
                                Name
                            </div>
			                <div class="table-cell">
			                    Download Directory
			                </div>
                            <div class="table-cell">
                                Upload Limit
                            </div>
                            <div class="table-cell">
                                Status
                            </div>
                            <div class="table-cell">
                                Last Updated
                            </div>
			            </div>
			            <c:forEach items="${feeds}" var="feed">
			                <div class="table-row">
                                <div class="table-cell">
                                    <c:choose>
                                        <c:when test="${feed.feedInfo.active}">
                                            <span class="feed-active" title="Enabled">&#10004;</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="feed-inactive" title="Disabled">&#10008;</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
			                    <div class="table-cell">
			                        <a href="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}">
			                            View
			                        </a>
			                    </div>
                                <div class="table-cell">
                                    ${feed.feedInfo.name}
                                </div>
			                    <div class="table-cell">
			                        ${feed.feedInfo.downloadDirectory}
			                    </div>
			                    <div class="table-cell">
				                	<c:choose>
										<c:when test="${feed.feedInfo.uploadLimit > 0}">
											${feed.feedInfo.uploadLimit} KB/s
										</c:when>
										<c:otherwise>
											Not set
										</c:otherwise>
									</c:choose>
			                    </div>
                                <div class="table-cell">
					                <c:choose>
					                    <c:when test="${feed.isFeedCurrent()}">
					                        <span class="feed-status-current">Up to date</span>
					                    </c:when>
					                    <c:otherwise>
					                        <c:if test="${not empty feed.statusMessage}">
                                                Error: <span class="feed-status-not-current">${feed.statusMessage}</span>
                                            </c:if>
					                    </c:otherwise>
					                </c:choose>
                                </div>
                                <div class="table-cell">
                                    ${feed.getDateUpdated()}
                                </div>
			                </div>
			            </c:forEach>
		            </div>
		        </form>
	        </div>
        </div>
        
    </body>
</html>
