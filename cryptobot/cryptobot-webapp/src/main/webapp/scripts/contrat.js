
let tableContrats = null;

$(document).ready(function() {
	populateTableContrats()
})


function populateTableContrats(event) {
	if (event) {
		event.preventDefault()
	}
	const tableSelector = 'table.contrats'
	const url =  contextPath + '/contrat/liste?' + $("input, select").serialize()
	const fieldNames = ['fluxRecu.dateFlux', 'fluxRecu.dateVacation', 'numContrat', 'offreCollective', 'siret', 'msgAnoNiv1', 'trtAnoNiv2', 'etat']
	const transformFn = function(row) {
		row.etat = ''
		if (row.statutIntegration != 'OK') {
			row.etat = row.libelleStatut;
		}
	}
	const otherConfig = {
		scrollY: screen.height/3,
		columnDefs: [ { target: 7, orderable: false } ],
	}
	const actionSelector = '#actions'
	tableContrats = populateTableFromServer({tableSelector, url, fieldNames, transformFn, actionSelector, otherConfig})
}

