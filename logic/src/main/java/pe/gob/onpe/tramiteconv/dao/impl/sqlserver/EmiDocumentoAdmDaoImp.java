package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.libreria.util.Utility;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.DatosInterBean;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.bean.DestinatarioDocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.DestinoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoObjBean;
import pe.gob.onpe.tramitedoc.bean.EmpleadoBean;
import pe.gob.onpe.tramitedoc.bean.ExpedienteBean;
import pe.gob.onpe.tramitedoc.bean.MotivoBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaEmiDocBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaRemitoBean;
import pe.gob.onpe.tramitedoc.bean.RemitenteEmiBean;
import pe.gob.onpe.tramitedoc.bean.TblRemitosBean;
import pe.gob.onpe.tramitedoc.dao.EmiDocumentoAdmDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;
import pe.gob.onpe.tramitedoc.util.AleatorioCodVerificacion;
import pe.gob.onpe.tramitedoc.util.FiltroPaginate;
import pe.gob.onpe.tramitedoc.util.ProcessResult;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;





@Repository("emiDocumentoAdmDao")
public class EmiDocumentoAdmDaoImp   extends SimpleJdbcDaoBase  implements EmiDocumentoAdmDao
{
  private SimpleJdbcCall spActualizaEstado;
  private SimpleJdbcCall spInsMesaVirtual;
  
  public List<DocumentoEmiBean> getDocumentosBuscaEmiAdm(BuscarDocumentoEmiBean buscarDocumentoEmi) {
    StringBuilder sql = new StringBuilder();
    boolean bBusqFiltro = false;
    String sOrdenList = "";
    Map<String, Object> objectParam = new HashMap<String, Object>();
    
    List<DocumentoEmiBean> list = new ArrayList<DocumentoEmiBean>();
    sql.append("SET LANGUAGE Español ");
    sql.append("SELECT TOP 600 X.*, "); //por lo mientras
    sql.append(" (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_EMI_REF](X.NU_ANN, X.NU_EMI)) DE_EMI_REF, ");
    sql.append(" [IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](X.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM, ");
    sql.append(" CASE X.NU_CANDES ");
    sql.append("\tWHEN 1 THEN (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP](X.NU_ANN, X.NU_EMI)) ");
    sql.append("\tELSE (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP_V](X.NU_ANN, X.NU_EMI)) ");
    sql.append(" END DE_EMP_PRO, ");
    sql.append(" [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](X.CO_EMP_RES) DE_EMP_RES, ");
    sql.append(" [IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS](X.ES_DOC_EMI,'TDTV_REMITOS') DE_ES_DOC_EMI, ");
    sql.append(" ROW_NUMBER() OVER (ORDER BY X.NU_ANN) AS ROWNUM ");
    sql.append(" FROM ( ");
    sql.append(" SELECT ");
    sql.append(" A.NU_COR_EMI, A.FE_EMI, ");
    sql.append(" CASE ISNULL(B.TI_EMI_REF, '0') + ISNULL(B.IN_EXISTE_ANEXO, '2') ");
    sql.append("\tWHEN '00' THEN 0 ");
    sql.append("\tWHEN '02' THEN 0 ");
    sql.append("    ELSE 1 ");
    sql.append(" END EXISTE_ANEXO, ");
    sql.append(" ISNULL(B.CO_PRIORIDAD, '1') CO_PRIORIDAD, ");
    sql.append(" CONVERT(VARCHAR(10), A.FE_EMI, 103) FE_EMI_CORTA, ");
    sql.append(" A.OBS_DOC DE_OBS_DOC,");
    sql.append(" B.NU_DOC,UPPER(A.DE_ASU) DE_ASU_M,A.NU_ANN,B.NU_EXPEDIENTE,A.NU_EMI,A.TI_CAP,B.IN_EXISTE_DOC EXISTE_DOC, ");
    sql.append(" A.ES_DOC_EMI,A.CO_TIP_DOC_ADM,A.NU_CANDES,A.CO_EMP_RES,CAST(ISNULL(A.DOC_ESTADO_MSJ,'-1') AS NVARCHAR(20)) as DOC_ESTADO_MSJ, ");
    sql.append(" (SELECT MAX(C.TI_DES) FROM IDOSGD.TDTV_DESTINOS C WHERE C.NU_EMI=A.NU_EMI AND C.NU_ANN=A.NU_ANN   ) as tiDest,CAST(ISNULL(A.TI_ENV_MSJ,'-1')AS NVARCHAR(20)) tiEnvMsj,");
    sql.append(" CASE A.TI_ENV_MSJ WHEN '0' THEN 'MESA DE PARTES' WHEN '1' THEN 'ENVDIRECTO' WHEN '2' THEN 'MESA VIRTUAL' END    recursoenvio");
    sql.append(" FROM IDOSGD.TDTV_REMITOS A ");
    sql.append(" INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN B ON B.NU_ANN=A.NU_ANN AND B.NU_EMI=A.NU_EMI ");
    sql.append(" WHERE 1=1 ");


    
    sql.append(" AND A.TI_EMI='01'");
    sql.append(" AND B.TI_EMI='01'");
    sql.append(" AND A.ES_ELI = '0'");
    sql.append(" AND A.CO_GRU = '1'");
    sql.append(" AND A.CO_DEP_EMI = :pCoDepEmi");

    
    objectParam.put("pCoDepEmi", buscarDocumentoEmi.getsCoDependencia());
    
    String pTipoBusqueda = buscarDocumentoEmi.getsTipoBusqueda();
    if (pTipoBusqueda.equals("1") && buscarDocumentoEmi.isEsIncluyeFiltro()) {
      bBusqFiltro = true;
    }
    String auxTipoAcceso = buscarDocumentoEmi.getsTiAcceso();
    String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1") || auxTipoAcceso.equals("2"))) ? auxTipoAcceso : "1";
    if (buscarDocumentoEmi.getsElaboradoPor() != null && buscarDocumentoEmi.getsElaboradoPor().trim().length() > 0 && (pTipoBusqueda.equals("0") || bBusqFiltro) && tiAcceso.equals("0")) {
      sql.append(" AND A.CO_EMP_RES = :pcoEmpRes ");
      objectParam.put("pcoEmpRes", buscarDocumentoEmi.getsElaboradoPor());
    }
    else if (tiAcceso.equals("1")) {
      sql.append(" AND A.CO_EMP_RES = :pcoEmpRes ");
      objectParam.put("pcoEmpRes", buscarDocumentoEmi.getsCoEmpleado());
    } else if (tiAcceso.equals("2")) {
      sql.append(" AND (A.CO_EMP_RES = :pcoEmpRes OR A.CO_EMP_EMI = :pcoEmpRes) ");
      objectParam.put("pcoEmpRes", buscarDocumentoEmi.getsCoEmpleado());
    } 


    
    if (pTipoBusqueda.equals("0") || bBusqFiltro) {
      if (buscarDocumentoEmi.getCoTema() != null && buscarDocumentoEmi.getCoTema().trim().length() > 0) {
        sql.append(" AND A.CO_TEMA = :pCoTema ");
        objectParam.put("pCoTema", buscarDocumentoEmi.getCoTema());
      } 
      if (buscarDocumentoEmi.getsTipoDoc() != null && buscarDocumentoEmi.getsTipoDoc().trim().length() > 0) {
        sql.append(" AND A.CO_TIP_DOC_ADM = :pCoDocEmi ");
        objectParam.put("pCoDocEmi", buscarDocumentoEmi.getsTipoDoc());
      } 
      if (buscarDocumentoEmi.getsEstadoDoc() != null && buscarDocumentoEmi.getsEstadoDoc().trim().length() > 0) {
        sql.append(" AND A.ES_DOC_EMI = :pEsDocEmi ");
        objectParam.put("pEsDocEmi", buscarDocumentoEmi.getsEstadoDoc());
      } else {
        sOrdenList = " DESC";
      } 
      if (buscarDocumentoEmi.getsPrioridadDoc() != null && buscarDocumentoEmi.getsPrioridadDoc().trim().length() > 0) {
        sql.append(" AND B.CO_PRIORIDAD = :pCoPrioridad ");
        objectParam.put("pCoPrioridad", buscarDocumentoEmi.getsPrioridadDoc());
      } 
      if (buscarDocumentoEmi.getsRefOrigen() != null && buscarDocumentoEmi.getsRefOrigen().trim().length() > 0) {
        sql.append(" AND CHARINDEX(:pTiEmiRef,B.TI_EMI_REF) > 0 ");
        objectParam.put("pTiEmiRef", buscarDocumentoEmi.getsRefOrigen());
      } 
      if (buscarDocumentoEmi.getsDestinatario() != null && buscarDocumentoEmi.getsDestinatario().trim().length() > 0) {
        sql.append(" AND CHARINDEX(:pTiEmpPro, B.TI_EMI_DES) > 0 ");
        objectParam.put("pTiEmpPro", buscarDocumentoEmi.getsDestinatario());
      } 
      
      if (buscarDocumentoEmi.getsTrabDestino() != null && buscarDocumentoEmi.getsTrabDestino().trim().length() > 0) {
        sql.append(" AND ISNULL((SELECT TOP 1 1 FROM IDOSGD.TDTV_DESTINOS C WHERE C.NU_EMI=A.NU_EMI AND C.NU_ANN=A.NU_ANN AND CO_EMP_DES=:pTiEmpDest  ),0) > 0 ");
        objectParam.put("pTiEmpDest", buscarDocumentoEmi.getsTrabDestino());
      } 
      if (buscarDocumentoEmi.getEsFiltroFecha() != null && (buscarDocumentoEmi
        .getEsFiltroFecha().equals("1") || buscarDocumentoEmi.getEsFiltroFecha().equals("3") || buscarDocumentoEmi.getEsFiltroFecha().equals("2"))) {
        String vFeEmiIni = buscarDocumentoEmi.getsFeEmiIni();
        String vFeEmiFin = buscarDocumentoEmi.getsFeEmiFin();
        if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
          .trim().length() > 0) {
          sql.append(" AND FE_EMI BETWEEN CONVERT(DATETIME, :pFeEmiIni, 103) AND CONVERT(DATETIME, :pFeEmiFin, 103) + 0.99999 ");
          objectParam.put("pFeEmiIni", vFeEmiIni);
          objectParam.put("pFeEmiFin", vFeEmiFin);
        } 
      } 
      
      if (buscarDocumentoEmi.getCoEstMensajeria() != null && buscarDocumentoEmi.getCoEstMensajeria().trim().length() > 0) {
        sql.append(" AND CO_TIP_DOC_ADM in ('011','012') AND A.ES_DOC_EMI=0 AND  ISNULL((A.TI_ENV_MSJ),3)= :pcoEstMensajeria");
        objectParam.put("pcoEstMensajeria", buscarDocumentoEmi.getCoEstMensajeria());
      } 
    } 

    
    if (pTipoBusqueda.equals("1")) {
      if (buscarDocumentoEmi.getsNumCorEmision() != null && buscarDocumentoEmi.getsNumCorEmision().trim().length() > 0) {
        sql.append(" AND A.NU_COR_EMI = :pnuCorEmi ");
        objectParam.put("pnuCorEmi", buscarDocumentoEmi.getsNumCorEmision());
      } 
      
      if (buscarDocumentoEmi.getsNumDoc() != null && buscarDocumentoEmi.getsNumDoc().trim().length() > 1) {
        sql.append(" AND B.NU_DOC LIKE ''+:pnuDocEmi+'%' ");
        objectParam.put("pnuDocEmi", buscarDocumentoEmi.getsNumDoc());
      } 
      
      if (buscarDocumentoEmi.getsBuscNroExpediente() != null && buscarDocumentoEmi.getsBuscNroExpediente().trim().length() > 1) {
        sql.append(" AND B.NU_EXPEDIENTE LIKE '%'+:pnuExpediente+'%' ");
        objectParam.put("pnuExpediente", buscarDocumentoEmi.getsBuscNroExpediente());
      } 

      
      if (buscarDocumentoEmi.getsDeAsuM() != null && buscarDocumentoEmi.getsDeAsuM().trim().length() > 1) {
        
        sql.append(" AND UPPER(A.DE_ASU) LIKE '%'+:pDeAsunto+'%' ");
        objectParam.put("pDeAsunto", buscarDocumentoEmi.getsDeAsuM());
      } 
    } 
    sql.append(") AS X ");
    sql.append(" ORDER BY X.FE_EMI").append(sOrdenList);
    
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class));
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
      e.printStackTrace();
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  public ProcessResult<List<DocumentoBean>> getDocumentosBuscaEmiAdmList(BuscarDocumentoEmiBean buscarDocumentoEmi, FiltroPaginate paginate) {
    StringBuffer sql = new StringBuffer();
    boolean bBusqFiltro = false;
    
    Map<String, Object> objectParam = new HashMap<String, Object>();
    ProcessResult<List<DocumentoBean>> Result = new ProcessResult();
    List<DocumentoBean> list = new ArrayList<DocumentoBean>();
    sql.append("SET LANGUAGE Español ");
    sql.append("SELECT TOP 400 X.*, "); //por lo mientras
    sql.append(" (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_EMI_REF](X.NU_ANN, X.NU_EMI)) DE_EMI_REF, ");
    sql.append(" [IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](X.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM, ");
    sql.append(" CASE X.NU_CANDES ");
    sql.append("\tWHEN 1 THEN (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP](X.NU_ANN, X.NU_EMI)) ");
    sql.append("\tELSE (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP_V](X.NU_ANN, X.NU_EMI)) ");
    sql.append(" END DE_EMP_PRO, ");
    sql.append(" [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](X.CO_EMP_RES) DE_EMP_RES, ");
    sql.append(" [IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS](X.ES_DOC_EMI,'TDTV_REMITOS') DE_ES_DOC_EMI, ");
    sql.append(" ROW_NUMBER() OVER (ORDER BY X.NU_ANN) AS ROWNUM ");
    sql.append(" FROM ( ");
    sql.append(" SELECT  COUNT(FE_EMI) OVER()  AS filasTotal,");
    sql.append(" ROW_NUMBER () OVER ( ");
    if (buscarDocumentoEmi.getOrden().equals("feEmiCorta-ASC")) { sql.append(" ORDER BY FE_EMI ASC  "); }
    else if (buscarDocumentoEmi.getOrden().equals("feEmiCorta-DESC")) { sql.append(" ORDER BY FE_EMI DESC "); }
    else if (buscarDocumentoEmi.getOrden().equals("deEmiRef-ASC")) { sql.append(" ORDER BY NU_RUC_EMI,NU_DNI_EMI,CO_OTR_ORI_EMI ASC"); }
    else if (buscarDocumentoEmi.getOrden().equals("deEmiRef-DESC")) { sql.append(" ORDER BY NU_RUC_EMI,NU_DNI_EMI,CO_OTR_ORI_EMI DESC "); }
    else if (buscarDocumentoEmi.getOrden().equals("deTipDocAdm-ASC")) { sql.append(" ORDER BY CO_TIP_DOC_ADM ASC "); }
    else if (buscarDocumentoEmi.getOrden().equals("deTipDocAdm-DESC")) { sql.append(" ORDER BY CO_TIP_DOC_ADM DESC "); }
    else if (buscarDocumentoEmi.getOrden().equals("nuDoc-ASC")) { sql.append(" ORDER BY NU_DOC ASC "); }
    else if (buscarDocumentoEmi.getOrden().equals("nuDoc-DESC")) { sql.append(" ORDER BY NU_DOC DESC "); }
    else if (buscarDocumentoEmi.getOrden().equals("deEmpPro-ASC")) { sql.append(" ORDER BY ti_emi ASC "); }
    else if (buscarDocumentoEmi.getOrden().equals("deEmpPro-DESC")) { sql.append(" ORDER BY ti_emi DESC "); }
    else if (buscarDocumentoEmi.getOrden().equals("deAsuM-ASC")) { sql.append(" ORDER BY DE_ASU ASC "); }
    else if (buscarDocumentoEmi.getOrden().equals("deAsuM-DESC")) { sql.append(" ORDER BY DE_ASU DESC "); }
    else if (buscarDocumentoEmi.getOrden().equals("nuExpediente-ASC")) { sql.append(" ORDER BY NU_EXPEDIENTE ASC "); }
    else if (buscarDocumentoEmi.getOrden().equals("nuExpediente-DESC")) { sql.append(" ORDER BY NU_EXPEDIENTE DESC "); }
    else if (buscarDocumentoEmi.getOrden().equals("deEmpRes-ASC")) { sql.append(" ORDER BY A.CO_EMP_RES ASC "); }
    else if (buscarDocumentoEmi.getOrden().equals("deEmpRes-DESC")) { sql.append(" ORDER BY A.CO_EMP_RES DESC "); }
    else if (buscarDocumentoEmi.getOrden().equals("deEsDocEmi-ASC")) { sql.append(" ORDER BY A.ES_DOC_EMI ASC "); }
    else if (buscarDocumentoEmi.getOrden().equals("deEsDocEmi-DESC")) { sql.append(" ORDER BY A.ES_DOC_EMI DESC "); }
    else { sql.append(" ORDER BY FE_EMI "); }
    
    sql.append("  ) AS fila, ");
    sql.append(" A.NU_COR_EMI, A.FE_EMI, ");
    sql.append(" CASE ISNULL(B.TI_EMI_REF, '0') + ISNULL(B.IN_EXISTE_ANEXO, '2') ");
    sql.append("\tWHEN '00' THEN 0 ");
    sql.append("\tWHEN '02' THEN 0 ");
    sql.append("    ELSE 1 ");
    sql.append(" END EXISTE_ANEXO, ");
    sql.append(" ISNULL(B.CO_PRIORIDAD, '1') CO_PRI, ");
    sql.append(" CONVERT(VARCHAR(10), A.FE_EMI, 103) FE_EMI_CORTA, ");
    sql.append(" A.OBS_DOC DE_OBS_DOC,");
    sql.append(" B.NU_DOC,UPPER(A.DE_ASU) DE_ASU_M,A.NU_ANN,B.NU_EXPEDIENTE,A.NU_EMI,A.TI_CAP,B.IN_EXISTE_DOC EXISTE_DOC, ");
    sql.append(" A.ES_DOC_EMI,A.CO_TIP_DOC_ADM,A.NU_CANDES,A.CO_EMP_RES,CAST(ISNULL(A.DOC_ESTADO_MSJ,'-1') AS NVARCHAR(20)) as DOC_ESTADO_MSJ, ");
    sql.append(" (SELECT MAX(C.TI_DES) FROM IDOSGD.TDTV_DESTINOS C WHERE C.NU_EMI=A.NU_EMI AND C.NU_ANN=A.NU_ANN   ) as tiDest,CAST(ISNULL(A.TI_ENV_MSJ,'-1')AS NVARCHAR(20)) tiEnvMsj,");
    sql.append(" CASE A.TI_ENV_MSJ WHEN '0' THEN 'MESA DE PARTES' WHEN '1' THEN 'ENVDIRECTO' WHEN '2' THEN 'MESA VIRTUAL'   WHEN '3' THEN 'NOTIFICACIÓN ELECTRÓNICA' END    recursoenvio");
    sql.append(" FROM IDOSGD.TDTV_REMITOS A ");
    sql.append(" INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN B ON B.NU_ANN=A.NU_ANN AND B.NU_EMI=A.NU_EMI ");
    sql.append(" WHERE 1=1 ");
    String pNUAnn = buscarDocumentoEmi.getsCoAnnio();
    if (!buscarDocumentoEmi.getEsFiltroFecha().equals("1") || !pNUAnn.equals("0")) {
      sql.append(" AND A.NU_ANN = :pNuAnn");
      
      objectParam.put("pNuAnn", pNUAnn);
    } 
    sql.append(" AND A.TI_EMI='01'");
    sql.append(" AND B.TI_EMI='01'");
    sql.append(" AND A.ES_ELI = '0'");
    sql.append(" AND A.CO_GRU = '1'");
    sql.append(" AND A.CO_DEP_EMI = :pCoDepEmi");

    
    objectParam.put("pCoDepEmi", buscarDocumentoEmi.getsCoDependencia());
    
    String pTipoBusqueda = buscarDocumentoEmi.getsTipoBusqueda();
    if (pTipoBusqueda.equals("1") && buscarDocumentoEmi.isEsIncluyeFiltro()) {
      bBusqFiltro = true;
    }
    String auxTipoAcceso = buscarDocumentoEmi.getsTiAcceso();
    String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1") || auxTipoAcceso.equals("2"))) ? auxTipoAcceso : "1";
    if (buscarDocumentoEmi.getsElaboradoPor() != null && buscarDocumentoEmi.getsElaboradoPor().trim().length() > 0 && (pTipoBusqueda.equals("0") || bBusqFiltro) && tiAcceso.equals("0")) {
      sql.append(" AND A.CO_EMP_RES = :pcoEmpRes ");
      objectParam.put("pcoEmpRes", buscarDocumentoEmi.getsElaboradoPor());
    }
    else if (tiAcceso.equals("1")) {
      sql.append(" AND A.CO_EMP_RES = :pcoEmpRes ");
      objectParam.put("pcoEmpRes", buscarDocumentoEmi.getsCoEmpleado());
    } else if (tiAcceso.equals("2")) {
      sql.append(" AND (A.CO_EMP_RES = :pcoEmpRes OR A.CO_EMP_EMI = :pcoEmpRes) ");
      objectParam.put("pcoEmpRes", buscarDocumentoEmi.getsCoEmpleado());
    } 


    
    if (pTipoBusqueda.equals("0") || bBusqFiltro) {
	      if (buscarDocumentoEmi.getCoTema() != null && buscarDocumentoEmi.getCoTema().trim().length() > 0) {
	        sql.append(" AND A.CO_TEMA = :pCoTema ");
	        objectParam.put("pCoTema", buscarDocumentoEmi.getCoTema());
	      } 
	      if (buscarDocumentoEmi.getsTipoDoc() != null && buscarDocumentoEmi.getsTipoDoc().trim().length() > 0) {
	        sql.append(" AND A.CO_TIP_DOC_ADM = :pCoDocEmi ");
	        objectParam.put("pCoDocEmi", buscarDocumentoEmi.getsTipoDoc());
	      } 
	      if (buscarDocumentoEmi.getsEstadoDoc() != null && buscarDocumentoEmi.getsEstadoDoc().trim().length() > 0) {
	        sql.append(" AND A.ES_DOC_EMI = :pEsDocEmi ");
	        objectParam.put("pEsDocEmi", buscarDocumentoEmi.getsEstadoDoc());
	      } 
	
	      
	      if (buscarDocumentoEmi.getsPrioridadDoc() != null && buscarDocumentoEmi.getsPrioridadDoc().trim().length() > 0) {
	        sql.append(" AND B.CO_PRIORIDAD = :pCoPrioridad ");
	        objectParam.put("pCoPrioridad", buscarDocumentoEmi.getsPrioridadDoc());
	      } 
	      if (buscarDocumentoEmi.getsRefOrigen() != null && buscarDocumentoEmi.getsRefOrigen().trim().length() > 0) {
	        sql.append(" AND CHARINDEX(:pTiEmiRef,B.TI_EMI_REF) > 0 ");
	        objectParam.put("pTiEmiRef", buscarDocumentoEmi.getsRefOrigen());
	      } 
	      if (buscarDocumentoEmi.getsDestinatario() != null && buscarDocumentoEmi.getsDestinatario().trim().length() > 0) {
	        sql.append(" AND CHARINDEX(:pTiEmpPro, B.TI_EMI_DES) > 0 ");
	        objectParam.put("pTiEmpPro", buscarDocumentoEmi.getsDestinatario());
	      } 
	      
	      if (buscarDocumentoEmi.getsTrabDestino() != null && buscarDocumentoEmi.getsTrabDestino().trim().length() > 0) {
	        sql.append(" AND ISNULL((SELECT TOP 1 1 FROM IDOSGD.TDTV_DESTINOS C WHERE C.NU_EMI=A.NU_EMI AND C.NU_ANN=A.NU_ANN AND CO_EMP_DES=:pTiEmpDest  ),0) > 0 ");
	        objectParam.put("pTiEmpDest", buscarDocumentoEmi.getsTrabDestino());
	      } 
	      if (buscarDocumentoEmi.getEsFiltroFecha() != null && (buscarDocumentoEmi
	        .getEsFiltroFecha().equals("1") || buscarDocumentoEmi.getEsFiltroFecha().equals("3"))) {
	        String vFeEmiIni = buscarDocumentoEmi.getsFeEmiIni();
	        String vFeEmiFin = buscarDocumentoEmi.getsFeEmiFin();
	        if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
	          .trim().length() > 0) {
	          sql.append(" AND FE_EMI BETWEEN CONVERT(DATETIME, :pFeEmiIni, 103) AND CONVERT(DATETIME, :pFeEmiFin, 103) + 0.99999 ");
	          objectParam.put("pFeEmiIni", vFeEmiIni);
	          objectParam.put("pFeEmiFin", vFeEmiFin);
	        } 
	      } 
	      
	      if (buscarDocumentoEmi.getCoEstMensajeria() != null && buscarDocumentoEmi.getCoEstMensajeria().trim().length() > 0) {
	       // sql.append(" AND CO_TIP_DOC_ADM in ('011','012') AND A.ES_DOC_EMI=0 AND  ISNULL((A.TI_ENV_MSJ),3)= :pcoEstMensajeria");
	       // objectParam.put("pcoEstMensajeria", buscarDocumentoEmi.getCoEstMensajeria());
	        
	        sql.append(" AND CO_TIP_DOC_ADM in ('011','012','248') AND A.DOC_ESTADO_MSJ is null and A.TI_ENV_MSJ is null ");
	        sql.append(" AND NOT EXISTS(select D.NU_EMI from IDOSGD.TDTV_DESTINOS D  WHERE TI_DES ='01' AND  D.NU_EMI=a.NU_EMI ");
	        sql.append(" AND D.NU_ANN=a.NU_ANN  and   D.ES_ELI='0' ) ");
	      } 
	      //EJHM
	      if (buscarDocumentoEmi.getsNumCorEmision() != null && buscarDocumentoEmi.getsNumCorEmision().trim().length() > 0) {
	          sql.append(" AND A.NU_COR_EMI = :pnuCorEmi ");
	          objectParam.put("pnuCorEmi", buscarDocumentoEmi.getsNumCorEmision());
	      }
	      
	      if (buscarDocumentoEmi.getsNumDoc() != null && buscarDocumentoEmi.getsNumDoc().trim().length() > 1) {
	          sql.append(" AND B.NU_DOC LIKE ''+:pnuDocEmi+'%' ");
	          objectParam.put("pnuDocEmi", buscarDocumentoEmi.getsNumDoc());
	        } 
	        
	        if (buscarDocumentoEmi.getsBuscNroExpediente() != null && buscarDocumentoEmi.getsBuscNroExpediente().trim().length() > 1) {
	          sql.append(" AND B.NU_EXPEDIENTE LIKE '%'+:pnuExpediente+'%' ");
	          objectParam.put("pnuExpediente", buscarDocumentoEmi.getsBuscNroExpediente());
	        } 
	       if (buscarDocumentoEmi.getsDeAsuM() != null && buscarDocumentoEmi.getsDeAsuM().trim().length() > 1) {
		        
		        sql.append(" AND UPPER(A.DE_ASU) LIKE '%'+:pDeAsunto+'%' ");
		        objectParam.put("pDeAsunto", buscarDocumentoEmi.getsDeAsuM());
		    } 
        
    } 

    
    if (pTipoBusqueda.equals("1")) {
	      if (buscarDocumentoEmi.getsNumCorEmision() != null && buscarDocumentoEmi.getsNumCorEmision().trim().length() > 0) {
	        sql.append(" AND A.NU_COR_EMI = :pnuCorEmi ");
	        objectParam.put("pnuCorEmi", buscarDocumentoEmi.getsNumCorEmision());
	      } 
	      
	      if (buscarDocumentoEmi.getsNumDoc() != null && buscarDocumentoEmi.getsNumDoc().trim().length() > 1) {
	        sql.append(" AND B.NU_DOC LIKE ''+:pnuDocEmi+'%' ");
	        objectParam.put("pnuDocEmi", buscarDocumentoEmi.getsNumDoc());
	      } 
	      
	      if (buscarDocumentoEmi.getsBuscNroExpediente() != null && buscarDocumentoEmi.getsBuscNroExpediente().trim().length() > 1) {
	        sql.append(" AND B.NU_EXPEDIENTE LIKE '%'+:pnuExpediente+'%' ");
	        objectParam.put("pnuExpediente", buscarDocumentoEmi.getsBuscNroExpediente());
	      } 
	
	      
	      if (buscarDocumentoEmi.getsDeAsuM() != null && buscarDocumentoEmi.getsDeAsuM().trim().length() > 1) {
	        
	        sql.append(" AND UPPER(A.DE_ASU) LIKE '%'+:pDeAsunto+'%' ");
	        objectParam.put("pDeAsunto", buscarDocumentoEmi.getsDeAsuM());
	      } 
    } 
    sql.append(" ) AS X ");
    
    sql.append(" WHERE fila BETWEEN " + paginate.getPaginaDesde() + " AND " + paginate.getPaginaHasta() + "  ");

  
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoBean.class));
      Result.setResult(list);
      Result.setIsSuccess(true);
      objectParam = new HashMap<String, Object>();
      buscarDocumentoEmi = new BuscarDocumentoEmiBean();
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

  
  public DocumentoEmiBean getDocumentoEmitido(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT ");
    sql.append("a.nu_ann, ");
    sql.append("a.nu_emi, ");
    sql.append("CAST(a.nu_cor_emi AS VARCHAR(10)) nu_cor_emi, ");
    sql.append("a.co_loc_emi, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_LOCAL](a.co_loc_emi) de_loc_emi, ");
    sql.append("a.co_dep_emi, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](a.co_dep_emi) de_dep_emi, ");
    sql.append("a.ti_emi, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_TI_DESTINO](a.ti_emi) de_tip_emi, ");
    sql.append("a.co_emp_emi, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](co_emp_emi) de_emp_emi, ");
    sql.append("a.co_emp_res, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](a.co_emp_res) de_emp_res, ");
    sql.append("a.nu_dni_emi, ");
    sql.append("a.nu_ruc_emi, ");
    sql.append("a.co_otr_ori_emi, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_TI_EMI_EMP](a.nu_ann, a.nu_emi) de_ori_emi, ");
    sql.append("CONVERT(VARCHAR(10), a.fe_emi, 103) fe_emi, ");
    sql.append("CONVERT(VARCHAR(10), A.FE_EMI, 103) FE_EMI_CORTA, ");
    sql.append("a.co_tip_doc_adm, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](a.co_tip_doc_adm) de_tip_doc_adm, ");
    sql.append("a.ti_emi, ");
    sql.append("a.nu_doc_emi, ");
    sql.append("a.de_doc_sig, ");
    sql.append("a.es_doc_emi, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS](a.es_doc_emi, 'TDTV_REMITOS') de_es_doc_emi, ");
    sql.append("a.nu_dia_ate, ");
    sql.append("a.de_asu, ");
    sql.append("-- a.co_pro, ");
    sql.append("-- a.co_sub, ");
    sql.append("a.ti_cap, ");
    sql.append("a.co_exp, ");
    sql.append("a.co_use_cre, ");
    sql.append("a.fe_use_cre, ");
    sql.append("a.co_use_mod, ");
    sql.append("a.fe_use_mod, ");
    sql.append("a.nu_ann_exp, ");
    sql.append("a.nu_sec_exp, ");
    sql.append("a.nu_det_exp, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_NU_EXPEDIENTE](a.nu_ann_exp,a.nu_sec_exp) NU_EXPEDIENTE ");
    sql.append("FROM IDOSGD.TDTV_REMITOS a ");
    sql.append("WHERE A.NU_ANN = ? ");
    sql.append("AND A.NU_EMI = ? ");
    
    DocumentoEmiBean documentoEmiBean = new DocumentoEmiBean();
    try {
      documentoEmiBean = (DocumentoEmiBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class), new Object[] { pnuAnn, pnuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      documentoEmiBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return documentoEmiBean;
  }


  
  public DocumentoEmiBean getDocumentoEmiAdm(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    sql.append("select A.*, ");
    sql.append("OBS_DOC DE_OBS_DOC,[IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](A.CO_EMP_EMI) DE_EMP_EMI, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](A.CO_EMP_RES) DE_EMP_RES, ");
    sql.append("CONVERT(VARCHAR(10), A.FE_EMI, 103) FE_EMI_CORTA, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS](A.ES_DOC_EMI,'TDTV_REMITOS') DE_ES_DOC_EMI, ");
    sql.append("B.FE_EXP, ");
    sql.append("CONVERT(VARCHAR(10), B.FE_EXP, 103) FE_EXP_CORTA,B.NU_EXPEDIENTE, ");
    sql.append("B.CO_PROCESO, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_PROCESO_EXP](B.CO_PROCESO) DE_PROCESO, ");
    sql.append("RR.IN_FIRMA_ANEXO, ");
    sql.append(" CAST(ISNULL(A.DOC_ESTADO_MSJ,'-1') AS NVARCHAR(20))  DOC_ESTADO_MSJ,(SELECT MAX(C.TI_DES) FROM IDOSGD.TDTV_DESTINOS C WHERE C.NU_EMI=A.NU_EMI AND C.NU_ANN=A.NU_ANN  ) as tiDest,CAST(ISNULL(A.TI_ENV_MSJ,'-1') AS NVARCHAR(20)) tiEnvMsj \n");
    sql.append("from IDOSGD.TDTV_REMITOS A ");
    sql.append("left join IDOSGD.TDTC_EXPEDIENTE B on A.NU_ANN_EXP = B.NU_ANN_EXP and A.NU_SEC_EXP = B.NU_SEC_EXP ");
    sql.append("left join IDOSGD.TDTX_REMITOS_RESUMEN RR ON A.NU_ANN=RR.NU_ANN AND A.NU_EMI=RR.NU_EMI ");
    sql.append("where A.NU_ANN = ? ");
    sql.append("AND A.NU_EMI = ? ");
    
    DocumentoEmiBean documentoEmiBean = new DocumentoEmiBean();
    try {
      documentoEmiBean = (DocumentoEmiBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class), new Object[] { pnuAnn, pnuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      documentoEmiBean = null;
    } catch (Exception e) {
      System.out.println("nu_ann=" + pnuAnn + ",nu_emi=" + pnuEmi);
      e.printStackTrace();
    } 
    return documentoEmiBean;
  }


  
  public ExpedienteBean getExpDocumentoEmitido(String pnuAnnExp, String pnuSecExp) {
    StringBuffer sql = new StringBuffer();
    sql.append("select B.NU_EXPEDIENTE, ");
    sql.append("B.FE_EXP, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_PROCESO_EXP](B.CO_PROCESO) DE_PROCESO, ");
    sql.append("CONVERT(VARCHAR(10), B.FE_EXP, 103) FE_EXP_CORTA, ");
    sql.append("B.NU_ANN_EXP, ");
    sql.append("B.NU_SEC_EXP, ");
    sql.append("B.CO_PROCESO ");
    sql.append("from IDOSGD.TDTC_EXPEDIENTE B ");
    sql.append("where B.NU_ANN_EXP = ? ");
    sql.append("AND B.NU_SEC_EXP = ? ");
    
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

  
  public EmpleadoBean getEmpleadoLocaltblDestinatario(String pcoDependencia) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT A.CO_LOC CO_LOCAL, ");
    sql.append("\t   [IDOSGD].[PK_SGD_DESCRIPCION_DE_LOCAL](A.CO_LOC) DE_LOCAL, ");
    sql.append("\t   B.CO_EMPLEADO CEMP_CODEMP, ");
    sql.append("\t   [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](B.CO_EMPLEADO) COMP_NAME ");
    sql.append("FROM IDOSGD.sitm_local_dependencia A ");
    sql.append(" INNER JOIN IDOSGD.rhtm_dependencia B  ON B.CO_DEPENDENCIA=A.CO_DEP ");
    sql.append("WHERE   A.CO_DEP = ? ");
    
    EmpleadoBean empleadoBean = new EmpleadoBean();
    try {
      empleadoBean = (EmpleadoBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[] { pcoDependencia });
    }
    catch (EmptyResultDataAccessException e) {
      empleadoBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return empleadoBean;
  }

  
  public List<EmpleadoBean> getPersonalDestinatario(String pcoDepen) {
    StringBuffer sql = new StringBuffer();
    List<EmpleadoBean> list = new ArrayList<EmpleadoBean>();
    
    sql.append("SELECT e.cemp_apepat, e.cemp_apemat, e.cemp_denom, e.CEMP_CODEMP ");
    sql.append("FROM IDOSGD.RHTM_PER_EMPLEADOS e INNER JOIN ");
    sql.append("( ");
    sql.append("SELECT CEMP_CODEMP ");
    sql.append("FROM IDOSGD.RHTM_PER_EMPLEADOS ");
    sql.append("where CEMP_EST_EMP = '1' ");
    sql.append("  and (CO_DEPENDENCIA=? or CEMP_CO_DEPEND=?) ");
    sql.append("union ");
    sql.append("select co_emp from IDOSGD.tdtx_dependencia_empleado  where co_dep=? and es_emp='0' ");
    sql.append("union ");
    sql.append("select co_empleado from IDOSGD.rhtm_dependencia where co_dependencia = ? ");
    sql.append(") a ON e.cemp_codemp = a.cemp_codemp ");
    sql.append("order by 1 ");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[] { pcoDepen, pcoDepen, pcoDepen, pcoDepen });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }


  
  public List<MotivoBean> getLstMotivoxTipoDocumento(String pcoDepen, String pcoTipDoc) {
    StringBuffer sql = new StringBuffer();
    List<MotivoBean> list = new ArrayList<MotivoBean>();
    
    sql.append("SELECT a.de_mot, ");
    sql.append("       a.co_mot ");
    sql.append("  FROM IDOSGD.tdtr_motivo         a, ");
    sql.append("       IDOSGD.tdtx_moti_docu_depe b ");
    sql.append(" WHERE a.co_mot     = b.co_mot ");
    sql.append("   AND b.co_dep     = ? ");
    sql.append("   AND b.co_tip_doc = ? ");
    sql.append("UNION ");
    sql.append("SELECT de_mot, ");
    sql.append("       co_mot ");
    sql.append("FROM IDOSGD.tdtr_motivo ");
    sql.append("where co_mot in ('0','1') ");
    sql.append(" ORDER BY 1 ");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(MotivoBean.class), new Object[] { pcoDepen, pcoTipDoc });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DocumentoBean> getLstDocEmitidoRef(String pcoEmpEmi, String pcoDepen, String pannio, String ptiDoc, String pnuDoc) {
    StringBuffer sql = new StringBuffer();
    List<DocumentoBean> list = new ArrayList<DocumentoBean>();
    
    sql.append("SELECT A.* ");
    sql.append("FROM ( ");
    
    sql.append("select E.*, ROW_NUMBER() OVER (ORDER BY E.fe_emi) AS ROWNUM ");
    sql.append("from( ");
    sql.append("select ");
    sql.append("RE.fe_emi, ");
    sql.append("SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](RE.co_tip_doc_adm), 1, 100) DE_TIP_DOC_ADM, ");
    sql.append("RR.NU_DOC, ");
    sql.append("CONVERT(VARCHAR(10), RE.fe_emi, 3) FE_EMI_CORTA, ");
    sql.append("null FE_REC_DOC_CORTA, ");
    sql.append("RE.NU_ANN, ");
    sql.append("RE.NU_EMI, ");
    sql.append("NULL NU_DES, ");
    sql.append("replace(ltrim(rtrim(RE.de_asu)), char(10), ' ') DE_ASU, ");
    sql.append("RE.CO_TIP_DOC_ADM, ");
    sql.append("SUBSTRING(RR.NU_EXPEDIENTE, 1, 20) NU_EXPEDIENTE, ");
    sql.append("RE.NU_ANN_EXP, ");
    sql.append("RE.NU_SEC_EXP ");
    sql.append("from IDOSGD.TDTV_REMITOS RE  ");
    sql.append("INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN RR ON RE.NU_ANN=RR.NU_ANN AND RE.NU_EMI=RR.NU_EMI  ");
    sql.append("where RE.nu_ann = ? ");
    sql.append("AND RE.es_eli = '0' ");
    sql.append("and RE.es_doc_emi not in ('9','7','5') ");
    sql.append("AND RE.CO_GRU = '1' ");
    sql.append("AND RE.co_dep_emi in ");
    sql.append("(select ? ");
    sql.append("union ");
    sql.append("  SELECT co_dep_ref ");
    sql.append("    FROM IDOSGD.tdtx_referencia ");
    sql.append("   WHERE co_dep_emi = ? ");
    sql.append("     AND ti_emi = 'D' ");
    sql.append("     AND es_ref = '1') ");
    sql.append("AND RE.co_tip_doc_adm = ? ");
    if (pnuDoc != null && pnuDoc.trim().length() > 0) {
      pnuDoc = Utility.getInstancia().rellenaCerosIzquierda(pnuDoc, 6);
      sql.append("AND (RE.NU_DOC_EMI = '").append(pnuDoc).append("' OR RR.NU_EXPEDIENTE LIKE '%").append(pnuDoc).append("') ");
    } 
    
    sql.append("UNION ");
    
    sql.append("select ");
    sql.append("RE.fe_emi, ");
    sql.append("SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](RE.co_tip_doc_adm), 1, 100) TIPO, ");
    sql.append("RR.NU_DOC numero, ");
    sql.append("CONVERT(VARCHAR(10), RE.fe_emi, 3) fecha_emision, ");
    sql.append("null fecha_recepcion, ");
    sql.append("RE.NU_ANN, ");
    sql.append("RE.NU_EMI, ");
    sql.append("NULL NU_DES, ");
    sql.append("replace(ltrim(rtrim(RE.de_asu)), char(10), ' ') ASUNTO, ");
    sql.append("RE.CO_TIP_DOC_ADM, ");
    sql.append("SUBSTRING(RR.NU_EXPEDIENTE, 1, 20) NU_EXPEDIENTE, ");
    sql.append("RE.NU_ANN_EXP, ");
    sql.append("RE.NU_SEC_EXP ");
    sql.append("from IDOSGD.TDTV_REMITOS RE  ");
    sql.append("INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN RR ON RE.NU_ANN=RR.NU_ANN AND RE.NU_EMI=RR.NU_EMI ");
    sql.append("where RE.nu_ann = ? ");
    sql.append("AND RE.es_eli = '0' ");
    sql.append("AND RE.es_doc_emi not in ('9','7','5') ");
    sql.append("AND RE.CO_GRU = '2' ");
    sql.append("AND RE.co_tip_doc_adm = ? ");
    if (pnuDoc != null && pnuDoc.trim().length() > 0) {
      pnuDoc = Utility.getInstancia().rellenaCerosIzquierda(pnuDoc, 6);
      sql.append("AND (RE.NU_DOC_EMI = '").append(pnuDoc).append("' OR RR.NU_EXPEDIENTE LIKE '%").append(pnuDoc).append("') ");
    } 
    if (pcoEmpEmi != null && pcoEmpEmi.trim().length() > 0) {
      sql.append("and RE.co_emp_emi = '").append(pcoEmpEmi).append("'\n");
    }

    
    sql.append(") E ");
    
    sql.append(")AS A ");
    
    sql.append(" WHERE ROWNUM < 600 "); // por lo mientras
    sql.append(" ORDER BY A.fe_emi DESC ");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoBean.class), new Object[] { pannio, pcoDepen, pcoDepen, ptiDoc, pannio, ptiDoc });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DocumentoBean> getLstDocRecepcionadoRef(String pcoDepen, String pannio, String ptiDoc, String pnuDoc) {
    StringBuffer sql = new StringBuffer();
    List<DocumentoBean> list = new ArrayList<DocumentoBean>();
    String nuDocT= pnuDoc;
    
    sql.append("SELECT A.* ");
    sql.append("FROM ( ");
    
    sql.append("select E.*, ROW_NUMBER() OVER (ORDER BY E.FE_REC_DOC) AS ROWNUM ");
    sql.append("from( ");
    sql.append("select ");
    sql.append("r.FE_EMI, d.FE_REC_DOC, ");
    sql.append("SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](r.co_tip_doc_adm), 1, 100) de_tip_doc_adm, ");
    sql.append("SUBSTRING(rr.nu_doc,1,50) NU_DOC, ");
    sql.append("CONVERT(VARCHAR(10), r.FE_EMI, 3) FE_EMI_CORTA, ");
    sql.append("CONVERT(VARCHAR(10), d.FE_REC_DOC, 3) FE_REC_DOC_CORTA, ");
    sql.append("r.nu_ann, ");
    sql.append("r.nu_emi, ");
    sql.append("d.nu_des, ");
    sql.append("REPLACE(LTRIM(RTRIM(r.DE_ASU)), CHAR(10),' ') DE_ASU, ");
    sql.append("r.co_tip_doc_adm, ");
    sql.append("SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](d.co_dep_des), 1, 200) de_dep_des, ");
    sql.append("SUBSTRING(rr.nu_expediente,1,20) NU_EXPEDIENTE, ");
    sql.append("r.NU_ANN_EXP, ");
    sql.append("r.NU_SEC_EXP ");
    sql.append("from IDOSGD.TDTV_REMITOS R   ");
    sql.append("INNER JOIN\t IDOSGD.TDTV_DESTINOS D ON d.nu_ann = r.nu_ann AND d.nu_emi = r.nu_emi ");
    sql.append("INNER JOIN\t IDOSGD.TDTX_REMITOS_RESUMEN RR ON rr.nu_ann=r.nu_ann AND rr.nu_emi = r.nu_emi ");
    sql.append("where ");
    sql.append("r.es_eli = '0' ");
    sql.append("AND d.es_eli = '0' ");
    sql.append("AND r.nu_ann = ? ");
    sql.append("AND r.es_doc_emi NOT IN ('5', '9', '7') ");
    sql.append("and d.es_doc_rec <> '0' ");
    sql.append("and d.co_dep_des in ");
    sql.append("(select ? ");
    sql.append("union ");
    sql.append("  SELECT co_dep_ref ");
    sql.append("    FROM IDOSGD.tdtx_referencia ");
    sql.append("   WHERE co_dep_emi = ? ");
    sql.append("     AND ti_emi = 'D' ");
    sql.append("     AND es_ref = '1') ");
    sql.append("AND r.co_tip_doc_adm = ? ");
    if (pnuDoc != null && pnuDoc.trim().length() > 0) {
      pnuDoc = Utility.getInstancia().rellenaCerosIzquierda(pnuDoc, 6);
      sql.append("AND (R.NU_DOC_EMI = '").append(pnuDoc).append("' OR RR.NU_EXPEDIENTE LIKE '%").append(pnuDoc).append("') ");
    } 
   
    sql.append("UNION \n");
    sql.append("select ");
    sql.append("r.FE_EMI, d.FE_REC_DOC, ");
    sql.append("SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](r.co_tip_doc_adm), 1, 100) de_tip_doc_adm, ");
    sql.append("SUBSTRING(rr.nu_doc,1,50) NU_DOC, ");
    sql.append("CONVERT(VARCHAR(10), r.FE_EMI, 3) FE_EMI_CORTA, ");
    sql.append("CONVERT(VARCHAR(10), d.FE_REC_DOC, 3) FE_REC_DOC_CORTA, ");
    sql.append("r.nu_ann, ");
    sql.append("r.nu_emi, ");
    sql.append("d.nu_des, ");
    sql.append("REPLACE(LTRIM(RTRIM(r.DE_ASU)), CHAR(10),' ') DE_ASU, ");
    sql.append("r.co_tip_doc_adm, ");
    sql.append("SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](d.co_dep_des), 1, 200) de_dep_des, ");
    sql.append("SUBSTRING(rr.nu_expediente,1,20) NU_EXPEDIENTE, ");
    sql.append("r.NU_ANN_EXP, ");
    sql.append("r.NU_SEC_EXP ");
    sql.append("from IDOSGD.TDTV_REMITOS R   ");
    sql.append("INNER JOIN\t IDOSGD.TDTV_DESTINOS D ON d.nu_ann = r.nu_ann AND d.nu_emi = r.nu_emi ");
    sql.append("INNER JOIN\t IDOSGD.TDTX_REMITOS_RESUMEN RR ON rr.nu_ann=r.nu_ann AND rr.nu_emi = r.nu_emi ");
    sql.append("where ");
    sql.append("r.es_eli = '0' ");
    sql.append("AND d.es_eli = '0' ");
    sql.append("AND r.nu_ann = ? ");
    sql.append("AND r.es_doc_emi NOT IN ('5', '9', '7') ");
    sql.append("and d.es_doc_rec <> '0' ");
    sql.append("and (R.nu_ann+R.nu_emi) in (  ");
    
    sql.append("select  (REF.nu_ann_ref+REF.nu_emi_ref) as nurem from IDOSGD.TDTR_REFERENCIA REF where (REF.nu_ann+REF.nu_emi) in \n(select (R.nu_ann+R.nu_emi) as nurem \nfrom IDOSGD.TDTV_REMITOS R  \nINNER JOIN IDOSGD.TDTV_DESTINOS D ON d.nu_ann = r.nu_ann AND d.nu_emi = r.nu_emi \nINNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN RR ON rr.nu_ann=r.nu_ann AND rr.nu_emi = r.nu_emi \nwhere\nr.es_eli = '0'\nAND d.es_eli = '0'\nAND r.nu_ann = ?\n ");
    sql.append(" AND r.es_doc_emi NOT IN ('5', '9', '7') and d.es_doc_rec <> '0'  and d.co_dep_des = ?  ");


    if (pnuDoc != null && pnuDoc.trim().length() > 0) {
      pnuDoc = Utility.getInstancia().rellenaCerosIzquierda(pnuDoc, 6);
      sql.append("AND (R.NU_DOC_EMI = '").append(pnuDoc).append("' OR RR.NU_EXPEDIENTE LIKE '%").append(pnuDoc).append("%')\n");
    } 
    sql.append("))  ");
   
    if (nuDocT != null && nuDocT.trim().length() >= 3) {
    	
    	 sql.append("UNION \n");
    	    sql.append("select ");
    	    sql.append(" r.FE_EMI, d.FE_REC_DOC, ");
    	    sql.append("SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](r.co_tip_doc_adm), 1, 100) de_tip_doc_adm, ");
    	    sql.append("SUBSTRING(rr.nu_doc,1,50) NU_DOC, ");
    	    sql.append("CONVERT(VARCHAR(10), r.FE_EMI, 3) FE_EMI_CORTA, ");
    	    sql.append("CONVERT(VARCHAR(10), d.FE_REC_DOC, 3) FE_REC_DOC_CORTA, ");
    	    sql.append("r.nu_ann, ");
    	    sql.append("r.nu_emi, ");
    	    sql.append("d.nu_des, ");
    	    sql.append("REPLACE(LTRIM(RTRIM(r.DE_ASU)), CHAR(10),' ') DE_ASU, ");
    	    sql.append("r.co_tip_doc_adm, ");
    	    sql.append("SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](d.co_dep_des), 1, 200) de_dep_des, ");
    	    sql.append("SUBSTRING(rr.nu_expediente,1,20) NU_EXPEDIENTE, ");
    	    sql.append("r.NU_ANN_EXP, ");
    	    sql.append("r.NU_SEC_EXP ");
    	    sql.append("from IDOSGD.TDTV_REMITOS R   ");
    	    sql.append("INNER JOIN\t IDOSGD.TDTV_DESTINOS D ON d.nu_ann = r.nu_ann AND d.nu_emi = r.nu_emi ");
    	    sql.append("INNER JOIN\t IDOSGD.TDTX_REMITOS_RESUMEN RR ON rr.nu_ann=r.nu_ann AND rr.nu_emi = r.nu_emi ");
    	    sql.append("where ");
    	    sql.append("r.es_eli = '0' ");
    	    sql.append("AND d.es_eli = '0' ");
    	    sql.append("AND r.nu_ann = '").append(pannio).append("'");
    	    sql.append("AND r.es_doc_emi NOT IN ('5', '9', '7') ");
    	    sql.append("and d.es_doc_rec <> '0'  and  d.co_dep_des = '").append(pcoDepen).append("'");
    	 
    	      sql.append("AND (R.NU_DOC_EMI = '").append(pnuDoc).append("' OR RR.NU_EXPEDIENTE LIKE '%").append(pnuDoc).append("%'");
    	      sql.append(" or r.DE_DOC_SIG like '%").append(nuDocT).append("%' )");
    	
    }
    
    sql.append(" ) E ");
    
    sql.append(") A ");
    sql.append("WHERE ROWNUM < 202 ");
    sql.append("order by FE_EMI,FE_REC_DOC desc ");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoBean.class), new Object[] { pannio, pcoDepen, pcoDepen, ptiDoc, pannio, pannio, pcoDepen });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DestinatarioDocumentoEmiBean> _getLstDestintariotlbEmi(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    List<DestinatarioDocumentoEmiBean> list = new ArrayList<DestinatarioDocumentoEmiBean>();
    sql.append(" SELECT a.nu_ann, ");
    sql.append(" a.nu_emi, ");
    sql.append(" a.nu_des, ");
    sql.append(" a.co_loc_des co_local, ");
    sql.append(" SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_LOCAL](a.co_loc_des), 1, 100) de_local, ");
    sql.append(" a.co_dep_des co_dependencia, ");
    sql.append(" SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](a.co_dep_des), 1, 100) de_dependencia, ");
    sql.append(" a.co_emp_des co_empleado, ");
    sql.append(" SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](a.co_emp_des), 1, 100) de_empleado, ");
    sql.append(" a.co_mot co_tramite, ");
    sql.append(" SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_MOTIVO](a.co_mot), 1, 100) de_tramite, ");
    sql.append(" a.co_pri co_prioridad, ");
    sql.append(" a.de_pro de_indicaciones ");
    sql.append(" FROM IDOSGD.tdtv_destinos a ");
    sql.append(" WHERE a.nu_ann = ? ");
    sql.append(" AND a.nu_emi = ? ");
    sql.append(" AND a.TI_DES = '01' ");
    sql.append(" AND a.ES_ELI='0' ");
    sql.append(" AND a.NU_EMI_REF is null ");
    sql.append(" ORDER BY 3 ");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinatarioDocumentoEmiBean.class), new Object[] { pnuAnn, pnuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DestinatarioDocumentoEmiBean> getLstDestintariotlbEmi(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    List<DestinatarioDocumentoEmiBean> list = new ArrayList<DestinatarioDocumentoEmiBean>();
    sql.append("select a.nu_ann, ");
    sql.append("a.nu_emi, ");
    sql.append("a.nu_des, ");
    sql.append("a.co_loc_des co_local, ");
    sql.append("CASE ");
    sql.append("WHEN a.co_loc_des IS NULL THEN NULL ");
    sql.append("ELSE SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_LOCAL](a.co_loc_des), 1, 100) ");
    sql.append("END de_local, ");
    sql.append("a.co_dep_des co_dependencia, ");
    sql.append("CASE ");
    sql.append("WHEN a.co_dep_des IS NULL THEN NULL ");
    sql.append("ELSE SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](a.co_dep_des), 1, 100) ");
    sql.append("END de_dependencia, ");
    sql.append("a.co_emp_des co_empleado, ");
    sql.append("CASE ");
    sql.append("WHEN a.co_emp_des IS NULL THEN NULL ");
    sql.append("ELSE SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](a.co_emp_des), 1, 100) ");
    sql.append("END de_empleado, ");
    sql.append("a.co_mot co_tramite, ");
    sql.append("CASE ");
    sql.append("WHEN a.co_mot IS NULL THEN NULL ");
    sql.append("ELSE SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_MOTIVO](a.co_mot), 1, 100) ");
    sql.append("END de_tramite, ");
    sql.append("a.co_pri co_prioridad, ");
    sql.append("a.de_pro de_indicaciones, ");
    sql.append("a.NU_RUC_DES nu_ruc, ");
    sql.append("CASE ");
    sql.append("WHEN a.NU_RUC_DES IS NULL THEN NULL ");
    sql.append("ELSE SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_PROVEEDOR](a.NU_RUC_DES), 1, 100) ");
    sql.append("END de_proveedor, ");
    sql.append("a.NU_DNI_DES nu_dni, ");
    sql.append("CASE ");
    sql.append("WHEN a.NU_DNI_DES IS NULL THEN NULL ");
    sql.append("ELSE SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](a.NU_DNI_DES), 1, 100) ");
    sql.append("END de_ciudadano, ");
    sql.append("a.CO_OTR_ORI_DES co_otro_origen, ");
    sql.append("CASE ");
    sql.append("WHEN a.CO_OTR_ORI_DES IS NULL THEN NULL ");
    sql.append("ELSE (SELECT ISNULL(C.DE_APE_PAT_OTR,'') + ' ' + ISNULL(C.DE_APE_MAT_OTR,'') + ', ' + ISNULL(C.DE_NOM_OTR,'') + ' - ' + ");
    sql.append("\t\t\t ISNULL(C.DE_RAZ_SOC_OTR,'') + '##' + ISNULL(B.CELE_DESELE,' ') + '##' + ");
    sql.append("\t\t\t ISNULL(C.NU_DOC_OTR_ORI,'') ");
    sql.append("\t  FROM IDOSGD.TDTR_OTRO_ORIGEN C ");
    sql.append("\t\t   LEFT OUTER JOIN ( ");
    sql.append("\t\t\t   SELECT CELE_CODELE, CELE_DESELE ");
    sql.append("\t\t\t   FROM IDOSGD.SI_ELEMENTO ");
    sql.append("\t\t\t   WHERE CTAB_CODTAB = 'TIP_DOC_IDENT') B ");
    sql.append("\t\t   ON C.CO_TIP_OTR_ORI = B.CELE_CODELE ");
    sql.append("\t  WHERE C.CO_OTR_ORI = a.CO_OTR_ORI_DES ");
    sql.append("\t ) ");
    sql.append("END de_otro_origen_full, ");
    sql.append("a.ti_des co_tipo_destino, ");
    sql.append("ISNULL(a.ES_ENV_POR_TRA, '0') ENV_MESA_PARTES, ");
    sql.append("CDIR_REMITE,CEXP_CORREOE,CTELEFONO,CCOD_DPTO,CCOD_PROV,CCOD_DIST,\n                rtrim(ltrim(UB.NODEP))+' '+rtrim(ltrim(UB.NOPRV))+' '+rtrim(ltrim(UB.NODIS)) ubigeo \n                ,A.REMI_TI_EMI , CASE A.REMI_TI_EMI WHEN '03' THEN IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL (A.REMI_NU_DNI_EMI)  \n                 WHEN '04' THEN IDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN (A.REMI_CO_OTR_ORI_EMI) END   nombres,\n               CASE A.REMI_TI_EMI WHEN '03' THEN REMI_NU_DNI_EMI WHEN '04' THEN REMI_CO_OTR_ORI_EMI END   REMI_NU_DNI_EMI  \n                   ,DE_CARGO AS cargo\t  ");




    
    sql.append("FROM IDOSGD.tdtv_destinos a ");
    sql.append("LEFT JOIN ( SELECT C.CO_OTR_ORI,ISNULL(C.DE_APE_PAT_OTR,'')+' '+ISNULL(C.DE_APE_MAT_OTR,'')+', '+ISNULL(C.DE_NOM_OTR,'') + ' - ' +\n                     ISNULL(C.DE_RAZ_SOC_OTR,'') +'##'+  ISNULL(B.CELE_DESELE,'   ') +'##'+  ISNULL(C.NU_DOC_OTR_ORI,'')  de_otro_origen_full\n                  FROM IDOSGD.TDTR_OTRO_ORIGEN C LEFT JOIN  (\n                  SELECT CELE_CODELE, CELE_DESELE  FROM IDOSGD.SI_ELEMENTO  WHERE CTAB_CODTAB ='TIP_DOC_IDENT') B\n                  ON C.CO_TIP_OTR_ORI = B.CELE_CODELE ) OTRDES ON OTRDES.CO_OTR_ORI=a.CO_OTR_ORI_DES \n  ");




    
    sql.append("LEFT JOIN IDOSGD.IDTUBIAS UB ON UB.UBDEP=a.CCOD_DPTO AND UB.UBPRV=a.CCOD_PROV AND UB.UBDIS=a.CCOD_DIST  ");
    sql.append("where nu_ann = ? ");
    sql.append("and nu_emi = ? ");
    sql.append("AND ES_ELI='0' ");
    sql.append("AND NU_EMI_REF is null ");
    sql.append("order by 3 ");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinatarioDocumentoEmiBean.class), new Object[] { pnuAnn, pnuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<ReferenciaBean> getLstDocumReferenciatblEmi(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    List<ReferenciaBean> list = new ArrayList<ReferenciaBean>();
    sql.append("SELECT ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](a.co_tip_doc_adm) de_tipo_doc, ");
    sql.append("CASE a.ti_emi ");
    sql.append("\t WHEN '01' THEN a.nu_doc_emi + '-' + a.nu_ann + '-' + a.de_doc_sig ");
    sql.append("\t WHEN '05' THEN a.nu_doc_emi + '-' + a.nu_ann + '-' + a.de_doc_sig ");
    sql.append("\t ELSE a.de_doc_sig ");
    sql.append("END li_nu_doc, ");
    sql.append("SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](a.co_dep_emi), 1, 100) de_dep_emi, ");
    sql.append("CONVERT(VARCHAR(10), a.fe_emi, 3) fe_emi_corta, ");
    sql.append("b.nu_ann, ");
    sql.append("b.nu_emi, ");
    sql.append("ISNULL(LTRIM(RTRIM(CAST(b.nu_des AS varchar))),'N') nu_des, ");
    sql.append("b.nu_ann_ref, ");
    sql.append("b.nu_emi_ref, ");
    sql.append("ISNULL(LTRIM(RTRIM(CAST(b.nu_des_ref AS varchar))),'N') nu_des_ref, ");
    sql.append("b.co_ref, ");
    sql.append("CASE ISNULL(LTRIM(RTRIM(CAST(b.nu_des_ref AS varchar))),'N') ");
    sql.append("\t WHEN 'N' THEN 'emi' ");
    sql.append("\t ELSE 'rec' ");
    sql.append("END tip_doc_ref, ");
    sql.append("a.co_tip_doc_adm, ");
    sql.append("'BD' accion_bd ");
    sql.append("FROM IDOSGD.tdtv_remitos a ");
    sql.append("INNER JOIN \t IDOSGD.TDTR_REFERENCIA b ON a.nu_ann = b.nu_ann_ref AND a.nu_emi = b.nu_emi_ref ");
    sql.append("WHERE  ");
    sql.append("  b.nu_ann = ? ");
    sql.append("AND b.nu_emi = ? ");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ReferenciaBean.class), new Object[] { pnuAnn, pnuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String getTipoDestinatarioEmi(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    String result = null;
    sql.append("SELECT MAX(ti_des) ");
    sql.append("FROM IDOSGD.tdtv_destinos ");
    sql.append("WHERE nu_emi = ? ");
    sql.append("AND nu_ann = ? ");
    sql.append("AND ES_ELI = '0' ");
    
    try {
      result = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { pnuEmi, pnuAnn });
    } catch (Exception e) {
      result = "1";
      e.printStackTrace();
    } 
    return result;
  }


  
  public String updDocumentoEmiBean(String nuAnn, String nuEmi, DocumentoEmiBean documentoEmiBean, ExpedienteBean expedienteBean, RemitenteEmiBean remitenteEmiBean, String pcoUserMod) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("update IDOSGD.tdtv_remitos A \nset A.CO_USE_MOD=?,");
    
    if (documentoEmiBean != null)   {

   
      sqlUpd.append("A.DE_ASU=?\n,A.NU_DIA_ATE=?\n,A.CO_TIP_DOC_ADM=?\n,A.ES_DOC_EMI=?\n,A.NU_DOC_EMI=?\n,A.DE_DOC_SIG=?\n,A.NU_COR_DOC= CASE \nWHEN ? IS NULL THEN ? ELSE (SELECT ISNULL(MAX(nu_cor_doc), 0) + 1\nFROM IDOSGD.tdtv_remitos\nWHERE nu_ann = ?\nAND co_dep_emi = ?\nAND co_tip_doc_adm = ?\nAND ti_emi = ?) END,\n");
    }


    
    if (expedienteBean != null) {
      sqlUpd.append("A.NU_SEC_EXP='" + expedienteBean.getNuSecExp() + "',\n");
    }
    if (remitenteEmiBean != null) {
      sqlUpd.append("A.CO_LOC_EMI='" + remitenteEmiBean.getCoLocal() + "'\n,A.CO_DEP_EMI='" + remitenteEmiBean
          .getCoDependencia() + "'\n,A.CO_EMP_EMI='" + remitenteEmiBean
          .getCoEmpFirma() + "',\n");
    }
    sqlUpd.append("A.FE_USE_MOD=CURRENT_TIMESTAMP where\nA.NU_ANN=? and\nA.NU_EMI=?");



    
    try {
      if (documentoEmiBean != null) {
        this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { pcoUserMod, documentoEmiBean.getDeAsu(), documentoEmiBean.getNuDiaAte(), documentoEmiBean
              .getCoTipDocAdm(), documentoEmiBean.getEsDocEmi(), documentoEmiBean.getNuDocEmi(), documentoEmiBean.getDeDocSig(), documentoEmiBean
              .getNuDocEmi(), documentoEmiBean.getNuCorDoc(), nuAnn, documentoEmiBean.getCoDepEmi(), documentoEmiBean.getCoTipDocAdm(), documentoEmiBean
              .getTiEmi(), nuAnn, nuEmi });
      } else {
        this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { pcoUserMod, nuAnn, nuEmi });
      } 
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    
    return vReturn;
  }


  
  public String insDocumentoEmiBean(DocumentoEmiBean documentoEmiBean, ExpedienteBean expedienteBean, RemitenteEmiBean remitenteEmiBean) {
    String vReturn = "NO_OK";
    documentoEmiBean.setTiEmi("01");
    StringBuffer sqlUpd = new StringBuffer();
    StringBuffer sqlQry = new StringBuffer();
    sqlQry.append("SELECT ISNULL(MAX(nu_cor_emi), 0) + 1 ");
    sqlQry.append("FROM IDOSGD.tdtv_remitos ");
    sqlQry.append("WHERE nu_ann = ? ");
    sqlQry.append("AND co_dep_emi = ? ");
    sqlQry.append("AND ti_emi = '01' ");
    
    sqlUpd.append("INSERT INTO IDOSGD.tdtv_remitos(\nNU_ANN,\nNU_EMI,\nNU_COR_EMI,\nCO_LOC_EMI,\nCO_DEP_EMI,\nCO_EMP_EMI,\nCO_EMP_RES, \nTI_EMI,\nFE_EMI,\nCO_GRU,\nDE_ASU, \nES_DOC_EMI, \nNU_DIA_ATE, \nTI_CAP, \nCO_TIP_DOC_ADM, \nNU_COR_DOC, \nDE_DOC_SIG, \nNU_ANN_EXP, \nNU_SEC_EXP, \nNU_DET_EXP, \nES_ELI,\nNU_DOC_EMI,\nCO_USE_CRE,\nFE_USE_CRE,\nCO_USE_MOD,\nFE_USE_MOD,OBS_DOC,COD_VER_EXT)\nVALUES(?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,'1',?,?,?,'03',?,\n?,?,?,?,1,\n'0',?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,?,?)");


    
    try {
      SimpleJdbcCall simpleJdbcCall = (new SimpleJdbcCall(this.dataSource)).withProcedureName("[IDOSGD].[PK_SGD_UTILS_SEC_REMITOS_NU_EMI]").withoutProcedureColumnMetaDataAccess().declareParameters(new SqlParameter[] { (SqlParameter)new SqlOutParameter("COD_NU_EMI", 4) });
      
      Map out = simpleJdbcCall.execute(new Object[0]);
      int codigo = ((Integer)out.get("COD_NU_EMI")).intValue();
      String snuEmi = String.format("%010d", new Object[] { Integer.valueOf(codigo) });


      
      String snuCorEmi = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { documentoEmiBean.getNuAnn(), documentoEmiBean.getCoDepEmi() });
      documentoEmiBean.setNuEmi(snuEmi);
      documentoEmiBean.setNuCorEmi(snuCorEmi);
      
      documentoEmiBean.setCoVerExt(AleatorioCodVerificacion.generaCodVerificacion());
      
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { documentoEmiBean.getNuAnn(), snuEmi, (snuCorEmi == null) ? null : Integer.valueOf(Integer.parseInt(snuCorEmi)), documentoEmiBean
            .getCoLocEmi(), documentoEmiBean.getCoDepEmi(), documentoEmiBean.getCoEmpEmi(), documentoEmiBean.getCoEmpRes(), documentoEmiBean
            .getTiEmi(), documentoEmiBean.getDeAsu(), documentoEmiBean.getEsDocEmi(), (documentoEmiBean.getNuDiaAte() == null) ? null : Integer.valueOf(Integer.parseInt(documentoEmiBean.getNuDiaAte())), documentoEmiBean.getCoTipDocAdm(), 
            (documentoEmiBean.getNuCorDoc() == null) ? null : Integer.valueOf(Integer.parseInt(documentoEmiBean.getNuCorDoc())), documentoEmiBean.getDeDocSig(), documentoEmiBean.getNuAnnExp(), documentoEmiBean.getNuSecExp(), 
            (documentoEmiBean.getNuDocEmi() == null) ? null : Integer.valueOf(Integer.parseInt(documentoEmiBean.getNuDocEmi())), documentoEmiBean.getCoUseMod(), documentoEmiBean.getCoUseMod(), documentoEmiBean.getDeObsDoc(), documentoEmiBean.getCoVerExt() });
      
      vReturn = "OK";
    } catch (DuplicateKeyException con) {
      vReturn = "Numero de Documento Duplicado.";
      con.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } 

    
    return vReturn;
  }


  
  public String insDestinatarioDocumentoEmi(String nuAnn, String nuEmi, DestinatarioDocumentoEmiBean destinatarioDocumentoEmiBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    StringBuffer sqlQry1 = new StringBuffer();
    StringBuffer sqlUpdUbigeo = new StringBuffer();
    sqlQry1.append("SELECT CELE_CODELE FROM IDOSGD.SI_ELEMENTO WHERE CTAB_CODTAB='UPD_PARAMT_UBIGEO'");
    sqlUpd.append("INSERT INTO IDOSGD.TDTV_DESTINOS(\nNU_ANN,\nNU_EMI,\nNU_DES,\nCO_LOC_DES,\nCO_DEP_DES,\nTI_DES,\nCO_EMP_DES,\nCO_PRI,\nDE_PRO,\nCO_MOT, \nCO_OTR_ORI_DES, \nNU_DNI_DES, \nNU_RUC_DES, \nES_ELI,\nFE_USE_CRE,\nFE_USE_MOD,\nCO_USE_MOD,\nCO_USE_CRE,\nES_DOC_REC,\nES_ENV_POR_TRA,CDIR_REMITE,CEXP_CORREOE,CTELEFONO,CCOD_DPTO,CCOD_PROV,CCOD_DIST,REMI_TI_EMI,REMI_NU_DNI_EMI,REMI_CO_OTR_ORI_EMI,DE_CARGO)\nVALUES(?,?,(select ISNULL(MAX(a.nu_des) + 1, 1) FROM IDOSGD.tdtv_destinos a where \nA.NU_ANN=? and\nA.NU_EMI=?),?,?,?,?,?,?,?,?,?,?,'0',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,?,?,'0',?,?,?,?,?,?,?,?,?,?,?)");

 
    try {
      String[] ubigeo = null;
      String dep = "";
      String pro = "";
      String dis = "";
      if (destinatarioDocumentoEmiBean.getCcodDpto() != null && !"".equals(destinatarioDocumentoEmiBean.getCcodDpto())) {
        ubigeo = destinatarioDocumentoEmiBean.getCcodDpto().split(",");
      }
      if (ubigeo != null && ubigeo.length > 0) {
        dep = ubigeo[0]; pro = ubigeo[1]; dis = ubigeo[2];
      } 
      if ("03".equals(destinatarioDocumentoEmiBean.getRemiTiEmi())) {
        destinatarioDocumentoEmiBean.setRemiCoOtrOriEmi("");
      }
      if ("04".equals(destinatarioDocumentoEmiBean.getRemiTiEmi())) {
        destinatarioDocumentoEmiBean.setRemiCoOtrOriEmi(destinatarioDocumentoEmiBean.getRemiNuDniEmi());
        destinatarioDocumentoEmiBean.setRemiNuDniEmi("");
      } 
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { nuAnn, nuEmi, nuAnn, nuEmi, destinatarioDocumentoEmiBean.getCoLocal(), destinatarioDocumentoEmiBean.getCoDependencia(), destinatarioDocumentoEmiBean.getCoTipoDestino(), destinatarioDocumentoEmiBean
            .getCoEmpleado(), destinatarioDocumentoEmiBean.getCoPrioridad(), destinatarioDocumentoEmiBean.getDeIndicaciones(), destinatarioDocumentoEmiBean.getCoTramite(), destinatarioDocumentoEmiBean
            .getCoOtroOrigen(), destinatarioDocumentoEmiBean.getNuDni(), destinatarioDocumentoEmiBean.getNuRuc(), destinatarioDocumentoEmiBean
            .getCoUseCre(), destinatarioDocumentoEmiBean.getCoUseCre(), destinatarioDocumentoEmiBean.getEnvMesaPartes(), destinatarioDocumentoEmiBean
            .getCdirRemite(), destinatarioDocumentoEmiBean.getCexpCorreoe(), destinatarioDocumentoEmiBean.getcTelefono(), dep, pro, dis, destinatarioDocumentoEmiBean
            .getRemiTiEmi(), destinatarioDocumentoEmiBean.getRemiNuDniEmi(), destinatarioDocumentoEmiBean.getRemiCoOtrOriEmi(), destinatarioDocumentoEmiBean.getCargo() });
      
      String isUpdateUbigeo = (String)this.jdbcTemplate.queryForObject(sqlQry1.toString(), String.class);
      if ("1".equals(isUpdateUbigeo)) {
        if ("02".equals(destinatarioDocumentoEmiBean.getCoTipoDestino())) {
          sqlUpdUbigeo.append("UPDATE  IDOSGD.lg_pro_proveedor SET cpro_domicil=?,cpro_ubigeo=?,cpro_telefo=?,cpro_email=?,cubi_coddep=?,\n    cubi_codpro=?,cubi_coddis=?  WHERE cpro_ruc=?");
          
          this.jdbcTemplate.update(sqlUpdUbigeo.toString(), new Object[] { destinatarioDocumentoEmiBean.getCdirRemite(), dep + pro + dis, destinatarioDocumentoEmiBean
                .getcTelefono(), destinatarioDocumentoEmiBean.getCexpCorreoe(), dep, pro, dis, destinatarioDocumentoEmiBean.getNuRuc() });
        } 
        if ("03".equals(destinatarioDocumentoEmiBean.getCoTipoDestino())) {
          sqlUpdUbigeo.append(" UPDATE  IDOSGD.TDTX_ANI_SIMIL  SET  DEDOMICIL=?,DETELEFO=?, DEEMAIL=?,UBDEP=?,UBPRV=?, UBDIS=?  WHERE  NULEM=? ");
          this.jdbcTemplate.update(sqlUpdUbigeo.toString(), new Object[] { destinatarioDocumentoEmiBean.getCdirRemite(), destinatarioDocumentoEmiBean
                .getcTelefono(), destinatarioDocumentoEmiBean.getCexpCorreoe(), dep, pro, dis, destinatarioDocumentoEmiBean.getNuDni() });
        } 
        if ("04".equals(destinatarioDocumentoEmiBean.getCoTipoDestino())) {
          sqlUpdUbigeo.append("UPDATE IDOSGD.TDTR_OTRO_ORIGEN SET  DE_DIR_OTRO_ORI=?,DE_TELEFO=?,DE_EMAIL=?,UB_DEP=?, UB_PRO=?, UB_DIS=?   WHERE  CO_OTR_ORI=?    ");
          this.jdbcTemplate.update(sqlUpdUbigeo.toString(), new Object[] { destinatarioDocumentoEmiBean.getCdirRemite(), destinatarioDocumentoEmiBean
                .getcTelefono(), destinatarioDocumentoEmiBean.getCexpCorreoe(), dep, pro, dis, destinatarioDocumentoEmiBean.getCoOtroOrigen() });
        } 
      } 

      
      vReturn = "OK";
    } catch (DuplicateKeyException con) {
      vReturn = "Datos Duplicado Destino.";
    }
    catch (Exception e) {
      e.printStackTrace();
    } 

    
    return vReturn;
  }


  
  public String updDestinatarioDocumentoEmi(String nuAnn, String nuEmi, DestinatarioDocumentoEmiBean destinatarioDocumentoEmiBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    
    sqlUpd.append("update IDOSGD.TDTV_DESTINOS \nset CO_LOC_DES=?\n,CO_DEP_DES=?\n,CO_EMP_DES=?\n,CO_PRI=?\n,DE_PRO=?\n,CO_MOT=?\n,CO_OTR_ORI_DES=? \n,NU_DNI_DES=? \n,NU_RUC_DES=? \n,FE_USE_MOD=CURRENT_TIMESTAMP\n,CO_USE_MOD=?\n,ES_ENV_POR_TRA=?\n,CDIR_REMITE=?\n,CEXP_CORREOE=?\n,CTELEFONO=?\n,CCOD_DPTO=?\n,CCOD_PROV=?\n,CCOD_DIST=?\n,REMI_TI_EMI=?\n,REMI_NU_DNI_EMI=?\n,REMI_CO_OTR_ORI_EMI=? \n,DE_CARGO=? \nwhere\nNU_ANN=? and\nNU_EMI=? and\nNU_DES=?");

  
    try {
      String[] ubigeo = null;
      String dep = "";
      String pro = "";
      String dis = "";
      if (destinatarioDocumentoEmiBean.getCcodDpto() != null && !"".equals(destinatarioDocumentoEmiBean.getCcodDpto())) {
        ubigeo = destinatarioDocumentoEmiBean.getCcodDpto().split(",");
      }
      if (ubigeo != null && ubigeo.length > 0) {
        dep = ubigeo[0]; pro = ubigeo[1]; dis = ubigeo[2];
      } 
      if ("03".equals(destinatarioDocumentoEmiBean.getRemiTiEmi())) {
        destinatarioDocumentoEmiBean.setRemiCoOtrOriEmi("");
      }
      if ("04".equals(destinatarioDocumentoEmiBean.getRemiTiEmi())) {
        destinatarioDocumentoEmiBean.setRemiCoOtrOriEmi(destinatarioDocumentoEmiBean.getRemiNuDniEmi());
        destinatarioDocumentoEmiBean.setRemiNuDniEmi("");
      } 
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { destinatarioDocumentoEmiBean.getCoLocal(), destinatarioDocumentoEmiBean.getCoDependencia(), destinatarioDocumentoEmiBean
            .getCoEmpleado(), destinatarioDocumentoEmiBean.getCoPrioridad(), destinatarioDocumentoEmiBean.getDeIndicaciones(), destinatarioDocumentoEmiBean.getCoTramite(), destinatarioDocumentoEmiBean
            .getCoOtroOrigen(), destinatarioDocumentoEmiBean.getNuDni(), destinatarioDocumentoEmiBean.getNuRuc(), destinatarioDocumentoEmiBean
            .getCoUseCre(), destinatarioDocumentoEmiBean.getEnvMesaPartes(), destinatarioDocumentoEmiBean
            .getCdirRemite(), destinatarioDocumentoEmiBean.getCexpCorreoe(), destinatarioDocumentoEmiBean.getcTelefono(), dep, pro, dis, destinatarioDocumentoEmiBean
            .getRemiTiEmi(), destinatarioDocumentoEmiBean.getRemiNuDniEmi(), destinatarioDocumentoEmiBean.getRemiCoOtrOriEmi(), destinatarioDocumentoEmiBean.getCargo(), nuAnn, nuEmi, destinatarioDocumentoEmiBean
            .getNuDes() });
      vReturn = "OK";
    } catch (DuplicateKeyException con) {
      vReturn = "Datos Duplicado Destino.";
    }
    catch (Exception e) {
      e.printStackTrace();
    } 

    
    return vReturn;
  }


  
  public String delDestinatarioDocumentoEmi(String nuAnn, String nuEmi, DestinatarioDocumentoEmiBean destinatarioDocumentoEmiBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlDel = new StringBuffer();
    sqlDel.append("delete from IDOSGD.TDTV_DESTINOS\nWHERE NU_ANN = ?\nAND NU_EMI = ?\nand NU_DES=?");

    try {
      this.jdbcTemplate.update(sqlDel.toString(), new Object[] { nuAnn, nuEmi, destinatarioDocumentoEmiBean.getNuDes() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 

    
    return vReturn;
  }


  
  public String insReferenciaDocumentoEmi(String nuAnn, String nuEmi, ReferenciaEmiDocBean referenciaEmiDocBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("INSERT INTO IDOSGD.tdtr_referencia( ");
    sqlUpd.append("NU_ANN, ");
    sqlUpd.append("NU_EMI, ");
    sqlUpd.append("CO_REF, ");
    sqlUpd.append("NU_ANN_REF, ");
    sqlUpd.append("NU_EMI_REF, ");
    sqlUpd.append("NU_DES_REF, ");
    sqlUpd.append("CO_USE_CRE, ");
    sqlUpd.append("FE_USE_CRE) ");
    sqlUpd.append("VALUES(?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) ");


    
    try {
      SimpleJdbcCall simpleJdbcCall = (new SimpleJdbcCall(this.dataSource)).withProcedureName("[IDOSGD].[PK_SGD_UTILS_SEC_REFERENCIA_CO_REF]").withoutProcedureColumnMetaDataAccess().declareParameters(new SqlParameter[] { (SqlParameter)new SqlOutParameter("COD_NU_ANN_REF", 4) });
      
      Map out = simpleJdbcCall.execute(new Object[0]);
      int codigo = ((Integer)out.get("COD_NU_ANN_REF")).intValue();
      String cod_num_ann_ref = String.format("%010d", new Object[] { Integer.valueOf(codigo) });
      
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { nuAnn, nuEmi, cod_num_ann_ref, referenciaEmiDocBean.getNuAnn(), referenciaEmiDocBean.getNuEmi(), 
            referenciaEmiDocBean.getNuDes().equals("") ? null : referenciaEmiDocBean.getNuDes(), referenciaEmiDocBean.getCoUseCre() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 

    
    return vReturn;
  }


  
  public String updReferenciaDocumentoEmi(String nuAnn, String nuEmi, ReferenciaEmiDocBean referenciaEmiDocBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("update IDOSGD.tdtr_referencia \nset NU_ANN_REF = ?,\nNU_EMI_REF = ?,\nNU_DES_REF = ?\nWHERE NU_ANN = ? AND NU_EMI = ? \nAND CO_REF = ?");
 
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { referenciaEmiDocBean.getNuAnn(), referenciaEmiDocBean.getNuEmi(), referenciaEmiDocBean
            .getNuDes(), nuAnn, nuEmi, referenciaEmiDocBean.getCoRef() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }


  
  public String delReferenciaDocumentoEmi(String nuAnn, String nuEmi, ReferenciaEmiDocBean referenciaEmiDocBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlIns = new StringBuffer();
    sqlIns.append("DELETE FROM IDOSGD.tdtr_referencia\nWHERE NU_ANN = ? AND NU_EMI = ? \nAND NU_ANN_REF = ? AND NU_EMI_REF = ?");


    
    try {
      this.jdbcTemplate.update(sqlIns.toString(), new Object[] { nuAnn, nuEmi, referenciaEmiDocBean.getNuAnn(), referenciaEmiDocBean.getNuEmi() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }

  
  public DocumentoEmiBean getDocumentoEmiAdmNew(String sEstadoDocEmi, String codDependencia) {
    StringBuffer sql = new StringBuffer();
    sql.append("select [IDOSGD].[PK_SGD_DESCRIPCION_DE_SIGLA](?) de_doc_sig, ");
    sql.append("CO_EMPLEADO co_emp_emi, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](CO_EMPLEADO) de_emp_emi, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS](?, 'TDTV_REMITOS') de_es_doc_emi, ");
    sql.append("'0' existe_doc, ");
    sql.append("'0' existe_anexo, ");
    sql.append("? co_es_doc_emi ");
    sql.append("from IDOSGD.rhtm_dependencia ");
    sql.append("where CO_DEPENDENCIA = ? ");
    
    DocumentoEmiBean documentoEmiBean = new DocumentoEmiBean();
    try {
      documentoEmiBean = (DocumentoEmiBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class), new Object[] { codDependencia, sEstadoDocEmi, sEstadoDocEmi, codDependencia });
    }
    catch (EmptyResultDataAccessException e) {
      documentoEmiBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return documentoEmiBean;
  }


  
  public String insExpedienteBean(ExpedienteBean expedienteBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlQry1 = new StringBuffer();
    sqlQry1.append("select RIGHT(REPLICATE('0', 7) + rtrim(ltrim(CAST((ISNULL(max(NU_CORR_EXP), 0) + 1) AS varchar))), 7) nuCorrExp, ");
    sqlQry1.append("SUBSTRING((SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_SIGLA_CORTA_EXP(?)), 1, 6) deSiglaCorta ");
    sqlQry1.append("from IDOSGD.tdtc_expediente ");
    sqlQry1.append("where nu_ann_exp = ? ");
    sqlQry1.append("and co_dep_exp = ? ");
    sqlQry1.append("and co_gru = '1' ");
    
    StringBuffer sqlQry2 = new StringBuffer();
    sqlQry2.append("select RIGHT(REPLICATE('0', 10) + CAST((ISNULL(max(nu_sec_exp), 0) + 1) AS VARCHAR), 10) ");
    sqlQry2.append("from IDOSGD.tdtc_expediente ");
    sqlQry2.append("where nu_ann_exp = ? ");
    
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("INSERT INTO IDOSGD.TDTC_EXPEDIENTE(\nnu_ann_exp,\nnu_sec_exp,\nfe_exp,\nco_proceso,\nde_detalle,\nco_dep_exp,\nco_gru,\nnu_corr_exp,\nnu_expediente,\nnu_folios,\nnu_plazo,\nus_crea_audi,\nfe_crea_audi,\nus_modi_audi,\nfe_modi_audi,\nes_estado)\nVALUES(?, ?, (SELECT CONVERT(DATETIME, ?, 103)),?,?,?,'1',?,?,?,?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,'0')");


    
    try {
      Map mapResult = this.jdbcTemplate.queryForMap(sqlQry1.toString(), new Object[] { expedienteBean.getCoDepExp(), expedienteBean.getNuAnnExp(), expedienteBean.getCoDepExp() });
      String snuSecExp = (String)this.jdbcTemplate.queryForObject(sqlQry2.toString(), String.class, new Object[] { expedienteBean.getNuAnnExp() });
      expedienteBean.setNuSecExp(snuSecExp);
      String snuCorrExp = String.valueOf(mapResult.get("nuCorrExp"));
      String sdeSiglaCorta = String.valueOf(mapResult.get("deSiglaCorta"));
      
      expedienteBean.setNuExpediente(Utilidades.AjustaCampoIzquierda(sdeSiglaCorta, 6, "0") + expedienteBean.getNuAnnExp() + snuCorrExp);
      
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { expedienteBean.getNuAnnExp(), snuSecExp, expedienteBean.getFeExp(), expedienteBean
            .getCoProceso(), expedienteBean.getDeProceso(), expedienteBean.getCoDepExp(), snuCorrExp, expedienteBean.getNuExpediente(), expedienteBean
            .getNuFolios(), expedienteBean.getNuPlazo(), expedienteBean.getUsCreaAudi(), expedienteBean.getUsCreaAudi() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }

  
  public String verificarDocumentoLeido(String pnuAnn, String pnuEmi) {
    String vReturn = "NO_OK";
    StringBuffer sqlQry = new StringBuffer();
    sqlQry.append("SELECT ISNULL(MAX(es_doc_rec),'0') ");
    sqlQry.append("FROM IDOSGD.tdtv_destinos ");
    sqlQry.append("WHERE nu_ann = ? ");
    sqlQry.append("AND nu_emi = ? ");
    sqlQry.append("AND es_eli = '0' ");
    
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { pnuAnn, pnuEmi });
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    return vReturn;
  }

  
  public String anularDocumentoEmiAdm(DocumentoEmiBean documentoEmiBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlQry = new StringBuffer();
    sqlQry.append("SELECT ISNULL(MAX(nu_cor_doc), 1) + 1 ");
    sqlQry.append("FROM IDOSGD.tdtv_remitos ");
    sqlQry.append("WHERE nu_ann         = ? ");
    sqlQry.append("AND ti_emi         = ? ");
    sqlQry.append("AND co_dep_emi     = ? ");
    sqlQry.append("AND co_tip_doc_adm = ? ");
    sqlQry.append("AND ((? IS NOT NULL AND nu_doc_emi = ?) ");
    sqlQry.append("OR ? IS NULL) ");
    
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("UPDATE IDOSGD.tdtv_remitos ");
    sqlUpd.append("set es_doc_emi = ?, ");
    sqlUpd.append("nu_cor_doc = ?, ");
    sqlUpd.append("fe_use_mod=CURRENT_TIMESTAMP, ");
    sqlUpd.append("co_use_mod=? ");
    sqlUpd.append("WHERE nu_ann = ? ");
    sqlUpd.append("AND nu_emi = ? ");
    sqlUpd.append("AND es_eli = '0' ");
    
    try {
      String snuCorDoc = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { documentoEmiBean.getNuAnn(), documentoEmiBean.getTiEmi(), documentoEmiBean
            .getCoDepEmi(), documentoEmiBean.getCoTipDocAdm(), documentoEmiBean.getNuDocEmi(), documentoEmiBean.getNuDocEmi(), documentoEmiBean.getNuDocEmi() });
      
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { documentoEmiBean.getEsDocEmi(), snuCorDoc, documentoEmiBean.getCoUseMod(), documentoEmiBean
            .getNuAnn(), documentoEmiBean.getNuEmi() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    return vReturn;
  }

  
  public List<DestinatarioDocumentoEmiBean> getLstDestinatarioGrupo(String pcoGrupo, String pcoTipDoc) {
    StringBuffer sql = new StringBuffer();
    List<DestinatarioDocumentoEmiBean> list = new ArrayList<DestinatarioDocumentoEmiBean>();
    
    sql.append("SELECT a.co_dep co_dependencia, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](a.co_dep) de_dependencia, ");
    sql.append("ISNULL(a.co_emp, b.co_empleado) co_empleado, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](ISNULL(a.co_emp, b.co_empleado)) de_empleado, ");
    sql.append("c.co_loc co_local, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_LOCAL](c.co_loc) de_local,'1' co_prioridad, ");
    sql.append("? co_tramite_first, ");
    sql.append("? de_tramite_first, ");
    sql.append("? co_tramite_next, ");
    sql.append("? de_tramite_next ");
    sql.append("FROM ( ");
    sql.append("  select y.cemp_codemp co_emp, ");
    sql.append("\t\t x.co_dep ");
    sql.append("  from IDOSGD.tdtd_dep_gru x ");
    sql.append("\t   RIGHT OUTER JOIN IDOSGD.rhtm_per_empleados y ");
    sql.append("\t   ON y.cemp_codemp = x.co_emp AND y.cemp_est_emp = '1' ");
    sql.append("  where x.co_gru_des= ? ");
    sql.append("  and x.es_dep_gru = '1' ");
    
    sql.append(") a  ");
    sql.append(" INNER  JOIN IDOSGD.rhtm_dependencia b ON a.co_dep = b.co_dependencia  ");
    sql.append(" INNER  JOIN IDOSGD.sitm_local_dependencia c ON a.co_dep = c.co_dep ");
    sql.append("order by a.co_dep ");
    
    try {
      if (pcoTipDoc.equals("232")) {
        list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinatarioDocumentoEmiBean.class), new Object[] { "4", "ATENDER", "F", "PARA CONOCIMINETO Y FINES", pcoGrupo });
      } else {
        
        list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinatarioDocumentoEmiBean.class), new Object[] { "0", "ORIGINAL", "1", "COPIA", pcoGrupo });
      }
    
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  public String updDocumentoObj(final DocumentoObjBean docObjBean) {
    String vReturn = "NO_OK";
    boolean inInsert = false;
    
    try {
      int cant = this.jdbcTemplate.queryForInt("select count(nu_ann) from IDOSGD.tdtv_archivo_doc where nu_ann = ? and nu_emi = ?", new Object[] { docObjBean.getNuAnn(), docObjBean.getNuEmi() });
      if (cant > 0) {
        inInsert = false;
      } else {
        inInsert = true;
      }
    
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 


    
    DefaultLobHandler defaultLobHandler = new DefaultLobHandler();
    if (inInsert) {
      StringBuffer sql = new StringBuffer();
      sql.append("INSERT INTO IDOSGD.tdtv_archivo_doc (NU_ANN,NU_EMI,BL_DOC,DE_RUTA_ORIGEN,ES_FIRMA,FEULA,W_BL_DOC,W_DE_RUTA_ORIGEN)\nVALUES(?,?,?,?,'0', (SELECT CONVERT(VARCHAR(10), CURRENT_TIMESTAMP, 112)),?,?)");
      
      try {
        this.jdbcTemplate.execute(sql.toString(), (PreparedStatementCallback)new AbstractLobCreatingPreparedStatementCallback((LobHandler)defaultLobHandler)
            {
              protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException
              {
                ps.setString(1, docObjBean.getNuAnn());
                ps.setString(2, docObjBean.getNuEmi());
                lobCreator.setBlobAsBytes(ps, 3, docObjBean.getDocumento());
                ps.setString(4, docObjBean.getNombreArchivo());
                lobCreator.setBlobAsBytes(ps, 5, docObjBean.getDocumento());
                ps.setString(6, docObjBean.getNombreArchivo());
              }
            });
        vReturn = "OK";
      } catch (Exception e) {
        e.printStackTrace();
      } 
    } else {
      
      StringBuffer sql1 = new StringBuffer();
      StringBuffer sql2 = new StringBuffer();
      if (docObjBean.getNombreArchivo().toUpperCase().indexOf(".DOCX") >= 0) {
        
        sql1.append("UPDATE IDOSGD.TDTV_ARCHIVO_DOC SET W_BL_DOC = ?, W_DE_RUTA_ORIGEN = ? \nWHERE NU_ANN = ? \nAND NU_EMI = ? ");


        
        this.jdbcTemplate.execute(sql1.toString(), (PreparedStatementCallback)new AbstractLobCreatingPreparedStatementCallback((LobHandler)defaultLobHandler)
            {
              protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException
              {
                lobCreator.setBlobAsBytes(ps, 1, docObjBean.getDocumento());
                ps.setString(2, docObjBean.getNombreArchivo());
                ps.setString(3, docObjBean.getNuAnn());
                ps.setString(4, docObjBean.getNuEmi());
              }
            });
      } 


      
      sql2.append("UPDATE IDOSGD.TDTV_ARCHIVO_DOC SET BL_DOC = ?, DE_RUTA_ORIGEN = ?, FEULA=(SELECT CONVERT(VARCHAR(10), CURRENT_TIMESTAMP, 112)) \nWHERE NU_ANN = ? \nAND NU_EMI = ? ");

      
      try {
        this.jdbcTemplate.execute(sql2.toString(), (PreparedStatementCallback)new AbstractLobCreatingPreparedStatementCallback((LobHandler)defaultLobHandler)
            {
              protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException
              {
                lobCreator.setBlobAsBytes(ps, 1, docObjBean.getDocumento());
                ps.setString(2, docObjBean.getNombreArchivo());
                ps.setString(3, docObjBean.getNuAnn());
                ps.setString(4, docObjBean.getNuEmi());
              }
            });
        vReturn = "OK";
      } catch (Exception e) {
        e.printStackTrace();
        vReturn = e.getMessage().substring(0, 20);
      } 
    } 
    
    return vReturn;
  }

  
  public String getCanDocumentoEmiDuplicados(DocumentoEmiBean documentoEmiBean) {
    String vReturn = "NO_OK";
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject("   SELECT COUNT(1)\n     FROM IDOSGD.tdtv_remitos tr\n    WHERE tr.nu_ann = ?\n      AND tr.co_dep_emi = ?\n      AND tr.co_tip_doc_adm = ?\n      AND tr.nu_doc_emi = ?\n      AND tr.ti_emi = '01'\n      AND nu_cor_doc = 1\n      AND tr.es_doc_emi NOT IN ('9')", String.class, new Object[] 
    		  { documentoEmiBean.getNuAnn(), documentoEmiBean.getCoDepEmi(), documentoEmiBean.getCoTipDocAdm(), documentoEmiBean.getNuDocEmi() });
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    return vReturn;
  }

  
  public List<EmpleadoBean> getPersonalEditDocAdmEmision(String pcoDepEmi) {
    StringBuffer sql = new StringBuffer();
    List<EmpleadoBean> list = new ArrayList<EmpleadoBean>();
    sql.append("SELECT e.cemp_apepat, ");
    sql.append("\t   e.cemp_apemat, ");
    sql.append("\t   e.cemp_denom, ");
    sql.append("\t   e.CEMP_CODEMP ");
    sql.append("FROM IDOSGD.RHTM_PER_EMPLEADOS e ");
    sql.append("WHERE e.CEMP_EST_EMP = '1' ");
    sql.append("AND (CEMP_CO_DEPEND in (select CO_DEPENDENCIA ");
    sql.append("\t\t            from IDOSGD.rhTM_dependencia d ");
    sql.append("\t\t \t    where d.CO_DEPENDENCIA =? ");
    sql.append("\t\t\t    or CO_DEPEN_PADRE=?) ");
    sql.append("\t\t\t    OR CO_DEPENDENCIA=? ) ");
    sql.append("UNION ");
    sql.append("SELECT  e.cemp_apepat, ");
    sql.append("\t    e.cemp_apemat, ");
    sql.append("\t    e.cemp_denom, ");
    sql.append("\t    e.CEMP_CODEMP ");
    sql.append("FROM IDOSGD.RHTM_PER_EMPLEADOS e ");
    sql.append("WHERE e.CEMP_EST_EMP = '1' ");
    sql.append("AND cemp_codemp ");
    sql.append("in ( ");
    sql.append("select co_emp ");
    sql.append("from IDOSGD.tdtx_dependencia_empleado ");
    sql.append("where co_dep=? ");
    sql.append("and es_emp='0' ");
    sql.append("UNION ");
    sql.append("select CO_EMPLEADO ");
    sql.append("from IDOSGD.rhtm_dependencia ");
    sql.append("where co_dependencia=? ");
    sql.append(") ");
    sql.append("ORDER BY 1 ");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[] { pcoDepEmi, pcoDepEmi, pcoDepEmi, pcoDepEmi, pcoDepEmi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  public String getUsuarioNofirmaProveido(String pcoEmpleado) {
    StringBuffer sql = new StringBuffer();
    String listTotal = "0";
    
    sql.append(" SELECT COUNT(*) TOTAL FROM IDOSGD.SI_ELEMENTO WHERE CTAB_CODTAB='NO_FIRMA_PROVEIDO' AND CELE_DESELE='" + pcoEmpleado + "' ");
    try {
      listTotal = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class);
    }
    catch (EmptyResultDataAccessException emptyResultDataAccessException) {
    
    } catch (Exception e) {
      
      e.printStackTrace();
    } 
    return listTotal;
  }
  
  public String updEstadoDocumentoEmiAdm(DocumentoEmiBean documentoEmiBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    
    sqlUpd.append("update IDOSGD.tdtv_remitos \nset CO_USE_MOD=?\n,ES_DOC_EMI=?\n,FE_USE_MOD=CURRENT_TIMESTAMP\nwhere\nNU_ANN=? and\nNU_EMI=?");


    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { documentoEmiBean.getCoUseMod(), documentoEmiBean.getEsDocEmi(), documentoEmiBean
            .getNuAnn(), documentoEmiBean.getNuEmi() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    
    return vReturn;
  }

  
  public String updEstadoDocumentoEmitido(DocumentoEmiBean documentoEmiBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    
    sqlUpd.append("update IDOSGD.tdtv_remitos \nset CO_USE_MOD=? \n,FE_USE_MOD=CURRENT_TIMESTAMP \n,FE_EMI=CURRENT_TIMESTAMP \n,NU_COR_DOC=? \n,NU_DOC_EMI=? \n,ES_DOC_EMI=? \nWHERE NU_ANN=? \nAND NU_EMI=? ");

 
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { documentoEmiBean.getCoUseMod(), documentoEmiBean.getNuCorDoc(), documentoEmiBean
            .getNuDocEmi(), documentoEmiBean.getEsDocEmi(), documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi() });
      vReturn = "OK";
    } catch (DuplicateKeyException con) {
      
      vReturn = "Numero de Documento Duplicado.";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    return vReturn;
  }


  
  public String updDocumentoEmiAdmBean(String nuAnn, String nuEmi, DocumentoEmiBean documentoEmiBean, ExpedienteBean expedienteBean, RemitenteEmiBean remitenteEmiBean, String pcoUserMod) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("update IDOSGD.tdtv_remitos \nset CO_USE_MOD=?,");
    
    if (documentoEmiBean != null) {
      documentoEmiBean.setNuAnn(nuAnn);
      sqlUpd.append("DE_ASU=?\n,NU_DIA_ATE=?\n,CO_TIP_DOC_ADM=?\n,NU_DOC_EMI=?\n,DE_DOC_SIG=?\n,NU_COR_DOC= (CASE WHEN ? IS NOT NULL THEN ? ELSE ? END)\n,OBS_DOC= ?,\n");


      if (documentoEmiBean.getNuDocEmi() == null || documentoEmiBean.getNuDocEmi().trim().equals("")) {
        String vnuCorDoc = getNroCorrelativoDocumento(documentoEmiBean);
        documentoEmiBean.setNuCorDoc(vnuCorDoc);
      } 


      
      if (documentoEmiBean.getNuCorEmi() == null || documentoEmiBean.getNuCorEmi().trim().equals("")) {
        String vnuCorEmi = getNroCorrelativoEmision(documentoEmiBean);
        documentoEmiBean.setNuCorEmi(vnuCorEmi);
        sqlUpd.append("NU_COR_EMI='" + documentoEmiBean.getNuCorEmi() + "',\n");
      } 
    } 
    if (expedienteBean != null) {
      sqlUpd.append("NU_ANN_EXP='" + expedienteBean.getNuAnnExp() + "',\n");
      sqlUpd.append("NU_SEC_EXP='" + expedienteBean.getNuSecExp() + "',\n");
    } 
    if (remitenteEmiBean != null) {
      sqlUpd.append("CO_LOC_EMI='" + remitenteEmiBean.getCoLocal() + "'\n,CO_DEP_EMI='" + remitenteEmiBean
          .getCoDependencia() + "'\n,CO_EMP_EMI='" + remitenteEmiBean
          .getCoEmpFirma() + "',\n");
    }
    sqlUpd.append("FE_USE_MOD=CURRENT_TIMESTAMP where\nNU_ANN=? and\nNU_EMI=?");



    
    try {
      if (documentoEmiBean != null) {
        this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { pcoUserMod, documentoEmiBean.getDeAsu(), documentoEmiBean.getNuDiaAte(), documentoEmiBean
              .getCoTipDocAdm(), documentoEmiBean.getNuDocEmi(), documentoEmiBean.getDeDocSig(), documentoEmiBean
              .getNuDocEmi(), documentoEmiBean.getNuCorDoc(), documentoEmiBean.getNuCorDoc(), documentoEmiBean.getDeObsDoc(), nuAnn, nuEmi });
      } else {
        
        this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { pcoUserMod, nuAnn, nuEmi });
      } 
      vReturn = "OK";
    } catch (DuplicateKeyException con) {
      
      vReturn = "Numero de Documento Duplicado";
    } catch (Exception e) {
      e.printStackTrace();
    } 

    
    return vReturn;
  }

  
  public String getNumeroDocSiguienteAdm(String pnuAnn, String pcoDepEmi, String pcoDoc) {
    String vReturn = "1";
    try {
      StringBuffer sql = new StringBuffer();
      sql.append("SELECT LTRIM(RTRIM(CAST((ISNULL(MAX(nu_doc_emi), 0) + 1) AS VARCHAR))) ");
      sql.append("FROM IDOSGD.tdtv_remitos tr ");
      sql.append("WHERE tr.nu_ann = ? ");
      sql.append("AND tr.co_dep_emi = ? ");
      sql.append("AND tr.co_tip_doc_adm = ? ");
      sql.append("AND tr.ti_emi = '01' ");
      sql.append("AND tr.nu_cor_doc = 1 ");
      sql.append("AND tr.es_doc_emi != '9' ");
      sql.append("AND ISNUMERIC(nu_doc_emi) = 1 ");
      vReturn = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { pnuAnn, pcoDepEmi, pcoDoc });
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = "1";
    } 
    
    vReturn = Utility.getInstancia().rellenaCerosIzquierda(vReturn, 6);
    return vReturn;
  }

  
  public DocumentoObjBean getPropiedadesArchivo(String pnuAnn, String pnuEmi, String ptiCap) {
    StringBuffer sql = new StringBuffer();
    sql.append("select ");
    sql.append("nu_ann, ");
    sql.append("nu_emi, ");
    sql.append("de_ruta_origen nombre_Archivo, ");
    sql.append("UPPER(SUBSTRING(de_ruta_origen, ((LEN(de_ruta_origen) - CHARINDEX('.', REVERSE(de_ruta_origen)) + 1) + 1), LEN(de_ruta_origen))) tipo_doc ");
    sql.append("from IDOSGD.tdtv_archivo_doc ");
    sql.append("where nu_ann = ? ");
    sql.append("and nu_emi = ? ");
    
    DocumentoObjBean docObjBean = new DocumentoObjBean();
    
    try {
      docObjBean = (DocumentoObjBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoObjBean.class), new Object[] { pnuAnn, pnuEmi });
    } catch (EmptyResultDataAccessException e) {
      docObjBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return docObjBean;
  }


  
  public String getNroCorrelativoDocumento(DocumentoEmiBean documentoEmiBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlQry = new StringBuffer();
    sqlQry.append("SELECT ISNULL(MAX(nu_cor_doc), 0) + 1 ");
    sqlQry.append("FROM IDOSGD.tdtv_remitos ");
    sqlQry.append("WHERE nu_ann       = ? ");
    sqlQry.append("AND ti_emi         = ? ");
    sqlQry.append("AND co_dep_emi     = ? ");
    sqlQry.append("AND co_tip_doc_adm = ? ");
    
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { documentoEmiBean.getNuAnn(), documentoEmiBean.getTiEmi(), documentoEmiBean
            .getCoDepEmi(), documentoEmiBean.getCoTipDocAdm() });
    
    }
    catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    
    return vReturn;
  }

  public boolean esDocoumentoSalidaExterna(String numEmi,String anio) {
	    String vReturn = "NO_OK";
	    StringBuffer sqlQry = new StringBuffer();
	    sqlQry.append("  SELECT '1' AS EXISTE   FROM IDOSGD.Tdtv_Remitos r  WHERE  ");
	    sqlQry.append(" r.CO_GRU = '1'  and r.TI_EMI ='01'   AND NOT EXISTS(select D.NU_EMI  ");
	    sqlQry.append(" from IDOSGD.TDTV_DESTINOS D  WHERE d.TI_DES ='01'  AND  D.NU_EMI=r.NU_EMI ");
	    sqlQry.append(" AND D.NU_ANN=r.NU_ANN   and   d.ES_ELI='0')  AND r.Es_Eli = '0' ");
	    sqlQry.append("  and r.NU_EMI=? AND r.NU_ANN=? ");
	    
	    
	    try {
	      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { numEmi,anio });
	    return vReturn==null? false:vReturn.equals("1")?true:false;
	    }
	    catch(EmptyResultDataAccessException e) {
	    	return false;
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	      vReturn = e.getMessage();
	    } 
	    
	    return false;
	  }
  public boolean esMensajeriaSedeCentral(String coDependencia) {
	    String vReturn = "NO_OK";
	    StringBuffer sqlQry = new StringBuffer();
	    sqlQry.append(" select '1' as existe from IDOSGD.RHTM_DEPENDENCIA where CO_DEPENDENCIA=? ");
	    sqlQry.append(" and in_baja='0'  ");
	    
	    
	    try {
	      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { coDependencia });
	      return vReturn==null? false:vReturn.equals("1")?true:false;
	    }
	    catch(EmptyResultDataAccessException e) {
	    	return false;
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	      vReturn = e.getMessage();
	    } 
	    
	    return false;
}
  public boolean esDirectoroResponsableOficina(String coDependencia,String codEmp) {
	    String vReturn = "NO_OK";
	    StringBuffer sqlQry = new StringBuffer();
	    sqlQry.append(" select '1' as existe from IDOSGD.RHTM_DEPENDENCIA where CO_DEPENDENCIA=? ");
	    sqlQry.append(" and in_baja='0' and CO_EMPLEADO=? ");
	    
	    
	    try {
	      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { coDependencia,codEmp });
	    return vReturn==null? false:vReturn.equals("1")?true:false;
	    }
	    catch(EmptyResultDataAccessException e) {
	    	return false;
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	      vReturn = e.getMessage();
	    } 
	    
	    return false;
  }
  public boolean tieneCargoHabilitadoparaEnvioDocsExterno(String codEmp) {
	    String vReturn = "NO_OK";
	    StringBuffer sqlQry = new StringBuffer();
	    sqlQry.append(" SELECT '1' AS EXISTE FROM IDOSGD.RHTM_CARGOS c ");
	    sqlQry.append(" inner join IDOSGD.RHTM_PER_EMPLEADOS e on (e.CEMP_CO_CARGO=c.CCAR_CO_CARGO) ");
	    sqlQry.append(" WHERE e.CEMP_CODEMP=? AND c.CCAR_HAB_ENVDOCS='1' ");
	    try {
	      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] {codEmp });
	    return vReturn==null? false:vReturn.equals("1")?true:false;
	    }
	    catch(EmptyResultDataAccessException e) {
	    	return false;
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	      vReturn = e.getMessage();
	    } 
	    
	    return false;
	  }
  
  public String getNroCorrelativoDocumentoDel(DocumentoEmiBean documentoEmiBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlQry = new StringBuffer();
    sqlQry.append("SELECT ISNULL(MAX(nu_cor_doc), 1) + 1 ");
    sqlQry.append("FROM IDOSGD.tdtv_remitos ");
    sqlQry.append("WHERE nu_ann       = ? ");
    sqlQry.append("AND ti_emi         = ? ");
    sqlQry.append("AND co_dep_emi     = ? ");
    sqlQry.append("AND co_tip_doc_adm = ? ");
    
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { documentoEmiBean.getNuAnn(), documentoEmiBean.getTiEmi(), documentoEmiBean
            .getCoDepEmi(), documentoEmiBean.getCoTipDocAdm() });
    
    }
    catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    
    return vReturn;
  }


  
  public String delAllReferenciaDocumentoEmi(String nuAnn, String nuEmi) {
    String vReturn = "NO_OK";
    StringBuffer sqlDel = new StringBuffer();
    sqlDel.append("DELETE FROM IDOSGD.tdtr_referencia ");
    sqlDel.append("WHERE NU_ANN = ? ");
    sqlDel.append("AND NU_EMI = ? ");
    
    try {
      this.jdbcTemplate.update(sqlDel.toString(), new Object[] { nuAnn, nuEmi });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    return vReturn;
  }


  
  public String delAllDestinatarioDocumentoEmi(String nuAnn, String nuEmi) {
    String vReturn = "NO_OK";
    StringBuffer sqlDel = new StringBuffer();
    sqlDel.append("DELETE FROM IDOSGD.TDTV_DESTINOS ");
    sqlDel.append("WHERE NU_ANN = ? ");
    sqlDel.append("AND NU_EMI = ? ");
    
    try {
      this.jdbcTemplate.update(sqlDel.toString(), new Object[] { nuAnn, nuEmi });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    return vReturn;
  }

  
  public String delDocumentoEmiAdm(DocumentoEmiBean documentoEmiBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    
    sqlUpd.append("update IDOSGD.tdtv_remitos ");
    sqlUpd.append("set CO_USE_MOD=?, ");
    sqlUpd.append("ES_ELI='1', ");
    sqlUpd.append("NU_DOC_EMI=?, ");
    sqlUpd.append("NU_COR_DOC=?, ");
    sqlUpd.append("NU_ANN_EXP=?, ");
    sqlUpd.append("NU_SEC_EXP=?, ");
    sqlUpd.append("NU_DET_EXP=?, ");
    sqlUpd.append("FE_USE_MOD=CURRENT_TIMESTAMP ");
    sqlUpd.append("where NU_ANN=? ");
    sqlUpd.append("and NU_EMI=? ");
    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { documentoEmiBean.getCoUseMod(), documentoEmiBean.getNuDocEmi(), documentoEmiBean
            .getNuCorDoc(), documentoEmiBean.getNuAnnExp(), documentoEmiBean.getNuSecExp(), documentoEmiBean.getNuDetExp(), documentoEmiBean
            .getNuAnn(), documentoEmiBean.getNuEmi() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    
    return vReturn;
  }

  
  public List<DependenciaBean> getListDestinatarioEmi(String pcoDepen, String pdeDepEmite) {
    StringBuffer sql = new StringBuffer();
    
    boolean vfiltro = (pdeDepEmite != null && !pdeDepEmite.trim().equals(""));
    
    sql.append("SELECT DE_DEPENDENCIA, ");
    sql.append("       CO_DEPENDENCIA, ");
    sql.append("       DE_CORTA_DEPEN ");
    sql.append("  FROM IDOSGD.RHTM_DEPENDENCIA ");
    sql.append(" WHERE co_nivel <> '6' ");
    sql.append("   AND IN_BAJA = '0' ");
    if (vfiltro) {
      sql.append(" AND (DE_DEPENDENCIA LIKE '%' + ? + '%' OR DE_CORTA_DEPEN LIKE ? + '%' ) ");
    }
    sql.append("UNION ");
    sql.append("SELECT DE_DEPENDENCIA, ");
    sql.append("       CO_DEPENDENCIA, ");
    sql.append("       DE_CORTA_DEPEN ");
    sql.append("  FROM IDOSGD.RHTM_DEPENDENCIA ");
    sql.append(" WHERE co_nivel = '6' ");
    sql.append("   AND IN_BAJA = '0' ");
    sql.append("   AND (CO_DEPEN_PADRE in ( select CO_DEPEN_PADRE co_dep ");
    sql.append("\t\t\t\t\t\t\tfrom IDOSGD.RHTM_DEPENDENCIA where CO_DEPENDENCIA = ?) ");
    sql.append("\t\t\t\t\t\t\tOR ? IN (CO_DEPENDENCIA, CO_DEPEN_PADRE)) ");
    if (vfiltro) {
      sql.append(" AND (DE_DEPENDENCIA LIKE '%' + ? + '%' OR DE_CORTA_DEPEN LIKE ? + '%') ");
    }
    sql.append("UNION ");
    sql.append("SELECT ' [TODOS]', ");
    sql.append(" \t   NULL, ");
    sql.append(" \t   NULL ");
    if (vfiltro) {
      sql.append(" where ' [TODOS]' like '%'+?+'%' ");
    }
    sql.append("  ORDER BY 1");

    
    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
    
    try {
      if (vfiltro) {
        list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { pdeDepEmite, pdeDepEmite, pcoDepen, pcoDepen, pdeDepEmite, pdeDepEmite, pdeDepEmite });
      } else {
        
        list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { pcoDepen, pcoDepen });
      } 
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DestinoBean> getListaDestinosCodDepTipoDes(String nu_ann, String nu_emi) {
    StringBuffer sql = new StringBuffer();
    List<DestinoBean> list = new ArrayList<DestinoBean>();
    sql.append("SELECT DISTINCT co_dep_des, ");
    sql.append("ti_des ");
    sql.append("FROM IDOSGD.tdtv_destinos ");
    sql.append("WHERE es_eli = '0' ");
    sql.append("AND nu_ann = ? ");
    sql.append("AND nu_emi = ? ");
    sql.append("ORDER BY ti_des");
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinoBean.class), new Object[] { nu_ann, nu_emi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DestinoBean> getListaDestinosOriTipoDes(String nu_ann, String nu_emi) {
    StringBuffer sql = new StringBuffer();
    List<DestinoBean> list = new ArrayList<DestinoBean>();
    sql.append("SELECT ti_des, ");
    sql.append("CASE ti_des ");
    sql.append("\tWHEN '02' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_PROVEEDOR](nu_ruc_des) ");
    sql.append("\tWHEN '03' THEN [IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](nu_dni_des) ");
    sql.append("\tWHEN '04' THEN [IDOSGD].[PK_SGD_DESCRIPCION_OTRO_ORIGEN](co_otr_ori_des) ");
    sql.append("\tELSE ' ' ");
    sql.append("END co_dep_des ");
    sql.append("FROM IDOSGD.tdtv_destinos ");
    sql.append("where nu_ann = ? ");
    sql.append("and nu_emi = ? ");
    sql.append("and es_eli = '0' ");
    sql.append("ORDER BY ti_des ");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinoBean.class), new Object[] { nu_ann, nu_emi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<ReferenciaRemitoBean> getListaReferenciaRemitos(String nu_ann, String nu_emi) {
    StringBuffer sql = new StringBuffer();
    List<ReferenciaRemitoBean> list = new ArrayList<ReferenciaRemitoBean>();
    sql.append("SELECT DISTINCT b.ti_emi, b.co_dep_emi ");
    sql.append("FROM   IDOSGD.tdtr_referencia a INNER JOIN IDOSGD.tdtv_remitos b ON a.nu_ann_ref = b.nu_ann AND    a.nu_emi_ref = b.nu_emi  ");
    sql.append("WHERE  ");
    sql.append("     a.nu_ann = ? ");
    sql.append("AND    a.nu_emi = ? ");
    sql.append("ORDER  BY b.ti_emi");
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ReferenciaRemitoBean.class), new Object[] { nu_ann, nu_emi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<ReferenciaRemitoBean> getOriReferenciaLista(String nu_ann, String nu_emi) {
    StringBuffer sql = new StringBuffer();
    List<ReferenciaRemitoBean> list = new ArrayList<ReferenciaRemitoBean>();
    sql.append("SELECT b.ti_emi, ");
    sql.append("CASE b.ti_emi ");
    sql.append("\tWHEN '02' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_PROVEEDOR](b.nu_ruc_emi) ");
    sql.append("\tWHEN '03' THEN [IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](b.nu_dni_emi) ");
    sql.append("\tWHEN '04' THEN [IDOSGD].[PK_SGD_DESCRIPCION_OTRO_ORIGEN](b.co_otr_ori_emi) ");
    sql.append("\tELSE ' ' ");
    sql.append("END co_dep_emi ");
    sql.append("FROM   IDOSGD.tdtr_referencia a ");
    sql.append("INNER JOIN\t   IDOSGD.tdtv_remitos b ON  a.nu_ann_ref = b.nu_ann AND    a.nu_emi_ref = b.nu_emi ");
    sql.append("WHERE ");
    sql.append("     a.nu_ann = ? ");
    sql.append("AND    a.nu_emi = ? ");
    sql.append("ORDER  BY b.ti_emi ");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ReferenciaRemitoBean.class), new Object[] { nu_ann, nu_emi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DestinoBean> getListaDestinosCodPri(String nu_ann, String nu_emi) {
    StringBuffer sql = new StringBuffer();
    List<DestinoBean> list = new ArrayList<DestinoBean>();
    sql.append("SELECT co_pri ");
    sql.append("FROM IDOSGD.tdtv_destinos ");
    sql.append("WHERE es_eli = '0' ");
    sql.append("AND nu_ann = ? ");
    sql.append("AND nu_emi = ? ");
    sql.append("ORDER BY co_pri desc");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinoBean.class), new Object[] { nu_ann, nu_emi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String getNumDestinos(String nu_ann, String nu_emi) {
    String vReturn = "0";
    StringBuffer sqlQry = new StringBuffer();
    sqlQry.append("SELECT count(nu_des) ");
    sqlQry.append("FROM IDOSGD.tdtv_destinos ");
    sqlQry.append("WHERE nu_ann = ? ");
    sqlQry.append("AND nu_emi = ? ");
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { nu_ann, nu_emi });
    } catch (Exception e) {
      vReturn = "0";
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String updRemitoResumenDestinatario(String pnuAnn, String pnuEmi, String vti_des, String vco_pri, String vnu_cant_des, String vresOriDes) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("UPDATE IDOSGD.TDTX_REMITOS_RESUMEN SET ");
    sqlUpd.append("TI_EMI_DES = ?,CO_PRIORIDAD = ? ,nu_candes=? ");
    sqlUpd.append("WHERE NU_ANN = ? ");
    sqlUpd.append("AND NU_EMI = ? ");
    
    StringBuffer sqlUpd2 = new StringBuffer();
    sqlUpd2.append("UPDATE IDOSGD.TDTV_REMITOS SET DE_ORI_DES = ?\nWHERE NU_ANN = ? \nAND NU_EMI = ? ");


    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { vti_des, vco_pri, vnu_cant_des, pnuAnn, pnuEmi });


      
      this.jdbcTemplate.update(sqlUpd2.toString(), new Object[] { vresOriDes, pnuAnn, pnuEmi });

      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }

  
  public String updRemitoResumenReferencia(String pnuAnn, String pnuEmi, String vti_ori, String vdeOriEmi) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("UPDATE IDOSGD.TDTX_REMITOS_RESUMEN SET ");
    sqlUpd.append("TI_EMI_REF = ? ");
    sqlUpd.append("WHERE NU_ANN = ? ");
    sqlUpd.append("AND NU_EMI = ? ");
    
    StringBuffer sqlUpd2 = new StringBuffer();
    sqlUpd2.append("UPDATE IDOSGD.TDTV_REMITOS SET DE_ORI_EMI = ? ");
    sqlUpd2.append("WHERE NU_ANN = ? ");
    sqlUpd2.append("AND NU_EMI = ? ");
    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { vti_ori, pnuAnn, pnuEmi });



      
      this.jdbcTemplate.update(sqlUpd2.toString(), new Object[] { vdeOriEmi, pnuAnn, pnuEmi });


      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }
  
  public DocumentoEmiBean existeDocumentoReferenciado(BuscarDocumentoEmiBean buscarDocumentoEmiBean) {
    StringBuffer sql = new StringBuffer();
    DocumentoEmiBean documentoEmiBean = null;
    sql.append("SELECT A.NU_ANN, ");
    sql.append("A.NU_EMI, ");
    sql.append("A.NU_COR_DOC ");
    sql.append("FROM IDOSGD.TDTV_REMITOS A ");
    sql.append("WHERE A.NU_ANN=? ");
    sql.append("AND A.CO_DEP_EMI=? ");
    sql.append("AND A.TI_EMI='01' ");
    sql.append("AND A.CO_TIP_DOC_ADM=? ");
    sql.append("AND A.NU_DOC_EMI=? ");
    sql.append("AND A.ES_ELI='0' ");
    sql.append("AND A.ES_DOC_EMI NOT IN ('5','7','9') ");
    
    try {
      documentoEmiBean = (DocumentoEmiBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class), new Object[] { buscarDocumentoEmiBean
            .getsCoAnnioBus(), buscarDocumentoEmiBean.getsBuscDestinatario(), buscarDocumentoEmiBean.getsDeTipoDocAdm(), buscarDocumentoEmiBean
            .getsNumDocRef() });
      documentoEmiBean.setMsjResult("OK");
    } catch (EmptyResultDataAccessException e) {
      documentoEmiBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return documentoEmiBean;
  }

  
  public List<DocumentoEmiBean> getDocumentosReferenciadoBusq(DocumentoEmiBean documentoEmiBean, String sTipoAcceso) {
    StringBuffer sql = new StringBuffer();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    List<DocumentoEmiBean> list = new ArrayList<DocumentoEmiBean>();
    
    sql.append("SELECT A.* ");
    sql.append("FROM ( ");
    sql.append("\tSELECT  DISTINCT B.NU_COR_EMI, ");
    sql.append("            (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_EMI_REF](B.NU_ANN, B.NU_EMI)) DE_EMI_REF, ");
    sql.append("\t\tB.FE_EMI, ");
    sql.append("\t\tCONVERT(VARCHAR(10), B.FE_EMI, 3) FE_EMI_CORTA, ");
    sql.append("\t\t(SELECT CDOC_DESDOC ");
    sql.append("\t\tFROM IDOSGD.SI_MAE_TIPO_DOC ");
    sql.append("\t\tWHERE CDOC_TIPDOC = B.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM, ");
    sql.append("\t\tCASE B.NU_CANDES ");
    sql.append("                WHEN 1 THEN (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP](B.NU_ANN, B.NU_EMI)) ");
    sql.append("                ELSE (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP_V](B.NU_ANN, B.NU_EMI)) ");
    sql.append("\t\tEND DE_EMP_PRO, ");
    sql.append("\t\t(SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM ");
    sql.append("\t\tFROM IDOSGD.RHTM_PER_EMPLEADOS ");
    sql.append("\t\tWHERE CEMP_CODEMP = B.CO_EMP_RES) DE_EMP_RES, ");
    sql.append("            (SELECT DE_EST ");
    sql.append("\t\tFROM IDOSGD.TDTR_ESTADOS ");
    sql.append("\t\tWHERE (CO_EST + DE_TAB) = (B.ES_DOC_EMI + 'TDTV_REMITOS')) DE_ES_DOC_EMI, ");
    sql.append("\t\tC.NU_DOC, ");
    sql.append("\t\tUPPER(B.DE_ASU) DE_ASU_M, ");
    sql.append("\t\tB.NU_ANN, ");
    sql.append("\t\tC.NU_EXPEDIENTE, ");
    sql.append("\t\tB.NU_EMI, ");
    sql.append("\t\tB.TI_CAP, ");
    sql.append("\t\tC.IN_EXISTE_DOC EXISTE_DOC, ");
    sql.append("\t\tCASE ISNULL(C.TI_EMI_REF,'0') + ISNULL(C.IN_EXISTE_ANEXO,'2') ");
    sql.append("                WHEN '00' THEN 0 ");
    sql.append("                WHEN '02' THEN 0 ");
    sql.append("                ELSE 1 ");
    sql.append("\t\tEND EXISTE_ANEXO, ");
    sql.append("\t\tISNULL(C.CO_PRIORIDAD,'1') CO_PRIORIDAD, ");
    sql.append("\t\tB.ES_DOC_EMI, ");
    sql.append("\t\tROW_NUMBER() OVER (ORDER BY B.NU_COR_EMI) AS ROWNUM ");
    sql.append("\tFROM ( ");
    sql.append("\t\t SELECT NU_ANN, NU_EMI FROM [IDOSGD].[PK_SGD_DESCRIPCION_ARBOL_SEG](:pCoAnio, :pNuEmi) ");
    sql.append("\t) A ");
    sql.append("\tINNER JOIN IDOSGD.TDTV_REMITOS B ON A.NU_ANN = B.NU_ANN AND A.NU_EMI = B.NU_EMI ");
    sql.append("\tINNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN C ON C.NU_ANN = B.NU_ANN AND C.NU_EMI = B.NU_EMI ");
    sql.append("\tWHERE  ");
    sql.append("\t  B.TI_EMI='01' ");
    sql.append("\tAND C.TI_EMI='01' ");
    sql.append("\tAND B.ES_ELI = '0' ");
    sql.append("\tAND B.CO_DEP_EMI = :pCoDepEmi ");
    if (sTipoAcceso.equals("1")) {
      sql.append(" AND B.CO_EMP_RES = :pcoEmpRes ");
      objectParam.put("pcoEmpRes", documentoEmiBean.getCoEmpRes());
    } 
    objectParam.put("pCoAnio", documentoEmiBean.getNuAnn());
    objectParam.put("pNuEmi", documentoEmiBean.getNuEmi());
    objectParam.put("pCoDepEmi", documentoEmiBean.getCoDepEmi());
    sql.append(") A ");
    sql.append("WHERE ROWNUM < 101 ");
    sql.append("ORDER BY A.NU_COR_EMI DESC ");
    
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public TblRemitosBean getDatosDocumento(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    TblRemitosBean tblRemitosBean = null;
    sql.append("SELECT A.NU_ANN, ");
    sql.append("A.NU_EMI, ");
    sql.append("A.CO_DEP_EMI, ");
    sql.append("A.FE_EMI ");
    sql.append("FROM IDOSGD.TDTV_REMITOS A ");
    sql.append("WHERE A.NU_ANN=? ");
    sql.append("AND A.NU_EMI=? ");
    sql.append("AND A.ES_ELI='0' ");
    
    try {
      tblRemitosBean = (TblRemitosBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TblRemitosBean.class), new Object[] { pnuAnn, pnuEmi });
      
      tblRemitosBean.setMsgResult("OK");
    } catch (EmptyResultDataAccessException e) {
      tblRemitosBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return tblRemitosBean;
  }

  
  public String getCoEmplFirmoDocumento(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    String result = "NO_OK";
    sql.append("SELECT CO_EMP_EMI ");
    sql.append("FROM IDOSGD.TDTV_REMITOS ");
    sql.append("WHERE nu_emi = ? ");
    sql.append("AND nu_ann = ? ");
    
    try {
      result = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { pnuEmi, pnuAnn });
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return result;
  }

  
  public DocumentoEmiBean getEstadoDocumento(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    DocumentoEmiBean documentoEmiBean = null;
    sql.append("SELECT CO_EMP_EMI, ");
    sql.append("\t   ES_DOC_EMI, ");
    sql.append("\t   NU_ANN,NU_EMI, ");
    sql.append("\t   TI_EMI, ");
    sql.append("\t   CO_DEP_EMI, ");
    sql.append("\t   CO_TIP_DOC_ADM, ");
    sql.append("\t   NU_DOC_EMI, ");
    sql.append("\t   CO_EMP_RES ");
    sql.append("FROM IDOSGD.TDTV_REMITOS ");
    sql.append("WHERE nu_ann = ? ");
    sql.append("AND nu_emi = ? ");
    sql.append("AND ES_ELI='0' ");
    
    try {
      documentoEmiBean = (DocumentoEmiBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class), new Object[] { pnuAnn, pnuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      documentoEmiBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return documentoEmiBean;
  }



  
  public String getNroCorrelativoEmision(DocumentoEmiBean documentoEmiBean) {
    String vReturn = "0";
    StringBuffer sqlQry = new StringBuffer();
    sqlQry.append("SELECT ISNULL(MAX(nu_cor_emi), 0) + 1 ");
    sqlQry.append("FROM IDOSGD.tdtv_remitos ");
    sqlQry.append("WHERE nu_ann = ? ");
    sqlQry.append("AND co_dep_emi = ? ");
    sqlQry.append("AND ti_emi = ? ");
    
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { documentoEmiBean.getNuAnn(), documentoEmiBean.getCoDepEmi(), documentoEmiBean.getTiEmi() });
    
    }
    catch (Exception e) {
      e.printStackTrace();
      vReturn = "0";
    } 
    return vReturn;
  }

  
  public String updChangeToDespacho(DocumentoEmiBean documentoEmiBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    
    sqlUpd.append("update IDOSGD.tdtv_remitos \nset CO_USE_MOD=?\n,ES_DOC_EMI=?\n,FE_USE_MOD=CURRENT_TIMESTAMP\n,FE_EMI=CURRENT_TIMESTAMP\nwhere\nNU_ANN=? and\nNU_EMI=?");


    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { documentoEmiBean.getCoUseMod(), documentoEmiBean.getEsDocEmi(), documentoEmiBean
            .getNuAnn(), documentoEmiBean.getNuEmi() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    
    return vReturn;
  }

  
  public String updEstadoDocumentoEnvioNotificacion(DocumentoEmiBean documentoEmiBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd2 = new StringBuffer();
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("UPDATE IDOSGD.TDTV_REMITOS \nset CO_USE_MOD=?\n,DOC_ESTADO_MSJ=?\n,COD_DEP_MSJ=?\n,FE_USE_MOD=CURRENT_TIMESTAMP\n,FE_ENV_MES=CURRENT_TIMESTAMP\n,TI_ENV_MSJ=?\nwhere\nNU_ANN=? and\nNU_EMI=?");


    sqlUpd2.append("update IDOSGD.tdtv_destinos \nset CO_USE_MOD=?\n,CO_PRI=?\n,FE_USE_MOD=CURRENT_TIMESTAMP\nwhere\nNU_ANN=? and\nNU_EMI=?");

    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { documentoEmiBean.getCoUseMod(), documentoEmiBean.getEsDocEmi(), documentoEmiBean
            .getCoDepEmi(), documentoEmiBean.getTiEnvMsj(), documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi() });
      this.jdbcTemplate.update(sqlUpd2.toString(), new Object[] { documentoEmiBean.getCoUseMod(), documentoEmiBean.getCoPrioridad(), documentoEmiBean
            .getNuAnn(), documentoEmiBean.getNuEmi() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    
    return vReturn;
  }

  
  public List<DocumentoBean> getLstDocRecepcionadoRefMp(String pcoDepen, String pannio, String ptiDoc, String pnuDoc) {
    StringBuffer sql = new StringBuffer();
    List<DocumentoBean> list = new ArrayList<DocumentoBean>();
    
    sql.append("SELECT A.* ");
    sql.append("FROM ( ");
    
    sql.append("\tselect E.*, ROW_NUMBER() OVER (ORDER BY FE_REC_DOC) AS ROWNUM ");
    sql.append("\tfrom ( ");
    sql.append("\t\tselect ");
    sql.append("\t\td.FE_REC_DOC, ");
    sql.append("\t\tSUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](r.co_tip_doc_adm),1,100) de_tip_doc_adm, ");
    sql.append("\t\tSUBSTRING(rr.nu_doc,1,50) NU_DOC, ");
    sql.append("\t\tCONVERT(VARCHAR(10), r.FE_EMI, 3) FE_EMI_CORTA, ");
    sql.append("\t\tCONVERT(VARCHAR(10), d.FE_REC_DOC, 3) FE_REC_DOC_CORTA, ");
    sql.append("\t\tr.nu_ann, ");
    sql.append("\t\tr.nu_emi, ");
    sql.append("\t\td.nu_des, ");
    sql.append("\t\tREPLACE(LTRIM(RTRIM(r.DE_ASU)), CHAR(10), ' ') DE_ASU, ");
    sql.append("\t\tr.co_tip_doc_adm, ");
    sql.append("\t\tSUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](d.co_dep_des),1,200) de_dep_des, ");
    sql.append("\t\tSUBSTRING(rr.nu_expediente,1,20) NU_EXPEDIENTE, ");
    sql.append("\t\tr.NU_ANN_EXP, ");
    sql.append("\t\tr.NU_SEC_EXP ");
    sql.append("\t\tfrom IDOSGD.TDTV_REMITOS R ");
    sql.append("\t\tINNER JOIN IDOSGD.TDTV_DESTINOS D ON d.nu_ann = r.nu_ann AND d.nu_emi = r.nu_emi ");
    sql.append("\t\tINNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN RR ON rr.nu_ann=r.nu_ann AND rr.nu_emi = r.nu_emi ");
    sql.append("\t\twhere ");
    sql.append("\t\tr.es_eli = '0' ");
    sql.append("\t\tAND d.es_eli = '0' ");
    sql.append("\t\tAND r.nu_ann = ? ");
    
    sql.append("\t\tAND r.es_doc_emi NOT IN ('5', '9', '7') ");
    sql.append("\t\tand d.es_doc_rec <> '0' ");
    sql.append("\t\tand d.co_dep_des in ");
    sql.append("\t\t(select ? ");
    sql.append("\t\tUNION ");
    sql.append("\t\t  SELECT co_dep_ref ");
    sql.append("\t\t\tFROM IDOSGD.tdtx_referencia ");
    sql.append("\t\t   WHERE co_dep_emi = ? ");
    sql.append("\t\t\t AND ti_emi = 'D' ");
    sql.append("\t\t\t AND es_ref = '1') ");
    sql.append("\t\tAND r.co_tip_doc_adm = ? ");
    if (pnuDoc != null && pnuDoc.trim().length() > 0) {
      pnuDoc = Utility.getInstancia().rellenaCerosIzquierda(pnuDoc, 6);
      sql.append("AND (R.NU_DOC_EMI = '").append(pnuDoc).append("' OR RR.NU_EXPEDIENTE LIKE '%").append(pnuDoc).append("')\n");
    } 
    
    sql.append("UNION \nselect\nd.FE_REC_DOC,\nSUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](r.co_tip_doc_adm),1,100) de_tip_doc_adm,\nSUBSTRING(rr.nu_doc,1,50) NU_DOC,\nCONVERT(VARCHAR(10), r.FE_EMI, 3) FE_EMI_CORTA,\nCONVERT(VARCHAR(10), d.FE_REC_DOC, 3) FE_REC_DOC_CORTA,\nr.nu_ann,\nr.nu_emi,\nd.nu_des,\nREPLACE(LTRIM(RTRIM(r.DE_ASU)), CHAR(10), ' ') DE_ASU,\nr.co_tip_doc_adm,\nSUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](d.co_dep_des),1,200) de_dep_des,\nSUBSTRING(rr.nu_expediente,1,20) NU_EXPEDIENTE,\nr.NU_ANN_EXP,\nr.NU_SEC_EXP\n ");

    sql.append("\t\tfrom IDOSGD.TDTV_REMITOS R ");
    sql.append("\t\tINNER JOIN IDOSGD.TDTV_DESTINOS D ON d.nu_ann = r.nu_ann AND d.nu_emi = r.nu_emi ");
    sql.append("\t\tINNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN RR ON rr.nu_ann=r.nu_ann AND rr.nu_emi = r.nu_emi ");
    sql.append(" where\nr.es_eli = '0'\nAND d.es_eli = '0'\nAND r.es_doc_emi NOT IN ('5', '9', '7')\nand d.es_doc_rec <> '0'  \nand (R.nu_ann+R.nu_emi) in ( \nselect  (REF.nu_ann_ref+REF.nu_emi_ref) as nurem from IDOSGD.TDTR_REFERENCIA REF where (REF.nu_ann+REF.nu_emi) in \n(select (R.nu_ann+R.nu_emi) as nurem \n");
 
    sql.append("\t\tfrom IDOSGD.TDTV_REMITOS R ");
    sql.append("\t\tINNER JOIN IDOSGD.TDTV_DESTINOS D ON d.nu_ann = r.nu_ann AND d.nu_emi = r.nu_emi ");
    sql.append("\t\tINNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN RR ON rr.nu_ann=r.nu_ann AND rr.nu_emi = r.nu_emi ");
    sql.append("where\nr.es_eli = '0'\nAND d.es_eli = '0'\nAND r.nu_ann = ? \nAND r.es_doc_emi NOT IN ('5', '9', '7')\nand d.es_doc_rec <> '0'  \nand d.co_dep_des = ? \n");
  
    if (pnuDoc != null && pnuDoc.trim().length() > 0) {
      pnuDoc = Utility.getInstancia().rellenaCerosIzquierda(pnuDoc, 6);
      sql.append("AND (R.NU_DOC_EMI = '").append(pnuDoc).append("' OR RR.NU_EXPEDIENTE LIKE '%").append(pnuDoc).append("%')\n");
    } 
    
    sql.append("))\t\tUNION ");
    sql.append("\t\tSELECT ");
    sql.append("\t\tDE.FE_REC_DOC, ");
    sql.append("\t\tSUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](r.co_tip_doc_adm),1,100) de_tip_doc_adm, ");
    sql.append("\t\tSUBSTRING(rr.nu_doc,1,50) NU_DOC, ");
    sql.append("\t\tCONVERT(VARCHAR(10), r.FE_EMI, 3) FE_EMI_CORTA, ");
    sql.append("\t\tCONVERT(VARCHAR(10), DE.FE_REC_DOC, 3) FE_REC_DOC_CORTA, ");
    sql.append("\t\tr.nu_ann, ");
    sql.append("\t\tr.nu_emi, ");
    sql.append("\t\tDE.nu_des, ");
    sql.append("\t\tREPLACE(LTRIM(RTRIM(r.DE_ASU)), CHAR(10), ' ') DE_ASU, ");
    sql.append("\t\tr.co_tip_doc_adm, ");
    sql.append("\t\tSUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](DE.co_dep_des),1,200) de_dep_des, ");
    sql.append("\t\tSUBSTRING(rr.nu_expediente, 1, 20) NU_EXPEDIENTE, ");
    sql.append("\t\tr.NU_ANN_EXP, ");
    sql.append("\t\tr.NU_SEC_EXP ");
    sql.append("\t\tFROM IDOSGD.TDTV_DESTINOS DE ");
    sql.append("\t\tINNER JOIN IDOSGD.TDTV_REMITOS R ON DE.NU_ANN=R.NU_ANN AND DE.NU_EMI=R.NU_EMI  ");
    sql.append("\t\tINNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN RR ON  RR.NU_ANN=R.NU_ANN AND R.NU_EMI=RR.NU_EMI ");
    sql.append("\t\tWHERE R.NU_ANN = ? ");
    sql.append("\t\tAND R.es_eli = '0' ");
    sql.append("\t\tAND DE.ES_ELI='0' ");
    sql.append("\t\tand R.es_doc_emi not in ('9','7','5') ");
    sql.append("\t\tAND R.CO_GRU = '1' ");
    sql.append("\t\tAND DE.ES_ENV_POR_TRA='1' ");
    sql.append("\t\tAND r.co_tip_doc_adm = ? ");
    if (pnuDoc != null && pnuDoc.trim().length() > 0) {
      pnuDoc = Utility.getInstancia().rellenaCerosIzquierda(pnuDoc, 6);
      sql.append("AND (R.NU_DOC_EMI = '").append(pnuDoc).append("' OR RR.NU_EXPEDIENTE LIKE '%").append(pnuDoc).append("')\n");
    } 




    
    sql.append("\t) E ");
    
    sql.append(") A ");
    sql.append("WHERE ROWNUM < 201 ");
    sql.append("order by 1 desc ");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoBean.class), new Object[] { pannio, pcoDepen, pcoDepen, ptiDoc, pannio, ptiDoc, pannio, ptiDoc });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public DocumentoEmiBean getEstadoDocumentoAudi(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    DocumentoEmiBean documentoEmiBean = null;
    sql.append("SELECT CO_EMP_EMI, ");
    sql.append("ES_DOC_EMI,NU_ANN, ");
    sql.append("NU_EMI, ");
    sql.append("TI_EMI, ");
    sql.append("CO_DEP_EMI, ");
    sql.append("CO_TIP_DOC_ADM, ");
    sql.append("NU_DOC_EMI ");
    sql.append("FROM IDOSGD.TDTV_REMITOS ");
    sql.append("WHERE nu_ann = ? ");
    sql.append("AND nu_emi = ? ");
    
    try {
      documentoEmiBean = (DocumentoEmiBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class), new Object[] { pnuAnn, pnuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      documentoEmiBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return documentoEmiBean;
  }

  
  public String insPersonalVoBo(String nuAnn, String nuEmi, String coDep, String coEmp, String coUser) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("INSERT INTO IDOSGD.TDTV_PERSONAL_VB(NU_ANN,NU_EMI,CO_DEP,\nCO_EMP_VB,IN_VB,CO_USE_CRE,\nFE_USE_CRE,CO_USE_MOD,FE_USE_MOD)\nVALUES(?,?,?,\n?,'0',?,\nCURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP)");

    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { nuAnn, nuEmi, coDep, coEmp, coUser, coUser });
      vReturn = "OK";
    } catch (DuplicateKeyException con) {
      vReturn = "Datos Duplicado INSERT IDOSGD.TDTV_PERSONAL_VB.";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String delPersonalVoBo(String nuAnn, String nuEmi, String coDep, String coEmp) {
    String vReturn = "NO_OK";
    StringBuffer sqlDel = new StringBuffer();
    sqlDel.append("DELETE FROM IDOSGD.TDTV_PERSONAL_VB\nWHERE\nNU_ANN=?\nAND NU_EMI=?\nAND CO_DEP=?\nAND CO_EMP_VB=?\nAND IN_VB <> '1'");

    
    try {
      this.jdbcTemplate.update(sqlDel.toString(), new Object[] { nuAnn, nuEmi, coDep, coEmp });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String getInNumeraDocAdm(String tipoDoc) {
    String vReturn = "0";
    StringBuffer sqlQry = new StringBuffer();
    sqlQry.append("SELECT IN_NUMERACION FROM IDOSGD.SI_MAE_TIPO_DOC\nWHERE CDOC_INDBAJ='0'\nAND CDOC_TIPDOC=?");

    
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { tipoDoc });
      if (vReturn == null) {
        vReturn = "0";
      }
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String getInFirmaDocAdm(String tipoDoc) {
    String vReturn = "2";
    StringBuffer sqlQry = new StringBuffer();
    sqlQry.append("SELECT IN_TIPO_FIRMA FROM IDOSGD.SI_MAE_TIPO_DOC\nWHERE CDOC_INDBAJ='0'\nAND CDOC_TIPDOC=?");

    
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { tipoDoc });
      if (vReturn == null) {
        vReturn = "2";
      }
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public List<DestinatarioDocumentoEmiBean> getLstPersVoBoGrupo(String pcoGrupo) {
    StringBuffer sql = new StringBuffer();
    List<DestinatarioDocumentoEmiBean> list = new ArrayList<DestinatarioDocumentoEmiBean>();
    
    sql.append("SELECT a.co_dep co_dependencia, ");
    sql.append("b.DE_CORTA_DEPEN de_dependencia, ");
    sql.append("ISNULL(a.co_emp, b.co_empleado) co_empleado, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](ISNULL(a.co_emp, b.co_empleado)) de_empleado ");
    sql.append("FROM ( ");
    sql.append("  select y.cemp_codemp co_emp, ");
    sql.append("\t\t x.co_dep ");
    sql.append("  from IDOSGD.tdtd_dep_gru x ");
    sql.append("\t   RIGHT OUTER JOIN IDOSGD.rhtm_per_empleados y ");
    sql.append("\t   ON y.cemp_codemp = x.co_emp AND y.cemp_est_emp = '1' ");
    sql.append("  where x.co_gru_des= ? ");
    sql.append("  and x.es_dep_gru = '1' ");
    sql.append(") a INNER JOIN ");
    sql.append(" IDOSGD.rhtm_dependencia b  ON a.co_dep = b.co_dependencia ");
    sql.append(" INNER JOIN IDOSGD.sitm_local_dependencia c ON a.co_dep = c.co_dep ");
    sql.append("order by 1 ");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinatarioDocumentoEmiBean.class), new Object[] { pcoGrupo });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String updArchivarDocumento(DocumentoEmiBean documentoEmiBean, Usuario usuario) {
    String vReturn = "NO_OK";
    
    this.spActualizaEstado = (new SimpleJdbcCall(this.dataSource)).withProcedureName("[IDOSGD].[ACTUALIZA_ESTADO_A]").withoutProcedureColumnMetaDataAccess().useInParameterNames(new String[] { "pnu_ann", "pnu_emi", "pde_ane" }).declareParameters(new SqlParameter[] { new SqlParameter("pnu_ann", 12), new SqlParameter("pnu_emi", 12), new SqlParameter("pde_ane", 12) });


    
    MapSqlParameterSource mapSqlParameterSource = (new MapSqlParameterSource()).addValue("pnu_ann", documentoEmiBean.getNuAnn()).addValue("pnu_emi", documentoEmiBean.getNuEmi()).addValue("pde_ane", documentoEmiBean.getDocObser());
    
    try {
      this.spActualizaEstado.execute((SqlParameterSource)mapSqlParameterSource);
      vReturn = "OK";
    } catch (Exception ex) {
      
      ex.printStackTrace();
    } 


    
    return vReturn;
  }


  
  public int getCantidadAnexo(DocumentoEmiBean documentoEmiBean) {
    String vReturn = "0";
    StringBuffer sqlQry = new StringBuffer();
    sqlQry.append("select sum(ISNULL(B.nu_des,0))\nFROM IDOSGD.tdtv_destinos A LEFT JOIN IDOSGD.tdtv_anexos B \nON A.nu_ann=B.nu_ann AND A.nu_emi=B.nu_emi AND A.nu_des=B.nu_des \nwhere A.nu_ann = ? \nand A.nu_emi = ? \n");



    
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi() });
    
    }
    catch (Exception e) {
      e.printStackTrace();
      vReturn = "0";
    } 
    return Integer.parseInt(vReturn);
  }




  
  public String insMesaVitual(DatosInterBean datosInter) {
    String vReturn = "NO_OK";

    
    this.spInsMesaVirtual = (new SimpleJdbcCall(this.dataSource)).withProcedureName("[IDOSGD].[INS_MESA_VIRTUAL] ").withoutProcedureColumnMetaDataAccess().useInParameterNames(new String[] { "P_COUSEMOD", "P_DEPDES", "P_NOMDES", "P_CARDES", "P_NUANN", "P_NUEMI" }).declareParameters(new SqlParameter[] { new SqlParameter("P_COUSEMOD", 12), new SqlParameter("P_DEPDES", 12), new SqlParameter("P_NOMDES", 12), new SqlParameter("P_CARDES", 12), new SqlParameter("P_NUANN", 12), new SqlParameter("P_NUEMI", 12) });


    MapSqlParameterSource mapSqlParameterSource = (new MapSqlParameterSource()).addValue("P_COUSEMOD", datosInter.getCoUseMod()).addValue("P_DEPDES", datosInter.getDeDepDes()).addValue("P_NOMDES", datosInter.getDeNomDes()).addValue("P_CARDES", datosInter.getDeCarDes()).addValue("P_NUANN", datosInter.getNuAnn()).addValue("P_NUEMI", datosInter.getNuEmi());
    try {
      this.spInsMesaVirtual.execute((SqlParameterSource)mapSqlParameterSource);
      vReturn = "OK";
    } catch (Exception ex) {
      
      ex.printStackTrace();
    } 

    
    return vReturn;
  }


  
  public List<DestinatarioDocumentoEmiBean> getLstDestExternoGrupo(String pcoGrupo, String pTiDes) {
    StringBuffer sql = new StringBuffer();
    List<DestinatarioDocumentoEmiBean> list = new ArrayList<DestinatarioDocumentoEmiBean>();
    
    if (pTiDes.equals("02"))
    {
      
      sql.append("SELECT CPRO_RUC NURUC, CPRO_RAZSOC deProveedor,ISNULL(CPRO_DOMICIL,' ') cdirRemite,ISNULL(CPRO_TELEFO,' ') cTelefono,ISNULL(CPRO_EMAIL,' ') cexpCorreoe, \nISNULL(LTRIM(NODEP)+' '+LTRIM(NOPRV)+' '+LTRIM(NODIS),' ') UBIGEO, ISNULL(LTRIM(CUBI_CODDEP),' ') CCOD_DPTO,ISNULL(LTRIM(CUBI_CODPRO),' ') CCOD_PROV,ISNULL(LTRIM(CUBI_CODDIS),' ') CCOD_DIST\nFROM IDOSGD.LG_PRO_PROVEEDOR J LEFT JOIN IDOSGD.IDTUBIAS U ON J.CUBI_CODDEP+J.CUBI_CODPRO+J.CUBI_CODDIS=U.UBDEP+U.UBPRV+U.UBDIS \nWHERE J.CPRO_RUC IN (SELECT CO_DES FROM IDOSGD.TDTD_DEP_GRU_VAR WHERE CO_GRU_DES = ? AND Es_dep_gru = '1' AND TI_DES = ?)\norder by CPRO_RAZSOC");
    }

    
    if (pTiDes.equals("03"))
    {
      sql.append("SELECT C.NULEM nu_dni,ISNULL(substring(IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL(C.NULEM), 1, 100),' ') de_ciudadano,\nISNULL(C.DEDOMICIL,' ') CDIR_REMITE,ISNULL(C.DEEMAIL,' ') CEXP_CORREOE,ISNULL(C.DETELEFO,' ') CTELEFONO,ISNULL(C.UBDEP,' ') CCOD_DPTO,ISNULL(C.UBPRV,' ') CCOD_PROV,ISNULL(C.UBDIS,' ') CCOD_DIST,\nISNULL(LTRIM(NODEP)+' '+LTRIM(NOPRV)+' '+LTRIM(NODIS),' ') UBIGEO\nFROM IDOSGD.TDTX_ANI_SIMIL C LEFT JOIN IDOSGD.IDTUBIAS U ON C.UBDEP+C.UBPRV+C.UBDIS=U.UBDEP+U.UBPRV+U.UBDIS \nWHERE C.NULEM IN (SELECT CO_DES FROM IDOSGD.TDTD_DEP_GRU_VAR WHERE CO_GRU_DES = ? AND Es_dep_gru = '1' AND TI_DES = ?)\norder by 2 ");
    }


    
    if (pTiDes.equals("04"))
    {
      sql.append("SELECT O.CO_OTR_ORI co_otro_origen,O.DE_APE_PAT_OTR+' '+O.DE_APE_MAT_OTR+', '+O.DE_NOM_OTR + ' - ' +\nO.DE_RAZ_SOC_OTR +'##'+  ISNULL(S.CELE_DESELE,'   ') +'##'+  O.NU_DOC_OTR_ORI  de_otro_origen_full\nISNULL(O.DE_DIR_OTRO_ORI,' ') CDIR_REMITE,ISNULL(O.DE_EMAIL,' ') CEXP_CORREOE,ISNULL(O.DE_TELEFO,' ') CTELEFONO,ISNULL(O.UB_DEP,' ') CCOD_DPTO,ISNULL(O.UB_PRO,' ') CCOD_PROV,ISNULL(O.UB_DIS,' ') CCOD_DIST,\nISNULL(LTRIM(NODEP)+' '+LTRIM(NOPRV)+' '+LTRIM(NODIS),' ') UBIGEO\nFROM IDOSGD.TDTR_OTRO_ORIGEN O LEFT JOIN IDOSGD.IDTUBIAS U ON O.UB_DEP+O.UB_PRO+O.UB_DIS=U.UBDEP+U.UBPRV+U.UBDIS \nLEFT JOIN  (\nSELECT CELE_CODELE, CELE_DESELE  FROM IDOSGD.SI_ELEMENTO  WHERE CTAB_CODTAB ='TIP_DOC_IDENT') S\nON O.CO_TIP_OTR_ORI = S.CELE_CODELE \nWHERE O.CO_OTR_ORI IN (SELECT CO_DES FROM IDOSGD.TDTD_DEP_GRU_VAR WHERE CO_GRU_DES = ? AND Es_dep_gru = '1' AND TI_DES = ?)\norder by 2 ");
    }


    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinatarioDocumentoEmiBean.class), new Object[] { pcoGrupo, pTiDes });
    
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
}
