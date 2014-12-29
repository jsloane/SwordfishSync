<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="fieldName" required="true" type="java.lang.String" description="Field name." %>
<%@ attribute name="fieldValue" required="true" type="java.lang.String" description="Field value." %>
<%@ attribute name="fieldClass" required="false" type="java.lang.Boolean" description="Field class." %>
<%@ attribute name="fieldNameAttributes" required="false" type="java.lang.String" description="Field name tag attributes." %>
<%@ attribute name="fieldValueAttributes" required="false" type="java.lang.String" description="Field value tag attributes." %>

<li class="table-row ${fieldClass}">
    <div class="table-cell field-name" ${fieldNameAttributes}>
        ${fieldName}
    </div>
    <div class="table-cell field-value" ${fieldValueAttributes}>
        ${fieldValue}
    </div>
</li>
