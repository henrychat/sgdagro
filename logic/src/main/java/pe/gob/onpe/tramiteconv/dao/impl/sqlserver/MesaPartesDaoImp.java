package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.BuscarDocumentoExtRecepBean;
import pe.gob.onpe.tramitedoc.bean.DestinatarioDocumentoEmiBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoExtRecepBean;
import pe.gob.onpe.tramitedoc.bean.ExpedienteDocExtRecepBean;
import pe.gob.onpe.tramitedoc.bean.MotivoBean;
import pe.gob.onpe.tramitedoc.bean.ProcesoExpBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaDocExtRecepBean;
import pe.gob.onpe.tramitedoc.bean.RemitenteBean;
import pe.gob.onpe.tramitedoc.bean.RemitenteDocExtRecepBean;
import pe.gob.onpe.tramitedoc.bean.RequisitoBean;
import pe.gob.onpe.tramitedoc.dao.MesaPartesDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;
import pe.gob.onpe.tramitedoc.web.util.BusquedaTextual;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;


@Repository("mesaPartesDao")
public class MesaPartesDaoImp
  extends SimpleJdbcDaoBase
  implements MesaPartesDao
{
  public List<DocumentoExtRecepBean> getDocumentosExtRecep(BuscarDocumentoExtRecepBean buscarDocumentoExtRecepBean) {
    boolean bBusqFiltro = false;
    boolean bBusqDep = false;
    String sqlContains = "";
    StringBuffer sql = new StringBuffer();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    sql.append("SELECT TOP 300  X.*, "); // por lo mientras
    sql.append("        [IDOSGD].[PK_SGD_DESCRIPCION_TI_DESTINO](X.TI_EMI) DE_TI_EMI, ");

    sql.append("    [IDOSGD].PK_SGD_DESCRIPCION_TI_EMI_EMP(X.NU_ANN, X.NU_EMI) DE_ORI_EMI_MP,");
    sql.append("        [IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](X.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM, ");
    sql.append("        CASE X.TI_EMI ");
    sql.append("            WHEN '01' THEN X.NU_DOC_EMI + '-' + X.NU_ANN + '/' + X.DE_DOC_SIG ");
    sql.append("            WHEN '05' THEN X.NU_DOC_EMI + '-' + X.NU_ANN + '/' + X.DE_DOC_SIG ");
    sql.append("            ELSE X.DE_DOC_SIG ");
    sql.append("        END NU_DOC, ");
    sql.append("        CASE X.NU_CANDES ");
    sql.append("            WHEN 1 THEN (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP](X.NU_ANN, X.NU_EMI)) ");
    sql.append("            ELSE (SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_TI_DES_EMP_V](X.NU_ANN, X.NU_EMI)) ");
    sql.append("        END DE_EMP_DES, ");
    sql.append("        [IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS_MP](X.ES_DOC_EMI, 'TDTV_REMITOS') DE_ES_DOC_EMI_MP, ");
    sql.append("        [IDOSGD].[PK_SGD_DESCRIPCION_DE_LOCAL](X.CO_LOC_EMI) DE_LOC_EMI, ");
    sql.append("        [IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](X.CO_DEP) DE_DEPENDENCIA ");
    sql.append("FROM ( ");
    sql.append("SELECT  A.NU_ANN, ");
    sql.append("        A.NU_EMI, ");
    sql.append("        B.NU_EXPEDIENTE, ");
    sql.append("        CONVERT(VARCHAR(10), A.FE_EMI, 103) FE_EMI_CORTA, ");
    sql.append("        A.DE_ASU, ");
    sql.append("        A.CO_DEP_EMI, ");
    sql.append("        A.CO_EMP_EMI, ");
    sql.append("        A.CO_OTR_ORI_EMI, ");
    sql.append("        A.CO_TIP_DOC_ADM, ");
    sql.append("        A.NU_DOC_EMI, ");
    sql.append("        A.DE_DOC_SIG, ");
    sql.append("        B.IN_EXISTE_DOC EXISTE_DOC, ");
    sql.append("        A.TI_EMI, ");
    sql.append("        A.NU_RUC_EMI, ");
    sql.append("        A.NU_DNI_EMI, ");
    sql.append("        A.NU_CANDES, ");
    sql.append("        A.ES_DOC_EMI, ");
    sql.append("        A.CO_LOC_EMI, ");
    sql.append("        A.CO_DEP, ");
    sql.append("        A.NU_COR_EMI, ");
    sql.append("        ROW_NUMBER() OVER (ORDER BY A.NU_COR_EMI) AS ROWNUM ");
    sql.append("FROM IDOSGD.TDTV_REMITOS A  ");
    sql.append("INNER JOIN   IDOSGD.TDTX_REMITOS_RESUMEN B ON B.NU_ANN=A.NU_ANN AND B.NU_EMI=A.NU_EMI ");
    sql.append("WHERE 1=1 ");

    
    String pNuAnn = buscarDocumentoExtRecepBean.getCoAnnio();
    String pEsFiltroFecha = buscarDocumentoExtRecepBean.getEsFiltroFecha();
    if (!pEsFiltroFecha.equals("1") || !pNuAnn.equals("0")) {
      sql.append(" AND A.NU_ANN = :pNuAnn");
      
      objectParam.put("pNuAnn", pNuAnn);
    } 
    sql.append(" AND A.CO_GRU = :pCoGru");
    objectParam.put("pCoGru", buscarDocumentoExtRecepBean.getCoGrupo());
    
    sql.append(" AND A.TI_EMI<>'01'");
    sql.append(" AND A.ES_ELI='0'");
    sql.append(" AND A.CO_DEP_EMI = :pCoDepEmi");
    objectParam.put("pCoDepEmi", buscarDocumentoExtRecepBean.getCoDepEmi());
    
    String pTipoBusqueda = buscarDocumentoExtRecepBean.getTipoBusqueda();
    if (pTipoBusqueda.equals("1") && buscarDocumentoExtRecepBean.isEsIncluyeFiltro()) {
      bBusqFiltro = true;
    }
    
    String auxTipoAcceso = buscarDocumentoExtRecepBean.getTiAcceso();
    String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1")  || auxTipoAcceso.equals("2"))) ? auxTipoAcceso : "1";
    if (tiAcceso.equals("1")) {
    	
      sql.append(" AND A.CO_EMP_RES = :pcoEmpRes ");
      objectParam.put("pcoEmpRes", buscarDocumentoExtRecepBean.getCoEmpleado());
    } else
    if (tiAcceso.equals("2")) {
    	sql.append(" AND A.CO_DEP = :pcodepOfi");
        objectParam.put("pcodepOfi", buscarDocumentoExtRecepBean.getCoDependencia());
        
    	}
    else if (buscarDocumentoExtRecepBean.getInMesaPartes().equals("0")) {
      bBusqDep = true;
      sql.append(" AND A.CO_DEP = :pCoDep");
      objectParam.put("pCoDep", buscarDocumentoExtRecepBean.getCoDependencia());
    } 

    
    if (buscarDocumentoExtRecepBean.getCoTipoDoc() != null && buscarDocumentoExtRecepBean.getCoTipoDoc().trim().length() > 0) {
      sql.append(" AND A.CO_TIP_DOC_ADM = :pCoDocEmi ");
      objectParam.put("pCoDocEmi", buscarDocumentoExtRecepBean.getCoTipoDoc());
    } 
    if (buscarDocumentoExtRecepBean.getCoEstadoDoc() != null && buscarDocumentoExtRecepBean.getCoEstadoDoc().trim().length() > 0) {
      sql.append(" AND A.ES_DOC_EMI = :pCoEsDocEmi ");
      objectParam.put("pCoEsDocEmi", buscarDocumentoExtRecepBean.getCoEstadoDoc());
    } 
    if (buscarDocumentoExtRecepBean.getCoTipoEmisor() != null && buscarDocumentoExtRecepBean.getCoTipoEmisor().trim().length() > 0) {
      sql.append(" AND A.TI_EMI = :pCoTipoEmisor ");
      objectParam.put("pCoTipoEmisor", buscarDocumentoExtRecepBean.getCoTipoEmisor());
    } 



    
    if (buscarDocumentoExtRecepBean.getCoDepOriRec() != null && buscarDocumentoExtRecepBean.getCoDepOriRec().trim().length() > 0 && !bBusqDep) {
      sql.append(" AND A.CO_DEP = :pCoDep");
      objectParam.put("pCoDep", buscarDocumentoExtRecepBean.getCoDepOriRec());
    } 
    if (buscarDocumentoExtRecepBean.getCoProceso() != null && buscarDocumentoExtRecepBean.getCoProceso().trim().length() > 0) {
      if (buscarDocumentoExtRecepBean.getCoProceso().equals("CON_TUPA")) {
        sql.append(" AND B.CO_PROCESO_EXP not in ('0000') ");
      } else {
        sql.append(" AND B.CO_PROCESO_EXP = :pcoProceso ");
        objectParam.put("pcoProceso", buscarDocumentoExtRecepBean.getCoProceso());
      } 
    }
    if (buscarDocumentoExtRecepBean.getEsFiltroFecha() != null && (pEsFiltroFecha.equals("1") || pEsFiltroFecha.equals("3"))) {
      String vFeEmiIni = buscarDocumentoExtRecepBean.getFeEmiIni();
      String vFeEmiFin = buscarDocumentoExtRecepBean.getFeEmiFin();
      if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
        .trim().length() > 0) {
        sql.append(" AND A.FE_EMI BETWEEN CONVERT(DATETIME, :pFeEmiIni, 103) AND CONVERT(DATETIME, :pFeEmiFin, 103) + 0.99999 ");
        objectParam.put("pFeEmiIni", vFeEmiIni);
        objectParam.put("pFeEmiFin", vFeEmiFin);
      } 
    } 



    
    if (buscarDocumentoExtRecepBean.getBusNumDoc() != null && buscarDocumentoExtRecepBean.getBusNumDoc().trim().length() > 1) {
      sql.append(" AND B.NU_DOC LIKE '%'+:pnuDocEmi+'%' ");
      objectParam.put("pnuDocEmi", buscarDocumentoExtRecepBean.getBusNumDoc());
    } 
    if (buscarDocumentoExtRecepBean.getBusNumExpediente() != null && buscarDocumentoExtRecepBean.getBusNumExpediente().trim().length() > 1) {
      sql.append(" AND B.NU_EXPEDIENTE LIKE '%'+:pnuExpediente+'%' ");
      objectParam.put("pnuExpediente", buscarDocumentoExtRecepBean.getBusNumExpediente());
    } 
    if (buscarDocumentoExtRecepBean.getBusAsunto() != null && buscarDocumentoExtRecepBean.getBusAsunto().trim().length() > 1) {

      
      sql.append(" AND UPPER(A.DE_ASU) LIKE '%'+:pDeAsunto+'%' ");
      objectParam.put("pDeAsunto", buscarDocumentoExtRecepBean.getBusAsunto());
    } 
    if (buscarDocumentoExtRecepBean.getCoTipoExp() != null && !buscarDocumentoExtRecepBean.getCoTipoExp().equals("")) {
      //sql.append(" AND C.CCOD_TIPO_EXP = :codTipoExp");
      //objectParam.put("codTipoExp", buscarDocumentoExtRecepBean.getCoTipoExp());
    	sql.append(" and exists( select  CCOD_TIPO_EXP from IDOSGD.tdtc_expediente where NU_SEC_EXP=A.NU_SEC_EXP");
    	sql.append(" and NU_ANN_EXP=A.NU_ANN_EXP and CCOD_TIPO_EXP=:codTipoExp)");
    	objectParam.put("codTipoExp", buscarDocumentoExtRecepBean.getCoTipoExp());
    	 
    			 
    } 
    
    if (buscarDocumentoExtRecepBean.getCoOriDoc() != null && !buscarDocumentoExtRecepBean.getCoOriDoc().equals("")) {
      sql.append(" AND A.CCOD_ORIGING = :coOriDoc");
      objectParam.put("coOriDoc", buscarDocumentoExtRecepBean.getCoOriDoc());
    } 

    
    if (buscarDocumentoExtRecepBean.getBusResultado().equals("1"))
    {
      if (buscarDocumentoExtRecepBean.getCoTipoPersona().equals("03")) {
        sql.append(" AND A.NU_DNI_EMI = :pNumDni");
        objectParam.put("pNumDni", buscarDocumentoExtRecepBean.getBusNumDni());
      }
      else if (buscarDocumentoExtRecepBean.getCoTipoPersona().equals("02")) {
        sql.append(" AND A.NU_RUC_EMI = :pNumRuc");
        objectParam.put("pNumRuc", buscarDocumentoExtRecepBean.getBusNumRuc());
      }
      else if (buscarDocumentoExtRecepBean.getCoTipoPersona().equals("04")) {
        sql.append(" AND A.CO_OTR_ORI_EMI = :pCoOtr");
        objectParam.put("pCoOtr", buscarDocumentoExtRecepBean.getBusCoOtros());
      } 
    }

    
    sql.append(")  AS X ");
    sql.append("ORDER BY X.NU_COR_EMI DESC ");
    
    List<DocumentoExtRecepBean> list = new ArrayList<DocumentoExtRecepBean>();

    
    try {
      if (sqlContains.length() > 0) {
        String cadena = (String)this.jdbcTemplate.queryForObject(sqlContains, String.class);
        objectParam.put("pBusquedaTextual", cadena);
      } 
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoExtRecepBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String insExpedienteBean(ExpedienteDocExtRecepBean expedienteBean) {
    String vReturn = "NO_OK";
    String clave = Utilidades.generateRandomLetter(4);
    StringBuffer sqlQry1 = new StringBuffer();
    sqlQry1.append("select RIGHT(REPLICATE('0', 10) + CAST((ISNULL(MAX(nu_sec_exp), 0) + 1) AS VARCHAR), 10) ");
    sqlQry1.append("from IDOSGD.tdtc_expediente ");
    sqlQry1.append("where nu_ann_exp = ? ");
    
    StringBuffer sqlIns = new StringBuffer();
    sqlIns.append("INSERT INTO IDOSGD.TDTC_EXPEDIENTE(\nnu_ann_exp,\nnu_sec_exp,\nfe_exp,\nfe_vence,\nco_proceso,\nde_detalle,\nco_dep_exp,\nco_gru,\nnu_corr_exp,\nnu_expediente,\nus_crea_audi,\nfe_crea_audi,\nus_modi_audi,\nfe_modi_audi,\nes_estado,ccod_tipo_exp,CCLAVE)\nVALUES(?,?,(SELECT CONVERT(DATETIME, ?, 103)),(SELECT CONVERT(DATETIME, ?, 103)),?,?,?,'3',?,?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,'0',?, ?)");

   
    try {
      String feVence = expedienteBean.getFeVence();
      if (feVence == null || feVence.trim().length() <= 0) {
        feVence = null;
      }
      String snuSecExp = (String)this.jdbcTemplate.queryForObject(sqlQry1.toString(), String.class, new Object[] { expedienteBean.getNuAnnExp() });
      expedienteBean.setNuSecExp(snuSecExp);
      
      this.jdbcTemplate.update(sqlIns.toString(), new Object[] { expedienteBean.getNuAnnExp(), snuSecExp, expedienteBean.getFeExp(), feVence, expedienteBean
            .getCoProceso(), expedienteBean.getDeDetalle(), expedienteBean.getCoDepEmi(), expedienteBean
            .getNuCorrExp(), expedienteBean.getNuExpediente(), expedienteBean.getUsCreaAudi(), expedienteBean
            .getUsCreaAudi(), expedienteBean.getCoTipoExp(), clave });
      vReturn = "OK";
    } catch (DuplicateKeyException con) {
      vReturn = "Numero de Expediente Duplicado.";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String updExpedienteBean(ExpedienteDocExtRecepBean expedienteBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    
    sqlUpd.append("update IDOSGD.TDTC_EXPEDIENTE \nset US_MODI_AUDI=?\n,FE_VENCE=(SELECT CONVERT(DATETIME, ?, 103))\n,CO_PROCESO=?\n,FE_MODI_AUDI=CURRENT_TIMESTAMP\nwhere\nNU_ANN_EXP=? and\nNU_SEC_EXP=?");

   
    try {
      String feVence = expedienteBean.getFeVence();
      if (feVence == null || feVence.trim().length() <= 0) {
        feVence = null;
      }
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { expedienteBean.getUsCreaAudi(), feVence, expedienteBean
            .getCoProceso(), expedienteBean.getNuAnnExp(), expedienteBean.getNuSecExp() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    
    return vReturn;
  }

  
  public String updDocumentoExtBean(String nuAnn, String nuEmi, DocumentoExtRecepBean documentoExtRecepBean, ExpedienteDocExtRecepBean expedienteBean, RemitenteDocExtRecepBean remitenteDocExtRecepBean, String pcoUserMod) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("update IDOSGD.tdtv_remitos \nset CO_USE_MOD=?,");
   // se agrega otros
    String coOtros=null;
    if(remitenteDocExtRecepBean!=null) {
    	 coOtros = remitenteDocExtRecepBean.getCoOtros();
        if (coOtros == null || coOtros.trim().length() <= 0) {
          coOtros = null;
        }	
    }
    
    
    if (documentoExtRecepBean != null) {
      documentoExtRecepBean.setNuAnn(nuAnn);
      sqlUpd.append("DE_ASU=?\n,COBS_DOCUMENTO=?\n,DE_DOC_SIG=?\n,CO_TIP_DOC_ADM=?\n,NU_FOLIOS=?\n,NU_DIA_ATE=?,\n");

      sqlUpd.append("CCOD_ORIGING=?,\n");
      sqlUpd.append("CNUM_DNIMSG=?,\n");
      sqlUpd.append("NRO_SOBREAUT=?,\n");
      sqlUpd.append("AN_SOBREAUT=?,\n");
      sqlUpd.append("CDOC_DESTRAM=?,\n");
      
      if (documentoExtRecepBean.getNuCorDoc() == null || documentoExtRecepBean.getNuCorDoc().trim().equals("")) {
        String vnuCorDoc = getNroCorrelativoDocumento(nuAnn, documentoExtRecepBean.getCoDepEmi(), documentoExtRecepBean.getTiEmi());
        documentoExtRecepBean.setNuCorDoc(vnuCorDoc);
        sqlUpd.append("NU_COR_DOC=").append(vnuCorDoc).append(",\n");
      } 

      sqlUpd.append("CIND_SENSIBLE='").append((documentoExtRecepBean.getSensible() == null || documentoExtRecepBean.getSensible().equals("null")) ? "0" : documentoExtRecepBean.getSensible()).append("',\n");
    } 
  
    if (remitenteDocExtRecepBean != null) {
      sqlUpd.append("TI_EMI=?,\n");
      sqlUpd.append("NU_RUC_EMI=?,\n");
      sqlUpd.append("NU_DNI_EMI=?,\n");
      sqlUpd.append("CO_OTR_ORI_EMI=?,\n");
      sqlUpd.append("CDIR_REMITE=?,\n");
      sqlUpd.append("CEXP_CORREOE=?,\n");
      sqlUpd.append("CTELEFONO=?,\n");
      sqlUpd.append("REMI_TI_EMI=?,\n");
      sqlUpd.append("REMI_NU_DNI_EMI=?,\n");
      sqlUpd.append("REMI_CARGO=?,\n");
      sqlUpd.append("CONG_CO_OTR_ORI=?,\n");
      sqlUpd.append("IND_TIPOCONG=?,\n");
      sqlUpd.append("IND_TIPOCONGINV=?,\n");
      sqlUpd.append("REMI_CO_OTR_ORI_EMI=?,\n");
      sqlUpd.append("CCOD_DPTO=?,\n");
      sqlUpd.append("CCOD_PROV=?,\n");
      sqlUpd.append("CCOD_DIST=?,\n");
      
      sqlUpd.append(" AUT_CORREOE='").append((remitenteDocExtRecepBean.getNotificado() == null || remitenteDocExtRecepBean.getNotificado().equals("null")) ? "0" : remitenteDocExtRecepBean.getNotificado()).append("',\n");
      sqlUpd.append(" IND_REITCONGINV='").append((remitenteDocExtRecepBean.getReiterativo() == null || remitenteDocExtRecepBean.getReiterativo().equals("null")) ? "0" : remitenteDocExtRecepBean.getReiterativo()).append("',\n");
    } 
    sqlUpd.append("FE_USE_MOD=CURRENT_TIMESTAMP where\nNU_ANN=? and\nNU_EMI=?");



    
    try {
      if (documentoExtRecepBean != null && remitenteDocExtRecepBean == null) {
        this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { pcoUserMod, documentoExtRecepBean
              
              .getDeAsu(), documentoExtRecepBean
              .getDeObservacion(), documentoExtRecepBean
              .getDeDocSig(), documentoExtRecepBean
              .getCoTipDocAdm(), documentoExtRecepBean
              .getNuFolios(), documentoExtRecepBean
              .getNuDiaAte(), documentoExtRecepBean
              .getCoOriDoc(), documentoExtRecepBean
              .getNroDniTramitante(), documentoExtRecepBean
              .getnSobre(), documentoExtRecepBean
              .getAnioSobre(), documentoExtRecepBean
              .getCoTraDest(), nuAnn, nuEmi });
      
      }
      else if (documentoExtRecepBean == null && remitenteDocExtRecepBean != null) {
        this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { pcoUserMod, remitenteDocExtRecepBean.getTiEmi(), 
        		remitenteDocExtRecepBean.getNuRuc(), remitenteDocExtRecepBean.getNuDni(), coOtros, 
        		remitenteDocExtRecepBean.getDeDireccion(),remitenteDocExtRecepBean.getDeCorreo(), remitenteDocExtRecepBean.getTelefono(), 
        		remitenteDocExtRecepBean.getEmiResp(), remitenteDocExtRecepBean.getNuDniRes(), remitenteDocExtRecepBean.getDeCargo(), 
        		remitenteDocExtRecepBean.getCoComision(), remitenteDocExtRecepBean.getCoTipoCongresista(), remitenteDocExtRecepBean.getCoTipoInv(), 
        		remitenteDocExtRecepBean.getCoOtrosRes(), remitenteDocExtRecepBean.getIdDepartamento(), remitenteDocExtRecepBean.getIdProvincia(), 
        		remitenteDocExtRecepBean.getIdDistrito(), nuAnn, nuEmi });
      
      }
      else if (documentoExtRecepBean != null && remitenteDocExtRecepBean != null) {
        this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { pcoUserMod,
        		documentoExtRecepBean.getDeAsu(), documentoExtRecepBean.getDeObservacion(), documentoExtRecepBean.getDeDocSig(), 
        		documentoExtRecepBean.getCoTipDocAdm(), documentoExtRecepBean.getNuFolios(), documentoExtRecepBean.getNuDiaAte(),
        		documentoExtRecepBean.getCoOriDoc(), documentoExtRecepBean.getNroDniTramitante(), documentoExtRecepBean.getnSobre(), 
        		documentoExtRecepBean.getAnioSobre(), documentoExtRecepBean.getCoTraDest(), remitenteDocExtRecepBean.getTiEmi(), 
        		remitenteDocExtRecepBean.getNuRuc(), remitenteDocExtRecepBean.getNuDni(), coOtros, 
        		remitenteDocExtRecepBean.getDeDireccion(), remitenteDocExtRecepBean.getDeCorreo(), remitenteDocExtRecepBean.getTelefono(), 
        		remitenteDocExtRecepBean.getEmiResp(), remitenteDocExtRecepBean.getNuDniRes(), remitenteDocExtRecepBean.getDeCargo(), 
        		remitenteDocExtRecepBean.getCoComision(), remitenteDocExtRecepBean.getCoTipoCongresista(), remitenteDocExtRecepBean.getCoTipoInv(), 
        		remitenteDocExtRecepBean.getCoOtrosRes(), remitenteDocExtRecepBean.getIdDepartamento(), remitenteDocExtRecepBean.getIdProvincia(), 
        		remitenteDocExtRecepBean.getIdDistrito(), nuAnn, nuEmi });
      }
      else {
        
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

  
  public String insDocumentoExtBean(DocumentoExtRecepBean documentoExtRecepBean, ExpedienteDocExtRecepBean expedienteBean, RemitenteDocExtRecepBean remitenteDocExtRecepBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    
    sqlUpd.append("INSERT INTO IDOSGD.tdtv_remitos(\nNU_ANN,\nNU_EMI,\nNU_COR_EMI,\nCO_LOC_EMI,\nCO_DEP_EMI,\nCO_EMP_EMI,\nCO_EMP_RES, \nTI_EMI,\nNU_DNI_EMI,\nNU_RUC_EMI,\nCO_OTR_ORI_EMI,\nFE_EMI,\nCO_GRU,\nDE_ASU, \nES_DOC_EMI, \nNU_DIA_ATE, \nCO_TIP_DOC_ADM, \nNU_COR_DOC, \nDE_DOC_SIG, \nNU_ANN_EXP, \nNU_SEC_EXP, \nNU_DET_EXP, \nNU_FOLIOS, \nES_ELI,\nCO_USE_CRE,\nFE_USE_CRE,\nCO_USE_MOD,\nCO_DEP,\nFE_USE_MOD,\nCDIR_REMITE,\nAUT_CORREOE,\nCEXP_CORREOE,\nCTELEFONO,\nCCOD_DPTO,\nCCOD_PROV,\nCCOD_DIST,\nREMI_TI_EMI,\nREMI_NU_DNI_EMI,\nREMI_CO_OTR_ORI_EMI,\nREMI_CARGO,\nCCOD_ORIGING,\nCNUM_DNIMSG,\nCOBS_DOCUMENTO,\nCDOC_DESTRAM,\nCONG_CO_OTR_ORI,\nIND_TIPOCONG,\nIND_TIPOCONGINV,\nIND_REITCONGINV,\nNRO_SOBREAUT,\nAN_SOBREAUT,\nCIND_SENSIBLE)\nVALUES(?,?,?,?,?,?,?,?,?,?,?,(SELECT CONVERT(DATETIME, ?, 103)),'3',?,'5',?,?,?,?,?,?,'1',?,'0',?,CURRENT_TIMESTAMP,?,?,CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");


    
    try {
      String coOtros = remitenteDocExtRecepBean.getCoOtros();
      if (coOtros == null || coOtros.trim().length() <= 0) {
        coOtros = null;
      }

      
      SimpleJdbcCall simpleJdbcCall = (new SimpleJdbcCall(this.dataSource)).withProcedureName("[IDOSGD].[PK_SGD_UTILS_SEC_REMITOS_NU_EMI]").withoutProcedureColumnMetaDataAccess().declareParameters(new SqlParameter[] { (SqlParameter)new SqlOutParameter("COD_NU_EMI", 4) });
      
      Map out = simpleJdbcCall.execute(new Object[0]);
      int codigo = ((Integer)out.get("COD_NU_EMI")).intValue();
      String snuEmi = String.format("%010d", new Object[] { Integer.valueOf(codigo) });
      
      documentoExtRecepBean.setNuEmi(snuEmi);
      
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { documentoExtRecepBean.getNuAnn(), snuEmi, expedienteBean.getNuCorrExp(), remitenteDocExtRecepBean
            .getCoLocEmi(), remitenteDocExtRecepBean.getCoDepEmi(), remitenteDocExtRecepBean.getCoEmpEmi(), remitenteDocExtRecepBean.getCoEmpRes(), remitenteDocExtRecepBean
            .getTiEmi(), remitenteDocExtRecepBean.getNuDni(), remitenteDocExtRecepBean.getNuRuc(), coOtros, expedienteBean.getFeExp(), documentoExtRecepBean
            .getDeAsu(), documentoExtRecepBean.getNuDiaAte(), documentoExtRecepBean.getCoTipDocAdm(), documentoExtRecepBean.getNuCorDoc(), documentoExtRecepBean.getDeDocSig(), expedienteBean
            .getNuAnnExp(), expedienteBean.getNuSecExp(), documentoExtRecepBean.getNuFolios(), documentoExtRecepBean.getCoUseMod(), documentoExtRecepBean.getCoUseMod(), remitenteDocExtRecepBean.getCoDep(), remitenteDocExtRecepBean
            .getDeDireccion(), remitenteDocExtRecepBean.getNotificado(), remitenteDocExtRecepBean.getDeCorreo(), remitenteDocExtRecepBean.getTelefono(), remitenteDocExtRecepBean
            .getIdDepartamento(), remitenteDocExtRecepBean.getIdProvincia(), remitenteDocExtRecepBean.getIdDistrito(), remitenteDocExtRecepBean.getEmiResp(), remitenteDocExtRecepBean.getNuDniRes(), remitenteDocExtRecepBean
            .getCoOtrosRes(), remitenteDocExtRecepBean.getDeCargo(), documentoExtRecepBean.getCoOriDoc(), documentoExtRecepBean.getNroDniTramitante(), documentoExtRecepBean
            .getDeObservacion(), documentoExtRecepBean.getCoTraDest(), remitenteDocExtRecepBean.getCoComision(), remitenteDocExtRecepBean.getCoTipoCongresista(), remitenteDocExtRecepBean.getCoTipoInv(), remitenteDocExtRecepBean
            .getReiterativo(), documentoExtRecepBean.getnSobre(), documentoExtRecepBean.getAnioSobre(), documentoExtRecepBean.getSensible() });

      
      vReturn = "OK";
    } catch (DuplicateKeyException con) {
      vReturn = "Numero de Documento Duplicado.";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }


  
  public String insDestinatarioDocumentoEmi(String nuAnn, String nuEmi, DestinatarioDocumentoEmiBean destinatarioDocumentoEmiBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("INSERT INTO IDOSGD.TDTV_DESTINOS(\nNU_ANN,\nNU_EMI,\nNU_DES,\nCO_LOC_DES,\nCO_DEP_DES,\nTI_DES,\nCO_EMP_DES,\nCO_PRI,\nDE_PRO,\nCO_MOT, \nES_ELI,\nFE_USE_CRE,\nFE_USE_MOD,\nCO_USE_MOD,\nCO_USE_CRE,\nES_DOC_REC,\nES_ENV_POR_TRA)\nVALUES(?,?,?,?,?,?,?,?,?,?,'0',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,?,?,'0','0')");


    
    try {
      String vnuDes = (String)this.jdbcTemplate.queryForObject("select ISNULL(MAX(a.nu_des) + 1,1) FROM IDOSGD.tdtv_destinos a where \nA.NU_ANN=? and\nA.NU_EMI=?", String.class, new Object[] { nuAnn, nuEmi });

      
      destinatarioDocumentoEmiBean.setNuDes(vnuDes);
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { nuAnn, nuEmi, vnuDes, destinatarioDocumentoEmiBean.getCoLocal(), destinatarioDocumentoEmiBean.getCoDependencia(), destinatarioDocumentoEmiBean.getCoTipoDestino(), destinatarioDocumentoEmiBean
            .getCoEmpleado(), destinatarioDocumentoEmiBean.getCoPrioridad(), destinatarioDocumentoEmiBean.getDeIndicaciones(), destinatarioDocumentoEmiBean.getCoTramite(), destinatarioDocumentoEmiBean
            
            .getCoUseCre(), destinatarioDocumentoEmiBean.getCoUseCre() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 

    
    return vReturn;
  }

  
  public String insReferenciaDocumentoEmi(String pnuAnn, String pnuEmi, ReferenciaDocExtRecepBean ref) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("INSERT INTO IDOSGD.tdtr_referencia(\nNU_ANN,\nNU_EMI,\nCO_REF,\nNU_ANN_REF,\nNU_EMI_REF,\nNU_DES_REF,\nCO_USE_CRE,\nFE_USE_CRE)\nVALUES(?,?,?,?,?,?,?,CURRENT_TIMESTAMP)");

   
    try {
      SimpleJdbcCall simpleJdbcCall = (new SimpleJdbcCall(this.dataSource)).withProcedureName("[IDOSGD].[PK_SGD_UTILS_SEC_REFERENCIA_CO_REF]").withoutProcedureColumnMetaDataAccess().declareParameters(new SqlParameter[] { (SqlParameter)new SqlOutParameter("COD_NU_ANN_REF", 4) });
      Map out = simpleJdbcCall.execute(new Object[0]);
      int codigo = ((Integer)out.get("COD_NU_ANN_REF")).intValue();
      String cod_num_ann_ref = String.format("%010d", new Object[] { Integer.valueOf(codigo) });

      
      ref.setCoRef(cod_num_ann_ref);
      String nuDes = ref.getNuDes();
      if (nuDes == null || nuDes.trim().length() <= 0) {
        nuDes = null;
      }
      
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { pnuAnn, pnuEmi, cod_num_ann_ref, ref.getNuAnn(), ref.getNuEmi(), nuDes, ref
            .getCoUseCre() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }


  
  public String getNroCorrelativoDocumento(String pnuAnn, String pcoDepEmi, String ptiEmi) {
    String vReturn = "NO_OK";
    StringBuffer sqlQry = new StringBuffer();
    sqlQry.append("select ISNULL(MAX(nu_cor_doc), 0) + 1 ");
    sqlQry.append("from IDOSGD.tdtv_remitos ");
    sqlQry.append("where nu_ann=? ");
    sqlQry.append("and co_dep_emi=? ");
    sqlQry.append("AND TI_EMI=? ");
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { pnuAnn, pcoDepEmi, ptiEmi });
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = "NO_OK";
    } 
    return vReturn;
  }

  
  public DocumentoExtRecepBean getDocumentoExtRecepNew(String coDependencia) {
    StringBuffer sql = new StringBuffer();
    sql.append("select  SUBSTRING((SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_SIGLA_CORTA(CO_DEPENDENCIA)), 1, 6) DE_DOC_SIG_G, ");
    sql.append("        [IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS_MP]('5', 'TDTV_REMITOS') DE_ES_DOC_EMI_MP, ");
    sql.append("        CO_EMPLEADO co_emp_emi ");
    sql.append("from IDOSGD.rhtm_dependencia ");
    sql.append("where CO_DEPENDENCIA = ? ");
    
    DocumentoExtRecepBean documentoExtRecepBean = null;
    try {
      documentoExtRecepBean = (DocumentoExtRecepBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoExtRecepBean.class), new Object[] { coDependencia });
    }
    catch (EmptyResultDataAccessException e) {
      documentoExtRecepBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return documentoExtRecepBean;
  }

  
  public List<MotivoBean> getLstMotivoxTipoDocumento() {
    StringBuffer sql = new StringBuffer();
    List<MotivoBean> list = new ArrayList<MotivoBean>();
    
    sql.append("SELECT  DE_MOT, ");
    sql.append("        CO_MOT ");
    sql.append("FROM IDOSGD.TDTR_MOTIVO ");
    sql.append("order by 1 ");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(MotivoBean.class), new Object[0]);
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<ReferenciaDocExtRecepBean> getLstDocExtReferencia(String pnuAnn, String pcoTiDoc, String pcoDepEmi, String pnuExpediente) {
    StringBuffer sql = new StringBuffer();
    List<ReferenciaDocExtRecepBean> list = new ArrayList<ReferenciaDocExtRecepBean>();
    
    sql.append("SELECT TOP 200  A.NU_ANN, ");
    sql.append("        A.NU_EMI, ");
    sql.append("        A.CO_TIP_DOC_ADM CO_TIP_DOC, ");
    sql.append("        B.NU_EXPEDIENTE, ");
    sql.append("        CONVERT(VARCHAR(10), B.FE_EXP, 3) FE_EXP, ");
    sql.append("        SUBSTRING(a.de_asu, 1, 59) de_asu,a.de_doc_sig ");
    sql.append("FROM IDOSGD.TDTV_REMITOS A  ");
    sql.append("LEFT JOIN\t IDOSGD.TDTC_EXPEDIENTE B ON A.NU_ANN_EXP=B.NU_ANN_EXP AND A.NU_SEC_EXP=B.NU_SEC_EXP AND A.NU_COR_EMI=B.NU_CORR_EXP ");
    sql.append("INNER JOIN  IDOSGD.TDTV_DESTINOS D  ON A.NU_ANN=D.NU_ANN AND A.NU_EMI=D.NU_EMI ");
    sql.append("WHERE  A.NU_ANN=? AND ");
    sql.append("A.CO_TIP_DOC_ADM=? AND ");
    sql.append("A.CO_DEP_EMI=? AND ");
    
    if (pnuExpediente.trim().length() > 0) {
      sql.append("(B.NU_CORR_EXP = '" + pnuExpediente + "' OR A.NU_DOC_EMI = '" + pnuExpediente + "') AND\n");
    }
    
    sql.append("A.ES_DOC_EMI NOT IN ('9','7','5')AND ");
    sql.append("A.TI_EMI NOT IN('05') AND ");
    sql.append("A.CO_GRU IN ('1','3') AND ");
    sql.append("A.NU_DET_EXP =1 AND ");
    sql.append("A.ES_ELI='0' AND ");
    sql.append("B.ES_ESTADO='0' AND ");
    sql.append("D.NU_DES='1' order by 1 desc ");
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ReferenciaDocExtRecepBean.class), new Object[] { pnuAnn, pcoTiDoc, pcoDepEmi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String getNroCorrelativoEmision(String pnuAnn, String pcoDepEmi) {
    String vReturn = "1";
    StringBuffer sqlQry = new StringBuffer();
    sqlQry.append("SELECT ISNULL(MAX(a.NU_COR_EMI), 0) + 1 ");
    sqlQry.append("FROM IDOSGD.TDTV_REMITOS a ");
    sqlQry.append("WHERE NU_ANN = ? ");
    sqlQry.append("AND CO_DEP_EMI=? ");
    sqlQry.append("AND co_gru ='3' ");
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { pnuAnn, pcoDepEmi });
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = "1";
    } 
    return vReturn;
  }

  
  public String updDestinatarioDocumentoEmi(String nuAnn, String nuEmi, DestinatarioDocumentoEmiBean destinatarioDocumentoEmiBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    
    sqlUpd.append("update IDOSGD.TDTV_DESTINOS \nset CO_LOC_DES=?\n,CO_DEP_DES=?\n,CO_EMP_DES=?\n,CO_PRI=?\n,DE_PRO=?\n,CO_MOT=?\n,CO_OTR_ORI_DES=? \n,NU_DNI_DES=? \n,NU_RUC_DES=? \n,FE_USE_MOD=CURRENT_TIMESTAMP\n,CO_USE_MOD=?\nwhere\nNU_ANN=? and\nNU_EMI=? and\nNU_DES=?");


    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { destinatarioDocumentoEmiBean.getCoLocal(), destinatarioDocumentoEmiBean.getCoDependencia(), destinatarioDocumentoEmiBean
            .getCoEmpleado(), destinatarioDocumentoEmiBean.getCoPrioridad(), destinatarioDocumentoEmiBean.getDeIndicaciones(), destinatarioDocumentoEmiBean.getCoTramite(), destinatarioDocumentoEmiBean
            .getCoOtroOrigen(), destinatarioDocumentoEmiBean.getNuDni(), destinatarioDocumentoEmiBean.getNuRuc(), destinatarioDocumentoEmiBean
            .getCoUseCre(), nuAnn, nuEmi, destinatarioDocumentoEmiBean.getNuDes() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String delDestinatarioDocumentoEmi(String nuAnn, String nuEmi, String pnuDes) {
    String vReturn = "NO_OK";
    StringBuffer sqlDel = new StringBuffer();
    sqlDel.append("delete from IDOSGD.TDTV_DESTINOS\nWHERE NU_ANN = ?\nAND NU_EMI = ?\nand NU_DES=?");


    
    try {
      this.jdbcTemplate.update(sqlDel.toString(), new Object[] { nuAnn, nuEmi, pnuDes });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String updReferenciaDocumentoEmi(String pnuAnn, String pnuEmi, ReferenciaDocExtRecepBean ref) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("update IDOSGD.tdtr_referencia \nset NU_ANN_REF = ?, \nNU_EMI_REF = ?, \nNU_DES_REF = ?\nWHERE NU_ANN = ? AND NU_EMI = ? \nAND CO_REF = ?");




    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { ref.getNuAnn(), ref.getNuEmi(), ref.getNuDes(), pnuAnn, pnuEmi, ref
            .getCoRef() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }

  
  public String delReferenciaDocumentoEmi(String pnuAnn, String pnuEmi, String pnuAnnRef, String pnuEmiRef) {
    String vReturn = "NO_OK";
    StringBuffer sqlIns = new StringBuffer();
    sqlIns.append("DELETE FROM IDOSGD.tdtr_referencia\nWHERE NU_ANN = ? AND NU_EMI = ? \nAND NU_ANN_REF = ? AND NU_EMI_REF = ?");


    
    try {
      this.jdbcTemplate.update(sqlIns.toString(), new Object[] { pnuAnn, pnuEmi, pnuAnnRef, pnuEmiRef });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }

  
  public DocumentoExtRecepBean getDocumentoExtRec(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT A.NU_ANN, ");
    sql.append("A.NU_EMI, ");
    sql.append("(SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_SIGLA_CORTA](A.CO_DEP_EMI)) DE_DOC_SIG_G, ");
    sql.append("A.CO_LOC_EMI, ");
    sql.append("A.CO_EMP_EMI, ");
    sql.append("A.CO_DEP_EMI, ");
    sql.append("A.NU_COR_DOC, ");
    sql.append("B.NU_ANN_EXP, ");
    sql.append("B.NU_SEC_EXP, ");
    sql.append("(CONVERT(VARCHAR(10), B.FE_EXP, 103) + ' ' + CONVERT(VARCHAR(5), B.FE_EXP, 108)) FE_EXP, ");
    sql.append("CONVERT(VARCHAR(10), B.FE_VENCE, 103) FE_VENCE, ");
    sql.append("B.NU_CORR_EXP, ");
    sql.append("B.NU_EXPEDIENTE, ");
    sql.append("B.CO_PROCESO, ");
    sql.append("A.ES_DOC_EMI CO_ES_DOC_EMI_MP, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_ESTADOS_MP](A.ES_DOC_EMI, 'TDTV_REMITOS') DE_ES_DOC_EMI_MP, ");
    sql.append("A.TI_EMI, ");
    sql.append("A.NU_DNI_EMI NU_DNI, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](A.NU_DNI_EMI) DE_NU_DNI, ");
    sql.append("A.NU_RUC_EMI NU_RUC, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_PROVEEDOR](A.NU_RUC_EMI) DE_NU_RUC, ");
    sql.append("A.CO_OTR_ORI_EMI CO_OTROS, ");
    sql.append("A.CO_EMP_RES, ");
    sql.append("[IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](A.CO_EMP_RES) DE_EMP_RES, ");
    sql.append("A.CO_TIP_DOC_ADM, ");
    sql.append("A.DE_DOC_SIG, ");
    sql.append("A.NU_FOLIOS, ");
    sql.append("A.DE_ASU, ");
    sql.append("A.NU_DIA_ATE, ");
    sql.append("A.CDIR_REMITE DE_DIRECCION, ISNULL(A.AUT_CORREOE,'0') NOTIFICADO, A.CEXP_CORREOE DE_CORREO, A.CTELEFONO TELEFONO, A.CCOD_PROV ID_PROVINCIA, A.CCOD_DIST ID_DISTRITO,  A.CCOD_DPTO ID_DEPARTAMENTO ,   ");
    sql.append("A.REMI_TI_EMI EMI_RESP, A.REMI_NU_DNI_EMI NU_DNI_RES, IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL(A.REMI_NU_DNI_EMI) DE_NU_DNI_RES,A.REMI_CO_OTR_ORI_EMI CO_OTROS_RES,IDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN (A.REMI_CO_OTR_ORI_EMI) DE_NOM_OTROS_RES, A.REMI_CARGO DE_CARGO, A.CCOD_ORIGING CO_ORI_DOC, A.CNUM_DNIMSG NRO_DNI_TRAMITANTE, A.COBS_DOCUMENTO DE_OBSERVACION, A.CDOC_DESTRAM CO_TRA_DEST,  ");
    sql.append("A.CONG_CO_OTR_ORI CO_COMISION, A.IND_TIPOCONG CO_TIPO_CONGRESISTA, A.IND_TIPOCONGINV CO_TIPO_INV, ISNULL(A.IND_REITCONGINV,'0') REITERATIVO, A.NRO_SOBREAUT NSOBRE, A.AN_SOBREAUT ANIO_SOBRE, B.CCOD_TIPO_EXP CO_TIPO_EXP, ISNULL(A.CIND_SENSIBLE,'0') SENSIBLE ");
    sql.append("FROM IDOSGD.TDTV_REMITOS A ");
    sql.append("left join IDOSGD.TDTC_EXPEDIENTE B ");
    sql.append("ON A.NU_ANN_EXP = B.NU_ANN_EXP AND A.NU_SEC_EXP = B.NU_SEC_EXP ");
    sql.append("WHERE A.NU_ANN=? ");
    sql.append("AND A.NU_EMI=? ");
    
    DocumentoExtRecepBean documentoExtRecepBean = null;
    try {
      documentoExtRecepBean = (DocumentoExtRecepBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoExtRecepBean.class), new Object[] { pnuAnn, pnuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      documentoExtRecepBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return documentoExtRecepBean;
  }

  
  public Map getDesFieldOtro(String pcoOtros) {
    Map mapResult = null;
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT  C.DE_APE_PAT_OTR + ' ' + C.DE_APE_MAT_OTR + ', ' + C.DE_NOM_OTR + ' - ' + C.DE_RAZ_SOC_OTR DE_NOM_OTROS, ");
    sql.append("        ISNULL(B.CELE_DESELE, '   ') DE_DOC_OTROS, ");
    sql.append("        C.NU_DOC_OTR_ORI NU_DOC_OTROS ");
    sql.append("FROM IDOSGD.TDTR_OTRO_ORIGEN C ");
    sql.append("\tLEFT OUTER JOIN ");
    sql.append("   (SELECT CELE_CODELE, CELE_DESELE ");
    sql.append("    FROM IDOSGD.SI_ELEMENTO ");
    sql.append("   WHERE CTAB_CODTAB ='TIP_DOC_IDENT') B ");
    sql.append("   ON C.CO_TIP_OTR_ORI = B.CELE_CODELE ");
    sql.append("WHERE C.CO_OTR_ORI = ? ");
    
    try {
      mapResult = this.jdbcTemplate.queryForMap(sql.toString(), new Object[] { pcoOtros });
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return mapResult;
  }

  
  public List<DestinatarioDocumentoEmiBean> getLstDestinoEmiDoc(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    List<DestinatarioDocumentoEmiBean> list = null;
    sql.append("select  a.nu_ann, ");
    sql.append("        a.nu_emi, ");
    sql.append("        a.nu_des, ");
    sql.append("        a.co_loc_des co_local, ");
    sql.append("        CASE WHEN a.co_loc_des IS NULL THEN NULL ");
    sql.append("\t\t ELSE SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_LOCAL](a.co_loc_des), 1, 100) ");
    sql.append("        END de_local, ");
    sql.append("        a.co_dep_des co_dependencia, ");
    sql.append("        CASE WHEN a.co_dep_des IS NULL THEN NULL ");
    sql.append("\t\t ELSE SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](a.co_dep_des), 1, 100) ");
    sql.append("        END de_dependencia, ");
    sql.append("        a.co_emp_des co_empleado, ");
    sql.append("        CASE WHEN a.co_emp_des IS NULL THEN NULL ");
    sql.append("\t\t ELSE SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](a.co_emp_des), 1, 100) ");
    sql.append("        END de_empleado, ");
    sql.append("        a.co_mot co_tramite, ");
    sql.append("        CASE WHEN a.co_mot IS NULL THEN NULL ");
    sql.append("\t\t ELSE SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_MOTIVO](a.co_mot), 1, 100) ");
    sql.append("        END de_tramite, ");
    sql.append("        a.co_pri co_prioridad, ");
    sql.append("        a.de_pro de_indicaciones, ");
    sql.append("        a.ti_des co_tipo_destino, ");
    sql.append("        'BD' accionBD ");
    sql.append("FROM IDOSGD.tdtv_destinos a ");
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
  
  public List<ReferenciaDocExtRecepBean> getLstReferenciaDoc(String pnuAnn, String pnuEmi) {
    List<ReferenciaDocExtRecepBean> list;
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT  A.NU_ANN, ");
    sql.append("        A.NU_EMI, ");
    sql.append("        A.CO_TIP_DOC_ADM CO_TIP_DOC, ");
    sql.append("        SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_NU_EXPEDIENTE](A.NU_ANN_EXP, A.NU_SEC_EXP), 1, 20) NU_EXPEDIENTE, ");
    sql.append("        CONVERT(VARCHAR(10), A.FE_EMI, 3) FE_EXP, ");
    sql.append("        'BD' ACCION_BD, ");
    sql.append("        B.CO_REF ");
    sql.append("FROM IDOSGD.TDTV_REMITOS A  ");
    sql.append("INNER JOIN\t IDOSGD.TDTR_REFERENCIA B ON  A.NU_ANN = B.NU_ANN_REF AND A.NU_EMI = B.NU_EMI_REF ");
    sql.append("WHERE B.NU_ANN = ? AND ");
    sql.append("B.NU_EMI = ? AND ");
    sql.append("A.ES_DOC_EMI NOT IN ('9', '7', '5') AND ");
    sql.append("A.NU_DET_EXP =1 AND ");
    sql.append("A.ES_ELI='0' ");
    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ReferenciaDocExtRecepBean.class), new Object[] { pnuAnn, pnuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public DocumentoExtRecepBean getDocumentoExtRecBasic(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT  A.NU_ANN,A.NU_EMI, ");
    sql.append("        A.CO_LOC_EMI, ");
    sql.append("        A.CO_EMP_EMI, ");
    sql.append("        A.CO_DEP_EMI, ");
    sql.append("        A.NU_COR_DOC, ");
    sql.append("        A.ES_DOC_EMI CO_ES_DOC_EMI_MP, ");
    sql.append("        A.TI_EMI, ");
    sql.append("        A.CO_EMP_RES, ");
    sql.append("        A.CO_TIP_DOC_ADM, ");
    sql.append("        A.DE_DOC_SIG, ");
    sql.append("        A.NU_FOLIOS, ");
    sql.append("        A.NU_ANN_EXP, ");
    sql.append("        A.NU_SEC_EXP ");
    sql.append("FROM IDOSGD.TDTV_REMITOS A ");
    sql.append("WHERE A.NU_ANN=? ");
    sql.append("AND A.NU_EMI=? ");
    sql.append("AND CO_GRU='3' ");
    sql.append("AND ES_ELI='0' ");
    sql.append("AND NU_DET_EXP='1' ");
    sql.append("AND ES_DOC_EMI <> '9' ");
    
    DocumentoExtRecepBean documentoExtRecepBean = null;
    try {
      documentoExtRecepBean = (DocumentoExtRecepBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoExtRecepBean.class), new Object[] { pnuAnn, pnuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      documentoExtRecepBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return documentoExtRecepBean;
  }

  
  public String updEstadoDocumentoExt(DocumentoExtRecepBean documentoExtRecepBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    
    sqlUpd.append("update IDOSGD.tdtv_remitos \nset CO_USE_MOD=? \n,FE_USE_MOD=CURRENT_TIMESTAMP \n,FE_EMI=CURRENT_TIMESTAMP \n,ES_DOC_EMI=? \nWHERE NU_ANN=? \nAND NU_EMI=? ");

  
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { documentoExtRecepBean.getCoUseMod(), documentoExtRecepBean
            .getCoEsDocEmiMp(), documentoExtRecepBean.getNuAnn(), documentoExtRecepBean.getNuEmi() });
      vReturn = "OK";
    } catch (DuplicateKeyException con) {
      
      vReturn = "Numero de Documento Duplicado.";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    return vReturn;
  }

  
  public String anularDocumentoExtRecep(DocumentoExtRecepBean documentoExtRecepBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlQry = new StringBuffer();
    sqlQry.append("SELECT ISNULL(MAX(nu_cor_doc), 1) + 1\nFROM IDOSGD.tdtv_remitos\nWHERE nu_ann         = ?\nAND ti_emi         = ?\nAND co_dep_emi     = ?\nAND co_tip_doc_adm = ?");

  
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("update IDOSGD.tdtv_remitos \nset es_doc_emi = ?,\nnu_cor_doc = ?,fe_use_mod=CURRENT_TIMESTAMP,co_use_mod=? \nWHERE nu_ann = ?\nAND nu_emi = ?\nAND es_eli = '0'");

   
    try {
      String snuCorDoc = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { documentoExtRecepBean.getNuAnn(), documentoExtRecepBean.getTiEmi(), documentoExtRecepBean
            .getCoDepEmi(), documentoExtRecepBean.getCoTipDocAdm() });
      
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { documentoExtRecepBean.getCoEsDocEmiMp(), snuCorDoc, documentoExtRecepBean.getCoUseMod(), documentoExtRecepBean
            .getNuAnn(), documentoExtRecepBean.getNuEmi() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    return vReturn;
  }

  
  public ReferenciaDocExtRecepBean getRefAtenderDocExtRec(String pnuAnn, String pnuEmi) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT\tA.NU_ANN, ");
    sql.append("\t\tA.NU_EMI, ");
    sql.append("\t\tSUBSTRING(B.NU_EXPEDIENTE, 1, 20) NU_EXPEDIENTE, ");
    sql.append("\t\tCONVERT(VARCHAR(10), B.FE_EXP, 3) FE_EXP, ");
    sql.append("\t\tA.CO_TIP_DOC_ADM CO_TIP_DOC, ");
    sql.append("\t\t'INS' ACCION_BD ");
    sql.append("FROM IDOSGD.TDTV_REMITOS A  ");
    sql.append("INNER JOIN IDOSGD.TDTC_EXPEDIENTE B ON B.NU_ANN_EXP=A.NU_ANN_EXP AND B.NU_SEC_EXP=A.NU_SEC_EXP ");
    sql.append("WHERE A.NU_ANN = ? ");
    sql.append("AND A.NU_EMI = ? ");
    sql.append("AND A.ES_DOC_EMI NOT IN ('9', '7', '5') ");
    sql.append("AND A.ES_ELI='0' ");
    sql.append("AND B.ES_ESTADO='0' ");
    ReferenciaDocExtRecepBean referenciaDocExtRecepBean = null;
    try {
      referenciaDocExtRecepBean = (ReferenciaDocExtRecepBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ReferenciaDocExtRecepBean.class), new Object[] { pnuAnn, pnuEmi });
    }
    catch (EmptyResultDataAccessException e) {
      referenciaDocExtRecepBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return referenciaDocExtRecepBean;
  }

  
  public String getNumeroExpediente(ExpedienteDocExtRecepBean expedienteBean) {
    String vReturn = "NO_OK";
    StringBuffer sqlQry1 = new StringBuffer();
    sqlQry1.append("select\tRIGHT(REPLICATE('0', 7) + (RTRIM(LTRIM(CAST((ISNULL(MAX(NU_CORR_EXP), 0) + 1) AS VARCHAR)))), 7) nuCorrExp, ");
    sqlQry1.append("\tSUBSTRING((SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_SIGLA_CORTA](?)), 1, 6) deSiglaCorta ");
    sqlQry1.append("from IDOSGD.tdtc_expediente ");
    sqlQry1.append("where nu_ann_exp = ? ");
    sqlQry1.append("and co_dep_exp = ? ");
    sqlQry1.append("and co_gru = '3' ");
    
    try {
      Map mapResult = this.jdbcTemplate.queryForMap(sqlQry1.toString(), new Object[] { expedienteBean.getCoDepEmi(), expedienteBean.getNuAnnExp(), expedienteBean.getCoDepEmi() });
      String snuCorrExp = String.valueOf(mapResult.get("nuCorrExp"));
      String sdeSiglaCorta = String.valueOf(mapResult.get("deSiglaCorta"));
      expedienteBean.setNuCorrExp(snuCorrExp);
      
      expedienteBean.setNuExpediente(expedienteBean.getNuAnnExp() + "-" + snuCorrExp);
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String updFechaExpedienteMP(String coUser, String nuAnnExp, String nuSecExp) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    
    sqlUpd.append("UPDATE IDOSGD.TDTC_EXPEDIENTE \nSET US_MODI_AUDI=? \n,FE_MODI_AUDI=CURRENT_TIMESTAMP \n,FE_EXP=getdate() \nWHERE NU_ANN_EXP=? \nAND NU_SEC_EXP=? ");





    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { coUser, nuAnnExp, nuSecExp });
      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    return vReturn;
  }

  
  public String getPermisoChangeEstadoMP(String coEmp, String coDep) {
    String vReturn = "NO_OK";
    StringBuffer sqlQry = new StringBuffer();
    sqlQry.append("SELECT IN_CAMBIO_EST FROM IDOSGD.TDTR_PERMISO_MP\nWHERE CO_EMP=? AND CO_DEP=?\nAND ES_ELI='0'");

    
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { coEmp, coDep });
    } catch (EmptyResultDataAccessException e) {
      vReturn = "0";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;
  }

  
  public ProcesoExpBean getProcesoExpediente(String pcoProceso) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT NU_PLAZO NU_DIAS_PLAZO, DE_ASUNTO FROM IDOSGD.TDTR_PROCESOS_EXP\nWHERE CO_PROCESO=? AND ES_ESTADO = '1'");
    
    ProcesoExpBean proceso = null;
    try {
      proceso = (ProcesoExpBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ProcesoExpBean.class), new Object[] { pcoProceso });
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return proceso;
  }

  
  public DestinatarioDocumentoEmiBean getEmpleadoDestinoDocExtMp(String pcoDependencia) {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT  A.CO_LOC CO_LOCAL, ");
    sql.append("        [IDOSGD].[PK_SGD_DESCRIPCION_DE_LOCAL](A.CO_LOC) DE_LOCAL, ");
    sql.append("        B.CO_EMPLEADO CO_EMPLEADO, ");
    sql.append("        [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](B.CO_EMPLEADO) DE_EMPLEADO, ");
    sql.append("        [IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](B.CO_DEPENDENCIA) DE_DEPENDENCIA, ");
    sql.append("        B.CO_DEPENDENCIA ");
    sql.append("FROM IDOSGD.SITM_LOCAL_DEPENDENCIA A ");
    sql.append("INNER JOIN\t IDOSGD.RHTM_DEPENDENCIA B ON B.CO_DEPENDENCIA = A.CO_DEP ");
    sql.append("WHERE  B.IN_MESA_PARTES='1' ");
    
    DestinatarioDocumentoEmiBean destino = null;

    
    try {
      destino = (DestinatarioDocumentoEmiBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DestinatarioDocumentoEmiBean.class));
    } catch (EmptyResultDataAccessException e) {
      destino = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return destino;
  }
  
  public List<RemitenteBean> getAllDependencias() {
    List<RemitenteBean> list;
    StringBuilder sql = new StringBuilder();
    
    sql.append("SELECT  DE_DEPENDENCIA descrip, ");
    sql.append("        CO_DEPENDENCIA codDep, ");
    sql.append("        DE_CORTA_DEPEN ");
    sql.append("FROM IDOSGD.RHTM_DEPENDENCIA ");
    sql.append("WHERE co_nivel <> '6' ");
    sql.append("AND IN_BAJA = '0' ");
    sql.append("order by 1 ");


    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(RemitenteBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  public List<RequisitoBean> getAllRequisitoExpediente(String nuAnnExp, String nuSecExp, String codProceso) {
    List<RequisitoBean> list;
    StringBuilder sql = new StringBuilder();
    
    sql.append("SELECT \n(SELECT DE_DESCRIPCION FROM IDOSGD.TDTR_REQUISITOS RQ WHERE RQ.COD_REQ=RQEXP.COD_REQUISITO AND RQ.ES_ESTADO='1') DESCRIPCION,\nRQEXP.IN_REQUISITO_PRESENTE DOC_PRESENTE,\nRQEXP.COD_REQUISITO COD_REQUISITO,\nRQEXP.NU_CORRELATIVO NU_CORRELATIVO\nFROM IDOSGD.TDTX_REQUISITO_EXPEDIENTE RQEXP\nWHERE RQEXP.NU_ANN_EXP=?\nAND RQEXP.NU_SEC_EXP=?\nAND RQEXP.ES_ESTADO='1'");


    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(RequisitoBean.class), new Object[] { nuAnnExp, nuSecExp });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String updReqExpedienteDocExtRec(RequisitoBean req, String nuAnnExp, String nuSecExp, String codProceso, String coUsuario) {
    String vReturn = "NO_OK";
    StringBuilder sqlUpd = new StringBuilder();
    
    sqlUpd.append("UPDATE IDOSGD.TDTX_REQUISITO_EXPEDIENTE ");
    sqlUpd.append("SET IN_REQUISITO_OK=(CASE IN_OBLIGATORIO WHEN '1' THEN (CASE ? WHEN '1' THEN '1' ELSE '0' END) ELSE '1' END), ");
    sqlUpd.append("IN_REQUISITO_PRESENTE=?, ");
    sqlUpd.append("US_MODI_AUDI=?, ");
    sqlUpd.append("FE_MODI_AUDI=CURRENT_TIMESTAMP ");
    sqlUpd.append("WHERE NU_ANN_EXP=? ");
    sqlUpd.append("AND NU_SEC_EXP=? ");
    sqlUpd.append("AND COD_REQUISITO=? ");
    sqlUpd.append("AND COD_PROCESO=? ");
    sqlUpd.append("AND NU_CORRELATIVO=? ");
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { Boolean.valueOf(req.isDocPresente()), Boolean.valueOf(req.isDocPresente()), coUsuario, nuAnnExp, nuSecExp, req
            .getCodRequisito(), codProceso, req.getNuCorrelativo() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    
    return vReturn;
  }

  
  public String getRequisitoPendiente(String nuAnnExp, String nuSecExp) {
    String vReturn = "NO_OK";
    StringBuilder sqlQry = new StringBuilder();
    sqlQry.append("SELECT COUNT(1)\nFROM IDOSGD.TDTX_REQUISITO_EXPEDIENTE\nWHERE NU_ANN_EXP=? AND NU_SEC_EXP=?\nAND IN_REQUISITO_OK='0'\nAND ES_ESTADO='1'");



    
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject(sqlQry.toString(), String.class, new Object[] { nuAnnExp, nuSecExp });
      if (vReturn != null && vReturn.equals("0")) {
        vReturn = "OK";
      }
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String getPkExpDocExtOrigen(String pnuAnn, String pnuEmi) {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT DISTINCT REX.NU_ANN_EXP + REX.NU_SEC_EXP ");
    sql.append("FROM (SELECT NU_ANN, NU_EMI, NU_DES FROM IDOSGD.PK_SGD_DESCRIPCION_ARBOL_SEG(?, ?)) DO  ");
    sql.append("INNER JOIN\t IDOSGD.TDTV_REMITOS RE ON RE.NU_ANN=DO.NU_ANN AND RE.NU_EMI=DO.NU_EMI  ");
    sql.append("INNER JOIN\t IDOSGD.TDTX_REQUISITO_EXPEDIENTE REX ON   REX.NU_ANN_EXP=RE.NU_ANN_EXP AND REX.NU_SEC_EXP=RE.NU_SEC_EXP ");
    sql.append("WHERE  RE.ES_ELI='0' ");
    sql.append("AND RE.CO_GRU='3' ");
    sql.append("AND RE.TI_EMI<>'01' ");
    
    String pkExp = null;
    try {
      pkExp = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { pnuAnn, pnuEmi });
    } catch (EmptyResultDataAccessException e) {
      pkExp = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return pkExp;
  }
  
  public List<RequisitoBean> getAllRequisitoExpediente(String nuAnnExp, String nuSecExp) {
    List<RequisitoBean> list;
    StringBuilder sql = new StringBuilder();
    
    sql.append("SELECT      (SELECT DE_DESCRIPCION ");
    sql.append("\t\tFROM IDOSGD.TDTR_REQUISITOS RQ ");
    sql.append("\t\tWHERE RQ.COD_REQ=RQEXP.COD_REQUISITO AND RQ.ES_ESTADO='1') DESCRIPCION, ");
    sql.append("\t\tRQEXP.IN_REQUISITO_PRESENTE DOC_PRESENTE, ");
    sql.append("\t\tRQEXP.COD_REQUISITO COD_REQUISITO, ");
    sql.append("\t\tRQEXP.NU_CORRELATIVO NU_CORRELATIVO ");
    sql.append("FROM IDOSGD.TDTX_REQUISITO_EXPEDIENTE RQEXP ");
    sql.append("WHERE RQEXP.NU_ANN_EXP=? ");
    sql.append("AND RQEXP.NU_SEC_EXP=? ");
    sql.append("AND RQEXP.ES_ESTADO='1' ");


    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(RequisitoBean.class), new Object[] { nuAnnExp, nuSecExp });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }


  
  public List<DocumentoBean> getListaReporteBusquedaVoucher(DocumentoBean documento) {
    StringBuffer prutaReporte = new StringBuffer();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    List<DocumentoBean> list = null;
    
    prutaReporte.append("SELECT     (EX.NU_ANN_EXP + '-' + EX.NU_CORR_EXP) AS nuExpediente, ");
    prutaReporte.append("\t\t(EX.NU_ANN_EXP + '-' + EX.NU_CORR_EXP) NU_CORR_EXP, ");
    prutaReporte.append("\t\t(SELECT DEP.TITULO_DEP FROM IDOSGD.RHTM_DEPENDENCIA DEP WHERE DEP.CO_DEPENDENCIA=RE.CO_DEP_EMI) DE_DEPENDENCIA, ");
    prutaReporte.append("\t\t(SELECT PAR.DE_PAR FROM IDOSGD.TDTR_PARAMETROS PAR WHERE PAR.CO_PAR='DE_INSTITUCION') DE_INSTITUCION, ");
    prutaReporte.append("\t\tISNULL((SELECT PAR.DE_PAR FROM IDOSGD.TDTR_PARAMETROS PAR WHERE PAR.CO_PAR='ANEXO_MESA_PARTES'), '') ANEXO_MESA_PARTES, ");
    prutaReporte.append("\t\tISNULL((SELECT PAR.DE_PAR FROM IDOSGD.TDTR_PARAMETROS PAR WHERE PAR.CO_PAR='FONO_INSTITUCION'), '') FONO_INSTITUCION, ");
    prutaReporte.append("\t\t(SELECT PAR.DE_PAR FROM IDOSGD.TDTR_PARAMETROS PAR WHERE PAR.CO_PAR='PAG_WEB') PAG_WEB, ");
    prutaReporte.append("\t\tISNULL((SELECT PAR.DE_PAR FROM IDOSGD.TDTR_PARAMETROS PAR WHERE PAR.CO_PAR='DE_MESA_PARTES'), '') DE_MESA_PARTES, ");
    prutaReporte.append("\t\tISNULL((SELECT PAR.DE_PAR FROM IDOSGD.TDTR_PARAMETROS PAR WHERE PAR.CO_PAR='DE_SLOGAN'), '') DE_SLOGAN, ");
    prutaReporte.append("\t\tEX.FE_EXP, (SELECT COD_USER FROM IDOSGD.SEG_USUARIOS1 WHERE CEMP_CODEMP = RE.CO_EMP_RES) COD_USER, ");
    prutaReporte.append("\t\tEX.CCLAVE ");
    prutaReporte.append("FROM IDOSGD.TDTV_REMITOS RE  ");
    prutaReporte.append("INNER JOIN\t  IDOSGD.TDTC_EXPEDIENTE EX ON RE.NU_ANN_EXP=EX.NU_ANN_EXP AND RE.NU_SEC_EXP=EX.NU_SEC_EXP ");
    
    try {
      prutaReporte.append("WHERE RE.NU_ANN = :nu_ann ");
      objectParam.put("nu_ann", documento.getNuAnn());
      prutaReporte.append("AND RE.NU_EMI = :nu_emi ");
      objectParam.put("nu_emi", documento.getNuEmi());
      
      list = this.namedParameterJdbcTemplate.query(prutaReporte.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoBean.class));
    }
    catch (Exception ex) {
      String vResult = "1" + ex.getMessage();
      ex.printStackTrace();
    } 
    return list;
  }


  
  public List<DocumentoExtRecepBean> getListaReporteBusqueda(BuscarDocumentoExtRecepBean buscarDocumentoExtRecepBean) {
    StringBuffer prutaReporte = new StringBuffer();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    List<DocumentoExtRecepBean> list = null;
    boolean bBusqFiltro = false;
    boolean bBusqDep = false;
    
    prutaReporte.append("SELECT  A.NU_COR_EMI, ");
    prutaReporte.append("\t     CONVERT(VARCHAR(20),A.FE_EMI,103)  FE_EMI_CORTA, ");
    prutaReporte.append("\t     (SELECT CDOC_DESDOC ");
    prutaReporte.append("\t     FROM IDOSGD.SI_MAE_TIPO_DOC ");
    prutaReporte.append("\t     WHERE CDOC_TIPDOC = A.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,B.NU_DOC, ");
    prutaReporte.append("\t     (select CELE_DESELE ");
    prutaReporte.append("\t     from IDOSGD.SI_ELEMENTO ");
    prutaReporte.append("\t     WHERE CTAB_CODTAB='TIP_DESTINO' ");
    prutaReporte.append("\t     AND CELE_CODELE=A.TI_EMI) tiRemitente, ");
    prutaReporte.append("\t     IDOSGD.PK_SGD_DESCRIPCION_TI_EMI_EMP(A.NU_ANN, A.NU_EMI) deRemitente, ");
    prutaReporte.append("\t     CASE A.NU_CANDES ");
    prutaReporte.append("\t     \tWHEN 1 THEN (SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_TI_DES_EMP(A.NU_ANN, A.NU_EMI)) ");
    prutaReporte.append("\t     \tELSE (SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_TI_DES_EMP_V(A.NU_ANN, A.NU_EMI)) ");
    prutaReporte.append("\t     END deEmpPro, ");
    prutaReporte.append("\t     A.DE_ASU, ");
    prutaReporte.append("\t     (SELECT DE_EST ");
    prutaReporte.append("\t     FROM IDOSGD.TDTR_ESTADOS ");
    prutaReporte.append("\t     WHERE CO_EST + DE_TAB = A.ES_DOC_EMI + 'TDTV_REMITOS') DE_ES_DOC_EMI, ");
    prutaReporte.append("\t     A.NU_DIA_ATE, ");
    prutaReporte.append("\t     B.NU_EXPEDIENTE, ");
    prutaReporte.append("\t     CONVERT(VARCHAR(20),A.FE_EMI,103)    FE_EXP_VENCE_CORTA, ");
    prutaReporte.append("\t     (SELECT DE_NOMBRE ");
    prutaReporte.append("\t     FROM IDOSGD.TDTR_PROCESOS_EXP ");
    prutaReporte.append("\t     WHERE CO_PROCESO = B.CO_PROCESO_EXP ");
    prutaReporte.append("\t     AND ES_ESTADO='1') DE_PROCESO_EXP, ");
    prutaReporte.append("\t     B.CO_PROCESO_EXP AS coProcesoExp, ");
    prutaReporte.append("\t     A.NU_FOLIOS AS nuFolios, ");
    prutaReporte.append("\t     IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(A.CO_EMP_RES) DE_EMP_REC ");
    prutaReporte.append("FROM IDOSGD.TDTV_REMITOS A With (readuncommitted) ");
    prutaReporte.append("INNER JOIN     IDOSGD.TDTX_REMITOS_RESUMEN B  With (readuncommitted) ON A.NU_ANN=B.NU_ANN AND A.NU_EMI=B.NU_EMI ");
    prutaReporte.append("INNER JOIN     IDOSGD.TDTC_EXPEDIENTE C  With (readuncommitted) ON C.NU_ANN_EXP=A.NU_ANN_EXP AND C.NU_SEC_EXP=A.NU_SEC_EXP ");
    prutaReporte.append("WHERE 1=1 ");
    
    try {
      String pnuAnn = buscarDocumentoExtRecepBean.getCoAnnio();
      if (!buscarDocumentoExtRecepBean.getEsFiltroFecha().equals("1") || !pnuAnn.equals("0"))
      {
        prutaReporte.append(" AND A.NU_ANN = '").append(pnuAnn).append("'");
      }
      prutaReporte.append(" AND A.CO_GRU = '3'");
      prutaReporte.append(" AND A.ES_ELI = '0'");
      
      prutaReporte.append(" AND A.CO_DEP_EMI = '").append(buscarDocumentoExtRecepBean.getCoDepEmi()).append("'");
      
      String auxTipoAcceso = buscarDocumentoExtRecepBean.getTiAcceso();
      String tiAcceso = (auxTipoAcceso != null && (auxTipoAcceso.equals("0") || auxTipoAcceso.equals("1") || auxTipoAcceso.equals("2"))) ? auxTipoAcceso : "1";
      if (tiAcceso.equals("1")) {
        prutaReporte.append(" AND A.CO_EMP_RES = '").append(buscarDocumentoExtRecepBean.getCoEmpleado()).append("'");
      }
      if (tiAcceso.equals("2")) {
    	  
    	  prutaReporte.append(" AND A.CO_DEP = '").append(buscarDocumentoExtRecepBean.getCoDependencia()).append("'");
    	 
          
      	}
      else if (buscarDocumentoExtRecepBean.getInMesaPartes().equals("0")) {
        bBusqDep = true;
        prutaReporte.append(" AND A.CO_DEP = '").append(buscarDocumentoExtRecepBean.getCoDependencia()).append("'");
      } 
      
      String pTipoBusqueda = buscarDocumentoExtRecepBean.getTipoBusqueda();
      
      if (pTipoBusqueda.equals("1") && buscarDocumentoExtRecepBean.isEsIncluyeFiltro()) {
        bBusqFiltro = true;
      }


      
      if (buscarDocumentoExtRecepBean.getCoTipoDoc() != null && buscarDocumentoExtRecepBean.getCoTipoDoc().trim().length() > 0) {
        prutaReporte.append(" AND A.CO_TIP_DOC_ADM = '").append(buscarDocumentoExtRecepBean.getCoTipoDoc()).append("'");
      }
      if (buscarDocumentoExtRecepBean.getCoEstadoDoc() != null && buscarDocumentoExtRecepBean.getCoEstadoDoc().trim().length() > 0) {
        prutaReporte.append(" AND A.ES_DOC_EMI = '").append(buscarDocumentoExtRecepBean.getCoEstadoDoc()).append("'");
      }
      if (buscarDocumentoExtRecepBean.getCoTipoEmisor() != null && buscarDocumentoExtRecepBean.getCoTipoEmisor().trim().length() > 0) {
        prutaReporte.append(" AND A.TI_EMI = '").append(buscarDocumentoExtRecepBean.getCoTipoEmisor()).append("'");
      }
      if (buscarDocumentoExtRecepBean.getCoDepOriRec() != null && buscarDocumentoExtRecepBean.getCoDepOriRec().trim().length() > 0 && !bBusqDep) {
        prutaReporte.append(" AND A.CO_DEP = '").append(buscarDocumentoExtRecepBean.getCoDepOriRec()).append("'");
      }
      
      if (buscarDocumentoExtRecepBean.getCoProceso() != null && buscarDocumentoExtRecepBean.getCoProceso().trim().length() > 0) {
        prutaReporte.append(" AND B.CO_PROCESO_EXP = '").append(buscarDocumentoExtRecepBean.getCoProceso()).append("'");
      }
      if (buscarDocumentoExtRecepBean.getCoDepDestino() != null && buscarDocumentoExtRecepBean.getCoDepDestino().trim().length() > 0) {
        prutaReporte.append(" AND INSTR(B.TI_EMI_DES, '").append(buscarDocumentoExtRecepBean.getCoDepDestino()).append("') > 0 ");
      }
      if (buscarDocumentoExtRecepBean.getEsFiltroFecha() != null && (buscarDocumentoExtRecepBean
        .getEsFiltroFecha().equals("1") || buscarDocumentoExtRecepBean.getEsFiltroFecha().equals("3"))) {
        String vFeEmiIni = buscarDocumentoExtRecepBean.getFeEmiIni();
        String vFeEmiFin = buscarDocumentoExtRecepBean.getFeEmiFin();
        if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin.trim().length() > 0) {
          prutaReporte.append("AND A.FE_EMI BETWEEN CONVERT(DATETIME, '").append(vFeEmiIni).append("', 103) AND CONVERT(DATETIME, '").append(vFeEmiFin).append("', 103) + 0.99999 ");
        }
      } 


      
      if (buscarDocumentoExtRecepBean.getBusNumEmision() != null && buscarDocumentoExtRecepBean.getBusNumEmision().trim().length() > 0) {
        prutaReporte.append(" AND A.NU_COR_EMI = :pnuCorEmi ");
        objectParam.put("pnuCorEmi", Integer.getInteger(buscarDocumentoExtRecepBean.getBusNumEmision()));
      } 
      
      if (buscarDocumentoExtRecepBean.getBusNumDoc() != null && buscarDocumentoExtRecepBean.getBusNumDoc().trim().length() > 1) {
        prutaReporte.append(" AND B.NU_DOC LIKE '%'+:pnuDocEmi+'%' ");
        objectParam.put("pnuDocEmi", buscarDocumentoExtRecepBean.getBusNumDoc());
      } 
      
      if (buscarDocumentoExtRecepBean.getBusNumExpediente() != null && buscarDocumentoExtRecepBean.getBusNumExpediente().trim().length() > 1) {
        prutaReporte.append(" AND B.NU_EXPEDIENTE LIKE '%'+:pnuExpediente+'%' ");
        objectParam.put("pnuExpediente", buscarDocumentoExtRecepBean.getBusNumExpediente());
      } 
      
      if (buscarDocumentoExtRecepBean.getBusAsunto() != null && buscarDocumentoExtRecepBean.getBusAsunto().trim().length() > 1) {    	  
    	  
    	  prutaReporte.append("AND upper (DE_ASU) like '%'+:asunto+'%' ");
    	  objectParam.put("asunto", buscarDocumentoExtRecepBean.getBusAsunto().trim().toUpperCase());
       // prutaReporte.append(" AND CONTAINS(in_busca_texto, '" + BusquedaTextual.getContextMixto(buscarDocumentoExtRecepBean.getBusAsunto()) + "', 1 ) > 1 ");
      }
      if (buscarDocumentoExtRecepBean.getCoTipoExp() != null && !buscarDocumentoExtRecepBean.getCoTipoExp().equals("")) {
       // prutaReporte.append(" AND C.CCOD_TIPO_EXP = :codTipoExp");
      //  objectParam.put("codTipoExp", buscarDocumentoExtRecepBean.getCoTipoExp());
        
        prutaReporte.append(" and exists( select  CCOD_TIPO_EXP from IDOSGD.tdtc_expediente where NU_SEC_EXP=A.NU_SEC_EXP");
        prutaReporte.append(" and NU_ANN_EXP=A.NU_ANN_EXP and CCOD_TIPO_EXP=:codTipoExp)");
    	objectParam.put("codTipoExp", buscarDocumentoExtRecepBean.getCoTipoExp());
      } 
      
      if (buscarDocumentoExtRecepBean.getCoOriDoc() != null && !buscarDocumentoExtRecepBean.getCoOriDoc().equals("")) {
        prutaReporte.append(" AND A.CCOD_ORIGING = :coOriDoc");
        objectParam.put("coOriDoc", buscarDocumentoExtRecepBean.getCoOriDoc());
      } 
      
      if (buscarDocumentoExtRecepBean.getBusResultado().equals("1"))
      {
        if (buscarDocumentoExtRecepBean.getCoTipoPersona().equals("03")) {
          prutaReporte.append(" AND A.NU_DNI_EMI = :pNumDni");
          objectParam.put("pNumDni", buscarDocumentoExtRecepBean.getBusNumDni());
        }
        else if (buscarDocumentoExtRecepBean.getCoTipoPersona().equals("02")) {
          prutaReporte.append(" AND A.NU_RUC_EMI = :pNumRuc");
          objectParam.put("pNumRuc", buscarDocumentoExtRecepBean.getBusNumRuc());
        }
        else if (buscarDocumentoExtRecepBean.getCoTipoPersona().equals("04")) {
          prutaReporte.append(" AND A.CO_OTR_ORI_EMI = :pCoOtr");
          objectParam.put("pCoOtr", buscarDocumentoExtRecepBean.getBusCoOtros());
        } 
      }

      
      prutaReporte.append(" ORDER BY A.NU_COR_EMI DESC ");
      
      list = this.namedParameterJdbcTemplate.query(prutaReporte.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoExtRecepBean.class));
    }
    catch (Exception ex) {
      String vResult = "1" + ex.getMessage();
      ex.printStackTrace();
    } 
    return list;
  }

  
  public String getPassDniPide(String pctabCodTab, String Dni) {
    StringBuffer sql = new StringBuffer();
    sql.append("SELECT CELE_DESCOR\nFROM   IDOSGD.SI_ELEMENTO\nWHERE  CTAB_CODTAB = ? AND CELE_DESELE= ?");


    
    String pkExp = null;
    try {
      pkExp = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { pctabCodTab, Dni });
    } catch (EmptyResultDataAccessException e) {
      pkExp = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return pkExp;
  }
}
