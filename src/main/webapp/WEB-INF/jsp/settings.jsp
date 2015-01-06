<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %> 
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="mmt" tagdir="/WEB-INF/tags" %>

<mmt:pageWrapper pageId="settings" pageName="Settings">
	<h3 class="page-heading">Settings</h3>
	
	Index page options:
	<mmt:buttonLink url="${pageContext.request.contextPath}/index/download" text="Download Index Page" />
	<mmt:buttonLink url="${pageContext.request.contextPath}/index/revert" text="Revert Default Index Page" />
	<form:form method="post" enctype="multipart/form-data" modelAttribute="uploadedFile" action="${pageContext.request.contextPath}/index/upload"> 
		<label for="file">Replace index page:</label>
		<input type="file" name="file" />
		<input type="submit" value="Upload Index Page" />
	</form:form>
	
	Options:
	<mmt:buttonLink url="${pageContext.request.contextPath}/settings/export" text="Export Settings" />
	<mmt:buttonLink url="${pageContext.request.contextPath}/settings/edit" text="Edit/Import Settings" />
	
	<ul class="table table-view" id="table-config">
		<c:set var="configDepth" value="0" />
		<c:forEach items="${config.rootNode.children}" var="configNode">
			<mmt:configNode config="${config}" configNode="${configNode}" configNodeKey="" depth="${1}" editable="${false}" />
		</c:forEach>
	</ul>
	<script type="text/javascript">
		$('#table-config ul').each(
			function(index) {
				sortList($(this), 'li.table-row', null, 'data-order', 'asc');
			}
		);
		sortList($('#table-config'), 'li.table-row', null, 'data-order', 'asc');
	</script>
	
	<p class="settings-location">
		Settings stored in <b>${configFile}</b> and <b>${propertiesFile}</b>
	</p>
</mmt:pageWrapper>