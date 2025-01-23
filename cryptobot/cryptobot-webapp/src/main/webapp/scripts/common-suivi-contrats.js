
let tableDetailsContrat = null;

const detailsFieldNames = ['codeTrt', 'dateTrt', 'champRef', 'libelleAno', 'donneeAno', 'etatIntegration']

const detailsTransformFn = (row) => {
	row['champRef'] = '';
	if (row['typeChampId'] && row['valeurChampId']) {
		 row['champRef'] = row['typeChampId'] + ' ' + row['valeurChampId'];
	}
}

const detailsOtherConfig = { scrollY: screen.height/3 }


function recycler(button) {
	console.log(`Recyclage de ${button.attributes['refid'].value}`)
}

function visualiser(button) {	
	const idContrat = button.attributes['refid'].value
	console.log(`Visualisation de ${idContrat}`)
	const modal = new bootstrap.Modal('#detailsContratModal')
	modal.show()
	populateTableDetailsContrat(idContrat)
}

function populateTableDetailsContrat(idContrat) {
	return populateTableDetailsContrat(idContrat, false)
}


function showDetailsForNumContrat(numContrat) {
	if (numContrat) {
		const url =  contextPath + '/contrat/details/?numContrat=' + numContrat
		$.get({
			url,
			success: data => populateContainerDetailsContrat(data),
		})
	}
}

function populateContainerDetailsContrat(listeDetails) {
	const selector = '#containerDetails'
	const tableSelector = selector + ' table'
	
	$(selector + ' span[id]').html('&nbsp;')	// reset
		
	if (listeDetails && Array.isArray(listeDetails) && listeDetails.length > 0) {
		let contrat = listeDetails[0]['integrationContrat']
		if (contrat) {
			let fluxRecu = contrat.fluxRecu
			if (fluxRecu.dateFlux) {
				$(selector + ' #dateFlux').html(fluxRecu.dateFlux)
			}
			if (contrat.dateVacation) {
				$(selector + ' #dateVacation').html(contrat.dateVacation)
			} else {
				$(selector + ' #dateVacation').html('non exécutée')
			}
			$(selector + ' #numCon').html(contrat['numContrat'])
			$(selector + ' #offre').html(contrat['offreCollective'])
			$(selector + ' #siret').html(contrat['siret'])
			$(selector + ' #anoNiv1').html(contrat['msgAnoNiv1'] ? contrat['msgAnoNiv1'] :'')
		}
	}
	tableDetailsContrat = populateTableFromListe({
		tableSelector,
		liste: listeDetails,
		fieldNames: detailsFieldNames,
		transformFn: detailsTransformFn,
	})
}


function populateTableDetailsContrat(idContrat, withNumContrat) {
	const tableSelector = 'table#detailsContrat'
	const url =  contextPath + '/contrat/details/' + (withNumContrat ? '?numContrat=' + idContrat : idContrat)
	tableDetailsContrat = populateTableAjax({
		tableSelector,
		url,
		fieldNames: detailsFieldNames,
		transformFn: detailsTransformFn,
		otherConfig: detailsOtherConfig
	})
}



