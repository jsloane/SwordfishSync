<li class="fieldcontain">
    <span id="${property}-label" class="property-label">
    	<g:message code="${bean.getClass().getName() + '.' + property}" default="${label}" />
    </span>
    <div class="property-value" aria-labelledby="${property}-label">
    	<g:if test="${collection != null}">
    		<ul>
    			<g:each in="${collection}" var="collectionValue">
	    			<g:if test="${collectionValue}">
	    				<ol>
	    					<g:if test="${collectionValue.instanceOf(swordfishsync.FilterAttribute)}">
	    						<g:link controller="filterAttribute" action="show" id="${collectionValue.id}" params="${linkParams}">${collectionValue}</g:link>
	    					</g:if>
	    					<g:else>
	    						${collectionValue}
	    					</g:else>
	    				</ol>
    				</g:if>
    			</g:each>
    		</ul>
    	</g:if>
    	<g:elseif test="${value}">
    		${value}
    	</g:elseif>
    	<g:else>
    		<g:fieldValue bean="${bean}" field="${property}"/>
    	</g:else>
    </div>
</li>
