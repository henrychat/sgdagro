package pe.gob.onpe.tramitedoc.service.impl;

import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.libreria.exception.validarDatoException;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoRecepBean;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.ExpedienteBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaBean;
import pe.gob.onpe.tramitedoc.dao.AnexoDocumentoDao;
import pe.gob.onpe.tramitedoc.dao.EmiDocumentoAdmDao;
import pe.gob.onpe.tramitedoc.dao.RecepDocumentoAdmDao;
import pe.gob.onpe.tramitedoc.service.AuditoriaMovimientoDocService;
import pe.gob.onpe.tramitedoc.service.RecepDocumentoAdmService;
import pe.gob.onpe.tramitedoc.util.FiltroPaginate;
import pe.gob.onpe.tramitedoc.util.Paginacion;
import pe.gob.onpe.tramitedoc.util.ProcessResult;

@Service("recepDocumentoAdmService")
public class RecepDocumentoAdmServiceImp implements RecepDocumentoAdmService {

    @Autowired
    private RecepDocumentoAdmDao recepDocumentoAdmDao;
    @Autowired
    private AnexoDocumentoDao anexoDocumentoDao;
    @Autowired
    AuditoriaMovimientoDocService audiMovDoc;
    @Autowired
    private EmiDocumentoAdmDao emiDocumentoAdmDao;

    public List<DocumentoBean> getDocumentosRecepAdm(BuscarDocumentoRecepBean buscarDocumentoRecep, Paginacion paginacion) {
        List<DocumentoBean> list = null;
        estadoRecepcionDocumento(buscarDocumentoRecep);
        try {
            list = this.recepDocumentoAdmDao.getDocumentosRecepAdm(buscarDocumentoRecep, paginacion);
            paginacion.setNumeroTotalRegistros(this.recepDocumentoAdmDao.getRowDocumentosRecepAdm(buscarDocumentoRecep));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }

    public List<DocumentoBean> getDocumentosBuscaRecepAdm(BuscarDocumentoRecepBean buscarDocumentoRecep) {
        List<DocumentoBean> list = null;

        try {
            list = this.recepDocumentoAdmDao.getDocumentosBuscaRecepAdm(buscarDocumentoRecep);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }

    public ProcessResult<List<DocumentoBean>> getDocumentosBuscaRecepAdmLst(BuscarDocumentoRecepBean buscarDocumentoRecep, FiltroPaginate paginate) {
        return this.recepDocumentoAdmDao.getDocumentosBuscaRecepAdmList(buscarDocumentoRecep, paginate);
    }

    public List<DependenciaBean> getDocumDepenProveido(String codDepen) {
        List<DependenciaBean> list = null;

        try {
            list = this.recepDocumentoAdmDao.getDocumDepenProveido(codDepen);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }

    public BuscarDocumentoRecepBean estadoRecepcionDocumento(BuscarDocumentoRecepBean buscarDocumentoRecepBean, String accion) {
        estadoRecepcionDocumento(buscarDocumentoRecepBean);
        return buscarDocumentoRecepBean;
    }

    private void estadoRecepcionDocumento(BuscarDocumentoRecepBean buscarDocumentoRecepBean) {
        String sEstado = buscarDocumentoRecepBean.getsEstadoDoc();
        if (sEstado.equals("04")) {
            buscarDocumentoRecepBean.setsEstadoDoc("0");
            buscarDocumentoRecepBean.setsPrioridadDoc("2");
        } else if (sEstado.equals("01")) {
            buscarDocumentoRecepBean.setsEstadoDoc("0");
        } else if (sEstado.equals("07")) {
            buscarDocumentoRecepBean.setsEstadoDoc("0");
            buscarDocumentoRecepBean.setsPrioridadDoc("3");
        }
    }

    public DocumentoBean getDocumentoRecepAdm(String pnuAnn, String pnuEmi, String pnuDes) {
        DocumentoBean documentoBean = null;
        try {
            documentoBean = this.recepDocumentoAdmDao.getDocumentoRecepAdm(pnuAnn, pnuEmi, pnuDes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return documentoBean;
    }

    public ExpedienteBean getExpDocumentoRecepAdm(String pnuAnnExp, String pnuSecExp) {
        ExpedienteBean expedienteBean = null;
        try {
            expedienteBean = this.recepDocumentoAdmDao.getExpDocumentoRecepAdm(pnuAnnExp, pnuSecExp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return expedienteBean;
    }

    public List<ReferenciaBean> getDocumentosRefRecepAdm(String pnuAnn, String pnuEmi) {
        List<ReferenciaBean> list = null;
        try {
            list = this.recepDocumentoAdmDao.getDocumentosRefRecepAdm(pnuAnn, pnuEmi);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public String updDocumentoRecepAdm(DocumentoBean documentoBean, String accion, String sEsDocAnu, Usuario usuario) throws Exception {
        String vReturn = "NO_OK";

        try {
            String esDocEmi = this.recepDocumentoAdmDao.getEstadoDocAdmBasico(documentoBean.getNuAnn(), documentoBean.getNuEmi());

            if (esDocEmi != null && esDocEmi.trim().length() > 0 && !esDocEmi.equals("5") && !esDocEmi.equals("7") && !esDocEmi.equals("9")) {

                DocumentoBean documentoDestinoBean = this.recepDocumentoAdmDao.getEstadoDocumento(documentoBean.getNuAnn(), documentoBean.getNuEmi(), documentoBean.getNuDes());

                accion = validarEstadoDocumentoRecepcion(documentoBean, accion);
                if (accion.equals("2")) {
                    vReturn = this.recepDocumentoAdmDao.validarAnulacionDocRecepcion(documentoBean.getNuAnn(), documentoBean.getNuEmi(), documentoBean.getNuDes());
                    if ("NO_OK".equals(vReturn)) {
                        throw new validarDatoException("No se puede Anular Recepcion, Documento ya fue Referenciado!");
                    }
                }

                String sTiEmi = documentoBean.getTiEmi();
                if (accion.equals("1")) {
                    documentoBean.setEsDocRec("1");

                    if (sTiEmi != null && !sTiEmi.equals("") && !sTiEmi.equals("01") && !sTiEmi.equals("05")) {
                        vReturn = this.recepDocumentoAdmDao.actualizarGuiaMesaPartes(documentoBean);
                        if ("NO_OK".equals(vReturn)) {
                            throw new validarDatoException("Error al Actualizar Guia Mesa Partes!");
                        }
                    }

                } else if (accion.equals("2")) {
                    documentoBean.setEsDocRec("0");
                    documentoBean.setCoEmpRec("");
                    documentoBean.setFeRecDoc("");
                    documentoBean.setFeAteDoc("");
                    documentoBean.setFeArcDoc("");
                    documentoBean.setNuCorDes("");
                    documentoBean.setCoEtiquetaRec("0");
                    documentoBean.setTiFisicoRec("0");

                    String vReturn2 = "";
                    vReturn2 = this.anexoDocumentoDao.delArchivoAnexoDest(documentoBean.getNuAnn(), documentoBean.getNuEmi(), documentoBean.getNuDes());

                    if (sTiEmi != null && !sTiEmi.equals("") && !sTiEmi.equals("01") && !sTiEmi.equals("05")) {

                        vReturn = this.recepDocumentoAdmDao.actualizarGuiaMesaPartes(documentoBean);
                        if ("NO_OK".equals(vReturn)) {
                            throw new validarDatoException("Error al Actualizar Guia Mesa Partes!");
                        }
                    }
                }

                String sEsDocRec = documentoBean.getEsDocRec();
                String sNuCorDes = documentoBean.getNuCorDes();
                if ((sEsDocRec.equals("1") || sEsDocRec.equals("2") || sEsDocRec.equals("3")) && sNuCorDes.equals("")) {
                    documentoBean.setNuCorDes(this.recepDocumentoAdmDao.getNumCorrelativoDestino(documentoBean.getNuAnn(), documentoBean.getCoDepDes()));
                }

                if (!accion.equals("2")) {

                    vReturn = this.recepDocumentoAdmDao.actualizarEstado(documentoBean);
                    if ("NO_OK".equals(vReturn)) {
                        throw new validarDatoException("Error al Actualizando Estado!");
                    }
                }

                vReturn = this.recepDocumentoAdmDao.updDocumentoBean(documentoBean, accion);
                if ("NO_OK".equals(vReturn)) {
                    throw new validarDatoException("Error al Actualizar Documento");
                }
                vReturn = this.recepDocumentoAdmDao.updEtiquetaTipoRecepDocumento(documentoBean);
                if ("NO_OK".equals(vReturn)) {
                    throw new validarDatoException("Error al Actualizar La etiqueta y tipo recepcion del documento");
                }
                documentoBean.setDeEsDocDes(this.recepDocumentoAdmDao.getDesEstadoDocRecepcion(documentoBean.getEsDocRec()));
                vReturn = this.audiMovDoc.audiEstadoDocumentoDestino(usuario, documentoDestinoBean.getNuAnn(), documentoDestinoBean.getNuEmi(), documentoDestinoBean.getNuDes(), documentoDestinoBean.getEsDocRec());
                if (!vReturn.equals("OK")) {
                    vReturn = "Error insertando Auditoria.";
                    throw new validarDatoException(vReturn);
                }
            } else {
                vReturn = "El Documento a cambiado de estado.";
            }
        } catch (validarDatoException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new validarDatoException("ERROR EN TRANSACCION");
        }
        return vReturn;
    }

    private String validarEstadoDocumentoRecepcion(DocumentoBean documentoBean, String accion) {
        if ((accion.equals("1") || accion.equals("0"))
                && documentoBean != null) {
            String sEsDocRec = documentoBean.getEsDocRec();
            String sFeAteDoc = documentoBean.getFeAteDoc();
            String sFeArcDoc = documentoBean.getFeArcDoc();
            if (sFeAteDoc != null && !sFeAteDoc.equals("")) {
                if (sEsDocRec != null && !sEsDocRec.equals("") && !sEsDocRec.equals("3")) {

                    documentoBean.setEsDocRec("2");

                    accion = "0";
                }
            }
            if (sFeArcDoc != null && !sFeArcDoc.equals("")) {

                documentoBean.setEsDocRec("3");

                accion = "0";
            }
        }

        return accion;
    }

    public HashMap getDocumentosEnReferencia(BuscarDocumentoRecepBean buscarDocumentoRecepBean) {
        List<DocumentoBean> list = null;
        DocumentoBean documentoBean = null;
        HashMap<Object, Object> mp = new HashMap<Object, Object>();
        try {
            documentoBean = this.recepDocumentoAdmDao.existeDocumentoReferenciado(buscarDocumentoRecepBean);
            if (documentoBean != null && documentoBean.getMsjResult() != null && documentoBean.getMsjResult().equals("OK")) {
                documentoBean.setCoDepDes(buscarDocumentoRecepBean.getsCoDependencia());
                documentoBean.setCoEmpDes(buscarDocumentoRecepBean.getsCoEmpleado());
                list = this.recepDocumentoAdmDao.getDocumentosReferenciadoBusq(documentoBean, buscarDocumentoRecepBean.getsTiAcceso());
                mp.put("recepDocumAdmList", list);
                mp.put("msjResult", "1");
            } else {
                mp.put("msjResult", "0");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return mp;
    }

    public String getVerificaAtendido(String pnuAnn, String pnuEmi, String pnuDes) {
        String vResult = "0";
        try {
            vResult = this.recepDocumentoAdmDao.getVerificaAtendido(pnuAnn, pnuEmi, pnuDes);
        } catch (Exception ex) {
            vResult = "0";
            ex.printStackTrace();
        }

        return vResult;
    }

    public List<ReferenciaBean> getConsultaAtendido(String pnuAnn, String pnuEmi, String pnuDes) {
        List<ReferenciaBean> list = null;
        try {
            list = this.recepDocumentoAdmDao.getConsultaAtendido(pnuAnn, pnuEmi, pnuDes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public String updDocumentoRecepAdm(DocumentoBean documentoBean, String accion, Usuario usuario) throws Exception {
        String vReturn = "NO_OK";

        try {
            DocumentoEmiBean documentoEmiBean = this.emiDocumentoAdmDao.getEstadoDocumento(documentoBean.getNuAnn(), documentoBean.getNuEmi());

            if (documentoEmiBean != null && documentoEmiBean.getEsDocEmi() != null && documentoEmiBean.getEsDocEmi().trim().length() > 0
                    && !documentoEmiBean.getEsDocEmi().equals("5") && !documentoEmiBean.getEsDocEmi().equals("7") && !documentoEmiBean.getEsDocEmi().equals("9")) {

                DocumentoBean documentoDestinoBean = this.recepDocumentoAdmDao.getEstadoDocumento(documentoBean.getNuAnn(), documentoBean.getNuEmi(), documentoBean.getNuDes());

                accion = validarEstadoDocumentoRecepcion(documentoBean, accion);

                String msgActEstadoRemito = "";
                String msgActEstadoDestino = "";
                if (accion.equals("0")) {

                    msgActEstadoRemito = this.recepDocumentoAdmDao.actualizarEstado(documentoBean);
                    msgActEstadoDestino = this.recepDocumentoAdmDao.updAtencionDocumentoBean(documentoBean);
                } else if (accion.equals("1")) {

                    if (documentoDestinoBean.getEsDocRec().equals("1")) {
                        throw new validarDatoException("El estado del documento ya es recibido.");
                    }
                    documentoBean.setEsDocRec("1");

                    documentoBean.setNuCorDes(this.recepDocumentoAdmDao.getNumCorrelativoDestino(documentoBean.getNuAnn(), documentoDestinoBean.getCoDepDes()));
                    msgActEstadoRemito = this.recepDocumentoAdmDao.actualizarEstado(documentoBean);
                    msgActEstadoDestino = this.recepDocumentoAdmDao.updRecepcionDocumentoBean(documentoBean);
                } else if (accion.equals("2")) {
                    if (documentoDestinoBean.getEsDocRec().equals("0")) {
                        throw new validarDatoException("No se puede anular la recepcidebido a que el estado del documento es No Leído");
                    }
                    documentoBean.setEsDocRec("0");
                    documentoBean.setCoEtiquetaRec("0");
                    documentoBean.setTiFisicoRec("0");
                    changeToAnulado(documentoBean.getNuAnn(), documentoBean.getNuEmi(), documentoBean.getNuDes(), usuario.getCoUsuario());
                }
                if ("NO_OK".equals(msgActEstadoDestino)) {
                    throw new validarDatoException("Error al Actualizar Documento");
                }

                if ("NO_OK".equals(msgActEstadoRemito)) {
                    throw new validarDatoException("Error al Actualizando Estado!");
                }

                if ((accion.equals("1") || accion.equals("2"))
                        && documentoEmiBean.getTiEmi() != null && !documentoEmiBean.getTiEmi().trim().equals("") && !documentoEmiBean.getTiEmi().equals("01") && !documentoEmiBean.getTiEmi().equals("05")) {

                    vReturn = this.recepDocumentoAdmDao.actualizarGuiaMesaPartes(documentoBean);
                    if ("NO_OK".equals(vReturn)) {
                        throw new validarDatoException("Error al Actualizar Guia Mesa Partes!");
                    }
                }

                vReturn = this.recepDocumentoAdmDao.updEtiquetaTipoRecepDocumento(documentoBean);
                if ("NO_OK".equals(vReturn)) {
                    throw new validarDatoException("Error al Actualizar La etiqueta y tipo recepcion del documento");
                }

                documentoBean.setDeEsDocDes(this.recepDocumentoAdmDao.getDesEstadoDocRecepcion(documentoBean.getEsDocRec()));
                vReturn = this.audiMovDoc.audiEstadoDocumentoDestino(usuario, documentoDestinoBean.getNuAnn(), documentoDestinoBean.getNuEmi(), documentoDestinoBean.getNuDes(), documentoDestinoBean.getEsDocRec());
                if (!vReturn.equals("OK")) {
                    vReturn = "Error insertando Auditoria.";
                    throw new validarDatoException(vReturn);
                }
            }
        } catch (validarDatoException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new validarDatoException("ERROR EN TRANSACCION");
        }
        return vReturn;
    }

    public String changeToAnulado(String nuAnn, String nuEmi, String nuDes, String coUseMod) throws Exception {
        String vReturn = this.recepDocumentoAdmDao.validarAnulacionDocRecepcion(nuAnn, nuEmi, nuDes);
        if ("NO_OK".equals(vReturn)) {
            throw new validarDatoException("No se puede Anular Recepcion, Documento ya fue Referenciado!");
        }
        vReturn = this.recepDocumentoAdmDao.updAnulaRecepecionDocumentoBean(nuAnn, nuEmi, nuDes, coUseMod);
        if ("NO_OK".equals(vReturn)) {
            throw new validarDatoException("Error al Actualizar Documento");
        }

        return vReturn;
    }

}
