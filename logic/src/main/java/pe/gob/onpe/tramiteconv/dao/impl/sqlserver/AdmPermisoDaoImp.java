package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.PermisoBean;
import pe.gob.onpe.tramitedoc.dao.AdmPermisoDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;


@Repository("admPermisoDao")
public class AdmPermisoDaoImp   extends SimpleJdbcDaoBase   implements AdmPermisoDao
{
  public List<PermisoBean> getPermisoList(String codDependencia) {
     StringBuilder sql = new StringBuilder();
    sql.append("select s.cdes_user, p.co_use, p.co_dep, p.es_act, p.es_act_wf from IDOSGD.TDTR_PERMISOS p inner join IDOSGD.seg_usuarios1 s on p.co_use = s.cod_user where co_dep=? ");
    List<PermisoBean> list = new ArrayList<PermisoBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(PermisoBean.class), new Object[] { codDependencia });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String eliPermiso(String codDep) {
    String vReturn = "NO_OK";
     StringBuilder sql = new StringBuilder();
    
    sql.append("delete from IDOSGD.TDTR_PERMISOS where co_dep=?");
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { codDep });

      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String insertarPermiso(PermisoBean permisoDetalle) {
    String vReturn = "NO_OK";
     StringBuilder sql = new StringBuilder();
    sql.append("insert into IDOSGD.TDTR_PERMISOS(co_use, co_dep, es_act) ");
    sql.append("values (?,?,?)");
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { permisoDetalle
            .getCoUse(), permisoDetalle.getCoDep(), permisoDetalle.getEsAct() });
      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
}
