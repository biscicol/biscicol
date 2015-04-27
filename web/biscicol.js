/*
 Javascript for the BiSciCol Demo Page
 */

// localStorage models key
var storageModels = "biscicolModels";

// path to search application interface
var searchPath = "/biscicol/rest/search/";
var loading = "<center><img src='/biscicol/images/gif-loading.gif'></center>";

// execute once the DOM has loaded
$(function() {
	// assign onSubmit event handler and action attribute to forms
	$("#searchForm").submit(runGo);
	$("#uploadLocalForm")
		.submit(beforeUploadLocal)
		.attr("action", searchPath);
	$("#loadUrlForm")
		.submit(beforeLoadUrl)
		.attr("action", searchPath);
	
	 $("#uploadTarget").appendTo($('body')); // prevent re-posting on reload

	// disable max depth if siblings search type selected
	var searchForm = document.getElementById("searchForm");
	$(searchForm.searchType).change(function () {
		searchForm.distance.disabled = this.value == "siblings";
	});

	// check which models from localStorage (plus model query parameter) exist on server
	var models = localStorage && localStorage.getObject(storageModels) || [],
		queryModel = $.parseQuery().model; // model query parameter
	if (queryModel && $.inArray(queryModel, models) < 0)
		models.push(queryModel); 
	if (models.length)
		$.ajax({
			url: searchPath,
			type: "POST",
			data: JSON.stringify(models),
			contentType: "application/json; charset=utf-8",
			dataType: "json",
			success: updateModels,
			error: function() {updateModels([]);}
		});
	else 
		updateModels([]);
})

// update models in localStorage, create form checkboxes, process query string
function updateModels(models) {
	var query = $.parseQuery(); // use parsequery plugin

	if (localStorage)
		localStorage.setObject(storageModels, models);
	var checkboxes = [checkbox("BiSciCol", "")];
	$.each(models, function(index, model) { 
		checkboxes.push(checkbox(model, model==query.model ? "checked" : ""));
	});
	$("#models").html(checkboxes.join(""))
		.parent().toggle(models.length > 0);
		
	// fill the form from query string and submit
	var searchForm = document.getElementById("searchForm"); // DOM element for convenient access to form fields
	setValue(searchForm.id, query.id);
	setValue(searchForm.dateLastModified, query.dateLastModified);
	setValue(searchForm.searchType, query.searchType);
	setValue(searchForm.distance, query.distance);
	$('input:radio[name=outputType][value=' + query.outputType + ']', searchForm).prop('checked', true);
	if (query.id)
		$(searchForm).submit();
	
	// var url = $.parseQuery().url; // url query parameter
	// if (url) {
		// var loadUrlForm = $("#loadUrlForm");
		// loadUrlForm.get(0).url.value = "http://" + url;
		// loadUrlForm.submit();
		// loadUrlForm.get(0).url.value = "";
	// }
}

// set the field value if non-empty
function setValue(field, value) {
	if (value) field.value = value;
}

// build model checkbox input
function checkbox(value, attributes) {
	return "<input type='checkbox' name='model' value='" + value + "' " + attributes + " /> " + value + "<br/>";
}

// process search form submission
function runGo() {
	// validate inputs

	// check if no ID
	if (!this.id.value) {
		alert('you must select an ID');
		return false;
	}
	// check if at least 1 model selected
	if ($("#models", this).parent().is(":visible") 
			&& $("#models input", this).length > 0 
			&& $("#models input:checked", this).length == 0) {
		alert('you must select at least 1 model');
		return false;
	}

	var outputType = $('input:radio[name=outputType]:checked', this).val();
	
	$("#HTMLresult")
		.removeClass()
		.addClass(outputType)
		.html(loading)
		.parent().css("padding","8px"); // clean up after showMap	
	
	$("#status").html("Contacting server and fetching data...");
	
	// call services to render data
	switch (outputType) {
	case 'tree':
		showTextTree(this);
		break;
	case 'table':
	case 'byType':
		showTable(this);
		break;
	case 'kml':
		showMap(this);
		break;
	case 'TreeImage':
	case 'N3':
		alert('function not yet implemented');
		break;
		//return showModelImg(this);
	}
	return false;
}

// render jstree from html data
function showTextTree(form) {
	$("#HTMLresult").jstree({
		html_data : {
			ajax : {
				url : searchPath,
				data: $(form).serialize(),
				success: function(data) {
					if (!data) noResults();					
				},
				 error: function (xhr, ajaxOptions, thrownError) {
                    alert(searchPath + $(form).serialize() + "; " + xhr.status + " " + thrownError);
                 }
			}
		},
		themes : { theme : "classic" },
		plugins : [ "themes", "html_data" ]
	});
}

function showTable(form) {
	$("#status").append("<br/>Building Table...");
	$.ajax({
		url: searchPath,
		data: $(form).serialize(),
		dataType: "html",
		success: function(data) {
			if (data) {
				$('#HTMLresult').html(data);
				byTypeInit();
			} else 
				noResults();
		}});
}

function noResults() {
	$('#HTMLresult').html("No results to display");
	$("#status").append("<br/>No results to display");
}

// initialize byType view: add onClick events to type headers
function byTypeInit() {
	var HTMLresult = $("#HTMLresult");
	var h1s = $('h1', HTMLresult);
	var trCount = $('tr', HTMLresult).length;
	var collapse = h1s.length > 1 && h1s.length + trCount > 12
	h1s.each(function() {
		if (collapse) toggleCollapse($(this));
		this.onclick = function() {toggleCollapse($(this));}
	});
	var itemCount = "1 + " + (trCount - (h1s.length ? h1s.length : 2));
	$("#status").append("<br/>Displaying " + itemCount + " items.");
}

// collapse/expand the next sibling of type header
function toggleCollapse(h1) {
	h1.toggleClass('collapsed');
	h1.next().slideToggle(Math.min(h1.next().find("tr").length*10+200, 1000));
}

function showModelImg(form) {
	var url = "rest/v2/modelimg?" + $(form).serialize();

	$('#HTMLresult').empty();
	$('#HTMLresult').append('<img id="theImg" src="' + url + '" />')
}

function showGeoRSS(form) {
	$('#HTMLresult').empty();
	$('#HTMLresult').load("rest/v2/georss?" + $(form).serialize());
}

function showMap(form) {
	$('#HTMLresult').parent().css("padding","0");
	$('#HTMLresult').empty();
	
	var pos = location.href.split("?")[0].lastIndexOf("/");
	if (pos < 10) pos = location.href.length;
	var url = location.href.substr(0, pos) + "/" + searchPath + "?" + $(form).serialize();

	$("#status").append("<br/>Building map...");
	//$("#status").append("<br/>KML URL=" + url);

	var myOptions = { mapTypeId: google.maps.MapTypeId.HYBRID }

	var map = new google.maps.Map(document.getElementById("HTMLresult"), myOptions);

	var georssLayer = new google.maps.KmlLayer(url);

	georssLayer.setMap(map);
}

//function reloadStylesheets() {
//	var queryString = '?reload=' + new Date().getTime();
//	$('link[rel="stylesheet"]').each(function () {
//		this.href = this.href.replace(/\?.*|$/, queryString);
//	});
//}

// validate local upload form, display status message, ...
function beforeUploadLocal() {
	return beforeLoad(this.file, "Please select a file to upload.");
}

// validate url upload form, display status message, ...
function beforeLoadUrl() {
	return beforeLoad(this.url, "Please enter a url to load model from.");
}

// validate load form, display status message, ...
function beforeLoad(reqField, reqMessage) {
	if (!reqField.value) {
		alert(reqMessage);
		reqField.focus();
		return false;
	}
	$("#status").html(loading);
	reqField.form.submitBtn.disabled = true;
	$("#uploadTarget").one("load", afterUpload);
	return true;
}

// update localStorage, create new checkbox with uploaded filename if no error
function afterUpload() {
	$("#status").empty();
	$("#uploadLocalForm :submit, #loadUrlForm :submit").prop("disabled", false);
	var data = frames.uploadTarget.document.body.textContent;
	// distinguish response OK status by JSON format (quotes in this case)
	if (data && data.charAt(0)=='"' && data.charAt(data.length-1)=='"') {
		var file = data.substr(1, data.length-2);
		if (localStorage) {
			var models = localStorage.getObject(storageModels) || [];
			models.push(file);
			localStorage.setObject(storageModels, models);
		}
		$("#models").append(checkbox(file, "checked"))
			.parent().slideDown(400);
		alert("Model '" + file + "' uploaded successfully.");
	}
	else
		alert("Error" + (data ? ":\n\n"+data : "."));	
}

Storage.prototype.setObject = function(key, value) {
	this.setItem(key, JSON.stringify(value));
}

Storage.prototype.getObject = function(key) {
	var value = this.getItem(key);
	return value && JSON.parse(value);
}

