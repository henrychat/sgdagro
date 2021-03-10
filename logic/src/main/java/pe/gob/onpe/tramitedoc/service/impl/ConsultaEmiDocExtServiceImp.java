package pe.gob.onpe.tramitedoc.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.libreria.util.Utility;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoExtConsulBean;
import pe.gob.onpe.tramitedoc.bean.DestinatarioDocumentoEmiConsulBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoExtConsulBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaDocExtRecepBean;
import pe.gob.onpe.tramitedoc.bean.RemitenteBean;
import pe.gob.onpe.tramitedoc.bean.ReporteBean;
import pe.gob.onpe.tramitedoc.dao.ConsultaEmiDocExtDao;
import pe.gob.onpe.tramitedoc.dao.MesaPartesDao;
import pe.gob.onpe.tramitedoc.service.ConsultaEmiDocExtService;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;













@Service("consultaEmiDocExtService")
public class ConsultaEmiDocExtServiceImp   implements ConsultaEmiDocExtService
{
  @Autowired
  private ConsultaEmiDocExtDao consultaEmiDocExtRecDao;
  @Autowired
  private MesaPartesDao mesaPartesDao;
  @Autowired
  private ApplicationProperties applicationProperties;
  
  public List<DocumentoExtConsulBean> getDocumentosExtConsulBean(BuscarDocumentoExtConsulBean buscarDocExt) {
    List<DocumentoExtConsulBean> list = null;

    
    try {
      list = this.consultaEmiDocExtRecDao.getDocumentosExternos(buscarDocExt);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }

  
  public DocumentoExtConsulBean getDocumentoExtConsulBean(String nuAnn, String nuEmi) {
    DocumentoExtConsulBean docExt = null;
    try {
      docExt = this.consultaEmiDocExtRecDao.getDocumentoExtConsulBean(nuAnn, nuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return docExt;
  }

  
  public List<DestinatarioDocumentoEmiConsulBean> getLsDestinoEmi(String pnuAnn, String pnuEmi) {
    List<DestinatarioDocumentoEmiConsulBean> list = null;
    try {
      list = this.consultaEmiDocExtRecDao.getLsDestinoEmi(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public List<ReferenciaDocExtRecepBean> getLsRefDocExterno(String pnuAnn, String pnuEmi) {
    List<ReferenciaDocExtRecepBean> list = null;
    try {
      if (pnuAnn != null && pnuAnn.trim().length() > 0 && pnuEmi != null && pnuEmi.trim().length() > 0) {
        list = this.consultaEmiDocExtRecDao.getLsRefDocExterno(pnuAnn, pnuEmi);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }


  
  public String getRutaReporte(BuscarDocumentoExtConsulBean DocExt) { return this.consultaEmiDocExtRecDao.getRutaReporte(DocExt); }


  
  public List<RemitenteBean> getAllDependencias() {
    List<RemitenteBean> list = null;
    try {
      list = this.mesaPartesDao.getAllDependencias();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }


  
  public List<DocumentoExtConsulBean> getListaReporte(BuscarDocumentoExtConsulBean buscarDocumentoExtConsulBean) { return this.consultaEmiDocExtRecDao.getListaReporteBusqueda(buscarDocumentoExtConsulBean); }


  
  public ReporteBean getGenerarReporte(BuscarDocumentoExtConsulBean buscarDocumentoExtConsulBean, Map parametros)  throws Exception,IOException {
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

      
      String fecha = Utility.getInstancia().getStringFechaYYYYMMDDHHmm(new Date());
      
      List<DocumentoExtConsulBean> lista = null;
      try {
        lista = getListaReporte(buscarDocumentoExtConsulBean);
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
      if (buscarDocumentoExtConsulBean.getFormatoReporte().equalsIgnoreCase("PDF")) {
        coReporte = "TDR33";
        extensionArch = ".pdf";
        tipoArchivo = 2;
      } else {
        coReporte = "TDR33_XLS";
        extensionArch = ".xls";
        tipoArchivo = 1;
      } 
      String rutaJasper = buscarDocumentoExtConsulBean.getRutaReporteJasper() + "/" + coReporte + ".jasper";
      
      buscarDocumentoExtConsulBean.setRutaReporteJasper(rutaJasper);
      
      nombreArchivo = Utilidades.generateRandomNumber(10);
      prutaReporte = "RECEPDOCEXT_" + nombreArchivo + extensionArch;
      deNoDoc = "temp|RECEPDOCEXT_" + fecha + extensionArch;
      
      Utilidades util = new Utilidades();
      byte[] archivo = util.GenerarReporteLista(buscarDocumentoExtConsulBean.getRutaReporteJasper(), lista, coReporte, parametros, tipoArchivo);
      
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
    	} 
    catch (JRException ex) { 
    	Logger.getLogger(ConsultaEmiDocExtServiceImp.class.getName()).log(Level.SEVERE, null, (Throwable)ex);
    	eserror = "1"; 
    	return objReporte; 
    }
    catch (FileNotFoundException ex)     { 
    	Logger.getLogger(ConsultaEmiDocExtServiceImp.class.getName()).log(Level.SEVERE, null, ex);
    	eserror = "1"; return objReporte; 
    } 
    catch (IOException ex)    { 
    	Logger.getLogger(ConsultaEmiDocExtServiceImp.class.getName()).log(Level.SEVERE, null, ex);
    	eserror = "1";
    	return objReporte;
    }
    finally
    { Exception exception = null;
      objReporte.setcoRespuesta(coRespuesta);
      objReporte.setdeRespuesta(deRespuesta);
      objReporte.setnoUrl(prutaReporte);
      objReporte.setnoDoc(deNoDoc); }
  
  }
}
