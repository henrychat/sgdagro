package pe.gob.onpe.tramitedoc.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.libreria.util.Mensaje;
import pe.gob.onpe.tramitedoc.bean.AsignacionFuncionarioBean;
import pe.gob.onpe.tramitedoc.dao.AsignacionFuncionarioDao;
import pe.gob.onpe.tramitedoc.service.AsignacionFuncionarioService;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.RestUtility;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;








@Service
public class AsignacionFuncionarioServiceImp
  implements AsignacionFuncionarioService
{
  @Autowired
  ApplicationProperties applicationProperties;
  @Autowired
  AsignacionFuncionarioDao asignacionFuncionarioDao;
  private static Logger logger = Logger.getLogger(AsignacionFuncionarioServiceImp.class);
  
  public List<AsignacionFuncionarioBean> getListAsignacionFuncionario(AsignacionFuncionarioBean asignacionFuncionarioBean) {
    List<AsignacionFuncionarioBean> lista = null;
    if (asignacionFuncionarioBean != null) {
      lista = this.asignacionFuncionarioDao.getListAsignacionFuncionario(asignacionFuncionarioBean);
    }
    return lista;
  }

  
  public String[] insAsignacionFuncionario(AsignacionFuncionarioBean asignacionFuncionarioBean) {
    String[] msg = new String[3];
    if (asignacionFuncionarioBean != null) {
      
      Date hoy = new Date();
      String msgCompara = Utilidades.getInstancia().comparaFechaSinHora(asignacionFuncionarioBean.getFeInicio(), hoy);
      
      if (asignacionFuncionarioBean.getCoTipoEncargo().equals("1")) {
        asignacionFuncionarioBean.setFeFin(null);
      }
      msg = this.asignacionFuncionarioDao.insAsignacionFuncionario(asignacionFuncionarioBean);
      
      if (msg[0].equals("01")) {
        long coAsignacionFuncionario = Long.parseLong(msg[2]);
        asignacionFuncionarioBean.setCoAsignacionFuncionario(coAsignacionFuncionario);
        msgCompara = Utilidades.getInstancia().comparaFechaSinHora(asignacionFuncionarioBean.getFeInicio(), hoy);
        
        if (msgCompara.equals("0") || msgCompara.equals("1")) {
          ObjectMapper mapper = new ObjectMapper();
          String url = this.applicationProperties.getUrlSgdRestTask();
          try {
            String jsonRest = RestUtility.getInstancia().restAsignarFuncionario(asignacionFuncionarioBean, url);
            Mensaje mensaje = (Mensaje)mapper.readValue(jsonRest, Mensaje.class);
            if (mensaje.getCoRespuesta().equals("01")) {
              msg[1] = "Se registry se actualizla asignacidel funcionario a la dependencia";
              logger.info("SE ACTUALIZEL SERVICIO REST");
            } 
          } catch (IOException ex) {
            msg[1] = "Se registresta pendiente la asignacidel funcionario a la dependencia";
            logger.info("SE ACTUALIZEL SERVICIO REST");
            ex.printStackTrace();
          } catch (Exception ex) {
            msg[1] = "Se registresta pendiente la asignacidel funcionario a la dependencia";
            logger.info("SE ACTUALIZEL SERVICIO REST");
            ex.printStackTrace();
          } 
        } 
      } 
    } 




    
    return msg;
  }


  
  public AsignacionFuncionarioBean getFuncionarioPorDependencia(String coDependencia) { return this.asignacionFuncionarioDao.getFuncionarioPorDependencia(coDependencia); }



  
  public int getExisteProgramacionEmpleado(String coEmpleado) { return this.asignacionFuncionarioDao.getExisteProgramacionEmpleado(coEmpleado); }



  
  public int getExisteProgramacionDependencia(String coDependencia) { return this.asignacionFuncionarioDao.getExisteProgramacionDependencia(coDependencia); }



  
  public String delAsignacionFuncionario(AsignacionFuncionarioBean asignacionFuncionarioBean) { return this.asignacionFuncionarioDao.delAsignacionFuncionario(asignacionFuncionarioBean); }



  
  public AsignacionFuncionarioBean getAsignacionFuncionario(long coAsignacionFuncionarioBean) { return this.asignacionFuncionarioDao.getAsignacionFuncionario(coAsignacionFuncionarioBean); }


  
  public String updAsignacionFuncionario(AsignacionFuncionarioBean asignacionFuncionarioBean) {
    String msg = "0";
    if (asignacionFuncionarioBean != null) {
      
      Date hoy = new Date();
      String msgCompara = "";
      
      msg = this.asignacionFuncionarioDao.updAsignacionFuncionario(asignacionFuncionarioBean);
      
      if (msg.equals("1")) {
        msgCompara = Utilidades.getInstancia().comparaFechaSinHora(asignacionFuncionarioBean.getFeInicio(), hoy);
        if (msgCompara.equals("0") || msgCompara.equals("1")) {
          ObjectMapper mapper = new ObjectMapper();
          String url = this.applicationProperties.getUrlSgdRestTask();
          String jsonRest = RestUtility.getInstancia().restAsignarFuncionario(asignacionFuncionarioBean, url);
          try {
            Mensaje mensaje = (Mensaje)mapper.readValue(jsonRest, Mensaje.class);
            if (mensaje.getCoRespuesta().equals("01")) {
              msg = "2";
              logger.info("SE ACTUALIZEL SERVICIO REST");
            } 
          } catch (IOException ex) {
            ex.printStackTrace();
          } 
        } 
      } 
    } 


    
    return msg;
  }
}
