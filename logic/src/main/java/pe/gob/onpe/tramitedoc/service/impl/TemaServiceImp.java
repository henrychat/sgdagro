package pe.gob.onpe.tramitedoc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.tramitedoc.bean.TemaBean;
import pe.gob.onpe.tramitedoc.dao.TemaDao;
import pe.gob.onpe.tramitedoc.service.TemaService;









@Service("temaService")
public class TemaServiceImp
  implements TemaService
{
  @Autowired
  private TemaDao temaDao;
  
  public List<TemaBean> getListTema(TemaBean buscarTemaBean) {
    List<TemaBean> list = null;
    try {
      list = this.temaDao.getListTema(buscarTemaBean);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }
  
  public String updTemaDestinos(String codigoEmision, String codTema) {
    String vReturn = "ERR";
    try {
      vReturn = this.temaDao.updTemaDestinos(codigoEmision, codTema);
    } catch (Exception e) {
      vReturn = "ERR";
    } 
    
    return vReturn;
  }

  
  public String updTemaRemitos(String codigoEmision, String codTema) {
    String vReturn = "ERR";
    try {
      vReturn = this.temaDao.updTemaRemitos(codigoEmision, codTema);
    } catch (Exception e) {
      vReturn = "ERR";
    } 
    
    return vReturn;
  }
  
  public String getEditTemaRemitos(String codigoEmision) {
    String vReturn = "ERR";
    try {
      vReturn = this.temaDao.getEditTemaRemitos(codigoEmision);
    } catch (Exception e) {
      vReturn = "ERR";
    } 
    
    return vReturn;
  }
  
  public String getEditTemaDestinos(String codigoEmision) {
    String vReturn = "ERR";
    try {
      vReturn = this.temaDao.getEditTemaDestinos(codigoEmision);
    } catch (Exception e) {
      vReturn = "ERR";
    } 
    
    return vReturn;
  }
}
