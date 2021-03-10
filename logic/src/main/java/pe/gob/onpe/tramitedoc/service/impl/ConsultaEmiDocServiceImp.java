package pe.gob.onpe.tramitedoc.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoEmiConsulBean;
import pe.gob.onpe.tramitedoc.bean.DestinatarioDocumentoEmiConsulBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiConsulBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaConsulBean;
import pe.gob.onpe.tramitedoc.bean.ReporteBean;
import pe.gob.onpe.tramitedoc.dao.ConsultaEmiDocDao;
import pe.gob.onpe.tramitedoc.service.ConsultaEmiDocService;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;


@Service("consultaEmiDocService")
public class ConsultaEmiDocServiceImp  implements ConsultaEmiDocService
{
  @Autowired
  ApplicationProperties applicationProperties;
  @Autowired
  private ConsultaEmiDocDao consultaEmiDocDao;
  
  public List<DocumentoEmiConsulBean> getDocumentosBuscaEmiAdm(BuscarDocumentoEmiConsulBean buscarDocumentoEmiConsulBean) {
    List<DocumentoEmiConsulBean> list = null;
    try {
      list = this.consultaEmiDocDao.getDocumentosBuscaEmiAdmCopia(buscarDocumentoEmiConsulBean);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }


  
  public DocumentoEmiConsulBean getDocumentoEmiAdm(String pnuAnn, String pnuEmi) {
    DocumentoEmiConsulBean documentoEmiConsulBean = null;
    try {
      documentoEmiConsulBean = this.consultaEmiDocDao.getDocumentoEmiAdm(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return documentoEmiConsulBean;
  }

  
  public String getTipoDestinatarioEmi(String pnuAnn, String pnuEmi) {
    String retval = null;
    try {
      retval = this.consultaEmiDocDao.getTipoDestinatarioEmi(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return retval;
  }

  
  public HashMap getLstDestintariotlbEmi(String pnuAnn, String pnuEmi) {
    List<DestinatarioDocumentoEmiConsulBean> list = null;
    HashMap map = null;
    try {
      list = this.consultaEmiDocDao.getLstDestintariotlbEmi(pnuAnn, pnuEmi);
      map = getLstDestinatario(list);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return map;
  }
  
  private HashMap getLstDestinatario(List<DestinatarioDocumentoEmiConsulBean> list) {
    HashMap<Object, Object> map = new HashMap<Object, Object>();
    List<DestinatarioDocumentoEmiConsulBean> lst1 = new ArrayList();
    List<DestinatarioDocumentoEmiConsulBean> lst2 = new ArrayList();
    List<DestinatarioDocumentoEmiConsulBean> lst3 = new ArrayList();
    List<DestinatarioDocumentoEmiConsulBean> lst4 = new ArrayList();
    for (DestinatarioDocumentoEmiConsulBean dest : list) {
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

  
  public List<ReferenciaConsulBean> getLstDocumReferenciatblEmi(String pnuAnn, String pnuEmi) {
    List<ReferenciaConsulBean> list = null;
    try {
      list = this.consultaEmiDocDao.getLstDocumReferenciatblEmi(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public HashMap getDocumentosEnReferencia(BuscarDocumentoEmiConsulBean buscarDocumentoEmiConsulBean) {
    List<DocumentoEmiConsulBean> list = null;
    DocumentoEmiConsulBean documentoEmiConsulBean = null;
    HashMap<Object, Object> mp = new HashMap<Object, Object>();
    try {
      documentoEmiConsulBean = this.consultaEmiDocDao.existeDocumentoReferenciado(buscarDocumentoEmiConsulBean);
      if (documentoEmiConsulBean != null && documentoEmiConsulBean.getMsjResult() != null && documentoEmiConsulBean.getMsjResult().equals("OK")) {
        documentoEmiConsulBean.setCoDepEmi(buscarDocumentoEmiConsulBean.getCoDependencia());
        documentoEmiConsulBean.setCoEmpRes(buscarDocumentoEmiConsulBean.getCoEmpleado());
        list = this.consultaEmiDocDao.getDocumentosReferenciadoBusq(documentoEmiConsulBean, buscarDocumentoEmiConsulBean.getTiAcceso());
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
  
  public String getRutaReporte(BuscarDocumentoEmiConsulBean buscarDocumentoEmiConsulBean) {
	  return this.consultaEmiDocDao.getRutaReporte(buscarDocumentoEmiConsulBean); 
  }
  
  public List<DocumentoEmiConsulBean> getListaReporte(BuscarDocumentoEmiConsulBean buscarDocumentoEmiConsulBean) {
	  return this.consultaEmiDocDao.getListaReporteBusqueda(buscarDocumentoEmiConsulBean);
  }

  
  public ReporteBean getGenerarReporte(BuscarDocumentoEmiConsulBean buscarDocumentoEmiConsulBean, Map parametros)  throws Exception{
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
      
      List<DocumentoEmiConsulBean> lista = null;
      try {
        lista = getListaReporte(buscarDocumentoEmiConsulBean);
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
      if (buscarDocumentoEmiConsulBean.getFormatoReporte().equalsIgnoreCase("PDF")) {
        coReporte = "TDR10_XLS";
        
        extensionArch = ".pdf";
        tipoArchivo = 2;
      } else {
        coReporte = "TDR10_XLS";
        extensionArch = ".xls";
        tipoArchivo = 1;
      } 
      String rutaJasper = buscarDocumentoEmiConsulBean.getRutaReporteJasper() + "/" + coReporte + ".jasper";
      
      buscarDocumentoEmiConsulBean.setRutaReporteJasper(rutaJasper);
      
      nombreArchivo = Utilidades.generateRandomNumber(10);
      prutaReporte = "EMISION_" + nombreArchivo + extensionArch;
      deNoDoc = "temp|EMISION_" + fecha + extensionArch;
      
      Utilidades util = new Utilidades();
      byte[] archivo = util.GenerarReporteLista(buscarDocumentoEmiConsulBean.getRutaReporteJasper(), lista, coReporte, parametros, tipoArchivo);
      
      if (archivo == null) {
        eserror = "1";
        coRespuesta = "1";
        deRespuesta = "No se genercorrectamente el archivo.";
        throw new Exception("No se genercorrectamente el archivo.");
      } 
      
	      String rutaBase = this.applicationProperties.getRutaTemporal() + "/" + prutaReporte;
	    //  String rutaBase = "D:\\\\glassfish4\\\\glassfish\\\\tmppcm" + "\\\\" + prutaReporte;
	      
	      
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
    	  Logger.getLogger(ConsultaEmiDocServiceImp.class.getName()).log(Level.SEVERE, null, (Throwable)ex); eserror = "1"; return objReporte;
    	  } 
          catch (FileNotFoundException ex) { 
		  Logger.getLogger(ConsultaEmiDocServiceImp.class.getName()).log(Level.SEVERE, null, ex);
		  eserror = "1"; 
		  return objReporte;
    	  } 
         catch (IOException ex)    {
    	  Logger.getLogger(ConsultaEmiDocServiceImp.class.getName()).log(Level.SEVERE, null, ex);
          eserror = "1"; return objReporte;
    }
    finally  { 
      Exception exception = null;
      objReporte.setcoRespuesta(coRespuesta);
      objReporte.setdeRespuesta(deRespuesta);
      objReporte.setnoUrl(prutaReporte);
      objReporte.setnoDoc(deNoDoc);
      }
  
  }
}
