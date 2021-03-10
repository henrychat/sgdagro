package pe.gob.onpe.tramitedoc.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import pe.gob.onpe.libreria.util.ServletUtility;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoEmiConsulBean;
import pe.gob.onpe.tramitedoc.service.ConsultaEmiDocService;
import pe.gob.onpe.tramitedoc.service.ReporteService;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;











@Service("reporteService")
public class ReporteServiceImp
  implements ReporteService
{
  @Autowired
  private ApplicationProperties applicationProperties;
  @Autowired
  private ConsultaEmiDocService consultaEmiDocService;
  
  public void ejecutaReporte(HttpServletRequest request, HttpServletResponse response, String pdataSource) {
    byte[] repBytes = null;

    
    String coReporte = ServletUtility.getInstancia().loadRequestParameter(request, "coReporte");
    String coParametros = ServletUtility.getInstancia().loadRequestParameter(request, "coParametros");
    String coSubReportes = ServletUtility.getInstancia().loadRequestParameter(request, "coSubReportes");
    String coImagenes = ServletUtility.getInstancia().loadRequestParameter(request, "coImagenes");





    
    Connection conn = null;
    ServletOutputStream ouputStream = null;
    HashMap<String, String> Lparameters = null;
    
    try {
      Lparameters = llenaHashMap(coParametros);
      
      WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
      
      DataSource datasource = (DataSource)applicationContext.getBean(pdataSource);
      conn = datasource.getConnection();
      
      ServletContext sc = request.getSession().getServletContext();

      
      File reportFile = new File(sc.getRealPath(this.applicationProperties.getRutaReportes() + coReporte + ".jasper"));




      
      if (coSubReportes != null && coSubReportes != "") {
        String parametros = coSubReportes.replace("|", "&");
        String[] listPar = parametros.split("&");
        for (int i = 0; i < listPar.length; i++) {
          try {
            String cadena = listPar[i];
            String vparam = cadena.substring(0, cadena.indexOf("="));
            String vvalor = cadena.substring(cadena.indexOf("=") + 1);
            
            File reportFileSubReport = new File(sc.getRealPath(this.applicationProperties.getRutaReportes() + vvalor + ".jasper"));
            Lparameters.put(vparam, reportFileSubReport.toString());
          } catch (Exception e) {
            e.printStackTrace();
          } 
        } 
      } 


      
      if (coImagenes != null && coImagenes != "") {
        String parametros = coImagenes.replace("|", "&");
        String[] listPar = parametros.split("&");
        for (int i = 0; i < listPar.length; i++) {
          try {
            String cadena = listPar[i];
            String vparam = cadena.substring(0, cadena.indexOf("="));
            String vvalor = cadena.substring(cadena.indexOf("=") + 1);
            
            File reportFileImg = new File(sc.getRealPath(this.applicationProperties.getRutaReportes() + vvalor));
            Lparameters.put(vparam, reportFileImg.toString());
          } catch (Exception e) {
            e.printStackTrace();
          } 
        } 
      } 


      
      if (coReporte.contains("XLS")) {
        repBytes = generateXLSOutput(Lparameters, reportFile, conn);
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + coReporte + "\"");

      
      }
      else {

        
        repBytes = generatePDFOutput(Lparameters, reportFile, conn);
        
        response.setContentType("application/pdf");
      } 

      
      response.setContentLength(repBytes.length);
      ouputStream = response.getOutputStream();
      ouputStream.write(repBytes, 0, repBytes.length);
      ouputStream.flush();
    } catch (Exception e) {
      System.out.println("***************************************************************");
      System.out.println("Error in Report:--> " + coReporte + ".jrxml");
      System.out.println("Parametros:--> " + Lparameters.toString());
      System.out.println("***************************************************************");
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (ouputStream != null) {
          ouputStream.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } 
    } 
  }

  
  public void ejecutaReporteLista(HttpServletRequest request, HttpServletResponse response) {
    String coReporte = ServletUtility.getInstancia().loadRequestParameter(request, "coReporte");
    String coParametros = ServletUtility.getInstancia().loadRequestParameter(request, "coParametros");
    String coSubReportes = ServletUtility.getInstancia().loadRequestParameter(request, "coSubReportes");
    String coImagenes = ServletUtility.getInstancia().loadRequestParameter(request, "coImagenes");






    
    ServletOutputStream ouputStream = null;
    HashMap<String, String> Lparameters = null;
    List lista = null;
    int posObjeto = 0;
    int posFiltro = 0;
    int posParametros = 0;
    int posServicio = 0;
    posObjeto = coParametros.indexOf("P_OBJETO") + 9;
    posFiltro = coParametros.indexOf("P_FILTROS") + 10;
    posParametros = coParametros.indexOf("P_OBJETO") - 1;
    posServicio = coParametros.indexOf("P_SERVICIO") + 11;
    String nomObjeto = coParametros.substring(posObjeto, posFiltro - 11);
    String filtros = coParametros.substring(posFiltro, posServicio - 12);
    String params = coParametros.substring(0, posParametros);
    String nomServicio = coParametros.substring(posServicio);
    byte[] repBytes = null;
    String rutaBase = "/reports/";

    
    Object obj = new Object();
    
    try {
      obj = CargarDatosObjeto(nomObjeto, filtros);
    } catch (ClassNotFoundException ex) {
      Logger.getLogger(ReporteServiceImp.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      Logger.getLogger(ReporteServiceImp.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(ReporteServiceImp.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(ReporteServiceImp.class.getName()).log(Level.SEVERE, null, ex);
    } 
    
    try {
      if (nomServicio.equals("consultaEmiDocService.getRutaReporteLista")) {
        lista = this.consultaEmiDocService.getListaReporte((BuscarDocumentoEmiConsulBean)obj);
      }



      
      Lparameters = llenaHashMap(params);
      ServletContext sc = request.getSession().getServletContext();
      
      File reportFile = new File(sc.getRealPath(rutaBase + coReporte + ".jasper"));

      
      if (coImagenes != null && coImagenes != "") {
        String parametros = coImagenes.replace("|", "&");
        String[] listPar = parametros.split("&");
        for (int i = 0; i < listPar.length; i++) {
          try {
            String cadena = listPar[i];
            String vparam = cadena.substring(0, cadena.indexOf("="));
            String vvalor = cadena.substring(cadena.indexOf("=") + 1);
            File reportFileImg = new File(sc.getRealPath(rutaBase + vvalor));
            Lparameters.put(vparam, reportFileImg.toString());
          } catch (Exception e) {
            e.printStackTrace();
          } 
        } 
      } 
      
      if (coReporte.contains("XLS")) {
        repBytes = generateXLSOutputLista(Lparameters, reportFile, lista);
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + coReporte + "\"");
      } else {
        
        repBytes = generatePDFOutputLista(Lparameters, reportFile, lista);
        response.setContentType("application/pdf");
      } 

      
      response.setContentLength(repBytes.length);
      ouputStream = response.getOutputStream();
      ouputStream.write(repBytes, 0, repBytes.length);
      ouputStream.flush();
    } catch (Exception e) {








      
      System.out.println("***************************************************************");
      System.out.println("Error in Report:--> " + coReporte + ".jrxml");
      System.out.println("Parametros:--> " + Lparameters.toString());
      System.out.println("***************************************************************");
      e.printStackTrace();
    } finally {

      
      try {
        if (ouputStream != null) {
          ouputStream.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } 
    } 
  }

  
  public void abrirReporteTemporal(HttpServletRequest request, HttpServletResponse response) {
    String coReporte = ServletUtility.getInstancia().loadRequestParameter(request, "coReporte");

    
    String rutaBase = this.applicationProperties.getRutaTemporal() + "/" + coReporte;
    String coSubReportes = ServletUtility.getInstancia().loadRequestParameter(request, "coSubReportes");


    
    File reportFile = new File(rutaBase);
    ServletOutputStream ouputStream = null;
    
    try {
      byte[] archivoArray = Utilidades.readFileToByteArray(reportFile);


      
      if (coReporte.contains("xls")) {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + coReporte + "\"");
      } else {
        
        response.setContentType("application/pdf");
      } 
      
      response.setContentLength(archivoArray.length);
      ouputStream = response.getOutputStream();
      ouputStream.write(archivoArray, 0, archivoArray.length);
      ouputStream.flush();
    } catch (IOException ex) {
      Logger.getLogger(ReporteServiceImp.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      try {
        if (ouputStream != null) {
          ouputStream.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } 
    } 
  }


  
  private Object CargarDatosObjeto(String nombreObjeto, String valores) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
    Class<?> clase = Class.forName(nombreObjeto);
    Object obj = clase.newInstance();
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    obj = mapper.readValue(valores, clase);
    
    return obj;
  }
  private HashMap llenaHashMap(String parametros) {
    HashMap<Object, Object> parameters = new HashMap<Object, Object>();
    parametros = parametros.replace("|", "&");
    String[] listPar = parametros.split("&");
    for (int i = 0; i < listPar.length; i++) {
      String cadena = listPar[i];
      if (cadena.length() > 0) {
        parameters.put(cadena.substring(0, cadena.indexOf("=")), cadena.substring(cadena.indexOf("=") + 1));
      }
    } 
    return parameters;
  }
  
  private byte[] generatePDFOutput(Map Lparameters, File reporte, Connection conn) throws JRException, SQLException, IOException {
    byte[] repBytes = null;
    try {
      JasperReport jasperReport = (JasperReport)JRLoader.loadObject(reporte);
      repBytes = JasperRunManager.runReportToPdf(jasperReport, Lparameters, conn);
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return repBytes;
  }
  private byte[] generatePDFOutputLista(Map Lparameters, File reporte, List lista) throws JRException, SQLException, IOException {
    byte[] repBytes = null;
    try {
      JasperReport jasperReport = (JasperReport)JRLoader.loadObject(reporte);
      repBytes = JasperRunManager.runReportToPdf(jasperReport, Lparameters, (JRDataSource)new JRBeanCollectionDataSource(lista));
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return repBytes;
  }
  private byte[] generatePDFDirect(Map Lparameters, File reporte, Connection conn) throws JRException, SQLException, IOException {
    byte[] repBytes = null;
    ByteArrayOutputStream baos = null;
    
    try {
      baos = new ByteArrayOutputStream();
      JRPdfExporter exporter = new JRPdfExporter();
      JasperReport jasperReport = (JasperReport)JRLoader.loadObject(reporte);
      JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, Lparameters, conn);
      
      exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
      exporter.setParameter((JRExporterParameter)JRPdfExporterParameter.PDF_JAVASCRIPT, "this.print({bUI: false,bSilent: true,bShrinkToFit: false});");


      
      exporter.setParameter((JRExporterParameter)JRPdfExporterParameter.METADATA_AUTHOR, "Secretaria General - SG");
      exporter.setParameter((JRExporterParameter)JRPdfExporterParameter.METADATA_TITLE, "Sistema Gestion Documental");
      exporter.setParameter((JRExporterParameter)JRPdfExporterParameter.METADATA_CREATOR, "SIO");
      
      exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
      exporter.exportReport();
      repBytes = baos.toByteArray();
    }
    catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (baos != null) {
          baos.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } 
    } 
    
    return repBytes;
  }
  
  private byte[] generateXLSOutput(Map Lparameters, File reporte, Connection conn) throws JRException, SQLException, IOException {
    ByteArrayOutputStream byteArrayOutputStream = null;
    try {
      JasperReport jasperReport = (JasperReport)JRLoader.loadObject(reporte);
      JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, Lparameters, conn);
      byteArrayOutputStream = new ByteArrayOutputStream();
      
      JRXlsExporter exporter = new JRXlsExporter();
      exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
      exporter.setParameter((JRExporterParameter)JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);

      
      exporter.setParameter((JRExporterParameter)JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);



      
      exporter.setParameter((JRExporterParameter)JRXlsExporterParameter.IS_COLLAPSE_ROW_SPAN, Boolean.TRUE);
      exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);

      
      exporter.exportReport();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (byteArrayOutputStream != null) {
          byteArrayOutputStream.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } 
    } 
    return byteArrayOutputStream.toByteArray();
  }
  private byte[] generateXLSOutputLista(Map Lparameters, File reporte, List lista) throws JRException, SQLException, IOException {
    ByteArrayOutputStream byteArrayOutputStream = null;
    try {
      JasperReport jasperReport = (JasperReport)JRLoader.loadObject(reporte);
      JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, Lparameters, (JRDataSource)new JRBeanCollectionDataSource(lista));
      byteArrayOutputStream = new ByteArrayOutputStream();
      
      JRXlsExporter exporter = new JRXlsExporter();
      exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
      exporter.setParameter((JRExporterParameter)JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);

      
      exporter.setParameter((JRExporterParameter)JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);



      
      exporter.setParameter((JRExporterParameter)JRXlsExporterParameter.IS_COLLAPSE_ROW_SPAN, Boolean.TRUE);
      exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);

      
      exporter.exportReport();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (byteArrayOutputStream != null) {
          byteArrayOutputStream.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } 
    } 
    return byteArrayOutputStream.toByteArray();
  }
  private StringBuffer buildReportURL() {
    StringBuffer urlReporte = new StringBuffer();
    urlReporte.append("http://");
    
    return urlReporte;
  }
}
