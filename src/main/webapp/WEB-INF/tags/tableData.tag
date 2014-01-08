<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="fieldName" required="true" type="java.lang.String" description="Field name." %>
<%@ attribute name="fieldData" required="true" type="java.lang.String" description="Field data." %>
<%@ attribute name="fieldAdvanced" required="false" type="java.lang.Boolean" description="Field advanced." %>

<div class="table-row ${advancedClass}">
    <div class="table-cell">
        ${fieldName}
    </div>
    <div class="table-cell">
        ${fieldData}
    </div>
</div>
