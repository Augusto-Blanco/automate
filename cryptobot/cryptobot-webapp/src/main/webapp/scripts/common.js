$(document).ready(tooltips)

function tooltips() {
	$('body [title]').each((i, elem) => {
		let title = $(elem).attr('title')
		$(elem).attr('data-bs-toggle', 'tooltip').attr('data-bs-title', title)
	})
	$('[data-bs-toggle="tooltip"]').each(
		(i, elem) => new bootstrap.Tooltip($(elem))
	)
}