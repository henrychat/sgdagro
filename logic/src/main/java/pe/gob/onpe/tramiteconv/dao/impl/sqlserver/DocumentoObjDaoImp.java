package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.stereotype.Repository;

import pe.gob.onpe.tramitedoc.bean.DocumentoAnexo;
import pe.gob.onpe.tramitedoc.bean.DocumentoDatoBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoNotificacionBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoObjBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoObjBlock;
import pe.gob.onpe.tramitedoc.dao.DocumentoObjDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;

import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import pe.gob.onpe.tramitedoc.util.Constantes;

@Repository("documentoObjDao")
public class DocumentoObjDaoImp extends SimpleJdbcDaoBase implements DocumentoObjDao {

    private static Logger logger = Logger.getLogger("SGDDocumentoObjDaoImp");

    public DocumentoDatoBean getDatosDoc(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        sql.append("select \n nu_ann,\n nu_emi,\n co_dep_emi,\n co_emp_emi,\n CAST(nu_cor_emi AS VARCHAR) nu_cor_emi, \n ti_emi,\n ti_cap,\n es_doc_emi,\n co_tip_doc_adm co_doc,\n IDOSGD.PK_SGD_DESCRIPCION_DE_DOCUMENTO(co_tip_doc_adm) tipo_Doc,\n COALESCE(nu_doc_emi,'') numero_Doc,\n de_doc_sig siglas_Doc,\n IDOSGD.PK_SGD_DESCRIPCION_FORMAT(fe_emi,'DD-MM-YYYY') fecha_Doc,\n IDOSGD.PK_SGD_DESCRIPCION_FORMAT(GETDATE(),'DD/MM/YYYY') fecha_actual,\n IDOSGD.PK_SGD_DESCRIPCION_FORMAT(GETDATE(),'YYYY-MM-DD') fecha_firma,\n IDOSGD.PK_SGD_DESCRIPCION_INITCAP((SELECT RETORNO FROM IDOSGD.PK_SGD_DESCRIPCION_DE_DISTRITO_LOCAL(co_loc_emi))) de_Lugar\n from IDOSGD.tdtv_remitos WITH (NOLOCK) \n where nu_ann = ?\nand nu_emi = ?");

        DocumentoDatoBean docDatoBean = new DocumentoDatoBean();

        try {
            docDatoBean = (DocumentoDatoBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoDatoBean.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            docDatoBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return docDatoBean;
    }

    public DocumentoObjBean getDatosDoc(String pnuAnn, String pnuEmi, String ptiCap) {
        StringBuilder sql = new StringBuilder();
        sql.append("select \n nu_ann, \n nu_emi, \n de_ruta_origen nombre_Archivo,\n COALESCE(len(bl_doc),0) nu_Tamano\n from IDOSGD.tdtv_archivo_doc WITH (NOLOCK) \n where nu_ann = ?\nand nu_emi = ?");

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

    public DocumentoObjBean getNombreArchivo(String pnuAnn, String pnuEmi, String ptiCap) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select \n nu_ann, \n nu_emi, \n de_ruta_origen nombre_Archivo,\n COALESCE(len(bl_doc),0) nu_Tamano,\nw_de_ruta_origen w_Nombre_Archivo,\nCOALESCE(len(w_bl_doc),0) w_Nu_Tamano \n from IDOSGD.tdtv_archivo_doc WITH (NOLOCK) \n where nu_ann = ?\nand nu_emi = ?");

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

    public String CopiarAnexo(String pnuAnn, String pnuEmi, String pnuAnexo, String pNuAnnDocProyecto, String pnuEmiDocProyecto) {
        StringBuilder sql = new StringBuilder();
        String result = null;
        sql.append("insert into IDOSGD.TDTV_ANEXOS \n (NU_ANN,NU_EMI,NU_ANE,DE_DET,BL_DOC,DE_RUT_ORI,DE_RUT_DES,CO_TIP_DOC,CO_USE_CRE,\n FE_USE_CRE,CO_USE_MOD,FE_USE_MOD,FEULA,REQ_FIRMA,NU_DES,TI_PUBLIC)\n SELECT '" + pNuAnnDocProyecto + "','" + pnuEmiDocProyecto + "',(SELECT COUNT(T.NU_ANE)+1 FROM IDOSGD.TDTV_ANEXOS T WITH (NOLOCK) where nu_emi='" + pnuEmiDocProyecto + "' and nu_ann='" + pNuAnnDocProyecto + "'),DE_DET,BL_DOC,DE_RUT_ORI,DE_RUT_DES,CO_TIP_DOC,CO_USE_CRE,\n  FE_USE_CRE,CO_USE_MOD,FE_USE_MOD,FEULA,REQ_FIRMA,NU_DES,TI_PUBLIC\n  FROM IDOSGD.TDTV_ANEXOS WITH (NOLOCK) \n  where nu_emi='" + pnuEmi + "' and nu_ann='" + pnuAnn + "' and nu_ane='" + pnuAnexo + "'");

        try {
            result = (String) this.jdbcTemplate.queryForObject(sql.toString(), String.class);
        } catch (EmptyResultDataAccessException e) {
            result = "S";
        } catch (Exception e) {
            result = "N";
            e.printStackTrace();
        }

        return result;
    }

    public String CopiarAnexo(String pnuAnn, String pnuEmi, String pnuAnexo, String pNuAnnDocProyecto, String pnuEmiDocProyecto, final String codUsu) {
        String vReturn = "NO_OK";

        StringBuilder sqlQry1 = new StringBuilder();
        sqlQry1.append(" SELECT '" + pNuAnnDocProyecto + "' nuAnn,'" + pnuEmiDocProyecto + "' nuEmi,(SELECT max(T.NU_ANE)+1 FROM IDOSGD.TDTV_ANEXOS T WITH (NOLOCK)  ");
        sqlQry1.append(" where nu_emi='" + pnuEmiDocProyecto + "' and nu_ann='" + pNuAnnDocProyecto + "') nuAne, ");
        sqlQry1.append(" DE_DET,BL_DOC blDoc ,DE_RUT_ORI,DE_RUT_DES,CO_TIP_DOC,CO_USE_CRE,FE_USE_CRE,CO_USE_MOD, FEULA,REQ_FIRMA, ");
        sqlQry1.append(" NU_DES,TI_PUBLIC  FROM IDOSGD.TDTV_ANEXOS WITH (NOLOCK) where nu_emi='" + pnuEmi + "' and nu_ann='" + pnuAnn + "' and nu_ane='" + pnuAnexo + "'");

        final DocumentoAnexo docAnexo;

        //datosUsuario= this.jdbcTemplate.queryForObject(sql.toString(), BeanPropertyRowMapper.newInstance(DatosUsuario.class), usuario.getCoUsuario(),coAplicativo );
        docAnexo = this.jdbcTemplate.queryForObject(sqlQry1.toString(), BeanPropertyRowMapper.newInstance(DocumentoAnexo.class));
        if (docAnexo.getNuAne() == null) {
            docAnexo.setNuAne("1");
        }

        final InputStream archivoAnexo = new ByteArrayInputStream(docAnexo.getBlDoc());
        final int size = (docAnexo.getBlDoc()).length;
        int maxUploadSize = 10000000;

        StringBuilder sqlIns = new StringBuilder();
        // sqlIns.append("insert into IDOSGD.TDTV_ANEXOS  (NU_ANN,NU_EMI,NU_ANE,DE_DET,BL_DOC,DE_RUT_ORI,DE_RUT_DES,CO_TIP_DOC,CO_USE_CRE, FE_USE_CRE,CO_USE_MOD,FE_USE_MOD,FEULA,REQ_FIRMA,NU_DES,TI_PUBLIC)");
        sqlIns.append("insert into IDOSGD.tdtv_anexos(nu_ann,nu_emi,nu_ane,de_det,de_rut_ori,co_use_cre,fe_use_cre,co_use_mod,fe_use_mod,feula, nu_des, bl_doc ) values(?,?,?,?,?,?,GETDATE(),?,null,CONVERT(VARCHAR(20),GETDATE(),112),?,?)");

        DefaultLobHandler defaultLobHandler = new DefaultLobHandler();

        try {
            this.jdbcTemplate.execute(sqlIns.toString(), (PreparedStatementCallback) new AbstractLobCreatingPreparedStatementCallback((LobHandler) defaultLobHandler) {
                protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                    ps.setString(1, docAnexo.getNuAnn());
                    ps.setString(2, docAnexo.getNuEmi());
                    ps.setString(3, docAnexo.getNuAne());
                    ps.setString(4, docAnexo.getDeDet());
                    ps.setString(5, docAnexo.getDeRutOri());
                    ps.setString(6, docAnexo.getCoUseCre());
                    ps.setString(7, codUsu);
                    ps.setString(8, docAnexo.getNuDes());
                    lobCreator.setBlobAsBinaryStream(ps, 9, archivoAnexo, size);
                }
            });
            vReturn = "S";
        } catch (Exception e) {
            vReturn = "N";
            e.printStackTrace();
        }

        return vReturn;

    }

    public DocumentoObjBean leerDocumento(String pnuAnn, String pnuEmi, String ptiCap) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select \n nu_ann, \n nu_emi, \n de_ruta_origen ,\n bl_doc \n from IDOSGD.tdtv_archivo_doc WITH (NOLOCK) \nwhere nu_ann = ?\nand nu_emi = ?");

        DocumentoObjBean docObjBean = new DocumentoObjBean();

        try {
            docObjBean = (DocumentoObjBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) new DocumentoRowMapper(docObjBean), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            docObjBean = null;
        } catch (Exception e) {
            StringBuilder mensaje = new StringBuilder();
            mensaje.append("DocBlob:" + pnuAnn + "." + pnuEmi + "." + ptiCap);
            logger.error(mensaje, e);
        }

        return docObjBean;
    }

    public List<DocumentoObjBean> leerDocumentoAnexo(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        sql.append("select nu_ann,nu_emi,nu_ane,de_rut_ori nombre_Archivo,bl_doc documento from IDOSGD.tdtv_anexos WITH (NOLOCK) where nu_ann = ? and nu_emi = ? ");

        List<DocumentoObjBean> docObjBean = new ArrayList<DocumentoObjBean>();

        try {
            docObjBean = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoObjBean.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            docObjBean = null;
        } catch (Exception e) {
            StringBuilder mensaje = new StringBuilder();
            mensaje.append("Anexo:" + pnuAnn + "." + pnuEmi);
            logger.error(mensaje, e);
        }

        return docObjBean;
    }

    public DocumentoObjBean leerDocumento(String pnuAnn, String pnuEmi, String ptiCap, String pAbreWord) {
        StringBuilder sql = new StringBuilder();
        sql.append("select \nnu_ann, \nnu_emi, \nw_de_ruta_origen de_ruta_origen ,\nw_bl_doc bl_doc \nfrom IDOSGD.tdtv_archivo_doc WITH (NOLOCK) \nwhere nu_ann = ?\nand nu_emi = ?");

        DocumentoObjBean docObjBean = new DocumentoObjBean();

        try {
            docObjBean = (DocumentoObjBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) new DocumentoRowMapper(docObjBean), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            docObjBean = null;
        } catch (Exception e) {
            StringBuilder mensaje = new StringBuilder();
            mensaje.append("DocBlob:" + pnuAnn + "." + pnuEmi + "." + ptiCap);
            logger.error(mensaje, e);
        }

        return docObjBean;
    }

    private class DocumentoRowMapper
            implements ParameterizedRowMapper<DocumentoObjBean> {

        private DocumentoObjBean docObjBean;

        public DocumentoRowMapper() {
        }

        public DocumentoRowMapper(DocumentoObjBean docObjBean) {
            this.docObjBean = docObjBean;
        }

        public DocumentoObjBean mapRow(ResultSet rs, int i) throws SQLException {
            int size = 0;
            try {
                Blob documento = rs.getBlob("bl_doc");
                if (documento != null) {

                    this.docObjBean.setNuAnn(rs.getString("nu_ann"));
                    this.docObjBean.setNuEmi(rs.getString("nu_emi"));
                    this.docObjBean.setNombreArchivo(rs.getString("de_ruta_origen"));
                    InputStream blobStream = null;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        int bytesRead;
                        blobStream = documento.getBinaryStream();
                        byte[] buffer = new byte[Constantes.BUFFER_SIZE];

                        do {
                            bytesRead = blobStream.read(buffer);
                            if (bytesRead <= 0) {
                                continue;
                            }
                            baos.write(buffer, 0, bytesRead);
                        } while (bytesRead > -1);
                        baos.flush();
                        this.docObjBean.setDocumento(baos.toByteArray());
                    } finally {
                        if (blobStream != null) {
                            blobStream.close();
                        }
                        if (baos != null) {
                            baos.close();
                        }
                    }
                }
                documento.free();
            } catch (Exception e) {
                StringBuilder mensaje = new StringBuilder();
                mensaje.append("LeerBlob:" + this.docObjBean.getNuAnn() + "." + this.docObjBean.getNuEmi());
                logger.error(mensaje, e);
            }

            return this.docObjBean;
        }
    }

    public DocumentoObjBean getNombreArchivoAnexo(String pnuAnn, String pnuEmi, String pnuAnexo) {
        StringBuilder sql = new StringBuilder();
        sql.append("\tselect \n nu_ann,\n nu_emi,\n nu_ane,\n de_rut_ori nombre_Archivo,\n COALESCE(LEN(bl_doc),0) nu_Tamano\n from IDOSGD.tdtv_anexos WITH (NOLOCK) \nwhere nu_ann = ?\nand nu_emi = ?\nand nu_ane = ?");

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

    public DocumentoObjBean getNombreArchivoAnexoArchivado(String pnuAnn, String pnuEmi, String pnuAnexo) {
        StringBuilder sql = new StringBuilder();
        sql.append("\tselect \n nu_ann,\n nu_emi,\n nu_ane,\n de_rut_ori nombre_Archivo,\n COALESCE(LEN(bl_doc),0) nu_Tamano\n from IDOSGD.tdtv_anexos WITH (NOLOCK) \nwhere nu_ann = ?\nand nu_emi = ?\nand nu_ane = ?");

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

    public DocumentoObjBean leerDocumentoAnexo(String pnuAnn, String pnuEmi, String pnuAne) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select \n nu_ann,\n nu_emi,\n nu_ane,\n de_rut_ori de_ruta_origen,\n bl_doc\n from IDOSGD.tdtv_anexos WITH (NOLOCK) \n where nu_ann = ?\n and nu_emi = ?\n and nu_ane = ?");

        DocumentoObjBean docObjBean = new DocumentoObjBean();

        try {
            docObjBean = (DocumentoObjBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) new DocumentoRowMapper(docObjBean), new Object[]{pnuAnn, pnuEmi, pnuAne});
        } catch (EmptyResultDataAccessException e) {
            docObjBean = null;
        } catch (Exception e) {
            StringBuilder mensaje = new StringBuilder();
            mensaje.append("Anexo:" + pnuAnn + "." + pnuEmi + "." + pnuAne);
            logger.error(mensaje, e);
        }

        return docObjBean;
    }

    public String getInFirmaDoc(String pcoDep, String pcoTipoDoc) {
        StringBuilder sql = new StringBuilder();
        String result = null;
        sql.append(" SELECT (\n CASE es_obl_firma when '0' THEN 'N'\n ELSE 'F' END\n )  FROM IDOSGD.sitm_doc_dependencia \n WHERE co_dep     = ?\n AND co_tip_doc = ?");

        try {
            result = (String) this.jdbcTemplate.queryForObject(sql.toString(), String.class, new Object[]{pcoDep, pcoTipoDoc});
        } catch (EmptyResultDataAccessException e) {
            result = "F";
        } catch (Exception e) {
            result = "F";
            e.printStackTrace();
        }

        return result;
    }

    public String insRemitosBlock(DocumentoObjBlock docBlock) {
        String vReturn = "NO_OK";
        StringBuilder sqlUpd = new StringBuilder();
        sqlUpd.append("INSERT INTO IDOSGD.TDTV_REMITOS_BLOCK(NU_ANN,NU_EMI,NU_SEC_FIRMA,\nFECHA_FIRMA,TIPO_FIRMA)\nVALUES(?,?,?,\nGETDATE(),?)");

        try {
            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{docBlock.getNuAnn(), docBlock.getNuEmi(), docBlock.getNuSecFirma(), docBlock.getTipoFirma()});
            vReturn = "OK";
        } catch (DuplicateKeyException con) {
            vReturn = "Datos Duplicado INSERT IDOSGD.TDTV_REMITOS_BLOCK.";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vReturn;
    }

    public String updRemitosBlock(DocumentoObjBlock docBlock) {
        String vReturn = "NO_OK";
        StringBuilder sqlUpd = new StringBuilder();

        sqlUpd.append("UPDATE IDOSGD.TDTV_REMITOS_BLOCK \n SET NU_SEC_FIRMA=?\n,FECHA_FIRMA=getdate() \n,TIPO_FIRMA=?\n WHERE\n NU_ANN=? and\n NU_EMI=?");

        try {
            this.jdbcTemplate.update(sqlUpd.toString(), new Object[]{docBlock.getNuSecFirma(), docBlock.getTipoFirma(), docBlock.getNuAnn(), docBlock.getNuEmi()});
            vReturn = "OK";
        } catch (Exception e) {
            e.printStackTrace();
            vReturn = e.getMessage();
        }
        return vReturn;
    }

    public DocumentoObjBlock getDatosDocBlock(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        DocumentoObjBlock docBlock = null;
        sql.append(" SELECT RB.NU_ANN,RB.NU_EMI,RB.NU_SEC_FIRMA,\n RB.FECHA_FIRMA,RB.TIPO_FIRMA,getdate() FECHA_ACTUAL\n FROM IDOSGD.TDTV_REMITOS_BLOCK RB\n WHERE RB.NU_ANN=?\n AND RB.NU_EMI=?");

        try {
            docBlock = (DocumentoObjBlock) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoObjBlock.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            docBlock = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return docBlock;
    }

    public DocumentoDatoBean CargarCabeceraReporte(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](a.co_dep_emi) de_dep_emi,\n\ta.co_dep_emi,\n\ta.co_tip_doc_adm,\n\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](a.co_tip_doc_adm) de_tip_doc_adm ,\n\tA.NU_ANN,A.DE_DOC_SIG,\n\ta.nu_dia_ate,\n\tLTRIM(RTRIM(SUBSTRING(a.de_asu, 1, 400))) de_asu,\n\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_NU_EXPEDIENTE](A.NU_ANN_EXP, A.NU_SEC_EXP) NU_EXPEDIENTE,\n\tCONVERT(VARCHAR(10), FE_EMI, 103) FECHA,\n\tCAST(ISNULL(NU_DOC, 0) AS VARCHAR) NU_DOC,\n\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](a.co_emp_emi) de_emp_emi,\n\t(SELECT RETORNO FROM [IDOSGD].[PK_SGD_DESCRIPCION_DE_CARGO_DOCUMENTO](a.co_dep_emi,a.co_emp_emi)) cargo_emp_emi,\nCOALESCE((SELECT DE_PAR FROM IDOSGD.TDTR_PARAMETROS WHERE CO_PAR = 'SIGLA_INSTITUCION'),' ') sigla_Institucion \n FROM IDOSGD.tdtv_remitos a WITH (NOLOCK) \nWHERE a.es_eli='0'\nAND a.nu_ann = ?\nAND a.nu_emi = ?");

        DocumentoDatoBean docDatoBean = new DocumentoDatoBean();

        try {
            docDatoBean = (DocumentoDatoBean) this.jdbcTemplate.queryForObject(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoDatoBean.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            docDatoBean = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return docDatoBean;
    }

    public List<DocumentoDatoBean> CargarSubReporte1(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        sql.append("select    a.nu_ann NU_ANN_R,\n\ta.nu_emi NU_EMI_R,\n\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](b.co_tip_doc_adm) + ' N' +\n\tCASE B.TI_EMI\n\t\tWHEN '01' THEN B.NU_DOC_EMI + '-' + B.NU_ANN + '-' + B.DE_DOC_SIG\n\t\tWHEN '05' THEN B.NU_DOC_EMI + '-' + B.NU_ANN + '-' + B.DE_DOC_SIG\n\t\tELSE B.DE_DOC_SIG\n\tEND DOCUMENTO,\n\tb.fe_emi FE_EMI_R,\n\tLTRIM(RTRIM(SUBSTRING(b.de_asu, 1, 200))) DE_ASU_R\nfrom IDOSGD.TDTR_REFERENCIA a WITH (NOLOCK) \n INNER JOIN  IDOSGD.TDTV_REMITOS b WITH (NOLOCK) ON a.nu_emi_ref = b.nu_emi and a.nu_ann_ref = b.nu_ann \nwhere  a.nu_ann= ?\nand a.nu_emi= ?");

        List<DocumentoDatoBean> list = new ArrayList<DocumentoDatoBean>();

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoDatoBean.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<DocumentoDatoBean> CargarSubReporte2(String pnuAnn, String pnuEmi) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT    a.nu_ann,\n\ta.nu_emi,\n\tb.nu_des,\n\ta.nu_cor_emi,a.co_dep_emi,\n\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](a.co_dep_emi) de_dep_emi,\n\ta.co_emp_emi,\n\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](a.co_emp_emi) de_emp_emi,\n\ta.fe_emi,\n\ta.co_tip_doc_adm,\n\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_DOCUMENTO](a.co_tip_doc_adm) de_tip_doc_adm,\n\tCASE A.TI_EMI\n\t\tWHEN '01' THEN A.NU_DOC_EMI + '-' + A.NU_ANN + '-' + A.DE_DOC_SIG\n\t\tWHEN '05' THEN A.NU_DOC_EMI + '-' + A.NU_ANN + '-' + A.DE_DOC_SIG\n\t\tELSE A.DE_DOC_SIG\n\tEND NU_DOC,\n\ta.nu_dia_ate,\n\tSUBSTRING(a.de_asu,1,400) de_asu,\n\tb.co_dep_des,\n\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](b.co_dep_des) de_dep_des,\n\tb.co_emp_des,\n\tCASE b.ti_des\n\t\tWHEN '01' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](b.co_emp_des)\n\t\tWHEN '02' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_PROVEEDOR](B.NU_RUC_DES)\n\t\tWHEN '03' THEN [IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](b.Nu_Dni_Des)\n\t\tWHEN '04' THEN [IDOSGD].[PK_SGD_DESCRIPCION_OTRO_ORIGEN](b.co_otr_ori_des)\n\tEND de_emp_des,\n\tCASE b.CO_EMP_DES\n\t\tWHEN (SELECT CO_EMPLEADO FROM IDOSGD.RHTM_DEPENDENCIA WHERE CO_DEPENDENCIA = b.CO_DEP_DES)\n\t\t\tTHEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](b.co_dep_des)\n\t\tELSE [IDOSGD].[PK_SGD_DESCRIPCION_DE_DEPENDENCIA](b.co_dep_des) + CHAR(10) + (\n\t\t\t\tCASE b.ti_des\n\t\t\t\t\tWHEN '01' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_NOM_EMP](b.co_emp_des)\n\t\t\t\t\tWHEN '02' THEN [IDOSGD].[PK_SGD_DESCRIPCION_DE_PROVEEDOR](B.NU_RUC_DES)\n\t\t\t\t\tWHEN '03' THEN [IDOSGD].[PK_SGD_DESCRIPCION_ANI_SIMIL](b.Nu_Dni_Des)\n\t\t\t\t\tWHEN '04' THEN [IDOSGD].[PK_SGD_DESCRIPCION_OTRO_ORIGEN](b.co_otr_ori_des)\n\t\t\t\tEND)\n\tEND DE_DEP_EMP,\n\t[IDOSGD].[PK_SGD_DESCRIPCION_MOTIVO](b.co_mot) de_mot,\n\t(SELECT DE_PRI FROM IDOSGD.TDTR_PRIORIDAD WHERE CO_PRI = b.co_pri) de_pri,\n\tb.de_pro,\n\t[IDOSGD].[PK_SGD_DESCRIPCION_DE_NU_EXPEDIENTE](A.NU_ANN_EXP,A.NU_SEC_EXP) NU_EXPEDIENTE \n FROM IDOSGD.tdtv_remitos a WITH (NOLOCK) \n INNER JOIN    IDOSGD.tdtv_destinos b WITH (NOLOCK) ON a.nu_ann = b.nu_ann AND a.nu_emi = b.nu_emi  \nWHERE a.es_eli = '0'\nAND b.es_eli = '0'\nAND a.nu_ann = ?\nAND a.nu_emi = ?\norder by 3");

        List<DocumentoDatoBean> list = new ArrayList<DocumentoDatoBean>();

        try {
            list = this.jdbcTemplate.query(sql.toString(), (RowMapper) BeanPropertyRowMapper.newInstance(DocumentoDatoBean.class), new Object[]{pnuAnn, pnuEmi});
        } catch (EmptyResultDataAccessException e) {
            list = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public DocumentoObjBean leerActaNotificacion(String pnuAnn, String pnuEmi, String pnuDes, String pnuActa) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DocumentoNotificacionBean> CargarReporteNotificacion(String pnuAnn, String pnuEmi, String tiOpe) {
        // TODO Auto-generated method stub
        return null;
    }

}
