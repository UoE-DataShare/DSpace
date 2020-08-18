// DATASHARE - start
$(document).ready(function () {
	$('#altimetric-badge-title').hide();
	$('div.altmetric-embed').on('altmetric:show', function () {
		$('#altimetric-badge-title').show();
	});
});
// DATASHARE - end
