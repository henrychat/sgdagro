package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoVoBoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoVoBoBean;
import pe.gob.onpe.tramitedoc.dao.DocumentoVoBoDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;



@Repository("documentoVoboDao")
public class DocumentoVoBoDaoImp
  extends SimpleJdbcDaoBase
  implements DocumentoVoBoDao
{
  public List<DocumentoVoBoBean> getLsDocsVoBo(BuscarDocumentoVoBoBean bDocVoBo) {
    List<DocumentoVoBoBean> list;
    StringBuffer sql = new StringBuffer();
    boolean bBusqFiltro = false;
    String sOrdenList = "";
    String sqlContains = "";
    Map<String, Object> objectParam = new HashMap<String, Object>();


    
    sql.append("SELECT TOP 100 A.* ");
    sql.append(" FROM ( ");
    sql.append("\tSELECT RE.NU_ANN, RE.NU_EMI, RE.NU_COR_EMI, CONVERT(VARCHAR(10), RE.FE_EMI, 3) FE_EMI_CORTA, RE.FE_EMI, ");
    sql.append("    (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_EMI_REF](RE.NU_ANN, RE.NU_EMI)) DE_EMI_REF, ");
    sql.append("\t(SELECT CDOC_DESDOC FROM IDOSGD.SI_MAE_TIPO_DOC WHERE CDOC_TIPDOC = RE.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,RR.NU_DOC, UPPER(SUBSTRING(RE.DE_ASU,0,399)) DE_ASU,");
    sql.append("\t(SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM FROM IDOSGD.RHTM_PER_EMPLEADOS WHERE CEMP_CODEMP = VB.CO_EMP_VB)  DE_EMP_VB,");
    sql.append("\t(SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM FROM IDOSGD.RHTM_PER_EMPLEADOS WHERE CEMP_CODEMP = RE.CO_EMP_RES) DE_EMP_ELABORA,");
    sql.append("\tRR.NU_EXPEDIENTE NRO_EXPEDIENTE,");
    sql.append("\t(SELECT DE_EST FROM IDOSGD.TDTR_ESTADOS WHERE (CO_EST + DE_TAB) = (CASE VB.IN_VB WHEN 'B' THEN '0' ELSE VB.IN_VB END) + 'TDTV_PERSONAL_VB') DE_ES_DOC_EMI,");
    sql.append("\tRR.IN_EXISTE_DOC EXISTE_DOC, (CASE ISNULL(RR.TI_EMI_REF, '0') + ISNULL(RR.IN_EXISTE_ANEXO, '2') WHEN '00' THEN 0 WHEN '02' THEN 0 ELSE 1 END) EXISTE_ANEXO ");
    sql.append("\tFROM IDOSGD.TDTV_REMITOS RE  ");
    sql.append("\tINNER JOIN IDOSGD.TDTV_PERSONAL_VB VB ON RE.NU_ANN=VB.NU_ANN AND RE.NU_EMI=VB.NU_EMI  ");
    sql.append("\tINNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN RR ON RR.NU_ANN=RE.NU_ANN AND RR.NU_EMI=RE.NU_EMI ");
    sql.append("\tWHERE  RR.TI_EMI in ('01','05')  ");
    sql.append("\tAND RE.TI_EMI in ('01','05') ");
    sql.append("\tAND (RE.ES_DOC_EMI='7' OR (RE.TI_EMI='05' AND RE.ES_DOC_EMI='5'))  ");
    sql.append("\tAND RE.ES_ELI='0'");
    sql.append("\tAND RE.CO_GRU in ('1','2')");
    
    String pnuAnn = bDocVoBo.getNuAnn();
    if (pnuAnn != null && pnuAnn.trim().length() == 4) {
      sql.append(" AND RE.NU_ANN = :pnuAnn");
      objectParam.put("pnuAnn", pnuAnn);
    } 
    sql.append(" AND VB.CO_DEP = :pCoDep");

    
    objectParam.put("pCoDep", bDocVoBo.getCoDepUsu());
    
    String pTipoBusqueda = bDocVoBo.getTipBusqueda();
    if (pTipoBusqueda.equals("1") && bDocVoBo.isEsIncluyeFiltro()) {
      bBusqFiltro = true;
    }
    
    sql.append(" AND VB.CO_EMP_VB = :pcoEmpVobo ");
    objectParam.put("pcoEmpVobo", bDocVoBo.getCoEmpUsu());

    
    if (pTipoBusqueda.equals("0") || bBusqFiltro) {
      if (bDocVoBo.getTipDoc() != null && bDocVoBo.getTipDoc().trim().length() > 0) {
        sql.append(" AND RE.CO_TIP_DOC_ADM = :pCoDocEmi ");
        objectParam.put("pCoDocEmi", bDocVoBo.getTipDoc());
      } 
      if (bDocVoBo.getEsDoc() != null && bDocVoBo.getEsDoc().trim().length() > 0) {
        sql.append(" AND (VB.IN_VB = :pEsDocEmi ");
        String esDoc = bDocVoBo.getEsDoc();
        if (esDoc.equals("0")) {
          sql.append(" OR VB.IN_VB = 'B') ");
        } else {
          sql.append(") ");
        } 
        objectParam.put("pEsDocEmi", bDocVoBo.getEsDoc());
      } else {
        sOrdenList = " DESC";
      } 
      if (bDocVoBo.getCoPrioridadDoc() != null && bDocVoBo.getCoPrioridadDoc().trim().length() > 0) {
        sql.append(" AND RR.CO_PRIORIDAD = :pCoPrioridad ");
        objectParam.put("pCoPrioridad", bDocVoBo.getCoPrioridadDoc());
      } 
      if (bDocVoBo.getCoRefOri() != null && bDocVoBo.getCoRefOri().trim().length() > 0) {
        sql.append(" AND CHARINDEX(:pTiEmiRef,RR.TI_EMI_REF) > 0 ");
        objectParam.put("pTiEmiRef", bDocVoBo.getCoRefOri());
      } 
      if (bDocVoBo.getCoEmpElabora() != null && bDocVoBo.getCoEmpElabora().trim().length() > 0) {
        sql.append(" AND RE.CO_EMP_RES = :pcoEmpEla ");
        objectParam.put("pcoEmpEla", bDocVoBo.getCoEmpElabora());
      } 
      String vFeEmiIni = bDocVoBo.getFeIni();
      String vFeEmiFin = bDocVoBo.getFeFin();
      if (vFeEmiIni != null && vFeEmiIni.trim().length() == 10 && vFeEmiFin != null && vFeEmiFin
        .trim().length() == 10) {
        sql.append("AND RE.FE_EMI BETWEEN CONVERT(DATETIME, :pFeEmiIni, 103) AND CONVERT(DATETIME, :pFeEmiFin, 103) + 0.99999 ");
        objectParam.put("pFeEmiIni", vFeEmiIni);
        objectParam.put("pFeEmiFin", vFeEmiFin);
      } 
    } 

    
    if (pTipoBusqueda.equals("1")) {
      if (bDocVoBo.getNuCorEmi() != null && bDocVoBo.getNuCorEmi().trim().length() > 0) {
        sql.append(" AND RE.NU_COR_EMI = :pnuCorEmi ");
        objectParam.put("pnuCorEmi", bDocVoBo.getNuCorEmi());
      } 
      
      if (bDocVoBo.getNuDoc() != null && bDocVoBo.getNuDoc().trim().length() > 1) {
        sql.append(" AND RR.NU_DOC LIKE ''+:pnuDocEmi+'%' ");
        objectParam.put("pnuDocEmi", bDocVoBo.getNuDoc());
      } 
      
      if (bDocVoBo.getNroExp() != null && bDocVoBo.getNroExp().trim().length() > 1) {
        sql.append(" AND RR.NU_EXPEDIENTE LIKE '%'+:pnuExpediente+'%' ");
        objectParam.put("pnuExpediente", bDocVoBo.getNroExp());
      } 

      
      if (bDocVoBo.getAsunto() != null && bDocVoBo.getAsunto().trim().length() > 1) {

        
        sql.append(" AND UPPER(RE.DE_ASU) LIKE '%'+:pDeAsunto+'%' ");
        objectParam.put("pDeAsunto", bDocVoBo.getAsunto());
      } 
    } 
    sql.append(") AS A ORDER BY FE_EMI ").append(sOrdenList);

    
    try {
      if (sqlContains.length() > 0) {
        String cadena = (String)this.jdbcTemplate.queryForObject(sqlContains, String.class);
        objectParam.put("pBusquedaTextual", cadena);
      } 
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoVoBoBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public DocumentoVoBoBean getDocumentoVoBo(String pnuAnn, String pnuEmi, String coDep, String coEmpVb) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT ");
    sql.append("RE.NU_ANN, ");
    sql.append("RE.NU_EMI, ");
    sql.append("RE.NU_COR_EMI, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](RE.CO_DEP_EMI) DE_DEP_EMI, ");
    sql.append("(SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM FROM IDOSGD.RHTM_PER_EMPLEADOS WHERE CEMP_CODEMP = RE.CO_EMP_EMI) DE_EMP_EMI, ");
    sql.append("(SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM FROM IDOSGD.RHTM_PER_EMPLEADOS WHERE CEMP_CODEMP = RE.CO_EMP_RES) DE_EMP_ELABORA, ");
    sql.append("CONVERT(VARCHAR(10), RE.FE_EMI, 103) FE_EMI_CORTA, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](RE.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM, ");
    sql.append("CASE RE.TI_EMI ");
    sql.append("\t WHEN '01' THEN ISNULL(RE.NU_DOC_EMI, ' ') + '-' + RE.NU_ANN + '-' + RE.DE_DOC_SIG ");
    sql.append("\t WHEN '05' THEN ISNULL(RE.NU_DOC_EMI, ' ') + '-' + RE.NU_ANN + '-' + RE.DE_DOC_SIG ");
    sql.append("\t ELSE ISNULL(RE.DE_DOC_SIG, 'S/N') ");
    sql.append("END NU_DOC, ");
    sql.append("CASE VB.IN_VB WHEN 'B' THEN '0' ELSE VB.IN_VB END ES_DOC_EMI, ");
    sql.append("(SELECT DE_EST FROM IDOSGD.TDTR_ESTADOS WHERE (CO_EST + DE_TAB) = (CASE VB.IN_VB WHEN 'B' THEN '0' ELSE VB.IN_VB END) + 'TDTV_PERSONAL_VB') DE_ES_DOC_EMI, ");
    sql.append("RE.NU_DIA_ATE, ");
    sql.append("RE.DE_ASU, ");
    sql.append("EX.NU_EXPEDIENTE NRO_EXPEDIENTE, ");
    sql.append("CONVERT(VARCHAR(10), EX.FE_EXP, 103) FE_EXP_CORTA, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_PROCESO_EXP](EX.CO_PROCESO) DE_PROCESO_EXP, ");
    sql.append("RE.TI_CAP, ");
    sql.append("VB.OBS_VB DE_OBS ");
    sql.append("FROM IDOSGD.TDTV_REMITOS RE ");
    sql.append("INNER JOIN IDOSGD.TDTV_PERSONAL_VB VB ON VB.NU_ANN=RE.NU_ANN AND VB.NU_EMI=RE.NU_EMI ");
    sql.append("LEFT JOIN IDOSGD.TDTC_EXPEDIENTE EX ON RE.NU_ANN_EXP=EX.NU_ANN_EXP AND RE.NU_SEC_EXP=EX.NU_SEC_EXP  ");
    
    sql.append("WHERE RE.NU_ANN=? AND RE.NU_EMI=? ");
    sql.append("AND VB.CO_DEP=? AND VB.CO_EMP_VB=? ");
    sql.append("AND (RE.ES_DOC_EMI='7' OR (RE.TI_EMI='05' AND RE.ES_DOC_EMI='5')) ");
    sql.append("AND RE.ES_ELI='0' ");
    sql.append("AND (RE.CO_GRU='1' OR RE.CO_GRU='2') ");
    
    DocumentoVoBoBean docVobo = null;
    try {
      docVobo = (DocumentoVoBoBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoVoBoBean.class), new Object[] { pnuAnn, pnuEmi, coDep, coEmpVb });
    }
    catch (EmptyResultDataAccessException e) {
      docVobo = null;
    } catch (Exception e) {
      System.out.println("nu_ann=" + pnuAnn + ",nu_emi=" + pnuEmi);
      e.printStackTrace();
    } 
    return docVobo;
  }

  
  public String updObsVistoBueno(String nuAnn, String nuEmi, String coDep, String coEmp, String obs, String coUserMod, String esDocVb) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("UPDATE IDOSGD.TDTV_PERSONAL_VB ");
    sqlUpd.append("SET OBS_VB=?, ");
    sqlUpd.append("\tCO_USE_MOD=?, ");
    sqlUpd.append("\tFE_USE_MOD=CURRENT_TIMESTAMP, ");
    sqlUpd.append("\tIN_VB=? ");
    sqlUpd.append("WHERE NU_ANN=? ");
    sqlUpd.append("AND NU_EMI=? ");
    sqlUpd.append("AND CO_DEP=? ");
    sqlUpd.append("AND CO_EMP_VB=? ");
    sqlUpd.append("AND IN_VB <> '1' ");
    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { obs, coUserMod, esDocVb, nuAnn, nuEmi, coDep, coEmp });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String updToEnviadoVistoBueno(String nuAnn, String nuEmi, String coDep, String coEmp, String esDocVobo, String coUserMod) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("UPDATE IDOSGD.TDTV_PERSONAL_VB ");
    sqlUpd.append("SET IN_VB=?, ");
    sqlUpd.append("FE_VB=CURRENT_TIMESTAMP, ");
    sqlUpd.append("CO_USE_MOD=?, ");
    sqlUpd.append("FE_USE_MOD=CURRENT_TIMESTAMP, ");
    sqlUpd.append("OBS_VB=NULL ");
    sqlUpd.append("WHERE NU_ANN=? ");
    sqlUpd.append("AND NU_EMI=? ");
    sqlUpd.append("AND CO_DEP=? ");
    sqlUpd.append("AND CO_EMP_VB=? ");
    sqlUpd.append("AND IN_VB <> '1' ");
    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { esDocVobo, coUserMod, nuAnn, nuEmi, coDep, coEmp });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String updToSinVistoBueno(String nuAnn, String nuEmi, String coDep, String coEmp, String esDocVobo, String coUserMod) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("UPDATE IDOSGD.TDTV_PERSONAL_VB ");
    sqlUpd.append("SET IN_VB=?, ");
    sqlUpd.append("CO_USE_MOD=?, ");
    sqlUpd.append("FE_USE_MOD=CURRENT_TIMESTAMP, ");
    sqlUpd.append("OBS_VB=NULL, ");
    sqlUpd.append("FE_VB=NULL ");
    sqlUpd.append("WHERE NU_ANN=? ");
    sqlUpd.append("AND NU_EMI=? ");
    sqlUpd.append("AND CO_DEP=? ");
    sqlUpd.append("AND CO_EMP_VB=? ");
    sqlUpd.append("AND IN_VB='1' ");
    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { esDocVobo, coUserMod, nuAnn, nuEmi, coDep, coEmp });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String getDeObsEmpVoBo(String nuAnn, String nuEmi, String coDep, String coEmp) {
    String deObs = null;
    StringBuffer sqlQry = new StringBuffer();
    sqlQry.append("SELECT VB.OBS_VB ");
    sqlQry.append("FROM IDOSGD.TDTV_PERSONAL_VB VB ");
    sqlQry.append("WHERE VB.NU_ANN=? ");
    sqlQry.append("AND VB.NU_EMI=? ");
    sqlQry.append("AND VB.CO_DEP=? ");
    sqlQry.append("AND VB.CO_EMP_VB=? ");
    sqlQry.append("AND VB.IN_VB='2' ");
    
    try {
      deObs = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { nuAnn, nuEmi, coDep, coEmp });
    } catch (EmptyResultDataAccessException e) {
      deObs = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return deObs;
  }

  
  public String existeVistoBuenoDocAdm(String nuAnn, String nuEmi) {
    String vResult = "1";
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT COUNT(1) ");
    sql.append("FROM IDOSGD.TDTV_PERSONAL_VB VB ");
    sql.append("WHERE VB.NU_ANN=? ");
    sql.append("AND VB.NU_EMI=? ");
    sql.append("AND VB.IN_VB='1' ");
    
    try {
      vResult = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { nuAnn, nuEmi });
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vResult;
  }

  
  public String existeVistoBuenoPendienteDocAdm(String nuAnn, String nuEmi) {
    String vResult = "NO_OK";
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT COUNT(1) ");
    sql.append("FROM IDOSGD.TDTV_PERSONAL_VB VB ");
    sql.append("WHERE VB.NU_ANN=? ");
    sql.append("AND VB.NU_EMI=? ");
    sql.append("AND VB.IN_VB <> '1' ");
    try {
      vResult = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { nuAnn, nuEmi });
    } catch (Exception e) {
      e.printStackTrace();
      vResult = e.getMessage().substring(0, 20);
    } 
    return vResult;
  }

  
  public String existePersonalVb(String nuAnn, String nuEmi, String coDep, String coEmpVb) {
    String vResult = "0";
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT COUNT(1) ");
    sql.append("FROM IDOSGD.TDTV_PERSONAL_VB VB ");
    sql.append("WHERE VB.NU_ANN=? ");
    sql.append("AND VB.NU_EMI=? ");
    sql.append("AND VB.CO_DEP=? ");
    sql.append("AND VB.CO_EMP_VB=? ");
    try {
      vResult = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { nuAnn, nuEmi, coDep, coEmpVb });
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vResult;
  }

  
  public String updVistoBuenoPersonal(String nuAnn, String nuEmi, String coDep, String coEmp, String esDocVobo, String coUserMod) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("UPDATE IDOSGD.TDTV_PERSONAL_VB ");
    sqlUpd.append("SET IN_VB=?, ");
    sqlUpd.append("CO_USE_MOD=?, ");
    sqlUpd.append("FE_USE_MOD=CURRENT_TIMESTAMP, ");
    sqlUpd.append("OBS_VB=NULL ");
    sqlUpd.append("WHERE NU_ANN=? ");
    sqlUpd.append("AND NU_EMI=? ");
    sqlUpd.append("AND CO_DEP=? ");
    sqlUpd.append("AND CO_EMP_VB=? ");
    sqlUpd.append("AND IN_VB <> '1' ");
    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { esDocVobo, coUserMod, nuAnn, nuEmi, coDep, coEmp });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }





















  
  public String updVistoBuenoPersonal(String nuAnn, String nuEmi) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("UPDATE IDOSGD.TDTV_PERSONAL_VB ");
    sqlUpd.append("SET IN_VB='0', ");
    sqlUpd.append("OBS_VB=NULL ");
    sqlUpd.append("WHERE NU_ANN=? ");
    sqlUpd.append("AND NU_EMI=? ");
    sqlUpd.append("AND IN_VB='B' ");
    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { nuAnn, nuEmi });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String getEsVobo(String nuAnn, String nuEmi, String coDep, String coEmpVb) {
    String vResult = "1";
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT VB.IN_VB ");
    sql.append("FROM IDOSGD.TDTV_PERSONAL_VB VB ");
    sql.append("WHERE VB.NU_ANN=? ");
    sql.append("AND VB.NU_EMI=? ");
    sql.append("AND VB.CO_DEP=? ");
    sql.append("AND VB.CO_EMP_VB=? ");
    try {
      vResult = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { nuAnn, nuEmi, coDep, coEmpVb });
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vResult;
  }
}
