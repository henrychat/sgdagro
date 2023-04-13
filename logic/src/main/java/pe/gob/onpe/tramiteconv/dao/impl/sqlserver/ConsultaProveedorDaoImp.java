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
import pe.gob.onpe.tramitedoc.bean.ProveedorBean;
import pe.gob.onpe.tramitedoc.dao.ConsultaProveedorDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoQuery;
import pe.gob.onpe.tramitedoc.util.FiltroPaginate;
import pe.gob.onpe.tramitedoc.util.ProcessResult;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;








@Repository("consultaProveedorDao")
public class ConsultaProveedorDaoImp
  extends SimpleJdbcDaoQuery
  implements ConsultaProveedorDao
{
  @Autowired
  private ApplicationProperties applicationProperties;
  
  public ProcessResult<List<ProveedorBean>> getProveedoresBusqList(String busNroRuc, String busRazonSocial, FiltroPaginate paginate) {
     StringBuilder sql = new StringBuilder();
    String sqlContains = "";
    Map<String, Object> objectParam = new HashMap<String, Object>();
    
    sql.append("SELECT A.*,  ");
    sql.append("\t\t(SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_GET_DEPARTAMENTO](A.cubi_coddep)) no_dep, ");
    sql.append("\t\t(SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_GET_PROVINCIA](A.cubi_coddep, A.cubi_codpro)) no_prv, ");
    sql.append("\t\t(SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_GET_DISTRITO](A.cubi_coddep, A.cubi_codpro, A.cubi_coddis)) no_dis ");
    sql.append("\tFROM ( select\tcpro_ruc nuRuc, ");
    sql.append("\t\tCONVERT(VARCHAR(10), dpro_fecins, 103) dpro_fecins, ");
    sql.append("\t\tcpro_razsoc descripcion, ");
    sql.append("\t\tcpro_domicil, ");
    sql.append("\t\tcubi_coddep, ");
    sql.append("\t\tcubi_codpro, ");
    sql.append("\t\tcubi_coddis, ");
    sql.append("\t\tcpro_telefo,cpro_email, ");
    sql.append("\t\tROW_NUMBER() OVER (ORDER BY cpro_ruc) AS fila, COUNT(cpro_ruc) OVER()  AS filasTotal  ");
    sql.append("\tfrom IDOSGD.LG_PRO_PROVEEDOR ");
    sql.append("\tWHERE LEN(cpro_ruc) = 11 ");
    
    if (busNroRuc != null && busNroRuc.trim().length() > 0) {
      sql.append(" and cpro_ruc like '%'+:pbusNroRuc+'%' ");
      objectParam.put("pbusNroRuc", busNroRuc);
    } 
    
    if (busRazonSocial != null && busRazonSocial.trim().length() > 0) {
  //    sql.append(" AND CONTAINS(cpro_razsoc, :pBusquedaTextual) ");
   //   sqlContains = "SELECT RETORNO FROM [IDOSGD].PK_SGD_DESCRIPCION_QUERYFILTER('" + busRazonSocial.trim() + "')";
    	   sql.append(" and cpro_razsoc like '%'+:pbusRazonSocial+'%' ");
    	      objectParam.put("pbusRazonSocial", busRazonSocial.trim());
    } 
    
    sql.append(") A ");
    sql.append("WHERE fila BETWEEN " + paginate.getPaginaDesde() + " AND " + paginate.getPaginaHasta() + " ");
    ProcessResult<List<ProveedorBean>> Result = new ProcessResult();
    List<ProveedorBean> list = new ArrayList<ProveedorBean>();
    
    try {
      if (sqlContains.length() > 0) {
        String cadena = (String)this.jdbcTemplate.queryForObject(sqlContains, String.class);
        objectParam.put("pBusquedaTextual", cadena);
      } 
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(ProveedorBean.class));
      Result.setResult(list);
      Result.setIsSuccess(true);
    } catch (EmptyResultDataAccessException e) {
      Result.setIsSuccess(false);
      Result.setMessage(e.getMessage());
    } catch (Exception e) {
      Result.setIsSuccess(false);
      Result.setMessage(e.getMessage());
      e.printStackTrace();
    } 
    return Result;
  }
}
