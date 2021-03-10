package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import java.util.Date;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.dao.DocumentoTriggerDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;








@Repository("documentoTriggerDao")
public class DocumentoTriggerDaoImp
  extends SimpleJdbcDaoBase
  implements DocumentoTriggerDao
{
  public String actualizarEstadoAfterEmitirDocumento(String pnuAnn, String pnuEmi, String pcoDepEmi, Date pfeEmi) {
    String mensaje = "NO_OK";


    
    SimpleJdbcCall spActualizaEstado = (new SimpleJdbcCall(this.dataSource)).withProcedureName("[IDOSGD].[PK_SGD_TRAMITE_ACTUALIZA_ESTA_U]").withoutProcedureColumnMetaDataAccess().useInParameterNames(new String[] { "p_nu_ann", "p_nu_emi", "p_dep_ori", "p_fecha" }).declareParameters(new SqlParameter[] { new SqlParameter("p_nu_ann", 12), new SqlParameter("p_nu_emi", 12), new SqlParameter("p_dep_ori", 12), new SqlParameter("p_fecha", 91) });


    MapSqlParameterSource mapSqlParameterSource = (new MapSqlParameterSource()).addValue("p_nu_ann", pnuAnn).addValue("p_nu_emi", pnuEmi).addValue("p_dep_ori", pcoDepEmi).addValue("p_fecha", pfeEmi);
    
    try {
      spActualizaEstado.execute((SqlParameterSource)mapSqlParameterSource);
      mensaje = "OK";
    } catch (Exception ex) {
      mensaje = "NO_OK";
      ex.printStackTrace();
    } 
    return mensaje;
  }
}
