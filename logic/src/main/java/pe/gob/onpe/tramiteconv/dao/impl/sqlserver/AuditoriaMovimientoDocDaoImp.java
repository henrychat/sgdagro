package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import org.springframework.stereotype.Repository;
import pe.gob.onpe.autentica.model.Usuario;
import pe.gob.onpe.tramitedoc.bean.AudiEstadosMovDocBean;
import pe.gob.onpe.tramitedoc.bean.DocumentoObjBean;
import pe.gob.onpe.tramitedoc.dao.AuditoriaMovimientoDocDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;


@Repository("auditoriaMovimientoDocDao")
public class AuditoriaMovimientoDocDaoImp   extends SimpleJdbcDaoBase   implements AuditoriaMovimientoDocDao
{
  public String audiVisualizaDocumento(DocumentoObjBean docVisualiza, Usuario usu) {
    String vReturn = "NO_OK";
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO IDOSGD.TDTV_VISUALIZA_DOC( NU_ANN, NU_EMI, FE_DML, DE_USER, DE_IPPC, DE_NAMEPC, DE_USERPC ) VALUES(?,?,GETDATE(),?,?,?,?)");


    try {
      int retorno = this.jdbcTemplate.update(sql.toString(), new Object[] { docVisualiza.getNuAnn(), docVisualiza.getNuEmi(), usu.getCoUsuario(), usu.getIpPC(), usu.getNombrePC(), usu.getUsuPc() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }

  
  public String audiEstadoDocumento(AudiEstadosMovDocBean audi) {
    String vReturn = "NO_OK";
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO IDOSGD.tdtv_estados_mov( NU_ANN, NU_EMI, NU_DES, FE_DML, TI_PROCESO, ES_PROCESO, DE_USER, DE_IPPC, DE_NAMEPC, DE_USERPC ) VALUES(?,?,?,CURRENT_TIMESTAMP,?,?,?,?,?,?)");


    try {
       this.jdbcTemplate.update(sql.toString(), new Object[] { audi.getNuAnn(), audi.getNuEmi(), audi.getNuDes(), audi.getTiProceso(), audi.getEsProceso(), audi
            .getDeUser(), audi.getDeIpPc(), audi.getDeNamePc(), audi.getDeUserPc() });
      vReturn = "OK";
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return vReturn;
  }
}
