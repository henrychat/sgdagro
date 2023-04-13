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
import pe.gob.onpe.tramitedoc.dao.ConsultaDocVoBoDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;

@Repository("consultaDocVoBoDao")
public class ConsultaDocVoBoDaoImp
        extends SimpleJdbcDaoBase
        implements ConsultaDocVoBoDao {

    public List<DocumentoVoBoBean> getLsDocsVoBo(BuscarDocumentoVoBoBean bDocVoBo) {
        List<DocumentoVoBoBean> list;
         StringBuilder sql = new StringBuilder();
        String sqlContains = "";
        boolean bBusqFiltro = false;
        String sOrdenList = "";
        Map<String, Object> objectParam = new HashMap<String, Object>();

        sql.append("SELECT TOP 100 RE.NU_ANN,RE.NU_EMI,\n");
        sql.append(" RE.NU_COR_EMI,CONVERT(VARCHAR(10), RE.FE_EMI, 103) FE_EMI_CORTA, \n");
        sql.append(" (SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_EMI_REF(RE.NU_ANN, RE.NU_EMI)) DE_EMI_REF,\n");
        sql.append(" (SELECT CDOC_DESDOC FROM IDOSGD.SI_MAE_TIPO_DOC WHERE CDOC_TIPDOC = RE.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,\n");
        sql.append(" RR.NU_DOC,UPPER(SUBSTRING(RE.DE_ASU,0,399)) DE_ASU,\n");
        sql.append(" (SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM FROM IDOSGD.RHTM_PER_EMPLEADOS WHERE CEMP_CODEMP = VB.CO_EMP_VB)  DE_EMP_VB,\n");
        sql.append(" (SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM FROM IDOSGD.RHTM_PER_EMPLEADOS WHERE CEMP_CODEMP = RE.CO_EMP_RES) DE_EMP_ELABORA,\n");
        sql.append(" RR.NU_EXPEDIENTE NRO_EXPEDIENTE, \n");
        sql.append(" (SELECT DE_EST FROM IDOSGD.TDTR_ESTADOS WHERE CO_EST + DE_TAB = (CASE WHEN VB.IN_VB='B' THEN '0' ELSE VB.IN_VB END) + 'TDTV_PERSONAL_VB') DE_ES_DOC_EMI,\n");
        sql.append(" RR.IN_EXISTE_DOC EXISTE_DOC,\n");
        sql.append(" (CASE \n");
        sql.append(" WHEN ISNULL(RR.TI_EMI_REF,'0')+ISNULL(RR.IN_EXISTE_ANEXO,'2')='00' OR ISNULL(RR.TI_EMI_REF,'0')+ISNULL(RR.IN_EXISTE_ANEXO,'2')='02' THEN 0 \n");
        sql.append(" ELSE 1 END) EXISTE_ANEXO\n");
        sql.append(" FROM IDOSGD.TDTV_REMITOS RE WITH(NOLOCK)  \n");
        sql.append(" INNER JOIN IDOSGD.TDTV_PERSONAL_VB VB WITH(NOLOCK) ON RE.NU_ANN=VB.NU_ANN AND RE.NU_EMI=VB.NU_EMI  \n");
        sql.append(" INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN RR WITH(NOLOCK) ON RR.NU_ANN=RE.NU_ANN AND RR.NU_EMI=RE.NU_EMI \n");
        sql.append(" WHERE  \n");

        sql.append(" RE.ES_DOC_EMI NOT IN('5','9')\n");
        sql.append(" AND RE.ES_ELI='0'\n");

        String pnuAnn = bDocVoBo.getNuAnn();
        if (pnuAnn != null && pnuAnn.trim().length() == 4) {
            sql.append(" AND RE.NU_ANN = :pnuAnn\n");
            objectParam.put("pnuAnn", pnuAnn);
        }
        sql.append(" AND VB.CO_DEP = :pCoDep\n");

        objectParam.put("pCoDep", bDocVoBo.getCoDepUsu());

        String pTipoBusqueda = bDocVoBo.getTipBusqueda();
        if (pTipoBusqueda.equals("1") && bDocVoBo.isEsIncluyeFiltro()) {
            bBusqFiltro = true;
        }

        sql.append(" AND VB.CO_EMP_VB = :pcoEmpVobo \n");
        objectParam.put("pcoEmpVobo", bDocVoBo.getCoEmpUsu());

        if (pTipoBusqueda.equals("0") || bBusqFiltro) {
            if (bDocVoBo.getTipDoc() != null && bDocVoBo.getTipDoc().trim().length() > 0) {
                sql.append(" AND RE.CO_TIP_DOC_ADM = :pCoDocEmi \n");
                objectParam.put("pCoDocEmi", bDocVoBo.getTipDoc());
            }
            if (bDocVoBo.getEsDoc() != null && bDocVoBo.getEsDoc().trim().length() > 0) {
                sql.append(" AND (VB.IN_VB = :pEsDocEmi \n");
                String esDoc = bDocVoBo.getEsDoc();
                if (esDoc.equals("0")) {
                    sql.append(" OR VB.IN_VB = 'B') \n");
                } else {
                    sql.append(") ");
                }
                objectParam.put("pEsDocEmi", bDocVoBo.getEsDoc());
            } else {
                sOrdenList = " DESC \n";
            }
            if (bDocVoBo.getCoPrioridadDoc() != null && bDocVoBo.getCoPrioridadDoc().trim().length() > 0) {
                sql.append(" AND RR.CO_PRIORIDAD = :pCoPrioridad \n");
                objectParam.put("pCoPrioridad", bDocVoBo.getCoPrioridadDoc());
            }
            if (bDocVoBo.getCoRefOri() != null && bDocVoBo.getCoRefOri().trim().length() > 0) {
                sql.append(" AND CHARINDEX(:pTiEmiRef, RR.TI_EMI_REF) > 0 \n");
                objectParam.put("pTiEmiRef", bDocVoBo.getCoRefOri());
            }
            if (bDocVoBo.getCoEmpElabora() != null && bDocVoBo.getCoEmpElabora().trim().length() > 0) {
                sql.append(" AND RE.CO_EMP_RES = :pcoEmpEla \n");
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
                sql.append(" AND RE.NU_COR_EMI = :pnuCorEmi \n");
                objectParam.put("pnuCorEmi", bDocVoBo.getNuCorEmi());
            }

            if (bDocVoBo.getNuDoc() != null && bDocVoBo.getNuDoc().trim().length() > 1) {
                sql.append(" AND RR.NU_DOC LIKE ''+:pnuDocEmi+'%' \n");
                objectParam.put("pnuDocEmi", bDocVoBo.getNuDoc());
            }

            if (bDocVoBo.getNroExp() != null && bDocVoBo.getNroExp().trim().length() > 1) {
                sql.append(" AND RR.NU_EXPEDIENTE LIKE '%'+:pnuExpediente+'%' \n");
                objectParam.put("pnuExpediente", bDocVoBo.getNroExp());
            }

            if (bDocVoBo.getAsunto() != null && bDocVoBo.getAsunto().trim().length() > 1) {

                sql.append(" AND UPPER(RE.DE_ASU) LIKE '%'+:pDeAsunto+'%' ");
                objectParam.put("pDeAsunto", bDocVoBo.getAsunto());
            }
        }
        sql.append(" ORDER BY RE.FE_EMI").append(sOrdenList);

        try {
            list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoVoBoBean.class));
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public DocumentoVoBoBean getDocumentoVoBo(String pnuAnn, String pnuEmi, String coDep, String coEmpVb) {
         StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        sql.append(" RE.NU_ANN,");
        sql.append(" RE.NU_EMI,");
        sql.append(" RE.NU_COR_EMI,");
        sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(RE.CO_DEP_EMI) DE_DEP_EMI,");
        sql.append(" (SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM FROM IDOSGD.RHTM_PER_EMPLEADOS WHERE CEMP_CODEMP = RE.CO_EMP_EMI) DE_EMP_EMI,");
        sql.append(" (SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM FROM IDOSGD.RHTM_PER_EMPLEADOS WHERE CEMP_CODEMP = RE.CO_EMP_RES) DE_EMP_ELABORA,");
        sql.append(" CONVERT(VARCHAR(10),RE.FE_EMI,103)FE_EMI_CORTA,");
        sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(RE.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,");
        sql.append(" (CASE WHEN RE.TI_EMI='01' OR RE.TI_EMI='05' THEN ISNULL(RE.NU_DOC_EMI,' ') + '-' + RE.NU_ANN + '-' + RE.DE_DOC_SIG");
        sql.append(" ELSE ISNULL(RE.DE_DOC_SIG,'S/N') END) NU_DOC,");
        sql.append(" (CASE WHEN VB.IN_VB = 'B' THEN '0' ELSE VB.IN_VB END) ES_DOC_EMI,");
        sql.append(" (SELECT DE_EST FROM IDOSGD.TDTR_ESTADOS WHERE CO_EST + DE_TAB = (CASE WHEN VB.IN_VB = 'B' THEN '0' ELSE VB.IN_VB END) + 'TDTV_PERSONAL_VB') DE_ES_DOC_EMI,");
        sql.append(" RE.NU_DIA_ATE,");
        sql.append(" RE.DE_ASU, ");
        sql.append(" EX.NU_EXPEDIENTE NRO_EXPEDIENTE,");
        sql.append(" CONVERT(datetime,EX.FE_EXP,103)FE_EXP_CORTA,");
        sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_PROCESO_EXP(EX.CO_PROCESO) DE_PROCESO_EXP,");
        sql.append(" RE.TI_CAP,");
        sql.append(" VB.OBS_VB DE_OBS");
        sql.append(" FROM IDOSGD.TDTV_REMITOS RE WITH(NOLOCK) LEFT JOIN IDOSGD.TDTC_EXPEDIENTE EX WITH(NOLOCK)");
        sql.append(" ON RE.NU_ANN_EXP=EX.NU_ANN_EXP AND RE.NU_SEC_EXP=EX.NU_SEC_EXP,IDOSGD.TDTV_PERSONAL_VB VB WITH(NOLOCK)");
        sql.append(" WHERE RE.NU_ANN=? AND RE.NU_EMI=?");
        sql.append(" AND VB.NU_ANN=? AND VB.NU_EMI=?");
        sql.append(" AND VB.CO_DEP=? AND VB.CO_EMP_VB=?");
        sql.append(" AND RE.ES_DOC_EMI NOT IN('5','9')");
        sql.append(" AND RE.ES_ELI='0'");
        sql.append(" AND RE.CO_GRU='1'");

        DocumentoVoBoBean docVobo = null;
        try {
            docVobo = (DocumentoVoBoBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoVoBoBean.class), new Object[]{pnuAnn, pnuEmi, pnuAnn, pnuEmi, coDep, coEmpVb});
        } catch (EmptyResultDataAccessException e) {
            docVobo = null;
        } catch (Exception e) {
            System.out.println("nu_ann=" + pnuAnn + ",nu_emi=" + pnuEmi);
            e.printStackTrace();
        }
        return docVobo;
    }
}
