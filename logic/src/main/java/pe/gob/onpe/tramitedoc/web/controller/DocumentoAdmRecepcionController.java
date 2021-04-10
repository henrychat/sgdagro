package pe.gob.onpe.tramitedoc.web.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.libreria.exception.validarDatoException;
import pe.gob.onpe.libreria.util.ServletUtility;
import pe.gob.onpe.libreria.util.Utility;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoRecepBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoFileBean;
import pe.gob.onpe.tramitedoc.bean.ExpedienteBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaBean;
import pe.gob.onpe.tramitedoc.bean.TemaBean;
import pe.gob.onpe.tramitedoc.bean.UsuarioConfigBean;
import pe.gob.onpe.tramitedoc.service.DocumentoMensajesService;
import pe.gob.onpe.tramitedoc.service.RecepDocumentoAdmService;
import pe.gob.onpe.tramitedoc.service.ReferencedData;
import pe.gob.onpe.tramitedoc.service.TemaService;
import pe.gob.onpe.tramitedoc.util.FiltroPaginate;
import pe.gob.onpe.tramitedoc.util.ProcessResult;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;

@Controller
@RequestMapping({"/{version}/srDocumentoAdmRecepcion.do"})
public class DocumentoAdmRecepcionController {
  @Autowired
  private RecepDocumentoAdmService recepDocumentoAdmService;
  @Autowired
  private ReferencedData referencedData;
  @Autowired
  private TemaService temaService;
  @Autowired
  private DocumentoMensajesService documentoMensajesService;
  private static Logger logger = Logger.getLogger("SGDDocumentoAdmRecepcionController");
  
  @RequestMapping(method = {RequestMethod.GET}, params = {"accion=goInicio"})
  public String goInicio(HttpServletRequest request, Model model) {
    String annio = Utility.getInstancia().dateToFormatStringYYYY(new Date());
    String pEstado = ServletUtility.getInstancia().loadRequestParameter(request, "estadoDoc");
    String pEtiqueta = ServletUtility.getInstancia().loadRequestParameter(request, "idEtiqueta");
    String codDependencia = ServletUtility.getInstancia().loadRequestParameter(request, "coDep");
    String coBandeja = ServletUtility.getInstancia().loadRequestParameter(request, "coBandeja");
    BuscarDocumentoRecepBean buscarDocumentoRecepBean = new BuscarDocumentoRecepBean();
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    TemaBean temabean = new TemaBean();
    temabean.setCoDependencia(usuario.getCoDep());
    List<TemaBean> tema = this.temaService.getListTema(temabean);
    List listDepa = null;
    buscarDocumentoRecepBean.setsCoAnnio(annio);
    buscarDocumentoRecepBean.setsCoAnnioBus(annio);
    buscarDocumentoRecepBean.setsEstadoDoc(pEstado);
    buscarDocumentoRecepBean.setsCoDependencia(codDependencia);
    buscarDocumentoRecepBean.setIdEtiqueta(pEtiqueta);
    if (coBandeja.equals("P")) {
      buscarDocumentoRecepBean.setEsIncluyeProfesional(true);
    } else {
      
      buscarDocumentoRecepBean.setEsIncluyeProfesional(false);
    } 
    if (coBandeja.equals("O")) {
      buscarDocumentoRecepBean.setEsIncluyeOficina(true);
    } else {
      
      buscarDocumentoRecepBean.setEsIncluyeOficina(false);
    } 
    listDepa = this.recepDocumentoAdmService.getDocumDepenProveido(usuario.getCoDep());
    this.recepDocumentoAdmService.estadoRecepcionDocumento(buscarDocumentoRecepBean, "");
    
    model.addAttribute(buscarDocumentoRecepBean);
    buscarDocumentoRecepBean = new BuscarDocumentoRecepBean();
    model.addAttribute("deListTema", tema);
    model.addAttribute("deAnnioList", this.referencedData.getAnnioList());

    
    model.addAttribute("esRecibido", pEstado);



    
    model.addAttribute("recepDocProveido", listDepa);
    listDepa = null;
    model.addAttribute("deEstadosList", this.referencedData.getLstEstadosDocumentoRec("TDTV_DESTINOS"));
    model.addAttribute("dePrioridadesList", this.referencedData.getPrioridadesDocumentoList());
    model.addAttribute("deTipoDocumentoList", this.referencedData.getTipoDocumentoList(codDependencia));
    
    model.addAttribute("deEtiquetasList", this.referencedData.getEtiquetasList());
    model.addAttribute("fechaActual", Utility.getInstancia().dateToFormatString(new Date()));
    model.addAttribute("annioActual", Utility.getInstancia().dateToFormatStringYYYY(new Date()));
    return "/recepDocumentosAdm/recepDocumentoAdm";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goInicio"})
  public String goInicio(BuscarDocumentoRecepBean buscarDocumentoRecepBean, HttpServletRequest request, BindingResult result, Model model) {
    String mensaje = "NO_OK";
    
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    buscarDocumentoRecepBean.setsCoDependencia(usuario.getCoDep());
    buscarDocumentoRecepBean.setsCoEmpleado(usuario.getCempCodemp());
    buscarDocumentoRecepBean.setsTiAcceso(usuarioConfigBean.getTiAcceso());
    
    List list = null;



    
    try {
      buscarDocumentoRecepBean = new BuscarDocumentoRecepBean();
      usuarioConfigBean = new UsuarioConfigBean();
    } catch (Exception ex) {
      mensaje = ex.getMessage();
    } 
    
    if (list != null) {
      if (list.size() >= 100) {
        model.addAttribute("msjEmision", "OPTIMICE SU CONSULTA");
      }
      
      model.addAttribute("recepDocumAdmList", list);
      list = null;
      mensaje = "OK";
    } else {
      mensaje = "No se encuentran registros.";
    } 
    if (mensaje.equals("OK")) {
      return "/recepDocumentosAdm/tablaDocumentoAdm";
    }
    model.addAttribute("pMensaje", mensaje);
    return "respuesta";
  }


  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goListaBusqDocumRecepAdmJson"}, produces = {"application/json"})
  @ResponseBody
  public ProcessResult<List<DocumentoBean>> goListaBusqDocumRecepAdmJson(HttpServletRequest request, Model model) {
    String mensaje = "NO_OK";
    ProcessResult<List<DocumentoBean>> Result = new ProcessResult();
    StringBuilder retval = new StringBuilder();
    try {
      BuscarDocumentoRecepBean buscarDocumentoRecepBean = new BuscarDocumentoRecepBean();



      
      Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
      UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);


      
      String objectg = ServletUtility.getInstancia().loadRequestParameter(request, "buscarDocumentoRecepBean");
      ObjectMapper mapper = new ObjectMapper();
      buscarDocumentoRecepBean = (BuscarDocumentoRecepBean)mapper.readValue(objectg, BuscarDocumentoRecepBean.class);
      
      buscarDocumentoRecepBean.setEsIncluyeOficina(Boolean.valueOf(ServletUtility.getInstancia().loadRequestParameter(request, "EsIncluyeOficina")).booleanValue());
      buscarDocumentoRecepBean.setEsIncluyeProfesional(Boolean.valueOf(ServletUtility.getInstancia().loadRequestParameter(request, "EsIncluyeProfesional")).booleanValue());
      
      buscarDocumentoRecepBean.setsCoDependencia(usuario.getCoDep());
      buscarDocumentoRecepBean.setsCoEmpleado(usuario.getCempCodemp());
      buscarDocumentoRecepBean.setsTiAcceso(usuarioConfigBean.getTiAcceso());


      
      String NumeroPagina = ServletUtility.getInstancia().loadRequestParameter(request, "NumeroPagina");
      String RegistrosPagina = ServletUtility.getInstancia().loadRequestParameter(request, "RegistrosPagina");
      String Orden = ServletUtility.getInstancia().loadRequestParameter(request, "Orden");
      buscarDocumentoRecepBean.setOrden(Orden);
      FiltroPaginate paginate = new FiltroPaginate();
      paginate.setNumeroPagina(Integer.parseInt(NumeroPagina));
      paginate.setRegistrosPagina(Integer.parseInt(RegistrosPagina));
      Result = this.recepDocumentoAdmService.getDocumentosBuscaRecepAdmLst(buscarDocumentoRecepBean, paginate);
      
      buscarDocumentoRecepBean = new BuscarDocumentoRecepBean();
      paginate = new FiltroPaginate();
      usuario = new Usuario();
      usuarioConfigBean = new UsuarioConfigBean();
      objectg = "";
      mapper = new ObjectMapper();
    
    }
    catch (Exception ex) {
      Result.setIsSuccess(false);
      Result.setMessage(ex.getMessage());
    } 
    
    return Result;
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goEditarDocumento"})
  public String goEditarDocumento(HttpServletRequest request, Model model) {
    String mensaje = "NO_OK";
    String snuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
    String snuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pnuEmi");
    String snuDes = ServletUtility.getInstancia().loadRequestParameter(request, "pnuDes");
    String scoPri = ServletUtility.getInstancia().loadRequestParameter(request, "pcoPri");
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    String fechaHoraActual = Utility.getInstancia().dateToFormatString3(new Date());

    try {
      DocumentoBean documentoBean = this.recepDocumentoAdmService.getDocumentoRecepAdm(snuAnn, snuEmi, snuDes);
      ExpedienteBean expedienteBean = this.recepDocumentoAdmService.getExpDocumentoRecepAdm(documentoBean.getNuAnnExp(), documentoBean.getNuSecExp());
      if (expedienteBean != null) {
        documentoBean.setNuExpediente(expedienteBean.getNuExpediente());
        documentoBean.setNuAnnExp(expedienteBean.getNuAnnExp());
        documentoBean.setNuSecExp(expedienteBean.getNuSecExp());
        documentoBean.setFeExpCorta(expedienteBean.getFeExpCorta());
        documentoBean.setCoProceso(expedienteBean.getCoProceso());
        documentoBean.setDeProceso(expedienteBean.getDeProceso());
      } 
      documentoBean.setCoPri(scoPri);
      List list = this.recepDocumentoAdmService.getDocumentosRefRecepAdm(snuAnn, snuEmi);
      model.addAttribute("pfechaHoraActual", fechaHoraActual);
      model.addAttribute("pcodEmp", usuario.getCempCodemp());
      model.addAttribute("pdesEmp", usuario.getDeFullName());
      model.addAttribute("documentoRecepBean", documentoBean);
      model.addAttribute("expedienteBean", expedienteBean);
      model.addAttribute("refRecepDocumAdmList", list);
      
      list = null;
      documentoBean = new DocumentoBean();
      expedienteBean = new ExpedienteBean();
      usuario = new Usuario();
      
      mensaje = "OK";
    } catch (Exception ex) {
      logger.error(snuAnn + ":" + snuEmi + ":" + snuDes, ex);
      mensaje = "Error en Editar Documento";
    } 
    if (mensaje.equals("OK")) {
      return "/recepDocumentosAdm/editRecepDocumAdm";
    }
    model.addAttribute("pMensaje", mensaje);
    return "respuesta";
  }

  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goUpdDocumento"})
  @ResponseBody
  public String goUpdDocumento(DocumentoBean documentoBean, HttpServletRequest request, BindingResult result, Model model) {
    String coRespuesta, mensaje = "NO_OK";
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    documentoBean.setCodUseMod(usuario.getCoUsuario());
    documentoBean.setCoEmpRec(usuario.getCempCodemp());
    String sAccion = ServletUtility.getInstancia().loadRequestParameter(request, "vAccion");
    String sEsDocAnu = ServletUtility.getInstancia().loadRequestParameter(request, "vEsDocAnu");
    sEsDocAnu = sEsDocAnu.equals("") ? "A" : sEsDocAnu;


    
    try {
      mensaje = this.recepDocumentoAdmService.updDocumentoRecepAdm(documentoBean, sAccion, sEsDocAnu, usuario);
    }
    catch (validarDatoException e) {
      mensaje = e.valorMsg;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    if (mensaje.equals("OK")) {
      coRespuesta = "1";
    } else {
      coRespuesta = "0";
    } 
    String deRespuesta = mensaje;
    
    StringBuilder retval = new StringBuilder();
    retval.append("{\"coRespuesta\":\"");
    retval.append(coRespuesta);
    retval.append("\",\"deRespuesta\":\"");
    retval.append(deRespuesta);
    retval.append("\",\"nuCorDes\":\"");
    retval.append(documentoBean.getNuCorDes());
    retval.append("\",\"esDocRec\":\"");
    retval.append(documentoBean.getEsDocRec());
    retval.append("\",\"deEsDocRec\":\"");
    retval.append(documentoBean.getDeEsDocDes());
    retval.append("\"}");

    
    documentoBean = new DocumentoBean();
    usuario = new Usuario();
    
    return retval.toString();
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goBuscaEmpleado"})
  public String goBuscaEmpleado(HttpServletRequest request, Model model) {
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    model.addAttribute("listaEmpleado", this.referencedData.getListEmpleadoDependencia(usuario.getCoDep()));
    usuario = new Usuario();
    return "/modalGeneral/consultaEmpleado";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goBuscaRemitente"})
  public String goBuscaRemitente(HttpServletRequest request, Model model) {
    String pnuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    model.addAttribute("listaRemitente", this.referencedData.getListRemitente(pnuAnn, usuarioConfigBean));
    usuarioConfigBean = new UsuarioConfigBean();
    return "/modalGeneral/consultaRemitente";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goBuscaDestinatario"})
  public String goBuscaDestinatario(HttpServletRequest request, Model model) {
    String pnuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    model.addAttribute("listaDestinatario", this.referencedData.getListDestinatario(pnuAnn, usuarioConfigBean.getCoDep(), usuarioConfigBean.getTiAcceso(), usuarioConfigBean.getCempCodemp()));
    usuarioConfigBean = new UsuarioConfigBean();
    return "/modalGeneral/consultaDestinatario";
  }
  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goBuscaDocumentoEnReferencia"})
  public String goBuscaDocumentoEnReferencia(BuscarDocumentoRecepBean buscarDocumentoRecepBean, HttpServletRequest request, BindingResult result, Model model) {
    String mensaje = "NO_OK";
    
    Usuario usuario = Utilidades.getInstancia().loadUserFromSession(request);
    UsuarioConfigBean usuarioConfigBean = Utilidades.getInstancia().loadUserConfigFromSession(request);
    buscarDocumentoRecepBean.setsCoDependencia(usuario.getCoDep());
    buscarDocumentoRecepBean.setsCoEmpleado(usuario.getCempCodemp());
    buscarDocumentoRecepBean.setsTiAcceso(usuarioConfigBean.getTiAcceso());
    
    HashMap mp = null;
    List list = null;
    
    try {
      mp = this.recepDocumentoAdmService.getDocumentosEnReferencia(buscarDocumentoRecepBean);
      buscarDocumentoRecepBean = new BuscarDocumentoRecepBean();
      usuarioConfigBean = new UsuarioConfigBean();
    }
    catch (Exception ex) {
      mensaje = ex.getMessage();
    } 
    mensaje = String.valueOf(mp.get("msjResult"));
    if (mensaje.equals("1")) {
      list = (List)mp.get("recepDocumAdmList");
      if (list != null) {
        if (list.size() >= 100) {
          model.addAttribute("msjEmision", "OPTIMICE SU CONSULTA");
        }
        model.addAttribute("recepDocumAdmList", list);
        list = null;
        mensaje = "OK";
      } 
    } 
    if (mensaje.equals("OK")) {
      return "/recepDocumentosAdm/tablaDocumentoAdm";
    }
    model.addAttribute("pMensaje", mensaje);
    return "respuesta";
  }


  
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goVerAtendido"})
  @ResponseBody
  public String goVerAtendido(DocumentoBean documentoBean, HttpServletRequest request, Model model) {
    String mensaje = "NO_OK";
    String pnuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
    String pnuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pnuEmi");
    String pnuDes = ServletUtility.getInstancia().loadRequestParameter(request, "pnuDes");
    
    try {
      String vret = this.recepDocumentoAdmService.getVerificaAtendido(pnuAnn, pnuEmi, pnuDes);
      if (vret == null || vret.equals("0")) {
        mensaje = "OK";
      } else {
        mensaje = "NO_OK";
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    StringBuilder retval = new StringBuilder();
    retval.append("{\"retval\":\"");
    retval.append(mensaje);
    retval.append("\"}");
    
    return retval.toString();
  }
  @RequestMapping(method = {RequestMethod.POST}, params = {"accion=goConsultaAtendido"})
  public String goConsultaAtendido(BuscarDocumentoRecepBean buscarDocumentoRecepBean, HttpServletRequest request, Model model) {
    String mensaje = "NO_OK";
    
    String pnuAnn = ServletUtility.getInstancia().loadRequestParameter(request, "pnuAnn");
    String pnuEmi = ServletUtility.getInstancia().loadRequestParameter(request, "pnuEmi");
    String pnuDes = ServletUtility.getInstancia().loadRequestParameter(request, "pnuDes");
    List<ReferenciaBean> listSeg = null;
    
    try {
      listSeg = this.recepDocumentoAdmService.getConsultaAtendido(pnuAnn, pnuEmi, pnuDes);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    model.addAttribute("listAtendido", listSeg);
    listSeg = null;
    return "/recepDocumentosAdm/consultaAtendido";
  }


  
  LinkedList<DocumentoFileBean> files = new LinkedList<DocumentoFileBean>();
  DocumentoFileBean fileMeta = null;
  
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
}
