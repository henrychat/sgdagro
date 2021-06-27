

function fn_inicializaTrazabilidad(sCoAnnio, fechaActual) {
    jQuery('#buscarDocumentoSeguiEstEmiBean').find('#esFiltroFecha').val("1");//rango fecha
    jQuery('#buscarDocumentoSeguiEstEmiBean').find("#fechaFiltro").showDatePicker({defaultOpcionSelected: 10});
    
    jQuery('#fechaFiltro').removeClass("glyphicon-collapse-down");
    jQuery('#spanDivBusquedaF').addClass("glyphicon-collapse-up");
    jQuery("#txtDepOrigen").val("[TODOS]");
    jQuery("#coDepOrigen").val("");
    
    jQuery('#buscarDocumentoSeguiEstEmiBean').find('#busNumDocRef').change(function() {
        $(this).val(replicate($(this).val(), 6));
    });
    jQuery('#buscarDocumentoSeguiEstEmiBean').find('#busNumDoc').change(function() {
        $(this).val(replicate($(this).val(), 6));
    });
    jQuery('#buscarDocumentoSeguiEstEmiBean').find('#busCoAnnio').find('option[value=""]').remove();
    pnumFilaSelect = 0;
    changeTipoBusqTrazabilidad("0");
}

function changeTipoBusqTrazabilidad(tipo) {
    jQuery('#buscarDocumentoSeguiEstEmiBean').find('#tipoBusqueda').val(tipo);
    submitAjaxFormEmiDocAdmSegui2(tipo);
   // mostrarOcultarDivBusqFiltro2SeguiEmi2();
}

function fn_buscaDestinatarioTrazabilidad() {
    var p = new Array();
    p[0] = "accion=goBuscaDestinatario";
    p[1] = "pnuAnn=" + jQuery('#buscarDocumentoSeguiEstEmiBean').find('#coAnnio').val();
    ajaxCall("/srConsultaTrazabilidad.do", p.join("&"), function(data) {
        fn_rptaBuscaDestinatarioEmiConsul(data);
    },
            'text', false, false, "POST");
}

function submitAjaxFormEmiDocAdmSegui2(tipo) {
	//hoy
    var validaFiltro = fu_validaFiltroTraza();
//    if (jQuery('#fechaFiltro').is(':visible')) {
//        
//        jQuery('#incluyeFiltroFecha').val("1");
//    } else {
//     
//        jQuery('#incluyeFiltroFecha').val("0");
//    }
    if (validaFiltro === "1") {
        ajaxCall("/srConsultaTrazabilidad.do?accion=goInicio", $('#buscarDocumentoSeguiEstEmiBean').serialize(), function(data) {
            refreshScript("divTblConsulDocumentoEmitido", data);
        }, 'text', false, false, "POST");
    } else if (validaFiltro === "2") {//buscar referencia
        ajaxCall("/srConsultaTrazabilidad.do?accion=goBuscaDocumentoEnReferencia", $('#buscarDocumentoSeguiEstEmiBean').serialize(), function(data) {
            fu_rptaBuscaDocumentoEnReferenciaRecepConsulEmi(data);
        }, 'text', false, false, "POST");
    }
    return false;
}

function mostrarOcultarDivBusqFiltro2SeguiEmi2() {

	
    if (jQuery('#divConfigFiltro').is(':visible')) {
        jQuery('#divConfigFiltro').hide();
        jQuery('#spanDivFiltro').removeClass("ui-icon-circle-arrow-n");
        jQuery('#spanDivFiltro').addClass("ui-icon-circle-arrow-s");
    }
    if (jQuery('#divConfigBusqueda').is(':visible')) {
        jQuery('#divConfigBusqueda').hide();
        jQuery('#spanDivBusqueda').removeClass("ui-icon-circle-arrow-n");
        jQuery('#spanDivBusqueda').addClass("ui-icon-circle-arrow-s");
    }
}


function fu_generarReporteTrazabilidadXLS() {
	fu_generarReporteTrazabilidad('XLS');
}
function fu_generarReporteTrazabilidad(pformatoReporte) {

    //var validaFiltro = fu_validaFiltroEmiDocAdmSeguiEst("0");
    var validaFiltro = fu_validaFiltroTraza();
    if (validaFiltro === "1") {
      
        ajaxCall("/srConsultaTrazabilidad.do?accion=goExportarArchivoLista&pformatRepor=" + pformatoReporte, $('#buscarDocumentoSeguiEstEmiBean').serialize(), function(data) {
            if (data !== null) {
                if (data.coRespuesta === "0") {
                    if (!!data.noUrl && !!data.noDoc) {

                        //fn_generaDocApplet(data.noUrl, data.noDoc, function(data) {
                        fn_generaDocDesktop(data.noUrl, data.noDoc, function(data) {
                            var result = data;
                            if (result !== "OK") {
                               bootbox.alert(result);
                            }
                        });

                    }
                } else {
                   bootbox.alert(data.deRespuesta);
                }
            } else {
               bootbox.alert("La respuesta del servidor es nula.");
            }
        }, 'json', false, true, "POST");
    }
    return false;
}


function fn_construyeTablaVencimientoDetalleSegui() {
    len = function(obj) {
        var L = 0;
        $.each(obj, function(i, elem) {
            L++;
        });
        return L;
    }
    var orden = [5, 0, 4, 1, 3, 2];
    for (var j = 0; j < len(coloresVencimiento); j++)
    {
        var label = coloresVencimiento[orden[j]].text;
        var color = coloresVencimiento[orden[j]].color;
        var descrip = coloresVencimiento[orden[j]].descrip;
        var clase = coloresVencimiento[orden[j]].cssClass;
        var filaAux = Array();
        filaAux.push("<tr><td><div class='redondito " + clase + "'></div></td><td>");
        filaAux.push("<h4 style='margin-top: 3px; margin-bottom: 0px;'>" + label + "</h4>");
        filaAux.push("<p>" + descrip + "</p>");
        filaAux.push("</td></tr>");
        var fila = "";
        for (var i = 0; i < filaAux.length; i++) {
            fila = fila + filaAux[i];
        }
        $("#tablaDetVencimientoCirculos").append(fila);
        fila = [];
    }
}
function fn_construyeTablaVencimientoDetalleCuadradosSegui() {
    len = function(obj) {
        var L = 0;
        $.each(obj, function(i, elem) {
            L++;
        });
        return L;
    }
    var orden = [5, 0, 4, 1, 3, 2];
    for (var j = 0; j < len(coloresVencimiento); j++)
    {
        var label = coloresVencimiento[orden[j]].text;
        var color = coloresVencimiento[orden[j]].color;
        var descrip = coloresVencimiento[orden[j]].descrip;
        var clase = coloresVencimiento[orden[j]].cssClass;
        var filaAux = Array();
        filaAux.push("<tr><td><div class='cuadradoVencimiento " + clase + "'>" + label + "</div></td>");
        filaAux.push("<td>" + descrip + "</td>");
        filaAux.push("</tr>");
        var fila = "";
        for (var i = 0; i < filaAux.length; i++) {
            fila = fila + filaAux[i];
        }
        $("#tablaDetVencimientoCuadrados").append(fila);
        fila = [];
    }
}

function fu_eventoTablaTrazabilidadSegui() {
    var oTable;
    oTable = $('#myTableFixed').dataTable({
        "bPaginate": false,
        "bFilter": false,
        "bSort": true,
        "bInfo": true,
        "bAutoWidth": true,
        "bDestroy": true,
        "sScrollY": "470px",
        "bScrollCollapse": false,
        "oLanguage": {
            "sZeroRecords": "No se encuentran registros.",
            "sInfo": "Registros: _TOTAL_ ",
            "sInfoEmpty": ""
        },
        "aoColumns": [
        	 {"bSortable": false},
             {"bSortable": false},
             {"bSortable": false},
             {"bSortable": false},
             {"bSortable": false},
             {"bSortable": false},
             {"bSortable": true},
             {"bSortable": false},
             {"bSortable": true},
             {"bSortable": true},
             {"bSortable": true},
             {"bSortable": true},
             {"bSortable": false},
 			 {"bSortable": false},
 			{"bSortable": false},
 			{"bSortable": false}
 			
        ]
    });

    jQuery.fn.dataTableExt.oSort['fecha-asc'] = function(a, b) {
        var ukDatea = a.split('/');
        var ukDateb = b.split('/');

        var x = (ukDatea[2] + ukDatea[1] + ukDatea[0]) * 1;
        var y = (ukDateb[2] + ukDateb[1] + ukDateb[0]) * 1;

        return ((x < y) ? -1 : ((x > y) ? 1 : 0));
    };

    jQuery.fn.dataTableExt.oSort['fecha-desc'] = function(a, b) {
        var ukDatea = a.split('/');
        var ukDateb = b.split('/');

        var x = (ukDatea[2] + ukDatea[1] + ukDatea[0]) * 1;
        var y = (ukDateb[2] + ukDateb[1] + ukDateb[0]) * 1;

        return ((x < y) ? 1 : ((x > y) ? -1 : 0));
    };
    function showdivToolTip(elemento, text)    {
        $('#divflotante').html(text);

        var x = elemento.left;
        var y = elemento.top + 24;
        $("#divflotante").css({left: x, top: y});
        document.getElementById('divflotante').style.display = 'block';

        return;
    }
    $("#myTableFixed tbody td").hover(
            function() {
                $(this).attr('id', 'divtitlemostrar');
                var index = $(this).index();
                if (index === 6 || index === 7 || index === 12 || index === 13) {
                    showdivToolTip($('#divtitlemostrar').position(), $(this).html());
                }
            },
            function() {
                $('#divtitlemostrar').removeAttr('id');
                $('#divflotante').hide();
            }
    );
   
    // aca
    
    $("#myTableFixed tbody tr").click(function(e) {
        if ($(this).hasClass('row_selected')) {
            //$(this).removeClass('row_selected');
            //jQuery('#txtpnuAnn').val("");
        }
        else {
            oTable.$('tr.row_selected').removeClass('row_selected');
            $(this).addClass('row_selected');

            /*obtiene datos*/
            //alert($(this).children('td')[12].innerHTML);
            //jQuery('#txtTextIndexSelect').val("0");
            if (typeof($(this).children('td')[13]) !== "undefined") {
                jQuery('#txtpnuAnn').val($(this).children('td')[1].innerHTML);
                jQuery('#txtpnuEmi').val($(this).children('td')[2].innerHTML);
                pnumFilaSelect = $(this).index();
            }
            //editarDocumentoRecep($(this).children('td')[12].innerHTML,$(this).children('td')[13].innerHTML,$(this).children('td')[15].innerHTML);
            /* comentado xq los botones de ver documento y ver anexos, ya se encuentran el la tabla
             var sData = $(this).find('td');
             passArrayToHtmlTabla("txtTextIndexSelect",sData);*/


        }
    });
    $("#myTableFixed tbody tr").dblclick(function() {
        //jQuery('#txtTextIndexSelect').val("-1"); 
    });
    if(jQuery('#myTableFixed >tbody >tr').length > 0){
        //$('#myTableFixed >tbody >tr').eq(0).addClass('row_selected');
        //var pauxNumFilaSelect = typeof(pnumFilaSelect)!=="undefined"? pnumFilaSelect:0;
        try{
            if(jQuery('#myTableFixed >tbody >tr').eq(pnumFilaSelect).length === 1){
                jQuery('#myTableFixed >tbody >tr').eq(pnumFilaSelect).trigger("click");
                jQuery('#myTableFixed >tbody >tr').eq(pnumFilaSelect).focus();
            }else{
                pnumFilaSelect=0;
            }
        }catch(ex){
            pnumFilaSelect=0;
        }
    }
    jQuery('#myTableFixed >tbody >tr').keydown(function(evento){
       if(evento.which===38){//up
            if (jQuery("#myTableFixed >tbody >tr").eq(pnumFilaSelect).prev('tr').length===0){
                pnumFilaSelect=$("#myTableFixed >tbody >tr").length;
            }
            pnumFilaSelect--;
            jQuery("#myTableFixed >tbody >tr").eq(pnumFilaSelect).trigger("click");
            jQuery("#myTableFixed >tbody >tr").eq(pnumFilaSelect).focus();
       }else if(evento.which===40){//down
            if ($("#myTableFixed >tbody >tr").eq(pnumFilaSelect).next('tr').length===0){
                pnumFilaSelect=-1;
            }           
            pnumFilaSelect++;
            jQuery("#myTableFixed >tbody >tr").eq(pnumFilaSelect).trigger("click");               
            jQuery("#myTableFixed >tbody >tr").eq(pnumFilaSelect).focus();           
       }
       evento.preventDefault();
    }); 
}

function fn_verAnexoEmi() {
    var pnuAnn = jQuery('#txtpnuAnn').val();
    var pnuEmi = jQuery('#txtpnuEmi').val();
    var pnuDes = "N";
    if (pnuAnn) {
        fn_verAnexosObj(pnuAnn, pnuEmi, pnuDes);
    } else {
        //alert("Seleccione una fila de la lista");
        alert_Info("Emisión :", "Seleccione una fila de la lista");
    }
}
function fn_verSeguimientoEstEmi() {
    var pnuAnn = jQuery('#txtpnuAnn').val();
    var pnuEmi = jQuery('#txtpnuEmi').val();
    var pnuDes = jQuery('#txtpnuDes').val();
    if (pnuAnn) {
        fn_verSeguimientoObj(pnuAnn, pnuEmi, pnuDes);
    } else {
        //alert("Seleccione una fila de la lista");
        alert_Info("Emisión :", "Seleccione una fila de la lista");
    }
}


function fu_generarReporteTrazabilidadXLS() {
	fu_generarReporteTrazabilidadXLS('XLS');
}
function fu_generarReporteTrazabilidadXLS(pformatoReporte) {

    var validaFiltro = fu_validaFiltroTraza();
    if (validaFiltro === "1") {
        
        ajaxCall("/srConsultaTrazabilidad.do?accion=goExportarArchivoLista&pformatRepor=" + pformatoReporte, $('#buscarDocumentoSeguiEstEmiBean').serialize(), function(data) {
            if (data !== null) {
                if (data.coRespuesta === "0") {
                    if (!!data.noUrl && !!data.noDoc) {

                        //fn_generaDocApplet(data.noUrl, data.noDoc, function(data) {
                        fn_generaDocDesktop(data.noUrl, data.noDoc, function(data) {
                            var result = data;
                            if (result !== "OK") {
                               bootbox.alert(result);
                            }
                        });

                    }
                } else {
                   bootbox.alert(data.deRespuesta);
                }
            } else {
               bootbox.alert("La respuesta del servidor es nula.");
            }
        }, 'json', false, true, "POST");
    }
    return false;
}



function mostrarOcultarDivFecha() {
	   
    if (jQuery('#fechaFiltro').is(':visible')) {
        jQuery('#fechaFiltro').hide();
      
        jQuery('#spanDivBusquedaF').removeClass("glyphicon-collapse-up");
        jQuery('#spanDivBusquedaF').addClass("glyphicon-collapse-down");
        
        jQuery('#mensajeFecha').html("No incluir filtro fecha");
        jQuery('#incluyeFiltroFecha').val("0");
    } else {
        jQuery('#fechaFiltro').show();
        jQuery('#fechaFiltro').removeClass("glyphicon-collapse-down");
        jQuery('#spanDivBusquedaF').addClass("glyphicon-collapse-up");
        jQuery('#mensajeFecha').html("Incluir filtro fecha");
        jQuery('#incluyeFiltroFecha').val("1");
    }

}

function fu_validaFiltroTraza() {
    var valRetorno = "1";

    jQuery('#buscarDocumentoSeguiEstEmiBean').find('#feEmiIni').val(jQuery('#coAnnio').parent('td').find('#fechaFiltro').attr('fini'));
    jQuery('#buscarDocumentoSeguiEstEmiBean').find('#feEmiFin').val(jQuery('#coAnnio').parent('td').find('#fechaFiltro').attr('ffin'));


    
    var pEsIncluyeFiltro= $('#esIncluyeFiltro1').val();
    
    var vFechaActual = jQuery('#txtFechaActual').val();
    
        valRetorno = fu_validaFiltroFecha(vFechaActual);
    
    return valRetorno;
}

function fu_validaFiltroFecha(vFechaActual) {
    var valRetorno = "1";
    if (valRetorno === "1") {
        fu_obtenerEsFiltroFechaConsulSeguiEstEmi('buscarDocumentoSeguiEstEmiBean');
        var pEsFiltroFecha = jQuery('#buscarDocumentoSeguiEstEmiBean').find("#esFiltroFecha").val();//"2" solo año ,"3" año mes,"1" rango fechas
        if (pEsFiltroFecha === "1" || pEsFiltroFecha === "2" || pEsFiltroFecha === "3") {
            if (pEsFiltroFecha === "2" || pEsFiltroFecha === "3") {
                var pAnnio = jQuery('#buscarDocumentoSeguiEstEmiBean').find('#coAnnio').val();
                if (pAnnio !== null && pAnnio !== "null" && typeof (pAnnio) !== "undefined" && pAnnio !== "") {
                    var vValidaNumero = fu_validaNumero(pAnnio);
                    if (vValidaNumero !== "OK") {
                       bootbox.alert("Año debe ser solo números.");
                        valRetorno = "0";
                    }
                }
            }

            var vFeInicio = jQuery('#buscarDocumentoSeguiEstEmiBean').find("#feEmiIni").val();
            var vFeFinal = jQuery('#buscarDocumentoSeguiEstEmiBean').find("#feEmiFin").val();

            if (valRetorno === "1" /*&& pEsFiltroFecha==="1"*/) {
                var pAnnioBusq = verificarAñoBusquedaFiltroSeguiEmi(vFeInicio, vFeFinal);
                if (pAnnioBusq !== null && pAnnioBusq !== "null" && typeof (pAnnioBusq) !== "undefined" && pAnnioBusq !== "") {
                    var vValidaNumero = fu_validaNumero(pAnnioBusq);
                    if (vValidaNumero !== "OK") {
                       bootbox.alert("Año debe ser solo números.");
                        valRetorno = "0";
                    } else if (vValidaNumero === "OK") {
                        jQuery('#buscarDocumentoSeguiEstEmiBean').find('#coAnnio').val(pAnnioBusq);
                    }
                }
            }

            if (pEsFiltroFecha === "1" /*|| pEsFiltroFecha==="3"*/) {
                //VALIDA FECHAS
                if (valRetorno === "1") {
                    if (vFeInicio === "") {
                       bootbox.alert('Debe ingresar Fecha Del');
                        valRetorno = "0";
                    } else {
                        valRetorno = fu_validaFechaConsulta(vFeInicio, vFechaActual);
                        if (valRetorno !== "1") {
                           bootbox.alert("Error en Fecha Del : " + valRetorno);
                            valRetorno = "0";
                        }
                    }
                }

                if (valRetorno === "1") {
                    //VALIDA FECHAS
                    if (vFeFinal === "") {
                       bootbox.alert('Debe ingresar Fecha Al');
                        valRetorno = "0";
                    } else {
                        if (pEsFiltroFecha === "3") {
                            vFechaActual = obtenerFechaUltimoDiaMes(vFechaActual);
                        }
                        valRetorno = fu_validaFechaConsulta(vFeFinal, vFechaActual);
                        if (valRetorno !== "1") {
                           bootbox.alert("Error en Fecha Al : " + valRetorno);
                            valRetorno = "0";
                        }
                    }
                }
                //se verifica que fechas DEL sea mayor o igual que fecha AL
                if (valRetorno === "1") {
                    var vCantidadDias = getNumeroDeDiasDiferencia(vFeInicio, vFeFinal);
                    if (vCantidadDias < 0) {
                       bootbox.alert("La Fecha Del debe ser mayor o igual a Fecha Al");
                        valRetorno = "0";
                    }
                }
            }
        }
    }
    return valRetorno;
}


function fu_cleanSeguiTrazabilidad() {
    
    jQuery("#busNumExpediente").val("");
    jQuery("#busAsunto").val("");
    jQuery("#busCodTipoDocRef option[value=]").prop("selected", "selected");
    jQuery("#feEmiIni").val("");
    jQuery("#feEmiFin").val("");
    jQuery("#busNumDocRef").val("");
    jQuery("#busCodDepEmiRef").val("");
    jQuery("#txtDepEmiteBus").val(" [TODOS]");
    jQuery("#esIncluyeFiltro1").prop('checked', false);
  
    jQuery("#busCoAnnio").find('option:first').prop("selected", "selected");
	 jQuery("#busNumDoc").val("");
    jQuery("#busNumExpediente").val("");
    jQuery("#busAsunto").val("");
    
    jQuery("#estadoDoc option[value=]").prop("selected", "selected");
    jQuery("#coRefOrigen").val("");
    jQuery("#txtRefOrigen").val("[TODOS]");
  
    jQuery("#coTipoDocAdm option[value=]").prop("selected", "selected");
    jQuery("#coRefOrigen").val("");
    jQuery("#txtRefOrigen").val("[TODOS]");
 //   jQuery("#coDepOrigen").val(jQuery('#buscarDocumentoEmiConsulBean').find("#coDependencia").val());

    jQuery("#coDepDestino").val("");
    jQuery("#txtDestinatario").val("[TODOS]");
    jQuery("#coEmpElaboro").val("");
    jQuery("#txtElaboradoPor").val("[TODOS]");
    jQuery("#coEmpDestino").val("");
    jQuery("#txtEmpDestino").val("[TODOS]");
    jQuery("#coExpediente option[value=]").prop("selected", "selected");
    jQuery("#esFiltroFecha").val("1");//rango fecha
    //var fechaActual = jQuery("#txtFechaActual").val();
    //jQuery("#fechaFiltro").html("Del: "+fechaActual+" Al: "+fechaActual);
    jQuery("#coAnnio").val(jQuery("#txtAnnioActual").val());
    //jQuery('#buscarDocumentoEmiConsulBean').find('#coAnnio').parent('td').find('#fechaFiltro').attr('fini',fechaActual);
    //jQuery('#buscarDocumentoEmiConsulBean').find('#coAnnio').parent('td').find('#fechaFiltro').attr('ffin',fechaActual);
    jQuery('#buscarDocumentoEmiConsulBean').find("#fechaFiltro").showDatePicker({defaultOpcionSelected: 1});

}
function mostrarOcultarDivBusTrazabilidad() {
	
	   
    if (jQuery('#divConfigFiltro').is(':visible')) {
        jQuery('#divConfigFiltro').hide();
        jQuery('#spanDivFiltro').removeClass("glyphicon-collapse-up");
        jQuery('#spanDivFiltro').addClass("glyphicon-collapse-down");
        
        jQuery("#txtDepOrigen").val("[TODOS]");
        jQuery("#coDepOrigen").val("");
        jQuery("#coTipoDocAdm option[value=]").prop("selected", "selected");
        jQuery("#coDepDestino").val("");
        jQuery("#txtDestinatario").val("[TODOS]");
        jQuery("#coEmpElaboro").val("");
        jQuery("#txtElaboradoPor").val("[TODOS]");
        jQuery("#coEmpDestino").val("");
        jQuery("#txtEmpDestino").val("[TODOS]");
        jQuery("#coRefOrigen").val("");
        jQuery("#txtRefOrigen").val("[TODOS]");
        
    } else {
        jQuery('#divConfigFiltro').show();
        jQuery('#spanDivFiltro').removeClass("glyphicon-collapse-down");
        jQuery('#spanDivFiltro').addClass("glyphicon-collapse-up");
        
        jQuery("#txtDepOrigen").val("[TODOS]");
        jQuery("#coDepOrigen").val("");
        jQuery("#coTipoDocAdm option[value=]").prop("selected", "selected");
        jQuery("#coDepDestino").val("");
        jQuery("#txtDestinatario").val("[TODOS]");
        jQuery("#coEmpElaboro").val("");
        jQuery("#txtElaboradoPor").val("[TODOS]");
        jQuery("#coEmpDestino").val("");
        jQuery("#txtEmpDestino").val("[TODOS]");
        jQuery("#coRefOrigen").val("");
        jQuery("#txtRefOrigen").val("[TODOS]");
    }
    
}
 function fn_buscaDestinoTraza(campo1,campo2) {
        var p = new Array();
        p[0] = "accion=goBuscaDependencia";
        p[1] = "campo1="+campo1;
        p[2] = "campo2="+campo2;
       // p[1] = jQuery('#buscarDocumentoSeguiEstEmiBean').find('#txtDepEmiteBus').val() === ' [TODOS]' ? "pdeDepEmite=" : "pdeDepEmite=" + fu_getValorUpperCase(jQuery('#buscarDocumentoSeguiEstEmiBean').find('#txtDepEmiteBus').val());
        ajaxCall("/srConsultaTrazabilidad.do", p.join("&"), function(data) {
        	fn_rptaBuscaDependenciaTraza(data,campo1,campo2);
        },
        'text', false, false, "POST");
    }
    function fn_rptaBuscaDependenciaTraza(XML_AJAX,campo1,campo2) {
        if (XML_AJAX !== null) {
            var obj = null;
            try{obj = jQuery.parseJSON(XML_AJAX);}catch(e){}
            if(obj){
                if(obj.coRespuesta==="1"){

                    jQuery('#buscarDocumentoSeguiEstEmiBean').find('#'+campo1).val(obj.coDependencia);
                    jQuery('#buscarDocumentoSeguiEstEmiBean').find('#'+campo2).val(obj.deDependencia);
                    jQuery('#buscarDocumentoSeguiEstEmiBean').find('#'+campo1).focus();
                }else{
                   bootbox.alert(obj.deRespuesta);
                }
            }else{
                $("body").append(XML_AJAX);
            }        
        }    
    }
  function fu_setDatoDependencia2(cod, desc,campo2,campo1) {
   jQuery('#buscarDocumentoSeguiEstEmiBean').find('#'+campo1).val(desc);
   jQuery('#buscarDocumentoSeguiEstEmiBean').find('#'+campo2).val(cod);
 
  removeDomId('windowConsultaDestinoEmi');
}
    
//    function fu_FiltrarTecladoCadenaFunParamGeneral(evt, autoSubmit, pvalidKeys, callbackFunction, campo1,campo2) {
//        eventEvaluate(evt);
//        var key = window.event ? oEvent.keyCode : oEvent.which;
//        if (!(key === 37)) {
//            var tk = new KeyboardClass(oEvent, pvalidKeys);
//            if (tk.isIntro()) {
//                callbackFunction(campo1,campo2);
//            }
//            return (((autoSubmit) ? autoSubmit : false) === true) ? tk.isValidKey : (tk.isValidKey && tk.k !== 13);
//        }
//        return false;
//    }
//    
//    function fn_buscaDestinoTraza2() {
//        var p = new Array();
//        p[0] = "accion=goBuscaDependenciaEmite";
//        p[1] = jQuery('#buscarDocumentoRecepConsulBean').find('#txtDepEmiteBus').val() === ' [TODOS]' ? "pdeDepEmite=" : "pdeDepEmite=" + fu_getValorUpperCase(jQuery('#buscarDocumentoRecepConsulBean').find('#txtDepEmiteBus').val());
//        ajaxCall("/srConsultaRecepcionDoc.do", p.join("&"), function(data) {
//        	fn_rptaBuscaDependenciaTraza(data);
//        },
//        'text', false, false, "POST");
//    }
//    
//    function fu_setDatoDependencia2(cod, desc,campo2,campo1) {
//        jQuery('#buscarDocumentoSeguiEstEmiBean').find('#'+campo2).val(desc);
//        jQuery('#buscarDocumentoSeguiEstEmiBean').find('#'+campo1).val(cod);
//       
//        removeDomId('windowConsultaDestinoEmi');
//    }

//    function fn_rptaBuscaDependenciaRecepConsul(XML_AJAX) {
//        if (XML_AJAX !== null) {
//            var obj = null;
//            try{obj = jQuery.parseJSON(XML_AJAX);}catch(e){}
//            if(obj){
//                if(obj.coRespuesta==="1"){
//                    jQuery('#buscarDocumentoRecepConsulBean').find('#busCodDepEmiRef').val(obj.coDependencia);
//                    jQuery('#buscarDocumentoRecepConsulBean').find('#txtDepEmiteBus').val(obj.deDependencia);
//                    jQuery('#buscarDocumentoRecepConsulBean').find('#busCodTipoDocRef').focus();
//                }else{
//                   bootbox.alert(obj.deRespuesta);
//                }
//            }else{
//                $("body").append(XML_AJAX);
//            }        
//        }      
//    }


