package pe.gob.onpe.tramitedoc.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.onpe.libreria.exception.validarDatoException;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.bean.EmpleadoBean;
import pe.gob.onpe.tramitedoc.bean.LocalDepBean;
import pe.gob.onpe.tramitedoc.bean.TrxDependenciaBean;
import pe.gob.onpe.tramitedoc.dao.CommonQueryDao;
import pe.gob.onpe.tramitedoc.dao.DependenciaDao;
import pe.gob.onpe.tramitedoc.service.DependenciaService;











@Service("dependenciaService")
public class DependenciaServiceImp
  implements DependenciaService
{
  @Autowired
  private DependenciaDao dependenciaDao;
  @Autowired
  private CommonQueryDao commonQueryDao;
  
  public List<DependenciaBean> getAllDependencia(boolean esAdmin, String codDependencia) {
    List<DependenciaBean> list = null;
    try {
      list = this.dependenciaDao.getAllDependencia(esAdmin, codDependencia);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }


  
  public List<DependenciaBean> getDependenciaHijo(String coDepPadre) {
    List<DependenciaBean> list = null;
    try {
      list = this.dependenciaDao.getDependenciaHijo(coDepPadre);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }


  
  public List<DependenciaBean> getBuscaDependencia(String busDep, String busTipo) {
    List<DependenciaBean> list = null;
    try {
      list = this.dependenciaDao.getBuscaDependencia(busDep, busTipo);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }

  
  public DependenciaBean getDependencia(String coDep) {
    DependenciaBean dependenciaBean = null;
    try {
      dependenciaBean = this.dependenciaDao.getDependencia(coDep);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return dependenciaBean;
  }



















  
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
  public String insDependenciaBean(TrxDependenciaBean trxDep, LocalDepBean localDep) throws Exception {
    String vReturn = "NO_OK";
    String[] vReturnAux = null;
    try {
      DependenciaBean dep = trxDep.getDependencia();
      dep.setIdAct(trxDep.getCoUseMod());
      vReturn = this.dependenciaDao.insDependencia(dep);
      if (vReturn.equals("ERROR")) {
        vReturn = "ERROR AL CREAR DEPENDENCIA.";
        throw new validarDatoException(vReturn);
      }  if (vReturn.equals("OK")) {
        if (dep.getTiDependencia().equals("1")) {
          trxDep.setCoDep(dep.getCoDependencia());
        }
        vReturnAux = this.commonQueryDao.getNroCorrLocalDependencia();
        if (vReturnAux != null && vReturnAux[0].equals("OK")) {
          localDep.setNuCorr(vReturnAux[1]);
          localDep.setCoDep(dep.getCoDependencia());
          localDep.setCoUseMod(dep.getIdAct());
          vReturn = this.dependenciaDao.insLocalDependencia(localDep);
          if (vReturn.equals("OK")) {
            String coDep = trxDep.getCoDep();
            ArrayList<EmpleadoBean> lstIntegrante = trxDep.getLstIntegrante();
            for (EmpleadoBean emp : lstIntegrante) {
              vReturn = this.dependenciaDao.insEmpDependencia(emp.getCempCodemp(), coDep);
              if (!vReturn.equals("OK")) {
                vReturn = "ERROR AL AGREGAR EMPLEADO DEPENDENCIA.";
                throw new validarDatoException(vReturn);
              } 
            } 
          } else {
            vReturn = "ERROR AL INSERTAR LOCAL DEPENDENCIA.";
            throw new validarDatoException(vReturn);
          } 
        } else {
          vReturn = "ERROR AL OBTENER NUMERO CORRELATIVO LOCAL DEPENDENCIA.";
          throw new validarDatoException(vReturn);
        } 
      } else {
        throw new validarDatoException(vReturn);
      } 
    } catch (validarDatoException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new validarDatoException("ERROR EN TRANSACCION NUEVA DEPENDENCIA.");
    } 
    return vReturn;
  }

  
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
  public String updDependenciaBean(TrxDependenciaBean trxDep, String coDepAn, LocalDepBean localDep, String pTipoAnt) throws Exception {
    String vReturn = "NO_OK";
    String[] vReturnAux = null;
    try {
      DependenciaBean dep = trxDep.getDependencia();
      if (dep != null) {
        dep.setIdAct(trxDep.getCoUseMod());
        if (coDepAn.equals(dep.getCoDependencia())) {
          vReturn = this.dependenciaDao.updDependencia(dep, coDepAn, pTipoAnt);
          if (vReturn.equals("ERROR")) {
            vReturn = "ERROR AL ACTUALIZAR DEPENDENCIA.";
            throw new validarDatoException(vReturn);
          }  if (vReturn.equals("DUPLICADO")) {
            throw new validarDatoException("CDependencia Duplicado.");
          }

          
          dep.setCoDependencia(vReturn);
          trxDep.setCoDep(vReturn);
          vReturn = this.dependenciaDao.delLocalDependencia(coDepAn);
          if (vReturn.equals("OK")) {
            vReturn = this.dependenciaDao.delLocalDependencia(dep.getCoDependencia());
            if (vReturn.equals("OK")) {
              vReturnAux = this.commonQueryDao.getNroCorrLocalDependencia();
              if (vReturnAux != null && vReturnAux[0].equals("OK")) {
                localDep.setNuCorr(vReturnAux[1]);
                localDep.setCoDep(dep.getCoDependencia());
                vReturn = this.dependenciaDao.insLocalDependencia(localDep);
                if (!vReturn.equals("OK")) {
                  vReturn = "ERROR AL INSERTAR LOCAL DEPENDENCIA.";
                  throw new validarDatoException(vReturn);
                } 
              } else {
                vReturn = "ERROR AL OBTENER NUMERO CORRELATIVO LOCAL DEPENDENCIA.";
                throw new validarDatoException(vReturn);
              } 
            } else {
              vReturn = "ERROR AL BORRAR LOCAL DEPENDENCIA.";
              throw new validarDatoException(vReturn);
            } 
          } else {
            vReturn = "ERROR AL BORRAR LOCAL DEPENDENCIA.";
            throw new validarDatoException(vReturn);
          }
        
        } else {
          
          vReturn = this.dependenciaDao.delLocalDependencia(coDepAn);
          if (vReturn.equals("OK")) {
            vReturn = this.dependenciaDao.updDependencia(dep, coDepAn, pTipoAnt);
            if (vReturn.equals("ERROR")) {
              vReturn = "ERROR AL ACTUALIZAR DEPENDENCIA.ZZ";
              throw new validarDatoException(vReturn);
            }  if (vReturn.equals("DUPLICADO")) {
              throw new validarDatoException("CDependencia Duplicado.");
            }
            dep.setCoDependencia(vReturn);
            trxDep.setCoDep(vReturn);
            vReturnAux = this.commonQueryDao.getNroCorrLocalDependencia();
            if (vReturnAux != null && vReturnAux[0].equals("OK")) {
              localDep.setNuCorr(vReturnAux[1]);
              localDep.setCoDep(dep.getCoDependencia());
              vReturn = this.dependenciaDao.insLocalDependencia(localDep);
              if (!vReturn.equals("OK")) {
                vReturn = "ERROR AL INSERTAR LOCAL DEPENDENCIA.";
                throw new validarDatoException(vReturn);
              } 
            } else {
              vReturn = "ERROR AL OBTENER NUMERO CORRELATIVO LOCAL DEPENDENCIA.";
              throw new validarDatoException(vReturn);
            } 
          } else {
            
            vReturn = "ERROR AL BORRAR LOCAL DEPENDENCIA.";
            throw new validarDatoException(vReturn);
          } 
        } 
      } 
      String coDep = trxDep.getCoDep();
      ArrayList<EmpleadoBean> lstIntegrante = trxDep.getLstIntegrante();
      if (lstIntegrante != null && lstIntegrante.size() > 0) {
        vReturn = this.dependenciaDao.delEmpDependencia(coDep);
        if (vReturn.equals("OK")) {
          for (EmpleadoBean emp : lstIntegrante) {
            vReturn = this.dependenciaDao.insEmpDependencia(emp.getCempCodemp(), coDep);
            if (!vReturn.equals("OK")) {
              vReturn = "ERROR AL AGREGAR EMPLEADO DEPENDENCIA.";
              throw new validarDatoException(vReturn);
            } 
          } 
        } else {
          vReturn = "ERROR AL BORRAR EMPLEADO DEPENDENCIA.";
          throw new validarDatoException(vReturn);
        } 
      } 
    } catch (validarDatoException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new validarDatoException("ERROR EN TRANSACCION ACTUALIZAR DEPENDENCIA.");
    } 
    return vReturn;
  }

  
  public String getCoLocal(String coDep) {
    String pCoLoc = null;
    try {
      LocalDepBean localDep = this.dependenciaDao.getLocalDepBean(coDep);
      if (localDep != null) {
        pCoLoc = localDep.getCoLoc();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return pCoLoc;
  }


  
  public List<EmpleadoBean> getLsEmpDepen(String coDep) {
    List<EmpleadoBean> list = null;
    try {
      list = this.dependenciaDao.getLsEmpDepen(coDep);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    
    return list;
  }
}
