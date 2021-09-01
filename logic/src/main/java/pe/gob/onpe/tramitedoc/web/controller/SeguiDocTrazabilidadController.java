package pe.gob.onpe.tramitedoc.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.libreria.util.ServletUtility;
import pe.gob.onpe.libreria.util.Utility;
import pe.gob.onpe.tramitedoc.bean.BuscarDocTrazabilidadBean;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoSeguiEstEmiBean;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiSeguiBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoSeguiTrazaBean;
import pe.gob.onpe.tramitedoc.bean.ReporteBean;
import pe.gob.onpe.tramitedoc.bean.UsuarioConfigBean;
import pe.gob.onpe.tramitedoc.service.CommonQryService;
import pe.gob.onpe.tramitedoc.service.ReferencedData;
import pe.gob.onpe.tramitedoc.service.SeguiEstEmitidoService;
import pe.gob.onpe.tramitedoc.service.SeguiTrazabilidadService;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.SerforDateUtils;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;


/**
*
* @author EJHM
*/
@Controller
@RequestMapping("/{version}/srConsultaTrazabilidad.do")
public class SeguiDocTrazabilidadController {
	
	 @Autowired
	 private ReferencedData referencedData;
	 
	 @Autowired
	 private CommonQryService commonQryService;
	 
	
	  
	 @Autowired
	 private SeguiTrazabilidadService seguiTrazabilidadService;
	 
	 @Autowired
	 private  SeguiEstEmitidoService seguiEstEmitidoService;
	    
	  @Autowired
	  private ApplicationProperties applicationProperties;
	
	
    @RequestMapping(method = RequestMethod.GET, params = "accion=goInicio")
    public String goInicio(HttpServletRequest request, Model model) {
        String codDependencia = ServletUtility.getInstancia().loadRequestParameter(request, "coDep");
        String annio = Utility.getInstancia().dateToFormatStringYYYY(new Date());
        String pEstado = ServletUtility.getInstancia().loadRequestParameter(request, "estadoDoc");
        BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean = new BuscarDocTrazabilidadBean();
        
        buscarDocumentoSeguiEstEmiBean.setCoAnnio(annio);
        buscarDocumentoSeguiEstEmiBean.setBusCoAnnio(annio);
        buscarDocumentoSeguiEstEmiBean.setCoDependencia(codDependencia);
        
        buscarDocumentoSeguiEstEmiBean.setCoDepOrigen(codDependencia);
        //buscarDocumentoSeguiEstEmiBean.setCoVencimiento("3");

        model.addAttribute("buscarDocumentoSeguiEstEmiBean",buscarDocumentoSeguiEstEmiBean);
        model.addAttribute("deAnnioList", referencedData.getAnnioList());
        
     //   model.addAttribute("deEstadosList", referencedData.getLstEstadosDocumentoEmiSegui("TDTV_DESTINOS"));
        
        model.addAttribute("deEstadosList",referencedData.getLstEstadosSoloDocumentoEmi("TDTV_REMITOS"));
        model.addAttribute("dePrioridadesList", referencedData.getPrioridadesDocumentoList());
        model.addAttribute("deTipoDocumentoList", referencedData.getTipoDocumentoEmiList(codDependencia));
       
        model.addAttribute("deEtiquetasList", referencedData.getEtiquetasList());
      //  model.addAttribute("deVencimientoList", referencedData.getVencimientoList());
        model.addAttribute("deDepenciaActual", commonQryService.getDependenciaxCoDependencia(codDependencia).getDeDependencia());

        model.addAttribute("fechaActual", Utility.getInstancia().dateToFormatString(new Date()));
        model.addAttribute("annioActual", Utility.getInstancia().dateToFormatStringYYYY(new Date()));
        return "/segTrazabilidad/segTrazabilidadDocu";
    }
    
    @RequestMapping(method = RequestMethod.POST, params = "accion=goInicio")
    public String goInicio(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean, HttpServletRequest request, BindingResult result, Model model) {
        String mensaje = "NO_OK";
        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
       String codDependencia = usuario.getCoDep();
        String codEmpleado = usuario.getCempCodemp();
        String tipAcceso = usuarioConfigBean.getTiConsulta();
        buscarDocumentoSeguiEstEmiBean.setCoDependencia(codDependencia);
        buscarDocumentoSeguiEstEmiBean.setCoEmpleado(codEmpleado);
        buscarDocumentoSeguiEstEmiBean.setTiAcceso(tipAcceso);

        List<DocumentoSeguiTrazaBean> list = new ArrayList<DocumentoSeguiTrazaBean>();

        try {
        	String mensaje2=validacionesBasicas( buscarDocumentoSeguiEstEmiBean);
        	if( mensaje2 !=null) {
        		  model.addAttribute("pMensaje", mensaje2);
        		  model.addAttribute("docTrabazabList", null);
                  return "/segTrazabilidad/tblSegTrazabilidad";
        		
        	}

            list = seguiTrazabilidadService.getListDocSegui(buscarDocumentoSeguiEstEmiBean);
        } catch (Exception ex) {
            mensaje = ex.getMessage();
        }

        if (list != null && !list.isEmpty()) {
            if (list.size() >= 300) {
                model.addAttribute("msjEmision", "OPTIMICE SU CONSULTA MAS DE 300 REGISTROS");
            }
            model.addAttribute("docTrabazabList", list);
            mensaje = "OK";
        } else {
            mensaje = "No se encuentran registros.";
        }
        
        if (mensaje.equals("OK")) {
        	  model.addAttribute("pMensaje", "");
            return "/segTrazabilidad/tblSegTrazabilidad";
        } else {
        	
        	 model.addAttribute("pMensaje", "");
            model.addAttribute("docTrabazabList", list);
            return "/segTrazabilidad/tblSegTrazabilidad";
        }
    }
   
    public String validacionesBasicas(BuscarDocTrazabilidadBean busqueda) {
    	 Date fechaInicio=  SerforDateUtils.getDateFromUnknownFormat( busqueda.getFeEmiIni());
		 Date fechaFin=SerforDateUtils.getDateFromUnknownFormat( busqueda.getFeEmiFin());
		 
    	 int dias=(int) ((fechaFin.getTime()-fechaInicio.getTime())/86400000)+1;
    	
    	boolean nuDocVacio=false;
		boolean nuExpVacio=false;
	
	  if(Utilidades.esVacio(busqueda.getBusNumDoc()) || 
			 busqueda.getBusNumDoc().trim().length()<3  )	{
		  nuDocVacio=true;  
	  }  else 
		  if(Utilidades.esCero(busqueda.getBusNumDoc() )) {
			  nuDocVacio=true;   
		  }
	  if (Utilidades.esVacio(busqueda.getBusNumExpediente()) || 
			  busqueda.getBusNumExpediente().length()<4 ){
   		  
		  nuExpVacio=true;
		 
	   }
	  else 
	 if(Utilidades.esCero(busqueda.getBusNumExpediente()) ){
		 nuExpVacio=true;   
	 }
    if(busqueda.getIncluyeFiltroFecha()!=null && busqueda.getIncluyeFiltroFecha().equals("0") || dias>=365) {
    	 if(!Utilidades.esVacio(busqueda.getBusCodDepEmiRef())&& !Utilidades.esVacio(busqueda.getBusAsunto()) && busqueda.getBusAsunto().length()>9) {
			  return null;
		  }
	     if( !Utilidades.esVacio(busqueda.getBusAsunto()) && busqueda.getBusAsunto().length()>15) {
			  return null;
	      }
    	  if(nuDocVacio && nuExpVacio ) {
    		  return  "Número de Documento debe contener como mínio 3 caracteres distintos de cero  o Número de expediente debe contener como mínio 4 caracteres caracteres distintos cero ";
    		  
    	  }
//    	  if(nuDocVacio && nuExpVacio && !Utilidades.esVacio(busqueda.getBusCodDepEmiRef()) &&  Utilidades.esVacio(busqueda.getEstadoDoc())) {
//    		  return  "Número de Documento debe contener como mínio 3 caracteres distintos de cero  o Número de expediente debe contener como mínio 4 caracteres caracteres distintos cero ";
//    		  
//    	  }

    	  if(Utilidades.esVacio(busqueda.getBusNumExpediente()) && !Utilidades.esVacio(busqueda.getBusNumDoc()) 
    			 && Utilidades.esVacio(busqueda.getBusCodDepEmiRef()) &&  Utilidades.esVacio(busqueda.getCoTipoDocAdm())) {
    		  return  "  Seleccione unidad organica o tipo de documento";
    	  }
    	  
    	  if(Utilidades.esVacio(busqueda.getBusNumExpediente()) && !Utilidades.esVacio(busqueda.getBusNumDoc()) 
    			  &&  nuDocVacio  &&  !Utilidades.esVacio(busqueda.getBusCodDepEmiRef())) {
    		  return  "  Número de documento debe contener como mínimo 3 caracteres diferentes de cero";
    	  }
    	}
    	  else {
    		  if(!Utilidades.esVacio(busqueda.getBusCodDepEmiRef())&& !Utilidades.esVacio(busqueda.getBusAsunto()) && busqueda.getBusAsunto().length()>9) {
    			  return null;
    		  }
    	     if( !Utilidades.esVacio(busqueda.getBusAsunto()) && busqueda.getBusAsunto().length()>15) {
    			  return null;
    	      }
    		
    	
    		// int dias=(int) ((fechaFin.getTime()-fechaInicio.getTime())/86400000)+1;
    		 if(fechaInicio==null) {
    			 return "fecha de inicio no puede ser vacío"; 
    		 }
    		 if(fechaInicio==null) {
    			 return "fecha de fin no puede ser vacío"; 
    		 }
    		 //estadoDoc
    		 if(nuDocVacio && nuExpVacio && Utilidades.esVacio(busqueda.getBusCodDepEmiRef())  && Utilidades.esVacio(busqueda.getEstadoDoc())
    				  && Utilidades.esVacio(busqueda.getCoDepDestino()) && Utilidades.esVacio(busqueda.getCoEmpElaboro()) && Utilidades.esVacio(busqueda.getCoRefOrigen()) &&  (dias)>8) {
    			  return  "Rango de Fechas debe ser menor a 8 dias "; 
    		 }
    		 if(nuDocVacio && nuExpVacio && Utilidades.esVacio(busqueda.getBusCodDepEmiRef())  && Utilidades.esVacio(busqueda.getEstadoDoc())
   				  && !Utilidades.esVacio(busqueda.getCoDepDestino()) &&   Utilidades.esVacio(busqueda.getCoEmpElaboro()) && Utilidades.esVacio(busqueda.getCoRefOrigen()) && (dias)>31) {
   			  return  "Rango de Fechas debe ser menor a 31 dias "; 
    		 }
    		 if(nuDocVacio && nuExpVacio && !Utilidades.esVacio(busqueda.getBusCodDepEmiRef())  && Utilidades.esVacio(busqueda.getEstadoDoc())
      				  && Utilidades.esVacio(busqueda.getCoDepDestino()) &&   Utilidades.esVacio(busqueda.getCoEmpElaboro()) && Utilidades.esVacio(busqueda.getCoRefOrigen())  && (dias)>31) {
      			  return  "Rango de Fechas debe ser menor a 31 dias "; 
       		 }
    		 if(nuDocVacio && nuExpVacio && Utilidades.esVacio(busqueda.getBusCodDepEmiRef())  && Utilidades.esVacio(busqueda.getEstadoDoc())
      				  && !Utilidades.esVacio(busqueda.getCoDepDestino()) &&   Utilidades.esVacio(busqueda.getCoEmpElaboro()) && Utilidades.esVacio(busqueda.getCoRefOrigen())   && (dias)>31) {
      			  return  "Rango de Fechas debe ser menor a 31 dias "; 
       		 }
    		 if(nuDocVacio && nuExpVacio && Utilidades.esVacio(busqueda.getBusCodDepEmiRef())  && Utilidades.esVacio(busqueda.getEstadoDoc())
     				  && Utilidades.esVacio(busqueda.getCoDepDestino()) &&   Utilidades.esVacio(busqueda.getCoEmpElaboro()) && !Utilidades.esVacio(busqueda.getCoRefOrigen())   && (dias)>31) {
     			  return  "Rango de Fechas debe ser menor a 31 dias "; 
      		 }
    		 if(nuDocVacio && nuExpVacio && Utilidades.esVacio(busqueda.getBusCodDepEmiRef())  && Utilidades.esVacio(busqueda.getEstadoDoc())
    				  && Utilidades.esVacio(busqueda.getCoDepDestino()) &&   !Utilidades.esVacio(busqueda.getCoEmpElaboro()) && Utilidades.esVacio(busqueda.getCoRefOrigen())   && (dias)>31) {
    			  return  "Rango de Fechas debe ser menor a 31 dias "; 
     		 }
    		 if(nuDocVacio && nuExpVacio && Utilidades.esVacio(busqueda.getBusCodDepEmiRef())  && Utilidades.esVacio(busqueda.getEstadoDoc())
   				  && !Utilidades.esVacio(busqueda.getCoDepDestino()) &&   !Utilidades.esVacio(busqueda.getCoEmpElaboro()) && Utilidades.esVacio(busqueda.getCoRefOrigen())   && (dias)>61) {
   			  return  "Rango de Fechas debe ser menor a 61 dias "; 
    		 }
    		 if(nuDocVacio && nuExpVacio && Utilidades.esVacio(busqueda.getBusCodDepEmiRef())  && Utilidades.esVacio(busqueda.getEstadoDoc())
    				  && !Utilidades.esVacio(busqueda.getCoDepDestino()) &&   Utilidades.esVacio(busqueda.getCoEmpElaboro()) && !Utilidades.esVacio(busqueda.getCoRefOrigen())   && (dias)>61) {
    			  return  "Rango de Fechas debe ser menor a 61 dias "; 
     		 }
    		 if(nuDocVacio && nuExpVacio && !Utilidades.esVacio(busqueda.getBusCodDepEmiRef())  && Utilidades.esVacio(busqueda.getEstadoDoc())
   				  && Utilidades.esVacio(busqueda.getCoDepDestino()) &&   Utilidades.esVacio(busqueda.getCoEmpElaboro()) && !Utilidades.esVacio(busqueda.getCoRefOrigen())   && (dias)>61) {
   			  return  "Rango de Fechas debe ser menor a 61 dias "; 
    		 }
    		 
    		 if(nuDocVacio && nuExpVacio && !Utilidades.esVacio(busqueda.getBusCodDepEmiRef())  && Utilidades.esVacio(busqueda.getEstadoDoc())
     				  && !Utilidades.esVacio(busqueda.getCoDepDestino()) &&   Utilidades.esVacio(busqueda.getCoEmpElaboro())   && (dias)>61) {
     			  return  "Rango de Fechas debe ser menor a 61 dias "; 
      		 }
    		 if(nuDocVacio && nuExpVacio && Utilidades.esVacio(busqueda.getBusCodDepEmiRef())  && Utilidades.esVacio(busqueda.getEstadoDoc())
      				  && !Utilidades.esVacio(busqueda.getCoDepDestino()) &&   !Utilidades.esVacio(busqueda.getCoEmpElaboro()) && (dias)>91) {
      			  return  "Rango de Fechas debe ser menor a 91 dias "; 
       		 }
    		 if(nuDocVacio && nuExpVacio && (Utilidades.esVacio(busqueda.getBusCodDepEmiRef()) 
      				 && !Utilidades.esVacio(busqueda.getCoDepDestino())) &&   !Utilidades.esVacio(busqueda.getCoEmpElaboro()) 
      				  && Utilidades.esVacio(busqueda.getEstadoDoc()) &&  (dias)>92) {
   			  return  "Rango de Fechas debe ser menor a 91 dias "; 
   		     }
    		 if(nuDocVacio && nuExpVacio && !Utilidades.esVacio(busqueda.getBusCodDepEmiRef()) 
      				 && Utilidades.esVacio(busqueda.getCoDepDestino()) &&   !Utilidades.esVacio(busqueda.getCoEmpElaboro()) 
      				  && Utilidades.esVacio(busqueda.getEstadoDoc()) &&  (dias)>92) {
   			  return  "Rango de Fechas debe ser menor a 91 dias "; 
   		     }
    		 if(nuDocVacio && nuExpVacio && !Utilidades.esVacio(busqueda.getBusCodDepEmiRef()) 
      				 && !Utilidades.esVacio(busqueda.getCoDepDestino()) &&   Utilidades.esVacio(busqueda.getCoEmpElaboro()) 
      				  && Utilidades.esVacio(busqueda.getEstadoDoc()) &&  (dias)>92) {
   			  return  "Rango de Fechas debe ser menor a 61 dias "; 
   		     }
    		 if(nuDocVacio && nuExpVacio && !Utilidades.esVacio(busqueda.getBusCodDepEmiRef())  && Utilidades.esVacio(busqueda.getEstadoDoc())
   				  && !Utilidades.esVacio(busqueda.getCoDepDestino()) && !Utilidades.esVacio(busqueda.getCoEmpElaboro()) &&  (dias)>121) {
   			  return  "Rango de Fechas debe ser menor a 120 dias "; 
    		 }
    		 if(nuDocVacio && nuExpVacio && !Utilidades.esVacio(busqueda.getBusCodDepEmiRef())  && !Utilidades.esVacio(busqueda.getEstadoDoc())
      				  && Utilidades.esVacio(busqueda.getCoDepDestino()) && !Utilidades.esVacio(busqueda.getCoEmpElaboro()) &&  (dias)>121) {
      			  return  "Rango de Fechas debe ser menor a 120 dias "; 
       		 }
    		 if(nuDocVacio && nuExpVacio && !Utilidades.esVacio(busqueda.getBusCodDepEmiRef())  && !Utilidades.esVacio(busqueda.getEstadoDoc())
     				  && !Utilidades.esVacio(busqueda.getCoDepDestino()) && Utilidades.esVacio(busqueda.getCoEmpElaboro()) &&  (dias)>121) {
     			  return  "Rango de Fechas debe ser menor a 120 dias "; 
      		 }
   		 
    		 
   		
    		
//    		 if(nuDocVacio && nuExpVacio && !Utilidades.esVacio(busqueda.getBusCodDepEmiRef()) && Utilidades.esVacio(busqueda.getEstadoDoc()) && dias>35 ) {
//    			 return  "Rango de Fechas debe ser menor a 32 dias "; 
//    		 }
//    		 if(!nuDocVacio || !nuExpVacio) && (dias+1)>62) {
//   			  return  "Rango de Fechas debe ser menor a 62 dias "; 
  // 		 }
    		 
    		  
    	  }
    
    	
    	 return null;
    }

    @RequestMapping(method = RequestMethod.POST, params = "accion=goDetalleDocumento")
    public String goDetalleDocumento(HttpServletRequest request, Model model) {
        String mensaje = "NO_OK";
        String snuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
        String snuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pnuEmi");
        String snuDes = ServletUtility.getInstancia().loadRequestParameter(request, "pnuDes");
        //Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
        String fechaHoraActual = Utility.getInstancia().dateToFormatString2(new Date());
        DocumentoEmiSeguiBean documentoEmiSeguiBean;
        List list;
        try {
            documentoEmiSeguiBean = seguiTrazabilidadService.getDocumentoEmiSeguiBean(snuAnn, snuEmi, snuDes);
            list = seguiTrazabilidadService.getDocumentosRefEmiAdmSegui(snuAnn, snuEmi);
            model.addAttribute("pfechaHoraActual", fechaHoraActual);
//           model.addAttribute("pcodEmp",usuario.getCempCodemp());
//           model.addAttribute("pdesEmp",usuario.getDeFullName());
            model.addAttribute("documentoEmiSeguiBean", documentoEmiSeguiBean);
            model.addAttribute("refRecepDocumAdmList", list);
            mensaje = "OK";
        } catch (Exception ex) {
            mensaje = ex.getMessage();
        }
        if (mensaje.equals("OK")) {
            return "/segTrazabilidad/detEmiDocumAdm";
        } else {
            model.addAttribute("pMensaje", mensaje);
            return "respuesta";
        }
    }
    
    @RequestMapping(method = RequestMethod.POST, params = "accion=goBuscaDestinatario")
    public String goBuscaDestinatario(HttpServletRequest request, Model model) {
//        String pcoDepen = ServletUtility.getInstancia().loadRequestParameter(request, "pcoDepen");
//        model.addAttribute("listaDestinatario", commonQryService.getListRemitente(pcoDepen));
//        return "/modalGeneral/consultaDestinatarioEmiSegui";
        String pnuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
        UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
        model.addAttribute("iniFuncionParm", "1");
        model.addAttribute("listaDestinatario", referencedData.getListDestinatarioEmi(pnuAnn, usuarioConfigBean));
        return "/modalGeneral/consultaDestinatarioEmiConsulSeguiEst";
    }

    @RequestMapping(method = RequestMethod.POST, params = "accion=goBuscaEmpDestino")
    public String goBuscaEmpDestino(HttpServletRequest request, Model model) {
        String pcoDepen = ServletUtility.getInstancia().loadRequestParameter(request, "pcoDepen");
        return "/modalGeneral/consultaEmpDestinoRecepSegui";
    }
    //goBuscaDestinatario
    
    @RequestMapping(method = RequestMethod.POST, params = "accion=goExportarArchivoLista") 
    public @ResponseBody String goExportarArchivoLista(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean,HttpServletRequest request,  BindingResult result, Model model) throws IOException  {
    	StringBuilder retval=null;
        ServletContext sc = request.getSession().getServletContext();
        
    
		try {
		    String coReporte = ServletUtility.getInstancia().loadRequestParameter(request, "pformatRepor");
	        
	        Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
	        UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
	        String codDependencia = usuario.getCoDep();
	        String codEmpleado = usuario.getCempCodemp();
	        String tipAcceso = usuarioConfigBean.getTiConsulta();
	        String anio = ServletUtility.getInstancia().loadRequestParameter(request, "coAnnio");
	        
	        String rutaReporte=sc.getRealPath("/reports/");
//	        String rutaImagen = sc.getRealPath("/reports/logo_onpe.jpg");
	        String logo = applicationProperties.getLogoReporteB64();                
	        ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(logo));
	        
	        Map parametros = new HashMap();
	        parametros.put("P_DE_DEPENDENCIA", usuarioConfigBean.getDeDep());
	        parametros.put("P_USER", usuario.getCoUsuario());
	        parametros.put("P_LOGO_DIR", bis.available()==0?null:bis);  
	        
	        buscarDocumentoSeguiEstEmiBean.setCoDependencia(codDependencia);
	        buscarDocumentoSeguiEstEmiBean.setCoEmpleado(codEmpleado);
	        buscarDocumentoSeguiEstEmiBean.setTiAcceso(tipAcceso);        
	        buscarDocumentoSeguiEstEmiBean.setRutaReporteJasper(rutaReporte);
	        buscarDocumentoSeguiEstEmiBean.setFormatoReporte(coReporte);
	        
	        ReporteBean objReporteBean; //BuscarDocumentoSeguiEstEmiBean
			objReporteBean = seguiTrazabilidadService.getGenerarReporte(buscarDocumentoSeguiEstEmiBean, parametros);
			if(bis!=null)bis.close();
	         retval = new StringBuilder();
	        retval.append("{\"coRespuesta\":\"");        
	        retval.append(objReporteBean.getcoRespuesta());
	        retval.append("\",\"deRespuesta\":\"");        
	        retval.append(objReporteBean.getdeRespuesta());
	        retval.append("\",\"noUrl\":\"");
	        retval.append(objReporteBean.getnoUrl());
	        retval.append("\",\"noDoc\":\"");
	        retval.append(objReporteBean.getnoDoc());
	        retval.append("\"}");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			  retval = new StringBuilder();
		        retval.append("{\"coRespuesta\":\"");        
		        retval.append("1");
		        retval.append("\",\"deRespuesta\":\"");        
		        retval.append(e.getMessage());
		        retval.append("\",\"noUrl\":\"");
		        retval.append("");
		        retval.append("\",\"noDoc\":\"");
		        retval.append("");
		        retval.append("\"}");
		}
        
        
        return retval.toString();
    }
    
    @RequestMapping(method = RequestMethod.POST, params = "accion=goBuscaDependencia")
    public String goBuscaDependenciaEmite(HttpServletRequest request, Model model){
        String pdeDepEmite = ServletUtility.getInstancia().loadRequestParameter(request, "pdeDepEmite");
        String campo1 = ServletUtility.getInstancia().loadRequestParameter(request, "campo1");
        String campo2 = ServletUtility.getInstancia().loadRequestParameter(request, "campo2");
        UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
        String vResult="";
        String coRespuesta;
        StringBuilder retval = new StringBuilder();
        HashMap map;
        
        try{
            map = seguiEstEmitidoService.getBuscaDependenciaEmite(usuarioConfigBean.getCoDep(),pdeDepEmite);
            String vReturn = (String)map.get("vReturn");
            if(vReturn.equals("1")){
                model.addAttribute("listaDependenciaDestEmi",map.get("listaDestinatario"));
                model.addAttribute("campo1",campo1);
                model.addAttribute("campo2",campo2);
                vResult = "/modalGeneral/consultaDependencia";
            }else if(vReturn.equals("0")){
                DependenciaBean dependenciaBean = (DependenciaBean)map.get("objDestinatario");
                coRespuesta = "1";
                retval.append("{\"coRespuesta\":\"");
                retval.append(coRespuesta);
                retval.append("\",\"coDependencia\":\"");
                retval.append(dependenciaBean.getCoDependencia());                
                retval.append("\",\"deDependencia\":\"");
                retval.append(dependenciaBean.getDeDependencia());                
                retval.append("\"}"); 
                model.addAttribute("pMensaje", retval.toString());
                vResult = "respuesta";
            }else{
                coRespuesta = "0";
                retval.append("{\"coRespuesta\":\"");
                retval.append(coRespuesta);
                retval.append("\",\"deRespuesta\":\"");
                retval.append("Error: No se puede obtener Dependencia.");                
                retval.append("\"}"); 
                model.addAttribute("pMensaje", retval.toString());
                vResult = "respuesta";
            }            
        }catch(Exception ex){
            ex.printStackTrace();
        }        
        return vResult;        
    }
    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goBuscaElaboradoPor"})
    public String goBuscaElaboradoPor(HttpServletRequest request, Model model) {
      UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    //  String pcoDep = ServletUtility.getInstancia().loadRequestParameter(request, "pcoDep");
      String campo1 = ServletUtility.getInstancia().loadRequestParameter(request, "campo1");
      String campo2 = ServletUtility.getInstancia().loadRequestParameter(request, "campo2");
      model.addAttribute("campo1", campo1);
      model.addAttribute("campo2", campo2);
      model.addAttribute("listaEmpleado", referencedData.getListEmpleado());
      return "/modalGeneral/consultaEmpleadoUlt";
    }
    
    @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goBuscaReferenciaOrigen"})
    public String goBuscaReferenciaOrigen(HttpServletRequest request, Model model) {
      
      Calendar cal= Calendar.getInstance();
      int year= cal.get(Calendar.YEAR);
      String campo1 = ServletUtility.getInstancia().loadRequestParameter(request, "campo1");
      String campo2 = ServletUtility.getInstancia().loadRequestParameter(request, "campo2");
      model.addAttribute("campo1", campo1);
      model.addAttribute("campo2", campo2);
      model.addAttribute("listaReferenOrig", referencedData.getListReferenciaOrigen(year));
      return "/modalGeneral/consultaReferenciaOrigenConsulUlt";
    }



}
