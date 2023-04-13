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
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.RequisitoBean;
import pe.gob.onpe.tramitedoc.dao.RequisitoDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;








@Repository("requisitoDao")
public class RequisitoDaoImpl
  extends SimpleJdbcDaoBase
  implements RequisitoDao
{
  @Autowired
  private ApplicationProperties applicationProperties;
  
  public List<RequisitoBean> getRequisitoList() {
     StringBuilder sql = new StringBuilder();
    sql.append("SELECT  COD_REQ AS codRequisito, ");
    sql.append("        DE_DESCRIPCION AS descripcion, ");
    sql.append("        ES_ESTADO, ");
    sql.append("        US_CREA_AUDI, ");
    sql.append("        CONVERT(VARCHAR(10), FE_CREA_AUDI, 103) FE_CREA_AUDI, ");
    sql.append("        US_MODI_AUDI, ");
    sql.append("        CONVERT(VARCHAR(10), FE_MODI_AUDI, 103) FE_MODI_AUDI ");
    sql.append("FROM IDOSGD.TDTR_REQUISITOS ");
    
    List<RequisitoBean> list = new ArrayList<RequisitoBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(RequisitoBean.class), new Object[0]);
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<RequisitoBean> getRequisitoBusqList(RequisitoBean requisito) {
     StringBuilder sql = new StringBuilder();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    
    sql.append("SELECT A.* FROM ( ");
    sql.append("\tSELECT\tCOD_REQ AS codRequisito, ");
    sql.append("            DE_DESCRIPCION AS descripcion, ");
    sql.append("\t\tES_ESTADO, ");
    sql.append("\t\tUS_CREA_AUDI, ");
    sql.append("\t\tCONVERT(VARCHAR(10), FE_CREA_AUDI, 103) FE_CREA_AUDI, ");
    sql.append("\t\tUS_MODI_AUDI, ");
    sql.append("\t\tCONVERT(VARCHAR(10), FE_MODI_AUDI, 103) FE_MODI_AUDI, ");
    sql.append("\t\tROW_NUMBER() OVER (ORDER BY COD_REQ) AS ROWNUM ");
    sql.append("\tFROM IDOSGD.TDTR_REQUISITOS ");
    sql.append("\tWHERE 1 = 1 ");
    if (requisito.getDescripcion() != null && !requisito.getDescripcion().equals("")) {
      sql.append("AND UPPER(DE_DESCRIPCION) like '%'+:descripcion+'%' ");
      objectParam.put("descripcion", requisito.getDescripcion());
    } 
    if (requisito.getIndicador() != null && !requisito.getIndicador().equals("")) {
      sql.append("AND COD_REQ NOT IN (SELECT COD_REQUISITO FROM IDOSGD.TDTX_REQUISITO_PROCESO WHERE COD_PROCESO = :indicador) ");
      objectParam.put("indicador", requisito.getIndicador());
    } 
    if (requisito.getEsEstado() != null && !requisito.getEsEstado().equals("")) {
      sql.append("AND ES_ESTADO = :estado ");
      objectParam.put("estado", requisito.getEsEstado());
    } 
    sql.append(") A ");
    sql.append("WHERE ROWNUM <= ").append(this.applicationProperties.getTopRegistrosConsultas());
    sql.append(" ORDER BY codRequisito ");
    
    List<RequisitoBean> list = new ArrayList<RequisitoBean>();
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(RequisitoBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String insRequisito(RequisitoBean requisito, String codusuario) {
    String vReturn = "NO_OK";
     StringBuilder sql = new StringBuilder();
    String genCoProceso = "";
    
    sql.append("INSERT INTO IDOSGD.TDTR_REQUISITOS ");
    sql.append("(COD_REQ, DE_DESCRIPCION, ES_ESTADO, US_CREA_AUDI, US_MODI_AUDI, FE_CREA_AUDI, FE_MODI_AUDI) ");
    sql.append("values (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ");

    
    try {
      String seq = "SELECT RIGHT(REPLICATE('0', 4) + CAST((ISNULL(MAX(CAST(COD_REQ AS INT)), 0) + 1) AS VARCHAR), 4) FROM IDOSGD.TDTR_REQUISITOS";
      genCoProceso = (String)this.jdbcTemplate.queryForObject(seq, String.class);

      
      this.jdbcTemplate.update(sql.toString(), new Object[] { genCoProceso, requisito
            .getDescripcion(), requisito.getEsEstado(), codusuario, codusuario });

      
      vReturn = "OK";
    }
    catch (DuplicateKeyException con) {
      vReturn = "Nde Codigo de requisito duplicado.";
      con.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public RequisitoBean getRequisito(String codRequisito) {
    RequisitoBean requisitoBean = new RequisitoBean();
     StringBuilder sql = new StringBuilder();
    sql.append("SELECT  COD_REQ AS codRequisito, ");
    sql.append("        DE_DESCRIPCION AS descripcion, ");
    sql.append("        ES_ESTADO, ");
    sql.append("        US_CREA_AUDI, ");
    sql.append("        CONVERT(VARCHAR(10), FE_CREA_AUDI, 103) FE_CREA_AUDI, ");
    sql.append("        US_MODI_AUDI, ");
    sql.append("        CONVERT(VARCHAR(10), FE_MODI_AUDI, 103) FE_MODI_AUDI ");
    sql.append("FROM IDOSGD.TDTR_REQUISITOS ");
    sql.append("WHERE COD_REQ = ? ");
    
    try {
      requisitoBean = (RequisitoBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(RequisitoBean.class), new Object[] { codRequisito });
    }
    catch (EmptyResultDataAccessException e) {
      requisitoBean = null;
    } catch (Exception e) {
      requisitoBean = null;
      e.printStackTrace();
    } 
    
    return requisitoBean;
  }

  
  public String updRequisito(RequisitoBean requisito, String codusuario) {
    String vReturn = "NO_OK";
    
     StringBuilder sql = new StringBuilder();
    sql.append("UPDATE IDOSGD.TDTR_REQUISITOS SET ");
    sql.append("ES_ESTADO=?, DE_DESCRIPCION=?, US_MODI_AUDI=?, FE_MODI_AUDI=CURRENT_TIMESTAMP ");
    sql.append("WHERE COD_REQ=? ");
    
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { requisito
            .getEsEstado(), requisito.getDescripcion(), codusuario, requisito.getCodRequisito() });
      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }

  
  public String insTupaRequisito(RequisitoBean requisito, String codusuario) {
    String vReturn = "NO_OK";
     StringBuilder sql = new StringBuilder();
    sql.append("INSERT INTO IDOSGD.TDTX_REQUISITO_PROCESO ");
    sql.append("(COD_REQUISITO, COD_PROCESO, NU_CORRELATIVO, ES_ESTADO, CO_USE_CRE, CO_USE_MOD, FE_USE_CRE, FE_USE_MOD, IN_OBLIGATORIO) ");
    sql.append("values (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?) ");
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { requisito
            .getCodRequisito(), requisito
            .getCodProceso(), requisito
            .getNuCorrelativo(), requisito
            .getEsEstado(), codusuario, codusuario, requisito

            
            .getIndicador() });
      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String updTupaRequisito(RequisitoBean requisito, String codusuario) {
    List<RequisitoBean> lis_expedientes = null;
    String vReturn = "NO_OK";
    
     StringBuilder sql = new StringBuilder();
    sql.append("UPDATE IDOSGD.TDTX_REQUISITO_PROCESO SET ");
    if (requisito.getCodRem() != null && !requisito.getCodRem().equals("")) {
      sql.append("COD_REQUISITO=?, CO_USE_MOD=?, FE_USE_MOD=CURRENT_TIMESTAMP ");
    } else if (requisito.getIndicador() != null && !requisito.getIndicador().equals("")) {
      sql.append("IN_OBLIGATORIO=?, CO_USE_MOD=?, FE_USE_MOD=CURRENT_TIMESTAMP ");
    } 
    sql.append("WHERE COD_REQUISITO=? AND COD_PROCESO=? ");
    
    try {
      if (requisito.getCodRem() != null && !requisito.getCodRem().equals("")) {
        
        lis_expedientes = getExpedienteRequisitoList(requisito.getCodRem(), requisito.getCodProceso());
        
        if (lis_expedientes != null && lis_expedientes.size() > 0) {

          
          eliExpedienteRequisito(requisito.getCodRem(), requisito.getCodProceso());

          
          this.jdbcTemplate.update(sql.toString(), new Object[] { requisito
                .getCodRequisito(), codusuario, requisito.getCodRem(), requisito.getCodProceso() });


          
          for (RequisitoBean req : lis_expedientes) {
            RequisitoBean req_expediente = new RequisitoBean();
            req_expediente.setCodProceso(requisito.getCodProceso());
            req_expediente.setCodRequisito(requisito.getCodRequisito());
            req_expediente.setAniExpediente(req.getAniExpediente());
            req_expediente.setNumExpediente(req.getNumExpediente());
            insExpedienteRequisito(req_expediente, codusuario);
          }
        
        } else {
          
          this.jdbcTemplate.update(sql.toString(), new Object[] { requisito
                .getCodRequisito(), codusuario, requisito.getCodRem(), requisito.getCodProceso() });
        }
      
      } else if (requisito.getIndicador() != null && !requisito.getIndicador().equals("")) {
        
        this.jdbcTemplate.update(sql.toString(), new Object[] { requisito
              .getIndicador(), codusuario, requisito.getCodRequisito(), requisito.getCodProceso() });
      } 
      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }

  
  public String eliTupaRequisito(RequisitoBean requisito) {
    List<RequisitoBean> lis_expedientes = null;
    String vReturn = "NO_OK";
     StringBuilder sql = new StringBuilder();
    sql.append("DELETE FROM IDOSGD.TDTX_REQUISITO_PROCESO ");
    sql.append("WHERE COD_REQUISITO=? AND COD_PROCESO=? ");
    
    try {
      lis_expedientes = getExpedienteRequisitoList(requisito.getCodRequisito(), requisito.getCodProceso());
      
      if (lis_expedientes != null && lis_expedientes.size() > 0)
      {
        
        eliExpedienteRequisito(requisito.getCodRequisito(), requisito.getCodProceso());
      }


      
      this.jdbcTemplate.update(sql.toString(), new Object[] { requisito
            .getCodRequisito(), requisito.getCodProceso() });

      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public List<RequisitoBean> getTupaRequisitoBusqList(RequisitoBean requisito) {
     StringBuilder sql = new StringBuilder();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    
    sql.append("SELECT  r.COD_REQ AS codRequisito, ");
    sql.append("\t    p.COD_PROCESO AS codProceso, ");
    sql.append("        r.DE_DESCRIPCION AS descripcion, ");
    sql.append("        p.IN_OBLIGATORIO AS indicador ");
    sql.append("FROM IDOSGD.TDTX_REQUISITO_PROCESO p ");
    sql.append("INNER JOIN IDOSGD.TDTR_REQUISITOS r ON p.COD_REQUISITO = r.COD_REQ ");
    sql.append("INNER JOIN IDOSGD.TDTR_PROCESOS_EXP e ON p.COD_PROCESO = e.CO_PROCESO ");
    sql.append("WHERE 1 = 1 ");
    
    if (requisito.getCodProceso() != null && !requisito.getCodProceso().equals("")) {
      sql.append("AND p.COD_PROCESO = :cod_proceso ");
      objectParam.put("cod_proceso", requisito.getCodProceso());
    } 
    if (requisito.getEsEstado() != null && !requisito.getEsEstado().equals("")) {
      sql.append("AND r.ES_ESTADO = :estado ");
      objectParam.put("estado", requisito.getEsEstado());
    } 
    sql.append("ORDER BY r.DE_DESCRIPCION ");
    
    List<RequisitoBean> list = new ArrayList<RequisitoBean>();
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(RequisitoBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<RequisitoBean> getExpedienteRequisitoList(String codRequisito, String codProceso) {
     StringBuilder sql = new StringBuilder();
    sql.append("SELECT  NU_ANN_EXP AS aniExpediente, ");
    sql.append("        NU_SEC_EXP AS numExpediente ");
    sql.append("FROM IDOSGD.TDTX_REQUISITO_EXPEDIENTE ");
    sql.append("WHERE COD_REQUISITO = ? ");
    sql.append("AND COD_PROCESO = ? ");
    
    List<RequisitoBean> list = new ArrayList<RequisitoBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(RequisitoBean.class), new Object[] { codRequisito, codProceso });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String eliExpedienteRequisito(String codRequisito, String codProceso) {
    String vReturn = "NO_OK";
     StringBuilder sql = new StringBuilder();
    sql.append("DELETE FROM IDOSGD.TDTX_REQUISITO_EXPEDIENTE ");
    sql.append("WHERE COD_REQUISITO = ? ");
    sql.append("AND COD_PROCESO = ? ");
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { codRequisito, codProceso });

      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String insExpedienteRequisito(RequisitoBean requisito, String codusuario) {
    String vReturn = "NO_OK";
     StringBuilder sql = new StringBuilder();
    sql.append("INSERT INTO IDOSGD.TDTX_REQUISITO_EXPEDIENTE( ");
    sql.append("\tNU_ANN_EXP, ");
    sql.append("\tNU_SEC_EXP, ");
    sql.append("\tCOD_REQUISITO, ");
    sql.append("\tCOD_PROCESO, ");
    sql.append("\tIN_OBLIGATORIO, ");
    sql.append("\tNU_CORRELATIVO, ");
    sql.append("\tIN_REQUISITO_OK, ");
    sql.append("\tUS_CREA_AUDI, ");
    sql.append("\tUS_MODI_AUDI) ");
    sql.append("\tSELECT ");
    sql.append("\t?, ");
    sql.append("\t?, ");
    sql.append("\tCOD_REQUISITO, ");
    sql.append("\tCOD_PROCESO, ");
    sql.append("\tIN_OBLIGATORIO, ");
    sql.append("\tNU_CORRELATIVO, ");
    sql.append("\tCASE IN_OBLIGATORIO WHEN '0' THEN '1' ELSE '0' END, ");
    sql.append("\t?, ");
    sql.append("\t? ");
    sql.append("\tFROM IDOSGD.TDTX_REQUISITO_PROCESO ");
    sql.append("\tWHERE COD_PROCESO = ? ");
    sql.append("\tAND COD_REQUISITO = ? ");
    
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { requisito
            .getAniExpediente(), requisito
            .getNumExpediente(), codusuario, codusuario, requisito

            
            .getCodProceso(), requisito
            .getCodRequisito() });
      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
}
