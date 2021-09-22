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
import pe.gob.onpe.tramitedoc.bean.BuscarDocTrazabilidadBean;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoSeguiEstEmiBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiSeguiBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoSeguiEstEmitidoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoSeguiTrazaBean;
import pe.gob.onpe.tramitedoc.bean.DocumentosTrazaBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaConsulBean;
import pe.gob.onpe.tramitedoc.bean.ReporteBean;

import pe.gob.onpe.tramitedoc.dao.SeguiTrazabilidadDao;

import pe.gob.onpe.tramitedoc.service.SeguiTrazabilidadService;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;


@Service("seguiTrazabilidadService")
public class SeguiTrazabilidadServiceImp   implements SeguiTrazabilidadService {
  @Autowired
  ApplicationProperties applicationProperties;
  @Autowired
  private SeguiTrazabilidadDao  seguiTrazabilidadDao;
  
  public DocumentoEmiSeguiBean getDocumentoEmiSeguiBean(String snuAnn, String snuEmi, String snuDes) {
    DocumentoEmiSeguiBean documentoEmiSeguiBean = null;
    try {
      documentoEmiSeguiBean = seguiTrazabilidadDao.getDocumentoEmiSeguiBean(snuAnn, snuEmi, snuDes);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return documentoEmiSeguiBean;
  }

  
  public List getDocumentosRefEmiAdmSegui(String snuAnn, String snuEmi) {
    List<ReferenciaConsulBean> list = null;
    try {
      list = seguiTrazabilidadDao.getDocumentosRefRecepAdm(snuAnn, snuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }


  
  public String getRutaReporte(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean) { 
	  return this.seguiTrazabilidadDao.getRutaReporte(buscarDocumentoSeguiEstEmiBean);
  }


  
  public List<DocumentoSeguiTrazaBean> getListDocSegui(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean) {
    List<DocumentoSeguiTrazaBean> list = null;
    try {
      list = seguiTrazabilidadDao.getListDocSeguiTrazab(buscarDocumentoSeguiEstEmiBean);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public HashMap getBuscaDependenciaEmite(String coDep, String pdeDepEmite) {
    String vReturn = "1";
    HashMap<Object, Object> map = new HashMap<Object, Object>();
    
    try {
      List list = this.seguiTrazabilidadDao.getListDestinatarioEmi(coDep, pdeDepEmite);
      if (list != null) {
        if (list.isEmpty()) {
          list = this.seguiTrazabilidadDao.getListDestinatarioEmi(coDep, null);
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

  
  public HashMap getDocumentosEnReferencia(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean) {
    List<DocumentoSeguiEstEmitidoBean> list = null;
    DocumentoSeguiEstEmitidoBean documentoSeguiEstEmitidoBean = null;
    HashMap<Object, Object> mp = new HashMap<Object, Object>();
    try {
      documentoSeguiEstEmitidoBean = seguiTrazabilidadDao.existeDocumentoReferenciado(buscarDocumentoSeguiEstEmiBean);
      if (documentoSeguiEstEmitidoBean != null && documentoSeguiEstEmitidoBean.getMsjResult() != null && documentoSeguiEstEmitidoBean.getMsjResult().equals("OK")) {
        documentoSeguiEstEmitidoBean.setCoDepEmi(buscarDocumentoSeguiEstEmiBean.getCoDependencia());
        documentoSeguiEstEmitidoBean.setCoEmpRes(buscarDocumentoSeguiEstEmiBean.getCoEmpleado());
        list = seguiTrazabilidadDao.getDocumentosReferenciadoBusq(documentoSeguiEstEmitidoBean, buscarDocumentoSeguiEstEmiBean.getTiAcceso());
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


  
  public ReporteBean getGenerarReporte(BuscarDocTrazabilidadBean buscarDocTrazabilidadBean, Map parametros)  throws Exception {
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
      
      List<DocumentoSeguiTrazaBean> lista = null;
      try {
        lista = getListaReporte(buscarDocTrazabilidadBean);
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
      if (buscarDocTrazabilidadBean.getFormatoReporte().equalsIgnoreCase("PDF")) {
        coReporte = "TDR395";
        extensionArch = ".pdf";
        tipoArchivo = 2;
      } else {
        coReporte = "TDR395_XLS";
        extensionArch = ".xls";
        tipoArchivo = 1;
      } 
      String rutaJasper = buscarDocTrazabilidadBean.getRutaReporteJasper() + "/" + coReporte + ".jasper";
      buscarDocTrazabilidadBean.setRutaReporteJasper(rutaJasper);
      
      nombreArchivo = Utilidades.generateRandomNumber(10);
      prutaReporte = "SEGUIMIENTOEMITIDO_" + nombreArchivo + extensionArch;
      deNoDoc = "temp|SEGUIMIENTOEMITIDO_" + fecha + extensionArch;
      
      Utilidades util = new Utilidades();
      byte[] archivo = util.GenerarReporteLista(buscarDocTrazabilidadBean.getRutaReporteJasper(), lista, coReporte, parametros, tipoArchivo);
      
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
      
      } catch (JRException ex) 
    	{ Logger.getLogger(ConsultaEmiDocServiceImp.class.getName()).log(Level.SEVERE, null, (Throwable)ex); eserror = "1";
    	return objReporte;
    	} 
    catch (FileNotFoundException ex) {
    	Logger.getLogger(ConsultaEmiDocServiceImp.class.getName()).log(Level.SEVERE, null, ex);
    	eserror = "1"; return objReporte;
    	} catch (IOException ex) { 
    		Logger.getLogger(ConsultaEmiDocServiceImp.class.getName()).log(Level.SEVERE, null, ex); eserror = "1"; 
    		return objReporte; 
    	}
    finally {
    	Exception exception = null; objReporte.setcoRespuesta(coRespuesta); objReporte.setdeRespuesta(deRespuesta);
    	objReporte.setnoUrl(prutaReporte);
    	objReporte.setnoDoc(deNoDoc);
    	}
     } 
  	public List<DocumentoSeguiTrazaBean> getListaReporte(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean)  { 
        return seguiTrazabilidadDao.getListDocSeguiTrazab(buscarDocumentoSeguiEstEmiBean);
    	// return this.seguiTrazabilidadDao.getListaReporteBusqueda(buscarDocumentoSeguiEstEmiBean);
     }


	@Override
	public List<DocumentosTrazaBean> existeDocumentoTraza(BuscarDocTrazabilidadBean bean) throws Exception {
		
		return seguiTrazabilidadDao.existeDocumentoTraza(bean);
		
	}


	@Override
	public List<DocumentosTrazaBean> listaExpedienteOrigen(BuscarDocTrazabilidadBean bean) throws Exception {
		// TODO Auto-generated method stub
		return seguiTrazabilidadDao.listaExpedienteOrigen( bean);
	}





	

	



}
