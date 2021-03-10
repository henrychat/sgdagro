package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.DestinatarioDocumentoEmiConsulBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiPersConsulBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaConsulBean;
import pe.gob.onpe.tramitedoc.dao.ConsultaEmiDocPersDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;








@Repository("consultaEmiDocPersDao")
public class ConsultaEmiDocPersDaoImp
  extends SimpleJdbcDaoBase
  implements ConsultaEmiDocPersDao
{
  public List<DocumentoEmiPersConsulBean> getDocsPersConsulta(DocumentoEmiPersConsulBean buscarDocPer) {
    Object list;
    StringBuffer sql = new StringBuffer();
    boolean bBusqFiltro = false;
    String sqlContains = "";
    Map<String, Object> objectParam = new HashMap<String, Object>();
    
    sql.append("SELECT  Z.* FROM ( ");
    sql.append("\tSELECT\tX.*, ");
    sql.append("\t\tIDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(X.CO_TIP_DOC_ADM) TIPO_DOC, ");
    sql.append("\t\t(CASE WHEN X.NU_CANDES=1 THEN (SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_TI_DES_EMP(X.NU_ANN, X.NU_EMI)) ");
    sql.append("\t\tELSE (SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_TI_DES_EMP_V(X.NU_ANN, X.NU_EMI)) END)DE_DEP_DESTINO, ");
    sql.append("\t\tIDOSGD.PK_SGD_DESCRIPCION_ESTADOS (X.ES_DOC_EMI,'TDTV_REMITOS') ESTADO_DOC, ");
    sql.append("\t\tIDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(X.CO_EMP_RES) DE_EMP_ELABORO, ");
    sql.append("\t\t(SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_EMI_REF(X.NU_ANN, X.NU_EMI)) DE_DEP_REF, ");
    sql.append("\t\tROW_NUMBER() OVER (ORDER BY X.NU_COR_EMI DESC) AS ROWNUM ");
    sql.append("\tFROM ( ");
    sql.append("\t\tSELECT R.NU_ANN,R.NU_EMI,R.NU_COR_EMI,CONVERT(VARCHAR(10), R.FE_EMI, 103) FE_EMI_CORTA, ");
    sql.append("\t\tRR.NU_DOC,R.CO_TIP_DOC_ADM,R.NU_CANDES,R.ES_DOC_EMI,R.CO_EMP_RES, ");
    sql.append("\t\tR.DE_ASU,RR.NU_EXPEDIENTE,RR.IN_EXISTE_DOC EXISTE_DOC,RR.IN_EXISTE_ANEXO EXISTE_ANEXO, ");
    sql.append("\t\tR.NU_DIA_ATE ");
    sql.append("\t\tFROM IDOSGD.TDTV_REMITOS R INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN RR ON R.NU_ANN=RR.NU_ANN AND R.NU_EMI=RR.NU_EMI ");
    sql.append("\t\tWHERE  1=1 ");
    
    String pNUAnn = buscarDocPer.getNuAnn();
    if (!buscarDocPer.getEsFiltroFecha().equals("1") || !pNUAnn.equals("0")) {
      sql.append(" AND R.NU_ANN = :pNuAnn");
      
      objectParam.put("pNuAnn", pNUAnn);
    } 
    
    sql.append(" AND R.TI_EMI='05'");
    sql.append(" AND R.CO_GRU = '2'");
    sql.append(" AND RR.TI_EMI='05'");
    sql.append(" AND R.ES_ELI = '0'");
    
    String pTipoBusqueda = buscarDocPer.getTipoBusqueda();
    String tipoAcceso=buscarDocPer.getTipoAcceso();
    if(tipoAcceso==null || !tipoAcceso.equals("0")) {
    	sql.append(" AND R.CO_EMP_RES = :pcoEmpRes ");
        objectParam.put("pcoEmpRes", buscarDocPer.getCoEmpleado());	
    }
       
    sql.append("    AND R.CO_DEP_EMI = :pcoDepEmi ");
    objectParam.put("pcoDepEmi", buscarDocPer.getCoDepEmi());
    
    if (pTipoBusqueda.equals("1") && buscarDocPer.isEsIncluyeFiltro()) {
      bBusqFiltro = true;
    }

    
    if (pTipoBusqueda.equals("0") || bBusqFiltro) {
      if (buscarDocPer.getTipoDoc() != null && buscarDocPer.getTipoDoc().trim().length() > 0) {
        sql.append(" AND R.CO_TIP_DOC_ADM = :pCoDocEmi ");
        objectParam.put("pCoDocEmi", buscarDocPer.getTipoDoc());
      } 
      if (buscarDocPer.getEstadoDoc() != null && buscarDocPer.getEstadoDoc().trim().length() > 0) {
        sql.append(" AND R.ES_DOC_EMI = :pEsDocEmi ");
        objectParam.put("pEsDocEmi", buscarDocPer.getEstadoDoc());
      } 
      if (buscarDocPer.getCoDepDestino() != null && buscarDocPer.getCoDepDestino().trim().length() > 0) {
        sql.append(" AND CHARINDEX(:pTiEmiDes, RR.TI_EMI_DES) > 0 ");
        objectParam.put("pTiEmiDes", buscarDocPer.getCoDepDestino());
      } 
      if (buscarDocPer.getCoDepRef() != null && buscarDocPer.getCoDepRef().trim().length() > 0) {
        sql.append(" AND CHARINDEX(:pTiEmiRef, RR.TI_EMI_REF) > 0 ");
        objectParam.put("pTiEmiRef", buscarDocPer.getCoDepRef());
      } 
      if (buscarDocPer.getEsFiltroFecha() != null && (buscarDocPer
        .getEsFiltroFecha().equals("1") || buscarDocPer.getEsFiltroFecha().equals("3"))) {
        list = buscarDocPer.getFeEmiIni();
        String vFeEmiFin = buscarDocPer.getFeEmiFin();
        //verificar
        if (list != null && list.toString().trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
          .trim().length() > 0) {
          sql.append("AND R.FE_EMI BETWEEN CONVERT(DATETIME, :pFeEmiIni, 103) AND CONVERT(DATETIME, :pFeEmiFin, 103) + 0.99999 ");
          objectParam.put("pFeEmiIni", list);
          objectParam.put("pFeEmiFin", vFeEmiFin);
        } 
      } 
    } 


    
    if (pTipoBusqueda.equals("1")) {
      
      if (buscarDocPer.getNuCorEmi() != null && buscarDocPer.getNuCorEmi().trim().length() > 0) {
        sql.append(" AND R.NU_COR_EMI = :pnuCorEmi ");
        objectParam.put("pnuCorEmi", buscarDocPer.getNuCorEmi());
      } 
      
      if (buscarDocPer.getNuDoc() != null && buscarDocPer.getNuDoc().trim().length() > 1) {
        sql.append(" AND RR.NU_DOC LIKE '%'+:pnuDocEmi+'%' ");
        objectParam.put("pnuDocEmi", buscarDocPer.getNuDoc());
      } 

      
      if (buscarDocPer.getDeAsu() != null && buscarDocPer.getDeAsu().trim().length() > 1) {

        
        sql.append(" AND UPPER(R.DE_ASU) LIKE '%'+:pDeAsunto+'%' ");
        objectParam.put("pDeAsunto", buscarDocPer.getDeAsu());
      } 
    } 
    
    sql.append("\t) X ");
    sql.append(") Z ");
    sql.append("WHERE Z.ROWNUM < 701 ");
   // sql.append("WHERE Z.ROWNUM  ");
    sql.append("ORDER BY Z.NU_COR_EMI DESC ");






    
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoEmiPersConsulBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return (List<DocumentoEmiPersConsulBean>)list;
  }

  
  public DocumentoEmiPersConsulBean getDocumentoPersonalEmi(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT R.NU_ANN,R.NU_EMI,R.NU_COR_EMI,\nIDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(R.CO_DEP_EMI) DE_DEP_EMI,\nIDOSGD.PK_SGD_DESCRIPCION_DE_LOCAL(R.CO_LOC_EMI) DE_LOC_EMI,\nIDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(R.CO_EMP_EMI) DE_EMP_FIRMO,\nIDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(R.CO_EMP_RES) DE_EMP_ELABORO,\nIDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(R.CO_TIP_DOC_ADM) TIPO_DOC,\nRR.NU_DOC,R.DE_ASU,R.NU_DIA_ATE,\nIDOSGD.PK_SGD_DESCRIPCION_ESTADOS(R.ES_DOC_EMI,'TDTV_REMITOS') DE_ES_DOC_EMI,\nCONVERT(VARCHAR(10),R.FE_EMI,103)FE_EMI_CORTA\nFROM IDOSGD.TDTV_REMITOS R INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN RR ON RR.NU_ANN=R.NU_ANN AND RR.NU_EMI=R.NU_EMI \nWHERE R.NU_ANN=?\nAND R.NU_EMI=?\nAND R.TI_EMI='05'\nAND RR.TI_EMI='05'\nAND R.ES_ELI='0'");














    
    DocumentoEmiPersConsulBean documentoEmiBean = null;
    try {
      documentoEmiBean = (DocumentoEmiPersConsulBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoEmiPersConsulBean.class), new Object[] { pnuAnn, pnuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      documentoEmiBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return documentoEmiBean;
  }

  
  public List<DestinatarioDocumentoEmiConsulBean> getLstDestintariotlbEmi(String pnuAnn, String pnuEmi) {
    List<DestinatarioDocumentoEmiConsulBean> list;
    StringBuffer sql = new StringBuffer();
    
    sql.append("select a.nu_ann,a.nu_emi,a.nu_des,a.co_loc_des co_local,SUBSTRING(IDOSGD.PK_SGD_DESCRIPCION_DE_LOCAL(a.co_loc_des), 1, 100) de_local,\n a.co_dep_des co_dependencia,SUBSTRING(IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(a.co_dep_des), 1, 100) de_dependencia,\n a.co_emp_des co_empleado,SUBSTRING(IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(a.co_emp_des), 1, 100) de_empleado,\n a.co_mot co_tramite,SUBSTRING(IDOSGD.PK_SGD_DESCRIPCION_MOTIVO(a.co_mot), 1, 100) de_tramite,\n a.co_pri co_prioridad,(SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_PRIORIDAD(a.co_pri)) de_prioridad,\n a.de_pro de_indicaciones,\n a.ti_des co_tipo_destino\n FROM IDOSGD.tdtv_destinos a\n where  A.TI_DES = '01' and a.nu_ann = ? and a.nu_emi = ?\n AND a.ES_ELI='0' AND a.NU_EMI_REF is null\n order by A.NU_COR_DES");









    
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
  
  public List<ReferenciaConsulBean> getLstDocumReferenciatblEmi(String pnuAnn, String pnuEmi) {
    List<ReferenciaConsulBean> list;
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT \n IDOSGD.PK_SGD_DESCRIPCION_de_documento (a.co_tip_doc_adm) de_tipo_doc, \n (CASE WHEN a.ti_emi='01' OR a.ti_emi='05' THEN a.nu_doc_emi + '-' + a.nu_ann + '-' + a.de_doc_sig ELSE a.de_doc_sig END)li_nu_doc,\n SUBSTRING(IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(a.co_dep_emi), 1, 100) de_dep_emi, \n CONVERT(VARCHAR(10),a.fe_emi,103) fe_emi_corta, \n b.nu_ann,\n b.nu_emi,\n COALESCE(RTRIM(Ltrim(CAST(b.nu_des AS VARCHAR))),'N') nu_des ,\n b.nu_ann_ref,\n b.nu_emi_ref,\n COALESCE(RTRIM(Ltrim(CAST(b.nu_des_ref AS VARCHAR))),'N') nu_des_ref,\n b.co_ref,\n (CASE WHEN COALESCE(RTRIM(LTRIM(CAST(b.nu_des_ref AS VARCHAR))),'N')='N' THEN 'emi' ELSE 'rec' END)tip_doc_ref,\n a.co_tip_doc_adm,'BD' accion_bd\n FROM IDOSGD.tdtv_remitos a INNER JOIN IDOSGD.TDTR_REFERENCIA b ON   a.nu_ann = b.nu_ann_ref AND a.nu_emi = b.nu_emi_ref \n WHERE  b.nu_ann = ?\n and b.nu_emi = ?");


    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ReferenciaConsulBean.class), new Object[] { pnuAnn, pnuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String getRutaReporte(DocumentoEmiPersConsulBean buscarDocPer) {
    String vResult = "0";
    StringBuffer sql = new StringBuffer();
    try {
      sql.append(" R.NU_ANN=RR.NU_ANN");
      sql.append(" AND R.NU_EMI=RR.NU_EMI");
      String pNUAnn = buscarDocPer.getNuAnn();
      if (!buscarDocPer.getEsFiltroFecha().equals("1") || !pNUAnn.equals("0")) {
        sql.append(" AND R.NU_ANN = '").append(pNUAnn).append("'");
      }
      sql.append(" AND R.TI_EMI='05'");
      sql.append(" AND RR.TI_EMI='05'");
      sql.append(" AND R.ES_ELI = '0'");
      
      sql.append(" AND R.CO_EMP_RES = '").append(buscarDocPer.getCoEmpleado()).append("'");
      
      if (buscarDocPer.getTipoDoc() != null && buscarDocPer.getTipoDoc().trim().length() > 0) {
        sql.append(" AND R.CO_TIP_DOC_ADM = '").append(buscarDocPer.getTipoDoc()).append("'");
      }
      if (buscarDocPer.getEstadoDoc() != null && buscarDocPer.getEstadoDoc().trim().length() > 0) {
        sql.append(" AND R.ES_DOC_EMI = '").append(buscarDocPer.getEstadoDoc()).append("'");
      }
      if (buscarDocPer.getCoDepDestino() != null && buscarDocPer.getCoDepDestino().trim().length() > 0) {
        sql.append(" AND CHARINDEX('").append(buscarDocPer.getCoDepDestino()).append("',RR.TI_EMI_DES ) > 0 ");
      }
      if (buscarDocPer.getCoDepRef() != null && buscarDocPer.getCoDepRef().trim().length() > 0) {
        sql.append(" AND CHARINDEX('").append(buscarDocPer.getCoDepRef()).append("',RR.TI_EMI_REF ) > 0 ");
      }
      if (buscarDocPer.getEsFiltroFecha() != null && (buscarDocPer
        .getEsFiltroFecha().equals("1") || buscarDocPer.getEsFiltroFecha().equals("3"))) {
        String vFeEmiIni = buscarDocPer.getFeEmiIni();
        String vFeEmiFin = buscarDocPer.getFeEmiFin();
        if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
          .trim().length() > 0) {
          sql.append(" AND R.FE_EMI BETWEEN CONVERT(DATETIME,'").append(vFeEmiIni).append("' , 103)").append(" AND CONVERT(DATETIME,'").append(vFeEmiFin).append("' , 103) + 0.99999 ");
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

  
  public List<DocumentoEmiPersConsulBean> getListaReporteBusqueda(DocumentoEmiPersConsulBean buscarDocPer) {
    List<DocumentoEmiPersConsulBean> list;
    String vResult = "0";
    boolean bBusqFiltro = false;
    String sqlContains = "";
    StringBuffer sql = new StringBuffer();
    Map<String, Object> objectParam = new HashMap<String, Object>();

    
    //sql.append("SELECT\tR.NU_COR_EMI,\n\tCONVERT(VARCHAR(10), R.FE_EMI, 103) FE_EMI_CORTA,\n\t(SELECT CDOC_DESDOC\n\tFROM IDOSGD.SI_MAE_TIPO_DOC\n\tWHERE CDOC_TIPDOC = R.CO_TIP_DOC_ADM) TIPO_DOC,RR.NU_DOC,\n\tCASE R.NU_CANDES\n\t\tWHEN 1 THEN (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP](R.NU_ANN, R.NU_EMI))\n\t\tELSE (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP_V](R.NU_ANN, R.NU_EMI))\n\tEND DE_DEP_DESTINO,\n\t(SELECT DE_EST\n\tFROM IDOSGD.TDTR_ESTADOS\n\tWHERE CO_EST + DE_TAB = R.ES_DOC_EMI + 'TDTV_REMITOS') ESTADO_DOC,\n\t(SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM\n\tFROM IDOSGD.RHTM_PER_EMPLEADOS\n\tWHERE CEMP_CODEMP = R.CO_EMP_RES) DE_EMP_ELABORO,\n\tR.DE_ASU,\n\tRR.NU_EXPEDIENTE,\n\t(SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_EMI_REF](R.NU_ANN, R.NU_EMI)) DE_DEP_REF,\n\tR.NU_DIA_ATE\n FROM IDOSGD.TDTV_REMITOS R \n INNER JOIN  IDOSGD.TDTX_REMITOS_RESUMEN RR ON R.NU_ANN=RR.NU_ANN AND R.NU_EMI=RR.NU_EMI  WHERE 1=1 ");
    
    sql.append(" SELECT R.NU_COR_EMI,CONVERT(VARCHAR(10), R.FE_EMI, 103) FE_EMI_CORTA,(SELECT CDOC_DESDOC  FROM IDOSGD.SI_MAE_TIPO_DOC  WHERE CDOC_TIPDOC = R.CO_TIP_DOC_ADM) TIPO_DOC, ");
    sql.append(" RR.NU_DOC,CASE R.NU_CANDES WHEN 1  THEN (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP](R.NU_ANN, R.NU_EMI)) ELSE ");
    sql.append(" (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP_V](R.NU_ANN, R.NU_EMI)) END DE_DEP_DESTINO, ");
    sql.append(" (SELECT DE_EST FROM IDOSGD.TDTR_ESTADOS WHERE CO_EST + DE_TAB = R.ES_DOC_EMI + 'TDTV_REMITOS') ESTADO_DOC, (SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM  "); 
    sql.append(" FROM IDOSGD.RHTM_PER_EMPLEADOS WHERE CEMP_CODEMP = R.CO_EMP_RES) DE_EMP_ELABORO,R.DE_ASU, RR.NU_EXPEDIENTE, ");
    sql.append(" (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_EMI_REF](R.NU_ANN, R.NU_EMI)) DE_DEP_REF, ");
    sql.append(" R.NU_DIA_ATE  FROM IDOSGD.TDTV_REMITOS R  With (readuncommitted) INNER JOIN  IDOSGD.TDTX_REMITOS_RESUMEN RR  With (readuncommitted)  ON R.NU_ANN=RR.NU_ANN AND R.NU_EMI=RR.NU_EMI  WHERE 1=1 ");

    
    try {
      String pNUAnn = buscarDocPer.getNuAnn();
      if (!buscarDocPer.getEsFiltroFecha().equals("1") || !pNUAnn.equals("0")) {
        
        sql.append(" AND R.NU_ANN = :pNuAnn");
        
        objectParam.put("pNuAnn", pNUAnn);
      } 
      sql.append(" AND R.TI_EMI='05'");
      sql.append(" AND RR.TI_EMI='05'");
      sql.append(" AND R.ES_ELI = '0'");

//      sql.append(" AND R.CO_EMP_RES = :pcoEmpRes ");
//      objectParam.put("pcoEmpRes", buscarDocPer.getCoEmpleado());
      
     String tipoAcceso=buscarDocPer.getTipoAcceso();
      if(tipoAcceso==null || !tipoAcceso.equals("0")) {
      	sql.append(" AND R.CO_EMP_RES = :pcoEmpRes ");
          objectParam.put("pcoEmpRes", buscarDocPer.getCoEmpleado());	
      }
         
      sql.append(" AND R.CO_DEP_EMI = :pcoDepEmi ");
      objectParam.put("pcoDepEmi", buscarDocPer.getCoDepEmi());
      
      String pTipoBusqueda = buscarDocPer.getTipoBusqueda();
      if (pTipoBusqueda.equals("1") && buscarDocPer.isEsIncluyeFiltro()) {
        bBusqFiltro = true;
      }


      
      if (pTipoBusqueda.equals("0") || bBusqFiltro) {
        if (buscarDocPer.getTipoDoc() != null && buscarDocPer.getTipoDoc().trim().length() > 0) {
          
          sql.append(" AND R.CO_TIP_DOC_ADM = :pCoDocEmi ");
          objectParam.put("pCoDocEmi", buscarDocPer.getTipoDoc());
        } 
        if (buscarDocPer.getEstadoDoc() != null && buscarDocPer.getEstadoDoc().trim().length() > 0) {
          
          sql.append(" AND R.ES_DOC_EMI = :pEsDocEmi ");
          objectParam.put("pEsDocEmi", buscarDocPer.getEstadoDoc());
        } 
        if (buscarDocPer.getCoDepDestino() != null && buscarDocPer.getCoDepDestino().trim().length() > 0) {
          
          sql.append(" AND CHARINDEX(:pTiEmiDes, RR.TI_EMI_DES) > 0 ");
          objectParam.put("pTiEmiDes", buscarDocPer.getCoDepDestino());
        } 
        if (buscarDocPer.getCoDepRef() != null && buscarDocPer.getCoDepRef().trim().length() > 0) {
          
          sql.append(" AND CHARINDEX(:pTiEmiRef, RR.TI_EMI_REF) > 0 ");
          objectParam.put("pTiEmiRef", buscarDocPer.getCoDepRef());
        } 
        if (buscarDocPer.getEsFiltroFecha() != null && (buscarDocPer
          .getEsFiltroFecha().equals("1") || buscarDocPer.getEsFiltroFecha().equals("3"))) {
          String vFeEmiIni = buscarDocPer.getFeEmiIni();
          String vFeEmiFin = buscarDocPer.getFeEmiFin();
          if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
            .trim().length() > 0) {
            sql.append("AND R.FE_EMI BETWEEN CONVERT(DATETIME, :pFeEmiIni, 103) AND CONVERT(DATETIME, :pFeEmiFin, 103) + 0.99999 ");
            objectParam.put("pFeEmiIni", vFeEmiIni);
            objectParam.put("pFeEmiFin", vFeEmiFin);
          } 
        } 
      } 

      
      if (pTipoBusqueda.equals("1")) {
        if (buscarDocPer.getNuCorEmi() != null && buscarDocPer.getNuCorEmi().trim().length() > 0) {
          sql.append(" AND R.NU_COR_EMI = :pnuCorEmi ");
          objectParam.put("pnuCorEmi", buscarDocPer.getNuCorEmi());
        } 
        
        if (buscarDocPer.getNuDoc() != null && buscarDocPer.getNuDoc().trim().length() > 1) {
          sql.append(" AND RR.NU_DOC LIKE '%'+:pnuDocEmi+'%' ");
          objectParam.put("pnuDocEmi", buscarDocPer.getNuDoc());
        } 

        
        if (buscarDocPer.getDeAsu() != null && buscarDocPer.getDeAsu().trim().length() > 1) {

          
          sql.append(" AND UPPER(R.DE_ASU) LIKE '%'+:pDeAsunto+'%' ");
          objectParam.put("pDeAsunto", buscarDocPer.getDeAsu());
        } 
      } 
      
      sql.append("ORDER BY R.NU_COR_EMI DESC ");

  
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoEmiPersConsulBean.class));
    } catch (Exception ex) {
      
      list = null;
      ex.printStackTrace();
    } 
    
    return list;
  }
}
