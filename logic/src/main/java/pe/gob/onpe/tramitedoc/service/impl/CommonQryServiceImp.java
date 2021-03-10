package pe.gob.onpe.tramitedoc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.tramitedoc.bean.CargoFuncionBean;
import pe.gob.onpe.tramitedoc.bean.DatosInterBean;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.bean.EmpleadoBean;
import pe.gob.onpe.tramitedoc.bean.EmpleadoVoBoBean;
import pe.gob.onpe.tramitedoc.bean.RemitenteBean;
import pe.gob.onpe.tramitedoc.bean.SiElementoBean;
import pe.gob.onpe.tramitedoc.bean.TablaMaestraBean;
import pe.gob.onpe.tramitedoc.dao.CommonQueryDao;
import pe.gob.onpe.tramitedoc.service.CommonQryService;




@Service("commonQryService")
public class CommonQryServiceImp   implements CommonQryService
{
  @Autowired
  private CommonQueryDao commonQueryDao;
  
  public DependenciaBean getDependenciaxCoDependencia(String coDepen) {
    DependenciaBean dependenciaBean = null;
    try {
      dependenciaBean = this.commonQueryDao.getDependenciaxCoDependencia(coDepen);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return dependenciaBean;
  }

  
  public List<RemitenteBean> getListRemitente(String coDepen) {
    List<RemitenteBean> list = null;
    try {
      list = this.commonQueryDao.getListRemitente(coDepen);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return list;
  }

  
  public String isExpedienteDuplicado(String pnuAnnExp, String pcoDepExp, String pcoGru, String pnuCorrExp) {
    String vResult = "1";
    try {
      vResult = this.commonQueryDao.verificarExpedienteDuplicado(pnuAnnExp, pcoDepExp, pcoGru, pnuCorrExp);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vResult;
  }

  
  public List<EmpleadoBean> getLsEmpleado(String coDepen) {
    List<EmpleadoBean> list = null;
    try {
      list = this.commonQueryDao.getLsEmpleado(coDepen);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<CargoFuncionBean> getLsEmpleo() {
    List<CargoFuncionBean> list = null;
    try {
      list = this.commonQueryDao.getLsEmpleo();
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DependenciaBean> getLsDepencia() {
    List<DependenciaBean> list = null;
    try {
      list = this.commonQueryDao.getLsDepencia();
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<SiElementoBean> getLsDepenciaMensjeria() {
    List<SiElementoBean> list = null;
    try {
      list = this.commonQueryDao.getLsDepenciaMensjeria();
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<EmpleadoBean> getLsEmpleadoIntitu(String deEmp) {
    List<EmpleadoBean> list = null;
    try {
      list = this.commonQueryDao.getLsEmpleadoIntitu(deEmp);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return list;
  }

  
  public EmpleadoBean getEmpJefeDep(String coDep) {
    EmpleadoBean empleadoBean = null;
    try {
      empleadoBean = this.commonQueryDao.getEmpJefeDep(coDep);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return empleadoBean;
  }

  
  public List<EmpleadoBean> getLsEmpDepDesEmp(String deEmp, String coDep) {
    List<EmpleadoBean> list = null;
    try {
      list = this.commonQueryDao.getLsEmpDepDesEmp(deEmp, coDep);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<EmpleadoVoBoBean> getLsPersonalVoBo(String nuAnn, String nuEmi) {
    List<EmpleadoVoBoBean> list = null;
    try {
      list = this.commonQueryDao.getLsPersonalVoBo(nuAnn, nuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }



  
  public String obtenerValorParametro(String nombreParametro) { return this.commonQueryDao.obtenerValorParametro(nombreParametro); }


  
  public String getCargoEmpleado(String coDep, String coEmp) {
    String cargoEmpleadoAux = this.commonQueryDao.getCargoEmpleado(coDep, coEmp);
    String cargoEmpleado = "";
    if (hasParam(cargoEmpleadoAux)) {
      cargoEmpleado = cargoEmpleadoAux;
    }
    return cargoEmpleado;
  }

  
  private boolean hasParam(String string1) { return (string1 != null && string1.trim().length() > 0); }



  
  public DatosInterBean DatosInter(String nuAnn, String nuEmi) {
    DatosInterBean list = null;
    try {
      list = this.commonQueryDao.DatosInter(nuAnn, nuEmi);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return list;
  }


@Override
public List<TablaMaestraBean> getLsContenidoTabla(String vTabla) {
	// TODO Auto-generated method stub
	return null;
}


@Override
public List<CargoFuncionBean> getCargoBusqList(CargoFuncionBean cargo) {
	// TODO Auto-generated method stub
	return null;
}


@Override
public String insCargo(CargoFuncionBean requisito, String codusuario) {
	// TODO Auto-generated method stub
	return null;
}






}
