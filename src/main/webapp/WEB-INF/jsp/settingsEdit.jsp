<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %> 
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="mmt" tagdir="/WEB-INF/tags" %>

<mmt:pageWrapper pageId="settings" pageName="Edit Settings">
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
	
	<form method="post" action="?">
		<ul class="table table-edit" id="table-config">
			<c:set var="configDepth" value="0" />
			<c:forEach items="${config.rootNode.children}" var="configNode">
				<mmt:configNode config="${config}" configNode="${configNode}" configNodeKey="" depth="${1}" editable="${true}" />
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
		<button type="submit">Submit</button>
		<mmt:buttonLink url="${pageContext.request.contextPath}/settings" text="Cancel" />
	</form>
</mmt:pageWrapper>