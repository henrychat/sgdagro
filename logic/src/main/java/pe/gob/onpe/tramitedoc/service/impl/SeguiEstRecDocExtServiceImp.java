package pe.gob.onpe.tramitedoc.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.libreria.util.Utility;
import pe.gob.onpe.tramitedoc.bean.DestinatarioDocumentoEmiConsulBean;
import pe.gob.onpe.tramitedoc.bean.DocExtRecSeguiEstBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaDocExtRecepBean;
import pe.gob.onpe.tramitedoc.bean.ReporteBean;
import pe.gob.onpe.tramitedoc.dao.SeguiEstRecDocExtDao;
import pe.gob.onpe.tramitedoc.service.SeguiEstRecDocExtService;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;















@Service("seguiEstRecDocExtService")
public class SeguiEstRecDocExtServiceImp   implements SeguiEstRecDocExtService {
  @Autowired
  ApplicationProperties applicationProperties;
  @Autowired
  private SeguiEstRecDocExtDao seguiEstRecDocExtDao;
  
  public List<DocExtRecSeguiEstBean> getLsDocExtRecSegui(DocExtRecSeguiEstBean docBuscar) {
    List<DocExtRecSeguiEstBean> list = null;

    
    try {
      list = this.seguiEstRecDocExtDao.getLsDocExtRecSegui(docBuscar);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public String getObjJson(String[] params) {
    String retval = null;
    try {
      if (params != null) {
        String coDepEmi = params[0].toString();
        String deDepEmi = params[1].toString();
        Writer strWriter = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(strWriter, deDepEmi);
        deDepEmi = strWriter.toString();
        String fechaActual = params[2].toString();
        String annioActual = params[3].toString();
        retval = "{\"coDepEmi\":\"" + coDepEmi + "\",\"deDepEmi\":" + deDepEmi + ",\"fechaActual\":\"" + fechaActual + "\",\"annioActual\":\"" + annioActual + "\"}";
      } 
    } catch (Exception e) {
      retval = null;
    } 
    return retval;
  }

  
  public DocExtRecSeguiEstBean getDocumentoExtSeguiBean(String nuAnn, String nuEmi, String nuDes) {
    DocExtRecSeguiEstBean docExt = null;
    try {
      docExt = this.seguiEstRecDocExtDao.getDocumentoExtSeguiBean(nuAnn, nuEmi, nuDes);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return docExt;
  }

  
  public List<DestinatarioDocumentoEmiConsulBean> getLsDestinoEmi(String pnuAnn, String pnuEmi) {
    List<DestinatarioDocumentoEmiConsulBean> list = null;
    try {
      list = this.seguiEstRecDocExtDao.getLsDestinoEmi(pnuAnn, pnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public List<ReferenciaDocExtRecepBean> getLsRefDocExterno(String pnuAnn, String pnuEmi) {
    List<ReferenciaDocExtRecepBean> list = null;
    try {
      if (pnuAnn != null && pnuAnn.trim().length() > 0 && pnuEmi != null && pnuEmi.trim().length() > 0) {
        list = this.seguiEstRecDocExtDao.getLsRefDocExterno(pnuAnn, pnuEmi);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }


  
  public String getRutaReporte(DocExtRecSeguiEstBean docBuscar) { return this.seguiEstRecDocExtDao.getRutaReporte(docBuscar); }



  
  public List<DocExtRecSeguiEstBean> getListaReporte(DocExtRecSeguiEstBean docBuscar) { return this.seguiEstRecDocExtDao.getListaReporteBusqueda(docBuscar); }


  
  public ReporteBean getGenerarReporte(DocExtRecSeguiEstBean docBuscar, Map parametros) {
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
      
      List<DocExtRecSeguiEstBean> lista = null;
      try {
        lista = getListaReporte(docBuscar);
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
      if (docBuscar.getFormatoReporte().equalsIgnoreCase("PDF")) {
        coReporte = "TDR392";
        extensionArch = ".pdf";
        tipoArchivo = 2;
      } else {
        coReporte = "TDR392_XLS";
        extensionArch = ".xls";
        tipoArchivo = 1;
      } 
      String rutaJasper = docBuscar.getRutaReporteJasper() + "/" + coReporte + ".jasper";
      docBuscar.setRutaReporteJasper(rutaJasper);
      
      nombreArchivo = Utilidades.generateRandomNumber(10);
      prutaReporte = "SEGUI_DOC_EXT_" + nombreArchivo + extensionArch;
      deNoDoc = "temp|SEGUI_DOC_EXT_" + fecha + extensionArch;
      
      Utilidades util = new Utilidades();
      byte[] archivo = util.GenerarReporteLista(docBuscar.getRutaReporteJasper(), lista, coReporte, parametros, tipoArchivo);
      
      String rutaBase = this.applicationProperties.getRutaTemporal() + "/" + prutaReporte;
      
      File tmpFile = new File(rutaBase);
      FileOutputStream fos = new FileOutputStream(tmpFile);
      fos.write(archivo);
      fos.flush();
      fos.close();
      String rutaUrl = "reporte?coReporte=";
      prutaReporte = rutaUrl + prutaReporte;

      return objReporte; 
      } catch (Exception ex) {
    	 eserror = "1"; 
    	return objReporte; 
     }
     finally  { 
       Exception exception = null;
      objReporte.setcoRespuesta(coRespuesta);
      objReporte.setdeRespuesta(deRespuesta);
      objReporte.setnoUrl(prutaReporte);
      objReporte.setnoDoc(deNoDoc); }
  
  }


	@Override
	public List<DocExtRecSeguiEstBean> getListaReporteResumen(DocExtRecSeguiEstBean docBuscar) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public ReporteBean getGenerarReporteReumen(DocExtRecSeguiEstBean docBuscar, Map parametros) {
		// TODO Auto-generated method stub
		return null;
	}
}
