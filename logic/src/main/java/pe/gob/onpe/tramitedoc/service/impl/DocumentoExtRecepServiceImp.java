package pe.gob.onpe.tramitedoc.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.libreria.exception.validarDatoException;
import pe.gob.onpe.libreria.util.Utility;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoExtRecepBean;
import pe.gob.onpe.tramitedoc.bean.CiudadanoBean;
import pe.gob.onpe.tramitedoc.bean.DestinatarioDocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.DestinatarioOtroOrigenBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoExtRecepBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoObjBean;
import pe.gob.onpe.tramitedoc.bean.ExpedienteDocExtRecepBean;
import pe.gob.onpe.tramitedoc.bean.MotivoBean;
import pe.gob.onpe.tramitedoc.bean.PermisoBean;
import pe.gob.onpe.tramitedoc.bean.PermisoUsuarioBean;
import pe.gob.onpe.tramitedoc.bean.ProcesoExpBean;
import pe.gob.onpe.tramitedoc.bean.ProveedorBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaDocExtRecepBean;
import pe.gob.onpe.tramitedoc.bean.RemitenteBean;
import pe.gob.onpe.tramitedoc.bean.RemitenteDocExtRecepBean;
import pe.gob.onpe.tramitedoc.bean.ReporteBean;
import pe.gob.onpe.tramitedoc.bean.RequisitoBean;
import pe.gob.onpe.tramitedoc.bean.TrxDocExternoRecepBean;
import pe.gob.onpe.tramitedoc.dao.CiudadanoDao;
import pe.gob.onpe.tramitedoc.dao.CommonQueryDao;
import pe.gob.onpe.tramitedoc.dao.DatosPlantillaDao;
import pe.gob.onpe.tramitedoc.dao.EmiDocumentoAdmDao;
import pe.gob.onpe.tramitedoc.dao.MesaPartesDao;
import pe.gob.onpe.tramitedoc.dao.ProveedorDao;
import pe.gob.onpe.tramitedoc.service.ActualizaResumenService;
import pe.gob.onpe.tramitedoc.service.AuditoriaMovimientoDocService;
import pe.gob.onpe.tramitedoc.service.DocumentoExtRecepService;
import pe.gob.onpe.tramitedoc.service.PermisoService;
import pe.gob.onpe.tramitedoc.util.Constantes;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;
import pe.gob.segdi.pide.consultas.bean.ConsultaDniBean;
import pe.gob.segdi.pide.consultas.bean.ConsultaSunatBean;
import pe.gob.segdi.pide.consultas.ws.WSConsultaDni;
import pe.gob.segdi.pide.consultas.ws.WSConsultaRuc;

@Service("documentoExtRecepService")
public class DocumentoExtRecepServiceImp implements DocumentoExtRecepService {

    @Autowired
    private EmiDocumentoAdmDao emiDocumentoAdmDao;
    @Autowired
    private ProveedorDao proveedorDao;
    @Autowired
    private CiudadanoDao ciudadanoDao;
    @Autowired
    private MesaPartesDao mesaPartesDao;
    @Autowired
    private DatosPlantillaDao datosPlantillaDao;
    @Autowired
    private CommonQueryDao commonQueryDao;
    @Autowired
    private ActualizaResumenService actualizaResumenService;
    @Autowired
    AuditoriaMovimientoDocService audiMovDoc;
    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private PermisoService permisoService;

    public List<DocumentoExtRecepBean> getDocumentosExtRecep(BuscarDocumentoExtRecepBean buscarDocumentoExtRecepBean) {
        List<DocumentoExtRecepBean> list = null;
        try {
            buscarDocumentoExtRecepBean.setCoGrupo("3");

            //EJHM
            //COD_PERMISO_DOC_MP_OFICINA
            PermisoUsuarioBean permisoUsuario = new PermisoUsuarioBean();
            permisoUsuario.setCodOpc(Constantes.COD_PERMISO_DOC_MP_OFICINA);
            permisoUsuario.setCempCodEmp(buscarDocumentoExtRecepBean.getCoEmpleado()); // EL CODIGO DE SESION
            permisoUsuario.setCodAplica(Constantes.COD_SISTEMA_SGD);
            if (permisoService.obtenerPermiso(permisoUsuario)) {
                buscarDocumentoExtRecepBean.setTiAcceso("2");//toda la bandeja de oficina
            }

            list = this.mesaPartesDao.getDocumentosExtRecep(buscarDocumentoExtRecepBean);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public String grabaDocumentoExternoRecep(TrxDocExternoRecepBean trxDocExternoRecepBean, Usuario usuario) throws Exception {
        String vReturn = "NO_OK";
        try {
            String pnuAnn = trxDocExternoRecepBean.getNuAnn();
            String pnuEmi = trxDocExternoRecepBean.getNuEmi();
            String pnuAnnExp = trxDocExternoRecepBean.getNuAnnExp();
            String pnuSecExp = trxDocExternoRecepBean.getNuSecExp();
            String pcoUserMod = trxDocExternoRecepBean.getCoUserMod();
            String pcempCodEmp = trxDocExternoRecepBean.getCempCodEmp();
            DocumentoExtRecepBean documentoExtRecepBean = trxDocExternoRecepBean.getDocumentoEmiBean();

            //System.out.println(trxDocExternoRecepBean.getDocumentoEmiBean().getNuExpediente()+" trxDocExternoRecepBean.getDocumentoEmiBean().getNuExpediente()");

            ExpedienteDocExtRecepBean expedienteBean = trxDocExternoRecepBean.getExpedienteEmiBean();

            RemitenteDocExtRecepBean remitenteBean = trxDocExternoRecepBean.getRemitenteEmiBean();
            ArrayList<ReferenciaDocExtRecepBean> lstReferencia = trxDocExternoRecepBean.getLstReferencia();
            ArrayList<DestinatarioDocumentoEmiBean> lstDestinatario = trxDocExternoRecepBean.getLstDestinatario();
            if (pnuAnn != null && pnuEmi != null && !pnuAnn.equals("") && !pnuEmi.equals("")) {
                System.out.println("ingresa upc");
                trxDocExternoRecepBean.setAccionBD("UPD");
                String pcoDepEmi = trxDocExternoRecepBean.getCoDependencia();
                if (expedienteBean != null) {
                    expedienteBean.setUsCreaAudi(pcempCodEmp);
                    vReturn = Utility.getInstancia().getStringFechaFormat(expedienteBean.getFeVence(), "dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy");
                    if ("NO_OK".equals(vReturn)) {
                        throw new validarDatoException("Error fecha vencimiento Expediente.");
                    }
                    expedienteBean.setFeVence(vReturn);
                    vReturn = this.mesaPartesDao.updExpedienteBean(expedienteBean);
                    if ("NO_OK".equals(vReturn)) {
                        throw new validarDatoException("Error actualizando Expediente.");
                    }
                }
                if (documentoExtRecepBean != null || remitenteBean != null) {
                    if (documentoExtRecepBean != null) {
                        documentoExtRecepBean.setCoDepEmi(pcoDepEmi);
                    }
                    vReturn = this.mesaPartesDao.updDocumentoExtBean(pnuAnn, pnuEmi, documentoExtRecepBean, expedienteBean, remitenteBean, pcoUserMod);
                    if (!"OK".equals(vReturn)) {
                        throw new validarDatoException("Error actualizando Documento.");
                    }
                }
                for (ReferenciaDocExtRecepBean ref : lstReferencia) {
                    String sAccionBD = ref.getAccionBd();
                    ref.setCoUseCre(pcoUserMod);
                    if ("INS".equals(sAccionBD)) {
                        vReturn = this.mesaPartesDao.insReferenciaDocumentoEmi(pnuAnn, pnuEmi, ref);
                        if ("NO_OK".equals(vReturn)) {
                            throw new validarDatoException("Error Agregando Referencia");
                        }
                        continue;
                    }
                    if ("UPD".equals(sAccionBD)) {
                        vReturn = this.mesaPartesDao.updReferenciaDocumentoEmi(pnuAnn, pnuEmi, ref);
                        if ("NO_OK".equals(vReturn)) {
                            throw new validarDatoException("Error Actualizando Referencia");
                        }
                        continue;
                    }
                    if ("DEL".equals(sAccionBD)) {
                        vReturn = this.mesaPartesDao.delReferenciaDocumentoEmi(pnuAnn, pnuEmi, ref.getNuAnn(), ref.getNuEmi());
                        if ("NO_OK".equals(vReturn)) {
                            throw new validarDatoException("Error Borrando Referencia");
                        }
                    }
                }
                for (DestinatarioDocumentoEmiBean dest : lstDestinatario) {
                    String sAccionBD = dest.getAccionBD();
                    dest.setCoUseCre(pcoUserMod);
                    if ("INS".equals(sAccionBD)) {
                        vReturn = this.mesaPartesDao.insDestinatarioDocumentoEmi(pnuAnn, pnuEmi, dest);
                        if ("NO_OK".equals(vReturn)) {
                            throw new validarDatoException("Error Agregando Destinatario2");
                        }
                        continue;
                    }
                    if ("UPD".equals(sAccionBD)) {
                        vReturn = this.mesaPartesDao.updDestinatarioDocumentoEmi(pnuAnn, pnuEmi, dest);
                        if ("NO_OK".equals(vReturn)) {
                            throw new validarDatoException("Error Actualizando Destinatario");
                        }
                        continue;
                    }
                    if ("DEL".equals(sAccionBD)) {
                        vReturn = this.mesaPartesDao.delDestinatarioDocumentoEmi(pnuAnn, pnuEmi, dest.getNuDes());
                        if ("NO_OK".equals(vReturn)) {
                            throw new validarDatoException("Error Borrando Destinatario");
                        }
                    }
                }
            } else {
                trxDocExternoRecepBean.setAccionBD("INS");
                expedienteBean.setUsCreaAudi(pcempCodEmp);

                vReturn = Utility.getInstancia().getStringFechaFormat(expedienteBean.getFeExp(), "dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm");
                if ("NO_OK".equals(vReturn)) {
                    throw new validarDatoException("Error fecha Expediente.");
                }


                expedienteBean.setFeExp(vReturn);
                vReturn = Utility.getInstancia().getStringFechaFormat(expedienteBean.getFeVence(), "dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy");
                if ("NO_OK".equals(vReturn)) {
                    throw new validarDatoException("Error fecha vencimiento Expediente.");
                }
                expedienteBean.setFeVence(vReturn);


                //HENRY

                if (expedienteBean.getNuCorrExp() == null || expedienteBean.getNuCorrExp().equals("")) {
                    vReturn = this.mesaPartesDao.getNumeroExpediente(expedienteBean);
                }
                expedienteBean.setNuCorrExp(vReturn);
                documentoExtRecepBean.setNuCorrExp(vReturn);
                documentoExtRecepBean.setNuExpediente(expedienteBean.getNuExpediente());
                //System.out.println(expedienteBean.getNuSecExp()+" documentoExtRecepBean.getNuSecExp()");
                //System.out.println(expedienteBean.getNuExpediente()+" getNuExpediente");

                if (expedienteBean.getNuExpediente() == null || expedienteBean.getNuExpediente().isEmpty() ){
                    //System.out.println("ingual ingresa por aqui");
                    expedienteBean.setNuExpediente(expedienteBean.getNuAnnExp()+"-"+vReturn);
                    documentoExtRecepBean.setNuExpediente(expedienteBean.getNuExpediente());
                    //System.out.println(expedienteBean.getNuExpediente()+" numero de expediente");
                    vReturn = this.mesaPartesDao.insExpedienteBean(expedienteBean);
                    if (!"OK".equals(vReturn)) {
                        throw new validarDatoException(vReturn);
                    }
                }

                documentoExtRecepBean.setCoUseMod(pcoUserMod);
                documentoExtRecepBean.setCoEsDocEmiMp("5");
                vReturn = this.mesaPartesDao.getNroCorrelativoDocumento(documentoExtRecepBean.getNuAnn(), remitenteBean.getCoDepEmi(), remitenteBean.getTiEmi());
                if ("NO_OK".equals(vReturn)) {
                    throw new validarDatoException("Error al obtener ncorrelativo.");
                }
                documentoExtRecepBean.setNuCorDoc(vReturn);
                documentoExtRecepBean.setNuSecExp(expedienteBean.getNuSecExp());
                remitenteBean.setCoDep(usuario.getCoDep());



                vReturn = this.mesaPartesDao.insDocumentoExtBean(documentoExtRecepBean, expedienteBean, remitenteBean);


                //HENRY HASTA AQUI

                if (!"OK".equals(vReturn)) {
                    throw new validarDatoException("Error al Grabar Documento.");
                }
                pnuEmi = documentoExtRecepBean.getNuEmi();
                vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, documentoExtRecepBean.getNuAnn(), pnuEmi, "NC");
                if (!vReturn.equals("OK")) {
                    vReturn = "Error insertando Auditoria.";
                    throw new validarDatoException(vReturn);
                }
                trxDocExternoRecepBean.setNuEmi(pnuEmi);
                pnuAnnExp = expedienteBean.getNuAnnExp();
                pnuSecExp = expedienteBean.getNuSecExp();
                trxDocExternoRecepBean.setNuAnnExp(pnuAnnExp);
                trxDocExternoRecepBean.setNuSecExp(pnuSecExp);
                for (ReferenciaDocExtRecepBean ref : lstReferencia) {
                    ref.setCoUseCre(pcoUserMod);
                    vReturn = this.mesaPartesDao.insReferenciaDocumentoEmi(pnuAnn, pnuEmi, ref);
                    if ("NO_OK".equals(vReturn)) {
                        throw new validarDatoException("Error Agregando Referencia");
                    }
                }
                for (DestinatarioDocumentoEmiBean dest : lstDestinatario) {
                    dest.setCoUseCre(pcoUserMod);
                    vReturn = this.mesaPartesDao.insDestinatarioDocumentoEmi(pnuAnn, pnuEmi, dest);
                    if ("NO_OK".equals(vReturn)) {
                        throw new validarDatoException("Error Agregando Destinatario3");
                    }
                }
            }

            if (lstDestinatario.size() >= 1) {
                vReturn = this.actualizaResumenService.updRemitosResumenDes(pnuAnn, pnuEmi);
                if ("NO_OK".equals(vReturn)) {
                    throw new validarDatoException("Error Actualizando Remito resumen destinatario");
                }
            }
            if (lstReferencia.size() >= 1) {
                vReturn = this.actualizaResumenService.updRemitosResumenRef(pnuAnn, pnuEmi);
                if ("NO_OK".equals(vReturn)) {
                    throw new validarDatoException("Error Actualizando Remito resumen Referencia");
                }
            }
        } catch (validarDatoException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw new validarDatoException("ERROR EN TRANSACCION.");
        }
        return vReturn;
    }

    public String getJsonRptGrabaDocumentoExternoRecep(TrxDocExternoRecepBean trxDocExternoRecepBean) {
        boolean bandera = false;
        StringBuilder retval = new StringBuilder();
        String paccionBD = trxDocExternoRecepBean.getAccionBD();
        try {
            DocumentoExtRecepBean documentoExtRecepBean = trxDocExternoRecepBean.getDocumentoEmiBean();
            if (paccionBD.equals("UPD")) {
                if (documentoExtRecepBean != null) {
                    retval.append("\"documentoEmiBean\":{");
                    retval.append("\"nuCorDoc\":\"");
                    retval.append(documentoExtRecepBean.getNuCorDoc());
                    retval.append("\",\"esDocEmi\":\"");
                    retval.append(documentoExtRecepBean.getCoEsDocEmiMp());
                    retval.append("\"},");
                }
            } else if (paccionBD.equals("INS")) {
                retval.append("\"documentoEmiBean\":{");
                retval.append("\"nuAnn\":\"");
                retval.append(documentoExtRecepBean.getNuAnn());
                retval.append("\",\"nuEmi\":\"");
                retval.append(documentoExtRecepBean.getNuEmi());
                retval.append("\",\"nuAnnExp\":\"");
                retval.append(documentoExtRecepBean.getNuAnn());
                retval.append("\",\"nuSecExp\":\"");
                retval.append(documentoExtRecepBean.getNuSecExp());
                retval.append("\",\"nuCorDoc\":\"");
                retval.append(documentoExtRecepBean.getNuCorDoc());
                retval.append("\",\"esDocEmi\":\"");
                retval.append(documentoExtRecepBean.getCoEsDocEmiMp());
                retval.append("\",\"nuCorrExp\":\"");
                retval.append(documentoExtRecepBean.getNuCorrExp());
                retval.append("\",\"nuExpediente\":\"");
                retval.append(documentoExtRecepBean.getNuExpediente());
                retval.append("\"},");
            }
            retval.append("\"lstReferencia\":[");
            for (ReferenciaDocExtRecepBean ref : trxDocExternoRecepBean.getLstReferencia()) {
                retval.append("{\"accionBD\":\"");
                retval.append(ref.getAccionBd());
                retval.append("\",\"nuAnn\":\"");
                retval.append(ref.getNuAnn());
                retval.append("\",\"nuEmi\":\"");
                retval.append(ref.getNuEmi());
                retval.append("\",\"coRef\":\"");
                retval.append(ref.getCoRef());
                retval.append("\"},");
                bandera = true;
            }
            if (bandera) {
                retval.deleteCharAt(retval.length() - 1);
                bandera = false;
            }
            retval.append("]");

            retval.append(",\"lstDestinatario\":[");
            for (DestinatarioDocumentoEmiBean dest : trxDocExternoRecepBean.getLstDestinatario()) {
                retval.append("{\"accionBD\":\"");
                retval.append(dest.getAccionBD());
                retval.append("\",\"coDependencia\":\"");
                retval.append(dest.getCoDependencia());
                retval.append("\",\"coEmpleado\":\"");
                retval.append(dest.getCoEmpleado());
                retval.append("\",\"nuDes\":\"");
                retval.append(dest.getNuDes());
                retval.append("\"},");
                bandera = true;
            }
            if (bandera) {
                retval.deleteCharAt(retval.length() - 1);
            }
            retval.append("]");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retval.toString();
    }

    public String isExpedienteDuplicado(String pnuAnnExp, String pcoDepExp, String pnuCorrExp) {
        String vResult = "1";
        try {
            vResult = this.commonQueryDao.verificarExpedienteDuplicado(pnuAnnExp, pcoDepExp, "3", pnuCorrExp);
            if (vResult.equals("0")) {
                vResult = this.commonQueryDao.verficarNumeroDuplicadoEmiDocExtRecep(pnuAnnExp, pcoDepExp, pnuCorrExp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vResult;
    }

    public DocumentoExtRecepBean getDocumentoExtRecepNew(String pcoDependencia) {
        DocumentoExtRecepBean docExterno = null;
        String esDocEmi = "5";
        try {
            docExterno = this.mesaPartesDao.getDocumentoExtRecepNew(pcoDependencia);
            if (docExterno != null) {
                docExterno.setCoEsDocEmiMp(esDocEmi);
                docExterno.setNuFolios("");
                docExterno.setTiEmi("03");

                docExterno.setCoTipDocAdm("231");
                docExterno.setDeAsu("");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return docExterno;
    }

    public List<MotivoBean> getLstMotivoxTipoDocumento() {
        List<MotivoBean> list = null;
        try {
            list = this.mesaPartesDao.getLstMotivoxTipoDocumento();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<ReferenciaDocExtRecepBean> getLstDocExtReferencia(String pnuAnn, String pcoTiDoc, String pcoDepEmi, String pnuExpediente) {
        List<ReferenciaDocExtRecepBean> list = null;
        try {
            if (pnuExpediente != null && pnuExpediente.trim().length() > 0) {
                pnuExpediente = Utility.getInstancia().rellenaCerosIzquierda(pnuExpediente, 7);
            }
            list = this.mesaPartesDao.getLstDocExtReferencia(pnuAnn, pcoTiDoc, pcoDepEmi, pnuExpediente);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public DocumentoExtRecepBean getDocumentoExtRec(String pnuAnn, String pnuEmi) {
        DocumentoExtRecepBean documentoExtRecepBean = null;
        try {
            documentoExtRecepBean = this.mesaPartesDao.getDocumentoExtRec(pnuAnn, pnuEmi);
            if (documentoExtRecepBean != null) {
                String ptiEmi = documentoExtRecepBean.getTiEmi();
                String pcoOtros = documentoExtRecepBean.getCoOtros();
                if (ptiEmi != null && ptiEmi.equals("04") && pcoOtros != null && pcoOtros.trim().length() > 0) {
                    Map map = this.mesaPartesDao.getDesFieldOtro(pcoOtros);
                    if (map != null) {
                        documentoExtRecepBean.setDeNomOtros(String.valueOf(map.get("DE_NOM_OTROS")));
                        documentoExtRecepBean.setDeDocOtros(String.valueOf(map.get("DE_DOC_OTROS")));
                        documentoExtRecepBean.setNuDocOtros(String.valueOf(map.get("NU_DOC_OTROS")));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentoExtRecepBean;
    }

    public List<DestinatarioDocumentoEmiBean> getLstDestinoEmiDoc(String pnuAnn, String pnuEmi) {
        List<DestinatarioDocumentoEmiBean> list = null;
        try {
            list = this.mesaPartesDao.getLstDestinoEmiDoc(pnuAnn, pnuEmi);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<ReferenciaDocExtRecepBean> getLstReferenciaDoc(String pnuAnn, String pnuEmi) {
        List<ReferenciaDocExtRecepBean> list = null;
        try {
            if (pnuAnn != null && pnuAnn.trim().length() > 0 && pnuEmi != null && pnuEmi.trim().length() > 0) {
                list = this.mesaPartesDao.getLstReferenciaDoc(pnuAnn, pnuEmi);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<ProveedorBean> getLstProveedores(String prazonSocial) {
        List<ProveedorBean> list = null;
        try {
            list = this.commonQueryDao.getLstProveedores(prazonSocial);
            if (list.size() == 0 || list == null) {
                WSConsultaRuc wSSunat = new WSConsultaRuc();
                list = wSSunat.listarSunat(this.datosPlantillaDao.getParametros("URL_RZ_SUNAT_REST"), prazonSocial);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public String getCiudadano(String pnuDoc, String pUsuario) {
        CiudadanoBean ciudadanoBean = null;
        String coRespuesta = "0";
        StringBuilder retval = new StringBuilder();
        try {
            String urlReniec = this.applicationProperties.getUrlReniec();
            ciudadanoBean = this.commonQueryDao.getCiudadano(pnuDoc);
            retval.append("{\"coRespuesta\":\"");
            if (ciudadanoBean != null) {
                coRespuesta = "1";
                retval.append(coRespuesta);
                retval.append("\",");
                retval.append("\"ciudadanoBean\":{");
                retval.append("\"nuDoc\":\"");
                retval.append(ciudadanoBean.getNuDocumento());
                retval.append("\",\"nombre\":\"");
                retval.append(ciudadanoBean.getNombre());
                retval.append("\",\"idDepartamento\":\"");
                retval.append((ciudadanoBean.getIdDepartamento() == null) ? "" : ciudadanoBean.getIdDepartamento());
                retval.append("\",\"idProvincia\":\"");
                retval.append((ciudadanoBean.getIdProvincia() == null) ? "" : ciudadanoBean.getIdProvincia());
                retval.append("\",\"idDistrito\":\"");
                retval.append((ciudadanoBean.getIdDistrito() == null) ? "" : ciudadanoBean.getIdDistrito());
                retval.append("\",\"deDireccion\":\"");
                retval.append((ciudadanoBean.getDeDireccion() == null) ? "" : ciudadanoBean.getDeDireccion());
                retval.append("\",\"deCorreo\":\"");
                retval.append((ciudadanoBean.getDeCorreo() == null) ? "" : ciudadanoBean.getDeCorreo());
                retval.append("\",\"telefono\":\"");
                retval.append((ciudadanoBean.getTelefono() == null) ? "" : ciudadanoBean.getTelefono());
                retval.append("\"}");
            } else {
                try {
                    String cCELE_DECOR = this.mesaPartesDao.getPassDniPide("CO_DNI_PIDE", pUsuario);

                    WSConsultaDni wSSunat = new WSConsultaDni();
                    ConsultaDniBean beanDdp = wSSunat.consultaDni(this.datosPlantillaDao.getParametros("URL_RENIEC_REST"), pnuDoc, cCELE_DECOR.substring(0, 8), this.datosPlantillaDao.getParametros("RUC_INSTITUCION"), Utility.getInstancia().descifrar(cCELE_DECOR.substring(9, cCELE_DECOR.length()), "SgDPasswordSecretPasswor"));
                    if (beanDdp.getPrenombres() != null && beanDdp.getApPrimer() != null) {
                        String vResult = this.ciudadanoDao.insPideCiudadano(beanDdp, pnuDoc);

                        if (vResult == "OK") {

                            ciudadanoBean = this.commonQueryDao.getCiudadano(pnuDoc);
                            if (ciudadanoBean != null) {
                                coRespuesta = "1";
                                retval.append(coRespuesta);
                                retval.append("\",");
                                retval.append("\"ciudadanoBean\":{");
                                retval.append("\"nuDoc\":\"");
                                retval.append(ciudadanoBean.getNuDocumento());
                                retval.append("\",\"nombre\":\"");
                                retval.append(ciudadanoBean.getNombre());
                                retval.append("\",\"idDepartamento\":\"");
                                retval.append((ciudadanoBean.getIdDepartamento() == null) ? "" : ciudadanoBean.getIdDepartamento());
                                retval.append("\",\"idProvincia\":\"");
                                retval.append((ciudadanoBean.getIdProvincia() == null) ? "" : ciudadanoBean.getIdProvincia());
                                retval.append("\",\"idDistrito\":\"");
                                retval.append((ciudadanoBean.getIdDistrito() == null) ? "" : ciudadanoBean.getIdDistrito());
                                retval.append("\",\"deDireccion\":\"");
                                retval.append((ciudadanoBean.getDeDireccion() == null) ? "" : ciudadanoBean.getDeDireccion());
                                retval.append("\",\"deCorreo\":\"");
                                retval.append((ciudadanoBean.getDeCorreo() == null) ? "" : ciudadanoBean.getDeCorreo());
                                retval.append("\",\"telefono\":\"");
                                retval.append((ciudadanoBean.getTelefono() == null) ? "" : ciudadanoBean.getTelefono());
                                retval.append("\"}");
                            } else {

                                retval.append(coRespuesta);
                                retval.append("\"");
                            }

                        } else {

                            retval.append(coRespuesta);
                            retval.append("\"");
                        }

                    } else {
                        retval.append(coRespuesta);
                        retval.append("\"");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    retval.append(coRespuesta);
                    retval.append("\"");
                }
            }
            retval.append("}");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return retval.toString();
    }

    public List<DestinatarioOtroOrigenBean> getLstOtrosOrigenes(String pdescripcion) {
        List<DestinatarioOtroOrigenBean> list = null;
        try {
            list = this.commonQueryDao.getLstOtrosOrigenes(pdescripcion);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public String getProveedor(String pnuRuc) {
        String coRespuesta = "0";
        StringBuilder retval = new StringBuilder();
        try {
            ProveedorBean proveedorBean = this.commonQueryDao.getProveedor(pnuRuc);
            retval.append("{\"coRespuesta\":\"");
            if (proveedorBean != null) {
                coRespuesta = "1";
                retval.append(coRespuesta);
                retval.append("\",");
                retval.append("\"proveedorBean\":{");
                retval.append("\"nuRuc\":\"");
                retval.append(proveedorBean.getNuRuc());
                retval.append("\",\"deRuc\":\"");
                retval.append(proveedorBean.getDescripcion());
                retval.append("\",\"idDepartamento\":\"");
                retval.append((proveedorBean.getIdDepartamento() == null) ? "" : proveedorBean.getIdDepartamento());
                retval.append("\",\"idProvincia\":\"");
                retval.append((proveedorBean.getIdProvincia() == null) ? "" : proveedorBean.getIdProvincia());
                retval.append("\",\"idDistrito\":\"");
                retval.append((proveedorBean.getIdDistrito() == null) ? "" : proveedorBean.getIdDistrito());
                retval.append("\",\"deDireccion\":\"");
                retval.append((proveedorBean.getDeDireccion() == null) ? "" : proveedorBean.getDeDireccion());
                retval.append("\",\"deCorreo\":\"");
                retval.append((proveedorBean.getDeCorreo() == null) ? "" : proveedorBean.getDeCorreo());
                retval.append("\",\"telefono\":\"");
                retval.append((proveedorBean.getTelefono() == null) ? "" : proveedorBean.getTelefono());
                retval.append("\"}");
            } else {

                try {
                    WSConsultaRuc wSSunat = new WSConsultaRuc();
                    ConsultaSunatBean beanDdp = wSSunat.consultaSunat(this.datosPlantillaDao.getParametros("URL_SUNAT_REST"), pnuRuc);
                    if (beanDdp.getDdp_nombre() != null) {
                        String vResult = this.proveedorDao.insPideProveedor(beanDdp);

                        if (vResult == "OK") {

                            proveedorBean = this.commonQueryDao.getProveedor(pnuRuc);
                            if (proveedorBean != null) {
                                coRespuesta = "1";
                                retval.append(coRespuesta);
                                retval.append("\",");
                                retval.append("\"proveedorBean\":{");
                                retval.append("\"nuRuc\":\"");
                                retval.append(proveedorBean.getNuRuc());
                                retval.append("\",\"deRuc\":\"");
                                retval.append(proveedorBean.getDescripcion());
                                retval.append("\",\"idDepartamento\":\"");
                                retval.append((proveedorBean.getIdDepartamento() == null) ? "" : proveedorBean.getIdDepartamento());
                                retval.append("\",\"idProvincia\":\"");
                                retval.append((proveedorBean.getIdProvincia() == null) ? "" : proveedorBean.getIdProvincia());
                                retval.append("\",\"idDistrito\":\"");
                                retval.append((proveedorBean.getIdDistrito() == null) ? "" : proveedorBean.getIdDistrito());
                                retval.append("\",\"deDireccion\":\"");
                                retval.append((proveedorBean.getDeDireccion() == null) ? "" : proveedorBean.getDeDireccion());
                                retval.append("\",\"deCorreo\":\"");
                                retval.append("");
                                retval.append("\",\"telefono\":\"");
                                retval.append("");
                                retval.append("\"}");
                            } else {
                                retval.append(coRespuesta);
                                retval.append("\"");
                            }

                        } else {

                            retval.append(coRespuesta);
                            retval.append("\"");
                        }

                    } else {
                        retval.append(coRespuesta);
                        retval.append("\"");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    retval.append(coRespuesta);
                    retval.append("\"");
                }
            }

            retval.append("}");
        } catch (Exception e) {
            e.printStackTrace();
            retval.append("}");
        }
        return retval.toString();
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public String changeToRegistrado(DocumentoExtRecepBean documentoExtRecepBean, Usuario usuario) throws Exception {
        String vReturn = "NO_OK";
        String pnuAnn = documentoExtRecepBean.getNuAnn();
        String pnuEmi = documentoExtRecepBean.getNuEmi();
        String pcoUseMod = documentoExtRecepBean.getCoUseMod();
        try {
            if (pnuAnn != null && pnuAnn.trim().length() > 0 && pnuEmi != null && pnuEmi.trim().length() > 0) {
                documentoExtRecepBean = this.mesaPartesDao.getDocumentoExtRecBasic(pnuAnn, pnuEmi);
                if (documentoExtRecepBean == null) {
                    throw new validarDatoException("Documento no Exite.");
                }

                String esDocEmi = documentoExtRecepBean.getCoEsDocEmiMp();
                if (esDocEmi != null && esDocEmi.equals("7")) {

                    String vpfd = getVerificaPdfDoc(documentoExtRecepBean.getNuAnn(), documentoExtRecepBean.getNuEmi(), null);
                    if (vpfd.equals("OK")) {

                        vReturn = this.mesaPartesDao.getRequisitoPendiente(documentoExtRecepBean.getNuAnnExp(), documentoExtRecepBean.getNuSecExp());
                        if (vReturn.equals("OK")) {

                            vReturn = this.mesaPartesDao.getPermisoChangeEstadoMP(usuario.getCempCodemp(), usuario.getCoDep());
                            if (vReturn.equals("1")) {

                                documentoExtRecepBean.setCoEsDocEmiMp("0");
                                documentoExtRecepBean.setCoUseMod(pcoUseMod);
                                vReturn = this.mesaPartesDao.updEstadoDocumentoExt(documentoExtRecepBean);
                                if (vReturn.equals("OK")) {

                                    vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, documentoExtRecepBean.getNuAnn(), documentoExtRecepBean.getNuEmi(), esDocEmi);
                                    if (!vReturn.equals("OK")) {
                                        vReturn = "Error insertando Auditoria.";
                                        throw new validarDatoException(vReturn);
                                    }

                                } else {

                                    throw new validarDatoException("Error cambiando a Estado Registrado.");
                                }
                            } else {
                                throw new validarDatoException("No habilitado para cambiar a estado <strong>\\\"Registrado\\\"</strong>.");
                            }
                        } else {
                            throw new validarDatoException("Requisitos faltantes.");
                        }
                    } else {
                        throw new validarDatoException("El Documento tiene que estar en PDF");
                    }
                } else {
                    throw new validarDatoException("Operacino Permitida.");
                }
            } else {

                throw new validarDatoException("Documento no Existe.");
            }
        } catch (validarDatoException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new validarDatoException("ERROR EN TRANSACCION A REGISTRADO.");
        }
        return vReturn;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public String changeToEnRegistro(DocumentoExtRecepBean documentoExtRecepBean, Usuario usuario) throws Exception {
        String vReturn = "NO_OK";
        String pnuAnn = documentoExtRecepBean.getNuAnn();
        String pnuEmi = documentoExtRecepBean.getNuEmi();
        String pcoEmpRes = documentoExtRecepBean.getCoEmpRes();
        String pcoUseMod = documentoExtRecepBean.getCoUseMod();
        try {
            if (pnuAnn != null && pnuAnn.trim().length() > 0 && pnuEmi != null && pnuEmi.trim().length() > 0) {
                documentoExtRecepBean = this.mesaPartesDao.getDocumentoExtRecBasic(pnuAnn, pnuEmi);
                if (documentoExtRecepBean == null) {
                    throw new validarDatoException("Documento no Exite.");
                }

                String esDocEmi = documentoExtRecepBean.getCoEsDocEmiMp();
                if (esDocEmi != null && (esDocEmi.equals("0") || esDocEmi.equals("7"))) {
                    boolean isChangeEstado = true;
                    if (esDocEmi.equals("0")) {

                        vReturn = this.emiDocumentoAdmDao.verificarDocumentoLeido(documentoExtRecepBean.getNuAnn(), documentoExtRecepBean.getNuEmi());
                        if (vReturn != null && vReturn.equals("0")) {
                            String pcoEmpEmi = documentoExtRecepBean.getCoEmpEmi();
                            if (pcoEmpEmi == null || !pcoEmpEmi.equals(pcoEmpRes)) {
                                //EJHM
                                PermisoUsuarioBean permisoUsuario = new PermisoUsuarioBean();
                                permisoUsuario.setCodOpc(Constantes.COD_PERMISO_REVERTIR_DOCUMENTO);
                                permisoUsuario.setCempCodEmp(pcoEmpRes); // EL CODIGO DE SESION
                                permisoUsuario.setCodAplica(Constantes.COD_SISTEMA_SGD);

                                if (!permisoService.obtenerPermiso(permisoUsuario)) {
                                    isChangeEstado = false;
                                    vReturn = "El documento solo puede ser cambiado por el Responsable del Área";
                                }

                            }
                        } else {
                            isChangeEstado = false;
                            vReturn = "Documento ya fue lepor los Destinatarios.";
                        }
                    }
                    if (isChangeEstado) {

                        documentoExtRecepBean.setCoEsDocEmiMp("5");
                        documentoExtRecepBean.setCoUseMod(pcoUseMod);
                        vReturn = this.mesaPartesDao.updEstadoDocumentoExt(documentoExtRecepBean);
                        if (vReturn.equals("OK")) {

                            vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, documentoExtRecepBean.getNuAnn(), documentoExtRecepBean.getNuEmi(), esDocEmi);
                            if (!vReturn.equals("OK")) {
                                vReturn = "Error insertando Auditoria.";
                                throw new validarDatoException(vReturn);
                            }

                        } else {

                            throw new validarDatoException("Error cambiando a Estado En Registro.");
                        }
                    }
                } else {
                    throw new validarDatoException("Operacino Permitida.");
                }
            } else {

                throw new validarDatoException("Documento no Existe.");
            }
        } catch (validarDatoException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new validarDatoException("ERROR EN TRANSACCION A REGISTRADO.");
        }
        return vReturn;
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

    public String anularDocumentoExtRecep(DocumentoExtRecepBean documentoExtRecepBean, Usuario usuario) {
        String vReturn = "NO_OK";
        String pcoUsedMod = documentoExtRecepBean.getCoUseMod();
        String pesDocEmi = documentoExtRecepBean.getCoEsDocEmiMp();
        String pnuAnn = documentoExtRecepBean.getNuAnn();
        String pnuEmi = documentoExtRecepBean.getNuEmi();
        try {
            if (pnuAnn != null && pnuAnn.trim().length() > 0 && pnuEmi != null && pnuEmi.trim().length() > 0) {
                if (pesDocEmi != null && pesDocEmi.equals("5")) {
                    documentoExtRecepBean = this.mesaPartesDao.getDocumentoExtRecBasic(pnuAnn, pnuEmi);
                    if (documentoExtRecepBean == null) {
                        vReturn = "Documento no Exite.";
                    } else {
                        pesDocEmi = documentoExtRecepBean.getCoEsDocEmiMp();
                        if (pesDocEmi != null && pesDocEmi.equals("5")) {
                            vReturn = this.emiDocumentoAdmDao.verificarDocumentoLeido(documentoExtRecepBean.getNuAnn(), documentoExtRecepBean.getNuEmi());
                            if (vReturn != null && vReturn.equals("0")) {
                                documentoExtRecepBean.setCoUseMod(pcoUsedMod);
                                documentoExtRecepBean.setCoEsDocEmiMp("9");
                                vReturn = this.mesaPartesDao.anularDocumentoExtRecep(documentoExtRecepBean);
                                if (vReturn.equals("OK")) {
                                    vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, documentoExtRecepBean.getNuAnn(), documentoExtRecepBean.getNuEmi(), pesDocEmi);
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
                            vReturn = "Operacino Permitida.";
                        }
                    }
                } else {
                    vReturn = "Operacino Permitida.";
                }
            } else {
                vReturn = "Documento no Existe.";
            }
        } catch (Exception e) {
            vReturn = e.getMessage();
        }
        return vReturn;
    }

    public Map getNewDocExtRecepAtender(DocumentoBean docRecBean) {
        Map<Object, Object> map = null;

        String esDocEmi = "5";
        try {
            if (docRecBean != null
                    && docRecBean.getNuAnn() != null && docRecBean.getNuAnn().trim().length() > 0 && docRecBean
                    .getNuEmi() != null && docRecBean.getNuEmi().trim().length() > 0 && docRecBean
                    .getNuDes() != null && docRecBean.getNuDes().trim().length() > 0 && docRecBean
                    .getEsDocRec() != null && docRecBean.getEsDocRec().trim().length() > 0
                    && !docRecBean.getEsDocRec().equals("0") && !docRecBean.getEsDocRec().equals("9")) {
                DocumentoExtRecepBean docExterno = this.mesaPartesDao.getDocumentoExtRecepNew(docRecBean.getCoDepEmi());
                if (docExterno != null) {
                    map = new HashMap<Object, Object>();
                    docExterno.setCoEsDocEmiMp(esDocEmi);
                    docExterno.setNuFolios("0");
                    docExterno.setTiEmi("03");
                    docExterno.setDeAsu(docRecBean.getDeAsu());
                    docExterno.setNuDiaAte("0");

                    List<ReferenciaDocExtRecepBean> lsRef = new ArrayList<ReferenciaDocExtRecepBean>();
                    ReferenciaDocExtRecepBean ref = this.mesaPartesDao.getRefAtenderDocExtRec(docRecBean.getNuAnn(), docRecBean.getNuEmi());
                    if (ref != null) {
                        ref.setNuDes(docRecBean.getNuDes());
                    }
                    lsRef.add(ref);

                    map.put("docExterno", docExterno);
                    map.put("lsReferencia", lsRef);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public String changeToParaVerificar(DocumentoExtRecepBean documentoExtRecepBean, Usuario usuario) throws Exception {
        String vReturn = "NO_OK";
        String pnuAnn = documentoExtRecepBean.getNuAnn();
        String pnuEmi = documentoExtRecepBean.getNuEmi();
        String pcoUseMod = documentoExtRecepBean.getCoUseMod();
        try {
            if (pnuAnn != null && pnuAnn.trim().length() > 0 && pnuEmi != null && pnuEmi.trim().length() > 0) {
                documentoExtRecepBean = this.mesaPartesDao.getDocumentoExtRecBasic(pnuAnn, pnuEmi);
                if (documentoExtRecepBean == null) {
                    throw new validarDatoException("Documento no Exite.");
                }

                String esDocEmi = documentoExtRecepBean.getCoEsDocEmiMp();
                if (esDocEmi != null && esDocEmi.equals("5")) {

                    String vpfd = getVerificaPdfDoc(documentoExtRecepBean.getNuAnn(), documentoExtRecepBean.getNuEmi(), null);
                    if (vpfd.equals("OK")) {

                        documentoExtRecepBean.setCoEsDocEmiMp("7");
                        documentoExtRecepBean.setCoUseMod(pcoUseMod);
                        vReturn = this.mesaPartesDao.updEstadoDocumentoExt(documentoExtRecepBean);
                        if (vReturn.equals("OK")) {

                            vReturn = this.audiMovDoc.audiEstadoDocumentoRemito(usuario, documentoExtRecepBean.getNuAnn(), documentoExtRecepBean.getNuEmi(), esDocEmi);
                            if (!vReturn.equals("OK")) {
                                vReturn = "Error insertando Auditoria.";
                                throw new validarDatoException(vReturn);
                            }

                        } else {

                            throw new validarDatoException("Error cambiando a Estado Para Verificar.");
                        }
                    } else {
                        throw new validarDatoException("El Documento tiene que estar en PDF");
                    }
                } else {
                    throw new validarDatoException("Operacino Permitida.");
                }
            } else {

                throw new validarDatoException("Documento no Existe.");
            }
        } catch (validarDatoException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new validarDatoException("ERROR EN TRANSACCION PARA VERIFICAR.");
        }
        return vReturn;
    }

    public String getProcesoExpediente(String pcoProceso) {
        String coRespuesta = "0";
        StringBuilder retval = new StringBuilder();
        String vretval = null;
        try {
            ProcesoExpBean proceso = this.mesaPartesDao.getProcesoExpediente(pcoProceso);
            retval.append("{\"coRespuesta\":\"");
            if (proceso != null) {
                Writer strWriter = new StringWriter();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(strWriter, proceso.getDeAsunto());
                vretval = strWriter.toString();
                coRespuesta = "1";
                retval.append(coRespuesta);
                retval.append("\",");
                retval.append("\"nuDiaAte\":\"");
                retval.append(proceso.getNuDiasPlazo());
                retval.append("\",");
                retval.append("\"deAsunto\":");
                retval.append(vretval);
            } else {
                retval.append(coRespuesta);
                retval.append("\"");
            }
            retval.append("}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retval.toString();
    }

    public List<DestinatarioDocumentoEmiBean> getLsDestinoNewDocExtMesaPartes() {
        List<DestinatarioDocumentoEmiBean> list = new ArrayList<DestinatarioDocumentoEmiBean>();

        try {
            DestinatarioDocumentoEmiBean destino = this.mesaPartesDao.getEmpleadoDestinoDocExtMp(null);
            if (destino != null) {
                destino.setDeTramite("ORIGINAL");
                destino.setCoTramite("0");
                destino.setCoPrioridad("1");
                destino.setAccionBD("INS");
                destino.setCoTipoDestino("01");
                list.add(destino);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<RemitenteBean> getAllDependencias() {
        List<RemitenteBean> list = null;
        try {
            list = this.mesaPartesDao.getAllDependencias();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<RequisitoBean> getAllRequisitoExpediente(String nuAnnExp, String nuSecExp, String codProceso) {
        List<RequisitoBean> list = null;
        try {
            list = this.mesaPartesDao.getAllRequisitoExpediente(nuAnnExp, nuSecExp, codProceso);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public String guardarReqExpDocExtRec(List<RequisitoBean> lRequisito, String nuAnnExp, String nuSecExp, String coProceso, String coUsuario) throws Exception {
        String vReturn = "NO_OK";
        try {
            for (RequisitoBean req : lRequisito) {
                vReturn = this.mesaPartesDao.updReqExpedienteDocExtRec(req, nuAnnExp, nuSecExp, coProceso, coUsuario);
                if (!"OK".equals(vReturn)) {
                    throw new validarDatoException("Error actualizando Requisito.");
                }
            }
        } catch (validarDatoException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new validarDatoException("ERROR EN TRANSACCION.");
        }
        return vReturn;
    }

    public String getEsDocEmi(String nuAnn, String nuEmi) {
        String esDocEmi = null;
        try {
            DocumentoExtRecepBean doc = this.mesaPartesDao.getDocumentoExtRecBasic(nuAnn, nuEmi);
            if (doc != null && doc.getCoEsDocEmiMp() != null && doc.getCoEsDocEmiMp().trim().length() > 0) {
                esDocEmi = doc.getCoEsDocEmiMp();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return esDocEmi;
    }

    public String getPkExpDocExtOrigen(String pkEmi) {
        String pkExp = null;
        try {
            String pNuAnn = pkEmi.substring(0, 4);
            String pNuEmi = pkEmi.substring(4, 14);

            pkExp = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pkExp;
    }

    public List<RequisitoBean> getAllRequisitoExpediente(String nuAnnExp, String nuSecExp) {
        List<RequisitoBean> list = null;
        try {
            list = this.mesaPartesDao.getAllRequisitoExpediente(nuAnnExp, nuSecExp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<DocumentoBean> getListaReporteVoucher(DocumentoBean documento) {
        return this.mesaPartesDao.getListaReporteBusquedaVoucher(documento);
    }

    //hoy jaime
    public ReporteBean getGenerarReporteVoucher(Map parametros, DocumentoBean documento) throws Exception {
        ReporteBean objReporte = new ReporteBean();
        String eserror = "1";
        String coRespuesta = "0"; // inicializador de respuesta
        String deRespuesta = "";
        String prutaReporte = "";
        String deNoDoc = "";
        String nombreArchivo = "";
        String coReporte = "";

        try {
            int tipoArchivo = 0;

            String fecha = Utility.getInstancia().getStringFechaYYYYMMDDHHmm(new Date());

            List<DocumentoBean> lista = null;
            try {
                lista = getListaReporteVoucher(documento);
                eserror = "0";
            } catch (Exception ex) {
                eserror = "1";
                deRespuesta = ex.getMessage();
            }
            if (lista.size() == 0) {
                coRespuesta = "1";
                deRespuesta = "No existe información para generar reporte.";
                throw new Exception(deRespuesta);
            }

            coReporte = "TDR15";
            String extensionArch = ".pdf";
            tipoArchivo = 2;

            String rutaJasper = documento.getRutaReporteJasper() + "/" + coReporte + ".jasper";

            documento.setRutaReporteJasper(rutaJasper);

            nombreArchivo = Utilidades.generateRandomNumber(10);
            prutaReporte = "VOUCHER_" + nombreArchivo + extensionArch;
            deNoDoc = "temp|VOUCHER_" + fecha + extensionArch;

            Utilidades util = new Utilidades();
            byte[] archivo = util.GenerarReporteLista(documento.getRutaReporteJasper(), lista, coReporte, parametros, tipoArchivo);

            if (archivo == null) {
                eserror = "1";
                coRespuesta = "1";
                deRespuesta = "No se genercorrectamente el archivo.";
                throw new Exception("No se genercorrectamente el archivo.");
            }

            String rutaBase = this.applicationProperties.getRutaTemporal() + "/" + prutaReporte;

            File tmpFile = new File(rutaBase);
            FileOutputStream fos = new FileOutputStream(tmpFile);
            fos.write(archivo);
            fos.flush();
            fos.close();
            String rutaUrl = "reporte?coReporte=";
            prutaReporte = rutaUrl + prutaReporte;

            return objReporte;
        } catch (JRException ex) {
            Logger.getLogger(DocumentoExtRecepServiceImp.class.getName()).log(Level.SEVERE, null, (Throwable) ex);
            eserror = "1";
            return objReporte;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(DocumentoExtRecepServiceImp.class.getName()).log(Level.SEVERE, null, ex);
            eserror = "1";
            return objReporte;
        } catch (IOException ex) {
            Logger.getLogger(DocumentoExtRecepServiceImp.class.getName()).log(Level.SEVERE, null, ex);
            eserror = "1";
            return objReporte;
        } finally {
            Exception exception = null;
            objReporte.setcoRespuesta(coRespuesta);
            objReporte.setdeRespuesta(deRespuesta);
            objReporte.setnoUrl(prutaReporte);
            objReporte.setnoDoc(deNoDoc);
        }
    }

    public ReporteBean getGenerarReporte(BuscarDocumentoExtRecepBean buscarDocumentoExtRecepBean, Map parametros) throws Exception {
        ReporteBean objReporte = new ReporteBean();
        String eserror = "1";
        String coRespuesta = "0";
        String deRespuesta = "";
        String prutaReporte = "";
        String deNoDoc = "";
        String nombreArchivo = "";
        String coReporte = "";

        try {
            String extensionArch;
            int tipoArchivo = 0;

            String fecha = Utility.getInstancia().getStringFechaYYYYMMDDHHmm(new Date());

            List<DocumentoExtRecepBean> lista = null;
            try {
                lista = getListaReporte(buscarDocumentoExtRecepBean);
                eserror = "0";
            } catch (Exception ex) {
                eserror = "1";
                deRespuesta = ex.getMessage();
            }
            if (lista.size() == 0) {
                coRespuesta = "1";
                deRespuesta = "No existe información para generar reporte.";
                throw new Exception(deRespuesta);
            }
            if (buscarDocumentoExtRecepBean.getFormatoReporte().equalsIgnoreCase("PDF")) {
                coReporte = "TDR33";
                extensionArch = ".pdf";
                tipoArchivo = 2;
            } else {
                coReporte = "TDR33_XLS";
                extensionArch = ".xls";
                tipoArchivo = 1;
            }
            String rutaJasper = buscarDocumentoExtRecepBean.getRutaReporteJasper() + "/" + coReporte + ".jasper";

            buscarDocumentoExtRecepBean.setRutaReporteJasper(rutaJasper);

            nombreArchivo = Utilidades.generateRandomNumber(10);
            prutaReporte = "RECEPDOCEXT_" + nombreArchivo + extensionArch;
            deNoDoc = "temp|RECEPDOCEXT_" + fecha + extensionArch;

            Utilidades util = new Utilidades();
            byte[] archivo = util.GenerarReporteLista(buscarDocumentoExtRecepBean.getRutaReporteJasper(), lista, coReporte, parametros, tipoArchivo);

            if (archivo == null) {
                eserror = "1";
                coRespuesta = "1";
                deRespuesta = "No se genercorrectamente el archivo.";
                throw new Exception("No se genercorrectamente el archivo.");
            }

            String rutaBase = this.applicationProperties.getRutaTemporal() + "/" + prutaReporte;

            File tmpFile = new File(rutaBase);
            FileOutputStream fos = new FileOutputStream(tmpFile);
            fos.write(archivo);
            fos.flush();
            fos.close();
            String rutaUrl = "reporte?coReporte=";
            prutaReporte = rutaUrl + prutaReporte;

            return objReporte;
        } catch (JRException ex) {
            Logger.getLogger(DocumentoExtRecepServiceImp.class.getName()).log(Level.SEVERE, null, (Throwable) ex);
            eserror = "1";
            return objReporte;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DocumentoExtRecepServiceImp.class.getName()).log(Level.SEVERE, null, ex);
            eserror = "1";
            return objReporte;
        } catch (IOException ex) {
            Logger.getLogger(DocumentoExtRecepServiceImp.class.getName()).log(Level.SEVERE, null, ex);
            eserror = "1";
            return objReporte;
        } finally {
            Exception exception = null;
            objReporte.setcoRespuesta(coRespuesta);
            objReporte.setdeRespuesta(deRespuesta);
            objReporte.setnoUrl(prutaReporte);
            objReporte.setnoDoc(deNoDoc);
        }

    }

    public List<DocumentoExtRecepBean> getListaReporte(BuscarDocumentoExtRecepBean buscarDocumentoExtRecepBean) {
        try {
            //COD_PERMISO_DOC_MP_OFICINA
            PermisoUsuarioBean permisoUsuario = new PermisoUsuarioBean();
            permisoUsuario.setCodOpc(Constantes.COD_PERMISO_DOC_MP_OFICINA);
            permisoUsuario.setCempCodEmp(buscarDocumentoExtRecepBean.getCoEmpleado()); // EL CODIGO DE SESION
            permisoUsuario.setCodAplica(Constantes.COD_SISTEMA_SGD);
            if (permisoService.obtenerPermiso(permisoUsuario)) {
                buscarDocumentoExtRecepBean.setTiAcceso("2");//toda la bandeja de oficina
            }
            return this.mesaPartesDao.getListaReporteBusqueda(buscarDocumentoExtRecepBean);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;

    }

    public List<CiudadanoBean> getLstCiudadanos(String sDescCiudadano) {
        List<CiudadanoBean> list = null;
        try {
            list = this.commonQueryDao.getLstCiudadanos(sDescCiudadano);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    @Override
    public String VerificaToObservado(DocumentoExtRecepBean documentoExtRecepBean, Usuario usuario) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String changeToObservado(DocumentoExtRecepBean documentoExtRecepBean, Usuario usuario) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}
