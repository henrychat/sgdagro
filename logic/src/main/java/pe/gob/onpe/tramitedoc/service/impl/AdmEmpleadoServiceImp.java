package pe.gob.onpe.tramitedoc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.autentica.service.UsuarioService;
import pe.gob.onpe.libreria.util.Utility;
import pe.gob.onpe.tramitedoc.bean.AdmEmpleadoAccesoBean;
import pe.gob.onpe.tramitedoc.bean.AdmEmpleadoBean;
import pe.gob.onpe.tramitedoc.bean.AdmEmpleadoCargoBean;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.dao.AdmEmpleadoDao;
import pe.gob.onpe.tramitedoc.service.AdmEmpleadoService;



@Service("admEmpleadoService")
public class AdmEmpleadoServiceImp
  implements AdmEmpleadoService
{
  @Autowired
  public AdmEmpleadoDao admEmpleadoDao;
  @Autowired
  public UsuarioService usuarioService;
  
  public AdmEmpleadoBean getAdmEmpleado(String codEmp) throws Exception {
    AdmEmpleadoBean empleado = new AdmEmpleadoBean();
    try {
      empleado = this.admEmpleadoDao.getAdmEmpleado(codEmp);
    } catch (Exception e) {
      throw e;
    } 
    return empleado;
  }

  public int updAdmEmpleado(AdmEmpleadoBean empleado, AdmEmpleadoAccesoBean acceso) throws Exception {
    int vReturn = 0;
    try {
      vReturn = this.admEmpleadoDao.updAdmEmpleado(empleado);
      String vnuDni = Utility.getInstancia().cifrar(empleado.getDni(), "SgDPasswordSecretPasswor");
      if (acceso.getCoUsuario().length() > 0)
      {
        this.admEmpleadoDao.saveNuevoAcceso(acceso.getCoUsuario(), acceso.getCoEmpleado(), vnuDni, acceso.getInAD());
      }
      
      this.admEmpleadoDao.updAcceso(acceso.getCoUsuario(), acceso.getCoEmpleado(), vnuDni, acceso.getInAD());
    } catch (Exception e) {
      throw e;
    } 
    return vReturn;
  }


  public String nuevoAdmEmpleado(AdmEmpleadoBean empleado, AdmEmpleadoAccesoBean acceso, Usuario usuario) throws Exception {
    String vResult = "NO_OK";
    try {
      empleado.setCoEmpleado(this.admEmpleadoDao.getNextCoEmpleado());
      if (empleado.getCoEmpleado() != null && empleado.getCoEmpleado().trim().length() > 0) {
        vResult = this.admEmpleadoDao.nuevoAdmEmpleado(empleado, usuario);
        if (vResult.equals("OK")) {
          
          acceso.setCoEmpleado(empleado.getCoEmpleado());
          String vnuDni = Utility.getInstancia().cifrar(empleado.getDni(), "SgDPasswordSecretPasswor");
          this.admEmpleadoDao.saveNuevoAcceso(acceso.getCoUsuario(), acceso.getCoEmpleado(), vnuDni, acceso.getInAD());
        } 
      } 
    } catch (Exception e) {
      throw e;
    } 
    return vResult;
  }


  public AdmEmpleadoBean getPersonaDesdeDni(String dni) throws Exception {
    try {
      return this.admEmpleadoDao.getPersonaDesdeDni(dni);
    } catch (Exception e) {
      throw e;
    } 
  }

  
  public AdmEmpleadoBean getAdmEmpleadoDesdeDni(String dni) throws Exception {
    try {
      return this.admEmpleadoDao.getAdmEmpleadoDesdeDni(dni);
    } catch (Exception e) {
      throw e;
    } 
  }

  
  public List<AdmEmpleadoBean> getBsqAdmEmpleado(String dni, String apPaterno, String apMaterno, String nombres, String usuario) throws Exception {
    try {
      return this.admEmpleadoDao.getBsqAdmEmpleado(dni, apPaterno, apMaterno, nombres, usuario);
    } catch (Exception e) {
      throw e;
    } 
  }

  
  public List<DependenciaBean> getBsqDependencia(String criterio) throws Exception {
    try {
      return this.admEmpleadoDao.getBsqDependencia(criterio);
    } catch (Exception e) {
      throw e;
    } 
  }

 
  public List<AdmEmpleadoCargoBean> getLsCargo() throws Exception {
    try {
      return this.admEmpleadoDao.getLsCargo();
    } catch (Exception e) {
      throw e;
    } 
  }

 
  public AdmEmpleadoAccesoBean getAcceso(String coEmpleado) throws Exception {
    try {
      return this.admEmpleadoDao.getAcceso(coEmpleado);
    } catch (Exception e) {
      throw e;
    } 
  }

 
  public List<AdmEmpleadoAccesoBean> getLsAcceso(AdmEmpleadoAccesoBean acceso) throws Exception {
    try {
      return this.admEmpleadoDao.getLsAcceso(acceso);
    } catch (Exception e) {
      throw e;
    } 
  }

 
  public boolean deleteAcceso(AdmEmpleadoAccesoBean acceso) throws Exception {
    try {
      acceso = this.admEmpleadoDao.getAcceso(acceso.getCoEmpleado());
      if (acceso != null) {
        this.usuarioService.delUser(acceso.getCoUsuario());
      }
      return true;
    } catch (Exception e) {
      throw e;
    } 
  }


  public String saveNuevoAcceso(AdmEmpleadoAccesoBean acceso) throws Exception {
    try {
      return this.admEmpleadoDao.saveNuevoAcceso(acceso.getCoUsuario(), acceso.getCoEmpleado(), null, acceso.getInAD());
    } catch (Exception e) {
      throw e;
    } 
  }


  public boolean restablecerAcceso(String coUse, AdmEmpleadoAccesoBean acceso, AdmEmpleadoBean empleado) throws Exception {
    try {
      acceso = this.admEmpleadoDao.getAcceso(acceso.getCoEmpleado());
      if (acceso != null) {
        this.usuarioService.resetPassword(acceso.getCoUsuario(), empleado.getDni(), coUse);
      }
      return true;
    } catch (Exception e) {
      throw e;
    } 
  }
  
  public String cambiarEstadoUsuario(String coEmpleado, String estado, String coUsuario) {
    String vResult = "NO_OK";
    try {
      vResult = this.usuarioService.cambiarEstadoUsuario(coEmpleado, estado, coUsuario);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vResult;
  }
}
