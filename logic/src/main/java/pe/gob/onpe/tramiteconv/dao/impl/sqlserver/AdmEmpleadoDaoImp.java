package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.tramitedoc.bean.AdmEmpleadoAccesoBean;
import pe.gob.onpe.tramitedoc.bean.AdmEmpleadoBean;
import pe.gob.onpe.tramitedoc.bean.AdmEmpleadoCargoBean;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.dao.AdmEmpleadoDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;










@Repository("admEmpleadoDao")
public class AdmEmpleadoDaoImp
  extends SimpleJdbcDaoBase   implements AdmEmpleadoDao {

  @Autowired
  private CommonQueryDaoImp commonQueryDaoImp;
	
  private String sql_base = "SELECT E.CEMP_CODEMP co_Empleado, E.CEMP_APEPAT ap_Paterno, E.CEMP_APEMAT ap_Materno,E.CEMP_DENOM nombres, E.CEMP_NU_DNI dni,E.CEMP_TIPSEX sexo, E.FEMP_FECNAC fecha_Nacimiento, E.CEMP_EMAIL email,E.CEMP_EST_EMP estado, E.CEMP_CO_DEPEND co_Dependencia, E.CEMP_CO_CARGO coCargo, IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(E.CEMP_CO_DEPEND) de_Dependencia, IDOSGD.PK_SGD_DESCRIPCION_DE_CARGO(E.CEMP_CO_CARGO) de_Cargo,  U.COD_USER usuario,U.ES_USUARIO esUsuario,CASE U.ES_USUARIO WHEN 'A' THEN 'Activo' WHEN 'N' THEN 'Nuevo' WHEN 'B' THEN 'Bloq. x Otros' WHEN 'X' THEN 'Baja' WHEN 'M' THEN 'Migracion' WHEN 'I' THEN 'Bloq. x Intentos' ELSE 'Sin Usuario' END estadousuario, \n U.IN_AD inAD FROM IDOSGD.RHTM_PER_EMPLEADOS E   LEFT JOIN IDOSGD.SEG_USUARIOS1 U ON U.CEMP_CODEMP= E.CEMP_CODEMP ";




  
  public AdmEmpleadoBean getAdmEmpleado(String codEmp) throws Exception {
     StringBuilder sql = new StringBuilder();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    sql.append(this.sql_base).append(" WHERE E.CEMP_CODEMP = :pCoEmp");
    objectParam.put("pCoEmp", codEmp);
    AdmEmpleadoBean empleado = new AdmEmpleadoBean();
    try {
      empleado = (AdmEmpleadoBean)this.namedParameterJdbcTemplate.queryForObject(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(AdmEmpleadoBean.class));
    } catch (EmptyResultDataAccessException e) {
      empleado = null;
    } catch (Exception e) {
      throw e;
    } 
    return empleado;
  }



  public AdmEmpleadoBean getAdmEmpleadoDesdeDni(String dni) throws Exception {
     StringBuilder sql = new StringBuilder();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    sql.append(this.sql_base).append(" WHERE E.CEMP_NU_DNI=:pnuDNI");
    objectParam.put("pnuDNI", dni);
    AdmEmpleadoBean empleado = new AdmEmpleadoBean();
    try {
      empleado = (AdmEmpleadoBean)this.namedParameterJdbcTemplate.queryForObject(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(AdmEmpleadoBean.class));
    } catch (EmptyResultDataAccessException e) {
      empleado = null;
    } catch (Exception e) {
      throw e;
    } 
    return empleado;
  }


  public int updAdmEmpleado(AdmEmpleadoBean empleado) throws Exception {
     StringBuilder sql = new StringBuilder();
    sql.append("SET LANGUAGE Español ");
    sql.append(" UPDATE IDOSGD.RHTM_PER_EMPLEADOS SET  CEMP_NU_DNI=?, CEMP_APEPAT=?, CEMP_APEMAT=?, CEMP_DENOM=?, FEMP_FECNAC=convert(varchar, ?, 103), CEMP_TIPSEX=?,  CEMP_EMAIL=?,  CEMP_EST_EMP=?,  CEMP_INDBAJ=?, " + (

  
        empleado.getEstado().equals("0") ? "FEMP_FECBAJ=getdate()," : "") + " CEMP_CO_DEPEND=?,  CO_DEPENDENCIA=?,  CEMP_CO_CARGO=?  WHERE CEMP_CODEMP=?");


    try {
      int upd = this.jdbcTemplate.update(sql.toString(), new Object[] { empleado.getDni(), empleado.getApPaterno().toUpperCase(), empleado.getApMaterno().toUpperCase(), empleado
            
            .getNombres().toUpperCase(), empleado.getFechaNacimiento2().equals("") ? null : empleado.getFechaNacimiento2(), empleado.getSexo(), empleado.getEmail().toLowerCase(), empleado.getEstado(), empleado.getEstado(), empleado
            .getCoDependencia(), empleado.getCoDependencia(), empleado.getCoCargo().equals("") ? "0000" : empleado.getCoCargo(), empleado.getCoEmpleado() });

      
      return upd;
    } catch (Exception e) {
      throw e;
    } 
  }


  
  public String nuevoAdmEmpleado(AdmEmpleadoBean empleado, Usuario usuario) throws Exception {
    String vReturn = "NO_OK";

  
    try {
       StringBuilder sql = new StringBuilder();
      sql.append("INSERT INTO IDOSGD.RHTM_PER_EMPLEADOS (CEMP_CODEMP,CEMP_CO_EMP, CEMP_NU_DNI,CEMP_APEPAT,CEMP_APEMAT,CEMP_DENOM,CEMP_EMAIL,CEMP_EST_EMP,CEMP_INDBAJ, CEMP_TIPSEX,FEMP_FECNAC,CEMP_CO_DEPEND,CO_DEPENDENCIA,CEMP_CO_CARGO,CEMP_CO_CATEGORIA,CEMP_FE_CREA,CEMP_ID_CREA,CEMP_FE_ACT,CEMP_ID_ACT,FEMP_FECALT,CEMP_TI_EMPLEADO,CEMP_CO_LOCAL ) VALUES (?,?,?,?,?,?,?,?,?, ?,convert(varchar, ?, 103),?,?, ?,'' ,GETDATE(),?,GETDATE(),? ,GETDATE(),'3','001' )");

    
      this.jdbcTemplate.update(sql.toString(), new Object[] { empleado.getCoEmpleado(), empleado.getCoEmpleado(), empleado.getDni(), empleado.getApPaterno().toUpperCase(), empleado
            .getApMaterno().toUpperCase(), empleado.getNombres().toUpperCase(), empleado.getEmail().toLowerCase(), empleado.getEstado(), empleado
            
            .getEstado(), empleado.getSexo(), empleado.getFechaNacimiento2().equals("") ? null : empleado.getFechaNacimiento2(), empleado.getCoDependencia(), empleado.getCoDependencia(), 
            empleado.getCoCargo().equals("") ? "0000" : empleado.getCoCargo(), usuario.getCempCodemp(), usuario.getCempCodemp() });



      
      vReturn = "OK";
    } catch (DuplicateKeyException e) {
      throw e;
    } catch (Exception e) {
      throw e;
    } 
    return vReturn;
  }


  public AdmEmpleadoBean getPersonaDesdeDni(String dni) throws Exception {
     StringBuilder sql = new StringBuilder();
    sql.append("SELECT A.nulem dni, A.DEAPP apPaterno, A.DEAPM apMaterno, A.DENOM nombres, ");
    sql.append(" convert(varchar, A.FENAC, 103) fechaNacimiento,A.INSEX sexo FROM  IDOSGD.idtanirs A ");
    sql.append(" WHERE A.nulem='").append(dni).append("' ");
    AdmEmpleadoBean empleado = new AdmEmpleadoBean();
    try {
      empleado = (AdmEmpleadoBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(AdmEmpleadoBean.class));
    } catch (EmptyResultDataAccessException e) {
      empleado = null;
    } catch (Exception e) {
      throw e;
    } 
    return empleado;
  }


  
  public List<AdmEmpleadoBean> getBsqAdmEmpleado(String dni, String apPaterno, String apMaterno, String nombres, String usuario) throws Exception {
     StringBuilder sql = new StringBuilder();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    sql.append(this.sql_base);
    if (dni.length() > 0 || apPaterno.length() > 0 || apMaterno.length() > 0 || nombres.length() > 0 || usuario.length() > 0) {
      String where_and = " WHERE ";
      if (dni.length() > 0) {
        sql.append(where_and).append(" E.CEMP_NU_DNI LIKE ''+:pDNI+'%' ");
        objectParam.put("pDNI", dni);
        where_and = " AND ";
      } 
      if (apPaterno.length() > 0) {
        sql.append(where_and).append(" E.CEMP_APEPAT LIKE ''+:pdeApPater+'%' ");
        objectParam.put("pdeApPater", apPaterno);
        where_and = " AND ";
      } 
      if (apMaterno.length() > 0) {
        sql.append(where_and).append(" E.CEMP_APEMAT LIKE ''+:pdeApMater+'%' ");
        objectParam.put("pdeApMater", apMaterno);
        where_and = " AND ";
      } 
      if (nombres.length() > 0) {
        sql.append(where_and).append(" E.CEMP_DENOM LIKE ''+:pnombres+'%' ");
        objectParam.put("pnombres", nombres);
      } 
      if (usuario.length() > 0) {
        sql.append(where_and).append(" U.COD_USER LIKE '%'+:pusuarios+'%' ");
        objectParam.put("pusuarios", usuario);
      } 
    } 
    
    List<AdmEmpleadoBean> list = new ArrayList<AdmEmpleadoBean>();
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(AdmEmpleadoBean.class));
    } catch (Exception e) {
      throw e;
    } 
    return list;
  }


  public List<DependenciaBean> getBsqDependencia(String criterio) throws Exception {
     StringBuilder sql = new StringBuilder();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    sql.append("SELECT CO_DEPENDENCIA coDependencia,DE_CORTA_DEPEN deCortaDepen, ");
    sql.append(" DE_DEPENDENCIA deDependencia  FROM IDOSGD.RHTM_DEPENDENCIA ");
    sql.append(" WHERE DE_CORTA_DEPEN LIKE ''+:pCriterio+'%' OR DE_DEPENDENCIA LIKE ''+:pCriterio+'%' ");
    objectParam.put("pCriterio", criterio);
    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class));
    } catch (Exception e) {
      throw e;
    } 
    return list;
  }

 
  public List<AdmEmpleadoCargoBean> getLsCargo() throws Exception {
     StringBuilder sql = new StringBuilder();
    sql.append("SELECT CCAR_CO_CARGO coCargo,CCAR_DESCAR deCargo FROM IDOSGD.RHTM_CARGOS ORDER BY CCAR_CO_CARGO ASC ");
    List<AdmEmpleadoCargoBean> list = new ArrayList<AdmEmpleadoCargoBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(AdmEmpleadoCargoBean.class));
    } catch (Exception e) {
      throw e;
    } 
    return list;
  }
  
  public AdmEmpleadoAccesoBean getAcceso(String coEmpleado) throws Exception {
     StringBuilder sql = new StringBuilder();
    sql.append("SELECT COD_USER coUsuario,CDES_USER deUsuario,ES_USUARIO esUsuario,IN_AD inAD FROM IDOSGD.SEG_USUARIOS1 WHERE CEMP_CODEMP='" + coEmpleado + "'");
    AdmEmpleadoAccesoBean usuario = new AdmEmpleadoAccesoBean();
    try {
      usuario = (AdmEmpleadoAccesoBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(AdmEmpleadoAccesoBean.class));
    } catch (EmptyResultDataAccessException e) {
      usuario = null;
    } catch (Exception e) {
      throw e;
    } 
    return usuario;
  }

 
  public List<AdmEmpleadoAccesoBean> getLsAcceso(AdmEmpleadoAccesoBean acceso) throws Exception {
     StringBuilder sql = new StringBuilder();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    
    sql.append("SELECT COD_USER coUsuario,CDES_USER deUsuario FROM IDOSGD.SEG_USUARIOS1 WHERE COD_USER=:pcoUser ");
    objectParam.put("pcoUser", acceso.getCoUsuario());

  
    List<AdmEmpleadoAccesoBean> list = new ArrayList<AdmEmpleadoAccesoBean>();
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(AdmEmpleadoAccesoBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      throw e;
    } 
    return list;
  }


  
  public String saveNuevoAcceso(String coUser, String coEmp, String nuDni, String inAD) throws Exception {
    try {
      if (coUser != null && coUser.length() > 0 && coEmp.length() > 0) {
    	  
        SimpleJdbcCall simpleJdbcCall = (new SimpleJdbcCall(this.dataSource)).withProcedureName("IDOSGD.PK_SGD_TRAMITE_CREA_USUARIO").withoutProcedureColumnMetaDataAccess().useInParameterNames(new String[] 
        		{ "PCO_USUARIO", "PCO_EMP", "PNU_DNI", "PIN_AD" }).declareParameters(new SqlParameter[] { new SqlParameter("PCO_USUARIO", 12), new SqlParameter("PCO_EMP", 12), new SqlParameter("PNU_DNI", 12), new SqlParameter("PIN_AD", 12) });

    
        MapSqlParameterSource mapSqlParameterSource = (new MapSqlParameterSource()).addValue("PCO_USUARIO", coUser).addValue("PCO_EMP", coEmp).addValue("PNU_DNI", nuDni).addValue("PIN_AD", inAD);

        
        String mensaje = null;
        try {
          simpleJdbcCall.execute((SqlParameterSource)mapSqlParameterSource);
          mensaje = "OK";
        } catch (Exception e) {
          e.printStackTrace();
          throw e;
        } 
        return mensaje;
      } 
      return "Sin crear nuevo Usuario";
    }
    catch (Exception e) {
      e.printStackTrace();
      throw e;
    } 
  }

  
  public String updAcceso(String coUser, String coEmp, String nuDni, String inAD) {
    String vReturn = "NO_OK";
     StringBuilder sqlUpd = new StringBuilder();
    sqlUpd.append("update IDOSGD.SEG_USUARIOS1 \nset IN_AD=?\nwhere\n CEMP_CODEMP=? \n");


    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { inAD, coEmp });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    
    return vReturn;
  }


  public String getNextCoEmpleado() {
    String vReturn = "1";
    
    String sql = "SELECT RIGHT(REPLICATE('0',5)+ CAST((CAST(MAX(CEMP_CODEMP) AS INT)+1) AS VARCHAR),5) coEmpleado FROM IDOSGD.RHTM_PER_EMPLEADOS";
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject(sql, String.class);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
}
