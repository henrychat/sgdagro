package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.CargoFuncionBean;
import pe.gob.onpe.tramitedoc.bean.CiudadanoBean;
import pe.gob.onpe.tramitedoc.bean.DatosInterBean;
import pe.gob.onpe.tramitedoc.bean.DependenciaBean;
import pe.gob.onpe.tramitedoc.bean.DestinatarioOtroOrigenBean;
import pe.gob.onpe.tramitedoc.bean.EmpleadoBean;
import pe.gob.onpe.tramitedoc.bean.EmpleadoVoBoBean;
import pe.gob.onpe.tramitedoc.bean.ProveedorBean;
import pe.gob.onpe.tramitedoc.bean.RemitenteBean;
import pe.gob.onpe.tramitedoc.bean.SiElementoBean;
import pe.gob.onpe.tramitedoc.bean.TablaMaestraBean;
import pe.gob.onpe.tramitedoc.dao.CommonQueryDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;


@Repository("commonQueryDao")
public class CommonQueryDaoImp
  extends SimpleJdbcDaoBase
  implements CommonQueryDao
{
  public DependenciaBean getDependenciaxCoDependencia(String coDepen) {
    StringBuffer sql = new StringBuffer();
    DependenciaBean dependenciaBean = null;
    sql.append("SELECT DE_DEPENDENCIA, CO_DEPENDENCIA FROM IDOSGD.RHTM_DEPENDENCIA WHERE\nCO_DEPENDENCIA=? AND IN_BAJA='0'");

    
    try {
      dependenciaBean = (DependenciaBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { coDepen });
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return dependenciaBean;
  }

  
  public DependenciaBean getDependenciaBasico(String coDepen) {
    StringBuffer sql = new StringBuffer();
    DependenciaBean dependenciaBean = null;
    sql.append("SELECT \nX.DE_DEPENDENCIA, \nX.CO_DEPENDENCIA, \nX.de_sigla,\nX.in_mesa_partes,\n(SELECT DEP.CO_DEPENDENCIA FROM IDOSGD.RHTM_DEPENDENCIA DEP WHERE DEP.IN_MESA_PARTES='1' AND DEP.IN_BAJA='0'\nAND 0 < (SELECT COUNT(1) FROM IDOSGD.TDTR_DEPENDENCIA_MP DMP WHERE DMP.CO_DEP=X.CO_DEPENDENCIA AND DMP.ES_ELI='0' AND DMP.IN_MP='1')) CO_DEP_MP \nFROM IDOSGD.RHTM_DEPENDENCIA X \nWHERE X.CO_DEPENDENCIA=?");








    
    try {
      dependenciaBean = (DependenciaBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[] { coDepen });
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return dependenciaBean;
  }

  
  public List<RemitenteBean> getListRemitente(String coDepen) {
    List<RemitenteBean> list = new ArrayList<RemitenteBean>();
    StringBuffer sql = new StringBuffer();
    sql.append("select D.DE_DEPENDENCIA descrip, D.CO_DEPENDENCIA cod_dep,D.DE_CORTA_DEPEN de_corta_depen from IDOSGD.RHTM_DEPENDENCIA D \nWHERE ? IN (CO_DEPENDENCIA,CO_DEPEN_PADRE) OR \nCO_DEPENDENCIA IN (SELECT co_dep_ref FROM IDOSGD.tdtx_referencia \nWHERE co_dep_emi = ?  AND ti_emi = 'D'   AND es_ref = '1') ORDER BY 1");


    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(RemitenteBean.class), new Object[] { coDepen, coDepen });
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return list;
  }

  
  public String verificarExpedienteDuplicado(String pnuAnnExp, String pcoDepExp, String pcoGru, String pnuCorrExp) {
    String vReturn = "1";
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject("select count(1) from IDOSGD.TDTC_EXPEDIENTE \nWHERE NU_ANN_EXP=? and CO_DEP_EXP=? and CO_GRU=? and NU_CORR_EXP=?", String.class, new Object[] { pnuAnnExp, pcoDepExp, pcoGru, pnuCorrExp });

    
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String verficarNumeroDuplicadoEmiDocExtRecep(String pnuAnn, String pcoDepEmi, String pnuCorEmi) {
    String vReturn = "1";
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject("SELECT count(1)\nFROM IDOSGD.tdtv_remitos \nWHERE NU_ANN = ?\nAND CO_DEP_EMI = ?\nAND CO_GRU = '3' \nAND NU_COR_EMI = ?", String.class, new Object[] { pnuAnn, pcoDepEmi, pnuCorEmi });




    
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public List<ProveedorBean> getLstProveedores(String prazonSocial) {
    StringBuilder sql = new StringBuilder();
    prazonSocial = Utilidades.fn_getCleanTextLenGreathree(Utilidades.fn_getCleanTextExpReg(prazonSocial));
    String sqlContains = "";
    Map<String, Object> objectParam = new HashMap<String, Object>();
    List<ProveedorBean> list = new ArrayList<ProveedorBean>();


    sql.append("SELECT A.* \nFROM (\nSELECT CPRO_RUC NU_RUC, CPRO_RAZSOC DESCRIPCION,CPRO_DOMICIL,CPRO_TELEFO,CPRO_EMAIL, \nLTRIM(NODEP) NODEP,LTRIM(NOPRV) NOPRV,LTRIM(NODIS) NODIS, LTRIM(CUBI_CODDEP) CUBI_CODDEP,LTRIM(CUBI_CODPRO) CUBI_CODPRO,LTRIM(CUBI_CODDIS) CUBI_CODDIS,  \nISNULL(CUBI_CODDEP,'') ID_DEPARTAMENTO, ISNULL(CUBI_CODPRO,'') ID_PROVINCIA, ISNULL(CUBI_CODDIS,'') ID_DISTRITO, ISNULL(CPRO_DOMICIL,'') DE_DIRECCION, ISNULL(CPRO_EMAIL,'') DE_CORREO,ISNULL(CPRO_TELEFO,'') TELEFONO \nFROM IDOSGD.LG_PRO_PROVEEDOR LEFT JOIN IDOSGD.IDTUBIAS ON UBDEP+UBPRV+UBDIS=CUBI_CODDEP+CUBI_CODPRO+CUBI_CODDIS\nwhere cpro_ruc='" + prazonSocial + "' or cpro_razsoc like '%" + prazonSocial + "%' \n  ) A order by 1 desc");


    
    try {
      if (prazonSocial != null && prazonSocial.trim().length() > 1) {
        list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(ProveedorBean.class));
      }
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<DestinatarioOtroOrigenBean> getLstOtrosOrigenes(String pdescripcion) {
    Map<String, Object> objectParam = new HashMap<String, Object>();
    String sqlContains = "";
    StringBuilder sql = new StringBuilder();
    pdescripcion = Utilidades.fn_getCleanTextLenGreathree(Utilidades.fn_getCleanTextExpReg(pdescripcion));
    
    List<DestinatarioOtroOrigenBean> list = new ArrayList<DestinatarioOtroOrigenBean>();


    sql.append("SELECT ISNULL(DE_APE_PAT_OTR,'')+' '+ ISNULL(DE_APE_MAT_OTR,'')+', '+ ISNULL(DE_NOM_OTR,'') + ' - ' + DE_RAZ_SOC_OTR DESCRIPCION,\n (SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_DOMINIOS('TIP_DOC_IDENT', CO_TIP_OTR_ORI )) TIPO_DOC_IDENTIDAD,\n NU_DOC_OTR_ORI NRO_DOC_IDENTIDAD, CO_OTR_ORI CO_OTRO_ORIGEN,\nISNULL(UB_DEP,'') ID_DEPARTAMENTO, ISNULL(UB_PRO,'') ID_PROVINCIA, ISNULL(UB_DIS,'') ID_DISTRITO, ISNULL(DE_DIR_OTRO_ORI,'') DE_DIRECCION, ISNULL(DE_EMAIL,'') DE_CORREO,ISNULL(DE_TELEFO,'') TELEFONO \n  ,UB_DEP,UB_PRO,UB_DIS\n  ,ltrim(UB.NODEP)+' '+ltrim(UB.NOPRV)+' '+ltrim(UB.NODIS) ubigeo,DE_EMAIL,a.DE_TELEFO,a.DE_DIR_OTRO_ORI\n ,ltrim(UB.NODEP) NODEP, ltrim(UB.NOPRV) NOPRV, ltrim(UB.NODIS) NODIS  \n  FROM IDOSGD.TDTR_OTRO_ORIGEN a\n LEFT JOIN IDOSGD.IDTUBIAS UB ON UB.UBDEP=a.UB_DEP AND UB.UBPRV=a.UB_PRO AND UB.UBDIS=a.UB_DIS\nwhere (DE_RAZ_SOC_OTR like '%" + pdescripcion + "%' \nOR (DE_APE_PAT_OTR+' '+ DE_APE_MAT_OTR+' '+DE_NOM_OTR) like '%" + pdescripcion + "%' \nOR (DE_NOM_OTR +' '+DE_APE_PAT_OTR+' '+ DE_APE_MAT_OTR) like '%" + pdescripcion + "%' \nOR NU_DOC_OTR_ORI like REPLACE('%" + pdescripcion + "%',' ','%') \n) and ISNULL(CO_TIP_OTR_ORI,'--') not in('09')\n order by 1 desc");


    
    try {
      if (pdescripcion != null && pdescripcion.trim().length() > 1) {
        list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DestinatarioOtroOrigenBean.class));
      }
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public CiudadanoBean getCiudadano(String pnuDoc) {
    StringBuffer sql = new StringBuffer();
    sql.append("select I.NULEM NU_DOCUMENTO,substring(RTRIM(I.DEAPP)+' '+RTRIM(I.DEAPM)+' '+RTRIM(I.DENOM),1,200) NOMBRE, \nISNULL(I.UBDEP,'') ID_DEPARTAMENTO, ISNULL(I.UBPRV,'') ID_PROVINCIA, ISNULL(I.UBDIS,'') ID_DISTRITO, ISNULL(I.DEDOMICIL,'') DE_DIRECCION, ISNULL(I.DEEMAIL,'') DE_CORREO,ISNULL(I.DETELEFO,'') TELEFONO \n,I.UBDEP, I.UBPRV,I.UBDIS,I.DEDOMICIL,I.DEEMAIL, I.DETELEFO ,  ltrim(UB.NODEP)+'/'+ltrim(UB.NOPRV)+'/'+ltrim(UB.NODIS) ubigeo \n from IDOSGD.IDTANIRS I LEFT JOIN IDOSGD.IDTUBIAS UB ON UB.UBDEP=I.UBDEP AND UB.UBPRV=I.UBPRV AND UB.UBDIS=I.UBDIS  \nwhere\nNULEM like ?+'%'\norder by 2");


    CiudadanoBean ciudadanoBean = new CiudadanoBean();
    try {
      ciudadanoBean = (CiudadanoBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(CiudadanoBean.class), new Object[] { pnuDoc });
    }
    catch (EmptyResultDataAccessException e) {
      ciudadanoBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return ciudadanoBean;
  }

  
  public ProveedorBean getProveedor(String pnuRuc) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT CPRO_RUC NU_RUC, CPRO_RAZSOC DESCRIPCION FROM IDOSGD.LG_PRO_PROVEEDOR\nwhere CPRO_RUC=?");

    
    ProveedorBean proveedorBean = null;
    try {
      proveedorBean = (ProveedorBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ProveedorBean.class), new Object[] { pnuRuc });
    }
    catch (EmptyResultDataAccessException e) {
      proveedorBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return proveedorBean;
  }
  
  public List<EmpleadoBean> getLsEmpleado(String coDep) {
    List<EmpleadoBean> list;
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT e.cemp_apepat+' '+e.cemp_apemat+' '+e.cemp_denom nombre, e.CEMP_CODEMP \nFROM IDOSGD.RHTM_PER_EMPLEADOS e\nwhere e.CEMP_EST_EMP = '1'\nORDER BY 1");

    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[0]);
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  public List<CargoFuncionBean> getLsEmpleo() {
    List<CargoFuncionBean> list;
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT CCAR_DESCAR DESCRIPCION, CCAR_CO_CARGO CODIGO\nFROM IDOSGD.RHTM_CARGOS\nWHERE CCAR_CO_CARGO <> '000'\nORDER BY 1");

    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(CargoFuncionBean.class), new Object[0]);
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  public List<DependenciaBean> getLsDepencia() {
    List<DependenciaBean> list;
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT DE_DEPENDENCIA,\n       CO_DEPENDENCIA,\n       DE_CORTA_DEPEN\n  FROM IDOSGD.RHTM_DEPENDENCIA\n WHERE co_nivel <> '6'\n   AND IN_BAJA = '0'\n   order by 1");

 
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DependenciaBean.class), new Object[0]);
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String[] getNroCorrLocalDependencia() {
    String vReturn = "ERROR|";
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT ISNULL( MAX( a.NU_CORRELATIVO) , 0)+1\nFROM IDOSGD.SITM_LOCAL_DEPENDENCIA a");

    
    try {
      String nroCorr = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class);
      vReturn = "OK|" + nroCorr;
    } catch (EmptyResultDataAccessException e) {
      vReturn = "OK|1";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn.split("\\|");
  }
  
  public List<EmpleadoBean> getLsEmpleadoIntitu(String deEmp) {
    List<EmpleadoBean> list;
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT e.cemp_apepat+' '+e.cemp_apemat+' '+e.cemp_denom nombre, e.CEMP_CODEMP \nFROM IDOSGD.RHTM_PER_EMPLEADOS e\nwhere e.CEMP_EST_EMP = '1' \nand  e.cemp_apepat+' '+e.cemp_apemat+' '+e.cemp_denom LIKE '%'+?+'%'\nORDER BY 1");


    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[] { deEmp });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String[] getCodigoMotivoDocRec(String nuAnn, String nuEmi, String nuDes) {
    String vReturn = "ERROR|";
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT CO_MOT FROM IDOSGD.TDTV_DESTINOS\nWHERE NU_ANN=?\nAND NU_EMI=?\nAND NU_DES=?\nAND ES_ELI='0'");




    
    try {
      String coMot = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { nuAnn, nuEmi, nuDes });
      vReturn = "OK|" + coMot;
    } catch (EmptyResultDataAccessException e) {
      vReturn = "ERROR|";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn.split("\\|");
  }

  
  public EmpleadoBean getEmpJefeDep(String coDep) {
    StringBuffer sql = new StringBuffer();
    sql.append("select CO_EMPLEADO CEMP_CODEMP,IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(CO_EMPLEADO) NOMBRE \nfrom IDOSGD.rhtm_dependencia\nwhere CO_DEPENDENCIA = ?\nAND IN_BAJA='0'");



    
    EmpleadoBean empleadoBean = new EmpleadoBean();
    try {
      empleadoBean = (EmpleadoBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[] { coDep });
    }
    catch (EmptyResultDataAccessException e) {
      empleadoBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return empleadoBean;
  }
  
  public List<EmpleadoBean> getLsEmpDepDesEmp(String deEmp, String coDep) {
    List<EmpleadoBean> list;
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT e.cemp_apepat+' '+e.cemp_apemat+' '+e.cemp_denom nombre, e.CEMP_CODEMP \nFROM IDOSGD.RHTM_PER_EMPLEADOS e, \n( \nSELECT CEMP_CODEMP \nFROM IDOSGD.RHTM_PER_EMPLEADOS\nwhere CEMP_EST_EMP = '1' \n  and (CO_DEPENDENCIA=? or CEMP_CO_DEPEND=?) \nunion \nselect co_emp from IDOSGD.tdtx_dependencia_empleado  where co_dep=? and es_emp='0' \nunion \nselect co_empleado from IDOSGD.rhtm_dependencia where co_dependencia=? \n) a \nwhere e.cemp_codemp = a.cemp_codemp\nand  e.cemp_apepat+' '+e.cemp_apemat+' '+e.cemp_denom LIKE '%'+?+'%'\norder by 1");
   
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(EmpleadoBean.class), new Object[] { coDep, coDep, coDep, coDep, deEmp });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  public List<EmpleadoVoBoBean> getLsPersonalVoBo(String nuAnn, String nuEmi) {
    List<EmpleadoVoBoBean> list;
    StringBuffer sql = new StringBuffer();

    
    sql.append("SELECT VB.CO_EMP_VB CO_EMPLEADO,(SELECT CEMP_APEPAT + ' ' + CEMP_APEMAT + ' ' + CEMP_DENOM FROM IDOSGD.RHTM_PER_EMPLEADOS WHERE CEMP_CODEMP = VB.CO_EMP_VB) NOMBRE,\nVB.CO_DEP CO_DEPENDENCIA, \n(SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_SIGLA_CORTA(VB.CO_DEP)) DE_DEPENDENCIA,\n(CASE \nWHEN VB.IN_VB ='B' THEN '0' \nELSE VB.IN_VB \nEND \n) IN_VOBO \nFROM IDOSGD.TDTV_PERSONAL_VB VB \nWHERE VB.NU_ANN=?\nAND VB.NU_EMI=?\nORDER BY NOMBRE");

    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(EmpleadoVoBoBean.class), new Object[] { nuAnn, nuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String getInFirmaDoc(String pcoDep, String pcoTipoDoc) {
    StringBuffer sql = new StringBuffer();
    String result = null;
    sql.append(" SELECT \n\t(\n\t\tCASE WHEN es_obl_firma='0' THEN 'N'\n\t\tELSE 'F'\n\t\tEND\n\t)\nFROM IDOSGD.sitm_doc_dependencia\nWHERE co_dep     = ?\nAND co_tip_doc = ?");


    try {
      result = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { pcoDep, pcoTipoDoc });
    } catch (EmptyResultDataAccessException e) {
      result = "F";
    } catch (Exception e) {
      result = "F";
      e.printStackTrace();
    } 
    
    return result;
  }
  
  public List<CiudadanoBean> getLstCiudadanos(String sDescCiudadano) {
    List<CiudadanoBean> list = new ArrayList<CiudadanoBean>();
    StringBuffer sql = new StringBuffer();
//    sql.append(" select I.NULEM NU_DOCUMENTO,substr(RTRIM(I.DEAPP)||' '||RTRIM(I.DEAPM)||' '||RTRIM(I.DENOM),1,200) NOMBRE,\nNVL(UBDEP,'') ID_DEPARTAMENTO, NVL(UBPRV,'') ID_PROVINCIA, NVL(UBDIS,'') ID_DISTRITO, NVL(DEDOMICIL,'') DE_DIRECCION, NVL(DEEMAIL,'') DE_CORREO,NVL(DETELEFO,'') TELEFONO \nfrom IDTANIRS I \nwhere\n(substr(RTRIM(I.DEAPP)||' '||RTRIM(I.DEAPM)||' '||RTRIM(I.DENOM),1,200)) like '%'||?||'%'\norder by 2");


    sql.append(" select I.NULEM NU_DOCUMENTO,SUBSTRING(RTRIM(I.DEAPP)+' '+RTRIM(I.DEAPM)+' '+RTRIM(I.DENOM),1,200)    NOMBRE, ");
    sql.append(" ISNULL(UBDEP,'') ID_DEPARTAMENTO, ISNULL(UBPRV,'') ID_PROVINCIA, ISNULL(UBDIS,'') ");
    sql.append(" ID_DISTRITO, ISNULL(DEDOMICIL,'') DE_DIRECCION, ISNULL(DEEMAIL,'') DE_CORREO, ");
    sql.append(" ISNULL(DETELEFO,'') TELEFONO  ");
    sql.append(" from IDOSGD.IDTANIRS I    where ");
    sql.append(" (SUBSTRING(RTRIM(I.DEAPP)+' '+RTRIM(I.DEAPM)+' '+RTRIM(I.DENOM),1,200)) like '%'+?+'%' ");
    sql.append(" order by 2 ");


    CiudadanoBean ciudadanoBean = new CiudadanoBean();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(CiudadanoBean.class), new Object[] { sDescCiudadano
            .toUpperCase() });
    } catch (EmptyResultDataAccessException e) {
      ciudadanoBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return list;
  }
  
  public List<SiElementoBean> getLsDepenciaMensjeria() {
    List<SiElementoBean> list;
    StringBuffer sql = new StringBuffer();
    
    sql.append(" SELECT CELE_DESELE,\n CELE_DESCOR \n FROM IDOSGD.SI_ELEMENTO\n WHERE SI_ELEMENTO.CTAB_CODTAB='MSJ_DEPENDENCIA'\n order by SI_ELEMENTO.NELE_NUMSEC");




    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(SiElementoBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }


  
  public String obtenerValorParametro(String nombreParametro) {
    StringBuffer sql = new StringBuffer();
    String result = null;
    sql.append("SELECT UPPER(DE_PAR) DE_PAR FROM IDOSGD.TDTR_PARAMETROS WHERE CO_PAR=?");
    try {
      result = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { nombreParametro });
    } catch (EmptyResultDataAccessException e) {
      result = "";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return result;
  }


  
  public String getCargoEmpleado(String coDep, String coEmp) {
    StringBuilder sql = new StringBuilder();
    String result = null;
    sql.append("SELECT RETORNO  FROM  IDOSGD.PK_SGD_DESCRIPCION_DE_CARGO_DOCUMENTO(?, ?)  ");
    try {
      result = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { coDep, coEmp });
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return result;
  }




  
  public DatosInterBean DatosInter(String nuAnn, String nuEmi) {
    DatosInterBean datosInter = new DatosInterBean();
    
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT (CASE WHEN LEN (A.NU_RUC_DES)>0 THEN A.NU_RUC_DES ELSE '0' END) AS NU_RUC_DES,(CASE WHEN LEN(A.DE_NOM_DES)>0 THEN a.DE_NOM_DES ELSE \n CASE WHEN LEN(A.REMI_NU_DNI_EMI)>0 THEN IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL(a.REMI_NU_DNI_EMI) ELSE \n CASE WHEN LEN(A.REMI_CO_OTR_ORI_EMI)>0 THEN IDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN(a.remi_co_otr_ori_emi) ELSE '' END END END) DE_NOM_DES,\n(CASE WHEN LEN(A.DE_DEP_DES)>0 THEN DE_DEP_DES ELSE '' END) DE_DEP_DES,\n(CASE WHEN LEN(A.DE_CAR_DES)>0 THEN DE_CAR_DES ELSE A.DE_CARGO END) DE_CAR_DES FROM IDOSGD.TDTV_DESTINOS A INNER JOIN IDOSGD.TDTV_REMITOS B ON A.NU_ANN=B.NU_ANN AND A.NU_EMI=B.NU_EMI AND B.CO_TIP_DOC_ADM IN (SELECT CDOC_TIPDOC FROM IDOSGD.IOTDTX_TIPO_DOCUMENTO) \nWHERE A.NU_ANN=?\nAND A.NU_EMI=?\nAND A.NU_DES=1");


    try {
      datosInter = (DatosInterBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DatosInterBean.class), new Object[] { nuAnn, nuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      
      datosInter = null;
    } catch (Exception e) {
      e.printStackTrace();
      datosInter = null;
    } 
    
    return datosInter;
  }


@Override
public List<TablaMaestraBean> getLsContenidoTabla(String vTabla) {
	// TODO Auto-generated method stub
	return null;
}


@Override
public List<CargoFuncionBean> getCargoBusqList(CargoFuncionBean cargo) {
	// TODO Auto-generated method stub
	return null;
}


@Override
public String insCargo(CargoFuncionBean cargo, String codusuario) {
	// TODO Auto-generated method stub
	return null;
}
}
