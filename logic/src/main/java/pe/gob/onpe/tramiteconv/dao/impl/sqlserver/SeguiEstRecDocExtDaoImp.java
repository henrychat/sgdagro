package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.DestinatarioDocumentoEmiConsulBean;
import pe.gob.onpe.tramitedoc.bean.DocExtRecSeguiEstBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaDocExtRecepBean;
import pe.gob.onpe.tramitedoc.dao.SeguiEstRecDocExtDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;









@Repository("seguiEstRecDocExtDao")
public class SeguiEstRecDocExtDaoImp
  extends SimpleJdbcDaoBase
  implements SeguiEstRecDocExtDao
{
  public List<DocExtRecSeguiEstBean> getLsDocExtRecSegui(DocExtRecSeguiEstBean docBuscar) {
    StringBuffer sql = new StringBuffer();
    boolean bBusqFiltro = false;
    Map<String, Object> objectParam = new HashMap<String, Object>();
    String sqlContains = "";
    List<DocExtRecSeguiEstBean> list = new ArrayList<DocExtRecSeguiEstBean>();

    
    sql.append("SELECT TOP 101 X.*, ");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_TI_EMI_EMP(X.NU_ANN, X.NU_EMI) DE_ORI_EMI,");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(X.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,");


    sql.append("CASE\nWHEN X.TI_DES = '01' THEN\nIDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA (X.CO_DEP_DES)+ ' - ' + IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (X.CO_EMP_DES)\nWHEN X.TI_DES = '02' THEN\nIDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (X.NU_RUC_DES) \nWHEN X.TI_DES = '03' THEN\nIDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL (X.NU_DNI_DES) \nWHEN X.TI_DES = '04' THEN\nIDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN (X.CO_OTR_ORI_DES) \nEND DE_EMP_DES,");


    
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_FORMAT(DATEADD(DAY, (SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_FU_DIA_MAS(CONVERT(DATETIME, X.FE_EMI), X.NU_DIA_ATE)), CONVERT(DATE, X.FE_EMI)),'DD/MM/YYYY') F_LIMITE_CORTA,");
    
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_ESTADOS(X.ES_DOC_EMI,'TDTV_REMITOS') DE_ES_DOC_EMI,");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_MOTIVO(X.CO_MOT) DE_MOTIVO,");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(X.CO_EMP_RES) DE_EMP_REC");
    
    sql.append(" FROM ( ");
    sql.append(" SELECT R.NU_ANN,R.NU_EMI,D.NU_DES,R.NU_COR_EMI,IDOSGD.PK_SGD_DESCRIPCION_FORMAT(R.FE_EMI,'DD/MM/YYYY') FE_EMI_CORTA,");
    sql.append(" RR.NU_EXPEDIENTE,R.CO_TIP_DOC_ADM,R.FE_EMI,R.ES_DOC_EMI,D.CO_MOT,R.CO_EMP_RES,");
    sql.append(" COALESCE(RR.NU_DOC,'S/N') NU_DOC,D.TI_DES,D.CO_DEP_DES,D.CO_EMP_DES,D.NU_RUC_DES,D.NU_DNI_DES,D.CO_OTR_ORI_DES,");
    sql.append(" R.DE_ASU,R.NU_DIA_ATE,");
    sql.append(" CASE");
    sql.append(" WHEN R.NU_DIA_ATE > 0");
    
    sql.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.FE_FINALIZA), CONVERT(DATE, GETDATE()))) <= 2");
    
    sql.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.FE_FINALIZA), CONVERT(DATE, GETDATE()))) > 0");
    sql.append(" AND D.FE_ATE_DOC IS NULL");
    sql.append(" AND D.FE_ARC_DOC IS NULL");
    sql.append(" AND M.IN_DIA = 0");
    sql.append(" THEN '1'");
    sql.append(" WHEN R.NU_DIA_ATE > 0");
    
    sql.append(" AND CONVERT(DATE, GETDATE()) > CONVERT(DATE, E.FE_FINALIZA)");
    sql.append(" AND D.FE_ATE_DOC IS NULL");
    sql.append(" AND D.FE_ARC_DOC IS NULL");
    sql.append(" AND M.IN_DIA = 0");
    sql.append(" THEN '2'");
    sql.append(" WHEN R.NU_DIA_ATE > 0");
    
    sql.append(" AND CONVERT(DATE, GETDATE()) = CONVERT(DATE, E.FE_FINALIZA)");
    sql.append(" AND D.FE_ATE_DOC IS NULL");
    sql.append(" AND D.FE_ARC_DOC IS NULL");
    sql.append(" AND M.IN_DIA = 0");
    sql.append(" THEN '3'");
    sql.append(" WHEN R.NU_DIA_ATE > 0");
    
    sql.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.FE_FINALIZA), CONVERT(DATE, GETDATE()))) > 2");
    sql.append(" AND D.FE_ATE_DOC IS NULL");
    sql.append(" AND D.FE_ARC_DOC IS NULL");
    sql.append(" AND M.IN_DIA = 0");
    sql.append(" THEN '4'");
    sql.append(" WHEN D.FE_ATE_DOC IS NOT NULL OR D.FE_ARC_DOC IS NOT NULL");
    sql.append(" THEN '5'");
    sql.append(" WHEN R.NU_DIA_ATE = 0 OR M.IN_DIA = 0");
    sql.append(" THEN '0'");
    sql.append(" END CO_EST_VEN,");
    sql.append(" CASE");
    sql.append(" WHEN R.NU_DIA_ATE > 0");
    
    sql.append(" AND CONVERT(DATE, GETDATE()) > CONVERT(DATE, E.FE_FINALIZA)");
    sql.append(" AND D.FE_ATE_DOC IS NULL");
    sql.append(" AND D.FE_ARC_DOC IS NULL");
    sql.append(" AND M.IN_DIA = 0");
    
    sql.append(" THEN (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_TRA](E.FE_FINALIZA, CONVERT(DATETIME, GETDATE())))");
    sql.append(" ELSE 0");
    sql.append(" END NU_DIA_EXC,");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_FORMAT(D.FE_ARC_DOC,'DD/MM/YYYY') FE_ARC_DOC_CORTA,");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_FORMAT(D.FE_ATE_DOC,'DD/MM/YYYY') FE_ATE_DOC_CORTA,");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_FORMAT(D.FE_REC_DOC,'DD/MM/YYYY') FE_REC_DOC_CORTA,");
    sql.append(" RR.IN_EXISTE_DOC EXISTE_DOC,");
    
    sql.append("\tCASE COALESCE(RR.TI_EMI_REF,'0') + COALESCE(RR.IN_EXISTE_ANEXO,'2') ");
    sql.append("               WHEN '00' THEN 0 ");
    sql.append("               WHEN '02' THEN 0 ");
    sql.append("               ELSE 1 ");
    sql.append("\tEND EXISTE_ANEXO ");
    sql.append(" FROM IDOSGD.TDTV_REMITOS R WITH (NOLOCK) ");
    sql.append(" INNER JOIN  IDOSGD.TDTV_DESTINOS D WITH (NOLOCK) ON D.NU_ANN=R.NU_ANN AND D.NU_EMI=R.NU_EMI ");
    sql.append(" INNER JOIN   IDOSGD.TDTX_REMITOS_RESUMEN RR WITH (NOLOCK) ON RR.NU_ANN=R.NU_ANN AND RR.NU_EMI=R.NU_EMI ");
    sql.append(" INNER JOIN   IDOSGD.TDTR_MOTIVO M ON M.CO_MOT=D.CO_MOT ");
    sql.append(" INNER JOIN (SELECT Y.NU_ANN, Y.NU_EMI, Y.FE_FINALIZA,");
    
    sql.append(" (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_TRA](CONVERT(DATETIME, GETDATE()), Y.FE_FINALIZA)) NU_DIA_FAL");
    sql.append(" FROM (SELECT X.NU_ANN, X.NU_EMI,");
    
    sql.append(" (DATEADD(DAY, (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_MAS](CONVERT(DATETIME, X.FE_EMI), NU_DIA_ATE)), CONVERT(DATE, X.FE_EMI))) AS FE_FINALIZA");
    sql.append(" FROM IDOSGD.TDTV_REMITOS X) Y) E ON E.NU_ANN=R.NU_ANN AND E.NU_EMI=R.NU_EMI ");
    sql.append(" WHERE 1=1 ");
    
    String pnuAnn = docBuscar.getNuAnn();
    if (!docBuscar.getEsFiltroFecha().equals("1") || !pnuAnn.equals("0")) {
      sql.append(" AND R.NU_ANN = :pnuAnn");
      
      objectParam.put("pnuAnn", pnuAnn);
    } 
    
    sql.append(" AND R.CO_GRU='3'");
    sql.append(" AND R.ES_ELI='0'");
    sql.append(" AND D.ES_ELI='0'");
    sql.append(" AND R.ES_DOC_EMI NOT IN ('5', '9', '7')");
    sql.append(" AND R.TI_EMI IN ('02','03','04')");




    
    String pTipoBusqueda = docBuscar.getTipoBusqueda();
    if (pTipoBusqueda.equals("1") && docBuscar.isEsIncluyeFiltro()) {
      bBusqFiltro = true;
    }

    
    String auxTipoAcceso = docBuscar.getTiAcceso();
    String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1"))) ? auxTipoAcceso : "1";
    if (tiAcceso.equals("1")) {
      sql.append(" AND R.CO_EMP_RES = :pcoEmpRes ");
      objectParam.put("pcoEmpRes", docBuscar.getCoEmpleado());
    }
    else if (docBuscar.getInMesaPartes().equals("0")) {
      
      sql.append(" AND R.CO_DEP = :pCoDep");
      objectParam.put("pCoDep", docBuscar.getCoDependencia());
    } 

    if (pTipoBusqueda.equals("0") || bBusqFiltro) {
      if (docBuscar.getCoTipDocAdm() != null && docBuscar.getCoTipDocAdm().trim().length() > 0) {
        sql.append(" AND R.CO_TIP_DOC_ADM = :pcotipDocAdm ");
        objectParam.put("pcotipDocAdm", docBuscar.getCoTipDocAdm());
      } 
      if (docBuscar.getCoEsDocEmi() != null && docBuscar.getCoEsDocEmi().trim().length() > 0) {
        sql.append(" AND R.ES_DOC_EMI = :pesDocEmi ");
        objectParam.put("pesDocEmi", docBuscar.getCoEsDocEmi());
      } 



      
      if (docBuscar.getTiEmi() != null && docBuscar.getTiEmi().trim().length() > 0) {
        sql.append(" AND R.TI_EMI = :ptiEmi ");
        objectParam.put("ptiEmi", docBuscar.getTiEmi());
      } 
      if (docBuscar.getCoDepEmi() != null && docBuscar.getCoDepEmi().trim().length() > 0) {
        sql.append(" AND R.CO_DEP_EMI = :pcoDepEmi ");
        objectParam.put("pcoDepEmi", docBuscar.getCoDepEmi());
      } 
      if (docBuscar.getCoDepDes() != null && docBuscar.getCoDepDes().trim().length() > 0) {
        sql.append(" AND CHARINDEX(:pTiEmpPro, RR.TI_EMI_DES) > 0 ");
        objectParam.put("pTiEmpPro", docBuscar.getCoDepDes());
      } 
      if (docBuscar.getCoEmpRes() != null && docBuscar.getCoEmpRes().trim().length() > 0) {
        sql.append(" AND R.CO_EMP_RES = :pcoEmpRes ");
        objectParam.put("pcoEmpRes", docBuscar.getCoEmpRes());
      } 
      if (docBuscar.getCoProceso() != null && docBuscar.getCoProceso().trim().length() > 0) {
        sql.append(" AND RR.CO_PROCESO_EXP = :pcoProcesoExp ");
        objectParam.put("pcoProcesoExp", docBuscar.getCoProceso());
      } 
      String pcoEstVencimiento = docBuscar.getCoEstVen();
      if (pcoEstVencimiento != null && pcoEstVencimiento.trim().length() > 0) {
        
        if ("1".equals(pcoEstVencimiento)) {
          
          sql.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.FE_FINALIZA), CONVERT(DATE, GETDATE()))) <=2");
          
          sql.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.FE_FINALIZA), CONVERT(DATE, GETDATE()))) <=2");
          sql.append(" AND d.fe_ate_doc IS NULL");
          sql.append(" AND d.fe_arc_doc IS NULL");
          sql.append(" AND m.in_dia = 0");
        } 
        if ("2".equals(pcoEstVencimiento)) {
          
          sql.append(" AND CONVERT(DATE, GETDATE()) > CONVERT(DATE, E.FE_FINALIZA)");
          sql.append(" AND d.fe_ate_doc IS NULL");
          sql.append(" AND d.fe_arc_doc IS NULL");
          sql.append(" AND m.in_dia = 0");
          sql.append(" AND r.NU_DIA_ATE <> 0");
        } 
        if ("3".equals(pcoEstVencimiento)) {

          
          sql.append(" AND CONVERT(DATE, GETDATE()) = CONVERT(DATE, E.FE_FINALIZA)");
          sql.append(" AND d.fe_ate_doc IS NULL");
          sql.append(" AND d.fe_arc_doc IS NULL");
          sql.append(" AND m.in_dia = 0");
          sql.append(" AND r.NU_DIA_ATE <> 0");
        } 
        if ("4".equals(pcoEstVencimiento)) {
          
          sql.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.FE_FINALIZA), CONVERT(DATE, GETDATE()))) > 2");
          sql.append(" AND d.fe_ate_doc IS NULL");
          sql.append(" AND d.fe_arc_doc IS NULL");
          sql.append(" AND m.in_dia = 0");
        } 
        if ("5".equals(pcoEstVencimiento)) {
          sql.append(" AND d.fe_ate_doc is not null");
          sql.append(" AND d.fe_rec_doc is not null");
        } 
        if ("0".equals(pcoEstVencimiento)) {
          sql.append(" and r.nu_dia_ate = 0");
          sql.append(" and m.in_dia = 0");
          sql.append(" AND d.fe_ate_doc IS NULL");
          sql.append(" AND d.fe_arc_doc IS NULL");
        } 
      } 

 
      if (docBuscar.getEsFiltroFecha() != null && (docBuscar
        .getEsFiltroFecha().equals("1") || docBuscar.getEsFiltroFecha().equals("3"))) {
        String vFeEmiIni = docBuscar.getFeEmiIni();
        String vFeEmiFin = docBuscar.getFeEmiFin();
        if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
          .trim().length() > 0) {
          sql.append(" AND R.FE_EMI BETWEEN CONVERT(DATETIME, :pFeEmiIni, 103) AND CONVERT(DATETIME, :pFeEmiFin, 103) + 0.99999 ");
          objectParam.put("pFeEmiIni", vFeEmiIni);
          objectParam.put("pFeEmiFin", vFeEmiFin);
        } 
      } 
    } 
    
    if (pTipoBusqueda.equals("1")) {
      
      if (!bBusqFiltro && 
        docBuscar.getCoDepEmi() != null && docBuscar.getCoDepEmi().trim().length() > 0) {
        sql.append(" AND R.CO_DEP_EMI = :pcoDepEmi ");
        objectParam.put("pcoDepEmi", docBuscar.getCoDepEmi());
      } 
      
      if (docBuscar.getNuCorEmi() != null && docBuscar.getNuCorEmi().trim().length() > 0) {
        sql.append(" AND R.NU_COR_EMI = :pnuCorEmi ");
        objectParam.put("pnuCorEmi", docBuscar.getNuCorEmi());
      } 
      if (docBuscar.getNuDoc() != null && docBuscar.getNuDoc().trim().length() > 1) {
        sql.append(" AND RR.NU_DOC LIKE ''+:pnuDocEmi+'%' ");
        objectParam.put("pnuDocEmi", docBuscar.getNuDoc());
      } 
      
      if (docBuscar.getNuExpediente() != null && docBuscar.getNuExpediente().trim().length() > 1) {
        sql.append(" AND RR.NU_EXPEDIENTE LIKE '%'+:pnuExpediente+'%' ");
        objectParam.put("pnuExpediente", docBuscar.getNuExpediente());
      } 

      
      if (docBuscar.getDeAsu() != null && docBuscar.getDeAsu().trim().length() > 1) {



        
        sql.append(" AND UPPER(R.DE_ASU) LIKE '%'||:pDeAsunto||'%' ");
        objectParam.put("pDeAsunto", docBuscar.getDeAsu());
      } 
    } 


    
    sql.append(") X ");
    
    sql.append(" ORDER BY X.FE_EMI DESC ");


    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)new RowMapperDocExtSeguiEstado());
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  private class RowMapperDocExtSeguiEstado
    implements ParameterizedRowMapper<DocExtRecSeguiEstBean> {
    public DocExtRecSeguiEstBean mapRow(ResultSet rs, int i) throws SQLException {
      DocExtRecSeguiEstBean docExt = new DocExtRecSeguiEstBean();
      docExt.setNuAnn(rs.getString("NU_ANN"));
      docExt.setNuEmi(rs.getString("NU_EMI"));
      docExt.setNuDes(rs.getString("NU_DES"));
      docExt.setNuCorEmi(rs.getString("NU_COR_EMI"));
      docExt.setFeEmiCorta(rs.getString("FE_EMI_CORTA"));
      docExt.setDeOriEmi(rs.getString("DE_ORI_EMI"));
      docExt.setNuExpediente(rs.getString("NU_EXPEDIENTE"));
      docExt.setDeTipDocAdm(rs.getString("DE_TIP_DOC_ADM"));
      docExt.setNuDoc(rs.getString("NU_DOC"));
      docExt.setDeEmpDes(rs.getString("DE_EMP_DES"));
      docExt.setDeAsu(rs.getString("DE_ASU"));
      docExt.setNuDiaAte(rs.getString("NU_DIA_ATE"));
      docExt.setFeLimiteCorta(rs.getString("F_LIMITE_CORTA"));
      docExt.setNuDiaExc(rs.getString("NU_DIA_EXC"));
      docExt.setFeArcDocCorta(rs.getString("FE_ARC_DOC_CORTA"));
      docExt.setFeAteDocCorta(rs.getString("FE_ATE_DOC_CORTA"));
      docExt.setFeRecDocCorta(rs.getString("FE_REC_DOC_CORTA"));
      docExt.setDeEsDocEmi(rs.getString("DE_ES_DOC_EMI"));
      docExt.setDeMotivo(rs.getString("DE_MOTIVO"));
      docExt.setDeEmpRec(rs.getString("DE_EMP_REC"));
      docExt.setExisteDoc(rs.getString("EXISTE_DOC"));
      docExt.setExisteAnexo(rs.getString("EXISTE_ANEXO"));
      String pcoEstVen = rs.getString("CO_EST_VEN");
      docExt.setCoEstVen(pcoEstVen);
      if (pcoEstVen != null && pcoEstVen.trim().length() > 0) {
        if (pcoEstVen.equals("0")) {
          docExt.setDeEstVen("NORMAL");
        } else if (pcoEstVen.equals("1")) {
          docExt.setDeEstVen("PROXIMO A VENCER");
        } else if (pcoEstVen.equals("2")) {
          docExt.setDeEstVen("VENCIDO");
        } else if (pcoEstVen.equals("3")) {
          docExt.setDeEstVen("VENCE HOY");
        } else if (pcoEstVen.equals("4")) {
          docExt.setDeEstVen("POR VENCER");
        } else if (pcoEstVen.equals("5")) {
          docExt.setDeEstVen("ATENDIDO");
        } 
      }
      return docExt;
    }
    
    private RowMapperDocExtSeguiEstado() {}
  }
  
  public DocExtRecSeguiEstBean getDocumentoExtSeguiBean(String nuAnn, String nuEmi, String nuDes) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT A.NU_ANN,A.NU_EMI,B.NU_DES,A.NU_COR_EMI,C.NU_EXPEDIENTE,IDOSGD.PK_SGD_DESCRIPCION_FORMAT((SELECT FE_EXP FROM IDOSGD.TDTC_EXPEDIENTE WITH (NOLOCK) WHERE NU_ANN_EXP=A.NU_ANN_EXP AND NU_SEC_EXP=A.NU_SEC_EXP),'DD/MM/YYYY') FE_EXP_CORTA,\nIDOSGD.PK_SGD_DESCRIPCION_DE_PROCESO_EXP(C.CO_PROCESO_EXP) DE_PROCESO_EXP,IDOSGD.PK_SGD_DESCRIPCION_TI_EMI_EMP (A.NU_ANN, A.NU_EMI) DE_ORI_EMI,IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (B.CO_EMP_REC) DE_EMP_REC,\nCASE\nWHEN B.TI_DES = '01' THEN\nIDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (B.CO_EMP_DES) \nWHEN B.TI_DES = '02' THEN\nIDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (B.NU_RUC_DES) \nWHEN B.TI_DES = '03' THEN\nIDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL (B.NU_DNI_DES) \nWHEN B.TI_DES = '04' THEN\nIDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN (B.CO_OTR_ORI_DES) \nEND DE_EMP_DES, C.NU_DOC,\nIDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA (B.CO_DEP_DES) DE_DEP_DES,IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO (A.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,IDOSGD.PK_SGD_DESCRIPCION_FORMAT(A.FE_EMI,'DD/MM/YYYY') FE_EMI_CORTA,\nA.DE_ASU,A.NU_DIA_ATE,IDOSGD.PK_SGD_DESCRIPCION_MOTIVO (B.CO_MOT) DE_MOTIVO,B.DE_PRO DE_INDICACIONES,(SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_PRIORIDAD (B.CO_PRI)) DE_PRIORIDAD,\nIDOSGD.PK_SGD_DESCRIPCION_FORMAT(B.FE_REC_DOC,'DD/MM/YYYY HH24:MI:SS') FE_REC_DOC_CORTA,IDOSGD.PK_SGD_DESCRIPCION_FORMAT(B.FE_ATE_DOC,'DD/MM/YYYY') FE_ATE_DOC_CORTA,\nIDOSGD.PK_SGD_DESCRIPCION_FORMAT(B.FE_ARC_DOC,'DD/MM/YYYY') FE_ARC_DOC_CORTA,\nIDOSGD.PK_SGD_DESCRIPCION_ESTADOS (A.ES_DOC_EMI,'TDTV_REMITOS') DE_ES_DOC_EMI,\nIDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (A.CO_EMP_RES) DE_EMP_RES,A.NU_FOLIOS\nFROM IDOSGD.TDTV_REMITOS A WITH (NOLOCK) \nINNER JOIN IDOSGD.TDTV_DESTINOS B WITH (NOLOCK) ON A.NU_ANN = B.NU_ANN AND A.NU_EMI = B.NU_EMI \nINNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN C WITH (NOLOCK) ON C.NU_ANN = A.NU_ANN AND C.NU_EMI = A.NU_EMI \nWHERE A.ES_ELI='0' AND B.ES_ELI='0'\nAND A.ES_DOC_EMI NOT IN ('5', '9', '7')\nAND A.CO_GRU='3'\nand A.TI_EMI IN ('02','03','04')\nAND B.NU_ANN=? AND B.NU_EMI=? AND B.NU_DES=?");

    DocExtRecSeguiEstBean docExt = new DocExtRecSeguiEstBean();
    try {
      docExt = (DocExtRecSeguiEstBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocExtRecSeguiEstBean.class), new Object[] { nuAnn, nuEmi, nuDes });
    }
    catch (EmptyResultDataAccessException e) {
      docExt = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return docExt;
  }
  
  public List<ReferenciaDocExtRecepBean> getLsRefDocExterno(String pnuAnn, String pnuEmi) {
    List<ReferenciaDocExtRecepBean> list;
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT A.NU_ANN,A.NU_EMI,IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(A.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,\nC.NU_DOC de_doc_sig,\nC.NU_EXPEDIENTE,\nIDOSGD.PK_SGD_DESCRIPCION_FORMAT(A.FE_EMI,'DD/MM/YY') FE_EMI_CORTA\nFROM IDOSGD.TDTV_REMITOS A WITH (NOLOCK) \nINNER JOIN IDOSGD.TDTR_REFERENCIA B WITH (NOLOCK) ON  A.NU_ANN=B.NU_ANN_REF AND A.NU_EMI=B.NU_EMI_REF \nINNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN C WITH (NOLOCK) ON C.NU_ANN=A.NU_ANN AND C.NU_EMI=A.NU_EMI \nWHERE B.NU_ANN=? AND\nB.NU_EMI=?");







    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ReferenciaDocExtRecepBean.class), new Object[] { pnuAnn, pnuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DestinatarioDocumentoEmiConsulBean> getLsDestinoEmi(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    List<DestinatarioDocumentoEmiConsulBean> list = null;
    sql.append("select\ta.nu_ann, ");
    sql.append("\t\ta.nu_emi, ");
    sql.append("\t\ta.nu_des, ");
    sql.append("\t\ta.co_loc_des co_local, ");
    sql.append("\t\tSUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_LOCAL](a.co_loc_des), 1, 100) de_local, ");
    sql.append("\t\ta.co_dep_des co_dependencia, ");
    sql.append("\t\tSUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](a.co_dep_des), 1, 100) de_dependencia, ");
    sql.append("\t\ta.co_emp_des co_empleado, ");
    sql.append("\t\tSUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](a.co_emp_des), 1, 100) de_empleado, ");
    sql.append("\t\ta.co_mot co_tramite, ");
    sql.append("\t\tSUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_MOTIVO](a.co_mot), 1, 100) de_tramite, ");
    sql.append("\t\ta.co_pri co_prioridad, ");
    sql.append("\t\t(SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_PRIORIDAD](a.co_pri)) de_prioridad, ");
    sql.append("\t\ta.de_pro de_indicaciones, ");
    sql.append("\t\ta.NU_RUC_DES nu_ruc, ");
    sql.append("\t\tSUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_PROVEEDOR](a.NU_RUC_DES), 1, 100) de_proveedor, ");
    sql.append("\t\ta.NU_DNI_DES nu_dni, ");
    sql.append("\t\tSUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](a.NU_DNI_DES), 1, 100) de_ciudadano, ");
    sql.append("\t\ta.CO_OTR_ORI_DES co_otro_origen, ");
    sql.append("\t\tCASE ");
    sql.append("\t\t\tWHEN a.CO_OTR_ORI_DES IS NOT NULL ");
    sql.append("\t\t\t\tTHEN (SELECT C.DE_APE_PAT_OTR + ' ' + C.DE_APE_MAT_OTR + ', ' + C.DE_NOM_OTR + ' - ' + ");
    sql.append("\t\t\t\t\t\t\t C.DE_RAZ_SOC_OTR + '##' + ISNULL(B.CELE_DESELE, '   ') + '##' + C.NU_DOC_OTR_ORI ");
    sql.append("\t\t\t\t\t  FROM IDOSGD.TDTR_OTRO_ORIGEN C ");
    sql.append("\t\t\t\t\t  LEFT OUTER JOIN ( ");
    sql.append("\t\t\t\t\t\tSELECT CELE_CODELE, CELE_DESELE ");
    sql.append("\t\t\t\t\t\tFROM IDOSGD.SI_ELEMENTO ");
    sql.append("\t\t\t\t\t\tWHERE CTAB_CODTAB ='TIP_DOC_IDENT') B ");
    sql.append("\t\t\t\t\t  ON C.CO_TIP_OTR_ORI = B.CELE_CODELE ");
    sql.append("\t\t\t\t\t  WHERE C.CO_OTR_ORI = a.CO_OTR_ORI_DES ");
    sql.append("\t\t\t\t\t) ");
    sql.append("\t\t\tELSE NULL ");
    sql.append("\t\tEND de_otro_origen_full, ");
    sql.append("\t\ta.ti_des co_tipo_destino ");
    sql.append("FROM IDOSGD.tdtv_destinos a WITH (NOLOCK) ");
    sql.append("where nu_ann = ? ");
    sql.append("and nu_emi = ? ");
    sql.append("AND ES_ELI='0' ");
    sql.append("AND NU_EMI_REF is null ");
    sql.append("order by 3 ");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinatarioDocumentoEmiConsulBean.class), new Object[] { pnuAnn, pnuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String getRutaReporte(DocExtRecSeguiEstBean docBuscar) {
    String vResult;
    StringBuffer sql = new StringBuffer();
    
    try {
      sql.append(" D.NU_ANN=R.NU_ANN");
      sql.append(" AND D.NU_EMI=R.NU_EMI");
      sql.append(" AND RR.NU_ANN=R.NU_ANN");
      sql.append(" AND RR.NU_EMI=R.NU_EMI");
      sql.append(" AND E.NU_ANN=R.NU_ANN");
      sql.append(" AND E.NU_EMI=R.NU_EMI");
      sql.append(" AND M.CO_MOT=D.CO_MOT");
      
      String pnuAnn = docBuscar.getNuAnn();
      if (!docBuscar.getEsFiltroFecha().equals("1") || !pnuAnn.equals("0")) {
        sql.append(" AND R.NU_ANN = '").append(pnuAnn).append("'");
      }
      
      sql.append(" AND R.CO_GRU='3'");
      sql.append(" AND R.ES_ELI='0'");
      sql.append(" AND D.ES_ELI='0'");
      sql.append(" AND R.ES_DOC_EMI NOT IN ('5', '9', '7')");
      sql.append(" AND R.TI_EMI IN ('02','03','04')");
      
      String auxTipoAcceso = docBuscar.getTiAcceso();
      String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1"))) ? auxTipoAcceso : "1";
      if (tiAcceso.equals("1")) {
        sql.append(" AND R.CO_EMP_RES = '").append(docBuscar.getCoEmpleado()).append("'");
      }
      else if (docBuscar.getInMesaPartes().equals("0")) {
        
        sql.append(" AND R.CO_DEP = '").append(docBuscar.getCoDependencia()).append("'");
      } 


      
      if (docBuscar.getCoTipDocAdm() != null && docBuscar.getCoTipDocAdm().trim().length() > 0) {
        sql.append(" AND R.CO_TIP_DOC_ADM = '").append(docBuscar.getCoTipDocAdm()).append("'");
      }
      if (docBuscar.getCoEsDocEmi() != null && docBuscar.getCoEsDocEmi().trim().length() > 0) {
        sql.append(" AND R.ES_DOC_EMI = '").append(docBuscar.getCoEsDocEmi()).append("'");
      }
      if (docBuscar.getTiEmi() != null && docBuscar.getTiEmi().trim().length() > 0) {
        sql.append(" AND R.TI_EMI = '").append(docBuscar.getTiEmi()).append("'");
      }
      if (docBuscar.getCoDepDes() != null && docBuscar.getCoDepDes().trim().length() > 0) {
        sql.append(" AND CHARINDEX('").append(docBuscar.getCoDepDes()).append("', RR.TI_EMI_DES) > 0");
      }
      if (docBuscar.getCoDepEmi() != null && docBuscar.getCoDepEmi().trim().length() > 0) {
        sql.append(" AND R.CO_DEP_EMI = '").append(docBuscar.getCoDepEmi()).append("'");
      }
      if (docBuscar.getCoEmpRes() != null && docBuscar.getCoEmpRes().trim().length() > 0) {
        sql.append(" AND R.CO_EMP_RES = '").append(docBuscar.getCoEmpRes()).append("'");
      }
      if (docBuscar.getCoProceso() != null && docBuscar.getCoProceso().trim().length() > 0) {
        sql.append(" AND RR.CO_PROCESO_EXP = '").append(docBuscar.getCoProceso()).append("'");
      }
      String pcoEstVencimiento = docBuscar.getCoEstVen();
      if (pcoEstVencimiento != null && pcoEstVencimiento.trim().length() > 0) {
        if ("1".equals(pcoEstVencimiento)) {
          sql.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) <= 2");
          sql.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) > 0");
          sql.append(" AND d.fe_ate_doc IS NULL");
          sql.append(" AND d.fe_arc_doc IS NULL");
          sql.append(" AND m.in_dia = 0");
        } 
        if ("2".equals(pcoEstVencimiento)) {
          sql.append(" AND CONVERT(DATE, GETDATE()) > CONVERT(DATE, E.fe_finaliza)");
          sql.append(" AND d.fe_ate_doc IS NULL");
          sql.append(" AND d.fe_arc_doc IS NULL");
          sql.append(" AND m.in_dia = 0");
          sql.append(" AND r.NU_DIA_ATE <> 0");
        } 
        if ("3".equals(pcoEstVencimiento)) {
          
          sql.append(" AND CONVERT(DATE, GETDATE()) = CONVERT(DATE, E.fe_finaliza)");
          sql.append(" AND d.fe_ate_doc IS NULL");
          sql.append(" AND d.fe_arc_doc IS NULL");
          sql.append(" AND m.in_dia = 0");
          sql.append(" AND r.NU_DIA_ATE >= 0");
        } 
        if ("4".equals(pcoEstVencimiento)) {
          sql.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) > 2");
          sql.append(" AND d.fe_ate_doc IS NULL");
          sql.append(" AND d.fe_arc_doc IS NULL");
          sql.append(" AND m.in_dia = 0");
        } 
        if ("5".equals(pcoEstVencimiento)) {
          sql.append(" AND d.fe_ate_doc is not null");
          sql.append(" AND d.fe_rec_doc is not null");
        } 
        if ("0".equals(pcoEstVencimiento)) {
          sql.append(" and r.nu_dia_ate = 0");
          sql.append(" and m.in_dia = 0");
          sql.append(" AND d.fe_ate_doc IS NULL");
          sql.append(" AND d.fe_arc_doc IS NULL");
        } 
      } 






      
      if (docBuscar.getEsFiltroFecha() != null && (docBuscar
        .getEsFiltroFecha().equals("1") || docBuscar.getEsFiltroFecha().equals("3"))) {
        String vFeEmiIni = docBuscar.getFeEmiIni();
        String vFeEmiFin = docBuscar.getFeEmiFin();
        if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
          .trim().length() > 0) {
          sql.append(" AND CAST(R.FE_EMI AS DATE) between CAST('").append(vFeEmiIni).append("' AS DATE) AND CAST('").append(vFeEmiFin).append("' AS DATE) ");
        }
      } 
      sql.append(" ");
      vResult = "0" + sql.toString();
    } catch (Exception ex) {
      vResult = "1" + ex.getMessage();
      ex.printStackTrace();
    } 
    return vResult;
  }

  
  public List<DocExtRecSeguiEstBean> getListaReporteBusqueda(DocExtRecSeguiEstBean docExtRecSeguiEstBean) {
    boolean bBusqFiltro = false;
    String vResult = "";
    Map<String, Object> objectParam = new HashMap<String, Object>();
    String sqlContains = "";
    StringBuffer prutaReporte = new StringBuffer();
    List<DocExtRecSeguiEstBean> list = null;
    
    prutaReporte.append("SELECT\tR.NU_COR_EMI, ");
    prutaReporte.append("\t\tCONVERT(VARCHAR(10), R.FE_EMI, 103) FE_EMI_CORTA, ");
    prutaReporte.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_TI_EMI_EMP](R.NU_ANN, R.NU_EMI) DE_ORI_EMI, ");
    prutaReporte.append("\t\tRR.NU_EXPEDIENTE, ");
    prutaReporte.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](R.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM, ");
    prutaReporte.append("\t\tISNULL(RR.NU_DOC, 'S/N') NU_DOC, ");
    prutaReporte.append("\t\tCASE D.TI_DES ");
    prutaReporte.append("\t\t\tWHEN '01' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](D.CO_DEP_DES) + ' - ' + [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](D.CO_EMP_DES) ");
    prutaReporte.append("\t\t\tWHEN '02' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](D.NU_RUC_DES) ");
    prutaReporte.append("\t\t\tWHEN '03' THEN [IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](D.NU_DNI_DES) ");
    prutaReporte.append("\t\t\tWHEN '04' THEN [IDOSGD].[PK_SGD_DESCRIPCION_OTRO_ORIGEN](D.CO_OTR_ORI_DES) ");
    prutaReporte.append("\t\tEND DE_EMP_DES, ");
    prutaReporte.append("\t\tR.DE_ASU, ");
    prutaReporte.append("\t\tR.NU_DIA_ATE, ");
    prutaReporte.append("\t\tCONVERT(VARCHAR(10), (DATEADD(DAY, (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_MAS](CONVERT(DATETIME, R.FE_EMI), R.NU_DIA_ATE)), CONVERT(DATE, R.FE_EMI))), 103) AS feLimiteCorta, ");
    prutaReporte.append("\t\tCASE ");
    prutaReporte.append("\t\t\tWHEN R.NU_DIA_ATE > 0 ");
    prutaReporte.append("\t\t\t\tAND\t(DATEDIFF(DAY, CONVERT(DATE, E.FE_FINALIZA), CONVERT(DATE, GETDATE()))) <= 2 ");
    prutaReporte.append("\t\t\t\tAND (DATEDIFF(DAY, CONVERT(DATE, E.FE_FINALIZA), CONVERT(DATE, GETDATE()))) > 0 ");
    prutaReporte.append("\t\t\t\tAND D.FE_ATE_DOC IS NULL ");
    prutaReporte.append("\t\t\t\tAND D.FE_ARC_DOC IS NULL ");
    prutaReporte.append("\t\t\t\tAND M.IN_DIA = 0 ");
    prutaReporte.append("\t\t\tTHEN '1' ");
    prutaReporte.append("\t\t\tWHEN R.NU_DIA_ATE > 0 ");
    prutaReporte.append("\t\t\t\tAND\tCONVERT(DATE, GETDATE()) > CONVERT(DATE, E.FE_FINALIZA) ");
    prutaReporte.append("\t\t\t\tAND D.FE_ATE_DOC IS NULL ");
    prutaReporte.append("\t\t\t\tAND D.FE_ARC_DOC IS NULL ");
    prutaReporte.append("\t\t\t\tAND M.IN_DIA = 0 ");
    prutaReporte.append("\t\t\tTHEN '2' ");
    prutaReporte.append("\t\t\tWHEN R.NU_DIA_ATE > 0 ");
    prutaReporte.append("\t\t\t\tAND\tCONVERT(DATE, GETDATE()) = CONVERT(DATE, E.FE_FINALIZA) ");
    prutaReporte.append("\t\t\t\tAND D.FE_ATE_DOC IS NULL ");
    prutaReporte.append("\t\t\t\tAND D.FE_ARC_DOC IS NULL ");
    prutaReporte.append("\t\t\t\tAND M.IN_DIA = 0 ");
    prutaReporte.append("\t\t\tTHEN '3' ");
    prutaReporte.append("\t\t\tWHEN R.NU_DIA_ATE > 0 ");
    prutaReporte.append("\t\t\t\tAND\t(DATEDIFF(DAY, CONVERT(DATE, E.FE_FINALIZA), CONVERT(DATE, GETDATE()))) > 2 ");
    prutaReporte.append("\t\t\t\tAND D.FE_ATE_DOC IS NULL ");
    prutaReporte.append("\t\t\t\tAND D.FE_ARC_DOC IS NULL ");
    prutaReporte.append("\t\t\t\tAND M.IN_DIA = 0 ");
    prutaReporte.append("\t\t\tTHEN '4' ");
    prutaReporte.append("\t\t\tWHEN D.FE_ATE_DOC IS NOT NULL OR D.FE_ARC_DOC IS NOT NULL ");
    prutaReporte.append("\t\t\tTHEN '5' ");
    prutaReporte.append("\t\t\tWHEN R.NU_DIA_ATE = 0 OR M.IN_DIA = 0 ");
    prutaReporte.append("\t\t\tTHEN '0' ");
    prutaReporte.append("\t\tEND CO_EST_VEN, ");
    prutaReporte.append("\t\tCASE ");
    prutaReporte.append("\t\t\tWHEN R.NU_DIA_ATE > 0 ");
    prutaReporte.append("\t\t\t\tAND\tCONVERT(DATE, GETDATE()) > CONVERT(DATE, E.FE_FINALIZA) ");
    prutaReporte.append("\t\t\t\tAND D.FE_ATE_DOC IS NULL ");
    prutaReporte.append("\t\t\t\tAND D.FE_ARC_DOC IS NULL ");
    prutaReporte.append("\t\t\t\tAND M.IN_DIA = 0 ");
    prutaReporte.append("\t\t\tTHEN (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_TRA](E.FE_FINALIZA, CONVERT(DATETIME, GETDATE()))) ");
    prutaReporte.append("\t\t\tELSE 0 ");
    prutaReporte.append("\t\tEND NU_DIA_EXC, ");
    prutaReporte.append("\t\tCONVERT(VARCHAR(10), D.FE_ARC_DOC, 103) FE_ARC_DOC_CORTA, ");
    prutaReporte.append("\t\tCONVERT(VARCHAR(10), D.FE_ATE_DOC, 103) FE_ATE_DOC_CORTA, ");
    prutaReporte.append("\t\tCONVERT(VARCHAR(10), D.FE_REC_DOC, 103) FE_REC_DOC_CORTA, ");
    prutaReporte.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS](R.ES_DOC_EMI,'TDTV_REMITOS') DE_ES_DOC_EMI, ");
    prutaReporte.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_MOTIVO](D.CO_MOT) DE_MOTIVO, ");
    prutaReporte.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](D.CO_EMP_REC) DE_EMP_REC, ");
    prutaReporte.append("\t\tR.NU_FOLIOS, ");
    prutaReporte.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](R.CO_EMP_RES) DE_EMP_RES, ");
    prutaReporte.append("\t\t(SELECT DE_NOMBRE ");
    prutaReporte.append("\t\tFROM IDOSGD.TDTR_PROCESOS_EXP ");
    prutaReporte.append("\t\tWHERE CO_PROCESO = RR.CO_PROCESO_EXP ");
    prutaReporte.append("\t\tAND ES_ESTADO='1') DE_PROCESO_EXP, ");
    prutaReporte.append("\t\tRR.CO_PROCESO_EXP ");
    prutaReporte.append("FROM IDOSGD.TDTV_REMITOS R WITH (NOLOCK) ");
    prutaReporte.append("INNER JOIN\t IDOSGD.TDTV_DESTINOS D WITH (NOLOCK) ON D.NU_ANN=R.NU_ANN AND D.NU_EMI=R.NU_EMI ");
    prutaReporte.append("INNER JOIN\t IDOSGD.TDTX_REMITOS_RESUMEN RR WITH (NOLOCK) ON RR.NU_ANN=R.NU_ANN AND RR.NU_EMI=R.NU_EMI ");
    prutaReporte.append("INNER JOIN\t IDOSGD.TDTR_MOTIVO M WITH (NOLOCK) ON M.CO_MOT=D.CO_MOT ");
    prutaReporte.append("INNER JOIN\t (SELECT Y.NU_ANN, ");
    prutaReporte.append("\t\t\t Y.NU_EMI, ");
    prutaReporte.append("\t\t\t Y.FE_FINALIZA, ");
    prutaReporte.append("\t\t\t (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_TRA](CONVERT(DATETIME, GETDATE()), Y.FE_FINALIZA)) NU_DIA_FAL ");
    prutaReporte.append("\t  FROM (SELECT X.NU_ANN, ");
    prutaReporte.append("\t\t\t\t   X.NU_EMI, ");
    prutaReporte.append("\t\t\t\t   (DATEADD(DAY, (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_MAS](CONVERT(DATETIME, X.FE_EMI), NU_DIA_ATE)), CONVERT(DATE, X.FE_EMI))) AS FE_FINALIZA ");
    prutaReporte.append("\t\t\tFROM IDOSGD.TDTV_REMITOS X) Y) E ON E.NU_ANN=R.NU_ANN AND E.NU_EMI=R.NU_EMI ");
    prutaReporte.append("WHERE 1=1 ");
    
    try {
      String pnuAnn = docExtRecSeguiEstBean.getNuAnn();
      if (!docExtRecSeguiEstBean.getEsFiltroFecha().equals("1") || !pnuAnn.equals("0")) {
        prutaReporte.append(" AND R.NU_ANN = '").append(pnuAnn).append("'");
      }
      
      prutaReporte.append(" AND R.CO_GRU='3'");
      prutaReporte.append(" AND R.ES_ELI='0'");
      prutaReporte.append(" AND D.ES_ELI='0'");
      prutaReporte.append(" AND R.ES_DOC_EMI NOT IN ('5', '9', '7')");
      prutaReporte.append(" AND R.TI_EMI IN ('02','03','04')");
      
      String auxTipoAcceso = docExtRecSeguiEstBean.getTiAcceso();
      String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1"))) ? auxTipoAcceso : "1";
      if (tiAcceso.equals("1")) {
        prutaReporte.append(" AND R.CO_EMP_RES = '").append(docExtRecSeguiEstBean.getCoEmpleado()).append("'");
      }
      else if (docExtRecSeguiEstBean.getInMesaPartes().equals("0")) {
        prutaReporte.append(" AND R.CO_DEP = '").append(docExtRecSeguiEstBean.getCoDependencia()).append("'");
      } 

      
      String pTipoBusqueda = docExtRecSeguiEstBean.getTipoBusqueda();
      if (pTipoBusqueda.equals("1") && docExtRecSeguiEstBean.isEsIncluyeFiltro()) {
        bBusqFiltro = true;
      }

      
      if (pTipoBusqueda.equals("0") || bBusqFiltro) {
        
        if (docExtRecSeguiEstBean.getCoTipDocAdm() != null && docExtRecSeguiEstBean.getCoTipDocAdm().trim().length() > 0) {
          prutaReporte.append(" AND R.CO_TIP_DOC_ADM = '").append(docExtRecSeguiEstBean.getCoTipDocAdm()).append("'");
        }
        if (docExtRecSeguiEstBean.getCoEsDocEmi() != null && docExtRecSeguiEstBean.getCoEsDocEmi().trim().length() > 0) {
          prutaReporte.append(" AND R.ES_DOC_EMI = '").append(docExtRecSeguiEstBean.getCoEsDocEmi()).append("'");
        }
        if (docExtRecSeguiEstBean.getTiEmi() != null && docExtRecSeguiEstBean.getTiEmi().trim().length() > 0) {
          prutaReporte.append(" AND R.TI_EMI = '").append(docExtRecSeguiEstBean.getTiEmi()).append("'");
        }
        if (docExtRecSeguiEstBean.getCoDepDes() != null && docExtRecSeguiEstBean.getCoDepDes().trim().length() > 0) {
          prutaReporte.append(" AND CHARINDEX('").append(docExtRecSeguiEstBean.getCoDepDes()).append("', RR.TI_EMI_DES) > 0");
        }
        if (docExtRecSeguiEstBean.getCoDepEmi() != null && docExtRecSeguiEstBean.getCoDepEmi().trim().length() > 0) {
          prutaReporte.append(" AND R.CO_DEP_EMI = '").append(docExtRecSeguiEstBean.getCoDepEmi()).append("'");
        }
        if (docExtRecSeguiEstBean.getCoEmpRes() != null && docExtRecSeguiEstBean.getCoEmpRes().trim().length() > 0) {
          prutaReporte.append(" AND R.CO_EMP_RES = '").append(docExtRecSeguiEstBean.getCoEmpRes()).append("'");
        }
        if (docExtRecSeguiEstBean.getCoProceso() != null && docExtRecSeguiEstBean.getCoProceso().trim().length() > 0) {
          prutaReporte.append(" AND RR.CO_PROCESO_EXP = '").append(docExtRecSeguiEstBean.getCoProceso()).append("'");
        }
        String pcoEstVencimiento = docExtRecSeguiEstBean.getCoEstVen();
        if (pcoEstVencimiento != null && pcoEstVencimiento.trim().length() > 0) {
          if ("1".equals(pcoEstVencimiento)) {
            prutaReporte.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) <= 2");
            prutaReporte.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) > 0");
            prutaReporte.append(" AND d.fe_ate_doc IS NULL");
            prutaReporte.append(" AND d.fe_arc_doc IS NULL");
            prutaReporte.append(" AND m.in_dia = 0");
          } 
          if ("2".equals(pcoEstVencimiento)) {
            prutaReporte.append(" AND CONVERT(DATE, GETDATE()) > CONVERT(DATE, E.fe_finaliza)");
            prutaReporte.append(" AND d.fe_ate_doc IS NULL");
            prutaReporte.append(" AND d.fe_arc_doc IS NULL");
            prutaReporte.append(" AND m.in_dia = 0");
            prutaReporte.append(" AND r.NU_DIA_ATE <> 0");
          } 
          if ("3".equals(pcoEstVencimiento)) {
            
            prutaReporte.append(" AND CONVERT(DATE, GETDATE()) = CONVERT(DATE, E.fe_finaliza)");
            prutaReporte.append(" AND d.fe_ate_doc IS NULL");
            prutaReporte.append(" AND d.fe_arc_doc IS NULL");
            prutaReporte.append(" AND m.in_dia = 0");
            prutaReporte.append(" AND r.NU_DIA_ATE >= 0");
          } 
          if ("4".equals(pcoEstVencimiento)) {
            prutaReporte.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) > 2");
            prutaReporte.append(" AND d.fe_ate_doc IS NULL");
            prutaReporte.append(" AND d.fe_arc_doc IS NULL");
            prutaReporte.append(" AND m.in_dia = 0");
          } 
          if ("5".equals(pcoEstVencimiento)) {
            prutaReporte.append(" AND d.fe_ate_doc is not null");
            prutaReporte.append(" AND d.fe_rec_doc is not null");
          } 
          if ("0".equals(pcoEstVencimiento)) {
            prutaReporte.append(" and r.nu_dia_ate = 0");
            prutaReporte.append(" and m.in_dia = 0");
            prutaReporte.append(" AND d.fe_ate_doc IS NULL");
            prutaReporte.append(" AND d.fe_arc_doc IS NULL");
          } 
        } 






        
        if (docExtRecSeguiEstBean.getEsFiltroFecha() != null && (docExtRecSeguiEstBean
          .getEsFiltroFecha().equals("1") || docExtRecSeguiEstBean.getEsFiltroFecha().equals("3"))) {
          String vFeEmiIni = docExtRecSeguiEstBean.getFeEmiIni();
          String vFeEmiFin = docExtRecSeguiEstBean.getFeEmiFin();
          if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
            .trim().length() > 0) {
            prutaReporte.append(" AND CAST(R.FE_EMI AS DATE) between CAST('").append(vFeEmiIni).append("' AS DATE) AND CAST('").append(vFeEmiFin).append("' AS DATE) ");
          }
        } 
      } 
      
      if (pTipoBusqueda.equals("1")) {
        if (!bBusqFiltro && 
          docExtRecSeguiEstBean.getCoDepEmi() != null && docExtRecSeguiEstBean.getCoDepEmi().trim().length() > 0) {
          prutaReporte.append(" AND R.CO_DEP_EMI = :pcoDepEmi ");
          objectParam.put("pcoDepEmi", docExtRecSeguiEstBean.getCoDepEmi());
        } 
        
        if (docExtRecSeguiEstBean.getNuCorEmi() != null && docExtRecSeguiEstBean.getNuCorEmi().trim().length() > 0) {
          prutaReporte.append(" AND R.NU_COR_EMI = :pnuCorEmi ");
          objectParam.put("pnuCorEmi", docExtRecSeguiEstBean.getNuCorEmi());
        } 
        if (docExtRecSeguiEstBean.getNuDoc() != null && docExtRecSeguiEstBean.getNuDoc().trim().length() > 1) {
          prutaReporte.append(" AND RR.NU_DOC LIKE ''+:pnuDocEmi+'%' ");
          objectParam.put("pnuDocEmi", docExtRecSeguiEstBean.getNuDoc());
        } 
        
        if (docExtRecSeguiEstBean.getNuExpediente() != null && docExtRecSeguiEstBean.getNuExpediente().trim().length() > 1) {
          prutaReporte.append(" AND RR.NU_EXPEDIENTE LIKE '%'+:pnuExpediente+'%' ");
          objectParam.put("pnuExpediente", docExtRecSeguiEstBean.getNuExpediente());
        } 

        
        if (docExtRecSeguiEstBean.getDeAsu() != null && docExtRecSeguiEstBean.getDeAsu().trim().length() > 1) {
          prutaReporte.append(" AND CONTAINS(R.DE_ASU, :pBusquedaTextual) ");
          sqlContains = "SELECT RETORNO FROM [IDOSGD].PK_SGD_DESCRIPCION_QUERYFILTER('" + docExtRecSeguiEstBean.getDeAsu() + "')";
        } 
        if (docExtRecSeguiEstBean.getCoTipoExp() != null && !docExtRecSeguiEstBean.getCoTipoExp().equals("")) {
          prutaReporte.append(" AND C.CCOD_TIPO_EXP = :codTipoExp");
          objectParam.put("codTipoExp", docExtRecSeguiEstBean.getCoTipoExp());
        } 
        
        if (docExtRecSeguiEstBean.getCoOriDoc() != null && !docExtRecSeguiEstBean.getCoOriDoc().equals("")) {
          prutaReporte.append(" AND R.CCOD_ORIGING = :coOriDoc");
          objectParam.put("coOriDoc", docExtRecSeguiEstBean.getCoOriDoc());
        } 
        
        if (docExtRecSeguiEstBean.getBusResultado().equals("1"))
        {
          if (docExtRecSeguiEstBean.getCoTipoPersona().equals("03")) {
            prutaReporte.append(" AND R.NU_DNI_EMI = :pNumDni");
            objectParam.put("pNumDni", docExtRecSeguiEstBean.getBusNumDni());
          }
          else if (docExtRecSeguiEstBean.getCoTipoPersona().equals("02")) {
            prutaReporte.append(" AND R.NU_RUC_EMI = :pNumRuc");
            objectParam.put("pNumRuc", docExtRecSeguiEstBean.getBusNumRuc());
          }
          else if (docExtRecSeguiEstBean.getCoTipoPersona().equals("04")) {
            prutaReporte.append(" AND R.CO_OTR_ORI_EMI = :pCoOtr");
            objectParam.put("pCoOtr", docExtRecSeguiEstBean.getBusCoOtros());
          } 
        }
      } 

      
      prutaReporte.append(" ORDER BY R.FE_EMI DESC ");
      
      if (sqlContains.length() > 0) {
        String cadena = (String)this.jdbcTemplate.queryForObject(sqlContains, String.class);
        objectParam.put("pBusquedaTextual", cadena);
      } 
      list = this.namedParameterJdbcTemplate.query(prutaReporte.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocExtRecSeguiEstBean.class));
    }
    catch (Exception ex) {
      vResult = "1" + ex.getMessage();
      ex.printStackTrace();
    } 
    return list;
  }

@Override
public List<DocExtRecSeguiEstBean> getListaReporteResumen(DocExtRecSeguiEstBean docExtRecSeguiEstBean) {
	// TODO Auto-generated method stub
	return null;
}
}
