package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.AnnioBean;
import pe.gob.onpe.tramitedoc.bean.DepartamentoBean;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.bean.DestinatarioBean;
import pe.gob.onpe.tramitedoc.bean.DistritoBean;
import pe.gob.onpe.tramitedoc.bean.EmpleadoBean;
import pe.gob.onpe.tramitedoc.bean.EstadoDocumentoBean;
import pe.gob.onpe.tramitedoc.bean.EtiquetaBean;
import pe.gob.onpe.tramitedoc.bean.ExpedienteBean;
import pe.gob.onpe.tramitedoc.bean.GrupoDestinatarioBean;
import pe.gob.onpe.tramitedoc.bean.LocalBean;
import pe.gob.onpe.tramitedoc.bean.MotivoBean;

import pe.gob.onpe.tramitedoc.bean.PrioridadDocumentoBean;
import pe.gob.onpe.tramitedoc.bean.ProvinciaBean;
import pe.gob.onpe.tramitedoc.bean.RemitenteBean;
import pe.gob.onpe.tramitedoc.bean.SiElementoBean;
import pe.gob.onpe.tramitedoc.bean.TipoDestinatarioEmiBean;
import pe.gob.onpe.tramitedoc.bean.TipoDocumento;
import pe.gob.onpe.tramitedoc.bean.TipoDocumentoBean;
import pe.gob.onpe.tramitedoc.bean.TupaExpedienteBean;
import pe.gob.onpe.tramitedoc.bean.UsuarioConfigBean;
import pe.gob.onpe.tramitedoc.bean.VencimientoBean;
import pe.gob.onpe.tramitedoc.dao.MaestrosDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;


@Repository("maestrosDao")
public class MaestrosDaoImp   extends SimpleJdbcDaoBase  implements MaestrosDao {
	@Override
	public String obtenerIndicarSalida(String coTipoDoc) throws Exception {
		  
		TipoDocumento dato;
		
			    StringBuffer sql = new StringBuffer();
			    
			    sql.append(" select IN_SALEXTERNA indSalExterna from IDOSGD.SI_MAE_TIPO_DOC  where cdoc_tipdoc=?  ");

			    try {
			    	 dato = (TipoDocumento)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TipoDocumento.class),
			    			new Object[] { coTipoDoc});
			     } 
			    catch (EmptyResultDataAccessException e) {
			      return "1";
			    } catch (Exception e) {
			    	 e.printStackTrace();
			      throw e;
			    } 
			    return dato.getIndSalExterna();

	}
	
  public List<AnnioBean> listAnnioEjec() {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT  CANO_ANOEJE CANO, ");
    sql.append("        CANO_ANOEJE DE_ANIO ");
    sql.append("FROM IDOSGD.SI_MAE_ANO_EJECUCION ");
    sql.append("UNION ");
    sql.append("SELECT  '.:TODOS.:', ");
    sql.append("        NULL ");
    sql.append("ORDER BY 1 DESC ");
    
    List<AnnioBean> list = new ArrayList<AnnioBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)ParameterizedBeanPropertyRowMapper.newInstance(AnnioBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    
    return list;
  }

  
  public List<EstadoDocumentoBean> listEstadosDocumento(String nomTabla) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT DE_EST, CO_EST ");
    sql.append("FROM IDOSGD.TDTR_ESTADOS ");
    sql.append("WHERE DE_TAB = ? ");
    sql.append("UNION ");
    sql.append("SELECT '.:TODOS.:', NULL ");
    sql.append("ORDER BY CO_EST ");
    
    List<EstadoDocumentoBean> list = new ArrayList<EstadoDocumentoBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)ParameterizedBeanPropertyRowMapper.newInstance(EstadoDocumentoBean.class), new Object[] { nomTabla });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    
    return list;
  }

  
  public List<EstadoDocumentoBean> listEstMensajeria(String nomTabla) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT '[TODOS]' DE_EST,NULL CO_EST  ");
    sql.append("UNION ");
    sql.append("SELECT DE_EST,CO_EST ");
    sql.append("FROM IDOSGD.TDTR_ESTADOS ");
    sql.append("WHERE DE_TAB = ? ");

    
    List<EstadoDocumentoBean> list = new ArrayList<EstadoDocumentoBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)ParameterizedBeanPropertyRowMapper.newInstance(EstadoDocumentoBean.class), new Object[] { nomTabla });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    
    return list;
  }

  
  public List<PrioridadDocumentoBean> listPrioridadDocumento() {
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT DE_PRI, CO_PRI ");
    sql.append("FROM IDOSGD.TDTR_PRIORIDAD ");
    sql.append("UNION ");
    sql.append("SELECT '.:TODOS.:',NULL ");
    
    List<PrioridadDocumentoBean> list = new ArrayList<PrioridadDocumentoBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)ParameterizedBeanPropertyRowMapper.newInstance(PrioridadDocumentoBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    
    return list;
  }

  //EJHM
  public List<TipoDocumentoBean> listTipoDocumento(String coDependencia) {
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT DE_TIP_DOC, CO_TIP_DOC ");
    sql.append("FROM (SELECT DISTINCT  A.CO_DEP CO_DEP_DES ,CO_DOC CO_TIP_DOC ,");
    sql.append("  CASE  WHEN B.IN_USOABREVIA IS NOT NULL THEN UPPER(B.CDOC_DESDONOMREAL)  ELSE UPPER(B.CDOC_DESDOC) END   DE_TIP_DOC  ");
    sql.append(" from IDOSGD.tdtx_resumen_doc A  INNER JOIN  IDOSGD.SI_MAE_TIPO_DOC B ON A.CO_DOC = B.CDOC_TIPDOC  WHERE ti_tab = 'D'  ) Q1 ");
    sql.append(" WHERE CO_DEP_DES = ? ");
    sql.append("UNION ");
    sql.append("SELECT '.:TODOS:.' DESCRI, NULL CODIGO ORDER BY 1 ");
    
    List<TipoDocumentoBean> list = new ArrayList<TipoDocumentoBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TipoDocumentoBean.class), new Object[] { coDependencia });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    
    return list;
  }

  //EJHM
  public List<TipoDocumentoBean> listTipoDocumentoEmi(String coDependencia) {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT CDOC_DESDOC DE_TIP_DOC,CDOC_TIPDOC CO_TIP_DOC ");
    sql.append(" FROM  (SELECT  A.CDOC_TIPDOC, B.CO_DEP, CASE  WHEN A.IN_USOABREVIA IS NOT NULL THEN UPPER(A.CDOC_DESDONOMREAL)  ELSE UPPER(A.CDOC_DESDOC) END  CDOC_DESDOC,");
    sql.append("  A.CDOC_GRUPO FROM IDOSGD.SI_MAE_TIPO_DOC A INNER JOIN IDOSGD.SITM_DOC_DEPENDENCIA B ON A.CDOC_TIPDOC = B.CO_TIP_DOC  WHERE A.CDOC_INDBAJ ='0'  ) AS Q1 ");

    sql.append(" WHERE CO_DEP = ? ");
    sql.append(" UNION ");
    sql.append(" SELECT '.:TODOS:.' DESCRI ,NULL CODIGO ORDER BY 1 ");
    
    List<TipoDocumentoBean> list = new ArrayList<TipoDocumentoBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TipoDocumentoBean.class), new Object[] { coDependencia });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    
    return list;
  }
  //EJHM tipo doc MP
  public List<TipoDocumentoBean> listTipoDocumentoEmiMesaPartes() {
	    StringBuilder sql = new StringBuilder();
	    sql.append("SELECT CDOC_DESDOC DE_TIP_DOC,CDOC_TIPDOC CO_TIP_DOC ");
	    sql.append(" FROM  (SELECT  A.CDOC_TIPDOC, CASE  WHEN A.IN_USOABREVIA IS NOT NULL THEN UPPER(A.CDOC_DESDONOMREAL)  ELSE UPPER(A.CDOC_DESDOC) END  CDOC_DESDOC,");
	    sql.append("  A.CDOC_GRUPO FROM IDOSGD.SI_MAE_TIPO_DOC A  WHERE A.CDOC_INDBAJ ='0' and a.IN_MESAPARTES='1'  ) AS Q1 ");
	    sql.append(" ");

//	    sql.append(" WHERE CO_DEP = ? ");
	    sql.append(" UNION ");
	    sql.append(" SELECT '.:TODOS:.' DESCRI ,NULL CODIGO ORDER BY 1 ");
	    
	    List<TipoDocumentoBean> list = new ArrayList<TipoDocumentoBean>();
	    
	    try {
	      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TipoDocumentoBean.class), new Object[] {  });
	    } catch (EmptyResultDataAccessException e) {
	      list = null;
	    } catch (Exception e) {
	      list = null;
	      e.printStackTrace();
	    } 
	    
	    return list;
	  }

  
  public List<ExpedienteBean> listExpedientes(String coDependencia) {
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT DE_EXP, CO_EXP ");
    sql.append("FROM IDOSGD.TDTR_EXPEDIENTE ");
    sql.append("WHERE CO_DEP = ? ");
    sql.append("UNION SELECT '.: TODOS :.', NULL ");
    sql.append("UNION SELECT '.:SIN EXPEDIENTE:.', 'SINEX' ORDER BY 1 ");
    
    List<ExpedienteBean> list = new ArrayList<ExpedienteBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ExpedienteBean.class), new Object[] { coDependencia });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    
    return list;
  }

  
  public List<RemitenteBean> listRemitente(String annio, UsuarioConfigBean usuarioConfigBean) {
    StringBuffer sql = new StringBuffer();
    sql.append("select  distinct DE_DEPENDENCIA descrip, ");
    sql.append("\t\tCO_DEPENDENCIA cod_dep, ");
    sql.append("\t\tb.de_corta_depen ");
    sql.append("from IDOSGD.tdtx_resumen_dep a, ");
    sql.append("\t IDOSGD.RHTM_DEPENDENCIA b ");
    sql.append("where a.nu_ann = ? ");
    sql.append("   and a.co_dep_ref = ? ");
    sql.append("   and a.ti_tab='D' ");
    sql.append("   and a.ti_emi = '01' ");
    sql.append("   AND (a.co_emp_res = (CASE ? WHEN 0 THEN a.co_emp_res ELSE ? END) ");
    sql.append("\t   OR ( a.co_emp_res IS NULL )  ) ");
    sql.append("   and a.CO_DEP = b.CO_DEPENDENCIA ");
    sql.append("UNION ");
    sql.append("select distinct ");
    sql.append("\t   CASE a.ti_emi ");
    sql.append("\t\t\tWHEN '02' THEN ' [PROVEEDORES]' ");
    sql.append("\t\t\tWHEN '03' THEN ' [CIUDADANOS]' ");
    sql.append("\t\t\tWHEN '04' THEN ' [OTROS]' ");
    sql.append("\t\t\tWHEN '05' THEN ' [PERSONALES]' ");
    sql.append("\t   END descrip, ");
    sql.append("\t   a.co_dep, ");
    sql.append("\t   NULL ");
    sql.append("from IDOSGD.tdtx_resumen_dep a ");
    sql.append("where a.nu_ann = ? ");
    sql.append("   and a.co_dep_ref = ? ");
    sql.append("   and a.ti_tab='D' ");
    sql.append("   and a.ti_emi <> '01' ");
    sql.append("   AND (a.co_emp_res = (CASE ? WHEN 0 THEN a.co_emp_res ELSE ? END) ");
    sql.append("\t   OR ( a.co_emp_res IS NULL )  ) ");
    sql.append("UNION ");
    sql.append("SELECT ' [TODOS]', ");
    sql.append("\t   NULL, ");
    sql.append("       NULL ");
    sql.append("ORDER BY 1 ");
    
    List<RemitenteBean> list = new ArrayList<RemitenteBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(RemitenteBean.class), new Object[] { annio, usuarioConfigBean.getCoDep(), usuarioConfigBean
            .getTiAcceso(), usuarioConfigBean.getCempCodemp(), annio, usuarioConfigBean.getCoDep(), usuarioConfigBean.getTiAcceso(), usuarioConfigBean.getCempCodemp() });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  public List<RemitenteBean> listRemitenteAnio(Integer annio, UsuarioConfigBean usuarioConfigBean) {
	  Integer annio2= annio-1;
	    StringBuffer sql = new StringBuffer();
	    sql.append("select  distinct DE_DEPENDENCIA descrip, ");
	    sql.append("\t\tCO_DEPENDENCIA cod_dep, ");
	    sql.append("\t\tb.de_corta_depen ");
	    sql.append("from IDOSGD.tdtx_resumen_dep a, ");
	    sql.append("\t IDOSGD.RHTM_DEPENDENCIA b ");
	    sql.append("where a.nu_ann  in(?,?) ");
	    sql.append("   and a.co_dep_ref = ? ");
	    sql.append("   and a.ti_tab='D' ");
	    sql.append("   and a.ti_emi = '01' ");
	    sql.append("   AND (a.co_emp_res = (CASE ? WHEN 0 THEN a.co_emp_res ELSE ? END) ");
	    sql.append("\t   OR ( a.co_emp_res IS NULL )  ) ");
	    sql.append("   and a.CO_DEP = b.CO_DEPENDENCIA ");
	    sql.append("UNION ");
	    sql.append("select distinct ");
	    sql.append("\t   CASE a.ti_emi ");
	    sql.append("\t\t\tWHEN '02' THEN ' [PROVEEDORES]' ");
	    sql.append("\t\t\tWHEN '03' THEN ' [CIUDADANOS]' ");
	    sql.append("\t\t\tWHEN '04' THEN ' [OTROS]' ");
	    sql.append("\t\t\tWHEN '05' THEN ' [PERSONALES]' ");
	    sql.append("\t   END descrip, ");
	    sql.append("\t   a.co_dep, ");
	    sql.append("\t   NULL ");
	    sql.append("from IDOSGD.tdtx_resumen_dep a ");
	    sql.append("where a.nu_ann in(?,?) ");
	    sql.append("   and a.co_dep_ref = ? ");
	    sql.append("   and a.ti_tab='D' ");
	    sql.append("   and a.ti_emi <> '01' ");
	    sql.append("   AND (a.co_emp_res = (CASE ? WHEN 0 THEN a.co_emp_res ELSE ? END) ");
	    sql.append("\t   OR ( a.co_emp_res IS NULL )  ) ");
	    sql.append("UNION ");
	    sql.append("SELECT ' [TODOS]', ");
	    sql.append("\t   NULL, ");
	    sql.append("       NULL ");
	    sql.append("ORDER BY 1 ");
	    
	    List<RemitenteBean> list = new ArrayList<RemitenteBean>();
	    
	    try {
	      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(RemitenteBean.class), new Object[] { annio,annio2, usuarioConfigBean.getCoDep(),
	    		  usuarioConfigBean.getTiAcceso(), usuarioConfigBean.getCempCodemp(), annio,annio2, usuarioConfigBean.getCoDep(), usuarioConfigBean.getTiAcceso(), usuarioConfigBean.getCempCodemp() });
	    } catch (EmptyResultDataAccessException e) {
	      list = null;
	    } catch (Exception e) {
	      list = null;
	      e.printStackTrace();
	    } 
	    return list;
	  }

  
  public List<DestinatarioBean> listDestinatario(String annio, String coDependencia, String ptiAcceso, String pcoEmpleado) {
    StringBuffer sql = new StringBuffer();
    boolean bquery = false;
    
    if (ptiAcceso != null && (ptiAcceso.equals("0") || ptiAcceso.equals("2"))) {
      sql.append("SELECT SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](CO_EMP_RES), 1, 200) nombres, ");
      sql.append("\t   CO_EMP_RES CO_EMP_DES ");
      sql.append("FROM ( select DISTINCT CO_EMP_RES ");
      sql.append("\t   from IDOSGD.TDTX_RESUMEN_DEP ");
      sql.append("\t   where NU_ANN = ? ");
      sql.append("\t   AND co_dep_REF = ? ");
      sql.append("\t   AND TI_TAB = 'D') A ");
      sql.append("UNION ");
      sql.append("SELECT ' [TODOS]', ");
      sql.append("\t   NULL ");
      sql.append("ORDER BY 1 ");
      bquery = true;
    } else {
      sql.append("SELECT SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](CO_EMP_RES), 1, 200) nombres, ");
      sql.append("\t   CO_EMP_RES CO_EMP_DES ");
      sql.append("FROM ( select DISTINCT CO_EMP_RES ");
      sql.append("\t   from IDOSGD.TDTX_RESUMEN_DEP ");
      sql.append("\t   where NU_ANN = ? ");
      sql.append("\t   AND co_dep_REF = ? ");
      sql.append("\t   AND TI_TAB = 'D' ");
      sql.append("\t   AND CO_EMP_RES=?) A ");
      sql.append("UNION ");
      sql.append("SELECT ' [TODOS]', ");
      sql.append("\t   NULL ");
      sql.append("ORDER BY 1 ");
    } 
    
    List<DestinatarioBean> list = new ArrayList<DestinatarioBean>();
    
    try {
      if (bquery) {
        list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinatarioBean.class), new Object[] { annio, coDependencia });
      } else {
        list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinatarioBean.class), new Object[] { annio, coDependencia, pcoEmpleado });
      } 
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  
  public List<DestinatarioBean> listDestinatarioAnio(Integer annio, String coDependencia, String ptiAcceso, String pcoEmpleado) {
	    StringBuffer sql = new StringBuffer();
	    boolean bquery = false;
	    Integer annio2= annio-1;
	    
	    if (ptiAcceso != null && (ptiAcceso.equals("0") || ptiAcceso.equals("2"))) {
	      sql.append("SELECT SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](CO_EMP_RES), 1, 200) nombres, ");
	      sql.append("\t   CO_EMP_RES CO_EMP_DES ");
	      sql.append("FROM ( select DISTINCT CO_EMP_RES ");
	      sql.append("\t   from IDOSGD.TDTX_RESUMEN_DEP ");
	      sql.append("\t   where NU_ANN IN  (?,?) ");
	      sql.append("\t   AND co_dep_REF = ? ");
	      sql.append("\t   AND TI_TAB = 'D') A ");
	      sql.append("UNION ");
	      sql.append("SELECT ' [TODOS]', ");
	      sql.append("\t   NULL ");
	      sql.append("ORDER BY 1 ");
	      bquery = true;
	    } else {
	      sql.append("SELECT SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](CO_EMP_RES), 1, 200) nombres, ");
	      sql.append("\t   CO_EMP_RES CO_EMP_DES ");
	      sql.append("FROM ( select DISTINCT CO_EMP_RES ");
	      sql.append("\t   from IDOSGD.TDTX_RESUMEN_DEP ");
	      sql.append("\t   where NU_ANN IN( ?,?) ");
	      sql.append("\t   AND co_dep_REF = ? ");
	      sql.append("\t   AND TI_TAB = 'D' ");
	      sql.append("\t   AND CO_EMP_RES=?) A ");
	      sql.append("UNION ");
	      sql.append("SELECT ' [TODOS]', ");
	      sql.append("\t   NULL ");
	      sql.append("ORDER BY 1 ");
	    } 
	    
	    List<DestinatarioBean> list = new ArrayList<DestinatarioBean>();
	    
	    try {
	      if (bquery) {
	        list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinatarioBean.class), new Object[] { annio,annio2, coDependencia });
	      } else {
	        list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinatarioBean.class), new Object[] { annio,annio2, coDependencia, pcoEmpleado });
	      } 
	    } catch (EmptyResultDataAccessException e) {
	      list = null;
	    } catch (Exception e) {
	      list = null;
	      e.printStackTrace();
	    } 
	    return list;
	  }


  
  public List<EmpleadoBean> listEmpleadoDependencia(String pcodDependencia) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT  e.cemp_apepat, ");
    sql.append("        e.cemp_apemat, ");
    sql.append("        e.CEMP_DENOM, ");
    sql.append("        e.CEMP_CODEMP ");
    sql.append("FROM IDOSGD.RHTM_PER_EMPLEADOS e ");
    sql.append("where e.CEMP_EST_EMP = '1' ");
    sql.append("and (CEMP_CO_DEPEND = ? or CO_DEPENDENCIA = ?) ");
    sql.append("ORDER BY 1 ");
    
    List<EmpleadoBean> list = new ArrayList<EmpleadoBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[] { pcodDependencia, pcodDependencia });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<RemitenteBean> listReferenciaOrigen(String annio, UsuarioConfigBean usuarioConfigBean) {
    StringBuffer sql = new StringBuffer();
    sql.append("select  distinct ");
    sql.append("        b.DE_DEPENDENCIA descrip, ");
    sql.append("        b.CO_DEPENDENCIA cod_dep, ");
    sql.append("        b.DE_CORTA_DEPEN ");
    sql.append("from IDOSGD.tdtx_resumen_dep a, ");
    sql.append("\t IDOSGD.RHTM_DEPENDENCIA b ");
    sql.append("where a.nu_ann = ? ");
    sql.append("and a.co_dep = ? ");
    sql.append("and a.ti_tab='R' ");
    sql.append("and a.ti_emi = '01' ");
    sql.append("and a.co_emp_res = (CASE ? WHEN 0 THEN a.co_emp_res ELSE ? END) ");
    sql.append("and a.CO_DEP_REF = b.CO_DEPENDENCIA ");
    sql.append("UNION ");
    sql.append("select  distinct ");
    sql.append("        CASE a.ti_emi ");
    sql.append("            WHEN '02' THEN ' [PROVEEDORES]' ");
    sql.append("            WHEN '03' THEN ' [CIUDADANOS]' ");
    sql.append("\t\tWHEN '04' THEN ' [OTROS]' ");
    sql.append("            WHEN '05' THEN ' [PERSONALES]' ");
    sql.append("        END descrip, ");
    sql.append("        a.co_dep_ref cod_dep, ");
    sql.append("        NULL ");
    sql.append("from IDOSGD.tdtx_resumen_dep a ");
    sql.append("where a.nu_ann = ? ");
    sql.append("and a.co_dep = ? ");
    sql.append("and a.ti_tab='R' ");
    sql.append("and a.ti_emi <> '01' ");
    sql.append("and a.co_emp_res = (CASE ? WHEN 0 THEN a.co_emp_res ELSE ? END) ");
    sql.append("UNION ");
    sql.append("SELECT ' [TODOS]', ");
    sql.append("\t   NULL, ");
    sql.append("\t   NULL ");
    sql.append("ORDER BY 1 ");
    
    List<RemitenteBean> list = new ArrayList<RemitenteBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(RemitenteBean.class), new Object[] { annio, usuarioConfigBean.getCoDep(), usuarioConfigBean
            .getTiAcceso(), usuarioConfigBean.getCempCodemp(), annio, usuarioConfigBean.getCoDep(), usuarioConfigBean.getTiAcceso(), usuarioConfigBean.getCempCodemp() });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  // EJHM
  public List<RemitenteBean> listReferenciaOrigenAnio(Integer annio, UsuarioConfigBean usuarioConfigBean) {
	  
	  Integer annio2= annio-1;
	    StringBuffer sql = new StringBuffer();
	    sql.append("select  distinct ");
	    sql.append("        b.DE_DEPENDENCIA descrip, ");
	    sql.append("        b.CO_DEPENDENCIA cod_dep, ");
	    sql.append("        b.DE_CORTA_DEPEN ");
	    sql.append("from IDOSGD.tdtx_resumen_dep a, ");
	    sql.append("\t IDOSGD.RHTM_DEPENDENCIA b ");
	    sql.append("where a.nu_ann  in( ?,?) ");
	    sql.append("and a.co_dep = ? ");
	    sql.append("and a.ti_tab='R' ");
	    sql.append("and a.ti_emi = '01' ");
	    sql.append("and a.co_emp_res = (CASE ? WHEN 0 THEN a.co_emp_res ELSE ? END) ");
	    sql.append("and a.CO_DEP_REF = b.CO_DEPENDENCIA ");
	    sql.append("UNION ");
	    sql.append("select  distinct ");
	    sql.append("        CASE a.ti_emi ");
	    sql.append("            WHEN '02' THEN ' [PROVEEDORES]' ");
	    sql.append("            WHEN '03' THEN ' [CIUDADANOS]' ");
	    sql.append("\t\tWHEN '04' THEN ' [OTROS]' ");
	    sql.append("            WHEN '05' THEN ' [PERSONALES]' ");
	    sql.append("        END descrip, ");
	    sql.append("        a.co_dep_ref cod_dep, ");
	    sql.append("        NULL ");
	    sql.append("from IDOSGD.tdtx_resumen_dep a ");
	    sql.append("where a.nu_ann in (?,?) ");
	    sql.append("and a.co_dep = ? ");
	    sql.append("and a.ti_tab='R' ");
	    sql.append("and a.ti_emi <> '01' ");
	    sql.append("and a.co_emp_res = (CASE ? WHEN 0 THEN a.co_emp_res ELSE ? END) ");
	    sql.append("UNION ");
	    sql.append("SELECT ' [TODOS]', ");
	    sql.append("\t   NULL, ");
	    sql.append("\t   NULL ");
	    sql.append("ORDER BY 1 ");
	    
	    List<RemitenteBean> list = new ArrayList<RemitenteBean>();
	    
	    try {
	      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(RemitenteBean.class), new Object[] { annio,annio2, usuarioConfigBean.getCoDep(), usuarioConfigBean
	            .getTiAcceso(), usuarioConfigBean.getCempCodemp(), annio,annio2 , usuarioConfigBean.getCoDep(), usuarioConfigBean.getTiAcceso(), usuarioConfigBean.getCempCodemp() });
	    } catch (EmptyResultDataAccessException e) {
	      list = null;
	    } catch (Exception e) {
	      list = null;
	      e.printStackTrace();
	    } 
	    return list;
	  }

  
  public List<DependenciaBean> listDestinatarioEmi(String annio, UsuarioConfigBean usuarioConfigBean) {
    StringBuffer sql = new StringBuffer();
    sql.append("select  distinct ");
    sql.append("        b.DE_DEPENDENCIA deDependencia, ");
    sql.append("        a.co_dep_ref coDependencia, ");
    sql.append("        b.DE_CORTA_DEPEN ");
    sql.append("from IDOSGD.tdtx_resumen_dep a, ");
    sql.append("\t IDOSGD.RHTM_DEPENDENCIA b ");
    sql.append("where a.nu_ann = ? ");
    sql.append("and a.co_dep = ? ");
    sql.append("and a.ti_tab='D' ");
    sql.append("and a.ti_emi = '01' ");
    sql.append("and a.co_emp_res = (CASE ? ");
    sql.append("                        WHEN 0 THEN a.co_emp_res ");
    sql.append("                        WHEN 2 THEN a.co_emp_res ");
    sql.append("                        ELSE ? ");
    sql.append("\t\t\tEND) ");
    sql.append("and a.CO_DEP_ref = b.CO_DEPENDENCIA ");
    sql.append("UNION ");
    sql.append("select  distinct ");
    sql.append("        CASE a.ti_emi ");
    sql.append("\t\tWHEN '02' THEN ' [PROVEEDORES]' ");
    sql.append("\t\tWHEN '03' THEN ' [CIUDADANOS]' ");
    sql.append("\t\tWHEN '04' THEN ' [OTROS]' ");
    sql.append("\t\tWHEN '05' THEN ' [PERSONALES]' ");
    sql.append("        END deDependencia, ");
    sql.append("        a.co_dep_ref coDependencia, ");
    sql.append("        NULL ");
    sql.append("from IDOSGD.tdtx_resumen_dep a ");
    sql.append("where a.nu_ann = ? ");
    sql.append("and a.co_dep = ? ");
    sql.append("and a.ti_tab='D' ");
    sql.append("and a.ti_emi <> '01' ");
    sql.append("and a.co_emp_res = (CASE ? ");
    sql.append("                        WHEN 0 THEN a.co_emp_res ");
    sql.append("                        WHEN 2 THEN a.co_emp_res ");
    sql.append("                        ELSE ? ");
    sql.append("\t\t\tEND) ");
    sql.append("UNION ");
    sql.append("SELECT ' [TODOS]', ");
    sql.append("\t   NULL, ");
    sql.append("\t   NULL ");
    sql.append("ORDER BY 1 ");
    
    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { annio, usuarioConfigBean.getCoDep(), 
    		  usuarioConfigBean.getTiConsulta(), usuarioConfigBean.getCempCodemp(), annio, usuarioConfigBean.getCoDep(), usuarioConfigBean.getTiConsulta(), usuarioConfigBean.getCempCodemp() });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  // EJHM
  public List<DependenciaBean> listDestinatarioEmiAnio(Integer annio, UsuarioConfigBean usuarioConfigBean) {
	  
	 // String annio2= String.valueOf(Integer.parseInt(annio)-1);
	  Integer annio2= annio-1;
	    StringBuffer sql = new StringBuffer();
	    sql.append("select  distinct ");
	    sql.append("        b.DE_DEPENDENCIA deDependencia, ");
	    sql.append("        a.co_dep_ref coDependencia, ");
	    sql.append("        b.DE_CORTA_DEPEN ");
	    sql.append("from IDOSGD.tdtx_resumen_dep a, ");
	    sql.append("\t IDOSGD.RHTM_DEPENDENCIA b ");
	    sql.append("where a.nu_ann in(?,?) ");
	    sql.append("and a.co_dep = ? ");
	    sql.append("and a.ti_tab='D' ");
	    sql.append("and a.ti_emi = '01' ");
	    sql.append("and a.co_emp_res = (CASE ? ");
	    sql.append("                        WHEN 0 THEN a.co_emp_res ");
	    sql.append("                        WHEN 2 THEN a.co_emp_res ");
	    sql.append("                        ELSE ? ");
	    sql.append("\t\t\tEND) ");
	    sql.append("and a.CO_DEP_ref = b.CO_DEPENDENCIA ");
	    sql.append("UNION ");
	    sql.append("select  distinct ");
	    sql.append("        CASE a.ti_emi ");
	    sql.append("\t\tWHEN '02' THEN ' [PROVEEDORES]' ");
	    sql.append("\t\tWHEN '03' THEN ' [CIUDADANOS]' ");
	    sql.append("\t\tWHEN '04' THEN ' [OTROS]' ");
	    sql.append("\t\tWHEN '05' THEN ' [PERSONALES]' ");
	    sql.append("        END deDependencia, ");
	    sql.append("        a.co_dep_ref coDependencia, ");
	    sql.append("        NULL ");
	    sql.append("from IDOSGD.tdtx_resumen_dep a ");
	    sql.append("where a.nu_ann in(?,?) ");
	    sql.append("and a.co_dep = ? ");
	    sql.append("and a.ti_tab='D' ");
	    sql.append("and a.ti_emi <> '01' ");
	    sql.append("and a.co_emp_res = (CASE ? ");
	    sql.append("                        WHEN 0 THEN a.co_emp_res ");
	    sql.append("                        WHEN 2 THEN a.co_emp_res ");
	    sql.append("                        ELSE ? ");
	    sql.append("\t\t\tEND) ");
	    sql.append("UNION ");
	    sql.append("SELECT ' [TODOS]', ");
	    sql.append("\t   NULL, ");
	    sql.append("\t   NULL ");
	    sql.append("ORDER BY 1 ");
	    
	    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
	    
	    try {
	      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] 
	    		  { annio,annio2, usuarioConfigBean.getCoDep(),usuarioConfigBean.getTiConsulta(), usuarioConfigBean.getCempCodemp(), 
	    		    annio,annio2, usuarioConfigBean.getCoDep(), usuarioConfigBean.getTiConsulta(), usuarioConfigBean.getCempCodemp() });
	    } catch (EmptyResultDataAccessException e) {
	      list = null;
	    } catch (Exception e) {
	      list = null;
	      e.printStackTrace();
	    } 
	    return list;
	  }

  
  public List<EmpleadoBean> listEmpleadoElaboradoPor(String pcodDependencia, String ptiAcceso, String pcoEmpleado) {
    StringBuffer sql = new StringBuffer();
    boolean bquery = false;
    
    if (ptiAcceso != null && (ptiAcceso.equals("0") || ptiAcceso.equals("2"))) {
      sql.append("SELECT  e.cemp_apepat + ' ' + e.cemp_apemat + ' ' + e.cemp_denom nombre, ");
      sql.append("\te.CEMP_CODEMP ");
      sql.append("FROM IDOSGD.RHTM_PER_EMPLEADOS e ");
      sql.append("where e.CEMP_EST_EMP = '1' ");
      sql.append("AND (CEMP_CO_DEPEND in (select CO_DEPENDENCIA ");
      sql.append("                        from IDOSGD.RHTM_DEPENDENCIA d ");
      sql.append("\t\t\twhere d.CO_DEPENDENCIA=? ");
      sql.append("\t\t\tor d.CO_DEPEN_PADRE=?) ");
      sql.append("OR CO_DEPENDENCIA =  ?) ");
      sql.append("UNION ");
      sql.append("SELECT  e.cemp_apepat + ' ' + e.cemp_apemat + ' ' + e.cemp_denom nombre, ");
      sql.append("\te.CEMP_CODEMP ");
      sql.append("FROM IDOSGD.RHTM_PER_EMPLEADOS e ");
      sql.append("where e.CEMP_EST_EMP = '1' ");
      sql.append("AND cemp_codemp in (select co_emp ");
      sql.append("                    from IDOSGD.tdtx_dependencia_empleado ");
      sql.append("                    where co_dep=? ");
      sql.append("                    and es_emp='0') ");
      sql.append("UNION ");
      sql.append("SELECT ' [TODOS]', ");
      sql.append("        NULL ");
      sql.append("ORDER BY 1 ");
      bquery = true;
    } else {
      sql.append("SELECT\te.cemp_apepat + ' ' + e.cemp_apemat + ' ' + e.cemp_denom nombre, ");
      sql.append("        e.CEMP_CODEMP ");
      sql.append("FROM IDOSGD.RHTM_PER_EMPLEADOS e ");
      sql.append("where e.CEMP_EST_EMP = '1' ");
      sql.append("AND (CEMP_CO_DEPEND in (select CO_DEPENDENCIA ");
      sql.append("\t\t\tfrom IDOSGD.RHTM_DEPENDENCIA d ");
      sql.append("\t\t\twhere d.CO_DEPENDENCIA=? ");
      sql.append("\t\t\tor d.CO_DEPEN_PADRE=?) ");
      sql.append("OR CO_DEPENDENCIA =  ?) ");
      sql.append("AND e.CEMP_CODEMP=? ");
      sql.append("UNION ");
      sql.append("SELECT  ' [TODOS]', ");
      sql.append("\tNULL ");
      sql.append("ORDER BY 1 ");
    } 

    
    List<EmpleadoBean> list = new ArrayList<EmpleadoBean>();
    
    try {
      if (bquery) {
        list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[] { pcodDependencia, pcodDependencia, pcodDependencia, pcodDependencia });
      } else {
        
        list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[] { pcodDependencia, pcodDependencia, pcodDependencia, pcoEmpleado });
      }
    
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<EmpleadoBean> listEmpleadoDestino(String pcodDependencia, String ptiAcceso, String pcoEmpleado) {
    StringBuffer sql = new StringBuffer();
    boolean bquery = false;
    
    if (ptiAcceso != null && (ptiAcceso.equals("0") || ptiAcceso.equals("2"))) {
      sql.append("SELECT  e.cemp_apepat + ' ' + e.cemp_apemat + ' ' + e.cemp_denom nombre, ");
      sql.append("\te.CEMP_CODEMP ");
      sql.append("FROM IDOSGD.RHTM_PER_EMPLEADOS e ");
      sql.append("where e.CEMP_EST_EMP = '1' ");
      sql.append("AND (CEMP_CO_DEPEND in (select CO_DEPENDENCIA ");
      sql.append("                        from IDOSGD.RHTM_DEPENDENCIA d ");
      sql.append("\t\t\twhere d.CO_DEPENDENCIA=? ");
      sql.append("\t\t\tor d.CO_DEPEN_PADRE=?) ");
      sql.append("OR CO_DEPENDENCIA =  ?) ");
      sql.append("UNION ");
      sql.append("SELECT  e.cemp_apepat + ' ' + e.cemp_apemat + ' ' + e.cemp_denom nombre, ");
      sql.append("\te.CEMP_CODEMP ");
      sql.append("FROM IDOSGD.RHTM_PER_EMPLEADOS e ");
      sql.append("where e.CEMP_EST_EMP = '1' ");
      sql.append("AND cemp_codemp in (select co_emp ");
      sql.append("                    from IDOSGD.tdtx_dependencia_empleado ");
      sql.append("                    where co_dep=? ");
      sql.append("                    and es_emp='0') ");
      sql.append("UNION ");
      sql.append("SELECT ' [TODOS]', ");
      sql.append("        NULL ");
      sql.append("ORDER BY 1 ");
      bquery = true;
    } else {
      sql.append("SELECT\te.cemp_apepat + ' ' + e.cemp_apemat + ' ' + e.cemp_denom nombre, ");
      sql.append("        e.CEMP_CODEMP ");
      sql.append("FROM IDOSGD.RHTM_PER_EMPLEADOS e ");
      sql.append("where e.CEMP_EST_EMP = '1' ");
      sql.append("AND (CEMP_CO_DEPEND in (select CO_DEPENDENCIA ");
      sql.append("\t\t\tfrom IDOSGD.RHTM_DEPENDENCIA d ");
      sql.append("\t\t\twhere d.CO_DEPENDENCIA=? ");
      sql.append("\t\t\tor d.CO_DEPEN_PADRE=?) ");
      sql.append("OR CO_DEPENDENCIA =  ?) ");
      sql.append("AND e.CEMP_CODEMP=? ");
      sql.append("UNION ");
      sql.append("SELECT  ' [TODOS]', ");
      sql.append("\tNULL ");
      sql.append("ORDER BY 1 ");
    } 

    
    List<EmpleadoBean> list = new ArrayList<EmpleadoBean>();
    
    try {
      if (bquery) {
        list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[] { pcodDependencia, pcodDependencia, pcodDependencia, pcodDependencia });
      } else {
        
        list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[] { pcodDependencia, pcodDependencia, pcodDependencia, pcoEmpleado });
      }
    
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DependenciaBean> listDependenciaDestinatarioEmi(String coDepen, String deDepen) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT DE_DEPENDENCIA, ");
    sql.append("       CO_DEPENDENCIA, ");
    sql.append("       DE_CORTA_DEPEN ");
    sql.append("FROM IDOSGD.RHTM_DEPENDENCIA ");
    sql.append("WHERE co_nivel <> '6' ");
    sql.append("AND IN_BAJA = '0' ");
    sql.append("AND (DE_DEPENDENCIA LIKE '%' + ? + '%' OR DE_CORTA_DEPEN LIKE ? + '%' ) ");
    sql.append("UNION ");
    sql.append("SELECT DE_DEPENDENCIA, ");
    sql.append("       CO_DEPENDENCIA, ");
    sql.append("       DE_CORTA_DEPEN ");
    sql.append("FROM IDOSGD.RHTM_DEPENDENCIA ");
    sql.append("WHERE co_nivel = '6' ");
    sql.append("   AND IN_BAJA = '0' ");
    sql.append("   AND (CO_DEPEN_PADRE in (select CO_DEPEN_PADRE co_dep ");
    sql.append("\t\t\t       from IDOSGD.RHTM_DEPENDENCIA ");
    sql.append("\t\t\t       where CO_DEPENDENCIA = ?) ");
    sql.append("       OR ? IN (CO_DEPENDENCIA, CO_DEPEN_PADRE)) ");
    sql.append("   AND (DE_DEPENDENCIA LIKE '%' + ? + '%' OR DE_CORTA_DEPEN LIKE ? + '%') ");
    sql.append("ORDER BY 1 ");
    
    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { deDepen, deDepen, coDepen, coDepen, deDepen, deDepen });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<TipoDocumentoBean> listTipDocXDependencia(String coDependencia) {
    StringBuffer sql = new StringBuffer();
    sql.append(" select  CASE  WHEN A.IN_USOABREVIA IS NOT NULL THEN UPPER(A.CDOC_DESDONOMREAL)  ELSE UPPER(A.CDOC_DESDOC) END DE_TIP_DOC, ");
    //sql.append("SELECT  UPPER(A.CDOC_DESDOC) DE_TIP_DOC, ");
    sql.append(" A.CDOC_TIPDOC CO_TIP_DOC ");
    sql.append("FROM IDOSGD.SI_MAE_TIPO_DOC A, ");
    sql.append("  IDOSGD.SITM_DOC_DEPENDENCIA B ");
    sql.append("WHERE A.CDOC_INDBAJ ='0' ");
    sql.append("AND A.CDOC_TIPDOC = B.CO_TIP_DOC ");
    sql.append("AND B.CO_DEP=? ");
    sql.append("ORDER BY 1 ");
    
    List<TipoDocumentoBean> list = new ArrayList<TipoDocumentoBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TipoDocumentoBean.class), new Object[] { coDependencia });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  //MP EJHM
  public List<TipoDocumentoBean> listTipDocXDependencia() {
	    StringBuffer sql = new StringBuffer();
	    sql.append(" select  CASE  WHEN A.IN_USOABREVIA IS NOT NULL THEN UPPER(A.CDOC_DESDONOMREAL)  ELSE UPPER(A.CDOC_DESDOC) END DE_TIP_DOC, ");
	    
	    sql.append(" A.CDOC_TIPDOC CO_TIP_DOC ");
	    sql.append("FROM IDOSGD.SI_MAE_TIPO_DOC A ");
	   
	    sql.append(" WHERE A.CDOC_INDBAJ ='0' and A.IN_MESAPARTES='1' ");
	  
	  //  sql.append("AND B.CO_DEP=? ");
	    sql.append("ORDER BY 1 ");
	    
	    List<TipoDocumentoBean> list = new ArrayList<TipoDocumentoBean>();
	    
	    try {
	      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TipoDocumentoBean.class), new Object[] { });
	    } catch (EmptyResultDataAccessException e) {
	      list = null;
	    } catch (Exception e) {
	      list = null;
	      e.printStackTrace();
	    } 
	    return list;
	  }
  
  public List<TipoDocumentoBean> listTipDocXDependenciaPersonal(String coDependencia) {
    StringBuffer sql = new StringBuffer();
    //sql.append("SELECT  UPPER(A.CDOC_DESDOC) DE_TIP_DOC, A.CDOC_TIPDOC CO_TIP_DOC,CDOC_GRUPO FROM IDOSGD.SI_MAE_TIPO_DOC A, ");
    sql.append(" SELECT  CASE  WHEN A.IN_USOABREVIA IS NOT  NULL THEN UPPER(A.CDOC_DESDONOMREAL)  ELSE UPPER(A.CDOC_DESDOC) END DE_TIP_DOC, ");
    sql.append(" A.CDOC_TIPDOC CO_TIP_DOC,CDOC_GRUPO FROM IDOSGD.SI_MAE_TIPO_DOC A,  ");
    sql.append(" IDOSGD.SITM_DOC_DEPENDENCIA B WHERE A.CDOC_INDBAJ ='0' AND CDOC_GRUPO='02' ");
    sql.append("AND A.CDOC_TIPDOC = B.CO_TIP_DOC AND B.CO_DEP=? ORDER BY 1 ");





    
    List<TipoDocumentoBean> list = new ArrayList<TipoDocumentoBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TipoDocumentoBean.class), new Object[] { coDependencia });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  public List<PrioridadDocumentoBean> listPrioridadDocTblEmi() {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT DE_PRI, CO_PRI ");
    sql.append("FROM IDOSGD.TDTR_PRIORIDAD ");
    
    List<PrioridadDocumentoBean> list = new ArrayList<PrioridadDocumentoBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)ParameterizedBeanPropertyRowMapper.newInstance(PrioridadDocumentoBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    
    return list;
  }

  
  public List<TipoDocumentoBean> listTipDocReferencia(String coDependencia) {
    StringBuffer sql = new StringBuffer();
    
    sql.append("select  a.CO_DOC CO_TIP_DOC, ");
    sql.append("  CASE  WHEN b.IN_USOABREVIA IS NOT  NULL THEN UPPER(b.CDOC_DESDONOMREAL)  ELSE UPPER(b.CDOC_DESDOC) END DE_TIP_DOC ");
    //sql.append("        b.CDOC_DESDOC DE_TIP_DOC ");
    sql.append("from (select DISTINCT co_doc ");
    sql.append("      from IDOSGD.tdtx_resumen_doc ");
    sql.append("      where CO_DEP = ?) a, ");
    sql.append("\t IDOSGD.SI_MAE_TIPO_DOC b ");
    sql.append("WHERE  A.CO_DOC = B.CDOC_TIPDOC ");
    sql.append("order by 2 ");
    
    List<TipoDocumentoBean> list = new ArrayList<TipoDocumentoBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TipoDocumentoBean.class), new Object[] { coDependencia });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<TipoDestinatarioEmiBean> listTipDestinatarioEmi() {
    StringBuffer sql = new StringBuffer();
    
    sql.append("select  CELE_DESELE de_destinatario, ");
    sql.append("        CELE_CODELE co_destinatario ");
    sql.append("from IDOSGD.SI_ELEMENTO ");
    sql.append("WHERE CTAB_CODTAB='TIP_DESTINO' ");
    sql.append("AND CELE_CODELE not in  ('05') ");
    
    List<TipoDestinatarioEmiBean> list = new ArrayList<TipoDestinatarioEmiBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TipoDestinatarioEmiBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<GrupoDestinatarioBean> listGrupoDestinatario(String codDependencia) {
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT  DE_GRU_DES de_grupo, ");
    sql.append("        CO_GRU_DES co_grupo ");
    sql.append("FROM IDOSGD.TDTM_GRU_DES ");
    sql.append("WHERE CO_DEP=? ");
    sql.append("AND ES_GRU_DES='1' ");
    sql.append("ORDER BY 2 ");
    
    List<GrupoDestinatarioBean> list = new ArrayList<GrupoDestinatarioBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(GrupoDestinatarioBean.class), new Object[] { codDependencia });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<MotivoBean> listMotivoDestinatario(String codDependencia, String coTipoDoc) {
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT  a.de_mot, ");
    sql.append("        a.co_mot ");
    sql.append("FROM IDOSGD.tdtr_motivo a, ");
    sql.append("\t IDOSGD.tdtx_moti_docu_depe b ");
    sql.append("WHERE a.co_mot = b.co_mot ");
    sql.append("AND b.co_dep = ? ");
    sql.append("AND b.co_tip_doc = ? ");
    sql.append("UNION ");
    sql.append("SELECT  de_mot, ");
    sql.append("        co_mot ");
    sql.append("FROM IDOSGD.tdtr_motivo ");
    sql.append("where co_mot in ('0','1') ");
    sql.append("ORDER BY 1 ");
    
    List<MotivoBean> list = new ArrayList<MotivoBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(MotivoBean.class), new Object[] { codDependencia, coTipoDoc });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DependenciaBean> listDependenciaRemitenteEmi(String coDependencia) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT  DE_DEPENDENCIA, ");
    sql.append("        CO_DEPENDENCIA ");
    sql.append("FROM IDOSGD.RHTM_DEPENDENCIA ");
    sql.append("WHERE CO_DEPENDENCIA in (SELECT ? ");
    sql.append("                         UNION ");
    sql.append("                         SELECT co_dep_ref ");
    sql.append("\t\t\t     FROM IDOSGD.tdtx_referencia ");
    sql.append("                         WHERE co_dep_emi = ? ");
    sql.append("                         AND ti_emi = 'R' ");
    sql.append("                         AND es_ref = '1') ");
    
    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { coDependencia, coDependencia });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<LocalBean> listLocalRemitenteEmi(String coDependencia) {
    StringBuffer sql = new StringBuffer();
    sql.append("select DE_NOMBRE_LOCAL DE_LOCAL, ");
    sql.append("\t   CCOD_LOCAL CO_LOCAL ");
    sql.append("from IDOSGD.SI_MAE_LOCAL ");
    sql.append("WHERE CCOD_LOCAL IN (SELECT CO_LOC ");
    sql.append("\t\t\t FROM IDOSGD.SITM_LOCAL_DEPENDENCIA ");
    sql.append("\t\t\t WHERE CO_DEP = ?) ");
    
    List<LocalBean> list = new ArrayList<LocalBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(LocalBean.class), new Object[] { coDependencia });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<LocalBean> listLocal() {
    StringBuffer sql = new StringBuffer();
    sql.append("select DE_NOMBRE_LOCAL DE_LOCAL, ");
    sql.append("\t   CCOD_LOCAL CO_LOCAL ");
    sql.append("from IDOSGD.SI_MAE_LOCAL ");
    sql.append("ORDER BY 2 ");
    
    List<LocalBean> list = new ArrayList<LocalBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(LocalBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<RemitenteBean> listReferenciaOrigenPersonal(String sCoEmpEmi) {
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT ");
    sql.append("        CASE c.ti_emi ");
    sql.append("\t\tWHEN '01' THEN d.de_dependencia ");
    sql.append("\t\tWHEN '02' THEN ' [PROVEEDORES]' ");
    sql.append("\t\tWHEN '03' THEN ' [CIUDADANOS]' ");
    sql.append("\t\tWHEN '04' THEN ' [OTROS]' ");
    sql.append("        END descrip, ");
    sql.append("        CASE c.ti_emi ");
    sql.append("\t\tWHEN '01' THEN c.co_dep_emi ");
    sql.append("\t\tWHEN '02' THEN 'P' ");
    sql.append("\t\tWHEN '03' THEN 'C' ");
    sql.append("\t\tWHEN '04' THEN 'O' ");
    sql.append("        END cod_dep, ");
    sql.append("        d.de_corta_depen ");
    sql.append("FROM   IDOSGD.tdtr_referencia\ta, ");
    sql.append("       IDOSGD.tdtv_remitos\t\tb, ");
    sql.append("       IDOSGD.tdtv_remitos\t\tc, ");
    sql.append("       IDOSGD.rhtm_dependencia  d ");
    sql.append("WHERE b.es_eli = '0' ");
    sql.append("   AND b.co_gru = '1' ");
    sql.append("   AND a.nu_ann = b.nu_ann ");
    sql.append("   AND a.nu_emi = b.nu_emi ");
    sql.append("   AND a.nu_ann_ref = c.nu_ann ");
    sql.append("   AND a.nu_emi_ref = c.nu_emi ");
    sql.append("   AND c.co_dep_emi = d.co_dependencia ");
    sql.append("   AND b.co_emp_emi = ? ");
    sql.append("GROUP BY c.ti_emi, ");
    sql.append("         c.co_dep_emi, ");
    sql.append("         d.de_dependencia, ");
    sql.append("         d.de_corta_depen ");
    sql.append("UNION ");
    sql.append("SELECT ' [TODOS]', ");
    sql.append("       NULL, ");
    sql.append("       NULL ");
    sql.append("ORDER BY 1 ");
    
    List<RemitenteBean> list = new ArrayList<RemitenteBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(RemitenteBean.class), new Object[] { sCoEmpEmi });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DependenciaBean> listDestinatarioEmiPersonal(String sCoEmpEmi) {
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT  CASE c.ti_des ");
    sql.append("\t\tWHEN '01' THEN d.de_dependencia ");
    sql.append("\t\tWHEN '02' THEN ' [PROVEEDORES]' ");
    sql.append("\t\tWHEN '03' THEN ' [CIUDADANOS]' ");
    sql.append("\t\tWHEN '04' THEN ' [OTROS]' ");
    sql.append("        END deDependencia, ");
    sql.append("        CASE c.ti_des ");
    sql.append("\t\tWHEN '01' THEN c.co_dep_des ");
    sql.append("\t\tWHEN '02' THEN 'P' ");
    sql.append("\t\tWHEN '03' THEN 'C' ");
    sql.append("        \tWHEN '04' THEN 'O' ");
    sql.append("        END coDependencia, ");
    sql.append("        d.DE_CORTA_DEPEN ");
    sql.append("FROM IDOSGD.tdtv_remitos    b, ");
    sql.append("     IDOSGD.tdtv_destinos   c ");
    sql.append("\t LEFT OUTER JOIN IDOSGD.rhtm_dependencia  d ");
    sql.append("\t ON c.co_dep_des = d.co_dependencia ");
    sql.append("WHERE b.es_eli = '0' ");
    sql.append("   AND b.co_gru = '1' ");
    sql.append("   AND b.nu_ann = c.nu_ann ");
    sql.append("   AND b.nu_emi = c.nu_emi ");
    sql.append("   AND b.co_emp_emi = ? ");
    sql.append("GROUP BY c.ti_des, ");
    sql.append("         c.co_dep_des, ");
    sql.append("         d.de_dependencia, ");
    sql.append("         d.de_corta_depen ");
    sql.append("UNION ");
    sql.append("SELECT ' [TODOS]', ");
    sql.append("       NULL, ");
    sql.append("       NULL ");
    sql.append("ORDER BY 1 ");
    
    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { sCoEmpEmi });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public DependenciaBean getDatosDependencia(String codDependencia) {
    StringBuffer sql = new StringBuffer();
    sql.append("select  co_dependencia, ");
    sql.append("        co_empleado, ");
    sql.append("de_dependencia tituloDep,\n");
    sql.append("        [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](co_empleado) de_dependencia, ");
    sql.append("        de_sigla ");
    sql.append("from IDOSGD.rhtm_dependencia ");
    sql.append("where co_dependencia = ? ");
    
    DependenciaBean depEmi = new DependenciaBean();
    
    try {
      depEmi = (DependenciaBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { codDependencia });
    } catch (EmptyResultDataAccessException e) {
      depEmi = null;
    } catch (Exception e) {
      depEmi = null;
      e.printStackTrace();
    } 
    
    return depEmi;
  }


  
  public List<EstadoDocumentoBean> listEstadosDocumentoMP(String nomTabla) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT  DE_EST_MP DE_EST, ");
    sql.append("        CO_EST ");
    sql.append("FROM IDOSGD.TDTR_ESTADOS ");
    sql.append("WHERE DE_TAB=? ");
    sql.append("AND DE_EST_MP IS NOT NULL ");
    sql.append("UNION ");
    sql.append("SELECT  '.: TODOS :.', ");
    sql.append("        NULL ");
    sql.append("ORDER BY CO_EST ");
    
    List<EstadoDocumentoBean> list = new ArrayList<EstadoDocumentoBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)ParameterizedBeanPropertyRowMapper.newInstance(EstadoDocumentoBean.class), new Object[] { nomTabla });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    
    return list;
  }

  
  public List<TipoDestinatarioEmiBean> listTipEmisorDocExtRecep() {
    StringBuffer sql = new StringBuffer();
    
    sql.append("select  CELE_DESELE de_destinatario, ");
    sql.append("        CELE_CODELE co_destinatario ");
    sql.append("from IDOSGD.SI_ELEMENTO ");
    sql.append("WHERE CTAB_CODTAB='TIP_DESTINO' ");
    sql.append("AND CELE_CODELE NOT IN ('01','05') ");
    sql.append("UNION ");
    sql.append("SELECT  '.: TODOS :.', ");
    sql.append("        NULL ");

    
    List<TipoDestinatarioEmiBean> list = new ArrayList<TipoDestinatarioEmiBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TipoDestinatarioEmiBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<TupaExpedienteBean> listTupaExpedienteNew() {
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT  DE_NOMBRE DE_TUPA, ");
    sql.append("        CO_PROCESO CO_TUPA ");
    sql.append("FROM IDOSGD.TDTR_PROCESOS_EXP ");
    sql.append("WHERE TI_PROCESO = '1' ");
    sql.append("AND ES_ESTADO = '1' ");
    sql.append("UNION ");
    sql.append("SELECT  '[SIN TUPA]', ");
    sql.append("        '0000' ");
    sql.append("ORDER BY 2 ");
    
    List<TupaExpedienteBean> list = new ArrayList<TupaExpedienteBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TupaExpedienteBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<TupaExpedienteBean> listTupaExpediente() {
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT DE_NOMBRE DE_TUPA, ");
    sql.append("       CO_PROCESO CO_TUPA ");
    sql.append("FROM IDOSGD.TDTR_PROCESOS_EXP ");
    sql.append("WHERE TI_PROCESO = '1' ");
    sql.append("UNION ");
    sql.append("SELECT  '[SIN TUPA]', ");
    sql.append("        '0000' ");
    sql.append("ORDER BY 2 ");
    
    List<TupaExpedienteBean> list = new ArrayList<TupaExpedienteBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(TupaExpedienteBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  public List<LocalBean> lsLocal() {
    List<LocalBean> list;
    StringBuffer sql = new StringBuffer();
    sql.append("select  DE_NOMBRE_LOCAL DE_LOCAL, ");
    sql.append("        CCOD_LOCAL CO_LOCAL ");
    sql.append("from IDOSGD.SI_MAE_LOCAL ");
    sql.append("union ");
    sql.append("select  ' [TODOS]' DE_LOCAL, ");
    sql.append("        NULL CO_LOCAL ");
    sql.append("ORDER BY 1 ");


    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(LocalBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<EtiquetaBean> getEtiquetasList() {
    StringBuffer sql = new StringBuffer();
    
    sql.append("select  co_est,de_est ");
    sql.append("from IDOSGD.tdtr_estados ");
    sql.append("where de_tab = 'CO_ETIQUETA_REC' ");
    sql.append("UNION ");
    sql.append("SELECT  NULL, ");
    sql.append("        '.: TODOS :.' ");
    
    List<EtiquetaBean> list = new ArrayList<EtiquetaBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(EtiquetaBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DependenciaBean> getlsDestinatarioEmiDocExt(String annio, String coDep) {
    StringBuffer sql = new StringBuffer();
    
    sql.append("select  b.DE_DEPENDENCIA deDependencia, ");
    sql.append("        a.co_dep_ref coDependencia, ");
    sql.append("        b.DE_CORTA_DEPEN ");
    sql.append("from IDOSGD.tdtx_resumen_dep a With (readuncommitted), ");
    sql.append(" IDOSGD.RHTM_DEPENDENCIA b With (readuncommitted) ");
    sql.append("where a.nu_ann = ? ");
    //sql.append("   and a.co_dep = ? ");
    sql.append("   and a.ti_tab='D' ");
    sql.append("   and a.ti_emi in ('02', '03', '04') ");
    sql.append("   and a.CO_DEP_ref = b.CO_DEPENDENCIA ");
    sql.append("UNION ");
    sql.append("SELECT  ' [TODOS]', ");
    sql.append("        NULL, ");
    sql.append("        NULL ");
    sql.append("ORDER BY 1 ");
    
    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { annio });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  //EJHM
  public List<DependenciaBean> getlsDestinatarioEmiDocExtAnio(Integer annio, String coDep) {
	    StringBuffer sql = new StringBuffer();
	    Integer annio2= annio-1;
	    
	    sql.append("select  b.DE_DEPENDENCIA deDependencia, ");
	    sql.append("        a.co_dep_ref coDependencia, ");
	    sql.append("        b.DE_CORTA_DEPEN ");
	    sql.append(" from IDOSGD.tdtx_resumen_dep a With (readuncommitted), ");
	    sql.append(" IDOSGD.RHTM_DEPENDENCIA b With (readuncommitted) ");
	    sql.append("where a.nu_ann in (?,?) ");
	//    sql.append("   and a.co_dep = ? ");
	    sql.append("   and a.ti_tab='D' ");
	    sql.append("   and a.ti_emi in ('02', '03', '04') ");
	    sql.append("   and a.CO_DEP_ref = b.CO_DEPENDENCIA ");
	    sql.append("UNION ");
	    sql.append("SELECT  ' [TODOS]', ");
	    sql.append("        NULL, ");
	    sql.append("        NULL ");
	    sql.append("ORDER BY 1 ");
	    
	    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
	    
	    try {
	      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { annio,annio2 });
	    }
	    catch (EmptyResultDataAccessException e) {
	      list = null;
	    } catch (Exception e) {
	      list = null;
	      e.printStackTrace();
	    } 
	    return list;
	  }

  
  public List<DependenciaBean> getlsDestinatarioDocPendEntrega(String coDep) {
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT  DE_DEPENDENCIA, ");
    sql.append("        CO_DEPENDENCIA, ");
    sql.append("        DE_CORTA_DEPEN ");
    sql.append("FROM IDOSGD.RHTM_DEPENDENCIA ");
    sql.append("where CO_DEPENDENCIA in (select CO_DEP ");
    sql.append("                         from IDOSGD.sitm_local_dependencia ");
    sql.append("                         WHERE CO_LOC=?) ");
    sql.append("UNION ");
    sql.append("select  ' [TODOS]', ");
    sql.append("        NULL, ");
    sql.append("        NULL ");
    sql.append("order by 1 ");
    
    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { coDep });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<VencimientoBean> getVencimientoList() {
    StringBuffer sql = new StringBuffer();
    
    sql.append("select  co_est, ");
    sql.append("        de_est ");
    sql.append("from IDOSGD.tdtr_estados ");
    sql.append("where de_tab = 'VENCIMIENTO' ");
    sql.append("UNION ");
    sql.append("SELECT  NULL, ");
    sql.append("        '.: TODOS :.' ");
    
    List<VencimientoBean> list = new ArrayList<VencimientoBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(VencimientoBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<EstadoDocumentoBean> listEstadosDocumentoEmiSegui(String tdtv_destinos) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT  DE_EST, ");
    sql.append("        CO_EST ");
    sql.append("FROM IDOSGD.TDTR_ESTADOS ");
    sql.append("WHERE DE_TAB = ? ");
    sql.append("AND CO_EST NOT IN(5, 9) ");
    sql.append("UNION ");
    sql.append("SELECT  '.:TODOS.:', ");
    sql.append("        NULL ");
    sql.append("ORDER BY CO_EST ");
    
    List<EstadoDocumentoBean> list = new ArrayList<EstadoDocumentoBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)ParameterizedBeanPropertyRowMapper.newInstance(EstadoDocumentoBean.class), new Object[] { tdtv_destinos });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<SiElementoBean> getLsSiElementoBean(String pctabCodtab) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT\tCELE_CODELE, ");
    sql.append("\t\tCELE_DESELE, ");
    sql.append("\t\tNELE_NUMSEC, ");
    sql.append("\t\tCELE_DESCOR,BL_DOC as archivo ");
    sql.append("FROM IDOSGD.SI_ELEMENTO ");
    sql.append("WHERE CTAB_CODTAB = ? ");
    
    List<SiElementoBean> list = null;
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(SiElementoBean.class), new Object[] { pctabCodtab });
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DepartamentoBean> listDepartamentos() {
    StringBuffer sql = new StringBuffer();
    
    sql.append("select ubdep coDepartamento,nodep noDepartamento from IDOSGD.idtubias where nodep != '               ' and nodep !='***************' and ub_inei is not null group by ubdep,nodep order by 1");





    
    List<DepartamentoBean> list = new ArrayList<DepartamentoBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DepartamentoBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DependenciaBean> listNewUpdDependenciaDestinatarioEmi(String coDepen, String deDepen) {
	  if(deDepen==null) {
		  deDepen="";
	  }
    StringBuilder sql = new StringBuilder();
   // sql.append("SELECT TB.* FROM\n(SELECT DE_DEPENDENCIA,\n       CO_DEPENDENCIA,\n       DE_CORTA_DEPEN\n  FROM IDOSGD.RHTM_DEPENDENCIA\n WHERE co_nivel <> '6'\n   AND IN_BAJA = '0'\n   AND (DE_DEPENDENCIA LIKE '%' + ? + '%' OR DE_CORTA_DEPEN LIKE ? + '%' OR ? = 'null' )\nUNION\nSELECT DE_DEPENDENCIA,\n       CO_DEPENDENCIA,\n       DE_CORTA_DEPEN\n  FROM IDOSGD.RHTM_DEPENDENCIA\n WHERE co_nivel = '6'\n   AND IN_BAJA = '0'\n   AND (CO_DEPEN_PADRE in ( select CO_DEPEN_PADRE co_dep from IDOSGD.RHTM_DEPENDENCIA where CO_DEPENDENCIA = ?)  OR ? IN (CO_DEPENDENCIA, CO_DEPEN_PADRE))\n   AND (DE_DEPENDENCIA LIKE '%' + ? + '%' OR DE_CORTA_DEPEN LIKE ? + '%' OR ? = 'null' )\n ) TB \nUNION\nSELECT '[Elija OpciORDER BY 1 \n");

    sql.append(" SELECT TB.* FROM (SELECT DE_DEPENDENCIA, CO_DEPENDENCIA,  DE_CORTA_DEPEN   FROM IDOSGD.RHTM_DEPENDENCIA "); 
    sql.append(" WHERE co_nivel <> '6'    AND IN_BAJA = '0'    AND (DE_DEPENDENCIA LIKE '%' + ? + '%' OR DE_CORTA_DEPEN LIKE ? + '%' OR ? = 'null' ) ");
    sql.append("  UNION SELECT DE_DEPENDENCIA, CO_DEPENDENCIA,  DE_CORTA_DEPEN   FROM IDOSGD.RHTM_DEPENDENCIA  WHERE co_nivel = '6'    AND IN_BAJA = '0' ");  
    sql.append(" AND (CO_DEPEN_PADRE in ( select CO_DEPEN_PADRE co_dep from IDOSGD.RHTM_DEPENDENCIA where CO_DEPENDENCIA = ?)  OR ? IN (CO_DEPENDENCIA, CO_DEPEN_PADRE)) ");  
    sql.append(" AND (DE_DEPENDENCIA LIKE '%' + ? + '%' OR DE_CORTA_DEPEN LIKE ? + '%' OR ? = 'null' )  ) TB  UNION SELECT '[Elija Opción]','','' order by 1 ");

    
    List<DependenciaBean> list = new ArrayList<DependenciaBean>();
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] 
    		  { deDepen + "", deDepen + "", deDepen + "", coDepen + "", coDepen + "", deDepen + "", deDepen + "", deDepen + "" });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  public List<ProvinciaBean> listProvincia(String coDep) {
    List<ProvinciaBean> list;
    StringBuffer sql = new StringBuffer();
    
    sql.append("select ubprv coProvincia, noprv noProvincia from IDOSGD.idtubias where ubdep=? and rtrim(noprv) is not null group by ubprv,noprv order by 2");


    
    try {
      list = this.jdbcTemplate.query(sql.toString(), new Object[] { coDep }, (RowMapper)BeanPropertyRowMapper.newInstance(ProvinciaBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  public List<DistritoBean> listDistrito(String coDep, String coDis) {
    List<DistritoBean> list;
    StringBuffer sql = new StringBuffer();
    sql.append("select ubdis coDistrito, nodis noDistrito from idosgd.idtubias where ubdep=? and ubprv=? and ubdis not in('00') and rtrim(nodis) is not null group by ubdis,nodis order by 2");

    
    try {
      list = this.jdbcTemplate.query(sql.toString(), new Object[] { coDep, coDis }, (RowMapper)BeanPropertyRowMapper.newInstance(DistritoBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }


@Override
public List<SiElementoBean> getLsSiElementoBeanOrder(String pctabCodtab) {
	// TODO Auto-generated method stub
	return null;
}
}
