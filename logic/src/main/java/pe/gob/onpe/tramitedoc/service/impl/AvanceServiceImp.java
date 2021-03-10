package pe.gob.onpe.tramitedoc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.tramitedoc.bean.AvanceBean;
import pe.gob.onpe.tramitedoc.dao.AvanceDao;
import pe.gob.onpe.tramitedoc.service.AvanceService;










@Service("avanceService")
public class AvanceServiceImp
  implements AvanceService
{
  @Autowired
  private AvanceDao avanceDao;
  
  public List<AvanceBean> getListAvance(AvanceBean buscarAvanceBean) {
    List<AvanceBean> list = null;
    try {
      list = this.avanceDao.getListAvance(buscarAvanceBean);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }
  
  public String insertAvance(AvanceBean buscarAvanzadaBean) {
    String vReturn = "ERR";
    try {
      vReturn = this.avanceDao.insertAvance(buscarAvanzadaBean);
    } catch (Exception e) {
      vReturn = "ERR";
    } 
    
    return vReturn;
  }
}
