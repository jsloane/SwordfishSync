<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mmt" tagdir="/WEB-INF/tags" %>

<mmt:pageWrapper pageId="feeds" pageName="Editing Feed Filter - ${feed.feedInfo.name}">
	<h3 class="feed-name">Editing filter for feed ${feed.feedInfo.name}</h3>
	Options:
	<mmt:buttonLink url="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}" text="Return to Feed" />
	<br/><br/>
	
	<form method="post" action="?">
		<label for="filter_enabled">Enable filter:</label>
		<input type="checkbox" name="filter_enabled" id="filter_enabled" ${filterEnabled} />
		<br/>
		<label for="filter_removeAddFilterOnMatch">Remove 'Add' entries on filter match:</label>
		<input type="checkbox" name="filter_removeAddFilterOnMatch" id="filter_removeAddFilterOnMatch" ${removeAddFilterOnMatch} />
		<br/>
		
		<label for="filter_action">Filter Action:</label>
		<select id="filter_action" name="filter_action">
			<option value="ignore" ${actionSelectedIgnore}>Ignore: Only add records matched by add regex</option>
			<option value="add" ${actionSelectedAdd}>Add: Add all records, unless matched by ignore regex</option>
		</select>
		
		<br/>
		
		<label for="filter_precedence">Filter Precedence:</label>
		<select id="filter_precedence" name="filter_precedence">
			<option value="ignore" ${precedenceSelectedIgnore}>Ignore: Check ignore filter first</option>
			<option value="add" ${precedenceSelectedAdd}>Add: check add filter first</option>
		</select>
		
		<br/><br/>
		<b>Simple:</b>
		<br/>
		<input type="text" id="filter_helper_regex"/>
		<select id="filter_helper_type">
			<option selected="selected" value="add">Add filter</option>
			<option value="ignore">Ignore filter</option>
		</select>
		<button id="filter_helper_add">Add to filter</button>
		
		<br/><br/>
		<b>Advanced:</b>
		<br/>
		
		Example regex: <pre>(?i).*Movie.*Title.*1080p.*<br/>(?i).*TV.*Show.*Title.*S?([0-9]+)E([0-9]+).*720p.*</pre>
		<div class="filter-textareas">
			<div class="filter-textarea float-left">
				Add regex:<br/>
				<textarea id="filter_add_regex" name="filter_add_regex"><c:forEach items="${feed.feedInfo.filterAttributes}" var="filterAttribute"><c:if test="${filterAttribute.filterType == 'add'}">${filterAttribute.filterRegex}${newLine}</c:if></c:forEach></textarea>
			</div>
			<div class="filter-textarea float-right">
				Ignore regex:<br/>
				<textarea id="filter_ignore_regex" name="filter_ignore_regex"><c:forEach items="${feed.feedInfo.filterAttributes}" var="filterAttribute"><c:if test="${filterAttribute.filterType == 'ignore'}">${filterAttribute.filterRegex}${newLine}</c:if></c:forEach></textarea>
			</div>
		</div>
		
		<br/>
		
		<button type="submit">Submit</button>
		<mmt:buttonLink url="${pageContext.request.contextPath}/feeds/${feed.feedInfo.id}" text="Cancel" />
	</form>
	
	<script type="text/javascript">
		$(document).ready(function() {
			var addToTextarea = function($texarea, string) {
				$texarea.append(string);
				$texarea.scrollTop(
					$texarea[0].scrollHeight - $texarea.height()
				);
			};
			
			$('#filter_helper_add').click(function(event) {
				var regexString = $('#filter_helper_regex').val();
				if (regexString) {
					regexString = '(?i).*' + regexString.replace(/ /g, '.*') + '.*&#13;&#10;';
					var type = $('#filter_helper_type').val();
					if (type === "add") {
						addToTextarea($('#filter_add_regex'), regexString);
					} else if (type === "ignore") {
						addToTextarea($('#filter_ignore_regex'), regexString);
					}
					$('#filter_helper_regex').val('');
				}
				event.preventDefault();
			});
		});
	</script>
</mmt:pageWrapper>