package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import pe.gob.onpe.tramitedoc.bean.BuscarDocTrazabilidadBean;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoSeguiEstEmiBean;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiSeguiBean;

import pe.gob.onpe.tramitedoc.bean.DocumentoSeguiEstEmitidoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoSeguiTrazaBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaConsulBean;

import pe.gob.onpe.tramitedoc.dao.SeguiTrazabilidadDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;


@Repository("seguiTrazabilidadDao")
public class SeguiTrazabilidadDaoImp    extends SimpleJdbcDaoBase   implements SeguiTrazabilidadDao {
	
	
  public  List<DocumentoSeguiTrazaBean> getListDocSeguiTrazab(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean) {
    StringBuffer sql = new StringBuffer();
    boolean bBusqFiltro = false;
    Map<String, Object> objectParam = new HashMap<String, Object>();
    
    List<DocumentoSeguiTrazaBean> list = new ArrayList<DocumentoSeguiTrazaBean>();

  
    sql.append(" select ROW_NUMBER() OVER(ORDER BY a.fe_emi  ASC) as nro , a.NU_EMI, a.NU_ANN , a.CO_GRU,R.IN_EXISTE_DOC existeDoc, ");
    sql.append(" case when CO_GRU='3' then ISNULL(A.COBS_DOCUMENTO,'') else ISNULL(A.OBS_DOC,'') end as obsRemite, ");
    sql.append(" ISNULL(A.CEXP_CORREOE,'') as correoRemite,ISNULL(A.CTELEFONO,'') as telefonoRemite, ");
    sql.append(" case when A.CO_GRU IN ('1','3')  and  TI_EMI_ref  is null then [IDOSGD].[PK_SGD_DESCRIPCION_DE_SIGLA](A.CO_DEP_EMI) else '' end as siglaOri,");
    sql.append(" CASE WHEN a.CO_GRU='3' THEN 'MESA PARTES'  WHEN a.CO_GRU='1' THEN 'ADMINISTRATIVO'   ELSE 'PROFESIONAL' END AS BANDEJA,  ");
    sql.append(" CONVERT (VARCHAR,FE_EMI,113) fecEmision,   [IDOSGD].[PK_SGD_DESCRIPCION_TI_DESTINO](A.TI_EMI) DE_TI_EMI,  ");
    sql.append(" [IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](A.CO_DEP) deDependenciaMP, a.CO_TIP_DOC_ADM, ");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO (a.CO_TIP_DOC_ADM) TIPO_DOCUMENTO, R.NU_DOC, A.ES_DOC_EMI, ISNULL(R.NU_EXPEDIENTE,'') NU_EXPEDIENTE,   ");
    sql.append(" case when CO_GRU='3'  then [IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS_MP](A.ES_DOC_EMI, 'TDTV_REMITOS') else ");
    sql.append(" [IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS](A.ES_DOC_EMI, 'TDTV_REMITOS')  end  ESTADO_DOC_DES, ");
	sql.append(" [IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](A.CO_DEP_EMI) as DEPENDENCIA_ORIGEN, IDOSGD.PK_SGD_DESCRIPCION_TI_EMI_EMP (a.NU_ANN, a.NU_EMI) REMITENTE,   ");
    sql.append(" DE_ASU ASUNTO,CASE A.NU_CANDES     WHEN 1 THEN (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP](A.NU_ANN, A.NU_EMI))  ");
    sql.append(" ELSE (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP_V](A.NU_ANN, A.NU_EMI))     END as destinos ");
    sql.append(" FROM IDOSGD.TDTV_REMITOS A  With (readuncommitted) INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN R  With (readuncommitted)  ON R.NU_ANN = A.NU_ANN AND R.NU_EMI = A.NU_EMI ");
    sql.append("AND A.ES_ELI = '0' 		AND A.ES_DOC_EMI NOT IN ('5', '9', '7') AND A.IN_OFICIO = '0' AND A.CO_GRU IN ('1','2','3')  ");
    sql.append("Where 1=1");

    
    String pNUAnn = buscarDocumentoSeguiEstEmiBean.getCoAnnio();
   
  
    String pTipoBusqueda = buscarDocumentoSeguiEstEmiBean.getTipoBusqueda();
    if (pTipoBusqueda.equals("1") ) {
      bBusqFiltro = true;
    }

    
//    String auxTipoAcceso = buscarDocumentoSeguiEstEmiBean.getTiAcceso();
//    String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1") || auxTipoAcceso.equals("2"))) ? auxTipoAcceso : "1";
//    if (buscarDocumentoSeguiEstEmiBean.getCoEmpElaboro() != null && buscarDocumentoSeguiEstEmiBean.getCoEmpElaboro().trim().length() > 0 && (pTipoBusqueda.equals("0") || bBusqFiltro) && tiAcceso.equals("0")) {
//      sql.append(" AND A.CO_EMP_RES = :pcoEmpRes ");
//      objectParam.put("pcoEmpRes", buscarDocumentoSeguiEstEmiBean.getCoEmpElaboro());
//    }
//    else if (tiAcceso.equals("1")) {
//      sql.append(" AND A.CO_EMP_RES = :pcoEmpRes ");
//      objectParam.put("pcoEmpRes", buscarDocumentoSeguiEstEmiBean.getCoEmpleado());
//    } else if (tiAcceso.equals("2")) {
//      sql.append(" AND (A.CO_EMP_RES = :pcoEmpRes OR A.CO_EMP_EMI = :pcoEmpRes) ");
//      objectParam.put("pcoEmpRes", buscarDocumentoSeguiEstEmiBean.getCoEmpleado());
//    } 
   if(buscarDocumentoSeguiEstEmiBean.getEsDocOrigen()!=null && buscarDocumentoSeguiEstEmiBean.getEsDocOrigen().equals("1")) {
	   sql.append(" AND A.CO_GRU IN ('1','3')  and  TI_EMI_ref  is null ");  
   }
    
    if (pTipoBusqueda.equals("0") || bBusqFiltro) {
      if (buscarDocumentoSeguiEstEmiBean.getEstadoDoc() != null && buscarDocumentoSeguiEstEmiBean.getEstadoDoc().trim().length() > 0) {
    	sql.append(" AND A.ES_DOC_EMI = :pEsDocEmi ");
        objectParam.put("pEsDocEmi", buscarDocumentoSeguiEstEmiBean.getEstadoDoc());
      } 
      if (buscarDocumentoSeguiEstEmiBean.getCoTipoDocAdm() != null && buscarDocumentoSeguiEstEmiBean.getCoTipoDocAdm().trim().length() > 0) {
        sql.append(" AND A.CO_TIP_DOC_ADM = :pCoDocEmi ");
        objectParam.put("pCoDocEmi", buscarDocumentoSeguiEstEmiBean.getCoTipoDocAdm());
      } 
      if (buscarDocumentoSeguiEstEmiBean.getCoRefOrigen() != null && buscarDocumentoSeguiEstEmiBean.getCoRefOrigen().trim().length() > 0) {
        sql.append(" AND CHARINDEX(:pTiEmiRef, C.TI_EMI_REF) > 0 ");
        objectParam.put("pTiEmiRef", buscarDocumentoSeguiEstEmiBean.getCoRefOrigen());
      } 
    //  busqueda.getBusCodDepEmiRef()
      if (buscarDocumentoSeguiEstEmiBean.getBusCodDepEmiRef()!= null && buscarDocumentoSeguiEstEmiBean.getBusCodDepEmiRef().trim().length() > 0) {
        sql.append(" AND A.CO_DEP_EMI = :pCoDepOrigenEmi ");
        objectParam.put("pCoDepOrigenEmi", buscarDocumentoSeguiEstEmiBean.getBusCodDepEmiRef());
      } 
//      if (buscarDocumentoSeguiEstEmiBean.getCoDepOrigen() != null && buscarDocumentoSeguiEstEmiBean.getCoDepOrigen().trim().length() > 0) {
//          sql.append(" AND A.CO_DEP_EMI = :pCoDepOrigen ");
//          objectParam.put("pCoDepOrigen", buscarDocumentoSeguiEstEmiBean.getCoDepOrigen());
//        } 
      if (buscarDocumentoSeguiEstEmiBean.getCoDepDestino() != null && buscarDocumentoSeguiEstEmiBean.getCoDepDestino().trim().length() > 0) {
        sql.append(" AND CHARINDEX(:pTiEmpPro, r.TI_EMI_DES) > 0 ");
        objectParam.put("pTiEmpPro", buscarDocumentoSeguiEstEmiBean.getCoDepDestino());
      } 
   
      if (buscarDocumentoSeguiEstEmiBean.getCoRefOrigen()!= null && buscarDocumentoSeguiEstEmiBean.getCoRefOrigen().trim().length() > 0) {
          sql.append(" AND CHARINDEX(:pTiEmiRef,r.TI_EMI_REF) > 0 ");
          objectParam.put("pTiEmiRef", buscarDocumentoSeguiEstEmiBean.getCoRefOrigen());
        } 
      if (buscarDocumentoSeguiEstEmiBean.getCoDepDestino()!= null && buscarDocumentoSeguiEstEmiBean.getCoDepDestino().trim().length() > 0) {
          sql.append(" AND   TI_EMI_DES LIKE '%'+:coDepDestino+'%' ");
          objectParam.put("coDepDestino", buscarDocumentoSeguiEstEmiBean.getCoDepDestino());
        } 
       if (buscarDocumentoSeguiEstEmiBean.getCoEmpElaboro() != null && buscarDocumentoSeguiEstEmiBean.getCoEmpElaboro().trim().length() > 0 ) {
          sql.append(" AND A.CO_EMP_RES = :coEmpRes");
          objectParam.put("coEmpRes", buscarDocumentoSeguiEstEmiBean.getCoEmpElaboro());
        }
     
    }
    
      
   if (buscarDocumentoSeguiEstEmiBean.getIncluyeFiltroFecha()!=null && buscarDocumentoSeguiEstEmiBean.getIncluyeFiltroFecha().equals("1")){
	   String vFeEmiIni = buscarDocumentoSeguiEstEmiBean.getFeEmiIni();
       String vFeEmiFin = buscarDocumentoSeguiEstEmiBean.getFeEmiFin();
       if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
         .trim().length() > 0) {
         sql.append(" AND A.FE_EMI BETWEEN CONVERT(DATETIME, :pFeEmiIni, 103) AND CONVERT(DATETIME, :pFeEmiFin, 103) + 0.99999 ");
         objectParam.put("pFeEmiIni", vFeEmiIni);
         objectParam.put("pFeEmiFin", vFeEmiFin);
         
       } 
      // sql.append(" AND A.NU_ANN = :pNuAnn ");
       
      //  objectParam.put("pNuAnn", pNUAnn);
    }
    
 //   if (pTipoBusqueda.equals("1")) {
//      if (!bBusqFiltro && 
//        buscarDocumentoSeguiEstEmiBean.getCoDependencia() != null && buscarDocumentoSeguiEstEmiBean.getCoDependencia().trim().length() > 0) {
//        sql.append(" AND B.CO_DEP_DES = :pcoDepDes ");
//        objectParam.put("pcoDepDes", buscarDocumentoSeguiEstEmiBean.getCoDependencia());
//      } 
      
      if (buscarDocumentoSeguiEstEmiBean.getBusNumDoc() != null && buscarDocumentoSeguiEstEmiBean.getBusNumDoc().trim().length() > 1) {
        sql.append(" AND A.NU_DOC_EMI LIKE '%'+:pnuDocEmi+'%' ");
        objectParam.put("pnuDocEmi", buscarDocumentoSeguiEstEmiBean.getBusNumDoc());
      } 
      
      if (buscarDocumentoSeguiEstEmiBean.getBusNumExpediente() != null && buscarDocumentoSeguiEstEmiBean.getBusNumExpediente().trim().length() > 1) {
       // sql.append(" AND R.NU_EXPEDIENTE LIKE '%'+:pnuExpediente+'%' ");
    	  sql.append(" AND R.NU_EXPEDIENTE LIKE '%'+:pnuExpediente ");
        objectParam.put("pnuExpediente", buscarDocumentoSeguiEstEmiBean.getBusNumExpediente());
      } 

      
      if (buscarDocumentoSeguiEstEmiBean.getBusAsunto() != null && buscarDocumentoSeguiEstEmiBean.getBusAsunto().trim().length() > 1) {



        
        sql.append(" AND UPPER(A.DE_ASU) LIKE '%'+:pDeAsunto+'%' ");
        objectParam.put("pDeAsunto", buscarDocumentoSeguiEstEmiBean.getBusAsunto());
      } 
 //   } 

  
  //  sql.append("ORDER BY A.FE_EMI desc,A.NU_COR_EMI desc  ");




    
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoSeguiTrazaBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DependenciaBean> getListDestinatarioEmi(String coDep, String pdeDepEmite) {
    StringBuffer sql = new StringBuffer();
    
    boolean vfiltro = (pdeDepEmite != null && !pdeDepEmite.trim().equals(""));
    
    sql.append("SELECT  DE_DEPENDENCIA, ");
    sql.append("        CO_DEPENDENCIA, ");
    sql.append("        DE_CORTA_DEPEN ");
    sql.append("FROM IDOSGD.RHTM_DEPENDENCIA ");
    sql.append("WHERE co_nivel <> '6' ");
    sql.append("  AND IN_BAJA = '0' ");
    if (vfiltro) {
      sql.append(" AND (DE_DEPENDENCIA LIKE '%' + ? + '%' OR DE_CORTA_DEPEN LIKE ? + '%') ");
    }
    sql.append("UNION ");
    sql.append("SELECT  DE_DEPENDENCIA, ");
    sql.append("        CO_DEPENDENCIA, ");
    sql.append("        DE_CORTA_DEPEN ");
    sql.append("FROM IDOSGD.RHTM_DEPENDENCIA ");
    sql.append("WHERE co_nivel = '6' ");
    sql.append("   AND IN_BAJA = '0' ");
    sql.append("   AND (CO_DEPEN_PADRE in (select CO_DEPEN_PADRE co_dep ");
    sql.append("\t\t\t       from IDOSGD.RHTM_DEPENDENCIA ");
    sql.append("                           where CO_DEPENDENCIA = ?) ");
    sql.append("   OR ? IN (CO_DEPENDENCIA, CO_DEPEN_PADRE)) ");
    if (vfiltro) {
      sql.append(" AND (DE_DEPENDENCIA LIKE '%' + ? + '%' OR DE_CORTA_DEPEN LIKE ? + '%') ");
    }
    sql.append("UNION ");
    sql.append("SELECT  ' [TODOS]', ");
    sql.append("        NULL, ");
    sql.append("        NULL ");
    if (vfiltro) {
      sql.append(" where ' [TODOS]' like '%' + ? + '%' ");
    }
    sql.append("ORDER BY 1 ");
    
    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
    
    try {
      if (vfiltro) {
        list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { pdeDepEmite, pdeDepEmite, coDep, coDep, pdeDepEmite, pdeDepEmite, pdeDepEmite });
      } else {
        
        list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { coDep, coDep });
      } 
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public DocumentoSeguiEstEmitidoBean existeDocumentoReferenciado(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean) {
    StringBuffer sql = new StringBuffer();
    DocumentoSeguiEstEmitidoBean documentoSeguiEstEmitidoBean = null;
    sql.append("SELECT A.NU_ANN,A.NU_EMI,A.NU_COR_DOC FROM IDOSGD.TDTV_REMITOS A\nWHERE A.NU_ANN=?\nAND A.CO_DEP_EMI=?\nAND A.TI_EMI='01'\nAND A.CO_TIP_DOC_ADM=?\nAND A.NU_DOC_EMI=?\nAND A.ES_ELI='0'\nAND A.ES_DOC_EMI NOT IN ('5','7','9')");


    
    try {
      documentoSeguiEstEmitidoBean = (DocumentoSeguiEstEmitidoBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoSeguiEstEmitidoBean.class), new Object[] { buscarDocumentoSeguiEstEmiBean
            .getBusCoAnnio(), buscarDocumentoSeguiEstEmiBean.getBusCodDepEmiRef(), buscarDocumentoSeguiEstEmiBean.getBusCodTipoDocRef(), buscarDocumentoSeguiEstEmiBean
            .getBusNumDocRef() });
      documentoSeguiEstEmitidoBean.setMsjResult("OK");
    } catch (EmptyResultDataAccessException e) {
      documentoSeguiEstEmitidoBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return documentoSeguiEstEmitidoBean;
  }

  
  public List<DocumentoSeguiEstEmitidoBean> getDocumentosReferenciadoBusq(DocumentoSeguiEstEmitidoBean documentoSeguiEstEmitidoBean, String tiAcceso) {
    StringBuffer sql = new StringBuffer();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    List<DocumentoSeguiEstEmitidoBean> list = new ArrayList<DocumentoSeguiEstEmitidoBean>();
    
    sql.append("SELECT  Z.* FROM ( ");
    sql.append("\tSELECT\tA.*, ");
    sql.append("\t\tROW_NUMBER() OVER (ORDER BY A.FE_EMI DESC) AS ROWNUM ");
    sql.append("\tFROM ( ");
    sql.append("\t\tSELECT\tDISTINCT A.NU_COR_EMI, ");
    sql.append("\t\t\t\t(SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_EMI_REF](A.NU_ANN, A.NU_EMI)) DE_EMI_REF, ");
    sql.append("\t\t\t\tA.NU_ANN, ");
    sql.append("\t\t\t\tA.NU_EMI, ");
    sql.append("\t\t\t\tB.NU_DES, ");
    sql.append("\t\t\t\tB.NU_COR_DES, ");
    sql.append("\t\t\t\tCONVERT(VARCHAR(10), A.FE_EMI, 3) FE_EMI_CORTA, ");
    sql.append("\t\t\t\tA.FE_EMI, ");
    sql.append("\t\t\t\t[IDOSGD].[PK_SGD_DESCRIPCION_TI_EMI_EMP](A.NU_ANN, A.NU_EMI) DE_ORI_EMI, ");
    sql.append("\t\t\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](A.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM, ");
    sql.append("\t\t\t\tC.NU_DOC, ");
    sql.append("\t\t\t\tCASE B.TI_DES ");
    sql.append("\t\t\t\t\tWHEN '01' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](B.CO_EMP_DES) ");
    sql.append("\t\t\t\t\tWHEN '02' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](B.NU_RUC_DES) ");
    sql.append("\t\t\t\t\tWHEN '03' THEN [IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](B.NU_DNI_DES) ");
    sql.append("\t\t\t\t\tWHEN '04' THEN [IDOSGD].[PK_SGD_DESCRIPCION_OTRO_ORIGEN](B.CO_OTR_ORI_DES) ");
    sql.append("\t\t\t\tEND DE_EMP_DES, ");
    sql.append("\t\t\t\tUPPER (A.DE_ASU) DE_ASU_M,C.NU_EXPEDIENTE, ");
    sql.append("\t\t\t\t[IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS](B.ES_DOC_REC, 'TDTV_DESTINOS') DE_ES_DOC_DES, ");
    sql.append("\t\t\t\tC.IN_EXISTE_DOC EXISTE_DOC, ");
    sql.append("\t\t\t\tCASE ISNULL(C.TI_EMI_REF, '0') + ISNULL(C.IN_EXISTE_ANEXO, '2') ");
    sql.append("\t\t\t\t\tWHEN '00' THEN 0 ");
    sql.append("\t\t\t\t\tWHEN '02' THEN 0 ");
    sql.append("\t\t\t\t\tELSE 1 ");
    sql.append("\t\t\t\tEND EXISTE_ANEXO, ");
    sql.append("\t\t\t\tISNULL(B.CO_PRI, '1') CO_PRI, ");
    sql.append("\t\t\t\tB.ES_DOC_REC, ");
    sql.append("\t\t\t\tB.FE_REC_DOC, ");
    sql.append("\t\t\t\tCONVERT(VARCHAR(10), B.FE_REC_DOC, 3) FE_REC_CORTA, ");
    sql.append("\t\t\t\t(SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM ");
    sql.append("\t\t\t\tFROM IDOSGD.RHTM_PER_EMPLEADOS ");
    sql.append("\t\t\t\tWHERE CEMP_CODEMP = A.CO_EMP_RES) DE_EMP_RES, ");
    sql.append("\t\t\t\t[IDOSGD].[PK_SGD_DESCRIPCION_MOTIVO](B.CO_MOT) DE_MOTIVO, ");
    sql.append("\t\t\t\t(SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM ");
    sql.append("\t\t\t\tFROM IDOSGD.RHTM_PER_EMPLEADOS ");
    sql.append("\t\t\t\tWHERE CEMP_CODEMP = B.CO_EMP_REC) DE_EMP_REC, ");
    sql.append("\t\t\t\tB.CO_ETIQUETA_REC, ");
    sql.append("\t\t\t\tA.NU_DIA_ATE, ");
    sql.append("\t\t\t\tDATEADD(DAY, (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_MAS](CONVERT(DATETIME, A.FE_EMI), A.NU_DIA_ATE)), CONVERT(DATE, A.FE_EMI)) F_LIMITE, ");
    sql.append("\t\t\t\tCONVERT(VARCHAR(10), (DATEADD(DAY, (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_MAS](CONVERT(DATETIME, A.FE_EMI), A.NU_DIA_ATE)), CONVERT(DATE, A.FE_EMI))), 3) F_LIMITE_CORTA, ");
    sql.append("\t\t\t\tCASE ");
    sql.append("\t\t\t\t\tWHEN a.nu_dia_ate > 0 ");
    sql.append("\t\t\t\t\t\tAND\t(DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) <= 2 ");
    sql.append("\t\t\t\t\t\tAND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) > 0 ");
    sql.append("\t\t\t\t\t\tAND b.fe_ate_doc IS NULL ");
    sql.append("\t\t\t\t\t\tAND b.fe_arc_doc IS NULL ");
    sql.append("\t\t\t\t\t\tAND d.in_dia = 0 ");
    sql.append("\t\t\t\t\tTHEN '1' ");
    sql.append("\t\t\t\t\tWHEN a.nu_dia_ate >  ");
    sql.append("\t\t\t\t\t\tAND\tCONVERT(DATE, GETDATE()) > CONVERT(DATE, E.fe_finaliza) ");
    sql.append("\t\t\t\t\t\tAND b.fe_ate_doc IS NULL ");
    sql.append("\t\t\t\t\t\tAND b.fe_arc_doc IS NULL ");
    sql.append("\t\t\t\t\t\tAND d.in_dia = 0 ");
    sql.append("\t\t\t\t\tTHEN '2' ");
    sql.append("\t\t\t\t\tWHEN a.nu_dia_ate > 0 ");
    sql.append("\t\t\t\t\t\tAND\tCONVERT(DATE, GETDATE()) = CONVERT(DATE, E.fe_finaliza) ");
    sql.append("\t\t\t\t\t\tAND b.fe_ate_doc IS NULL ");
    sql.append("\t\t\t\t\t\tAND b.fe_arc_doc IS NULL ");
    sql.append("\t\t\t\t\t\tAND d.in_dia = 0 ");
    sql.append("\t\t\t\t\tTHEN '3' ");
    sql.append("\t\t\t\t\tWHEN a.nu_dia_ate > 0 ");
    sql.append("\t\t\t\t\t\tAND\t(DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) > 2 ");
    sql.append("\t\t\t\t\t\tAND b.fe_ate_doc IS NULL ");
    sql.append("\t\t\t\t\t\tAND b.fe_arc_doc IS NULL ");
    sql.append("\t\t\t\t\t\tAND d.in_dia = 0 ");
    sql.append("\t\t\t\t\tTHEN '4' ");
    sql.append("\t\t\t\t\tWHEN b.fe_ate_doc IS NOT NULL OR b.fe_arc_doc IS NOT NULL ");
    sql.append("\t\t\t\t\tTHEN '5' ");
    sql.append("\t\t\t\t\tWHEN a.nu_dia_ate = 0 OR D.in_dia = 0 ");
    sql.append("\t\t\t\t\tTHEN '0' ");
    sql.append("\t\t\t\tEND co_est_ven, ");
    sql.append("\t\t\t\tCASE ");
    sql.append("\t\t\t\t\tWHEN a.nu_dia_ate > 0 ");
    sql.append("\t\t\t\t\t\tAND\tCONVERT(DATE, GETDATE()) > CONVERT(DATE, E.fe_finaliza) ");
    sql.append("\t\t\t\t\t\tAND b.fe_ate_doc IS NULL ");
    sql.append("\t\t\t\t\t\tAND b.fe_arc_doc IS NULL ");
    sql.append("\t\t\t\t\t\tAND d.in_dia = 0 ");
    sql.append("\t\t\t\t\tTHEN (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_TRA](fe_finaliza, CONVERT(DATETIME, GETDATE()))) ");
    sql.append("\t\t\t\t\tELSE 0 ");
    sql.append("\t\t\t\tEND nu_dia_exc, ");
    sql.append("\t\t\t\tb.fe_arc_doc, ");
    sql.append("\t\t\t\tb.fe_ate_doc, ");
    sql.append("\t\t\t\tCONVERT(VARCHAR(10), b.fe_arc_doc, 3) fe_arc_doc_CORTA, ");
    sql.append("\t\t\t\tCONVERT(VARCHAR(10), b.fe_ate_doc, 3) fe_ate_doc_CORTA ");
    sql.append("\tFROM IDOSGD.TDTV_REMITOS A  ");
    sql.append("\tINNER JOIN\t IDOSGD.TDTV_DESTINOS B ON  A.NU_ANN=B.NU_ANN AND A.NU_EMI=B.NU_EMI ");
    sql.append("\tINNER JOIN\t IDOSGD.TDTX_REMITOS_RESUMEN C ON C.NU_ANN=B.NU_ANN AND C.NU_EMI = B.NU_EMI ");
    sql.append("\tINNER JOIN\t IDOSGD.TDTR_MOTIVO D ON  b.co_mot = d.co_mot ");
    sql.append("\tINNER JOIN\t (SELECT y.nu_ann, ");
    sql.append("\t\t\t\t y.nu_emi, ");
    sql.append("\t\t\t\t y.fe_finaliza, ");
    sql.append("\t\t\t\t (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_TRA](CONVERT(DATETIME, GETDATE()), y.fe_finaliza)) nu_dia_fal ");
    sql.append("\t\t  FROM (SELECT x.nu_ann, ");
    sql.append("\t\t\t\t\t   x.nu_emi, ");
    sql.append("\t\t\t\t\t   (DATEADD(DAY, (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_MAS](CONVERT(DATETIME, x.fe_emi), nu_dia_ate)), CONVERT(DATE, x.fe_emi))) AS fe_finaliza ");
    sql.append("\t\t\t\tFROM IDOSGD.tdtv_remitos x) y ");
    sql.append("\t\t ) E ON a.nu_ann = E.nu_ann AND a.nu_emi = E.nu_emi ");
    sql.append("\tINNER JOIN\t  [IDOSGD].[PK_SGD_DESCRIPCION_ARBOL_SEG](:pCoAnio, :pNuEmi) F ON F.NU_ANN=A.NU_ANN AND F.NU_EMI=A.NU_EMI ");
    
    objectParam.put("pCoAnio", documentoSeguiEstEmitidoBean.getNuAnn());
    objectParam.put("pNuEmi", documentoSeguiEstEmitidoBean.getNuEmi());
    sql.append(" WHERE ");
    String pNUAnn = documentoSeguiEstEmitidoBean.getNuAnn();
    sql.append(" A.NU_ANN = :pNuAnn ");
    objectParam.put("pNuAnn", pNUAnn);

    
    sql.append(" AND A.ES_DOC_EMI NOT IN ('5', '9', '7') ");
    sql.append(" AND A.ES_ELI = '0' ");
    sql.append(" AND B.ES_ELI = '0' ");
    
    if (tiAcceso.equals("1")) {
      sql.append(" AND A.CO_EMP_RES = :pcoEmpRes");
      objectParam.put("pcoEmpRes", documentoSeguiEstEmitidoBean.getCoEmpRes());
    } 
    
    sql.append("\t) A ");
    sql.append(") Z ");
    sql.append("WHERE Z.ROWNUM < 301 ");
    sql.append("ORDER BY Z.FE_EMI DESC ");
    
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoSeguiEstEmitidoBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public DocumentoEmiSeguiBean getDocumentoEmiSeguiBean(String pnuAnn, String pnuEmi, String pnuDes) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT      B.DE_ANE, ");
    sql.append("\t\tA.NU_ANN, ");
    sql.append("\t\tA.NU_EMI, ");
    sql.append("\t\tB.NU_DES, ");
    sql.append("\t\tB.NU_COR_DES, ");
    sql.append("\t\tC.NU_EXPEDIENTE, ");
    sql.append("\t\tCONVERT(VARCHAR(10), (SELECT FE_EXP ");
    sql.append("\t\t\t\t\t\t\t  FROM IDOSGD.TDTC_EXPEDIENTE ");
    sql.append("\t\t\t\t\t\t\t  WHERE NU_ANN_EXP=A.NU_ANN_EXP ");
    sql.append("\t\t\t\t\t\t\t  AND NU_SEC_EXP=A.NU_SEC_EXP), 103) FE_EXP_CORTA, ");
    sql.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_PROCESO_EXP](C.CO_PROCESO_EXP) DE_PROCESO_EXP, ");
    sql.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_TI_EMI_EMP] (A.NU_ANN, A.NU_EMI) DE_ORI_EMI, ");
    sql.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](B.CO_EMP_REC) DE_EMP_REC, ");
    sql.append("\t\tCASE B.TI_DES ");
    sql.append("\t\t\tWHEN '01' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](B.CO_EMP_DES) ");
    sql.append("\t\t\tWHEN '02' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](B.NU_RUC_DES) ");
    sql.append("\t\t\tWHEN '03' THEN [IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](B.NU_DNI_DES) ");
    sql.append("\t\t\tWHEN '04' THEN [IDOSGD].[PK_SGD_DESCRIPCION_OTRO_ORIGEN](B.CO_OTR_ORI_DES) ");
    sql.append("\t\tEND DE_EMP_DES, ");
    sql.append("\t\tCASE A.TI_EMI ");
    sql.append("\t\t\tWHEN '01' THEN A.NU_DOC_EMI + '-' + A.NU_ANN + '/' + A.DE_DOC_SIG ");
    sql.append("\t\t\tWHEN '05' THEN A.NU_DOC_EMI + '-' + A.NU_ANN + '/' + A.DE_DOC_SIG ");
    sql.append("\t\t\tELSE A.DE_DOC_SIG ");
    sql.append("\t\tEND NU_DOC, ");
    sql.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](B.CO_DEP_DES) DE_DEP_DES, ");
    sql.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](A.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM, ");
    sql.append("\t\tCONVERT(VARCHAR(10), A.FE_EMI, 103) FE_EMI_CORTA, ");
    sql.append("\t\tA.DE_ASU,A.NU_DIA_ATE, ");
    sql.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_MOTIVO](B.CO_MOT) DE_MOTIVO, ");
    sql.append("\t\tB.DE_PRO DE_INDICACIONES, ");
    sql.append("\t\t(SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_PRIORIDAD](B.CO_PRI)) DE_PRIORIDAD, ");
    sql.append("\t\t(CONVERT(VARCHAR(10), B.FE_REC_DOC, 103) + ' '+\tCONVERT(VARCHAR(20), B.FE_REC_DOC, 108)) FE_REC_CORTA, ");
    sql.append("\t\tCONVERT(VARCHAR(10), B.FE_ATE_DOC, 103) FE_ATENCION_CORTA, ");
    sql.append("\t\tCONVERT(VARCHAR(10), B.FE_ARC_DOC, 103) FE_ARCHIVAMIENTO_CORTA, ");
    sql.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS](B.ES_DOC_REC, 'TDTV_DESTINOS') DE_ES_DOC_REC, ");
    sql.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](A.CO_EMP_RES) DE_EMP_RES ");
    sql.append("FROM IDOSGD.TDTV_REMITOS A  ");
    sql.append("INNER JOIN\t IDOSGD.TDTV_DESTINOS B ON A.NU_ANN = B.NU_ANN AND A.NU_EMI = B.NU_EMI ");
    sql.append("INNER JOIN\t IDOSGD.TDTX_REMITOS_RESUMEN C ON C.NU_ANN = A.NU_ANN  AND C.NU_EMI = A.NU_EMI ");
    sql.append("WHERE A.ES_ELI='0' ");
    sql.append("AND B.ES_ELI='0' ");
    sql.append("AND A.ES_DOC_EMI NOT IN ('5', '9', '7') ");
    sql.append("AND A.IN_OFICIO = '0' ");
    sql.append("AND B.NU_ANN=? ");
    sql.append("AND B.NU_EMI=? ");
    sql.append("AND B.NU_DES=? ");
    
    DocumentoEmiSeguiBean documentoEmiSeguiBean = new DocumentoEmiSeguiBean();
    try {
      documentoEmiSeguiBean = (DocumentoEmiSeguiBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoEmiSeguiBean.class), new Object[] { pnuAnn, pnuEmi, pnuDes });
    }
    catch (EmptyResultDataAccessException e) {
      documentoEmiSeguiBean = null;
    } catch (Exception e) {
      System.out.println("nu_ann=" + pnuAnn + ",nu_emi=" + pnuEmi + "nu_des=" + pnuDes);
      e.printStackTrace();
    } 
    return documentoEmiSeguiBean;
  }

  
  public List<ReferenciaConsulBean> getDocumentosRefRecepAdm(String snuAnn, String snuEmi) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT  [IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](a.co_tip_doc_adm) de_tipo_doc, ");
    sql.append("        CASE a.ti_emi ");
    sql.append("\t\tWHEN '01' THEN a.nu_doc_emi + '-' + a.nu_ann + '/' + a.de_doc_sig ");
    sql.append("\t\tWHEN '05' THEN a.nu_doc_emi + '-' + a.nu_ann + '/' + a.de_doc_sig ");
    sql.append("\t\tELSE a.de_doc_sig ");
    sql.append("        END li_nu_doc, ");
    sql.append("        CONVERT(varchar(10), a.fe_emi, 103) fe_emi_corta, ");
    sql.append("        b.co_ref, ");
    sql.append("        b.nu_ann, ");
    sql.append("        b.nu_emi, ");
    sql.append("        b.nu_des, ");
    sql.append("        b.nu_ann_ref, ");
    sql.append("        b.nu_emi_ref, ");
    sql.append("        b.nu_des_ref ");
    sql.append("FROM IDOSGD.tdtv_remitos a  ");
    sql.append("INNER JOIN\t IDOSGD.TDTR_REFERENCIA b ON a.nu_ann = b.nu_ann_ref AND a.nu_emi = b.nu_emi_ref ");
    sql.append("WHERE   b.NU_EMI=? ");
    sql.append("AND b.NU_ANN=? ");
    
    List<ReferenciaConsulBean> list = new ArrayList<ReferenciaConsulBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ReferenciaConsulBean.class), new Object[] { snuEmi, snuAnn });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String getRutaReporte(BuscarDocTrazabilidadBean buscarDocumentoSeguiEstEmiBean) {
    String vResult;
    StringBuffer sql = new StringBuffer();
    
    try {
      String pNUAnn = buscarDocumentoSeguiEstEmiBean.getCoAnnio();
      if (!buscarDocumentoSeguiEstEmiBean.getEsFiltroFecha().equals("1") || !pNUAnn.equals("0")) {
        sql.append(" A.NU_ANN = '").append(pNUAnn).append("'");
      }
      sql.append(" AND A.NU_ANN=B.NU_ANN ");
      sql.append(" AND A.NU_EMI=B.NU_EMI ");
      sql.append(" AND C.NU_EMI = B.NU_EMI ");
      sql.append(" AND A.ES_DOC_EMI NOT IN ('5', '9', '7') ");
      sql.append(" AND A.ES_ELI = '0' ");
      sql.append(" AND B.ES_ELI = '0' ");
      sql.append(" AND b.co_mot = d.co_mot ");
      sql.append(" AND a.nu_ann = E.nu_ann ");
      sql.append(" AND a.nu_emi = E.nu_emi ");
      sql.append(" AND A.TI_EMI='01' ");

      
      String auxTipoAcceso = buscarDocumentoSeguiEstEmiBean.getTiAcceso();
      String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1") || auxTipoAcceso.equals("2"))) ? auxTipoAcceso : "1";
      if (buscarDocumentoSeguiEstEmiBean.getCoEmpElaboro() != null && buscarDocumentoSeguiEstEmiBean.getCoEmpElaboro().trim().length() > 0 && tiAcceso.equals("0")) {
        sql.append(" AND A.CO_EMP_RES = '").append(buscarDocumentoSeguiEstEmiBean.getCoEmpElaboro()).append("'");
      }
      else if (tiAcceso.equals("1")) {
        sql.append(" AND A.CO_EMP_RES = '").append(buscarDocumentoSeguiEstEmiBean.getCoEmpleado()).append("'");
      } else if (tiAcceso.equals("2")) {
        sql.append(" AND (A.CO_EMP_RES = '").append(buscarDocumentoSeguiEstEmiBean.getCoEmpleado()).append("' OR A.CO_EMP_EMI = '").append(buscarDocumentoSeguiEstEmiBean.getCoEmpleado()).append("') ");
      } 


      
      if (buscarDocumentoSeguiEstEmiBean.getEstadoDoc() != null && buscarDocumentoSeguiEstEmiBean.getEstadoDoc().trim().length() > 0) {
        sql.append(" AND B.ES_DOC_REC = '").append(buscarDocumentoSeguiEstEmiBean.getEstadoDoc()).append("'");
      }
      if (buscarDocumentoSeguiEstEmiBean.getCoTipoDocAdm() != null && buscarDocumentoSeguiEstEmiBean.getCoTipoDocAdm().trim().length() > 0) {
        sql.append(" AND A.CO_TIP_DOC_ADM = '").append(buscarDocumentoSeguiEstEmiBean.getCoTipoDocAdm()).append("'");
      }
      if (buscarDocumentoSeguiEstEmiBean.getCoRefOrigen() != null && buscarDocumentoSeguiEstEmiBean.getCoRefOrigen().trim().length() > 0) {
        sql.append(" AND CHARINDEX('").append(buscarDocumentoSeguiEstEmiBean.getCoRefOrigen()).append("', C.TI_EMI_REF) > 0 ");
      }
      if (buscarDocumentoSeguiEstEmiBean.getCoDepOrigen() != null && buscarDocumentoSeguiEstEmiBean.getCoDepOrigen().trim().length() > 0) {
        sql.append(" AND A.CO_DEP_EMI = '").append(buscarDocumentoSeguiEstEmiBean.getCoDepOrigen()).append("'");
      }
      if (buscarDocumentoSeguiEstEmiBean.getCoDepDestino() != null && buscarDocumentoSeguiEstEmiBean.getCoDepDestino().trim().length() > 0) {
        sql.append(" AND CHARINDEX('").append(buscarDocumentoSeguiEstEmiBean.getCoDepDestino()).append("', C.TI_EMI_DES) > 0 ");
      }
      
      if (buscarDocumentoSeguiEstEmiBean.getCoVencimiento() != null && buscarDocumentoSeguiEstEmiBean.getCoVencimiento().trim().length() > 0) {
        
        if ("1".equals(buscarDocumentoSeguiEstEmiBean.getCoVencimiento())) {
          sql.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) <= 2");
          sql.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) > 0");
          sql.append(" AND b.fe_ate_doc IS NULL");
          sql.append(" AND b.fe_arc_doc IS NULL");
          sql.append(" AND d.in_dia = 0");
        } 
        if ("2".equals(buscarDocumentoSeguiEstEmiBean.getCoVencimiento())) {
          sql.append(" AND CONVERT(DATE, GETDATE()) > CONVERT(DATE, E.fe_finaliza)");
          sql.append(" AND b.fe_ate_doc IS NULL");
          sql.append(" AND b.fe_arc_doc IS NULL");
          sql.append(" AND d.in_dia = 0");
          sql.append(" AND A.NU_DIA_ATE <> 0");
        } 
        if ("3".equals(buscarDocumentoSeguiEstEmiBean.getCoVencimiento())) {
          
          sql.append(" AND CONVERT(DATE, GETDATE()) = CONVERT(DATE, E.fe_finaliza)");
          sql.append(" AND b.fe_ate_doc IS NULL");
          sql.append(" AND b.fe_arc_doc IS NULL");
          sql.append(" AND d.in_dia = 0");
          sql.append(" AND A.NU_DIA_ATE <> 0");
        } 
        if ("4".equals(buscarDocumentoSeguiEstEmiBean.getCoVencimiento())) {
          sql.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) > 2");
          sql.append(" AND b.fe_ate_doc IS NULL");
          sql.append(" AND b.fe_arc_doc IS NULL");
          sql.append(" AND d.in_dia = 0");
        } 
        if ("5".equals(buscarDocumentoSeguiEstEmiBean.getCoVencimiento())) {
          sql.append(" AND b.fe_ate_doc is not null");
          sql.append(" AND b.fe_rec_doc is not null");
        } 
        if ("0".equals(buscarDocumentoSeguiEstEmiBean.getCoVencimiento())) {
          sql.append(" and a.nu_dia_ate = 0");
          sql.append(" and D.in_dia = 0");
          sql.append(" AND b.fe_ate_doc IS NULL");
          sql.append(" AND b.fe_arc_doc IS NULL");
        } 
      } 






      
      if (buscarDocumentoSeguiEstEmiBean.getEsFiltroFecha() != null && (buscarDocumentoSeguiEstEmiBean
        .getEsFiltroFecha().equals("1") || buscarDocumentoSeguiEstEmiBean.getEsFiltroFecha().equals("3"))) {
        String vFeEmiIni = buscarDocumentoSeguiEstEmiBean.getFeEmiIni();
        String vFeEmiFin = buscarDocumentoSeguiEstEmiBean.getFeEmiFin();
        if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
          .trim().length() > 0) {
          sql.append(" AND CAST(A.FE_EMI AS DATE) between CAST('").append(vFeEmiIni).append("' AS DATE) AND CAST('").append(vFeEmiFin).append("' AS DATE) ");
        }
      } 
      sql.append(" ");
      vResult = "0" + sql.toString().replace("+", "%2B").replace("-", "%2D").replace("<", "%3C").replace(">", "%3E");
    }
    catch (Exception ex) {
      vResult = "1" + ex.getMessage();
      ex.printStackTrace();
    } 
    return vResult;
  }


  
  public List<DocumentoSeguiEstEmitidoBean> getListaReporteBusqueda(BuscarDocumentoSeguiEstEmiBean buscarDocumentoSeguiEstEmiBean) {
    boolean bBusqFiltro = false;
    Map<String, Object> objectParam = new HashMap<String, Object>();
    String sqlContains = "";
    StringBuffer sql = new StringBuffer();
    List<DocumentoSeguiEstEmitidoBean> list = new ArrayList<DocumentoSeguiEstEmitidoBean>();
    
    try {
     // sql.append("SELECT\t(SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_EMI_REF](A.NU_ANN, A.NU_EMI)) DE_EMI_REF,\n\t\tA.NU_COR_EMI,\n\t\tCONVERT(VARCHAR(10), A.FE_EMI, 103) FE_EMI_CORTA,\n\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](A.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,\n\t\tC.NU_DOC,\n\t\tCASE B.TI_DES\n\t\t\tWHEN '01' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](B.CO_DEP_DES) + ' - ' + [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](B.CO_EMP_DES)\n\t\t\tWHEN '02' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](B.NU_RUC_DES)\n\t\t\tWHEN '03' THEN [IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](B.NU_DNI_DES)\n\t\t\tWHEN '04' THEN [IDOSGD].[PK_SGD_DESCRIPCION_OTRO_ORIGEN](B.CO_OTR_ORI_DES)\n\t\tEND DE_EMP_DES,\n        A.DE_ASU,\n\t\tC.NU_EXPEDIENTE,\n\t\t[IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS](B.ES_DOC_REC, 'TDTV_DESTINOS') DE_ES_DOC_DES,\n        CONVERT(VARCHAR(10), B.FE_REC_DOC, 103) FE_REC_DOC_CORTA,\n\t\t(SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM\n\t\tFROM IDOSGD.RHTM_PER_EMPLEADOS\n\t\tWHERE CEMP_CODEMP = A.CO_EMP_RES) DE_EMP_RES,\n\t\t[IDOSGD].[PK_SGD_DESCRIPCION_MOTIVO](B.CO_MOT) DE_MOTIVO,\n\t\t(SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_PRIORIDAD](B.CO_PRI)) DE_PRIORIDAD,\n\t\t(SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM\n\t\tFROM IDOSGD.RHTM_PER_EMPLEADOS\n\t\tWHERE CEMP_CODEMP = B.CO_EMP_REC) DE_EMP_REC,\n        A.NU_DIA_ATE,\n\t\tCONVERT(VARCHAR(10), (DATEADD(DAY, (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_MAS](CONVERT(DATETIME, A.FE_EMI), A.NU_DIA_ATE)), CONVERT(DATE, A.FE_EMI))), 103) F_LIMITE_CORTA,\n\t\tCASE\n\t\t\tWHEN a.nu_dia_ate > 0\n\t\t\t\tAND\t(DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) <= 2\n\t\t\t\tAND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) > 0\n\t\t\t\tAND b.fe_ate_doc IS NULL\n\t\t\t\tAND b.fe_arc_doc IS NULL\n\t\t\t\tAND d.in_dia = 0\n\t\t\tTHEN '1'\n\t\t\tWHEN a.nu_dia_ate > 0\n\t\t\t\tAND\tCONVERT(DATE, GETDATE()) > CONVERT(DATE, E.fe_finaliza)\n\t\t\t\tAND b.fe_ate_doc IS NULL\n\t\t\t\tAND b.fe_arc_doc IS NULL\n\t\t\t\tAND d.in_dia = 0\n\t\t\tTHEN '2'\n\t\t\tWHEN a.nu_dia_ate > 0\n\t\t\t\tAND\tCONVERT(DATE, GETDATE()) = CONVERT(DATE, E.fe_finaliza)\n\t\t\t\tAND b.fe_ate_doc IS NULL\n\t\t\t\tAND b.fe_arc_doc IS NULL\n\t\t\t\tAND d.in_dia = 0\n\t\t\tTHEN '3'\n\t\t\tWHEN a.nu_dia_ate > 0\n\t\t\t\tAND\t(DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) > 2\n\t\t\t\tAND b.fe_ate_doc IS NULL\n\t\t\t\tAND b.fe_arc_doc IS NULL\n\t\t\t\tAND d.in_dia = 0\n\t\t\tTHEN '4'\n\t\t\tWHEN b.fe_ate_doc IS NOT NULL OR b.fe_arc_doc IS NOT NULL\n\t\t\tTHEN '5'\n\t\t\tWHEN a.nu_dia_ate = 0 OR D.in_dia = 0\n\t\t\tTHEN '0'\n\t\tEND co_est_ven,\n\t\tCASE\n\t\t\tWHEN a.nu_dia_ate > 0\n\t\t\t\tAND\tCONVERT(DATE, GETDATE()) > CONVERT(DATE, E.fe_finaliza)\n\t\t\t\tAND b.fe_ate_doc IS NULL\n\t\t\t\tAND b.fe_arc_doc IS NULL\n\t\t\t\tAND d.in_dia = 0\n\t\t\tTHEN (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_TRA](fe_finaliza, CONVERT(DATETIME, GETDATE())))\n\t\t\tELSE 0\n\t\tEND nu_dia_exc,\n        CONVERT(VARCHAR(10), b.fe_arc_doc, 103) fe_arc_doc_CORTA,\n        CONVERT(VARCHAR(10), b.fe_ate_doc, 103) fe_ate_doc_CORTA\nFROM IDOSGD.TDTV_REMITOS A \nINNER JOIN\t IDOSGD.TDTV_DESTINOS B ON A.NU_ANN=B.NU_ANN AND A.NU_EMI=B.NU_EMI  \nINNER JOIN\t IDOSGD.TDTX_REMITOS_RESUMEN C ON C.NU_ANN=B.NU_ANN AND C.NU_EMI = B.NU_EMI  \nINNER JOIN\t IDOSGD.TDTR_MOTIVO D ON b.co_mot = d.co_mot \nINNER JOIN\t (SELECT y.nu_ann,\n\t\t\t y.nu_emi,\n\t\t\t y.fe_finaliza,\n\t\t\t (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_TRA](CONVERT(DATETIME, GETDATE()), y.fe_finaliza)) nu_dia_fal\n\t  FROM (SELECT x.nu_ann,\n\t\t\t\t   x.nu_emi,\n\t\t\t\t   (DATEADD(DAY, (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_MAS](CONVERT(DATETIME, X.FE_EMI), nu_dia_ate)), CONVERT(DATE, x.fe_emi))) AS fe_finaliza\n\t\t\tFROM IDOSGD.tdtv_remitos x) y) E ON a.nu_ann = E.nu_ann AND a.nu_emi = E.nu_emi \n WHERE ");

      
      sql.append(" SELECT (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_EMI_REF](A.NU_ANN, A.NU_EMI)) DE_EMI_REF,A.NU_COR_EMI, ");
       sql.append(" CONVERT(VARCHAR(10), A.FE_EMI, 103) FE_EMI_CORTA, [IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](A.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM, C.NU_DOC, ");
       sql.append(" CASE  TI_DES WHEN '01' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](B.CO_DEP_DES) + ' - ' + [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](B.CO_EMP_DES) ");
      
       sql.append(" WHEN '02' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](B.NU_RUC_DES) WHEN '03' THEN [IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](B.NU_DNI_DES) WHEN '04' ");
       sql.append(" THEN [IDOSGD].[PK_SGD_DESCRIPCION_OTRO_ORIGEN](B.CO_OTR_ORI_DES) END DE_EMP_DES, A.DE_ASU, C.NU_EXPEDIENTE, [IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS](B.ES_DOC_REC, 'TDTV_DESTINOS') DE_ES_DOC_DES,   CONVERT(VARCHAR(10), "); 
       sql.append("	   B.FE_REC_DOC, 103) FE_REC_DOC_CORTA, (SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM FROM IDOSGD.RHTM_PER_EMPLEADOS WHERE CEMP_CODEMP = A.CO_EMP_RES) DE_EMP_RES, [IDOSGD].[PK_SGD_DESCRIPCION_MOTIVO](B.CO_MOT) DE_MOTIVO, ");
       sql.append(" (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_PRIORIDAD](B.CO_PRI)) DE_PRIORIDAD, (SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM ");
       sql.append("	FROM IDOSGD.RHTM_PER_EMPLEADOS WHERE CEMP_CODEMP = B.CO_EMP_REC) DE_EMP_REC,  A.NU_DIA_ATE, CONVERT(VARCHAR(10), "); 
       sql.append("	(DATEADD(DAY, (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_MAS](CONVERT(DATETIME, A.FE_EMI), A.NU_DIA_ATE)), CONVERT(DATE, A.FE_EMI))), 103) F_LIMITE_CORTA, "); 
       sql.append(" CASE WHEN a.nu_dia_ate > 0 AND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) <= 2 AND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) > 0  AND b.fe_ate_doc IS NULL AND b.fe_arc_doc IS NULL ");
       sql.append(" AND d.in_dia = 0 THEN '1'  WHEN a.nu_dia_ate > 0 AND CONVERT(DATE, GETDATE()) > CONVERT(DATE, E.fe_finaliza) AND b.fe_ate_doc IS NULL AND b.fe_arc_doc IS NULL AND d.in_dia = 0 THEN '2' "); 
       sql.append(" WHEN a.nu_dia_ate > 0 AND CONVERT(DATE, GETDATE()) = CONVERT(DATE, E.fe_finaliza) AND b.fe_ate_doc IS NULL AND b.fe_arc_doc IS NULL AND d.in_dia = 0 THEN '3' WHEN a.nu_dia_ate > 0 ");
       sql.append(" AND  (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) > 2  AND b.fe_ate_doc IS NULL AND b.fe_arc_doc IS NULL  AND d.in_dia = 0 THEN '4' WHEN b.fe_ate_doc IS NOT NULL OR b.fe_arc_doc IS NOT NULL THEN '5' ");
       sql.append(" WHEN a.nu_dia_ate = 0 OR D.in_dia = 0 THEN '0' END co_est_ven, CASE  WHEN a.nu_dia_ate > 0 AND\tCONVERT(DATE, GETDATE()) > CONVERT(DATE, E.fe_finaliza) AND b.fe_ate_doc IS NULL  AND b.fe_arc_doc IS NULL AND d.in_dia = 0  THEN (SELECT RETORNO "); 
       sql.append("	FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_TRA](fe_finaliza, CONVERT(DATETIME, GETDATE()))) ELSE 0 END nu_dia_exc,        CONVERT(VARCHAR(10), b.fe_arc_doc, 103) fe_arc_doc_CORTA, "); 
       sql.append(" CONVERT(VARCHAR(10), b.fe_ate_doc, 103) fe_ate_doc_CORTA "); 
       sql.append(" FROM IDOSGD.TDTV_REMITOS A With (readuncommitted) INNER JOIN IDOSGD.TDTV_DESTINOS B With (readuncommitted) ON A.NU_ANN=B.NU_ANN AND A.NU_EMI=B.NU_EMI   INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN C ON C.NU_ANN=B.NU_ANN AND C.NU_EMI = B.NU_EMI "); 
       sql.append("  INNER JOIN  IDOSGD.TDTR_MOTIVO D  With (readuncommitted) ON b.co_mot = d.co_mot ");  
       sql.append(" INNER JOIN (SELECT y.nu_ann, y.nu_emi, y.fe_finaliza, (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_TRA](CONVERT(DATETIME, GETDATE()), y.fe_finaliza)) nu_dia_fal "); 
       sql.append("  FROM (SELECT x.nu_ann,  x.nu_emi,   (DATEADD(DAY,     (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_FU_DIA_MAS](CONVERT(DATETIME, X.FE_EMI), nu_dia_ate)),");
       sql.append("  CONVERT(DATE, x.fe_emi))) AS fe_finaliza FROM IDOSGD.tdtv_remitos x With (readuncommitted) ) y) E ON a.nu_ann = E.nu_ann AND a.nu_emi = E.nu_emi  WHERE 1=1 ");
       
      String pNUAnn = buscarDocumentoSeguiEstEmiBean.getCoAnnio();
      if (!buscarDocumentoSeguiEstEmiBean.getEsFiltroFecha().equals("1") || !pNUAnn.equals("0")) {
        
        sql.append(" and A.NU_ANN = :pNuAnn ");
        
        objectParam.put("pNuAnn", pNUAnn);
      } 
      sql.append(" AND A.ES_DOC_EMI NOT IN ('5', '9', '7') ");
      sql.append(" AND A.ES_ELI = '0' ");
      sql.append(" AND B.ES_ELI = '0' ");
      sql.append(" AND A.TI_EMI='01' ");

      
      String pTipoBusqueda = buscarDocumentoSeguiEstEmiBean.getTipoBusqueda();
      if (pTipoBusqueda.equals("1") && buscarDocumentoSeguiEstEmiBean.isEsIncluyeFiltro()) {
        bBusqFiltro = true;
      }

      
      String auxTipoAcceso = buscarDocumentoSeguiEstEmiBean.getTiAcceso();
      String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1") || auxTipoAcceso.equals("2"))) ? auxTipoAcceso : "1";
      if (buscarDocumentoSeguiEstEmiBean.getCoEmpElaboro() != null && buscarDocumentoSeguiEstEmiBean.getCoEmpElaboro().trim().length() > 0 && tiAcceso.equals("0")) {
        
        sql.append(" AND A.CO_EMP_RES = :pcoEmpRes ");
        objectParam.put("pcoEmpRes", buscarDocumentoSeguiEstEmiBean.getCoEmpElaboro());
      }
      else if (tiAcceso.equals("1")) {
        
        sql.append(" AND A.CO_EMP_RES = :pcoEmpRes ");
        objectParam.put("pcoEmpRes", buscarDocumentoSeguiEstEmiBean.getCoEmpleado());
      } else if (tiAcceso.equals("2")) {
        
        sql.append(" AND (A.CO_EMP_RES = :pcoEmpRes OR A.CO_EMP_EMI = :pcoEmpRes) ");
        objectParam.put("pcoEmpRes", buscarDocumentoSeguiEstEmiBean.getCoEmpleado());
      } 



      
      if (pTipoBusqueda.equals("0") || bBusqFiltro) {
        
        if (buscarDocumentoSeguiEstEmiBean.getEstadoDoc() != null && buscarDocumentoSeguiEstEmiBean.getEstadoDoc().trim().length() > 0) {
          
          sql.append(" AND B.ES_DOC_REC = :pEsDocRec ");
          objectParam.put("pEsDocRec", buscarDocumentoSeguiEstEmiBean.getEstadoDoc());
        } 
        if (buscarDocumentoSeguiEstEmiBean.getCoTipoDocAdm() != null && buscarDocumentoSeguiEstEmiBean.getCoTipoDocAdm().trim().length() > 0) {
          
          sql.append(" AND A.CO_TIP_DOC_ADM = :pCoDocEmi ");
          objectParam.put("pCoDocEmi", buscarDocumentoSeguiEstEmiBean.getCoTipoDocAdm());
        } 
        if (buscarDocumentoSeguiEstEmiBean.getCoRefOrigen() != null && buscarDocumentoSeguiEstEmiBean.getCoRefOrigen().trim().length() > 0) {
          
          sql.append(" AND CHARINDEX(:pTiEmiRef, C.TI_EMI_REF) > 0 ");
          objectParam.put("pTiEmiRef", buscarDocumentoSeguiEstEmiBean.getCoRefOrigen());
        } 
        if (buscarDocumentoSeguiEstEmiBean.getCoDepOrigen() != null && buscarDocumentoSeguiEstEmiBean.getCoDepOrigen().trim().length() > 0) {
          
          sql.append(" AND A.CO_DEP_EMI = :pCoDepOrigen ");
          objectParam.put("pCoDepOrigen", buscarDocumentoSeguiEstEmiBean.getCoDepOrigen());
        } 
        if (buscarDocumentoSeguiEstEmiBean.getCoDepDestino() != null && buscarDocumentoSeguiEstEmiBean.getCoDepDestino().trim().length() > 0) {
          
          sql.append(" AND CHARINDEX(:pTiEmpPro, C.TI_EMI_DES) > 0 ");
          objectParam.put("pTiEmpPro", buscarDocumentoSeguiEstEmiBean.getCoDepDestino());
        } 
        
        if (buscarDocumentoSeguiEstEmiBean.getCoVencimiento() != null && buscarDocumentoSeguiEstEmiBean.getCoVencimiento().trim().length() > 0) {
          
          if ("1".equals(buscarDocumentoSeguiEstEmiBean.getCoVencimiento())) {
            sql.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) <= 2");
            sql.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) > 0");
            sql.append(" AND b.fe_ate_doc IS NULL");
            sql.append(" AND b.fe_arc_doc IS NULL");
            sql.append(" AND d.in_dia = 0");
          } 
          if ("2".equals(buscarDocumentoSeguiEstEmiBean.getCoVencimiento())) {
            sql.append(" AND CONVERT(DATE, GETDATE()) > CONVERT(DATE, E.fe_finaliza)");
            sql.append(" AND b.fe_ate_doc IS NULL");
            sql.append(" AND b.fe_arc_doc IS NULL");
            sql.append(" AND d.in_dia = 0");
            sql.append(" AND A.NU_DIA_ATE <> 0");
          } 
          if ("3".equals(buscarDocumentoSeguiEstEmiBean.getCoVencimiento())) {
            
            sql.append(" AND CONVERT(DATE, GETDATE()) = CONVERT(DATE, E.fe_finaliza)");
            sql.append(" AND b.fe_ate_doc IS NULL");
            sql.append(" AND b.fe_arc_doc IS NULL");
            sql.append(" AND d.in_dia = 0");
            sql.append(" AND A.NU_DIA_ATE <> 0");
          } 
          if ("4".equals(buscarDocumentoSeguiEstEmiBean.getCoVencimiento())) {
            sql.append(" AND (DATEDIFF(DAY, CONVERT(DATE, E.fe_finaliza), CONVERT(DATE, GETDATE()))) > 2");
            sql.append(" AND b.fe_ate_doc IS NULL");
            sql.append(" AND b.fe_arc_doc IS NULL");
            sql.append(" AND d.in_dia = 0");
          } 
          if ("5".equals(buscarDocumentoSeguiEstEmiBean.getCoVencimiento())) {
            sql.append(" AND b.fe_ate_doc is not null");
            sql.append(" AND b.fe_rec_doc is not null");
          } 
          if ("0".equals(buscarDocumentoSeguiEstEmiBean.getCoVencimiento())) {
            sql.append(" and a.nu_dia_ate = 0");
            sql.append(" and D.in_dia = 0");
            sql.append(" AND b.fe_ate_doc IS NULL");
            sql.append(" AND b.fe_arc_doc IS NULL");
          } 
        } 






        
        if (buscarDocumentoSeguiEstEmiBean.getEsFiltroFecha() != null && (buscarDocumentoSeguiEstEmiBean
          .getEsFiltroFecha().equals("1") || buscarDocumentoSeguiEstEmiBean.getEsFiltroFecha().equals("3"))) {
          String vFeEmiIni = buscarDocumentoSeguiEstEmiBean.getFeEmiIni();
          String vFeEmiFin = buscarDocumentoSeguiEstEmiBean.getFeEmiFin();
          if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
            .trim().length() > 0) {
            
            sql.append(" AND CAST(A.FE_EMI AS DATE) BETWEEN CAST(:pFeEmiIni AS DATE) AND CAST(:pFeEmiFin AS DATE) ");
            objectParam.put("pFeEmiIni", vFeEmiIni);
            objectParam.put("pFeEmiFin", vFeEmiFin);
          } 
        } 
      } 

      
      if (pTipoBusqueda.equals("1")) {
        if (!bBusqFiltro && 
          buscarDocumentoSeguiEstEmiBean.getCoDependencia() != null && buscarDocumentoSeguiEstEmiBean.getCoDependencia().trim().length() > 0) {
          sql.append(" AND B.CO_DEP_DES = :pcoDepDes ");
          objectParam.put("pcoDepDes", buscarDocumentoSeguiEstEmiBean.getCoDependencia());
        } 
        
        if (buscarDocumentoSeguiEstEmiBean.getBusNumDoc() != null && buscarDocumentoSeguiEstEmiBean.getBusNumDoc().trim().length() > 1) {
          sql.append(" AND A.NU_DOC_EMI LIKE '%'+:pnuDocEmi+'%' ");
          objectParam.put("pnuDocEmi", buscarDocumentoSeguiEstEmiBean.getBusNumDoc());
        } 
        
        if (buscarDocumentoSeguiEstEmiBean.getBusNumExpediente() != null && buscarDocumentoSeguiEstEmiBean.getBusNumExpediente().trim().length() > 1) {
          sql.append(" AND C.NU_EXPEDIENTE LIKE '%'+:pnuExpediente+'%' ");
          objectParam.put("pnuExpediente", buscarDocumentoSeguiEstEmiBean.getBusNumExpediente());
        } 

        
        if (buscarDocumentoSeguiEstEmiBean.getBusAsunto() != null && buscarDocumentoSeguiEstEmiBean.getBusAsunto().trim().length() > 1) {



          
          sql.append(" AND UPPER(A.DE_ASU) LIKE '%'+:pDeAsunto+'%' ");
          objectParam.put("pDeAsunto", buscarDocumentoSeguiEstEmiBean.getBusAsunto());
        } 
      } 

      
      sql.append("ORDER BY A.FE_EMI DESC ");




      
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoSeguiEstEmitidoBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception ex) {
      
      list = null;
      ex.printStackTrace();
    } 
    
    return list;
  }














}
