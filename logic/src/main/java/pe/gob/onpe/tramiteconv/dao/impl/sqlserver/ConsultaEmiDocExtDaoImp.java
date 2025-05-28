package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoExtConsulBean;
import pe.gob.onpe.tramitedoc.bean.DestinatarioDocumentoEmiConsulBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoExtConsulBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaDocExtRecepBean;
import pe.gob.onpe.tramitedoc.dao.ConsultaEmiDocExtDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;

@Repository("consultaEmiDocExtDao")
public class ConsultaEmiDocExtDaoImp
        extends SimpleJdbcDaoBase
        implements ConsultaEmiDocExtDao {

    public List<DocumentoExtConsulBean> getDocumentosExternos(BuscarDocumentoExtConsulBean buscarDocExt) {
         StringBuilder sql = new StringBuilder();
        boolean bBusqFiltro = false;
        boolean bBusqDep = false;
        String sqlContains = "";
        Map<String, Object> objectParam = new HashMap<String, Object>();

        List<DocumentoExtConsulBean> list = new ArrayList<DocumentoExtConsulBean>();

        sql.append(" SELECT TOP 50 X.*,"); // por lo mientras
        sql.append(" IDOSGD.PK_SGD_DESCRIPCION_TI_EMI_EMP (X.NU_ANN, X.NU_EMI) DE_ORI_EMI,");
        sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO (X.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,");
        sql.append(" (CASE WHEN X.NU_CANDES=1 THEN (SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_TI_DES_EMP (X.NU_ANN, X.NU_EMI))");
        sql.append(" ELSE (SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_TI_DES_EMP_V (X.NU_ANN, X.NU_EMI)) END)DE_EMP_DES,");
        sql.append(" IDOSGD.PK_SGD_DESCRIPCION_ESTADOS_MP (X.ES_DOC_EMI,'TDTV_REMITOS') DE_ES_DOC_EMI,");
        sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(X.CO_EMP_RES) DE_EMP_REC,");
        sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_LOCAL(X.CO_LOC_EMI) DE_LOC_EMI,");
        sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(X.CO_DEP) DE_DEPENDENCIA");
        sql.append(" FROM ( ");
        sql.append(" SELECT ");
        sql.append(" A.NU_ANN,A.NU_EMI,A.NU_COR_EMI,A.CO_TIP_DOC_ADM,");
        sql.append(" B.NU_DOC,IDOSGD.PK_SGD_DESCRIPCION_FORMAT(A.FE_EMI,'DD/MM/YYYY') FE_EMI_CORTA,");
        sql.append(" A.DE_ASU,A.NU_CANDES,A.ES_DOC_EMI,A.CO_EMP_RES,");
        sql.append(" A.NU_DIA_ATE,B.NU_EXPEDIENTE,B.IN_EXISTE_DOC EXISTE_DOC,B.IN_EXISTE_ANEXO EXISTE_ANEXO,");
        sql.append(" IDOSGD.PK_SGD_DESCRIPCION_FORMAT(C.FE_VENCE,'DD/MM/YYYY') FE_EXP_VENCE_CORTA, A.CO_LOC_EMI,");
        sql.append(" A.CO_DEP,  case when  B.CO_PROCESO_EXP='0000' then 'SIN TUPA' ELSE P.DE_NOMBRE END AS TUPA,");
        sql.append(" case when  B.CO_PROCESO_EXP='0000' then ' ' ELSE P.DE_DESCRIPCION END AS DESCRIPCION_TUPA ");
        sql.append(" FROM IDOSGD.TDTV_REMITOS A WITH(NOLOCK) ");
        sql.append(" INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN B WITH(NOLOCK) ON A.NU_ANN=B.NU_ANN AND A.NU_EMI=B.NU_EMI ");
        sql.append(" left join IDOSGD.TDTR_PROCESOS_EXP p WITH(NOLOCK) on p.CO_PROCESO =B.CO_PROCESO_EXP and TI_PROCESO='1'");
        //left join IDOSGD.TDTR_PROCESOS_EXP p on (p.CO_PROCESO =B.CO_PROCESO_EXP and TI_PROCESO='1'
        sql.append(" INNER JOIN IDOSGD.TDTC_EXPEDIENTE C WITH(NOLOCK) ON C.NU_ANN_EXP=A.NU_ANN_EXP AND C.NU_SEC_EXP=A.NU_SEC_EXP ");
        sql.append(" WHERE 1=1 ");
        String pnuAnn = buscarDocExt.getCoAnnio();
        if (!buscarDocExt.getEsFiltroFecha().equals("1") || !pnuAnn.equals("0")) {
            sql.append(" AND A.NU_ANN = :pnuAnn");

            objectParam.put("pnuAnn", pnuAnn);
        }
        sql.append(" AND A.CO_GRU='3'");
        sql.append(" AND A.ES_ELI = '0'");

        String pTipoBusqueda = buscarDocExt.getTipoBusqueda();
        if (pTipoBusqueda.equals("1") && buscarDocExt.isEsIncluyeFiltro()) {
            bBusqFiltro = true;
        }

        String auxTipoAcceso = buscarDocExt.getTiAcceso();
        String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1"))) ? auxTipoAcceso : "1";
        if (tiAcceso.equals("1")) {
            sql.append(" AND A.CO_EMP_RES = :pcoEmpRes ");
            objectParam.put("pcoEmpRes", buscarDocExt.getCoEmpleado());
        }
        if (tiAcceso.equals("2")) {
            bBusqDep = true;
            sql.append(" AND A.CO_DEP = :pCoDep");
            objectParam.put("pCoDep", buscarDocExt.getCoDependencia());
        }
        // si tipo de permiso es cero tiene acceso total TI_ACCESO_MP=0,
        //el permiso trazabilidad prevalece sobre el permiso de acceso total si lo tuviera

        // if (pTipoBusqueda.equals("0") || bBusqFiltro) {
        if (buscarDocExt.getTipoDoc() != null && buscarDocExt.getTipoDoc().trim().length() > 0) {
            sql.append(" AND A.CO_TIP_DOC_ADM = :pCoDocEmi ");
            objectParam.put("pCoDocEmi", buscarDocExt.getTipoDoc());
        }
        if (buscarDocExt.getEstadoDoc() != null && buscarDocExt.getEstadoDoc().trim().length() > 0) {
            sql.append(" AND A.ES_DOC_EMI = :pEsDocEmi ");
            objectParam.put("pEsDocEmi", buscarDocExt.getEstadoDoc());
        }
        if (buscarDocExt.getCoTipoRemite() != null && buscarDocExt.getCoTipoRemite().trim().length() > 0) {
            sql.append(" AND A.TI_EMI = :pcoTipoRemi ");
            objectParam.put("pcoTipoRemi", buscarDocExt.getCoTipoRemite());
        }

        if (buscarDocExt.getCoDepOriRec() != null && buscarDocExt.getCoDepOriRec().trim().length() > 0 && !bBusqDep) {
            sql.append(" AND A.CO_DEP = :pCoDep");
            objectParam.put("pCoDep", buscarDocExt.getCoDepOriRec());
        }

        if (buscarDocExt.getCoProceso() != null && buscarDocExt.getCoProceso().trim().length() > 0) {
            if (buscarDocExt.getCoProceso().equals("CON_TUPA")) {
                sql.append(" AND B.CO_PROCESO_EXP != '0000' ");

            } else if (buscarDocExt.getCoProceso().equals("SIN_TUPA")) {
                sql.append(" AND B.CO_PROCESO_EXP ='0000' ");
            } else {
                sql.append(" AND B.CO_PROCESO_EXP = :pcoProceso ");
                objectParam.put("pcoProceso", buscarDocExt.getCoProceso());

            }

        }
        if (buscarDocExt.getCoDepDestino() != null && buscarDocExt.getCoDepDestino().trim().length() > 0) {
            sql.append(" AND CHARINDEX(:pTiEmiDes,B.TI_EMI_DES) > 0 ");
            objectParam.put("pTiEmiDes", buscarDocExt.getCoDepDestino());
        }
        if (buscarDocExt.getEsFiltroFecha() != null && (buscarDocExt.getEsFiltroFecha().equals("1") || buscarDocExt.getEsFiltroFecha().equals("3"))) {
            String vFeEmiIni = buscarDocExt.getFeEmiIni();
            String vFeEmiFin = buscarDocExt.getFeEmiFin();
            if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
                    .trim().length() > 0) {
                sql.append(" AND A.FE_EMI BETWEEN CONVERT(DATETIME, :pFeEmiIni, 103) AND CONVERT(DATETIME, :pFeEmiFin, 103) + 0.99999 ");
                objectParam.put("pFeEmiIni", vFeEmiIni);
                objectParam.put("pFeEmiFin", vFeEmiFin);
            }
        }
        // } 

        if (buscarDocExt.getBusNumEmision() != null && buscarDocExt.getBusNumEmision().trim().length() > 0) {
            sql.append(" AND A.NU_COR_EMI = :pnuCorEmi ");
            objectParam.put("pnuCorEmi", buscarDocExt.getBusNumEmision());
        }

        if (buscarDocExt.getBusNumDoc() != null && buscarDocExt.getBusNumDoc().trim().length() > 1) {
            sql.append(" AND B.NU_DOC LIKE '%'+:pnuDocEmi+'%' ");
            objectParam.put("pnuDocEmi", buscarDocExt.getBusNumDoc());
        }

        if (buscarDocExt.getBusNumExpediente() != null && buscarDocExt.getBusNumExpediente().trim().length() > 1) {
            sql.append(" AND B.NU_EXPEDIENTE LIKE '%'+:pnuExpediente+'%' ");
            objectParam.put("pnuExpediente", buscarDocExt.getBusNumExpediente());
        }

        if (buscarDocExt.getBusAsunto() != null && buscarDocExt.getBusAsunto().trim().length() > 1) {
            sql.append(" AND UPPER(A.DE_ASU) LIKE '%'+:pDeAsunto+'%' ");
            objectParam.put("pDeAsunto", buscarDocExt.getBusAsunto());
        }

        if (buscarDocExt.getCoTipoExp() != null && !buscarDocExt.getCoTipoExp().equals("")) {
            sql.append(" AND C.CCOD_TIPO_EXP = :codTipoExp");
            objectParam.put("codTipoExp", buscarDocExt.getCoTipoExp());
        }

        if (buscarDocExt.getCoOriDoc() != null && !buscarDocExt.getCoOriDoc().equals("")) {
            sql.append(" AND A.CCOD_ORIGING = :coOriDoc");
            objectParam.put("coOriDoc", buscarDocExt.getCoOriDoc());
        }

        if (buscarDocExt.getBusResultado().equals("1")) {
            if (buscarDocExt.getCoTipoPersona().equals("03")) {
                sql.append(" AND A.NU_DNI_EMI = :pNumDni");
                objectParam.put("pNumDni", buscarDocExt.getBusNumDni());
            } else if (buscarDocExt.getCoTipoPersona().equals("02")) {
                sql.append(" AND A.NU_RUC_EMI = :pNumRuc");
                objectParam.put("pNumRuc", buscarDocExt.getBusNumRuc());
            } else if (buscarDocExt.getCoTipoPersona().equals("04")) {
                sql.append(" AND A.CO_OTR_ORI_EMI = :pCoOtr");
                objectParam.put("pCoOtr", buscarDocExt.getBusCoOtros());
            }
        }

        sql.append(") X ");
        sql.append(" ORDER BY X.NU_COR_EMI DESC");

        try {
            list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoExtConsulBean.class));
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public DocumentoExtConsulBean getDocumentoExtConsulBean(String nuAnn, String nuEmi) {
         StringBuilder sql = new StringBuilder();
        sql.append("SELECT A.NU_ANN,A.NU_EMI,A.NU_COR_EMI,B.NU_EXPEDIENTE,\nCONVERT(VARCHAR(10),B.FE_EXP,103)FE_EXP_CORTA,IDOSGD.PK_SGD_DESCRIPCION_DE_PROCESO_EXP(B.CO_PROCESO) DE_PROCESO_EXP,IDOSGD.PK_SGD_DESCRIPCION_TI_EMI_EMP (A.NU_ANN, A.NU_EMI) DE_ORI_EMI,\nIDOSGD.PK_SGD_DESCRIPCION_DE_LOCAL(A.CO_LOC_EMI) DE_LOC_EMI,IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO (A.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,\nA.DE_DOC_SIG NU_DOC,A.DE_ASU,A.NU_DIA_ATE,CONVERT(VARCHAR(10),A.FE_EMI,103) FE_EMI_CORTA,\nIDOSGD.PK_SGD_DESCRIPCION_ESTADOS (A.ES_DOC_EMI,'TDTV_REMITOS') DE_ES_DOC_EMI,\nIDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(A.CO_EMP_RES) DE_EMP_REC\nFROM IDOSGD.TDTV_REMITOS A WITH(NOLOCK) LEFT JOIN IDOSGD.TDTC_EXPEDIENTE B WITH(NOLOCK)\nON A.NU_ANN_EXP = B.NU_ANN_EXP AND A.NU_SEC_EXP = B.NU_SEC_EXP\nWHERE\nA.NU_ANN=?\nAND A.NU_EMI=?\nAND A.ES_ELI='0'\nAND A.CO_GRU='3'");

        DocumentoExtConsulBean docExt = new DocumentoExtConsulBean();
        try {
            docExt = (DocumentoExtConsulBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoExtConsulBean.class), new Object[]{nuAnn, nuEmi});
        } catch (EmptyResultDataAccessException e) {
            docExt = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return docExt;
    }

    public List<ReferenciaDocExtRecepBean> getLsRefDocExterno(String pnuAnn, String pnuEmi) {
        List<ReferenciaDocExtRecepBean> list;
         StringBuilder sql = new StringBuilder();

        sql.append("SELECT A.NU_ANN,A.NU_EMI,IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(A.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,\nC.NU_DOC de_doc_sig,\nC.NU_EXPEDIENTE,\nCONVERT(VARCHAR(10),A.FE_EMI,103)FE_EMI_CORTA\nFROM IDOSGD.TDTV_REMITOS A WITH(NOLOCK) \nINNER JOIN IDOSGD.TDTR_REFERENCIA B WITH(NOLOCK) ON A.NU_ANN=B.NU_ANN_REF AND A.NU_EMI=B.NU_EMI_REF \nINNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN C WITH(NOLOCK) ON C.NU_ANN=A.NU_ANN AND C.NU_EMI=A.NU_EMI \nWHERE B.NU_ANN=? AND \n B.NU_EMI=? ");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(ReferenciaDocExtRecepBean.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<DestinatarioDocumentoEmiConsulBean> getLsDestinoEmi(String pnuAnn, String pnuEmi) {
         StringBuilder sql = new StringBuilder();
        List<DestinatarioDocumentoEmiConsulBean> list = null;
        sql.append("select a.nu_ann,a.nu_emi,a.nu_des,a.co_loc_des co_local,substrING(IDOSGD.PK_SGD_DESCRIPCION_DE_LOCAL(a.co_loc_des), 1, 100) de_local,\na.co_dep_des co_dependencia,substrING(IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(a.co_dep_des), 1, 100) de_dependencia,\na.co_emp_des co_empleado,substrING(IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(a.co_emp_des), 1, 100) de_empleado,\na.co_mot co_tramite,substrING(IDOSGD.PK_SGD_DESCRIPCION_MOTIVO(a.co_mot), 1, 100) de_tramite,\na.co_pri co_prioridad,(SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_PRIORIDAD(a.co_pri)) de_prioridad,\na.de_pro de_indicaciones,\na.NU_RUC_DES nu_ruc,substrING(IDOSGD.PK_SGD_DESCRIPCION_DE_PROVEEDOR(a.NU_RUC_DES), 1, 100) de_proveedor,\na.NU_DNI_DES nu_dni,substrING(IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL(a.NU_DNI_DES), 1, 100) de_ciudadano,\na.CO_OTR_ORI_DES co_otro_origen,\nCOALESCE(\na.CO_OTR_ORI_DES,\n(SELECT C.DE_APE_PAT_OTR+' '+C.DE_APE_MAT_OTR+', '+C.DE_NOM_OTR + ' - ' +\n     C.DE_RAZ_SOC_OTR +'##'+\n     COALESCE(B.CELE_DESELE,'   ') +'##'+\n     C.NU_DOC_OTR_ORI  \n  FROM IDOSGD.TDTR_OTRO_ORIGEN C \n  LEFT OUTER JOIN \n  (   SELECT CELE_CODELE, CELE_DESELE     FROM IDOSGD.SI_ELEMENTO \n   WHERE CTAB_CODTAB ='TIP_DOC_IDENT') B\n ON C.CO_TIP_OTR_ORI = B.CELE_CODELE \n WHERE\n C.CO_OTR_ORI = a.CO_OTR_ORI_DES),\nNULL\n) de_otro_origen_full,\na.ti_des co_tipo_destino\nFROM IDOSGD.tdtv_destinos a\nwhere nu_ann = ? and nu_emi = ?\nAND ES_ELI='0' AND NU_EMI_REF is null\norder by 3");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DestinatarioDocumentoEmiConsulBean.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public String getRutaReporte(BuscarDocumentoExtConsulBean DocExt) {
        String vResult = "0";
        boolean bBusqDep = false;
         StringBuilder sql = new StringBuilder();

        try {
            sql.append(" A.NU_ANN=B.NU_ANN");
            sql.append(" AND A.NU_EMI=B.NU_EMI");
            sql.append(" AND C.NU_ANN_EXP=A.NU_ANN_EXP");
            sql.append(" AND C.NU_SEC_EXP=A.NU_SEC_EXP");
            String pnuAnn = DocExt.getCoAnnio();
            if (!DocExt.getEsFiltroFecha().equals("1") || !pnuAnn.equals("0")) {
                sql.append(" AND A.NU_ANN = '").append(pnuAnn).append("'");
            }
            sql.append(" AND A.CO_GRU='3'");
            sql.append(" AND A.ES_ELI = '0'");

            sql.append(" AND A.CO_DEP_EMI = '").append(DocExt.getCoDependencia()).append("'");

            String auxTipoAcceso = DocExt.getTiAcceso();
            String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1"))) ? auxTipoAcceso : "1";
            if (tiAcceso.equals("1")) {
                sql.append(" AND A.CO_EMP_RES = '").append(DocExt.getCoEmpleado()).append("'");
            } else if (DocExt.getInMesaPartes().equals("0")) {
                bBusqDep = true;
                sql.append(" AND A.CO_DEP = '").append(DocExt.getCoDependencia()).append("'");
            }

            if (DocExt.getTipoDoc() != null && DocExt.getTipoDoc().trim().length() > 0) {
                sql.append(" AND A.CO_TIP_DOC_ADM = '").append(DocExt.getTipoDoc()).append("'");
            }
            if (DocExt.getEstadoDoc() != null && DocExt.getEstadoDoc().trim().length() > 0) {
                sql.append(" AND A.ES_DOC_EMI = '").append(DocExt.getEstadoDoc()).append("'");
            }
            if (DocExt.getCoTipoRemite() != null && DocExt.getCoTipoRemite().trim().length() > 0) {
                sql.append(" AND A.TI_EMI = '").append(DocExt.getCoTipoRemite()).append("'");
            }
            if (DocExt.getCoDepOriRec() != null && DocExt.getCoDepOriRec().trim().length() > 0 && !bBusqDep) {
                sql.append(" AND A.CO_DEP = '").append(DocExt.getCoDepOriRec()).append("'");
            }

            if (DocExt.getCoProceso() != null && DocExt.getCoProceso().trim().length() > 0) {
                sql.append(" AND B.CO_PROCESO_EXP = '").append(DocExt.getCoProceso()).append("'");
            }
            if (DocExt.getCoDepDestino() != null && DocExt.getCoDepDestino().trim().length() > 0) {
                sql.append(" AND CHARINDEX('").append(DocExt.getCoDepDestino()).append("',B.TI_EMI_DES) > 0 ");
            }
            if (DocExt.getEsFiltroFecha() != null && (DocExt
                    .getEsFiltroFecha().equals("1") || DocExt.getEsFiltroFecha().equals("3"))) {
                String vFeEmiIni = DocExt.getFeEmiIni();
                String vFeEmiFin = DocExt.getFeEmiFin();
                if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
                        .trim().length() > 0) {
                    sql.append(" AND CAST(A.FE_EMI AS DATE) between CAST('").append(vFeEmiIni).append("' AS DATE) AND CAST('").append(vFeEmiFin)
                            .append("' AS DATE) ");
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

    public List<DocumentoExtConsulBean> getListaReporteBusqueda(BuscarDocumentoExtConsulBean buscarDocumentoExtConsulBean) {
         StringBuilder prutaReporte = new StringBuilder();
        Map<String, Object> objectParam = new HashMap<String, Object>();
        List<DocumentoExtConsulBean> list = null;
        boolean bBusqFiltro = false;
        boolean bBusqDep = false;
        String sqlContains = "";

        prutaReporte.append("SELECT  A.NU_COR_EMI, ");
        prutaReporte.append("     CONVERT(VARCHAR(10), A.FE_EMI, 103) FE_EMI_CORTA, ");
        prutaReporte.append("     (SELECT CDOC_DESDOC ");
        prutaReporte.append("     FROM IDOSGD.SI_MAE_TIPO_DOC ");
        prutaReporte.append("    WHERE CDOC_TIPDOC = A.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,B.NU_DOC, ");
        prutaReporte.append("     (select CELE_DESELE ");
        prutaReporte.append("     from IDOSGD.SI_ELEMENTO ");
        prutaReporte.append("     WHERE CTAB_CODTAB='TIP_DESTINO' ");
        prutaReporte.append("     AND CELE_CODELE=A.TI_EMI) tiRemitente , ");
        prutaReporte.append("     [IDOSGD].[PK_SGD_DESCRIPCION_TI_EMI_EMP](A.NU_ANN, A.NU_EMI) deRemitente, ");
        prutaReporte.append("     CASE A.NU_CANDES ");
        prutaReporte.append("     WHEN 1 THEN (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP](A.NU_ANN, A.NU_EMI)) ");
        prutaReporte.append("     ELSE (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP_V](A.NU_ANN, A.NU_EMI)) ");
        prutaReporte.append("     END deEmpPro, ");
        prutaReporte.append("     A.DE_ASU, ");
        // prutaReporte.append("     (SELECT DE_EST ");
        // prutaReporte.append("     FROM IDOSGD.TDTR_ESTADOS ");
        prutaReporte.append("     IDOSGD.PK_SGD_DESCRIPCION_ESTADOS_MP (A.ES_DOC_EMI,'TDTV_REMITOS') DE_ES_DOC_EMI, ");
        prutaReporte.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(A.CO_DEP) DE_DEPENDENCIA, ");
        prutaReporte.append("     A.NU_DIA_ATE, ");
        prutaReporte.append("     B.NU_EXPEDIENTE, ");
        prutaReporte.append("     CONVERT(VARCHAR(10), C.FE_VENCE, 103) FE_EXP_VENCE_CORTA, ");
//    prutaReporte.append("     (SELECT DE_NOMBRE ");
//    prutaReporte.append("     FROM IDOSGD.TDTR_PROCESOS_EXP ");
//    prutaReporte.append("     WHERE CO_PROCESO = B.CO_PROCESO_EXP ");
//    prutaReporte.append("     AND ES_ESTADO='1') DE_PROCESO_EXP, ");
        prutaReporte.append("   case when  B.CO_PROCESO_EXP='0000' then 'SIN TUPA' ELSE P.DE_NOMBRE END AS DE_PROCESO_EXP,");
        prutaReporte.append(" case when  B.CO_PROCESO_EXP='0000' then ' ' ELSE P.DE_DESCRIPCION END AS DESCRIPCION_TUPA, ");
        prutaReporte.append("     B.CO_PROCESO_EXP AS coProcesoExp, ");
        prutaReporte.append("     A.NU_FOLIOS AS nuFolios, ");
        prutaReporte.append("     [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](A.CO_EMP_RES) DE_EMP_REC ");
        prutaReporte.append(" FROM IDOSGD.TDTV_REMITOS A  With (readuncommitted) ");
        prutaReporte.append(" INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN B With (readuncommitted) ON A.NU_ANN=B.NU_ANN AND A.NU_EMI=B.NU_EMI ");
        prutaReporte.append(" left join IDOSGD.TDTR_PROCESOS_EXP p With (readuncommitted) on p.CO_PROCESO =B.CO_PROCESO_EXP and TI_PROCESO='1'");
        prutaReporte.append("INNER JOIN IDOSGD.TDTC_EXPEDIENTE C With (readuncommitted) ON  C.NU_ANN_EXP=A.NU_ANN_EXP AND C.NU_SEC_EXP=A.NU_SEC_EXP ");
        prutaReporte.append("WHERE 1=1 ");

        try {
            String pnuAnn = buscarDocumentoExtConsulBean.getCoAnnio();
            if (!buscarDocumentoExtConsulBean.getEsFiltroFecha().equals("1") || !pnuAnn.equals("0")) {
                prutaReporte.append(" AND A.NU_ANN = '").append(pnuAnn).append("'");
            }
            prutaReporte.append(" AND A.CO_GRU='3'");
            prutaReporte.append(" AND A.ES_ELI = '0'");

            //  prutaReporte.append(" AND A.CO_DEP_EMI = '").append(buscarDocumentoExtConsulBean.getCoDependencia()).append("'");
//         if(prutaReporte.getCoDepEmi() != null) {
//	 sql.append(" AND A.CO_DEP_EMI = :pCoDepEmi");
//      objectParam.put("pCoDepEmi", buscarDocExt.getCoDepEmi());	
//// }
            String auxTipoAcceso = buscarDocumentoExtConsulBean.getTiAcceso();
            String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1"))) ? auxTipoAcceso : "1";
            if (tiAcceso.equals("1")) {
                prutaReporte.append(" AND A.CO_EMP_RES = '").append(buscarDocumentoExtConsulBean.getCoEmpleado()).append("'");
            } else if (buscarDocumentoExtConsulBean.getInMesaPartes().equals("0")) {
                bBusqDep = true;
                prutaReporte.append(" AND A.CO_DEP = '").append(buscarDocumentoExtConsulBean.getCoDependencia()).append("'");
            }

            String pTipoBusqueda = buscarDocumentoExtConsulBean.getTipoBusqueda();
            if (pTipoBusqueda.equals("1") && buscarDocumentoExtConsulBean.isEsIncluyeFiltro()) {
                bBusqFiltro = true;
            }

            // if (pTipoBusqueda.equals("0") || bBusqFiltro) {
            if (buscarDocumentoExtConsulBean.getTipoDoc() != null && buscarDocumentoExtConsulBean.getTipoDoc().trim().length() > 0) {
                prutaReporte.append(" AND A.CO_TIP_DOC_ADM = '").append(buscarDocumentoExtConsulBean.getTipoDoc()).append("'");
            }
            if (buscarDocumentoExtConsulBean.getEstadoDoc() != null && buscarDocumentoExtConsulBean.getEstadoDoc().trim().length() > 0) {
                prutaReporte.append(" AND A.ES_DOC_EMI = '").append(buscarDocumentoExtConsulBean.getEstadoDoc()).append("'");
            }
            if (buscarDocumentoExtConsulBean.getCoTipoRemite() != null && buscarDocumentoExtConsulBean.getCoTipoRemite().trim().length() > 0) {
                prutaReporte.append(" AND A.TI_EMI = '").append(buscarDocumentoExtConsulBean.getCoTipoRemite()).append("'");
            }
            if (buscarDocumentoExtConsulBean.getCoDepOriRec() != null && buscarDocumentoExtConsulBean.getCoDepOriRec().trim().length() > 0 && !bBusqDep) {
                prutaReporte.append(" AND A.CO_DEP = '").append(buscarDocumentoExtConsulBean.getCoDepOriRec()).append("'");
            }

            if (buscarDocumentoExtConsulBean.getCoProceso() != null && buscarDocumentoExtConsulBean.getCoProceso().trim().length() > 0) {
                //    prutaReporte.append(" AND B.CO_PROCESO_EXP = '").append(buscarDocumentoExtConsulBean.getCoProceso()).append("'");

                if (buscarDocumentoExtConsulBean.getCoProceso().equals("CON_TUPA")) {
                    prutaReporte.append(" AND B.CO_PROCESO_EXP != '0000' ");

                } else if (buscarDocumentoExtConsulBean.getCoProceso().equals("SIN_TUPA")) {
                    prutaReporte.append(" AND B.CO_PROCESO_EXP ='0000' ");
                } else {
                    prutaReporte.append(" AND B.CO_PROCESO_EXP ='").append(buscarDocumentoExtConsulBean.getCoProceso()).append("'");

                }

            }
            if (buscarDocumentoExtConsulBean.getCoDepDestino() != null && buscarDocumentoExtConsulBean.getCoDepDestino().trim().length() > 0) {
                prutaReporte.append(" AND CHARINDEX('").append(buscarDocumentoExtConsulBean.getCoDepDestino()).append("',B.TI_EMI_DES) > 0 ");
            }
            if (buscarDocumentoExtConsulBean.getEsFiltroFecha() != null && (buscarDocumentoExtConsulBean
                    .getEsFiltroFecha().equals("1") || buscarDocumentoExtConsulBean.getEsFiltroFecha().equals("3"))) {
                String vFeEmiIni = buscarDocumentoExtConsulBean.getFeEmiIni();
                String vFeEmiFin = buscarDocumentoExtConsulBean.getFeEmiFin();
                if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin.trim().length() > 0) {
                    prutaReporte.append(" AND CAST(A.FE_EMI AS DATE) between CAST('").append(vFeEmiIni).append("' AS DATE) AND CAST('").append(vFeEmiFin)
                            .append("' AS DATE) ");
                }
            }
            // } 

            if (buscarDocumentoExtConsulBean.getBusNumEmision() != null && buscarDocumentoExtConsulBean.getBusNumEmision().trim().length() > 0) {
                prutaReporte.append(" AND A.NU_COR_EMI = :pnuCorEmi ");
                objectParam.put("pnuCorEmi", buscarDocumentoExtConsulBean.getBusNumEmision());
            }

            if (buscarDocumentoExtConsulBean.getBusNumDoc() != null && buscarDocumentoExtConsulBean.getBusNumDoc().trim().length() > 1) {
                prutaReporte.append(" AND B.NU_DOC LIKE '%'+:pnuDocEmi+'%' ");
                objectParam.put("pnuDocEmi", buscarDocumentoExtConsulBean.getBusNumDoc());
            }

            if (buscarDocumentoExtConsulBean.getBusNumExpediente() != null && buscarDocumentoExtConsulBean.getBusNumExpediente().trim().length() > 1) {
                prutaReporte.append(" AND B.NU_EXPEDIENTE LIKE '%'+:pnuExpediente+'%' ");
                objectParam.put("pnuExpediente", buscarDocumentoExtConsulBean.getBusNumExpediente());
            }

            if (buscarDocumentoExtConsulBean.getBusAsunto() != null && buscarDocumentoExtConsulBean.getBusAsunto().trim().length() > 1) {
                prutaReporte.append(" AND CONTAINS(A.DE_ASU, :pBusquedaTextual) ");
                sqlContains = "SELECT RETORNO FROM [IDOSGD].PK_SGD_DESCRIPCION_QUERYFILTER('" + buscarDocumentoExtConsulBean.getBusAsunto() + "')";
            }
            if (buscarDocumentoExtConsulBean.getCoTipoExp() != null && !buscarDocumentoExtConsulBean.getCoTipoExp().equals("")) {
                prutaReporte.append(" AND C.CCOD_TIPO_EXP = :codTipoExp");
                objectParam.put("codTipoExp", buscarDocumentoExtConsulBean.getCoTipoExp());
            }

            if (buscarDocumentoExtConsulBean.getCoOriDoc() != null && !buscarDocumentoExtConsulBean.getCoOriDoc().equals("")) {
                prutaReporte.append(" AND A.CCOD_ORIGING = :coOriDoc");
                objectParam.put("coOriDoc", buscarDocumentoExtConsulBean.getCoOriDoc());
            }

            if (buscarDocumentoExtConsulBean.getBusResultado().equals("1")) {
                if (buscarDocumentoExtConsulBean.getCoTipoPersona().equals("03")) {
                    prutaReporte.append(" AND A.NU_DNI_EMI = :pNumDni");
                    objectParam.put("pNumDni", buscarDocumentoExtConsulBean.getBusNumDni());
                } else if (buscarDocumentoExtConsulBean.getCoTipoPersona().equals("02")) {
                    prutaReporte.append(" AND A.NU_RUC_EMI = :pNumRuc");
                    objectParam.put("pNumRuc", buscarDocumentoExtConsulBean.getBusNumRuc());
                } else if (buscarDocumentoExtConsulBean.getCoTipoPersona().equals("04")) {
                    prutaReporte.append(" AND A.CO_OTR_ORI_EMI = :pCoOtr");
                    objectParam.put("pCoOtr", buscarDocumentoExtConsulBean.getBusCoOtros());
                }
            }

            prutaReporte.append(" ORDER BY A.NU_COR_EMI DESC ");

            if (sqlContains.length() > 0) {
                String cadena = (String) this.jdbcTemplate.queryForObject(sqlContains, String.class);
                objectParam.put("pBusquedaTextual", cadena);
            }
            list = this.namedParameterJdbcTemplate.query(prutaReporte.toString(), objectParam, (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoExtConsulBean.class));
        } catch (Exception ex) {
            String vResult = "1" + ex.getMessage();
            ex.printStackTrace();
        }
        return list;
    }
}
