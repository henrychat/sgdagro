package pe.gob.onpe.tramitedoc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.tramitedoc.bean.AvisoBandejaEntradaBean;
import pe.gob.onpe.tramitedoc.bean.EtiquetaBandejaEnBean;
import pe.gob.onpe.tramitedoc.dao.AvisoBandejaEntradaDao;
import pe.gob.onpe.tramitedoc.service.AvisoBandejaEntradaService;



@Service("avisoBandejaEntradaService")
public class AvisoBandejaEntradaServiceImp
  implements AvisoBandejaEntradaService
{
  @Autowired
  private AvisoBandejaEntradaDao avisoBandejaEntradaDao;
  
  public List<AvisoBandejaEntradaBean> getBandejaEntrada(String coDependencia, String coEmpleado, String tipoAcesso) {
    List<AvisoBandejaEntradaBean> list = null;
    try {
      if (tipoAcesso.equals("0")) {
        list = this.avisoBandejaEntradaDao.getBandejaEntradaAccesoTotal(coDependencia, coEmpleado);
      } else if (tipoAcesso.equals("2")) {
        list = this.avisoBandejaEntradaDao.getBandejaEntradaAccesoFuncionario(coDependencia, coEmpleado);
      } else {
        list = this.avisoBandejaEntradaDao.getBandejaEntradaAccesoPersonal(coDependencia, coEmpleado);
      } 
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }

  
  public List<EtiquetaBandejaEnBean> getListEtiquetaBandejaEntrada(String coDep, String coEmp) {
    List<EtiquetaBandejaEnBean> list = null;
    try {
      list = this.avisoBandejaEntradaDao.getListEtiquetaBandejaEntrada(coDep, coEmp);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }
}
