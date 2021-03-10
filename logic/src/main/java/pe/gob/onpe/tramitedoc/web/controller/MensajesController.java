package pe.gob.onpe.tramitedoc.web.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.libreria.util.ServletUtility;
import pe.gob.onpe.libreria.util.Utility;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoCargaMsjBean;
import pe.gob.onpe.tramitedoc.bean.DescargaMensajeBean;
import pe.gob.onpe.tramitedoc.bean.DocRespuestaBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoFileBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoVerBean;
import pe.gob.onpe.tramitedoc.bean.ElementoMensajeroBean;
import pe.gob.onpe.tramitedoc.bean.MensajesConsulBean;
import pe.gob.onpe.tramitedoc.bean.ReporteBean;
import pe.gob.onpe.tramitedoc.bean.UsuarioConfigBean;
import pe.gob.onpe.tramitedoc.service.CommonQryService;
import pe.gob.onpe.tramitedoc.service.DocumentoMensajesService;
import pe.gob.onpe.tramitedoc.service.MensajesData;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;

@Controller
@RequestMapping({"/{version}/srGestionMensajes.do"})
@SessionAttributes({"docSession"})
public class MensajesController {
  @Autowired
  private MensajesData mensajesData;
  
  @Autowired
  private ApplicationProperties applicationProperties;
  
  @Autowired
  private DocumentoMensajesService documentoMensajesService;
  
  @Autowired
  private CommonQryService commonQryService;
  
  @RequestMapping(method = {RequestMethod.GET}, params = {"accion=goInicio"})
  public String goInicio(HttpServletRequest request, Model model) {
    String codDependencia = ServletUtility.getInstancia().loadRequestParameter(request, "coDep");
    String sCoAnnio = Utility.getInstancia().dateToFormatStringYYYY(new Date());
    BuscarDocumentoCargaMsjBean buscarDocumentoCargaMsjBean = new BuscarDocumentoCargaMsjBean();
    buscarDocumentoCargaMsjBean.setCoAnnio(sCoAnnio);
    buscarDocumentoCargaMsjBean.setCoEstadoDoc("2");
    buscarDocumentoCargaMsjBean.setCoAmbitoMsj(".: TODOS :.");
    buscarDocumentoCargaMsjBean.setCoTipoEnvMsj(".: TODOS :.");
    buscarDocumentoCargaMsjBean.setCoTipoMsj(".: TODOS :.");
    buscarDocumentoCargaMsjBean.setCoDependencia(codDependencia);
    model.addAttribute(buscarDocumentoCargaMsjBean);
    model.addAttribute("deEstadosList", this.mensajesData.getLstEstadoMsj("TD_MENSAJERIA"));
    model.addAttribute("deAmbitosList", this.mensajesData.getLstAmbitoMsj("DE_AMBITO_MENS"));
    model.addAttribute("deTipoEnvList", this.mensajesData.getListTipoEnvMsj("RE_ENV_MSJ_MENS"));
    model.addAttribute("deOficinaList", this.mensajesData.getListOficina());
    List listTipoMensajero = this.documentoMensajesService.getListTipoElementoMensajeria("DE_TIP_MSJ_MENS");
    model.addAttribute("deTipoMsjList", listTipoMensajero);
    model.addAttribute("fechaActual", Utility.getInstancia().dateToFormatString(new Date()));
    model.addAttribute("annioActual", Utility.getInstancia().dateToFormatStringYYYY(new Date()));
    return "/mpGestionMensajes/BuscarMensajes";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goInicio"})
  public String goInicio(BuscarDocumentoCargaMsjBean buscarDocumentoCargaMsjBean, HttpServletRequest request, BindingResult result, Model model) {
    String mensaje = "NO_OK";
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    String codDependencia = usuarioConfigBean.getCoDep();
    String codEmpleado = usuarioConfigBean.getCempCodemp();
    String tipAcceso = usuarioConfigBean.getTiAccesoMp();
    buscarDocumentoCargaMsjBean.setCoDependencia(codDependencia);
    buscarDocumentoCargaMsjBean.setCoEmpleado(codEmpleado);
    buscarDocumentoCargaMsjBean.setTiAcceso(tipAcceso);
    buscarDocumentoCargaMsjBean.setCoDepEmi(usuarioConfigBean.getCoDepMp());
    buscarDocumentoCargaMsjBean.setInMesaPartes(usuarioConfigBean.getInMesaPartes());
    List list = null;
    try {
      list = this.documentoMensajesService.getBuscaDocumentosCarga(buscarDocumentoCargaMsjBean);
    } catch (Exception ex) {
      mensaje = ex.getMessage();
    } 
    if (list != null) {
      if (list.size() >= 50)
        model.addAttribute("msjEmision", "OPTIMICE SU CONSULTA"); 
      model.addAttribute("docMensajeLst", list);
      mensaje = "OK";
    } else {
      mensaje = "No se encuentran registros.";
    } 
    if (mensaje.equals("OK"))
      return "/mpGestionMensajes/tblMensajes"; 
    model.addAttribute("pMensaje", mensaje);
    return "respuesta";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDescargarMensaje"})
  public String goDescargarMensaje(HttpServletRequest request, Model model) {
    String pnu_ann = ServletUtility.getInstancia().loadRequestParameter(request, "nu_ann");
    String pnu_emi = ServletUtility.getInstancia().loadRequestParameter(request, "nu_emi");
    String pnu_des = ServletUtility.getInstancia().loadRequestParameter(request, "nu_des");
    String pnu_msj = ServletUtility.getInstancia().loadRequestParameter(request, "nu_msj");
    String pfec_enviomsj = ServletUtility.getInstancia().loadRequestParameter(request, "fec_enviomsj");
    String pfec_pla = ServletUtility.getInstancia().loadRequestParameter(request, "fec_plazo");
    String fechaHoraActual = Utility.getInstancia().dateToFormatString2(new Date());
    String estadoAneCargo = this.commonQryService.obtenerValorParametro("ANEXAR_CARGO") + "";
    DescargaMensajeBean descargaMensajeBean = new DescargaMensajeBean();
    MensajesConsulBean mensajesConsulBean = new MensajesConsulBean();
    descargaMensajeBean.setCo_EstadoDoc("0");
    mensajesConsulBean = this.documentoMensajesService.getBuscaDocumentosMsj(pnu_ann, pnu_emi, pnu_des, pnu_msj);
    model.addAttribute(descargaMensajeBean);
    model.addAttribute("fechaActual", Utility.getInstancia().dateToFormatString(new Date()));
    model.addAttribute("deEstadosList", this.mensajesData.getLstEstadoCarga("TD_MENSAJERIA"));
    model.addAttribute("annioActual", Utility.getInstancia().dateToFormatStringYYYY(new Date()));
    model.addAttribute("horaActual", Utility.getInstancia().dateToFormatStringHHmm(new Date()));
    model.addAttribute("pfechaHoraActual", fechaHoraActual);
    model.addAttribute("pnu_ann", pnu_ann);
    model.addAttribute("pnu_emi", pnu_emi);
    model.addAttribute("pnu_des", pnu_des);
    model.addAttribute("pnu_msj", pnu_msj);
    model.addAttribute("pfec_enviomsj", pfec_enviomsj);
    model.addAttribute("pfe_pla", pfec_pla);
    model.addAttribute("pdias_pla_dev", mensajesConsulBean.getDias_pla_devo());
    model.addAttribute("anexar_cargo", estadoAneCargo.toUpperCase());
    model.addAttribute("deMotivoList", this.mensajesData.getLstMotivoMsj("MOTIVO_DEV_MSJ"));
    model.addAttribute("pde_TipoDoc", mensajesConsulBean.getDe_tip_doc());
    model.addAttribute("pde_TipoMsj", mensajesConsulBean.getDe_tip_msj());
    model.addAttribute("pPeEnvMsj", mensajesConsulBean.getPe_env_msj());
    model.addAttribute("pde_Asu", mensajesConsulBean.getDe_asu());
    model.addAttribute("pfe_emi", mensajesConsulBean.getFe_emi());
    model.addAttribute("pfec_recepmp", mensajesConsulBean.getFec_recepmp());
    model.addAttribute("pde_dependencia", mensajesConsulBean.getDe_dependencia());
    model.addAttribute("pde_destinatario", mensajesConsulBean.getDestinatario());
    model.addAttribute("pde_ambito", mensajesConsulBean.getDe_ambito());
    model.addAttribute("pde_tipomsj", mensajesConsulBean.getDe_tip_msj());
    model.addAttribute("pde_tipoenv", mensajesConsulBean.getDe_tip_env());
    model.addAttribute("pde_resenv", mensajesConsulBean.getRe_env_msj());
    return "/mpGestionMensajes/DescargarMensaje";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goConsDescargarMensaje"})
  public String goConsDescargarMensaje(HttpServletRequest request, Model model) {
    String pnu_ann = ServletUtility.getInstancia().loadRequestParameter(request, "nu_ann");
    String pnu_emi = ServletUtility.getInstancia().loadRequestParameter(request, "nu_emi");
    String pnu_des = ServletUtility.getInstancia().loadRequestParameter(request, "nu_des");
    String pnu_msj = ServletUtility.getInstancia().loadRequestParameter(request, "nu_msj");
    String fechaHoraActual = Utility.getInstancia().dateToFormatString2(new Date());
    MensajesConsulBean mensajesConsulBean = new MensajesConsulBean();
    mensajesConsulBean = this.documentoMensajesService.getBuscaDocumentosMsj(pnu_ann, pnu_emi, pnu_des, pnu_msj);
    model.addAttribute("fechaActual", Utility.getInstancia().dateToFormatString(new Date()));
    model.addAttribute("deEstadosList", this.mensajesData.getLstEstadoCarga("TD_MENSAJERIA"));
    model.addAttribute("annioActual", Utility.getInstancia().dateToFormatStringYYYY(new Date()));
    model.addAttribute("horaActual", Utility.getInstancia().dateToFormatStringHHmm(new Date()));
    model.addAttribute("pfechaHoraActual", fechaHoraActual);
    model.addAttribute("pnu_ann", pnu_ann);
    model.addAttribute("pnu_emi", pnu_emi);
    model.addAttribute("pnu_des", pnu_des);
    model.addAttribute("pnu_msj", pnu_msj);
    model.addAttribute("pfec_enviomsj", mensajesConsulBean.getFec_enviomsj());
    model.addAttribute("pest_msj", mensajesConsulBean.getEst_msj());
    model.addAttribute("nro_guia_devolucion", mensajesConsulBean.getNro_guia_devolucion());
    model.addAttribute("pes_pen_msj", mensajesConsulBean.getEs_pen_msj());
    model.addAttribute("pes_pen_dev", mensajesConsulBean.getEs_pen_dev());
    model.addAttribute("pobservacion", mensajesConsulBean.getOb_msj());
    model.addAttribute("pfe_ent_msj", mensajesConsulBean.getFe_ent_msj());
    model.addAttribute("pho_ent_msj", mensajesConsulBean.getHo_ent_msj());
    model.addAttribute("pfe_dev_msj", mensajesConsulBean.getFe_dev_msj());
    model.addAttribute("pho_dev_msj", mensajesConsulBean.getHo_dev_msj());
    model.addAttribute("pfe_pla", mensajesConsulBean.getFe_pla_msj());
    System.out.println("Motivo==>>" + mensajesConsulBean.getMo_msj_dev());
    model.addAttribute("pmo_msj_dev", mensajesConsulBean.getMo_msj_dev());
    model.addAttribute("tiene_cargo", mensajesConsulBean.getTieneanexocargo());
    model.addAttribute("pde_TipoDoc", mensajesConsulBean.getDe_tip_doc());
    model.addAttribute("pde_Asu", mensajesConsulBean.getDe_asu());
    model.addAttribute("pfe_emi", mensajesConsulBean.getFe_emi());
    model.addAttribute("pfec_recepmp", mensajesConsulBean.getFec_recepmp());
    return "/mpGestionMensajes/ConsDescargarMensaje";
  }
  
  LinkedList<DocumentoFileBean> files = new LinkedList<DocumentoFileBean>();
  
  DocumentoFileBean fileMeta = null;
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goUpdDescargaMsj"})
  public String goUpdDescargarMensaje(@RequestBody DescargaMensajeBean descargaMensajeBean, HttpServletRequest request, Model model) throws ServletException, IOException {
    String mensaje = "NO_OK";
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    String coUsu = usuario.getCoUsuario();
    try {
      String respuesta = this.documentoMensajesService.updMensajeriaDocumento(descargaMensajeBean);
      mensaje = respuesta;
    } catch (Exception ex) {
      mensaje = ex.getMessage();
    } 
    if (mensaje.equals("OK")) {
      model.addAttribute("pMensaje", "Datos guardados.");
      return "respuesta";
    } 
    model.addAttribute("pMensaje", mensaje);
    return "respuesta";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goUploadMsj"})
  @ResponseBody
  public String goUploadMsj(MultipartHttpServletRequest request, HttpServletResponse response) {
    Iterator<String> itr = request.getFileNames();
    MultipartFile mpf = null;
    String pNuAnn = null;
    String pNuEmi = null;
    String pNuDes = null;
    pNuAnn = ServletUtility.getInstancia().loadRequestParameter((HttpServletRequest)request, "NuAnn");
    pNuEmi = ServletUtility.getInstancia().loadRequestParameter((HttpServletRequest)request, "NuEmi");
    pNuDes = ServletUtility.getInstancia().loadRequestParameter((HttpServletRequest)request, "NuDes");
    System.out.println("Hola mundoooo");
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession((HttpServletRequest)request);
    String coUsu = usuario.getCoUsuario();
    String vreturn = "NO_OK";
    while (itr.hasNext()) {
      mpf = request.getFile(itr.next());
      this.fileMeta = new DocumentoFileBean();
      this.fileMeta.setNombreArchivo(mpf.getOriginalFilename());
      this.fileMeta.setTamanoArchivo((mpf.getSize() / 1024L) + " Kb");
      this.fileMeta.setTipoArchivo(mpf.getContentType());
      try {
        this.fileMeta.setArchivoBytes(mpf.getBytes());
        vreturn = this.documentoMensajesService.insArchivoAnexoDes(coUsu, pNuAnn, pNuEmi, pNuDes, this.fileMeta);
      } catch (IOException e) {
        e.printStackTrace();
      } 
      this.files.add(this.fileMeta);
    } 
    String id = this.fileMeta.getIdDocumento();
    String nombreArchivo = this.fileMeta.getNombreArchivo();
    String tamanoArchivo = this.fileMeta.getTamanoArchivo();
    String tipoArchivo = this.fileMeta.getTipoArchivo();
    String res = "[{\"id\":\"%s\",\"name\":\"%s\",\"fileSize\":\"%s\",\"fileType\":\"%s\"}]";
    res = String.format(res, new Object[] { id, nombreArchivo, tamanoArchivo, tipoArchivo });
    return res;
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goUpload"})
  @ResponseBody
  public String goUpload(MultipartHttpServletRequest request, HttpServletResponse response) {
    Iterator<String> itr = request.getFileNames();
    MultipartFile mpf = null;
    String pNuAnn = null;
    String pNuEmi = null;
    String pNuDes = null;
    pNuAnn = ServletUtility.getInstancia().loadRequestParameter((HttpServletRequest)request, "NuAnn");
    pNuEmi = ServletUtility.getInstancia().loadRequestParameter((HttpServletRequest)request, "NuEmi");
    pNuDes = ServletUtility.getInstancia().loadRequestParameter((HttpServletRequest)request, "NuDes");
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession((HttpServletRequest)request);
    String coUsu = usuario.getCoUsuario();
    String vreturn = "NO_OK";
    while (itr.hasNext()) {
      mpf = request.getFile(itr.next());
      this.fileMeta = new DocumentoFileBean();
      this.fileMeta.setNombreArchivo(mpf.getOriginalFilename());
      this.fileMeta.setTamanoArchivo((mpf.getSize() / 1024L) + " Kb");
      this.fileMeta.setTipoArchivo(mpf.getContentType());
      System.out.println(this.fileMeta.getNombreArchivo());
      try {
        this.fileMeta.setArchivoBytes(mpf.getBytes());
        vreturn = this.documentoMensajesService.insArchivoAnexoDes(coUsu, pNuAnn, pNuEmi, pNuDes, this.fileMeta);
      } catch (IOException e) {
        e.printStackTrace();
      } 
      this.files.add(this.fileMeta);
    } 
    String id = this.fileMeta.getIdDocumento();
    String nombreArchivo = this.fileMeta.getNombreArchivo();
    String tamanoArchivo = this.fileMeta.getTamanoArchivo();
    String tipoArchivo = this.fileMeta.getTipoArchivo();
    String res = "[{\"id\":\"%s\",\"name\":\"%s\",\"fileSize\":\"%s\",\"fileType\":\"%s\"}]";
    res = String.format(res, new Object[] { id, nombreArchivo, tamanoArchivo, tipoArchivo });
    return res;
  }
  
  private byte[] getByte(String path) {
    byte[] getBytes = new byte[0];
    try {
      File file = new File(path);
      getBytes = new byte[(int)file.length()];
      InputStream is = new FileInputStream(file);
      is.read(getBytes);
      is.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } 
    return getBytes;
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDelete"})
  public String goDelete(HttpServletRequest request, Model model) {
    String mensaje = "NO_OK";
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    String coUsu = usuario.getCoUsuario();
    String pnu_ann = ServletUtility.getInstancia().loadRequestParameter(request, "nu_ann");
    String pnu_emi = ServletUtility.getInstancia().loadRequestParameter(request, "nu_emi");
    String pnu_des = ServletUtility.getInstancia().loadRequestParameter(request, "nu_des");
    DescargaMensajeBean descargaMensajeBean = new DescargaMensajeBean();
    descargaMensajeBean.setNu_ann(pnu_ann);
    descargaMensajeBean.setNu_emi(pnu_emi);
    descargaMensajeBean.setNu_des(pnu_des);
    try {
      String respuesta = this.documentoMensajesService.deleteMsj(descargaMensajeBean);
      mensaje = respuesta;
    } catch (Exception ex) {
      mensaje = ex.getMessage();
    } 
    if (mensaje.equals("OK")) {
      model.addAttribute("pMensaje", "Datos guardados.");
      return "respuesta";
    } 
    model.addAttribute("pMensaje", mensaje);
    return "respuesta";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goEliminaMensaje"})
  public String goEliminaMensaje(HttpServletRequest request, Model model) {
    String mensaje = "NO_OK";
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    String coUsu = usuario.getCoUsuario();
    String pnu_ann = ServletUtility.getInstancia().loadRequestParameter(request, "nu_ann");
    String pnu_emi = ServletUtility.getInstancia().loadRequestParameter(request, "nu_emi");
    String pnu_des = ServletUtility.getInstancia().loadRequestParameter(request, "nu_des");
    String pnu_msj = ServletUtility.getInstancia().loadRequestParameter(request, "nu_msj");
    DescargaMensajeBean descargaMensajeBean = new DescargaMensajeBean();
    descargaMensajeBean.setNu_ann(pnu_ann);
    descargaMensajeBean.setNu_emi(pnu_emi);
    descargaMensajeBean.setNu_des(pnu_des);
    descargaMensajeBean.setNu_msj(pnu_msj);
    try {
      String respuesta = this.documentoMensajesService.delMensajeriaDocumento(descargaMensajeBean);
      mensaje = respuesta;
    } catch (Exception ex) {
      mensaje = ex.getMessage();
    } 
    if (mensaje.equals("OK")) {
      model.addAttribute("pMensaje", "Datos guardados.");
      return "respuesta";
    } 
    model.addAttribute("pMensaje", mensaje);
    return "respuesta";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goListaResponsable"})
  public String goListaResponsable(HttpServletRequest request, Model model) {
    String mensaje = "NO_OK";
    String sTipo = ServletUtility.getInstancia().loadRequestParameter(request, "vCodResposable");
    String vAmbito = ServletUtility.getInstancia().loadRequestParameter(request, "vCodAmbito");
    try {
      List<ElementoMensajeroBean> tipoList = this.documentoMensajesService.getListResponsableMensajeria(sTipo, vAmbito);
      model.addAttribute("listResponsable", tipoList);
    } catch (Exception ex) {
      mensaje = ex.getMessage();
    } 
    return "/mpGestionMensajes/ResponsableListado";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goExportarArchivoLista"})
  @ResponseBody
  public String goExportarArchivoLista(BuscarDocumentoCargaMsjBean buscarDocumentoCargaMsjBean, HttpServletRequest request, BindingResult result, Model model) throws IOException {
    ServletContext sc = request.getSession().getServletContext();
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    String codDependencia = usuario.getCoDep();
    String codEmpleado = usuario.getCempCodemp();
    String tipAcceso = usuarioConfigBean.getTiConsulta();
    String anio = ServletUtility.getInstancia().loadRequestParameter(request, "coAnnio");
    String rutaReporte = sc.getRealPath("/reports/");
    String logo = this.applicationProperties.getLogoReporteB64();
    ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(logo));
    String coReporte = ServletUtility.getInstancia().loadRequestParameter(request, "pformatRepor");
    Map<Object, Object> parametros = new HashMap<Object, Object>();
    parametros.put("P_DE_DEPENDENCIA", usuarioConfigBean.getDeDep());
    parametros.put("P_USER", usuario.getCoUsuario());
    parametros.put("P_LOGO_DIR", (bis.available() == 0) ? null : bis);
    buscarDocumentoCargaMsjBean.setCoDependencia(codDependencia);
    buscarDocumentoCargaMsjBean.setCoEmpleado(codEmpleado);
    buscarDocumentoCargaMsjBean.setTiAcceso(tipAcceso);
    buscarDocumentoCargaMsjBean.setCoAnnio(anio);
    buscarDocumentoCargaMsjBean.setRutaReporteJasper(rutaReporte);
    buscarDocumentoCargaMsjBean.setFormatoReporte(coReporte);
    ReporteBean objReporteBean = this.documentoMensajesService.getGenerarReporte(buscarDocumentoCargaMsjBean, parametros);
    if (bis != null)
      bis.close(); 
    StringBuilder retval = new StringBuilder();
    retval.append("{\"coRespuesta\":\"");
    retval.append(objReporteBean.getcoRespuesta());
    retval.append("\",\"deRespuesta\":\"");
    retval.append(objReporteBean.getdeRespuesta());
    retval.append("\",\"noUrl\":\"");
    retval.append(objReporteBean.getnoUrl());
    retval.append("\",\"noDoc\":\"");
    retval.append(objReporteBean.getnoDoc());
    retval.append("\"}");
    return retval.toString();
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goObtieneDocumentoMsj"})
  @ResponseBody
  private String goObtieneDocumentoMsj(HttpServletRequest request, Model model) throws Exception {
    String retval = "";
    DocRespuestaBean retRespuesta = new DocRespuestaBean();
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    String pNuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "nuAnn");
    String pNuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "nuEmi");
    String pNuDes = ServletUtility.getInstancia().loadRequestParameter(request, "nuDes");
    if (pNuAnn != null && pNuEmi != null && pNuDes != null) {
      try {
        DocumentoVerBean docVerBean = new DocumentoVerBean();
        docVerBean = this.documentoMensajesService.getNombreDocMsj(pNuAnn, pNuEmi, pNuDes);
        retRespuesta.setRetval(docVerBean.getDeMensaje());
        retRespuesta.setNuAnn(docVerBean.getNuAnn());
        retRespuesta.setNuEmi(docVerBean.getNuEmi());
        retRespuesta.setNuAne(docVerBean.getNuAne());
        retRespuesta.setNoDoc(docVerBean.getNoDocumento());
        retRespuesta.setNoUrl(docVerBean.getUrlDocumento());
      } catch (Exception e) {
        retRespuesta.setRetval("Error en Obtener nombre del Anexo");
        e.printStackTrace();
      } 
      try {
        Writer strWriter = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(strWriter, retRespuesta);
        retval = strWriter.toString();
      } catch (Exception e) {
        retval = "";
      } 
    } 
    return retval;
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goDescargarMensajeDirecto"})
  public String goDescargarMensajeDirecto(HttpServletRequest request, Model model) {
    String pnu_ann = ServletUtility.getInstancia().loadRequestParameter(request, "nu_ann");
    String pnu_emi = ServletUtility.getInstancia().loadRequestParameter(request, "nu_emi");
    String pnu_des = "0";
    String pnu_msj = "0";
    String pfec_enviomsj = Utility.getInstancia().dateToFormatString(new Date());
    String pfec_pla = Utility.getInstancia().dateToFormatString(new Date());
    String fechaHoraActual = Utility.getInstancia().dateToFormatString2(new Date());
    DescargaMensajeBean descargaMensajeDirectoBean = new DescargaMensajeBean();
    descargaMensajeDirectoBean.setCo_EstadoDoc("0");
    model.addAttribute(descargaMensajeDirectoBean);
    model.addAttribute("fechaActual", Utility.getInstancia().dateToFormatString(new Date()));
    model.addAttribute("deEstadosList", this.mensajesData.getLstEstadoCarga("TD_MENSAJERIA"));
    model.addAttribute("annioActual", Utility.getInstancia().dateToFormatStringYYYY(new Date()));
    model.addAttribute("horaActual", Utility.getInstancia().dateToFormatStringHHmm(new Date()));
    model.addAttribute("pfechaHoraActual", fechaHoraActual);
    model.addAttribute("pnu_ann", pnu_ann);
    model.addAttribute("pnu_emi", pnu_emi);
    model.addAttribute("pnu_des", pnu_des);
    model.addAttribute("pnu_msj", pnu_msj);
    model.addAttribute("pfec_enviomsj", pfec_enviomsj);
    model.addAttribute("pfe_pla", pfec_pla);
    System.out.println(pnu_ann);
    return "/mpGestionMensajes/DescargarMensajeDirecto";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goRevertirMensaje"})
  public String goRevertirMensaje(HttpServletRequest request, Model model) {
    String mensaje = "NO_OK";
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    String coUsu = usuario.getCoUsuario();
    String pnu_ann = ServletUtility.getInstancia().loadRequestParameter(request, "nu_ann");
    String pnu_emi = ServletUtility.getInstancia().loadRequestParameter(request, "nu_emi");
    String pnu_des = ServletUtility.getInstancia().loadRequestParameter(request, "nu_des");
    String pnu_msj = ServletUtility.getInstancia().loadRequestParameter(request, "nu_msj");
    DescargaMensajeBean descargaMensajeBean = new DescargaMensajeBean();
    descargaMensajeBean.setNu_ann(pnu_ann);
    descargaMensajeBean.setNu_emi(pnu_emi);
    descargaMensajeBean.setNu_des(pnu_des);
    descargaMensajeBean.setNu_msj(pnu_msj);
    try {
      String respuesta = this.documentoMensajesService.revMensajeriaDocumento(descargaMensajeBean);
      mensaje = respuesta;
    } catch (Exception ex) {
      mensaje = ex.getMessage();
    } 
    if (mensaje.equals("OK")) {
      model.addAttribute("pMensaje", "Datos guardados.");
      return "respuesta";
    } 
    model.addAttribute("pMensaje", mensaje);
    return "respuesta";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goCalFechaPlazo"})
  @ResponseBody
  public String goCalFechaPlazo(HttpServletRequest request, Model model) {
    String coRespuesta, mensaje = "NO_OK";
    String pfechaent = ServletUtility.getInstancia().loadRequestParameter(request, "pfechaent");
    String pdipladev = ServletUtility.getInstancia().loadRequestParameter(request, "pdipladev");
    DescargaMensajeBean descargaMensajeBean = new DescargaMensajeBean();
    descargaMensajeBean.setFe_ent_msj(pfechaent);
    descargaMensajeBean.setDi_pla_dev(pdipladev);
    try {
      mensaje = this.documentoMensajesService.selectCalFechaPlazo(descargaMensajeBean);
    } catch (Exception ex) {
      mensaje = ex.getMessage();
    } 
    if (mensaje.equals("OK")) {
      coRespuesta = "1";
    } else {
      coRespuesta = "0";
    } 
    StringBuilder retval = new StringBuilder();
    retval.append("{\"coRespuesta\":\"");
    retval.append(coRespuesta);
    retval.append("\",\"deFechaDev\":\"");
    retval.append(descargaMensajeBean.getFe_pla_dev());
    retval.append("\"");
    retval.append("");
    retval.append("}");
    return retval.toString();
  }
}
