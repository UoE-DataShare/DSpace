//Preview
$(document).ready(function() {
	// Only csv, ts, tsv, fasta, fas, fa, or txt files (case insesitive so ig)
	var REGEX_FOR_DSPACE_FILE_URL = /\/bitstream\/handle\/\d*\/\d*\/(.)+(\.csv|\.txt|\.fasta|\.fas|\.fa|\.tsv|\.ts)(?=(\?))/ig;

	var myModal = '<div class="modal fade" id="myModal">' +
	'<div class="modal-dialog" style="min-width: 80%;">' +
	'<div class="modal-content">' +
	'<div id="modal-header" class="modal-header">' +
	'<button type="button" class="close" data-dismiss="modal" aria-label="Close">' +
	'<span aria-hidden="true">&times;</span>' +
	'</button>' +
	'<h2 id="modal-title" class="modal-title" style="word-wrap: break-word; width: 75%;">Modal title</h2>' +
	'<div id="modal-warning-about-format" class"small-text text-danger"></div>' +
	'</div>' +
	'<div id="modal-data" class="modal-body"></div>' +
	'<div class="modal-footer">' +
	'<a href="#" data-dismiss="modal" class="btn">Close</a>' +
	'</div>' +
	'</div>' +
	'</div>' +
	'</div>';

	$('body').append(myModal);
	$('#myModal').modal({
		backdrop: 'static',
		keyboard: false,
		show: false
	});

	if ($('.item-page-field-wrapper').length > 0) {
		$('a[test]').each(function() {
//			console.log("test:", $(this).attr('test'));
			var fileUrl = $(this).attr('href');
			// Only interested in files not under embargo, i.e., isAllowed=y
			if (fileUrl.includes("isAllowed=y")) {
				var matchStr = fileUrl.toLowerCase().match(REGEX_FOR_DSPACE_FILE_URL);
				console.log("matchStr:", matchStr);
				if (matchStr && matchStr.length > 0) {
					if(matchStr[0].endsWith(".csv") || matchStr[0].endsWith(".tsv")) {
						$('<br><button type=\"button\" class=\"preview btn btn-link\" style=\"display: inline-block;\" ><span style="color: #000;">[Preview file]</span></button> <button type=\"button\" class=\"preview btn btn-link\" style=\"display: inline-block;\"><span style="color: #000;">[Preview as table]</span></button><br>').insertAfter($(this));
					} else {
						$('<br><button type=\"button\" class=\"preview btn btn-link\" style=\"display: inline-block;\"><span style="color: #000;">[Preview file]</span></button>').insertAfter($(this));
					}
				}
			}
		});

		$('.preview').on('click', function() {
			var previewInTableFormatRequired = $(this).children("span:contains('table')").length > 0;
			var getUrl = window.location;
			var serverUrl = getUrl.protocol + "//" + getUrl.host;
			var baseUrl = getUrl.protocol + "//" + getUrl.host + "/" + getUrl.pathname.split('/')[1];
			var fileUrl = serverUrl + $(this).prevUntil('a[test]').last().prev().attr('href');
			var previewUrl = encodeURI(serverUrl + "/datashare-preview/preview?file=" + fileUrl);
			console.log("previewUrl : ", previewUrl);

			var matchStr = fileUrl.match(REGEX_FOR_DSPACE_FILE_URL);
			console.log("matchStr:", matchStr);
			if (matchStr && matchStr[0]) {
				var splitStr = matchStr[0].split("/");
				var fileName = decodeURI(splitStr[splitStr.length - 1]);
				console.log("fileName: ", fileName);
				var displayTitle = "Preview of " + fileName;
//				console.log("displayTitle: ", displayTitle);
				$('#modal-title').text(displayTitle);

				var json = {
						fileUrl: fileUrl
				};

				var jsonString = JSON.stringify(json);
//				console.log("jsonString: ", jsonString);

				$.ajax({
					url: previewUrl,
					type: "POST",
					contentType: "application/json",
					data: jsonString,
					dataType: "json",
					success: function(data, textStatus, jQxhr) {
//						console.log(data.content);
						var content = data.content;
						var lines = content.split(/\^M\r\n|\n/);
						var numOfLinesDisplay = lines.length < 30 ? lines.length : 30;
						var displayText = "";
						for (var line = 0; line < numOfLinesDisplay; line++) {
//							console.log(line + " --> " + lines[line]);
							if ((fileName.toLowerCase().endsWith(".csv") || fileName.toLowerCase().endsWith(".tsv")) && previewInTableFormatRequired) {
								displayText = displayText + lines[line] + "\n";
							} else {
								displayText = displayText + lines[line] + "<br>";
							}
						}

						if ((fileName.toLowerCase().endsWith(".csv") || fileName.toLowerCase().endsWith(".tsv")) && previewInTableFormatRequired) {
//							console.log("Table format: ", fileName);
//							console.log(displayText);
							var formatWarning = "* Please note the table format may be incorrect in some cases.";
							$('#modal-warning-about-format').text(formatWarning);
							Papa.parse(displayText, papaParseConfig);
						} else {
//							console.log(displayText);
							$('#modal-data').html(displayText);
						}

						$('#myModal').modal({
							show: true
						});
					},
					error: function(jqXhr, textStatus, errorThrown) {
						console.log(errorThrown);
					}
				});
			}
		});
	}
	// PapaParse date rendered as HTML table
	function displayHTMLTable(results) {
		var table = "<div class='table-responsive'> <table class='table table-striped table-bordered'>";
		var data = results.data;

		for (i = 0; i < data.length; i++) {
			table += "<tr>";
			var row = data[i];
			var cells = row.join(",").split(",");

			for (j = 0; j < cells.length; j++) {
				table += "<td>";
				table += cells[j];
				table += "</th>";
			}
			table += "</tr>";
		}
		table += "</table>";
		$("#modal-data").html(table);
	}

	var papaParseConfig = {
			delimiter: "auto",
			complete: displayHTMLTable,
	};

});
