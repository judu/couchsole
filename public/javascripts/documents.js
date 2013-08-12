function loadDocs(qty,page) {
	$.getJSON(
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
				var obj = (typeof(val.type) !== 'undefined' && val.type === 'json') ? val.doc.substring(0,val.doc.length > 140 ? 140 : val.doc.length) : "&lt;binary data&gt;";
				var tddata = $('<td>'+obj+'</td>');
				var buttons = $('<td><button class="btn">Edit</button><button class="btn">Delete</button></td>');
				line.append(th);
				line.append(tddata);
				line.append(buttons);
				table.append(line);
			});
		}
	);
}
console.log("coucou");

$(document).ready(function() {
	loadDocs(10,0);
});
