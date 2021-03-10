package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.TupaBean;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;
import pe.gob.onpe.tramitedoc.dao.TupaDao;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;






@Repository("tupaDao")
public class TupaDaoImp
  extends SimpleJdbcDaoBase
  implements TupaDao
{
  @Autowired
  private ApplicationProperties applicationProperties;
  
  public List<TupaBean> getTupaList() {
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT  CO_PROCESO, ");
    sql.append("        TI_PROCESO, ");
    sql.append("        CO_DEP_PRO, ");
    sql.append("        DE_NOMBRE, ");
    sql.append("        DE_DESCRIPCION, ");
    sql.append("        NU_PLAZO, ");
    sql.append("        ES_ESTADO, ");
    sql.append("        US_CREA_AUDI, ");
    sql.append("        CONVERT(VARCHAR(10), FE_CREA_AUDI, 103) FE_CREA_AUDI, ");
    sql.append("        US_MODI_AUDI, ");
    sql.append("        CONVERT(VARCHAR(10), FE_MODI_AUDI, 103) FE_MODI_AUDI ");
    sql.append("FROM IDOSGD.TDTR_PROCESOS_EXP ");
    List<TupaBean> list = new ArrayList<TupaBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TupaBean.class), new Object[0]);
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  public List<TupaBean> getTupaBusqList(String busTupaCorto, String busTupaDescripcion) {
    StringBuffer sql = new StringBuffer();
    Object[] objectParam = null;
    
    sql.append("SELECT A.* FROM ( ");
    sql.append("\tSELECT\tCO_PROCESO, ");
    sql.append("\t\tTI_PROCESO, ");
    sql.append("\t\tCO_DEP_PRO, ");
    sql.append("\t\tDE_NOMBRE, ");
    sql.append("\t\tDE_DESCRIPCION, ");
    sql.append("\t\tNU_PLAZO, ");
    sql.append("\t\tES_ESTADO, ");
    sql.append("\t\tUS_CREA_AUDI, ");
    sql.append("\t\tCONVERT(VARCHAR(10), FE_CREA_AUDI, 103) FE_CREA_AUDI, ");
    sql.append("\t\tUS_MODI_AUDI, ");
    sql.append("\t\tCONVERT(VARCHAR(10), FE_MODI_AUDI, 103) FE_MODI_AUDI, ");
    sql.append("\t\tROW_NUMBER() OVER (ORDER BY CO_PROCESO) AS ROWNUM ");
    sql.append("\tFROM IDOSGD.TDTR_PROCESOS_EXP ");
    sql.append("\tWHERE TI_PROCESO='1' ");
    if (busTupaCorto != null && !busTupaCorto.equals("")) {
      sql.append("AND UPPER(DE_NOMBRE) like '%'+?+'%' ");
      objectParam = new Object[] { busTupaCorto };
    }
    else if (busTupaDescripcion != null && !busTupaDescripcion.equals("")) {
      sql.append("AND UPPER(DE_DESCRIPCION) like '%'+?+'%' ");
      objectParam = new Object[] { busTupaDescripcion };
    } 
    sql.append(") A ");
    sql.append("WHERE ROWNUM <= ").append(this.applicationProperties.getTopRegistrosConsultas());
    sql.append(" order by CO_PROCESO ");
    
    List<TupaBean> list = new ArrayList<TupaBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TupaBean.class), objectParam);
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String insTupa(TupaBean tupa, String codusuario) {
    String vReturn = "NO_OK";
    StringBuffer sql = new StringBuffer();
    String genCoProceso = "";
    
    sql.append("insert into IDOSGD.TDTR_PROCESOS_EXP ");
    sql.append("(CO_PROCESO, TI_PROCESO, ES_ESTADO, CO_DEP_PRO, DE_NOMBRE, DE_DESCRIPCION, NU_PLAZO, US_CREA_AUDI, US_MODI_AUDI, FE_CREA_AUDI, FE_MODI_AUDI) ");
    sql.append("values (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ");

    
    try {
      String seq = "SELECT RIGHT(REPLICATE('0', 4) + CAST((ISNULL(MAX(CAST(CO_PROCESO AS INT)), 0) + 1) AS VARCHAR), 4) FROM IDOSGD.TDTR_PROCESOS_EXP";
      genCoProceso = (String)this.jdbcTemplate.queryForObject(seq, String.class);

      
      this.jdbcTemplate.update(sql.toString(), new Object[] { genCoProceso, "1", tupa
            .getEsEstado(), "00000", tupa.getDeNombre(), tupa.getDeDescripcion(), tupa
            .getNuPlazo(), codusuario, codusuario });

      
      vReturn = "OK";
    }
    catch (DuplicateKeyException con) {
      vReturn = "Nde Codigo de proceso duplicado.";
      con.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
  
  public TupaBean getTupa(String codTupa) {
    TupaBean tupaBean = new TupaBean();
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT  CO_PROCESO, ");
    sql.append("        TI_PROCESO, ");
    sql.append("        CO_DEP_PRO, ");
    sql.append("        DE_NOMBRE, ");
    sql.append("        DE_DESCRIPCION, ");
    sql.append("        NU_PLAZO, ");
    sql.append("        ES_ESTADO, ");
    sql.append("        US_CREA_AUDI, ");
    sql.append("        CONVERT(VARCHAR(10), FE_CREA_AUDI, 103) FE_CREA_AUDI, ");
    sql.append("        US_MODI_AUDI, ");
    sql.append("        CONVERT(VARCHAR(10), FE_MODI_AUDI, 103) FE_MODI_AUDI ");
    sql.append("FROM IDOSGD.TDTR_PROCESOS_EXP ");
    sql.append("WHERE CO_PROCESO = ? ");
    
    try {
      tupaBean = (TupaBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TupaBean.class), new Object[] { codTupa });
    }
    catch (EmptyResultDataAccessException e) {
      tupaBean = null;
    } catch (Exception e) {
      tupaBean = null;
      e.printStackTrace();
    } 
    
    return tupaBean;
  }
  
  public String updTupa(TupaBean tupa, String codusuario) {
    String vReturn = "NO_OK";
    
    StringBuffer sql = new StringBuffer();
    sql.append("update IDOSGD.TDTR_PROCESOS_EXP set ");
    
    sql.append(" ES_ESTADO=?, DE_NOMBRE=?, DE_DESCRIPCION=?, NU_PLAZO=?, US_MODI_AUDI=?, FE_MODI_AUDI=CURRENT_TIMESTAMP ");
    sql.append("where CO_PROCESO=? ");
    
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { tupa
            
            .getEsEstado(), tupa.getDeNombre(), tupa.getDeDescripcion(), tupa.getNuPlazo(), codusuario, tupa.getCoProceso() });
      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }
}
