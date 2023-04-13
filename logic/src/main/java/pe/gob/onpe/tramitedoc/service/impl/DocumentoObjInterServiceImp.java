package pe.gob.onpe.tramitedoc.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.libreria.exception.validarDatoException;
import pe.gob.onpe.tramitedoc.bean.DocumentoDatoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoObjBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoObjBlock;
import pe.gob.onpe.tramitedoc.bean.DocumentoVerBean;
import pe.gob.onpe.tramitedoc.bean.ReporteBean;
import pe.gob.onpe.tramitedoc.bean.UsuarioConfigBean;
import pe.gob.onpe.tramitedoc.dao.AuditoriaMovimientoDocDao;
import pe.gob.onpe.tramitedoc.dao.DocumentoObjDao;
import pe.gob.onpe.tramitedoc.service.AnexoDocumentoService;
import pe.gob.onpe.tramitedoc.service.DocumentoObjInterService;
import pe.gob.onpe.tramitedoc.service.DocumentoVoBoService;
import pe.gob.onpe.tramitedoc.service.DocumentoXmlService;
import pe.gob.onpe.tramitedoc.service.EmiDocumentoInteroperabilidadService;
import pe.gob.onpe.tramitedoc.service.EmiDocumentoPersonalService;
import pe.gob.onpe.tramitedoc.util.ArchivoTemporal;
import pe.gob.onpe.tramitedoc.util.LsAleatorioViewDoc;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;


@Service("documentoObjInterService")
public class DocumentoObjInterServiceImp   implements DocumentoObjInterService
{
  private static Logger logger = Logger.getLogger("SGDDocObjService");
  
  @Autowired
  private DocumentoObjDao documentoObjDao;
  
  @Autowired
  private EmiDocumentoInteroperabilidadService emiDocumentoAdmService;
  
  @Autowired
  private EmiDocumentoPersonalService emiDocumentoPersonalService;
  
  @Autowired
  private AuditoriaMovimientoDocDao auditoriaMovimientoDocDao;
  
  @Autowired
  private AnexoDocumentoService anexoDocumentoService;
  
  @Autowired
  private DocumentoVoBoService documentoVoBoService;
  
  @Autowired
  private ApplicationProperties applicationProperties;
  
  @Autowired
  private DocumentoXmlService DocumentoXmlService;

  
  public DocumentoDatoBean getDatosDoc(String pnuAnn, String pnuEmi) {
    DocumentoDatoBean docDatoBean = null;
    try {
      docDatoBean = this.documentoObjDao.getDatosDoc(pnuAnn, pnuEmi);
      if (docDatoBean != null) {
        docDatoBean.setNuSecFirma(Utilidades.generateRandomNumber(8));
      }
    } catch (Exception e) {
      docDatoBean = null;
    } 
    return docDatoBean;
  }


  
  public DocumentoObjBean leerDocumento(String pnuAnn, String pnuEmi, String ptiCap) {
    DocumentoObjBean docObjBean = null;
    docObjBean = this.documentoObjDao.leerDocumento(pnuAnn, pnuEmi, ptiCap);
    
    if (docObjBean != null && docObjBean.getNombreArchivo() != null && docObjBean.getNombreArchivo().length() > 0) {
      int pos = docObjBean.getNombreArchivo().lastIndexOf(".");
      if (pos > 0) {
        docObjBean.setTipoDoc(docObjBean.getNombreArchivo().substring(pos + 1).toLowerCase());
      } else {
        docObjBean.setTipoDoc("octet-stream");
      } 
    } 
    
    return docObjBean;
  }


  
  public DocumentoObjBean leerDocumentoAnexo(String pnuAnn, String pnuEmi, String pnuAne) {
    DocumentoObjBean docObjBean = null;
    docObjBean = this.documentoObjDao.leerDocumentoAnexo(pnuAnn, pnuEmi, pnuAne);
    return docObjBean;
  }


  
  public DocumentoObjBean getNombreArchivo(String pnuAnn, String pnuEmi, String ptiCap) {
    DocumentoObjBean docObjBean = null;
    docObjBean = this.documentoObjDao.getNombreArchivo(pnuAnn, pnuEmi, ptiCap);
    
    if (docObjBean != null && docObjBean.getNombreArchivo().length() > 0) {
      int pos = docObjBean.getNombreArchivo().lastIndexOf(".");
      if (pos > 0) {
        docObjBean.setTipoDoc(docObjBean.getNombreArchivo().substring(pos + 1).toLowerCase());
      } else {
        docObjBean.setTipoDoc("archivo");
      } 
    } 
    return docObjBean;
  }


  
  public DocumentoVerBean getNombreDoc(String pnuAnn, String pnuEmi, String ptiOpe, Usuario usuario) {
    DocumentoVerBean docVerBean = new DocumentoVerBean();
    
    docVerBean.setNuAnn(pnuAnn);
    docVerBean.setNuEmi(pnuEmi);
    docVerBean.setInDoc(false);
    
    DocumentoDatoBean docDatoBean = null;
    DocumentoObjBean docObjBean = null;
    
    docDatoBean = getDatosDoc(pnuAnn, pnuEmi);
    if (docDatoBean != null) {
      docObjBean = getNombreArchivo(pnuAnn, pnuEmi, docDatoBean.getTiCap());
      if (docObjBean != null && docObjBean.getNuTamano() > 0) {
        docVerBean.setInDoc(true);
        docVerBean.setDeMensaje("OK");
        
        this.auditoriaMovimientoDocDao.audiVisualizaDocumento(docObjBean, usuario);
      } else {
        docVerBean.setInDoc(false);
        docVerBean.setDeMensaje("Documento no se encuentra cargado en Repositorio");
      } 
    } 
    
    if (docVerBean.isInDoc()) {

      
      String nombreDoc, urlDoc = "documento?accion=abrirDoc&nuAnn=" + docDatoBean.getNuAnn() + "&nuEmi=" + docDatoBean.getNuEmi() + "&tiCap=" + docDatoBean.getTiCap() + "&nroView=" + LsAleatorioViewDoc.getInstancia().add();












      
      if (docDatoBean.getTiEmi().equals("06")) {
        
        if (ptiOpe.equals("0")) {
          if (docDatoBean.getNumeroDoc() != null && docDatoBean.getNumeroDoc().trim().length() > 0) {
            nombreDoc = "TEMP|" + docDatoBean.getTipoDoc() + "-" + docDatoBean.getNumeroDoc() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + "." + docObjBean.getTipoDoc();
          } else {
            nombreDoc = "TEMP|" + docDatoBean.getTipoDoc() + "-" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + "." + docObjBean.getTipoDoc();
          } 
        } else if (ptiOpe.equals("1") || ptiOpe.equals("2")) {
          nombreDoc = "TEMP|" + docDatoBean.getTipoDoc() + "-I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + "." + docObjBean.getTipoDoc();
        } else if (ptiOpe.equals("3")) {
          nombreDoc = docDatoBean.getNuAnn() + "|" + docDatoBean.getTipoDoc() + "-I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + ".docx";
        } else if (ptiOpe.equals("4")) {
          nombreDoc = docDatoBean.getNuAnn() + "|" + docDatoBean.getTipoDoc() + "-I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + ".pdf";
        } else if (ptiOpe.equals("5")) {
          nombreDoc = docDatoBean.getNuAnn() + "|" + docDatoBean.getTipoDoc() + "-I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + "." + docObjBean.getTipoDoc();
        } else {
          nombreDoc = "TEMP|" + docDatoBean.getTipoDoc() + "-I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + "." + docObjBean.getTipoDoc();
        }
      
      } else {
        
        nombreDoc = "TEMP|I" + docDatoBean.getNuCorEmi() + "." + docObjBean.getTipoDoc();
      } 
      
      docVerBean.setUrlDocumento(urlDoc);
      docVerBean.setNoDocumento(nombreDoc);
    } 
    return docVerBean;
  }

  
  public DocumentoVerBean getNombreGeneraDocx(String pnuAnn, String pnuEmi, String ptiOpe) {
    DocumentoVerBean docVerBean = new DocumentoVerBean();
    
    docVerBean.setNuAnn(pnuAnn);
    docVerBean.setNuEmi(pnuEmi);
    docVerBean.setInDoc(false);
    
    DocumentoDatoBean docDatoBean = null;
    
    docDatoBean = getDatosDoc(pnuAnn, pnuEmi);
    
    if (docDatoBean != null)
    {
      if (docDatoBean.getCoDoc().equals("232") || docDatoBean.getCoDoc().equals("304")) {
        docVerBean.setInDoc(false);
        docVerBean.setDeMensaje("Tipo de Documento no requiere Generar Formato");
      } else {
        docVerBean.setInDoc(true);
      } 
    }
    
    if (docVerBean.isInDoc()) {

      
      String urlDoc = "documento?accion=creaDocx&nuAnn=" + docDatoBean.getNuAnn() + "&nuEmi=" + docDatoBean.getNuEmi() + "&tiCap=" + docDatoBean.getTiCap();
      String nombreDoc = "";


      
      if (docDatoBean.getTiEmi().equals("06")) {
        nombreDoc = docDatoBean.getNuAnn() + "|" + docDatoBean.getTipoDoc() + "-I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + ".docx";
      }
      docVerBean.setUrlDocumento(urlDoc);
      docVerBean.setNoDocumento(nombreDoc);
      docVerBean.setInDoc(true);
      docVerBean.setDeMensaje("OK");
    } 
    return docVerBean;
  }


  
  public DocumentoObjBean getNombreArchivoAnexo(String pnuAnn, String pnuEmi, String pnuAnexo) {
    DocumentoObjBean docObjBean = null;
    docObjBean = this.documentoObjDao.getNombreArchivoAnexo(pnuAnn, pnuEmi, pnuAnexo);
    
    if (docObjBean != null && docObjBean.getNombreArchivo().length() > 0) {
      int pos = docObjBean.getNombreArchivo().lastIndexOf("\\");
      if (pos > 0) {
        docObjBean.setNombreArchivo(docObjBean.getNombreArchivo().substring(pos + 1));
      }
      
      pos = docObjBean.getNombreArchivo().lastIndexOf(".");
      if (pos > 0) {
        docObjBean.setTipoDoc(docObjBean.getNombreArchivo().substring(pos + 1).toLowerCase());
      } else {
        docObjBean.setTipoDoc("archivo");
      } 
    } 
    return docObjBean;
  }

  
  public DocumentoVerBean getNombreDocAnexo(String pnuAnn, String pnuEmi, String pnuAnexo) {
    DocumentoVerBean docVerBean = new DocumentoVerBean();
    
    docVerBean.setNuAnn(pnuAnn);
    docVerBean.setNuEmi(pnuEmi);
    docVerBean.setNuAne(pnuAnexo);
    docVerBean.setInDoc(false);
    
    DocumentoObjBean docObjBean = null;
    
    docObjBean = getNombreArchivoAnexo(pnuAnn, pnuEmi, pnuAnexo);
    if (docObjBean != null && docObjBean.getNuTamano() > 0) {
      docVerBean.setInDoc(true);
    }
    
    if (docVerBean.isInDoc()) {

      
      docVerBean.setDeMensaje("OK");
      String urlDoc = "documento?accion=abrirAnexo&nuAnn=" + docObjBean.getNuAnn() + "&nuEmi=" + docObjBean.getNuEmi() + "&nuAne=" + docObjBean.getNuAne();

      
      String nombreDoc = "TEMP|" + docObjBean.getNombreArchivo();
      
      docVerBean.setUrlDocumento(urlDoc);
      docVerBean.setNoDocumento(nombreDoc);
    } 
    return docVerBean;
  }

  
  public DocumentoVerBean getNombreCargaDoc(String pnuAnn, String pnuEmi, String ptiOpe) {
    DocumentoVerBean docVerBean = new DocumentoVerBean();
    
    docVerBean.setNuAnn(pnuAnn);
    docVerBean.setNuEmi(pnuEmi);
    docVerBean.setInDoc(false);
    
    DocumentoDatoBean docDatoBean = null;
    
    docDatoBean = getDatosDoc(pnuAnn, pnuEmi);
    if (docDatoBean != null) {
      docVerBean.setInDoc(true);
      docVerBean.setNuSecFirma(docDatoBean.getNuSecFirma());
      docVerBean.setCoDocEmi(docDatoBean.getCoDoc());
      docVerBean.setTiCap(docDatoBean.getTiCap());
      if (docDatoBean.getEsDocEmi().equals("7") && ptiOpe.equals("3")) {
        ptiOpe = "4";
      }
    } else {
      docVerBean.setDeMensaje("Error en datos del documento");
    } 









    
    if (docVerBean.isInDoc()) {

      
      String urlDoc = "documento?accion=cargaDoc&nuAnn=" + docDatoBean.getNuAnn() + "&nuEmi=" + docDatoBean.getNuEmi() + "&nuSec=" + docDatoBean.getNuSecFirma();
      String nombreDoc = null;


      
      if (docDatoBean.getTiEmi().equals("06")) {
        
        String vnumDoc = "";
        
        if (ptiOpe.equals("3")) {
          vnumDoc = "I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + ".docx";
          nombreDoc = docDatoBean.getNuAnn() + "|" + docDatoBean.getTipoDoc() + "-" + vnumDoc;
        } else if (ptiOpe.equals("4")) {
          vnumDoc = "I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + ".pdf";
          nombreDoc = docDatoBean.getNuAnn() + "|" + docDatoBean.getTipoDoc() + "-" + vnumDoc;
        } else {
          nombreDoc = null;
        } 
        docVerBean.setUrlDocumento(urlDoc);
        docVerBean.setNoDocumento(nombreDoc);
        docVerBean.setDeMensaje("OK");
      } else {
        docVerBean.setDeMensaje("Operacino permitida");
      } 
    } 


    
    return docVerBean;
  }

  
  public DocumentoVerBean getNombreFirmaDoc(String pnuAnn, String pnuEmi, String ptiOpe, UsuarioConfigBean usuarioConfig, String rutaReporteBase) {
    DocumentoVerBean docVerBean = new DocumentoVerBean();
    String vin_pref = "1";
    String vin_TSA = "0";
    
    docVerBean.setNuAnn(pnuAnn);
    docVerBean.setNuEmi(pnuEmi);
    docVerBean.setInDoc(false);
    
    DocumentoDatoBean docDatoBean = null;
    
    if (usuarioConfig.getSwFirma() != null) {
      docVerBean.setNoPrefijo("[NF]");
      if (usuarioConfig.getSwFirma().equals("R")) {
        docVerBean.setNoPrefijo("[R]");
      }
      docDatoBean = getDatosDoc(pnuAnn, pnuEmi);
    } 
    
    if (docDatoBean != null) {
      if (docDatoBean.getTiEmi().equals("06") && docDatoBean.getEsDocEmi().equals("7") && ptiOpe.equals("5")) {
        
        String coTipDoc = docDatoBean.getCoDoc();
        String vformatoDoc = "NO_OK";
        if (coTipDoc.equals("232") || coTipDoc.equals("304")) {
          vformatoDoc = "OK";
          vin_pref = "N";
        } else {
          vformatoDoc = this.emiDocumentoAdmService.getVerificaPdfDoc(docDatoBean.getNuAnn(), docDatoBean.getNuEmi(), docDatoBean.getTiCap());
          
          String vin_firma = this.emiDocumentoAdmService.getInFirmaDocAdm(coTipDoc);
          if (vin_firma.equals("1")) {
            vin_pref = "N";
          } else if (vin_firma.equals("2")) {
            vin_pref = "1";
          } 
        } 









        
        if (vformatoDoc.equals("OK")) {
          docVerBean.setDeMensaje("OK");
          docVerBean.setInDoc(true);
          docVerBean.setNuSecFirma(docDatoBean.getNuSecFirma());
          docVerBean.setFeFirma(docDatoBean.getFechaActual());
          docVerBean.setCoDocEmi(docDatoBean.getCoDoc());
          if (usuarioConfig.getInFirma() != null && usuarioConfig.getInFirma().equals("1")) {
            docVerBean.setInTipoFirma("N");
          }
          else if (usuarioConfig.getInFirma().equals("0")) {
            String vinDoc = this.documentoObjDao.getInFirmaDoc(docDatoBean.getCoDepEmi(), docDatoBean.getCoDoc());
            docVerBean.setInTipoFirma(vinDoc);
          } else {
            docVerBean.setInTipoFirma("F");
          } 


          
          if (docDatoBean.getNumeroDoc() == null || docDatoBean.getNumeroDoc().length() == 0) {
            
            if (docDatoBean.getTiEmi().equals("06")) {
              String vnuDoc = this.emiDocumentoAdmService.getNumeroDocSiguienteAdm(docDatoBean.getNuAnn(), docDatoBean.getCoDepEmi(), docDatoBean.getCoDoc());
              docVerBean.setNumeroDoc(vnuDoc);
              docDatoBean.setNumeroDoc(vnuDoc);
            
            }

          
          }
          else {

            
            docVerBean.setNumeroDoc(docDatoBean.getNumeroDoc());
          } 
        } else {
          docVerBean.setDeMensaje(vformatoDoc);
          docVerBean.setInDoc(false);
        } 
      } 
    } else {
      docVerBean.setDeMensaje("Error en datos del documento");
    } 








    
    if (docVerBean.isInDoc()) {

      
      String urlDoc = "documento?accion=abrirDoc&nuAnn=" + docDatoBean.getNuAnn() + "&nuEmi=" + docDatoBean.getNuEmi() + "&tiCap=" + docDatoBean.getTiCap() + "&nroView=" + LsAleatorioViewDoc.getInstancia().add();
      
      if (docVerBean.getCoDocEmi().equals("232") || docVerBean.getCoDocEmi().equals("304")) {


        
        String vgnum_reg = "R" + docDatoBean.getNuCorEmi() + "." + docDatoBean.getNuSecFirma();
        String cadena = generarFormatoSiglasFirma(docDatoBean.getTipoDoc(), docDatoBean.getNumeroDoc(), docDatoBean.getNuAnn(), this.applicationProperties.getSiglaInstitucion(), docDatoBean.getSiglasDoc());
        String vgnumDoc = cadena.replace(" ", "") + "$" + docDatoBean.getDeLugar() + "$" + docDatoBean.getFechaFirma().replace("-", "$");
        String gnombreDoc = "TEMP|" + vgnum_reg + "$" + vgnumDoc + "$" + vin_pref + ".pdf";

        
        List<DocumentoDatoBean> listaSubReport1 = this.documentoObjDao.CargarSubReporte1(docDatoBean.getNuAnn(), docDatoBean.getNuEmi());
        
        List<DocumentoDatoBean> listaSubReport2 = this.documentoObjDao.CargarSubReporte2(docDatoBean.getNuAnn(), docDatoBean.getNuEmi());



        
        String logo = this.applicationProperties.getLogoReporteB64();
        ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(logo));
        
        Map<String, Object> parametros = new HashMap<String, Object>();
        parametros.put("P_NU_ANN", docDatoBean.getNuAnn());
        parametros.put("P_NU_EMI", docDatoBean.getNuEmi());
        parametros.put("P_NU_DOC", docDatoBean.getNumeroDoc());
        parametros.put("P_FECHA", docVerBean.getFeFirma());
        parametros.put("P_LOGO_DIR", (bis.available() == 0) ? null : bis);
        parametros.put("datosSubRep1", listaSubReport1);
        parametros.put("datosSubRep2", listaSubReport2);
        parametros.put("SUB_REPORT_DIR1", rutaReporteBase);
        parametros.put("SUB_REPORT_DIR2", rutaReporteBase);
        
        try {
          ReporteBean objReporte = getGenerarReporte("PDF", rutaReporteBase, parametros, gnombreDoc, docDatoBean.getNuAnn(), docDatoBean.getNuEmi());
          if (bis != null) bis.close(); 
          urlDoc = objReporte.noUrl;
        } catch (Exception e) {
          docVerBean.setInDoc(false);
          docVerBean.setDeMensaje(e.getMessage());
        } 
      } 
      
      String nombreDoc = null;
      String nombreFirma = null;


      
      if (docDatoBean.getTiEmi().equals("06")) {
        
        String vnumDoc = "";
        String vnum_reg = "";
        
        if (ptiOpe.equals("5")) {







          
          if (usuarioConfig.getSwFirma() != null && usuarioConfig.getSwFirma().equals("O"))
          {
            
            vnum_reg = "R" + docDatoBean.getNuCorEmi() + "." + docDatoBean.getNuSecFirma();


            
            String cadena = generarFormatoSiglasFirma(docDatoBean.getTipoDoc(), docDatoBean.getNumeroDoc(), docDatoBean.getNuAnn(), this.applicationProperties.getSiglaInstitucion(), docDatoBean.getSiglasDoc());
            
            vnumDoc = cadena.replace(" ", "") + "$" + docDatoBean.getDeLugar() + "$" + docDatoBean.getFechaFirma().replace("-", "$");


            
            nombreDoc = "TEMP|" + vnum_reg + "$" + vnumDoc + "$" + vin_pref + ".pdf";
            nombreFirma = "TEMP|" + vnum_reg + "$" + vnumDoc + "$" + vin_pref;


          
          }


        
        }
        else if (ptiOpe.equals("6")) {
          nombreDoc = null;
        } else {
          nombreDoc = null;
        } 
      } 
      docVerBean.setUrlDocumento(urlDoc);
      docVerBean.setNoDocumento(nombreDoc);
      docVerBean.setNoFirma(nombreFirma);
    } 
    return docVerBean;
  }

  
  public DocumentoVerBean getNombreCargaFirmaDoc(String pnuAnn, String pnuEmi, String ptiOpe) {
    DocumentoVerBean docVerBean = new DocumentoVerBean();
    
    docVerBean.setNuAnn(pnuAnn);
    docVerBean.setNuEmi(pnuEmi);
    docVerBean.setInDoc(false);
    
    DocumentoDatoBean docDatoBean = null;
    
    docDatoBean = getDatosDoc(pnuAnn, pnuEmi);
    if (docDatoBean != null) {
      if (docDatoBean.getTiEmi().equals("06") && docDatoBean.getEsDocEmi().equals("7") && ptiOpe.equals("6")) {
        docVerBean.setDeMensaje("OK");
        docVerBean.setInDoc(true);
        docVerBean.setNuSecFirma(docDatoBean.getNuSecFirma());
        docVerBean.setFeFirma(docDatoBean.getFechaActual());
        docVerBean.setCoDocEmi(docDatoBean.getCoDoc());
      } else {
        docVerBean.setDeMensaje("Estado del Documento a cambiado");
      } 
    } else {
      docVerBean.setDeMensaje("Error en datos del documento");
    } 
    
    if (docVerBean.isInDoc()) {

      
      String urlDoc = "documento?accion=cargaDoc&nuAnn=" + docDatoBean.getNuAnn() + "&nuEmi=" + docDatoBean.getNuEmi() + "&nuSec=" + docDatoBean.getNuSecFirma();
      
      String nombreDoc = null;

      
      if (docDatoBean.getTiEmi().equals("06"))
      {
        if (ptiOpe.equals("6")) {
          nombreDoc = "OK";
        } else {
          nombreDoc = null;
        } 
      }
      docVerBean.setUrlDocumento(urlDoc);
      docVerBean.setNoDocumento(nombreDoc);
    } 
    return docVerBean;
  }

  
  public String getFormatoDoc(String pnuAnn, String pnuEmi, String ptiCap) {
    String vReturn = "ERR";
    try {
      vReturn = this.emiDocumentoAdmService.getFormatoDoc(pnuAnn, pnuEmi, ptiCap);
    } catch (Exception e) {
      vReturn = "ERR";
    } 
    
    return vReturn;
  }


  
  public DocumentoVerBean getNombreDocEmi(String pnuAnn, String pnuEmi, String ptiOpe, Usuario usuario, String rutaReporteBase) {
    DocumentoVerBean docVerBean = new DocumentoVerBean();
    
    docVerBean.setNuAnn(pnuAnn);
    docVerBean.setNuEmi(pnuEmi);
    docVerBean.setInDoc(false);
    
    DocumentoDatoBean docDatoBean = null;
    DocumentoObjBean docObjBean = null;
    
    docDatoBean = getDatosDoc(pnuAnn, pnuEmi);
    if (docDatoBean != null) {
      docVerBean.setNuSecFirma(docDatoBean.getNuSecFirma());
      docVerBean.setNumeroDoc(docDatoBean.getNumeroDoc());
      docVerBean.setFeFirma(docDatoBean.getFechaActual());
      docVerBean.setCoDocEmi(docDatoBean.getCoDoc());
      if (docVerBean.getCoDocEmi().equals("232") || docVerBean.getCoDocEmi().equals("304")) {
        docVerBean.setInDoc(true);
        docVerBean.setDeMensaje("OK");
        docObjBean = new DocumentoObjBean();
        docObjBean.setTipoDoc("pdf");
        if (docDatoBean.getNumeroDoc() == null) {
          docDatoBean.setNumeroDoc("      ");
          docVerBean.setNumeroDoc("      ");
        } 
      } else {
        docObjBean = getNombreArchivo(pnuAnn, pnuEmi, docDatoBean.getTiCap());
        if (docObjBean != null && docObjBean.getNuTamano() > 0) {
          docVerBean.setInDoc(true);
          docVerBean.setDeMensaje("OK");
          
          this.auditoriaMovimientoDocDao.audiVisualizaDocumento(docObjBean, usuario);
        } else {
          
          docVerBean.setInDoc(false);
          docVerBean.setDeMensaje("Documento no se encuentra cargado en Repositorio");
        } 
      } 
    } 
    
    if (docVerBean.isInDoc()) {

      
      String nombreDoc, urlDoc = "documento?accion=abrirDoc&nuAnn=" + docDatoBean.getNuAnn() + "&nuEmi=" + docDatoBean.getNuEmi() + "&tiCap=" + docDatoBean.getTiCap() + "&nroView=" + LsAleatorioViewDoc.getInstancia().add();
      if (docVerBean.getCoDocEmi().equals("232") || docVerBean.getCoDocEmi().equals("304")) {


        
        nombreDoc = "TEMP|R" + docDatoBean.getNuCorEmi() + "." + docObjBean.getTipoDoc();

        
        List<DocumentoDatoBean> listaSubReport1 = this.documentoObjDao.CargarSubReporte1(docDatoBean.getNuAnn(), docDatoBean.getNuEmi());
        
        List<DocumentoDatoBean> listaSubReport2 = this.documentoObjDao.CargarSubReporte2(docDatoBean.getNuAnn(), docDatoBean.getNuEmi());


        
        String logo = this.applicationProperties.getLogoReporteB64();
        ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(logo));
        
        Map<String, Object> parametros = new HashMap<String, Object>();
        parametros.put("P_NU_ANN", docDatoBean.getNuAnn());
        parametros.put("P_NU_EMI", docDatoBean.getNuEmi());
        parametros.put("P_NU_DOC", docDatoBean.getNumeroDoc());
        parametros.put("P_FECHA", docDatoBean.getFechaActual());
        parametros.put("P_LOGO_DIR", (bis.available() == 0) ? null : bis);
        parametros.put("datosSubRep1", listaSubReport1);
        parametros.put("datosSubRep2", listaSubReport2);
        parametros.put("SUB_REPORT_DIR1", rutaReporteBase);
        parametros.put("SUB_REPORT_DIR2", rutaReporteBase);
        
        try {
          ReporteBean objReporte = getGenerarReporte("PDF", rutaReporteBase, parametros, nombreDoc, docDatoBean.getNuAnn(), docDatoBean.getNuEmi());
          if (bis != null) bis.close(); 
          urlDoc = objReporte.noUrl;
        } catch (Exception e) {
          docVerBean.setInDoc(false);
          docVerBean.setDeMensaje(e.getMessage());
        } 
      } 









      
      if (docDatoBean.getTiEmi().equals("06")) {
        
        if (ptiOpe.equals("3")) {
          nombreDoc = docDatoBean.getNuAnn() + "|" + docDatoBean.getTipoDoc() + "-I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + ".docx";
        } else if (ptiOpe.equals("4")) {
          nombreDoc = docDatoBean.getNuAnn() + "|" + docDatoBean.getTipoDoc() + "-I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + ".pdf";
        } else if (ptiOpe.equals("5")) {
          nombreDoc = docDatoBean.getNuAnn() + "|" + docDatoBean.getTipoDoc() + "-I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + "." + docObjBean.getTipoDoc();
        } else {
          nombreDoc = "TEMP|" + docDatoBean.getTipoDoc() + "-I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + "." + docObjBean.getTipoDoc();
        } 
      } else {
        
        nombreDoc = "TEMP|I" + docDatoBean.getNuCorEmi() + "." + docObjBean.getTipoDoc();
      } 
      docVerBean.setUrlDocumento(urlDoc);
      docVerBean.setNoDocumento(nombreDoc);
    } 
    return docVerBean;
  }

  
  public DocumentoVerBean getNombreVoBoDoc(String pnuAnn, String pnuEmi, String ptiOpe, UsuarioConfigBean usuarioConfig, String rutaReporteBase) {
    DocumentoVerBean docVerBean = new DocumentoVerBean();
    
    docVerBean.setNuAnn(pnuAnn);
    docVerBean.setNuEmi(pnuEmi);
    docVerBean.setInDoc(false);
    boolean bswFirmaOnpe = true;
    
    DocumentoDatoBean docDatoBean = null;
    
    if (usuarioConfig.getSwFirma() != null) {
      docVerBean.setNoPrefijo("[VF]");
      if (usuarioConfig.getSwFirma().equals("R")) {
        docVerBean.setNoPrefijo("[R]");
        bswFirmaOnpe = false;
      } else {
        docDatoBean = getDatosDoc(pnuAnn, pnuEmi);
      } 
    } 
    if (bswFirmaOnpe) {
      if (docDatoBean != null) {
        if (docDatoBean.getTiEmi().equals("06") && docDatoBean.getEsDocEmi().equals("7") && ptiOpe.equals("5")) {
          
          String vformatoDoc = "NO_OK";
          
          if (docDatoBean.getCoDoc().equals("232") || docDatoBean.getCoDoc().equals("304")) {
            vformatoDoc = "OK";
          } else {
            vformatoDoc = this.emiDocumentoAdmService.getVerificaPdfDoc(docDatoBean.getNuAnn(), docDatoBean.getNuEmi(), docDatoBean.getTiCap());
          } 
          
          if (vformatoDoc.equals("OK")) {
            docVerBean.setDeMensaje("OK");
            docVerBean.setInDoc(true);
            docVerBean.setNuSecFirma(docDatoBean.getNuSecFirma());
            docVerBean.setFeFirma(docDatoBean.getFechaActual());
            docVerBean.setCoDocEmi(docDatoBean.getCoDoc());
            docVerBean.setInTipoFirma("F");

            
            if (docDatoBean.getNumeroDoc() == null) {
              docDatoBean.setNumeroDoc("");
            }
            docVerBean.setNumeroDoc(docDatoBean.getNumeroDoc());
          } else {
            docVerBean.setDeMensaje(vformatoDoc);
            docVerBean.setInDoc(false);
          } 
        } else {
          docVerBean.setDeMensaje("documento en elaboración");
        } 
      } else {
        docVerBean.setDeMensaje("Error en datos del documento");
      } 
    } else {
      docVerBean.setDeMensaje("Utilizar software Firma Onpe.");
    } 









    
    if (docVerBean.isInDoc()) {

      
      String urlDoc = "documento?accion=abrirDoc&nuAnn=" + docDatoBean.getNuAnn() + "&nuEmi=" + docDatoBean.getNuEmi() + "&tiCap=" + docDatoBean.getTiCap() + "&nroView=" + LsAleatorioViewDoc.getInstancia().add();
      
      if (docVerBean.getCoDocEmi().equals("232") || docVerBean.getCoDocEmi().equals("304")) {


        
        String vgnum_reg = "R" + docDatoBean.getNuCorEmi() + "." + docDatoBean.getNuSecFirma();
        String vgnumDoc = docDatoBean.getTipoDoc() + "$" + docDatoBean.getNumeroDoc() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc();
        String gnombreDoc = "TEMP|" + vgnum_reg + "$" + vgnumDoc + ".pdf";

        
        List<DocumentoDatoBean> listaSubReport1 = this.documentoObjDao.CargarSubReporte1(docDatoBean.getNuAnn(), docDatoBean.getNuEmi());
        
        List<DocumentoDatoBean> listaSubReport2 = this.documentoObjDao.CargarSubReporte2(docDatoBean.getNuAnn(), docDatoBean.getNuEmi());


        
        String logo = this.applicationProperties.getLogoReporteB64();
        ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(logo));
        
        Map<String, Object> parametros = new HashMap<String, Object>();
        parametros.put("P_NU_ANN", docDatoBean.getNuAnn());
        parametros.put("P_NU_EMI", docDatoBean.getNuEmi());
        parametros.put("P_NU_DOC", docDatoBean.getNumeroDoc());
        parametros.put("P_FECHA", docVerBean.getFeFirma());
        parametros.put("P_LOGO_DIR", (bis.available() == 0) ? null : bis);
        parametros.put("datosSubRep1", listaSubReport1);
        parametros.put("datosSubRep2", listaSubReport2);
        parametros.put("SUB_REPORT_DIR1", rutaReporteBase);
        parametros.put("SUB_REPORT_DIR2", rutaReporteBase);
        
        try {
          ReporteBean objReporte = getGenerarReporte("PDF", rutaReporteBase, parametros, gnombreDoc, docDatoBean.getNuAnn(), docDatoBean.getNuEmi());
          if (bis != null) bis.close(); 
          urlDoc = objReporte.noUrl;
        } catch (Exception e) {
          docVerBean.setInDoc(false);
          docVerBean.setDeMensaje(e.getMessage());
        } 
      } 

      
      String nombreDoc = null;
      String nombreFirma = null;


      
      if (docDatoBean.getTiEmi().equals("06")) {
        
        String vnumDoc = "";
        String vnum_reg = "";
        
        if (ptiOpe.equals("5")) {

          
          if (usuarioConfig.getSwFirma() != null && usuarioConfig.getSwFirma().equals("O")) {

            
            vnum_reg = "R" + docDatoBean.getNuCorEmi() + "." + docDatoBean.getNuSecFirma();
            vnumDoc = docDatoBean.getTipoDoc() + "$" + docDatoBean.getNumeroDoc() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc();
            nombreDoc = "TEMP|" + vnum_reg + "$" + vnumDoc + ".pdf";
            nombreFirma = "TEMP|" + vnum_reg + "$" + vnumDoc;
            
            this.documentoVoBoService.bloquearVoBoPersonal(pnuAnn, pnuEmi, usuarioConfig.getCoDep(), usuarioConfig.getCempCodemp(), usuarioConfig.getCempCodemp());
          } else {
            
            nombreDoc = null;
            nombreFirma = null;
          }
        
        } else if (ptiOpe.equals("6")) {
          nombreDoc = null;
        } else {
          nombreDoc = null;
        } 
      } 
      docVerBean.setUrlDocumento(urlDoc);
      docVerBean.setNoDocumento(nombreDoc);
      docVerBean.setNoFirma(nombreFirma);
    } 
    return docVerBean;
  }

  
  public DocumentoVerBean getNombreCargaVoBoDoc(String pnuAnn, String pnuEmi, String ptiOpe) {
    DocumentoVerBean docVerBean = new DocumentoVerBean();
    
    docVerBean.setNuAnn(pnuAnn);
    docVerBean.setNuEmi(pnuEmi);
    docVerBean.setInDoc(false);
    
    DocumentoDatoBean docDatoBean = null;
    
    docDatoBean = getDatosDoc(pnuAnn, pnuEmi);
    if (docDatoBean != null) {
      if (docDatoBean.getTiEmi().equals("06") && (docDatoBean.getEsDocEmi().equals("5") || docDatoBean.getEsDocEmi().equals("7")) && ptiOpe.equals("6")) {
        docVerBean.setDeMensaje("OK");
        docVerBean.setInDoc(true);
        docVerBean.setNuSecFirma(docDatoBean.getNuSecFirma());
        docVerBean.setFeFirma(docDatoBean.getFechaActual());
        docVerBean.setCoDocEmi(docDatoBean.getCoDoc());
      } else {
        docVerBean.setDeMensaje("Estado del Documento a cambiado");
      } 
    } else {
      docVerBean.setDeMensaje("Error en datos del documento");
    } 
    
    if (docVerBean.isInDoc()) {

      
      String urlDoc = "documento?accion=cargaDoc&nuAnn=" + docDatoBean.getNuAnn() + "&nuEmi=" + docDatoBean.getNuEmi() + "&nuSec=" + docDatoBean.getNuSecFirma();
      
      String nombreDoc = null;

      
      if (docDatoBean.getTiEmi().equals("06"))
      {
        if (ptiOpe.equals("6")) {
          nombreDoc = "OK";
        } else {
          nombreDoc = null;
        } 
      }
      docVerBean.setUrlDocumento(urlDoc);
      docVerBean.setNoDocumento(nombreDoc);
    } 
    return docVerBean;
  }
  
  public String verificarBloqueoDocumento(String pnuAnn, String pnuEmi, String pnuSecFirma) {
    String vResult = "NO_OK";
    try {
      DocumentoObjBlock docBlock = this.documentoObjDao.getDatosDocBlock(pnuAnn, pnuEmi);
      if (docBlock != null) {
        if (docBlock.getNuSecFirma() != null && docBlock.getNuSecFirma().equals(pnuSecFirma)) {
          vResult = "OK";
        } else {
          Date fechaFirma = docBlock.getFechaFirma();
          Date fechaActual = docBlock.getFechaActual();
          if (fechaFirma != null && fechaActual != null) {
            Date fechaMaxBloqueoDoc = new Date(fechaFirma.getTime() + 180000L);
            if (fechaActual.after(fechaMaxBloqueoDoc)) {
              docBlock = new DocumentoObjBlock();
              docBlock.setNuAnn(pnuAnn);
              docBlock.setNuEmi(pnuEmi);
              docBlock.setNuSecFirma(pnuSecFirma);
              docBlock.setTipoFirma("1");
              vResult = this.documentoObjDao.updRemitosBlock(docBlock);
            } else {
              vResult = "DOCUMENTO SE ESTA FIRMANDO.";
            } 
          } 
        } 
      } else {
        docBlock = new DocumentoObjBlock();
        docBlock.setNuAnn(pnuAnn);
        docBlock.setNuEmi(pnuEmi);
        docBlock.setNuSecFirma(pnuSecFirma);
        docBlock.setTipoFirma("1");
        vResult = this.documentoObjDao.insRemitosBlock(docBlock);
      } 
    } catch (Exception e) {
      e.printStackTrace();
      vResult = e.getMessage();
    } 









    
    return vResult;
  }

  
  public DocumentoVerBean getNombreFirmaDocAnexo(String pnuAnn, String pnuEmi, String pnuAnexo, String ptiOpe, UsuarioConfigBean usuarioConfig) {
    DocumentoVerBean docVerBean = new DocumentoVerBean();
    
    docVerBean.setNuAnn(pnuAnn);
    docVerBean.setNuEmi(pnuEmi);
    docVerBean.setNuAne(pnuAnexo);
    docVerBean.setInDoc(false);
    boolean bswFirmaOnpe = true;
    
    DocumentoObjBean docObjBean = null;
    
    if (usuarioConfig.getSwFirma() != null) {
      docVerBean.setNoPrefijo("[AF]");
      if (usuarioConfig.getSwFirma().equals("R")) {
        docVerBean.setNoPrefijo("[R]");
        bswFirmaOnpe = false;
      } else {
        docObjBean = getNombreArchivoAnexo(pnuAnn, pnuEmi, pnuAnexo);
        if (docObjBean != null && docObjBean.getNuTamano() > 0) {
          docVerBean.setInDoc(true);
        }
      } 
    } 
    if (bswFirmaOnpe) {
      if (docObjBean != null) {
        String vformatoDoc = "NO_OK";
        vformatoDoc = this.anexoDocumentoService.getVerificaPdfDoc(docObjBean.getNuAnn(), docObjBean.getNuEmi(), docObjBean.getNuAne());
        
        if (vformatoDoc.equals("OK")) {
          docVerBean.setDeMensaje("OK");
          docVerBean.setInDoc(true);
          docVerBean.setNuSecFirma(Utilidades.generateRandomNumber(8));
          docVerBean.setInTipoFirma("F");
        } else {
          docVerBean.setDeMensaje(vformatoDoc);
          docVerBean.setInDoc(false);
        } 
      } else {
        docVerBean.setDeMensaje("Error en datos del documento");
      } 
    } else {
      docVerBean.setDeMensaje("Utilizar software Firma Onpe.");
    } 









    
    if (docVerBean.isInDoc()) {

      
      String urlDoc = "documento?accion=abrirAnexo&nuAnn=" + docObjBean.getNuAnn() + "&nuEmi=" + docObjBean.getNuEmi() + "&nuAne=" + docObjBean.getNuAne();
      
      String nombreDoc = null;
      String nombreFirma = null;




      
      String vnumDoc = "";
      String vnum_reg = "";
      
      if (ptiOpe.equals("5")) {

        
        if (usuarioConfig.getSwFirma() != null && usuarioConfig.getSwFirma().equals("O")) {

          
          vnum_reg = "R" + docObjBean.getNuAnn() + "." + docObjBean.getNuEmi() + "-" + docObjBean.getNuAne();
          vnumDoc = docVerBean.getNuSecFirma();
          nombreDoc = "TEMP|" + vnum_reg + "-" + vnumDoc + ".pdf";
          nombreFirma = "TEMP|" + vnum_reg + "-" + vnumDoc;
        } else {
          
          nombreDoc = null;
          nombreFirma = null;
        }
      
      } else if (ptiOpe.equals("6")) {
        nombreDoc = null;
      } else {
        nombreDoc = null;
      } 
      
      docVerBean.setUrlDocumento(urlDoc);
      docVerBean.setNoDocumento(nombreDoc);
      docVerBean.setNoFirma(nombreFirma);
    } 
    return docVerBean;
  }

  
  public DocumentoVerBean getNombreCargaFirmaDocAnexo(String pnuAnn, String pnuEmi, String pnuAne, String ptiOpe) {
    DocumentoVerBean docVerBean = new DocumentoVerBean();
    
    docVerBean.setNuAnn(pnuAnn);
    docVerBean.setNuEmi(pnuEmi);
    docVerBean.setNuAne(pnuAne);
    docVerBean.setInDoc(false);
    
    DocumentoObjBean docObjBean = null;
    
    docObjBean = getNombreArchivoAnexo(pnuAnn, pnuEmi, pnuAne);
    if (docObjBean != null) {
      if (ptiOpe.equals("6")) {
        docVerBean.setDeMensaje("OK");
        docVerBean.setInDoc(true);
        docVerBean.setNuSecFirma(Utilidades.generateRandomNumber(8));
      } else {
        docVerBean.setDeMensaje("Estado del Documento a cambiado");
      } 
    } else {
      docVerBean.setDeMensaje("Error en datos del documento");
    } 
    
    if (docVerBean.isInDoc()) {

      
      String urlDoc = "documento?accion=cargaDocAnexo&nuAnn=" + docObjBean.getNuAnn() + "&nuEmi=" + docObjBean.getNuEmi() + "&nuAne=" + docObjBean.getNuAne() + "&nuSec=" + docVerBean.getNuSecFirma();
      
      String nombreDoc = null;



      
      if (ptiOpe.equals("6")) {
        nombreDoc = "OK";
      } else {
        nombreDoc = null;
      } 
      
      docVerBean.setUrlDocumento(urlDoc);
      docVerBean.setNoDocumento(nombreDoc);
    } 
    return docVerBean;
  }

  
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
  public String cargaDocAnexo(String pnuAnn, String pnuEmi, String pnuAne, String pnuSecFirma, String coUsuario, String prutaDoc) throws Exception {
    String vReturn = "NO_OK";
    try {
      DocumentoObjBean docObjBean = getNombreArchivoAnexo(pnuAnn, pnuEmi, pnuAne);
      if (docObjBean != null && docObjBean.getNuTamano() > 0) {
        docObjBean.setCoUseMod(coUsuario);
        docObjBean.setNumeroSecuencia(pnuSecFirma);
        vReturn = cargaDocAnexoFirmado(docObjBean);
        if (vReturn.equals("OK")) {
          vReturn = this.anexoDocumentoService.getCanAnexosReqFirma(pnuAnn, pnuEmi);
          if (vReturn.equals("0")) {
            vReturn = this.anexoDocumentoService.updRemitosResumenInFirmaAnexos("0", pnuAnn, pnuEmi);
            if (!vReturn.equals("OK")) {
              throw new validarDatoException("Error actualizando remitos resumen firma anexos.");
            }
          } else {
            vReturn = "OK";
          } 
        } 
      } else {
        throw new validarDatoException("Error cargando documento Firmado.");
      } 
    } catch (validarDatoException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new validarDatoException("ERROR EN TRANSACCION.");
    } 
    return vReturn;
  }
  
  public String cargaDocAnexoFirmado(DocumentoObjBean docObjBean) {
    String vReturn = "NO_OK";
    
    if (docObjBean != null) {
      
      try {
        byte[] archivoByte = ArchivoTemporal.leerArchivo(docObjBean.getNumeroSecuencia());
        docObjBean.setDocumento(archivoByte);
        docObjBean.setNuTamano((int)Math.round(archivoByte.length / 1024.0D));

        
        vReturn = this.anexoDocumentoService.updArchivoAnexoFirmado(docObjBean);
      } catch (Exception ex) {
         StringBuilder mensaje = new StringBuilder();
        vReturn = "Error en Leer Documento de Repositorio";
        mensaje.append(docObjBean.getNumeroSecuencia() + ":");
        mensaje.append(docObjBean.getNuAnn() + "." + docObjBean.getNuEmi());
        logger.error(mensaje, ex);
      } 
    } else {
      vReturn = "Error en Carga de Documento";
       StringBuilder mensaje = new StringBuilder();
      mensaje.append("cargaDocEmi:Objeto docObjBean NULO");
      logger.error(mensaje);
    } 
    return vReturn;
  }
  
  public String generarFormatoSiglasFirma(String tipoDoc, String nuDoc, String anio, String institucion, String uuoo) {
    tipoDoc = (tipoDoc == null) ? " " : tipoDoc;
    nuDoc = (nuDoc == null) ? " " : nuDoc;
    anio = (anio == null) ? " " : anio;
    institucion = (institucion == null) ? " " : institucion;
    uuoo = (uuoo == null) ? " " : uuoo;
    
    String cadenaSiglas = this.applicationProperties.getFormatoSiglas();
    cadenaSiglas = cadenaSiglas.replace("{TIPODOC}", tipoDoc + "$");
    cadenaSiglas = cadenaSiglas.replace("{NUDOC}", nuDoc);
    cadenaSiglas = cadenaSiglas.replace("{ANIO}", anio);
    cadenaSiglas = cadenaSiglas.replace("{INS}", institucion);
    cadenaSiglas = cadenaSiglas.replace("{UUOO}", uuoo);
    cadenaSiglas = cadenaSiglas.replace("Â", "");
    cadenaSiglas = cadenaSiglas.replace("N°", "");
    
    cadenaSiglas = cadenaSiglas.replace("/", "!");
    return cadenaSiglas;
  }


  
  public DocumentoVerBean getNombreDocEmiReporte(String pnuAnn, String pnuEmi, String ptiOpe, Usuario usuario, String rutaReporteBase) {
    DocumentoVerBean docVerBean = new DocumentoVerBean();
    
    docVerBean.setNuAnn(pnuAnn);
    docVerBean.setNuEmi(pnuEmi);
    docVerBean.setInDoc(false);
    
    DocumentoDatoBean docDatoBean = null;
    DocumentoObjBean docObjBean = null;
    
    docDatoBean = getDatosDoc(pnuAnn, pnuEmi);
    if (docDatoBean != null) {
      docVerBean.setNuSecFirma(docDatoBean.getNuSecFirma());
      docVerBean.setNumeroDoc(docDatoBean.getNumeroDoc());
      docVerBean.setFeFirma(docDatoBean.getFechaActual());
      docVerBean.setCoDocEmi(docDatoBean.getCoDoc());
      if (docVerBean.getCoDocEmi().equals("232") || docVerBean.getCoDocEmi().equals("304")) {
        docVerBean.setInDoc(true);
        docVerBean.setDeMensaje("OK");
        docObjBean = new DocumentoObjBean();
        docObjBean.setTipoDoc("pdf");
        if (docDatoBean.getNumeroDoc() == null) {
          docDatoBean.setNumeroDoc("      ");
          docVerBean.setNumeroDoc("      ");
        } 
      } else {
        
        docObjBean = getNombreArchivo(pnuAnn, pnuEmi, docDatoBean.getTiCap());
        if (docObjBean != null && docObjBean.getNuTamano() > 0) {
          docVerBean.setInDoc(true);
          docVerBean.setDeMensaje("OK");
          
          docVerBean.setNombreDocumentoWord(docDatoBean.getNuAnn() + "|" + docObjBean.getwNombreArchivo());

          
          this.auditoriaMovimientoDocDao.audiVisualizaDocumento(docObjBean, usuario);
        } else {
          
          docVerBean.setInDoc(false);
          docVerBean.setDeMensaje("Documento no se encuentra cargado en Repositorio");
        } 
      } 
    } 
    
    if (docVerBean.isInDoc()) {

      
      String nombreDoc, urlDoc = "documento?accion=abrirDoc&nuAnn=" + docDatoBean.getNuAnn() + "&nuEmi=" + docDatoBean.getNuEmi() + "&tiCap=" + docDatoBean.getTiCap() + "&nroView=" + LsAleatorioViewDoc.getInstancia().add();












      
      if (docDatoBean.getTiEmi().equals("06")) {
        
        if (ptiOpe.equals("3")) {
          nombreDoc = docDatoBean.getNuAnn() + "|" + docDatoBean.getTipoDoc() + "-I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + ".docx";
        } else if (ptiOpe.equals("4")) {
          nombreDoc = docDatoBean.getNuAnn() + "|" + docDatoBean.getTipoDoc() + "-I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + ".pdf";
        } else if (ptiOpe.equals("5")) {
          nombreDoc = docDatoBean.getNuAnn() + "|" + docDatoBean.getTipoDoc() + "-I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + "." + docObjBean.getTipoDoc();
        } else {
          nombreDoc = "TEMP|" + docDatoBean.getTipoDoc() + "-I" + docDatoBean.getNuCorEmi() + "-" + docDatoBean.getNuAnn() + "-" + docDatoBean.getSiglasDoc() + "." + docObjBean.getTipoDoc();
        } 
      } else {
        
        nombreDoc = "TEMP|I" + docDatoBean.getNuCorEmi() + "." + docObjBean.getTipoDoc();
      } 
      
      if (docVerBean.getCoDocEmi().equals("232") || docVerBean.getCoDocEmi().equals("304")) {
        
        List<DocumentoDatoBean> listaSubReport1 = this.documentoObjDao.CargarSubReporte1(docDatoBean.getNuAnn(), docDatoBean.getNuEmi());
        
        List<DocumentoDatoBean> listaSubReport2 = this.documentoObjDao.CargarSubReporte2(docDatoBean.getNuAnn(), docDatoBean.getNuEmi());


        
        String logo = this.applicationProperties.getLogoReporteB64();
        ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(logo));


        
        Map<String, Object> parametros = new HashMap<String, Object>();
        parametros.put("P_NU_ANN", docDatoBean.getNuAnn());
        parametros.put("P_NU_EMI", docDatoBean.getNuEmi());
        parametros.put("P_NU_DOC", docDatoBean.getNumeroDoc());
        parametros.put("P_FECHA", docDatoBean.getFechaActual());
        parametros.put("P_LOGO_DIR", (bis.available() == 0) ? null : bis);
        parametros.put("datosSubRep1", listaSubReport1);
        parametros.put("datosSubRep2", listaSubReport2);
        parametros.put("SUB_REPORT_DIR1", rutaReporteBase);
        parametros.put("SUB_REPORT_DIR2", rutaReporteBase);
        
        try {
          ReporteBean objReporte = getGenerarReporte("PDF", rutaReporteBase, parametros, nombreDoc, docDatoBean.getNuAnn(), docDatoBean.getNuEmi());
          if (bis != null) bis.close(); 
          urlDoc = objReporte.noUrl;
        } catch (Exception e) {
          docVerBean.setInDoc(false);
          docVerBean.setDeMensaje(e.getMessage());
        } 
      } 
      
      docVerBean.setUrlDocumento(urlDoc);
      docVerBean.setNoDocumento(nombreDoc);
    } 
    return docVerBean;
  }

  
  public ReporteBean getGenerarReporte(String formato, String rutaReporteBase, Map<String, Object> parametros, String archivoServer, String nuAnn, String nuEmi) throws Exception{
    ReporteBean objReporte = new ReporteBean();
    String eserror = "1";
    String coRespuesta = "";
    String deRespuesta = "";
    String prutaReporte = "";
    String deNoDoc = "";
    String nombreArchivo = "";
    String coReporte = "";

    
    try { String extensionArch;
      int tipoArchivo = 0;



      
      List<DocumentoDatoBean> lista = null;
      String formatoSiglas = "";
      
      try {
        lista = new ArrayList<DocumentoDatoBean>();
        DocumentoDatoBean objDocDatoBean = this.documentoObjDao.CargarCabeceraReporte(nuAnn, nuEmi);
        lista.add(objDocDatoBean);
        String nuDocumento = (parametros.get("P_NU_DOC") == null) ? "" : (parametros.get("P_NU_DOC").toString().equals("0") ? "" : parametros.get("P_NU_DOC").toString());
        nuDocumento = nuDocumento.equals("") ? (objDocDatoBean.getNuDoc().equals("0") ? "" : objDocDatoBean.getNuDoc()) : nuDocumento;
        formatoSiglas = this.DocumentoXmlService.generarFormatoSiglas(objDocDatoBean.getDeTipDocAdm(), nuDocumento, objDocDatoBean.getNuAnn(), this.applicationProperties.getSiglaInstitucion(), objDocDatoBean.getDeDocSig());
        parametros.put("formatoSiglas", formatoSiglas);
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
      if (formato.equalsIgnoreCase("PDF")) {
        coReporte = "TDR11";
        extensionArch = ".pdf";
        tipoArchivo = 2;
      } else {
        coReporte = "TDR11_XLS";
        extensionArch = ".xls";
        tipoArchivo = 1;
      } 
      String rutaJasper = rutaReporteBase + "/" + coReporte + ".jasper";
      
      nombreArchivo = Utilidades.generateRandomNumber(10);


      
      prutaReporte = nombreArchivo + extensionArch;
      deNoDoc = archivoServer;

      
      Utilidades util = new Utilidades();
      
      byte[] archivo = util.GenerarReporteLista(rutaJasper, lista, coReporte, parametros, tipoArchivo);
      
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

      return objReporte; } catch (FileNotFoundException ex) { eserror = "1"; return objReporte; } catch (IOException ex) { eserror = "1"; return objReporte; }
    finally
    { Exception exception = null;
      objReporte.setcoRespuesta(coRespuesta);
      objReporte.setdeRespuesta(deRespuesta);
      objReporte.setnoUrl(prutaReporte);
      objReporte.setnoDoc(deNoDoc); }
     } 
  public DocumentoVerBean getNombreFirmaDocReporte(String pnuAnn, String pnuEmi, String ptiOpe, UsuarioConfigBean usuarioConfig, String rutaReporteBase) { DocumentoVerBean docVerBean = new DocumentoVerBean();
    String vin_pref = "1";
    String vin_TSA = "0";
    
    docVerBean.setNuAnn(pnuAnn);
    docVerBean.setNuEmi(pnuEmi);
    docVerBean.setInDoc(false);
    
    DocumentoDatoBean docDatoBean = null;
    
    if (usuarioConfig.getSwFirma() != null) {
      docVerBean.setNoPrefijo("[NF]");
      if (usuarioConfig.getSwFirma().equals("R")) {
        docVerBean.setNoPrefijo("[R]");
      }
      docDatoBean = getDatosDoc(pnuAnn, pnuEmi);
    } 
    
    if (docDatoBean != null) {
      if (docDatoBean.getTiEmi().equals("06") && docDatoBean.getEsDocEmi().equals("7") && ptiOpe.equals("5")) {
        
        String coTipDoc = docDatoBean.getCoDoc();
        String vformatoDoc = "NO_OK";
        if (coTipDoc.equals("232") || coTipDoc.equals("304")) {
          vformatoDoc = "OK";
          vin_pref = "N";
        } else {
          vformatoDoc = this.emiDocumentoAdmService.getVerificaPdfDoc(docDatoBean.getNuAnn(), docDatoBean.getNuEmi(), docDatoBean.getTiCap());
          
          String vin_firma = this.emiDocumentoAdmService.getInFirmaDocAdm(coTipDoc);
          if (vin_firma.equals("1")) {
            vin_pref = "N";
          } else if (vin_firma.equals("2")) {
            vin_pref = "1";
          } 
        } 


        if (vformatoDoc.equals("OK")) {
          docVerBean.setDeMensaje("OK");
          docVerBean.setInDoc(true);
          docVerBean.setNuSecFirma(docDatoBean.getNuSecFirma());
          docVerBean.setFeFirma(docDatoBean.getFechaActual());
          docVerBean.setCoDocEmi(docDatoBean.getCoDoc());
          if (usuarioConfig.getInFirma() != null && usuarioConfig.getInFirma().equals("1")) {
            docVerBean.setInTipoFirma("N");
          }
          else if (usuarioConfig.getInFirma().equals("0")) {
            String vinDoc = this.documentoObjDao.getInFirmaDoc(docDatoBean.getCoDepEmi(), docDatoBean.getCoDoc());
            docVerBean.setInTipoFirma(vinDoc);
          } else {
            docVerBean.setInTipoFirma("F");
          } 


          
          if (docDatoBean.getNumeroDoc() == null || docDatoBean.getNumeroDoc().length() == 0) {
            
            if (docDatoBean.getTiEmi().equals("06")) {
              String vnuDoc = this.emiDocumentoAdmService.getNumeroDocSiguienteAdm(docDatoBean.getNuAnn(), docDatoBean.getCoDepEmi(), docDatoBean.getCoDoc());
              docVerBean.setNumeroDoc(vnuDoc);
              docDatoBean.setNumeroDoc(vnuDoc);
            
            }

          
          }
          else {

            
            docVerBean.setNumeroDoc(docDatoBean.getNumeroDoc());
          } 
        } else {
          docVerBean.setDeMensaje(vformatoDoc);
          docVerBean.setInDoc(false);
        } 
      } 
    } else {
      docVerBean.setDeMensaje("Error en datos del documento");
    } 








    
    if (docVerBean.isInDoc()) {

      
      String urlDoc = "documento?accion=abrirDoc&nuAnn=" + docDatoBean.getNuAnn() + "&nuEmi=" + docDatoBean.getNuEmi() + "&tiCap=" + docDatoBean.getTiCap() + "&nroView=" + LsAleatorioViewDoc.getInstancia().add();
      
      String nombreDoc = null;
      String nombreFirma = null;


      
      if (docDatoBean.getTiEmi().equals("06")) {
        
        String vnumDoc = "";
        String vnum_reg = "";
        
        if (ptiOpe.equals("5")) {







          
          if (usuarioConfig.getSwFirma() != null && usuarioConfig.getSwFirma().equals("O"))
          {
            
            vnum_reg = "I" + docDatoBean.getNuCorEmi() + "." + docDatoBean.getNuSecFirma();


            
            String cadena = generarFormatoSiglasFirma(docDatoBean.getTipoDoc(), docDatoBean.getNumeroDoc(), docDatoBean.getNuAnn(), this.applicationProperties.getSiglaInstitucion(), docDatoBean.getSiglasDoc());
            
            vnumDoc = cadena.replace(" ", "") + "$" + docDatoBean.getDeLugar() + "$" + docDatoBean.getFechaFirma().replace("-", "$");


            
            nombreDoc = "TEMP|" + vnum_reg + "$" + vnumDoc + "$" + vin_pref + ".pdf";
            nombreFirma = "TEMP|" + vnum_reg + "$" + vnumDoc + "$" + vin_pref;


          
          }


        
        }
        else if (ptiOpe.equals("6")) {
          nombreDoc = null;
        } else {
          nombreDoc = null;
        } 

        
        if (docVerBean.getCoDocEmi().equals("232") || docVerBean.getCoDocEmi().equals("304")) {
          List<DocumentoDatoBean> listaSubReport1 = this.documentoObjDao.CargarSubReporte1(docDatoBean.getNuAnn(), docDatoBean.getNuEmi());
          
          List<DocumentoDatoBean> listaSubReport2 = this.documentoObjDao.CargarSubReporte2(docDatoBean.getNuAnn(), docDatoBean.getNuEmi());


          
          String logo = this.applicationProperties.getLogoReporteB64();
          ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(logo));
          
          Map<String, Object> parametros = new HashMap<String, Object>();
          parametros.put("P_NU_ANN", docDatoBean.getNuAnn());
          parametros.put("P_NU_EMI", docDatoBean.getNuEmi());
          parametros.put("P_NU_DOC", docDatoBean.getNumeroDoc());
          parametros.put("P_FECHA", docDatoBean.getFechaActual());
          parametros.put("P_LOGO_DIR", (bis.available() == 0) ? null : bis);
          parametros.put("datosSubRep1", listaSubReport1);
          parametros.put("datosSubRep2", listaSubReport2);
          parametros.put("SUB_REPORT_DIR1", rutaReporteBase);
          parametros.put("SUB_REPORT_DIR2", rutaReporteBase);
          
          try {
            ReporteBean objReporte = getGenerarReporte("PDF", rutaReporteBase, parametros, nombreDoc, docDatoBean.getNuAnn(), docDatoBean.getNuEmi());
            if (bis != null) bis.close(); 
            urlDoc = objReporte.noUrl;
          } catch (Exception e) {
            docVerBean.setInDoc(false);
            docVerBean.setDeMensaje(e.getMessage());
          } 
        } 
      } 


      
      docVerBean.setUrlDocumento(urlDoc);
      docVerBean.setNoDocumento(nombreDoc);
      docVerBean.setNoFirma(nombreFirma);
    } 
    return docVerBean; }


  
  public DocumentoObjBean leerDocumento(String pnuAnn, String pnuEmi, String ptiCap, String pAbreWord) {
    DocumentoObjBean docObjBean = null;
    docObjBean = this.documentoObjDao.leerDocumento(pnuAnn, pnuEmi, ptiCap, pAbreWord);
    
    if (docObjBean != null && docObjBean.getNombreArchivo() != null && docObjBean.getNombreArchivo().length() > 0) {
      int pos = docObjBean.getNombreArchivo().lastIndexOf(".");
      if (pos > 0) {
        docObjBean.setTipoDoc(docObjBean.getNombreArchivo().substring(pos + 1).toLowerCase());
      } else {
        docObjBean.setTipoDoc("octet-stream");
      } 
    } 
    
    return docObjBean;
  }
}
