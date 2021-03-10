package pe.gob.onpe.tramitedoc.web.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.autentica.model.UsuarioAcceso;
import pe.gob.onpe.libreria.util.ServletUtility;
import pe.gob.onpe.tramitedoc.bean.DocumentoVerBean;
import pe.gob.onpe.tramitedoc.bean.UsuarioConfigBean;

public class Utilidades {
  private static boolean instanciated = false;
  
  private static Utilidades instancia;
  
  @Autowired
  private ApplicationProperties applicationProperties;
  
  public static Utilidades getInstancia() {
    if (!instanciated) {
      instancia = new Utilidades();
      instanciated = true;
    } 
    return instancia;
  }
  
  public void saveUserInCookie(HttpServletResponse response, Usuario usuario) {
    String serial = usuario.getNuDni() + "/" + usuario.getDeApellidoPaterno() + "/" + usuario.getDeApellidoMaterno() + "/" + usuario.getDePrenombres();
    agregarCookie(response, "Usuario", serial, true);
  }
  
  public void saveAccessInCookie(HttpServletResponse response, Usuario usuario) {
    String serial = "";
    for (UsuarioAcceso usuarioAcceso : usuario.getUsuarioAccesos()) {
      if (!serial.equals(""))
        serial = serial + "/"; 
      serial = serial + usuarioAcceso.getCoModulo() + usuarioAcceso.getCoOpcion();
    } 
    agregarCookie(response, "Acceso", serial, false);
  }
  
  private void agregarCookie(HttpServletResponse response, String id, String valor, boolean expire) {
    Cookie cookie = new Cookie(id, valor);
    response.addCookie(cookie);
  }
  
  public void saveSessionInCookie(HttpServletResponse response, Usuario usuario) {
    saveUserInCookie(response, usuario);
    saveAccessInCookie(response, usuario);
    agregarCookie(response, "State", "1", false);
  }
  
  public Usuario loadUserFromCookie(HttpServletRequest request) {
    Usuario usuario = new Usuario();
    String user = giveMeCoockieValue(request, "Usuario");
    splitAndPopuleUser(user, usuario);
    return usuario;
  }
  
  public void splitAndPopuleUser(String user, Usuario usuario) {
    if (!user.equals("")) {
      String[] str = user.split("/");
      if (str.length == 6) {
        usuario.setNuDni(str[0]);
        usuario.setDeApellidoPaterno(str[1]);
        usuario.setDeApellidoMaterno(str[2]);
        usuario.setDePrenombres(str[3]);
      } 
    } 
  }
  
  public String giveMeCoockieValue(HttpServletRequest request, String cookieName) {
    String retval = "";
    Cookie[] cookies = request.getCookies();
    if (cookies != null)
      for (int i = 0; i < cookies.length; i++) {
        if (cookies[i].getName().equals(cookieName)) {
          retval = cookies[i].getValue();
          break;
        } 
      }  
    return retval;
  }
  
  public void loadAccessFromCookie(HttpServletRequest request, Usuario usuario) {
    String acces = giveMeCoockieValue(request, "Acceso");
    if (!acces.equals("")) {
      String[] str = acces.split("/");
      List<UsuarioAcceso> usuarioAccesos = new ArrayList<UsuarioAcceso>();
      for (String x : str)
        usuarioAccesos.add(new UsuarioAcceso(x.substring(1, 2), x.substring(3))); 
      usuario.setUsuarioAccesos(usuarioAccesos);
    } 
  }
  
  public void setCatcha(HttpServletRequest request, String valor) {
    ServletUtility.getInstancia().saveRequestAttribute(request, "CAPTCHA", valor);
  }
  
  public String getCatcha(HttpServletRequest request) {
    return (String)ServletUtility.getInstancia().loadSessionAttribute(request, "CAPTCHA");
  }
  
  public Usuario loadUserFromSession(HttpServletRequest request) {
    return (Usuario)ServletUtility.getInstancia().loadSessionAttribute(request, "usuario");
  }
  
  public UsuarioConfigBean loadUserConfigFromSession(HttpServletRequest request) {
    return (UsuarioConfigBean)ServletUtility.getInstancia().loadSessionAttribute(request, "usuarioConfig");
  }
  
  public DocumentoVerBean loadDocFromSession(HttpServletRequest request) {
    return (DocumentoVerBean)ServletUtility.getInstancia().loadSessionAttribute(request, "docSession");
  }
  
  public void readHttpAndWriteInOutputStream(InputStream in, HttpServletResponse response, String contentType) throws Exception {
    try {
      long h = 0L;
      int length = -1;
      byte[] buffer = new byte[4096];
      response.setContentType(contentType);
      ServletOutputStream ouputStream = response.getOutputStream();
      while ((length = in.read(buffer)) != -1) {
        ouputStream.write(buffer, 0, length);
        ouputStream.flush();
        h += length;
      } 
      ouputStream.flush();
      ouputStream.close();
    } catch (Exception e) {
      throw e;
    } 
  }
  
  public StringBuffer readHttpAndWriteInOutput(InputStream in) throws Exception {
    StringBuffer sf = new StringBuffer();
    try {
      BufferedReader input = new BufferedReader(new InputStreamReader(in));
      String line = "";
      while ((line = input.readLine()) != null)
        sf.append(line.trim()); 
    } catch (Exception e) {
      throw e;
    } 
    return sf;
  }
  
  public String procesaReportePrimerIntento(HttpServletResponse response, String urlReporte) throws IOException, ServletException {
    String urlRetval = null;
    HttpURLConnection m_con = null;
    InputStream in = null;
    String contentType = null;
    try {
      URL url = new URL(urlReporte.toString().replace(" ", "%20"));
      m_con = (HttpURLConnection)url.openConnection();
      if (m_con != null) {
        contentType = m_con.getHeaderField("Content-Type");
        if (contentType != null && contentType.equals("application/pdf")) {
          in = new BufferedInputStream(m_con.getInputStream());
          readHttpAndWriteInOutputStream(in, response, contentType);
        } else {
          urlRetval = "forward:srReporteContingencia.do";
        } 
      } else {
        urlRetval = "forward:srReporteContingencia.do";
      } 
    } catch (Exception e) {
      urlRetval = "forward:srReporteContingencia.do";
      e.printStackTrace();
    } finally {
      try {
        if (in != null)
          in.close(); 
        if (m_con != null)
          m_con.disconnect(); 
      } catch (Exception e) {
        e.printStackTrace();
      } 
    } 
    return urlRetval;
  }
  
  public String procesaReporteSegundoIntento(HttpServletResponse response, String urlReporte, Model model) throws IOException, ServletException {
    String urlRetval = null;
    ServletOutputStream ouputStream = null;
    HttpURLConnection m_con = null;
    InputStream in = null;
    String contentType = null;
    try {
      URL url = new URL(urlReporte.toString().replace(" ", "%20"));
      m_con = (HttpURLConnection)url.openConnection();
      if (m_con != null) {
        contentType = m_con.getHeaderField("Content-Type");
        if (contentType != null && contentType.equals("application/pdf")) {
          in = new BufferedInputStream(m_con.getInputStream());
          readHttpAndWriteInOutputStream(in, response, contentType);
        } else {
          urlRetval = "reportError";
          StringBuffer sf = readHttpAndWriteInOutput(m_con.getInputStream());
          model.addAttribute("textError", sf.toString());
        } 
      } else {
        urlRetval = "reportError";
        model.addAttribute("textError", "<H4>No se puede conectar al servidor de reportes, esta fuera de servicio o hay problemas de conexion</h4>");
      } 
    } catch (Exception e) {
      e.printStackTrace();
      response.reset();
      urlRetval = "reportError";
      model.addAttribute("textError", "<H4>Error interno en la ejecucidel reporte</h4>");
      model.addAttribute("detailError", e.getStackTrace());
    } finally {
      try {
        if (in != null)
          in.close(); 
        if (m_con != null)
          m_con.disconnect(); 
      } catch (Exception e) {
        e.printStackTrace();
      } 
    } 
    return urlRetval;
  }
  
  public static String AjustaCampoDerecha(String szBuffer, int tam_dest, String szCar) {
    String szDest = "";
    for (int i = 0; i < tam_dest; i++)
      szDest = szDest.concat(szCar); 
    int iLen = szBuffer.length();
    if (iLen > tam_dest)
      iLen = tam_dest; 
    szDest = szDest.substring(0, tam_dest - iLen) + szBuffer;
    return szDest;
  }
  
  public static String AjustaCampoIzquierda(String szBuffer, int tam_dest, String szCar) {
    int iLen = szBuffer.length();
    StringBuffer sbdest = new StringBuffer();
    for (int i = 0; i < tam_dest - iLen; i++)
      sbdest.append(szCar); 
    if (iLen > tam_dest)
      iLen = tam_dest; 
    return szBuffer + sbdest;
  }
  
  public static String reduceWhitespace(String text) {
    char[] chars = text.trim().toCharArray();
    int start = 0;
    int end = 0;
    int occ = 0;
    for (int i = 0; i < chars.length; i++) {
      if (Character.isWhitespace(chars[i])) {
        occ++;
      } else {
        occ = 0;
      } 
      if (occ < 2)
        chars[end++] = chars[i]; 
    } 
    return new String(chars, start, end);
  }
  
  public static String generateRandomNumber2(int length) {
    StringBuffer buffer = new StringBuffer();
    String characters = "12345678901234567890";
    try {
      int charactersLength = characters.length();
      for (int i = 0; i < length; i++) {
        double index = Math.random() * charactersLength;
        buffer.append(characters.charAt((int)index));
      } 
    } catch (Exception e) {
      buffer.append("0");
    } 
    return buffer.toString();
  }
  
  public static String generateRandomNumber(int length) {
    String randomString = "";
    try {
      double multi = Math.pow(10.0D, length);
      int randomValue = (int)(Math.random() * multi);
      randomString = String.valueOf(randomValue);
    } catch (Exception e) {
      randomString = "0";
    } 
    return randomString;
  }
  public static String obtenerTipoPersona(String tipoPErsona) {
	  String desripcion="";
	 if(tipoPErsona.equals("01")) {
		 return "LE/DNI";
		 
	 } else
	 if(tipoPErsona.equals("02")) {
		 return "CARNE EXTRANJERÍA";	 
	
	 } else
	  if(tipoPErsona.equals("03")) {
		  return "PASAPORTE";		 
	 } else
	 if(tipoPErsona.equals("04")) {
		 return "LIBRETA MILITAR";		 
	 } else
	 if(tipoPErsona.equals("05")) {
		 return "CARNE DE FUERZAS POLICIALES";				 
	 } else
	 if(tipoPErsona.equals("06")) {
		 return "CARNET DE FUERZAS ARMADAS";
	 }  
 
    
  
	return desripcion;  
	  
	  
  }
  public static boolean  esVacio(String descripcion) {
	  if(descripcion==null) {
		   return true;
	  }
	  if(descripcion.trim().equals("")) {
		  return true;
	  }
	  
	  return false;
	  
  }
  

  
   public static String fn_getCleanTextExpReg(String text) {
	    if (text != null && text.trim().length() > 0) {
	      text = text.trim().replaceAll("[^a-zA-Z 0-9ÑñäëïöüÄËÏÖÜÁÉÍÓÚáéíóú]", "").replaceAll("\\s+", " ");
	     }
	     return text;
	}
  
  public static String fn_getCleanTextLenGreathree(String text) {
    if (text != null && text.trim().length() > 0) {
      String[] arrText = text.split(" ");
      String textAux = "";
      for (String value : arrText) {
        if (value.trim().length() > 1)
          textAux = textAux + " " + value; 
      } 
      text = textAux.trim();
    } 
    return text;
  }
  
  public String comparaFechaSinHora(Date date1, Date date2) {
    Date d1 = getDateWithOutTime(date1);
    Date d2 = getDateWithOutTime(date2);
    String msg = "0";
    if (d1.before(d2))
      msg = "1"; 
    if (d1.after(d2))
      msg = "2"; 
    return msg;
  }
  
  public Date getDateWithOutTime(Date fecha) {
    Date res = null;
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(fecha);
    calendar.set(11, 0);
    calendar.set(12, 0);
    calendar.set(13, 0);
    calendar.set(14, 0);
    res = calendar.getTime();
    return res;
  }
  

  public static String fn_getCleanFileName(String text){
      if(text!=null&&text.trim().length()>0){
          text=text.trim().replaceAll("[^-_ABCDEFGHIJKLMNÑOPQRSTUVWXYZabcdefghijklmnñopqrstuvwxyz0-9ÁÉÍÓÚáéíóú. ]", "").replaceAll("\\s+", " ");
      }
      return text;
 }
  
  
  public byte[] GenerarReporte(HttpServletRequest request, HttpServletResponse response, List lista, String nombreReporte, Map parametros, int TipoArchivo) throws JRException {
    HashMap Lparameters = null;
    byte[] repBytes = null;
    Connection conn = null;
    ServletOutputStream ouputStream = null;
    ServletContext sc = request.getSession().getServletContext();
    String rutaArchivo = sc.getRealPath("/reports/" + nombreReporte + ".jasper");
    File reportFile = new File(rutaArchivo);
    try {
      Lparameters = (HashMap)parametros;
      if (TipoArchivo == 1) {
        repBytes = generateXLSOutput(Lparameters, reportFile, lista);
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + nombreReporte + ".xls\"");
      } 
    } catch (Exception e) {
      System.out.println("***************************************************************");
      System.out.println("Error in Report:--> " + nombreReporte + ".jrxml");
      System.out.println("Parametros:--> " + Lparameters.toString());
      System.out.println("***************************************************************");
      e.printStackTrace();
    } 
    return repBytes;
  }
  
  public byte[] GenerarReporteLista(String rutaReporte, List lista, String nombreReporte, Map parametros, int TipoArchivo) throws JRException {
    HashMap Lparameters = null;
    byte[] repBytes = null;
    String rutaArchivo = rutaReporte;
    File reportFile = new File(rutaArchivo);
    try {
      Lparameters = (HashMap)parametros;
      if (TipoArchivo == 1) {
        repBytes = generateXLSOutput(Lparameters, reportFile, lista);
      } else {
        repBytes = generatePDFOutputLista(Lparameters, reportFile, lista);
      } 
    } catch (JRException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } 
    return repBytes;
  }
  
  private byte[] generateXLSOutput(Map Lparameters, File reporte, List lista) throws JRException, SQLException, IOException {
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
        if (byteArrayOutputStream != null)
          byteArrayOutputStream.close(); 
      } catch (Exception e) {
        e.printStackTrace();
      } 
    } 
    return byteArrayOutputStream.toByteArray();
  }
  
  private byte[] generatePDFOutputLista(Map Lparameters, File reporte, List lista) throws JRException, SQLException, IOException {
    byte[] repBytes = null;
    try {
      JasperReport jasperReport = (JasperReport)JRLoader.loadObject(reporte);
      repBytes = JasperRunManager.runReportToPdf(jasperReport, Lparameters, (JRDataSource)new JRBeanCollectionDataSource(lista));
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return repBytes;
  }
  
  public static String numeroRandom(int min, int max) {
    int valor = (int)(Math.random() * (min - max + 1) + max + 1.0D);
    String dato = String.format("%09d", new Object[] { Integer.valueOf(valor) });
    return dato;
  }
  
  public static byte[] readFileToByteArray(File archivo) {
    FileInputStream fis = null;
    byte[] bArray = new byte[(int)archivo.length()];
    try {
      fis = new FileInputStream(archivo);
      fis.read(bArray);
      fis.close();
    } catch (IOException e) {
      e.printStackTrace();
    } 
    return bArray;
  }
  
  public static String generateRandomLetter(int length) {
    String[] _cadena = { 
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
        "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", 
        "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", 
        "4", "5", "6", "7", "8", "9" };
    String[] cadena = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
    String randomString = "";
    try {
      for (int i = 0; i < length; i++) {
        int index = (int)Math.round(Math.random() * 9.0D);
        randomString = randomString + cadena[index];
      } 
    } catch (Exception e) {
      randomString = "0";
    } 
    return randomString;
  }
}
