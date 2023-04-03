package pe.gob.onpe.tramitedoc.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.libreria.util.Utility;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoRecepMensajeriaBean;
import pe.gob.onpe.tramitedoc.bean.DestiDocumentoEnvMensajeriaBean;
import pe.gob.onpe.tramitedoc.bean.DestinoResBen;
import pe.gob.onpe.tramitedoc.bean.DocumentoRecepMensajeriaBean;
import pe.gob.onpe.tramitedoc.bean.ReporteBean;
import pe.gob.onpe.tramitedoc.bean.TipoElementoMensajeriaBean;
import pe.gob.onpe.tramitedoc.dao.MensajeriaDao;
import pe.gob.onpe.tramitedoc.service.DocumentoMensajeriaService;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;









@Service("documentoMensajeriaService")
public class DocumentoMensajeriaServiceImp   implements DocumentoMensajeriaService
{
  @Autowired
  private MensajeriaDao mensajeriaDao;
  @Autowired
  ApplicationProperties applicationProperties;
  
  public List<DocumentoRecepMensajeriaBean> getDocumentoRecepMensajeria(BuscarDocumentoRecepMensajeriaBean buscarDocumentoRecepMensajeriaBean) {
    List<DocumentoRecepMensajeriaBean> list = null;
    try {
      buscarDocumentoRecepMensajeriaBean.setCoGrupo("1");
      list = this.mensajeriaDao.getDocumentoRecepMensajeria(buscarDocumentoRecepMensajeriaBean);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }
  
  public List<DestiDocumentoEnvMensajeriaBean> getLstDestinoEnvMensajeria(String pnuAnnpnuEmi) {
    List<DestiDocumentoEnvMensajeriaBean> list = null;
    try {
      list = this.mensajeriaDao.getLstDestinoEnvMensajeria(pnuAnnpnuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }
  
  public List<TipoElementoMensajeriaBean> getlistTipoElementoMensajeria(String tipo) {
    List<TipoElementoMensajeriaBean> list = null;
    try {
      list = this.mensajeriaDao.getlistTipoElementoMensajeria(tipo);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }
  
  public List<TipoElementoMensajeriaBean> getListResponsableMensajeria(String tipo, String Ambito, String tipoEnvio) {
    List<TipoElementoMensajeriaBean> list = null;
    try {
      list = this.mensajeriaDao.getListResponsableMensajeria(tipo, Ambito, tipoEnvio);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }
  
  public String insMensajeriaDocumento(DocumentoRecepMensajeriaBean documentoMensajeria) {
    String vReturn = "ERR";
    try {
      vReturn = this.mensajeriaDao.insMensajeriaDocumento(documentoMensajeria);
    } catch (Exception e) {
      vReturn = "ERR";
    } 
    
    return vReturn;
  }
  
  /*
   * MODIFICADO: 12/07/2022 0027
   * DEVELOPER: ESILVERIO
   */
  public String updMensajeriaDocumentoRecibir(String codigos) {
    String vReturn = "ERR";
    String[] arrCodigo = codigos.split(",");
    try {
    	for (int i = 0; i < arrCodigo.length; i++) {
    		vReturn = this.mensajeriaDao.updMensajeriaDocumentoRecibir(arrCodigo[i]);
    		if ("ERR".equals(vReturn))
    			break;
    	}
    } catch (Exception e) {
      vReturn = "ERR";
    } 
    
    return vReturn;
  }
  
  public String updMensajeriaDocumentoDevolver(String codigos) {
    String vReturn = "ERR";
    try {
      vReturn = this.mensajeriaDao.updMensajeriaDocumentoDevolver(codigos);
    } catch (Exception e) {
      vReturn = "ERR";
    } 
    
    return vReturn;
  }
  
  public String selectCalcularFechaPlazo(DocumentoRecepMensajeriaBean documentoMensajeria) {
    String vReturn = "ERR";
    try {
      vReturn = this.mensajeriaDao.selectCalcularFechaPlazo(documentoMensajeria);
    } catch (Exception e) {
      vReturn = "ERR";
    } 
    
    return vReturn;
  }
  
  public List<DocumentoRecepMensajeriaBean> getLstDetalleMensajeria(DestinoResBen oDestinoResBen) {
    List<DocumentoRecepMensajeriaBean> list = null;
    try {
      list = this.mensajeriaDao.getLstDetalleMensajeria(oDestinoResBen);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }

  
  public ReporteBean getImprimirReporte(BuscarDocumentoRecepMensajeriaBean buscarDocumentoRecepConsulBean, Map parametros) {
    List lista = null;
    ReporteBean objReporte = new ReporteBean();
    String coRespuesta = "";
    String deRespuesta = "";
    String prutaReporte = "";
    String deNoDoc = "";
    String coReporte = "";
    
    String nombreArchivo = "";
    int tipoArchivo = 0;
    
    String eserror = "1";

    
    try { String extensionArch, fecha = Utility.getInstancia().getStringFechaYYYYMMDDHHmm(new Date());
      try {
        lista = this.mensajeriaDao.getEditLstDetalleMensajeria(buscarDocumentoRecepConsulBean.getBusNumDoc());
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
      
      if (buscarDocumentoRecepConsulBean.getFormatoReporte().equalsIgnoreCase("PDF")) {
        extensionArch = ".pdf";
        tipoArchivo = 2;
        coReporte = "TDR_IMPRE_MSJ";
      } else {
        extensionArch = ".xls";
        tipoArchivo = 1;
        coReporte = "TDR_IMPRE_MSJ_XLS";
      } 
      
      String rutaJasper = buscarDocumentoRecepConsulBean.getRutaReporteJasper() + "/" + coReporte + ".jasper";
      buscarDocumentoRecepConsulBean.setRutaReporteJasper(rutaJasper);

      
      nombreArchivo = Utilidades.generateRandomNumber(10);
      prutaReporte = "DOCUSALMJS_" + nombreArchivo + extensionArch;
      deNoDoc = "temp|DOCUSALMJS_" + fecha + extensionArch;
      
      Utilidades util = new Utilidades();
      byte[] archivo = util.GenerarReporteLista(buscarDocumentoRecepConsulBean.getRutaReporteJasper(), lista, coReporte, parametros, tipoArchivo);
      
      if (archivo == null) {
        eserror = "0";
        coRespuesta = eserror;
        throw new Exception("No genercorrectamente el archivo del reporte.");
      } 
      
      String rutaBase = this.applicationProperties.getRutaTemporal() + "/" + prutaReporte;
      
      File tmpFile = new File(rutaBase);
      FileOutputStream fos = new FileOutputStream(tmpFile);
      fos.write(archivo);
      fos.flush();
      fos.close();
      String rutaUrl = "reporte?coReporte=";
      prutaReporte = rutaUrl + prutaReporte;














      
      return objReporte; } catch (Exception e) { Logger.getLogger(ConsultaEmiDocServiceImp.class.getName()).log(Level.SEVERE, null, e); eserror = "1"; return objReporte; }
    finally
    { Exception exception = null;
      objReporte.setcoRespuesta(coRespuesta);
      objReporte.setdeRespuesta(deRespuesta);
      objReporte.setnoUrl(prutaReporte);
      objReporte.setnoDoc(deNoDoc); }
  
  }

@Override
public List<DocumentoRecepMensajeriaBean> getLstDetalleMesaVirtual(DestinoResBen oDestinoResBen) {
	// TODO Auto-generated method stub
	return null;
}
}
