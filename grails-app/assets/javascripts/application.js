// This is a manifest file that'll be compiled into application.js.
//
// Any JavaScript file within this directory can be referenced here using a relative path.
//
// You're free to add application-wide JavaScript to this file, but it's generally better
// to create separate JavaScript files as needed.
//
//= require jquery-2.2.0.min
//= require jquery.dataTables.min
//= require bootstrap
//= require_tree .
//= require_self

if (typeof jQuery !== 'undefined') {
    (function($) {
        $('#spinner').ajaxStart(function() {
            $(this).fadeIn();
        }).ajaxStop(function() {
            $(this).fadeOut();
        });
        
        $(document).ready(function() {
        	
        	var baseUrl = $('body').attr('data-base-url');
        	
        	/**
        	 * Alert dismissal
        	 */
        	$('.dismissable .alert a.close').on('click', function() {
        		var id = $(this).attr('data-id');
        		$.post(baseUrl + 'api/deleteMessage', {id: id});
			});
        	
        	/**
        	 * Initilise Data Tables
        	 */
        	var dataTableSelector = '.do-data-table';
        	var doDataTable = function($table) {
        		var paging = true;
        		var searching = true;
        		if ($table.attr('data-paging') == 'false') {
        			paging = false;
        		}
        		if ($table.attr('data-searching') == 'false') {
        			searching = false;
        		}
                var dataTableOptions = {
                	"paging": paging,
                	"searching": searching
                	//,"jQueryUI": true
                };
                if ($table.find('thead th.default-sort').length) {
                	var defaultOrder = 'asc';
                	if ($table.find('thead th.default-sort.default-sort-order-desc').length) {
                		defaultOrder = 'desc';
                	}
                	dataTableOptions = $.extend({}, dataTableOptions, {
                    	"aaSorting": [[ $table.find('thead th.default-sort').index(), defaultOrder ]]
                    });
                }
        		$table.DataTable(dataTableOptions);
        	}
        	$(dataTableSelector).each(function(i, e) {
        		doDataTable($(this));
        	})
        	
        	/**
        	 * Render input helper text
        	 */
        	var renderInputHelperText = function() {
        		var helpText = {
        			'#systemCommand': 'Passed arguments: name=torrent name, directory=torrent download directory (if applicable). Needs to be executable for process owner.',
        			'#detailsUrlValueFromRegex': 'eg: http://localhost/(\d+)/.*',
        			'#detailsUrlFormat': 'eg: http://localhost/details?id={regex-value}',
        			'#removeAddFilterOnMatch': 'Removes the Add Entry when an entry is matched by the filter',
        			'#filterAction': 'Ignore: Only add records matched by add regex. Add: Add all records, unless matched by ignore regex.',
        			'#filterPrecedence': 'Ignore: Check ignore filter first. Add: check add filter first.',
        			'#syncInterval': 'Sync interval in minutes.',
            		'#deleteInterval': 'Delete interval in days.',
            		'#uploadLimit': 'Upload limit in Kb/s.'
        		};
        		
        		$.each(helpText, function(selector, text) {
        			var $el = $(selector);
        			if ($el.length) {
        				$('<p class="form-input-help-text">' + text + '</p>').insertAfter($el);
        			}
        		});
        	};
        	renderInputHelperText();
        	
        });
        
    })(jQuery);
}
