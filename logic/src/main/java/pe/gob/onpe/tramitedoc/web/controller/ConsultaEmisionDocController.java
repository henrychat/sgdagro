package pe.gob.onpe.tramitedoc.web.controller;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import net.sf.jasperreports.engine.JRException;
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
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoEmiConsulBean;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiConsulBean;
import pe.gob.onpe.tramitedoc.bean.ReporteBean;
import pe.gob.onpe.tramitedoc.bean.UsuarioConfigBean;
import pe.gob.onpe.tramitedoc.service.CommonQryService;
import pe.gob.onpe.tramitedoc.service.ConsultaEmiDocService;
import pe.gob.onpe.tramitedoc.service.EmiDocumentoAdmService;
import pe.gob.onpe.tramitedoc.service.ReferencedData;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;














@Controller
@RequestMapping({"/{version}/srConsultaEmisionDoc.do"})
public class ConsultaEmisionDocController
{
  @Autowired
  private CommonQryService commonQryService;
  @Autowired
  private ReferencedData referencedData;
  @Autowired
  private EmiDocumentoAdmService emiDocumentoAdmService;
  @Autowired
  private ConsultaEmiDocService consultaEmiDocService;
  @Autowired
  private ApplicationProperties applicationProperties;
  
  @RequestMapping(method = {RequestMethod.GET}, params = {"accion=goInicio"})
  public String goInicio(HttpServletRequest request, Model model) {
    String codDependencia = ServletUtility.getInstancia().loadRequestParameter(request, "coDep");
    String pEstado = ServletUtility.getInstancia().loadRequestParameter(request, "estadoDoc");
    
    BuscarDocumentoEmiConsulBean buscarDocumentoEmiConsulBean = new BuscarDocumentoEmiConsulBean();
    String sCoAnnio = Utility.getInstancia().dateToFormatStringYYYY(new Date());
    buscarDocumentoEmiConsulBean.setCoAnnio(sCoAnnio);
    buscarDocumentoEmiConsulBean.setBusCoAnnio(sCoAnnio);
    buscarDocumentoEmiConsulBean.setEstadoDoc(pEstado);
    buscarDocumentoEmiConsulBean.setCoDependencia(codDependencia);
    buscarDocumentoEmiConsulBean.setCoDepOrigen(codDependencia);
    
    model.addAttribute(buscarDocumentoEmiConsulBean);
    model.addAttribute("deAnnioList", this.referencedData.getAnnioList());
    model.addAttribute("deEstadosList", this.referencedData.getLstEstadosDocumentoEmi("TDTV_REMITOS"));
    model.addAttribute("dePrioridadesList", this.referencedData.getPrioridadesDocumentoList());
    model.addAttribute("deTipoDocumentoList", this.referencedData.getTipoDocumentoEmiList(codDependencia));
    
    model.addAttribute("deDepenciaActual", this.commonQryService.getDependenciaxCoDependencia(codDependencia).getDeDependencia());
    model.addAttribute("fechaActual", Utility.getInstancia().dateToFormatString(new Date()));
    model.addAttribute("annioActual", Utility.getInstancia().dateToFormatStringYYYY(new Date()));
    return "/consultaEmiDoc/consultaEmiDoc";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goInicio"})
  public String goInicio(BuscarDocumentoEmiConsulBean buscarDocumentoEmiConsulBean, HttpServletRequest request, BindingResult result, Model model) {
    String mensaje = "NO_OK";
    
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    String codDependencia = usuario.getCoDep();
    String codEmpleado = usuario.getCempCodemp();
    String tipAcceso = usuarioConfigBean.getTiConsulta();
    buscarDocumentoEmiConsulBean.setCoDependencia(codDependencia);
    buscarDocumentoEmiConsulBean.setCoEmpleado(codEmpleado);
    buscarDocumentoEmiConsulBean.setTiAcceso(tipAcceso);
    
    List list = null;
    
    try {
      list = this.consultaEmiDocService.getDocumentosBuscaEmiAdm(buscarDocumentoEmiConsulBean);
    } catch (Exception ex) {
      mensaje = ex.getMessage();
    } 
    
    if (list != null) {
      if (list.size() >= 300) {
        model.addAttribute("msjEmision", "OPTIMICE SU CONSULTA");
      }
      model.addAttribute("emiDocumAdmList", list);
      mensaje = "OK";
    } else {
      mensaje = "No se encuentran registros.";
    } 
    if (mensaje.equals("OK")) {
      return "/consultaEmiDoc/tblDocEmitidos";
    }
    model.addAttribute("pMensaje", mensaje);
    return "respuesta";
  }

  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goBuscaDependenciaEmite"})
  public String goBuscaDependenciaEmite(HttpServletRequest request, Model model) {
    String pdeDepEmite = ServletUtility.getInstancia().loadRequestParameter(request, "pdeDepEmite");
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    String vResult = "";
    
    StringBuilder retval = new StringBuilder();

    
    try {
      HashMap map = this.emiDocumentoAdmService.getBuscaDependenciaEmite(usuarioConfigBean.getCoDep(), pdeDepEmite);
      String vReturn = (String)map.get("vReturn");
      if (vReturn.equals("1")) {
        model.addAttribute("listaDependenciaDestEmi", map.get("listaDestinatario"));
        vResult = "/modalGeneral/consultaDependenciaEmiConsul";
      } else if (vReturn.equals("0")) {
        DependenciaBean dependenciaBean = (DependenciaBean)map.get("objDestinatario");
        String coRespuesta = "1";
        retval.append("{\"coRespuesta\":\"");
        retval.append(coRespuesta);
        retval.append("\",\"coDependencia\":\"");
        retval.append(dependenciaBean.getCoDependencia());
        retval.append("\",\"deDependencia\":\"");
        retval.append(dependenciaBean.getDeDependencia());
        retval.append("\"}");
        model.addAttribute("pMensaje", retval.toString());
        vResult = "respuesta";
      } else {
        String coRespuesta = "0";
        retval.append("{\"coRespuesta\":\"");
        retval.append(coRespuesta);
        retval.append("\",\"deRespuesta\":\"");
        retval.append("Error: No se puede obtener Dependencia.");
        retval.append("\"}");
        model.addAttribute("pMensaje", retval.toString());
        vResult = "respuesta";
      } 
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return vResult;
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goBuscaReferenciaOrigen"})
  public String goBuscaReferenciaOrigen(HttpServletRequest request, Model model) {
    String pnuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    model.addAttribute("iniFuncionParm", "1");
    model.addAttribute("listaReferenOrig", referencedData.getListReferenciaOrigen(pnuAnn, usuarioConfigBean));
    return "/modalGeneral/consultaReferenciaOrigenConsul";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goBuscaDestinatario"})
  public String goBuscaDestinatario(HttpServletRequest request, Model model) {
    String pnuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    model.addAttribute("iniFuncionParm", "1");
    model.addAttribute("listaDestinatario", this.referencedData.getListDestinatarioEmi(pnuAnn, usuarioConfigBean));
    return "/modalGeneral/consultaDestinatarioEmiConsul";
  }



  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goBuscaEmpDestino"})
  public String goBuscaEmpDestino(HttpServletRequest request, Model model) {
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    String pcoDep = ServletUtility.getInstancia().loadRequestParameter(request, "pcoDep");
    model.addAttribute("listaEmpleado", this.referencedData.getListEmpleadoElaboradoPor(pcoDep, usuarioConfigBean.getTiConsulta(), usuarioConfigBean.getCempCodemp()));
    return "/modalGeneral/consultaEmpDestino";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goBuscaElaboradoPor"})
  public String goBuscaElaboradoPor(HttpServletRequest request, Model model) {
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    String pcoDep = ServletUtility.getInstancia().loadRequestParameter(request, "pcoDep");
    model.addAttribute("iniFuncionParm", "1");
    model.addAttribute("listaEmpleado", this.referencedData.getListEmpleadoElaboradoPor(pcoDep, usuarioConfigBean.getTiConsulta(), usuarioConfigBean.getCempCodemp()));
    return "/modalGeneral/consultaElaboradoPorConsul";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goEditDocumentoEmi"})
  public String goEditDocumentoEmi(HttpServletRequest request, Model model) {
    String mensaje = "NO_OK";
    String snuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
    String snuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pnuEmi");



    
    try {
      DocumentoEmiConsulBean documentoEmiConsulBean = this.consultaEmiDocService.getDocumentoEmiAdm(snuAnn, snuEmi);
      String stipoDestinatario = this.consultaEmiDocService.getTipoDestinatarioEmi(snuAnn, snuEmi);
      if (stipoDestinatario != null) {
        HashMap map = this.consultaEmiDocService.getLstDestintariotlbEmi(snuAnn, snuEmi);
        model.addAttribute("lstDestintarioDocAdmEmi", map.get("lst1"));
        model.addAttribute("lstDestintarioProveedorDocAdmEmi", map.get("lst2"));
        model.addAttribute("lstDestintarioCiudadanoDocAdmEmi", map.get("lst3"));
        model.addAttribute("lstDestintarioOtroOrigenDocAdmEmi", map.get("lst4"));
      } 
      
      List listReferenciaDocAdmEmi = this.consultaEmiDocService.getLstDocumReferenciatblEmi(snuAnn, snuEmi);
      model.addAttribute(documentoEmiConsulBean);
      
      model.addAttribute("lstReferenciaDocAdmEmi", listReferenciaDocAdmEmi);
      mensaje = "OK";
    } catch (Exception ex) {
      mensaje = ex.getMessage();
    } 
    if (mensaje.equals("OK")) {
      return "/consultaEmiDoc/detalleDocumAdmEmi";
    }
    model.addAttribute("pMensaje", mensaje);
    return "respuesta";
  }

  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goBuscaDocumentoEnReferencia"})
  public String goBuscaDocumentoEnReferencia(BuscarDocumentoEmiConsulBean buscarDocumentoEmiConsulBean, HttpServletRequest request, Model model) {
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    buscarDocumentoEmiConsulBean.setCoDependencia(usuario.getCoDep());
    buscarDocumentoEmiConsulBean.setCoEmpleado(usuario.getCempCodemp());
    buscarDocumentoEmiConsulBean.setTiAcceso(usuarioConfigBean.getTiAcceso());




    
    String mensaje = "NO_OK";
    HashMap mp = null;
    List list = null;
    
    try {
      mp = this.consultaEmiDocService.getDocumentosEnReferencia(buscarDocumentoEmiConsulBean);
    } catch (Exception ex) {
      mensaje = ex.getMessage();
    } 
    mensaje = String.valueOf(mp.get("msjResult"));
    if (mensaje.equals("1")) {
      list = (List)mp.get("emiDocumAdmList");
      if (list != null) {
        if (list.size() >= 300) {
          model.addAttribute("msjEmision", "OPTIMICE SU CONSULTA");
        }
        model.addAttribute("emiDocumAdmList", list);
        mensaje = "OK";
      } 
    } 
    if (mensaje.equals("OK")) {
      return "/consultaEmiDoc/tblDocEmitidos";
    }
    model.addAttribute("pMensaje", mensaje);
    return "respuesta";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goRutaReporte"})
  @ResponseBody
  public String goRutaReporte(BuscarDocumentoEmiConsulBean buscarDocumentoEmiConsulBean, HttpServletRequest request, BindingResult result, Model model) {
    String extensionArch, coRespuesta, mensaje = "NO_OK";
    
    String deRespuesta = "";


    
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    String codDependencia = usuario.getCoDep();
    String codEmpleado = usuario.getCempCodemp();
    String tipAcceso = usuarioConfigBean.getTiConsulta();
    buscarDocumentoEmiConsulBean.setCoDependencia(codDependencia);
    buscarDocumentoEmiConsulBean.setCoEmpleado(codEmpleado);
    buscarDocumentoEmiConsulBean.setTiAcceso(tipAcceso);

    
    String pcoReporte = ServletUtility.getInstancia().loadRequestParameter(request, "pformatRepor");
    buscarDocumentoEmiConsulBean.setTipoBusqueda("0");
    buscarDocumentoEmiConsulBean.setEsIncluyeFiltro(false);
    String fecha = Utility.getInstancia().getStringFechaYYYYMMDDHHmm(new Date());
    
    ServletContext sc = request.getSession().getServletContext();
    String rutaUrl = "reporte?coReporte=";
    
    String logo = this.applicationProperties.getLogoReporteB64();
    ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(logo));
    
    Map<Object, Object> parametros = new HashMap<Object, Object>();
    parametros.put("P_DE_DEPENDENCIA", usuarioConfigBean.getDeDep());
    parametros.put("P_USER", usuario.getCoUsuario());
    parametros.put("P_LOGO_DIR", (bis.available() == 0) ? null : bis);
    
    if (pcoReporte.equalsIgnoreCase("PDF")) {
      pcoReporte = "TDR10";
      extensionArch = ".pdf";
    } else {
      pcoReporte = "TDR10_XLS";
      extensionArch = ".xls";
    } 
    String deNoDoc = "temp|EMISION_" + fecha + extensionArch;

    
    String prutaReporte = "reporte?coReporte=" + pcoReporte + "&coParametros=P_USER=" + usuario.getCoUsuario() + "|P_DE_DEPENDENCIA=" + usuarioConfigBean.getDeDep() + "|P_WHERE=";
    String prutaReporteAux = "";
    String eserror = "1";
    try {
      prutaReporteAux = this.consultaEmiDocService.getRutaReporte(buscarDocumentoEmiConsulBean);
      if (bis != null) bis.close(); 
      eserror = prutaReporteAux.substring(0, 1);
    } catch (Exception ex) {
      mensaje = ex.getMessage();
    } 
    
    if (eserror.equals("0")) {
      coRespuesta = eserror;
      prutaReporte = prutaReporte + prutaReporteAux.substring(1) + "&coImagenes=P_LOGO_DIR=logo_onpe.jpg";
    } else {
      coRespuesta = "1";
      deRespuesta = prutaReporteAux.substring(1);
    } 
    
    StringBuilder retval = new StringBuilder();
    retval.append("{\"coRespuesta\":\"");
    retval.append(coRespuesta);
    retval.append("\",\"deRespuesta\":\"");
    retval.append(deRespuesta);
    retval.append("\",\"noUrl\":\"");
    retval.append(prutaReporte);
    retval.append("\",\"noDoc\":\"");
    retval.append(deNoDoc);
    retval.append("\"}");
    
    return retval.toString();
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goBuscaDependenciaOrigen"})
  public String goBuscaDependenciaOrigen(HttpServletRequest request, Model model) {
    String pcoDepen = ServletUtility.getInstancia().loadRequestParameter(request, "pcoDepen");
    model.addAttribute("iniFuncionParm", "1");
    model.addAttribute("lstRemitente", this.commonQryService.getListRemitente(pcoDepen));
    return "/modalGeneral/consultaDependenciaOrigenConsul";
  }

  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goExportarArchivo"})
  @ResponseBody
  public String goExportarArchivo(BuscarDocumentoEmiConsulBean buscarDocumentoEmiConsulBean, HttpServletRequest request, BindingResult result, Model model) throws JRException {
    String extensionArch, coRespuesta, mensaje = "NO_OK";
    
    String deRespuesta = "";


    
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    String codDependencia = usuario.getCoDep();
    String codEmpleado = usuario.getCempCodemp();
    String tipAcceso = usuarioConfigBean.getTiConsulta();




    
    String pcoReporte = ServletUtility.getInstancia().loadRequestParameter(request, "pformatRepor");


    
    String fecha = Utility.getInstancia().getStringFechaYYYYMMDDHHmm(new Date());
    
    String anio = ServletUtility.getInstancia().loadRequestParameter(request, "coAnnio");
    
    if (pcoReporte.equalsIgnoreCase("PDF")) {
      pcoReporte = "TDR10";
      
      extensionArch = ".pdf";
    } else {
      pcoReporte = "TDR10_XLS";
      extensionArch = ".xls";
    } 
    String deNoDoc = "temp|EMISION_" + fecha + extensionArch;

    
    String prutaReporte = "reporte?coReporte=" + pcoReporte + "&coParametros=P_USER=" + usuario.getCoUsuario() + "|P_DE_DEPENDENCIA=" + usuarioConfigBean.getDeDep() + "|P_OBJETO=BuscarDocumentoEmiConsulBean|P_FILTROS={'coDependencia':'" + codDependencia + "','coEmpleado':'" + codEmpleado + "','tiAcceso':'" + tipAcceso + "','tipoBusqueda':'0','esIncluyeFiltro':'false','coAnnio':'" + anio + "'}|P_SERVICIO=consultaEmiDocService.getRutaReporteLista";





    
    String prutaReporteAux = "";
    
    String eserror = "0";
    try {
      prutaReporteAux = "";
    } catch (Exception ex) {
      mensaje = ex.getMessage();
    } 
    
    if (eserror.equals("0")) {
      coRespuesta = eserror;
      
      prutaReporte = prutaReporte + "&coImagenes=P_LOGO_DIR=logo_onpe.jpg";
    } else {
      coRespuesta = "1";
      deRespuesta = prutaReporteAux.substring(1);
    } 
    
    StringBuilder retval = new StringBuilder();
    retval.append("{\"coRespuesta\":\"");
    retval.append(coRespuesta);
    retval.append("\",\"deRespuesta\":\"");
    retval.append(deRespuesta);
    retval.append("\",\"noUrl\":\"");
    retval.append(prutaReporte);
    retval.append("\",\"noDoc\":\"");
    retval.append(deNoDoc);
    retval.append("\"}");
    
    return retval.toString();
  }

  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goExportarArchivoLista"})
  @ResponseBody
  public String goExportarArchivoLista(BuscarDocumentoEmiConsulBean buscarDocumentoEmiConsulBean, HttpServletRequest request, BindingResult result, Model model) throws JRException, FileNotFoundException, IOException {
	  
	  StringBuilder retval = new StringBuilder(); 
	  try {
	  	ServletContext sc = request.getSession().getServletContext();
	    
	    String coReporte = ServletUtility.getInstancia().loadRequestParameter(request, "pformatRepor");
	    
	    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
	    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
	    String codDependencia = usuario.getCoDep();
	    String codEmpleado = usuario.getCempCodemp();
	    String tipAcceso = usuarioConfigBean.getTiConsulta();
	    String anio = ServletUtility.getInstancia().loadRequestParameter(request, "coAnnio");
	    
	    String rutaReporte = sc.getRealPath("/reports/");
	    
	    String logo = this.applicationProperties.getLogoReporteB64();
	    ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(logo));
	    
	    Map<Object, Object> parametros = new HashMap<Object, Object>();
	    parametros.put("P_DE_DEPENDENCIA", usuarioConfigBean.getDeDep());
	    parametros.put("P_USER", usuario.getCoUsuario());
	    parametros.put("P_LOGO_DIR", (bis.available() == 0) ? null : bis);
	    
	    buscarDocumentoEmiConsulBean.setCoDependencia(codDependencia);
	    buscarDocumentoEmiConsulBean.setCoEmpleado(codEmpleado);
	    buscarDocumentoEmiConsulBean.setTiAcceso(tipAcceso);
	    buscarDocumentoEmiConsulBean.setCoAnnio(anio);
	    buscarDocumentoEmiConsulBean.setRutaReporteJasper(rutaReporte);
	    buscarDocumentoEmiConsulBean.setFormatoReporte(coReporte);
    
	    ReporteBean objReporteBean;
	
		objReporteBean = consultaEmiDocService.getGenerarReporte(buscarDocumentoEmiConsulBean, parametros);
		if (bis != null)
		bis.close(); 
	   
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
		e.printStackTrace();
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
}
