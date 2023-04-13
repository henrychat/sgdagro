package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
import org.xml.sax.SAXException;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.libreria.util.Utility;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.bean.DestinatarioDocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.DestinoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoObjBean;
import pe.gob.onpe.tramitedoc.bean.EmpleadoBean;
import pe.gob.onpe.tramitedoc.bean.EstadoDocumentoBean;
import pe.gob.onpe.tramitedoc.bean.ExpedienteBean;
import pe.gob.onpe.tramitedoc.bean.MotivoBean;
import pe.gob.onpe.tramitedoc.bean.ProveedorBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaEmiDocBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaRemitoBean;
import pe.gob.onpe.tramitedoc.bean.RemitenteEmiBean;
import pe.gob.onpe.tramitedoc.bean.TblRemitosBean;
import pe.gob.onpe.tramitedoc.bean.TipoCategoriaBean;
import pe.gob.onpe.tramitedoc.bean.TipoDocumentoBean;
import pe.gob.onpe.tramitedoc.dao.DatosPlantillaDao;
import pe.gob.onpe.tramitedoc.dao.EmiDocumentoInteroperabilidadDao;
import pe.gob.onpe.tramitedoc.dao.ProveedorDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;
import pe.gob.segdi.pide.consultas.bean.ConsultaSunatBean;
import pe.gob.segdi.pide.consultas.ws.WSConsultaRuc;

@Repository("emiDocumentoInteroperabilidadDao")
public class EmiDocumentoInteroperabilidadDaoImp
        extends SimpleJdbcDaoBase
        implements EmiDocumentoInteroperabilidadDao {

    private SimpleJdbcCall spEmiDocumento;
    private SimpleJdbcCall spProveedor;
    @Autowired
    private ProveedorDao proveedorDao;
    @Autowired
    private DatosPlantillaDao datosPlantillaDao;

    public List<DocumentoEmiBean> getDocumentosBuscaEmiAdm(BuscarDocumentoEmiBean buscarDocumentoEmi) {
        StringBuilder sql = new StringBuilder();
        boolean bBusqFiltro = false;
        String sOrdenList = "";
        Map<String, Object> objectParam = new HashMap<String, Object>();

        List<DocumentoEmiBean> list = new ArrayList<DocumentoEmiBean>();

        sql.append("SELECT TOP 101 X.*,");
        sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_EMI_REF(X.NU_ANN, X.NU_EMI) DE_EMI_REF,");
        sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(X.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,");
        sql.append(" CASE X.NU_CANDES WHEN 1 THEN IDOSGD.PK_SGD_DESCRIPCION_TI_DES_EMP(X.NU_ANN, X.NU_EMI) ELSE IDOSGD.PK_SGD_DESCRIPCION_TI_DES_EMP_V(X.NU_ANN, X.NU_EMI)  END   DE_EMP_PRO, ");
        sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(X.CO_EMP_RES) DE_EMP_RES,");
        sql.append(" IDOSGD.PK_SGD_DESCRIPCION_ESTADOS(X.ES_DOC_EMI,'TDTV_REMITOS') DE_ES_DOC_EMI,");
        sql.append(" ROW_NUMBER() OVER(ORDER BY  X.NU_ANN ) ROWNUM ");
        sql.append(" FROM ( ");
        sql.append(" SELECT ");
        sql.append(" CASE ISNULL(B.TI_EMI_REF,'0')+ISNULL(B.IN_EXISTE_ANEXO,'2') WHEN '00' THEN 0 WHEN '02' THEN 0 ELSE 1 END EXISTE_ANEXO,  ");
        sql.append(" A.NU_COR_EMI,A.FE_EMI,  NVL(B.CO_PRIORIDAD,'1') CO_PRIORIDAD,");
        sql.append(" CONVERT(VARCHAR(20),A.FE_EMI,103)   FE_EMI_CORTA,");
        sql.append(" B.NU_DOC,UPPER(A.DE_ASU) DE_ASU_M,A.NU_ANN,B.NU_EXPEDIENTE,A.NU_EMI,A.TI_CAP,B.IN_EXISTE_DOC EXISTE_DOC,");
        sql.append(" A.ES_DOC_EMI,A.CO_TIP_DOC_ADM,A.NU_CANDES,A.CO_EMP_RES,NVL(A.DOC_ESTADO_MSJ,-1) as DOC_ESTADO_MSJ,");
        sql.append(" (SELECT TOP 1 C.TI_DES FROM IDOSGD.TDTV_DESTINOS C WHERE C.NU_EMI=A.NU_EMI AND C.NU_ANN=A.NU_ANN ) as tiDest,A.TI_ENV_MSJ tiEnvMsj,");
        sql.append(" CASE A.TI_ENV_MSJ WHEN '0' THEN 'MESA DE PARTES' WHEN '1' THEN 'ENVDIRECTO' END    recursoenvio");
        sql.append(" FROM IDOSGD.TDTV_REMITOS A WITH(NOLOCK) INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN B WITH(NOLOCK) ON B.NU_ANN=A.NU_ANN AND B.NU_EMI=A.NU_EMI ");
        sql.append(" WHERE   ");
        sql.append(" A.TI_EMI='06'");
        sql.append(" AND B.TI_EMI='06'");
        sql.append(" AND A.ES_ELI = '0'");
        sql.append(" AND A.CO_GRU = '6'");
        String pNUAnn = buscarDocumentoEmi.getsCoAnnio();
        if (!buscarDocumentoEmi.getEsFiltroFecha().equals("1") || !pNUAnn.equals("0")) {
            sql.append(" AND A.NU_ANN = :pNuAnn");

            objectParam.put("pNuAnn", pNUAnn);
        }

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
        } else if (tiAcceso.equals("1")) {
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
                sql.append(" AND INSTR(B.TI_EMI_REF, :pTiEmiRef) > 0 ");
                objectParam.put("pTiEmiRef", buscarDocumentoEmi.getsRefOrigen());
            }
            if (buscarDocumentoEmi.getsDestinatario() != null && buscarDocumentoEmi.getsDestinatario().trim().length() > 0) {
                sql.append(" AND INSTR(B.TI_EMI_DES, :pTiEmpPro) > 0 ");
                objectParam.put("pTiEmpPro", buscarDocumentoEmi.getsDestinatario());
            }
            if (buscarDocumentoEmi.getEsFiltroFecha() != null && (buscarDocumentoEmi
                    .getEsFiltroFecha().equals("1") || buscarDocumentoEmi.getEsFiltroFecha().equals("3"))) {
                String vFeEmiIni = buscarDocumentoEmi.getsFeEmiIni();
                String vFeEmiFin = buscarDocumentoEmi.getsFeEmiFin();
                if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
                        .trim().length() > 0) {
                    sql.append("AND FE_USE_CRE BETWEEN CONVERT(DATETIME, :pFeEmiIni, 103) AND CONVERT(DATETIME, :pFeEmiFin, 103) + 0.99999 ");
                    objectParam.put("pFeEmiIni", vFeEmiIni);
                    objectParam.put("pFeEmiFin", vFeEmiFin);
                }
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
        sql.append(" ORDER BY A.FE_EMI").append(sOrdenList);
        sql.append(") X ");

        try {
            list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class));
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public DocumentoEmiBean getDocumentoEmitido(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT \na.nu_ann, \na.nu_emi, \nCAST(a.nu_cor_emi AS NVARCHAR(20)) nu_cor_emi, \na.co_loc_emi, IDOSGD.PK_SGD_DESCRIPCION_de_local(a.co_loc_emi) de_loc_emi, \na.co_dep_emi, IDOSGD.PK_SGD_DESCRIPCION_de_dependencia(a.co_dep_emi) de_dep_emi,\na.ti_emi, IDOSGD.PK_SGD_DESCRIPCION_ti_destino(a.ti_emi) de_tip_emi,\na.co_emp_emi, IDOSGD.PK_SGD_DESCRIPCION_de_nom_emp(co_emp_emi) de_emp_emi,\na.co_emp_res, IDOSGD.PK_SGD_DESCRIPCION_de_nom_emp(a.co_emp_res) de_emp_res,\na.nu_dni_emi, \na.nu_ruc_emi, \na.co_otr_ori_emi,\nIDOSGD.PK_SGD_TRAMITEIDOSGD.ti_emi_emp (a.nu_ann, a.nu_emi) de_ori_emi,\nCONVERT(VARCHAR(20),a.fe_emi,103)  fe_emi, \nCONVERT(VARCHAR(20),A.FE_EMI,103)  FE_EMI_CORTA,\na.co_tip_doc_adm, IDOSGD.PK_SGD_DESCRIPCION_de_documento(a.co_tip_doc_adm) de_tip_doc_adm,\na.ti_emi,\na.nu_doc_emi,\na.de_doc_sig,\na.es_doc_emi, IDOSGD.PK_SGD_DESCRIPCION_estados (a.es_doc_emi,'TDTV_REMITOS') de_es_doc_emi,\na.nu_dia_ate, \na.de_asu, \na.co_pro, \na.co_sub, \na.ti_cap, \na.co_exp,\na.co_use_cre,\na.fe_use_cre, \na.co_use_mod, \na.fe_use_mod, \na.nu_ann_exp,\na.nu_sec_exp, \na.nu_det_exp,\nIDOSGD.PK_SGD_DESCRIPCION_DE_NU_EXPEDIENTE(a.nu_ann_exp,a.nu_sec_exp) NU_EXPEDIENTE\nFROM IDOSGD.tdtv_remitos a WITH(NOLOCK) \nWHERE A.NU_ANN = ? AND A.NU_EMI=?");

        DocumentoEmiBean documentoEmiBean = new DocumentoEmiBean();
        try {
            documentoEmiBean = (DocumentoEmiBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            documentoEmiBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentoEmiBean;
    }

    public DocumentoEmiBean getDocumentoEmiAdm(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        sql.append("select A.*,\n IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(A.CO_EMP_EMI) DE_EMP_EMI, IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(A.CO_EMP_RES) DE_EMP_RES,\n CONVERT(VARCHAR(20),A.FE_EMI,103)   FE_EMI_CORTA, IDOSGD.PK_SGD_DESCRIPCION_ESTADOS(A.ES_DOC_EMI,'TDTV_REMITOS') DE_ES_DOC_EMI,\n B.FE_EXP,TO_CHAR(B.FE_EXP,'DD/MM/YYYY') FE_EXP_CORTA,B.NU_EXPEDIENTE,\n B.CO_PROCESO, IDOSGD.PK_SGD_DESCRIPCION_DE_PROCESO_EXP(B.CO_PROCESO) DE_PROCESO,\n RR.IN_FIRMA_ANEXO\n from IDOSGD.TDTV_REMITOS A WITH(NOLOCK) left join IDOSGD.TDTC_EXPEDIENTE B WITH(NOLOCK)\n on A.NU_ANN_EXP = B.NU_ANN_EXP and A.NU_SEC_EXP = B.NU_SEC_EXP \n INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN RR ON RR.NU_ANN = A.NU_ANN AND RR.NU_EMI=A.NU_EMI \n where \n A.NU_ANN = ? AND A.NU_EMI = ? \n");

        DocumentoEmiBean documentoEmiBean = new DocumentoEmiBean();
        try {
            documentoEmiBean = (DocumentoEmiBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            documentoEmiBean = null;
        } catch (Exception e) {
            System.out.println("nu_ann=" + pnuAnn + ",nu_emi=" + pnuEmi);
            e.printStackTrace();
        }
        return documentoEmiBean;
    }

    public ExpedienteBean getExpDocumentoEmitido(String pnuAnnExp, String pnuSecExp) {
        StringBuilder sql = new StringBuilder();
        sql.append("select B.NU_EXPEDIENTE,B.FE_EXP,IDOSGD.PK_SGD_DESCRIPCION_DE_PROCESO_EXP(B.CO_PROCESO) DE_PROCESO,CONVERT(VARCHAR(20),B.FE_EXP,103)   FE_EXP_CORTA,\nB.NU_ANN_EXP,B.NU_SEC_EXP,B.CO_PROCESO\nfrom IDOSGD.TDTC_EXPEDIENTE B WITH(NOLOCK) \nwhere \nB.NU_ANN_EXP=? AND B.NU_SEC_EXP=?");

        ExpedienteBean expedienteBean = new ExpedienteBean();
        try {
            expedienteBean = (ExpedienteBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(ExpedienteBean.class), new Object[]{pnuAnnExp, pnuSecExp});
        } catch (EmptyResultDataAccessException e) {
            expedienteBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return expedienteBean;
    }

    public EmpleadoBean getEmpleadoLocaltblDestinatario(String pcoDependencia) {
        StringBuilder sql = new StringBuilder();
        sql.append("select A.CO_LOC CO_LOCAL,IDOSGD.PK_SGD_DESCRIPCION_DE_LOCAL(A.CO_LOC) DE_LOCAL,B.CO_EMPLEADO CEMP_CODEMP,IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(B.CO_EMPLEADO)COMP_NAME\nfrom IDOSGD.sitm_local_dependencia A WITH(NOLOCK) ,IDOSGD.rhtm_dependencia B WITH(NOLOCK)\nwhere B.CO_DEPENDENCIA = ? and A.CO_DEP=?");

        EmpleadoBean empleadoBean = new EmpleadoBean();
        try {
            empleadoBean = (EmpleadoBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[]{pcoDependencia, pcoDependencia});
        } catch (EmptyResultDataAccessException e) {
            empleadoBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return empleadoBean;
    }

    public List<EmpleadoBean> getPersonalDestinatario(String pcoDepen) {
        StringBuilder sql = new StringBuilder();
        List<EmpleadoBean> list = new ArrayList<EmpleadoBean>();

        sql.append("SELECT e.cemp_apepat,e.cemp_apemat,e.cemp_denom, e.CEMP_CODEMP \nFROM IDOSGD.RHTM_PER_EMPLEADOS e WITH(NOLOCK), \n( \nSELECT CEMP_CODEMP \nFROM IDOSGD.RHTM_PER_EMPLEADOS WITH(NOLOCK) \nwhere CEMP_EST_EMP = '1' \n  and (CO_DEPENDENCIA=? or CEMP_CO_DEPEND=?) \nunion \nselect co_emp from IDOSGD.tdtx_dependencia_empleado  where co_dep=? and es_emp='0' \nunion \nselect co_empleado from IDOSGD.rhtm_dependencia where co_dependencia = ? \n) a \nwhere e.cemp_codemp = a.cemp_codemp\norder by 1");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[]{pcoDepen, pcoDepen, pcoDepen, pcoDepen});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<MotivoBean> getLstMotivoxTipoDocumento(String pcoDepen, String pcoTipDoc) {
        StringBuilder sql = new StringBuilder();
        List<MotivoBean> list = new ArrayList<MotivoBean>();

        sql.append("SELECT a.de_mot,\n       a.co_mot\n  FROM IDOSGD.tdtr_motivo         a WITH(NOLOCK),\n       IDOSGD.tdtx_moti_docu_depe b WITH(NOLOCK)\n WHERE a.co_mot     = b.co_mot\n   AND b.co_dep     = ?\n   AND b.co_tip_doc = ?\nUNION\nSELECT de_mot,\n       co_mot\nFROM IDOSGD.tdtr_motivo  \nwhere co_mot in ('0','1')\n ORDER BY 1");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(MotivoBean.class), new Object[]{pcoDepen, pcoTipDoc});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<DocumentoBean> getLstDocEmitidoRef(String pcoEmpEmi, String pcoDepen, String pannio, String ptiDoc, String pnuDoc) {
        StringBuilder sql = new StringBuilder();
        List<DocumentoBean> list = new ArrayList<DocumentoBean>();

        sql.append("SELECT TOP 201 A.*, ROW_NUMBER() OVER (ORDER BY A.NU_ANN ) ROWNUM");
        sql.append(" FROM ( ");

        sql.append("select *\nfrom(select \nRE.fe_emi,\nSUBSTRING(IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(RE.co_tip_doc_adm),1,100) DE_TIP_DOC_ADM,\nRR.NU_DOC,\nCONVERT(VARCHAR(20),RE.fe_emi,103)  FE_EMI_CORTA,\nnull FE_REC_DOC_CORTA,\nRE.NU_ANN,\nRE.NU_EMI,\nNULL NU_DES,\nreplace(ltrim(rtrim(RE.de_asu)),char(10),' ') DE_ASU, \nRE.CO_TIP_DOC_ADM,  \nSUBSTRING(RR.NU_EXPEDIENTE,1,20) NU_EXPEDIENTE,\nRE.NU_ANN_EXP,\nRE.NU_SEC_EXP\nfrom IDOSGD.TDTV_REMITOS RE WITH(NOLOCK),IDOSGD.TDTX_REMITOS_RESUMEN RR WITH(NOLOCK)\nwhere RE.nu_ann = ?\nAND RE.NU_ANN=RR.NU_ANN\nAND RE.NU_EMI=RR.NU_EMI\nAND RE.es_eli = '0'\nand RE.es_doc_emi not in ('9','7','5')\nAND RE.CO_GRU = '1'\nAND RE.co_dep_emi in \n(select ?/*:b_02.co_dep_emi*/ from dual \nunion \n  SELECT co_dep_ref\n    FROM IDOSGD.tdtx_referencia\n   WHERE co_dep_emi = ?/*:b_02.co_dep_emi*/\n     AND ti_emi = 'D'\n     AND es_ref = '1')\nAND RE.co_tip_doc_adm = ?/*:B_03.LI_TIP_DOC*/\n");

        if (pnuDoc != null && pnuDoc.trim().length() > 0) {
            pnuDoc = Utility.getInstancia().rellenaCerosIzquierda(pnuDoc, 6);
            sql.append("AND (RE.NU_DOC_EMI = '").append(pnuDoc).append("' OR RR.NU_EXPEDIENTE LIKE '%").append(pnuDoc).append("')\n");
        }
        sql.append("UNION\nselect \nRE.fe_emi,\nSUBSTRING(IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(RE.co_tip_doc_adm),1,100) TIPO,\nRR.NU_DOC numero,\nCONVERT(VARCHAR(20),RE.fe_emi,103)  fecha_emision,\nnull fecha_recepcion,\nRE.NU_ANN,\nRE.NU_EMI,\nNULL NU_DES,\nreplace(ltrim(rtrim(RE.de_asu)),char(10),' ') ASUNTO, \nRE.CO_TIP_DOC_ADM,  \nSUBSTRING(RR.NU_EXPEDIENTE,1,20) NU_EXPEDIENTE,\nRE.NU_ANN_EXP,\nRE.NU_SEC_EXP\nfrom IDOSGD.TDTV_REMITOS RE WITH(NOLOCK),IDOSGD.TDTX_REMITOS_RESUMEN RR WITH(NOLOCK)\nwhere RE.nu_ann = ?\nAND RE.NU_ANN=RR.NU_ANN\nAND RE.NU_EMI=RR.NU_EMI\nAND RE.es_eli = '0'\nand RE.es_doc_emi not in ('9','7','5')\nAND RE.CO_GRU = '2'\nAND RE.co_tip_doc_adm = ?/*:B_03.LI_TIP_DOC*/\n");

        if (pnuDoc != null && pnuDoc.trim().length() > 0) {
            pnuDoc = Utility.getInstancia().rellenaCerosIzquierda(pnuDoc, 6);
            sql.append("AND (RE.NU_DOC_EMI = '").append(pnuDoc).append("' OR RR.NU_EXPEDIENTE LIKE '%").append(pnuDoc).append("')\n");
        }
        sql.append("and RE.co_emp_emi = ?/*:GLOBAL.USER*/\n) E\n ");

        sql.append(") A ");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoBean.class), new Object[]{pannio, pcoDepen, pcoDepen, ptiDoc, pannio, ptiDoc, pcoEmpEmi});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<DocumentoBean> getLstDocRecepcionadoRef(String pcoDepen, String pannio, String ptiDoc, String pnuDoc) {
        StringBuilder sql = new StringBuilder();
        List<DocumentoBean> list = new ArrayList<DocumentoBean>();

        sql.append("SELECT TOP 201 A.*, ROWNUM");
        sql.append(" FROM ( ");

        sql.append("select *\nfrom(\nselect\nd.FE_REC_DOC,\nSUBSTRING(IDOSGD.PK_SGD_DESCRIPCION_de_documento(r.co_tip_doc_adm),1,100) de_tip_doc_adm,\nSUBSTRING(rr.nu_doc,1,50) NU_DOC,\nCONVERT(VARCHAR(20),r.FE_EMI,103)   FE_EMI_CORTA,\nCONVERT(VARCHAR(20),d.FE_REC_DOC,103)   FE_REC_DOC_CORTA,\nr.nu_ann,\nr.nu_emi,\nd.nu_des,\nREPLACE(LTRIM(RTRIM(r.DE_ASU)),CHAR(10),' ') DE_ASU,\nr.co_tip_doc_adm,\nsubstring(IDOSGD.PK_SGD_DESCRIPCION_de_dependencia(d.co_dep_des),1,200) de_dep_des,\nSUBSTRING(rr.nu_expediente,1,20) NU_EXPEDIENTE,\nr.NU_ANN_EXP,\nr.NU_SEC_EXP\nfrom IDOSGD.TDTV_REMITOS R WITH(NOLOCK),IDOSGD.TDTV_DESTINOS D WITH(NOLOCK),IDOSGD.TDTX_REMITOS_RESUMEN RR WITH(NOLOCK)\nwhere\nr.es_eli = '0'\nAND d.es_eli = '0'\nAND r.nu_ann = ?/*:b_03.NU_ANN_REF*/\nand rr.nu_ann=r.nu_ann\nAND rr.nu_emi = r.nu_emi\nAND d.nu_ann = r.nu_ann\nAND d.nu_emi = r.nu_emi\nAND r.es_doc_emi NOT IN ('5', '9', '7')\nand d.es_doc_rec <> '0'  \nand d.co_dep_des in \n(select ?/*:b_02.co_dep_emi*/ from dual \nunion \n  SELECT co_dep_ref\n    FROM IDOSGD.tdtx_referencia\n   WHERE co_dep_emi = ?/*:b_02.co_dep_emi*/\n     AND ti_emi = 'D'\n     AND es_ref = '1')\nAND r.co_tip_doc_adm = ?/*:B_03.LI_TIP_DOC*/\n");

        if (pnuDoc != null && pnuDoc.trim().length() > 0) {
            pnuDoc = Utility.getInstancia().rellenaCerosIzquierda(pnuDoc, 6);
            sql.append("AND (R.NU_DOC_EMI = '").append(pnuDoc).append("' OR RR.NU_EXPEDIENTE LIKE '%").append(pnuDoc).append("')\n");
        }
        sql.append(") E\norder by 1 desc");

        sql.append(") A ");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoBean.class), new Object[]{pannio, pcoDepen, pcoDepen, ptiDoc});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<DestinatarioDocumentoEmiBean> _getLstDestintariotlbEmi(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        List<DestinatarioDocumentoEmiBean> list = new ArrayList<DestinatarioDocumentoEmiBean>();
        sql.append("select a.nu_ann,a.nu_emi,a.nu_des,a.co_loc_des co_local,substr(IDOSGD.PK_SGD_DESCRIPCION_DE_LOCAL(a.co_loc_des), 1, 100) de_local,\na.co_dep_des co_dependencia,substring(IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(a.co_dep_des), 1, 100) de_dependencia,\na.co_emp_des co_empleado,substr(IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(a.co_emp_des), 1, 100) de_empleado,\na.co_mot co_tramite,substring(IDOSGD.PK_SGD_DESCRIPCION_MOTIVO(a.co_mot), 1, 100) de_tramite,\na.co_pri co_prioridad,\na.de_pro de_indicaciones\nFROM IDOSGD.tdtv_destinos a WITH(NOLOCK)\nwhere a.nu_ann = ? and a.nu_emi = ?\nAND a.TI_DES = '01' AND a.ES_ELI='0' AND a.NU_EMI_REF is null\norder by 3");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DestinatarioDocumentoEmiBean.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<DestinatarioDocumentoEmiBean> getLstDestintariotlbEmi(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        List<DestinatarioDocumentoEmiBean> list = new ArrayList<DestinatarioDocumentoEmiBean>();
        sql.append("select a.nu_ann,a.nu_emi,a.nu_des,a.co_loc_des co_local, substring(IDOSGD.PK_SGD_DESCRIPCION_DE_LOCAL(a.co_loc_des), 1, 100)  de_local,\n                a.co_dep_des co_dependencia, substring(IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(a.co_dep_des), 1, 100)  de_dependencia,\n                a.co_emp_des co_empleado, substring(IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(a.co_emp_des), 1, 100)  de_empleado,\n                a.co_mot co_tramite, substring(IDOSGD.PK_SGD_DESCRIPCION_MOTIVO(a.co_mot), 1, 100)  de_tramite,\n                a.co_pri co_prioridad,\n                a.de_pro de_indicaciones,\n                a.NU_RUC_DES nu_ruc, substring(IDOSGD.PK_SGD_DESCRIPCION_DE_PROVEEDOR(a.NU_RUC_DES), 1, 100)  de_proveedor,\n                a.NU_DNI_DES nu_dni, substring(IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL(a.NU_DNI_DES), 1, 100)  de_ciudadano,\n                a.CO_OTR_ORI_DES co_otro_origen,\n                ISNULL(a.CO_OTR_ORI_DES,de_otro_origen_full) de_otro_origen_full, a.ti_des co_tipo_destino, ISNULL(a.ES_ENV_POR_TRA,'0') ENV_MESA_PARTES,\n                CDIR_REMITE,CEXP_CORREOE,CTELEFONO,CCOD_DPTO,CCOD_PROV,CCOD_DIST,\n                ltrim(UB.NODEP)+' '+ltrim(UB.NOPRV)+' '+ltrim(UB.NODIS) ubigeo \n                ,A.REMI_TI_EMI   ,  CASE A.REMI_TI_EMI WHEN  '03' THEN  IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL (A.REMI_NU_DNI_EMI)   \n                WHEN '04' THEN IDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN (A.REMI_CO_OTR_ORI_EMI) END  nombres, \n                CASE A.REMI_TI_EMI WHEN '03' THEN REMI_NU_DNI_EMI  \n                WHEN '04' THEN REMI_CO_OTR_ORI_EMI END REMI_NU_DNI_EMI,  \n                    DE_CARGO AS cargo,A.CIDCAT,(SELECT VDESCAT FROM IDOSGD.IOTDX_CATEGORIA WHERE CIDCAT=A.CIDCAT) DECAT,A.DE_DEP_DES,A.DE_NOM_DES,A.DE_CAR_DES \n                FROM IDOSGD.tdtv_destinos a\n                LEFT JOIN ( SELECT C.CO_OTR_ORI,C.DE_APE_PAT_OTR+' '+C.DE_APE_MAT_OTR+', '+C.DE_NOM_OTR + ' - ' +\n                     C.DE_RAZ_SOC_OTR +'##'+  NVL(B.CELE_DESELE,'   ') +'##'+  C.NU_DOC_OTR_ORI  de_otro_origen_full\n                  FROM IDOSGD.TDTR_OTRO_ORIGEN C LEFT JOIN  (\n                  SELECT CELE_CODELE, CELE_DESELE  FROM IDOSGD.SI_ELEMENTO  WHERE CTAB_CODTAB ='TIP_DOC_IDENT') B\n                  ON C.CO_TIP_OTR_ORI = B.CELE_CODELE ) OTRDES ON OTRDES.CO_OTR_ORI=a.CO_OTR_ORI_DES\n                LEFT JOIN IDOSGD.IDTUBIAS UB ON UB.UBDEP=a.CCOD_DPTO AND UB.UBPRV=a.CCOD_PROV AND UB.UBDIS=a.CCOD_DIST\n                where nu_ann = ? and nu_emi =? \n                AND ES_ELI='0' AND NU_EMI_REF is null\n                order by 3");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DestinatarioDocumentoEmiBean.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<ReferenciaBean> getLstDocumReferenciatblEmi(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        List<ReferenciaBean> list = new ArrayList<ReferenciaBean>();
        sql.append("SELECT \nIDOSGD.PK_SGD_DESCRIPCION_de_documento (a.co_tip_doc_adm) de_tipo_doc, \n CASE a.ti_emi WHEN '01'  THEN a.nu_doc_emi + '-' + a.nu_ann + '-' + a.de_doc_sig  \n  WHEN '05' THEN    a.nu_doc_emi + '-' + a.nu_ann + '-' + a.de_doc_sig ELSE a.de_doc_sig END li_nu_doc ,\nsubstring(IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(a.co_dep_emi), 1, 100) de_dep_emi, \nCONVERT(VARCHAR(20),a.fe_emi,103)   fe_emi_corta, \nb.nu_ann,\nb.nu_emi,\nISNULL(ltrim(cast(b.nu_des as varchar(20))),'N') nu_des ,\nb.nu_ann_ref,\nb.nu_emi_ref,\nisnull(ltrim(cast(b.nu_des_ref as varchar(20))),'N') nu_des_ref,\nb.co_ref,\ncase  isnull(ltrim(to_char(b.nu_des_ref)),'N') when 'N' then 'emi' else 'rec' END  tip_doc_ref,\na.co_tip_doc_adm,'BD' accion_bd\nFROM IDOSGD.tdtv_remitos a,IDOSGD.TDTR_REFERENCIA b \nWHERE a.nu_ann = b.nu_ann_ref\nAND a.nu_emi = b.nu_emi_ref\nand b.nu_ann = ?\nand b.nu_emi = ?\n");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(ReferenciaBean.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public String getTipoDestinatarioEmi(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        String result = null;

        sql.append("SELECT MAX(ti_des)\n/*INTO :b_02.li_ti_des*/\nFROM IDOSGD.tdtv_destinos WITH(NOLOCK) \nWHERE nu_emi = ?\n AND nu_ann = ? AND ES_ELI = '0'");

        try {
            result = (String) this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[]{pnuEmi, pnuAnn});
        } catch (Exception e) {
            result = "1";
            e.printStackTrace();
        }
        return result;
    }

    public String updDocumentoEmiBean(String nuAnn, String nuEmi, DocumentoEmiBean documentoEmiBean, ExpedienteBean expedienteBean, RemitenteEmiBean remitenteEmiBean, String pcoUserMod) {
        String vReturn = "NO_OK";
        StringBuilder sqlUpd = new StringBuilder();
        sqlUpd.append("update IDOSGD.tdtv_remitos A \nset A.CO_USE_MOD=?,");

        if (documentoEmiBean != null) {

            sqlUpd.append("A.DE_ASU=?\n,A.NU_DIA_ATE=?\n,A.CO_TIP_DOC_ADM=?\n,A.ES_DOC_EMI=?\n,A.NU_DOC_EMI=?\n,A.DE_DOC_SIG=?\n,A.NU_COR_DOC=ISNULL(?,ISNULL(?,(SELECT ISNULL(MAX(nu_cor_doc), 0) + 1\nFROM IDOSGD.tdtv_remitos\nWHERE nu_ann = ?\nAND co_dep_emi = ?\nAND co_tip_doc_adm = ?\nAND ti_emi = ?))),\n");
        }

        if (expedienteBean != null) {
            sqlUpd.append("A.NU_SEC_EXP='" + expedienteBean.getNuSecExp() + "',\n");
        }
        if (remitenteEmiBean != null) {
            sqlUpd.append("A.CO_LOC_EMI='" + remitenteEmiBean.getCoLocal() + "'\n,A.CO_DEP_EMI='" + remitenteEmiBean
                    .getCoDependencia() + "'\n,A.CO_EMP_EMI='" + remitenteEmiBean
                            .getCoEmpFirma() + "',\n");
        }
        sqlUpd.append("A.FE_USE_MOD=GETDATE(),A.NU_FOL=? where\nA.NU_ANN=? and\nA.NU_EMI=?");

        try {
            if (documentoEmiBean != null) {
                this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{pcoUserMod, documentoEmiBean.getDeAsu(), documentoEmiBean.getNuDiaAte(), documentoEmiBean
                    .getCoTipDocAdm(), documentoEmiBean.getEsDocEmi(), documentoEmiBean.getNuDocEmi(), documentoEmiBean.getDeDocSig(), documentoEmiBean
                    .getNuDocEmi(), documentoEmiBean.getNuCorDoc(), nuAnn, documentoEmiBean.getCoDepEmi(), documentoEmiBean.getCoTipDocAdm(), documentoEmiBean
                    .getTiEmi(), documentoEmiBean.getNuFol(), nuAnn, nuEmi});
            } else {
                this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{pcoUserMod, nuAnn, nuEmi});
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
        StringBuilder sqlUpd = new StringBuilder();
        StringBuilder sqlQry = new StringBuilder();
        StringBuilder sqlQry1 = new StringBuilder();
        sqlQry.append("select lpad(SEC_REMITOS_NU_EMI.NextVal, 10, '0') from dual");
        sqlQry1.append("SELECT ISNULL(MAX(nu_cor_emi), 0) + 1\nFROM IDOSGD.tdtv_remitos WITH(NOLOCK) \nWHERE nu_ann = ?\nAND co_dep_emi = ?\nAND ti_emi = '06'");

        sqlUpd.append("INSERT INTO IDOSGD.tdtv_remitos(\nNU_ANN,\nNU_EMI,\nNU_COR_EMI,\nCO_LOC_EMI,\nCO_DEP_EMI,\nCO_EMP_EMI,\nCO_EMP_RES, \nTI_EMI,\nFE_EMI,\nCO_GRU,\nDE_ASU, \nES_DOC_EMI, \nNU_DIA_ATE, \nTI_CAP, \nCO_TIP_DOC_ADM, \nNU_COR_DOC, \nDE_DOC_SIG, \nNU_ANN_EXP, \nNU_SEC_EXP, \nNU_DET_EXP, \nES_ELI,\nNU_DOC_EMI,\nCO_USE_CRE,\nFE_USE_CRE,\nCO_USE_MOD,\nFE_USE_MOD,NU_FOL)\nVALUES(?,?,?,?,?,?,?,'06',GETDATE(),'6',?,?,?,'03',?,\n?,?,?,?,1,\n'0',?,?,GETDATE(),?,GETDATE(),?)");

        try {
            String snuEmi = (String) this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class);
            String snuCorEmi = (String) this.jdbcTemplate.queryForObject(sqlQry1.toString(), String.class, new Object[]{documentoEmiBean.getNuAnn(), documentoEmiBean.getCoDepEmi()});
            documentoEmiBean.setNuEmi(snuEmi);
            documentoEmiBean.setNuCorEmi(snuCorEmi);

            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{documentoEmiBean.getNuAnn(), snuEmi, snuCorEmi, documentoEmiBean
                .getCoLocEmi(), documentoEmiBean.getCoDepEmi(), documentoEmiBean.getCoEmpEmi(), documentoEmiBean.getCoEmpRes(), documentoEmiBean
                .getDeAsu(), documentoEmiBean.getEsDocEmi(), documentoEmiBean.getNuDiaAte(), documentoEmiBean.getCoTipDocAdm(), documentoEmiBean
                .getNuCorDoc(), documentoEmiBean.getDeDocSig(), documentoEmiBean.getNuAnnExp(), documentoEmiBean.getNuSecExp(), documentoEmiBean
                .getNuDocEmi(), documentoEmiBean.getCoUseMod(), documentoEmiBean.getCoUseMod(), documentoEmiBean.getNuFol()});

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
        StringBuilder sqlUpd = new StringBuilder();
        StringBuilder sqlQry1 = new StringBuilder();
        StringBuilder sqlQry2 = new StringBuilder();
        StringBuilder sqlUpdUbigeo = new StringBuilder();
        sqlQry1.append("SELECT CELE_CODELE FROM SI_ELEMENTO WITH(NOLOCK) WHERE CTAB_CODTAB='UPD_PARAMT_UBIGEO'");
        sqlQry2.append("SELECT COUNT(*) FROM LG_PRO_PROVEEDOR WITH(NOLOCK) WHERE CPRO_RUC='" + destinatarioDocumentoEmiBean.getNuRuc() + "'");

        sqlUpd.append("INSERT INTO TDTV_DESTINOS(\nNU_ANN,\nNU_EMI,\nNU_DES,\nCO_LOC_DES,\nCO_DEP_DES,\nTI_DES,\nCO_EMP_DES,\nCO_PRI,\nDE_PRO,\nCO_MOT, \nCO_OTR_ORI_DES, \nNU_DNI_DES, \nNU_RUC_DES, \nES_ELI,\nFE_USE_CRE,\nFE_USE_MOD,\nCO_USE_MOD,\nCO_USE_CRE,\nES_DOC_REC,\nES_ENV_POR_TRA,CDIR_REMITE,CEXP_CORREOE,CTELEFONO,CCOD_DPTO,CCOD_PROV,CCOD_DIST,REMI_TI_EMI,REMI_NU_DNI_EMI,REMI_CO_OTR_ORI_EMI,DE_CARGO,CIDCAT,DE_DEP_DES,DE_NOM_DES,DE_CAR_DES)\nVALUES(?,?,(select nvl(MAX(a.nu_des) + 1,1) FROM tdtv_destinos a where \nA.NU_ANN=? and\nA.NU_EMI=?),?,?,?,?,?,?,?,?,?,?,'0',getdate(),getdate(),?,?,'0',?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        try {
            String[] ubigeo = null;
            String dep = "";
            String pro = "";
            String dis = "";

            String siExisteRuc = (String) this.jdbcTemplate.queryForObject(sqlQry2.toString(), String.class);

            if (siExisteRuc.equalsIgnoreCase("0")) {

                try {

                    WSConsultaRuc wSSunat = new WSConsultaRuc();

                    ConsultaSunatBean beanDdp = wSSunat.consultaSunat(this.datosPlantillaDao.getParametros("URL_RZ_SUNAT_REST"), destinatarioDocumentoEmiBean.getNuRuc());
                    if (beanDdp.getDdp_nombre() != null) {
                        this.proveedorDao.insPideProveedor(beanDdp);
                    }
                } catch (SAXException sAXException) {

                } catch (IOException iOException) {
                }
            }

            if (destinatarioDocumentoEmiBean.getCcodDpto() != null && !"".equals(destinatarioDocumentoEmiBean.getCcodDpto())) {
                ubigeo = destinatarioDocumentoEmiBean.getCcodDpto().split(",");
            }
            if (ubigeo != null && ubigeo.length > 0) {
                dep = ubigeo[0];
                pro = ubigeo[1];
                dis = ubigeo[2];
            }
            if ("03".equals(destinatarioDocumentoEmiBean.getRemiTiEmi())) {
                destinatarioDocumentoEmiBean.setRemiCoOtrOriEmi("");
            }
            if ("04".equals(destinatarioDocumentoEmiBean.getRemiTiEmi())) {
                destinatarioDocumentoEmiBean.setRemiCoOtrOriEmi(destinatarioDocumentoEmiBean.getRemiNuDniEmi());
                destinatarioDocumentoEmiBean.setRemiNuDniEmi("");
            }

            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{nuAnn, nuEmi, nuAnn, nuEmi, destinatarioDocumentoEmiBean.getCoLocal(), destinatarioDocumentoEmiBean.getCoDependencia(), destinatarioDocumentoEmiBean.getCoTipoDestino(), destinatarioDocumentoEmiBean
                .getCoEmpleado(), destinatarioDocumentoEmiBean.getCoPrioridad(), destinatarioDocumentoEmiBean.getDeIndicaciones(), destinatarioDocumentoEmiBean.getCoTramite(), destinatarioDocumentoEmiBean
                .getCoOtroOrigen(), destinatarioDocumentoEmiBean.getNuDni(), destinatarioDocumentoEmiBean.getNuRuc(), destinatarioDocumentoEmiBean
                .getCoUseCre(), destinatarioDocumentoEmiBean.getCoUseCre(), destinatarioDocumentoEmiBean.getEnvMesaPartes(), destinatarioDocumentoEmiBean
                .getCdirRemite(), destinatarioDocumentoEmiBean.getCexpCorreoe(), destinatarioDocumentoEmiBean.getcTelefono(), dep, pro, dis, destinatarioDocumentoEmiBean
                .getRemiTiEmi(), destinatarioDocumentoEmiBean.getRemiNuDniEmi(), destinatarioDocumentoEmiBean.getRemiCoOtrOriEmi(), destinatarioDocumentoEmiBean
                .getCargo(), destinatarioDocumentoEmiBean.getCidCat(), destinatarioDocumentoEmiBean.getDeDepDes(), destinatarioDocumentoEmiBean.getDeNomDes(), destinatarioDocumentoEmiBean.getDeCarDes()});

            String isUpdateUbigeo = (String) this.jdbcTemplate.queryForObject(sqlQry1.toString(), String.class);
            if ("1".equals(isUpdateUbigeo)) {
                if ("02".equals(destinatarioDocumentoEmiBean.getCoTipoDestino())) {
                    sqlUpdUbigeo.append("UPDATE  lg_pro_proveedor SET cpro_domicil=?,cpro_ubigeo=?,cpro_telefo=?,cpro_email=?,cubi_coddep=?,\n    cubi_codpro=?,cubi_coddis=?  WHERE cpro_ruc=?");

                    this.jdbcTemplate.update(sqlUpdUbigeo.toString(), new Object[]{destinatarioDocumentoEmiBean.getCdirRemite(), dep + pro + dis, destinatarioDocumentoEmiBean
                        .getcTelefono(), destinatarioDocumentoEmiBean.getCexpCorreoe(), dep, pro, dis, destinatarioDocumentoEmiBean.getNuRuc()});
                }
                if ("03".equals(destinatarioDocumentoEmiBean.getCoTipoDestino())) {
                    sqlUpdUbigeo.append(" UPDATE  TDTX_ANI_SIMIL  SET  DEDOMICIL=?,DETELEFO=?, DEEMAIL=?,UBDEP=?,UBPRV=?, UBDIS=?  WHERE  NULEM=? ");
                    this.jdbcTemplate.update(sqlUpdUbigeo.toString(), new Object[]{destinatarioDocumentoEmiBean.getCdirRemite(), destinatarioDocumentoEmiBean
                        .getcTelefono(), destinatarioDocumentoEmiBean.getCexpCorreoe(), dep, pro, dis, destinatarioDocumentoEmiBean.getNuDni()});
                }
                if ("04".equals(destinatarioDocumentoEmiBean.getCoTipoDestino())) {
                    sqlUpdUbigeo.append("UPDATE TDTR_OTRO_ORIGEN SET  DE_DIR_OTRO_ORI=?,DE_TELEFO=?,DE_EMAIL=?,UB_DEP=?, UB_PRO=?, UB_DIS=?   WHERE  CO_OTR_ORI=?    ");
                    this.jdbcTemplate.update(sqlUpdUbigeo.toString(), new Object[]{destinatarioDocumentoEmiBean.getCdirRemite(), destinatarioDocumentoEmiBean
                        .getcTelefono(), destinatarioDocumentoEmiBean.getCexpCorreoe(), dep, pro, dis, destinatarioDocumentoEmiBean.getCoOtroOrigen()});
                }
            }

            vReturn = "OK";
        } catch (DuplicateKeyException con) {
            vReturn = "Datos Duplicado Destino.";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vReturn;
    }

    public String updDestinatarioDocumentoEmi(String nuAnn, String nuEmi, DestinatarioDocumentoEmiBean destinatarioDocumentoEmiBean) {
        String vReturn = "NO_OK";
        StringBuilder sqlUpd = new StringBuilder();

        sqlUpd.append("update TDTV_DESTINOS \nset CO_LOC_DES=?\n,CO_DEP_DES=?\n,CO_EMP_DES=?\n,CO_PRI=?\n,DE_PRO=?\n,CO_MOT=?\n,CO_OTR_ORI_DES=? \n,NU_DNI_DES=? \n,NU_RUC_DES=? \n,FE_USE_MOD=getdate() \n,CO_USE_MOD=?\n,ES_ENV_POR_TRA=?\n,CDIR_REMITE=?\n,CEXP_CORREOE=?\n,CTELEFONO=?\n,CCOD_DPTO=?\n,CCOD_PROV=?\n,CCOD_DIST=?\n,REMI_TI_EMI=?\n,REMI_NU_DNI_EMI=?\n,REMI_CO_OTR_ORI_EMI=? \n,DE_CARGO=? \n,CIDCAT=? \n,DE_DEP_DES=? \n,DE_NOM_DES=? \n,DE_CAR_DES=? \nWHERE\nNU_ANN=? and\nNU_EMI=? and\nNU_DES=?");

        try {
            String[] ubigeo = null;
            String dep = "";
            String pro = "";
            String dis = "";
            if (destinatarioDocumentoEmiBean.getCcodDpto() != null && !"".equals(destinatarioDocumentoEmiBean.getCcodDpto())) {
                ubigeo = destinatarioDocumentoEmiBean.getCcodDpto().split(",");
            }
            if (ubigeo != null && ubigeo.length > 0) {
                dep = ubigeo[0];
                pro = ubigeo[1];
                dis = ubigeo[2];
            }
            if ("03".equals(destinatarioDocumentoEmiBean.getRemiTiEmi())) {
                destinatarioDocumentoEmiBean.setRemiCoOtrOriEmi("");
            }
            if ("04".equals(destinatarioDocumentoEmiBean.getRemiTiEmi())) {
                destinatarioDocumentoEmiBean.setRemiCoOtrOriEmi(destinatarioDocumentoEmiBean.getRemiNuDniEmi());
                destinatarioDocumentoEmiBean.setRemiNuDniEmi("");
            }

            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{destinatarioDocumentoEmiBean.getCoLocal(), destinatarioDocumentoEmiBean.getCoDependencia(), destinatarioDocumentoEmiBean
                .getCoEmpleado(), destinatarioDocumentoEmiBean.getCoPrioridad(), destinatarioDocumentoEmiBean.getDeIndicaciones(), destinatarioDocumentoEmiBean.getCoTramite(), destinatarioDocumentoEmiBean
                .getCoOtroOrigen(), destinatarioDocumentoEmiBean.getNuDni(), destinatarioDocumentoEmiBean.getNuRuc(), destinatarioDocumentoEmiBean
                .getCoUseCre(), destinatarioDocumentoEmiBean.getEnvMesaPartes(), destinatarioDocumentoEmiBean
                .getCdirRemite(), destinatarioDocumentoEmiBean.getCexpCorreoe(), destinatarioDocumentoEmiBean.getcTelefono(), dep, pro, dis, destinatarioDocumentoEmiBean
                .getRemiTiEmi(), destinatarioDocumentoEmiBean.getRemiNuDniEmi(), destinatarioDocumentoEmiBean.getRemiCoOtrOriEmi(), destinatarioDocumentoEmiBean.getCargo(), destinatarioDocumentoEmiBean
                .getCidCat(), destinatarioDocumentoEmiBean.getDeDepDes(), destinatarioDocumentoEmiBean.getDeNomDes(), destinatarioDocumentoEmiBean.getDeCarDes(), nuAnn, nuEmi, destinatarioDocumentoEmiBean
                .getNuDes()});
            vReturn = "OK";
        } catch (DuplicateKeyException con) {
            vReturn = "Datos Duplicado Destino.";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vReturn;
    }

    public String delDestinatarioDocumentoEmi(String nuAnn, String nuEmi, DestinatarioDocumentoEmiBean destinatarioDocumentoEmiBean) {
        String vReturn = "NO_OK";
        StringBuilder sqlDel = new StringBuilder();
        sqlDel.append("delete from TDTV_DESTINOS\nWHERE NU_ANN = ?\nAND NU_EMI = ?\nand NU_DES=?");

        try {
            this.jdbcTemplate.update(sqlDel.toString(), new Object[]{nuAnn, nuEmi, destinatarioDocumentoEmiBean.getNuDes()});
            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vReturn;
    }

    public String insReferenciaDocumentoEmi(String nuAnn, String nuEmi, ReferenciaEmiDocBean referenciaEmiDocBean) {
        String vReturn = "NO_OK";
        StringBuilder sqlUpd = new StringBuilder();
        sqlUpd.append("INSERT INTO tdtr_referencia(\nNU_ANN,\nNU_EMI,\nCO_REF,\nNU_ANN_REF,\nNU_EMI_REF,\nNU_DES_REF,\nCO_USE_CRE,\nFE_USE_CRE)\nVALUES(?,?,lpad(SEC_REFERENCIA_CO_REF.NextVal, 10, '0'),?,?,?,?,getdate())");

        try {
            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{nuAnn, nuEmi, referenciaEmiDocBean.getNuAnn(), referenciaEmiDocBean.getNuEmi(), referenciaEmiDocBean
                .getNuDes(), referenciaEmiDocBean.getCoUseCre()});
            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vReturn;
    }

    public String updReferenciaDocumentoEmi(String nuAnn, String nuEmi, ReferenciaEmiDocBean referenciaEmiDocBean) {
        String vReturn = "NO_OK";
        StringBuilder sqlUpd = new StringBuilder();
        sqlUpd.append("update tdtr_referencia \nset NU_ANN_REF = ?,\nNU_EMI_REF = ?,\nNU_DES_REF = ?\nWHERE NU_ANN = ? AND NU_EMI = ? \nAND CO_REF = ?");

        try {
            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{referenciaEmiDocBean.getNuAnn(), referenciaEmiDocBean.getNuEmi(), referenciaEmiDocBean
                .getNuDes(), nuAnn, nuEmi, referenciaEmiDocBean.getCoRef()});
            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vReturn;
    }

    public String delReferenciaDocumentoEmi(String nuAnn, String nuEmi, ReferenciaEmiDocBean referenciaEmiDocBean) {
        String vReturn = "NO_OK";
        StringBuilder sqlIns = new StringBuilder();
        sqlIns.append("DELETE FROM tdtr_referencia\nWHERE NU_ANN = ? AND NU_EMI = ? \nAND NU_ANN_REF = ? AND NU_EMI_REF = ?");

        try {
            this.jdbcTemplate.update(sqlIns.toString(), new Object[]{nuAnn, nuEmi, referenciaEmiDocBean.getNuAnn(), referenciaEmiDocBean.getNuEmi()});
            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vReturn;
    }

    public DocumentoEmiBean getDocumentoEmiAdmNew(String sEstadoDocEmi, String codDependencia) {
        StringBuilder sql = new StringBuilder();
        sql.append("select  PK_SGD_DESCRIPCION.DE_SIGLA(?) de_doc_sig,\n CO_EMPLEADO co_emp_emi, PK_SGD_DESCRIPCION.DE_NOM_EMP(CO_EMPLEADO) de_emp_emi,\n PK_SGD_DESCRIPCION.ESTADOS (?,'TDTV_REMITOS') de_es_doc_emi, '0' existe_doc, '0' existe_anexo,\n ? co_es_doc_emi \n from rhtm_dependencia WITH(NOLOCK)\n where CO_DEPENDENCIA = ?");

        DocumentoEmiBean documentoEmiBean = new DocumentoEmiBean();
        try {
            documentoEmiBean = (DocumentoEmiBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class), new Object[]{codDependencia, sEstadoDocEmi, sEstadoDocEmi, codDependencia});
        } catch (EmptyResultDataAccessException e) {
            documentoEmiBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentoEmiBean;
    }

    public String insExpedienteBean(ExpedienteBean expedienteBean) {
        String vReturn = "NO_OK";
        StringBuilder sqlQry1 = new StringBuilder();
        sqlQry1.append("select lpad(rtrim(ltrim(to_char(NVL(max(NU_CORR_EXP),0) +1 ))), 7, '0') nuCorrExp, \n substr(PK_SGD_DESCRIPCION.DE_SIGLA_CORTA(?),1,6) deSiglaCorta\nfrom tdtc_expediente  WITH(NOLOCK) \nwhere nu_ann_exp = ?\nand co_dep_exp = ?\nand co_gru     = '1'");

        StringBuilder sqlQry2 = new StringBuilder();
        sqlQry2.append("select lpad(NVL(max(nu_sec_exp),0) +1, 10, '0') \nfrom tdtc_expediente WITH(NOLOCK) \nwhere nu_ann_exp = ?");

        StringBuilder sqlUpd = new StringBuilder();
        sqlUpd.append("INSERT INTO TDTC_EXPEDIENTE(\nnu_ann_exp,\nnu_sec_exp,\nfe_exp,\nco_proceso,\nde_detalle,\nco_dep_exp,\nco_gru,\nnu_corr_exp,\nnu_expediente,\nnu_folios,\nnu_plazo,\nus_crea_audi,\nfe_crea_audi,\nus_modi_audi,\nfe_modi_audi,\nes_estado)\nVALUES(?,?,(select to_date(?,'DD/MM/YYYY') from dual),?,?,?,'1',?,?,?,?,?,getdate(),?,getdate(),'0')");

        try {
            Map mapResult = this.jdbcTemplate.queryForMap(sqlQry1.toString(), new Object[]{expedienteBean.getCoDepExp(), expedienteBean.getNuAnnExp(), expedienteBean.getCoDepExp()});
            String snuSecExp = (String) this.jdbcTemplate.queryForObject(sqlQry2.toString(), String.class, new Object[]{expedienteBean.getNuAnnExp()});
            expedienteBean.setNuSecExp(snuSecExp);
            String snuCorrExp = String.valueOf(mapResult.get("nuCorrExp"));
            String sdeSiglaCorta = String.valueOf(mapResult.get("deSiglaCorta"));

            expedienteBean.setNuExpediente(Utilidades.AjustaCampoIzquierda(sdeSiglaCorta, 6, "0") + expedienteBean.getNuAnnExp() + snuCorrExp);

            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{expedienteBean.getNuAnnExp(), snuSecExp, expedienteBean.getFeExp(), expedienteBean
                .getCoProceso(), expedienteBean.getDeProceso(), expedienteBean.getCoDepExp(), snuCorrExp, expedienteBean.getNuExpediente(), expedienteBean
                .getNuFolios(), expedienteBean.getNuPlazo(), expedienteBean.getUsCreaAudi(), expedienteBean.getUsCreaAudi()});
            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vReturn;
    }

    public String verificarDocumentoLeido(String pnuAnn, String pnuEmi) {
        String vReturn = "NO_OK";
        StringBuilder sqlQry = new StringBuilder();
        sqlQry.append("SELECT nvl(MAX(es_doc_rec),'0')\nFROM tdtv_destinos WITH(NOLOCK) \nWHERE nu_ann = ?\nAND nu_emi = ?\nAND es_eli = '0'");

        try {
            vReturn = (String) this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[]{pnuAnn, pnuEmi});
        } catch (Exception e) {
            e.printStackTrace();
            vReturn = e.getMessage();
        }
        return vReturn;
    }

    public String anularDocumentoEmiAdm(DocumentoEmiBean documentoEmiBean) {
        String vReturn = "NO_OK";
        StringBuilder sqlQry = new StringBuilder();
        sqlQry.append("SELECT nvl(MAX(nu_cor_doc), 1) + 1\nFROM tdtv_remitos  WITH(NOLOCK) \nWHERE nu_ann         = ?\nAND ti_emi         = ?\nAND co_dep_emi     = ?\nAND co_tip_doc_adm = ?\nAND (  (? IS NOT NULL AND nu_doc_emi = ?)\nOR ? IS NULL\n)");

        StringBuilder sqlUpd = new StringBuilder();
        sqlUpd.append("update tdtv_remitos \nset es_doc_emi = ?,\nnu_cor_doc = ?,fe_use_mod=getdate(),co_use_mod=? \nWHERE nu_ann = ?\nAND nu_emi = ?\nAND es_eli = '0'");

        try {
            String snuCorDoc = (String) this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[]{documentoEmiBean.getNuAnn(), documentoEmiBean.getTiEmi(), documentoEmiBean
                .getCoDepEmi(), documentoEmiBean.getCoTipDocAdm(), documentoEmiBean.getNuDocEmi(), documentoEmiBean.getNuDocEmi(), documentoEmiBean.getNuDocEmi()});

            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{documentoEmiBean.getEsDocEmi(), snuCorDoc, documentoEmiBean.getCoUseMod(), documentoEmiBean
                .getNuAnn(), documentoEmiBean.getNuEmi()});
            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
            vReturn = e.getMessage();
        }
        return vReturn;
    }

    public List<DestinatarioDocumentoEmiBean> getLstDestinatarioGrupo(String pcoGrupo, String pcoTipDoc) {
        StringBuilder sql = new StringBuilder();
        List<DestinatarioDocumentoEmiBean> list = new ArrayList<DestinatarioDocumentoEmiBean>();

        sql.append("SELECT a.co_dep co_dependencia,PK_SGD_DESCRIPCION.DE_DEPENDENCIA(a.co_dep) de_dependencia,\nNVL(a.co_emp, b.co_empleado) co_empleado,PK_SGD_DESCRIPCION.DE_NOM_EMP(NVL(a.co_emp, b.co_empleado)) de_empleado,\nc.co_loc co_local,PK_SGD_DESCRIPCION.DE_LOCAL(c.co_loc) de_local,'1' co_prioridad,\n? co_tramite_first, ? de_tramite_first, ? co_tramite_next, ? de_tramite_next \nFROM (   \n  select y.cemp_codemp co_emp,  x.co_dep\n  from tdtd_dep_gru x, rhtm_per_empleados y\n  where x.co_gru_des= ?\n  and x.es_dep_gru = '1'\n  and y.cemp_codemp (+) = x.co_emp \n  and y.cemp_est_emp(+) = '1'\n  order by y.rowid\n) a , rhtm_dependencia b, sitm_local_dependencia c\nWHERE a.co_dep = b.co_dependencia\nAND a.co_dep = c.co_dep \norder by a.rowid ");

        try {
            if (pcoTipDoc.equals("232")) {
                list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DestinatarioDocumentoEmiBean.class), new Object[]{"4", "ATENDER", "F", "PARA CONOCIMINETO Y FINES", pcoGrupo});
            } else {

                list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DestinatarioDocumentoEmiBean.class), new Object[]{"0", "ORIGINAL", "1", "COPIA", pcoGrupo});
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
            int cant = this.jdbcTemplate.queryForInt("select count(nu_ann) from tdtv_archivo_doc where nu_ann = ? and nu_emi = ?", new Object[]{docObjBean.getNuAnn(), docObjBean.getNuEmi()});
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
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO tdtv_archivo_doc (NU_ANN,NU_EMI,BL_DOC,DE_RUTA_ORIGEN,ES_FIRMA,FEULA,W_BL_DOC,W_DE_RUTA_ORIGEN)\nVALUES(?,?,?,?,'0',TO_CHAR(getdate(),'YYYYMMDD'),?,?)");

            try {
                this.jdbcTemplate.execute(sql.toString(), (PreparedStatementCallback) new AbstractLobCreatingPreparedStatementCallback((LobHandler) defaultLobHandler) {
                    protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
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
            StringBuilder sql1 = new StringBuilder();
            StringBuilder sql2 = new StringBuilder();

            sql1.append("UPDATE TDTV_ARCHIVO_DOC SET W_BL_DOC = BL_DOC, W_DE_RUTA_ORIGEN = DE_RUTA_ORIGEN \nWHERE NU_ANN = ? \nAND NU_EMI = ? \nAND SUBSTR(DE_RUTA_ORIGEN,-4) = 'docx' ");

            sql2.append("UPDATE TDTV_ARCHIVO_DOC SET BL_DOC = ?, DE_RUTA_ORIGEN = ?, FEULA=TO_CHAR(getdate(),'YYYYMMDD')\nWHERE NU_ANN = ? \nAND NU_EMI = ? ");

            try {
                this.jdbcTemplate.update(sql1.toString(), new Object[]{docObjBean.getNuAnn(), docObjBean.getNuEmi()});

                this.jdbcTemplate.execute(sql2.toString(), (PreparedStatementCallback) new AbstractLobCreatingPreparedStatementCallback((LobHandler) defaultLobHandler) {
                    protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
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
            vReturn = (String) this.jdbcTemplate.queryForObject("   SELECT COUNT(1)\n     FROM tdtv_remitos tr WITH(NOLOCK) \n    WHERE tr.nu_ann = ?\n      AND tr.co_dep_emi = ?\n      AND tr.co_tip_doc_adm = ?\n      AND tr.nu_doc_emi = ?\n      AND tr.ti_emi = '06'\n      AND nu_cor_doc = 1\n      AND tr.es_doc_emi NOT IN ('9')", String.class, new Object[]{documentoEmiBean
                .getNuAnn(), documentoEmiBean.getCoDepEmi(), documentoEmiBean
                .getCoTipDocAdm(), documentoEmiBean.getNuDocEmi()});
        } catch (Exception e) {
            e.printStackTrace();
            vReturn = e.getMessage();
        }
        return vReturn;
    }

    public List<EmpleadoBean> getPersonalEditDocAdmEmision(String pcoDepEmi) {
        StringBuilder sql = new StringBuilder();
        List<EmpleadoBean> list = new ArrayList<EmpleadoBean>();

        sql.append("SELECT e.cemp_apepat,e.cemp_apemat,e.cemp_denom, e.CEMP_CODEMP\nFROM RHTM_PER_EMPLEADOS e  WITH(NOLOCK) \nWHERE e.CEMP_EST_EMP = '1'\nAND (CEMP_CO_DEPEND in (select CO_DEPENDENCIA from rhTM_dependencia d where d.CO_DEPENDENCIA =? or CO_DEPEN_PADRE=?) \nOR CO_DEPENDENCIA=? )\nUNION\nSELECT e.cemp_apepat,e.cemp_apemat,e.cemp_denom, e.CEMP_CODEMP\nFROM RHTM_PER_EMPLEADOS e\nWHERE e.CEMP_EST_EMP = '1'\nAND cemp_codemp \nin (\nselect co_emp from tdtx_dependencia_empleado  where co_dep=? and es_emp='0'\nunion select CO_EMPLEADO from rhtm_dependencia  where co_dependencia=?\n )\nORDER BY 1");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[]{pcoDepEmi, pcoDepEmi, pcoDepEmi, pcoDepEmi, pcoDepEmi});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public String updEstadoDocumentoEmiAdm(DocumentoEmiBean documentoEmiBean) {
        String vReturn = "NO_OK";
        StringBuilder sqlUpd = new StringBuilder();

        sqlUpd.append("update tdtv_remitos set CO_USE_MOD=?,ES_DOC_EMI=? ,FE_USE_MOD=getdate() where NU_ANN=? and\nNU_EMI=?");

        try {
            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{documentoEmiBean.getCoUseMod(), documentoEmiBean.getEsDocEmi(), documentoEmiBean
                .getNuAnn(), documentoEmiBean.getNuEmi()});
            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
            vReturn = e.getMessage();
        }

        return vReturn;
    }

    public String updEstadoDocumentoEmitido(DocumentoEmiBean documentoEmiBean) {
        String vReturn = "NO_OK";

        this.spEmiDocumento = (new SimpleJdbcCall(this.dataSource)).withCatalogName("PK_SGD_EMIDOCUMENTO").withProcedureName("UPD_ESTADODOCUMENTOEMITIDO").withoutProcedureColumnMetaDataAccess().useInParameterNames(new String[]{"P_COUSEMOD", "P_NUCORDOC", "P_NUDOCEMI", "P_ESDOCEMI", "P_NUANN", "P_NUEMI"}).declareParameters(new SqlParameter[]{new SqlParameter("P_COUSEMOD", 12), new SqlParameter("P_NUCORDOC", 12), new SqlParameter("P_NUDOCEMI", 12), new SqlParameter("P_ESDOCEMI", 12), new SqlParameter("P_NUANN", 12), new SqlParameter("P_NUEMI", 12)});

        MapSqlParameterSource mapSqlParameterSource = (new MapSqlParameterSource()).addValue("P_COUSEMOD", documentoEmiBean.getCoUseMod()).addValue("P_NUCORDOC", documentoEmiBean.getNuCorDoc()).addValue("P_NUDOCEMI", documentoEmiBean.getNuDocEmi()).addValue("P_ESDOCEMI", documentoEmiBean.getEsDocEmi()).addValue("P_NUANN", documentoEmiBean.getNuAnn()).addValue("P_NUEMI", documentoEmiBean.getNuEmi());
        try {
            this.spEmiDocumento.execute((SqlParameterSource) mapSqlParameterSource);
            vReturn = "OK";
        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return vReturn;
    }

    public String updDocumentoEmiAdmBean(String nuAnn, String nuEmi, DocumentoEmiBean documentoEmiBean, ExpedienteBean expedienteBean, RemitenteEmiBean remitenteEmiBean, String pcoUserMod) {
        String vReturn = "NO_OK";
        StringBuilder sqlUpd = new StringBuilder();
        sqlUpd.append("update tdtv_remitos A \nset A.CO_USE_MOD=?,");

        if (documentoEmiBean != null) {
            documentoEmiBean.setNuAnn(nuAnn);
            sqlUpd.append("A.DE_ASU=?\n,A.NU_DIA_ATE=?\n,A.CO_TIP_DOC_ADM=?\n,A.NU_DOC_EMI=?\n,A.DE_DOC_SIG=?\n,A.NU_COR_DOC=nvl2(?,?,?),\n");

            if (documentoEmiBean.getNuDocEmi() == null || documentoEmiBean.getNuDocEmi().trim().equals("")) {
                String vnuCorDoc = getNroCorrelativoDocumento(documentoEmiBean);
                documentoEmiBean.setNuCorDoc(vnuCorDoc);
            }

            if (documentoEmiBean.getNuCorEmi() == null || documentoEmiBean.getNuCorEmi().trim().equals("")) {
                String vnuCorEmi = getNroCorrelativoEmision(documentoEmiBean);
                documentoEmiBean.setNuCorEmi(vnuCorEmi);
                sqlUpd.append("A.NU_COR_EMI='" + documentoEmiBean.getNuCorEmi() + "',\n");
            }
        }
        if (expedienteBean != null) {
            sqlUpd.append("A.NU_ANN_EXP='" + expedienteBean.getNuAnnExp() + "',\n");
            sqlUpd.append("A.NU_SEC_EXP='" + expedienteBean.getNuSecExp() + "',\n");
        }
        if (remitenteEmiBean != null) {
            sqlUpd.append("A.CO_LOC_EMI='" + remitenteEmiBean.getCoLocal() + "'\n,A.CO_DEP_EMI='" + remitenteEmiBean
                    .getCoDependencia() + "'\n,A.CO_EMP_EMI='" + remitenteEmiBean
                            .getCoEmpFirma() + "',\n");
        }
        sqlUpd.append("A.FE_USE_MOD=getdate() where\nA.NU_ANN=? and\nA.NU_EMI=?");

        try {
            if (documentoEmiBean != null) {
                this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{pcoUserMod, documentoEmiBean.getDeAsu(), documentoEmiBean.getNuDiaAte(), documentoEmiBean
                    .getCoTipDocAdm(), documentoEmiBean.getNuDocEmi(), documentoEmiBean.getDeDocSig(), documentoEmiBean
                    .getNuDocEmi(), documentoEmiBean.getNuCorDoc(), documentoEmiBean.getNuCorDoc(), nuAnn, nuEmi});
            } else {

                this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{pcoUserMod, nuAnn, nuEmi});
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
            StringBuilder sql = new StringBuilder();
            sql.append("         SELECT trim(to_char(nvl(MAX(nu_doc_emi), 0) + 1)) \n           FROM tdtv_remitos tr WITH(NOLOCK) \n          WHERE tr.nu_ann = ?\n            AND tr.co_dep_emi = ?\n            AND tr.co_tip_doc_adm = ?\n            AND tr.ti_emi = '06'\n            AND tr.nu_cor_doc = 1\n            AND tr.es_doc_emi != '9'\n            AND PK_SGD_DESCRIPCION.is_number(nu_doc_emi) = 1");

            vReturn = (String) this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[]{pnuAnn, pcoDepEmi, pcoDoc});
        } catch (Exception e) {
            e.printStackTrace();
            vReturn = "1";
        }

        vReturn = Utility.getInstancia().rellenaCerosIzquierda(vReturn, 6);
        return vReturn;
    }

    public DocumentoObjBean getPropiedadesArchivo(String pnuAnn, String pnuEmi, String ptiCap) {
        StringBuilder sql = new StringBuilder();
        sql.append("select \nnu_ann, \nnu_emi, \nde_ruta_origen nombre_Archivo,\nUPPER(SUBSTR(de_ruta_origen,instr(de_ruta_origen, '.', -1, 1)+1)) tipo_doc\nfrom tdtv_archivo_doc WITH(NOLOCK) \nwhere nu_ann = ?\nand nu_emi = ?");

        DocumentoObjBean docObjBean = new DocumentoObjBean();

        try {
            docObjBean = (DocumentoObjBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoObjBean.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            docObjBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return docObjBean;
    }

    public String getNroCorrelativoDocumento(DocumentoEmiBean documentoEmiBean) {
        String vReturn = "NO_OK";
        StringBuilder sqlQry = new StringBuilder();
        sqlQry.append("SELECT nvl(MAX(nu_cor_doc), 0) + 1\nFROM tdtv_remitos WITH(NOLOCK) \nWHERE nu_ann       = ?\nAND ti_emi         = ?\nAND co_dep_emi     = ?\nAND co_tip_doc_adm = ?");

        try {
            System.out.println(documentoEmiBean.getNuAnn());
            System.out.println(documentoEmiBean.getTiEmi());
            System.out.println(documentoEmiBean.getCoDepEmi());
            System.out.println(documentoEmiBean.getCoTipDocAdm());

            vReturn = (String) this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[]{documentoEmiBean.getNuAnn(), documentoEmiBean.getTiEmi(), documentoEmiBean
                .getCoDepEmi(), documentoEmiBean.getCoTipDocAdm()});

        } catch (Exception e) {
            e.printStackTrace();
            vReturn = e.getMessage();
        }

        return vReturn;
    }

    public String getNroCorrelativoDocumentoDel(DocumentoEmiBean documentoEmiBean) {
        String vReturn = "NO_OK";
        StringBuilder sqlQry = new StringBuilder();
        sqlQry.append("SELECT nvl(MAX(nu_cor_doc), 1) + 1\nFROM tdtv_remitos WITH(NOLOCK) \nWHERE nu_ann       = ?\nAND ti_emi         = ?\nAND co_dep_emi     = ?\nAND co_tip_doc_adm = ?");

        try {
            vReturn = (String) this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[]{documentoEmiBean.getNuAnn(), documentoEmiBean.getTiEmi(), documentoEmiBean
                .getCoDepEmi(), documentoEmiBean.getCoTipDocAdm()});

        } catch (Exception e) {
            e.printStackTrace();
            vReturn = e.getMessage();
        }

        return vReturn;
    }

    public String delAllReferenciaDocumentoEmi(String nuAnn, String nuEmi) {
        String vReturn = "NO_OK";
        StringBuilder sqlDel = new StringBuilder();
        sqlDel.append("DELETE FROM tdtr_referencia\nWHERE NU_ANN = ? AND NU_EMI = ?");

        try {
            this.jdbcTemplate.update(sqlDel.toString(), new Object[]{nuAnn, nuEmi});
            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
            vReturn = e.getMessage();
        }
        return vReturn;
    }

    public String delAllDestinatarioDocumentoEmi(String nuAnn, String nuEmi) {
        String vReturn = "NO_OK";
        StringBuilder sqlDel = new StringBuilder();
        sqlDel.append("DELETE FROM TDTV_DESTINOS\nWHERE NU_ANN = ? AND NU_EMI = ?");

        try {
            this.jdbcTemplate.update(sqlDel.toString(), new Object[]{nuAnn, nuEmi});
            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
            vReturn = e.getMessage();
        }
        return vReturn;
    }

    public String delDocumentoEmiAdm(DocumentoEmiBean documentoEmiBean) {
        String vReturn = "NO_OK";
        StringBuilder sqlUpd = new StringBuilder();

        sqlUpd.append("update tdtv_remitos \nset CO_USE_MOD=?\n,ES_ELI='1',NU_DOC_EMI=?,NU_COR_DOC=?,NU_ANN_EXP=?,NU_SEC_EXP=?,NU_DET_EXP=?\n,FE_USE_MOD=getdate()\nwhere\nNU_ANN=? and\nNU_EMI=?");

        try {
            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{documentoEmiBean.getCoUseMod(), documentoEmiBean.getNuDocEmi(), documentoEmiBean
                .getNuCorDoc(), documentoEmiBean.getNuAnnExp(), documentoEmiBean.getNuSecExp(), documentoEmiBean.getNuDetExp(), documentoEmiBean
                .getNuAnn(), documentoEmiBean.getNuEmi()});
            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
            vReturn = e.getMessage();
        }

        return vReturn;
    }

    public List<DependenciaBean> getListDestinatarioEmi(String pcoDepen, String pdeDepEmite) {
        StringBuilder sql = new StringBuilder();

        boolean vfiltro = (pdeDepEmite != null && !pdeDepEmite.trim().equals(""));

        sql.append("SELECT DE_DEPENDENCIA,\n       CO_DEPENDENCIA,\n       DE_CORTA_DEPEN\n  FROM RHTM_DEPENDENCIA WITH(NOLOCK) \n WHERE co_nivel <> '6'\n   AND IN_BAJA = '0'");

        if (vfiltro) {
            sql.append(" AND (DE_DEPENDENCIA LIKE '%' || ? || '%' OR DE_CORTA_DEPEN LIKE ? || '%' )");
        }
        sql.append(" union  ");
        sql.append("SELECT DE_DEPENDENCIA,\n       CO_DEPENDENCIA,\n       DE_CORTA_DEPEN\n  FROM RHTM_DEPENDENCIA WITH(NOLOCK) \n WHERE co_nivel = '6'\n   AND IN_BAJA = '0'\n   AND (CO_DEPEN_PADRE in ( select CO_DEPEN_PADRE co_dep from RHTM_DEPENDENCIA where CO_DEPENDENCIA = ?)\n       OR ? IN (CO_DEPENDENCIA, CO_DEPEN_PADRE))");

        if (vfiltro) {
            sql.append(" AND (DE_DEPENDENCIA LIKE '%' || ? || '%' OR DE_CORTA_DEPEN LIKE ? || '%')");
        }
        sql.append("  UNION");
        sql.append(" SELECT ' [TODOS]',");
        sql.append(" \t   NULL,");
        sql.append(" \t   NULL");
        sql.append("   FROM dual");
        if (vfiltro) {
            sql.append(" where ' [TODOS]' like '%'||?||'%'");
        }
        sql.append("  ORDER BY 1");

        List<DependenciaBean> list = new ArrayList<DependenciaBean>();

        try {
            if (vfiltro) {
                list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[]{pdeDepEmite, pdeDepEmite, pcoDepen, pcoDepen, pdeDepEmite, pdeDepEmite, pdeDepEmite});
            } else {

                list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[]{pcoDepen, pcoDepen});
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
        StringBuilder sql = new StringBuilder();
        List<DestinoBean> list = new ArrayList<DestinoBean>();
        sql.append("SELECT DISTINCT co_dep_des, ti_des ");
        sql.append("FROM tdtv_destinos WITH(NOLOCK) ");
        sql.append("WHERE es_eli = '0' ");
        sql.append("AND nu_ann = ? ");
        sql.append("AND nu_emi = ? ");
        sql.append("ORDER BY ti_des");
        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DestinoBean.class), new Object[]{nu_ann, nu_emi});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<DestinoBean> getListaDestinosOriTipoDes(String nu_ann, String nu_emi) {
        StringBuilder sql = new StringBuilder();
        List<DestinoBean> list = new ArrayList<DestinoBean>();
        sql.append(" SELECT ti_des,\n decode(ti_des,'02',PK_SGD_DESCRIPCION.DE_PROVEEDOR(nu_ruc_des),\n               '03',PK_SGD_DESCRIPCION.ANI_SIMIL(nu_dni_des),\n               '04',PK_SGD_DESCRIPCION.OTRO_ORIGEN(co_otr_ori_des),' ' ) co_dep_des\n FROM tdtv_destinos \n where nu_ann = ?\n and nu_emi = ?\n and es_eli = '0' \n ORDER BY ti_des ");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DestinoBean.class), new Object[]{nu_ann, nu_emi});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<ReferenciaRemitoBean> getListaReferenciaRemitos(String nu_ann, String nu_emi) {
        StringBuilder sql = new StringBuilder();
        List<ReferenciaRemitoBean> list = new ArrayList<ReferenciaRemitoBean>();
        sql.append("SELECT DISTINCT b.ti_emi, b.co_dep_emi ");
        sql.append("FROM   tdtr_referencia a WITH(NOLOCK), tdtv_remitos b WITH(NOLOCK) ");
        sql.append("WHERE  a.nu_ann_ref = b.nu_ann ");
        sql.append("AND    a.nu_emi_ref = b.nu_emi ");
        sql.append("AND    a.nu_ann = ? ");
        sql.append("AND    a.nu_emi = ? ");
        sql.append("ORDER  BY b.ti_emi");
        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(ReferenciaRemitoBean.class), new Object[]{nu_ann, nu_emi});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<ReferenciaRemitoBean> getOriReferenciaLista(String nu_ann, String nu_emi) {
        StringBuilder sql = new StringBuilder();
        List<ReferenciaRemitoBean> list = new ArrayList<ReferenciaRemitoBean>();
        sql.append(" SELECT b.ti_emi, \n decode(b.ti_emi,'02',PK_SGD_DESCRIPCION.DE_PROVEEDOR(b.nu_ruc_emi),     '03',PK_SGD_DESCRIPCION.ANI_SIMIL(b.nu_dni_emi),\n               '04',PK_SGD_DESCRIPCION.OTRO_ORIGEN(b.co_otr_ori_emi),' ' ) co_dep_emi\n FROM   tdtr_referencia a, tdtv_remitos b \n WHERE  a.nu_ann_ref = b.nu_ann \n AND    a.nu_emi_ref = b.nu_emi \n AND    a.nu_ann = ? \n AND    a.nu_emi = ? \n ORDER  BY b.ti_emi");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(ReferenciaRemitoBean.class), new Object[]{nu_ann, nu_emi});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public List<DestinoBean> getListaDestinosCodPri(String nu_ann, String nu_emi) {
        StringBuilder sql = new StringBuilder();
        List<DestinoBean> list = new ArrayList<DestinoBean>();
        sql.append("SELECT co_pri ");
        sql.append("FROM tdtv_destinos WITH(NOLOCK) ");
        sql.append("WHERE es_eli = '0' ");
        sql.append("AND nu_ann = ? ");
        sql.append("AND nu_emi = ? ");
        sql.append("ORDER BY co_pri desc");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DestinoBean.class), new Object[]{nu_ann, nu_emi});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public String getNumDestinos(String nu_ann, String nu_emi) {
        String vReturn = "0";
        StringBuilder sqlQry = new StringBuilder();
        sqlQry.append("SELECT count(nu_des) ");
        sqlQry.append("FROM tdtv_destinos WITH(NOLOCK) ");
        sqlQry.append("WHERE nu_ann = ? ");
        sqlQry.append("AND nu_emi = ? ");
        try {
            vReturn = (String) this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[]{nu_ann, nu_emi});
        } catch (Exception e) {
            vReturn = "0";
            e.printStackTrace();
        }
        return vReturn;
    }

    public String updRemitoResumenDestinatario(String pnuAnn, String pnuEmi, String vti_des, String vco_pri, String vnu_cant_des, String vresOriDes) {
        String vReturn = "NO_OK";
        StringBuilder sqlUpd = new StringBuilder();
        sqlUpd.append("UPDATE TDTX_REMITOS_RESUMEN SET ");
        sqlUpd.append("TI_EMI_DES = ?,CO_PRIORIDAD = ? ,nu_candes=? ");
        sqlUpd.append("WHERE NU_ANN = ? ");
        sqlUpd.append("AND NU_EMI = ? ");

        StringBuilder sqlUpd2 = new StringBuilder();
        sqlUpd2.append("UPDATE TDTV_REMITOS SET DE_ORI_DES = ?\nWHERE NU_ANN = ? \nAND NU_EMI = ? ");

        try {
            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{vti_des, vco_pri, vnu_cant_des, pnuAnn, pnuEmi});

            this.jdbcTemplate.update(sqlUpd2.toString(), new Object[]{vresOriDes, pnuAnn, pnuEmi});

            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vReturn;
    }

    public String updRemitoResumenReferencia(String pnuAnn, String pnuEmi, String vti_ori, String vdeOriEmi) {
        String vReturn = "NO_OK";
        StringBuilder sqlUpd = new StringBuilder();
        sqlUpd.append("UPDATE TDTX_REMITOS_RESUMEN SET ");
        sqlUpd.append("TI_EMI_REF = ? ");
        sqlUpd.append("WHERE NU_ANN = ? ");
        sqlUpd.append("AND NU_EMI = ?");

        StringBuilder sqlUpd2 = new StringBuilder();
        sqlUpd2.append("UPDATE TDTV_REMITOS SET DE_ORI_EMI = ?\nWHERE NU_ANN = ? \nAND NU_EMI = ? ");

        try {
            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{vti_ori, pnuAnn, pnuEmi});

            this.jdbcTemplate.update(sqlUpd2.toString(), new Object[]{vdeOriEmi, pnuAnn, pnuEmi});

            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vReturn;
    }

    public DocumentoEmiBean existeDocumentoReferenciado(BuscarDocumentoEmiBean buscarDocumentoEmiBean) {
        StringBuilder sql = new StringBuilder();
        DocumentoEmiBean documentoEmiBean = null;
        sql.append("SELECT A.NU_ANN,A.NU_EMI,A.NU_COR_DOC FROM TDTV_REMITOS A WITH(NOLOCK) \nWHERE A.NU_ANN=?\nAND A.CO_DEP_EMI=?\nAND A.TI_EMI='06'\nAND A.CO_TIP_DOC_ADM=?\nAND A.NU_DOC_EMI=?\nAND A.ES_ELI='0'\nAND A.ES_DOC_EMI NOT IN ('5','7','9')");

        try {
            documentoEmiBean = (DocumentoEmiBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class), new Object[]{buscarDocumentoEmiBean
                .getsCoAnnioBus(), buscarDocumentoEmiBean.getsBuscDestinatario(), buscarDocumentoEmiBean.getsDeTipoDocAdm(), buscarDocumentoEmiBean
                .getsNumDocRef()});
            documentoEmiBean.setMsjResult("OK");
        } catch (EmptyResultDataAccessException e) {
            documentoEmiBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentoEmiBean;
    }

    public List<DocumentoEmiBean> getDocumentosReferenciadoBusq(DocumentoEmiBean documentoEmiBean, String sTipoAcceso) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> objectParam = new HashMap<String, Object>();
        List<DocumentoEmiBean> list = new ArrayList<DocumentoEmiBean>();

        sql.append("SELECT A.*, ROWNUM");
        sql.append(" FROM ( ");
        sql.append(" SELECT ");
        sql.append(" DISTINCT B.NU_COR_EMI,PK_SGD_TRAMITE.DE_EMI_REF(B.NU_ANN, B.NU_EMI) DE_EMI_REF,B.FE_EMI,");
        sql.append(" TO_CHAR(B.FE_EMI,'DD/MM/YY') FE_EMI_CORTA,");
        sql.append(" (SELECT CDOC_DESDOC FROM SI_MAE_TIPO_DOC WHERE CDOC_TIPDOC = B.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,");
        sql.append(" DECODE(B.NU_CANDES, 1, PK_SGD_TRAMITE.TI_DES_EMP(B.NU_ANN, B.NU_EMI),PK_SGD_TRAMITE.TI_DES_EMP_V(B.NU_ANN, B.NU_EMI)) DE_EMP_PRO,");
        sql.append(" (SELECT CEMP_APEPAT || ' ' || CEMP_APEMAT || ' ' || CEMP_DENOM FROM RHTM_PER_EMPLEADOS WHERE CEMP_CODEMP = B.CO_EMP_RES) DE_EMP_RES,");
        sql.append(" (SELECT DE_EST FROM TDTR_ESTADOS WHERE CO_EST || DE_TAB = B.ES_DOC_EMI || 'TDTV_REMITOS') DE_ES_DOC_EMI,");
        sql.append(" C.NU_DOC,UPPER(B.DE_ASU) DE_ASU_M,B.NU_ANN,C.NU_EXPEDIENTE,B.NU_EMI,B.TI_CAP,C.IN_EXISTE_DOC EXISTE_DOC,DECODE(NVL(C.TI_EMI_REF,'0')||NVL(C.IN_EXISTE_ANEXO,'2'),'00',0,'02',0,1) EXISTE_ANEXO, NVL(C.CO_PRIORIDAD,'1') CO_PRIORIDAD,");
        sql.append(" B.ES_DOC_EMI");
        sql.append(" FROM ( ");
        sql.append("  SELECT NU_ANN, NU_EMI ");
        sql.append("  FROM TDTR_ARBOL_SEG WITH(NOLOCK) ");
        sql.append("\t START WITH PK_REF = :pCoAnio||:pNuEmi||'0' ");
        sql.append("\tCONNECT BY PRIOR PK_EMI = PK_REF");
        sql.append(" ) A , TDTV_REMITOS B WITH(NOLOCK), TDTX_REMITOS_RESUMEN C WITH(NOLOCK)");
        sql.append(" WHERE A.NU_ANN = B.NU_ANN");
        sql.append(" AND A.NU_EMI = B.NU_EMI");
        sql.append(" AND C.NU_ANN = B.NU_ANN");
        sql.append(" AND C.NU_EMI = B.NU_EMI");
        sql.append(" AND B.TI_EMI='06'");
        sql.append(" AND C.TI_EMI='06'");
        sql.append(" AND B.ES_ELI = '0'");
        sql.append(" AND B.CO_DEP_EMI = :pCoDepEmi");
        if (sTipoAcceso.equals("1")) {
            sql.append(" AND B.CO_EMP_RES = :pcoEmpRes");
            objectParam.put("pcoEmpRes", documentoEmiBean.getCoEmpRes());
        }
        objectParam.put("pCoAnio", documentoEmiBean.getNuAnn());
        objectParam.put("pNuEmi", documentoEmiBean.getNuEmi());
        objectParam.put("pCoDepEmi", documentoEmiBean.getCoDepEmi());

        sql.append(" ORDER BY B.NU_COR_EMI DESC");
        sql.append(") A ");
        sql.append("WHERE ROWNUM < 300");// por lo mientras

        try {
            list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class));
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public TblRemitosBean getDatosDocumento(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        TblRemitosBean tblRemitosBean = null;
        sql.append("SELECT A.NU_ANN,A.NU_EMI,A.CO_DEP_EMI,A.FE_EMI FROM TDTV_REMITOS A WITH(NOLOCK)\nWHERE A.NU_ANN=? AND A.NU_EMI=? AND A.ES_ELI='0'");

        try {
            tblRemitosBean = (TblRemitosBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(TblRemitosBean.class), new Object[]{pnuAnn, pnuEmi});

            tblRemitosBean.setMsgResult("OK");
        } catch (EmptyResultDataAccessException e) {
            tblRemitosBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tblRemitosBean;
    }

    public String getCoEmplFirmoDocumento(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        String result = "NO_OK";
        sql.append("SELECT CO_EMP_EMI\nFROM TDTV_REMITOS WITH(NOLOCK) \nWHERE nu_emi = ?\nAND nu_ann = ?");

        try {
            result = (String) this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[]{pnuEmi, pnuAnn});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public DocumentoEmiBean getEstadoDocumento(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        DocumentoEmiBean documentoEmiBean = null;
        sql.append("SELECT CO_EMP_EMI,ES_DOC_EMI,NU_ANN,NU_EMI,TI_EMI,CO_DEP_EMI,CO_TIP_DOC_ADM,NU_DOC_EMI,CO_EMP_RES\nFROM TDTV_REMITOS WITH(NOLOCK) \nWHERE nu_ann = ?\nAND nu_emi = ? AND ES_ELI='0'");

        try {
            documentoEmiBean = (DocumentoEmiBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            documentoEmiBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentoEmiBean;
    }

    public String getNroCorrelativoEmision(DocumentoEmiBean documentoEmiBean) {
        String vReturn = "0";
        StringBuilder sqlQry = new StringBuilder();
        sqlQry.append("SELECT nvl(MAX(nu_cor_emi), 0) + 1\nFROM tdtv_remitos WITH(NOLOCK) \nWHERE nu_ann = ?\nAND co_dep_emi = ?\nAND ti_emi = ?");

        try {
            vReturn = (String) this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[]{documentoEmiBean.getNuAnn(), documentoEmiBean.getCoDepEmi(), documentoEmiBean.getTiEmi()});

        } catch (Exception e) {
            e.printStackTrace();
            vReturn = "0";
        }
        return vReturn;
    }

    public String updChangeToDespacho(DocumentoEmiBean documentoEmiBean) {
        String vReturn = "NO_OK";
        StringBuilder sqlUpd = new StringBuilder();

        sqlUpd.append("update tdtv_remitos \nset CO_USE_MOD=?\n,ES_DOC_EMI=?\n,FE_USE_MOD=getdate()\n,FE_EMI=getdate()\nwhere\nNU_ANN=? and\nNU_EMI=?");

        try {
            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{documentoEmiBean.getCoUseMod(), documentoEmiBean.getEsDocEmi(), documentoEmiBean
                .getNuAnn(), documentoEmiBean.getNuEmi()});
            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
            vReturn = e.getMessage();
        }

        return vReturn;
    }

    public String updEstadoDocumentoEnvioNotificacion(DocumentoEmiBean documentoEmiBean) {
        String vReturn = "NO_OK";
        StringBuilder sqlUpd = new StringBuilder();
        StringBuilder sqlUpd2 = new StringBuilder();

        sqlUpd.append("UPDATE TDTV_REMITOS \nset CO_USE_MOD=?\n,DOC_ESTADO_MSJ=?\n,COD_DEP_MSJ=?\n,FE_USE_MOD=getdate()\n,TI_ENV_MSJ=?\nwhere\nNU_ANN=? and\nNU_EMI=?");

        sqlUpd2.append("UPDATE TDTV_DESTINOS \nset CO_USE_MOD=?\n,CO_PRI=?\n,FE_USE_MOD=getdate()\nwhere\nNU_ANN=? and\nNU_EMI=?");

        try {
            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{documentoEmiBean.getCoUseMod(), documentoEmiBean.getEsDocEmi(), documentoEmiBean
                .getCoDepEmi(), documentoEmiBean.getTiEnvMsj(), documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi()});
            this.jdbcTemplate.update(sqlUpd2.toString(), new Object[]{documentoEmiBean.getCoUseMod(), documentoEmiBean.getCoPrioridad(), documentoEmiBean
                .getNuAnn(), documentoEmiBean.getNuEmi()});

            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
            vReturn = e.getMessage();
        }

        return vReturn;
    }

    public List<DocumentoBean> getLstDocRecepcionadoRefMp(String pcoDepen, String pannio, String ptiDoc, String pnuDoc) {
        StringBuilder sql = new StringBuilder();
        List<DocumentoBean> list = new ArrayList<DocumentoBean>();

        sql.append("SELECT A.*, ROWNUM");
        sql.append(" FROM ( ");

        sql.append("select *\nfrom(\nselect\nd.FE_REC_DOC,\nSUBSTR(PK_SGD_DESCRIPCION.de_documento(r.co_tip_doc_adm),1,100) de_tip_doc_adm,\nSUBSTR(rr.nu_doc,1,50) NU_DOC,\nTO_CHAR(r.FE_EMI,'DD/MM/YY') FE_EMI_CORTA,\nTO_CHAR(d.FE_REC_DOC,'DD/MM/YY') FE_REC_DOC_CORTA,\nr.nu_ann,\nr.nu_emi,\nd.nu_des,\nREPLACE(LTRIM(RTRIM(r.DE_ASU)),CHR(10),' ') DE_ASU,\nr.co_tip_doc_adm,\nsubstr(PK_SGD_DESCRIPCION.de_dependencia(d.co_dep_des),1,200) de_dep_des,\nSUBSTR(rr.nu_expediente,1,20) NU_EXPEDIENTE,\nr.NU_ANN_EXP,\nr.NU_SEC_EXP\nfrom TDTV_REMITOS R WITH(NOLOCK) ,TDTV_DESTINOS D,TDTX_REMITOS_RESUMEN RR WITH(NOLOCK) \nwhere\nr.es_eli = '0'\nAND d.es_eli = '0'\nAND r.nu_ann = ?/*:b_03.NU_ANN_REF*/\nand rr.nu_ann=r.nu_ann\nAND rr.nu_emi = r.nu_emi\nAND d.nu_ann = r.nu_ann\nAND d.nu_emi = r.nu_emi\nAND r.es_doc_emi NOT IN ('5', '9', '7')\nand d.es_doc_rec <> '0'  \nand d.co_dep_des in \n(select ?/*:b_02.co_dep_emi*/ from dual \nunion \n  SELECT co_dep_ref\n    FROM tdtx_referencia\n   WHERE co_dep_emi = ?/*:b_02.co_dep_emi*/\n     AND ti_emi = 'D'\n     AND es_ref = '1')\nAND r.co_tip_doc_adm = ?/*:B_03.LI_TIP_DOC*/\n");

        if (pnuDoc != null && pnuDoc.trim().length() > 0) {
            pnuDoc = Utility.getInstancia().rellenaCerosIzquierda(pnuDoc, 6);
            sql.append("AND (R.NU_DOC_EMI = '").append(pnuDoc).append("' OR RR.NU_EXPEDIENTE LIKE '%").append(pnuDoc).append("')\n");
        }
        sql.append("UNION \nSELECT\nDE.FE_REC_DOC,\nSUBSTR(PK_SGD_DESCRIPCION.de_documento(r.co_tip_doc_adm),1,100) de_tip_doc_adm,\nSUBSTR(rr.nu_doc,1,50) NU_DOC,\nTO_CHAR(r.FE_EMI,'DD/MM/YY') FE_EMI_CORTA,\nTO_CHAR(DE.FE_REC_DOC,'DD/MM/YY') FE_REC_DOC_CORTA,\nr.nu_ann,\nr.nu_emi,\nDE.nu_des,\nREPLACE(LTRIM(RTRIM(r.DE_ASU)),CHR(10),' ') DE_ASU,\nr.co_tip_doc_adm,\nsubstr(PK_SGD_DESCRIPCION.de_dependencia(DE.co_dep_des),1,200) de_dep_des,\nSUBSTR(rr.nu_expediente,1,20) NU_EXPEDIENTE,\nr.NU_ANN_EXP,\nr.NU_SEC_EXP\nFROM TDTV_DESTINOS DE,TDTV_REMITOS R WITH(NOLOCK) ,TDTX_REMITOS_RESUMEN RR WITH(NOLOCK) \nWHERE R.NU_ANN = ?\nAND DE.NU_ANN=R.NU_ANN\nAND RR.NU_ANN=R.NU_ANN\nAND DE.NU_EMI=RR.NU_EMI\nAND R.NU_EMI=RR.NU_EMI\nAND R.es_eli = '0'\nAND DE.ES_ELI='0'\nand R.es_doc_emi not in ('9','7','5')\nAND R.CO_GRU = '1'\nAND DE.ES_ENV_POR_TRA='1'\nAND r.co_tip_doc_adm = ?\n");

        if (pnuDoc != null && pnuDoc.trim().length() > 0) {
            pnuDoc = Utility.getInstancia().rellenaCerosIzquierda(pnuDoc, 6);
            sql.append("AND (R.NU_DOC_EMI = '").append(pnuDoc).append("' OR RR.NU_EXPEDIENTE LIKE '%").append(pnuDoc).append("')\n");
        }
        sql.append(") E\norder by 1 desc");

        sql.append(") A ");
        sql.append("WHERE ROWNUM < 201");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoBean.class), new Object[]{pannio, pcoDepen, pcoDepen, ptiDoc, pannio, ptiDoc});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public DocumentoEmiBean getEstadoDocumentoAudi(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        DocumentoEmiBean documentoEmiBean = null;
        sql.append("SELECT CO_EMP_EMI,ES_DOC_EMI,NU_ANN,NU_EMI,TI_EMI,CO_DEP_EMI,CO_TIP_DOC_ADM,NU_DOC_EMI\nFROM TDTV_REMITOS WITH(NOLOCK) \nWHERE nu_ann = ?\nAND nu_emi = ?");

        try {
            documentoEmiBean = (DocumentoEmiBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoEmiBean.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            documentoEmiBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentoEmiBean;
    }

    public String insPersonalVoBo(String nuAnn, String nuEmi, String coDep, String coEmp, String coUser) {
        String vReturn = "NO_OK";
        StringBuilder sqlUpd = new StringBuilder();
        sqlUpd.append("INSERT INTO TDTV_PERSONAL_VB(NU_ANN,NU_EMI,CO_DEP,\nCO_EMP_VB,IN_VB,CO_USE_CRE,\nFE_USE_CRE,CO_USE_MOD,FE_USE_MOD)\nVALUES(?,?,?,\n?,'0',?,\n getdate(),?,getdate())");

        try {
            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{nuAnn, nuEmi, coDep, coEmp, coUser, coUser});
            vReturn = "OK";
        } catch (DuplicateKeyException con) {
            vReturn = "Datos Duplicado INSERT TDTV_PERSONAL_VB.";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vReturn;
    }

    public String delPersonalVoBo(String nuAnn, String nuEmi, String coDep, String coEmp) {
        String vReturn = "NO_OK";
        StringBuilder sqlDel = new StringBuilder();
        sqlDel.append("DELETE FROM TDTV_PERSONAL_VB\nWHERE\nNU_ANN=?\nAND NU_EMI=?\nAND CO_DEP=?\nAND CO_EMP_VB=?\nAND IN_VB <> '1'");

        try {
            this.jdbcTemplate.update(sqlDel.toString(), new Object[]{nuAnn, nuEmi, coDep, coEmp});
            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vReturn;
    }

    public String getInNumeraDocAdm(String tipoDoc) {
        String vReturn = "0";
        StringBuilder sqlQry = new StringBuilder();
        sqlQry.append("SELECT IN_NUMERACION FROM SI_MAE_TIPO_DOC WITH(NOLOCK) \nWHERE CDOC_INDBAJ='0'\nAND CDOC_TIPDOC=?");

        try {
            vReturn = (String) this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[]{tipoDoc});
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
        StringBuilder sqlQry = new StringBuilder();
        sqlQry.append("SELECT IN_TIPO_FIRMA FROM SI_MAE_TIPO_DOC WITH(NOLOCK) \nWHERE CDOC_INDBAJ='0'\nAND CDOC_TIPDOC=?");

        try {
            vReturn = (String) this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[]{tipoDoc});
            if (vReturn == null) {
                vReturn = "2";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vReturn;
    }

    public List<DestinatarioDocumentoEmiBean> getLstPersVoBoGrupo(String pcoGrupo) {
        StringBuilder sql = new StringBuilder();
        List<DestinatarioDocumentoEmiBean> list = new ArrayList<DestinatarioDocumentoEmiBean>();

        sql.append("SELECT a.co_dep co_dependencia,b.DE_CORTA_DEPEN de_dependencia,\nNVL(a.co_emp, b.co_empleado) co_empleado,PK_SGD_DESCRIPCION.DE_NOM_EMP(NVL(a.co_emp, b.co_empleado)) de_empleado\nFROM (   \n  select y.cemp_codemp co_emp,  x.co_dep\n  from tdtd_dep_gru x, rhtm_per_empleados y\n  where x.co_gru_des= ?\n  and x.es_dep_gru = '1'\n  and y.cemp_codemp (+) = x.co_emp \n  and y.cemp_est_emp(+) = '1'\n  order by y.rowid\n) a , rhtm_dependencia b, sitm_local_dependencia c\nWHERE a.co_dep = b.co_dependencia\nAND a.co_dep = c.co_dep \norder by a.rowid ");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DestinatarioDocumentoEmiBean.class), new Object[]{pcoGrupo});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public String updArchivarDocumento(DocumentoEmiBean documentoEmiBean, Usuario usuario) {
        String vReturn = "NO_OK";

        this.spEmiDocumento = (new SimpleJdbcCall(this.dataSource)).withCatalogName("PK_SGD_MENSAJERIA").withProcedureName("actualiza_estado_a").withoutProcedureColumnMetaDataAccess().useInParameterNames(new String[]{"pnu_ann", "pnu_emi", "pde_ane"}).declareParameters(new SqlParameter[]{new SqlParameter("pnu_ann", 12), new SqlParameter("pnu_emi", 12), new SqlParameter("pde_ane", 12)});

        MapSqlParameterSource mapSqlParameterSource = (new MapSqlParameterSource()).addValue("pnu_ann", documentoEmiBean.getNuAnn()).addValue("pnu_emi", documentoEmiBean.getNuEmi()).addValue("pde_ane", documentoEmiBean.getDocObser());

        try {
            this.spEmiDocumento.execute((SqlParameterSource) mapSqlParameterSource);
            vReturn = "OK";
        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return vReturn;
    }

    public int getCantidadAnexo(DocumentoEmiBean documentoEmiBean) {
        String vReturn = "0";
        StringBuilder sqlQry = new StringBuilder();
        sqlQry.append("select sum(NVL(B.nu_des,'0'))\nFROM tdtv_destinos A LEFT JOIN tdtv_anexos B \nON A.nu_ann=B.nu_ann AND A.nu_emi=B.nu_emi AND A.nu_des=B.nu_des \nwhere A.nu_ann = ? \nand A.nu_emi = ? \n");

        try {
            vReturn = (String) this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[]{documentoEmiBean.getNuAnn(), documentoEmiBean.getNuEmi()});

        } catch (Exception e) {
            e.printStackTrace();
            vReturn = "0";
        }
        return Integer.parseInt(vReturn);
    }

    public List<TipoCategoriaBean> listTipCategoria() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT  CIDCAT, VDESCAT \nFROM IOTDX_CATEGORIA ORDER BY VDESCAT ASC");

        List<TipoCategoriaBean> list = new ArrayList<TipoCategoriaBean>();

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(TipoCategoriaBean.class), new Object[0]);
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        return list;
    }

    public String insProveedorEmi(ProveedorBean proveedor) {
        String vReturn = "NO_OK";

        this.spProveedor = (new SimpleJdbcCall(this.dataSource)).withCatalogName("PK_SGD_PROVEEDOR").withProcedureName("INS_PROVEEDOR_EMI").withoutProcedureColumnMetaDataAccess().useInParameterNames(new String[]{"PCPRO_RUC", "PCPRO_RAZSOC"}).declareParameters(new SqlParameter[]{new SqlParameter("PCPRO_RUC", 12), new SqlParameter("PCPRO_RAZSOC", 12)});

        MapSqlParameterSource mapSqlParameterSource = (new MapSqlParameterSource()).addValue("PCPRO_RUC", proveedor.getNuRuc()).addValue("PCPRO_RAZSOC", proveedor.getDescripcion());

        try {
            this.spProveedor.execute((SqlParameterSource) mapSqlParameterSource);
            vReturn = "OK";
        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return vReturn;
    }

    public List<TipoDocumentoBean> listTipoDocumentoEmi() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT   UPPER(A.CDOC_DESDOC) DE_TIP_DOC,A.CDOC_TIPDOC CO_TIP_DOC\nFROM IDOSGD.SI_MAE_TIPO_DOC A,IDOSGD.IOTDTX_TIPO_DOCUMENTO B\nWHERE A.CDOC_INDBAJ ='0'\nAND A.CDOC_TIPDOC = B.CDOC_TIPDOC\nUNION \nSELECT '.:TODOS:.' DESCRI ,NULL CODIGO FROM DUAL ORDER BY 1  ");

        List<TipoDocumentoBean> list = new ArrayList<TipoDocumentoBean>();

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(TipoDocumentoBean.class));
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }

        return list;
    }

    public List<TipoDocumentoBean> listTipoDocumentoxRef() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT   UPPER(A.CDOC_DESDOC) DE_TIP_DOC,A.CDOC_TIPDOC CO_TIP_DOC\nFROM IDOSGD.SI_MAE_TIPO_DOC A,IDOSGD.IOTDTX_TIPO_DOCUMENTO B\nWHERE A.CDOC_INDBAJ ='0'\nAND A.CDOC_TIPDOC = B.CDOC_TIPDOC ORDER BY 1");

        List<TipoDocumentoBean> list = new ArrayList<TipoDocumentoBean>();

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(TipoDocumentoBean.class));
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }

        return list;
    }

    public List<EstadoDocumentoBean> listEstadosDocumento(String nomTabla) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DE_EST,CO_EST ");
        sql.append("FROM TDTR_ESTADOS WITH(NOLOCK) ");
        sql.append("WHERE DE_TAB = ? AND CO_EST IN('0','5','7','9') ");
        sql.append("UNION ");
        sql.append("SELECT '.:TODOS.:',NULL FROM DUAL ORDER BY CO_EST ");

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
}
