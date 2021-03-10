package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.CargoEntregaBean;
import pe.gob.onpe.tramitedoc.bean.DetGuiaMesaPartesBean;
import pe.gob.onpe.tramitedoc.bean.DocPedienteEntregaBean;
import pe.gob.onpe.tramitedoc.bean.GuiaMesaPartesBean;
import pe.gob.onpe.tramitedoc.dao.CargoEntregaDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;








@Repository("cargoEntregaDao")
public class CargoEntregaDaoImp
  extends SimpleJdbcDaoBase
  implements CargoEntregaDao
{
  public List<CargoEntregaBean> getCargosEntrega(CargoEntregaBean cargo) {
    List<CargoEntregaBean> list;
    StringBuffer sql = new StringBuffer();
    Map<String, Object> objectParam = new HashMap<String, Object>();


    
    sql.append("SELECT TOP 51 A.*");
    sql.append(" FROM ( ");
    sql.append(" SELECT A.NU_ANN_GUI NU_ANN_GUIA, A.NU_GUI NU_GUIA,A.NU_COR_GUI NU_COR_GUIA,CONVERT(VARCHAR(10), A.FE_GUI_MP, 103) FE_GUIA_MP_CORTA,");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_LOCAL(A.CO_LOC_DES) DE_LOC_DES,");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(A.CO_DEP_DES) DE_DEP_DES,");
    sql.append(" A.ES_GUI_MP ESTADO_GUIA_MP,");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_ESTADOS(A.ES_GUI_MP,'TDTC_GUIA_MP') DE_ESTADO_GUIA_MP");
    sql.append(" FROM IDOSGD.TDTC_GUIA_MP A");
    sql.append(" WHERE");
    sql.append(" A.ES_ELI = '0'");

    
    String pNUAnn = cargo.getNuAnnGuia();
    if (!cargo.getEsFiltroFecha().equals("1") || !pNUAnn.equals("0")) {
      sql.append(" AND A.NU_ANN_GUI = :pNuAnn");
      objectParam.put("pNuAnn", pNUAnn);
    } 
    sql.append(" AND A.CO_DEP_ORI=:pcoDepOri");
    objectParam.put("pcoDepOri", cargo.getCoDepOri());

    
    if (cargo.getCoLocDes() != null && cargo.getCoLocDes().trim().length() > 0) {
      sql.append(" AND A.CO_LOC_DES = :pcoLocDes ");
      objectParam.put("pcoLocDes", cargo.getCoLocDes());
    } 
    if (cargo.getCoDepDes() != null && cargo.getCoDepDes().trim().length() > 0) {
      sql.append(" AND A.CO_DEP_DES = :pcoDepDes ");
      objectParam.put("pcoDepDes", cargo.getCoDepDes());
    } 
    if (cargo.getEstadoGuiaMp() != null && cargo.getEstadoGuiaMp().trim().length() > 0) {
      sql.append(" AND A.ES_GUI_MP = :pestadoGuia ");
      objectParam.put("pestadoGuia", cargo.getEstadoGuiaMp());
    } 
    if (cargo.getEsFiltroFecha() != null && (cargo
      .getEsFiltroFecha().equals("1") || cargo.getEsFiltroFecha().equals("3"))) {
      String vFechaIni = cargo.getFeGuiaIni();
      String vFechaFin = cargo.getFeGuiaFin();
      if (vFechaIni != null && vFechaIni.trim().length() > 0 && vFechaFin != null && vFechaFin
        .trim().length() > 0) {
        sql.append("AND A.FE_GUI_MP BETWEEN CONVERT(DATETIME, :pfechaIni, 103) AND CONVERT(DATETIME, :pfechaFin, 103) + 0.99999 ");
        objectParam.put("pfechaIni", vFechaIni);
        objectParam.put("pfechaFin", vFechaFin);
      } 
    } 

    
    sql.append(") A ");

    
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(CargoEntregaBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }
  
  public List<DocPedienteEntregaBean> getDocsPendienteEntrega(DocPedienteEntregaBean busqDoc) {
    List<DocPedienteEntregaBean> list;
    boolean bBusqFiltro = false;
    StringBuffer sql = new StringBuffer();
    Map<String, Object> objectParam = new HashMap<String, Object>();

    
    sql.append("SELECT TOP 300 A.* FROM"); // por lo mientras
    sql.append("(");
    sql.append(" SELECT A.NU_ANN,A.NU_EMI,B.NU_DES,A.NU_COR_EMI,IDOSGD.PK_SGD_DESCRIPCION_TI_EMI_EMP(A.NU_ANN, A.NU_EMI) DE_ORI_EMI,");
    sql.append(" CONVERT(VARCHAR(10), A.FE_EMI, 103) FE_EMI_CORTA,IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(A.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM,");
    sql.append(" C.NU_DOC,IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(B.CO_DEP_DES) DE_DEP_DES,");
    sql.append(" (");
    sql.append(" CASE ");
    sql.append(" WHEN B.TI_DES='01' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (B.CO_EMP_DES)");
    sql.append(" WHEN B.TI_DES='02' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP (B.NU_RUC_DES)");
    sql.append(" WHEN B.TI_DES='03' THEN IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL (B.NU_DNI_DES)");
    sql.append(" WHEN B.TI_DES='04' THEN IDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN (B.CO_OTR_ORI_DES)");
    sql.append(" END");
    sql.append(")DE_EMP_DES,");
    sql.append(" A.DE_ASU");
    sql.append(" FROM IDOSGD.TDTV_REMITOS A ");
    sql.append(" INNER JOIN IDOSGD.TDTV_DESTINOS B ON B.NU_ANN = A.NU_ANN AND B.NU_EMI = A.NU_EMI ");
    sql.append(" INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN C ON C.NU_ANN = B.NU_ANN AND C.NU_EMI = B.NU_EMI ");
    sql.append(" WHERE 1=1 ");
    
    String pNUAnn = busqDoc.getNuAnn();
    if (!busqDoc.getFiltroFecha().equals("1") || !pNUAnn.equals("0")) {
      sql.append(" AND A.NU_ANN = :pNuAnn");
      
      objectParam.put("pNuAnn", pNUAnn);
    } 
    
    sql.append(" AND A.CO_DEP_EMI = :pcoDepEmi");
    objectParam.put("pcoDepEmi", busqDoc.getCoDepEmi());


    
    sql.append(" AND A.ES_ELI = '0'");
    sql.append(" AND B.ES_ELI = '0'");
    sql.append(" AND A.TI_EMI NOT IN ('01','05')");
    sql.append(" AND A.ES_DOC_EMI NOT IN ('5', '9', '7')");
    sql.append(" AND A.IN_OFICIO = '0'");
    sql.append(" AND B.ES_ENT_MP='0'");

    
    String pTipoBusqueda = busqDoc.getTipoBusqueda();
    if (pTipoBusqueda.equals("1") && busqDoc.isEsIncluyeFiltro()) {
      bBusqFiltro = true;
    }


    
    if (pTipoBusqueda.equals("0") || bBusqFiltro) {
      if (busqDoc.getCoTipDocAdm() != null && busqDoc.getCoTipDocAdm().trim().length() > 0) {
        sql.append(" AND A.CO_TIP_DOC_ADM = :pCoTipDocAdm ");
        objectParam.put("pCoTipDocAdm", busqDoc.getCoTipDocAdm());
      } 
      if (busqDoc.getCoLocDes() != null && busqDoc.getCoLocDes().trim().length() > 0) {
        sql.append(" AND B.CO_LOC_DES = :pCoLocDes ");
        objectParam.put("pCoLocDes", busqDoc.getCoLocDes());
      } 
      if (busqDoc.getCoDepDes() != null && busqDoc.getCoDepDes().trim().length() > 0) {
        sql.append(" AND B.CO_DEP_DES = :pCoDepDes ");
        objectParam.put("pCoDepDes", busqDoc.getCoDepDes());
      } 

  
      if (busqDoc.getFiltroFecha() != null && (busqDoc
        .getFiltroFecha().equals("1") || busqDoc.getFiltroFecha().equals("3"))) {
        String vFeEmiIni = busqDoc.getFeEmiIni();
        String vFeEmiFin = busqDoc.getFeEmiFin();
        if (vFeEmiIni != null && vFeEmiIni.trim().length() > 0 && vFeEmiFin != null && vFeEmiFin
          .trim().length() > 0) {
          sql.append("AND A.FE_EMI BETWEEN CONVERT(DATETIME, :pFeEmiIni, 103) AND CONVERT(DATETIME, :pFeEmiFin, 103) + 0.99999 ");
          objectParam.put("pFeEmiIni", vFeEmiIni);
          objectParam.put("pFeEmiFin", vFeEmiFin);
        } 
      } 
    } 
    
    if (pTipoBusqueda.equals("1")) {
      
      if (busqDoc.getNuCorEmi() != null && busqDoc.getNuCorEmi().trim().length() > 0) {
        sql.append(" AND A.NU_COR_EMI = :pnuCorEmi ");
        objectParam.put("pnuCorEmi", busqDoc.getNuCorEmi());
      } 
      
      if (busqDoc.getNuDoc() != null && busqDoc.getNuDoc().trim().length() > 0) {
        sql.append(" AND UPPER(C.NU_DOC) LIKE '%'+:pnuDoc+'%' ");
        objectParam.put("pnuDoc", busqDoc.getNuDoc());
      } 

      
      if (busqDoc.getDeAsu() != null && busqDoc.getDeAsu().trim().length() > 0) {
        sql.append(" AND UPPER(A.DE_ASU) LIKE '%'+:pDeAsunto+'%' ");
        objectParam.put("pDeAsunto", busqDoc.getDeAsu());
      } 
    } 

    sql.append(") A ");

    
    try {
      list = this.namedParameterJdbcTemplate.query(sql.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(DocPedienteEntregaBean.class));
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String isDocPendienteEnGuiaMp(String pnuAnn, String pnuEmi, String pnuDes) {
    String vReturn = "1";
    try {
    	 StringBuffer sql = new StringBuffer();
    	 sql.append("select count(1) from IDOSGD.TDTD_GUIA_MP GD,IDOSGD.TDTC_GUIA_MP G ");
    	 sql.append(" where GD.NU_ANN_GUI=G.NU_ANN_GUI AND GD.NU_GUI=G.NU_GUI AND GD.NU_ANN_DES=?  ");
    	 sql.append(" AND GD.NU_EMI=? AND GD.NU_DES=? AND G.ES_GUI_MP<>9 AND G.ES_ELI = '0' ");
      vReturn = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { pnuAnn, pnuEmi, pnuDes });

    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public DocPedienteEntregaBean getDocPendienteEntrega(String pnuAnn, String pnuEmi, String pnuDes) {
    StringBuffer sql = new StringBuffer();
    DocPedienteEntregaBean doc = null;
    sql.append("SELECT A.NU_ANN,A.NU_EMI,B.NU_DES,C.NU_EXPEDIENTE,IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(A.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM, ");
    sql.append(" C.NU_DOC,CONVERT(VARCHAR(10), A.FE_EMI, 103) FE_EMI_CORTA,IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(B.CO_DEP_DES) DE_DEP_DES,  ");
    sql.append(" IDOSGD.PK_SGD_DESCRIPCION_TI_EMI_EMP(A.NU_ANN, A.NU_EMI) DE_ORI_EMI FROM IDOSGD.TDTV_REMITOS A   ");
    sql.append(" INNER JOIN  IDOSGD.TDTV_DESTINOS B ON B.NU_ANN=A.NU_ANN AND B.NU_EMI=A.NU_EMI   INNER JOIN  IDOSGD.TDTX_REMITOS_RESUMEN  ");
    sql.append(" C ON C.NU_ANN=B.NU_ANN AND C.NU_EMI=B.NU_EMI  WHERE A.NU_ANN=? AND A.NU_EMI=? AND  B.NU_DES=?  ");
    sql.append("  AND A.TI_EMI NOT IN ('01','05') AND A.ES_DOC_EMI NOT IN ('5', '9', '7')   ");
    sql.append(" AND A.IN_OFICIO = '0' AND A.ES_ELI='0'AND B.ES_ELI='0' AND B.ES_ENT_MP='0' ");
 
    try {
      doc = (DocPedienteEntregaBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocPedienteEntregaBean.class), new Object[] { pnuAnn, pnuEmi, pnuDes });
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return doc;
  }

  
  public String insGuiaMp(GuiaMesaPartesBean guia) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("SET LANGUAGE Español ");
    sqlUpd.append("INSERT INTO IDOSGD.TDTC_GUIA_MP(  NU_ANN_GUI,NU_GUI, CO_LOC_ORI,CO_DEP_ORI, CO_LOC_DES,CO_DEP_DES, ES_GUI_MP,FE_GUI_MP, DE_OBS,NU_COR_GUI, ES_ELI,CO_USE_MOD, FE_USE_MOD,CO_USE_CRE, FE_USE_CRE) VALUES(?,?,?,?,?,?,'0',CONVERT(DATETIME,?),?,?,'0',?,GETDATE(),?,GETDATE())");


    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { guia.getNuAnn(), guia.getNuGuia(), guia.getCoLocOri(), guia.getCoDepOri(), guia
            .getCoLocDes(), guia.getCoDepDes(), guia.getFeGuiMp(), guia.getDeObs(), guia.getNuCorGui(), guia.getCoUseMod(), guia.getCoUseMod() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String updGuiaMp(GuiaMesaPartesBean guia) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    
    sqlUpd.append("UPDATE IDOSGD.TDTC_GUIA_MP SET FE_GUI_MP=CONVERT(DATETIME,?) ,DE_OBS=? ,FE_USE_MOD=GETDATE() ,CO_USE_MOD=? WHERE NU_ANN_GUI=? AND NU_GUI=?");

    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { guia.getFeGuiMp(), guia
            .getDeObs(), guia.getCoUseMod(), guia.getNuAnn(), guia.getNuGuia() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    
    return vReturn;
  }

  
  public String insDetGuiaMp(DetGuiaMesaPartesBean detGuia) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    sqlUpd.append("INSERT INTO IDOSGD.TDTD_GUIA_MP( NU_ANN_GUI,NU_GUI, NU_COR,NU_ANN_DES, NU_EMI,NU_DES, ES_DET_GUI,ES_ELI, FE_USE_MOD,CO_USE_MOD, CO_USE_CRE,FE_USE_CRE) VALUES(?,?,?,?,?,?,'0','0',GETDATE(),?,?,GETDATE())");







    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { detGuia.getNuAnnGui(), detGuia.getNuGui(), detGuia.getNuCor(), detGuia
            .getNuAnn(), detGuia.getNuEmi(), detGuia.getNuDes(), detGuia.getCoUseMod(), detGuia.getCoUseMod() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String getNroGuiaCabecera(String pnuAnnGuia) {
    String vReturn = "-1";
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject("SELECT ISNULL(MAX(convert(INT, NU_GUI)), 0) + 1 FROM IDOSGD.TDTC_GUIA_MP WHERE NU_ANN_GUI=?", String.class, new Object[] { pnuAnnGuia });

    
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String getNroCorrelativoGuiaCabecera(String pnuAnnGuia, String pcoDepOri) {
    String vReturn = "-1";
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject("SELECT ISNULL(MAX(convert(INT, NU_COR_GUI)), 0) + 1 FROM IDOSGD.TDTC_GUIA_MP WHERE NU_ANN_GUI=? and CO_DEP_ORI=?", String.class, new Object[] { pnuAnnGuia, pcoDepOri });


    
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String getNroCorrelativoDetGuiaCabecera(String pnuAnnGuia, String pnuGuia) {
    String vReturn = "-1";
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject("SELECT ISNULL(MAX(convert(INT, NU_COR)), 0) + 1  FROM IDOSGD.TDTD_GUIA_MP WHERE NU_ANN_GUI=? AND NU_GUI=?", String.class, new Object[] { pnuAnnGuia, pnuGuia });


    
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public GuiaMesaPartesBean getGuiaMp(String pnuAnnGuia, String pnuGuia) {
    StringBuffer sql = new StringBuffer();
    GuiaMesaPartesBean guia = null;
    sql.append("SELECT NU_ANN_GUI NU_ANN,NU_GUI NU_GUIA,CO_DEP_ORI,CO_LOC_ORI, CO_LOC_DES,IDOSGD.PK_SGD_DESCRIPCION_DE_LOCAL(CO_LOC_DES) DE_LOC_DES, IDOSGD.PK_SGD_DESCRIPCION_DE_LOCAL(CO_LOC_ORI) DE_LOC_ORI, IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(CO_DEP_DES) DE_DEP_DES, IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(CO_DEP_ORI) DE_DEP_ORI, DE_OBS,CONVERT(VARCHAR(10), FE_GUI_MP, 103) FE_GUI_MP,NU_COR_GUI, ES_GUI_MP ESTADO_GUIA,IDOSGD.PK_SGD_DESCRIPCION_ESTADOS(ES_GUI_MP,'TDTC_GUIA_MP') DE_ESTADO_GUIA FROM IDOSGD.TDTC_GUIA_MP WHERE NU_ANN_GUI=? AND NU_GUI=?");








    
    try {
      guia = (GuiaMesaPartesBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(GuiaMesaPartesBean.class), new Object[] { pnuAnnGuia, pnuGuia });
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return guia;
  }
  
  public List<DocPedienteEntregaBean> getDetalleGuiaMp(String pnuAnnGuia, String pnuGuia) {
    List<DocPedienteEntregaBean> list;
    StringBuffer sql = new StringBuffer();
    
    sql.append("SELECT GD.NU_ANN_DES,GD.NU_EMI,GD.NU_DES,GD.NU_COR NU_COR_EMI,RR.NU_EXPEDIENTE, IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(R.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM, RR.NU_DOC,CONVERT(VARCHAR(10), R.FE_EMI, 103) FE_EMI_CORTA,IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(D.CO_DEP_DES) DE_DEP_DES, IDOSGD.PK_SGD_DESCRIPCION_TI_EMI_EMP(R.NU_ANN, R.NU_EMI) DE_ORI_EMI,GD.ES_DET_GUI ES_DET_GUIA, IDOSGD.PK_SGD_DESCRIPCION_FORMAT(D.FE_REC_DOC,'dd/mm/yyyy HH24:MI') FE_REC_DOC_CORTA FROM IDOSGD.TDTC_GUIA_MP G  INNER JOIN IDOSGD.TDTD_GUIA_MP GD ON GD.NU_ANN_GUI = G.NU_ANN_GUI AND GD.NU_GUI = G.NU_GUI   INNER JOIN IDOSGD.TDTV_REMITOS R ON  R.NU_ANN = GD.NU_ANN_GUI AND R.NU_EMI = GD.NU_EMI   INNER JOIN IDOSGD.TDTV_DESTINOS D  ON GD.NU_DES = D.NU_DES  AND GD.NU_ANN_GUI = D.NU_ANN AND GD.NU_EMI = D.NU_EMI   INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN RR ON  RR.NU_ANN=R.NU_ANN AND RR.NU_EMI=R.NU_EMI  WHERE G.NU_ANN_GUI = ? AND G.NU_GUI = ? AND GD.NU_ANN_GUI=? AND GD.NU_GUI=? AND G.ES_ELI = '0'");













    
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocPedienteEntregaBean.class), new Object[] { pnuAnnGuia, pnuGuia, pnuAnnGuia, pnuGuia });
    }
    catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String updEstadoGuiaMp(String estado, String pcoUseMod, String pnuAnnGuia, String pnuGuia) {
    String vReturn = "NO_OK";
    StringBuffer sqlUpd = new StringBuffer();
    
    sqlUpd.append("UPDATE IDOSGD.TDTC_GUIA_MP SET ES_GUI_MP=? ,FE_USE_MOD=GETDATE() ,CO_USE_MOD=? WHERE NU_ANN_GUI=? AND NU_GUI=?");




    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { estado, pcoUseMod, pnuAnnGuia, pnuGuia });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    
    return vReturn;
  }

  
  public DocPedienteEntregaBean getDependenciaDestinoDocExtRec(String pnuAnn, String pnuEmi, String pnuDes) {
    StringBuffer sql = new StringBuffer();
    DocPedienteEntregaBean doc = null;
    sql.append(" SELECT B.CO_LOC_DES,IDOSGD.PK_SGD_DESCRIPCION_DE_LOCAL(CO_LOC_DES) DE_LOC_DES, B.CO_DEP_DES,IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(B.CO_DEP_DES) DE_DEP_DES FROM IDOSGD.TDTV_REMITOS A INNER JOIN IDOSGD.TDTV_DESTINOS B ON A.NU_ANN= B.NU_ANN AND A.NU_EMI=B.NU_EMI  WHERE A.NU_ANN=? AND A.NU_EMI=?  AND B.NU_DES=? AND A.TI_EMI NOT IN ('01','05') AND A.ES_DOC_EMI NOT IN ('5', '9', '7') AND A.ES_ELI='0' AND B.ES_ELI='0' AND B.ES_ENT_MP='0'");









    
    try {
      doc = (DocPedienteEntregaBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocPedienteEntregaBean.class), new Object[] { pnuAnn, pnuEmi, pnuDes });
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
    return doc;
  }


  
  public List<CargoEntregaBean> getListaReporteBusqueda(CargoEntregaBean cargo) {
    StringBuffer prutaReporte = new StringBuffer();
    Map<String, Object> objectParam = new HashMap<String, Object>();
    List<CargoEntregaBean> list = null;
    
    prutaReporte.append("SELECT\t G.NU_ANN_GUI AS nuAnnGuia, ");
    prutaReporte.append("\t\tG.NU_GUI AS nuGuia, ");
    prutaReporte.append("\t\tG.NU_COR_GUI AS nuCorGuia, ");
    prutaReporte.append("\t\tG.CO_LOC_ORI, ");
    prutaReporte.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_LOCAL](G.CO_LOC_ORI) DE_LOC_ORI, ");
    prutaReporte.append("\t\tG.CO_LOC_DES, ");
    prutaReporte.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_LOCAL](G.CO_LOC_DES) DE_LOC_DES, ");
    prutaReporte.append("\t\tG.CO_DEP_ORI, ");
    prutaReporte.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](G.CO_DEP_ORI) DE_DEP_ORI, ");
    prutaReporte.append("\t\tG.CO_LOC_DES, ");
    prutaReporte.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](G.CO_DEP_DES) DE_DEP_DES_CAB, ");
    prutaReporte.append("\t\tGD.NU_COR, ");
    prutaReporte.append("\t\tRR.NU_EXPEDIENTE, ");
    prutaReporte.append("\t\tCASE R.TI_EMI ");
    prutaReporte.append("\t\t\tWHEN '01' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](R.CO_EMP_EMI) ");
    prutaReporte.append("\t\t\tWHEN '02' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_PROVEEDOR](R.NU_RUC_EMI) + '-' + R.NU_RUC_EMI ");
    prutaReporte.append("\t\t\tWHEN '03' THEN [IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](R.NU_DNI_EMI) + '-' + R.NU_DNI_EMI ");
    prutaReporte.append("\t\t\tWHEN '04' THEN [IDOSGD].[PK_SGD_DESCRIPCION_OTRO_ORIGEN](R.CO_OTR_ORI_EMI) ");
    prutaReporte.append("\t\tEND DE_ORI_EMI, ");
    prutaReporte.append("\t\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](R.CO_TIP_DOC_ADM) DE_TIP_DOC_ADM, ");
    prutaReporte.append("\t\tRR.NU_DOC, ");
    prutaReporte.append("\t\tCASE d.ti_des ");
    prutaReporte.append("\t\t\tWHEN '01' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](d.co_emp_des) ");
    prutaReporte.append("\t\t\tELSE NULL ");
    prutaReporte.append("\t\tEND de_des, ");
    prutaReporte.append("\t\tCASE d.ti_des ");
    prutaReporte.append("\t\t\tWHEN '01' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](d.co_dep_des) ");
    prutaReporte.append("\t\t\tWHEN '02' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_PROVEEDOR](d.nu_ruc_des) + ' -RUC:' + d.nu_ruc_des ");
    prutaReporte.append("\t\t\tWHEN '03' THEN ISNULL([IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](d.nu_dni_des), ");
    prutaReporte.append("\t\t\t\t\t\t\t\t\t(SELECT LTRIM(RTRIM(deapp)) + ' ' + LTRIM(RTRIM(deapm))\t+ ' ' + LTRIM(RTRIM(denom)) ");
    prutaReporte.append("\t\t\t\t\t\t\t\t\t FROM IDOSGD.idtanirs ");
    prutaReporte.append("\t\t\t\t\t\t\t\t\t WHERE nulem = d.nu_dni_des ");
    prutaReporte.append("\t\t\t\t\t\t\t\t )) + ' -DNI:' + d.nu_dni_des ");
    prutaReporte.append("\t\t\tWHEN '04' THEN [IDOSGD].[PK_SGD_DESCRIPCION_OTRO_ORIGEN](d.co_otr_ori_des) ");
    prutaReporte.append("\t\tEND de_dep_des, ");
    prutaReporte.append("\t\tG.FE_GUI_MP ");
    prutaReporte.append(" FROM IDOSGD.TDTC_GUIA_MP G  INNER JOIN IDOSGD.TDTD_GUIA_MP GD ON GD.NU_ANN_GUI=G.NU_ANN_GUI AND GD.NU_GUI=G.NU_GUI   INNER JOIN IDOSGD.TDTV_REMITOS R ON  R.NU_ANN=GD.NU_ANN_DES AND R.NU_EMI=GD.NU_EMI     INNER JOIN IDOSGD.TDTV_DESTINOS D  ON  R.NU_ANN=D.NU_ANN AND R.NU_EMI=D.NU_EMI    INNER JOIN IDOSGD.TDTX_REMITOS_RESUMEN RR ON  RR.NU_ANN=R.NU_ANN  AND RR.NU_EMI=R.NU_EMI  ");


    try {
      prutaReporte.append("WHERE G.NU_ANN_GUI = :num_ani_guia ");
      objectParam.put("num_ani_guia", cargo.getNuAnnGuia());
      prutaReporte.append("AND G.NU_GUI = :num_guia ");
      objectParam.put("num_guia", cargo.getNuGuia());
      prutaReporte.append("AND G.ES_ELI = '0' ");
      prutaReporte.append("ORDER BY GD.NU_COR ");
      
      list = this.namedParameterJdbcTemplate.query(prutaReporte.toString(), objectParam, (RowMapper)BeanPropertyRowMapper.newInstance(CargoEntregaBean.class));
    }
    catch (Exception ex) {
      String vResult = "1" + ex.getMessage();
      ex.printStackTrace();
    } 
    return list;
  }
}
