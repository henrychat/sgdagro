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
import pe.gob.onpe.tramitedoc.bean.DestinatarioDocumentoEmiConsulBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiPersConsulBean;
import pe.gob.onpe.tramitedoc.bean.PermisoUsuarioBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaConsulBean;
import pe.gob.onpe.tramitedoc.bean.ReporteBean;
import pe.gob.onpe.tramitedoc.dao.ConsultaEmiDocPersDao;
import pe.gob.onpe.tramitedoc.service.ConsultaEmiDocPerService;
import pe.gob.onpe.tramitedoc.service.PermisoService;
import pe.gob.onpe.tramitedoc.util.Constantes;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;












@Service("consultaEmiDocPerService")
public class ConsultaEmiDocPerServiceImp   implements ConsultaEmiDocPerService
{
  @Autowired
  ApplicationProperties applicationProperties;
  @Autowired
  private ConsultaEmiDocPersDao consultaEmiDocPersDao;
  
  
  
  
  
  @Autowired
  private PermisoService permisoService;
  
  public List<DocumentoEmiPersConsulBean> getDocsPersConsulta(DocumentoEmiPersConsulBean buscarDocPer) {
    List<DocumentoEmiPersConsulBean> list = null;
    try {

    	if (buscarDocPer.isEsIncluyeTodosDoc()==true) {
    		
    		if(buscarDocPer.getTipoAcceso()==null || !buscarDocPer.getTipoAcceso().equals(Constantes.TIPO_ACCESO_TOTAL)) {
    			PermisoUsuarioBean permiso= new  PermisoUsuarioBean();
                permiso.setCempCodEmp(buscarDocPer.getCoEmpleado());
                permiso.setCodAplica(Constantes.COD_SISTEMA_SGD);
                permiso.setCodOpc(Constantes.COD_PERMISO_DOC_EMITIDOS);
                if(permisoService.obtenerPermiso(permiso)) {
             	   buscarDocPer.setTipoAcceso(Constantes.TIPO_ACCESO_TOTAL);    
                } 		
    		}
    		
    	}
    	else {
        	buscarDocPer.setTipoAcceso(null);
        } 
    	
        
      list = this.consultaEmiDocPersDao.getDocsPersConsulta(buscarDocPer);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }

  
  public DocumentoEmiPersConsulBean getDocumentoPersonalEmi(String pnuAnn, String pnuEmi) {
    DocumentoEmiPersConsulBean docPers = null;
    try {
      docPers = this.consultaEmiDocPersDao.getDocumentoPersonalEmi(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return docPers;
  }

  
  public List<DestinatarioDocumentoEmiConsulBean> getLstDestintariotlbEmi(String pnuAnn, String pnuEmi) {
    List<DestinatarioDocumentoEmiConsulBean> list = null;
    try {
      list = this.consultaEmiDocPersDao.getLstDestintariotlbEmi(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public List<ReferenciaConsulBean> getLstDocumReferenciatblEmi(String pnuAnn, String pnuEmi) {
    List<ReferenciaConsulBean> list = null;
    try {
      list = this.consultaEmiDocPersDao.getLstDocumReferenciatblEmi(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }


  
  public String getRutaReporte(DocumentoEmiPersConsulBean buscarDocPer) { return this.consultaEmiDocPersDao.getRutaReporte(buscarDocPer); }


  
  public List<DocumentoEmiPersConsulBean> getListaReporte(DocumentoEmiPersConsulBean buscarDocPer) throws Exception {
	  
	  if (buscarDocPer.isEsIncluyeTodosDoc()==true) {
  		
  		if(buscarDocPer.getTipoAcceso()==null || !buscarDocPer.getTipoAcceso().equals(Constantes.TIPO_ACCESO_TOTAL)) {
  			PermisoUsuarioBean permiso= new  PermisoUsuarioBean();
              permiso.setCempCodEmp(buscarDocPer.getCoEmpleado());
              permiso.setCodAplica(Constantes.COD_SISTEMA_SGD);
              permiso.setCodOpc(Constantes.COD_PERMISO_DOC_EMITIDOS);
              if(permisoService.obtenerPermiso(permiso)) {
           	   buscarDocPer.setTipoAcceso(Constantes.TIPO_ACCESO_TOTAL);    
              } 		
  		}
  		
  	}
  	else {
      	buscarDocPer.setTipoAcceso(null);
      } 
	  
	  return consultaEmiDocPersDao.getListaReporteBusqueda(buscarDocPer); 
	  
  }


  
  public ReporteBean getGenerarReporte(DocumentoEmiPersConsulBean buscarDocPer, Map parametros) {
    ReporteBean objReporte = new ReporteBean();
    String eserror = "1";
    String coRespuesta = "0";
    String deRespuesta = "";
    String prutaReporte = "";
    String deNoDoc = "";
    String nombreArchivo = "";
    String coReporte = "";
    List<DocumentoEmiPersConsulBean> lista = null;

    
    try { 
	    	String extensionArch;
	    	int tipoArchivo = 0;
		
		      
		      String fecha = Utility.getInstancia().getStringFechaYYYYMMDDHHmm(new Date());
		      try {
		        lista = getListaReporte(buscarDocPer);
		        eserror = "0";
		      } catch (Exception e) {
		        eserror = "1";
		        deRespuesta = e.getMessage();
		      } 
		      if (lista.size() == 0) {
		        coRespuesta = "1";
		        deRespuesta = "No existe información para generar reporte.";
		        throw new Exception(deRespuesta);
		      } 
		      
		      if (buscarDocPer.getFormatoReporte().equalsIgnoreCase("PDF")) {
		        coReporte = "TDR31";
		        extensionArch = ".pdf";
		        tipoArchivo = 2;
		      } else {
		        coReporte = "TDR31_XLS";
		        extensionArch = ".xls";
		        tipoArchivo = 1;
		      } 
		      String rutaJasper = buscarDocPer.getRutaReporteJasper() + "/" + coReporte + ".jasper";
		      
		      buscarDocPer.setRutaReporteJasper(rutaJasper);
		      
		      nombreArchivo = Utilidades.generateRandomNumber(10);
		      prutaReporte = "EMISIONPERS_" + nombreArchivo + extensionArch;
		      deNoDoc = "temp|EMISIONPERS_" + fecha + extensionArch;
		      
		      Utilidades util = new Utilidades();
		      byte[] archivo = util.GenerarReporteLista(buscarDocPer.getRutaReporteJasper(), lista, coReporte, parametros, tipoArchivo);
		      
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
		  Logger.getLogger(ConsultaEmiDocPerServiceImp.class.getName()).log(Level.SEVERE, null, (Throwable)ex); 
		  eserror = "1"; 
		  return objReporte; 
    
    } catch (FileNotFoundException ex) {
		  Logger.getLogger(ConsultaEmiDocPerServiceImp.class.getName()).log(Level.SEVERE, null, ex); 
		  eserror = "1"; 
		  return objReporte; 
    } 
    catch (IOException ex){ 
    	Logger.getLogger(ConsultaEmiDocPerServiceImp.class.getName()).log(Level.SEVERE, null, ex); 
    	eserror = "1"; return objReporte;
    
    } catch (Exception e) {
    	Logger.getLogger(ConsultaEmiDocPerServiceImp.class.getName()).log(Level.SEVERE, null, e);
    	eserror = "1"; 
    	return objReporte;
    }
    finally {
      Exception exception = null;
      objReporte.setcoRespuesta(coRespuesta);
      objReporte.setdeRespuesta(deRespuesta);
      objReporte.setnoUrl(prutaReporte);
      objReporte.setnoDoc(deNoDoc); 
      }
  
  }
}
