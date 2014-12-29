
var sortList = function(listId, rowClass, tagClass, tagAttribute, order) {
	$(listId).children(rowClass).sort(function(a, b) {
		var first = b;
		var second = a;
		if (order && order == "asc") {
			first = a;
			second = b;
		}
        return $(first).find(tagClass).attr(tagAttribute).localeCompare($(second).find(tagClass).attr(tagAttribute));
    }).appendTo(listId);
};

var getDate = function(timestamp) {
    return new Date(timestamp * 1000);
};
