function loadDocs(qty,page) {
	$.getJson(
		'/documents',
		{
			quantity: qty || 10,
			page: page || 0
		},
		function(data) {
			var table = $('#doctable>tbody');
			$.each(data, function(key,val) {
				var line = $('<tr></tr>');
				var th = $('<th>'+val.id+'</th>');
				var data = (typeof(val.type) !== 'undefined' && val.type === 'json') ? data.substring(0,data.length > 140 ? 140, data.length) : "&lt;binary data&gt;";
				var tddata = $('<td>'+data+'</td>');
				var buttons = $('<td><button class="btn">Edit</button><button class="btn">Delete</button></td>');
				line.push(th);
				line.push(tddata);
				line.push(buttons);
				table.push(line);
			});
		});
}

$(document).ready(function() {
	loadDocs(10,0);
});
