package pe.gob.onpe.tramitedoc.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.onpe.libreria.exception.validarDatoException;
import pe.gob.onpe.libreria.util.Utility;
import pe.gob.onpe.tramitedoc.bean.CargoEntregaBean;
import pe.gob.onpe.tramitedoc.bean.DetGuiaMesaPartesBean;
import pe.gob.onpe.tramitedoc.bean.DocPedienteEntregaBean;
import pe.gob.onpe.tramitedoc.bean.GuiaMesaPartesBean;
import pe.gob.onpe.tramitedoc.bean.ReporteBean;
import pe.gob.onpe.tramitedoc.bean.TrxGeneraGuiaMpBean;
import pe.gob.onpe.tramitedoc.dao.CargoEntregaDao;
import pe.gob.onpe.tramitedoc.service.CargoEntregaService;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;












@Service("cargoEntregaService")
public class CargoEntregaServiceImp
  implements CargoEntregaService
{
  @Autowired
  private CargoEntregaDao cargoEntregaDao;
  @Autowired
  private ApplicationProperties applicationProperties;
  
  public List<CargoEntregaBean> getCargosEntrega(CargoEntregaBean cargo) {
    List<CargoEntregaBean> list = null;
    try {
      list = this.cargoEntregaDao.getCargosEntrega(cargo);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }

  
  public List<DocPedienteEntregaBean> getDocsPendienteEntrega(DocPedienteEntregaBean busqDoc) {
    List<DocPedienteEntregaBean> list = null;
    try {
      list = this.cargoEntregaDao.getDocsPendienteEntrega(busqDoc);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }

  
  public List<DocPedienteEntregaBean> getDocsPendienteEntrega(List<DocPedienteEntregaBean> docs) {
    List<DocPedienteEntregaBean> list = null;
    try {
      list = getListDepuradoDocPendEntrega(docs);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return list;
  }

  
  private List<DocPedienteEntregaBean> getListDepuradoDocPendEntrega(List<DocPedienteEntregaBean> list) {
    List<DocPedienteEntregaBean> ls = null;
    try {
      ls = new ArrayList<DocPedienteEntregaBean>();
      for (DocPedienteEntregaBean doc : list) {
        if (doc != null) {
          String nuAnn = doc.getNuAnn();
          String nuEmi = doc.getNuEmi();
          String nuDes = doc.getNuDes();
          if (nuAnn != null && nuAnn.trim().length() > 0 && nuEmi != null && nuEmi
            .trim().length() > 0 && nuDes != null && nuDes
            .trim().length() > 0) {
            String vResult = this.cargoEntregaDao.isDocPendienteEnGuiaMp(nuAnn, nuEmi, nuDes);
            if (vResult.equals("0"))
            {
              ls.add(doc);
            }
          } 
        } 
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return ls;
  }

  
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
  public String grabarCargoEntrega(TrxGeneraGuiaMpBean trxGuia) throws Exception {
    String vReturn = "NO_OK";
    
    try {
      String pnuAnnGuia = trxGuia.getNuAnnGuia();
      String pnuGuia = trxGuia.getNuGuia();
      String pcoUsuario = trxGuia.getCoUsuario();
      GuiaMesaPartesBean guia = trxGuia.getGuia();
      List<DetGuiaMesaPartesBean> lsDetGuia = trxGuia.getLsDetGuia();
      
      if (pnuAnnGuia != null && pnuGuia != null && pnuAnnGuia.trim().length() > 1 && pnuGuia.trim().length() > 1) {
        
        trxGuia.setAccionBd("UPD");
        if (guia != null) {
          
          vReturn = Utility.getInstancia().getStringFechaFormat(guia.getFeGuiMp(), "dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm");
          if ("NO_OK".equals(vReturn)) {
            throw new validarDatoException("Error fecha Guia ");
          }
          guia.setFeGuiMp(vReturn);
          guia.setCoUseMod(pcoUsuario);
          vReturn = this.cargoEntregaDao.updGuiaMp(guia);
          if (!vReturn.equals("OK")) {
            throw new validarDatoException("Error Actualizando Cabecera de Guia ");
          }
        } 
      } else {
        
        trxGuia.setAccionBd("INS");
        guia.setCoUseMod(pcoUsuario);
        vReturn = this.cargoEntregaDao.getNroCorrelativoGuiaCabecera(pnuAnnGuia, guia.getCoDepOri());
        if (vReturn.equals("-1")) {
          throw new validarDatoException("Error al obtener ncorrelativo GuCabecera.");
        }
        guia.setNuCorGui(vReturn);
        vReturn = this.cargoEntregaDao.getNroGuiaCabecera(pnuAnnGuia);
        if (vReturn.equals("-1")) {
          throw new validarDatoException("Error al obtener nGuCabecera.");
        }
        guia.setNuGuia(Utility.getInstancia().rellenaCerosIzquierda(vReturn, 10));
        vReturn = this.cargoEntregaDao.insGuiaMp(guia);
        if ("OK".equals(vReturn)) {
          pnuGuia = guia.getNuGuia();
          for (DetGuiaMesaPartesBean detGuia : lsDetGuia) {
            detGuia.setCoUseMod(pcoUsuario);
            vReturn = this.cargoEntregaDao.getNroCorrelativoDetGuiaCabecera(pnuAnnGuia, pnuGuia);
            if (vReturn.equals("-1")) {
              throw new validarDatoException("Error al obtener nCorrelativo Detalle Guia ");
            }
            detGuia.setNuCor(vReturn);
            detGuia.setNuAnnGui(pnuAnnGuia);
            detGuia.setNuGui(pnuGuia);
            vReturn = this.cargoEntregaDao.insDetGuiaMp(detGuia);
            if (!"OK".equals(vReturn)) {
              throw new validarDatoException("Error Insertando Detalle de Guia ");
            }
          } 
        } else {
          
          throw new validarDatoException("Error Insertando Cabecera de Guia");
        }
      
      }
    
    } catch (validarDatoException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new validarDatoException("ERROR EN TRANSACCION 1" + e.getMessage());
    } 
    return vReturn;
  }

  
  public String getJsonRptGrabarCargoEntrega(TrxGeneraGuiaMpBean trxGuia) {
    boolean bandera = false;
    StringBuilder retval = new StringBuilder();
    String paccionBD = trxGuia.getAccionBd();
    try {
      GuiaMesaPartesBean guia = trxGuia.getGuia();
      retval.append("\"accionBd\":\"").append(paccionBD).append("\",");
      if (!paccionBD.equals("UPD"))
      {
        if (paccionBD.equals("INS")) {
          retval.append("\"guiaBean\":{");
          retval.append("\"nuAnn\":\"");
          retval.append(guia.getNuAnn());
          retval.append("\",\"nuCorGuia\":\"");
          retval.append(guia.getNuCorGui());
          retval.append("\",\"nuGuia\":\"");
          retval.append(guia.getNuGuia());
          retval.append("\"},");
        }  } 
      retval.append("\"lstDetGuia\":[");
      for (DetGuiaMesaPartesBean detGuia : trxGuia.getLsDetGuia()) {
        retval.append("{\"nuAnnGuia\":\"");
        retval.append(detGuia.getNuAnnGui());
        retval.append("\",\"nuGuia\":\"");
        retval.append(detGuia.getNuGui());
        retval.append("\",\"nuCorr\":\"");
        retval.append(detGuia.getNuCor());
        retval.append("\",\"nuAnn\":\"");
        retval.append(detGuia.getNuAnn());
        retval.append("\",\"nuEmi\":\"");
        retval.append(detGuia.getNuEmi());
        retval.append("\",\"nuDes\":\"");
        retval.append(detGuia.getNuDes());
        retval.append("\"},");
        bandera = true;
      } 
      if (bandera) {
        retval.deleteCharAt(retval.length() - 1);
        bandera = false;
      } 
      retval.append("]");
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return retval.toString();
  }

  
  public GuiaMesaPartesBean getGuiaMp(String pnuAnnGuia, String pnuGuia) {
    GuiaMesaPartesBean guia = null;
    try {
      guia = this.cargoEntregaDao.getGuiaMp(pnuAnnGuia, pnuGuia);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return guia;
  }

  
  public List<DocPedienteEntregaBean> getDetalleGuiaMp(String pnuAnnGuia, String pnuGuia) {
    List<DocPedienteEntregaBean> list = null;
    try {
      list = this.cargoEntregaDao.getDetalleGuiaMp(pnuAnnGuia, pnuGuia);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return list;
  }

  
  public String anularGuiaMp(GuiaMesaPartesBean guia) {
    String vReturn = "NO_OK";
    String coUseMod = guia.getCoUseMod();
    try {
      guia = this.cargoEntregaDao.getGuiaMp(guia.getNuAnn(), guia.getNuGuia());
      if (guia != null) {
        String esGuiaMp = guia.getEstadoGuia();
        if (esGuiaMp != null && esGuiaMp.equals("0")) {
          esGuiaMp = "9";
          vReturn = this.cargoEntregaDao.updEstadoGuiaMp(esGuiaMp, coUseMod, guia.getNuAnn(), guia.getNuGuia());
          if (!vReturn.equals("OK")) {
            vReturn = "Error anulando Cargo.";
          }
        } else {
          vReturn = "Sse puede anular los Generados.";
        } 
      } else {
        vReturn = "Guno Existe.";
      } 
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return vReturn;
  }

  
  public String getRutaGuia(String pnuAnnGuia, String pnuGuia, String pcoUser) {
    String vResult = "NO_OK";
    String nomReporte = "TDR161";
    String extArch = ".pdf";
    try {
      String urlGuia = ",\"noUrl\":\"reporte?coReporte=" + nomReporte + "&coParametros=P_NU_ANN_GUIA=" + pnuAnnGuia + "|P_NU_GUIA=" + pnuGuia + "|P_USER=" + pcoUser + "&coImagenes=P_LOGO_DIR=logo_onpe.jpg";
      urlGuia = ",\"noDoc\":\"TEMP|" + pnuAnnGuia + pnuGuia + extArch + "\"" + urlGuia;
      vResult = urlGuia;
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return vResult;
  }

  
  public GuiaMesaPartesBean getGuiaMp(List<DocPedienteEntregaBean> docs) {
    DocPedienteEntregaBean docPedEntrega = null;
    List<DocPedienteEntregaBean> docsAux = null;
    GuiaMesaPartesBean guia = null;
    try {
      docsAux = getLsDependenciaDestinoDocExt(docs);
      docPedEntrega = esMismaDependenciaDocExt(docsAux);
      if (docPedEntrega != null) {
        guia = new GuiaMesaPartesBean();
        guia.setCoDepDes(docPedEntrega.getCoDepDes());
        guia.setDeDepDes(docPedEntrega.getDeDepDes());
        guia.setCoLocDes(docPedEntrega.getCoLocDes());
        guia.setDeLocDes(docPedEntrega.getDeLocDes());
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return guia;
  }
  
  private List<DocPedienteEntregaBean> getLsDependenciaDestinoDocExt(List<DocPedienteEntregaBean> docs) {
    List<DocPedienteEntregaBean> docsAux = null;
    try {
      docsAux = new ArrayList<DocPedienteEntregaBean>();
      for (DocPedienteEntregaBean doc : docs) {
        if (doc != null) {
          DocPedienteEntregaBean docPedEntrega = this.cargoEntregaDao.getDependenciaDestinoDocExtRec(doc.getNuAnn(), doc.getNuEmi(), doc.getNuDes());
          docsAux.add(docPedEntrega);
        } 
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return docsAux;
  }
  
  private DocPedienteEntregaBean esMismaDependenciaDocExt(List<DocPedienteEntregaBean> docs) {
    DocPedienteEntregaBean docPedEntrega = null;
    try {
      String coDepDesAnterior = null;
      for (DocPedienteEntregaBean doc : docs) {
        docPedEntrega = doc;
        String coDepDes = doc.getCoDepDes();
        if (coDepDesAnterior != null) {
          if (!coDepDesAnterior.equals(coDepDes)) {
            docPedEntrega = null; break;
          } 
          continue;
        } 
        coDepDesAnterior = coDepDes;
      }
    
    } catch (Exception e) {
      docPedEntrega = null;
      e.printStackTrace();
    } 
    return docPedEntrega;
  }


  
  public List<CargoEntregaBean> getListaReporte(CargoEntregaBean cargo) { return this.cargoEntregaDao.getListaReporteBusqueda(cargo); }


  
  public ReporteBean getGenerarReporte(Map parametros, CargoEntregaBean cargo) throws Exception {
    ReporteBean objReporte = new ReporteBean();
    String eserror = "1";
    String coRespuesta = "0";
    String deRespuesta = "";
    String prutaReporte = "";
    String deNoDoc = "";
    String nombreArchivo = "";
    String coReporte = "";


    
    try { int tipoArchivo = 0;

      
      String fecha = Utility.getInstancia().getStringFechaYYYYMMDDHHmm(new Date());
      
      List<CargoEntregaBean> lista = null;
      try {
        lista = getListaReporte(cargo);
        eserror = "0";
      } catch (Exception ex) {
        eserror = "1";
        deRespuesta = ex.getMessage();
      } 
      if (lista.size() == 0) {
        coRespuesta = "1";
        deRespuesta = "No existe informaci�n para generar reporte.";
        throw new Exception(deRespuesta);
      } 
      
      coReporte = "TDR161";
      String extensionArch = ".pdf";
      tipoArchivo = 2;
      
      String rutaJasper = cargo.getRutaReporteJasper() + "/" + coReporte + ".jasper";
      
      cargo.setRutaReporteJasper(rutaJasper);
      
      nombreArchivo = Utilidades.generateRandomNumber(10);
      prutaReporte = "CARENTREGA_" + nombreArchivo + extensionArch;
      deNoDoc = "temp|CARENTREGA_" + fecha + extensionArch;
      
      Utilidades util = new Utilidades();
      byte[] archivo = util.GenerarReporteLista(cargo.getRutaReporteJasper(), lista, coReporte, parametros, tipoArchivo);
      
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























      
      return objReporte; } catch (JRException ex) { Logger.getLogger(CargoEntregaServiceImp.class.getName()).log(Level.SEVERE, null, (Throwable)ex); eserror = "1"; return objReporte; } catch (FileNotFoundException ex) { Logger.getLogger(CargoEntregaServiceImp.class.getName()).log(Level.SEVERE, null, ex); eserror = "1"; return objReporte; } catch (IOException ex) { Logger.getLogger(CargoEntregaServiceImp.class.getName()).log(Level.SEVERE, null, ex); eserror = "1"; return objReporte; }
    finally
    { Exception exception = null;
      objReporte.setcoRespuesta(coRespuesta);
      objReporte.setdeRespuesta(deRespuesta);
      objReporte.setnoUrl(prutaReporte);
      objReporte.setnoDoc(deNoDoc); }
  
  }
}
