<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="pageid" required="true" type="java.lang.String" description="Page ID" %>

<ul>
	<li <c:if test="${pageid  == 'index'}"> class="active"</c:if>>
        <a href="${pageContext.request.contextPath}/">Status</a>
	</li>
    <li <c:if test="${pageid  == 'feeds'}"> class="active"</c:if>>
        <a href="${pageContext.request.contextPath}/feeds">Feeds</a>
        <!-- sub menu could go here -->
    </li>
    <li <c:if test="${pageid  == 'active-torrents'}"> class="active"</c:if>>
	    <a href="${pageContext.request.contextPath}/client-torrents">Client torrents</a>
	</li>
    <li <c:if test="${pageid  == 'settings'}"> class="active"</c:if>>
        <a href="${pageContext.request.contextPath}/settings">Settings</a>
	</li>
</ul>
