package pe.gob.onpe.tramitedoc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.tramitedoc.bean.RequisitoBean;
import pe.gob.onpe.tramitedoc.dao.RequisitoDao;
import pe.gob.onpe.tramitedoc.service.RequisitoService;










@Service("requisitoService")
public class RequisitoServiceImpl
  implements RequisitoService
{
  @Autowired
  private RequisitoDao requisitoDao;
  
  public List<RequisitoBean> getRequisitoList() {
    List<RequisitoBean> list = null;
    try {
      list = this.requisitoDao.getRequisitoList();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public List<RequisitoBean> getRequisitoBusqList(RequisitoBean requisito) {
    List<RequisitoBean> list = null;
    try {
      list = this.requisitoDao.getRequisitoBusqList(requisito);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }

  
  public String insRequisito(RequisitoBean requisito, String codusuario) {
    String vReturn = "NO_OK";
    try {
      vReturn = this.requisitoDao.insRequisito(requisito, codusuario);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public RequisitoBean getRequisito(String codRequisito) {
    RequisitoBean tupaBean = null;
    try {
      tupaBean = this.requisitoDao.getRequisito(codRequisito);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return tupaBean;
  }

  
  public String updRequisito(RequisitoBean requisito, String codusuario) {
    String vReturn = "NO_OK";
    try {
      requisito.setDescripcion(requisito.getDescripcion().toUpperCase());
      
      vReturn = this.requisitoDao.updRequisito(requisito, codusuario);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String insTupaRequisito(RequisitoBean requisito, String codusuario) {
    String vReturn = "NO_OK";
    try {
      vReturn = this.requisitoDao.insTupaRequisito(requisito, codusuario);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String updTupaRequisito(RequisitoBean requisito, String codusuario) {
    String vReturn = "NO_OK";
    try {
      vReturn = this.requisitoDao.updTupaRequisito(requisito, codusuario);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String eliTupaRequisito(RequisitoBean requisito) {
    String vReturn = "NO_OK";
    try {
      vReturn = this.requisitoDao.eliTupaRequisito(requisito);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public List<RequisitoBean> getTupaRequisitoBusqList(RequisitoBean requisito) {
    List<RequisitoBean> list = null;
    try {
      list = this.requisitoDao.getTupaRequisitoBusqList(requisito);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }
}
