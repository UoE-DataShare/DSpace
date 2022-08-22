$( document ).ready( function() {
    var homePopup = '<div class="modal fade" id="homePopup" role="dialog">' +
	'<div class="modal-dialog" style="min-width: 50%;" role="document">' +
	'<div class="modal-content">' +
	'<div id="modal-header" class="modal-header">' +
	'<button type="button" class="close" data-dismiss="modal" aria-label="Close">' +
	'<span aria-hidden="true">&times;</span>' +
	'</button>' +
	'<h2 id="modal-title" class="modal-title" style="cursor:move; word-wrap: break-word; width: 75%;">Work in Progress!!!</h2>' +
	'<div id="modal-warning-about-format" class"small-text text-danger"></div>' +
	'</div>' +
	'<div id="modal-data" class="modal-body" style="overflow: auto; height: 60%">' +
  '<div class="content"> <iframe src="https://docs.google.com/forms/d/163EdecVK8fBflAtAEGzxWxWzM66u7ITtk1esJuXmT98/viewform?embedded=true" marginheight="0" marginwidth="0" width="100%" height="2150" frameborder="0">Loading...</iframe></div>' +
  '</div>' +
	'<div class="modal-footer">' +
	'<a href="#" data-dismiss="modal" class="btn">Close</a>' +
	'</div>' +
	'</div>' +
	'</div>' +
	'</div>';

    var styleFragment = '<style> .modal-content {  background-color: whitesmoke; }</style>';

    $('header').append(styleFragment);

    var homePopupContainer = '<div id="homePopupContainer"></div>';

	$('body').append(homePopupContainer);

    $('#homePopupContainer').append(homePopup);

    if (sessionStorage.getItem('cookiePopupSeen') != 'shown') {
        sessionStorage.setItem('cookiePopupSeen','shown');
        $('#homePopup').modal({
    		backdrop: 'static',
    		keyboard: false,
            show: true
		});
    }
	 

  });