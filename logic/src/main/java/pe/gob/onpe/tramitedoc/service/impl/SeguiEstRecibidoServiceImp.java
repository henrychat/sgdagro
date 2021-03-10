package pe.gob.onpe.tramitedoc.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.libreria.util.Utility;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoSeguiEstRecBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoRecepSeguiBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoSeguiEstRecibidoBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaConsulBean;
import pe.gob.onpe.tramitedoc.bean.ReporteBean;
import pe.gob.onpe.tramitedoc.dao.SeguiEstRecibidoDao;
import pe.gob.onpe.tramitedoc.service.SeguiEstRecibidoService;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;











@Service("consultaEstRecibidoService")
public class SeguiEstRecibidoServiceImp   implements SeguiEstRecibidoService
{
  @Autowired
  ApplicationProperties applicationProperties;
  @Autowired
  private SeguiEstRecibidoDao seguiEstRecibidoDao;
  
  public List<DocumentoSeguiEstRecibidoBean> getListDocSeguiEstRec(BuscarDocumentoSeguiEstRecBean buscarDocumentoSeguiEstRecBean) {
    List<DocumentoSeguiEstRecibidoBean> list = null;
    try {
      list = this.seguiEstRecibidoDao.getListDocSeguiEstRec(buscarDocumentoSeguiEstRecBean);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public DocumentoRecepSeguiBean getDocumentoRecepAdmSegui(String snuAnn, String snuEmi, String snuDes) {
    DocumentoRecepSeguiBean documentoRecepConsulBean = null;
    try {
      documentoRecepConsulBean = this.seguiEstRecibidoDao.getDocumentoRecepAdmSegui(snuAnn, snuEmi, snuDes);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return documentoRecepConsulBean;
  }

  
  public List<ReferenciaConsulBean> getDocumentosRefRecepAdmSegui(String snuAnn, String snuEmi) {
    List<ReferenciaConsulBean> list = null;
    
    try {
      list = this.seguiEstRecibidoDao.getDocumentosRefRecepAdmSegui(snuAnn, snuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }

  
  public HashMap getDocumentosEnReferencia(BuscarDocumentoSeguiEstRecBean buscarDocumentoSeguiEstRecBean) {
    List<DocumentoSeguiEstRecibidoBean> list = null;
    DocumentoSeguiEstRecibidoBean documentoSeguiEstRecibidoBean = null;
    HashMap<Object, Object> mp = new HashMap<Object, Object>();
    try {
      documentoSeguiEstRecibidoBean = this.seguiEstRecibidoDao.existeDocumentoReferenciado(buscarDocumentoSeguiEstRecBean);
      if (documentoSeguiEstRecibidoBean != null && documentoSeguiEstRecibidoBean.getMsjResult() != null && documentoSeguiEstRecibidoBean.getMsjResult().equals("OK")) {
        documentoSeguiEstRecibidoBean.setCoDepDes(buscarDocumentoSeguiEstRecBean.getCoDependencia());
        documentoSeguiEstRecibidoBean.setCoEmpDes(buscarDocumentoSeguiEstRecBean.getCoEmpleado());
        
        list = this.seguiEstRecibidoDao.getDocumentosReferenciadoBusq(documentoSeguiEstRecibidoBean, buscarDocumentoSeguiEstRecBean.getTiAcceso());
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


  
  public String getRutaReporte(BuscarDocumentoSeguiEstRecBean buscarDocumentoSeguiEstRecBean) { return this.seguiEstRecibidoDao.getRutaReporte(buscarDocumentoSeguiEstRecBean); }


  
  public HashMap getBuscaDependenciaEmite(String coDep, String pdeDepEmite) {
    String vReturn = "1";
    HashMap<Object, Object> map = new HashMap<Object, Object>();
    
    try {
      List list = this.seguiEstRecibidoDao.getListDestinatarioEmi(coDep, pdeDepEmite);
      if (list != null) {
        if (list.isEmpty()) {
          list = this.seguiEstRecibidoDao.getListDestinatarioEmi(coDep, null);
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


  
  public ReporteBean getGenerarReporte(BuscarDocumentoSeguiEstRecBean buscarDocumentoSeguiEstRecBean, Map parametros) throws Exception {
    ReporteBean objReporte = new ReporteBean();
    String eserror = "1";
    String coRespuesta = "0";
    String deRespuesta = "";
    String prutaReporte = "";
    String deNoDoc = "";
    String nombreArchivo = "";
    String coReporte = "";

    
    try { String extensionArch;
      int tipoArchivo = 0;

      
      String fecha = Utility.getInstancia().getStringFechaYYYYMMDDHHmm(new Date());
      
      List<DocumentoSeguiEstRecibidoBean> lista = null;
      
      try {
        lista = getListaReporte(buscarDocumentoSeguiEstRecBean);
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
      if (buscarDocumentoSeguiEstRecBean.getFormatoReporte().equalsIgnoreCase("PDF")) {
        coReporte = "TDR391";
        extensionArch = ".pdf";
        tipoArchivo = 2;
      } else {
        coReporte = "TDR391_XLS";
        extensionArch = ".xls";
        tipoArchivo = 1;
      } 
      String rutaJasper = buscarDocumentoSeguiEstRecBean.getRutaReporteJasper() + "/" + coReporte + ".jasper";
      buscarDocumentoSeguiEstRecBean.setRutaReporteJasper(rutaJasper);
      
      nombreArchivo = Utilidades.generateRandomNumber(10);
      prutaReporte = "SEGUIMIENTO_" + nombreArchivo + extensionArch;
      deNoDoc = "temp|SEGUIMIENTO_" + fecha + extensionArch;
      
      Utilidades util = new Utilidades();
      byte[] archivo = util.GenerarReporteLista(buscarDocumentoSeguiEstRecBean.getRutaReporteJasper(), lista, coReporte, parametros, tipoArchivo);
      
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























      
      return objReporte; } catch (JRException ex) { Logger.getLogger(SeguiEstRecibidoServiceImp.class.getName()).log(Level.SEVERE, null, (Throwable)ex); eserror = "1"; return objReporte; } catch (FileNotFoundException ex) { Logger.getLogger(SeguiEstRecibidoServiceImp.class.getName()).log(Level.SEVERE, null, ex); eserror = "1"; return objReporte; } catch (IOException ex) { Logger.getLogger(SeguiEstRecibidoServiceImp.class.getName()).log(Level.SEVERE, null, ex); eserror = "1"; return objReporte; }
    finally
    { Exception exception = null; objReporte.setcoRespuesta(coRespuesta); objReporte.setdeRespuesta(deRespuesta);
      objReporte.setnoUrl(prutaReporte);
      objReporte.setnoDoc(deNoDoc); }
     } public List<DocumentoSeguiEstRecibidoBean> getListaReporte(BuscarDocumentoSeguiEstRecBean buscarDocumentoSeguiEstRecBean) { return this.seguiEstRecibidoDao.getListaReporteBusqueda(buscarDocumentoSeguiEstRecBean); }
}
