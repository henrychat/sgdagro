package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.bean.EmpleadoBean;
import pe.gob.onpe.tramitedoc.bean.LocalDepBean;
import pe.gob.onpe.tramitedoc.dao.DependenciaDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;



@Repository("dependenciaDao")
public class DependenciaDaoImp
  extends SimpleJdbcDaoBase
  implements DependenciaDao
{
  public List<DependenciaBean> getAllDependencia(boolean esAdmin, String codDependencia) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT DE_DEPENDENCIA, CO_DEPENDENCIA FROM IDOSGD.RHTM_DEPENDENCIA WHERE ");
    if (!esAdmin) {
      sql.append(" CO_DEPENDENCIA='");
      sql.append(codDependencia);
      sql.append("'");
      sql.append(" AND");
    } 
    sql.append(" IN_BAJA='0' ORDER BY 1 ");
    
    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public DependenciaBean getDependencia(String coDep) {
    DependenciaBean dependenciaBean = null;
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT co_dependencia,de_dependencia,in_baja,co_nivel,\nde_corta_depen,co_empleado,IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(co_empleado) de_empleado,\nde_sigla,co_cargo,IDOSGD.PK_SGD_DESCRIPCION_DE_CARGO(co_cargo) de_cargo,co_tipo_encargatura,\n(SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_DOMINIOS('CO_TIPO_ENC',co_tipo_encargatura)) de_tipo_encargatura,\nde_cargo_completo,co_depen_padre,IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(co_depen_padre) de_depen_padre,\ntitulo_dep,\nti_dependencia tiDependencia \nFROM IDOSGD.RHTM_DEPENDENCIA\nWHERE CO_DEPENDENCIA=?");


    try {
      dependenciaBean = (DependenciaBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { coDep });
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return dependenciaBean;
  }


  
  public List<DependenciaBean> getDependenciaHijo(String coDepPadre) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT \nco_dependencia, \nde_dependencia, \nin_baja, \nco_nivel, \nde_corta_depen, \nco_depen_padre, \nco_empleado,\nIDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(co_empleado) de_empleado, \nti_dependencia, \nde_sigla, \nco_cargo, \nIDOSGD.PK_SGD_DESCRIPCION_DE_CARGO(co_cargo) de_cargo,\nco_tipo_encargatura, \n(SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_DOMINIOS('CO_TIPO_ENC',co_tipo_encargatura)) de_tipo_encargatura,\nco_sec_dep, \ntitulo_dep, \nde_cargo_completo \nFROM IDOSGD.RHTM_DEPENDENCIA \nwhere co_depen_padre = ? \norder by co_dependencia");

    
    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { coDepPadre });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DependenciaBean> getBuscaDependencia(String busDep, String busTipo) {
    StringBuffer sql = new StringBuffer();
    String sqlDep = " and ti_dependencia='" + busTipo + "' ";
    if (busTipo.equals("2")) {
      sqlDep = "";
    }
    
    sql.append("SELECT  co_dependencia, ");
    sql.append("        de_dependencia, ");
    sql.append("        in_baja, ");
    sql.append("        co_nivel, ");
    sql.append("        de_corta_depen, ");
    sql.append("        co_depen_padre, ");
    sql.append("        co_empleado, ");
    sql.append("        IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(co_empleado) de_empleado, ");
    sql.append("        ti_dependencia, ");
    sql.append("        de_sigla, ");
    sql.append("        co_cargo, ");
    sql.append("        IDOSGD.PK_SGD_DESCRIPCION_DE_CARGO(co_cargo) de_cargo, ");
    sql.append("        co_tipo_encargatura, ");
    sql.append("        (SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_DOMINIOS('CO_TIPO_ENC',co_tipo_encargatura)) de_tipo_encargatura, ");
    sql.append("        co_sec_dep, ");
    sql.append("        titulo_dep, ");
    sql.append("        de_cargo_completo ");
    sql.append("FROM IDOSGD.RHTM_DEPENDENCIA ");
    sql.append("where de_dependencia like '%'+?+'%' ");
    sql.append(sqlDep);
    sql.append("order by ti_dependencia, co_dependencia ");
    
    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { busDep });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }


  
  public String updDependencia(DependenciaBean dep, String coDepAn, String pTipoAnt) {
    if (pTipoAnt.equals("0") && dep.getTiDependencia().equals("1")) {
      String sql1 = "(SELECT RIGHT(REPLICATE('0',5),5)+CONVERT(VARCHAR(5),COALESCE(CAST(MAX(CO_DEPENDENCIA)AS INT),0)+1) FROM IDOSGD.RHTM_DEPENDENCIA WHERE TI_DEPENDENCIA='1')";
      String coDep = (String)this.jdbcTemplate.queryForObject(sql1, String.class);
      dep.setCoDependencia(coDep);
    } 
    String vReturn = "ERROR";
    StringBuffer sql = new StringBuffer();
    sql.append("UPDATE IDOSGD.RHTM_DEPENDENCIA SET\nCO_DEPENDENCIA='" + dep
        .getCoDependencia() + "' ,DE_DEPENDENCIA=?,IN_BAJA=?,CO_NIVEL=?,DE_CORTA_DEPEN=?,\nCO_DEPEN_PADRE=?,CO_EMPLEADO=?,FE_ACT=CURRENT_TIMESTAMP,ID_ACT=?,\nDE_SIGLA=?,CO_CARGO=?,CO_TIPO_ENCARGATURA=?,TITULO_DEP=?,\nDE_CARGO_COMPLETO=?,TI_DEPENDENCIA=? \nWHERE CO_DEPENDENCIA=?");




    
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { dep.getDeDependencia(), dep.getInBaja(), dep.getCoNivel(), dep
            .getDeCortaDepen(), dep.getCoDepenPadre(), dep.getCoEmpleado(), dep.getIdAct(), dep.getDeSigla(), dep.getCoCargo(), dep
            .getCoTipoEncargatura(), dep.getTituloDep(), dep.getDeCargoCompleto(), dep.getTiDependencia(), coDepAn });
      
      vReturn = dep.getCoDependencia();
    } catch (DuplicateKeyException con) {
      vReturn = "DUPLICADO";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String insDependencia(DependenciaBean dep) {
    if (dep.getTiDependencia().equals("1")) {
      String sql1 = "SELECT RIGHT(REPLICATE('0', 5) + CAST((ISNULL(CAST(MAX(CO_DEPENDENCIA) AS INT), 0) + 1) AS VARCHAR), 5) FROM IDOSGD.RHTM_DEPENDENCIA WHERE TI_DEPENDENCIA='1'";
      String coDep = (String)this.jdbcTemplate.queryForObject(sql1, String.class);
      dep.setCoDependencia(coDep);
    } 
    String vReturn = "ERROR";
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO IDOSGD.RHTM_DEPENDENCIA(\nCO_DEPENDENCIA,DE_DEPENDENCIA,IN_BAJA,CO_NIVEL,DE_CORTA_DEPEN,\nCO_DEPEN_PADRE,CO_EMPLEADO,TI_DEPENDENCIA,FE_CREA,ID_CREA,FE_ACT,ID_ACT,\nDE_SIGLA,CO_CARGO,CO_TIPO_ENCARGATURA,TITULO_DEP,DE_CARGO_COMPLETO)\nVALUES(?,?,'0',?,?,\n?,?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,?,\n?,?,?,?,?)");





    
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { dep.getCoDependencia(), dep.getDeDependencia(), dep.getCoNivel(), dep
            .getDeCortaDepen(), dep.getCoDepenPadre(), dep.getCoEmpleado(), dep.getTiDependencia(), dep.getIdAct(), dep.getIdAct(), dep
            .getDeSigla(), dep.getCoCargo(), dep.getCoTipoEncargatura(), dep.getTituloDep(), dep.getDeCargoCompleto() });
      vReturn = "OK";
    } catch (DuplicateKeyException con) {
      vReturn = "CDependencia Duplicado.";
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }

  
  public String insLocalDependencia(LocalDepBean localDep) {
    String vReturn = "ERROR";
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO IDOSGD.SITM_LOCAL_DEPENDENCIA(CO_DEP,CO_LOC,NU_CORRELATIVO,ES_ELI,\nCO_USE_CRE,FE_USE_CRE,CO_USE_MOD,FE_USE_MOD)\nVALUES(?,?,?,'0',?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP)");


    
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { localDep.getCoDep(), localDep.getCoLoc(), localDep.getNuCorr(), localDep
            .getCoUseMod(), localDep.getCoUseMod() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }

  
  public LocalDepBean getLocalDepBean(String coDep) {
    LocalDepBean localDepBean = null;
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT CO_DEP,CO_LOC,NU_CORRELATIVO NUCORR,ES_ELI FROM IDOSGD.SITM_LOCAL_DEPENDENCIA\nWHERE CO_DEP=?");

    
    try {
      localDepBean = (LocalDepBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(LocalDepBean.class), new Object[] { coDep });
    }
    catch (EmptyResultDataAccessException e) {
      localDepBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return localDepBean;
  }

  
  public String delLocalDependencia(String coDep) {
    String vReturn = "ERROR";
    StringBuffer sql = new StringBuffer();
    sql.append("DELETE FROM IDOSGD.SITM_LOCAL_DEPENDENCIA\nWHERE CO_DEP=?");

    
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { coDep });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String insEmpDependencia(String coEmp, String coDep) {
    String vReturn = "ERROR";
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO IDOSGD.TDTX_DEPENDENCIA_EMPLEADO(CO_DEP,CO_EMP,ES_EMP)\nVALUES(?,?,'0')");

    
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { coDep, coEmp });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }

  
  public String delEmpDependencia(String coDep) {
    String vReturn = "ERROR";
    StringBuffer sql = new StringBuffer();
    sql.append("DELETE FROM IDOSGD.TDTX_DEPENDENCIA_EMPLEADO\nWHERE CO_DEP=?");

    
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { coDep });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }


  
  public List<EmpleadoBean> getLsEmpDepen(String coDep) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT CO_EMP CEMP_CODEMP,ES_EMP ESTADO,IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(CO_EMP) NOMBRE\nFROM IDOSGD.TDTX_DEPENDENCIA_EMPLEADO\nWHERE CO_DEP=?");


    
    List<EmpleadoBean> list = new ArrayList<EmpleadoBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[] { coDep });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
}
