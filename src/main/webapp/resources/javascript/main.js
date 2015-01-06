
var sortList = function($list, rowClass, tagClass, tagAttribute, order) {
	$list.children(rowClass).sort(function(a, b) {
		var first = b;
		var second = a;
		if (order && order == 'asc') {
			first = a;
			second = b;
		}
		if (tagClass) {
			return $(first).find(tagClass).attr(tagAttribute).localeCompare($(second).find(tagClass).attr(tagAttribute));
		}
		return $(first).attr(tagAttribute).localeCompare($(second).attr(tagAttribute));
    }).appendTo($list);
};

var getDate = function(timestamp) {
    return new Date(timestamp * 1000);
};

$(document).ready(function() {
	
	$('button[type="submit"]').click(function() {
		// disable submit button when clicked
		this.innerText = 'Submitting...';
		this.disabled = 'disabled';
		this.form.submit();
	});
	
});