package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.DocumentoDatoBean;
import pe.gob.onpe.tramitedoc.bean.EmpleadoBean;
import pe.gob.onpe.tramitedoc.dao.NotificacionDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;

@Repository("notificacionDao")
public class NotificacionDaoImp
        extends SimpleJdbcDaoBase
        implements NotificacionDao {

    public DocumentoDatoBean getDatosDoc(String nuAnn, String nuEmi) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT A.NU_EMI, \nA.NU_ANN,\n (CONVERT(VARCHAR(10), A.FE_EMI, 103)  +' '+ convert(VARCHAR(8), A.FE_EMI, 14)) FECHA_DOC,B.NU_DOC SIGLAS_DOC, \nA.DE_ASU, \nA.ES_DOC_EMI, \nIDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(A.CO_TIP_DOC_ADM) TIPO_DOC,\nIDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(A.CO_DEP_EMI) DE_DEP_EMI\nFROM IDOSGD.TDTV_REMITOS A WITH(NOLOCK), IDOSGD.TDTX_REMITOS_RESUMEN B  WITH(NOLOCK)\nWHERE A.NU_ANN=? AND A.NU_EMI=?\nAND B.NU_ANN=? AND B.NU_EMI=?");

        DocumentoDatoBean docDatoBean = new DocumentoDatoBean();

        try {
            docDatoBean = (DocumentoDatoBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoDatoBean.class), new Object[]{nuAnn, nuEmi, nuAnn, nuEmi});
        } catch (EmptyResultDataAccessException e) {
            docDatoBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return docDatoBean;
    }

    public String notificarVistoBueno(String nuEmi, String nuAnn) {
         StringBuilder sql = new StringBuilder();
        List<EmpleadoBean> list = null;
        sql.append("SET LANGUAGE Español ");
        sql.append("select R.CEMP_APEPAT+' '+R.CEMP_APEMAT+' '+R.CEMP_DENOM as cempDenom ,R.CEMP_EMAIL as compName,\n'<b>'+tip.CDOC_DESDOC+' '+ISNULL(rt.NU_DOC_EMI,'')+'-'+(SELECT TOP 1 DE_PAR FROM IDOSGD.TDTR_PARAMETROS WHERE CO_PAR='DE_INSTITUCION'  )+'-'+rt.DE_DOC_SIG+'<br/>'+'FECHA:'+   CONVERT(varchar(20),rt.FE_EMI, 131) +'</b><br/><p ><b>ASUNTO:</b> '+rt.DE_ASU+'</p>' as accionBD from IDOSGD.TDTV_PERSONAL_VB T\ninner join IDOSGD.tdtv_remitos rt ON rt.NU_ANN=T.NU_ANN AND rt.NU_EMI=T.NU_EMI\ninner join IDOSGD.si_mae_tipo_doc tip ON tip.CDOC_TIPDOC=rt.CO_TIP_DOC_ADM\ninner join IDOSGD.rhtm_per_empleados R ON T.CO_EMP_VB=R.CEMP_CODEMP\nwhere T.nu_ann='" + nuAnn + "' and T.nu_emi='" + nuEmi + "'");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(EmpleadoBean.class));
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        if (list != null) {
            EmpleadoBean oEmpleadoBean = new EmpleadoBean();
            for (int i = 0; i < list.size(); i++) {

                oEmpleadoBean = list.get(i);
                RegisternotificarCorreo(oEmpleadoBean.getCempDenom(), oEmpleadoBean.getCompName(), oEmpleadoBean.getAccionBD(), "Notificacion del Documento para Visto Bueno", "El Sistema de Gestion Documental le recuerda que tiene los siguientes documentos por Visar");
            }
        }
        return "OK";
    }

    public String getLsEmpleadoNotificar(String nuEmi, String nuAnn, String detalleDoc) {
         StringBuilder sql = new StringBuilder();
        List<EmpleadoBean> list = null;
        sql.append("SET LANGUAGE Español ");
        sql.append("select DISTINCT R.CEMP_APEPAT+' '+R.CEMP_APEMAT+' '+R.CEMP_DENOM as cempDenom ,R.CEMP_EMAIL as compName,\n ISNULL('<b>EXP. '+(SELECT TOP 1 ISNULL(NU_EXPEDIENTE,'') FROM IDOSGD.TDTC_EXPEDIENTE WHERE NU_SEC_EXP=rt.NU_SEC_EXP  )+'<br/>','')+ISNULL(tip.CDOC_DESDOC,'')+' '+ISNULL(rt.NU_DOC_EMI,'')+'-'+ (CASE WHEN rt.TI_EMI ='01' THEN  (SELECT TOP 1 DE_PAR FROM IDOSGD.TDTR_PARAMETROS WHERE CO_PAR='DE_INSTITUCION'  )  ELSE '' END ) +'-'+rt.DE_DOC_SIG+'<br/>'+'FECHA:'+ CONVERT(varchar(20),rt.FE_EMI, 103) +' '+CONVERT(varchar(20),rt.FE_EMI, 108) +'</b><br/><p ><b>ASUNTO:</b> '+rt.DE_ASU+'</p>' as accionBD  from IDOSGD.tdtv_destinos T\ninner join IDOSGD.tdtv_remitos rt ON rt.NU_ANN=T.NU_ANN AND rt.NU_EMI=T.NU_EMI\ninner join IDOSGD.si_mae_tipo_doc tip ON tip.CDOC_TIPDOC=rt.CO_TIP_DOC_ADM\ninner join IDOSGD.rhtm_per_empleados R ON T.CO_EMP_DES=R.CEMP_CODEMP\ninner join IDOSGD.TDTX_CONFIG_EMP C ON R.CEMP_CODEMP=C.CO_EMP  \nwhere T.nu_ann='" + nuAnn + "' and T.nu_emi='" + nuEmi + "' and  (C.IN_MAIL_DERIV='3' OR (C.IN_MAIL_DERIV=1 and T.CO_PRI IN (2,3)) OR (C.IN_MAIL_DERIV=2 and rt.CO_TIP_DOC_ADM='011'))");

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(EmpleadoBean.class));
        } catch (Exception e) {
            list = null;
            e.printStackTrace();
        }
        if (list != null) {
            EmpleadoBean oEmpleadoBean = new EmpleadoBean();
            for (int i = 0; i < list.size(); i++) {

                oEmpleadoBean = list.get(i);
                RegisternotificarCorreo(oEmpleadoBean.getCempDenom(), oEmpleadoBean.getCompName(), oEmpleadoBean.getAccionBD(), "Notificacion del Documento Derivado", "El Sistema de Gestion Documental le notifica la derivacion del siguiente documento ");
            }
        }
        return "OK";
    }

    public void RegisternotificarCorreo(String v_nom, String p_Para, String detalleDoc, String asunto, String cuerpo) {
        String mensaje = "NO_OK";

        SimpleJdbcCall spNotificarCorreo = (new SimpleJdbcCall(this.dataSource)).withProcedureName("[IDOSGD].[PK_SGD_TRAMITE_CREA_NOTIFICACION_LOG_INSERTA]").withoutProcedureColumnMetaDataAccess().useInParameterNames(new String[]{"NOMBRES", "PARA", "DETALLE", "ASUNTO", "CUERPO"}).declareParameters(new SqlParameter[]{new SqlParameter("NOMBRES", 12), new SqlParameter("PARA", 12), new SqlParameter("DETALLE", 12), new SqlParameter("ASUNTO", 12), new SqlParameter("CUERPO", 12)});

        MapSqlParameterSource mapSqlParameterSource = (new MapSqlParameterSource()).addValue("NOMBRES", v_nom).addValue("PARA", p_Para).addValue("DETALLE", detalleDoc).addValue("ASUNTO", asunto).addValue("CUERPO", cuerpo);

        try {
            spNotificarCorreo.execute((SqlParameterSource) mapSqlParameterSource);
            mensaje = "OK";
        } catch (Exception ex) {
            mensaje = "NO_OK";
            ex.printStackTrace();
        }
    }

    @Override
    public String getLsMesaVirtualNotificar(String nuEmi, String nuAnn, String detalleDoc) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLsTupaNotificar(String nuEmi, String nuAnn, String detalleDoc) {
        // TODO Auto-generated method stub
        return null;
    }
}
