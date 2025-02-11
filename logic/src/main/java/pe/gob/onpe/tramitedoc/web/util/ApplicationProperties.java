package pe.gob.onpe.tramitedoc.web.util;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import pe.gob.onpe.libreria.util.Utility;

@Component("applicationProperties")
public class ApplicationProperties {
  @Autowired
  @Qualifier("applicationProps")
  private Properties properties;
  
  private String coAplicativo;
  
  private String staticResourceServer;
  
  private String rutaTemporal;
  
  private String reportServerA;
  
  private String reportServerB;
  
  private String reportServiceA;
  
  private String reportServiceB;
  
  private String reportUserId;
  
  private String applicationVersion;
  
  private String localCacheObjectsVersion;
  
  private String resourcesVersion;
  
  private String versionJre;
  
  private String coMotSegui;
  
  private int diasAntesExpira;
  
  private String nroRucInstitu;
  
  private String rutaReportes;
  
  private int topRegistrosConsultas;
  
  private String urlSgdRestTask;
  
  private String siglaInstitucion;
  
  private String logoReporteB64;
  
  private String urlManualAyuda;
  
  private String urlVideoTutorial;
  
  private String nroIntentoLoginMaximo;
  
  private String NroIntentoLoginCaptchaMaximo;
  
  private String ADAmbitoBusquedaUsuario;
  
  private String ADSecureLDAP;
  
  private String ADHost;
  
  private String ADDomain;
  
  private String ADPort;
  
  private String ADValidacionActiva;
  
  private String ADMinutosRestableceIntento;
  
  private String urlConsultaAvanzada;
  
  private String fileSizeMaxMP;
  
  private String filSizeMaxCargo;

  private String urlReniec;

  private String urlSunat;

  public String getUrlSunat() {
    return this.properties.getProperty("URL_SUNAT");
  }

  public String getUrlReniec() {
    return this.properties.getProperty("URL_RENIEC");
  }

  public String getFileSizeMaxMP() {
    return this.properties.getProperty("FILE_SIZE_MAX_MP");
  }
  
  public String getFilSizeMaxCargo() {
    return this.properties.getProperty("FILE_SIZE_MAX_CARGO");
  }
  
  public Properties getProperties() {
    return this.properties;
  }
  
  public String getLogoReporteB64() {
    String retval = "";
    try {
      retval = (this.properties.getProperty("B64_LOGO_REPORTE") == null) ? "" : this.properties.getProperty("B64_LOGO_REPORTE");
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return retval;
  }
  
  public void setProperties(Properties properties) {
    this.properties = properties;
  }
  
  public String getSiglaInstitucion() {
    String retval = "";
    try {
      retval = this.properties.getProperty("sigla_institucion");
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return retval;
  }
  
  public String getRutaReportes() {
    return this.properties.getProperty("ruta_reportes");
  }
  
  public String getNroRucInstitu() {
    String retval = "";
    try {
      retval = Utility.getInstancia().descifrar(this.properties.getProperty("nro_ruc"), "SgDPropertiesSecretPasswor");
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return retval;
  }
  
  public int getDiasAntesExpira() {
    String retval = "";
    try {
      retval = Utility.getInstancia().descifrar(this.properties.getProperty("dias_antes_expira"), "SgDPropertiesSecretPasswor");
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return Integer.parseInt(retval);
  }
  
  public String getCoMotSegui() {
    String retval = "";
    try {
      retval = Utility.getInstancia().descifrar(this.properties.getProperty("co_mot_segui"), "SgDPropertiesSecretPasswor");
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return retval;
  }
  
  public String getCoAplicativo() {
    String retval = "";
    try {
      retval = Utility.getInstancia().descifrar(this.properties.getProperty("co_aplicativo"), "SgDPropertiesSecretPasswor");
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return retval;
  }
  
  public String getRutaTemporal() {
    String retval = "";
    try {
      retval = Utility.getInstancia().descifrar(this.properties.getProperty("ruta_temporal"), "SgDPropertiesSecretPasswor");
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return retval;
  }
  
  public String getApplicationVersion() {
    return this.properties.getProperty("application.version");
  }
  
  public String getLocalCacheObjectsVersion() {
    return this.properties.getProperty("localCacheObjects.version");
  }
  
  public String getResourcesVersion() {
    return this.properties.getProperty("resources.version");
  }
  
  public String getVersionJre() {
    return this.properties.getProperty("version_jre");
  }
  
  public int getTopRegistrosConsultas() {
    String retval = "";
    try {
      retval = Utility.getInstancia().descifrar(this.properties.getProperty("topRegistros_consultas"), "SgDPropertiesSecretPasswor");
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return Integer.parseInt(retval);
  }
  
  public String getFormatoSiglas() {
    String retval = "";
    try {
      retval = this.properties.getProperty("formato_siglas");
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return retval;
  }
  
  public String getUrlSgdRestTask() {
    return this.properties.getProperty("url_sgdRestTask");
  }
  
  public String getReportServerA() {
    return this.properties.getProperty("report_serverA");
  }
  
  public String getReportServerB() {
    return this.properties.getProperty("report_serverB");
  }
  
  public String getReportServiceA() {
    return this.properties.getProperty("report_serviceA");
  }
  
  public String getReportServiceB() {
    return this.properties.getProperty("report_serviceB");
  }
  
  public String getReportUserId() {
    return this.properties.getProperty("report_user_id");
  }
  
  public String getUrlManualAyuda() {
    return this.properties.getProperty("url_manual_ayuda");
  }
  
  public String getUrlVideoTutorial() {
    return this.properties.getProperty("url_video_tutorial");
  }
  
  public String getNroIntentoLoginMaximo() {
    return this.properties.getProperty("NRO_INTENTO_LOGIN_MAXIMO");
  }
  
  public String getNroIntentoLoginCaptchaMaximo() {
    return this.properties.getProperty("NRO_INTENTO_LOGIN_CAPTCHA_MAXIMO");
  }
  
  public String getADAmbitoBusquedaUsuario() {
    return this.properties.getProperty("AD_AMBITO_BUSQUEDA_USUARIO");
  }
  
  public String getADHost() {
    return this.properties.getProperty("AD_HOST");
  }
  
  public String getADDomain() {
    return this.properties.getProperty("AD_DOMAIN");
  }
  
  public String getADPort() {
    return this.properties.getProperty("AD_PORT");
  }
  
  public String getADValidacionActiva() {
    return this.properties.getProperty("AD_VALIDACION_ACTIVA");
  }
  
  public String getADMinutosRestableceIntento() {
    return this.properties.getProperty("AD_MINUTOS_RESTABLECE_INTENTO");
  }
  
  public String urlConsultaAvanzada() {
    return this.properties.getProperty("url_consulta_avanzada");
  }
  
  public String getADSecureLDAP() {
    return this.properties.getProperty("AD_SECURE_LDAP");
  }
}
