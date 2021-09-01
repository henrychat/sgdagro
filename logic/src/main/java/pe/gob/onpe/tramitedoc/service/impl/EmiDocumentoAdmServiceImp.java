package pe.gob.onpe.tramitedoc.service.impl;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.libreria.exception.validarDatoException;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.CasillaElectronicaBean;
import pe.gob.onpe.tramitedoc.bean.CiudadanoBean;
import pe.gob.onpe.tramitedoc.bean.DatosInterBean;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.bean.DestinatarioDocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.DestinatarioOtroOrigenBean;
import pe.gob.onpe.tramitedoc.bean.DestinoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoDatoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoObjBean;
import pe.gob.onpe.tramitedoc.bean.EmpleadoBean;
import pe.gob.onpe.tramitedoc.bean.EmpleadoVoBoBean;
import pe.gob.onpe.tramitedoc.bean.ExpedienteBean;
import pe.gob.onpe.tramitedoc.bean.MotivoBean;
import pe.gob.onpe.tramitedoc.bean.OtroOrigenBean;

import pe.gob.onpe.tramitedoc.bean.PermisoUsuarioBean;
import pe.gob.onpe.tramitedoc.bean.ProveedorBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaEmiDocBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaRemitoBean;
import pe.gob.onpe.tramitedoc.bean.RemitenteEmiBean;
import pe.gob.onpe.tramitedoc.bean.TblRemitosBean;
import pe.gob.onpe.tramitedoc.bean.TrxDocumentoEmiBean;
import pe.gob.onpe.tramitedoc.dao.CasillaElectronicaDao;
import pe.gob.onpe.tramitedoc.dao.CommonQueryDao;
import pe.gob.onpe.tramitedoc.dao.DocumentoTriggerDao;
import pe.gob.onpe.tramitedoc.dao.DocumentoVoBoDao;
import pe.gob.onpe.tramitedoc.dao.EmiDocumentoAdmDao;
import pe.gob.onpe.tramitedoc.dao.EmiDocumentoPersonalDao;
import pe.gob.onpe.tramitedoc.dao.MaestrosDao;
import pe.gob.onpe.tramitedoc.dao.OtroOrigenDao;
import pe.gob.onpe.tramitedoc.service.ActualizaResumenService;
import pe.gob.onpe.tramitedoc.service.AuditoriaMovimientoDocService;
import pe.gob.onpe.tramitedoc.service.EmiDocumentoAdmService;
import pe.gob.onpe.tramitedoc.service.PermisoService;
import pe.gob.onpe.tramitedoc.service.TipoDocumentoService;
import pe.gob.onpe.tramitedoc.service.ValidarFirmaService;
import pe.gob.onpe.tramitedoc.util.ArchivoTemporal;
import pe.gob.onpe.tramitedoc.util.Constantes;
import pe.gob.onpe.tramitedoc.util.FiltroPaginate;
import pe.gob.onpe.tramitedoc.util.ProcessResult;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;




@Service("emiDocumentoAdmService")
public class EmiDocumentoAdmServiceImp  implements EmiDocumentoAdmService
{
  private static Logger logger = Logger.getLogger("SGDEmiDocumentoAdmService");

  
  @Autowired
  private EmiDocumentoAdmDao emiDocumentoAdmDao;

  
  @Autowired
  private EmiDocumentoPersonalDao emiDocumentoPersonalDao;

  
  @Autowired
  private DocumentoTriggerDao documentoTriggerDao;
  
  @Autowired
  private MaestrosDao maestrosDao;
  
  @Autowired
  private CommonQueryDao commonQueryDao;
  
  @Autowired
  private ActualizaResumenService actualizaResumenService;
  
  @Autowired
  private ValidarFirmaService validarFirmaService;
  
  @Autowired
  AuditoriaMovimientoDocService audiMovDoc;
  
  @Autowired
  private DocumentoVoBoDao docVoboDao;
  
  @Autowired
  private TipoDocumentoService tipoDocumentoService;
  
  @Autowired
  private CasillaElectronicaDao casillaElectronicaDao;
  
  @Autowired
  private OtroOrigenDao otroOrigenDao;
  
  @Autowired
  private PermisoService permisoService;

  
  public List<DocumentoEmiBean> getDocumentosBuscaEmiAdm(BuscarDocumentoEmiBean buscarDocumentoEmiBean) {
    List<DocumentoEmiBean> list = null;
    try {
      list = this.emiDocumentoAdmDao.getDocumentosBuscaEmiAdm(buscarDocumentoEmiBean);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }

  public ProcessResult<List<DocumentoBean>> getDocumentosBuscaEmiAdmList(BuscarDocumentoEmiBean buscarDocumentoEmi, FiltroPaginate paginate) { 
	  return this.emiDocumentoAdmDao.getDocumentosBuscaEmiAdmList(buscarDocumentoEmi, paginate); 
  }
 

  public DocumentoEmiBean getDocumentoEmitido(String pnuAnn, String pnuEmi) {
    DocumentoEmiBean documentoEmiBean = null;
    try {
      documentoEmiBean = this.emiDocumentoAdmDao.getDocumentoEmitido(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return documentoEmiBean;
  }

  
  public DocumentoEmiBean getDocumentoEmiAdm(String pnuAnn, String pnuEmi) {
    DocumentoEmiBean documentoEmiBean = null;
    try {
      documentoEmiBean = this.emiDocumentoAdmDao.getDocumentoEmiAdm(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return documentoEmiBean;
  }

  
  public ExpedienteBean getExpDocumentoEmitido(String pnuAnnExp, String pnuSecExp) {
    ExpedienteBean expedienteBean = null;
    try {
      expedienteBean = this.emiDocumentoAdmDao.getExpDocumentoEmitido(pnuAnnExp, pnuSecExp);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return expedienteBean;
  }

  
  public EmpleadoBean getEmpleadoLocaltblDestinatario(String pcoDependencia) {
    EmpleadoBean empleadoBean = null;
    try {
      empleadoBean = this.emiDocumentoAdmDao.getEmpleadoLocaltblDestinatario(pcoDependencia);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return empleadoBean;
  }
  
  public List<EmpleadoBean> getPersonalDestinatario(String pcoDepen) {
    List<EmpleadoBean> list = null;
    try {
      list = this.emiDocumentoAdmDao.getPersonalDestinatario(pcoDepen);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }

  
  public List<MotivoBean> getLstMotivoxTipoDocumento(String pcoDepen, String pcoTipDoc) {
    List<MotivoBean> list = null;
    try {
      list = this.emiDocumentoAdmDao.getLstMotivoxTipoDocumento(pcoDepen, pcoTipDoc);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public List<DocumentoBean> getLstDocEmitidoRef(String pcoEmpEmi, String pcoDepen, String pannio, String ptiDoc, String pnuDoc) {
    List<DocumentoBean> list = null;
    try {
      list = this.emiDocumentoAdmDao.getLstDocEmitidoRef(pcoEmpEmi, pcoDepen, pannio, ptiDoc, pnuDoc);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public List<DocumentoBean> getLstDocRecepcionadoRef(String pcoDepen, String pannio, String ptiDoc, String pnuDoc, String inMesaPartes) {
    List<DocumentoBean> list = null;
    try {
      if (inMesaPartes != null && inMesaPartes.equals("1")) {
        list = this.emiDocumentoAdmDao.getLstDocRecepcionadoRefMp(pcoDepen, pannio, ptiDoc, pnuDoc);
      } else {
        list = this.emiDocumentoAdmDao.getLstDocRecepcionadoRef(pcoDepen, pannio, ptiDoc, pnuDoc);
      } 
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public List<DestinatarioDocumentoEmiBean> _getLstDestintariotlbEmi(String pnuAnn, String pnuEmi) {
    List<DestinatarioDocumentoEmiBean> list = null;
    try {
      list = this.emiDocumentoAdmDao.getLstDestintariotlbEmi(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public HashMap getLstDestintariotlbEmi(String pnuAnn, String pnuEmi) {
    List<DestinatarioDocumentoEmiBean> list = null;
    HashMap map = null;
    try {
      list = this.emiDocumentoAdmDao.getLstDestintariotlbEmi(pnuAnn, pnuEmi);
      map = getLstDestinatario(list);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return map;
  }

  
  private HashMap getLstDestinatario(List<DestinatarioDocumentoEmiBean> list) {
    HashMap<Object, Object> map = new HashMap<Object, Object>();
    List<DestinatarioDocumentoEmiBean> lst1 = new ArrayList();
    List<DestinatarioDocumentoEmiBean> lst2 = new ArrayList();
    List<DestinatarioDocumentoEmiBean> lst3 = new ArrayList();
    List<DestinatarioDocumentoEmiBean> lst4 = new ArrayList();
    for (DestinatarioDocumentoEmiBean dest : list) {
      if (dest != null) {
        String coTipoDest = dest.getCoTipoDestino();
        if (coTipoDest.equals("01")) {
          lst1.add(dest); continue;
        }  if (coTipoDest.equals("02")) {
          lst2.add(dest); continue;
        }  if (coTipoDest.equals("03")) {
          lst3.add(dest); continue;
        }  if (coTipoDest.equals("04")) {
          lst4.add(dest);
        }
      } 
    } 
    map.put("lst1", lst1);
    map.put("lst2", lst2);
    map.put("lst3", lst3);
    map.put("lst4", lst4);
    return map;
  }

  
  public List<ReferenciaBean> getLstDocumReferenciatblEmi(String pnuAnn, String pnuEmi) {
    List<ReferenciaBean> list = null;
    try {
      list = this.emiDocumentoAdmDao.getLstDocumReferenciatblEmi(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public String getTipoDestinatarioEmi(String pnuAnn, String pnuEmi) {
    String retval = null;
    try {
      retval = this.emiDocumentoAdmDao.getTipoDestinatarioEmi(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return retval;
  }

  
  public List<DestinatarioOtroOrigenBean> getLstOtrosOrigenesAgrega(String pdescripcion) {
    List<DestinatarioOtroOrigenBean> list = null;
    try {
      list = this.commonQueryDao.getLstOtrosOrigenes(pdescripcion);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public List<CiudadanoBean> getLstCiudadano(String nombres) {
    List<CiudadanoBean> ciudadanoBean = null;
    try {
      ciudadanoBean = this.commonQueryDao.getLstCiudadanos(nombres);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return ciudadanoBean;
  }













































































































  
  public CiudadanoBean getCiudadano(String pnuDoc) {
    CiudadanoBean ciudadanoBean = null;
    try {
      ciudadanoBean = this.commonQueryDao.getCiudadano(pnuDoc);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return ciudadanoBean;
  }

  
  public List<ProveedorBean> getLstProveedoresAgrega(String pRazonSocial) {
    List<ProveedorBean> list = null;
    try {
      list = this.commonQueryDao.getLstProveedores(pRazonSocial);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }


  
  public String getJsonAfterGrabaDocumentoEmi(String snuAnn, String snuEmi) {
    String retval = null;

    
    try {
      List<DestinatarioDocumentoEmiBean> list1 = this.emiDocumentoAdmDao.getLstDestintariotlbEmi(snuAnn, snuEmi);
      List<ReferenciaBean> list2 = this.emiDocumentoAdmDao.getLstDocumReferenciatblEmi(snuAnn, snuEmi);
      Writer strWriter = new StringWriter();
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(strWriter, getJsonTblDocEmiAdm(list1, list2));
      retval = strWriter.toString();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return retval;
  }


  
  public String _getJsonAfterGrabaDocumentoEmi(String snuAnn, String snuEmi) {
    String retval = null;

    
    try {
      List<DestinatarioDocumentoEmiBean> list1 = this.emiDocumentoAdmDao.getLstDestintariotlbEmi(snuAnn, snuEmi);
      List<ReferenciaBean> list2 = this.emiDocumentoAdmDao.getLstDocumReferenciatblEmi(snuAnn, snuEmi);
      retval = _getJsonTblDocEmiAdm(list1, list2);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return retval;
  }
  
  private String _getJsonTblDocEmiAdm(List<DestinatarioDocumentoEmiBean> lDest, List<ReferenciaBean> lRef) {
    String userDataJSON = null;
    StringBuilder retval = new StringBuilder();
    
    try {
      HashMap map = getLstDestinatario(lDest);
      Writer strWriter = new StringWriter();
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(strWriter, lRef);
      userDataJSON = strWriter.toString();
      
      String coRespuesta = "1";
      retval.append("{\"coRespuesta\":\"");
      retval.append(coRespuesta);
      retval.append("\",\"lsReferencia\":");
      retval.append(userDataJSON);
      retval.append(",\"lsDestInstitu\":");
      strWriter = new StringWriter();
      mapper = new ObjectMapper();
      mapper.writeValue(strWriter, map.get("lst1"));
      userDataJSON = strWriter.toString();
      retval.append(userDataJSON);
      
      retval.append(",\"lsDestProveedor\":");
      strWriter = new StringWriter();
      mapper = new ObjectMapper();
      mapper.writeValue(strWriter, map.get("lst2"));
      userDataJSON = strWriter.toString();
      retval.append(userDataJSON);
      
      retval.append(",\"lsDestCiudadano\":");
      strWriter = new StringWriter();
      mapper = new ObjectMapper();
      mapper.writeValue(strWriter, map.get("lst3"));
      userDataJSON = strWriter.toString();
      retval.append(userDataJSON);
      
      retval.append(",\"lsDestOtros\":");
      strWriter = new StringWriter();
      mapper = new ObjectMapper();
      mapper.writeValue(strWriter, map.get("lst4"));
      userDataJSON = strWriter.toString();
      retval.append(userDataJSON);
      retval.append("}");
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    
    return retval.toString();
  }
  
  private StringBuilder getJsonTblDocEmiAdm(List<DestinatarioDocumentoEmiBean> lDest, List<ReferenciaBean> lRef) {
    boolean bandera = false;
    StringBuilder retval = new StringBuilder();
    HashMap map = getLstDestinatario(lDest);
    String coRespuesta = "1";
    retval.append("{\"coRespuesta\":\"");
    retval.append(coRespuesta);
    retval.append("\",\"lsDestInstitu\":");
    retval.append("[");
    
    for (DestinatarioDocumentoEmiBean dest : (List<DestinatarioDocumentoEmiBean>)map.get("lst1")) {
      retval.append("{\"deDependencia\":\"");
      retval.append(dest.getDeDependencia());
      retval.append("\",\"coDependencia\":\"");
      retval.append(dest.getCoDependencia());
      retval.append("\",\"coLocal\":\"");
      retval.append(dest.getCoLocal());
      retval.append("\",\"deLocal\":\"");
      retval.append(dest.getDeLocal());
      retval.append("\",\"coEmpleado\":\"");
      retval.append(dest.getCoEmpleado());
      retval.append("\",\"deEmpleado\":\"");
      retval.append(dest.getDeEmpleado());
      retval.append("\",\"coTramite\":\"");
      retval.append(dest.getCoTramite());
      retval.append("\",\"deTramite\":\"");
      retval.append(dest.getDeTramite());
      retval.append("\",\"deIndicaciones\":\"");
      retval.append(dest.getDeIndicaciones());
      retval.append("\",\"coPrioridad\":\"");
      retval.append(dest.getCoPrioridad());
      retval.append("\",\"nuDes\":\"");
      retval.append(dest.getNuDes());
      retval.append("\"},");
      bandera = true;
    } 
    if (bandera) {
      retval.deleteCharAt(retval.length() - 1);
      bandera = false;
    } 
    retval.append("]");
    
    retval.append(",\"lsDestProveedor\":");
    retval.append("[");
    for (DestinatarioDocumentoEmiBean dest : (List<DestinatarioDocumentoEmiBean>)map.get("lst2")) {
      retval.append("{\"nuRuc\":\"");
      retval.append(dest.getNuRuc());
      retval.append("\",\"deProveedor\":\"");
      retval.append(dest.getDeProveedor());
      retval.append("\",\"nuDes\":\"");
      retval.append(dest.getNuDes());
      retval.append("\"},");
      bandera = true;
    } 
    if (bandera) {
      retval.deleteCharAt(retval.length() - 1);
      bandera = false;
    } 
    retval.append("]");
    
    retval.append(",\"lsDestCiudadano\":");
    retval.append("[");
    for (DestinatarioDocumentoEmiBean dest : (List<DestinatarioDocumentoEmiBean>)map.get("lst3")) {
      retval.append("{\"nuDni\":\"");
      retval.append(dest.getNuDni());
      retval.append("\",\"deCiudadano\":\"");
      retval.append(dest.getDeCiudadano());
      retval.append("\",\"coLocal\":\"");
      retval.append(dest.getCoLocal());
      retval.append("\",\"nuDes\":\"");
      retval.append(dest.getNuDes());
      retval.append("\"},");
      bandera = true;
    } 
    if (bandera) {
      retval.deleteCharAt(retval.length() - 1);
      bandera = false;
    } 
    retval.append("]");
    
    retval.append(",\"lsDestOtros\":");
    retval.append("[");
    for (DestinatarioDocumentoEmiBean dest : (List<DestinatarioDocumentoEmiBean>)map.get("lst4")) {
      retval.append("{\"coOtroOrigen\":\"");
      retval.append(dest.getCoOtroOrigen());
      retval.append("\",\"deNombreOtroOrigen\":\"");
      retval.append(dest.getDeNombreOtroOrigen());
      retval.append("\",\"deTipoDocOtroOrigen\":\"");
      retval.append(dest.getDeTipoDocOtroOrigen());
      retval.append("\",\"nuDocOtroOrigen\":\"");
      retval.append(dest.getNuDocOtroOrigen());
      retval.append("\",\"nuDes\":\"");
      retval.append(dest.getNuDes());
      retval.append("\"},");
      bandera = true;
    } 
    if (bandera) {
      retval.deleteCharAt(retval.length() - 1);
      bandera = false;
    } 
    retval.append("]");
    
    retval.append(",\"lsReferencia\":");
    retval.append("[");
    for (ReferenciaBean ref : lRef) {
      retval.append("{\"coRef\":\"");
      retval.append(ref.getCoRef());
      retval.append("\",\"nuAnnRef\":\"");
      retval.append(ref.getNuAnnRef());
      retval.append("\",\"nuEmiRef\":\"");
      retval.append(ref.getNuEmiRef());
      retval.append("\",\"nuDesRef\":\"");
      retval.append(ref.getNuDesRef());
      retval.append("\",\"liNuDoc\":\"");
      retval.append(ref.getLiNuDoc());
      retval.append("\",\"liTipDoc\":\"");
      retval.append(ref.getLiTipDoc());
      retval.append("\",\"feEmiCorta\":\"");
      retval.append(ref.getFeEmiCorta());
      retval.append("\",\"tipDocRef\":\"");
      retval.append(ref.getTipDocRef());
      retval.append("\",\"coTipDocAdm\":\"");
      retval.append(ref.getCoTipDocAdm());
      retval.append("\",\"nuDes\":\"");
      retval.append(ref.getNuDes());
      retval.append("\"},");
      bandera = true;
    } 
    if (bandera) {
      retval.deleteCharAt(retval.length() - 1);
    }
    retval.append("]");
    retval.append("}");
    
    return retval;
  }


  
  public DocumentoEmiBean getDocumentoEmiAdmNew(String sEstadoDocEmi, String codDependencia) {
    DocumentoEmiBean documentoEmiBean = null;
    try {
      documentoEmiBean = this.emiDocumentoAdmDao.getDocumentoEmiAdmNew(sEstadoDocEmi, codDependencia);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return documentoEmiBean;
  }


















  
  public String anularDocumentoEmiAdm(DocumentoEmiBean documentoEmiBean, Usuario usuario) {
    String vReturn = "NO_OK";
    String pcoUsedMod = documentoEmiBean.getCoUseMod();
    try {
      documentoEmiBean = this.emiDocumentoAdmDao.getEstadoDocumento(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
      String pesDocEmi = documentoEmiBean.getEsDocEmi();
      if (pesDocEmi != null && (pesDocEmi.equals("5") || pesDocEmi.equals("7"))) {
        documentoEmiBean.setCoUseMod(pcoUsedMod);
        vReturn = this.emiDocumentoAdmDao.verificarDocumentoLeido(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
        if (vReturn != null && vReturn.equals("0")) {
          
          vReturn = this.docVoboDao.existeVistoBuenoDocAdm(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
          if (vReturn.equals("0")) {
            documentoEmiBean.setEsDocEmi("9");
            vReturn = this.emiDocumentoAdmDao.anularDocumentoEmiAdm(documentoEmiBean);
            if (vReturn.equals("OK")) {
              vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi(), pesDocEmi);
              if (!vReturn.equals("OK")) {
                vReturn = "Error insertando Auditoria.";
              }
            } else {
              vReturn = "Error anulando documento.";
            } 
          } else {
            vReturn = "Doc. con Visto Bueno u Observado.";
          } 
        } else {
          vReturn = "Documento ya fue leido por los Destinatarios";
        } 
      } else {
        vReturn = "Documento ya fue leido por los Destinatarios";
      } 
    } catch (Exception e) {
      vReturn = e.getMessage();
    } 
    return vReturn;
  }

  
  public String getLstDestintarioAgregaGrupo(String pcoGrupo, String pcoTipDoc) {
    StringBuilder retval = new StringBuilder();

    
    List<DestinatarioDocumentoEmiBean> list = null;
    
    try {
      list = this.emiDocumentoAdmDao.getLstDestinatarioGrupo(pcoGrupo, pcoTipDoc);
      if (list != null) {
        Writer strWriter = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        
        String coRespuesta = "1";
        retval.append("{\"coRespuesta\":\"");
        retval.append(coRespuesta);
        retval.append("\",\"lsDestInstitu\":");
        strWriter = new StringWriter();
        mapper = new ObjectMapper();
        mapper.writeValue(strWriter, list);
        retval.append(strWriter.toString());
        retval.append("}");
      } 
    } catch (Exception e) {
      String coRespuesta = "0";
      retval.append("{\"coRespuesta\":\"");
      retval.append(coRespuesta);
      retval.append("\",\"deRespuesta\":\"");
      retval.append(e.getMessage());
      retval.append("\"}");
    } 
    
    return retval.toString();
  }

  
  public String cargaDocEmi(DocumentoObjBean docObjBean) {
    String vReturn = "NO_OK";
    
    if (docObjBean != null) {
      try {
        docObjBean.setTipoDoc(docObjBean.getNombreArchivo().substring(docObjBean.getNombreArchivo().lastIndexOf('.') + 1));
        docObjBean.setNombreArchivo(docObjBean.getNombreArchivo().substring(docObjBean.getNombreArchivo().lastIndexOf('|') + 1));



        
        byte[] archivoByte = ArchivoTemporal.leerArchivo(docObjBean.getNumeroSecuencia());
        docObjBean.setDocumento(archivoByte);
        docObjBean.setNuTamano((int)Math.round(archivoByte.length / 1024.0D));
        
        DocumentoEmiBean documentoEmiBean = this.emiDocumentoAdmDao.getEstadoDocumento(docObjBean.getNuAnn(), docObjBean.getNuEmi());
        if (documentoEmiBean != null && (documentoEmiBean.getEsDocEmi().equals("5") || documentoEmiBean.getEsDocEmi().equals("7"))) {
          vReturn = this.emiDocumentoAdmDao.updDocumentoObj(docObjBean);
        } else {
          vReturn = "El Documento a cambiado de estado.";
        } 
      } catch (Exception ex) {
        StringBuffer mensaje = new StringBuffer();
        vReturn = "Error en Leer Documento de Repositorio";
        mensaje.append(docObjBean.getNumeroSecuencia() + ":");
        mensaje.append(docObjBean.getNuAnn() + "." + docObjBean.getNuEmi());
        logger.error(mensaje, ex);
      } 
    } else {
      vReturn = "Error en Carga de Documento";
      StringBuffer mensaje = new StringBuffer();
      mensaje.append("cargaDocEmi:Objeto docObjBean NULO");
      logger.error(mensaje);
    } 
    return vReturn;
  }

  
  public String verificaNroDocumentoEmiDuplicado(DocumentoEmiBean documentoEmiBean) {
    String vReturn = "NO_OK";

    
    try {
      vReturn = this.emiDocumentoAdmDao.getCanDocumentoEmiDuplicados(documentoEmiBean);
      if (vReturn.equals("0")) {
        vReturn = "OK";
      } else {
        vReturn = "Nde documento duplicado....!";
      } 
    } catch (Exception e) {
      vReturn = e.getMessage();
    } 


    
    return vReturn;
  }

  
  public List<ReferenciaBean> getLstDocumReferenciaAtiendeDeriva(DocumentoBean documentoRecepBean) {
    List<ReferenciaBean> list = new ArrayList<ReferenciaBean>();
    ReferenciaBean referenciaBean = new ReferenciaBean();
    try {
      referenciaBean.setNuAnnRef(documentoRecepBean.getNuAnn());
      referenciaBean.setCoTipDocAdm(documentoRecepBean.getCoTipDocAdm());
      referenciaBean.setTipDocRef("rec");
      referenciaBean.setLiNuDoc(documentoRecepBean.getNuDoc());
      referenciaBean.setFeEmiCorta(documentoRecepBean.getFeEmiCorta2());
      referenciaBean.setNuEmiRef(documentoRecepBean.getNuEmi());
      referenciaBean.setNuDesRef(documentoRecepBean.getNuDes());
      referenciaBean.setAccionBd("INS");
      referenciaBean.setCoRef("");
      list.add(referenciaBean);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public List<EmpleadoBean> getPersonalEditDocAdmEmision(String pcoDepEmi) {
    List<EmpleadoBean> list = null;
    try {
      list = this.emiDocumentoAdmDao.getPersonalEditDocAdmEmision(pcoDepEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }
  
  public String changeToProyecto(DocumentoEmiBean documentoEmiBean, Usuario usuario) {
    String vReturn = "NO_OK";
    
    String pcoUseMod = documentoEmiBean.getCoUseMod();
    String pcoEmpRes = documentoEmiBean.getCoEmpRes();
    try {
      documentoEmiBean = this.emiDocumentoAdmDao.getEstadoDocumento(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
      if (documentoEmiBean != null && (documentoEmiBean.getEsDocEmi().equals("7") || documentoEmiBean.getEsDocEmi().equals("0"))) {
        String pesDocEmi = documentoEmiBean.getEsDocEmi();
        vReturn = this.emiDocumentoAdmDao.verificarDocumentoLeido(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
        if (vReturn != null && vReturn.equals("0")) {
          if (pesDocEmi.equals("0") && !documentoEmiBean.getCoEmpEmi().equals(pcoEmpRes)) {
            vReturn = "El Documento solo puede ser cambiado por el Funcionario que lo Firmo";
          } else {
            boolean ischangeToProyecto = false;
            if (pesDocEmi.equals("0")) {
              ischangeToProyecto = true;
            } else {
              
              vReturn = this.docVoboDao.existeVistoBuenoDocAdm(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
              if (vReturn.equals("0")) {
                ischangeToProyecto = true;
              }
              else if (documentoEmiBean.getCoEmpEmi().equals(usuario.getCempCodemp()) || documentoEmiBean.getCoEmpRes().equals(usuario.getCempCodemp())) {
                ischangeToProyecto = true;
              } else {
                vReturn = "Doc. con Visto Bueno u Observado.";
              } 
            } 
            
            if (ischangeToProyecto) {
              documentoEmiBean.setEsDocEmi("5");
              documentoEmiBean.setCoUseMod(pcoUseMod);
              vReturn = this.emiDocumentoAdmDao.updEstadoDocumentoEmiAdm(documentoEmiBean);
              if (vReturn.equals("OK")) {
                vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi(), pesDocEmi);
                if (!vReturn.equals("OK")) {
                  vReturn = "Error insertando Auditoria.";
                }
              } else {
                vReturn = "Error al Cambiar a Proyecto el Documento.";
              } 
            } 
          } 
        } else {
          vReturn = "Documento ya fue leido por los Destinatarios.";
        } 
      } else {
        vReturn = "El Documento a cambiado de estado.";
      } 
    } catch (Exception e) {
      vReturn = e.getMessage();
    } 
    return vReturn;
  }
  
  public String getUsuarioNofirmaProveido(String pcoEmpleado) {
    String vReturn = "0";

    
    try {
      vReturn = this.emiDocumentoAdmDao.getUsuarioNofirmaProveido(pcoEmpleado);
    }
    catch (Exception e) {
      vReturn = e.getMessage();
    } 
    return vReturn;
  }

  
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
  public String changeToDespacho(DocumentoEmiBean documentoEmiBean, Usuario usuario) throws Exception {
    String vReturn = "NO_OK";
    String pesDocEmi = documentoEmiBean.getEsDocEmi();
    String coUseMod = documentoEmiBean.getCoUseMod();
    String ticap = documentoEmiBean.getTiCap();
    String nuSecFirma = documentoEmiBean.getNuSecuenciaFirma();
    if (pesDocEmi != null && !pesDocEmi.equals("")) {
      try {
        documentoEmiBean = this.emiDocumentoAdmDao.getEstadoDocumento(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
        if (documentoEmiBean != null && documentoEmiBean.getEsDocEmi() != null && documentoEmiBean.getEsDocEmi().equals("5")) {
          pesDocEmi = documentoEmiBean.getEsDocEmi();
          documentoEmiBean.setCoUseMod(coUseMod);
          if (nuSecFirma != null) {
            DocumentoObjBean docObjBean = new DocumentoObjBean();
            
            docObjBean.setNuAnn(documentoEmiBean.getNuAnn());
            docObjBean.setNuEmi(documentoEmiBean.getNuEmi());
            docObjBean.setTiCap(ticap);
            docObjBean.setNombreArchivo(nuSecFirma);
            docObjBean.setNumeroSecuencia(nuSecFirma);
            docObjBean.setCoUseMod(documentoEmiBean.getCoUseMod());
            vReturn = cargaDocEmi(docObjBean);
            if (!vReturn.equals("OK")) {
              throw new validarDatoException("Error al Cargar Documento");
            }
          } 
          documentoEmiBean.setEsDocEmi("7");
          vReturn = this.emiDocumentoAdmDao.updChangeToDespacho(documentoEmiBean);
          if (!vReturn.equals("OK")) {
            throw new validarDatoException(vReturn);
          }
          
          vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi(), pesDocEmi);
          if (!vReturn.equals("OK")) {
            vReturn = "Error insertando Auditoria.";
            throw new validarDatoException(vReturn);
          
          }

        
        }
        else {

          
          vReturn = "El Documento a cambiado de estado.";
        } 
      } catch (validarDatoException e) {
        throw e;
      } catch (Exception e) {
        e.printStackTrace();
        throw new validarDatoException("ERROR EN TRANSACCION PARA DESPACHO");
      } 
    } else {
      vReturn = "Documento no esta en Despacho.";
    } 
    return vReturn;
  }

  
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
  public String changeToEmitido(DocumentoEmiBean documentoEmiBean, String prutaDoc, Usuario usuario, String nroRucInstitucion) throws Exception {
    String vReturn = "NO_OK";
    String pesDocEmi = documentoEmiBean.getEsDocEmi();
    String coUseMod = documentoEmiBean.getCoUseMod();
    String pnuDocEmi = documentoEmiBean.getNuDocEmi();
    String ticap = documentoEmiBean.getTiCap();
    String nuSecFirma = documentoEmiBean.getNuSecuenciaFirma();
    if (pesDocEmi != null && !pesDocEmi.equals("")) {
      try {
        documentoEmiBean = this.emiDocumentoAdmDao.getEstadoDocumento(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
        if (documentoEmiBean != null && documentoEmiBean.getEsDocEmi() != null && documentoEmiBean.getEsDocEmi().equals("7")) {
          if (!documentoEmiBean.getCoEmpEmi().equals(usuario.getCempCodemp()))
          {
            throw new validarDatoException("El Documento solo puede ser emitido por: <strong>Firmado Por</strong>");
          }
          pesDocEmi = documentoEmiBean.getEsDocEmi();
          documentoEmiBean.setCoUseMod(coUseMod);
          documentoEmiBean.setNuDocEmi(pnuDocEmi);
          DocumentoObjBean docObjBean = new DocumentoObjBean();
          
          docObjBean.setNuAnn(documentoEmiBean.getNuAnn());
          docObjBean.setNuEmi(documentoEmiBean.getNuEmi());
          docObjBean.setTiCap(ticap);
          docObjBean.setNombreArchivo(prutaDoc);
          docObjBean.setNumeroSecuencia(nuSecFirma);
          docObjBean.setCoUseMod(documentoEmiBean.getCoUseMod());
          String NoFirma = this.emiDocumentoAdmDao.getUsuarioNofirmaProveido(usuario.getCempCodemp());
          if (!NoFirma.equals("1") || !documentoEmiBean.getCoTipDocAdm().equals("232")) {



            
            String esOblFirma = this.commonQueryDao.getInFirmaDoc(documentoEmiBean.getCoDepEmi(), documentoEmiBean.getCoTipDocAdm());
            vReturn = this.validarFirmaService.getValidaCertificadoEmp(docObjBean.getNumeroSecuencia(), usuario.getNuDni(), nroRucInstitucion);
            if (vReturn.equals("NO_OK"))
              throw new validarDatoException("Firma no pertenece a Usuario"); 
            if (vReturn.equals("") && esOblFirma.equals("F")) {
              throw new validarDatoException("Necesita Firmar el Documento.");
            }
          } 
          vReturn = cargaDocEmi(docObjBean);
          if (!vReturn.equals("OK")) {
            throw new validarDatoException("Error al Cargar Documento");
          }
          documentoEmiBean.setEsDocEmi("0");
          documentoEmiBean.setNuCorDoc("1");
          vReturn = this.emiDocumentoAdmDao.updEstadoDocumentoEmitido(documentoEmiBean);
          if (!vReturn.equals("OK")) {
            throw new validarDatoException(vReturn);
          }


          
          List<ReferenciaRemitoBean> lsReferencia = this.emiDocumentoPersonalDao.getListaReferenciaRemitos(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
          ReferenciaRemitoBean oReferenciaRemitoBean = new ReferenciaRemitoBean();
          String g = "";
          for (int i = 0; i < lsReferencia.size(); i++) {
            
            oReferenciaRemitoBean = lsReferencia.get(i);
            
            g = this.emiDocumentoPersonalDao.updEstadoDocumentoAdmReferenciasEmitido(docObjBean.getCoUseMod(), oReferenciaRemitoBean.getNu_emi(), oReferenciaRemitoBean.getNu_ann(), oReferenciaRemitoBean.getNu_des_ref());
          } 



          
          TblRemitosBean tblRemitosBean = this.emiDocumentoAdmDao.getDatosDocumento(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
          if (!tblRemitosBean.getMsgResult().equals("OK")) {
            throw new validarDatoException(vReturn);
          }
          vReturn = this.documentoTriggerDao.actualizarEstadoAfterEmitirDocumento(tblRemitosBean.getNuAnn(), tblRemitosBean.getNuEmi(), tblRemitosBean.getCoDepEmi(), tblRemitosBean.getFeEmi());
          if (!vReturn.equals("OK")) {
            throw new validarDatoException(vReturn);
          }
          vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi(), pesDocEmi);
          if (!vReturn.equals("OK")) {
            vReturn = "Error insertando Auditoria.";
            throw new validarDatoException(vReturn);
          } 
          
          vReturn = this.docVoboDao.existeVistoBuenoPendienteDocAdm(tblRemitosBean.getNuAnn(), tblRemitosBean.getNuEmi());
          if (vReturn.equals("0")) {
            vReturn = "OK";
          } else {
            throw new validarDatoException("Doc. con Visto Bueno Pendiente u Observado.");
          }
        
        }
      
      } catch (validarDatoException e) {
        throw e;
      } catch (Exception e) {
        e.printStackTrace();
        throw new validarDatoException("ERROR EN TRANSACCION DE EMISION");
      } 
    } else {
      vReturn = "Documento no esta Listo.";
    } 
    return vReturn;
  }

  
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
  public String changeToEmitidoAlta(DocumentoEmiBean documentoEmiBean) throws Exception {
    String vReturn = "NO_OK";
    String pesDocEmi = documentoEmiBean.getEsDocEmi();
    if (pesDocEmi != null && !pesDocEmi.equals("")) {

      
      try {
        String vpfd = getVerificaPdfDoc(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi(), documentoEmiBean.getTiCap());
        
        if (!vpfd.equals("OK")) {
          throw new validarDatoException("El Documento tiene que estar en PDF");
        }
        
        documentoEmiBean.setEsDocEmi("0");
        documentoEmiBean.setNuCorDoc("1");
        vReturn = this.emiDocumentoAdmDao.updEstadoDocumentoEmitido(documentoEmiBean);
        if (!vReturn.equals("OK")) {
          throw new validarDatoException(vReturn);
        }
        TblRemitosBean tblRemitosBean = this.emiDocumentoAdmDao.getDatosDocumento(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
        if (!tblRemitosBean.getMsgResult().equals("OK")) {
          throw new validarDatoException(vReturn);
        }
        vReturn = this.documentoTriggerDao.actualizarEstadoAfterEmitirDocumento(tblRemitosBean.getNuAnn(), tblRemitosBean.getNuEmi(), tblRemitosBean.getCoDepEmi(), tblRemitosBean.getFeEmi());
        if (!vReturn.equals("OK")) {
          throw new validarDatoException(vReturn);
        }
      } catch (validarDatoException e) {
        throw e;
      } catch (Exception e) {
        e.printStackTrace();
        throw new validarDatoException("ERROR EN TRANSACCION DE EMISION");
      } 
    } else {
      vReturn = "Documento no esta Listo.";
    } 
    return vReturn;
  }

  private boolean pasoEnvioMensajeria(DocumentoEmiBean documentoEmiBean, Usuario usuario) throws Exception {
	 
	 if(!documentoEmiBean.getTiEnvMsj().equals(Constantes.CO_TIPO_ENVIO_MENSAJERIA)) {
		  return true;
	  }
	  if(usuario.getCoLocal().equals(Constantes.COD_LOCAL_CENTRAL) && !documentoEmiBean.getCoDepEmi().equals(Constantes.COD_MENSAJERIA_CENTRAL) ) {
		  PermisoUsuarioBean permiso =new PermisoUsuarioBean(); 
		  // documentoEmiBean.getTiEnvMsj().equals("0")
	    	permiso.setCodAplica(Constantes.COD_SISTEMA_SGD);
	    	permiso.setCodOpc(Constantes.COD_PERMISO_DER_MENSAJ_EXT_SGD);
	    	permiso.setCempCodEmp(usuario.getCempCodemp());
		  if(permisoService.obtenerPermiso(permiso)) {
			  
			   return true;
		  }
		  return false; 
	  }
	   return true;
	  
  }
  private String validaDestinatario(DocumentoEmiBean documentoEmiBean) throws Exception {
	  String mensaje="";
	 List<DestinatarioDocumentoEmiBean> listTemp =  emiDocumentoAdmDao.getLstDestintariotlbEmi(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
	 if(listTemp!=null && !listTemp.isEmpty()) {
		  for (DestinatarioDocumentoEmiBean dest : listTemp) {
			   if(dest.getAccionBD()!=null && dest.getAccionBD().equals("DEL")) {
				   continue;
			   }
			   if ("01".equals(dest.getCoTipoDestino())) {
				if(!mensaje.equals("")) 
				mensaje=mensaje+",<br>";
				   
				   mensaje=mensaje+ "No esta habilitado la notificación electrónica para destinatario institución ";  
			     }
			   if ("02".equals(dest.getCoTipoDestino())) {
				 
				    //co_tipo_destino
				  if(! Utilidades.esVacio( dest.getRemiNuDniEmi()) || !Utilidades.esVacio( dest.getRemiCoOtrOriEmi())){
					  CasillaElectronicaBean casilla=null;
				  
					  if ("04".equals(dest.getRemiTiEmi())) {
					    	OtroOrigenBean  otro =otroOrigenDao.getOtroOrigen( dest.getRemiCoOtrOriEmi());
					    	if(otro.getNuDocOtrOri()==null || otro.getNuDocOtrOri().trim().equals("")) {
					    		if(!mensaje.equals("")) 
									   mensaje=mensaje+",<br>";
									   mensaje=mensaje+ " Destinatario "+otro.getDeRazSocOtr()+" con tipo de documento"
									   +Utilidades.obtenerTipoPersona(otro.getCoTipOtrOri())+" presenta número de documento vacío";
					    	} else
					    	
					    	 casilla= casillaElectronicaDao.obtenerCasillaPersonaJuridica( dest.getRemiTiEmi(),   otro.getNuDocOtrOri(),dest.getNuRuc());
					  } else {
						   casilla= casillaElectronicaDao.obtenerCasillaPersonaJuridica( dest.getRemiTiEmi(),  dest.getRemiNuDniEmi(),dest.getNuRuc());  
					  }
					  if(casilla==null) {
						   if(!mensaje.equals("")) 
						   mensaje=mensaje+",<br>";
						   mensaje=mensaje+ "No existe casilla electrónica para destinatario Persona Jurídica";   
					   }
				  } else {
				   
					   if(!mensaje.equals("")) 
						   mensaje=mensaje+",<br>";
						   mensaje=mensaje+ "No ha señalado representante para destinatario Persona Jurídica";   
				 }
			   }
			   if ("03".equals(dest.getCoTipoDestino())) {
				   CasillaElectronicaBean casilla= casillaElectronicaDao.obtenerCasilla( dest.getCoTipoDestino(),  dest.getNuDni());
				
				   if(casilla==null) {
					   if(!mensaje.equals("") ) 
						   mensaje=mensaje+",<br>";
					   		mensaje=mensaje+ "No existe casilla electrónica para destinatario Ciudadano";   
				   }
				   
			     }
			    if ("04".equals(dest.getCoTipoDestino())) {
			    	OtroOrigenBean  otro =otroOrigenDao.getOtroOrigen( dest.getCoOtroOrigen());
			    	CasillaElectronicaBean casilla=null;
			    	if(otro.getNuDocOtrOri()==null || otro.getNuDocOtrOri().trim().equals("")) {
			    		
			    		if(!mensaje.equals("")) 
							   mensaje=mensaje+",<br>";
							   mensaje=mensaje+ " Destinatario "+otro.getDeRazSocOtr()+" con tipo de documento "
							   +Utilidades.obtenerTipoPersona(otro.getCoTipOtrOri())+" presenta número de documento vacío ";
			    	} else {
			    	 casilla= casillaElectronicaDao.obtenerCasilla( dest.getCoTipoDestino(),  otro.getNuDocOtrOri());
					   
			    	
					   if(casilla==null) {
						   if(!mensaje.equals("")) 
						    mensaje=mensaje+",<br>";
						   mensaje=mensaje+ " Destinatario: "+otro.getDeRazSocOtr()+" con  "
					       +Utilidades.obtenerTipoPersona(otro.getCoTipOtrOri())+"  Nro. "+otro.getNuDocOtrOri()+
								   " no presenta casilla electrónica";   
					   }
			    	}   
			     }
	        
		 
		  }
	 }
	  
   return mensaje;
  }
  public String changeToEnvioNotificacion(DocumentoEmiBean documentoEmiBean, Usuario usuario) throws Exception {
    String vReturn = "NO_OK";


    //EJHM
  
  
      documentoEmiBean.setEsDocEmi("0");
//      if(documentoEmiBean.getTiEnvMsj().equals(Constantes.COD_NOTIFICACION_ELECTRONICA)) {
//    	     String mensaje=	validaDestinatario( documentoEmiBean); 
//    	     if(!mensaje.equals("")) {
//    	    	 throw new validarDatoException(mensaje);// mensaje;	 
//    	     }
//    }
      if(!pasoEnvioMensajeria( documentoEmiBean,  usuario)) {
    	  throw new validarDatoException(" Usuario de Sede Central no puede derivar documentos a mensajeria diferente a la Oficina de Trámite Documentario. ");
      }
    	   
      int cCant = this.emiDocumentoAdmDao.getCantidadAnexo(documentoEmiBean);
      
      if (cCant == 0) { // no se entiende esta linea pero asumo que siempre sera cero
        
        vReturn = this.emiDocumentoAdmDao.updEstadoDocumentoEnvioNotificacion(documentoEmiBean);
        if (vReturn.equals("OK")) {
          vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi(), "0");
          if (!vReturn.equals("OK")) {
            vReturn = "Error insertando Auditoria.";
          }
        } else {
          vReturn = "Error al Enviar el documento ea la mensajería"+ vReturn;
        } 
      } else {
        vReturn = "NO_OK";



      
      }




    
  
    return vReturn;
  }


  private String validarDestinatario(List<DestinatarioDocumentoEmiBean> lstDestinatario, String  tipoDoc) throws Exception {
	  if(lstDestinatario==null) {
		  return null;
	  }
  
	  String indSalidaDoc=tipoDocumentoService.obtenerIndicarSalida(tipoDoc);
	     if(!indSalidaDoc.equals("1")) {
	      
		   for (DestinatarioDocumentoEmiBean dest : lstDestinatario) {
			   if(dest.getAccionBD()!=null && dest.getAccionBD().equals("DEL")) {
				   continue;
			   }
			   if ("02".equals(dest.getCoTipoDestino())) {
				  return "Debe Seleccionar tipo de Documento Carta o Oficio para Destinatario Persona Jurídica";    
			     }
			   
			   if ("03".equals(dest.getCoTipoDestino())) {
				   return "Debe Seleccionar tipo de Documento Carta o Oficio para Destinatario Ciudadano";
			     }
			    if ("04".equals(dest.getCoTipoDestino())) {
			    	return "Debe Seleccionar tipo de Documento Carta o Oficio para Destinatario Otros";
			     }
	        } 
	  }
	 
	 return null;
  }
  
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
  public String grabaDocumentoEmiAdm(TrxDocumentoEmiBean trxDocumentoEmiBean, String pcrearExpediente, Usuario usuario) throws Exception {
    String vReturn = "NO_OK";
    try {
    	     
       	
      String pnuAnn = trxDocumentoEmiBean.getNuAnn();
      String pnuEmi = trxDocumentoEmiBean.getNuEmi();
      String pcoUserMod = trxDocumentoEmiBean.getCoUserMod();
      String pcempCodEmp = trxDocumentoEmiBean.getCempCodEmp();
      DocumentoEmiBean documentoEmiBean = trxDocumentoEmiBean.getDocumentoEmiBean();
      ExpedienteBean expedienteEmiBean = trxDocumentoEmiBean.getExpedienteEmiBean();
      RemitenteEmiBean remitenteEmiBean = trxDocumentoEmiBean.getRemitenteEmiBean();
      ArrayList<ReferenciaEmiDocBean> lstReferencia = trxDocumentoEmiBean.getLstReferencia();
      List<DestinatarioDocumentoEmiBean> lstDestinatario = trxDocumentoEmiBean.getLstDestinatario();
      ArrayList<EmpleadoVoBoBean> lstEmpVoBo = trxDocumentoEmiBean.getLstEmpVoBo();
      DocumentoEmiBean documentoEmiBeanBD = null;
      if (pnuAnn != null && pnuEmi != null && !pnuAnn.equals("") && !pnuEmi.equals("")) {
        documentoEmiBeanBD = this.emiDocumentoAdmDao.getEstadoDocumento(pnuAnn, pnuEmi);
        String mensaje=null;
        //validacion de salida de documento
        String codTipDoc=documentoEmiBean==null?documentoEmiBeanBD.getCoTipDocAdm():documentoEmiBean.getCoTipDocAdm();
        if(trxDocumentoEmiBean.getLstDestinatario()==null || trxDocumentoEmiBean.getLstDestinatario().isEmpty() ) {
        	  List<DestinatarioDocumentoEmiBean> listTemp =  emiDocumentoAdmDao.getLstDestintariotlbEmi(pnuAnn, pnuEmi);
        	   mensaje=validarDestinatario(listTemp,codTipDoc);
        } else {
        	 mensaje=validarDestinatario(trxDocumentoEmiBean.getLstDestinatario(),codTipDoc);
        }
        if( mensaje!=null) {
        	   throw new validarDatoException(mensaje);
        }
        
        if (documentoEmiBeanBD != null && documentoEmiBeanBD.getEsDocEmi() != null && (documentoEmiBeanBD.getEsDocEmi().equals("5") || documentoEmiBeanBD.getEsDocEmi().equals("7"))) {
          if (documentoEmiBean != null || expedienteEmiBean != null || remitenteEmiBean != null) {
            if (documentoEmiBean != null && !documentoEmiBean.getCoTipDocAdm().equals(documentoEmiBeanBD.getCoTipDocAdm())) {
              vReturn = getInNumeraDocAdm(documentoEmiBean.getCoTipDocAdm());
              if ("NO_OK".equals(vReturn)) {
                throw new validarDatoException("Error al Obtener Indicador de Numeración");
              }
              if (vReturn.equals("1")) {
                vReturn = getNumeroDocSiguienteAdm(documentoEmiBean.getNuAnn(), documentoEmiBean.getCoDepEmi(), documentoEmiBean.getCoTipDocAdm());
                if ("NO_OK".equals(vReturn)) {
                  throw new validarDatoException("Error al Obtener Número de Documento.");
                }
                documentoEmiBean.setNuDocEmi(vReturn);
                trxDocumentoEmiBean.setNuDocEmi(vReturn);
                documentoEmiBean.setNuCorDoc("1");
              } 
            } 
            vReturn = this.emiDocumentoAdmDao.updDocumentoEmiAdmBean(pnuAnn, pnuEmi, documentoEmiBean, expedienteEmiBean, remitenteEmiBean, pcoUserMod);
            if ("NO_OK".equals(vReturn)) {
              throw new validarDatoException("Error al Actualizar Documento");
            }
            if (documentoEmiBean != null) {
              trxDocumentoEmiBean.setNuCorEmi(documentoEmiBean.getNuCorEmi());
            }
          } 

          
          for (ReferenciaEmiDocBean ref : lstReferencia) {
            String sAccionBD = ref.getAccionBD();
            if ("INS".equals(sAccionBD)) {
              ref.setCoUseCre(pcoUserMod);
              vReturn = this.emiDocumentoAdmDao.insReferenciaDocumentoEmi(pnuAnn, pnuEmi, ref);
              if ("NO_OK".equals(vReturn))
                throw new validarDatoException("Error Agregando Referencia");  continue;
            } 
            if ("UPD".equals(sAccionBD)) {
              vReturn = this.emiDocumentoAdmDao.updReferenciaDocumentoEmi(pnuAnn, pnuEmi, ref);
              if ("NO_OK".equals(vReturn))
                throw new validarDatoException("Error Actualizando Referencia");  continue;
            } 
            if ("DEL".equals(sAccionBD)) {
              vReturn = this.emiDocumentoAdmDao.delReferenciaDocumentoEmi(pnuAnn, pnuEmi, ref);
              if ("NO_OK".equals(vReturn)) {
                throw new validarDatoException("Error Borrando Referencia");
              }
            } 
          } 
          for (DestinatarioDocumentoEmiBean dest : lstDestinatario) {
            String sAccionBD = dest.getAccionBD();
            dest.setCoUseCre(pcoUserMod);
            if ("INS".equals(sAccionBD)) {
              vReturn = emiDocumentoAdmDao.insDestinatarioDocumentoEmi(pnuAnn, pnuEmi, dest);
              if ("NO_OK".equals(vReturn))
                throw new validarDatoException("Error Agregando Destinatario");  continue;
            } 
            if ("UPD".equals(sAccionBD)) {
              vReturn = this.emiDocumentoAdmDao.updDestinatarioDocumentoEmi(pnuAnn, pnuEmi, dest);
              if ("NO_OK".equals(vReturn))
                throw new validarDatoException("Error Actualizando Destinatario");  continue;
            } 
            if ("DEL".equals(sAccionBD)) {
              vReturn = this.emiDocumentoAdmDao.delDestinatarioDocumentoEmi(pnuAnn, pnuEmi, dest);
              if ("NO_OK".equals(vReturn)) {
                throw new validarDatoException("Error Borrando Destinatario");
              }
            } 
          } 
          for (EmpleadoVoBoBean emp : lstEmpVoBo) {
            String sAccionBD = emp.getAccionBD();
            if ("DEL".equals(sAccionBD)) {
              vReturn = this.emiDocumentoAdmDao.delPersonalVoBo(pnuAnn, pnuEmi, emp.getCoDependencia(), emp.getCoEmpleado());
              if (!"OK".equals(vReturn))
                throw new validarDatoException("Error Borrando Personal VB.");  continue;
            } 
            if ("INS".equals(sAccionBD)) {
              vReturn = this.emiDocumentoAdmDao.insPersonalVoBo(pnuAnn, pnuEmi, emp.getCoDependencia(), emp.getCoEmpleado(), pcoUserMod);
              if (!"OK".equals(vReturn)) {
                throw new validarDatoException("Error Agregando Personal VB.");
              }
            } 
          } 
        } else {
          throw new validarDatoException("El Documento a cambiado de estado.");
        } 
      } else {
        if (documentoEmiBean != null) {
        
          String mensaje=validarDestinatario(trxDocumentoEmiBean.getLstDestinatario(),trxDocumentoEmiBean.getDocumentoEmiBean().getCoTipDocAdm());
             if( mensaje!=null) {
             	   throw new validarDatoException(mensaje);
             }
        	
          if (pcrearExpediente.equals("1")) {
            ExpedienteBean expedienteBean = new ExpedienteBean();
            expedienteBean.setUsCreaAudi(pcempCodEmp);
            expedienteBean.setFeExp(documentoEmiBean.getFeEmiCorta());
            expedienteBean.setCoDepExp(documentoEmiBean.getCoDepEmi());
            expedienteBean.setNuAnnExp(documentoEmiBean.getFeEmiCorta().trim().substring(6, 10));
            vReturn = this.emiDocumentoAdmDao.insExpedienteBean(expedienteBean);
            if ("NO_OK".equals(vReturn)) {
              throw new validarDatoException("Error al Grabar Expediente");
            }
            trxDocumentoEmiBean.setNuAnnExp(expedienteBean.getNuAnnExp());
            trxDocumentoEmiBean.setFeExp(expedienteBean.getFeExp());
            trxDocumentoEmiBean.setNuExpediente(expedienteBean.getNuExpediente());
            trxDocumentoEmiBean.setNuSecExp(expedienteBean.getNuSecExp());
            documentoEmiBean.setNuAnnExp(expedienteBean.getNuAnnExp());
            documentoEmiBean.setNuSecExp(expedienteBean.getNuSecExp());
          } 
          documentoEmiBean.setCoUseMod(pcoUserMod);
          
          documentoEmiBean.setNuDocEmi(null);
          vReturn = getInNumeraDocAdm(documentoEmiBean.getCoTipDocAdm());
          if ("NO_OK".equals(vReturn)) {
            throw new validarDatoException("Error al Obtener Indicador de Numeracion");
          }
          if (vReturn.equals("1")) {
            vReturn = getNumeroDocSiguienteAdm(documentoEmiBean.getNuAnn(), documentoEmiBean.getCoDepEmi(), documentoEmiBean.getCoTipDocAdm());
            if ("NO_OK".equals(vReturn)) {
              throw new validarDatoException("Error al Obtener Numero de Documento.");
            }
            documentoEmiBean.setNuDocEmi(vReturn);
            trxDocumentoEmiBean.setNuDocEmi(vReturn);
            documentoEmiBean.setNuCorDoc("1");
          } else {
            vReturn = this.emiDocumentoAdmDao.getNroCorrelativoDocumento(documentoEmiBean);
            if ("NO_OK".equals(vReturn)) {
              throw new validarDatoException("Error al Obtener Numero Correlativo de Documento.");
            }
            documentoEmiBean.setNuCorDoc(vReturn);
          } 
          
          vReturn = this.emiDocumentoAdmDao.insDocumentoEmiBean(documentoEmiBean, expedienteEmiBean, remitenteEmiBean);
          if ("NO_OK".equals(vReturn)) {
            throw new validarDatoException("Error al Grabar Documento");
          }
          trxDocumentoEmiBean.setNuEmi(documentoEmiBean.getNuEmi());
          trxDocumentoEmiBean.setNuCorEmi(documentoEmiBean.getNuCorEmi());
          
          pnuEmi = documentoEmiBean.getNuEmi();
          vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi(), "NC");
          if (!vReturn.equals("OK")) {
            vReturn = "Error insertando Auditoria.";
            throw new validarDatoException(vReturn);
          } 
        } 
        for (ReferenciaEmiDocBean ref : lstReferencia) {
          ref.setCoUseCre(pcoUserMod);
          vReturn = this.emiDocumentoAdmDao.insReferenciaDocumentoEmi(pnuAnn, pnuEmi, ref);
          if ("NO_OK".equals(vReturn)) {
            throw new validarDatoException("Error Agregando Referencia");
          }
        } 
        for (DestinatarioDocumentoEmiBean dest : lstDestinatario) {
          dest.setCoUseCre(pcoUserMod);
          vReturn = this.emiDocumentoAdmDao.insDestinatarioDocumentoEmi(pnuAnn, pnuEmi, dest);
          if ("NO_OK".equals(vReturn)) {
            throw new validarDatoException("Error Agregando Destinatario");
          }
        } 
        for (EmpleadoVoBoBean emp : lstEmpVoBo) {
          vReturn = this.emiDocumentoAdmDao.insPersonalVoBo(pnuAnn, pnuEmi, emp.getCoDependencia(), emp.getCoEmpleado(), pcoUserMod);
          if (!"OK".equals(vReturn)) {
            throw new validarDatoException("Error Agregando Personal VB.");
          }
        } 
      } 
      
      if (documentoEmiBeanBD == null || (!documentoEmiBeanBD.getCoEmpEmi().equals(usuario.getCempCodemp()) && !documentoEmiBeanBD.getCoEmpRes().equals(usuario.getCempCodemp()))) {
        vReturn = this.docVoboDao.existeVistoBuenoDocAdm(pnuAnn, pnuEmi);
        if (vReturn.equals("0")) {
          vReturn = "OK";
        } else {
          throw new validarDatoException("Doc. con Visto Bueno u Observado.");
        } 
      } 
      
      if (lstDestinatario.size() >= 1) {
        
        vReturn = this.actualizaResumenService.updRemitosResumenDes(pnuAnn, pnuEmi);
        if ("NO_OK".equals(vReturn)) {
          throw new validarDatoException("Error Actualizando Remito resumen destinatario");
        }
      } 
      if (lstReferencia.size() >= 1)
      {
        vReturn = this.actualizaResumenService.updRemitosResumenRef(pnuAnn, pnuEmi);
        if ("NO_OK".equals(vReturn)) {
          throw new validarDatoException("Error Actualizando Remito resumen Referencia");

        
        }
      
      }

    
    }
    catch (validarDatoException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new validarDatoException("ERROR EN TRANSACCION");
    } 
    return vReturn;
  }
  
  public List<DestinoBean> getDestinosListCodDepTipoDes(String nu_ann, String nu_emi) {
    List<DestinoBean> list = null;
    try {
      list = this.emiDocumentoAdmDao.getListaDestinosCodDepTipoDes(nu_ann, nu_emi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }
  
  public List<ReferenciaRemitoBean> getReferenciaRemitoList(String nu_ann, String nu_emi) {
    List<ReferenciaRemitoBean> list = null;
    try {
      list = this.emiDocumentoAdmDao.getListaReferenciaRemitos(nu_ann, nu_emi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }
  
  public List<DestinoBean> getDestinosListCodPri(String nu_ann, String nu_emi) {
    List<DestinoBean> list = null;
    try {
      list = this.emiDocumentoAdmDao.getListaDestinosCodPri(nu_ann, nu_emi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
  public String delDocumentoEmiAdm(DocumentoEmiBean documentoEmiBean, Usuario usuario) throws Exception {
    String vReturn = "NO_OK";
    
    try {
      String pesDocEmi = documentoEmiBean.getEsDocEmi();
      if (pesDocEmi != null && pesDocEmi.equals("9")) {
        vReturn = this.emiDocumentoAdmDao.getNroCorrelativoDocumentoDel(documentoEmiBean);
        String snuCordoc = vReturn;
        if ("NO_OK".equals(vReturn)) {
          throw new validarDatoException("Error al obtener nro correlativo");
        }
        vReturn = this.emiDocumentoAdmDao.delAllReferenciaDocumentoEmi(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
        if ("NO_OK".equals(vReturn)) {
          throw new validarDatoException("Error al eliminar referencias");
        }
        vReturn = this.emiDocumentoAdmDao.delAllDestinatarioDocumentoEmi(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
        if ("NO_OK".equals(vReturn)) {
          throw new validarDatoException("Error al eliminar Destinatarios");
        }
        documentoEmiBean.setNuCorDoc(snuCordoc);
        documentoEmiBean.setNuDocEmi(null);
        documentoEmiBean.setNuAnnExp(null);
        documentoEmiBean.setNuSecExp(null);
        documentoEmiBean.setNuDetExp(null);
        vReturn = this.emiDocumentoAdmDao.delDocumentoEmiAdm(documentoEmiBean);
        if (!"OK".equals(vReturn)) {
          throw new validarDatoException("Error al actualizar documento");
        }
        vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi(), pesDocEmi);
        if (!vReturn.equals("OK")) {
          vReturn = "Error insertando Auditoria.";
          throw new validarDatoException(vReturn);
        } 
      } else {
        vReturn = "Documento NO puede ser eliminado!";
      } 
    } catch (validarDatoException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new validarDatoException("ERROR EN TRANSACCION ELIMINAR DOCUMENTO EMISION");
    } 
    return vReturn;
  }

  
  public String getNumeroDocSiguienteAdm(String pnuAnn, String pcoDepEmi, String pcoDoc) {
    String vReturn = "NO_OK";
    try {
      vReturn = this.emiDocumentoAdmDao.getNumeroDocSiguienteAdm(pnuAnn, pcoDepEmi, pcoDoc);
    } catch (Exception e) {
      vReturn = "NO_OK";
    } 
    
    return vReturn;
  }
  public boolean tienePermisoSalidaExterna(DocumentoDatoBean docDatoBean, String codUsu) throws Exception {
	 
	  if(emiDocumentoAdmDao.esDirectoroResponsableOficina(docDatoBean.getCoDepEmi(),docDatoBean.getCoEmpEmi())) {
		  return true;
	  }
	  if(emiDocumentoAdmDao.tieneCargoHabilitadoparaEnvioDocsExterno(docDatoBean.getCoEmpEmi())) {
		  return true;
	  }
	  PermisoUsuarioBean permiso= new PermisoUsuarioBean();
	  permiso.setCodAplica(Constantes.COD_SISTEMA_SGD);
	  permiso.setCodOpc(Constantes.COD_PERMISO_ENVIAR_EXTERN_DOCUMENTOS);
	  permiso.setCempCodEmp(docDatoBean.getCoEmpEmi());
	  if(permisoService.obtenerPermiso(permiso)){
		  return true;
	  }
  
	   return false;
  }
  public boolean esDocumentoSalidaExterna(DocumentoDatoBean docDatoBean) {
	 return  emiDocumentoAdmDao.esDocoumentoSalidaExterna(docDatoBean.getNuEmi(), docDatoBean.getNuAnn());
	   
 }
  
  public String getVerificaPdfDoc(String pnuAnn, String pnuEmi, String ptiCap) {
    String vReturn = "NO_OK";
    try {
      DocumentoObjBean docObjBean = this.emiDocumentoAdmDao.getPropiedadesArchivo(pnuAnn, pnuEmi, ptiCap);
      if (docObjBean != null) {
        if (docObjBean.getTipoDoc() != null && docObjBean.getTipoDoc().equals("PDF")) {
          vReturn = "OK";
        } else {
          vReturn = "Para la Firmar del documento tiene que estar en PDF ";
        } 
      } else {
        vReturn = "Error en verificar formato del documento";
      }
    
    } catch (Exception e) {
      vReturn = "Error en verificar formato del documento";
    } 
    
    return vReturn;
  }

  
  public String getFormatoDoc(String pnuAnn, String pnuEmi, String ptiCap) {
    String vReturn = "ERR";
    try {
      DocumentoObjBean docObjBean = this.emiDocumentoAdmDao.getPropiedadesArchivo(pnuAnn, pnuEmi, ptiCap);
      if (docObjBean != null && docObjBean.getTipoDoc() != null) {
        vReturn = docObjBean.getTipoDoc();
      } else {
        vReturn = "ERR";
      }
    
    } catch (Exception e) {
      vReturn = "ERR";
    } 
    
    return vReturn;
  }

  
  public HashMap getBuscaDependenciaEmite(String pcoDepen, String pdeDepEmite) {
    String vReturn = "1";
    HashMap<Object, Object> map = new HashMap<Object, Object>();
    
    try {
      List list = this.emiDocumentoAdmDao.getListDestinatarioEmi(pcoDepen, pdeDepEmite);
      if (list != null) {
        if (list.isEmpty()) {
          list = this.emiDocumentoAdmDao.getListDestinatarioEmi(pcoDepen, null);
        } else if (list.size() == 1) {
          vReturn = "0";
          map.put("objDestinatario", list.get(0));
        } 
      }
      map.put("listaDestinatario", list);
    } catch (Exception e) {
      vReturn = e.getMessage();
    } 
    map.put("vReturn", vReturn);
    return map;
  }

  
  public HashMap getDocumentosEnReferencia(BuscarDocumentoEmiBean buscarDocumentoEmiBean) {
    List<DocumentoEmiBean> list = null;
    DocumentoEmiBean documentoEmiBean = null;
    HashMap<Object, Object> mp = new HashMap<Object, Object>();
    try {
      documentoEmiBean = this.emiDocumentoAdmDao.existeDocumentoReferenciado(buscarDocumentoEmiBean);
      if (documentoEmiBean != null && documentoEmiBean.getMsjResult() != null && documentoEmiBean.getMsjResult().equals("OK")) {
        documentoEmiBean.setCoDepEmi(buscarDocumentoEmiBean.getsCoDependencia());
        documentoEmiBean.setCoEmpRes(buscarDocumentoEmiBean.getsCoEmpleado());
        list = this.emiDocumentoAdmDao.getDocumentosReferenciadoBusq(documentoEmiBean, buscarDocumentoEmiBean.getsTiAcceso());
        mp.put("emiDocumAdmList", list);
        mp.put("msjResult", "1");
      } else {
        mp.put("msjResult", "0");
      } 
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return mp;
  }


  
  public DependenciaBean cambiaDepEmi(String pcoDep) {
    DependenciaBean depEmi = null;
    
    try {
      depEmi = this.maestrosDao.getDatosDependencia(pcoDep);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      depEmi = null;
    } 
    
    return depEmi;
  }

  
  public String[] getNotificaciones(List<EmpleadoVoBoBean> lsEmpleadoVb, String reqFirmaAnexos) {
    String[] aMensaje = null;
    String sMensaje = null;
    if (lsEmpleadoVb != null) {
      for (EmpleadoVoBoBean empVobo : lsEmpleadoVb) {
        if (empVobo != null && empVobo.getInVobo() != null && (empVobo.getInVobo().equals("0") || empVobo.getInVobo().equals("2"))) {
          sMensaje = "Documento con visto bueno faltante!";
          break;
        } 
      } 
    }
    if (reqFirmaAnexos != null && reqFirmaAnexos.equals("1")) {
      if (sMensaje != null) {
        sMensaje = sMensaje + ";Documento con Anexos Pendientes de Firma!";
      } else {
        sMensaje = "Documento con Anexos Pendientes de Firma!";
      } 
    }
    if (sMensaje != null) {
      aMensaje = sMensaje.split(";");
    }
    return aMensaje;
  }
  
  private String getInNumeraDocAdm(String tipoDoc) {
    String vReturn = "NO_OK";
    
    try {
      vReturn = this.emiDocumentoAdmDao.getInNumeraDocAdm(tipoDoc);
    } catch (Exception e) {
      vReturn = "NO_OK";
    } 
    return vReturn;
  }

  
  public String getInFirmaDocAdm(String tipoDoc) {
    String vReturn = "NO_OK";
    
    try {
      vReturn = this.emiDocumentoAdmDao.getInFirmaDocAdm(tipoDoc);
    } catch (Exception e) {
      vReturn = "NO_OK";
    } 
    return vReturn;
  }

  
  public String getLstPersVoBoGrupo(String pcoGrupo) {
    StringBuilder retval = new StringBuilder();
    
    List<DestinatarioDocumentoEmiBean> list = null;
    
    try {
      list = this.emiDocumentoAdmDao.getLstPersVoBoGrupo(pcoGrupo);
      if (list != null) {
        Writer strWriter = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        
        String coRespuesta = "1";
        retval.append("{\"coRespuesta\":\"");
        retval.append(coRespuesta);
        retval.append("\",\"lsDestInstitu\":");
        strWriter = new StringWriter();
        mapper = new ObjectMapper();
        mapper.writeValue(strWriter, list);
        retval.append(strWriter.toString());
        retval.append("}");
      } 
    } catch (Exception e) {
      String coRespuesta = "0";
      retval.append("{\"coRespuesta\":\"");
      retval.append(coRespuesta);
      retval.append("\",\"deRespuesta\":\"");
      retval.append(e.getMessage());
      retval.append("\"}");
    } 
    
    return retval.toString();
  }

  
  public String updArchivarDocumento(DocumentoEmiBean documentoEmiBean, Usuario usuario) {
    String vReturn = "ERR";
    try {
      vReturn = this.emiDocumentoAdmDao.updArchivarDocumento(documentoEmiBean, usuario);
    } catch (Exception e) {
      vReturn = "ERR";
    } 
    
    return vReturn;
  }

  
  public String changeToEnvioMesaVirtual(DatosInterBean datosInter, Usuario usuario) {
    String vReturn = "NO_OK";



    
    try {
      vReturn = this.emiDocumentoAdmDao.insMesaVitual(datosInter);
      if (vReturn.equals("OK")) {
        vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, datosInter.getNuAnn(), datosInter.getNuEmi(), "0");
        if (!vReturn.equals("OK")) {
          vReturn = "Error insertando Auditoria.";
        }
      } else {
        vReturn = "Error al Enviar el documento a la mensajería"+ vReturn;
      }
    
    }
    catch (Exception e) {
      vReturn = e.getMessage();
    } 
    return vReturn;
  }

  
  public String getLstDestExternoAgregaGrupo(String pcoGrupo, String ptiDes) {
    StringBuilder retval = new StringBuilder();

    
    List<DestinatarioDocumentoEmiBean> list = null;
    
    try {
      list = this.emiDocumentoAdmDao.getLstDestExternoGrupo(pcoGrupo, ptiDes);
      if (list != null) {
        Writer strWriter = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        
        String coRespuesta = "1";
        retval.append("{\"coRespuesta\":\"");
        retval.append(coRespuesta);
        retval.append("\",\"lsDestInstitu\":");
        strWriter = new StringWriter();
        mapper = new ObjectMapper();
        mapper.writeValue(strWriter, list);
        retval.append(strWriter.toString());
        retval.append("}");
      } 
    } catch (Exception e) {
      String coRespuesta = "0";
      retval.append("{\"coRespuesta\":\"");
      retval.append(coRespuesta);
      retval.append("\",\"deRespuesta\":\"");
      retval.append(e.getMessage());
      retval.append("\"}");
    } 
    
    return retval.toString();
  }
}
