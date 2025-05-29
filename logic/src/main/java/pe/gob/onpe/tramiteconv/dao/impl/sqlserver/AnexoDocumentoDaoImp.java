package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.DocumentoAnexoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoObjBean;
import pe.gob.onpe.tramitedoc.bean.ReferenciaBean;
import pe.gob.onpe.tramitedoc.dao.AnexoDocumentoDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;
import pe.gob.onpe.tramitedoc.web.util.ApplicationProperties;
import pe.gob.onpe.tramitedoc.web.util.Utilidades;


@Repository("anexoDocumentoDao")
public class AnexoDocumentoDaoImp   extends SimpleJdbcDaoBase   implements AnexoDocumentoDao
{

  @Autowired
  private ApplicationProperties applicationProperties;

  private SimpleJdbcCall spdataSource;
  private static Logger logger = Logger.getLogger("AnexoDocumentoDaoImp");

  public List<ReferenciaBean> getDocumentosReferencia(String pnuAnn, String pnuEmi) {
     StringBuilder sql = new StringBuilder();
    sql.append(" SELECT  IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(a.co_tip_doc_adm) li_tip_doc, ");
    sql.append("        (CASE WHEN (a.ti_emi='01' OR a.ti_emi='05') THEN ISNULL(a.nu_doc_emi,' ') + '-' + a.nu_ann + '-' + a.de_doc_sig ");
    sql.append("    ELSE ISNULL(a.de_doc_sig,'S/N') ");
    sql.append("         END) li_nu_doc, ");
    sql.append("        SUBSTRING( ");
    sql.append("        (CASE a.ti_emi ");
    sql.append("  WHEN '01' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(a.co_dep_emi) ");
    sql.append("  WHEN '02' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_PROVEEDOR(a.nu_ruc_emi) ");
    sql.append("  WHEN '03' THEN 'CIUDADANO - ' + IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL(a.nu_dni_emi) ");
    sql.append("  WHEN '04' THEN IDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN(a.co_otr_ori_emi) ");
    sql.append("  WHEN '05' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(a.co_emp_emi) ");
    sql.append("  ELSE ' ' ");
    sql.append("         END) ");
    sql.append("        , 1, 100) de_dep_emi, ");
    sql.append("        convert(varchar, a.fe_emi, 103) fe_emi_corta, ");
    sql.append("        b.nu_ann, ");
    sql.append("        b.nu_emi, ");
    sql.append("        ISNULL(LTRIM(RTRIM(CAST(b.nu_des AS VARCHAR))), 'N') nu_des, ");
    sql.append("        b.nu_ann_ref, ");
    sql.append("        b.nu_emi_ref, ");
    sql.append("        ISNULL(LTRIM(RTRIM(CAST(b.nu_des_ref AS VARCHAR))), 'N') nu_des_ref,ISNULL(a.de_asu,'') as deAsu,a.CO_TIP_DOC_ADM  as coTipDocAdm ");
    sql.append(" FROM IDOSGD.tdtv_remitos a WITH (NOLOCK) ");
    sql.append(" INNER JOIN  IDOSGD.TDTR_REFERENCIA b WITH (NOLOCK) ON a.nu_ann = b.nu_ann_ref AND a.nu_emi = b.nu_emi_ref  ");
    sql.append(" WHERE  b.nu_ann = ? ");
    sql.append(" and b.nu_emi = ? ");
    
    List<ReferenciaBean> list = new ArrayList<ReferenciaBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ReferenciaBean.class), new Object[] { pnuAnn, pnuEmi });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }


  
  public List<ReferenciaBean> getDocumentoEmi(String pnuAnn, String pnuEmi) {
     StringBuilder sql = new StringBuilder();
    sql.append(" SELECT  IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO (a.co_tip_doc_adm) li_tip_doc, ");
    sql.append("        (CASE WHEN (a.ti_emi='01' OR a.ti_emi='05') THEN ISNULL(a.nu_doc_emi,' ') + '-' + a.nu_ann + '-' + a.de_doc_sig ");
    sql.append("    ELSE ISNULL(a.de_doc_sig, 'S/N') ");
    sql.append("         END) li_nu_doc, ");
    sql.append("        SUBSTRING( ");
    sql.append("  (CASE a.ti_emi ");
    sql.append("                WHEN '01' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_DEPENDENCIA(a.co_dep_emi) ");
    sql.append("                WHEN '02' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_PROVEEDOR(a.nu_ruc_emi) ");
    sql.append("                WHEN '03' THEN 'CIUDADANO - ' + IDOSGD.PK_SGD_DESCRIPCION_ANI_SIMIL(a.nu_dni_emi) ");
    sql.append("                WHEN '04' THEN IDOSGD.PK_SGD_DESCRIPCION_OTRO_ORIGEN(a.co_otr_ori_emi) ");
    sql.append("                WHEN '05' THEN IDOSGD.PK_SGD_DESCRIPCION_DE_NOM_EMP(a.co_emp_emi) ");
    sql.append("                ELSE ' ' ");
    sql.append("   END) ");
    sql.append("        ,1 , 100) de_dep_emi, ");
    sql.append("        convert(varchar(10), a.fe_emi, 103) fe_emi_corta, ");
    sql.append("        a.nu_ann, ");
    sql.append("        a.nu_emi, ");
    sql.append("        'N' nu_des, ");
    sql.append("        a.nu_ann nu_ann_ref, ");
    sql.append("        a.nu_emi nu_emi_ref, ");
    sql.append("        'N'  nu_des_ref ");
    sql.append(" FROM IDOSGD.tdtv_remitos a WITH (NOLOCK) ");
    sql.append(" WHERE a.nu_ann = ? ");
    sql.append(" and a.nu_emi = ? ");
    
    List<ReferenciaBean> list = new ArrayList<ReferenciaBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ReferenciaBean.class), new Object[] { pnuAnn, pnuEmi });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<ReferenciaBean> getDocumentosSeguimiento(String pnuAnn, String pnuEmi, String pnuDes) {
     StringBuilder sql = new StringBuilder();
    sql.append(" SELECT  [IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](b.co_tip_doc_adm) li_tip_doc, ");
    sql.append("        CASE b.ti_emi ");
    sql.append("  WHEN '01' THEN COALESCE(b.nu_doc_emi,'') + '-' + COALESCE(b.nu_ann,'') + '-' + COALESCE(b.de_doc_sig, '') ");
    sql.append("  WHEN '05' THEN COALESCE(b.nu_doc_emi,'') + '-' + COALESCE(b.nu_ann,'') + '-' + COALESCE(b.de_doc_sig, '') ");
    sql.append("  ELSE ISNULL(b.de_doc_sig, 'S/N') ");
    sql.append("        END li_nu_doc, ");
    sql.append("        SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](b.co_dep_emi), 1, 100) de_dep_emi, ");
    sql.append("        SUBSTRING( ");
    sql.append("            CASE c.ti_des ");
    sql.append("                WHEN '01' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](c.co_dep_des) ");
    sql.append("                WHEN '02' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_PROVEEDOR](c.nu_ruc_des) ");
    sql.append("                WHEN '03' THEN 'CIUDADANO - ' + [IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](c.nu_dni_des) ");
    sql.append("                WHEN '04' THEN [IDOSGD].[PK_SGD_DESCRIPCION_OTRO_ORIGEN](c.co_otr_ori_des) ");
    sql.append("                ELSE ' ' ");
    sql.append("            END ");
    sql.append("        , 1, 100) de_dep_des, ");
    sql.append("        CONVERT(VARCHAR(10), b.fe_emi, 103) fe_emi_corta, ");
    sql.append("        c.nu_ann, ");
    sql.append("        c.nu_emi, ");
    sql.append("        CAST(c.nu_des AS VARCHAR) nu_des, ");
    sql.append("        a.nu_ann_ref, ");
    sql.append("        a.nu_emi_ref ");
    sql.append(" FROM IDOSGD.tdtr_referencia a WITH (NOLOCK) ");
    sql.append(" INNER JOIN  IDOSGD.tdtv_remitos b WITH (NOLOCK) ON a.nu_ann = b.nu_ann and a.nu_emi = b.nu_emi ");
    sql.append(" INNER JOIN  IDOSGD.tdtv_destinos c WITH (NOLOCK) ON b.nu_ann = c.nu_ann and b.nu_emi = c.nu_emi ");
    sql.append(" WHERE  b.es_eli = '0' ");
    sql.append(" and b.es_doc_emi <> '9' ");
    sql.append(" and a.nu_ann_ref = ? ");
    sql.append(" and a.nu_emi_ref = ? ");
    sql.append(" and a.nu_des_ref = ? ");
    
    List<ReferenciaBean> list = new ArrayList<ReferenciaBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ReferenciaBean.class), new Object[] { pnuAnn, pnuEmi, pnuDes });
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public List<ReferenciaBean> getDocumentoEmiSeg(String pnuAnn, String pnuEmi, String pnuDes) {
     StringBuilder sql = new StringBuilder();
    Object[] objectParam = null;
    
    sql.append(" SELECT  [IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](b.co_tip_doc_adm) li_tip_doc, ");
    sql.append("        CASE b.ti_emi ");
    sql.append("  WHEN '01' THEN COALESCE(b.nu_doc_emi,'') + '-' + COALESCE(b.nu_ann,'') + '-' + COALESCE(b.de_doc_sig, '') ");
    sql.append("  WHEN '05' THEN COALESCE(b.nu_doc_emi,'') + '-' + COALESCE(b.nu_ann,'') + '-' + COALESCE(b.de_doc_sig, '') ");
    sql.append("  ELSE ISNULL(b.de_doc_sig, 'S/N') ");
    sql.append("        END li_nu_doc, ");
    sql.append("        SUBSTRING([IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](b.co_dep_emi), 1, 100) de_dep_emi, ");
    sql.append("        CASE c.ti_des ");
    sql.append("  WHEN '01' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](c.co_dep_des) ");
    sql.append("  WHEN '02' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_PROVEEDOR](c.nu_ruc_des) ");
    sql.append("  WHEN '03' THEN 'CIUDADANO - ' + [IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](c.nu_dni_des) ");
    sql.append("  WHEN '04' THEN [IDOSGD].[PK_SGD_DESCRIPCION_OTRO_ORIGEN](c.co_otr_ori_des) ");
    sql.append("  ELSE ' ' ");
    sql.append("        END  de_dep_des, ");
    sql.append("        CONVERT(VARCHAR(10), b.fe_emi, 103) fe_emi_corta, ");
    sql.append("        c.nu_ann, ");
    sql.append("        c.nu_emi, ");
    sql.append("        CAST(c.nu_des AS VARCHAR) nu_des ");
    sql.append(" FROM IDOSGD.tdtv_remitos b WITH (NOLOCK)  ");
    sql.append(" INNER JOIN  IDOSGD.tdtv_destinos c WITH (NOLOCK) ON b.nu_ann = c.nu_ann and b.nu_emi = c.nu_emi  ");
    sql.append(" WHERE  b.es_eli = '0'  and c.es_eli='0' ");
    sql.append(" and b.es_doc_emi <> '9' ");
    sql.append(" and b.nu_ann = ? ");
    sql.append(" and b.nu_emi = ? ");
    
    if (pnuDes.equals("N")) {
      objectParam = new Object[] { pnuAnn, pnuEmi };
    } else {
      
      sql.append(" and c.nu_des = ? ");
      objectParam = new Object[] { pnuAnn, pnuEmi, pnuDes };
    } 
    
    List<ReferenciaBean> list = new ArrayList<ReferenciaBean>();
    try {
      list = this.jdbcTemplate.query(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(ReferenciaBean.class), objectParam);
    } catch (EmptyResultDataAccessException e) {
      list = null;
    } catch (Exception e) {
      list = null;
      e.printStackTrace();
    } 
    return list;
  }

  
  public String getUltimoAnexo(String pnuAnn, String pnuEmi) {
    String result = null;
    
     StringBuilder sql = new StringBuilder();
    sql.append(" select CAST(ISNULL(max(nu_ane),0) + 1 AS VARCHAR(10)) nu_ane  from IDOSGD.tdtv_anexos WITH (NOLOCK) where nu_ann = ?  and nu_emi = ? ");


    try {
      result = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { pnuAnn, pnuEmi });
    } catch (Exception e) {
      result = "1";
      e.printStackTrace();
    } 
    return result;
  }

  
  @SuppressWarnings({ "rawtypes", "unchecked" })
public String updArchivoAnexo(final DocumentoAnexoBean docAnexo, final InputStream archivoAnexo, final int size) {
    String vReturn = "NO_OK";
    
     StringBuilder sql = new StringBuilder();
    sql.append("update IDOSGD.tdtv_anexos set ti_public=?,nu_ann=?,nu_emi=?, nu_ane=?, de_det=?, de_rut_ori=?, co_use_cre=?, ");
    sql.append(" fe_use_cre=CURRENT_TIMESTAMP, co_use_mod=?, fe_use_mod=CURRENT_TIMESTAMP, ");
    sql.append(" feula=convert(varchar, GETDATE(), 103) where  nu_ann=? and nu_emi=? and nu_ane=? ");


    DefaultLobHandler defaultLobHandler = new DefaultLobHandler();


    String nombreArchivo = Utilidades.fn_getCleanFileName(docAnexo.getDeDet());
    //3HENRY
    String baseRuta = this.applicationProperties.getRutaTemporal2(); // ejemplo: /glassfish/tmppcm
    String subRuta = docAnexo.getNuAnn() + "/" + docAnexo.getNuEmi(); // ejemplo: 2025/0000006429
    //nombreArchivo = docObjBean.getNombreArchivo();

    String rutaDestino = baseRuta + "/" + subRuta + "/" + nombreArchivo;

    File directorio = new File(baseRuta + "/" + subRuta);
    if (!directorio.exists()) {
      directorio.mkdirs(); // crea toda la estructura intermedia
    }

    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(rutaDestino);
      byte[] buffer = new byte[4096];
      int bytesRead;

      while ((bytesRead = archivoAnexo.read(buffer)) != -1) {
        fos.write(buffer, bytesRead == -1 ? 0 : 0, bytesRead);
      }

      fos.flush();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (fos != null) fos.close();
        if (archivoAnexo != null) archivoAnexo.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    try {
      this.jdbcTemplate.execute(sql.toString(), (PreparedStatementCallback)new AbstractLobCreatingPreparedStatementCallback((LobHandler)defaultLobHandler)
          {
            protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException
            {
              ps.setString(1, docAnexo.getTiPublic());
              ps.setString(2, docAnexo.getNuAnn());
              ps.setString(3, docAnexo.getNuEmi());
              ps.setString(4, docAnexo.getNuAne());
              ps.setString(5, Utilidades.fn_getCleanFileName(docAnexo.getDeDet()));
              ps.setString(6, Utilidades.fn_getCleanFileName(docAnexo.getDeRutOri()));
              ps.setString(7, docAnexo.getCoUseCre());
              ps.setString(8, docAnexo.getCoUseMod());
              //lobCreator.setBlobAsBinaryStream(ps, 9, archivoAnexo, size);
              ps.setString(9, docAnexo.getNuAnn());
              ps.setString(10, docAnexo.getNuEmi());
              ps.setString(11, docAnexo.getNuAne());
            }
          });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
  
  public String delArchivoAnexo(DocumentoAnexoBean docAnexo) {
    String vReturn = "NO_OK";
    
     StringBuilder sqlDel = new StringBuilder();
    sqlDel.append("delete from IDOSGD.tdtv_anexos WHERE NU_ANN = ? AND NU_EMI = ? and nu_ane=CAST(? AS NUMERIC(2,0))");

    try {
      this.jdbcTemplate.update(sqlDel.toString(), new Object[] { docAnexo.getNuAnn(), docAnexo.getNuEmi(), docAnexo.getNuAne() });
      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      e.getMessage();
    } 
    return vReturn;
  }
  
  public String delArchivoAnexoDest(String pNuAnn, String pNuEmi, String pNuDes) {
    String vReturn = "NO_OK";
     StringBuilder sqlDel = new StringBuilder();
    sqlDel.append("delete from idosgd.tdtv_anexos WHERE NU_ANN = ? AND NU_EMI = ? and nu_des=?");

 
    try {
      this.jdbcTemplate.update(sqlDel.toString(), new Object[] { pNuAnn, pNuEmi, pNuDes });
      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      e.getMessage();
    } 
    return vReturn;
  }
  
  public String updAnexoDetalle(DocumentoAnexoBean docAnexo) {
    String vReturn = "NO_OK";
     StringBuilder sqlUpd = new StringBuilder();
    sqlUpd.append("update IDOSGD.tdtv_anexos  set ti_public=?,de_det=? ,co_use_mod=? ,fe_use_mod=GETDATE() ,REQ_FIRMA=? where nu_ann=? and nu_emi=? and nu_ane=?");

 
    try {
      String deDet = Utilidades.fn_getCleanFileName(docAnexo.getDeDet());
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { docAnexo.getTiPublic(), deDet, docAnexo.getCoUseMod(), docAnexo.getReqFirma(), docAnexo.getNuAnn(), docAnexo.getNuEmi(), docAnexo.getNuAne() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    
    return vReturn;
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  // HENRY CARGAR
public String insArchivoAnexo(final DocumentoAnexoBean docAnexo, final InputStream archivoAnexo, final int size) {
    String vReturn = "NO_OK";
    
     StringBuilder sql = new StringBuilder();
    sql.append("insert into IDOSGD.tdtv_anexos( nu_ann, nu_emi, nu_ane, de_det, de_rut_ori, co_use_cre, fe_use_cre, co_use_mod, fe_use_mod, feula,  ti_public  ) values(?,?,?,?,?,?,GETDATE(),?,GETDATE(),IDOSGD.PK_SGD_DESCRIPCION_FORMAT(GETDATE(),'yyyymmdd'),'1')");


    DefaultLobHandler defaultLobHandler = new DefaultLobHandler();

    String nombreArchivo = Utilidades.fn_getCleanFileName(docAnexo.getDeDet());

    //5HENRY
    String baseRuta = this.applicationProperties.getRutaTemporal2(); // ejemplo: /glassfish/tmppcm
    String subRuta = docAnexo.getNuAnn() + "/" + docAnexo.getNuEmi(); // ejemplo: 2025/0000006429
    //nombreArchivo = docObjBean.getNombreArchivo();

    String rutaDestino = baseRuta + "/" + subRuta + "/" + nombreArchivo;

    File directorio = new File(baseRuta + "/" + subRuta);
    if (!directorio.exists()) {
      directorio.mkdirs(); // crea toda la estructura intermedia
    }

    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(rutaDestino);
      byte[] buffer = new byte[4096];
      int bytesRead;

      while ((bytesRead = archivoAnexo.read(buffer)) != -1) {
        fos.write(buffer, bytesRead == -1 ? 0 : 0, bytesRead);
      }

      fos.flush();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (fos != null) fos.close();
        if (archivoAnexo != null) archivoAnexo.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    try {
      this.jdbcTemplate.execute(sql.toString(), (PreparedStatementCallback)new AbstractLobCreatingPreparedStatementCallback((LobHandler)defaultLobHandler)
          {
            protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException
            {
              ps.setString(1, docAnexo.getNuAnn());
              ps.setString(2, docAnexo.getNuEmi());
              ps.setString(3, docAnexo.getNuAne());
              ps.setString(4, Utilidades.fn_getCleanFileName(docAnexo.getDeDet()));
              ps.setString(5, Utilidades.fn_getCleanFileName(docAnexo.getDeRutOri()));
              ps.setString(6, docAnexo.getCoUseCre());
              ps.setString(7, docAnexo.getCoUseMod());
              //lobCreator.setBlobAsBinaryStream(ps, 8, archivoAnexo, size);
            }
          });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }


  
  public String getNombreArchivo(String pNuAnn, String pNuEmi, String pNuAne) {
     StringBuilder sql = new StringBuilder();
    String result = "NO_OK";
    sql.append("select de_det from IDOSGD.tdtv_anexos WITH (NOLOCK) where nu_ann=? and nu_emi=? and nu_ane=?");
    
    try {
      result = (String)this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[] { pNuAnn, pNuEmi, pNuAne });
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return result;
  }

  
  public String updExisteAnexo(String pNuAnn, String pNuEmi) {
     StringBuilder sql = new StringBuilder();
    String result = "NO_OK";
    sql.append("update IDOSGD.TDTX_REMITOS_RESUMEN SET IN_EXISTE_ANEXO ='0' where NU_ANN=? and NU_EMI=?");
    
    try {
      this.jdbcTemplate.update(sql.toString(), new Object[] { pNuAnn, pNuEmi });
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return result;
  }

  
  public DocumentoObjBean getPropiedadesArchivo(String pnuAnn, String pnuEmi, String pnuAne) {
     StringBuilder sql = new StringBuilder();
    sql.append("select  nu_ann,  nu_emi,  de_rut_ori nombre_Archivo, UPPER(right(de_rut_ori,3 ))  tipo_doc from IDOSGD.tdtv_anexos WITH (NOLOCK) where nu_ann = ? and nu_emi = ? AND NU_ANE = ?");









    
    DocumentoObjBean docObjBean = new DocumentoObjBean();
    
    try {
      docObjBean = (DocumentoObjBean)this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper)BeanPropertyRowMapper.newInstance(DocumentoObjBean.class), new Object[] { pnuAnn, pnuEmi, pnuAne });
    } catch (EmptyResultDataAccessException e) {
      docObjBean = null;
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return docObjBean;
  }

  
  public String updRemitosResumenInFirmaAnexos(String pFirmaAnexo, String pnuAnn, String pnuEmi) {
    /*String vReturn = "NO_OK";
     StringBuilder sqlUpd = new StringBuilder();
    sqlUpd.append("UPDATE IDOSGD.TDTX_REMITOS_RESUMEN SET IN_FIRMA_ANEXO = ? WHERE NU_ANN = ?  AND NU_EMI = ? ");


    
    try {
      this.jdbcTemplate.update(sqlUpd.toString(), new Object[] { pFirmaAnexo, pnuAnn, pnuEmi });

      
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    
    return vReturn;*/

    /* INICIO
  DEVELOPER:  LARH
  FECHA_MODIFICACION: 07/07/2022
  ACCION: Modificacion de llamado a Query por SP*/

    String vReturn = "NO_OK";
     StringBuilder sqlUpd = new StringBuilder();
    this.spdataSource = new SimpleJdbcCall(this.dataSource).withProcedureName("[IDOSGD].[PK_SGD_TRAMITE_PROCE_REMITOS_RESUMEN_FIRMA_ANEXO_UDP]")
            .withoutProcedureColumnMetaDataAccess()
            .useInParameterNames("p_anio", "p_nuEmi", "p_pFirmaAnexo")
            .declareParameters(
                    new SqlParameter("p_anio", Types.VARCHAR),
                    new SqlParameter("p_nuEmi", Types.VARCHAR),
                    new SqlParameter("p_pFirmaAnexo", Types.VARCHAR));
    SqlParameterSource in = new MapSqlParameterSource()
            .addValue("p_anio", pnuAnn)
            .addValue("p_nuEmi", pnuEmi)
            .addValue("p_pFirmaAnexo", pFirmaAnexo);
    try {
      this.spdataSource.execute(in);
      vReturn = "OK";
      this.spdataSource = null;
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      ex.printStackTrace();
    }

    return vReturn;

  }

  
  public String updArchivoAnexoFirmado(final DocumentoObjBean docObjBean) {
    String vReturn = "NO_OK";
    
    DefaultLobHandler defaultLobHandler = new DefaultLobHandler();
     StringBuilder sql = new StringBuilder();
    sql.append("UPDATE IDOSGD.TDTV_ANEXOS SET BL_DOC=?, CO_USE_MOD=?, FE_USE_MOD=GETDATE(), FEULA=IDOSGD.PK_SGD_DESCRIPCION_FORMAT(GETDATE(),'YYYYMMDD'), REQ_FIRMA='0' WHERE NU_ANN=? AND NU_EMI=? AND NU_ANE=?");

    
    try {
      this.jdbcTemplate.execute(sql.toString(), (PreparedStatementCallback)new AbstractLobCreatingPreparedStatementCallback((LobHandler)defaultLobHandler)
          {
            protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException
            {
              lobCreator.setBlobAsBytes(ps, 1, docObjBean.getDocumento());
              ps.setString(2, docObjBean.getCoUseMod());
              ps.setString(3, docObjBean.getNuAnn());
              ps.setString(4, docObjBean.getNuEmi());
              ps.setString(5, docObjBean.getNuAne());
            }
          });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage().substring(0, 20);
    } 
    
    return vReturn;
  }

  
  public String getCanAnexosReqFirma(String nuAnn, String nuEmi) {
    String vReturn = "NO_OK";
    try {
      vReturn = (String)this.jdbcTemplate.queryForObject("SELECT count(1) from IDOSGD.tdtv_anexos WITH (NOLOCK) where NU_ANN=? AND NU_EMI=? AND REQ_FIRMA='1'", String.class, new Object[] { nuAnn, nuEmi });

    
    }
    catch (Exception e) {
      e.printStackTrace();
      vReturn = e.getMessage();
    } 
    return vReturn;
  }
}
