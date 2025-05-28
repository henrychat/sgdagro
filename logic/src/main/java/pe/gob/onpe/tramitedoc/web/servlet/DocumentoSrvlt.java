package pe.gob.onpe.tramitedoc.web.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import pe.gob.onpe.tramitedoc.bean.DocumentoObjBean;
import pe.gob.onpe.tramitedoc.service.DocumentoObjService;
import pe.gob.onpe.tramitedoc.service.DocumentoXmlService;
import pe.gob.onpe.tramitedoc.util.Constantes;
import pe.gob.onpe.tramitedoc.util.LsAleatorioViewDoc;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;

public class DocumentoSrvlt extends HttpServlet {
  private DocumentoObjService documentoObjService;
  
  private DocumentoXmlService documentoXmlService;
  
  private ApplicationProperties applicationProperties;
  
  public void init() throws ServletException {
    super.init();
  }
  
  protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String accion = request.getParameter("accion");
    if (accion.equals("abrirDoc")) {
      loadDocumento(request, response);
    } else if (accion.equals("cargaDoc")) {
      cargaDocumento(request, response);
    } else if (accion.equals("abrirAnexo")) {
      loadAnexo(request, response);
    } else if (accion.equals("abrirAnexos")) {
      loadAnexos(request, response);
    } else if (accion.equals("creaDocx")) {
      creaDocx(request, response);
    } else if (accion.equals("creaPdfx")) {
      creaPdfx(request, response);
    } else if (accion.equals("cargaDocAnexo")) {
      cargaDocumentoAnexo(request, response);
    } 
  }
  
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    processRequest(request, response);
  }
  
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    processRequest(request, response);
  }
  
  private void loadDocumento(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    //System.out.println("loadDocumento");
    String pnuAnn = request.getParameter("nuAnn");
    String pnuEmi = request.getParameter("nuEmi");
    String ptiCap = request.getParameter("tiCap");
    String pnroView = request.getParameter("nroView");
    String pAbreWord = (request.getParameter("pAbreWord") == null) ? "NO" : request.getParameter("pAbreWord");
    if (pnuAnn != null && pnuEmi != null && ptiCap != null && pnroView != null && pAbreWord != null && 
      !pnuAnn.equals("") && !pnuEmi.equals("") && !ptiCap.equals("") && !pnroView.equals("")) {
      boolean bisView = LsAleatorioViewDoc.getInstancia().remove(pnroView);
      if (bisView) {
        bisView = false;
        WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
        this.documentoObjService = (DocumentoObjService)applicationContext.getBean("documentoObjService");
        DocumentoObjBean docObjBean = null;
        if (pAbreWord.equals("SI")) {
          docObjBean = this.documentoObjService.leerDocumento(pnuAnn, pnuEmi, ptiCap, pAbreWord);
        } else {
          docObjBean = this.documentoObjService.leerDocumento(pnuAnn, pnuEmi, ptiCap);
        } 
        if (docObjBean != null) {
          bisView = true;
          if (docObjBean.getTipoDoc() != null) {
            if (docObjBean.getTipoDoc().equals("pdf")) {
              response.setContentType("application/pdf");
            } else if (docObjBean.getTipoDoc().equals("docx")) {
              response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            } else if (docObjBean.getTipoDoc().equals("doc")) {
              response.setContentType("application/msword");
            } else {
              response.setContentType("application/" + docObjBean.getTipoDoc());
            } 
          } else {
            response.setContentType("application/octet-stream");
          } 
          response.setHeader("Content-Disposition", "inline; filename=\"" + docObjBean.getNombreArchivo() + "\"");
          response.setHeader("Pragma", "no-cache");
          response.setHeader("Expires", "0");
          response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
          response.setContentLength((docObjBean.getDocumento()).length);
          ServletOutputStream ouputStream = null;
          ouputStream = response.getOutputStream();
          ouputStream.write(docObjBean.getDocumento(), 0, (docObjBean.getDocumento()).length);
          ouputStream.flush();
          ouputStream.close();
        } 
      } 
      if (!bisView) {
        response.setContentType("application/std");
        response.setHeader("Cache-Control", "no-cache");
        PrintWriter out = response.getWriter();
        out.println("Documento no se encuentra");
        out.close();
        out.flush();
      } 
    } 
  }
  
  private void cargaDocumento(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    boolean vestado = false;
    String pnuAnn = request.getParameter("nuAnn");
    String pnuEmi = request.getParameter("nuEmi");
    String pnuSec = request.getParameter("nuSec");
    String fileName = "ERROR";
    if (pnuAnn != null && pnuEmi != null && pnuSec != null && !pnuAnn.equals("") && !pnuEmi.equals("") && !pnuSec.equals("")) {
      ServletInputStream servletInputStream=null;
      InputStream inputStream = null;
      FileOutputStream outputStream = null;
      WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
      this.applicationProperties = (ApplicationProperties)applicationContext.getBean("applicationProperties");
      try {
        String cabFile = request.getHeader("fileName");
        fileName = pnuAnn + pnuEmi + pnuSec + cabFile.substring(cabFile.lastIndexOf('.'));
        String rutaFile = this.applicationProperties.getRutaTemporal();
        System.out.println("aqui: " + rutaFile);
        System.out.println(rutaFile);
        File saveFile = new File(rutaFile + "//" + fileName);
        servletInputStream = request.getInputStream();
        outputStream = new FileOutputStream(saveFile);
        byte[] buffer = new byte[Constantes.BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = servletInputStream.read(buffer)) != -1)
          outputStream.write(buffer, 0, bytesRead); 
        outputStream.flush();
        outputStream.close();
        servletInputStream.close();
        vestado = true;
      } catch (Exception e1) {
        e1.printStackTrace();
      } finally {
        try {
          if (outputStream != null)
            outputStream.close(); 
          if (servletInputStream != null)
            servletInputStream.close(); 
        } catch (IOException e) {
          e.printStackTrace();
        } 
      } 
    } 
    if (vestado) {
      response.setContentType("text/html; charset=UTF-8");
      response.setHeader("Cache-Control", "no-cache");
      PrintWriter out = response.getWriter();
      out.println(fileName);
      out.close();
      out.flush();
    } else {
      response.setContentType("text/html; charset=UTF-8");
      response.setHeader("Cache-Control", "no-cache");
      PrintWriter out = response.getWriter();
      out.println("ERROR");
      out.close();
      out.flush();
    } 
  }
  
  private void loadAnexo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String pnuAnn = request.getParameter("nuAnn");
    String pnuEmi = request.getParameter("nuEmi");
    String pnuAne = request.getParameter("nuAne");
    if (pnuAnn != null && pnuEmi != null && pnuAne != null && !pnuAnn.equals("") && !pnuEmi.equals("") && !pnuAne.equals("")) {
      WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
      this.documentoObjService = (DocumentoObjService)applicationContext.getBean("documentoObjService");
      DocumentoObjBean docObjBean = null;
      docObjBean = this.documentoObjService.leerDocumentoAnexo(pnuAnn, pnuEmi, pnuAne);
      if (docObjBean != null) {
        response.setContentType("application/octet-stream");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentLength((docObjBean.getDocumento()).length);
        ServletOutputStream ouputStream = null;
        ouputStream = response.getOutputStream();
        ouputStream.write(docObjBean.getDocumento(), 0, (docObjBean.getDocumento()).length);
        ouputStream.flush();
        ouputStream.close();
        
      } else {
        response.setContentType("application/std");
        response.setHeader("Cache-Control", "no-cache");
        PrintWriter out = response.getWriter();
        out.println("Documento no se encuentra");
        out.flush();
        out.close();
        
      } 
    } 
  }
  
  private void loadAnexos(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String pnuAnn = request.getParameter("nuAnn");
    String pnuEmi = request.getParameter("nuEmi");
    if (pnuAnn != null && pnuEmi != null && !pnuAnn.equals("") && !pnuEmi.equals("")) {
      WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
      this.documentoObjService = (DocumentoObjService)applicationContext.getBean("documentoObjService");
      List<DocumentoObjBean> docObjBean = null;
      docObjBean = this.documentoObjService.leerDocumentoAnexo(pnuAnn, pnuEmi);
      if (docObjBean != null && docObjBean.size() > 0) {
        response.setContentType("application/octet-stream");
        response.setHeader("Cache-Control", "no-cache");
        byte[] zips = zip(docObjBean);
        response.setContentLength(zips.length);
        ServletOutputStream ouputStream = null;
        ouputStream = response.getOutputStream();
        ouputStream.write(zips, 0, zips.length);
        ouputStream.close();
        ouputStream.flush();
      } else {
        response.setContentType("application/std");
        response.setHeader("Cache-Control", "no-cache");
        PrintWriter out = response.getWriter();
        out.println("Documento no se encuentra");
        out.close();
        out.flush();
      } 
    } 
  }
  
  private byte[] zip(List<DocumentoObjBean> documentos) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ZipOutputStream zout = null;
    try {
      zout = new ZipOutputStream(bos);
      for (DocumentoObjBean doc : documentos) {
        try {
          ZipEntry ze = new ZipEntry(doc.getNombreArchivo());
          zout.putNextEntry(ze);
          zout.write(doc.getDocumento());
          zout.closeEntry();
        } catch (Exception exception) {
        	exception.printStackTrace();
        }
      } 
    } catch (Exception exception) {
    	exception.printStackTrace();
    } finally {
      if (zout != null)
        zout.close(); 
    } 
    return bos.toByteArray();
  }
  
  private void creaDocx(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    //System.out.println("crea doxc desde cero");
    String pnuAnn = request.getParameter("nuAnn");
    String pnuEmi = request.getParameter("nuEmi");
    if (pnuAnn != null && pnuEmi != null && !pnuAnn.equals("") && !pnuEmi.equals("")) {
      WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
      this.documentoXmlService = (DocumentoXmlService)applicationContext.getBean("documentoXmlService");
      DocumentoObjBean docObjBean = null;
      docObjBean = this.documentoXmlService.crearDocx(pnuAnn, pnuEmi);
      if (docObjBean != null) {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + docObjBean.getNombreArchivo());
        response.setHeader("Cache-Control", "no-cache");
        response.setContentLength((docObjBean.getDocumento()).length);
        ServletOutputStream ouputStream = null;
        ouputStream = response.getOutputStream();
        ouputStream.write(docObjBean.getDocumento(), 0, (docObjBean.getDocumento()).length);
        ouputStream.close();
        ouputStream.flush();
      } else {
        response.setContentType("application/std");
        response.setHeader("Cache-Control", "no-cache");
        PrintWriter out = response.getWriter();
        out.println("Plantilla no Existe para este Documento");
        out.close();
        out.flush();
      } 
    } 
  }
  
  private void creaPdfx(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String pnuAnn = request.getParameter("nuAnn");
    String pnuEmi = request.getParameter("nuEmi");
    String pnuAne = request.getParameter("nuAne");
    WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
    this.documentoXmlService = (DocumentoXmlService)applicationContext.getBean("documentoXmlService");
    DocumentoObjBean docObjBean = null;
    docObjBean = this.documentoXmlService.crearPdfx(pnuAnn, pnuEmi, pnuAne);
    response.setContentType("text/html; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    out.println("Documento no se encuentra");
    out.close();
    out.flush();
  }
  
  private void cargaDocumentoAnexo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    boolean vestado = false;
    String pnuAnn = request.getParameter("nuAnn");
    String pnuEmi = request.getParameter("nuEmi");
    String pnuAne = request.getParameter("nuAne");
    String pnuSec = request.getParameter("nuSec");
    String fileName = "ERROR";
    if (pnuAnn != null && pnuEmi != null && pnuAne != null && pnuSec != null && !pnuAnn.equals("") && !pnuEmi.equals("") && !pnuSec.equals("") && !pnuAne.equals("")) {
      ServletInputStream servletInputStream=null;
      InputStream inputStream = null;
      FileOutputStream outputStream = null;
      WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
      this.applicationProperties = (ApplicationProperties)applicationContext.getBean("applicationProperties");
      try {
        String cabFile = request.getHeader("fileName");
        fileName = pnuAnn + pnuEmi + pnuAne + pnuSec + cabFile.substring(cabFile.lastIndexOf('.'));
        String rutaFile = this.applicationProperties.getRutaTemporal();
        File saveFile = new File(rutaFile + "//" + fileName);
        servletInputStream = request.getInputStream();
        outputStream = new FileOutputStream(saveFile);
        byte[] buffer = new byte[Constantes.BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = servletInputStream.read(buffer)) != -1)
          outputStream.write(buffer, 0, bytesRead); 
        outputStream.flush();
        outputStream.close();
        servletInputStream.close();
        vestado = true;
      } catch (Exception e1) {
        e1.printStackTrace();
      } finally {
        try {
          if (outputStream != null)
            outputStream.close(); 
          if (servletInputStream != null)
            servletInputStream.close(); 
        } catch (IOException e) {
          e.printStackTrace();
        } 
      } 
    } 
    if (vestado) {
      response.setContentType("text/html; charset=UTF-8");
      response.setHeader("Cache-Control", "no-cache");
      PrintWriter out = response.getWriter();
      out.println(fileName);
      out.close();
      out.flush();
    } else {
      response.setContentType("text/html; charset=UTF-8");
      response.setHeader("Cache-Control", "no-cache");
      PrintWriter out = response.getWriter();
      out.println("ERROR");
      out.close();
      out.flush();
    } 
  }
}
