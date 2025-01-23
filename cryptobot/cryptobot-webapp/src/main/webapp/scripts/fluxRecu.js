
var contenuFluxRecus = {};

async function afficherContrats(idFlux) {
	let listeContrats = await chargerContratsFluxRecu(idFlux);
	populateTableContrats('#contrats-' + idFlux + ' table', listeContrats);
}

async function afficherContratsKO(idFlux) {
	let listeContrats = await chargerContratsFluxRecu(idFlux);
	listeContrats = listeContrats.filter(contrat => contrat.contratEnAnomalie);	
	populateTableContrats('#contratsKO-' + idFlux + ' table', listeContrats);
}

async function chargerContratsFluxRecu(idFlux) {
	let listeContrats = [];
	if (!contenuFluxRecus[idFlux]) {
		const url = contextPath + '/contrat/liste/' + idFlux;
		listeContrats = await $.when($.get(url));
		listeContrats = listeContrats.map(
			contrat => {
				let etat = '';
				if (contrat.statutIntegration != 'OK') {
					etat = contrat.libelleStatut;
				}
				return { etat, ...contrat };
			}
		);
		contenuFluxRecus[idFlux] = listeContrats;
	}
	return contenuFluxRecus[idFlux];
}

function populateTableContrats(tableSelector, liste) {	
	const fieldNames = ['numContrat', 'offreCollective', 'siret', 'msgAnoNiv1', 'trtAnoNiv2', 'etat']
	const pageLength = 10
	const actionSelector = '#actions'
	const otherConfig = { 
		drawCallback: function (settings) {
			handleOnClick()
		}
	}
	return populateTableFromListe({tableSelector, fieldNames, liste, pageLength, actionSelector, otherConfig})
}

function handleOnClick() {
	$("button[name='visualiser']").on('click', (event) => {
		visualiser(event.currentTarget)
	})
} 
