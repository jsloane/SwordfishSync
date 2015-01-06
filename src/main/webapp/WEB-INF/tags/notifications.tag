<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="systemErrors" required="false" type="java.util.ArrayList" description="System errors." %>
<%@ attribute name="errorMessages" required="false" type="java.util.ArrayList" description="Error messages." %>
<%@ attribute name="successMessages" required="false" type="java.util.ArrayList" description="Success messages." %>

<%-- display any notifications --%>
<c:forEach items="${systemErrors}" var="systemError">
    <div class="alert-box error"><span>System Error: </span>${systemError}</div>
</c:forEach>
<c:forEach items="${errorMessages}" var="errorMessage">
    <div class="alert-box error"><span>Error: </span>${errorMessage}</div>
</c:forEach>
<c:forEach items="${successMessages}" var="successMessage">
    <div class="alert-box success"><span>Success: </span>${successMessage}</div>
</c:forEach>
