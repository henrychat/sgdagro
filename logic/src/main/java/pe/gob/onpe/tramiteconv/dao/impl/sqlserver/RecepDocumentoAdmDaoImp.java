package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoRecepBean;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoBean;
import pe.gob.onpe.tramitedoc.bean.ExpedienteBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaBean;
import pe.gob.onpe.tramitedoc.dao.RecepDocumentoAdmDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;
import pe.gob.onpe.tramitedoc.util.FiltroPaginate;
import pe.gob.onpe.tramitedoc.util.Paginacion;
import pe.gob.onpe.tramitedoc.util.ProcessResult;










@Repository("recepDocumentoAdmDao")
public class RecepDocumentoAdmDaoImp
  extends SimpleJdbcDaoBase
  implements RecepDocumentoAdmDao
{
  private SimpleJdbcCall spPuActualizaGuiaMp;
  private SimpleJdbcCall spActualizaEstado;
  
  public List<DocumentoBean> getDocumentosBuscaRecepAdm(BuscarDocumentoRecepBean buscarDocumentoRecep) {
    StringBuffer sql = new StringBuffer();
    String sqlContains = "";
    boolean bBusqFiltro = false;
    String sOrdenList = "";
    Map<String, Object> objectParam = new HashMap<String, Object>();
    
    List<DocumentoBean> list = new ArrayList<DocumentoBean>();
    sql.append("SET LANGUAGE Español ");
    sql.append("SELECT TOP 500 X.*,");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_TI_EMI_EMP(X.NU_ANN, X.NU_EMI) DE_ORI_EMI,");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(X.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM, ");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_ESTADOS(X.ES_DOC_REC,'TDTV_DESTINOS') DE_ES_DOC_DES,");
    sql.append(" CASE X.TI_DES");
    sql.append("  WHEN '01' THEN  IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(X.CO_EMP_DES)");
    sql.append("  WHEN '02' THEN  IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(X.NU_RUC_DES)");
    sql.append("  WHEN '03' THEN  IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL(X.NU_DNI_DES)");
    sql.append("  WHEN '04' THEN  IDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN(X.CO_OTR_ORI_DES)");
    sql.append(" END DE_EMP_DES");
    sql.append(" FROM ( ");
    sql.append("SELECT A.NU_ANN,A.NU_EMI,A.TI_CAP,B.NU_COR_DES,IDOSGD.PK_SGD_DESCRIPCION_FORMAT(A.FE_EMI,'DD/MM/YYYY') FE_EMI_CORTA,A.FE_EMI,");
    sql.append(" A.CO_TIP_DOC_ADM , C.NU_DOC,");
    sql.append(" B.TI_DES,B.CO_EMP_DES,B.NU_RUC_DES,B.NU_DNI_DES, B.CO_OTR_ORI_DES,");
    sql.append(" UPPER (A.DE_ASU) DE_ASU_M,C.NU_EXPEDIENTE,");
    sql.append(" C.IN_EXISTE_DOC EXISTE_DOC,");
    sql.append(" CASE ISNULL(C.TI_EMI_REF, '0') + ISNULL(C.IN_EXISTE_ANEXO, '2') ");
    sql.append("\tWHEN '00' THEN 0 ");
    sql.append("\tWHEN '02' THEN 0 ");
    sql.append("    ELSE 1 ");
    sql.append(" END EXISTE_ANEXO, ");
    sql.append(" NU_DES,ISNULL(B.CO_PRI,'1') CO_PRI, B.ES_DOC_REC,");
    sql.append(" B.CO_ETIQUETA_REC");
    sql.append(" , CASE WHEN B.ES_DOC_REC='1' THEN  CASE WHEN ISNULL(totalproceso_ref,0)>1 THEN 'En Proceso de Atenci<br/> Hay '+CAST(totalproceso_ref AS VARCHAR)+' documentos' WHEN ISNULL(totalproceso_ref,0)=1 THEN 'En Proceso de Atenci<br/> '+tipodocumento_ref+'/'+nrodocumento_ref+'<br/> '+estado_ref ELSE '' END  ELSE '' END enprocesoatencion  ");
    sql.append(" FROM IDOSGD.TDTV_REMITOS A INNER JOIN IDOSGD.TDTV_DESTINOS B ON B.NU_ANN = A.NU_ANN  AND B.NU_EMI = A.NU_EMI ");
    sql.append(" INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN C ON   C.NU_ANN = B.NU_ANN AND C.NU_EMI = B.NU_EMI ");
    
    sql.append(" LEFT JOIN (  SELECT  a.nu_ann_ref,a.nu_emi_ref,a.nu_des_ref,MAX(IDOSGD.PK_SGD_DESCRIPCION_de_documento(b.co_tip_doc_adm)) tipodocumento_ref , \n                MAX((CASE b.ti_emi WHEN '01' THEN b.nu_doc_emi + '-' + b.nu_ann + '-' + b.de_doc_sig WHEN '05' THEN b.nu_doc_emi + '-' + b.nu_ann + '-' + b.de_doc_sig ELSE ISNULL(b.de_doc_sig,'S/N') END) ) nrodocumento_ref,                 \n                MAX(IDOSGD.PK_SGD_DESCRIPCION_ESTADOS(B.ES_DOC_EMI,'TDTV_REMITOS'))  estado_ref, COUNT(1) totalproceso_ref\n                FROM IDOSGD.tdtr_referencia a \n                INNER JOIN IDOSGD.tdtv_remitos b ON a.nu_ann = b.nu_ann and a.nu_emi = b.nu_emi\n                INNER JOIN IDOSGD.tdtv_destinos c ON b.nu_ann = c.nu_ann  and b.nu_emi = c.nu_emi\n                WHERE  b.es_eli = '0'  and b.es_doc_emi <> '9'  \n                GROUP BY a.nu_ann_ref,a.nu_emi_ref,a.nu_des_ref ) TB  ON   TB.nu_ann_ref=c.nu_ann AND TB.nu_emi_ref=c.nu_emi AND TB.nu_des_ref =B.nu_des   ");

    sql.append(" WHERE 1=1 ");


    sql.append(" AND B.CO_DEP_DES = :pCoDepDes");
    sql.append(" AND A.ES_ELI = '0'");
    sql.append(" AND A.CO_GRU IN ('1','2','3')");
    sql.append(" AND B.ES_ELI = '0'");
    sql.append(" AND A.ES_DOC_EMI NOT IN ('5', '9', '7')");
    sql.append(" AND A.IN_OFICIO = '0'");

    
    objectParam.put("pCoDepDes", buscarDocumentoRecep.getsCoDependencia());
    
    String pTipoBusqueda = buscarDocumentoRecep.getsTipoBusqueda();
    if (pTipoBusqueda.equals("1") && buscarDocumentoRecep.isEsIncluyeFiltro()) {
      bBusqFiltro = true;
    }
    String auxTipoAcceso = buscarDocumentoRecep.getsTiAcceso();
    String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1") || auxTipoAcceso.equals("2"))) ? auxTipoAcceso : "1";
    if (buscarDocumentoRecep.getsDestinatario() != null && buscarDocumentoRecep.getsDestinatario().trim().length() > 0 && (pTipoBusqueda.equals("0") || bBusqFiltro) && tiAcceso.equals("0")) {
      sql.append(" AND B.CO_EMP_DES = :pcoEmpDes ");
      objectParam.put("pcoEmpDes", buscarDocumentoRecep.getsDestinatario());
    }
    else if (tiAcceso.equals("1")) {
      sql.append(" AND B.CO_EMP_DES = :pcoEmpDes ");
      objectParam.put("pcoEmpDes", buscarDocumentoRecep.getsCoEmpleado());
    } else if (tiAcceso.equals("2")) {
      sql.append(" AND B.CO_EMP_DES = :pcoEmpDes  ");
      objectParam.put("pcoEmpDes", buscarDocumentoRecep.getsCoEmpleado());
    } 


    
    if (pTipoBusqueda.equals("0") || bBusqFiltro) {
      if (buscarDocumentoRecep.getsTipoDoc() != null && buscarDocumentoRecep.getsTipoDoc().trim().length() > 0) {
        sql.append(" AND A.CO_TIP_DOC_ADM = :pCoDocEmi ");
        objectParam.put("pCoDocEmi", buscarDocumentoRecep.getsTipoDoc());
      } 
      if (buscarDocumentoRecep.getsEstadoDoc() != null && buscarDocumentoRecep.getsEstadoDoc().trim().length() > 0) {
        String estadoDoc = buscarDocumentoRecep.getsEstadoDoc();
        sql.append(" AND B.ES_DOC_REC = :pEsDocRec ");
        objectParam.put("pEsDocRec", estadoDoc);
        if (!estadoDoc.equals("0")) {
          sOrdenList = " DESC";
        }
      } else {
        sOrdenList = " DESC";
      } 
      if (buscarDocumentoRecep.getsPrioridadDoc() != null && buscarDocumentoRecep.getsPrioridadDoc().trim().length() > 0) {
        sql.append(" AND B.CO_PRI = :pCoPrioridad ");
        objectParam.put("pCoPrioridad", buscarDocumentoRecep.getsPrioridadDoc());
      } 
      if (buscarDocumentoRecep.getsRemitente() != null && buscarDocumentoRecep.getsRemitente().trim().length() > 0) {
        sql.append(" AND ISNULL(C.CO_DEP_EMI_REF,'0') = :pTiEmiRef ");
        objectParam.put("pTiEmiRef", buscarDocumentoRecep.getsRemitente());
      } 
      if (buscarDocumentoRecep.getIdEtiqueta() != null && buscarDocumentoRecep.getIdEtiqueta().trim().length() > 0) {
        sql.append(" AND B.CO_ETIQUETA_REC = :pcoEtiquetaRec ");
        objectParam.put("pcoEtiquetaRec", buscarDocumentoRecep.getIdEtiqueta());
      } 
      if (buscarDocumentoRecep.getEsFiltroFecha() != null) {
        
        String vFeEmiIni = buscarDocumentoRecep.getsFeEmiIni();
        String vFeEmiFin = buscarDocumentoRecep.getsFeEmiFin();
        if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
          .trim().length() > 0) {
          sql.append(" AND FE_EMI BETWEEN CONVERT(DATETIME, :pFeEmiIni, 103) AND CONVERT(DATETIME, :pFeEmiFin, 103) + 0.99999 ");
          objectParam.put("pFeEmiIni", vFeEmiIni);
          objectParam.put("pFeEmiFin", vFeEmiFin);
        } 
      } 
    } 
    
    if (pTipoBusqueda.equals("1")) {
      if (buscarDocumentoRecep.getsNroDocumento() != null && buscarDocumentoRecep.getsNroDocumento().trim().length() > 1) {
        sql.append(" AND A.NU_DOC_EMI LIKE ''+:pnuDocEmi+'%' ");
        objectParam.put("pnuDocEmi", buscarDocumentoRecep.getsNroDocumento());
      } 
      
      if (buscarDocumentoRecep.getsBuscNroExpediente() != null && buscarDocumentoRecep.getsBuscNroExpediente().trim().length() > 1) {
        sql.append(" AND C.NU_EXPEDIENTE LIKE '%'+:pnuExpediente+'%' ");
        objectParam.put("pnuExpediente", buscarDocumentoRecep.getsBuscNroExpediente());
      } 

      
      if (buscarDocumentoRecep.getsBuscAsunto() != null && buscarDocumentoRecep.getsBuscAsunto().trim().length() > 1) {


        
        sql.append(" AND CONTAINS(A.DE_ASU, :pBusquedaTextual) ");
        sqlContains = "SELECT RETORNO FROM [IDOSGD].PK_SGD_DESCRIPCION_QUERYFILTER('" + buscarDocumentoRecep.getsBuscAsunto() + "')";
      } 
    } 

    
    sql.append(") X ");
    sql.append(" ORDER BY X.FE_EMI").append(sOrdenList);
    
    try {
      if (sqlContains.length() > 0) {
        String cadena = (String)this.jdbcTemplate.queryForObject(sqlContains, String.class);
        objectParam.put("pBusquedaTextual", cadena);
      } 
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoBean.class));
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public ProcessResult<List<DocumentoBean>> getDocumentosBuscaRecepAdmList(BuscarDocumentoRecepBean buscarDocumentoRecep, FiltroPaginate paginate) {
    StringBuffer sql = new StringBuffer();
    boolean bBusqFiltro = false;
    String sOrdenList = "";
    Map<String, Object> objectParam = new HashMap<String, Object>();
    ProcessResult<List<DocumentoBean>> Result = new ProcessResult();
    List<DocumentoBean> list = new ArrayList<DocumentoBean>();
    sql.append("SET LANGUAGE Español ");
    sql.append("SELECT X.*,");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_TI_EMI_EMP(X.NU_ANN, X.NU_EMI) DE_ORI_EMI,");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(X.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM, ");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_ESTADOS(X.ES_DOC_REC,'TDTV_DESTINOS') DE_ES_DOC_DES,");
    sql.append(" CASE X.TI_DES WHEN '01' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(X.CO_EMP_DES)  ");
    sql.append(" WHEN '02' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(X.NU_RUC_DES) ");
    sql.append(" WHEN '03' THEN IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL(X.NU_DNI_DES) ");
    sql.append(" WHEN '04' THEN IDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN(X.CO_OTR_ORI_DES) END  ");
    sql.append(" DE_EMP_DES , GETDATE() feExp");
    sql.append(" ");
    sql.append(" FROM ( ");
    sql.append("SELECT A.NU_ANN,A.NU_EMI,A.TI_CAP,A.TI_EMI,ISNULL(CAST(B.NU_COR_DES AS VARCHAR(20)),'') as NU_COR_DES,CONVERT(varchar(20), A.FE_EMI, 103)  FE_EMI_CORTA,CONVERT(varchar(20), A.FE_EMI, 131)  FE_EMI,");
    sql.append(" A.CO_TIP_DOC_ADM , C.NU_DOC,");
    sql.append(" B.TI_DES,B.CO_EMP_DES,B.NU_RUC_DES,B.NU_DNI_DES, B.CO_OTR_ORI_DES,");
    sql.append(" '<p style=''text-align:justify;''>'+UPPER(A.DE_ASU)+'</p>' DE_ASU_M,C.NU_EXPEDIENTE,");
    sql.append(" C.IN_EXISTE_DOC EXISTE_DOC, CASE ISNULL(C.TI_EMI_REF,'0')+ISNULL(C.IN_EXISTE_ANEXO,'2') WHEN '00' THEN '0'  WHEN '02' THEN '0'  ELSE  '1' END   EXISTE_ANEXO,CAST(NU_DES AS VARCHAR) NU_DES,ISNULL(B.CO_PRI,'1') CO_PRI, B.ES_DOC_REC,");
    sql.append(" B.CO_ETIQUETA_REC");
    sql.append(" ,(SELECT count(*) FROM IDOSGD.TDTR_AVANCE AVA WHERE AVA.NU_EMI=A.NU_EMI AND AVA.NU_ANN=A.NU_ANN) as iCanAvance");
    sql.append(" , CASE WHEN B.ES_DOC_REC='1' THEN  CASE WHEN ISNULL(totalproceso_ref,0)>1 THEN 'En Proceso de Atenci<br/> Hay '+CAST(totalproceso_ref AS VARCHAR)+' documentos' WHEN ISNULL(totalproceso_ref,0)=1 THEN 'En Proceso de Atenci<br/> '+tipodocumento_ref+'/'+nrodocumento_ref+'<br/> '+estado_ref ELSE '' END  ELSE '' END enprocesoatencion, COUNT(A.NU_ANN) OVER()  AS filasTotal ");
    
    sql.append(" ,  ROW_NUMBER() OVER (   ");
    if (buscarDocumentoRecep.getOrden().equals("nuCorDes-ASC")) { sql.append(" ORDER BY NU_COR_DES ASC "); }
    else if (buscarDocumentoRecep.getOrden().equals("nuCorDes-DESC")) { sql.append(" ORDER BY NU_COR_DES DESC "); }
    else if (buscarDocumentoRecep.getOrden().equals("feEmiCorta-ASC")) { sql.append(" ORDER BY FE_EMI ASC "); }
    else if (buscarDocumentoRecep.getOrden().equals("feEmiCorta-DESC")) { sql.append(" ORDER BY FE_EMI DESC "); }
    else if (buscarDocumentoRecep.getOrden().equals("deOriEmi-ASC")) { sql.append(" ORDER BY A.TI_EMI,B.NU_RUC_DES,NU_DNI_DES,CO_OTR_ORI_DES ASC "); }
    else if (buscarDocumentoRecep.getOrden().equals("deOriEmi-DESC")) { sql.append(" ORDER BY A.TI_EMI,B.NU_RUC_DES,NU_DNI_DES,CO_OTR_ORI_DES DESC "); }
    else if (buscarDocumentoRecep.getOrden().equals("deTipDocAdm-ASC")) { sql.append(" ORDER BY CO_TIP_DOC_ADM ASC "); }
    else if (buscarDocumentoRecep.getOrden().equals("deTipDocAdm-DESC")) { sql.append(" ORDER BY CO_TIP_DOC_ADM DESC "); }
    else if (buscarDocumentoRecep.getOrden().equals("nuDoc-ASC")) { sql.append(" ORDER BY A.NU_DOC ASC "); }
    else if (buscarDocumentoRecep.getOrden().equals("nuDoc-DESC")) { sql.append(" ORDER BY A.NU_DOC DESC "); }
    else if (buscarDocumentoRecep.getOrden().equals("deEmpDes-ASC")) { sql.append(" ORDER BY CO_EMP_DES,B.NU_RUC_DES,NU_DNI_DES,CO_OTR_ORI_DES ASC "); }
    else if (buscarDocumentoRecep.getOrden().equals("deEmpDes-DESC")) { sql.append(" ORDER BY CO_EMP_DES,B.NU_RUC_DES,NU_DNI_DES,CO_OTR_ORI_DES DESC "); }
    else if (buscarDocumentoRecep.getOrden().equals("deAsuM-ASC")) { sql.append(" ORDER BY A.DE_ASU ASC "); }
    else if (buscarDocumentoRecep.getOrden().equals("deAsuM-DESC")) { sql.append(" ORDER BY A.DE_ASU DESC "); }
    else if (buscarDocumentoRecep.getOrden().equals("nuExpediente-ASC")) { sql.append(" ORDER BY NU_EXPEDIENTE ASC "); }
    else if (buscarDocumentoRecep.getOrden().equals("nuExpediente-DESC")) { sql.append(" ORDER BY NU_EXPEDIENTE DESC "); }
    else if (buscarDocumentoRecep.getOrden().equals("deEsDocDes-ASC")) { sql.append(" ORDER BY ES_DOC_REC ASC "); }
    else if (buscarDocumentoRecep.getOrden().equals("deEsDocDes-DESC")) { sql.append(" ORDER BY ES_DOC_REC DESC "); }
    else { sql.append(" ORDER BY FE_EMI"); }
    
    sql.append("  ) AS fila  ");
    sql.append(" FROM IDOSGD.TDTV_REMITOS A INNER JOIN IDOSGD.TDTV_DESTINOS B ON B.NU_ANN = A.NU_ANN  AND B.NU_EMI = A.NU_EMI ");
    sql.append(" INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN C ON   C.NU_ANN = B.NU_ANN AND C.NU_EMI = B.NU_EMI ");
    
    sql.append(" LEFT JOIN (  SELECT  a.nu_ann_ref,a.nu_emi_ref,a.nu_des_ref,MAX(IDOSGD.PK_SGD_DESCRIPCION_de_documento(b.co_tip_doc_adm)) tipodocumento_ref , \n                MAX((CASE b.ti_emi WHEN '01' THEN b.nu_doc_emi + '-' + b.nu_ann + '-' + b.de_doc_sig WHEN '05' THEN b.nu_doc_emi + '-' + b.nu_ann + '-' + b.de_doc_sig ELSE ISNULL(b.de_doc_sig,'S/N') END) ) nrodocumento_ref,                 \n                MAX(IDOSGD.PK_SGD_DESCRIPCION_ESTADOS(B.ES_DOC_EMI,'TDTV_REMITOS'))  estado_ref, COUNT(1) totalproceso_ref\n                FROM IDOSGD.tdtr_referencia a \n                INNER JOIN IDOSGD.tdtv_remitos b ON a.nu_ann = b.nu_ann and a.nu_emi = b.nu_emi\n                INNER JOIN IDOSGD.tdtv_destinos c ON b.nu_ann = c.nu_ann  and b.nu_emi = c.nu_emi\n                WHERE  b.es_eli = '0'  and b.es_doc_emi <> '9'  \n                GROUP BY a.nu_ann_ref,a.nu_emi_ref,a.nu_des_ref ) TB  ON   TB.nu_ann_ref=c.nu_ann AND TB.nu_emi_ref=c.nu_emi AND TB.nu_des_ref =B.nu_des   ");







    
    sql.append(" WHERE 1=1 ");
    
  //String pNUAnn = buscarDocumentoRecep.getsCoAnnio();
  // hacer que el filtro siempre sea por fecha de emision  
//    if (!buscarDocumentoRecep.getEsFiltroFecha().equals("1") || !pNUAnn.equals("0")) {
//      sql.append(" AND A.NU_ANN = :pNuAnn");
//      
//      objectParam.put("pNuAnn", pNUAnn);
//    } 

    
    sql.append(" AND B.CO_DEP_DES='" + buscarDocumentoRecep.getsCoDependencia() + "'");
    sql.append(" AND A.ES_ELI = '0'");
    sql.append(" AND A.CO_GRU IN ('1','2','3')");
    sql.append(" AND B.ES_ELI = '0'");
    sql.append(" AND A.ES_DOC_EMI NOT IN ('5', '9', '7')");
    sql.append(" AND A.IN_OFICIO = '0'");

    
    objectParam.put("pCoDepDes", buscarDocumentoRecep.getsCoDependencia());
    
    String pTipoBusqueda = buscarDocumentoRecep.getsTipoBusqueda();
    
    bBusqFiltro = true;
    
    String auxTipoAcceso = buscarDocumentoRecep.getsTiAcceso();
    String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1") || auxTipoAcceso.equals("2"))) ? auxTipoAcceso : "1";
    if (buscarDocumentoRecep.getsDestinatario() != null && buscarDocumentoRecep.getsDestinatario().trim().length() > 0 && (pTipoBusqueda.equals("0") || bBusqFiltro) && tiAcceso.equals("0")) {
      sql.append(" AND B.CO_EMP_DES = :pcoEmpDes ");
      objectParam.put("pcoEmpDes", buscarDocumentoRecep.getsDestinatario());
    }
    else if (tiAcceso.equals("1")) {
      sql.append(" AND B.CO_EMP_DES = :pcoEmpDes ");
      objectParam.put("pcoEmpDes", buscarDocumentoRecep.getsCoEmpleado());
    } else if (tiAcceso.equals("2")) {
      sql.append(" AND B.CO_EMP_DES = :pcoEmpDes  ");
      objectParam.put("pcoEmpDes", buscarDocumentoRecep.getsCoEmpleado());
    } 


    
    String bExcepSeach = "0";
    
    if ((buscarDocumentoRecep.getsNroDocumento() != null && buscarDocumentoRecep.getsNroDocumento().trim().length() > 1) || (buscarDocumentoRecep
      .getsBuscNroExpediente() != null && buscarDocumentoRecep.getsBuscNroExpediente().trim().length() > 1) || (buscarDocumentoRecep
      .getsBuscAsunto() != null && buscarDocumentoRecep.getsBuscAsunto().trim().length() > 1) || (buscarDocumentoRecep
      .getsNumDocRef() != null && buscarDocumentoRecep.getsNumDocRef().trim().length() > 1))
    {
      bExcepSeach = "1";
    }
  
    //EJHM
    // se libera los filtros

    if (buscarDocumentoRecep.getsTipoDoc() != null && buscarDocumentoRecep.getsTipoDoc().trim().length() > 0) {
        sql.append(" AND A.CO_TIP_DOC_ADM = :pCoDocEmi ");
        objectParam.put("pCoDocEmi", buscarDocumentoRecep.getsTipoDoc());
      } 
      
      if (buscarDocumentoRecep.getsEstadoDoc() != null && buscarDocumentoRecep.getsEstadoDoc().trim().length() > 0) {
        String estadoDoc = buscarDocumentoRecep.getsEstadoDoc();
        sql.append(" AND B.ES_DOC_REC = :pEsDocRec ");
        objectParam.put("pEsDocRec", estadoDoc);
        if (!estadoDoc.equals("0")) {
          sOrdenList = " DESC";
        }
      } else {
        sOrdenList = " DESC";
      } 
      
      if (buscarDocumentoRecep.getEsFiltroFecha() != null) {
	        //aca esta el error
	        String vFeEmiIni = buscarDocumentoRecep.getsFeEmiIni();
	        String vFeEmiFin = buscarDocumentoRecep.getsFeEmiFin();
	        if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
	          .trim().length() > 0) {
	        // EJHM
	        //  sql.append(" AND CAST(A.FE_EMI AS DATETIME) between  :pFeEmiIni   AND  :pFeEmiFin   ");
	        sql.append(" AND A.FE_EMI BETWEEN CONVERT(DATETIME, :pFeEmiIni, 103) AND CONVERT(DATETIME,  :pFeEmiFin , 103) + 0.99999   ");
	          objectParam.put("pFeEmiIni", vFeEmiIni);
	          objectParam.put("pFeEmiFin", vFeEmiFin);
	        } 
	      } 

   //fin se libera los filtros 
    if (bExcepSeach == "0") {

	      
	      if ((buscarDocumentoRecep.isEsIncluyeOficina() && !buscarDocumentoRecep.isEsIncluyeProfesional()) || (!buscarDocumentoRecep.isEsIncluyeOficina() && buscarDocumentoRecep.isEsIncluyeProfesional())) {
	        if (buscarDocumentoRecep.isEsIncluyeOficina()) {
	          sql.append(" AND B.CO_EMP_DES  in ( SELECT CO_EMPLEADO FROM IDOSGD.RHTM_DEPENDENCIA WHERE CO_DEPENDENCIA= :pcoEmpDesOficina  )");
	          objectParam.put("pcoEmpDesOficina", buscarDocumentoRecep.getsCoDependencia());
	        } 
	        if (buscarDocumentoRecep.isEsIncluyeProfesional()) {
	          sql.append(" AND NOT B.CO_EMP_DES  in ( SELECT CO_EMPLEADO FROM IDOSGD.RHTM_DEPENDENCIA WHERE CO_DEPENDENCIA= :pcoEmpDesOficina  )");
	          objectParam.put("pcoEmpDesOficina", buscarDocumentoRecep.getsCoDependencia());
	        } 
	      } 
	      
	      if (buscarDocumentoRecep.getCoTema() != null && buscarDocumentoRecep.getCoTema().trim().length() > 0) {
	        sql.append(" AND B.CO_TEMA = :pCoTema ");
	        objectParam.put("pCoTema", buscarDocumentoRecep.getCoTema());
	      } 
//	      if (buscarDocumentoRecep.getsTipoDoc() != null && buscarDocumentoRecep.getsTipoDoc().trim().length() > 0) {
//	        sql.append(" AND A.CO_TIP_DOC_ADM = :pCoDocEmi ");
//	        objectParam.put("pCoDocEmi", buscarDocumentoRecep.getsTipoDoc());
//	      } 
//	      
//	      if (buscarDocumentoRecep.getsEstadoDoc() != null && buscarDocumentoRecep.getsEstadoDoc().trim().length() > 0) {
//	        String estadoDoc = buscarDocumentoRecep.getsEstadoDoc();
//	        sql.append(" AND B.ES_DOC_REC = :pEsDocRec ");
//	        objectParam.put("pEsDocRec", estadoDoc);
//	        if (!estadoDoc.equals("0")) {
//	          sOrdenList = " DESC";
//	        }
//	      } else {
//	        sOrdenList = " DESC";
//	      } 
	      if (buscarDocumentoRecep.getsPrioridadDoc() != null && buscarDocumentoRecep.getsPrioridadDoc().trim().length() > 0) {
	        sql.append(" AND B.CO_PRI = :pCoPrioridad ");
	        objectParam.put("pCoPrioridad", buscarDocumentoRecep.getsPrioridadDoc());
	      } 
	      if (buscarDocumentoRecep.getsRemitente() != null && buscarDocumentoRecep.getsRemitente().trim().length() > 0) {
	        sql.append(" AND ISNULL(C.CO_DEP_EMI_REF,'0') = :pTiEmiRef ");
	        objectParam.put("pTiEmiRef", buscarDocumentoRecep.getsRemitente());
	      } 
	      if (buscarDocumentoRecep.getIdEtiqueta() != null && buscarDocumentoRecep.getIdEtiqueta().trim().length() > 0) {
	        sql.append(" AND B.CO_ETIQUETA_REC = :pcoEtiquetaRec ");
	        objectParam.put("pcoEtiquetaRec", buscarDocumentoRecep.getIdEtiqueta());
	      } 
//	      if (buscarDocumentoRecep.getEsFiltroFecha() != null) {
//	        //aca esta el error
//	        String vFeEmiIni = buscarDocumentoRecep.getsFeEmiIni();
//	        String vFeEmiFin = buscarDocumentoRecep.getsFeEmiFin();
//	        if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
//	          .trim().length() > 0) {
//	        // EJHM
//	        //  sql.append(" AND CAST(A.FE_EMI AS DATETIME) between  :pFeEmiIni   AND  :pFeEmiFin   ");
//	        sql.append(" AND A.FE_EMI BETWEEN CONVERT(DATETIME, :pFeEmiIni, 103) AND CONVERT(DATETIME,  :pFeEmiFin , 103) + 0.99999   ");
//	          objectParam.put("pFeEmiIni", vFeEmiIni);
//	          objectParam.put("pFeEmiFin", vFeEmiFin);
//	        } 
//	      } 
    } 

    
    if (buscarDocumentoRecep.getsNroDocumento() != null && buscarDocumentoRecep.getsNroDocumento().trim().length() > 1) {
     // sql.append(" AND A.NU_DOC_EMI LIKE ''+:pnuDocEmi+'%'   ");
    	 sql.append(" AND C.NU_DOC LIKE '%'+:pnuDocEmi+'%'   ");
      objectParam.put("pnuDocEmi", buscarDocumentoRecep.getsNroDocumento());
    } 
    
    if (buscarDocumentoRecep.getsBuscNroExpediente() != null && buscarDocumentoRecep.getsBuscNroExpediente().trim().length() > 1) {
      sql.append(" AND C.NU_EXPEDIENTE LIKE '%'+:pnuExpediente+'%' ");
      objectParam.put("pnuExpediente", buscarDocumentoRecep.getsBuscNroExpediente());
    } 

    
    if (buscarDocumentoRecep.getsBuscAsunto() != null && buscarDocumentoRecep.getsBuscAsunto().trim().length() > 1) {
      
      sql.append(" AND UPPER(A.DE_ASU) LIKE '%'+:pDeAsunto+'%' ");
      objectParam.put("pDeAsunto", buscarDocumentoRecep.getsBuscAsunto());
    } 
    
    if (buscarDocumentoRecep.getsNumDocRef() != null && buscarDocumentoRecep.getsNumDocRef().trim().length() > 1) {

      
      sql.append("  AND A.NU_EMI IN( ");
      sql.append("   SELECT D.NU_EMI ");
      sql.append("   FROM IDOSGD.TDTR_ARBOL_SEG D ");
      sql.append("   INNER JOIN IDOSGD.TDTV_REMITOS R ON R.NU_ANN=D.NU_ANN_REF AND D.NU_EMI_REF=R.NU_EMI ");
      sql.append("   WHERE R.CO_TIP_DOC_ADM='" + buscarDocumentoRecep.getsDeTipoDocAdm() + "' AND R.CO_DEP_EMI='" + buscarDocumentoRecep.getsBuscDestinatario() + "' ");
      sql.append("   AND R.NU_ANN=" + buscarDocumentoRecep.getsCoAnnioBus() + " AND ((R.NU_DOC LIKE '%'+'" + buscarDocumentoRecep.getsNumDocRef() + "'+'%') OR (R.DE_DOC_SIG LIKE '%'+'" + buscarDocumentoRecep.getsNumDocRef() + "'+'%'))");
      sql.append(" )");
    } 





    
    sql.append(") X ");
    sql.append("WHERE fila BETWEEN " + paginate.getPaginaDesde() + " AND " + paginate.getPaginaHasta() + "  ");


    
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoBean.class));
      Result.setResult(list);
      Result.setIsSuccess(true);
      objectParam = new HashMap<String, Object>();
      buscarDocumentoRecep = new BuscarDocumentoRecepBean();
      list = new ArrayList<DocumentoBean>();
      sql = new StringBuffer();
    }
    catch (EmptyResultDataAccessException e) {
      Result.setIsSuccess(false);
      Result.setMessage(e.getMessage());
    } catch (Exception e) {
      Result.setIsSuccess(false);
      Result.setMessage(e.getMessage());
      e.printStackTrace();
    } 
    return Result;
  }

  
  public List<DependenciaBean> getDocumDepenProveido(String codDepen) {
    StringBuffer sql = new StringBuffer();
    
    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
    
    sql.append(" SELECT CO_DEPENDENCIA coDependencia,DE_DEPENDENCIA deDependencia,DE_CORTA_DEPEN deCortaDepen  FROM IDOSGD.RHTM_DEPENDENCIA WHERE CO_DEPENDENCIA IN \n( SELECT CELE_DESELE FROM IDOSGD.SI_ELEMENTO WHERE CTAB_CODTAB='DE_DOCUM_PROVEIDO' AND CELE_DESCOR=? ) ");


    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { codDepen });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  public DocumentoBean getDocumentoRecepAdm(String pnuAnn, String pnuEmi, String pnuDes) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT A.NU_ANN,A.NU_EMI,A.TI_EMI,IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(A.CO_EMP_RES) DE_EMP_RES,A.FE_EMI,\nIDOSGD.PK_SGD_DESCRIPCION_TI_EMI_EMP(A.NU_ANN, A.NU_EMI) DE_ORI_EMI,A.CO_TIP_DOC_ADM,IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(A.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,\nCASE A.TI_EMI WHEN '01' THEN A.NU_DOC_EMI + '-' + A.NU_ANN + '-' + A.DE_DOC_SIG WHEN '05' THEN A.NU_DOC_EMI + '-' + A.NU_ANN + '-' + A.DE_DOC_SIG ELSE A.DE_DOC_SIG END NU_DOC,\nA.NU_DIA_ATE,A.DE_ASU,B.DE_ANE,B.NU_DES,B.NU_COR_DES,B.CO_DEP_DES,IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(B.CO_DEP_DES) DE_DEP_DES,(SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_PRIORIDAD(B.CO_PRI)) DE_PRI,\nCASE B.TI_DES WHEN '01' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (B.CO_EMP_DES) WHEN '02' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (B.NU_RUC_DES) WHEN '03' THEN IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL (B.NU_DNI_DES) WHEN '04' THEN IDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN (B.CO_OTR_ORI_DES) END DE_EMP_DES,\nIDOSGD.PK_SGD_DESCRIPCION_MOTIVO (B.CO_MOT) DE_MOT,B.DE_PRO,B.CO_EMP_REC,IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (B.CO_EMP_REC) DE_EMP_REC, B.ES_DOC_REC, IDOSGD.PK_SGD_DESCRIPCION_ESTADOS (B.ES_DOC_REC,'TDTV_DESTINOS') DE_ES_DOC_DES, B.FE_REC_DOC,\nB.FE_ARC_DOC, B.FE_ATE_DOC,A.NU_ANN_EXP,A.NU_SEC_EXP,IDOSGD.PK_SGD_DESCRIPCION_FORMAT(A.FE_EMI,'DD/MM/YYYY HH24:MI') FE_EMI_CORTA,IDOSGD.PK_SGD_DESCRIPCION_FORMAT(A.FE_EMI,'DD/MM/YY') FE_EMI_CORTA2,\nIDOSGD.PK_SGD_DESCRIPCION_FORMAT(B.FE_REC_DOC,'DD/MM/YYYY HH24:MI') FE_REC_DOC_CORTA, IDOSGD.PK_SGD_DESCRIPCION_FORMAT(B.FE_ARC_DOC,'DD/MM/YYYY HH24:MI') FE_ARC_DOC_CORTA, \nIDOSGD.PK_SGD_DESCRIPCION_FORMAT(B.FE_ATE_DOC,'DD/MM/YYYY HH24:MI') FE_ATE_DOC_CORTA,'1' EXISTE_DOC,(SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_FU_DOC_ANE(A.NU_ANN,A.NU_EMI)) EXISTE_ANEXO,\nB.Ti_Fisico_Rec,B.Co_Etiqueta_Rec\nFROM IDOSGD.TDTV_REMITOS A INNER JOIN IDOSGD.TDTV_DESTINOS B ON A.NU_ANN = B.NU_ANN AND A.NU_EMI = B.NU_EMI \nWHERE A.ES_ELI='0' AND B.ES_ELI='0'\nAND A.ES_DOC_EMI NOT IN ('5', '9', '7')\nAND A.IN_OFICIO = '0'\nAND B.NU_ANN=? AND B.NU_EMI=? AND B.NU_DES=?");

    
    DocumentoBean documentoBean = new DocumentoBean();
    try {
      documentoBean = (DocumentoBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoBean.class), new Object[] { pnuAnn, pnuEmi, pnuDes });
    }
    catch (EmptyResultDataAccessException e) {
      documentoBean = null;
    } catch (Exception e) {
      System.out.println("nu_ann=" + pnuAnn + ",nu_emi=" + pnuEmi + "nu_des=" + pnuDes);
      e.printStackTrace();
    } 
    return documentoBean;
  }

  
  public ExpedienteBean getExpDocumentoRecepAdm(String pnuAnnExp, String pnuSecExp) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT NU_ANN_EXP,NU_SEC_EXP,FE_EXP,IDOSGD.PK_SGD_DESCRIPCION_FORMAT(FE_EXP,'DD/MM/YYYY') FE_EXP_CORTA,FE_VENCE,CO_PROCESO,IDOSGD.PK_SGD_DESCRIPCION_DE_PROCESO_EXP(CO_PROCESO) DE_PROCESO,DE_DETALLE,CO_DEP_EXP,CO_GRU,NU_CORR_EXP,NU_EXPEDIENTE,NU_FOLIOS,NU_PLAZO,US_CREA_AUDI,FE_CREA_AUDI,US_MODI_AUDI,FE_MODI_AUDI,ES_ESTADO\nFROM IDOSGD.TDTC_EXPEDIENTE\nwhere\nNU_ANN_EXP=? and NU_SEC_EXP=?");



    
    ExpedienteBean expedienteBean = new ExpedienteBean();
    try {
      expedienteBean = (ExpedienteBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ExpedienteBean.class), new Object[] { pnuAnnExp, pnuSecExp });
    }
    catch (EmptyResultDataAccessException e) {
      expedienteBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return expedienteBean;
  }

  
  public List<ReferenciaBean> getDocumentosRefRecepAdm(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO (a.co_tip_doc_adm) li_tip_doc, CASE a.ti_emi\n                  WHEN '01' THEN a.nu_doc_emi + '-' + a.nu_ann + '/' + a.de_doc_sig\n                  WHEN '05' THEN a.nu_doc_emi + '-' + a.nu_ann + '/' + a.de_doc_sig\n                  ELSE a.de_doc_sig\n                  END li_nu_doc,a.fe_emi,IDOSGD.PK_SGD_DESCRIPCION_FORMAT(a.fe_emi,'DD/MM/YYYY') fe_emi_corta,b.co_ref,b.nu_ann,b.nu_emi,b.nu_des,b.nu_ann_ref,\n                 b.nu_emi_ref,b.nu_des_ref\nFROM IDOSGD.tdtv_remitos a INNER JOIN IDOSGD.TDTR_REFERENCIA b ON a.nu_ann = b.nu_ann_ref AND a.nu_emi = b.nu_emi_ref \nWHERE \n b.NU_EMI=? \nAND b.NU_ANN=?");
 
    List<ReferenciaBean> list = new ArrayList<ReferenciaBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ReferenciaBean.class), new Object[] { pnuEmi, pnuAnn });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String actualizarGuiaMesaPartes(DocumentoBean documentoBean) {
    String mensaje = "NO_OK";
    this.spPuActualizaGuiaMp = (new SimpleJdbcCall(this.dataSource)).withProcedureName("IDOSGD.PK_SGD_TRAMITE_PU_ACTUALIZA_GUIA_MP").withoutProcedureColumnMetaDataAccess().useInParameterNames(new String[] { "pnu_ann", "pnu_emi", "pnu_ann", "pnu_emi" }).declareParameters(new SqlParameter[] { new SqlParameter("pnu_ann", 12), new SqlParameter("pnu_emi", 12), new SqlParameter("pnu_des", 12), new SqlParameter("pes_doc_rec", 12) });

    MapSqlParameterSource mapSqlParameterSource = (new MapSqlParameterSource()).addValue("pnu_ann", documentoBean.getNuAnn()).addValue("pnu_emi", documentoBean.getNuEmi()).addValue("pnu_des", documentoBean.getNuDes()).addValue("pes_doc_rec", documentoBean.getEsDocRec());
    
    try {
      this.spPuActualizaGuiaMp.execute((SqlParameterSource)mapSqlParameterSource);
      mensaje = "OK";
    } catch (Exception ex) {
      
      ex.printStackTrace();
    } 
    return mensaje;
  }

  
  public String getNumCorrelativoDestino(String nuAnn, String coDepDes) {
    String result = "1";
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT ISNULL(MAX(nu_cor_des), 0) + 1\nFROM IDOSGD.tdtv_destinos\nWHERE nu_ann = ?\nAND co_dep_des = ?");



    
    try {
      result = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { nuAnn, coDepDes });
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return result;
  }

  
  public String actualizarEstado(DocumentoBean documentoBean) {
    String mensaje = "NO_OK";
    this.spActualizaEstado = (new SimpleJdbcCall(this.dataSource)).withProcedureName("IDOSGD.PK_SGD_TRAMITE_ACTUALIZA_ESTADO").withoutProcedureColumnMetaDataAccess().useInParameterNames(new String[] { "pnu_ann", "pnu_emi", "pnu_des", "pest" }).declareParameters(new SqlParameter[] 
    		{ new SqlParameter("pnu_ann", 12), new SqlParameter("pnu_emi", 12), new SqlParameter("pnu_des", 2), new SqlParameter("pest", 12) });


    
    MapSqlParameterSource mapSqlParameterSource = (new MapSqlParameterSource()).addValue("pnu_ann", documentoBean.getNuAnn()).addValue("pnu_emi", documentoBean.getNuEmi()).addValue("pnu_des", Integer.valueOf(Integer.parseInt(documentoBean.getNuDes()))).addValue("pest", documentoBean.getEsDocRec());
    
    try {
      this.spActualizaEstado.execute((SqlParameterSource)mapSqlParameterSource);
      mensaje = "OK";
    } catch (Exception ex) {
      
      ex.printStackTrace();
    } 
    return mensaje;
  }

  
  public String updDocumentoBean(DocumentoBean documentoBean, String paccion) {
    StringBuffer sql = new StringBuffer();
    sql.append("SET LANGUAGE Español ");
    sql.append("UPDATE IDOSGD.TDTV_DESTINOS ");
    sql.append("SET ES_DOC_REC = ?, CO_USE_MOD = ?, FE_USE_MOD = CURRENT_TIMESTAMP");
    
    if (paccion.equals("1") || paccion.equals("0")) {
      sql.append(",FE_REC_DOC = CONVERT(DATETIME,'");
      sql.append(documentoBean.getFeRecDoc());
      sql.append("'),");
      sql.append("CO_EMP_REC ='");
      sql.append(documentoBean.getCoEmpRec()).append("'");
      
      if (documentoBean.getFeAteDoc() != null && documentoBean.getFeAteDoc().trim().length() > 0) {
        sql.append(",FE_ATE_DOC=CONVERT(DATETIME,'");
        sql.append(documentoBean.getFeAteDoc());
        sql.append("')");
      } 
      if (documentoBean.getFeArcDoc() != null && documentoBean.getFeArcDoc().trim().length() > 0) {
        sql.append(",FE_ARC_DOC=CONVERT(DATETIME,'");
        sql.append(documentoBean.getFeArcDoc());
        sql.append("')");

      
      }

    
    }
    else if (paccion.equals("2")) {
      sql.append(",CO_EMP_REC=NULL,FE_REC_DOC=NULL,FE_ATE_DOC=NULL,FE_ARC_DOC=NULL");
    } 
    if (documentoBean.getNuCorDes() != null && documentoBean.getNuCorDes().trim().length() > 0) {
      sql.append(",NU_COR_DES=").append(documentoBean.getNuCorDes());
    } else {
      sql.append(",NU_COR_DES=NULL");
    } 
    sql.append(",DE_ANE=?");
    sql.append(" WHERE NU_ANN = ? ");
    sql.append("AND NU_EMI = ? ");
    sql.append("AND NU_DES = ? ");
    String vReturn = "NO_OK";
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { documentoBean
            .getEsDocRec(), documentoBean.getCodUseMod(), documentoBean
            .getDeAne(), documentoBean
            .getNuAnn(), documentoBean.getNuEmi(), documentoBean.getNuDes() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public List<DocumentoBean> getDocumentosRecepAdm(BuscarDocumentoRecepBean buscarDocumentoRecep, Paginacion paginacion) {
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT * ");
    sql.append("FROM ( SELECT A.*, ROWNUM row_number ");
    sql.append("FROM ( ");
    
    sql.append("SELECT NU_ANN,NU_EMI,TI_CAP,NU_COR_DES,TO_CHAR(FE_EMI,'DD/MM/YY') FE_EMI_CORTA,FE_EMI,DE_ORI_EMI,DE_TIP_DOC_ADM,NU_DOC,DE_EMP_DES,DE_ASU_M,NU_EXPEDIENTE,DE_ES_DOC_DES,");
    sql.append("'1' EXISTE_DOC,TD_PK_TRAMITE.FU_DOC_ANE(NU_ANN,NU_EMI) EXISTE_ANEXO,NU_DES ");
    sql.append("FROM TDVV_DESTINOS_ADM ");
    sql.append("WHERE ");
    sql.append(" NU_ANN = ISNULL(?/*:B_01_ANN.DE_ANIO*/, NU_ANN)");
    sql.append(" AND CO_DEP_DES = ?/*:GLOBAL.VCOD_DEP*/ ");
    sql.append(" AND CO_TIP_DOC_ADM = ISNULL(?/*:B_01_ANN.DE_DOC_DEP*/,CO_TIP_DOC_ADM)");
    sql.append(" AND ES_DOC_REC = DECODE(?/*:B_01_ANN.DE_ESTADOS*/, NULL, ES_DOC_REC, ?/*:B_01_ANN.DE_ESTADOS*/)");
    sql.append(" AND CO_PRI     = DECODE(?/*:B_01_ANN.DE_PRI*/, NULL, CO_PRI, ?/*:B_01_ANN.DE_PRI*/) ");
    sql.append(" AND (  CO_EMP_DES = DECODE(?/*:GLOBAL.TI_ACCESO*/, 0, CO_EMP_DES, ?/*:GLOBAL.USER*/)");
    sql.append("\tOR (   CO_EMP_DES IS NULL ");
    sql.append("\t   AND TI_DES ='01'");
    sql.append("\t   )");
    sql.append("\t)");
    sql.append(" AND ISNULL(TI_EMI_REF,'0') = ISNULL(?/*:B_01_ANN.TI_EMI_REF*/, ISNULL(TI_EMI_REF,'0'))");
    sql.append(" AND ISNULL(CO_EMP_DES,'NULO') = ISNULL(?/*:B_01_ANN.TI_EMP_DES*/, ISNULL(CO_EMP_DES,'NULO'))");
    sql.append(" AND (  (   ISNULL(?/*:B_01_ANN.LI_EXPE*/, 'NULO') = 'SINEX'");
    sql.append("\t   AND CO_EXP IS NULL");
    sql.append("\t   )");
    sql.append("\tOR (   ISNULL(?/*:B_01_ANN.LI_EXPE*/, 'NULO') != 'NULO'");
    sql.append("\t   AND CO_EXP = ?/*:B_01_ANN.LI_EXPE*/");
    sql.append("\t   )");
    sql.append("\tOR ?/*:B_01_ANN.LI_EXPE*/ IS NULL");
    sql.append("\t)");
    sql.append(" ORDER BY FE_EMI DESC ");
    
    sql.append(") A ");
    sql.append("WHERE ROWNUM < ((? * ?) + 1 ) ");
    sql.append(") ");
    sql.append("WHERE row_number >= (((?-1) * ?) + 1) ");
    
    List<DocumentoBean> list = new ArrayList<DocumentoBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoBean.class), new Object[] { buscarDocumentoRecep
            .getsCoAnnio(), buscarDocumentoRecep.getsCoDependencia(), buscarDocumentoRecep.getsTipoDoc(), buscarDocumentoRecep
            .getsEstadoDoc(), buscarDocumentoRecep.getsEstadoDoc(), buscarDocumentoRecep.getsPrioridadDoc(), buscarDocumentoRecep.getsPrioridadDoc(), buscarDocumentoRecep
            .getsTiAcceso(), buscarDocumentoRecep.getsCoEmpleado(), buscarDocumentoRecep.getsRemitente(), buscarDocumentoRecep.getsDestinatario(), buscarDocumentoRecep
            .getsExpediente(), buscarDocumentoRecep.getsExpediente(), buscarDocumentoRecep.getsExpediente(), buscarDocumentoRecep.getsExpediente(), 
            Integer.valueOf(paginacion.getNumeroDePagina()), Integer.valueOf(paginacion.getRegistrosPorPagina()), Integer.valueOf(paginacion.getNumeroDePagina()), Integer.valueOf(paginacion.getRegistrosPorPagina()) });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public int getRowDocumentosRecepAdm(BuscarDocumentoRecepBean buscarDocumentoRecep) {
    int result = -1;
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT COUNT(1) ");
    sql.append("FROM TDVV_DESTINOS_ADM ");
    sql.append("WHERE ");
    sql.append(" NU_ANN = ISNULL(?/*:B_01_ANN.DE_ANIO*/, NU_ANN)");
    sql.append(" AND CO_DEP_DES = ?/*:GLOBAL.VCOD_DEP*/ ");
    sql.append(" AND CO_TIP_DOC_ADM = ISNULL(?/*:B_01_ANN.DE_DOC_DEP*/,CO_TIP_DOC_ADM)");
    sql.append(" AND ES_DOC_REC = DECODE(?/*:B_01_ANN.DE_ESTADOS*/, NULL, ES_DOC_REC, ?/*:B_01_ANN.DE_ESTADOS*/)");
    sql.append(" AND CO_PRI     = DECODE(?/*:B_01_ANN.DE_PRI*/, NULL, CO_PRI, ?/*:B_01_ANN.DE_PRI*/) ");
    sql.append(" AND (  CO_EMP_DES = DECODE(?/*:GLOBAL.TI_ACCESO*/, 0, CO_EMP_DES, ?/*:GLOBAL.USER*/)");
    sql.append("\tOR (   CO_EMP_DES IS NULL ");
    sql.append("\t   AND TI_DES ='01'");
    sql.append("\t   )");
    sql.append("\t)");
    sql.append(" AND ISNULL(TI_EMI_REF,'0') = ISNULL(?/*:B_01_ANN.TI_EMI_REF*/, ISNULL(TI_EMI_REF,'0'))");
    sql.append(" AND ISNULL(CO_EMP_DES,'NULO') = ISNULL(?/*:B_01_ANN.TI_EMP_DES*/, ISNULL(CO_EMP_DES,'NULO'))");
    sql.append(" AND (  (   ISNULL(?/*:B_01_ANN.LI_EXPE*/, 'NULO') = 'SINEX'");
    sql.append("\t   AND CO_EXP IS NULL");
    sql.append("\t   )");
    sql.append("\tOR (   ISNULL(?/*:B_01_ANN.LI_EXPE*/, 'NULO') != 'NULO'");
    sql.append("\t   AND CO_EXP = ?/*:B_01_ANN.LI_EXPE*/");
    sql.append("\t   )");
    sql.append("\tOR ?/*:B_01_ANN.LI_EXPE*/ IS NULL");
    sql.append("\t)");
    
    try {
      result = this.jdbcTemplate.queryForInt(sql.toString(), new Object[] { buscarDocumentoRecep.getsCoAnnio(), buscarDocumentoRecep.getsCoDependencia(), buscarDocumentoRecep.getsTipoDoc(), buscarDocumentoRecep
            .getsEstadoDoc(), buscarDocumentoRecep.getsEstadoDoc(), buscarDocumentoRecep.getsPrioridadDoc(), buscarDocumentoRecep.getsPrioridadDoc(), buscarDocumentoRecep
            .getsTiAcceso(), buscarDocumentoRecep.getsCoEmpleado(), buscarDocumentoRecep.getsRemitente(), buscarDocumentoRecep.getsDestinatario(), buscarDocumentoRecep
            .getsExpediente(), buscarDocumentoRecep.getsExpediente(), buscarDocumentoRecep.getsExpediente(), buscarDocumentoRecep.getsExpediente() });
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return result;
  }


  
  public String getDesEstadoDocRecepcion(String sesDocRec) {
    String result = null;
    StringBuffer sql = new StringBuffer();
    sql.append("select IDOSGD.PK_SGD_DESCRIPCION_ESTADOS(?,'TDTV_DESTINOS')");
    
    try {
      result = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { sesDocRec });
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return result;
  }
  
  public String validarAnulacionDocRecepcion(String pnuAnn, String pnuEmi, String pnuDes) {
    String vReturn = "NO_OK";
    StringBuffer sqlQry1 = new StringBuffer();
    sqlQry1.append("SELECT COUNT(re.nu_emi)\nFROM IDOSGD.tdtr_referencia rf,\nIDOSGD.tdtv_remitos   re\nWHERE re.nu_ann = rf.nu_ann\nAND re.nu_emi = rf.nu_emi\nAND rf.nu_ann_ref = ?\nAND rf.nu_emi_ref = ?\nAND rf.nu_des_ref = ?\nAND re.ES_DOC_EMI <> '9'");


    
    StringBuffer sqlQry2 = new StringBuffer();
    sqlQry2.append("SELECT COUNT(nu_emi)\nFROM IDOSGD.tdtv_destinos\nWHERE nu_ann = ?\nAND nu_emi = ?\nAND nu_des <> ?\nAND es_doc_rec <> '0'\nAND es_eli = '0'");


    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("UPDATE IDOSGD.tdtv_remitos\nSET es_doc_emi = '0' -- EMITIDO\nWHERE nu_ann = ?\nAND nu_emi = ?");


    
    try {
      String vResult = (String)this.jdbcTemplate.queryForObject(sqlQry1.toString(), String.class, new Object[] { pnuAnn, pnuEmi, pnuDes });
      if (vResult != null && vResult.equals("0")) {
        vResult = (String)this.jdbcTemplate.queryForObject(sqlQry2.toString(), String.class, new Object[] { pnuAnn, pnuEmi, pnuDes });
        if (vResult != null) {
          if (vResult.equals("0")) {
            this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { pnuAnn, pnuEmi });
          }
          vReturn = "OK";
        } 
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }

  
  public DocumentoBean existeDocumentoReferenciado(BuscarDocumentoRecepBean buscarDocumentoRecep) {
    StringBuffer sql = new StringBuffer();
    DocumentoBean documentoBean = null;
    sql.append("SELECT A.NU_ANN,A.NU_EMI,A.NU_COR_DOC FROM IDOSGD.TDTV_REMITOS A\nWHERE A.NU_ANN=?\nAND A.CO_DEP_EMI=?\nAND A.TI_EMI='01'\nAND A.CO_TIP_DOC_ADM=?\nAND A.NU_DOC_EMI=?\nAND A.ES_ELI='0'\nAND A.ES_DOC_EMI NOT IN ('5','7','9')");

    
    try {
      documentoBean = (DocumentoBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoBean.class), new Object[] { buscarDocumentoRecep
            .getsCoAnnioBus(), buscarDocumentoRecep.getsBuscDestinatario(), buscarDocumentoRecep.getsDeTipoDocAdm(), buscarDocumentoRecep
            .getsNumDocRef() });
      documentoBean.setMsjResult("OK");
    } catch (EmptyResultDataAccessException e) {
      documentoBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return documentoBean;
  }

  
  public List<DocumentoBean> getDocumentosReferenciadoBusq(DocumentoBean documentoBean, String sTipoAcceso) {
    StringBuffer sql = new StringBuffer();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    List<DocumentoBean> list = new ArrayList<DocumentoBean>();
    
    sql.append("SELECT Z.* ");
    sql.append("FROM ( ");
    sql.append("\tSELECT  DISTINCT A.NU_ANN, ");
    sql.append("\t\tA.NU_EMI, ");
    sql.append("\t\tA.TI_CAP, ");
    sql.append("\t\tB.NU_COR_DES, ");
    sql.append("\t\tCONVERT(VARCHAR(10), A.FE_EMI, 3) FE_EMI_CORTA, ");
    sql.append("\t\tA.FE_EMI, ");
    sql.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_TI_EMI_EMP](A.NU_ANN, A.NU_EMI) DE_ORI_EMI, ");
    sql.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](A.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM, ");
    sql.append("\t\tC.NU_DOC, ");
    sql.append("\t\tCASE B.TI_DES ");
    sql.append("                WHEN '01' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](B.CO_EMP_DES) ");
    sql.append("                WHEN '02' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](B.NU_RUC_DES) ");
    sql.append("                WHEN '03' THEN [IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](B.NU_DNI_DES) ");
    sql.append("                WHEN '04' THEN [IDOSGD].[PK_SGD_DESCRIPCION_OTRO_ORIGEN](B.CO_OTR_ORI_DES) ");
    sql.append("\t\tEND DE_EMP_DES, ");
    sql.append("\t\tUPPER (A.DE_ASU) DE_ASU_M, ");
    sql.append("\t\tC.NU_EXPEDIENTE, ");
    sql.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS](B.ES_DOC_REC, 'TDTV_DESTINOS') DE_ES_DOC_DES, ");
    sql.append("\t\tC.IN_EXISTE_DOC EXISTE_DOC, ");
    sql.append("\t\tCASE ISNULL(C.TI_EMI_REF, '0') + ISNULL(C.IN_EXISTE_ANEXO, '2') ");
    sql.append("                WHEN '00' THEN 0 ");
    sql.append("                WHEN '02' THEN 0 ");
    sql.append("                ELSE 1 ");
    sql.append("\t\tEND EXISTE_ANEXO, ");
    sql.append("\t\tB.NU_DES, ");
    sql.append("\t\tISNULL(B.CO_PRI, '1') CO_PRI, ");
    sql.append("\t\tB.ES_DOC_REC, ");
    sql.append("\t\tROW_NUMBER() OVER (ORDER BY A.FE_EMI DESC) AS ROWNUM ");
    sql.append("\t FROM\t[IDOSGD].[PK_SGD_DESCRIPCION_ARBOL_SEG](:pCoAnio, :pNuEmi) D  ");
    sql.append("\t INNER JOIN\tIDOSGD.TDTV_REMITOS A  ON D.NU_ANN = A.NU_ANN AND D.NU_EMI = A.NU_EMI ");
    sql.append("\t INNER JOIN\tIDOSGD.TDTV_DESTINOS B ON B.NU_ANN = A.NU_ANN AND B.NU_EMI = A.NU_EMI  ");
    sql.append("\t INNER JOIN\tIDOSGD.TDTX_REMITOS_RESUMEN C ON C.NU_ANN = B.NU_ANN AND C.NU_EMI = B.NU_EMI ");
    sql.append("\t WHERE  B.CO_DEP_DES = :pCoDepDes  ");
    
    if (sTipoAcceso.equals("1")) {
      sql.append(" AND B.CO_EMP_DES = :pcoEmpDes ");
      objectParam.put("pcoEmpDes", documentoBean.getCoEmpDes());
    } 
    sql.append("\tAND A.ES_ELI = '0' ");
    sql.append("\tAND B.ES_ELI = '0' ");
    sql.append("\tAND A.ES_DOC_EMI NOT IN ('5', '9', '7') ");
    sql.append("\tAND A.IN_OFICIO = '0' ");
    
    objectParam.put("pCoAnio", documentoBean.getNuAnn());
    objectParam.put("pNuEmi", documentoBean.getNuEmi());
    objectParam.put("pCoDepDes", documentoBean.getCoDepDes());
    
    sql.append(") Z ");
    sql.append("WHERE Z.ROWNUM < 101 ");
    sql.append("ORDER BY Z.FE_EMI DESC ");
    
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String getVerificaAtendido(String pnuAnn, String pnuEmi, String pnuDes) {
    String vResult = "0";
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT count(a.nu_ann) nu_cantidad \nFROM IDOSGD.tdtr_referencia a INNER JOIN IDOSGD.tdtv_remitos b ON a.nu_ann = b.nu_ann and a.nu_emi = b.nu_emi \nWHERE  \n  b.es_eli = '0' \nand b.es_doc_emi <> '9' \nand a.nu_ann_ref = ?\nand a.nu_emi_ref = ? \nand a.nu_des_ref = ?");


    try {
      vResult = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { pnuAnn, pnuEmi, pnuDes });
    } catch (Exception e) {
      vResult = "0";
      e.printStackTrace();
    } 
    return vResult;
  }

  
  public List<ReferenciaBean> getConsultaAtendido(String pnuAnn, String pnuEmi, String pnuDes) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT  [IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](b.co_tip_doc_adm) li_tip_doc, ");
    sql.append("        CASE b.ti_emi ");
    sql.append("\t\tWHEN '01' THEN b.nu_doc_emi + '-' + b.nu_ann + '-' + b.de_doc_sig ");
    sql.append("\t\tWHEN '05' THEN b.nu_doc_emi + '-' + b.nu_ann + '-' + b.de_doc_sig ");
    sql.append("\t\tELSE b.de_doc_sig ");
    sql.append("        END li_nu_doc, ");
    sql.append("        CONVERT(VARCHAR(10), b.fe_emi, 103) fe_emi_corta, ");
    sql.append("        b.nu_ann, ");
    sql.append("        b.nu_emi, ");
    sql.append("        CAST(b.nu_cor_emi AS VARCHAR) nu_cor_emi, ");
    sql.append("        [IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS](b.es_doc_emi,'TDTV_REMITOS') DE_ES_DOC_EMI, ");
    sql.append("        a.nu_ann_ref, ");
    sql.append("        a.nu_emi_ref ");
    sql.append("FROM IDOSGD.tdtr_referencia a ");
    sql.append("INNER JOIN IDOSGD.tdtv_remitos b ON  a.nu_ann = b.nu_ann and a.nu_emi = b.nu_emi ");
    sql.append("WHERE ");
    sql.append("  b.es_eli = '0' ");
    sql.append("and b.es_doc_emi <> '9' ");
    sql.append("and a.nu_ann_ref = ? ");
    sql.append("and a.nu_emi_ref = ? ");
    sql.append("and a.nu_des_ref = ? ");
    sql.append("order by b.nu_cor_emi desc ");
    
    List<ReferenciaBean> list = new ArrayList<ReferenciaBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ReferenciaBean.class), new Object[] { pnuAnn, pnuEmi, pnuDes });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }


  
  public String updEtiquetaTipoRecepDocumento(DocumentoBean documentoBean) {
    StringBuffer sql = new StringBuffer();
    sql.append("UPDATE IDOSGD.TDTV_DESTINOS ");
    sql.append("SET ti_fisico_rec =?,co_etiqueta_rec =?");
    sql.append(" WHERE NU_ANN = ? ");
    sql.append("AND NU_EMI = ? ");
    sql.append("AND NU_DES = ? ");
    String vReturn = "NO_OK";
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { documentoBean
            .getTiFisicoRec(), documentoBean.getCoEtiquetaRec(), documentoBean
            .getNuAnn(), documentoBean.getNuEmi(), documentoBean.getNuDes() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public DocumentoBean getEstadoDocumento(String nuAnn, String nuEmi, String nuDes) {
    StringBuffer sql = new StringBuffer();
    DocumentoBean documentoBean = null;
    sql.append("SELECT CO_EMP_DES,ES_DOC_REC,NU_ANN,NU_EMI,NU_DES,TI_DES,CO_DEP_DES\nFROM IDOSGD.TDTV_DESTINOS\nWHERE NU_ANN = ?\nAND NU_EMI = ? AND NU_DES = ? AND ES_ELI='0'");
 
    try {
      documentoBean = (DocumentoBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoBean.class), new Object[] { nuAnn, nuEmi, nuDes });
    }
    catch (EmptyResultDataAccessException e) {
      documentoBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return documentoBean;
  }

  
  public String getEstadoDocAdmBasico(String nuAnn, String nuEmi) {
    String vResult = "NO_OK";
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT ES_DOC_EMI\nFROM IDOSGD.TDTV_REMITOS\nWHERE nu_ann = ? \nAND nu_emi = ? \nAND ES_ELI='0'");


    
    try {
      vResult = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { nuAnn, nuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      vResult = "NO_OK";
    } catch (Exception e) {
      vResult = "NO_OK";
      e.printStackTrace();
    } 
    return vResult;
  }

  
  public String updAnulaRecepecionDocumentoBean(String nuAnn, String nuEmi, String nuDes, String coUseMod) {
    StringBuilder sql = new StringBuilder();
    sql.append("UPDATE IDOSGD.TDTV_DESTINOS ");
    sql.append("SET ES_DOC_REC = '0', CO_USE_MOD = ?, FE_USE_MOD = CURRENT_TIMESTAMP ");
    sql.append(",CO_EMP_REC=NULL,FE_REC_DOC=NULL,FE_ATE_DOC=NULL,FE_ARC_DOC=NULL ");
    sql.append(",NU_COR_DES=NULL");
    sql.append(" WHERE NU_ANN = ? ");
    sql.append("AND NU_EMI = ? ");
    sql.append("AND NU_DES = ? ");
    String vReturn = "NO_OK";
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { coUseMod, nuAnn, nuEmi, nuDes });
      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String updRecepcionDocumentoBean(DocumentoBean documentoBean) {
    StringBuilder sql = new StringBuilder();
    sql.append("SET LANGUAGE Español ");
    sql.append("UPDATE IDOSGD.TDTV_DESTINOS SET ");
    sql.append("ES_DOC_REC = ?,");
    sql.append("CO_USE_MOD = ?,");
    sql.append("CO_EMP_REC ='");
    sql.append(documentoBean.getCoEmpRec());
    sql.append("',FE_USE_MOD = CURRENT_TIMESTAMP,");
    sql.append(" FE_REC_DOC = CAST('");
    sql.append(documentoBean.getFeRecDoc());
    sql.append("'+' '+CONVERT(char(8), GETDATE(), 108) AS DATETIME),");
    sql.append(" FE_ATE_DOC=CAST('");
    sql.append(documentoBean.getFeAteDoc());
    sql.append("'+' '+CONVERT(char(8), GETDATE(), 108) AS DATETIME ),");
    sql.append(" FE_ARC_DOC=CAST('");
    sql.append(documentoBean.getFeArcDoc());
    sql.append("'+' '+CONVERT(char(8), GETDATE(), 108)  AS DATETIME),");
    sql.append(" NU_COR_DES=?,");
    sql.append(" DE_ANE=?");
    sql.append(" WHERE NU_ANN = ? ");
    sql.append(" AND NU_EMI = ? ");
    sql.append(" AND NU_DES = ? ");
    String vReturn = "NO_OK";
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { documentoBean
            .getEsDocRec(), documentoBean.getCodUseMod(), documentoBean.getNuCorDes(), documentoBean
            .getDeAne(), documentoBean.getNuAnn(), documentoBean.getNuEmi(), documentoBean.getNuDes() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String updAtencionDocumentoBean(DocumentoBean documentoBean) {
    StringBuilder sql = new StringBuilder();
    sql.append("SET LANGUAGE Español ");
    sql.append("UPDATE IDOSGD.TDTV_DESTINOS SET ");
    sql.append("ES_DOC_REC = ?,");
    sql.append("CO_USE_MOD = ?,");
    if (documentoBean.getFeAteDoc() != null && documentoBean.getFeAteDoc().trim().length() > 0) {
      sql.append(" FE_ATE_DOC=CAST('");
      sql.append(documentoBean.getFeAteDoc());
      sql.append("'+' '+CONVERT(char(8), GETDATE(), 108) AS DATETIME ),");
    } 
    if (documentoBean.getFeArcDoc() != null && documentoBean.getFeArcDoc().trim().length() > 0) {
      sql.append(" FE_ARC_DOC=CAST('");
      sql.append(documentoBean.getFeArcDoc());
      sql.append("'+' '+CONVERT(char(8), GETDATE(), 108) AS DATETIME), ");
    } 
    sql.append(" FE_USE_MOD = CURRENT_TIMESTAMP ");
    sql.append(" WHERE NU_ANN = ? ");
    sql.append(" AND NU_EMI = ? ");
    sql.append(" AND NU_DES = ? ");
    String vReturn = "NO_OK";
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { documentoBean
            .getEsDocRec(), documentoBean.getCodUseMod(), documentoBean.getNuAnn(), documentoBean.getNuEmi(), documentoBean.getNuDes() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }


@Override
public List<DocumentoBean> getResumenRecepAdmList(BuscarDocumentoRecepBean buscarDocumentoRecep) {
	// TODO Auto-generated method stub
	return null;
}
}
