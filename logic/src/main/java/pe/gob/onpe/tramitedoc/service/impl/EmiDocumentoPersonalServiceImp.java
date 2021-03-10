package pe.gob.onpe.tramitedoc.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.libreria.exception.validarDatoException;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.DestinatarioDocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.DestinoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoObjBean;
import pe.gob.onpe.tramitedoc.bean.EmpleadoVoBoBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaEmiDocBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaRemitoBean;
import pe.gob.onpe.tramitedoc.bean.TblRemitosBean;
import pe.gob.onpe.tramitedoc.bean.TrxDocumentoEmiBean;
import pe.gob.onpe.tramitedoc.dao.CommonQueryDao;
import pe.gob.onpe.tramitedoc.dao.DocumentoVoBoDao;
import pe.gob.onpe.tramitedoc.dao.EmiDocumentoAdmDao;
import pe.gob.onpe.tramitedoc.dao.EmiDocumentoPersonalDao;
import pe.gob.onpe.tramitedoc.service.ActualizaResumenService;
import pe.gob.onpe.tramitedoc.service.AuditoriaMovimientoDocService;
import pe.gob.onpe.tramitedoc.service.EmiDocumentoPersonalService;
import pe.gob.onpe.tramitedoc.service.ValidarFirmaService;
import pe.gob.onpe.tramitedoc.util.ArchivoTemporal;

















@Service("emiDocumentoPersonalService")
public class EmiDocumentoPersonalServiceImp
  implements EmiDocumentoPersonalService
{
  @Autowired
  private EmiDocumentoPersonalDao emiDocumentoPersonalDao;
  @Autowired
  private ActualizaResumenService actualizaResumenService;
  @Autowired
  AuditoriaMovimientoDocService audiMovDoc;
  @Autowired
  private ValidarFirmaService validarFirmaService;
  @Autowired
  private CommonQueryDao commonQueryDao;
  @Autowired
  private EmiDocumentoAdmDao emiDocumentoAdmDao;
  @Autowired
  private DocumentoVoBoDao docVoboDao;
  
  public List<DocumentoEmiBean> getDocumentosEmiAdm(BuscarDocumentoEmiBean buscarDocumentoEmiBean) {
    List<DocumentoEmiBean> list = null;
    try {
      list = this.emiDocumentoPersonalDao.getDocumentosEmiAdm(buscarDocumentoEmiBean);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }


  
  public DocumentoEmiBean getDocumentoEmiAdm(String pnuAnn, String pnuEmi) {
    DocumentoEmiBean documentoEmiBean = null;
    try {
      documentoEmiBean = this.emiDocumentoPersonalDao.getDocumentoEmiAdm(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return documentoEmiBean;
  }

  
  public List<DestinatarioDocumentoEmiBean> getLstDestintariotlbEmi(String pnuAnn, String pnuEmi) {
    List<DestinatarioDocumentoEmiBean> list = null;
    try {
      list = this.emiDocumentoPersonalDao.getLstDestintariotlbEmi(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public List<ReferenciaBean> getLstDocumReferenciatblEmi(String pnuAnn, String pnuEmi) {
    List<ReferenciaBean> list = null;
    try {
      list = this.emiDocumentoPersonalDao.getLstDocumReferenciatblEmi(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }


  
  public DocumentoEmiBean getDocumentoEmiAdmNew(String codDependencia, String codEmpleado, String codLocal) {
    DocumentoEmiBean documentoEmiBean = null;
    try {
      documentoEmiBean = this.emiDocumentoPersonalDao.getDocumentoEmiAdmNew(codDependencia, codEmpleado, codLocal);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return documentoEmiBean;
  }

  
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
  public String grabaDocumentoEmiAdm(TrxDocumentoEmiBean trxDocumentoEmiBean, Usuario usuario) throws Exception {
    String vReturn = "NO_OK";
    try {
      String pnuAnn = trxDocumentoEmiBean.getNuAnn();
      String pnuEmi = trxDocumentoEmiBean.getNuEmi();
      String pcoUserMod = trxDocumentoEmiBean.getCoUserMod();
      DocumentoEmiBean documentoEmiBean = trxDocumentoEmiBean.getDocumentoEmiBean();
      
      ArrayList<ReferenciaEmiDocBean> lstReferencia = trxDocumentoEmiBean.getLstReferencia();
      ArrayList<DestinatarioDocumentoEmiBean> lstDestinatario = trxDocumentoEmiBean.getLstDestinatario();
      ArrayList<EmpleadoVoBoBean> lstEmpVoBo = trxDocumentoEmiBean.getLstEmpVoBo();
      DocumentoEmiBean documentoEmiBeanBD = null;
      if (pnuAnn != null && pnuEmi != null && pnuAnn.trim().length() > 0 && pnuEmi.trim().length() > 0) {
        documentoEmiBeanBD = this.emiDocumentoAdmDao.getEstadoDocumento(pnuAnn, pnuEmi);
        if (documentoEmiBean != null) {
          documentoEmiBean.setCoUseMod(pcoUserMod);
          documentoEmiBean.setNuAnn(pnuAnn);
          documentoEmiBean.setNuEmi(pnuEmi);
          if (documentoEmiBean.getNuDocEmi() == null || documentoEmiBean.getNuDocEmi().trim().length() <= 0) {
            throw new validarDatoException("Especifique Nro. de Documento.");
          }
          if(!documentoEmiBean.getCoTipDocAdm().equals(documentoEmiBeanBD.getCoTipDocAdm()) && documentoEmiBean.getNuDocEmi()!=null) {
        	  throw new validarDatoException("No se permite que el tipo de documento sea modificado, luego de haber registrado.");  
          }
          vReturn = this.emiDocumentoPersonalDao.updDocumentoEmiAdmBean(documentoEmiBean);
          if (!"OK".equals(vReturn)) {
            throw new validarDatoException("Error al Actualizar Documento.");
          }
        } 
        for (ReferenciaEmiDocBean ref : lstReferencia) {
          String sAccionBD = ref.getAccionBD();
          if ("INS".equals(sAccionBD)) {
            ref.setCoUseCre(pcoUserMod);
            vReturn = this.emiDocumentoPersonalDao.insReferenciaDocumentoEmi(pnuAnn, pnuEmi, ref);
            if (!"OK".equals(vReturn))
              throw new validarDatoException("Error Agregando Referencia.");  continue;
          } 
          if ("UPD".equals(sAccionBD)) {
            vReturn = this.emiDocumentoPersonalDao.updReferenciaDocumentoEmi(pnuAnn, pnuEmi, ref);
            if (!"OK".equals(vReturn))
              throw new validarDatoException("Error Actualizando Referencia.");  continue;
          } 
          if ("DEL".equals(sAccionBD)) {
            vReturn = this.emiDocumentoPersonalDao.delReferenciaDocumentoEmi(pnuAnn, pnuEmi, ref);
            if (!"OK".equals(vReturn)) {
              throw new validarDatoException("Error Borrando Referencia.");
            }
          } 
        } 
        for (DestinatarioDocumentoEmiBean dest : lstDestinatario) {
          String sAccionBD = dest.getAccionBD();
          dest.setCoUseCre(pcoUserMod);
          if ("INS".equals(sAccionBD)) {
            vReturn = this.emiDocumentoPersonalDao.insDestinatarioDocumentoEmi(pnuAnn, pnuEmi, dest);
            if (!"OK".equals(vReturn))
              throw new validarDatoException("Error Agregando Destinatario.");  continue;
          } 
          if ("UPD".equals(sAccionBD)) {
            vReturn = this.emiDocumentoPersonalDao.updDestinatarioDocumentoEmi(pnuAnn, pnuEmi, dest);
            if (!"OK".equals(vReturn))
              throw new validarDatoException("Error Actualizando Destinatario.");  continue;
          } 
          if ("DEL".equals(sAccionBD)) {
            vReturn = this.emiDocumentoPersonalDao.delDestinatarioDocumentoEmi(pnuAnn, pnuEmi, dest);
            if (!"OK".equals(vReturn)) {
              throw new validarDatoException("Error Borrando Destinatario.");
            }
          } 
        } 
        for (EmpleadoVoBoBean emp : lstEmpVoBo) {
          String sAccionBD = emp.getAccionBD();
          if ("DEL".equals(sAccionBD)) {
            vReturn = this.emiDocumentoPersonalDao.delPersonalVoBo(pnuAnn, pnuEmi, emp.getCoDependencia(), emp.getCoEmpleado());
            if (!"OK".equals(vReturn))
              throw new validarDatoException("Error Borrando Personal VB.");  continue;
          } 
          if ("INS".equals(sAccionBD)) {
            vReturn = this.emiDocumentoPersonalDao.insPersonalVoBo(pnuAnn, pnuEmi, emp.getCoDependencia(), emp.getCoEmpleado(), pcoUserMod);
            if (!"OK".equals(vReturn)) {
              throw new validarDatoException("Error Agregando Personal VB.");
            }
          } 
        } 
      } else {
        documentoEmiBean.setCoUseMod(pcoUserMod);
        
        documentoEmiBean.setNuAnnExp(trxDocumentoEmiBean.getNuAnnExp());
        documentoEmiBean.setNuSecExp(trxDocumentoEmiBean.getNuSecExp());
        
        vReturn = this.emiDocumentoPersonalDao.insDocumentoEmiBean(documentoEmiBean);
        if (!"OK".equals(vReturn)) {
          throw new validarDatoException("Error al Grabar Documento - NDe Documento Existente.");
        }
        pnuEmi = documentoEmiBean.getNuEmi();
        vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, documentoEmiBean.getNuAnn(), pnuEmi, "NC");
        if (!vReturn.equals("OK")) {
          vReturn = "Error insertando Auditoria.";
          throw new validarDatoException(vReturn);
        } 
        trxDocumentoEmiBean.setNuEmi(pnuEmi);
        trxDocumentoEmiBean.setNuCorEmi(documentoEmiBean.getNuCorEmi());
        trxDocumentoEmiBean.setNuDoc(documentoEmiBean.getNuDoc());
        trxDocumentoEmiBean.setNuDocEmi(documentoEmiBean.getNuDocEmi());
        for (ReferenciaEmiDocBean ref : lstReferencia) {
          ref.setCoUseCre(pcoUserMod);
          vReturn = this.emiDocumentoPersonalDao.insReferenciaDocumentoEmi(pnuAnn, pnuEmi, ref);
          if (!"OK".equals(vReturn)) {
            throw new validarDatoException("Error Agregando Referencia.");
          }
        } 
        for (DestinatarioDocumentoEmiBean dest : lstDestinatario) {
          dest.setCoUseCre(pcoUserMod);
          vReturn = this.emiDocumentoPersonalDao.insDestinatarioDocumentoEmi(pnuAnn, pnuEmi, dest);
          if (!"OK".equals(vReturn)) {
            throw new validarDatoException("Error Agregando Destinatario.");
          }
        } 
        for (EmpleadoVoBoBean emp : lstEmpVoBo) {
          vReturn = this.emiDocumentoPersonalDao.insPersonalVoBo(pnuAnn, pnuEmi, emp.getCoDependencia(), emp.getCoEmpleado(), pcoUserMod);
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
        if (!"OK".equals(vReturn)) {
          throw new validarDatoException("Error Actualizando Remito resumen destinatario");
        }
      } 
      if (lstReferencia.size() >= 1) {
        vReturn = this.actualizaResumenService.updRemitosResumenRef(pnuAnn, pnuEmi);
        if (!"OK".equals(vReturn)) {
          throw new validarDatoException("Error Actualizando Remito resumen Referencia");
        }
      } 
    } catch (validarDatoException e) {
      throw e;
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new validarDatoException("ERROR EN TRANSACCION");
    } 
    return vReturn;
  }
  
  public List<DestinoBean> getDestinosListCodDepTipoDes(String nu_ann, String nu_emi) {
    List<DestinoBean> list = null;
    try {
      list = this.emiDocumentoPersonalDao.getListaDestinosCodDepTipoDes(nu_ann, nu_emi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }
  
  public List<ReferenciaRemitoBean> getReferenciaRemitoList(String nu_ann, String nu_emi) {
    List<ReferenciaRemitoBean> list = null;
    try {
      list = this.emiDocumentoPersonalDao.getListaReferenciaRemitos(nu_ann, nu_emi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }
  
  public List<DestinoBean> getDestinosListCodPri(String nu_ann, String nu_emi) {
    List<DestinoBean> list = null;
    try {
      list = this.emiDocumentoPersonalDao.getListaDestinosCodPri(nu_ann, nu_emi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public String verificaNroDocumentoEmiDuplicado(DocumentoEmiBean documentoEmiBean) {
    String vReturn = "NO_OK";
    
    try {
      DocumentoEmiBean documentoEmiBeanBD = this.emiDocumentoPersonalDao.getEstadoDocumento(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
      String pEsDocEmi = documentoEmiBeanBD.getEsDocEmi();
      if (pEsDocEmi != null && (pEsDocEmi.equals("5") || pEsDocEmi.equals("7"))) {
        vReturn = this.emiDocumentoPersonalDao.getCanDocumentoEmiDuplicados(documentoEmiBean);
        if (vReturn.equals("0")) {
          vReturn = "OK";
        } else {
          vReturn = "Nde documento duplicado.";
        } 
      } else {
        vReturn = "Operacino permitida.";
      } 
    } catch (Exception e) {
      vReturn = e.getMessage();
    } 
    return vReturn;
  }

  
  public String changeToProyecto(DocumentoEmiBean documentoEmiBean, Usuario usuario) {
    String vReturn = "NO_OK";
    String pcoUseMod = documentoEmiBean.getCoUseMod();
    try {
      documentoEmiBean = this.emiDocumentoPersonalDao.getEstadoDocumento(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
      if (documentoEmiBean != null && (documentoEmiBean.getEsDocEmi().equals("7") || documentoEmiBean.getEsDocEmi().equals("0"))) {
        String pesDocEmi = documentoEmiBean.getEsDocEmi();
        vReturn = this.emiDocumentoPersonalDao.verificarDocumentoLeido(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
        if (vReturn != null && vReturn.equals("0")) {
          documentoEmiBean.setEsDocEmi("5");
          documentoEmiBean.setCoUseMod(pcoUseMod);
          vReturn = this.emiDocumentoPersonalDao.updEstadoDocumentoEmiAdm(documentoEmiBean);
          if (vReturn.equals("OK")) {
            vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi(), pesDocEmi);
            if (!vReturn.equals("OK")) {
              vReturn = "Error insertando Auditoria.";
            }
          } else {
            vReturn = "Error al Cambiar a Proyecto el Documento.";
          } 
        } else {
          vReturn = "Documento ya fue leido por los Destinatarios";
        } 
      } else {
        vReturn = "Documento no puede cambiar de Estado.";
      } 
    } catch (Exception e) {
      vReturn = e.getMessage();
    } 
    return vReturn;
  }

  
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
  public String changeToEmitido(DocumentoEmiBean documentoEmiBean, String prutaDoc, Usuario usuario) throws Exception {
    String vReturn = "NO_OK";
    String pesDocEmi = documentoEmiBean.getEsDocEmi();
    if (pesDocEmi != null && !pesDocEmi.equals("")) {
      
      try {
        DocumentoObjBean docObjBean = new DocumentoObjBean();
        
        docObjBean.setNuAnn(documentoEmiBean.getNuAnn());
        docObjBean.setNuEmi(documentoEmiBean.getNuEmi());
        docObjBean.setTiCap(documentoEmiBean.getTiCap());
        docObjBean.setNombreArchivo(prutaDoc);
        docObjBean.setNumeroSecuencia(documentoEmiBean.getNuSecuenciaFirma());
        docObjBean.setCoUseMod(documentoEmiBean.getCoUseMod());
        
        String esOblFirma = this.commonQueryDao.getInFirmaDoc(documentoEmiBean.getCoDepEmi(), documentoEmiBean.getCoTipDocAdm());
        vReturn = this.validarFirmaService.getValidaCertificadoEmp(docObjBean.getNumeroSecuencia(), usuario.getNuDni());
        if (vReturn.equals("NO_OK"))
          throw new validarDatoException("Firma Personal no pertenece a Usuario"); 
        if (vReturn.equals("") && esOblFirma.equals("F")) {
          throw new validarDatoException("Necesita Firmar el Documento.");
        }
        
        vReturn = cargaDocEmi(docObjBean);
        if (!vReturn.equals("OK")) {
          throw new validarDatoException("Error al Cargar Documento");
        }
        documentoEmiBean.setEsDocEmi("0");
        vReturn = this.emiDocumentoPersonalDao.updEstadoDocumentoEmitido(documentoEmiBean);
        if (!vReturn.equals("OK")) {
          throw new validarDatoException(vReturn);
        }


        
        List<ReferenciaRemitoBean> lsReferencia = this.emiDocumentoPersonalDao.getListaReferenciaRemitos(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
        ReferenciaRemitoBean oReferenciaRemitoBean = new ReferenciaRemitoBean();
        String g = "";
        for (int i = 0; i < lsReferencia.size(); i++) {

          
          oReferenciaRemitoBean = lsReferencia.get(i);
          g = this.emiDocumentoPersonalDao.updEstadoDocumentoReferenciasEmitido(docObjBean.getCoUseMod(), oReferenciaRemitoBean.getNu_emi(), oReferenciaRemitoBean.getNu_ann(), oReferenciaRemitoBean.getNu_des_ref());
        } 



        
        TblRemitosBean tblRemitosBean = this.emiDocumentoAdmDao.getDatosDocumento(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
        if (!tblRemitosBean.getMsgResult().equals("OK")) {
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
      } catch (validarDatoException e) {
        throw e;
      }
      catch (Exception e) {
        e.printStackTrace();
        throw new validarDatoException("ERROR EN TRANSACCION DE EMISION");
      } 
    } else {
      vReturn = "Documento no esta Listo.";
    } 
    return vReturn;
  }




  
  public String updEstadoDocumentoReferenciasEmitido(String coUseMod, String nuEmi, String nuAnn, String nuDes) { return this.emiDocumentoPersonalDao.updEstadoDocumentoReferenciasEmitido(coUseMod, nuEmi, nuAnn, nuDes); }


  
  public String cargaDocEmi(DocumentoObjBean docObjBean) {
    String vReturn = "NO_OK";
    
    try {
      docObjBean.setTipoDoc(docObjBean.getNombreArchivo().substring(docObjBean.getNombreArchivo().lastIndexOf('.') + 1));
      docObjBean.setNombreArchivo(docObjBean.getNombreArchivo().substring(docObjBean.getNombreArchivo().lastIndexOf('|') + 1));
    } catch (Exception exception) {}


    
    try {
      byte[] archivoByte = ArchivoTemporal.leerArchivo(docObjBean.getNumeroSecuencia());
      docObjBean.setDocumento(archivoByte);
      docObjBean.setNuTamano((int)Math.round(archivoByte.length / 1024.0D));
      vReturn = this.emiDocumentoPersonalDao.updDocumentoObj(docObjBean);
    } catch (Exception e) {
      vReturn = e.getMessage().substring(0, 20);
    } 
    return vReturn;
  }

  
  public String anularDocumento(DocumentoEmiBean documentoEmiBean, Usuario usuario) {
    String vReturn = "NO_OK";
    String pcoUsedMod = documentoEmiBean.getCoUseMod();
    try {
      documentoEmiBean = this.emiDocumentoPersonalDao.getEstadoDocumento(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
      String pesDocEmi = documentoEmiBean.getEsDocEmi();
      if (pesDocEmi != null && (pesDocEmi.equals("5") || pesDocEmi.equals("7"))) {
        documentoEmiBean.setCoUseMod(pcoUsedMod);
        vReturn = this.emiDocumentoPersonalDao.verificarDocumentoLeido(documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi());
        if (vReturn != null && vReturn.equals("0")) {
          documentoEmiBean.setEsDocEmi("9");
          vReturn = this.emiDocumentoPersonalDao.anularDocumento(documentoEmiBean);
          if (vReturn.equals("OK")) {
            vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi(), pesDocEmi);
            if (!vReturn.equals("OK")) {
              vReturn = "Error insertando Auditoria.";
            }
          } else {
            vReturn = "Error anulando documento.";
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

  
  public String getNumeroDocSiguientePersonal(String pnuAnn, String pcoEmp, String pcoDoc) {
    String vReturn = "NO_OK";
    try {
      vReturn = this.emiDocumentoPersonalDao.getNumeroDocSiguientePersonal(pnuAnn, pcoEmp, pcoDoc);
    } catch (Exception e) {
      vReturn = "NO_OK";
    } 
    
    return vReturn;
  }
}
