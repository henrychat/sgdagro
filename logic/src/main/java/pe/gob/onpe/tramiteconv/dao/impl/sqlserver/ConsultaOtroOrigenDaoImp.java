package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.OtroOrigenBean;
import pe.gob.onpe.tramitedoc.dao.ConsultaOtroOrigenDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoQuery;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;



@Repository("ConsultaOtroOrigenDao")
public class ConsultaOtroOrigenDaoImp
  extends SimpleJdbcDaoQuery
  implements ConsultaOtroOrigenDao
{
  @Autowired
  private ApplicationProperties applicationProperties;
  
  public List<OtroOrigenBean> getOtroOrigenBusqList(OtroOrigenBean otroOrigen) {
     StringBuilder sql = new StringBuilder();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    String sqlContains = "";
    sql.append("SELECT A.* FROM ( ");
    sql.append("\tSELECT\tco_otr_ori, ");
    sql.append("\t\tnu_doc_otr_ori, ");
    sql.append("\t\tco_tip_otr_ori, ");
    sql.append("\t\tde_ape_pat_otr, ");
    sql.append("\t\tde_ape_mat_otr, ");
    sql.append("\t\tde_nom_otr, ");
    sql.append("\t\tde_raz_soc_otr, ");
    sql.append("\t\tde_dir_otro_ori, ");
    sql.append("\t\tti_origen, ");
    sql.append("\t\tref_co_otr_ori, ");
    sql.append("\t\t(SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_GET_DEPARTAMENTO](ub_dep)) no_dep, ");
    sql.append("\t\t(SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_GET_PROVINCIA](ub_dep, ub_pro)) no_pro, ");
    sql.append("\t\t(SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_GET_DISTRITO](ub_dep, ub_pro, ub_dis)) no_dis, ");
    sql.append("\t\tes_activo, de_email,de_telefo, ");
    sql.append("\t\tROW_NUMBER() OVER (ORDER BY de_ape_pat_otr) AS ROWNUM ");
    sql.append("\tFROM IDOSGD.tdtr_otro_origen ");
    sql.append("\tWHERE 1=1 ");
    if (otroOrigen.getDeRazSocOtr() != null && otroOrigen.getDeRazSocOtr().trim().length() > 0) {
      sql.append(" AND CONTAINS(*, :pBusquedaTextual) ");
      sqlContains = "SELECT RETORNO FROM [IDOSGD].PK_SGD_DESCRIPCION_QUERYFILTER('" + otroOrigen.getDeRazSocOtr() + "')";
    } 
    sql.append(") A ");
    sql.append("WHERE ROWNUM <= ").append(this.applicationProperties.getTopRegistrosConsultas());
    sql.append(" ORDER BY de_ape_pat_otr ");
    
    List<OtroOrigenBean> list = new ArrayList<OtroOrigenBean>();
    
    try {
      if (sqlContains.length() > 0) {
        String cadena = (String)this.jdbcTemplate.queryForObject(sqlContains, String.class);
        objectParam.put("pBusquedaTextual", cadena);
      } 
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(OtroOrigenBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
}
