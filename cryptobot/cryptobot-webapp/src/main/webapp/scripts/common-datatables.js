const tablesMappedBySelector = {}

const dataTableTexts = {
	processing: "Chargement en cours...",
    search:     "Rechercher",
    lengthMenu: "Afficher _MENU_ éléments",
    info:       "Elements _START_ à _END_ sur _TOTAL_",
    infoEmpty:  "Aucun élément à afficher",
    infoFiltered:   "(filtré de _MAX_ éléments au total)",
    infoPostFix:    "",
    loadingRecords: "Chargement en cours...",
    zeroRecords:    "Aucun &eacute;l&eacute;ment &agrave; afficher",
    emptyTable:     "Aucune donnée disponible dans le tableau",
//    paginate: {
//        first:      "Premier",
//        previous:   "Pr&eacute;c&eacute;dent",
//        next:       "Suivant",
//        last:       "Dernier"
//    },
    aria: {
        sortAscending:  ": tri par ordre croissant",
        sortDescending: ": tri par ordre décroissant"
    }
}

const commonDataTableConfig = {
    lengthMenu: [10, 20, 30, 40, 50],    
    processing: true,
	lengthChange: false,
	searching: false,
//	ordering:  false,
//	pageLength: 15,
//	order: [],
	select: true,
	language: dataTableTexts,
}


function populateTableFromListe({tableSelector, liste, fieldNames, pageLength, transformFn, initialOrder, actionSelector, otherConfig}) {
	
	if (liste && liste.length) {
		liste.forEach(row => {
			row.DT_RowId = row.id
			if (typeof transformFn == 'function') {
				transformFn(row)	 			
		 	}
		})
	}
	let columns = defineColumns(fieldNames, otherConfig)
	let actionsHandler = getActionsHandler(columns, actionSelector, tableSelector)	
	let order = getOrder(initialOrder)
	
	if (!pageLength) {
		pageLength = 15;
	}
		
    let config = Object.assign({}, commonDataTableConfig, {		
		data: liste,
	    columns,
		pageLength,
		order
	})
	
	if (actionsHandler) {
		config.createdRow = actionsHandler
	}
	
	if (otherConfig && Object.keys(otherConfig)) {
		Object.assign(config, otherConfig)
	}
	
	return createDataTableForSelector(tableSelector, config)	
}

function createDataTableForSelector(tableSelector, config) {
	let tableForSelector = tablesMappedBySelector[tableSelector]
	if (tableForSelector) {
		tableForSelector.clear().draw()
		tableForSelector.destroy()
	}
	tableForSelector = new DataTable(tableSelector, config)
	tablesMappedBySelector[tableSelector] = tableForSelector
	return tableForSelector	
}


function populateTableFromServer({tableSelector, url, fieldNames, pageLength, transformFn, intialOrder, actionSelector, otherConfig}) {
	otherConfig = Object.assign({serverSide: true}, otherConfig)
	return populateTableAjax({tableSelector, url, fieldNames, pageLength, transformFn, intialOrder, actionSelector, otherConfig})
}


function populateTableAjax({tableSelector, url, fieldNames, pageLength, transformFn, initialOrder, actionSelector, otherConfig}) {
	
	let columns = defineColumns(fieldNames, otherConfig)
	let actionsHandler = getActionsHandler(columns, actionSelector, tableSelector)	
	let order = getOrder(initialOrder)
	
	if (!pageLength) {
		pageLength = 15;
	}
		
	const requestParams = (data) => {
        if (typeof data.start != 'undefined' && data.length ) {
			let numPage = Math.floor(data.start / data.length)
			data.numPage = numPage
		}
		if (data.draw == 1 || !data.order || !data.order.length) {
			data.order = order
		} 
    }
    	
	const dataFilter = (resp) => {
		let respObj
		if (resp) {
	        respObj = JSON.parse(resp)
	        if (typeof respObj.totalElements === 'number') {
		        respObj.recordsTotal = respObj.totalElements
		        respObj.recordsFiltered = respObj.totalElements
	        }
        } else {
			 respObj = {}
		}
		resp = JSON.stringify(respObj)
        return resp
    }
    
    const dataSrc = (resp) => {
		let data = resp.data
		if (!data) {
			data = resp
		}
		if (data && Array.isArray(data)) {
			data.forEach((row) => {
				row.DT_RowId = row.id
				if (typeof transformFn == 'function') {
					transformFn(row)	 			
		 		}
			})
		}
		return data
	}
	
    let config = Object.assign({}, commonDataTableConfig, {		
		ajax: {
			url,
			dataFilter,
			data: requestParams,
			dataSrc,
		},
	    columns,
		pageLength,
		order,
	})
	
	if (actionsHandler) {
		config.createdRow = actionsHandler
	}
	
	if (otherConfig && Object.keys(otherConfig)) {
		Object.assign(config, otherConfig)
	}

	return createDataTableForSelector(tableSelector, config)
}



function getColumnDef(configObj, index) {
	let columnDef = null;
	if (configObj && Array.isArray(configObj.columnDefs)) {
		columnDef = configObj.columnDefs.filter(def => def.target === index)
	}
	if (columnDef && columnDef.length > 0) {
		return columnDef[0]
	}
	return null	
}

function defineColumns(fieldNames, configObj) {
	let columns = fieldNames;
	if (Array.isArray(fieldNames)) {
		columns = fieldNames.map((name, index) => {
			let column = {data: name}
			let def = getColumnDef(configObj, index)
			if (!def) {
				column = {...column, defaultContent: ''}
			}
			return column
		})
	}
	return columns
}

function getActionsHandler(columns, actionSelector, tableSelector) {
	let actionsHandler = null;
	if (actionSelector) {
		let actionHtml = $(actionSelector).html()
		if (actionHtml) {
			columns.push({data: 'actions', defaultContent: actionHtml, orderable: false})
		}
		actionsHandler = function(row) { onCreatedRow(row, tableSelector) }
	}
	return actionsHandler
}

function getOrder(initialOrder) {
	let order = []
	if (initialOrder) {
		if (typeof initialOrder == 'string') {
			order[0] = { name: initialOrder, dir: 'asc' }
		} else if (typeof initialOrder == 'object') {
			order[0] = initialOrder;
		}
	}
	return order
}


function onCreatedRow(row, tableSelector) {
	
	const getActionElement = (tr) => $(tr).children('td:last-child').children().first()	
	const selectRow = (tr) => {
		getActionElement(tr).removeClass('invisible')
	}	
	const deselectRow = (tr) => {
		getActionElement(tr).addClass('invisible')
	}
	
	getActionElement(row).find('button, input, a').each((i, elem) => {
		let name = $(elem).attr('name')
		if (name) {
			elem.id = name + '-' + row.id
		}
		$(elem).attr('refid', row.id)
		if (elem.tagName.toLowerCase() == 'button') {
			elem.value = row.id
		}
		
	})
	
	$(row).on('mouseover', (event) => {
		selectRow(event.currentTarget)
	})
		
	$(row).on('mouseout', (event) => {
		if (!$(event.currentTarget).hasClass('selected')) {
			deselectRow(event.currentTarget);
		}
	})
	
	$(row).on('click', (event) => {
		$(tableSelector + ' tr').each((i, elem) => {
			deselectRow(elem)
		})
		selectRow(event.currentTarget)
	})
}