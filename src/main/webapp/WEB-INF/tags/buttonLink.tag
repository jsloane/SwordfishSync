<%@ attribute name="url" required="true" type="java.lang.String" description="URL" %>
<%@ attribute name="text" required="true" type="java.lang.String" description="Text" %>

<!-- 
<%-- do not use this inside a form --%>
<form method="get" action="${url}" class="button-link">
    <button type="submit">${text}</button>
</form> -->
<!-- <a class="button" href="${url}">${text}</a> -->
<a href="${url}"><button type="button">${text}</button></a><!-- note: not valid markup -->
