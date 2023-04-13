package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoCargaMsjBean;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.bean.DescargaMensajeBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoAnexoDesBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoObjBean;
import pe.gob.onpe.tramitedoc.bean.ElementoMensajeroBean;
import pe.gob.onpe.tramitedoc.bean.EstadoDocumentoBean;
import pe.gob.onpe.tramitedoc.bean.MensajesConsulBean;
import pe.gob.onpe.tramitedoc.bean.SiElementoBean;
import pe.gob.onpe.tramitedoc.dao.MensajesDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;

@Repository("mensajesDao")
public class MensajesDaoImp
        extends SimpleJdbcDaoBase
        implements MensajesDao {

    private SimpleJdbcCall spDescargamsj;

    public List<EstadoDocumentoBean> listEstadosMsj(String nomTabla) {
         StringBuilder sql = new StringBuilder();

        sql.append("SELECT DE_EST_MP DE_EST,CO_EST \nFROM IDOSGD.TDTR_ESTADOS \nWHERE DE_TAB=?\nAND DE_EST_MP IS NOT NULL UNION SELECT '.: TODOS :.',NULL  \nORDER BY CO_EST");

        List<EstadoDocumentoBean> list = new ArrayList<EstadoDocumentoBean>();

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) ParameterizedBeanPropertyRowMapper.newInstance(EstadoDocumentoBean.class), new Object[]{nomTabla});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }

        return list;
    }

    public List<SiElementoBean> getLsSiElementoBean(String pctabCodtab) {
         StringBuilder sql = new StringBuilder();
        sql.append("SELECT CELE_DESELE,CELE_CODELE,NELE_NUMSEC,CELE_DESCOR\nFROM   IDOSGD.SI_ELEMENTO\nWHERE  CTAB_CODTAB = ? UNION SELECT '.: TODOS :.',NULL,NULL,NULL  \nORDER BY CELE_CODELE");

        List<SiElementoBean> list = null;
        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(SiElementoBean.class), new Object[]{pctabCodtab});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ElementoMensajeroBean> getListMensajero(String tipo, String Ambito) {
         StringBuilder sql = new StringBuilder();

        if (tipo.equals("COURRIER")) {

            if (!Ambito.equals(".: TODOS :.")) {
                sql.append("SELECT CPRO_RUC codigo ,CPRO_RAZSOC nombre FROM IDOSGD.LG_PRO_PROVEEDOR WHERE CPRO_RUC IN (  select CU.CELE_DESELE from IDOSGD.SI_ELEMENTO CU \n INNER JOIN IDOSGD.SI_ELEMENTO AM ON CU.cele_codele2=AM.cele_codele AND AM.CTAB_CODTAB='DE_AMBITO_MENS' \n WHERE CU.CTAB_CODTAB='DE_COURRIER' AND AM.cele_desele='" + Ambito + "'  ) ");

            } else {

                sql.append("SELECT CPRO_RUC codigo ,CPRO_RAZSOC nombre FROM IDOSGD.LG_PRO_PROVEEDOR WHERE CPRO_RUC IN (  select CU.CELE_DESELE from IDOSGD.SI_ELEMENTO CU \n INNER JOIN IDOSGD.SI_ELEMENTO AM ON CU.cele_codele2=AM.cele_codele AND AM.CTAB_CODTAB='DE_AMBITO_MENS' \n WHERE CU.CTAB_CODTAB='DE_COURRIER') ");

            }

        } else {

            sql.append("select NULEM codigo,DEAPP+' '+DEAPM+' '+DENOM nombre from IDOSGD.TDTX_ANI_SIMIL where NULEM in (select CELE_DESELE from IDOSGD.SI_ELEMENTO WHERE CTAB_CODTAB='" + tipo + "')");
        }
        List<ElementoMensajeroBean> list = new ArrayList<ElementoMensajeroBean>();
        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(ElementoMensajeroBean.class));
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }

        return list;
    }

    public List<EstadoDocumentoBean> listEstadosCarga(String nomTabla) {
         StringBuilder sql = new StringBuilder();

        sql.append("SELECT DE_EST,CO_EST \nFROM IDOSGD.TDTR_ESTADOS \nWHERE CO_EST!=2 AND DE_TAB=?\nAND DE_EST IS NOT NULL AND DE_EST_MP IS NOT NULL ORDER BY CO_EST");

        List<EstadoDocumentoBean> list = new ArrayList<EstadoDocumentoBean>();

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) ParameterizedBeanPropertyRowMapper.newInstance(EstadoDocumentoBean.class), new Object[]{nomTabla});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }

        return list;
    }

    public List<MensajesConsulBean> getBuscaDocumentosCarga(BuscarDocumentoCargaMsjBean buscarDocumentoCargaMsjBean) {
         StringBuilder sql = new StringBuilder();

        boolean bBusqFiltro = false;

        Map<String, Object> objectParam = new HashMap<String, Object>();

        List<MensajesConsulBean> list = new ArrayList<MensajesConsulBean>();

        sql.append("SELECT  ");
        sql.append("A.NU_MSJ,CONVERT(VARCHAR(20),M.FEC_ENVIOMSJ,103)   FEC_ENVIOMSJ, A.NU_ANN,A.NU_EMI,A.NU_DES,IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(B.CO_TIP_DOC_ADM)+ ' ' + ");
        sql.append(" (CASE B.TI_EMI WHEN '01' THEN B.NU_DOC_EMI + '-' + B.NU_ANN + '/' + B.DE_DOC_SIG ");
        sql.append(" WHEN '05' THEN B.NU_DOC_EMI+ '-'+ B.NU_ANN + '/' +B.DE_DOC_SIG ");
        sql.append(" ELSE  B.DE_DOC_SIG END )  ");
        sql.append("DE_TIP_DOC,UPPER(B.DE_ASU) DE_ASU,A.CO_LOC_DES CO_LOCAL, SUBSTRING(IDOSGD.PK_SGD_DESCRIPCION_DE_LOCAL(A.CO_LOC_DES), 1, 100)  DE_LOCAL, ");
        sql.append("B.CO_DEP_EMI CO_DEPENDENCIA,");
        sql.append(" SUBSTRING(IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(B.CO_DEP_EMI), 1, 100)  DE_DEPENDENCIA,");
        sql.append(" CASE A.TI_DES  WHEN '01'  THEN IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(A.CO_DEP_DES) +'-'+IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (A.CO_EMP_DES) ");
        sql.append("  WHEN '02'  THEN IDOSGD.PK_SGD_DESCRIPCION_DE_PROVEEDOR (A.NU_RUC_DES) ");
        sql.append("  WHEN '03'  THEN IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL (A.NU_DNI_DES) ");
        sql.append("  WHEN '04'  THEN IDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN (A.CO_OTR_ORI_DES) ");
        sql.append("  WHEN '05'  THEN IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(A.CO_DEP_DES) +'-'+IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (A.CO_EMP_DES) END DESTINATARIO, ");
        sql.append("A.CO_MOT CO_TRAMITE, SUBSTRING(IDOSGD.PK_SGD_DESCRIPCION_MOTIVO(A.CO_MOT), 1, 100)  DE_TRAMITE, ");
        sql.append("A.CO_PRI CO_PRIORIDAD, ");
        sql.append("A.DE_PRO DE_INDICACIONES, ");
        sql.append("A.TI_DES CO_TIPO_DESTINO,");
        sql.append("A.CDIR_REMITE direccion,");
        sql.append(" CASE A.TI_DES WHEN '01'  THEN RTRIM(LTRIM(DEP.NODEP))+'/'+RTRIM(LTRIM(DEP.NOPRV))+'/'+RTRIM(LTRIM(DEP.NODIS)) ");
        sql.append("   WHEN '02'  THEN RTRIM(LTRIM(UBIDESTINO.NODEP))+'/'+RTRIM(LTRIM(UBIDESTINO.NOPRV))+'/'+RTRIM(LTRIM(UBIDESTINO.NODIS)) ");
        sql.append("   WHEN '03'  THEN RTRIM(LTRIM(UBIDESTINO.NODEP))+'/'+RTRIM(LTRIM(UBIDESTINO.NOPRV))+'/'+RTRIM(LTRIM(UBIDESTINO.NODIS)) ");
        sql.append("   WHEN '04'  THEN RTRIM(LTRIM(UBIDESTINO.NODEP))+'/'+RTRIM(LTRIM(UBIDESTINO.NOPRV))+'/'+RTRIM(LTRIM(UBIDESTINO.NODIS)) ");
        sql.append("   WHEN '05'  THEN RTRIM(LTRIM(DEP.NODEP))+'/'+RTRIM(LTRIM(DEP.NOPRV))+'/'+RTRIM(LTRIM(DEP.NODIS)) END DEPARTAMENTO, ");
        sql.append(" M.DE_TIP_MSJ,M.DE_TIP_ENV,");
        sql.append("(CASE WHEN M.DE_TIP_MSJ='COURRIER' THEN (SELECT LTRIM(CPRO_RAZSOC) FROM IDOSGD.LG_PRO_PROVEEDOR WHERE CPRO_RUC=M.RE_ENV_MSJ) ELSE (SELECT LTRIM(CEMP_APEPAT)+' '+LTRIM(CEMP_APEMAT) +' '+ LTRIM(CEMP_DENOM) FROM IDOSGD.RHTM_PER_EMPLEADOS WHERE CEMP_NU_DNI=M.RE_ENV_MSJ) END) RE_ENV_MSJ, ");
        sql.append("M.NU_SER_MSJ+'-'+M.AN_SER_MSJ NU_SERVICIO,");
        sql.append("ISNULL(CONVERT(VARCHAR(20),A.FE_ENT_MSJ,103),'') FE_ENT_MSJ, ");
        sql.append("ISNULL(CONVERT(VARCHAR(20),A.FE_DEV_MSJ,103),'') FE_DEV_MSJ, ");
        sql.append("(SELECT DE_EST FROM IDOSGD.TDTR_ESTADOS WHERE CO_EST=A.ES_DOC_REC AND DE_TAB='TD_MENSAJERIA') EST_MSJ,A.ES_DOC_REC COD_EST_MSJ,M.DE_AMBITO,CONVERT(VARCHAR(20),M.FE_PLA_MSJ,103)  FE_PLA_MSJ,A.ES_PEN_MSJ,ISNULL(M.DE_OBS_MSJ, ' ')+'  '+ISNULL(A.OB_MSJ,' ') OB_MSJ,ISNULL(A.MO_MSJ_DEV,' ') MO_MSJ_DEV, ");
        sql.append("( CAST( M.FE_PLA_MSJ AS DATETIME)- CAST(M.FEC_ENVIOMSJ AS DATETIME) )+1 DIA_PLA,");
        sql.append("(CASE WHEN A.ES_DOC_REC=2 THEN IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,GETDATE()) ELSE ");
        sql.append(" IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,A.FE_ENT_MSJ)  END) DIA_TRA,");
        sql.append("(CASE WHEN M.PE_ENV_MSJ='1' AND (IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,M.FE_PLA_MSJ)- IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,GETDATE()))>0 AND A.ES_DOC_REC='2' ");
        sql.append("THEN (IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,M.FE_PLA_MSJ))-(IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,GETDATE())) ELSE ");
        sql.append("0 END) DIA_PEN, ");
        sql.append("(CASE WHEN M.PE_ENV_MSJ='1' AND (IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,M.FE_PLA_MSJ)-IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,GETDATE()))<0 AND A.ES_DOC_REC='2' ");
        sql.append("THEN (IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,GETDATE())-IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,M.FE_PLA_MSJ)) ELSE CASE WHEN M.PE_ENV_MSJ='1' AND (IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,M.FE_PLA_MSJ)-IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,A.FE_ENT_MSJ))<0 AND A.ES_DOC_REC='3' ");
        sql.append("THEN (IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,A.FE_ENT_MSJ)-IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,M.FE_PLA_MSJ)) ELSE CASE WHEN M.PE_ENV_MSJ='1' AND  (IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,M.FE_PLA_MSJ)-IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,A.FE_DEV_MSJ))<0 AND A.ES_DOC_REC='4' THEN (IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,A.FE_DEV_MSJ)-IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FEC_ENVIOMSJ,M.FE_PLA_MSJ)) ELSE 0 END END END) DIA_VEN, ");
        sql.append("(CASE WHEN A.ES_DOC_REC='3' AND M.PE_ENV_MSJ='1' AND IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FE_PLA_MSJ,A.FE_ENT_MSJ)>0 THEN IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(M.FE_PLA_MSJ,A.FE_ENT_MSJ) ELSE 0 END) DIA_ENT,");
        sql.append("(CASE WHEN A.ES_DOC_REC='4' AND M.PE_ENV_MSJ='1' AND IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(A.FE_PLA_DEV,A.FE_DEV_MSJ)>0 THEN IDOSGD.PK_SGD_MENSAJERIA_CAL_DIF_FECHAS_HABILES(A.FE_PLA_DEV,A.FE_DEV_MSJ) ELSE 0 END) DIA_DEV,");
        sql.append("B.ES_DOC_EMI,ISNULL(CONVERT(VARCHAR(20),A.FE_PLA_DEV,103),' ') FE_PLA_DEV, ");
        sql.append("ISNULL( CONVERT(VARCHAR(20),B.FE_ENV_MES,103),' ') FE_ENV_MES,ISNULL(CONVERT(VARCHAR(20),B.FE_EMI,103),' ') FE_EMI, ");
        sql.append("ISNULL(CONVERT(VARCHAR(20),B.FEC_RECEPMP,103),' ') FEC_RECEPMP, ");
        sql.append("ISNULL(A.nro_guia_devolucion, ' ') as nro_guia_devolucion ");
        sql.append("FROM IDOSGD.TDTV_DESTINOS A WITH(NOLOCK) INNER JOIN IDOSGD.TDTV_REMITOS B WITH(NOLOCK) ON A.NU_ANN = B.NU_ANN AND A.NU_EMI = B.NU_EMI ");
        sql.append("LEFT JOIN ( SELECT D.CO_DEPENDENCIA,D.DE_DEPENDENCIA,D.DE_CORTA_DEPEN,L.DE_NOMBRE_LOCAL,L.DE_DIRECCION_LOCAL,NODEP,NOPRV,NODIS ,L.CCOD_LOCAL ");
        sql.append("FROM IDOSGD.RHTM_DEPENDENCIA D WITH(NOLOCK) ");
        sql.append("INNER JOIN IDOSGD.SITM_LOCAL_DEPENDENCIA DL WITH(NOLOCK) ON DL.CO_DEP=D.CO_DEPENDENCIA ");
        sql.append("INNER JOIN IDOSGD.SI_MAE_LOCAL L WITH(NOLOCK) ON L.CCOD_LOCAL=DL.CO_LOC ");
        sql.append("LEFT JOIN IDOSGD.IDTUBIAS U WITH(NOLOCK) ON U.UBDEP+U.UBPRV+U.UBDIS =L.CO_UBIGEO  ) DEP ON DEP.CO_DEPENDENCIA=A.co_dep_DES AND DEP.CCOD_LOCAL=a.co_loc_des ");
        sql.append("LEFT JOIN IDOSGD.IDTUBIAS UBIDESTINO WITH(NOLOCK) ON UBIDESTINO.UBDEP+UBIDESTINO.UBPRV+UBIDESTINO.UBDIS =a.CCOD_DPTO+a.CCOD_PROV+a.CCOD_DIST  ");
        sql.append("INNER JOIN IDOSGD.TD_MENSAJERIA M WITH(NOLOCK) ON A.NU_MSJ=M.NU_MSJ ");
        sql.append(" WHERE B.TI_ENV_MSJ='0' AND B.COD_DEP_MSJ ='" + buscarDocumentoCargaMsjBean.getCoDependencia() + "' ");
        String pNUAnn = buscarDocumentoCargaMsjBean.getCoAnnio();

        String pTipoBusqueda = buscarDocumentoCargaMsjBean.getTipoBusqueda();

         StringBuilder sqlQry2 = new StringBuilder();
        sqlQry2.append("SELECT CELE_DESELE FROM IDOSGD.SI_ELEMENTO WHERE CTAB_CODTAB='MOTORIZADO' AND CELE_DESELE IN (SELECT CEMP_NU_DNI FROM IDOSGD.RHTM_PER_EMPLEADOS  WHERE CEMP_CODEMP='" + buscarDocumentoCargaMsjBean.getCoEmpleado() + "')");

        String siExisteDni = "";

        try {
            siExisteDni = (String) this.jdbcTemplate.queryForObject(sqlQry2.toString(), String.class);
        } catch (Exception e) {

            siExisteDni = "";
        }

        if (pTipoBusqueda.equals("1") && buscarDocumentoCargaMsjBean.isEsIncluyeFiltro()) {
            bBusqFiltro = true;
        }

        String pEsFiltroFecha = buscarDocumentoCargaMsjBean.getEsFiltroFecha();
        if (!pEsFiltroFecha.equals("1") || !pNUAnn.equals("0")) {
            sql.append(" AND A.NU_ANN = :pNuAnn");

            objectParam.put("pNuAnn", pNUAnn);
        }

        if (buscarDocumentoCargaMsjBean.getCoAmbitoMsj() != null && buscarDocumentoCargaMsjBean.getCoAmbitoMsj().trim().length() > 0 && !buscarDocumentoCargaMsjBean.getCoAmbitoMsj().equals(".: TODOS :.")) {
            sql.append(" AND M.DE_AMBITO= :pAmbito ");
            objectParam.put("pAmbito", buscarDocumentoCargaMsjBean.getCoAmbitoMsj());
        }

        if (buscarDocumentoCargaMsjBean.getCoTipoEnvMsj() != null && buscarDocumentoCargaMsjBean.getCoTipoEnvMsj().trim().length() > 0 && !buscarDocumentoCargaMsjBean.getCoTipoEnvMsj().equals(".: TODOS :.")) {
            sql.append(" AND M.DE_TIP_ENV= :pTipoEnv ");
            objectParam.put("pTipoEnv", buscarDocumentoCargaMsjBean.getCoTipoEnvMsj());
        }

        if (buscarDocumentoCargaMsjBean.getCoTipoMsj() != null && buscarDocumentoCargaMsjBean.getCoTipoMsj().trim().length() > 0 && !buscarDocumentoCargaMsjBean.getCoTipoMsj().equals(".: TODOS :.")) {
            sql.append(" AND M.DE_TIP_MSJ= :pTipoMsj ");
            objectParam.put("pTipoMsj", buscarDocumentoCargaMsjBean.getCoTipoMsj());
        }

        if (buscarDocumentoCargaMsjBean.getCoEstadoDoc() != null && buscarDocumentoCargaMsjBean.getCoEstadoDoc().trim().length() > 0 && !buscarDocumentoCargaMsjBean.getCoEstadoDoc().equals(".: TODOS :.")) {
            sql.append(" AND A.ES_DOC_REC= :pEstadoDoc ");
            objectParam.put("pEstadoDoc", buscarDocumentoCargaMsjBean.getCoEstadoDoc());
        } else {

            sql.append(" AND A.ES_DOC_REC IN ('2','3','4') ");
        }

        if (buscarDocumentoCargaMsjBean.getCoResponsable() != null && buscarDocumentoCargaMsjBean.getCoResponsable().trim().length() > 0 && !buscarDocumentoCargaMsjBean.getCoResponsable().equals("-1")) {
            sql.append(" AND RE_ENV_MSJ= :pResponsable ");
            objectParam.put("pResponsable", buscarDocumentoCargaMsjBean.getCoResponsable());
        }

        if (buscarDocumentoCargaMsjBean.getEsFiltroFecha() != null && (pEsFiltroFecha
                .equals("1") || pEsFiltroFecha.equals("3"))) {
            String vFeEmiIni = buscarDocumentoCargaMsjBean.getFeEmiIni();
            String vFeEmiFin = buscarDocumentoCargaMsjBean.getFeEmiFin();
            if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
                    .trim().length() > 0) {
                sql.append("AND M.FEC_ENVIOMSJ BETWEEN CONVERT(DATETIME, :pFeEmiIni, 103) AND CONVERT(DATETIME, :pFeEmiFin, 103) + 0.99999 ");
                objectParam.put("pFeEmiIni", vFeEmiIni);
                objectParam.put("pFeEmiFin", vFeEmiFin);
            }
        }

        String auxTipoAcceso = buscarDocumentoCargaMsjBean.getTiAcceso();
        String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1"))) ? auxTipoAcceso : "1";
        if (tiAcceso.equals("1")
                && siExisteDni.length() >= 8) {

            sql.append(" AND M.RE_ENV_MSJ= :pReEnvMsj ");
            objectParam.put("pReEnvMsj", siExisteDni);
        }

        if (buscarDocumentoCargaMsjBean.getBusNuMsj() != null && buscarDocumentoCargaMsjBean.getBusNuMsj().trim().length() > 0) {
            sql.append(" AND A.NU_MSJ= :pNuMsj ");
            objectParam.put("pNuMsj", buscarDocumentoCargaMsjBean.getBusNuMsj());
        }

        if (buscarDocumentoCargaMsjBean.getBusAnSerMsj() != null && buscarDocumentoCargaMsjBean.getBusAnSerMsj().trim().length() > 0) {
            sql.append(" AND M.AN_SER_MSJ= :pAnSerMsj ");
            objectParam.put("pAnSerMsj", buscarDocumentoCargaMsjBean.getBusAnSerMsj());
        }

        if (buscarDocumentoCargaMsjBean.getBusNuSerMsj() != null && buscarDocumentoCargaMsjBean.getBusNuSerMsj().trim().length() > 0) {
            sql.append(" AND M.NU_SER_MSJ LIKE '%'+:pNuSerMsj+'%' ");
            objectParam.put("pNuSerMsj", buscarDocumentoCargaMsjBean.getBusNuSerMsj());
        }

        if (buscarDocumentoCargaMsjBean.getBusDesti() != null && buscarDocumentoCargaMsjBean.getBusDesti().trim().length() > 0) {
            sql.append(" AND UPPER(CASE A.TI_DES WHEN '01' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(A.CO_DEP_DES) +'-'+IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (A.CO_EMP_DES) ");
            sql.append("  WHEN '02' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_PROVEEDOR (A.NU_RUC_DES) ");
            sql.append("  WHEN '03' THEN IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL (A.NU_DNI_DES) ");
            sql.append("  WHEN '04' THEN IDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN (A.CO_OTR_ORI_DES) ");
            sql.append("  WHEN '05' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(A.CO_DEP_DES) +'-'+IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (A.CO_EMP_DES) END) ");
            sql.append("  LIKE '%'+UPPER(:pDesti)+'%' ");

            objectParam.put("pDesti", buscarDocumentoCargaMsjBean.getBusDesti());
        }

        if (buscarDocumentoCargaMsjBean.getBusNuDoc() != null && buscarDocumentoCargaMsjBean.getBusNuDoc().trim().length() > 0) {
            String palabra = "";

            int cont = 0;
            String cadena = buscarDocumentoCargaMsjBean.getBusNuDoc();
            sql.append(" AND (");

            for (int i = 0; i < cadena.length(); i++) {

                if (cadena.charAt(i) == ' ') {

                    if (palabra.trim().length() > 0) {
                        if (cont == 0) {

                            sql.append("UPPER(IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(B.CO_TIP_DOC_ADM)+ ' ' + ");
                            sql.append(" (CASE B.TI_EMI  WHEN '01' THEN B.NU_DOC_EMI + '-' + B.NU_ANN + '/' + B.DE_DOC_SIG ");
                            sql.append("     WHEN '05' THEN B.NU_DOC_EMI + '-' + B.NU_ANN + '/' + B.DE_DOC_SIG ");
                            sql.append("     ELSE B.DE_DOC_SIG END )  ");
                            sql.append(") LIKE '%'+UPPER(:pNuDoc)+'%' ");
                            objectParam.put("pNuDoc", palabra.trim());
                        } else {

                            sql.append(" OR UPPER(IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(B.CO_TIP_DOC_ADM)+ ' ' + ");
                            sql.append(" (CASE B.TI_EMI WHEN '01' THEN  B.NU_DOC_EMI + '-' + B.NU_ANN + '/' + B.DE_DOC_SIG ");
                            sql.append("   WHEN '05' THEN B.NU_DOC_EMI + '-' + B.NU_ANN + '/' + B.DE_DOC_SIG ");
                            sql.append("   ELSE B.DE_DOC_SIG  END   ");
                            sql.append(")) LIKE '%'+UPPER(:pNuDoc)+'%' ");
                            objectParam.put("pNuDoc", palabra.trim());
                        }
                    }

                    cont++;
                    palabra = "";
                } else {

                    palabra = palabra + cadena.charAt(i);
                }
            }

            if (palabra.trim().length() > 0) {

                if (cont == 0) {

                    sql.append("UPPER(IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(B.CO_TIP_DOC_ADM)+ ' ' + (");
                    sql.append(" CASE B.TI_EMI WHEN '01' THEN B.NU_DOC_EMI + '-' + B.NU_ANN + '/' + B.DE_DOC_SIG ");
                    sql.append("   WHEN '05' THEN B.NU_DOC_EMI + '-' + B.NU_ANN + '/' + B.DE_DOC_SIG ");
                    sql.append("   ELSE B.DE_DOC_SIG  END   ");
                    sql.append(")) LIKE '%'+UPPER(:pNuDoc)+'%' ");
                    objectParam.put("pNuDoc", palabra.trim());
                } else {

                    sql.append(" OR UPPER(IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(B.CO_TIP_DOC_ADM)+ ' ' +  (");
                    sql.append(" CASE B.TI_EMI WHEN '01' THEN B.NU_DOC_EMI + '-' + B.NU_ANN + '/' + B.DE_DOC_SIG ");
                    sql.append(" WHEN '05' THEN B.NU_DOC_EMI + '-' + B.NU_ANN + '/' + B.DE_DOC_SIG ");
                    sql.append(" ELSE B.DE_DOC_SIG END  ");
                    sql.append(")) LIKE '%'+UPPER(:pNuDoc)+'%' ");
                    objectParam.put("pNuDoc", palabra.trim());
                }
            }

            sql.append(" ) ");
        }

        if (buscarDocumentoCargaMsjBean.getCoOficina() != null && buscarDocumentoCargaMsjBean.getCoOficina().trim().length() > 0 && !buscarDocumentoCargaMsjBean.getCoOficina().equals(".: TODOS :.")) {
            sql.append(" AND B.CO_DEP_EMI= :pOficina ");
            objectParam.put("pOficina", buscarDocumentoCargaMsjBean.getCoOficina());
        }

        sql.append("ORDER BY 3");

        try {
            list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper) BeanPropertyRowMapper.newInstance(MensajesConsulBean.class));
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public String updMensajeriaDocumento(DescargaMensajeBean descargaMensaje) {
        String vReturn = "NO_OK";

        if (descargaMensaje.getCo_EstadoDoc().equals("3")) {
            descargaMensaje.setMo_msj_dev("");
        }

        if (descargaMensaje.getEs_pen_msj().equals("0")) {

            descargaMensaje.setFe_pla_dev("");
            descargaMensaje.setEs_pen_msj("");
            descargaMensaje.setEs_pen_dev("");
        }

        this.spDescargamsj = (new SimpleJdbcCall(this.dataSource)).withProcedureName("[IDOSGD].[PK_SGD_MENSAJERIA_DESCARGAMSJ]").withoutProcedureColumnMetaDataAccess().useInParameterNames(new String[]{"pnu_ann", "pnu_emi", "pnu_des", "pnu_msj", "pfechaent", "phoraent", "pfechadev", "phoradev", "pob_msj", "pmo_msj_dev", "pes_pen_msj", "pes_pen_dev", "pest", "pnro_guia_devolucion", "pfepladev"}).declareParameters(new SqlParameter[]{
            new SqlParameter("pnu_ann", 12), new SqlParameter("pnu_emi", 12), new SqlParameter("pnu_des", 12), new SqlParameter("pnu_msj", 12), new SqlParameter("pfechaent", 12), new SqlParameter("phoraent", 12), new SqlParameter("pfechadev", 12), new SqlParameter("phoradev", 12), new SqlParameter("pob_msj", 12), new SqlParameter("pmo_msj_dev", 12),
            new SqlParameter("pes_pen_msj", 12), new SqlParameter("pes_pen_dev", 12), new SqlParameter("pest", 12), new SqlParameter("pnro_guia_devolucion", 12), new SqlParameter("pfepladev", 12)});

        MapSqlParameterSource mapSqlParameterSource = (new MapSqlParameterSource()).addValue("pnu_ann", descargaMensaje.getNu_ann()).addValue("pnu_emi", descargaMensaje.getNu_emi()).addValue("pnu_des", descargaMensaje.getNu_des()).addValue("pnu_msj", descargaMensaje.getNu_msj()).addValue("pfechaent", descargaMensaje.getFe_ent_msj()).addValue("phoraent", descargaMensaje.getHo_ent_msj()).addValue("pfechadev", descargaMensaje.getFe_dev_msj()).addValue("phoradev", descargaMensaje.getHo_dev_msj()).addValue("pob_msj", descargaMensaje.getOb_msj()).addValue("pmo_msj_dev", descargaMensaje.getMo_msj_dev()).addValue("pes_pen_msj", descargaMensaje.getEs_pen_msj()).addValue("pes_pen_dev", descargaMensaje.getEs_pen_dev()).addValue("pest", descargaMensaje.getCo_EstadoDoc()).addValue("pnro_guia_devolucion", descargaMensaje.getNro_guia_devolucion()).addValue("pfepladev", descargaMensaje.getFe_pla_dev());

        try {
            this.spDescargamsj.execute((SqlParameterSource) mapSqlParameterSource);
            vReturn = "OK";
        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return vReturn;
    }

    public String insArchivoAnexoDes(final DocumentoAnexoDesBean docAnexo, final InputStream archivoAnexo, final int size) {
        String vReturn = "NO_OK";

         StringBuilder sql = new StringBuilder();

        String pcount = getCount(docAnexo.getNuAnn(), docAnexo.getNuEmi(), docAnexo.getNuDes());

        if (pcount.equals("0")) {

            sql.append("insert into IDOSGD.tdtv_anexos(\nnu_ann,\nnu_emi,\nnu_ane,\nde_det,\nde_rut_ori,\nco_use_cre,\nfe_use_cre,\nco_use_mod,\nfe_use_mod,\nfeula, \nnu_des, \nbl_doc )\nvalues(?,?,?,?,?,?,GETDATE(),?,GETDATE(),CONVERT(VARCHAR(20),GETDATE(),112),?,?)");

            DefaultLobHandler defaultLobHandler = new DefaultLobHandler();

            try {
                this.jdbcTemplate.execute(sql.toString(), (PreparedStatementCallback) new AbstractLobCreatingPreparedStatementCallback((LobHandler) defaultLobHandler) {
                    protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                        ps.setString(1, docAnexo.getNuAnn());
                        ps.setString(2, docAnexo.getNuEmi());
                        ps.setString(3, docAnexo.getNuAne());
                        ps.setString(4, docAnexo.getDeDet());
                        ps.setString(5, docAnexo.getDeRutOri());
                        ps.setString(6, docAnexo.getCoUseCre());
                        ps.setString(7, docAnexo.getCoUseMod());
                        ps.setString(8, docAnexo.getNuDes());
                        lobCreator.setBlobAsBinaryStream(ps, 9, archivoAnexo, size);
                    }
                });
                vReturn = "OK";
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            sql.append("update IDOSGD.tdtv_anexos set\nde_det=?,\nde_rut_ori=?,\nco_use_cre=?,\nfe_use_cre=getdate(),\nco_use_mod=?,\nfe_use_mod=getdate(),\nfeula=CONVERT(VARCHAR(20),GETDATE(),112),\nbl_doc=?\nwhere \nnu_ann=? and nu_emi=? and nu_des=?");

            DefaultLobHandler defaultLobHandler = new DefaultLobHandler();

            try {
                this.jdbcTemplate.execute(sql.toString(), (PreparedStatementCallback) new AbstractLobCreatingPreparedStatementCallback((LobHandler) defaultLobHandler) {

                    protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                        ps.setString(1, docAnexo.getDeDet());
                        ps.setString(2, docAnexo.getDeRutOri());
                        ps.setString(3, docAnexo.getCoUseCre());
                        ps.setString(4, docAnexo.getCoUseMod());
                        lobCreator.setBlobAsBinaryStream(ps, 5, archivoAnexo, size);
                        ps.setString(6, docAnexo.getNuAnn());
                        ps.setString(7, docAnexo.getNuEmi());
                        ps.setString(8, docAnexo.getNuDes());
                    }
                });
                vReturn = "OK";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return vReturn;
    }

    public String getCount(String pnuAnn, String pnuEmi, String pnuDes) {
        String result = null;

         StringBuilder sql = new StringBuilder();
        sql.append("select count(*) \nfrom IDOSGD.tdtv_anexos \nwhere nu_ann = ? \nand nu_emi = ? \nand nu_des = ? ");

        try {
            result = (String) this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[]{pnuAnn, pnuEmi, pnuDes});
        } catch (Exception e) {
            result = "0";
            e.printStackTrace();
        }
        return result;
    }

    public String getUltimoAnexo(String pnuAnn, String pnuEmi) {
        String result = null;

         StringBuilder sql = new StringBuilder();
        sql.append("select CAST(ISNULL(max(nu_ane),0) + 1  AS VARCHAR(20)) nu_ane \nfrom IDOSGD.tdtv_anexos \nwhere nu_ann = ? \nand nu_emi = ? ");

        try {
            result = (String) this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[]{pnuAnn, pnuEmi});
        } catch (Exception e) {
            result = "1";
            e.printStackTrace();
        }
        return result;
    }

    public String delMensajeriaDocumento(DescargaMensajeBean descargaMensaje) {
        String vReturn = "NO_OK";

        this.spDescargamsj = (new SimpleJdbcCall(this.dataSource)).withProcedureName("[IDOSGD].[PK_SGD_MENSAJERIA_DELMSJ]").withoutProcedureColumnMetaDataAccess().useInParameterNames(new String[]{"pnu_ann", "pnu_emi", "pnu_des", "pnu_msj"}).declareParameters(new SqlParameter[]{new SqlParameter("pnu_ann", 12), new SqlParameter("pnu_emi", 12), new SqlParameter("pnu_des", 12), new SqlParameter("pnu_msj", 12)});

        MapSqlParameterSource mapSqlParameterSource = (new MapSqlParameterSource()).addValue("pnu_ann", descargaMensaje.getNu_ann()).addValue("pnu_emi", descargaMensaje.getNu_emi()).addValue("pnu_des", descargaMensaje.getNu_des()).addValue("pnu_msj", descargaMensaje.getNu_msj());

        try {
            this.spDescargamsj.execute((SqlParameterSource) mapSqlParameterSource);
            vReturn = "OK";
        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return vReturn;
    }

    public MensajesConsulBean getBuscaDocumentosMsj(String nu_ann, String nu_emi, String nu_des, String nu_msj) {
         StringBuilder sql = new StringBuilder();
        MensajesConsulBean mensajesConsulBean = new MensajesConsulBean();

        sql.append("SELECT  ");
        sql.append("A.NU_MSJ,CONVERT(VARCHAR(20),M.FEC_ENVIOMSJ,103)  FEC_ENVIOMSJ, A.NU_ANN,A.NU_EMI,A.NU_DES,IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(B.CO_TIP_DOC_ADM)+ ' ' +  ");
        sql.append(" (CASE B.TI_EMI WHEN '01' THEN B.NU_DOC_EMI + '-' + B.NU_ANN + '/' + B.DE_DOC_SIG ");
        sql.append(" WHEN '05' THEN B.NU_DOC_EMI + '-' + B.NU_ANN + '/' + B.DE_DOC_SIG ");
        sql.append(" ELSE B.DE_DOC_SIG END) DE_TIP_DOC,  ");
        sql.append("UPPER(B.DE_ASU) DE_ASU,A.CO_LOC_DES CO_LOCAL, SUBSTRING(IDOSGD.PK_SGD_DESCRIPCION_DE_LOCAL(A.CO_LOC_DES), 1, 100)  DE_LOCAL, ");
        sql.append("B.CO_DEP_EMI CO_DEPENDENCIA,");
        sql.append(" SUBSTRING(IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(B.CO_DEP_EMI), 1, 100)  DE_DEPENDENCIA,");
        sql.append(" CASE A.TI_DES WHEN '01' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(A.CO_DEP_DES) +'-'+IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (A.CO_EMP_DES) ");
        sql.append("  WHEN '02' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_PROVEEDOR (A.NU_RUC_DES) ");
        sql.append("  WHEN '03' THEN IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL (A.NU_DNI_DES) ");
        sql.append("  WHEN '04' THEN IDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN (A.CO_OTR_ORI_DES) ");
        sql.append("  WHEN '05' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(A.CO_DEP_DES) +'-'+IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (A.CO_EMP_DES) ");
        sql.append("  END DESTINATARIO, ");
        sql.append("A.CO_MOT CO_TRAMITE, SUBSTRING(IDOSGD.PK_SGD_DESCRIPCION_MOTIVO(A.CO_MOT), 1, 100)  DE_TRAMITE, ");
        sql.append("A.CO_PRI CO_PRIORIDAD, ");
        sql.append("A.DE_PRO DE_INDICACIONES, ");
        sql.append("A.TI_DES CO_TIPO_DESTINO,");
        sql.append(" CASE A.TI_DES WHEN '01' THEN LTRIM(DEP.DE_DIRECCION_LOCAL) ");
        sql.append(" WHEN '02' THEN 'DIRECCION PROVEEDOR' ");
        sql.append(" WHEN '03' THEN 'DIRECCION ASIMIL' ");
        sql.append(" WHEN '04' THEN 'DIRECCION OTROS' ");
        sql.append(" WHEN '05' THEN LTRIM(DEP.DE_DIRECCION_LOCAL) ");
        sql.append(" END DIRECCION, ");
        sql.append(" CASE A.TI_DES WHEN '01' THEN RTRIM(LTRIM(DEP.NODEP))+'/'+RTRIM(LTRIM(DEP.NOPRV))+'/'+RTRIM(LTRIM(DEP.NODIS)) ");
        sql.append(" WHEN '02' THEN 'DEPARTAMENTO PROVEEDOR' ");
        sql.append(" WHEN '03' THEN 'DEPARTAMENTO ASIMIL' ");
        sql.append(" WHEN '04' THEN 'DEPARTAMENTO OTROS' ");
        sql.append(" WHEN '05' THEN  RTRIM(LTRIM(DEP.NODEP))+'/'+RTRIM(LTRIM(DEP.NOPRV))+'/'+RTRIM(LTRIM(DEP.NODIS)) ");
        sql.append(" END   DEPARTAMENTO,M.DE_TIP_MSJ,M.DE_TIP_ENV,");

        sql.append("(CASE WHEN M.DE_TIP_MSJ='COURRIER' THEN (SELECT LTRIM(CPRO_RAZSOC) FROM IDOSGD.LG_PRO_PROVEEDOR WHERE CPRO_RUC=M.RE_ENV_MSJ) ELSE (SELECT LTRIM(CEMP_APEPAT)+' '+LTRIM(CEMP_APEMAT) +' '+ LTRIM(CEMP_DENOM) FROM IDOSGD.RHTM_PER_EMPLEADOS WHERE CEMP_NU_DNI=M.RE_ENV_MSJ) END) RE_ENV_MSJ, ");
        sql.append("M.NU_SER_MSJ+'-'+M.AN_SER_MSJ NU_SERVICIO,");
        sql.append("ISNULL(CONVERT(VARCHAR(20),A.FE_ENT_MSJ,103) ,' ') FE_ENT_MSJ, ");
        sql.append("ISNULL(CONVERT(VARCHAR(20),A.FE_DEV_MSJ,103)  ,' ') FE_DEV_MSJ,");
        sql.append("(SELECT DE_EST FROM IDOSGD.TDTR_ESTADOS WHERE CO_EST=A.ES_DOC_REC AND DE_TAB='TD_MENSAJERIA') EST_MSJ,A.ES_DOC_REC COD_EST_MSJ,M.DE_AMBITO,M.FE_PLA_MSJ,A.ES_PEN_MSJ,ISNULL(A.OB_MSJ,' ') OB_MSJ,ISNULL(A.MO_MSJ_DEV,' ') MO_MSJ_DEV, ");
        sql.append("ISNULL(CONVERT(VARCHAR(20),A.HO_ENT_MSJ,103) ,' ') HO_ENT_MSJ,");
        sql.append("ISNULL(CONVERT(VARCHAR(20),A.HO_DEV_MSJ,108),' ') HO_DEV_MSJ,");
        sql.append(" CASE WHEN TOTAL>0 THEN 'SI' ELSE 'NO' END tieneanexocargo,CONVERT(VARCHAR(20),B.FE_EMI,103)  FE_EMI,CONVERT(VARCHAR(20),B.FEC_RECEPMP,103)   FEC_RECEPMP,M.DIAS_PLA_DEVO,A.ES_PEN_DEV,M.PE_ENV_MSJ, ");
        sql.append(" A.nro_guia_devolucion ");
        sql.append("FROM IDOSGD.TDTV_DESTINOS A INNER JOIN IDOSGD.TDTV_REMITOS B ON A.NU_ANN = B.NU_ANN AND A.NU_EMI = B.NU_EMI ");
        sql.append(" LEFT JOIN ( SELECT D.CO_DEPENDENCIA,D.DE_DEPENDENCIA,D.DE_CORTA_DEPEN,L.DE_NOMBRE_LOCAL,L.DE_DIRECCION_LOCAL,NODEP,NOPRV,NODIS ,L.CCOD_LOCAL ");
        sql.append(" FROM IDOSGD.RHTM_DEPENDENCIA D ");
        sql.append(" INNER JOIN IDOSGD.SITM_LOCAL_DEPENDENCIA DL ON DL.CO_DEP=D.CO_DEPENDENCIA ");
        sql.append(" INNER JOIN IDOSGD.SI_MAE_LOCAL L ON L.CCOD_LOCAL=DL.CO_LOC ");
        sql.append(" LEFT JOIN IDOSGD.IDTUBIAS U ON U.UBDEP+U.UBPRV+U.UBDIS =L.CO_UBIGEO) DEP ON DEP.CO_DEPENDENCIA=A.CO_DEP_DES AND DEP.CCOD_LOCAL=A.CO_LOC_DES ");
        sql.append(" INNER JOIN IDOSGD.TD_MENSAJERIA M ON A.NU_MSJ=M.NU_MSJ ");
        sql.append(" LEFT JOIN ( select  nu_ann,nu_emi,nu_des, COUNT(1) TOTAL    from IDOSGD.tdtv_anexos  GROUP BY nu_ann,nu_emi,nu_des ) TB ON  TB.nu_ann = A.nu_ann  AND TB.nu_emi=A.nu_emi AND TB.nu_des=A.nu_des ");
        sql.append(" WHERE  A.NU_ANN =? AND A.NU_EMI =? AND A.NU_DES =? AND  A.NU_MSJ =? ");

        sql.append("ORDER BY 3");

        try {
            mensajesConsulBean = (MensajesConsulBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(MensajesConsulBean.class), new Object[]{nu_ann, nu_emi, nu_des, nu_msj});

        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {

        } catch (Exception e) {

            e.printStackTrace();
        }
        return mensajesConsulBean;
    }

    public DocumentoObjBean getNombreArchivoMsj(String pnuAnn, String pnuEmi, String pnuAnexo) {
         StringBuilder sql = new StringBuilder();
        sql.append("select \nnu_ann,\nnu_emi,\nnu_ane,\nde_rut_ori nombre_Archivo,\nISNULL(len(bl_doc),0) nu_Tamano\nfrom IDOSGD.tdtv_anexos\nwhere nu_ann = ?\nand nu_emi = ?\nand nu_des = ?");

        System.out.println("" + pnuAnn);
        System.out.println("" + pnuEmi);
        System.out.println("" + pnuAnexo);
        DocumentoObjBean docObjBean = new DocumentoObjBean();

        try {
            docObjBean = (DocumentoObjBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoObjBean.class), new Object[]{pnuAnn, pnuEmi, pnuAnexo});
        } catch (EmptyResultDataAccessException e) {
            docObjBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return docObjBean;
    }

    public String deleteMsj(DescargaMensajeBean descargaMensaje) {
        String vReturn = "NO_OK";
         StringBuilder sqlUpd = new StringBuilder();

        sqlUpd.append("DELETE FROM IDOSGD.TDTV_ANEXOS \nWHERE\nNU_ANN=? AND\nNU_EMI=? AND\nNU_DES=?");

        System.out.println(descargaMensaje.getNu_ann());
        System.out.println(descargaMensaje.getNu_emi());
        System.out.println(descargaMensaje.getNu_des());

        try {
            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{descargaMensaje.getNu_ann(), descargaMensaje.getNu_emi(), descargaMensaje
                .getNu_des()});

            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
            vReturn = e.getMessage();
        }

        return vReturn;
    }

    public String revMensajeriaDocumento(DescargaMensajeBean descargaMensaje) {
        String vReturn = "NO_OK";

        this.spDescargamsj = (new SimpleJdbcCall(this.dataSource)).withProcedureName("[IDOSGD].[PK_SGD_MENSAJERIA_REVMSJ]").withoutProcedureColumnMetaDataAccess().useInParameterNames(new String[]{"pnu_ann", "pnu_emi", "pnu_des", "pnu_msj"}).declareParameters(new SqlParameter[]{new SqlParameter("pnu_ann", 12), new SqlParameter("pnu_emi", 12), new SqlParameter("pnu_des", 12), new SqlParameter("pnu_msj", 12)});

        MapSqlParameterSource mapSqlParameterSource = (new MapSqlParameterSource()).addValue("pnu_ann", descargaMensaje.getNu_ann()).addValue("pnu_emi", descargaMensaje.getNu_emi()).addValue("pnu_des", descargaMensaje.getNu_des()).addValue("pnu_msj", descargaMensaje.getNu_msj());

        try {
            this.spDescargamsj.execute((SqlParameterSource) mapSqlParameterSource);
            System.out.println("se revirtio");
            vReturn = "OK";
        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return vReturn;
    }

    public List<DependenciaBean> getLsOficina() {
         StringBuilder sql = new StringBuilder();

        sql.append("SELECT deDependencia,coDependencia FROM (SELECT  SUBSTRING(IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(CO_DEP_EMI), 1, 100)  deDependencia,CO_DEP_EMI coDependencia \nFROM IDOSGD.TDTV_REMITOS \nWHERE CO_DEP_EMI IS NOT NULL \nUNION SELECT '.: TODOS :.',NULL \n  ) TB \nORDER BY deDependencia");

        List<DependenciaBean> list = new ArrayList<DependenciaBean>();

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) ParameterizedBeanPropertyRowMapper.newInstance(DependenciaBean.class));
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }

        return list;
    }

    public String selectCalFechaPlazo(DescargaMensajeBean descargaMensaje) {
        String vReturn = "NO_OK";
         StringBuilder sqlQry = new StringBuilder();
         StringBuilder sqlQry2 = new StringBuilder();

        try {
            sqlQry2.append("SET LANGUAGE Español select CONVERT(VARCHAR(20),IDOSGD.PK_SGD_CONSULTAS_CALCULAR_DIAS_HABILES( CONVERT(DATETIME,'" + descargaMensaje.getFe_ent_msj() + "')  ,1),103)    ");
            String snufechai = (String) this.jdbcTemplate.queryForObject(sqlQry2.toString(), String.class);

            int diadev = Integer.parseInt(descargaMensaje.getDi_pla_dev()) - 1;
            if (diadev < 0) {
                diadev = 0;
            }
            sqlQry.append("SET LANGUAGE Español select CONVERT(VARCHAR(20),IDOSGD.PK_SGD_CONSULTAS_CALCULAR_DIAS_HABILES(CONVERT(DATETIME,'" + snufechai + "') ," + diadev + ") ,103)    ");
            String snufecha = (String) this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class);
            descargaMensaje.setFe_pla_dev(snufecha);

            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vReturn;
    }

    public List<SiElementoBean> getLsMotivo(String pctabCodtab) {
         StringBuilder sql = new StringBuilder();
        sql.append("SELECT CELE_DESELE,CELE_CODELE,NELE_NUMSEC,CELE_DESELE+'|'+(CASE WHEN LEN(CELE_DESCOR)=0 OR CELE_DESCOR IS NULL THEN '0|0|0' ELSE CELE_DESCOR END) CELE_DESCOR\nFROM   IDOSGD.SI_ELEMENTO\nWHERE  CTAB_CODTAB = ? \nORDER BY CELE_CODELE");

        List<SiElementoBean> list = null;
        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(SiElementoBean.class), new Object[]{pctabCodtab});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public String insArchivoActa(MensajesConsulBean docAnexo, InputStream archivoAnexo, int size) {
        // TODO Auto-generated method stub
        return null;
    }
}
