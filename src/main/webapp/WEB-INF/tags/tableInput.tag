<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="fieldType" required="true" type="java.lang.String" description="Field type." %>
<%@ attribute name="fieldName" required="true" type="java.lang.String" description="Field name." %>
<%@ attribute name="fieldLabel" required="true" type="java.lang.String" description="Field value." %>
<%@ attribute name="fieldSize" required="false" type="java.lang.String" description="Field size." %>
<%@ attribute name="fieldHelp" required="false" type="java.lang.String" description="Field help." %>
<%@ attribute name="fieldValue" required="false" type="java.lang.String" description="Field value." %>
<%@ attribute name="fieldValues" required="false" type="java.util.Map" description="Field values." %>
<%@ attribute name="fieldChecked" required="false" type="java.lang.String" description="Field value." %>
<%@ attribute name="fieldRequired" required="false" type="java.lang.Boolean" description="Field required." %>
<%@ attribute name="fieldClass" required="false" type="java.lang.String" description="Field class." %>
<%@ attribute name="fieldAttributes" required="false" type="java.lang.String" description="Field tag attributes." %>
<%@ attribute name="fieldNameAttributes" required="false" type="java.lang.String" description="Field name tag attributes." %>
<%@ attribute name="fieldValueAttributes" required="false" type="java.lang.String" description="Field value tag attributes." %>

<li class="table-row ${fieldClass}" ${fieldAttributes}>
    <div class="table-cell field-name" ${fieldNameAttributes}>
        <label for="${fieldName}">${fieldLabel}</label><c:if test="${fieldRequired}">*</c:if>
    </div>
    <div class="table-cell field-value" ${fieldValueAttributes}>
        <c:choose>
            <c:when test="${fieldType eq 'checkbox'}">
                <input type="${fieldType}" name="${fieldName}" id="${fieldName}" ${fieldChecked}/>
                <input type="hidden" name="${fieldName}" id="${fieldName}_hidden" value="off" />
            </c:when>
            <c:when test="${fieldType eq 'select'}">
                <select name="${fieldName}" id="${fieldName}">
                    <c:forEach items="${fieldValues}" var="selectOption">
                        <option value="${selectOption.key}"<c:if test="${fieldValue  == selectOption.key}"> selected="selected"</c:if>>${selectOption.value}</option>
                    </c:forEach>
                </select>
            </c:when>
            <c:when test="${fieldType eq 'textarea'}">
                <textarea name="${fieldName}" class="${fieldClass}">${fieldValue}</textarea>
            </c:when>
            <c:otherwise>
                <input type="${fieldType}" class="${fieldClass}" name="${fieldName}" id="${fieldName}" value="${fieldValue}" size="${fieldSize}"/>
            </c:otherwise>
        </c:choose>
        <p class="help-text">${fieldHelp}</p>
    </div>
</li>
