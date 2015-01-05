<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mmt" tagdir="/WEB-INF/tags" %>
<%@ attribute name="config" required="true" type="org.apache.commons.configuration.XMLConfiguration" description="Config." %>
<%@ attribute name="configNode" required="true" type="org.apache.commons.configuration.tree.ConfigurationNode" description="Config node." %>
<%@ attribute name="configNodeKey" required="true" type="java.lang.String" description="Config node key." %>
<%@ attribute name="editable" required="true" type="java.lang.Boolean" description="Editable." %>
<%@ attribute name="depth" required="true" type="java.lang.Integer" description="Depth." %>

<c:set var="fieldType" value="" />
<c:set var="order" value="0" />
<c:set var="label" value="" />
<c:set var="configNodeKey" value="${config.expressionEngine.nodeKey(configNode, configNodeKey)}" />

<c:forEach items="${configNode.attributes}" var="configNodeAttribute">
    <c:if test="${configNodeAttribute.name == 'fieldtype'}">
        <c:set var="fieldType" value="${configNodeAttribute.value}" />
    </c:if>
    <c:if test="${configNodeAttribute.name == 'order'}">
        <c:set var="order" value="${configNodeAttribute.value}" />
    </c:if>
    <c:if test="${configNodeAttribute.name == 'label'}">
        <c:set var="label" value="${configNodeAttribute.value}" />
    </c:if>
</c:forEach>

<c:choose>
	<c:when test="${fieldType eq 'section'}">
        <%-- Start a section list with the heading --%>
        <li class="table-row" data-order="${order}">
        <ul class="table">
            <c:if test="${not empty label}">
				<li class="table-row" data-order="0">
					<h${depth}>${label}</h${depth}>
				</li>
			</c:if>
			<c:forEach items="${configNode.children}" var="configNodeChild">
				<mmt:configNode
				    config="${config}"
				    configNode="${configNodeChild}"
				    configNodeKey="${configNodeKey}"
				    depth="${depth + 1}"
				    editable="${editable}" />
			</c:forEach>
		</ul>
		</li>
	</c:when>
	<c:when test="${fieldType eq 'text' or fieldType eq 'textarea' or fieldType eq 'password' or fieldType eq 'checkbox'}">
        <c:choose>
            <c:when test="${editable}">
				<c:set var="fieldChecked" value="" />
				<c:if test='${fieldType == "checkbox" and configNode.value == "true"}'>
				    <c:set var="fieldChecked" value='checked="checked"' />
				</c:if>
				<mmt:tableInput
					fieldType="${fieldType}"
					fieldName='${fn:replace(configNodeKey, "/", ".")}'
					fieldLabel="${label}:"
					fieldValue="${configNode.value}"
					fieldChecked="${fieldChecked}"
					fieldAttributes='data-order="${order}"' />
            </c:when>
		    <c:otherwise>
                <%-- Add to section list --%>
                <c:set var="fieldValue" value="${configNode.value}" />
                <c:if test="${fieldType eq 'password'}">
                    <c:set var="fieldValue" value="********" />
                </c:if>
		        <mmt:tableData
		            fieldName="${label}:"
		            fieldValue="${fieldValue}"
		            fieldAttributes='data-order="${order}"' />
		    </c:otherwise>
        </c:choose>
	</c:when>
	<c:otherwise>
		<c:forEach items="${configNode.children}" var="configNodeChild">
            <%-- Check for child sections/items --%>
			<mmt:configNode
                config="${config}"
                configNode="${configNodeChild}"
                configNodeKey="${configNodeKey}"
                depth="${depth + 1}"
                editable="${editable}" />
		</c:forEach>
	</c:otherwise>
</c:choose>
