package pe.gob.onpe.tramiteconv.dao.impl.sqlserver;

import org.springframework.stereotype.Repository;
import pe.gob.onpe.tramitedoc.bean.DocumentoFileBean;
import pe.gob.onpe.tramitedoc.dao.EmiDocumentoAltaDao;
import pe.gob.onpe.tramitedoc.dao.SimpleJdbcDaoBase;



@Repository("emiDocumentoAltaDao")
public class EmiDocumentoAltaDaoImp
  extends SimpleJdbcDaoBase
  implements EmiDocumentoAltaDao
{
  public String cargaDocumentoEmi(String coUsu, String pnuAnn, String pnuEmi, DocumentoFileBean pfileAnexo) {
    String vReturn = "NO_OK";
    
    return vReturn;
  }
}
